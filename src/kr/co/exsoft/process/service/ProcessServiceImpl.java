package kr.co.exsoft.process.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import kr.co.exsoft.common.dao.CommonDao;
import kr.co.exsoft.common.service.CacheService;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.CommentVO;
import kr.co.exsoft.common.vo.RecentlyObjectVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.dao.DocumentDao;
import kr.co.exsoft.document.dao.PageDao;
import kr.co.exsoft.document.dao.TypeDao;
import kr.co.exsoft.document.service.DocumentService;
import kr.co.exsoft.document.service.PageService;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.document.vo.PageVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.library.ExsoftAbstractServiceImpl;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.eframework.util.PagingAjaxUtil;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.folder.dao.FolderDao;
import kr.co.exsoft.folder.vo.FolderVO;
import kr.co.exsoft.permission.service.AclService;
import kr.co.exsoft.permission.vo.AclExItemVO;
import kr.co.exsoft.process.dao.ProcessDao;
import kr.co.exsoft.process.vo.ProcessExecutorVO;
import kr.co.exsoft.process.vo.ProcessVO;
import kr.co.exsoft.user.dao.GroupDao;
import net.sf.json.JSONArray;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Process 서비스 구현 부분
 * @author 패키지 개발팀
 * @since 2015.03.12
 * @version 3.0
 * [3000][기능추가]	2015-09-15	성예나 : 업무요청한 문서 삭제시 doc_root_id를 가져오지못해서 생기는 오류 수정.	
 * [3001][EDMS-REQ-040]	2015-09-15 	성예나 : 업무문서삭제시 storage_usage 업데이트처리
 */	
@Service("processService")
public class ProcessServiceImpl extends ExsoftAbstractServiceImpl implements ProcessService {
	
	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	
	@Autowired
	@Qualifier("sqlSessionBatch")
	private SqlSession sqlSessionBatch;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private AclService aclService;
	
	/**
	 * public method 구현
	 */	
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 협업 목록에서 사용할 tooltip set 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setProcessTooltip
	 * @param processList
	 * @param map
	 * @throws Exception void
	 */
	protected void setProcessTooltip(List<ProcessVO> processList, HashMap<String, Object> map) throws Exception{
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		List<String> processIdList = new ArrayList<String>();
		for(ProcessVO processVo : processList){
			processIdList.add(processVo.getProcess_id());
		}
		
		if( processIdList.size() == 0){
			//throw new Exception("[ProcessServiceImpl.setProcessTooltip] process_id not found Exception!!");
			return; // 데이터가 없을 경우
		}
			
		map.put("processIdList", processIdList);
		List<ProcessExecutorVO> peVoList = processDao.processExcutorList(map);
		
		// 협업 리스트, 협업 단계조회에서 사용됨
		setExecutorInfo(processList, peVoList);
		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 협업 목록, 협업단계 조회, 협업 상세조회에서 사용할 승인, 열람 정보를 set
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setExecutorInfo
	 * @param processList
	 * @param peVoList
	 * @throws Exception void
	 */
	protected void setExecutorInfo(List<ProcessVO> processList, List<ProcessExecutorVO> peVoList) throws Exception{
		
		List<ProcessExecutorVO> tempList = new ArrayList<ProcessExecutorVO>();
		tempList.addAll(peVoList);
		
		for(int i=0; i < processList.size(); i++) {
			int wirteCntIng=0, approveCntIng=0, receiveCntIng=0;
			int wirteCntEnd=0, approveCntEnd=0, receiveCntEnd=0;
			
			ProcessVO tempProcessVo = new ProcessVO(); 
			tempProcessVo =	processList.get(i);
		
			for(Iterator<ProcessExecutorVO> iter = tempList.iterator(); iter.hasNext();){
				ProcessExecutorVO tempVo = iter.next();
				if(tempProcessVo.getProcess_id().equals(tempVo.getProcess_id())){
					switch (tempVo.getType()) {
						case Constant.PROCESS_TYPE_AUTHOR: tempProcessVo.setAuthor_nm(tempVo.getExecutor_name()); tempProcessVo.setAuthor_id(tempVo.getExecutor_id());//공동작성자와 같이 처리 하기 위해 break 안함
						case Constant.PROCESS_TYPE_COAUTHOR:{
							if(tempVo.getStatus().equals(Constant.PROCESS_EXECUTOR_END)){
								wirteCntEnd++; //완료
								tempProcessVo.getWrite_list().add(tempVo.getExecutor_name()+"|작성완료");
								tempProcessVo.setWrite_list(tempProcessVo.getWrite_list());
							}else{
								wirteCntIng++; //미완료
								tempProcessVo.getWrite_list().add(tempVo.getExecutor_name()+"|작성중");
								tempProcessVo.setWrite_list(tempProcessVo.getWrite_list());
							}
						};break;
						case Constant.PROCESS_TYPE_APPROVER:{
							if(tempVo.getStatus().equals(Constant.PROCESS_EXECUTOR_END)){
								approveCntEnd++; //완료
								tempProcessVo.getApproval_list().add(tempVo.getExecutor_name()+"|승인완료");
								tempProcessVo.setApproval_list(tempProcessVo.getApproval_list());
							}else{
								approveCntIng++; //미완료
								tempProcessVo.getApproval_list().add(tempVo.getExecutor_name()+"|승인대기");
								tempProcessVo.setApproval_list(tempProcessVo.getApproval_list());
							}
						};break;
						case Constant.PROCESS_TYPE_RECEIVER:{
							if(tempVo.getStatus().equals(Constant.PROCESS_EXECUTOR_END)){
								receiveCntEnd++; //완료
								tempProcessVo.getReceiver_list().add(tempVo.getExecutor_name()+"|열람완료");
								tempProcessVo.setReceiver_list(tempProcessVo.getReceiver_list());
							}else{
								receiveCntIng++; //미완료
								tempProcessVo.getReceiver_list().add(tempVo.getExecutor_name()+"|열람대기");
								tempProcessVo.setReceiver_list(tempProcessVo.getReceiver_list());
							}
							
						};break;
	
						default:
							break;
					}
			
					iter.remove(); // 사용한 peVoList 값은 remove 시킨다.
				}
			} // 실행자 for end...
			
			tempProcessVo.setWrite_count(wirteCntEnd+"/"+(wirteCntIng+wirteCntEnd));
			tempProcessVo.setApproval_count(approveCntEnd+"/"+(approveCntIng+approveCntEnd));
			tempProcessVo.setReceiver_count(receiveCntEnd+"/"+(receiveCntIng+receiveCntEnd));
			// 기존 내용 변경 시 add가 아닌 set을 이용한다.
			processList.set(i, tempProcessVo);
		} // 협업 for end..		
	}
	
	protected void setExecutorAclExItem(String process_id, String doc_id) throws Exception{
//		for(AclExItemVO aclExItemVO : documentVO.getAclExItemList()){
//			aclExItemVO.setDoc_id(documentVO.getDoc_id());
//			aclService.aclExItemWrite(param2, aclExItemVO, sessionVO);
//		}
		/**
		 * 대표작성자는 소유자가 되기 때문에 추가권한에 추가 하지 않는다. 
		 */
		
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
				
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String, String> insertExecutorMap = new HashMap<String, String>();
		
		// 1. 실행자 정보를 가져온다
		param.put("type", Constant.ACTION_PROCESS_END);
		param.put("process_id", process_id);
		List<ProcessExecutorVO> peVoList = processDao.processExcutorList(param);
		
		// 1-1. 문서 정보를 가져온다.
		param.clear();
		param.put("table_nm", Constant.DOC_TABLE);
		param.put("doc_id", doc_id);		
		DocumentVO documentVo = documentDao.commonDocDetail(param);
	
		// 2. 확장 acl 목록을 가져온다.
		param.clear();
		param.put("doc_id", doc_id);
		List<AclExItemVO> exAclItemList = aclService.exAclItemList(param);
		
		// 3. 2번 for문을 돌면서 없으면 insert 한다.
		boolean isExist = false;
		for(ProcessExecutorVO tempVo : peVoList){
			isExist = false;
			
			for(Iterator<AclExItemVO> iter = exAclItemList.iterator(); iter.hasNext();){
				AclExItemVO aclExItemVo = iter.next();
				// 추가 접근자로 이미 등록된 내용이 있는지 체크한다.
				if(tempVo.getExecutor_id().equals(aclExItemVo.getAccessor_id())){
					isExist = true;
					iter.remove();
					break;
				}
			}
			
			if(documentVo.getOwner_id().equals(tempVo.getExecutor_id())){
				// 실행자가 소유자와 동일하면 추가 접근 권한으로 등록 안한다.
				// 단 등록/수정에서 추가 접근 권한으로 등록 시 기존 상태값 유지
				isExist = true;
			}
			
			if(!isExist){
				// 존재하지 않거나, 등록되진 않았으면 insert
				if(!insertExecutorMap.containsValue(tempVo.getExecutor_id())){
					AclExItemVO newAclExitenVo = new AclExItemVO();
					newAclExitenVo.setDoc_id(doc_id);
					newAclExitenVo.setAccessor_id(tempVo.getExecutor_id());
					newAclExitenVo.setAccessor_isgroup(Constant.F);
					newAclExitenVo.setAccessor_isalias(Constant.F);
					newAclExitenVo.setAct_browse(Constant.T);
					newAclExitenVo.setAct_read(Constant.T);
					newAclExitenVo.setAct_update(Constant.F);
					newAclExitenVo.setAct_delete(Constant.F);
					newAclExitenVo.setAct_create(Constant.F);
					newAclExitenVo.setAct_cancel_checkout(Constant.F);
					newAclExitenVo.setAct_change_permission(Constant.F);
					
					aclService.aclExItemWrite(null, newAclExitenVo);
					
					// 권한 등록 한 실행자는 map 정보에 담아 중복 처리 되지 않도록 한다.					
					insertExecutorMap.put(tempVo.getExecutor_id(), tempVo.getExecutor_id());
				}
			} // isExist IF End...
		}
	}
	

	/**
	 * Interface override 
	 */	
	@Override
	public Map<String, Object> processCount(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		//int count = processDao.processCount(map);
		int count = processDao.processListCount(map);
		
		resultMap.put("count",count);
		resultMap.put("result",Constant.RESULT_TRUE);
		return resultMap;
	}

	@Override
	public Map<String, Object> processList(HashMap<String, Object> map) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<ProcessVO> processList = new ArrayList<ProcessVO>();
		int total = 0;
		
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		// 1. 문서 목록을 가져 온다
		total = processDao.processListCount(map);
		processList = processDao.processList(map);
		
		// 2. 승인, 열람 tooltip을 set. 엑셀저장은 매핑 안함
		if(processList.size() > 0) {
			setProcessTooltip(processList, map);
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",processList);		
		
		// Ajax Paging 
		String strLink = "javascript:exsoftProcessFunc.event.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		

		return resultMap;
	}
	
	@Override
	public Map<String, Object> processRecentlyList(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<ProcessVO> processList = new ArrayList<ProcessVO>();
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		processList = processDao.processRecentlyRegistList(map);
		
		resultMap.put("result",Constant.RESULT_TRUE);
		resultMap.put("list",processList);		
		

		return resultMap;
	}
	
	@Override
	public Map<String, Object> selectProcessRecently(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<ProcessExecutorVO> processExecutorList = new ArrayList<ProcessExecutorVO>();
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		GroupDao groupDao = sqlSession.getMapper(GroupDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		// 1. 업무명 가져오기
		ProcessVO processInfo = processDao.processInfo(map);
		
		// 1-1 폴더_id 가져 오기
		map.put("doc_id", processInfo.getDoc_root_id());
		String processName = processInfo.getName();
		String folderId = processDao.processFolderIdByDocId(map);
		map.put("folder_id", folderId);
		FolderVO folderVo = folderDao.folderDetail(map);
		
		// 2. 협업자 정보 가져오기
		processExecutorList = processDao.processExcutorList(map);
		
		// 3. 협업자 정보를 이용하여 협업자의 부서명 가져오기
		List<String> userIdList = new ArrayList<String>();
		for(ProcessExecutorVO processExecutorVO : processExecutorList){
			userIdList.add(processExecutorVO.getExecutor_id());
		}
		
		map.put("userIdList", userIdList);
		List<CaseInsensitiveMap> groupList = groupDao.groupInfoByUserId(map);
		HashMap<String, String> groupInfoMap = new HashMap<String, String>();
		for(CaseInsensitiveMap tempMap : groupList){
			groupInfoMap.put((String)tempMap.get("user_id"), (String)tempMap.get("group_nm"));
		}
		
		for(int i=0; i<processExecutorList.size(); i++){
			processExecutorList.get(i).setGroup_nm(groupInfoMap.get((processExecutorList.get(i).getExecutor_id())));
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		resultMap.put("processName",processName);
		resultMap.put("full_path",cacheService.getFolderFullpathNameByFolderId(folderId, true));		
		resultMap.put("folder_id",folderId);
		resultMap.put("map_id",folderVo.getMap_id());
		resultMap.put("acl_id",folderVo.getAcl_id());
		resultMap.put("processInfo",processInfo);
		resultMap.put("list",processExecutorList);		

		return resultMap;
	}

	@Override
	public Map<String, Object> processWrite(SessionVO sessionVO, Model model, HashMap<String, Object> map, HttpServletRequest request) throws Exception {
		
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int process_id = commonService.commonNextVal(Constant.COUNTER_ID_PROCESS);
		int recently_id = commonService.commonNextVal(Constant.COUNTER_ID_RECENTLY);

		String strProcessId = CommonUtil.getStringID(Constant.ID_PREFIX_PROCESS, process_id);
		String strContent = map.get("content") != null ? map.get("content").toString() : "";
		
		// 1. documentVo set
		DocumentVO documentVo = new DocumentVO();
		//documentVo.setDoc_id(strDocumentId);
		documentVo.setDoc_name(map.get("name").toString());
		documentVo.setPage_cnt(Integer.parseInt(map.get("page_cnt").toString()));
		documentVo.setAcl_id(map.get("acl_id").toString());
		documentVo.setDoc_type(map.get("doc_type").toString());
		documentVo.setFolder_id(map.get("folder_id").toString());
		documentVo.setAccess_grade(Constant.DOCUMENT_DEFALUT_ACCESSGRADE);
		documentVo.setSecurity_level(Constant.DOCUMENT_DEFALUT_SECURITY_LEVEL);
		documentVo.setDoc_status(Constant.DOCUMENT_STATUS_PROCESS_ING);			//process 진행단계는 P, 완료 후 C
		 
		// vailidation check를 위한 추가
		map.put("isType", Constant.INSERT);
		map.put("version_type", Constant.VERSION_NEW_DOCUMENT);
		
		// 1-1. 확장속성이 존재하는 경우	
		List<HashMap<String,Object>> attrList = new ArrayList<HashMap<String,Object>>();
//		if(map.get("is_extended") != null && map.get("is_extended").toString().equals(Constant.T))	{	
			attrList = documentService.docExtendedAttrList(request,documentVo.getDoc_type()); 				
//		}
		
		// 1-2 doc_id, 확장 권한(aclExItem_list), 첨부파일(fileList) 등 set
		documentService.writeDocValid(map, documentVo, sessionVO);
		
		// 2. processVo set
		ProcessVO processVo = new ProcessVO();
		processVo.setProcess_id(strProcessId);
		processVo.setDoc_root_id(documentVo.getDoc_id());
		processVo.setCreator_id(sessionVO.getSessId());
		processVo.setCreator_name(sessionVO.getSessName());
		processVo.setName(map.get("name").toString()); 						// 업무명
		processVo.setStatus(Constant.PROCESS_STATUS_WRITE);
		//processVo.setExpect_date(map.get("expect_date").toString());		// 완료 예정일
		processVo.setExpect_dateDB(CommonUtil.getCurruentTimeByDate(map.get("expect_date").toString()));
		processVo.setContent(strContent);
		// 2-1 process Dao 처리
		processDao.insertProcess(processVo);
		
		// 3. processExecutorVo set
		// 3-1 작성자, 공동작성자, 승인자, 수신자
		List<ProcessExecutorVO> proExecutorList = CommonUtil.jsonArrayToProcessExecutorList(map);
		// 3-2 요청자
		ProcessExecutorVO processExecutorVo = new ProcessExecutorVO();
		processExecutorVo.setType(Constant.PROCESS_TYPE_REQUESTOR);
		processExecutorVo.setExecutor_id(map.get("requestorId").toString());
		processExecutorVo.setExecutor_name(map.get("requestorName").toString());
		processExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_END);
		processExecutorVo.setSort_index(0);
		
		proExecutorList.add(processExecutorVo);
		for(ProcessExecutorVO tempVo : proExecutorList){
			String execute_id = CommonUtil.getStringID(Constant.ID_PREFIX_PROCESS_EXECUTOR, commonService.commonNextVal(Constant.COUNTER_ID_PROCESS_EXECUTOR));
			tempVo.setExecute_id(execute_id);
			tempVo.setProcess_id(strProcessId);
			tempVo.setDoc_root_id(documentVo.getDoc_id());
			
			// 날짜 및 상태값
			if(tempVo.getType().equals(Constant.PROCESS_TYPE_AUTHOR) || tempVo.getType().equals(Constant.PROCESS_TYPE_COAUTHOR)){
				if(tempVo.getType().equals(Constant.PROCESS_TYPE_AUTHOR)){
					// 문서 등록자, 소유자는 주작성자가 된다. writeDocValid에서 현재 사용자로 set된 값을 변경 한다.
					documentVo.setCreator_id(tempVo.getExecutor_id());
					documentVo.setCreator_name(tempVo.getExecutor_name());
					documentVo.setOwner_id(tempVo.getExecutor_id());
				}
				
				tempVo.setStatus(Constant.PROCESS_EXECUTOR_START);
				tempVo.setStart_dateDB(CommonUtil.getCurruentTime());	// 오늘 날짜
			}else{
				tempVo.setStatus(Constant.PROCESS_EXECUTOR_WAIT);
				tempVo.setStart_dateDB(CommonUtil.getCurruentTimeByDate("9999-01-01"));  // 임시 날짜
			}
			
			// processExecutor Dao 처리
			processDao.insertProcessExecutor(tempVo);
		}
		
		// 4. recently set
		RecentlyObjectVO recentlyObjectVo = new RecentlyObjectVO();
		recentlyObjectVo.setIdx(CommonUtil.getStringID(Constant.ID_PREFIX_RECENTLY, recently_id));
		recentlyObjectVo.setUser_id(sessionVO.getSessId());
		recentlyObjectVo.setTarget_id(processVo.getProcess_id());
		recentlyObjectVo.setTarget_type(Constant.RECENTLY_TYPE_PROCESS);
		
		// 4-1 recently Dao 처리 
		commonService.insertRecentlyObject(recentlyObjectVo);
		
		// 1-3 documentService.writeDocProc(); 호출
		documentService.writeDocProc(map, documentVo, attrList, sessionVO);
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

	@Override
	public Map<String, Object> processDetail(HashMap<String, Object> map) throws Exception {
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ProcessVO processVo = new ProcessVO();
		List<ProcessVO> tempProcessList = new ArrayList<ProcessVO>();
		List<ProcessExecutorVO> executorList = new ArrayList<ProcessExecutorVO>();
		
		processVo = processDao.processInfo(map);
		executorList = processDao.processExcutorList(map);
		
		tempProcessList.add(processVo);
		setExecutorInfo(tempProcessList, executorList);
		
		resultMap.put("processVo",tempProcessList.get(0));		
		resultMap.put("processExecutorList",executorList);		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

	@Override
	public Map<String, Object> approveAction(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		String actionType = map.get("actionType").toString();	// null check 안함.(null point exception)
		String process_id = map.get("process_id").toString();
		String doc_id = map.get("doc_root_id").toString();
		boolean isNextExecutor = true;
		
		// 1. xr_comment 추가 :: com_step은 증가, com_order는 default(0) 즉, 답글 형태로 구성 안함
		map.put("doc_root_id", process_id);
		CommentVO commentVo = new CommentVO();
		commentVo.setCom_id( CommonUtil.getStringID(Constant.ID_PREFIX_COMMENT, commonService.commonNextVal(Constant.COUNTER_ID_COMMENT)));
		commentVo.setDoc_root_id(process_id);  
		commentVo.setCom_step(String.valueOf(documentDao.checkMaxStep(map)+1));  // doc_root_id => process_id
		commentVo.setCreator_id(sessionVO.getSessId());
		commentVo.setCreator_name(sessionVO.getSessName());
		commentVo.setParent_creator_name(sessionVO.getSessName());
		commentVo.setContent(map.get("content").toString());
		
		documentDao.docCommentWrite(commentVo);	
		
		// 2. xr_process 승인 단계로 변경
		ProcessVO processVo = new ProcessVO(); // process_id, status를 제외한 DB값은 빈값
		processVo.setProcess_id(process_id);
		processVo.setComplete_dateDB(CommonUtil.getCurruentTime());
		
		// 3. xr_process_executor 요청한 실행자 상태값 변경
		ProcessExecutorVO processExecutorVo = new ProcessExecutorVO();
		processExecutorVo.setUpdateDBType(actionType);
		processExecutorVo.setProcess_id(process_id);		
		
		if(actionType.equals(Constant.PROCESS_ACTION_APPROVEREJECT)){
			// 반려
			isNextExecutor = false;
			processVo.setStatus(Constant.PROCESS_STATUS_MODIFY);
			processExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_WAIT);
			processExecutorVo.setEnd_dateDB(CommonUtil.getCurruentTimeByDate("8888-01-01"));
			
		}else{
			// 승인요청, 승인
			processExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_END);
			processExecutorVo.setEnd_dateDB(CommonUtil.getCurruentTime());
			
		}
		
		// [3]action 요청한 실행자 정보 update
		processDao.updateProcessExecutor(processExecutorVo);
		
		// 4. xr_process_executor 첫번째 승인자 승인 시작으로 변경 :: status(S), start_date(sysdate)
		if(isNextExecutor){
			ProcessExecutorVO currentApprover = processDao.currentApproverInfo(map);
			if(currentApprover != null && !StringUtil.isEmpty(currentApprover.getExecutor_id())){
				processVo.setStatus(Constant.PROCESS_STATUS_APPROVAL);
				processExecutorVo = new ProcessExecutorVO();
				processExecutorVo.setExecute_id(currentApprover.getExecute_id());
				processExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_START);
				processExecutorVo.setStart_dateDB(CommonUtil.getCurruentTime());
				// 다음 승인자 update
				processDao.updateProcessExecutor(processExecutorVo);
				
			}else{
				processVo.setStatus(Constant.PROCESS_STATUS_END);
				
				// 1. 협업자들 문서에 확장 ACL로 등록
				setExecutorAclExItem(process_id, doc_id);
				
				// 2. 문서  status = 'C'로 변경
				DocumentVO documentVo = new DocumentVO();
				documentVo.setUpdate_action(Constant.ACTION_PROCESS_END);
				documentVo.setDoc_id(doc_id);
				documentVo.setDoc_status(Constant.DOC_STATUS_CREATE);
				int resultCnt = documentDao.documentUpdate(documentVo);
				if(resultCnt == 0){
					throw new Exception("Process Document Not Found...");
				}
				
			}
		}else if(processVo.getStatus().equals(Constant.PROCESS_STATUS_MODIFY)){
			processExecutorVo = new ProcessExecutorVO();
			processExecutorVo.setUpdateDBType(Constant.PROCESS_ACTION_APPROVEREQUEST);
			processExecutorVo.setProcess_id(process_id);
			processExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_START);
			processExecutorVo.setStart_dateDB(CommonUtil.getCurruentTime());
			processExecutorVo.setEnd_dateDB(CommonUtil.getCurruentTimeByDate("8888-01-01"));
			// 다음 승인자 update
			processDao.updateProcessExecutor(processExecutorVo);
		}
		
		// [2]업무(협업) 단계 update
		processDao.updateProcess(processVo);
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

	@Override
	public Map<String, Object> pageDelete(SessionVO sessionVO, HashMap<String, Object> map) throws Exception {
		//기존엔 sqlSession이였는데 sqlSessionBatch로 수정.( processDelete쪽에서 sqlSessionBatch를 사용)	[3000]
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String strDocRootId = StringUtil.getMapString(map, "doc_root_id");
		if(StringUtil.isEmpty(strDocRootId)){
			throw processException("process.doc_root_id.found.error");
		}
		
		List<String> listPageId = StringUtil.getMapStringArray(map, "page_ids", "\\|");
		param.put("page_ids", listPageId);
		param.put("is_deleted", Constant.T);
		
		pageDao.xrFiledDelete(param);
		pageDao.pageInfoUpdate(param);
		
		// 상기 로직 xr_document도 처리해야 함. 협업 수정 처리 과정과 동일하게 적용 필요
		param.clear();
		param.put("doc_id", strDocRootId);
		List<PageVO> pageList = pageDao.comDocPageList(param);  		// 변경된 첨부파일 목록을 가져온다.
		
		DocumentVO documentVo = new DocumentVO();
		documentVo.setUpdate_action(Constant.ACTION_PAGE);
		documentVo.setDoc_id(strDocRootId);
							
		long page_size = 0;
		
		if(pageList != null && pageList.size() > 0 )	{
			for(PageVO pageVO : pageList)	{			
				page_size += pageVO.getPage_size();						
			}
			documentVo.setPage_total(page_size);
			documentVo.setPage_cnt(pageList.size());
		}else {
			documentVo.setPage_total(0);
			documentVo.setPage_cnt(0);
		}
			
		// XR_DOCUMENT PAGE_CNT/PAGE_TOTAL 변경처리
		documentDao.documentUpdate(documentVo);
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

	@Override
	public Map<String, Object> processDelete(SessionVO sessionVO, HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		/** 
		 * processDelete는 협업 진행단계(작성,보완)에서만 가능
		 * 승인 단계는 삭제 불가능, 완료 단계는 권한에 의해 documentService에서 삭제됨 
		 * 삭제 성공에 대한 return(int 값)에 대해서 validte 처리는 불필요함.
		 */
		
		//processData
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
		ProcessDao processDao = sqlSessionBatch.getMapper(ProcessDao.class);
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		CommonDao commonDao = sqlSessionBatch.getMapper(CommonDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);		
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		String deleteType = StringUtil.getMapString(map, "deleteType", "process");
		String user_id = sessionVO.getSessId();
		String process_id = "";
		String doc_id = "";
		
		String delProcessList = StringUtil.getMapString(map, "processData");
		if(StringUtil.isEmpty(delProcessList))	{
			throw processException("process.delete.list.error");
		}
		
		JSONArray jsonArray = JSONArray.fromObject(delProcessList);
		if(jsonArray.size() > 0 ) {	
			for(int j=0;j < jsonArray.size();j++)	{				 
				doc_id = jsonArray.getJSONObject(j).getString("doc_root_id").toString();
				
				// 구분값 기준으로 문서에서 삭제하는 것, 협업에서 삭제하는 것 
				if(deleteType.equals("process")){
					process_id = jsonArray.getJSONObject(j).getString("process_id").toString();
					
					// 업무문서 삭제시 storage_usage 업데이트 처리	[3001]
					param.put("doc_id",doc_id);
					String folder_id = processDao.processFolderIdByDocId(param);
					param.put("folder_id",folder_id); 	
					folderDao.storageUsageDelete(param);	
					
					// 1. XR_LINKED 삭제
					param.put("table_nm","XR_LINKED");
					documentDao.docCommonDelete(param);
					
					// 2. XR_DOCUMENT 삭제
					param.put("table_nm","XR_DOCUMENT");
					documentDao.docCommonDelete(param);
					
					// 3. XR_PAGE IS_DELETE=T로 변경 및 XR_FILED 삭제		
					// pageList 가져 온다.
					param.clear();
					param.put("doc_id",doc_id);
					List<CaseInsensitiveMap> listPageId = pageDao.xrPageList(param);
					
					if(listPageId.size() > 0){
						String page_ids = "";
						for(CaseInsensitiveMap tempMap : listPageId){
							page_ids += tempMap.get("PAGE_ID") + "|";
						}
						
						param.put("page_ids", page_ids);
						// [3000]
						param.put("doc_root_id",doc_id);
						pageDelete(sessionVO, param);
					}
					
				}else{
					// 문서 삭제에서 호출
					param.clear();
					param.put("doc_root_id",doc_id);
//					process_id = processDao.getProcessIdByDocRootId(param);
					ProcessVO processVo = processDao.processInfo(param);
					process_id = processVo != null ? processVo.getProcess_id() : "";
				}
				
				if(!StringUtil.isEmpty(process_id)){
					// 5. 협업최근 문서 XR_RECENTLY 삭제 user_id, target_id
					param.clear();
					param.put("user_id", user_id);
					param.put("target_id", process_id);
					commonDao.deleteRecently(param);
					
					// 6. 협업 처리 현황 XR_COMMENT 삭제 :: service로직이 불필요하여 dao 호출
					param.clear();
					param.put("doc_root_id", process_id);
					documentDao.deleteCommentRow(param);
					
					// 7. XR_PROCESS_EXECUTOR 삭제
					param.clear();
					param.put("process_id", process_id);
					processDao.deleteProcessExecutor(param);
					
					// 8. XR_PROCESS 삭제
					processDao.deleteProcess(param);
				} // if end...
				
			}
			
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

	@Override
	public Map<String, Object> processUpdate(SessionVO sessionVO, Model model, HashMap<String, Object> map, HttpServletRequest request) throws Exception {
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String strProcessId = StringUtil.getMapString(map, "process_id");
		String strDocRootId = StringUtil.getMapString(map, "doc_root_id");
		
		if(StringUtil.isEmpty(strProcessId)){
			throw processException("process.id.found.error");
		}else if(StringUtil.isEmpty(strDocRootId)){
			throw processException("process.doc_root_id.found.error");
		}
		
		// 1. 포르세스 정보 변경 :: 업무명, 완료일자, 업무요청
		ProcessVO processVo = new ProcessVO();
		processVo.setProcess_id(strProcessId);
		processVo.setName(StringUtil.getMapString(map, "name")); 						// 업무명
		processVo.setExpect_dateDB(CommonUtil.getCurruentTimeByDate(map.get("expect_date").toString()));
		processVo.setContent(StringUtil.getMapString(map, "content"));
		processVo.setStatus(""); // 상태값은 변경 안한다.
		
		processDao.updateProcess(processVo);
		
		// 2. 프로세스 실행자 변경 :: 작성단계 및 보완단계에서 처리됨
		// 2-1 :: 승인자 및 수신자는 정보 삭제 후 insert
		// 2-2 :: 대표작성자 및 공동작성자는 기존정보와 비교 후 처리
		// 기존정보	신규정보	처리방법
		// 홍길동		홍길동		삭제>insert :: 작성자이면 상태값 S, 나머지 상태값 N
		// 심청이		없음		삭제
		// 없음		이몽룡		추가
		// 2-3 :: 요청자는 아무 작업도 하지 않음
		// 3-1 작성자, 공동작성자, 승인자, 수신자
		List<ProcessExecutorVO> newExecutorList = CommonUtil.jsonArrayToProcessExecutorList(map);
		
		param.clear();
		param.put("process_id", strProcessId);
		List<ProcessExecutorVO> currentExecutorList = processDao.processExcutorList(param);
		
		for(ProcessExecutorVO newExecutorVo : newExecutorList){
			// 현재 실행자
			for(Iterator<ProcessExecutorVO> iter = currentExecutorList.iterator(); iter.hasNext();){
				ProcessExecutorVO oldExecutorVo = iter.next();
				if(newExecutorVo.getExecutor_id().equals(oldExecutorVo.getExecutor_id()) 
						|| newExecutorVo.getType().equals(oldExecutorVo.getType())){
					
					// 삭제 후 insert :: insert는 신규 협업자 insert에서 insert
					param.clear();
					param.put("execute_id", oldExecutorVo.getExecute_id());
					processDao.deleteProcessExecutor(param);
					
					iter.remove(); // 사용한 currentExecutorList 값은 remove 시킨다.
				}
			}
		}
		
		// 신규 협업자 insert :: newExecutorList
		for(ProcessExecutorVO insertExecutorVo : newExecutorList){
			String execute_id = CommonUtil.getStringID(Constant.ID_PREFIX_PROCESS_EXECUTOR, commonService.commonNextVal(Constant.COUNTER_ID_PROCESS_EXECUTOR));
			insertExecutorVo.setExecute_id(execute_id);
			insertExecutorVo.setProcess_id(strProcessId);
			insertExecutorVo.setDoc_root_id(strDocRootId);
			
			// 날짜 및 상태값
			if(insertExecutorVo.getType().equals(Constant.PROCESS_TYPE_AUTHOR) || insertExecutorVo.getType().equals(Constant.PROCESS_TYPE_COAUTHOR)){
				insertExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_START);
				insertExecutorVo.setStart_dateDB(CommonUtil.getCurruentTime());	// 오늘 날짜
				
			}else{
				insertExecutorVo.setStatus(Constant.PROCESS_EXECUTOR_WAIT);
				insertExecutorVo.setStart_dateDB(CommonUtil.getCurruentTimeByDate("9999-01-01"));  // 임시 날짜
			}
			
			processDao.insertProcessExecutor(insertExecutorVo);
		}
		
		// 기존 협업자 delete :: currentExecutorList
		for(ProcessExecutorVO delExecutorVo : currentExecutorList){
			param.clear();
			param.put("execute_id", delExecutorVo.getExecute_id());
			processDao.deleteProcess(param);
		}
		
		// 3. 문서에 대한 처리 :: 문서명, 확장문서, 권한, 확장권한, 파일처리
		DocumentVO documentVo = new DocumentVO();
		documentVo.setUpdate_action(Constant.ACTION_PROCESS_UPDATE);
		documentVo.setDoc_id(strDocRootId);
		documentVo.setDoc_name(map.get("name").toString());
//		documentVo.setPage_cnt(Integer.parseInt(map.get("page_cnt").toString()));
		documentVo.setAcl_id(map.get("acl_id").toString());
		documentVo.setDoc_type(map.get("doc_type").toString());
		documentVo.setFolder_id(map.get("folder_id").toString());
		
		int resultCnt = documentDao.documentUpdate(documentVo);
		// 3-1 EX_ACL_ID :: 사용안함 별도 테이블 처리 => XR_EXACLITEM
		List<AclExItemVO> aclExItemList = CommonUtil.jsonArrayToExAclItemList(map);
		documentVo.setAclExItemList(aclExItemList);

		param.clear();
		aclService.aclExItemDelete(param, strDocRootId); // 전체 추가접근자 삭제
		for(AclExItemVO aclExItemVO : documentVo.getAclExItemList()){
			aclExItemVO.setDoc_id(strDocRootId);
			aclService.aclExItemWrite(param, aclExItemVO);
		}
		// 3-2 문서유형 처리
		param.clear();
		List<HashMap<String,Object>> attrList = new ArrayList<HashMap<String,Object>>();
		attrList = documentService.docExtendedAttrList(request,documentVo.getDoc_type()); 	
		documentService.extendTypeWrite(param, Constant.VERSION_SAME_VERSION, attrList, documentVo, typeDao);
		
		// 3-3 파일관련 PAGE_CNT, PAGE_TOTAL
		// xr_document, xr_page, xr_filed :: page_no 확인
		// 1. 삭제 대상 처리 
		List<HashMap<String,Object>> delFileList = new ArrayList<HashMap<String,Object>>();
		delFileList = CommonUtil.jsonArrayToDelFileList(map);
		documentVo.setDelFileList(delFileList);
		
		// 2. 등록 대상 처리
		List<HashMap<String,Object>> fileList = CommonUtil.jsonArrayToFileList(map);
		documentVo.setInsertFileList(fileList);
		
		documentVo.setPage_cnt(Integer.parseInt(map.get("page_cnt").toString()));
		documentService.processFileUpdate(documentVo);
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}

}
