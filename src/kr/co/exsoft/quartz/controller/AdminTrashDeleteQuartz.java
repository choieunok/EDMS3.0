package kr.co.exsoft.quartz.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.service.DocumentService;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.exception.BizException;
import kr.co.exsoft.quartz.service.QuartzService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;


/**
 * 휴지통 - 시스템 휴지통 자동 삭제처리
 *
 * @author 패키지팀
 * @since 2014. 9. 29.
 * @version 1.0
 * 
 */

public class AdminTrashDeleteQuartz  extends QuartzJob {

	protected static final Log logger = LogFactory.getLog(AdminTrashDeleteQuartz.class);
	protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	protected HashMap<String,Object> strash = new HashMap<String,Object>();
	protected HashMap<String,Object> ptrash = new HashMap<String,Object>();
	protected int delimiter = 10;
	
	@Override
	protected void executeJob(JobExecutionContext context) {

		CommonService commonService = (CommonService)super.getBean("commonService");
		DocumentService documentService = (DocumentService)super.getBean("documentService");
		QuartzService quartzService = (QuartzService)super.getBean("quartzService");
				
		List<HashMap<String,Object>> trashConfig = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> historyUser = new HashMap<String,Object>();
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		long sTime = System.currentTimeMillis();
		
		try {
			
			logger.info("TerminateTrashDeleteQuartz START ="+df.format(sTime));

			// 1 XR_DOCUMENT_HT 사용자 정보 :: 시스템관리자
			historyUser = quartzService.systemUserInfo(param);
			
			// 2. 휴지통관리 정책 정보 가져오기
			param.put("stype",Constant.SYS_TYPE_TRASH);
			trashConfig = commonService.trashConfig(param);
			this.setTrashConfig(trashConfig);
			
			// 3.관리자 휴지통 자동삭제처리 :: 폐기처리
			this.strashDelete(strash,historyUser,commonService,documentService,quartzService);
			
		}catch(Exception e) {			
			logger.error(e.getMessage());								
		}finally{
			
		}
		
		long eTime = System.currentTimeMillis();
		
		logger.info("TerminateTrashDeleteQuartz END ="+df.format(eTime));
	}

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 휴지통(개인/시스템) 정책관리 설정 ..
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setTrashConfig
	 * @param trashConfig
	 * @param ptrash
	 * @param strash void
	 */
	public void setTrashConfig(List<HashMap<String,Object>> trashConfig) {
		
		if(trashConfig != null && trashConfig.size() > 0)	{
			
			for(HashMap<String,Object> map : trashConfig) {
									
				if(map.get("doc_type") != null && map.get("doc_type").toString().equals(Constant.PRIVATE_TRASH)) {
					// 1.1 개인휴지통 정책
					this.ptrash.put("is_use",map.get("is_use"));
					this.ptrash.put("decade",map.get("decade"));						
				}else {
					// 1.2 관리자 휴지통 정책
					this.strash.put("is_use",map.get("is_use"));
					this.strash.put("decade",map.get("decade"));
				}
			}				
		}
		
	}
		
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 시스템 휴지통 완전삭제처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : strashDelete
	 * @param strash
	 * @param historyUser
	 * @param commonService
	 * @param documentService
	 * @param quartzService void
	 */
	public void strashDelete(HashMap<String,Object> strash,HashMap<String,Object> historyUser,CommonService commonService,
			DocumentService documentService,QuartzService quartzService) {
		
		/********************************************************************
		* 	시스템휴지통 처리 :: XR_DOCUMENT_DEL WASTE_DATE
		* 	환경설정 :: XR_DOC_CONFIG DOC_TYPE=STRASH											
		********************************************************************/
		
		List<HashMap<String,Object>> strashList = new ArrayList<HashMap<String,Object>>();
		List<HashMap<String,Object>> strashDelList = new ArrayList<HashMap<String,Object>>();
		
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		SessionVO sessionVO = new SessionVO();		// 빈 세션객체 생성(XR_DOCUMENT_HT 기록을 위해)
		
		long work_idx = 0;
		long successCnt = 0;
		long failCnt = 0;
		long sTime = System.currentTimeMillis();
		
		try {
			
			logger.info("시스템휴지통 처리시작 ="+df.format(sTime));
			
			// 1. 시스템 휴지통 로그 등록처리
			work_idx = commonService.commonNextVal(Constant.COUNTER_ID_BATCH_WORK);
			quartzService.batchWorkWrite(work_idx,Constant.WORK_BATCH,Constant.BATCH_STRASH_DOC);
			
			// 완료 후 Constant.NO => Constant.YES
			if(strash.get("is_use") != null && strash.get("is_use").toString().equals(Constant.YES)) {
				
				// 2.0 시스템휴지통 삭제기준일 적용
				param.put("workType",Constant.STRASH);
				param.put("decade",this.strash.get("decade"));
				
				// 2.1 시스템휴지통 목록 구하기
				strashList = quartzService.batchDocList(param);
				
				// 2.2 시스템휴지통 폐기처리 :: 데이터베이스 처리
				if(strashList != null && strashList.size() > 0) {
					for(int i=0;i<strashList.size();i++)		{
												
						HashMap<String,Object> strashInfo = new HashMap<String,Object>();
						
						strashInfo = (HashMap<String,Object>)strashList.get(i);						
						strashDelList.add(strashInfo);
										
						if( i !=0 && ( i % delimiter == 0 ) ) {								
							try {
								documentService.adminTrashDeleteDoc(strashDelList,historyUser,sessionVO);															
								successCnt = successCnt + strashDelList.size();
							}catch(Exception e){
								failCnt = failCnt + strashDelList.size();
							}
							strashDelList.clear();
							
						}else if(i == (strashList.size()-1) ) {
							try {
								documentService.adminTrashDeleteDoc(strashDelList,historyUser,sessionVO);											
								successCnt = successCnt + strashDelList.size();
							}catch(Exception e){
								failCnt = failCnt + strashDelList.size();
							}
							strashDelList.clear();
						}
										
					}
					
					resultMap.put("message","시스템 휴지통 삭제처리건수 성공건수::"+successCnt + "/ 실패건수::"+failCnt);
				}else {
					resultMap.put("message","시스템 휴지통 삭제처리건수 없음");
				}

			}else {
				resultMap.put("message","시스템 휴지통 자동 비우기 사용안함");
			}			
			
			resultMap.put("work_state",Constant.T);
			
		}catch(BizException e){					
			logger.error(e.getMessage());
			resultMap.put("work_state",Constant.F);
			resultMap.put("message","비지니스 로직 에러");			
		}catch(Exception e) {			
			logger.error(e.getMessage());
			resultMap.put("work_state",Constant.F);
			resultMap.put("message","EXCEPTION ERROR");						
		}finally{
		
			Date now = new Date();				
			resultMap.put("work_idx",work_idx);
			resultMap.put("work_edate",df.format(now));

			try {
				quartzService.batchWorkUpdate(resultMap);
							
			}catch(Exception e){
				logger.error(e.getMessage());
			}
		}
		
		long eTime = System.currentTimeMillis();
		logger.info("시스템휴지통 처리완료 ="+df.format(eTime));
		
	}
	
	
	
}
