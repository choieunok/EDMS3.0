package kr.co.exsoft.quartz.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.quartz.service.QuartzService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

/**
 * 열람 요청 문서 열람만료일지난 문서 삭제처리
 *
 * @author 패키지팀
 * @since 2015. 7. 28.
 * @version 1.0
 * 
 */
public class DocReadReqExpiredDeleteQuartz extends QuartzJob {
	
	protected static final Log logger = LogFactory.getLog(DocReadReqExpiredDeleteQuartz.class);
	protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	protected void executeJob(JobExecutionContext jobexecutioncontext) {
		
		QuartzService quartzService = (QuartzService)super.getBean("quartzService");
		CommonService commonService = (CommonService)super.getBean("commonService");
		
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		boolean isProcess = false;
		long work_idx = 0;
		long sTime = System.currentTimeMillis();
		
		try {
			logger.info("DocReadReqExpiredDeleteQuartz START ="+df.format(sTime));
			
			// 1.수행일 기준 감사정책 배치업무 수행여부 체크
			param.put("work_nm",Constant.BATCH_READREQ_EXPIRED);
			param.put("work_type",Constant.WORK_BATCH);
			param.put("work_sdate", StringUtil.getToday());
			param.put("work_state",Constant.T);
			
			// 2.미수행인 경우에만 수행처리를 한다.
			isProcess = quartzService.isBatchWork(param);
			if(!isProcess)	{
				// 3. 로그등록처리
				work_idx = commonService.commonNextVal(Constant.COUNTER_ID_BATCH_WORK);
				quartzService.batchWorkWrite(work_idx,Constant.WORK_BATCH,Constant.BATCH_READREQ_EXPIRED);
				
				// 4. 열람만료일지난 문서 삭제처리
				resultMap = quartzService.docReadReqExpiredDelete();
				
				// 5. 로그결과처리
				resultMap.put("work_state",Constant.T);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			resultMap.put("work_state",Constant.F);
			resultMap.put("message","EXCEPTION ERROR");
		} finally {
			Date now = new Date();				
			resultMap.put("work_idx",work_idx);
			resultMap.put("work_edate",df.format(now));

			try {
				// 배치수행시만 로그를 변경처리한다.
				if(!isProcess && work_idx != 0)	{
					quartzService.batchWorkUpdate(resultMap);
				}
				
			}catch(Exception e){
				logger.error(e.getMessage());
			}
		}
		
		long eTime = System.currentTimeMillis();
		logger.info("DocReadReqExpiredDeleteQuartz END ="+df.format(eTime));
		
	}

}
