package kr.co.exsoft.quartz.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.co.exsoft.common.dao.HistoryDao;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.DocumentHtVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.dao.DocumentDao;
import kr.co.exsoft.document.dao.PageDao;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.document.vo.PageVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.library.ExsoftAbstractServiceImpl;
import kr.co.exsoft.eframework.repository.EXrepClient;
import kr.co.exsoft.eframework.util.ARIAUtil;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.external.dao.ExternalDao;
import kr.co.exsoft.folder.dao.FolderDao;
import kr.co.exsoft.folder.vo.FolderVO;
import kr.co.exsoft.permission.dao.AclDao;
import kr.co.exsoft.quartz.dao.QuartzDao;
import kr.co.exsoft.quartz.vo.BatchWorkVO;
import kr.co.exsoft.quartz.vo.FileQueueDeleteVO;
import kr.co.exsoft.quartz.vo.SyncGroupVO;
import kr.co.exsoft.quartz.vo.SyncUserVO;
import kr.co.exsoft.statistics.dao.AuditDao;
import kr.co.exsoft.user.dao.GroupDao;
import kr.co.exsoft.user.service.GroupService;
import kr.co.exsoft.user.service.UserService;
import kr.co.exsoft.user.vo.GroupVO;
import kr.co.exsoft.user.vo.UserVO;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 배치프로그램 서비스 구현 부분
 * @author 패키지 개발팀
 * @since 2014.07.17
 * @version 3.0
 *	
 * [2000][EDMS-REQ-036]	2015-09-07	이재민 : 조직도연계처리
 */
@Service("quartzService")
public class QuartzServiceImpl extends ExsoftAbstractServiceImpl implements QuartzService {

	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	
	@Autowired
	@Qualifier("sqlSessionBatch")
	private SqlSession sqlSessionBatch;
	
	@Autowired
	@Qualifier("sqlSessionImp")
	private SqlSession sqlSessionImp;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private GroupService groupService;
	
	@Autowired
	private UserService userService;
	
	protected static final Log logger = LogFactory.getLog(QuartzServiceImpl.class);
	
	@Override
	public int batchWorkWrite(long work_idx,String work_type,String work_nm) throws Exception {
		
		int ret = 0;
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		BatchWorkVO batchworkVO = setBatchWork(work_idx,work_type,work_nm);
		
		ret = quartzDao.batchWorkWrite(batchworkVO);
				
		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 배치작업로그 객체 생성하기.
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setBatchWork
	 * @param work_type
	 * @param work_nm
	 * @return BatchWorkVO
	 */
	public BatchWorkVO setBatchWork(long work_idx,String work_type,String work_nm) {
		
		BatchWorkVO vo = new BatchWorkVO();
		
		vo.setWork_idx(work_idx);
		vo.setWork_nm(work_nm);
		vo.setWork_type(work_type);
		
		return vo;
	}
	
	@Override
	public int batchWorkUpdate(HashMap<String,Object> map) throws Exception {
		
		int ret = 0;
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		ret = quartzDao.batchWorkUpdate(map);
				
		return ret;
	}
	

	@Override
	public boolean isBatchWork(HashMap<String,Object> map) throws Exception {
		
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		int result = quartzDao.isBatchWork(map);
		
		if(result > 0)	{
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public List<HashMap<String,Object>> auditExceedList(HashMap<String,Object> map) throws Exception {
		
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		List<CaseInsensitiveMap> auditList = new ArrayList<CaseInsensitiveMap>();
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		auditList = quartzDao.auditExceedList(map);
		
		if(auditList.size() > 0)	{
			
			for(CaseInsensitiveMap caseMap : auditList) {
				
				HashMap<String,Object> auditMap = new HashMap<String,Object>();
				auditMap.put("user_id",caseMap.get("user_id"));
				auditMap.put("user_name",caseMap.get("user_name"));
				auditMap.put("read_count",caseMap.get("read_count"));
				auditMap.put("group_name",caseMap.get("group_name"));
				auditMap.put("group_id",caseMap.get("group_id"));
				
				ret.add(auditMap);
			}
			
		}
				
		return ret;
	}
	
	@Override
	public int writeAudit(HashMap<String,Object> map) throws Exception  {
		
		int ret = 0;
		
		AuditDao auditDao =  sqlSession.getMapper(AuditDao.class);
		
		ret = auditDao.writeAudit(map);
		
		return ret;
	}
	
	@Override
	public List<HashMap<String, Object>> batchDocList(HashMap<String,Object> map) throws Exception {
	
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		List<CaseInsensitiveMap> docList = new ArrayList<CaseInsensitiveMap>();

		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		String workType = map.get("workType") != null ? map.get("workType").toString() : "";
		
		// 0. 업무구분 정보가 없을 경우 에러처리
		if(workType.equals("")) {
			throw processException("common.system.error");	
		}
		
		// 1.만기문서:expired 개인휴지통:privateTrash 시스템휴지통:systemTrash
		if(workType.equals("expired")) {
			docList = quartzDao.expiredDocList(map);
		}else if(workType.equals("privateTrash")) {
			docList = quartzDao.privateTrashDocList(map);
		}else if(workType.equals("systemTrash")) {
			docList = quartzDao.systemTrashDocList(map);
		}
		
		// 2.문서목록이 있는 경우 수행처리
		if(docList.size() > 0)	{
			
			for(CaseInsensitiveMap caseMap : docList) {
				
				HashMap<String,Object> expiredMap = new HashMap<String,Object>();
				expiredMap.put("doc_id",caseMap.get("doc_id"));
				expiredMap.put("root_id",caseMap.get("root_id") != null ? caseMap.get("root_id") : "");
				expiredMap.put("is_locked",caseMap.get("is_locked"));
				expiredMap.put("type_id",caseMap.get("doc_type"));
			
				ret.add(expiredMap);
			}

		}
		
		return ret;
	}
	
	@Override
	public HashMap<String,Object> systemUserInfo(HashMap<String,Object> map) throws Exception {
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		CaseInsensitiveMap userInfo = new CaseInsensitiveMap();
		HashMap<String,Object> ret = new HashMap<String,Object>();		
		
		// 1.SYSTEM_OPERATOR 정보 조회 :: 반드시 1계정만 존재한다.
		userInfo = quartzDao.systemUserInfo(map);
		
		// 2.리턴값 재정의 CaseInsensitiveMap => HashMap 변경처리
		ret.put("user_id", userInfo.get("user_id"));
		ret.put("user_name",userInfo.get("user_name"));
		ret.put("group_name", userInfo.get("group_name"));
		ret.put("group_id",userInfo.get("group_id"));
		
		return ret;
	}

	@Override
	public void docStatusProc(HashMap<String,Object> map) throws Exception {
		
		QuartzDao quartzDao = sqlSessionBatch.getMapper(QuartzDao.class);
		
		List<CaseInsensitiveMap> userDocList = new ArrayList<CaseInsensitiveMap>();
		List<CaseInsensitiveMap> groupDocList = new ArrayList<CaseInsensitiveMap>();
		
		// 1. 사용자별 문서현황 집계 정보 가져오기
		userDocList = quartzDao.userDocStatus(map);
		
		if(userDocList != null && userDocList.size() > 0) {
			
			for(CaseInsensitiveMap caseMap : userDocList) {
				
				// XR_DOCUMENT_USER_HT 객체값 설정
				HashMap<String,Object> status = new HashMap<String,Object>();					
				status.put("udate", caseMap.get("action_date"));
				status.put("user_id", caseMap.get("actor_id"));
				status.put("type_id", caseMap.get("type_id"));
				status.put("user_nm", caseMap.get("actor_nm"));
				status.put("group_id", caseMap.get("group_id"));
				status.put("group_nm", caseMap.get("group_nm"));
				status.put("create_cnt", caseMap.get("create_cnt"));
				status.put("read_cnt", caseMap.get("read_cnt"));
				status.put("update_cnt", caseMap.get("update_cnt"));
				status.put("delete_cnt", caseMap.get("delete_cnt"));
				
				// XR_DOCUMENT_USER_HT  등록처리
				quartzDao.userDocHtWrite(status);
			}

		}
		
		// 2. 부서별 문서현황 집계 정보 가져오기
		groupDocList = quartzDao.groupDocStatus(map);
		if(groupDocList != null && groupDocList.size() > 0) {
			
			for(CaseInsensitiveMap caseMap : groupDocList) {
				
				// XR_DOCUMENT_GROUP_HT 객체값 설정
				HashMap<String,Object> status = new HashMap<String,Object>();					
				status.put("gdate", caseMap.get("action_date"));
				status.put("group_id", caseMap.get("group_id"));
				status.put("type_id", caseMap.get("type_id"));
				status.put("create_cnt", caseMap.get("create_cnt"));
				status.put("read_cnt", caseMap.get("read_cnt"));
				status.put("update_cnt", caseMap.get("update_cnt"));
				status.put("delete_cnt", caseMap.get("delete_cnt"));
				
				// XR_DOCUMENT_GROUP_HT  등록처리
				quartzDao.groupDocHtWrite(status);
			}

		}

	}
	
	@Override
	public List<PageVO> delPageList(HashMap<String,Object> map) throws Exception {
		
		List<PageVO> ret = new ArrayList<PageVO>();
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		ret = quartzDao.delPageList(map);
		
		return ret;
	}
	
	
	//FileQueue 리스트가져오기
	public List<FileQueueDeleteVO> fileQueueDeleteList(HashMap<String,Object> map) throws Exception {
		
		List<FileQueueDeleteVO> ret = new ArrayList<FileQueueDeleteVO>();
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		
		ret = quartzDao.fileQueueDeleteList(map);
		
		return ret;
	}
	
	
	@Override
	public HashMap<String,Object> delPageProc(List<PageVO> pageList,EXrepClient eXrepClient ) throws Exception	{
	
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
				
		HashMap<String,Object> result = new HashMap<String,Object>();

		long successCnt = 0;
		long totalSize = 0;
		
		// 삭제대상 반복처리
		for(PageVO pageVO : pageList) {
		
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("page_id",pageVO.getPage_id());
			param.put("is_deleted",pageVO.getIs_deleted());
			
			pageDao.xrPageDelete(param);
			
			// eXrep 파일 삭제처리 :: 이미 삭제된 문서의 파일로 별도 백업처리하지 않는다.
			if(eXrepClient.isExists(pageVO.getVolume_id(),pageVO.getContent_path())) {
					
				if(eXrepClient.removeFile(pageVO.getVolume_id(), pageVO.getContent_path())) {
					successCnt++;
					totalSize = totalSize + pageVO.getPage_size();
				}				
			}									
			
			// NEXT PAGE Proc				
		}
		
		// 처리 결과 저장
		result.put("successCnt",successCnt);
		result.put("totalSize",totalSize);
		
				
		return result;
		
	}
	
	// FileQueue 삭제
	@Override
	public HashMap<String,Object> delFileProc(List<FileQueueDeleteVO> pageList,EXrepClient eXrepClient ) throws Exception	{
	
		QuartzDao quartzDao = sqlSessionBatch.getMapper(QuartzDao.class);
				
		HashMap<String,Object> result = new HashMap<String,Object>();	

		long successCnt = 0;
		//long totalSize = 0;
		
		// 삭제대상 반복처리
		for(FileQueueDeleteVO filequeuedeleteVO : pageList) {
		
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("content_path",filequeuedeleteVO.getContent_path());
			
			quartzDao.deleteQueue(param);
			
			// eXrep 파일 삭제처리 :: 이미 삭제된 문서의 파일로 별도 백업처리하지 않는다.
			if(eXrepClient.isExists(filequeuedeleteVO.getVolume_id(),filequeuedeleteVO.getContent_path())) {
					
				if(eXrepClient.removeFile(filequeuedeleteVO.getVolume_id(), filequeuedeleteVO.getContent_path())) {
					successCnt++;
				}				
			}									
			
			// NEXT PAGE Proc				
		}
		
		// 처리 결과 저장
		result.put("successCnt",successCnt);

		return result;
		
	}

	@Override
	public List<HashMap<String,Object>> tempDelDocList(HashMap<String,Object> map) throws Exception {
		
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		List<CaseInsensitiveMap> delDocList = new ArrayList<CaseInsensitiveMap>();
		
		QuartzDao quartzDao = sqlSessionBatch.getMapper(QuartzDao.class);
		
		delDocList = quartzDao.tempDelDocList(map);

		if(delDocList.size() > 0)	{
			
			for(CaseInsensitiveMap caseMap : delDocList) {
				
				HashMap<String,Object> delMap = new HashMap<String,Object>();
				delMap.put("doc_id",caseMap.get("doc_id"));
				delMap.put("root_id",caseMap.get("root_id") != null ? caseMap.get("root_id") : "");
				delMap.put("user_id",caseMap.get("user_id"));
				delMap.put("work_date",caseMap.get("work_date"));
			
				ret.add(delMap);
			}

		}
		
		return ret;
	}
		
	@Override
	public void tempDocDelete(HashMap<String,Object> map) throws Exception{
		
		QuartzDao quartzDao = sqlSessionBatch.getMapper(QuartzDao.class);
		quartzDao.tempDocDelete(map);		
		
	}

	@Override
	public HashMap<String,Object> docReadReqExpiredDelete() throws Exception {
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
		
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		AclDao aclDao = sqlSession.getMapper(AclDao.class);
		
		int i = 0;
		
		// 1. 문서 목록을 가져 온다.
		docList = quartzDao.docReadRequestList();
		
		if(docList.size() > 0) {
			for(DocumentVO documentVO : docList) {
				
				Date today = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date access_enddate = format.parse(documentVO.getAccess_enddate());
				
				// 2. 열람 만료일이 지났으면 목록에서 삭제한다.
				if(!today.before(access_enddate)) {
					// 2-1. 추가접근자 삭제
					param.clear();
					param.put("doc_id", documentVO.getDoc_id());
					param.put("accessor_id", documentVO.getReq_userid());
					aclDao.aclExItemDelete(param);
					
					// 2-2. 열람요청문서 목록에서 삭제
					param.clear();
					param.put("req_id", documentVO.getReq_id());
					documentDao.deleteDocReadRequest(param);
					
					// 3. XR_DOCUMENT_HT 등록처리
					// 3-1. 문서상세
					param.clear();
					param.put("doc_id", documentVO.getDoc_id());
					param.put("table_nm", Constant.DOC_TABLE);
					DocumentVO docVO = documentDao.commonDocDetail(param);
					
					// 3-2. 히스토리등록
					long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);
					
					String root_id = docVO.getRoot_id() != "" ? docVO.getRoot_id() : docVO.getDoc_id(); 
					
					DocumentHtVO vo = new DocumentHtVO(); 
					vo.setDoc_seq(doc_seq);
					vo.setRoot_id(root_id);
					vo.setAction_id("READREQUESTEXPIRED");
					vo.setTarget_id(docVO.getDoc_id());
					vo.setType_id(docVO.getDoc_type());
					vo.setDoc_name(docVO.getDoc_name());
					vo.setVersion_no(docVO.getVersion_no());
					vo.setActor_id(documentVO.getReq_userid());
					vo.setActor_nm(documentVO.getReq_username());
					vo.setGroup_id(documentVO.getGroup_id());
					vo.setGroup_nm(documentVO.getGroup_name());
					vo.setConnect_ip(Constant.BATCH_IP);
					vo.setAction_place(Constant.ACTION_PLACE);
					historyDao.documentHtWrite(vo);
					
					i++;
				}
			}
			resultMap.put("message", "정상처리 완료 : " + i + "건");
		} else {
			resultMap.put("message", "열람 만료일이 자난 문서 없음");
		}
		
		return resultMap;
	}
	
	@Override
	// [2000] 그룹 연계처리
	public String syncGroup() {
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		GroupDao groupDao = sqlSession.getMapper(GroupDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		String resultStr = "[syncGroup(부서) :: 동기화할 목록이 없음. ]";
		int errorCount = 0;
		
		String dept_cd = "";
		String group_name_ko = "";
		String group_name_en = "";
		String group_status = "";
		String group_id = "";
		String parent_id = "";
		int sort_index = 0;
		
		//1. 10일이상 지난 data삭제
		param.put("table_nm", "XR_SYNCGROUP");
		externalDao.deleteExpiredSyncData(param);
		
		//2. EAI에서 넘어온 ROOT의 parent_id에 dept_cd를 넣어준다 (그냥 하면 null이라 group insert시 처리안됨)
		externalDao.updateSynGroupRootParentId();
		
		//3. 인사정보 테이블에서 데이터얻기
		List<SyncGroupVO> syncGroupList = externalDao.syncGroup();
		
		//4. 기존 그룹정보 데이터얻기
		List<GroupVO> tempGroupList = quartzDao.selectGroupListForBatch();
		HashMap<String, GroupVO> tempGroupMap = new HashMap<String, GroupVO>();
		
		// 기존 그룹정보 map에 담기
		for(GroupVO tempGroupVO : tempGroupList) {
			tempGroupMap.put(tempGroupVO.getDept_cd(), tempGroupVO);
		}
		
		boolean isExist;
		
		// 맨 처음 가져온 루트그룹의 경우 parent_id를 group_id와 동일하게 해주는 처리를 위해 추가
		int groupCount = 0;
		
		for(SyncGroupVO syncGroupVO : syncGroupList) {
			try {
				isExist = false;
				
				dept_cd = syncGroupVO.getDept_cd();
				group_name_ko = syncGroupVO.getGroup_name_ko();
				group_status = syncGroupVO.getGroup_status();
				parent_id = syncGroupVO.getParent_id();
				sort_index = syncGroupVO.getSort_index();
				
				logger.debug("== syncGroup == dept_cd  " + dept_cd);
				logger.debug("== syncGroup == group_name_ko  " + group_name_ko);
				
				//5. 그룹 존재여부 확인
				if(tempGroupMap.containsKey(dept_cd)) {
					GroupVO tempVO = tempGroupMap.get(dept_cd);
					
					isExist = true;
					group_id = tempVO.getGroup_id();
					
					logger.debug("== syncGroup == dept_cd -> group_id  " + dept_cd + " -> " + group_id);
				}
				
				// parent 정보얻기
				if(tempGroupMap.containsKey(parent_id)) {
					GroupVO tempVO = tempGroupMap.get(parent_id);
					parent_id = tempVO.getGroup_id();
					
					logger.debug("== syncGroup == parent_id -> group_id  " + parent_id + " -> " + group_id);
				}
				
				GroupVO newGroupVO = new GroupVO();
				newGroupVO.setDept_cd(dept_cd);
				newGroupVO.setGroup_name_ko(group_name_ko);
				newGroupVO.setGroup_name_en(group_name_en);
				newGroupVO.setGroup_status(group_status);
				newGroupVO.setParent_id(parent_id);
				newGroupVO.setSort_index(sort_index);
				newGroupVO.setMap_id(Constant.MAP_ID_DEPT);
				
				//6. 등록/수정 처리
				
				// ----------------------------------
				// 수정일때
				// ----------------------------------
				if(isExist) {
					// group_id 세팅
					newGroupVO.setGroup_id(group_id);
					// 빈 sessionVO생성 (수정시 사용하지 않음)
					SessionVO sessionVO = new SessionVO();
					
					groupService.groupUpdate(newGroupVO, sessionVO);
					
				// ----------------------------------	
				// 등록일때
				// ----------------------------------	
				} else {
					// 빈 sessionVO생성 (부서기본 ACL, 폴더생성시 사용)
					SessionVO sessionVO = new SessionVO();
					sessionVO.setSessId(Constant.SYSTEM_ACCOUNT);
					sessionVO.setSessName("시스템관리자");
					
					groupService.groupWrite(newGroupVO, sessionVO, Constant.GROUP_WRITE_SYNC);
					
					if(groupCount == 0) {
						newGroupVO.setParent_id(newGroupVO.getGroup_id());
						groupDao.groupUpdate(newGroupVO);
						
						FolderVO folderVO = new FolderVO();
						folderVO.setUpdate_action(Constant.Status.UPDATE.toString());
						// 부서 폴더 ID 얻기
						String folder_id = CommonUtil.getChangedResourceIDByPrefix(newGroupVO.getGroup_id(), Constant.ID_PREFIX_FOLDER);
						folderVO.setFolder_id(folder_id);
						folderVO.setParent_id(folder_id);
						folderDao.folderUpdate(folderVO);
					}
					
					// 새롭게 생성된 VO객체를 dept_cd와 함께 tempmap정보에 담는다
					tempGroupMap.put(dept_cd, newGroupVO);
					
					groupCount++;
				}
				
			} catch (Exception e) {
				errorCount ++;
				e.printStackTrace();
			} finally {
				resultStr = "[syncGroup(부서) :: 총 "+ syncGroupList.size() + " 건 / 실패 : " + errorCount + " 건 ]";
			}
		}
		
		return resultStr;
	}

	@Override
	// [2000] 사용자 연계처리
	public String syncUser() {
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		QuartzDao quartzDao = sqlSession.getMapper(QuartzDao.class);
		GroupDao groupDao = sqlSession.getMapper(GroupDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		String resultStr = "[syncUser(사용자) :: 동기화할 목록이 없음. ]";
		int errorCount = 0;
		
		//1. 10일이상 지난 data삭제
		param.put("table_nm", "XR_SYNCUSER");
		externalDao.deleteExpiredSyncData(param);
		
		//2. 인사정보 테이블에서 데이터 얻기
		List<SyncUserVO> syncUserList = externalDao.syncUser();
		
		//3. 기존 그룹정보 데이터얻기
		List<GroupVO> tempGroupList = quartzDao.selectGroupListForBatch();
		HashMap<String, GroupVO> tempGroupMap = new HashMap<String, GroupVO>();
		
		// 기존 그룹정보 map에 담기
		for(GroupVO tempGroupVO : tempGroupList) {
			tempGroupMap.put(tempGroupVO.getDept_cd(), tempGroupVO);
		}
		
		//4. 기존 사용자정보 데이터얻기
		List<UserVO> tempUserList = quartzDao.selectUserListForBatch();
		HashMap<String, UserVO> tempUserMap = new HashMap<String, UserVO>();
		
		// 기존 그룹정보 map에 담기
		for(UserVO tempUserVO : tempUserList) {
			tempUserMap.put(tempUserVO.getEmp_no(), tempUserVO);
		}
		
		boolean isExist;
		
		for(SyncUserVO syncUserVO : syncUserList) {
			
			try {
				isExist = false;
				
				logger.debug("== syncUser == emp_no  " + syncUserVO.getEmp_no());
				logger.debug("== syncUser == user_id  " + syncUserVO.getUser_id());
				logger.debug("== syncUser == user_name_ko  " + syncUserVO.getUser_name_ko());
				
				// 5. 사용자 존재여부 확인
				UserVO newUserVO = new UserVO();
				
				if(tempUserMap.containsKey(syncUserVO.getEmp_no())) {
					isExist = true;
					
					logger.debug("== syncUser == EXIST TRUE");
					
					UserVO tempUserVO = tempUserMap.get(syncUserVO.getEmp_no());
					
					logger.debug("== syncUser ==  emp_no : " + syncUserVO.getEmp_no() + " - quota = " + tempUserVO.getStorage_quota());
					// 기존 사용자VO에 있는 스토리지 할당량 세팅
					newUserVO.setStorage_quota(tempUserVO.getStorage_quota());
				} else {
					// 신규등록 사용자의 경우 2GB를 넣어준다.
					logger.debug("== syncUser == EXIST FALSE");
					newUserVO.setStorage_quota(2147483648l);
					
					logger.debug("== syncUser ==  emp_no : " + syncUserVO.getEmp_no() + " - quota = " + newUserVO.getStorage_quota());
				}

				newUserVO.setUser_id(syncUserVO.getUser_id());
				newUserVO.setEmp_no(syncUserVO.getEmp_no());
				newUserVO.setUser_name_ko(syncUserVO.getUser_name_ko());
//				newUserVO.setUser_pass(syncUserVO.getUser_pass()); // [TODO] : 실제로 패스워드가 어뜨케 넘어오는가 체크필요...
				newUserVO.setUser_pass(ARIAUtil.ariaEncrypt("1111", syncUserVO.getUser_id()));
				newUserVO.setUser_status(syncUserVO.getUser_status());
				newUserVO.setUser_type(syncUserVO.getUser_type());
				newUserVO.setJobtitle(syncUserVO.getJobtitle());
				newUserVO.setPosition(syncUserVO.getPosition());
				newUserVO.setEmail(syncUserVO.getEmail());
				newUserVO.setTelephone(syncUserVO.getTelephone());
				String dept_cd = syncUserVO.getDept_cd();
				if(tempGroupMap.containsKey(dept_cd)) {
					GroupVO tempVO = tempGroupMap.get(dept_cd) ;
					// xr_user_ht에 들어갈 vo세팅
					newUserVO.setGroup_id(tempVO.getGroup_id());
					newUserVO.setGroup_nm(tempVO.getGroup_name_ko());
				} else {
					// 부서정보가 없을때는 미소속그룹으로 넣는다.
					GroupVO tempGroup = groupDao.independentGroupDetail();
					
					newUserVO.setGroup_id(tempGroup.getGroup_id());
					newUserVO.setGroup_nm(tempGroup.getGroup_name_ko());
				}
				
				//6. 등록/수정 처리
				
				// ----------------------------------
				// 수정일때
				// ----------------------------------
				if(isExist) {
					List<UserVO> userList = new ArrayList<UserVO>();
					
					UserVO tempUserVO = tempUserMap.get(syncUserVO.getEmp_no());
					newUserVO.setRole_id(tempUserVO.getRole_id()); // xr_user_ht
					newUserVO.setManage_group(newUserVO.getGroup_id()); // grouped update시 update manage_group
					
					// Update를 하기 위해 UserVO를 List로 변환
					userList.add(newUserVO);
					
					// 업데이트 수행
					userService.userUpdate(userList, false);
					userService.userGroupedUpdate(userList);
					
				// ----------------------------------
				// 등록일때
				// ----------------------------------
				} else {
					
					newUserVO.setRole_id("CREATOR");
					newUserVO.setPage_size("15");
					newUserVO.setShare_name("MYPAGE,MYDEPT");
					newUserVO.setMyexpiredComeAlarm("N");
					newUserVO.setMyexpiredDocAlarm("N");
					
					// xr_grouped에 들어갈 group_id가져오기
					param.clear();
					param.put("user_id", syncUserVO.getUser_id());
					param.put("group_id", newUserVO.getGroup_id());
					
					// 등록수행
					userService.userWrite(newUserVO, param);
				}
				
			} catch (Exception e) {
				errorCount ++;
				e.printStackTrace();
			} finally {
				resultStr = "[syncUser(사용자) :: 총 "+ syncUserList.size() + " 건 / 실패 : " + errorCount + " 건 ]";
			}
		}
		return resultStr;
	}
	
}
