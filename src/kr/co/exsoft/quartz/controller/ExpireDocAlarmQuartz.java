package kr.co.exsoft.quartz.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.exsoft.document.service.DocumentService;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.exception.BizException;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.quartz.service.QuartzService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

/**
 * 보존년한이 지난 문서에 대해 만기 처리한다
 *
 * @author 패키지팀
 * @since 2014. 10. 01.
 * @version 1.0
 * [3000][EDMS-REQ-033]	2015-08-25	성예나	 : 만기문서 자동,사전알림 대상자에게 알람쪽지 배치
 * 
 */

//[3000]
public class ExpireDocAlarmQuartz extends QuartzJob {

	protected static final Log logger = LogFactory.getLog(ExpireDocAlarmQuartz.class);
	protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	@Override
	protected void executeJob(JobExecutionContext context) {
		
		CommonService commonService = (CommonService)super.getBean("commonService");
		DocumentService documentService = (DocumentService)super.getBean("documentService");
		QuartzService quartzService = (QuartzService)super.getBean("quartzService");
		
		//만기사전알람,만기문서알람의 배치처리 메세지를 위한 true,false
		boolean expiredComeAlarmList =false;
		boolean expiredDocAlarmList =false;
		
		
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
		
		long work_idx = 0;
		long sTime = System.currentTimeMillis();
		
		try {
			
			logger.info("ExpireDocCheckQuartz START ="+df.format(sTime));
			
			// 1.  XR_BATCHWORK	 에 로그등록처리
			work_idx = commonService.commonNextVal(Constant.COUNTER_ID_BATCH_WORK);
			quartzService.batchWorkWrite(work_idx,Constant.WORK_BATCH,Constant.BATCH_EXPIRED_DOC);		
			
			//2.쪽지등록 프로세스
			//2-1 사전만기문서 알람대상자에게 쪽찌 알림 프로세스
			expiredComeAlarmList = documentService.expiredComeAlarmList();
			//2-2 만기만료문서 알람대상자에게 쪽찌 알림 프로세스
			expiredDocAlarmList = documentService.expireDocAlarmList();
			
			//3. XR_BATCHWORK 에 등록되는 message 처리
			if(expiredComeAlarmList == true && expiredDocAlarmList ==true)	{
				resultMap.put("message","만기문서 사전,자동 알람이 정상처리되었습니다.");	
				
			}else if(expiredComeAlarmList == true && expiredDocAlarmList ==false){
				resultMap.put("message","만기문서사전알람 정상처리,만기문서자동알람 대상자가 존재하지않습니다.");	
				
			}else if(expiredComeAlarmList == false && expiredDocAlarmList ==true){
				resultMap.put("message","만기문서사전알람대상자가 존재하지 않습니다,만기문서자동알람 정상처리.");		
				
			}else {				
				resultMap.put("message","만기문서 알림대상자가 존재하지 않습니다.");			}
			
								
			// 4. 로그결과처리
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
				
				// 배치로그 수정처리 : 완료시간/상태/메세지
				quartzService.batchWorkUpdate(resultMap);
							
			}catch(Exception e){
				logger.error(e.getMessage());
			}
		}
		
		long eTime = System.currentTimeMillis();
		
		logger.info("ExpireDocCheckQuartz END ="+df.format(eTime));
	
	}

	
}
