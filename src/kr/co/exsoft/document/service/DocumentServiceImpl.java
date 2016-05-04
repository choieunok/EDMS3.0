package kr.co.exsoft.document.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import kr.co.exsoft.common.dao.CommonDao;
import kr.co.exsoft.common.dao.ConfDao;
import kr.co.exsoft.common.dao.HistoryDao;
import kr.co.exsoft.common.service.CacheService;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.CodeVO;
import kr.co.exsoft.common.vo.CommentVO;
import kr.co.exsoft.common.vo.ConfVO;
import kr.co.exsoft.common.vo.DocumentHtVO;
import kr.co.exsoft.common.vo.RecentlyObjectVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.dao.DocumentDao;
import kr.co.exsoft.document.dao.MyDocumentDao;
import kr.co.exsoft.document.dao.PageDao;
import kr.co.exsoft.document.dao.TypeDao;
import kr.co.exsoft.document.dao.WorkDocumentDao;
import kr.co.exsoft.document.vo.AttrItemVO;
import kr.co.exsoft.document.vo.AttrVO;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.document.vo.PageVO;
import kr.co.exsoft.document.vo.TypeVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.library.ExsoftAbstractServiceImpl;
import kr.co.exsoft.eframework.repository.EXrepClient;
import kr.co.exsoft.eframework.util.ARIAUtil;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.util.PatternUtil;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.folder.dao.FolderDao;
import kr.co.exsoft.folder.service.FolderService;
import kr.co.exsoft.folder.vo.FolderVO;
import kr.co.exsoft.note.dao.NoteDao;
import kr.co.exsoft.note.vo.NoteManageVO;
import kr.co.exsoft.permission.dao.AclDao;
import kr.co.exsoft.permission.service.AclService;
import kr.co.exsoft.permission.vo.AclItemListVO;
import kr.co.exsoft.permission.vo.AclVO;
import kr.co.exsoft.permission.vo.AclExItemVO;
import kr.co.exsoft.permission.vo.AclItemVO;
import kr.co.exsoft.process.dao.ProcessDao;
import kr.co.exsoft.process.service.ProcessService;
import kr.co.exsoft.process.vo.ProcessExecutorVO;
import kr.co.exsoft.process.vo.ProcessVO;
import kr.co.exsoft.user.dao.GroupDao;
import kr.co.exsoft.user.dao.UserDao;
import kr.co.exsoft.user.vo.GroupVO;
import kr.co.exsoft.user.vo.UserVO;
import kr.co.exsoft.note.service.NoteService;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import kr.co.exsoft.eframework.util.PagingAjaxUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Document 서비스 구현 부분
 * @author 패키지 개발팀
 * @since 2014.07.21
 * @version 3.0
 * 
 * [3000][EDMS-REQ-033]	2015-08-24	성예나	 : 만기 문서 사전알림 쪽지 보내기
 * [3001][EDMS-REQ-033]	2015-08-25	성예나	 : 만기 문서 자동알림 쪽지 보내기
 * [3002][EDMS-REQ-015]	2015-09-02	성예나	 : 버전복원시  선택폴더로 문서복원
 * [3003][EDMS-REQ-040]	2015-09-08 	성예나 : 문서등록,수정시 폴더 스토리지용량체크 
 * [3004][EDMS-REQ-040]	2015-09-08 	성예나 : 문서등록,수정시 storage_usage 업데이트처리
 * [3005][EDMS-REQ-040]	2015-09-08 	성예나 : 문서삭제시 storage_usage 업데이트처리
 * [3006][EDMS-REQ-040]	2015-09-09 	성예나 : 문서복원시 storage_usage 업데이트처리
 * [3007][EDMS-REQ-040]	2015-09-09 	성예나 : 문서이동시 storage_usage 업데이트처리
 * [3008][EDMS-REQ-040]	2015-09-09 	성예나 : 버전삭제시 storage_usage 업데이트처리
 * [3009][EDMS-REQ-040]	2015-09-09 	성예나 : 문서복사시 storage_usage 업데이트처리
 * [3010][EDMS-REQ-040]	2015-09-15 	성예나 : 폴더 storage_usage 업데이트 메소드.	
 * [3011][EDMS-REQ-040]	2015-09-17 	성예나 : 문서수정시 현재버전유지일 경우엔  xr_folder 의 storage_usage에 새로추가되는 문서의 용량만큼 더해준다.
 * [2000][신규개발]	2015-09-01	이재민	 : 관리자 > 휴지통 관리 - 복원기능
 * [2001][로직수정]	2015-09-03	이재민 : 문서상세 > 버전탭 - 버전삭제시도시 js에러수정 및 최신버전문서 is_current T로 만드는 로직 추가
 * [2002][로직수정]	2015-09-14	이재민	 : 업무/개인문서함 - Drag&Drop기능 - 여러개문서 선택후 이동가능하게 수정
 * [1000][로직수정]	2015-09-04	최은옥 : 문서 이력 -> 연계이력 추가를 위해 flag 추가 처리
 * [1000][로직수정]	2015-09-07	최은옥 : 연계 문서 이동을 이해 moveCopyDocValidList override
 * [1006][로직수정] 2016-05-02 최은옥:  관리자 휴지통 삭제문서 복원후 문서조회  (폴더 아이디비교로 인한 오류)
 */

@Service("documentService")
public class DocumentServiceImpl extends ExsoftAbstractServiceImpl implements DocumentService {

	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	
	@Autowired
	@Qualifier("sqlSessionBatch")
	private SqlSession sqlSessionBatch;

	@Autowired
	private CommonService commonService;
	
	@Autowired
	private FolderService folderService;
	
	@Autowired
	private AclService aclService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private ProcessService processService;
	
	@Autowired
	NoteService noteservice; 	//[3000],[3001]	
	
	@Override
	public Map<String, Object> expiredDocumentList(HashMap<String, Object> map) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<DocumentVO> ret = new ArrayList<DocumentVO>();
		int total = 0;
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		// 검색어 조건에 따른 변경처리
		if(map.get("orderCol").toString().equalsIgnoreCase("user_name_ko"))	{
			map.put("orderCol","U."+map.get("orderCol").toString());
		}else {
			map.put("orderCol","D."+map.get("orderCol").toString());
		}
				
		total = documentDao.expiredPagingCount(map);
		ret = documentDao.expiredDocumentList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);
		
		// Ajax Paging 
		String strLink = "javascript:expiredManager.event.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),5,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);	
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> duplicateDocList(HashMap<String,Object> map) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> ret = new ArrayList<DocumentVO>();
		int total = 0;
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		total = documentDao.duplicateDocCount(map);
		ret = documentDao.duplicateDocList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);		
		
		// Ajax Paging 
		String strLink = "javascript:exsoftAdminDupFunc.event.gridSubPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		
						
		return resultMap;
	}
	
	@Override
	public List<HashMap<String, Object>> documentValidList(HashMap<String,Object> map) throws Exception {
		
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
		
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);

		/***********************************************************************************************************
		 * 주) 문서(삭제/완전삭제/휴지통) 처리 파라미터정의 JsonArry 형식이며 아래의 필수값 포함해야됨.
		 * delDocList=[
		 * {"doc_id":"DOC000000007536","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007535","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007534","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"}]
		 ***********************************************************************************************************/
		String delDocList =  map.get("delDocList") != null ? map.get("delDocList").toString() : "";
		
		// 1.입력값 유효성 체크
		if(delDocList.equals(""))	{
			throw processException("common.required.error");
		}
			
		// 2.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(delDocList);
		if(jsonArray.size() > 0 ) {		
			 for(int j=0;j < jsonArray.size();j++)	{				 
				 
				 HashMap<String, Object> docInfo = new HashMap<String, Object>();
				 
				 docInfo.put("doc_id",jsonArray.getJSONObject(j).getString("doc_id").toString());
				 docInfo.put("root_id",jsonArray.getJSONObject(j).getString("root_id").toString());
				 docInfo.put("is_locked",jsonArray.getJSONObject(j).getString("is_locked").toString());
				 docInfo.put("type_id",jsonArray.getJSONObject(j).getString("doc_type").toString());
				 
				 //개인문서일때 와 부서/프로젝트일때 구별하기 위한 map_id	[3005]
				 docInfo.put("map_id",jsonArray.getJSONObject(j).getString("map_id").toString());				 
				 
				 // 3.문서유형정보가 삭제불가인지 체크한다. :: parameter : type_id
				 TypeVO typeVO = typeDao.typeDetailInfo(docInfo);
				 if(typeVO != null && typeVO.getIs_modify().equals(Constant.F))	{
					 String[] args = new String[]{typeVO.getType_name()};
					 throw processException("doc.fail.terminate.type.delete",args);					 					 
				 }
				 
				 ret.add(docInfo);
			 }
		}

		return ret;
	}
	
	@Override
	public Map<String, Object> deleteDocument(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSessionBatch.getMapper(HistoryDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);				
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// 0.문서삭제 대상 문서 목록 리스트 구하기
		for(HashMap<String,Object> dMap : docList) {
			
			// 1.모든 버전 문서 목록 리스트 가져오기
			String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
			String doc_id = dMap.get("doc_id").toString();
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_TABLE);
			
			for(DocumentVO vo : allVersionList)	{
				
				/*****************************************************************************************
				// 주) 버전별 삭제는 3.0 부터 지원안함 :: RGATE 문서의 경우 전체버전 삭제처리
				*****************************************************************************************/
				
				// 2.XR_DOCUMENT UPDATE :: DOC_STATUS = C -> D 변경처리
				HashMap<String,Object> param1 = new  HashMap<String, Object>();
				param1.put("isType",Constant.DELETE);
				param1.put("doc_id", vo.getDoc_id());
				param1.put("doc_status", Constant.DOC_STATUS_DELETE);
				param1.put("user_id", sessionVO.getSessId());
				param1.put("user_name",sessionVO.getSessName()); 		
				documentDao.docInfoUpdate(param1);				
				
				// 문서삭제시 xr_folder의 storage_usage 업데이트 처리 [3005]  
				dMap.put("user_id", sessionVO.getSessId());
				storageUsageDeleteControl(Constant.ACTION_DELETE, folderDao, vo, dMap);
				
				// 3.XR_DOCUMENT_HT 등록처리 :: ACTION : DELETE
				 long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
				docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_DELETE, vo.getDoc_type(), vo.getDoc_name(),vo.getVersion_no(),sessionVO);
				
			}
			
			allVersionList.clear();
			
		}	// END OF FOR		

		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> trashDeleteDoc(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSessionBatch.getMapper(HistoryDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);
		CommonDao commonDao = sqlSessionBatch.getMapper(CommonDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();
	
		// 0.관리자휴지통 이관 대상 문서 목록 리스트 구하기
		for(HashMap<String,Object> dMap : docList) {
			
			/*****************************************************************************************
			// 주) 휴지통 문서삭제/비우기 
			 * XR_DOCUMENT => XR_DOCUMENT_DEL 로 데이터 이동처리
			 * XR_LINKED/XR_DOCUMENT/XR_FAVORITE 데이터 삭제처리
			 * XR_FILED/XR_PAGE 데이터 유지 :: 관리자 휴지통삭제/비우기에서 처리함
			*****************************************************************************************/
			// 1.모든 버전 문서 목록 리스트 가져오기
			String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
			String doc_id = dMap.get("doc_id").toString();
			
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_TABLE);
	
			for(DocumentVO vo : allVersionList)	{
				
				// XR_LINKED / XR_FILED 처리 파라미터 선언
				HashMap<String,Object> param = new  HashMap<String, Object>();
				param.put("doc_id",vo.getDoc_id());
				
				// 2. XR_LINKED DELETE :: parameter : doc_id
				folderDao.xrLinkedDelete(param);
				
				// 3. XR_DOCUMENT => XR_DOCUMENT_DEL 이동처리 :: parameter : doc_id
				documentDao.adminTrashMove(param);
				
				// 4.XR_DOCUMNET DELETE
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_TABLE);
				
				// 5. XR_FAVORITE_DOC DELETE
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_FAVORITE_TABLE);
				
				// 6.XR_REF_DOC DELETE :: 관련문서 MAIN
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
				
				// 7,XR_REF_DOC DELETE :: 관련문서 SUB
				docCommonRefDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
				
				// 8. 최근문서 현황 삭제 ::  parameter : user_id,target_id
				param.put("user_id", sessionVO.getSessId());
				param.put("target_id", vo.getDoc_id());
				commonDao.deleteRecently(param);
	
				// 9. XR_COMMENT 삭제 :: parameter : doc_root_id
				String strRootDocId = StringUtil.isEmpty(vo.getRoot_id()) ? vo.getDoc_id() : vo.getRoot_id();
				param.put("doc_root_id", strRootDocId);
				documentDao.deleteCommentRow(param);
				
				// 10. 협업관련 처리 :: service 호출
				param.clear();
				// doc_id jsonArry으로 넘길 것
				param.put("deleteType", "document");
				param.put("processData", "[{\"doc_root_id\":\""+strRootDocId+"\"}]");
				processService.processDelete(sessionVO, param);
				
				// 11.XR_DOCUMENT_HT 등록처리 ::  ACTION : ERASE	 	- 배치프로그램인 경우 sessionVO = sessId s
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
				if(sessionVO.getSessId().equals(""))	{
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_ERASE, vo.getDoc_type(), vo.getDoc_name(),vo.getVersion_no(),map);
				}else {
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_ERASE, vo.getDoc_type(), vo.getDoc_name(),vo.getVersion_no(),sessionVO);
				}
								
			}

		}	// END OF FOR		

		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;		
	}
	
	@Override
	public Map<String, Object> adminTrashDeleteDoc(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
				
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
		HistoryDao historyDao = sqlSessionBatch.getMapper(HistoryDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// 0. 완전삭제 대상 문서 목록 리스트 반복 수행처리
		for(HashMap<String,Object> dMap : docList) {
			
			// 1. 모든 버전 문서 목록 리스트 가져오기 :: 주) 대상테이블 : XR_DOCUMENT_DEL			
			String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
			String doc_id = dMap.get("doc_id").toString();
			
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_DEL_TABLE);
			
			for(DocumentVO vo : allVersionList)	{
				
				// 파라미터 선언
				HashMap<String,Object> param = new  HashMap<String, Object>();
				param.put("doc_id",vo.getDoc_id());
				
				// 2.XR_DOCUMENT_DEL DELETE :: parameter : doc_id , table_nm
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_DEL_TABLE);
				
				// 3.XR_FILE PAGE_LIST 가져와서 FLAG처리 :: parameter : doc_id
				updatePageList(pageDao,vo.getDoc_id());
								
				// 4. XR_FILED  DELETE ::  parameter : doc_id
				pageDao.xrFiledDelete(param);
				
				// 5. XR_DOCUMENT_HT 등록처리 ::  ACTION : TERMINATE
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
				if(sessionVO.getSessId().equals(""))	{
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_TERMINATE,vo.getDoc_type(),vo.getDoc_name(),vo.getVersion_no(), map);
				}else	{
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_TERMINATE,vo.getDoc_type(), vo.getDoc_name(),vo.getVersion_no(),sessionVO);
				}
									
			}		// END OF FOR DocumentVO		
			
			allVersionList.clear();
			
		}	// END OF FOR docList

		resultMap.put("result",Constant.RESULT_TRUE);
			
		return resultMap;
	}
	
	//[1000]
	@Override
	public Map<String, Object> terminateDocument(List<HashMap<String, Object>>  docList,HashMap<String,Object> map,SessionVO sessionVO)
			throws Exception {
		return terminateDocument(docList,map,sessionVO,false,null);
	}
	
	//[1000]
	@Override
	public Map<String, Object> terminateDocument(List<HashMap<String, Object>>  docList,HashMap<String,Object> map,SessionVO sessionVO, boolean isExternal, String actionPlace)
			throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		CommonDao commonDao = sqlSessionBatch.getMapper(CommonDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
		HistoryDao historyDao = sqlSessionBatch.getMapper(HistoryDao.class);		
		TypeDao typeDao = sqlSessionBatch.getMapper(TypeDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// 0. 완전삭제 대상 문서 목록 리스트 반복 수행처리
		for(HashMap<String,Object> dMap : docList) {

			// 1. 모든 버전 문서 목록 리스트 가져오기			
			String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
			String doc_id = dMap.get("doc_id").toString();
			
			// ROOT_ID => DOC_ID 변경처리
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_TABLE);
						
			for(DocumentVO vo : allVersionList)	{

				// 2.문서확장 속성 삭제처리 문서유형 정보 가져온다.
				TypeVO typeVO = typeAttrDelete(typeDao,vo);
				
				// 3.XR_DOCUMENT DELETE :: parameter : doc_id , table_nm
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_TABLE);

				// 4. XR_FAVORITE_DOC DELETE :: parameter : doc_id , table_nm
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_FAVORITE_TABLE);
				
				// 5.XR_REF_DOC DELETE :: 관련문서 MAIN
				docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
				
				// 6,XR_REF_DOC DELETE :: 관련문서 SUB
				docCommonRefDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
				
				// 7.XR_FILE PAGE_LIST 가져와서 FLAG처리 :: parameter : doc_id
				updatePageList(pageDao,vo.getDoc_id());

				// XR_LINKED / XR_FILED 처리 파라미터 선언
				HashMap<String,Object> param = new  HashMap<String, Object>();
				param.put("doc_id",vo.getDoc_id());
								
				// 8. XR_LINKED DELETE :: parameter : doc_id
				folderDao.xrLinkedDelete(param);
				
				// 9. XR_FILED  DELETE ::  parameter : doc_id
				pageDao.xrFiledDelete(param);
				
				// 10. 최근문서 현황 삭제 ::  parameter : user_id,target_id
				param.put("user_id", sessionVO.getSessId());
				param.put("target_id", vo.getDoc_id());
				commonDao.deleteRecently(param);
	
				// 11. XR_COMMENT 삭제 :: parameter : doc_root_id
				String strRootDocId = StringUtil.isEmpty(vo.getRoot_id()) ? vo.getDoc_id() : vo.getRoot_id();
				param.put("doc_root_id", strRootDocId);
				documentDao.deleteCommentRow(param);
				
				// 12. 협업관련 처리 :: service 호출
				param.clear();
				// doc_id jsonArry으로 넘길 것
				param.put("deleteType", "document");
				param.put("processData", "[{\"doc_root_id\":\""+strRootDocId+"\"}]");
				processService.processDelete(sessionVO, param);
				
				// 13. XR_DOCUMENT_HT 등록처리 ::  ACTION : TERMINATE
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
				
				//[1000]
				if(isExternal){
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_TERMINATE,typeVO.getType_id(),vo.getDoc_name(),vo.getVersion_no(),sessionVO,isExternal, actionPlace);
				}else{
					docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_TERMINATE,typeVO.getType_id(),vo.getDoc_name(),vo.getVersion_no(),sessionVO);	
				}
			}		// END OF FOR

			allVersionList.clear();
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> versionDocDelete(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);
		PageDao pageDao = sqlSessionBatch.getMapper(PageDao.class);
		HistoryDao historyDao = sqlSessionBatch.getMapper(HistoryDao.class);		
		TypeDao typeDao = sqlSessionBatch.getMapper(TypeDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// 0. 완전삭제 대상 문서 목록 리스트 반복 수행처리
		for(HashMap<String,Object> dMap : docList) {

			String root_id = StringUtil.getMapString(map, "root_id").toString().equals("") ? StringUtil.getMapString(map, "doc_id").toString() : StringUtil.getMapString(map, "root_id").toString();
			
			// 1.버전삭제문서 정보 가져오기 :: parameter doc_id
			DocumentVO vo = documentDao.commonDocDetail(dMap);
			
			// 2.문서확장 속성 삭제처리 문서유형 정보 가져온다.
			TypeVO typeVO = typeAttrDelete(typeDao,vo);
			
			// 3.XR_DOCUMENT DELETE :: parameter : doc_id , table_nm
			docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_TABLE);

			// 4. XR_FAVORITE_DOC DELETE :: parameter : doc_id , table_nm
			docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_FAVORITE_TABLE);
			
			// 5.XR_REF_DOC DELETE :: 관련문서 MAIN
			docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
			
			// 6,XR_REF_DOC DELETE :: 관련문서 SUB
			docCommonRefDelete(documentDao,vo.getDoc_id(),Constant.DOC_REF_TABLE);
			
			// 7.XR_FILE PAGE_LIST 가져와서 FLAG처리 :: parameter : doc_id
			updatePageList(pageDao,vo.getDoc_id());

			// XR_LINKED / XR_FILED 처리 파라미터 선언
			HashMap<String,Object> param = new  HashMap<String, Object>();
			param.put("doc_id",vo.getDoc_id());			


			
			// 8. XR_LINKED DELETE :: parameter : doc_id
			param.clear();	
			param.put("doc_id",vo.getDoc_id());	
			folderDao.xrLinkedDelete(param);

			// 9. XR_FILED  DELETE ::  parameter : doc_id
			pageDao.xrFiledDelete(param);
			
			// [2001] Start
			// 10. 최신문서의 is_current 를 T로 만들기
			List <DocumentVO> currentDocList = null;
			// 10-1. root_id로 검색
			param.clear();
			param.put("root_id", root_id);
			currentDocList = documentDao.selectRecentDocList(param);
			
			// 10-2. root_id로 검색시 없으면 doc_id로 검색
			if(currentDocList.size() <= 0) {
				param.clear();
				param.put("doc_id", root_id);
				currentDocList = documentDao.selectRecentDocList(param);
			}					

			// 10-3. XR_DOCUMENT :: UPDATE
			DocumentVO docVO = new DocumentVO();
			docVO.setUpdate_action("COMMON");
			docVO.setDoc_id(currentDocList.get(0).getDoc_id());
			docVO.setVersion_no(currentDocList.get(0).getVersion_no());
			docVO.setPage_total(currentDocList.get(0).getPage_total());
			docVO.setAccess_grade(currentDocList.get(0).getAccess_grade());
			docVO.setSecurity_level(currentDocList.get(0).getSecurity_level());
			docVO.setPage_cnt(currentDocList.get(0).getPage_cnt());
			docVO.setIs_current("T");
			
			documentDao.documentUpdate(docVO);

			// [2001] End			

			// 10. XR_DOCUMENT_HT 등록처리 ::  ACTION : TERMINATE
			long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
			docHistoryWrite(historyDao,doc_seq,root_id,vo.getDoc_id(),Constant.ACTION_VERSION_TERMINATE,typeVO.getType_id(), vo.getDoc_name(),vo.getVersion_no(),sessionVO);
			
			// [2001] return시 doc_id를 함께보내 상세창 refresh에 사용
			resultMap.put("doc_id",docVO.getDoc_id());
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
			
		return resultMap;
	}
	
	@Override
	//	versionDocDelete 안에서 storage_usage업데이트 처리를 할 경우 storage_usage가 null이라고 발생해서 업데이트 처리를 따로 생성.		[3008]
	public Map<String, Object> versionDocDeleteStorageUpdate(List<HashMap<String, Object>> docList, SessionVO sessionVO)throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		FolderDao folderDao = sqlSessionBatch.getMapper(FolderDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		for(HashMap<String,Object> dMap : docList) {
			
			// 1.버전삭제문서 정보 가져오기 :: parameter doc_id
			DocumentVO vo = documentDao.commonDocDetail(dMap);
			
			// 버전삭제시 xr_folder의 storage_usage 업데이트 처리 [3008]
			HashMap<String,Object> param = new  HashMap<String, Object>();
			
			param.put("map_id",vo.getMap_id());
			param.put("user_id",sessionVO.getSessId());
			storageUsageDeleteControl(Constant.ACTION_VERSION_TERMINATE, folderDao, vo, param);						
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}
	

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서확장 속성 삭제처리 :: 공통함수
	 * 2. 처리내용 : sqlSessionBatch / sqlSession 객체에 따른 호출처리 
	 * </pre>
	 * @Method Name : typeAttrDelete
	 * @param typeDao
	 * @param vo
	 * @return TypeVO
	 */
	public TypeVO typeAttrDelete(TypeDao typeDao,DocumentVO vo) {

		// 1. 문서유형 속성 정보 구하기.
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("type_id", vo.getDoc_type());
		TypeVO typeVO  = typeDao.typeDetailInfo(param);
		
		// 2.확장 문서유형이면서 확장 속성이 존재하는 경우 속성 삭제처리
		if (typeVO.getIs_base().equals(Constant.F) && !typeVO.getTbl_name().equals("")) {
			param.put("tbl_name",typeVO.getTbl_name());
			param.put("doc_id", vo.getDoc_id());
			typeDao.attrValueDelete(param);
		}
			
		// 3.문서확장 속성삭제 후 객체 리턴..		
		return typeVO;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 모든 버전 문서 목록 리스트 가져오기 :: 공통함수
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docAllVersionList
	 * @param documentDao
	 * @param doc_id
	 * @return List<DocumentVO>
	 */
	public List<DocumentVO> docAllVersionList(DocumentDao documentDao,String doc_id,String table_nm) {
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("doc_id",doc_id);
		param.put("table_nm",table_nm);
		
		// 1.DOC_ID 로 모든 문서 목록 리스트 가져오기
		docList = documentDao.allVersionDocumentList(param);
		
		// 2. ROOT_ID 중복 제거처리.
		List<DocumentVO> ret = new ArrayList<DocumentVO>(new HashSet<DocumentVO>(docList));

		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 모든 버전 문서와 첨부파일 목록 리스트 가져오기 :: 공통함수
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docAllVersionInfoList
	 * @param documentDao
	 * @param root_id
	 * @return List<DocumentVO>
	 */
	public Map<String, Object> docAllVersionInfoList(HashMap<String, Object> map) {
		HashMap<String,Object> resultMap = new  HashMap<String, Object>();
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);

		// 테이블명 구분처리 :: XR_DOCUMENT_DEL 의 경우 TYPE_ID가 없는 경우도 발생할 수 있음
		String table_nm = map.get("table_nm") != null  ? map.get("table_nm").toString() : Constant.DOC_TABLE;
		
		// 1.ROOT_ID 로 모든 문서 목록 리스트 가져오기
		if(table_nm.equals(Constant.DOC_TABLE))	{
			docList = documentDao.allVersionDocumentList(map);
		}else {
			docList = documentDao.allVersionDelDocumentList(map);
		}
		
		// 2. ROOT_ID 중복 제거처리.
		List<DocumentVO> ret = new ArrayList<DocumentVO>(new HashSet<DocumentVO>(docList));

		// 3. 첨부파일 조회
		for (DocumentVO doc : ret) {
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("doc_id", doc.getDoc_id());
			doc.setPageList(pageDao.comDocPageList(param));
		}
		
		// 4. 반환
		resultMap.put("docAllVersionList", docList);
		
		return resultMap;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서삭제처리 :: 공통함수 
	 * 2. 처리내용 : 
	 * 		XR_DOCUMENT,XR_DOCUMENT_DEL,XR_FAVORITE_DOC 등
	 *      sqlSessionBatch 사용시 return 값은 사용하지 않도록 한다. 
	 * </pre>
	 * @Method Name : docCommonDelete
	 * @param documentDao
	 * @param doc_id
	 * @param table_nm
	 * @return int
	 */
	public int docCommonDelete(DocumentDao documentDao,String doc_id,String table_nm) {
		
		int result = 0;
		
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("doc_id",doc_id);
		param.put("table_nm",table_nm);	

		result = documentDao.docCommonDelete(param);		
				
		return result;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서삭제처리 :: 공통함수 XR_REF_DOC
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docCommonRefDelete
	 * @param documentDao
	 * @param doc_id
	 * @param table_nm
	 * @return int
	 */
	public int docCommonRefDelete(DocumentDao documentDao,String doc_id,String table_nm) {
		
		int result = 0;
		
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("ref_doc",doc_id);
		param.put("table_nm",table_nm);
	
		result = documentDao.docCommonDelete(param);
		
		return result;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : XR_FILED 의 페이지 리스트를 가져와서 삭제FLAG 처리를 한다.
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : updatePggeList
	 * @param pageDao
	 * @param doc_id void
	 */
	public void updatePageList(PageDao pageDao,String doc_id) {
	
		List<CaseInsensitiveMap> pageList = new ArrayList<CaseInsensitiveMap>();
		
		HashMap<String,Object> param1 = new  HashMap<String, Object>();
		param1.put("doc_id",doc_id);
		
		// 1.XR_FILED 에서 페이지 리스트를 가져온다.
		pageList = pageDao.xrPageList(param1);
		
		if(pageList.size() > 0 )	{
			
			for(CaseInsensitiveMap caseMap : pageList )	{
				
				HashMap<String,Object> param2 = new  HashMap<String, Object>();
				param2.put("is_deleted",Constant.T	);
				param2.put("page_id",caseMap.get("page_id"));
				
				// 2. XR_PAGE IS_DELETED FLAG 처리 :: 물리적 파일 삭제는 배치 프로그램에서 수행함 parameter : doc_id , is_deleted				
				pageDao.pageInfoUpdate(param2);
			}
		}
		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서이력처리 :: 공통함수
	 * 2. 처리내용 : XR_DOCUMENT_HT
	 * </pre>
	 * @Method Name : documentHtWrite
	 * @param historyDao
	 * @param doc_seq : 이력SEQ
	 * @param root_id : 문서 ROOT_ID
	 * @param doc_id : 문서 ID
	 * @param action_type : 등록/수정/삭제/완전삭제(폐기)/체크인/체크아웃 등
	 * @param type_id : 문서유형 ID
	 * @param doc_name : 문서제목
	 * @param version_no : 문서버전
	 * @param sessionVO : 세션객체
	 * @return int
	 */
	//[1000]
	public int docHistoryWrite(HistoryDao historyDao,long doc_seq,String root_id,String doc_id,String action_type,
			String type_id,String doc_name,String version_no,SessionVO sessionVO) {
		return docHistoryWrite(historyDao,doc_seq,root_id, doc_id, action_type, type_id, doc_name, version_no, sessionVO,false,null);
	}
	//[1000]
	public int docHistoryWrite(HistoryDao historyDao,long doc_seq,String root_id,String doc_id,String action_type,
			String type_id,String doc_name,String version_no,SessionVO sessionVO, boolean isExternal, String actionPlace) {
		
		int result = 0;
		
		if(root_id == null || root_id.equals(""))	{
			root_id = doc_id;
		}
		
		DocumentHtVO documentHtVO = CommonUtil.setDocumentHistory(doc_seq, root_id, doc_id, action_type, type_id, doc_name,version_no,sessionVO,isExternal,actionPlace);
		result = historyDao.documentHtWrite(documentHtVO);
		
		return result;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 :  문서이력처리 :: 공통함수 배치프로그램
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docHistoryWrite
	 * @param historyDao
	 * @param doc_seq
	 * @param root_id
	 * @param doc_id
	 * @param action_type
	 * @param type_id
	 * @param doc_name : 문서제목
	 * @param version_no : 문서버전
	 * @param systemUser : 시스펨관리자 정보
	 * @return int
	 */
	public int docHistoryWrite(HistoryDao historyDao,long doc_seq,String root_id,String doc_id,String action_type,
			String type_id,String doc_name,String version_no,HashMap<String,Object> systemUser) {
		
		int result = 0;
		
		if(root_id == null || root_id.equals(""))	{
			root_id = doc_id;
		}
		
		DocumentHtVO documentHtVO = CommonUtil.setDocumentHistory(doc_seq, root_id, doc_id, action_type, type_id, doc_name,version_no,systemUser);
		result = historyDao.documentHtWrite(documentHtVO);
		
		return result;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서이력처리 :: 공통함수(문서이동/소유권변경시만 사용함
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docHistoryWrite
	 *  * @Method Name : documentHtWrite
	 * @param historyDao
	 * @param doc_seq : 이력SEQ
	 * @param root_id : 문서 ROOT_ID
	 * @param doc_id : 문서 ID
	 * @param action_type : 등록/수정/삭제/완전삭제(폐기)/체크인/체크아웃 등
	 * @param type_id : 문서유형 ID
	 * @param doc_name : 문서제목
	 * @param version_no : 문서버전
	 * @param sessionVO : 세션객체
	 * @param targetMap : 문서이동/소유권변경시 객체 정보 
	 * @return int
	 */
	public int docHistoryWrite(HistoryDao historyDao,long doc_seq,String root_id,String doc_id,String action_type,
			String type_id,String doc_name,String version_no,HashMap<String,Object> targetMap,SessionVO sessionVO) {
		
		int result = 0;
		
		if(root_id == null || root_id.equals(""))	{
			root_id = doc_id;
		}
		
		DocumentHtVO documentHtVO = CommonUtil.setDocumentHistory(doc_seq, root_id, doc_id, action_type, type_id,doc_name,version_no,targetMap,sessionVO);
		result = historyDao.documentHtWrite(documentHtVO);
		
		return result;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : STORAGE_USAGE 업데이트처리 :: +되는 항목들	[3010]
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : storageUsageUpdateControl
	 * @param action_id
	 * @param folderDao
	 * @param docVO
	 * @param map
	 * @return void
	 */
//	public void storageUsageUpdateControl(String action_id, FolderDao folderDao, DocumentVO docVO, HashMap<String, Object> map) {
//		
//		HashMap<String,Object> param = new HashMap<String,Object>();
//		
//		String map_id = map.get("map_id") != null ? map.get("map_id").toString() : "";
//		
//		// map_id가 MYPAGE이고 이동이 아닐때 (개인문서함에서 이동시에는 storate_update가 필요하지 않음.)
//		if(map_id.equals(Constant.MAP_ID_MYPAGE) && !action_id.equals(Constant.ACTION_MOVE)) {
//			
//			// 등록 / 수정 / 복사일때 (storage_usage가 더해져야 한다.)
//			if(action_id.equals(Constant.ACTION_UPDATE) || action_id.equals(Constant.ACTION_CREATE) || action_id.equals(Constant.ACTION_COPY)) {	
//				
//				// 개인문서함일때 storage_usage업데이트는 는 folder_id가 최상의루트인 자기자신폴더여야 한다.(root_folder_id))
//				param.put("folder_id", map.get("root_folder_id"));
//				
//			// 복원일때 (storage_usage가 더해져야 한다.)
//			} else if(action_id.equals(Constant.ACTION_RESTORE)) {
//				param.put("folder_id", map.get("user_id"));
//			}
//			param.put("doc_id", docVO.getDoc_id());
//		} else {
//			if(action_id.equals(Constant.ACTION_RESTORE) || action_id.equals(Constant.ACTION_MOVE)) {
//				param.put("folder_id", map.get("target_folder_id"));
//			} else {
//				param.put("folder_id",docVO.getFolder_id());
//			}
//			param.put("doc_id",docVO.getDoc_id());
//		}
//		
//		//doc_id, folder_id로 업데이트처리
//		folderDao.storageUsageUpdate(param);
//	}
	
	
	public void storageUsageUpdateControl(String action_id, FolderDao folderDao, DocumentVO docVO, HashMap<String, Object> map) {
	
	HashMap<String,Object> param = new HashMap<String,Object>();	
	String map_id = map.get("map_id") != null ? map.get("map_id").toString() : "";
	// 이동,복사시엔 version_type이 없음으로 처리
	String version_type =  map.get("version_type") != null ? map.get("version_type").toString() :Constant.VERSION_NEW_DOCUMENT;
	long total_size = Long.valueOf(map.get("total_size").toString());
	
	//문서수정시 현재버전유지 이면서 추가되는파일이 있을때는 추가문서용량만 업데이트를한다.
	if(version_type.equals(Constant.VERSION_SAME_VERSION) && total_size >0  ) {
		//total_size는 새로추가되는 문서사이즈
		param.put("total_size",total_size);
		param.put("update_action","sameverfile");
		
		if(map.get("map_id").equals(Constant.MAP_ID_MYPAGE)){
			param.put("folder_id", map.get("root_folder_id"));	
		}else{				
			param.put("folder_id",docVO.getFolder_id());			
		}
		//doc_id, folder_id로 업데이트처리
		folderDao.storageUsageUpdate(param);
		
	// 현재버전유지업데이트가 아닐 경우 (현재버전유지업데이트는 폴더strage_usage의 업데이트가 불필요함.)
	}else if(!version_type.equals(Constant.VERSION_SAME_VERSION)){
		param.put("update_action","nosamever");
		
		// map_id가 MYPAGE이고 이동이 아닐때 (개인문서함에서 이동시에는 storate_update가 필요하지 않음.)
		if(map_id.equals(Constant.MAP_ID_MYPAGE) && !action_id.equals(Constant.ACTION_MOVE)) {
			
			// 등록 / 수정 / 복사일때 (storage_usage가 더해져야 한다.)
			if(action_id.equals(Constant.ACTION_UPDATE) || action_id.equals(Constant.ACTION_CREATE) || action_id.equals(Constant.ACTION_COPY)) {	
				
				// 개인문서함일때 storage_usage업데이트는 는 folder_id가 최상의루트인 자기자신폴더여야 한다.(root_folder_id))
				param.put("folder_id", map.get("root_folder_id"));
				
			// 복원일때 (storage_usage가 더해져야 한다.)
			} else if(action_id.equals(Constant.ACTION_RESTORE)) {
				param.put("folder_id", map.get("user_id"));
			}
			param.put("doc_id", docVO.getDoc_id());
		} else {
			if(action_id.equals(Constant.ACTION_RESTORE) || action_id.equals(Constant.ACTION_MOVE)) {
				param.put("folder_id", map.get("target_folder_id"));
			} else {
				param.put("folder_id",docVO.getFolder_id());
			}
			param.put("doc_id",docVO.getDoc_id());
		}
		//doc_id, folder_id로 업데이트처리
		folderDao.storageUsageUpdate(param);
	}
		
	
}
	
	
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : STORAGE_USAGE 업데이트처리 :: -되는 항목들	[3010]
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : storageUsageDeleteControl
	 * @param action_id
	 * @param folderDao
	 * @param docVO
	 * @param map
	 * @return void
	 */
	public void storageUsageDeleteControl(String action_id, FolderDao folderDao, DocumentVO docVO, HashMap<String, Object> map) {
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		String map_id = map.get("map_id") != null ? map.get("map_id").toString() : "";
		
		// map_id가 MYPAGE이고 이동이 아닐때 (개인문서함에서 이동시에는 storate_update가 필요하지 않음.)
		if(map_id.equals(Constant.MAP_ID_MYPAGE) && !action_id.equals(Constant.ACTION_MOVE)) {
			if(action_id.equals(Constant.ACTION_DELETE) || action_id.equals(Constant.ACTION_VERSION_TERMINATE)) {
				
				// 개인문서함일때 storage_usage업데이트는 는 folder_id가 최상의루트인 자기자신폴더여야 한다.(user_id))
				param.put("folder_id", map.get("user_id"));

			}
			param.put("doc_id", docVO.getDoc_id());
		} else {
			if(action_id.equals(Constant.ACTION_MOVE)) {
				param.put("folder_id", map.get("folder_id"));
			} else {
				param.put("folder_id",docVO.getFolder_id());
			}
			param.put("doc_id",docVO.getDoc_id());
		}
		//doc_id, folder_id로 업데이트처리
		folderDao.storageUsageDelete(param);
	}
	
	@Override
	public Map<String, Object> wasteDocList(HashMap<String, Object> map) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> ret = new ArrayList<DocumentVO>();
		int total = 0;
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		// 검색어 조건에 따른 변경처리
		if(map.get("orderCol").toString().equalsIgnoreCase("user_name_ko"))	{
			map.put("orderCol","U."+map.get("orderCol").toString());
		}else {
			map.put("orderCol","D."+map.get("orderCol").toString());
		}
		
		total = documentDao.wastePagingCount(map);
		ret = documentDao.wasteList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);		
				
		// Ajax Paging 
		String strLink = "javascript:exsoftAdminDocFunc.event.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),5,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		
						
		return resultMap;
	}


	@Override
	public Map<String, Object> preservationYearUpdate(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);	
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		String[] docIdList = StringUtil.getMapString(map, "docIdList").split(",");
		String sPreservationYear = StringUtil.getMapString(map, "preservation_year");

		
		// 유효성 검사
		if (docIdList.length == 0 || sPreservationYear.equals("")) {
			throw processException("common.required.error");
		}
		
		// 보존연한에 따라 파기 예정일자를 얻는다
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar preservationDate = Calendar.getInstance();
		
		if (sPreservationYear.equals("0")) {
			preservationDate.setTime(sdf.parse("9999-12-31"));
		} else {
			preservationDate.setTime(new Date());
		    preservationDate.add(Calendar.YEAR, Integer.parseInt(sPreservationYear));
		}
	    
		
		for (String docId : docIdList) {
			// 모든 버전의 문서 목록을 조회한다.
			List<DocumentVO> allDocList = docAllVersionList(documentDao, docId, Constant.DOC_TABLE);
		
			for (DocumentVO doc : allDocList) {
				// 문서 보존기한 연장
				HashMap<String,Object> param = new  HashMap<String, Object>();
				param.put("doc_id", doc.getDoc_id());
				param.put("preservation_year", sPreservationYear);
				param.put("expired_date", sdf.format(preservationDate.getTimeInMillis()));
				
				documentDao.preservationYearUpdate(param);
				
				// 히스토리 저장 : 임시코드(root id == null 처리)
				if (doc.getRoot_id().equals("")) doc.setRoot_id(doc.getDoc_id());
				
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);
				docHistoryWrite(historyDao,doc_seq,doc.getRoot_id(),doc.getDoc_id(),Constant.ACTION_UPDATE,doc.getDoc_type(),doc.getDoc_name(),doc.getVersion_no(),sessionVO);
			}
			
		}
		
		resultMap.put("result", Constant.RESULT_TRUE);
		return resultMap;
	}
	
	//[1000]
	@Override
	public Map<String, Object> docCommonView(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		return docCommonView(map,sessionVO,false);
	}
	
	//[1000]
	@Override
	public Map<String, Object> docCommonView(HashMap<String, Object> map, SessionVO sessionVO,boolean isExternal) throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		AclDao aclDao = sqlSession.getMapper(AclDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);	
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);	
		ProcessDao processDao = sqlSession.getMapper(ProcessDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();

		// 0.입력값 유효성 체크 :: 문서ID
		if(map.get("doc_id") == null && map.get("table_nm") == null ) {
			throw processException("common.required.error");	
		}
		
		// 1.문서기본정보 :: XR_DOCUMENT / XR_DOCUMENT_DEL
		DocumentVO documentVO = new DocumentVO();
		if(map.get("table_nm").toString().equals(Constant.DOC_TABLE))	{
			documentVO = documentDao.commonDocDetail(map);	
		}else {
			documentVO = documentDao.commonDocWasteDetail(map);
		}
				
		resultMap.put("documentVO",documentVO);
				
		String root_id = !StringUtil.isEmpty(documentVO.getRoot_id()) ? documentVO.getRoot_id() : documentVO.getDoc_id();
		
		// 1.1 문서수정-체크아웃에만 적용됨
		if(map.get("actionType") != null && 
				map.get("actionType").toString().equals(Constant.ACTION_CHECKOUT)) {
			
			if(documentVO.getIs_locked().equals(Constant.T)) {
				
				// 1.2 문서잠금 유저가 동일한지 체크한다.
				if(!documentVO.getLock_owner().equals(sessionVO.getSessId())) {
					throw processException("doc.fail.lock.user");	
				}
				
			}else {
				
				// 1.3 문서를 잠근다. CHECK OUT				
				documentVO.setUpdate_action(Constant.ACTION_CHECKOUT);
				documentVO.setIs_locked(Constant.T);
				documentVO.setLock_owner(sessionVO.getSessId());
				
				int result = documentDao.documentUpdate(documentVO);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// 1.4. XR_DOCUMENT_HT 등록처리 :: ACTION - CHECKOUT
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);	
				docHistoryWrite(historyDao,doc_seq,root_id,documentVO.getDoc_id(),Constant.ACTION_CHECKOUT,documentVO.getDoc_type(), documentVO.getDoc_name(),documentVO.getVersion_no(),sessionVO);
			}
			
		}	// END OF 문서수정에서만 사용처리
		
		// 2.문서폴더 전체경로 :: XR_FOLDER
		param.put("folder_id",documentVO.getFolder_id());
		
		// 2.1 폴더 문서 저장 유형 정보 
		FolderVO folderVO = folderDao.folderDetail(param);
		
		// 2.2 삭제된 폴더의 경우 폴더정보가 없을 수 있다.
		if(folderVO != null)	{
			resultMap.put("is_type",folderVO.getIs_type());	// 폴더의 기본문서유형					
			resultMap.put("folderPath",commonService.folderFullPath(param));
		}else {
			resultMap.put("is_type",Constant.DOC_TABLE);
			resultMap.put("folderPath","Deleted Folder");
		}
		// 3.문서권한정보 :: XR_ACLITEM / XR_EXACLITEM		

		// 3.1 문서 기본 ACL 정보 :: parameter : acl_id
		param.put("acl_id",documentVO.getAcl_id());		
		AclVO aclVO = aclDao.aclDetail(param);
		resultMap.put("aclDetail",aclVO);
		
		// 3.2 문서 기본 ACLITEM 정보 :: parameter : doc_id , is_type
		param.put("is_type",Constant.D);		// ACL 문서 권한
		List<AclItemListVO> aclItemList = aclService.aclItemList(param);
		resultMap.put("aclItemList",aclItemList);
		
		// 3.3 문서 확장 ACLITEM 정보 :: parameter : doc_id
		param.put("doc_id",documentVO.getDoc_id());					
		List<AclExItemVO> aclExItemList = aclDao.aclExDetailItems(param);
		resultMap.put("aclExItemList",aclExItemList);
			
		// 관리자 휴지통 문서확장 정보 SKIPPED
		if(map.get("table_nm") .toString().equals(Constant.DOC_TABLE))	{
				
			// 3.4 문서확장 속성 정보 가져오기(XR_TYPE IS_SYSTEM=F) :: 사용자화면 개발시 추후 검증예정	
			if(documentVO.getIs_system().equals(Constant.F))	{			
				List<AttrVO>  attrList = docExtendAttrList(typeDao,documentVO);
				resultMap.put("attrList",attrList);			
			}
		}
		
		// 4.문서첨부파일정보 :: XR_FILED/XR_PAGE :: parameter : doc_id
		List<PageVO> pageList = pageDao.comDocPageList(map);
		resultMap.put("pageList",pageList);
		
		// 5.XR_DOCUMENT_HT 등록처리 :: 문서조회=READ
		if(map.get("table_nm") .toString().equals(Constant.DOC_TABLE))	{
			long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
			//[1000] 외부연계는 별도 히스토리 
			if(!isExternal){
				docHistoryWrite(historyDao,doc_seq,root_id,documentVO.getDoc_id(),Constant.ACTION_READ,documentVO.getDoc_type(),documentVO.getDoc_name(),documentVO.getVersion_no(),sessionVO);
			}
		}
		
		// 6.Role 코드값
		param.put("gcode_id", Constant.CODE_POSITION);
		List<CodeVO> positionList = commonService.codeList(param);
		resultMap.put("positionList", positionList);
		
		// 7.보안등급 코드값
		param.put("gcode_id", Constant.CODE_SECURITY_LEVEL);
		List<CodeVO> securityList = commonService.codeList(param);
		resultMap.put("securityList", securityList);
		
		if(map.get("table_nm") .toString().equals(Constant.DOC_TABLE))	{
			
			// 8.관련문서 목록 가져오기 :: doc_id => root_id 로 변경처리
			param.put("doc_id",root_id);				
			
			List<DocumentVO> refDocumentList = documentDao.refDocumentList(param);
			resultMap.put("refDocumentList", refDocumentList);
			
			// 9.다차원 분류 목록 가져오기 :: 파라미터 doc_id
			List<String> multiFolders = documentDao.multiFolderList(map);
			List<HashMap<String,Object>> multiFolderList = new ArrayList<HashMap<String,Object>>();
			if(multiFolders != null && multiFolders.size() > 0 ){			
				for(String folder_id : multiFolders) {
					HashMap<String, Object> folderMap = new HashMap<String, Object>();
					folderMap.put("folder_id",folder_id);
					folderMap.put("folder_path",commonService.folderFullPath(folderMap));
					multiFolderList.add(folderMap);
				}			
			}
			
			resultMap.put("multiFolderList", multiFolderList);
		}
		
		// 10. 로그인 사용자의 문서접근 권한을 설정 (상세 조회일 경우만)
		if(map.get("actionType") != null && 
			map.get("actionType").toString().equals(Constant.ACTION_READ)) {
			
			// 문서권한메뉴 & 폴더ID 파라미터 추가(2015/01/08)
			map.put("folder_id",documentVO.getFolder_id());
			map.put("document_menu_part",CommonUtil.getMenuPart(sessionVO, Constant.USER_DOC_MENU_CODE));
			map.put("manage_group_id", sessionVO.getSessManage_group());
			
			setDocumentAcl(documentVO, map);
		}
		
		// 의견 건수 조회
		int comInt= documentDao.docCommentPagingCount(root_id);
		resultMap.put("commentCnt", comInt);
		
		// 협업정보 조회 (상세 조회일 경우 - 협업정보 / 처리현황 조회)
		param.clear();
		param.put("doc_root_id",root_id);
		ProcessVO processVO = processDao.processInfo(param);
		
		if(processVO != null && processVO.getStatus().equals(Constant.PROCESS_STATUS_END)){
			param.clear();
			param.put("process_id", processVO.getProcess_id());
			param.put("executor_id", sessionVO.getSessId());
			param.put("type", Constant.PROCESS_TYPE_RECEIVER); // 수신자
			ProcessExecutorVO peVO = processDao.getExecutorStatus(param);
			
			// 열람완료가 아니면 update :: 현재 조회 사용자가 수신자가 아닐경우 peVO값은 null
			if(peVO != null && !peVO.getStatus().equals(Constant.PROCESS_STATUS_END)) {
				peVO.setExecute_id(peVO.getExecute_id());
				peVO.setStart_dateDB(CommonUtil.getCurruentTime());
				peVO.setEnd_dateDB(CommonUtil.getCurruentTime());
				peVO.setStatus(Constant.PROCESS_STATUS_END); // 열람완료
				processDao.updateProcessExecutor(peVO);
			} // 열람완료이면 bypass
		}
		
		resultMap.put("process_id", processVO != null ? processVO.getProcess_id() : "");
		
		// ** 결과 메세지 처리
		resultMap.put("result", Constant.RESULT_TRUE);
		return resultMap;
	}

	@Override
	public Map<String, Object> docCommonUpdateValid(HashMap<String,Object> map,SessionVO sessionVO) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 1.문서정보 및 map_id 정보를 가져온다.
		
		// 2. 개인map인 경우 owner_id = session 정보 일치 확인
		
		// 3.업무문서함인 경우 문서의 권한을 확인한다. 
		
		// 4.권한정보를 넘겨준다. 
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> ownerDocList(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> ret = new ArrayList<DocumentVO>();
		int total = 0;
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		// 검색어 조건에 따른 변경처리
		if(map.get("orderCol").toString().equalsIgnoreCase("user_name_ko"))	{
			map.put("orderCol","U."+map.get("orderCol").toString());
		}else {
			map.put("orderCol","D."+map.get("orderCol").toString());
		}
		
		total = documentDao.ownerPagingCount(map);
		ret = documentDao.ownerDocumentList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);		
		
		// Ajax Paging 
		String strLink = "javascript:exsoftAdminDocFunc.event.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),5,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		
						
		return resultMap;
	}


	@Override
	public Map<String, Object> changeDocOwner(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> docParam = new HashMap<String, Object>();
		List<DocumentVO> changeDocList = new ArrayList<DocumentVO>();

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		
		String targetUserId = StringUtil.getMapString(map, "targetUserId");
		HashMap<String, Object> changeOwnerHT = changeOwnerDocumentHT(sessionVO.getSessId(), targetUserId);
		List<String> selectDocList = StringUtil.getMapStringArray(map, "selectDocList", ",");
		
		
		// # 소유권 이전 형태에 따라 이전 대상 문서를 구성한다
		switch (StringUtil.getMapString(map, "changeType")) {
		
			// 1. 선택한 문서를 소유권 이전함
			case Constant.CHANGE_SELECT_DOC :
				docParam.put("table_nm", Constant.DOC_TABLE);
				
				for (String docId : selectDocList) {
					docParam.put("doc_id", docId);
					changeDocList.add(documentDao.commonDocDetail(docParam));
				}
				
				break;
				
			// 2. 검색 결과 문서를 소유권 이전함 :: 목록리스트 XML 분리 처리 (사용자 화면에서는 사용되지 않음)
			case Constant.CHANGE_SEARCH_DOC :
				changeDocList = documentDao.ownerDocumentNoPageList(map);
				break;
			
			// 3. 소유자의 전체 문서를 소유권 이전함 :: 목록리스트 XML 분리 처리
			case Constant.CHANGE_ALL_DOC :
				docParam.put("ownerKeyword", StringUtil.getMapString(map, "ownerKeyword", sessionVO.getSessId()));
				docParam.put("search_type", StringUtil.getMapString(map, "search_type"));
				changeDocList = documentDao.ownerDocumentNoPageList(docParam);
				break;
				
			default :
				throw processException("doc.fail.owner.type.exist");					
		}
		
		// # 이전할 문서 중 잠김 문서가 있는지 확인한다
		for (DocumentVO doc : changeDocList) {
			if (doc.getIs_locked().equals(Constant.T)) {
				throw processException("doc.fail.change.owner.doc.lock");
			}
		}
		
		// # 개인문서함의 경우 인계할 문서의 폴더구조를 인수자에게도 동일하게 구성해줘야 한다.
		switch (StringUtil.getMapString(map, "search_type")) {

			// 1. 개인 문서함일 경우 (폴더 경로 생성) 
			case Constant.CHANGE_SCOPE_MYWORK :
								
				// 이동할 문서 수 만큼 루프
				for (DocumentVO doc : changeDocList) {
					
					HashMap<String, Object> param = new HashMap<String, Object>();					
					param.put("folder_id", doc.getFolder_id());		// LNK_FOLDER => FOLDER_ID 변경처리 :: 소유권변경대상폴더는 기본폴더
					
					// 소유권 이전 대상 사용자에게 폴더를 그린다
					FolderVO changeFolderVO = folderService.ownerChangeFolder(targetUserId, doc, commonService.folderFullPath(param));


					// 모든 버전의 문서를 구한다
					param.put("doc_id", doc.getDoc_id());
					param.put("table_nm", Constant.DOC_TABLE);
					List<DocumentVO> allDocList = documentDao.allVersionDocumentList(param);
					
					// 모든 버전의 문서를 소유권 이전한다
					for (DocumentVO allDoc : allDocList) {
						
						// 문서 소유권을 이전한다 : 쿼리 및 파라미터 수정처리						
						allDoc.setOwner_id(targetUserId);
						allDoc.setFolder_id(changeFolderVO.getFolder_id());
						allDoc.setUpdate_action(Constant.ACTION_CHANGE_OWNER);
						documentDao.documentUpdate(allDoc);
						
						// XR_LINKED 정보 갱신처리 :: XR_LINKED DELETE 후 INSERT 기본폴더정보에 대해서만(다중분류는 제외)
						HashMap<String,Object> linked = new  HashMap<String, Object>();
						linked.put("doc_id",allDoc.getDoc_id());
						linked.put("folder_id", doc.getFolder_id());		// PRE FOLDER_ID
						folderDao.xrLinkedDelete(linked);
						
						linked.put("folder_id",changeFolderVO.getFolder_id());
						folderDao.writeXrLinked(linked);
					}

					// XR_DOCUMENT_HT 기록 :: ACTION - CHANGE_CREATOR
					int result = 0;
					long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);
					result = docHistoryWrite(historyDao, doc_seq, StringUtil.isEmpty(doc.getRoot_id()) ? doc.getDoc_id() : doc.getRoot_id(), doc.getDoc_id(), Constant.ACTION_CHANGE_CREATOR, doc.getDoc_type(), doc.getDoc_name(), doc.getVersion_no(), changeOwnerHT, sessionVO);
					if(result == 0)	{	throw processException("common.system.error");	}
				}
				
				break;
				
			// 2. 업무 문서함일 경우
			case Constant.CHANGE_SCOPE_WORKSPACE :
				
				// 이동할 문서 수 만큼 루프
				for (DocumentVO doc : changeDocList) {
					
					HashMap<String, Object> param = new HashMap<String, Object>();
					
					// 모든 버전의 문서를 구한다
					param.put("doc_id", doc.getDoc_id());
					param.put("table_nm", Constant.DOC_TABLE);
					List<DocumentVO> allDocList = documentDao.allVersionDocumentList(param);
					
					// 모든 버전의 문서를 소유권 이전한다
					for (DocumentVO allDoc : allDocList) {
						
						// 문서 소유권을 이전한다 :: 쿼리 및 파라미터 변경처리
						allDoc.setOwner_id(targetUserId);
						allDoc.setUpdate_action(Constant.ACTION_CHANGE_OWNER_WORK);
						documentDao.documentUpdate(allDoc);				
						
					}
					
					// XR_DOCUMENT_HT 기록 :: ACTION - CHANGE_CREATOR
					int result = 0;
					long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);
					result = docHistoryWrite(historyDao, doc_seq, StringUtil.isEmpty(doc.getRoot_id()) ? doc.getDoc_id() : doc.getRoot_id(), doc.getDoc_id(), Constant.ACTION_CHANGE_CREATOR, doc.getDoc_type(), doc.getDoc_name(), doc.getVersion_no(), changeOwnerHT, sessionVO);
					if(result == 0)	{	throw processException("common.system.error");	}
				}
				break;
		}

		resultMap.put("result",Constant.RESULT_TRUE);
		return resultMap;
	}
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서상세보기 공통 :: 확장속성정보 리스트를 가져온다.
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docExtendAttrList
	 * @param typeDao
	 * @param documentVO
	 * @return List<AttrVO>
	 */
	public List<AttrVO> docExtendAttrList(TypeDao typeDao,DocumentVO documentVO) {
		
		List<AttrVO>  attrList = new ArrayList<AttrVO>();
		
		HashMap<String, Object> param1 = new HashMap<String, Object>();
		HashMap<String, Object> param2 = new HashMap<String, Object>();
		
		param1.put("type_id",documentVO.getDoc_type());
		param1.put("is_extended",Constant.T);
		
		// 1.확장속성 목록을 가쟈온다.
		attrList = typeDao.attrList(param1);
		
		if(attrList != null && attrList.size() > 0 )		{
			
			for(AttrVO attrVO : attrList) {
				
				param2.put("doc_id",documentVO.getDoc_id());
				param2.put("tbl_name",documentVO.getTbl_name());
				param2.put("attr_id",attrVO.getAttr_id());
				
				// 2.확장속성값 값을 가져온다. :: 문서에 등록된값
				attrVO.setAttr_value(typeDao.attrValueDetail(param2));
				
				// 3.속성 아이템이 있는 경우 아이템 목록을 가져온다.
				if(attrVO.getHas_item() != null && attrVO.getHas_item().equals(Constant.T))	{
					param1.put("type_id",documentVO.getDoc_type());
					param1.put("attr_id",attrVO.getAttr_id());
					List<AttrItemVO> itemList = typeDao.attrItemList(param1);							
					attrVO.setItem_list(itemList);					
				}
			}			
		}
		
		return attrList;
	}
	

	@Override
	public List<HashMap<String, Object>> allDocumentValidList(HashMap<String, Object> map) throws Exception {
				
		List<DocumentVO> list = new ArrayList<DocumentVO>();
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);

		list = documentDao.wasteAllDocList(map);
	
		for(DocumentVO vo : list) {

			HashMap<String, Object> docInfo = new HashMap<String, Object>();
			docInfo.put("doc_id",vo.getDoc_id());
			docInfo.put("root_id",vo.getRoot_id());
			docInfo.put("is_locked",vo.getIs_locked());
			docInfo.put("type_id",vo.getDoc_type());
			
			// 관리자 휴지통내 삭제시 문서잠금 및 문서유형정보 삭제불가 체크 SKIPPED
			
			ret.add(docInfo);
		}
						
		return ret;
	}
	
	@Override
	public Map<String, Object> expiredDocProc(List<HashMap<String, Object>> docList ,HashMap<String,Object> map) throws Exception {
		
		DocumentDao documentDao = sqlSessionBatch.getMapper(DocumentDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int docCnt = 0;
		
		// 0.만기대상 문서 목록 리스트 구하기
		for(HashMap<String,Object> dMap : docList) {
			
			// 1.모든 버전 문서 목록 리스트 가져오기(ROOT_ID => DOC_ID)
			allVersionList = docAllVersionList(documentDao,dMap.get("doc_id").toString(),Constant.DOC_TABLE);
			
			for(DocumentVO vo : allVersionList)	{
				
				// 2.XR_DOCUMENT UPDATE :: IS_EXPIRED = F->T
				HashMap<String,Object> param1 = new  HashMap<String, Object>();
				param1.put("isType",Constant.EXPIRED);
				param1.put("is_expired",Constant.T);
				param1.put("doc_id", vo.getDoc_id());
				documentDao.docInfoUpdate(param1);
				
				docCnt++;
				// 3.XR_DOCUMENT_HT SKIPPED
			}
			
			allVersionList.clear();
			
		}	// END OF FOR		
		
		resultMap.put("total",docCnt);
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}

	@Override
	public Map<String, Object> docHistoryList(HashMap<String, Object> map) throws Exception {
		
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentHtVO> ret  = new ArrayList<DocumentHtVO>();
		int total = 0;
		
		// 이력조회 기준 :: root_id
		map.put("root_id", documentDao.selectRootId(map) != null ? documentDao.selectRootId(map) : documentDao.selectRootIdFromDocumentDel(map));
		
		total = historyDao.docHtPagingCount(map);
		ret = historyDao.docHtList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);		
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));
		resultMap.put("list",ret);
		
		// Ajax Paging 
		String strLink ="";
		if(StringUtil.getMapString(map, "isReference").equals("true")){
			strLink = map.get("strLink").toString();
		}else{
			strLink =  "javascript:exsoftAdminDocFunc.event.gridHistoryPage";
		}

		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);	
				
		return resultMap;
	}
	
	@Override
	public Map<String, Object> myDocumentBasicList(HashMap<String, Object> map) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		int total = 0;
		
		MyDocumentDao myDocumentDao = sqlSession.getMapper(MyDocumentDao.class);
		
		total = myDocumentDao.myDocumentListCnt(map);
		
		String action = map.get("select_action").toString();
		if("show_date".equals(map.get("orderCol").toString())) {
			switch (action) {
			case "CHECKOUT": map.put("orderCol", "LOCK_DATE"); break;
			case "EXPIRED": map.put("orderCol", "EXPIRED_DATE"); break;
			case "TRASHCAN": map.put("orderCol", "DELETE_DATE"); break;
			default: map.put("orderCol", "CREATE_DATE"); break;
			}
		}

		// 1. 문서 목록을 가져 온다.
		docList = myDocumentDao.myDocumentList(map);
		
		// 반출일/만기일/삭제일/등록일 조회처리위한 분기
		for (int i = 0; i < docList.size(); i++) {
			if("CHECKOUT".equals(action)) {
				docList.get(i).setShow_date(docList.get(i).getLock_date());
			} else if("EXPIRED".equals(action)) {
				docList.get(i).setShow_date(docList.get(i).getExpired_date());
			} else if("TRASHCAN".equals(action)) {
				docList.get(i).setShow_date(docList.get(i).getDelete_date());
			} else {
				docList.get(i).setShow_date(docList.get(i).getCreate_date());
			}
			docList.set(i, docList.get(i));
		}
		
		// 2. 문서에 대한 권한을 셋팅 한다. 엑셀저장은 매핑 안함
		String oper = map.get("oper") != null ? map.get("oper").toString() : "";
		if(!oper.equals(Constant.EXCEL_FORMAT)) {
			setDocumentAcl( docList, map);
		}

		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",docList);
		
		// 3. Ajax Paging 
		String strLink = "javascript:exsoftMypageFunc.event.docFunctions.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);	
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> workDocumentBasicList(HashMap<String, Object> map) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		int total = 0;
		
		WorkDocumentDao workDocumentDao = sqlSession.getMapper(WorkDocumentDao.class);
		
		// 1. 문서 목록을 가져 온다. :: 퀵메뉴 공유폴더 바로가기 선택시 공유폴더가 없는 사용자인 경우
		if(map.get("folder_id") != null && !map.get("folder_id").toString().equals(""))		{
			total = workDocumentDao.workDocumentListCnt(map);
			
			String action = map.get("select_action") != null ? map.get("select_action").toString() : null;
			if(action != null) {
				map.put("orderCol", "CREATE_DATE");
			}
			
			docList = workDocumentDao.workDocumentList(map);
			
			// 나의문서 > 공유받은 폴더 등록일조회 처리위한 분기
			if(action != null) {
				for (int i = 0; i < docList.size(); i++) {
					docList.get(i).setShow_date(docList.get(i).getCreate_date());
					docList.set(i, docList.get(i));
				}
			}
		}
			
		// 2. 문서에 대한 권한을 셋팅 한다. 엑셀저장은 매핑 안함
		String oper = map.get("oper") != null ? map.get("oper").toString() : "";
		if(!oper.equals(Constant.EXCEL_FORMAT)) {
			setDocumentAcl( docList, map);
		}
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",docList);
		resultMap.put("result",Constant.RESULT_TRUE);

		// 3. Ajax Paging 
		String strLink = map.get("strLink").toString();
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		
				
				
		return resultMap;
	}	

	@Override	
	public Map<String, Object> documentInfoForInserting(HashMap<String, Object> map,SessionVO sessionVO) throws Exception {
		
		HashMap<String, Object> param1 = new HashMap<String, Object>();
		HashMap<String, Object> param2 = new HashMap<String, Object>();
		HashMap<String, Object> param3 = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);		
		AclDao aclDao = sqlSession.getMapper(AclDao.class);
		
		/*********************************************************************
		 ** 문서등록 위한 기본 속성 정보
		 ** 파라미터 map_id,folder_id,sessionVO  
		 ** defaultAcl : 기본ACL
		 ** defaultType : 기본문서유형
		 ** aclPart : 사용자(권한) 접근 권한
		 ** docPart : 사용자(문서) 접근 권한
		*********************************************************************/
		String defaultAcl = "";
		String defaultType = "";	
		String mapId = "";
		String docPart = "";						

		// 1. 폴더정보조회 : 파라미터 folder_id
		FolderVO folderVO = folderDao.folderDetail(map);
		if(folderVO == null)	{
			 throw processException("doc.fail.folder.not.exist");					 	
		}
		
		resultMap.put("folderVO",folderVO);			// Return Val 기본폴더정보
		
		// 1.1 폴더 문서저장여부 체크
		if(folderVO.getIs_save().equals(Constant.NO))	{
			 throw processException("doc.fail.folder.not.save");				
		}
		
		// 1.2 기본권한 : 폴더기본권한 및 ACL 목록 리스트를 가져온다.
		mapId = folderVO.getMap_id();
		if(mapId.equals(Constant.MAP_ID_MYPAGE))	{
			defaultAcl = Constant.ACL_ID_OWNER;
		}else {
			
			/**************************************************************************************************************************
			 * ALL : 전체관리자 / GROUP : 부서권한 관리자 / TEAM : 팀권한 관리자 =>	문서/폴더의 모든 권한을 가진다.
			 * EMPTY : 일반사용자/권한없는 관리자 => ACL권한이 있는 경우 권한을 가진다.
			 ***************************************************************************************************************************/
			
			// 1.2.1 폴더권한 여부를 체크한다. :: MYDEPT(부서함)인 경우에만 적용됨
			if(mapId.equals(Constant.MAP_ID_DEPT))	{
				param1.put("menu_cd",Constant.USER_FOLDER_MENU_CODE);
				param1.put("role_id",sessionVO.getSessRole_id());
				docPart = commonService.getMenuAuth(param1);
			}
			
			//` isAuth = isFolderDocCreateAuth(map,sessionVO,docPart,folderDao,aclDao); 			
			// ROLE권한자 & 일반권한자 모두 체크
			if(!isFolderDocCreateAuth(map,sessionVO,docPart,folderDao,aclDao)) {
				throw processException("doc.fail.folder.not.create.auth");								
			}
			
			defaultAcl = folderVO.getAcl_id();				
		}
		
		resultMap.put("defaultAcl",defaultAcl);				// Return Val :: 기본ACL
		
		// 기본ACL_NAME :: aclDetail
		param3.put("acl_id", defaultAcl);
		AclVO aclVO = aclDao.aclDetail(param3);
//		resultMap.put("defaultAclName",aclVO.getAcl_name());		// Return Val :: 기본ACL명
		resultMap.put("aclDetail",aclVO);		// Return Val :: 기본ACL명
		
		// 1.3 ACLITEM 정보 가져오기.
		param2.clear();
		param2.put("acl_id",defaultAcl);						
		param2.put("is_type",Constant.D);					// ACL 문서 권한
//		List<AclItemVO> aclItemList = aclDao.aclDetailItems(param2);
		List<AclItemListVO> aclItemList = aclService.aclItemList(param2);
		resultMap.put("aclItemList",aclItemList);			// Return Val :: 기본ACLITEM 단 추가접근자는 문서수정화면에서 가져온다.
				
		// 1.4 기본문서유형 : 폴더문서유형 상속
		if(folderVO.getIs_type().equals(Constant.FOLDER_TYPE_ALL_TYPE)
				|| folderVO.getIs_type().equals(Constant.DOC_TABLE))	{
			defaultType = Constant.DOC_TABLE;
			resultMap.put("isChangeType",Constant.TRUE);
		}else {
			defaultType = folderVO.getIs_type();			
			resultMap.put("isChangeType",Constant.FALSE);		
		}
		
		resultMap.put("is_type",folderVO.getIs_type());	// 폴더의 기본문서유형
		resultMap.put("defaultType",defaultType);			// Return Val :: 기본문서유형
		
		return resultMap;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 폴더내 문서등록 권한 여부 체크
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : isFolderDocCreateAuth
	 * @param map
	 * @param sessionVO
	 * @param docPart
	 * @param folderDao
	 * @param aclDao
	 * @return
	 * @throws Exception boolean
	 */
	public boolean isFolderDocCreateAuth(HashMap<String, Object> map,SessionVO sessionVO,String docPart,FolderDao folderDao,AclDao aclDao) 
			throws Exception {
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		boolean isAuth = false;
		
		if(docPart != null && !docPart.equals(""))	{
			// 권한관리 사용자(ALL/GROUP/TEAM)
			if(docPart.equals(Constant.MENU_ALL))	{
				isAuth = true;		// 전체권한인 경우
			} else if(docPart.equals(Constant.MENU_GROUP) || docPart.equals(Constant.MENU_TEAM))	{
				// 관리대상 폴더에 속하는지 체크한다. 
				isAuth = cacheService.menuAuthByFolderID((String)map.get("folder_id"), sessionVO.getSessManage_group());
			} 			
		}else {
			// 일반사용자
			param.put("user_id",sessionVO.getSessId());
			param.put("folder_id",map.get("folder_id"));
			param.put("is_type",Constant.D);
			
			// 그룹+프로젝트 그룹 ID
			String[] group_id_list = sessionVO.getSessProjectGroup().toArray(new String[sessionVO.getSessProjectGroup().size()+1]);
			group_id_list[sessionVO.getSessProjectGroup().size()] = sessionVO.getSessGroup_id();
			param.put("group_id_list",group_id_list);
			
			AclItemVO aclItemVO = aclDao.isAuthCheck(param);
			
			if(aclItemVO != null && aclItemVO.getAct_create().equals(Constant.T)) {
				isAuth = true;
			}
			
		}
		
		return isAuth;
	}
	
	@Override	
	public Map<String, Object> documentListForInserting(HashMap<String, Object> map,SessionVO sessionVO) throws Exception {
		
		HashMap<String, Object> param1 = new HashMap<String, Object>();
		HashMap<String, Object> param2 = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		ConfDao confDao = sqlSession.getMapper(ConfDao.class);

		List<ConfVO> confList =  new ArrayList<ConfVO>();
		
		// common/configFileInfo.do에서 처리 함.
//		// 1. 첨부파일 관련 설정정보(XR_FILE_CONFIG =>  XR_SYSCONFIG 테이블 통합처리)		
//		param1.put("stype", Constant.SYS_TYPE_FILE);
//		confList = confDao.sysConfigDetail(param1);
//		
//		if(confList != null & confList.size() > 0)	{
//			
//			for(ConfVO conf : confList)	{				
//				resultMap.put(conf.getSkey(), conf.getSval());				
//			}
//			
//		}else {
//			
//			// 시스템 기본값 설정
//			resultMap.put("EXT","exe;bat;dll;ocx;");
//			resultMap.put("FILECNT",10);
//			resultMap.put("FILESIZE",1024);
//			resultMap.put("FILETOTAL",4096);
//		}
						
		
		// 2.문서유형 : 파라미터 - is_doc 
		List<TypeVO> typeList = new ArrayList<TypeVO>();
		param1.put("is_doc", Constant.T);
		param1.put("is_hidden", Constant.T); // 쿼리가 != 비교여서 T를 넘김
		typeList = typeDao.typeList(param1);
		resultMap.put("typeList", typeList);
		
		// 3. 보존년한 : 파라미터 - gcode_id	
		param2.put("gcode_id", Constant.CODE_PRESERVATION_YEAR);
		List<CodeVO> preservation = commonService.codeList(param2);
		resultMap.put("preservation_year", preservation);
		
		// 4 보안등급 : 파라미터 - gcode_id 
		param2.put("gcode_id", Constant.CODE_SECURITY_LEVEL);
		List<CodeVO> sercurity = commonService.codeList(param2);
		resultMap.put("sercurity", sercurity);
		
		// 5  조회등급 : 파라미터 - gcode_id,is_use 
		param2.put("gcode_id", Constant.CODE_POSITION);
		param2.put("is_use", Constant.YES);
		List<CodeVO> position = commonService.codeList(param2);
		resultMap.put("position", position);
		
		// 6.문서버전설정(개인/업무문서함)
		param1.put("stype", Constant.SYS_TYPE_VERSION);
		confList = confDao.sysConfigDetail(param1);
		
		if(confList != null & confList.size() > 0)	{
			
			for(ConfVO conf : confList)	{
				
				CaseInsensitiveMap versonMap = new CaseInsensitiveMap();
				
				versonMap.put("vkey", conf.getSkey());
				versonMap.put("val", conf.getSval());
				versonMap.put("is_use", conf.getIs_use());				
				resultMap.put(conf.getSkey().toLowerCase(), versonMap);
			}
			
		}else {
			// 시스템 기본값 설정
			CaseInsensitiveMap versonMap = new CaseInsensitiveMap();
			versonMap.put("vkey", Constant.MAP_ID_MYPAGE);
			versonMap.put("val", Constant.VERSION_SAME_VERSION);
			versonMap.put("is_use", Constant.NO);
			resultMap.put("mypage", versonMap);
			
			versonMap.put("vkey", Constant.MAP_ID_WORKSPACE);
			versonMap.put("val", Constant.VERSION_MINOR_VERSION);
			versonMap.put("is_use", Constant.YES);
			resultMap.put("workspace", versonMap);
		}
		
		return resultMap;
	
	}
	
	@Override
	public Map<String, Object> documentUpdate(HashMap<String, Object> map, DocumentVO documentVO, SessionVO sessionVO) 
			throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int result = 0;
		
		String type =  map.get(Constant.TYPE) != null ? map.get(Constant.TYPE).toString() : "";
		
		if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_UPDATE)) {
			//TODO : 문서 수정
		} else if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_CHECKOUT)) {
			// is_locked=T lock_owner=sesssoin
			documentVO.setUpdate_action( Constant.ACTION_CHECKOUT);			
			result = documentDao.documentUpdate(documentVO);
			
		} else if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_CHECKIN)) {
			// 문서 잠금 해제
			documentVO.setUpdate_action( Constant.ACTION_CHECKIN); //update type set
			result = documentDao.documentUpdate(documentVO);
			
			// 3.XR_FILE FLAG처리 :: parameter : doc_id
			HashMap<String,Object> param = new  HashMap<String, Object>();
			param.put("doc_id",map.get("doc_id").toString());
			param.put("is_locked",Constant.F);
			
			List<PageVO> PageList = new ArrayList<PageVO>();	
			// 첨부파일 목록을 가져온다.
			PageList = pageDao.comDocPageList(param);
			if(PageList.size() > 0){

				// 1.XR_FILED의 is_locked를 플래그 처리한다.
				result = pageDao.xrFileListUpdate(param);
			}
			
			
		} else if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_MOVE)) {
			// 문서 이동
			documentVO.setUpdate_action(Constant.ACTION_MOVE);			
			result = documentDao.documentUpdate(documentVO);
			
		} else if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_CANCEL_CHECKOUT)){			
			documentVO.setUpdate_action(Constant.ACTION_CANCEL_CHECKOUT);
			documentVO.setDoc_id(map.get("doc_id").toString());
			documentVO.setIs_locked(map.get("is_locked").toString());
			result = documentDao.documentUpdate(documentVO);
			
			// 3.XR_FILE FLAG처리 :: parameter : doc_id
			HashMap<String,Object> param = new  HashMap<String, Object>();
			param.put("doc_id",map.get("doc_id").toString());
			param.put("is_locked",Constant.F);
			
			List<PageVO> PageList = new ArrayList<PageVO>();	
			// 첨부파일 목록을 가져온다.
			PageList = pageDao.comDocPageList(param);
			if(PageList.size() > 0){
				// 1.XR_FILED의 is_locked를 플래그 처리한다.
				result = pageDao.xrFileListUpdate(param);
			}
			
		} else if(!StringUtil.isEmpty(type) && type.equals(Constant.ACTION_RESTORE)) {
			result = documentDao.documentUpdate(documentVO);
		}else {
			result = 0;
		}
		
		if(result == 0)	{	throw processException("common.system.error");	}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		return resultMap;
	}
	
	
	@Override
	public Map<String, Object> documentRelationDocByDoc_id(	HashMap<String, Object> map) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		param.put("doc_id", map.get("doc_id") != null ? map.get("doc_id") : "" );
		
		// 1. 관련 문서를 가져 온다
		List<DocumentVO> relationDocList = documentDao.refDocumentList(param);
		
		// 2. 관련문서에 대한 권한을 셋팅 한다.
		setDocumentAcl( relationDocList, map);
		
		resultMap.put("relationDocList", relationDocList);
		return resultMap;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : DocumentList에 대한 권한 매핑 작업
	 * 2. 처리내용 : map값에서는 반드시 user_id, group_id가 param 값으로 존재해야 함.
	 * </pre>
	 * @Method Name : setDocumentAcl
	 * @param doc_list
	 * @param map
	 * @throws Exception void
	 */
	public void setDocumentAcl(List<DocumentVO> doc_list, HashMap<String, Object> map) throws Exception {
		// map값에서는 반드시 user_id, group_id가 param 값으로 존재해야 함.
		AclDao aclDao = sqlSession.getMapper(AclDao.class);
		
		String document_menu_part = map.get("document_menu_part") != null ? map.get("document_menu_part").toString() : "";
		String manage_group_id = map.get("manage_group_id") != null ? map.get("manage_group_id").toString() : "";
		String folder_id = map.get("folder_id") != null ? map.get("folder_id").toString() : "";
		boolean isGroupFolder = false;
		
		String action = map.get("select_action") != null ? map.get("select_action").toString() : null;
		// 나의문서 > 최신문서함 일때는 Role에 따른 권한처리를 할때 문서들의 folder_id에 대하여 모두 체크해야하므로 분기처리
		if(action != null && "RECENTLYDOC".equals(action)) {
			for (int j = 0; j < doc_list.size(); j++) {
				if(document_menu_part.equals(Constant.MENU_GROUP) || document_menu_part.equals(Constant.MENU_TEAM)){
					isGroupFolder = cacheService.menuAuthByFolderID(doc_list.get(j).getFolder_id(), manage_group_id);
				} 

				// 문서 메뉴 접근 권한 rule 적용
				if(document_menu_part.equals(Constant.MENU_ALL) || 
						document_menu_part.equals(Constant.MENU_GROUP) || 
						document_menu_part.equals(Constant.MENU_TEAM)) {
					// 전사 문서 관리자, 하위부서포함 문서 관리자, 소속부서 관리자
					if(document_menu_part.equals(Constant.MENU_ALL) || isGroupFolder){
						for(int i=0; i < doc_list.size(); i++) {
							DocumentVO tempDocVo = new DocumentVO(); 
							tempDocVo =	doc_list.get(i);
							
							tempDocVo.setAcl_level(Constant.ACL_DELETE);
							tempDocVo.setAcl_create(Constant.T);
							tempDocVo.setAcl_checkoutCancel(Constant.T);
							tempDocVo.setAcl_changePermission(Constant.T);
							
							// 기존 내용 변경 시 add가 아닌 set을 이용한다.
							doc_list.set(i, tempDocVo);
						}
						return; // (전사문서관리자 또는 하위부서포함(소속부서) :: 해당폴더가 관리 부서 폴더일 경우) 권한 체크 안함
					}
				}
				
			}
			
		} else {  // 나머지 경우들에는 map으로 넘어온 folder_id로 체크
			if(document_menu_part.equals(Constant.MENU_GROUP) || document_menu_part.equals(Constant.MENU_TEAM)){
				isGroupFolder = cacheService.menuAuthByFolderID(folder_id, manage_group_id);
			} 

			// 문서 메뉴 접근 권한 rule 적용
			if(document_menu_part.equals(Constant.MENU_ALL) || 
					document_menu_part.equals(Constant.MENU_GROUP) || 
					document_menu_part.equals(Constant.MENU_TEAM)) {
				// 전사 문서 관리자, 하위부서포함 문서 관리자, 소속부서 관리자
				if(document_menu_part.equals(Constant.MENU_ALL) || isGroupFolder){
					for(int i=0; i < doc_list.size(); i++) {
						DocumentVO tempDocVo = new DocumentVO(); 
						tempDocVo =	doc_list.get(i);
						
						tempDocVo.setAcl_level(Constant.ACL_DELETE);
						tempDocVo.setAcl_create(Constant.T);
						tempDocVo.setAcl_checkoutCancel(Constant.T);
						tempDocVo.setAcl_changePermission(Constant.T);
						
						// 기존 내용 변경 시 add가 아닌 set을 이용한다.
						doc_list.set(i, tempDocVo);
					}
					return; // (전사문서관리자 또는 하위부서포함(소속부서) :: 해당폴더가 관리 부서 폴더일 경우) 권한 체크 안함
				}
			}
		}
		
		
		
		// 1. 문서에 대한 권한을 셋팅 한다.
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		List<String> doc_idList = new ArrayList<String>();
		
		// 2. docList의 doc_id를 list값으로 map에 담는다
		int listCnt = 0;
		for(DocumentVO docVo : doc_list) {
			tempMap.put(docVo.getDoc_id(), listCnt++);
			doc_idList.add(docVo.getDoc_id());
		}
		// 3. 1번 기준 acl값 가져온다
		map.put("doc_idList", doc_idList);
		if( doc_idList.size() > 0)
			aclList = aclDao.alcListByDocumentIDs(map);
		
		// 4. doc_list에 최종 값을 담는다.
		listCnt = 0;
		for(CaseInsensitiveMap caseMap : aclList) {
			listCnt = (Integer)tempMap.get(caseMap.get("DOC_ID").toString());
			DocumentVO tempDocVo = new DocumentVO(); 
			tempDocVo =	doc_list.get(listCnt);
			
			if(caseMap.get("ACT_DELETE").toString().equals(Constant.T)) {
				tempDocVo.setAcl_level(Constant.ACL_DELETE);
			} else if(caseMap.get("ACT_UPDATE").toString().equals(Constant.T)) {
				tempDocVo.setAcl_level(Constant.ACL_UPDATE);
			} else if(caseMap.get("ACT_READ").toString().equals(Constant.T)) {
				tempDocVo.setAcl_level(Constant.ACL_READ);
			} else if(caseMap.get("ACT_BROWSE").toString().equals(Constant.T)) {
				tempDocVo.setAcl_level(Constant.ACL_BROWSE);
			} else {
				tempDocVo.setAcl_level(Constant.ACL_NONE);
			}
			
			tempDocVo.setAcl_create(caseMap.get("ACT_CREATE").toString());
			tempDocVo.setAcl_checkoutCancel(caseMap.get("ACT_CANCEL_CHECKOUT").toString());
			tempDocVo.setAcl_changePermission(caseMap.get("ACT_CHANGE_PERMISSION").toString());
			
			// 기존 내용 변경 시 add가 아닌 set을 이용한다.
			doc_list.set(listCnt, tempDocVo);
		}
	}
	
	/**
	 * <pre>
	 * 1. 개용 : DocumentList에 대한 권한 매핑 작업 (오버로딩)
	 * 2. 처리내용 : map값에서는 반드시 user_id, group_id가 param 값으로 존재해야 함.
	 * </pre>
	 * @Method Name : setDocumentAcl
	 * @param docVO
	 * @param map
	 * @throws Exception
	 */
	public void setDocumentAcl(DocumentVO docVO, HashMap<String, Object> map) throws Exception {
		setDocumentAcl(Arrays.asList(docVO), map);
	}
	//[1000]
	@Override
	public Map<String, Object> moveDocListUpdate(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
		return moveDocListUpdate(docList,map,sessionVO,false);
	}
	@Override
	public Map<String, Object> moveDocListUpdate(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO, boolean isExternal) 
			throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();
	
		//String oriFolderId = map.get("folder_id") != null ? map.get("folder_id").toString() : "";
		String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
	
		// 1.이동할 폴더의 ACL_ID 얻기
		FolderVO folderVO = new FolderVO();
		folderVO.setFolder_id(targetFolderId);
		FolderVO getFldInfo = folderDao.getFolderAcl(folderVO);
		
		
		String folderAcl = getFldInfo.getAcl_id();
		
		for(HashMap<String, Object> mMap : docList){
			
			String root_id = mMap.get("root_id").toString().equals("") ? mMap.get("doc_id").toString() : mMap.get("root_id").toString();
			String doc_id = mMap.get("doc_id").toString();
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_TABLE);
			
			mMap.put("type", Constant.ACTION_MOVE);
			
			// 2. 문서의 모든버전 구하기
			for(DocumentVO vo : allVersionList){		
				
				int result = 0;
				vo.setDoc_id(vo.getDoc_id());
				vo.setLnk_folder_id(targetFolderId);
				vo.setFolder_id(targetFolderId);

				
				String inheritAcl = mMap.get("is_inherit_acl").toString();
				if(inheritAcl.equals(Constant.F)){
					vo.setAcl_id(folderAcl);
				} 		
				
				// 동일 문서명이 있을 경우 '-이동본'이 붙은 문서명으로 변경 처리
				if(!mMap.get("doc_name").toString().equals(vo.getDoc_name())){
					vo.setDoc_name(mMap.get("doc_name").toString());
				}				
				
				// 3.XR_DOCUMENT UPDATE 
				resultMap = documentUpdate(mMap, vo, sessionVO);				

				// 문서이동시 storage_usage 업데이트처리 [3007] START
				if(!map.get("map_id").equals("MYPAGE")){
				storageUsageDeleteControl(Constant.ACTION_MOVE, folderDao, vo, mMap);
				}
				
				// 문서이동시 필요한 target_folder_id
				folderVO.setFolder_id(targetFolderId);
				mMap.put("target_folder_id", targetFolderId);			
				
				// 복사,이동시에는 total_size가 없어서 0으로 처리	
				map.put("total_size", 0);
				mMap.put("total_size", map.get("total_size").toString());
				
				// 개인문서함일때 이동은 storage_usage의 업데이트처리가 불필요.
				if(!map.get("map_id").equals("MYPAGE")){
					storageUsageUpdateControl(Constant.ACTION_MOVE, folderDao, vo, mMap);
				}
				// [3007] END
				
				// 4.삭제할 XR_LINKED의 정보 입력 ::
				HashMap<String,Object> linked = new  HashMap<String, Object>();
				linked.put("doc_id",vo.getDoc_id());
				linked.put("folder_id", mMap.get("folder_id").toString());			
											
				// 5.XR_LINKED : DELETE
				folderDao.xrLinkedDelete(linked);
				
				// 6.XR_LINKED :: DOC_ID
				linked.put("folder_id", targetFolderId);
				folderDao.writeXrLinked(linked);
				//documentDao.documentMove(vo);				
						
				// 7. XR_DOCUMENT_HT 등록할 Beford_nm, After_nm 값 셋팅
				HashMap<String, Object> htMap = new HashMap<String, Object>();
				htMap = moveDocumentHT(mMap.get("folder_id").toString(), targetFolderId);
	
				// 8. XR_DOCUMENT_HT 등록처리 :: ACTION - MOVE
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);
				//[1000]
				if(!isExternal){
					result = docHistoryWrite(historyDao, doc_seq, root_id, vo.getDoc_id(), Constant.ACTION_MOVE, vo.getDoc_type(), vo.getDoc_name(), vo.getVersion_no(), htMap, sessionVO);
					if(result == 0)	{	throw processException("common.system.error");	}
				}
				
			}
			allVersionList.clear();
			
		}
		resultMap.put("result",Constant.RESULT_TRUE);
			
		return resultMap;
	}
	
	@Override
	public HashMap<String, Object> moveDocumentHT(String oriFolderId, String targetFolderId) throws Exception {
		
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();	
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		List<String> fldId = new ArrayList<String>();
		List<FolderVO> nameList = new ArrayList<FolderVO>();
		 
		// folder_id로 before_nm, after_nm구하기
		fldId.add(oriFolderId);
		fldId.add(targetFolderId);
		param.put("folder_list", fldId);
		
		nameList = folderDao.getFolderName(param);
		for(FolderVO list : nameList){
			String getId = list.getFolder_id();
			String getName = list.getFolder_name_ko();					
			
			if(oriFolderId.equals(getId)){
				map.put("before_id", oriFolderId);
				map.put("before_nm", getName);
			} else {
				if(targetFolderId.equals(getId))
					map.put("after_id", targetFolderId);
				map.put("after_nm", getName);						
			}					
		 }
		 return map;
	}

	
	@Override
	public HashMap<String, Object> changeOwnerDocumentHT(String srcUserId, String desUserId) throws Exception {
		UserDao userDao = sqlSession.getMapper(UserDao.class);
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		// 원 사용자 정보 조회
		param.put("user_id", srcUserId);
		UserVO srcUser = userDao.userDetail(param);
		
		// param 초기화
		param.clear();
		
		// 대상 사용자 정보 조회
		param.put("user_id", desUserId);
		UserVO desUser = userDao.userDetail(param);
		
		// 대상자 정보 저장
		result.put("before_id", srcUser.getUser_id());
		result.put("before_nm", srcUser.getUser_name_ko());
		result.put("after_id", desUser.getUser_id());
		result.put("after_nm", desUser.getUser_name_ko());
		return result;
	}

	@Override
	public Map<String, Object> copyDocListUpdate(List<HashMap<String, Object>> copyList, HashMap<String, Object> map, SessionVO sessionVO) throws Exception {

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// eXrep C/S Client 생성. 
		EXrepClient eXrepClient = new EXrepClient();
		
		// 1. 복사할 폴더 ID 
		String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
		
		// 2. 복사할 Folder의 ACL_ID 얻기
		FolderVO folderVO = new FolderVO();
		folderVO.setFolder_id(targetFolderId);
		FolderVO getFldInfo = folderDao.getFolderAcl(folderVO);
		String folderAcl = getFldInfo.getAcl_id();
				
		for(HashMap<String,Object> cMap : copyList) {

			int result = 0;
			List<CaseInsensitiveMap> pageList = new ArrayList<CaseInsensitiveMap>();
			
			// 3.기존 문서 정보 얻기. XR_DOCUMENT, XR_TYPE, XR_PAGE, XR_FILED
			cMap.put("table_nm", Constant.DOC_TABLE);
			cMap.put("type_id", cMap.get("doc_type").toString());
			
			DocumentVO documentVO = documentDao.getDocumentInfo(cMap);	
			TypeVO typeVO = typeDao.typeDetailInfo(cMap);
			pageList = pageDao.xrPageList(cMap);
						
			// 2.PAGE_CNT 설정
			if(pageList != null && pageList.size() > 0){
				documentVO.setPage_cnt(pageList.size());
			}			
			
			// 3. 문서명 설정 
			String getDocName = cMap.get("doc_name").toString();
			
			// 4.XR_DOCUMENT_HT 등록처리 :: ACTION : COPY
			long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);		
			String root_id = documentVO.getRoot_id() != null ? documentVO.getRoot_id() : documentVO.getDoc_id();		
//			result = docHistoryWrite(historyDao,doc_seq,root_id,documentVO.getDoc_id(),Constant.ACTION_COPY,documentVO.getDoc_type(), documentVO.getDoc_name(),documentVO.getVersion_no(),sessionVO);
			// [3002] 버전복원시 이력을 쌓을수있게 처리
			result = docHistoryWrite(historyDao,doc_seq,root_id, documentVO.getDoc_id(), map.get(Constant.TYPE).toString(), documentVO.getDoc_type(), documentVO.getDoc_name(), documentVO.getVersion_no(),sessionVO);
			if(result == 0)	{	throw processException("common.system.error");	}
			
			// 5.새 문서 정보 설정 
			DocumentVO newDocVO = documentVO;
			
			int newID = commonService.commonNextVal(Constant.COUNTER_ID_FILE);
			String newDocID = CommonUtil.getStringID(Constant.ID_PREFIX_DOCUMENT, newID);
			String newRefID = newDocID.replace(Constant.ID_PREFIX_DOCUMENT, Constant.ID_PREFIX_REF);
			
			newDocVO.setDoc_id(newDocID);
			newDocVO.setDoc_name(getDocName);
			newDocVO.setRoot_id(null);
			newDocVO.setPage_cnt(pageList.size());
			
			newDocVO.setIs_current(Constant.T);
			newDocVO.setIs_locked(Constant.F);
			newDocVO.setVersion_no(Constant.DEFAULT_VERSION_NO);
			
			newDocVO.setDoc_status(Constant.C);

			newDocVO.setAcl_id(folderAcl);
			newDocVO.setFolder_id(targetFolderId);
			newDocVO.setCreator_id(sessionVO.getSessId());
			newDocVO.setCreator_name(sessionVO.getSessName());
			
			newDocVO.setOwner_id(sessionVO.getSessId());
			newDocVO.setRef_id(newRefID);
			
			// 6.XR_DOCUMENT : INSERT		
			result = documentDao.writeDocument(newDocVO);			
			
			//7.확장 문서이면서, 확장 속성이 존재하는 경우 :: 문서 속성 복사(등록)
			if(typeVO.getIs_base().equals(Constant.F) && !StringUtil.isEmpty(typeVO.getTbl_name())) {
				
				HashMap<String,Object> attrParam = new HashMap<String, Object>();
				HashMap<String,Object> getAttr = new HashMap<String, Object>();
				
				// 속성을 초기화
				attrParam.put("tbl_name", typeVO.getTbl_name());
				attrParam.put("doc_id", newDocVO.getDoc_id());
				attrParam.put("type_id", typeVO.getType_id());
				attrParam.put("is_extended", Constant.T);
				typeDao.insertAttrValue(attrParam);
				
				// 속성 목록을 얻는다.
				List<AttrVO> attrList = typeDao.attrList(attrParam);
				
				for(AttrVO attrVO : attrList) {					
					String attr_id = attrVO.getAttr_id();
					
					getAttr.put("tbl_name", typeVO.getTbl_name());
					getAttr.put("attr_id", attr_id);
					getAttr.put("type_id", typeVO.getType_id());
					getAttr.put("doc_id", cMap.get("doc_id").toString());
					
					String attr_value = typeDao.attrValueDetail(getAttr);				
					attrParam.put("attr_id", attr_id);
					attrParam.put("attr_value", attr_value);					
					
					typeDao.updateAttrValue(attrParam);					
				}
			}
			// 8.XR_LINKED INSERT (FOLDER_DOCUMENT)
			HashMap<String,Object> linked = new  HashMap<String, Object>();
			linked.put("doc_id", newDocVO.getDoc_id());
			linked.put("folder_id", targetFolderId);
			folderDao.writeXrLinked(linked);			
			
			// 문서복사시 xr_folder의 storage_usage 업데이트 처리 [3009]			
			// 복사,이동시에는 total_size가 없어서 0으로 처리	
			map.put("total_size", 0);			
			storageUsageUpdateControl(Constant.ACTION_COPY, folderDao, documentVO, map);


			List<PageVO> allPageList = new ArrayList<PageVO>();		
			// 9.XR_PAGE / XR_FILED INSERT			
			if(pageList.size() > 0 )	{

				eXrepClient.connect();
				
				for(CaseInsensitiveMap caseMap : pageList )	{
					int newPageId = commonService.commonNextVal(Constant.COUNTER_ID_PAGE);
										
					// Page 생성
					PageVO newPageVO = new PageVO();
					
					// Page ID 셋팅
					newPageVO.setPage_id(CommonUtil.getStringID(Constant.ID_PREFIX_PAGE, newPageId));
					newPageVO.setPage_name(caseMap.get("page_name").toString());
					newPageVO.setPage_extension(CommonUtil.getFileExtension(caseMap.get("page_name").toString()));
					newPageVO.setPage_size(Long.parseLong(caseMap.get("page_size").toString()));
					newPageVO.setVolume_id(ConfigData.getString(Constant.EXREP_VOLUME_NM));
					// Page Content Path 생성(년/월/일/PageID(UUID))
					newPageVO.setContent_path(CommonUtil.getContentPathByDate(Constant.EXREP_ROOT_EDMS_NM)+newPageVO.getPage_id());
					
					// eXrep file copy
					if(!eXrepClient.copyFile(newPageVO.getVolume_id(), caseMap.get("content_path").toString(), newPageVO.getContent_path(), true)){
						throw processException("doc.fail.copy.exrep");
					}
					
					// DB에 등록할 객체 생성
					allPageList.add(newPageVO);
				}
				
			// eXrep C/S Client Close
			eXrepClient.disconnect();
			}	

			HashMap<String,Object> pageParam = new HashMap<String,Object>();
			if(allPageList != null && pageList.size() > 0) {
				pageParam.put("doc_id", newDocVO.getDoc_id());
				
				for(PageVO page : allPageList){
					result = pageDao.writePage(page);
					if(result == 0)	{	throw processException("common.system.error");	}
					
					pageParam.put("page_id", page.getPage_id());
					result = pageDao.writeXrFiled(pageParam);
					if(result == 0)	{	throw processException("common.system.error");	}
				}
			}
	
			// 10. XR_DOCUMENT_HT 등록처리 :: ACTION : CREATE
			doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);		
			root_id = newDocVO.getRoot_id() != null ? newDocVO.getRoot_id() : newDocVO.getDoc_id();		
			result = docHistoryWrite(historyDao,doc_seq,root_id,newDocVO.getDoc_id(),Constant.ACTION_CREATE,newDocVO.getDoc_type(), newDocVO.getDoc_name(),newDocVO.getVersion_no(),sessionVO);
			if(result == 0)	{	throw processException("common.system.error");	}

			
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}
	
	@Override
	public DocumentVO  writeDocValid(HashMap<String,Object> map,DocumentVO documentVO,SessionVO sessionVO) throws Exception {

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);			
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		HashMap<String, Object> param = new HashMap<String, Object>();	

		
		// 1.유효성 체크
		
		// 1.1 업무구분체크 : 등록/수정 :: update/insert
		String isType = map.get("isType") != null ? map.get("isType").toString() : "";
		String version_type = map.get("version_type") != null ? map.get("version_type").toString() : "";
		if(isType.equals("") || version_type.equals("")) {
			throw processException("common.required.error");
		}
		
		// 1.2 같은 폴더내에 동일한 문서제목이 존재하는지 체크한다.(RGATE의 경우 SKIPPED)
		if(isType.equals(Constant.UPDATE))	{
			documentVO.setPre_doc_id(documentVO.getDoc_id());		// 이전버전ID 설정
			param.put("doc_id",documentVO.getDoc_id());
		}
		
		param.put("is_current",Constant.T);
		param.put("doc_status",Constant.C);
		
		//[1002] 최은옥 문서명 특수문사 여부 체크 20150901
		//[1002]START========================================
		if(PatternUtil.webfolderCheck(documentVO.getDoc_name()) || PatternUtil.webfolderCheck(documentVO.getDoc_name())) {
			throw processException("folder.fail.write.misnaming");
		}
		//[1002]END =========================================
		
		param.put("doc_name",documentVO.getDoc_name());
		param.put("folder_id",documentVO.getFolder_id());
		
		if(documentDao.isExitsDocName(param) > 0)	{
			throw processException("doc.fail.doc.name.exist");
		}
		
		// 2.DocumentVO 객체 생성

		// 문서 등록자 설정
		documentVO.setCreator_id(sessionVO.getSessId());
		documentVO.setCreator_name(sessionVO.getSessName());
				
		// 2.1 DOC_ID/REF_ID 설정
		if(isType.equals(Constant.UPDATE) && version_type.equals(Constant.VERSION_SAME_VERSION) )	{

			// DOC_ID/ROOT_ID/REF_ID/OWNER_ID/VERSION_NO SKIPPED
			
		}else {
			
			// 신규문서등록 OR 문서수정-주버전/부버전인 경우			
			int doc_id =  commonService.commonNextVal(Constant.COUNTER_ID_FILE);
			documentVO.setDoc_id(CommonUtil.getStringID(Constant.ID_PREFIX_DOCUMENT, doc_id));
			documentVO.setRef_id(documentVO.getDoc_id().replaceAll(Constant.ID_PREFIX_DOCUMENT,Constant.ID_PREFIX_REF)); // REF_ID => DOC -> REF 변경처리

			// 문서 소유자 설정 || 문서 수정의 경우 1.0 버전의 문서의 소유자가 소유자가 된다.
			if(isType.equals(Constant.INSERT))	{
				// 업무 등록의 경우 대표작성자가 owner가 된다
				if(StringUtil.isEmpty(documentVO.getOwner_id())){
					documentVO.setOwner_id(sessionVO.getSessId());
				}
				documentVO.setRoot_id(null);
			}else {

				// 문서의 소유자 정보는 소유권변경시에만 변경처리되면 문서수정시에는 기존버전의 OWNER_ID가 그대로 사용됨								
				// 문서의 버전번호 증가처리 
				if(version_type.equals(Constant.VERSION_MINOR_VERSION) || version_type.equals(Constant.VERSION_MAJOR_VERSION)) {
					documentVO.setVersion_no(CommonUtil.getUpVersion(documentVO.getVersion_no(), version_type));
				}
			}

		}

		// 2.3 IS_CURRENT/IS_EXPIRED/IS_LOCKED/ROOT_ID			
		documentVO.setIs_current(Constant.T);
		documentVO.setIs_expired(Constant.F);
		documentVO.setIs_locked(Constant.F);
		
		// 2.4 EX_ACL_ID :: 사용안함 별도 테이블 처리 => XR_EXACLITEM
		List<AclExItemVO> aclExItemList = CommonUtil.jsonArrayToExAclItemList(map);
		documentVO.setAclExItemList(aclExItemList);
	
		// 2.5 EXPIRED_DATE :: 만기일 DB에서 입력처리한다.
		
		// 2.6 AUTHOR_LIST  :: 미입력시 등록자 성명 입력처리
		if(documentVO.getAuthor_list().equals(""))	{
			documentVO.setAuthor_list(sessionVO.getSessName());
		}		
		
		// 3.1 첨부파일
		List<HashMap<String,Object>> fileList = CommonUtil.jsonArrayToFileList(map);
		documentVO.setInsertFileList(fileList);	
		
		// 3.2 삭제파일 :: 문서수정시에만 유효함
		List<HashMap<String,Object>> delFileList = new ArrayList<HashMap<String,Object>>();
		if(isType.equals(Constant.UPDATE))	{
			delFileList = CommonUtil.jsonArrayToDelFileList(map);
			documentVO.setDelFileList(delFileList);
		}				

		// 4.  문서 등록시&수정시 문서와Storage 용량 계산 :: [3003] START
		// if문 주석처리하는 이유는 문서등록 뿐아니라  문서수정할때에도 필요해서 주석처리.
		//if(isType.equals(Constant.INSERT)) {	
			long totalSize = 0;			
			//totalSize는 등록할문서의 사이즈
			for(int i=0; i < fileList.size(); i++) {
				totalSize = totalSize + Long.valueOf(fileList.get(i).get("fileSize").toString());
			}
			
			String rootFolderId = "";		

			//개인문서함 폴더일 경우
			if(map.get("map_id").toString().equals("MYPAGE")){				
				HashMap<String,Object> folderParam = new HashMap<String, Object>();
				folderParam.put("folder_id", documentVO.getFolder_id());
				//개인문서함일 경우 root_folder_id는 로그인한 sessionId
				map.put("root_folder_id",sessionVO.getSessId());
				rootFolderId = map.get("root_folder_id").toString();
				
			} else{
				// 부서함,프로젝트함 일 경우 
				HashMap<String,Object> folderParam = new HashMap<String, Object>();
				folderParam.put("folder_id", documentVO.getFolder_id());
				
				FolderVO getFolderInfo = new FolderVO();
				getFolderInfo = folderDao.folderDetail(folderParam);
				
				//rootFolderId는 부모폴더가아닌 자기자신폴더Id (부서함,프로젝트함의 폴더용량은 개인문서와 달리 각각 계산함)
				rootFolderId = getFolderInfo.getFolder_id();				
			} 
			
			FolderVO parentInfo = new FolderVO();
			FolderVO getFolder = new FolderVO();
			parentInfo.setFolder_id(rootFolderId);
			getFolder = folderDao.getFolderStorage(parentInfo);
			
			//폴더 quota
			long quota = getFolder.getStorage_quota();
			//폴더 사용량
			long usage = getFolder.getStorage_usage();
			//쿼터가 무제한이 아닐경우 
			if(quota > -1){
				long currentFileSize = 0;		
				//  문서수정시 폴더안의 남은 quota 보다 등록될 문서용량이 크면 문서등록 불가.
				if(isType.equals(Constant.UPDATE))	{
					
					// 문서수정시 현재버전유지 이면서 추가문서가있을경우에는 용량 체크한다. 추가문서가 없을경우는 체크안함
					if(version_type.equals(Constant.VERSION_SAME_VERSION) && totalSize >0 ){
						//추가문서가 있을때는 현재사용량+추가되는문서용량이 폴더쿼터보다 크면안된다.
						if((totalSize+usage) > quota){
							throw processException("storage.quota.overflow");				
					}
					//문서 수정시 현재버전유지가 추가문서가 없을경우는 체크안함, 부/주버전업은 체크필요.
					}else if(!version_type.equals(Constant.VERSION_SAME_VERSION)){
						currentFileSize=Long.valueOf(map.get("currentFileSize").toString());	
						if((quota - (usage + currentFileSize)) < totalSize){
							throw processException("storage.quota.overflow");				
						}
					}					
												
				} else {
					//폴더안의 남은 quota 보다 등록될 문서용량이 크면 문서등록 불가.
					if((quota - usage) < totalSize){
						throw processException("storage.quota.overflow");
					}
				}			
			}
			//[3003] END
		//}
		
		
		// 5.업무문서함의 경우만 해당됨(관련문서/다차원분류)
		if(map.get("map_id") != null && !map.get("map_id").toString().equals(Constant.MAP_ID_MYPAGE))	{
			
			// 5.1 관련문서
			//  jsonRefList=[{"root_ref_id":"DOC000000204579"},{"root_ref_id":"DOC000000204574"}
			List<String> refList = CommonUtil.jsonArrayToList(map,"jsonRefList","root_ref_id");
			documentVO.setRefList(refList);
					
			// 5.2 다차원분류
			List<String> multiFolders = CommonUtil.jsonArrayToList(map,"jsonMultiFolders", "folder_id");		
			documentVO.setMultiFolders(multiFolders);
				
		}else {
			// MYPAGE 인 경우  = ACL:기본권한 / 권한변경유무=F / 공유문서=F로 고정설정함
			documentVO.setAcl_id(Constant.ACL_ID_OWNER);
			documentVO.setIs_inherit_acl(Constant.F);
			documentVO.setIs_share(Constant.F);
		}

		// 문서설명 HTML ESCAPE
		String doc_description = map.get("doc_description") != null ? map.get("doc_description").toString() : "";
		documentVO.setDoc_description(StringUtil.java2Html(doc_description));
		
		return documentVO;
	}

	@Override
	public List<HashMap<String,Object>> docExtendedAttrList(HttpServletRequest request,String doc_type) throws Exception {
		
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		List<AttrVO>  attrList = new ArrayList<AttrVO>();
		
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();			
		
		String inputVal = "";
		
		// 1.문서유형 속성ITEM 가져온다.
		param.put("type_id",doc_type);
		param.put("is_extended",Constant.T);
		attrList =  typeDao.attrList(param);
		
		// 2.속성ITEM에 해당하는 입력값을 가져온다.
		if(attrList != null && attrList.size() > 0 ) {
			
			for(AttrVO attrVO : attrList) {
				
				HashMap<String, Object> input = new HashMap<String, Object>();
				
				// CHECK 인 경우 다중선택이 가능하여 분리하여 입력된 값을 받아서 처리한다.
				if(attrVO.getDisplay_type().equals(Constant.DISPLAY_TYPE_CHECK))	{
					
					// CHECKBOX 가 하나도 선택되지 않은 경우 예외처리
					String[] values = request.getParameterValues(attrVO.getAttr_id());
					StringBuffer buf = new StringBuffer();
					if(values == null)	{
						inputVal = "-1";
					}else {						
						for(int i = 0; i < values.length; i++){
							buf.append(values[i]);
							buf.append(",");
						}
						
						inputVal = buf.toString();
						if(inputVal.lastIndexOf(",") != -1 )	{
							inputVal = inputVal.substring(0,inputVal.length()-1);
						}
					}
					
					// ATTR_ID 값 설정(DISPLAY_TYPE=CHECK)
					input.put(attrVO.getAttr_id(),inputVal);
					
					buf.setLength(0);
					
				}else {
					// ATTR_ID 값 설정(DISPLAY_TYPE=SELECT/INPUT/RADIO)
					inputVal = request.getParameter(attrVO.getAttr_id());
					
					/************************************************************************
					if(attrVO.getDisplay_type().equals(Constant.DISPLAY_TYPE_INPUT)){
						inputVal = "%" + inputVal + "%";
					}
					***************************************************************************/
					input.put(attrVO.getAttr_id(), inputVal);					
				}
				
				ret.add(input);

			}
			
		}
		
		return ret;
	}
	
	@Override
	public Map<String, Object> writeDocProc(HashMap<String,Object> map,DocumentVO documentVO,List<HashMap<String,Object>> attrList,SessionVO sessionVO)
			throws Exception {
				
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);

		HashMap<String,Object> param = new HashMap<String,Object>();
		HashMap<String, Object> param2 = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
				
		List<PageVO> pageList = new ArrayList<PageVO>();				// 신규추가 파일 리스트		
		List<PageVO> copyPageList = new ArrayList<PageVO>();			// 이전버전 첨부복사 파일 리스트	
		List<PageVO> prePageList = new ArrayList<PageVO>();				// 이전버전 첨부파일 리스트
		List<String> delPageList = new ArrayList<String>();				// 이전버전 삭제대상 파일 리스트
		
		EXrepClient eXrepClient = new EXrepClient();					// eXrep C/S Client 생성. 
		
		int result = 0;
		long pageTotal = 0;
		
		// 0.업무구분 isType : insert/update  / version_type : SAME/MAJOR/MINOR/NEW
		String isType = map.get("isType").toString();
		String version_type = map.get("version_type").toString();
		String action_id = "";		// XR_DOCUMENT_HT 구분
		
		// 0.1 문서 ROOT_ID 및 ACTION_ID 설정 
		String root_doc_id = getRootId(isType,version_type,documentVO,documentDao);		
		if(isType.equals(Constant.UPDATE))	{
			documentVO.setRoot_id(root_doc_id);
			action_id = Constant.ACTION_UPDATE;
		}else {
			action_id = Constant.ACTION_CREATE;
		}
	
		// 1.PageVO 객체 생성  :: 첨부파일 존재시 이미 exrep에 등록 됨 (신규/수정)
		List<PageVO> insertPageList =  insertNewPage(documentVO); 
		pageList.addAll(insertPageList);
		
		// 새로추가되는 첨부파일의 size	[3011]
		long total_size=0;
		for(PageVO size: insertPageList){
			total_size += size.getPage_size();
		}
		map.put("total_size", total_size);
		
		// 1.1 문서수정시 파일복사 및 파일삭제 대상 목록 구하기
		if(isType.equals(Constant.UPDATE))	{
			
			// 이전버전의 첨부파일 목록을 가져온다.
			param.put("doc_id",documentVO.getPre_doc_id());
			prePageList = pageDao.comDocPageList(param);

			// 1.1.1 문서수정시 복사/삭제할 첨부파일 목록을 설정 :: 복사대상 파일목록 리스트 변경
			if(documentVO.getDelFileList() != null && documentVO.getDelFileList().size() > 0) {
				
				for(HashMap<String,Object> pageInfo : documentVO.getDelFileList()) {		

					for(int i=0;i<prePageList.size();i++)	{						
						PageVO pageVO = (PageVO)prePageList.get(i);
						if(pageVO.getPage_id().equals(pageInfo.get("page_id").toString()))	{
							prePageList.remove(i);
							delPageList.add(pageInfo.get("page_id").toString());
						}						
					}		// END OF FOR PREPAGELIST					
				}		// END OF FOR DELFILELIST							
			}		// END OF IF DELFILELIST
			
			// 1.1.2 복사대상 파일목록 리스트 
			copyPageList.addAll(prePageList);
			
		}		// END OF 문서수정
		
		// 2.데이터베이스 처리 
		// 2.1 XR_DOCUMENT INSERT 및 UPDATE(VERSION_TYPE=SAME)
		documentWrite(isType,version_type,pageTotal,documentVO,documentDao);
		
		// 2.2 MAP_ID 구분에 따른 별도 처리
		if(map.get("map_id") != null && !map.get("map_id").toString().equals(Constant.MAP_ID_MYPAGE))	{			
			docRefTableInit(isType,version_type,documentVO,root_doc_id,documentDao,folderDao);		// XR_LINKED DELETE :: 다중분류체계	|| XR_REF_DOC INSERT :: 관련문서	초기화			
			docRefTableWrite(documentVO,root_doc_id,documentDao,folderDao); 								// XR_LINKED INSERT :: 다중분류체계	|| XR_REF_DOC INSERT :: 관련문서			
		}else {			
			// MYPAGE AND 문서수정 VERSION_TYPE : SAME SKIPPED 
			xrLinkedWrite(isType,version_type,documentVO,folderDao); 
		}

		// 2.3 XR_FILED/XR_PAGE  INSERT : 버전업시에 신규파일이 첨부안되는 경우 조건처리 page_list != null 조건 삭제	
			if(isType.equals(Constant.INSERT) )		{				
				// 신규등록파일 DB처리 :: XR_PAGE/XR_FILED INSERT
				docPageInsert(pageList,documentVO,pageDao);				
			}else {
				
				// 2.3.1 SAME 버전처리	
				if(version_type.equals(Constant.VERSION_SAME_VERSION) ) {				
					pageInfoUpdate(delPageList ,documentVO,pageDao);			// XR_PAGE IS_DELETED=T UPDATE / XR_FILED DELETE					
					docPageInsert(pageList,documentVO,pageDao);				// 신규등록파일 DB처리 :: XR_PAGE / XR_FILED INSERT								
					pageList.addAll(copyPageList);										// PAGE_CNT,PAGE_TOTAL 업데이트 처리					
					
				}else {					
					// MAJOR/MINOR 버전처리					
					// 2.3.2 이전버전 첨부파일 목록 등록처리 :: copyPageList
					List<PageVO> insertCopyList = insertCopyPage(copyPageList,eXrepClient);
					pageList.addAll(insertCopyList);			
					
					// 2.3.3 신규등록파일 DB처리 :: XR_PAGE / XR_FILED INSERT
					docPageInsert(pageList,documentVO,pageDao);	
					
				}	// END OF MAJOR/MINOR 버전처리
				
			}	// 	END OF 문서수정처리
			// 2.3.4 첨부파일수 및 첨부파일 용량을 계산하여 업데이트 처리한다.
			if(DocPageInfoUpdate(pageList,documentVO,documentDao) == 0)	{
				throw processException("common.system.error");	
			}
			
			
		// 2.5 확장속성인 경우 확장문서 테이블에 입력처리		
		extendTypeWrite(map,version_type, attrList,documentVO,typeDao); 
		
		// 2.6 XR_REF_DOC 등록처리
		// 2.6.1 기존의 관련문서 제거
		param2.put("table_nm", Constant.DOC_REF_TABLE);
		param2.put("doc_id", root_doc_id);
		documentDao.docCommonDelete(param2);
		
		// 2.6.2 최종버전에 관련문서 추가
		for (String refId : documentVO.getRefList()) {
			param2.clear();
			param2.put("root_doc_id", root_doc_id);
			param2.put("root_ref_id", refId);
			documentDao.writeRefDoc(param2);
			
		}
		
		// 2.7 추가 접근자 처리 :: XR_EXACLITEM 등록 처리

		// 2.7.1 기존 추가접근자 지운다
		param2.clear();
		if(!isType.equals(Constant.INSERT)){
			aclService.aclExItemDelete(param2, documentVO.getDoc_id());
		}
		
		// 2.7.2 추가 접근자 등록
		for(AclExItemVO aclExItemVO : documentVO.getAclExItemList()){
			aclExItemVO.setDoc_id(documentVO.getDoc_id());
			aclService.aclExItemWrite(param2, aclExItemVO);
		}
		
		// 2.7. XR_DOCUMENT_HT 등록처리 ::  ACTION : CREATE
		long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);		
		result = docHistoryWrite(historyDao,doc_seq,root_doc_id,documentVO.getDoc_id(),action_id,documentVO.getDoc_type(), 
				documentVO.getDoc_name(),documentVO.getVersion_no(),sessionVO);
		if(result == 0)	{	throw processException("common.system.error");	}
		
		// 2.8. 문서등록,수정시 xr_folder의 storage_usage 업데이트 처리 [3004] START	
		//문서수정시 현재버전유지 이면서 추가되는파일이 있을때는 추가문서용량만 업데이트를한다.
//		if(version_type.equals(Constant.VERSION_SAME_VERSION) && total_size >0  ) {	
//			//total_size는 새로추가되는 문서사이즈
//			param.put("total_size",total_size);			
//			if(map.get("map_id").equals(Constant.MAP_ID_MYPAGE)){
//				param.put("folder_id", map.get("root_folder_id"));				
//			}else{				
//				param.put("folder_id",documentVO.getFolder_id());			
//			}
//			// [3011]
//			folderDao.storageUsageUpdateSameVer(param);
//			
//		// 문서수정시 부/주버전 업데이트일때 
//		}else if(!version_type.equals(Constant.VERSION_SAME_VERSION)){
//			storageUsageUpdateControl(action_id, folderDao, documentVO, map);
//		}		
		storageUsageUpdateControl(action_id, folderDao, documentVO, map);

		// [3004] EDN
		if(!documentVO.getDoc_status().equals(Constant.DOCUMENT_STATUS_PROCESS_ING)){
			//최근등록한 문서
			int recently_id = commonService.commonNextVal(Constant.COUNTER_ID_RECENTLY);
			// 3. recently set 	
			RecentlyObjectVO recentlyObjectVo = new RecentlyObjectVO();
			recentlyObjectVo.setIdx(CommonUtil.getStringID(Constant.ID_PREFIX_RECENTLY, recently_id));
			recentlyObjectVo.setUser_id(sessionVO.getSessId());
			recentlyObjectVo.setTarget_id(documentVO.getDoc_id());
			if(map.get("map_id").toString().equals(Constant.MAP_ID_MYPAGE)){
			recentlyObjectVo.setTarget_type(Constant.RECENTLY_TYPE_DOCUMENT_PRIVATE);
			}else{
				recentlyObjectVo.setTarget_type(Constant.RECENTLY_TYPE_DOCUMENT_WORK);
			}
			// 3-1 recently Dao 처리 
			commonService.insertRecentlyObject(recentlyObjectVo);
		}					
		
		resultMap.put("result",Constant.RESULT_TRUE);		
		return resultMap;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 :  문서 등록/수정시 복사 첨부파일
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : insertCopyPage
	 * @param copyPageList
	 * @param eXrepClient
	 * @return
	 * @throws Exception List<PageVO>
	 */
	public List<PageVO> insertCopyPage(List<PageVO> copyPageList,EXrepClient eXrepClient) throws Exception {
		
		List<PageVO> ret = new ArrayList<PageVO>();
		if(copyPageList != null && copyPageList.size() > 0)	{
			eXrepClient.connect();	
		
			for(PageVO pageVO : copyPageList)	{
				// 문서 첨부파일 객체 생성 - 신규등록시
				PageVO newPageVO = setPageInfo(pageVO,eXrepClient); 
				ret.add(newPageVO);			// 복사된 파일목록을 신규목록에 추가한다.
			}
			
			eXrepClient.disconnect();		
		}
		
		return ret;
	}
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 등록/수정시 신규 첨부파일
	 * 2. 처리내용 : 물리적 파일은 이미 exRep ECM에 등록되어 있음.
	 * </pre>
	 * @Method Name : insertNewPage
	 * @param documentVO
	 * @return
	 * @throws Exception List<PageVO>
	 */
	public List<PageVO> insertNewPage(DocumentVO documentVO) throws Exception {
		
		List<PageVO> ret = new ArrayList<PageVO>();
		
		if(documentVO.getPage_cnt() > 0  )	{
			
			for(HashMap<String,Object> pageInfo : documentVO.getInsertFileList())	{				
				// 문서 첨부파일 객체 생성 - 신규등록시
				PageVO pageVO = setPageInfo(pageInfo); 		
				ret.add(pageVO);	// DB에 등록할 객체 생성
			}
			
		}
		
		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 등록/수정시 신규 첨부파일
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : insertNewPage
	 * @param documentVO
	 * @param eXrepClient
	 * @return throw new Exception();
	 * @throws Exception List<PageVO>
	 */
	@Deprecated
	public List<PageVO> insertNewPage(DocumentVO documentVO, EXrepClient eXrepClient) throws Exception {
		throw new Exception();
//		List<PageVO> ret = new ArrayList<PageVO>();
//		
//		if(documentVO.getPage_cnt() > 0  )	{
//			
//			eXrepClient.connect();		
//			
//			for(HashMap<String,Object> pageInfo : documentVO.getInsertFileList())	{				
//				// 문서 첨부파일 객체 생성 - 신규등록시
//				// 사용안함
//				throw new Exception();
////				PageVO pageVO = setPageInfo(pageInfo,eXrepClient); 				
////				ret.add(pageVO);	// DB에 등록할 객체 생성
//			}
//			
//			eXrepClient.disconnect();
//		}
//		
//		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서수정시 관련테이블 초기화
	 * 2. 처리내용 : XR_LINKED / XR_RFE_DOC 초기화
	 * </pre>
	 * @Method Name : docRefTableInit
	 * @param isType
	 * @param version_type
	 * @param documentVO
	 * @param root_doc_id
	 * @param documentDao
	 * @param folderDao void
	 */
	public void docRefTableInit(String isType,String version_type,DocumentVO documentVO,String root_doc_id,
			DocumentDao documentDao,FolderDao folderDao) throws Exception {
	
		HashMap<String,Object> param1 = new HashMap<String,Object>();
		HashMap<String,Object> param2 = new HashMap<String,Object>();
		
		int result = 0;
		
		// 1, SAME VERSION 처리
		if(isType.equals(Constant.UPDATE) && version_type.equals(Constant.VERSION_SAME_VERSION)  )	{
			// XR_LINKED DELETE :: 다중분류체계 삭제처리
			param1.put("doc_id",documentVO.getDoc_id());
			folderDao.xrLinkedDelete(param1);
			
			// XR_LINKED INSERT :: 기본폴더분류체계 등록처리
			param1.put("folder_id",documentVO.getFolder_id());		
			folderDao.writeXrLinked(param1);
			
			// XR_REF_DOC DELETE :: 관련문서 삭제처리
			param2.put("doc_id",root_doc_id);
			param2.put("table_nm",Constant.DOC_REF_TABLE);
			documentDao.docCommonDelete(param2);
			
		}else if(isType.equals(Constant.UPDATE) && !version_type.equals(Constant.VERSION_SAME_VERSION)  )	{			// 1,  MAJOR/MINOR VERSION 처리
			
			// MAJOR/MINOR VERSION :: 이전버전 다중분류체계 및 관련문서 정보 삭제처리
			// XR_LINKED DELETE :: 다중분류체계 삭제처리
			param1.put("doc_id",documentVO.getPre_doc_id());
			folderDao.xrLinkedDelete(param1);
			
			// XR_LINKED INSERT :: 기본폴더분류체계 등록처리
			param1.put("folder_id",documentVO.getFolder_id());		
			folderDao.writeXrLinked(param1);
			
			// XR_REF_DOC DELETE :: 관련문서 삭제처리(이전버전)
			param2.put("doc_id",root_doc_id);
			param2.put("table_nm",Constant.DOC_REF_TABLE);
			documentDao.docCommonDelete(param2);
			
			// XR_LINKED INSERT :: MAJOR/MINOR VERSION
			param1.clear();
			param1.put("folder_id",documentVO.getFolder_id());		
			param1.put("doc_id",documentVO.getDoc_id());
			result = folderDao.writeXrLinked(param1);
			if(result == 0)	{	throw processException("common.system.error");	}	
			
			
		}else if(isType.equals(Constant.INSERT))	{ 
			
			// XR_LINKED INSERT :: NEW VERSION
			param1.put("folder_id",documentVO.getFolder_id());		
			param1.put("doc_id",documentVO.getDoc_id());
			result = folderDao.writeXrLinked(param1);
			if(result == 0)	{	throw processException("common.system.error");	}	
		}
				
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 등록/수정시 관련 테이블 등록처리
	 * 2. 처리내용 : XR_LINKED / XR_RFE_DOC 등록처리
	 * </pre>
	 * @Method Name : docRefTableWrite
	 * @param documentVO
	 * @param root_doc_id
	 * @param documentDao
	 * @param folderDao
	 * @throws Exception void
	 */
	public void docRefTableWrite(DocumentVO documentVO,String root_doc_id,DocumentDao documentDao,FolderDao folderDao) throws Exception {
		
		HashMap<String,Object> param1 = new HashMap<String,Object>();
		HashMap<String,Object> param2 = new HashMap<String,Object>();
		
		int result = 0;
		
		// XR_LINKED INSERT :: 다중분류체계			
		param1.put("doc_id",documentVO.getDoc_id());
		if(documentVO.getMultiFolders() != null && documentVO.getMultiFolders().size() > 0)	{ 	
			for(String folder_ids : documentVO.getMultiFolders()) {
				
				param1.put("folder_id",folder_ids);
									
				if(!folder_ids.equals(documentVO.getFolder_id())) {
					result = folderDao.writeXrLinked(param1);
					if(result == 0)	{	throw processException("common.system.error");	}
				}
								
			}				
		}
		
		// XR_REF_DOC INSERT :: 관련문서
		if(documentVO.getRefList() != null && documentVO.getRefList().size() > 0)	{
			
			// 문서신규 등록인 경우 ROOT_ID = NULL				
			param2.put("root_doc_id",root_doc_id);
			
			for(String root_id : documentVO.getRefList()) {
				param2.put("root_ref_id",root_id);
				/////////////////////////////////////////////////////////////////////////////////////////////
				// 연계처리 후 테스트 진행
				/*
				result  = documentDao.writeRefDoc(param2);
				if(result == 0)	{	throw processException("common.system.error");	}
				*/					
				 //////////////////////////////////////////////////////////////////////////////////////////////////////
			}
		}
		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 첨부파일 객체 생성 - 신규등록시
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setPageInfo
	 * @param pageInfo
	 * @return
	 * @throws Exception PageVO
	 */
	public PageVO setPageInfo(HashMap<String,Object> pageInfo) throws Exception {
		
		PageVO pageVO = new PageVO();
		
		pageVO.setPage_id(CommonUtil.getStringID(Constant.ID_PREFIX_PAGE, commonService.commonNextVal(Constant.COUNTER_ID_PAGE))); 
		pageVO.setPage_name(pageInfo.get("orgFile").toString());
		pageVO.setPage_extension(CommonUtil.getFileExtension(pageInfo.get("orgFile").toString()));
		pageVO.setPage_size(Long.parseLong(pageInfo.get("fileSize").toString()));
		//암호화하기 2015/5/18
		//pageVO.setVolume_id(ARIAUtil.ariaEncrypt(pageInfo.get("volumeId").toString(),doc_id));	
		//pageVO.setVolume_id(ARIAUtil.ariaEncrypt(pageInfo.get("contentPath").toString(),doc_id));	
		pageVO.setVolume_id(pageInfo.get("volumeId").toString()); 
		pageVO.setContent_path(pageInfo.get("contentPath").toString());
		
		return pageVO;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 첨부파일 객체 생성 - 신규등록시
	 * 2. 처리내용 : 사용안함
	 * </pre>
	 * @Method Name : setPageInfo
	 * @param pageInfo
	 * @param eXrepClient
	 * @return throw new Exception();
	 * @throws Exception PageVO
	 */
	@Deprecated
	public PageVO setPageInfo(HashMap<String,Object> pageInfo,EXrepClient eXrepClient) throws Exception {
		throw new Exception();
		/*PageVO pageVO = new PageVO();
		
		pageVO.setPage_id(CommonUtil.getStringID(Constant.ID_PREFIX_PAGE, commonService.commonNextVal(Constant.COUNTER_ID_PAGE))); 
		pageVO.setPage_name(pageInfo.get("orgFile").toString());
		pageVO.setPage_extension(CommonUtil.getFileExtension(pageInfo.get("orgFile").toString()));
		pageVO.setPage_size(Long.parseLong(pageInfo.get("fileSize").toString()));
		pageVO.setVolume_id(ConfigData.getString(Constant.EXREP_VOLUME_NM));
		pageVO.setContent_path(CommonUtil.getContentPathByDate(ConfigData.getString(Constant.EXREP_ROOT_EDMS_NM))+UUID.randomUUID().toString());
		
		// eXrep file put
		if(!eXrepClient.putFile(pageInfo.get("filePath").toString(),ConfigData.getString(Constant.EXREP_VOLUME_NM), pageVO.getContent_path(),true)) {										
			throw processException("doc.fail.regist.exrep");
		}
		
		return pageVO;*/
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 첨부파일 객체 생성 - 문서수정시 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : setPageInfo
	 * @param pageVO
	 * @param eXrepClient
	 * @return
	 * @throws Exception PageVO
	 */
	public PageVO setPageInfo(PageVO pageVO,EXrepClient eXrepClient) throws Exception {
		
		PageVO newPageVO = new PageVO();
		
		newPageVO.setPage_id(CommonUtil.getStringID(Constant.ID_PREFIX_PAGE, commonService.commonNextVal(Constant.COUNTER_ID_PAGE))); 
		newPageVO.setPage_name(pageVO.getPage_name());
		newPageVO.setPage_extension(pageVO.getPage_extension());
		newPageVO.setPage_size(pageVO.getPage_size());
		newPageVO.setVolume_id(ConfigData.getString(Constant.EXREP_VOLUME_NM));
		newPageVO.setContent_path(CommonUtil.getContentPathByDate(ConfigData.getString(Constant.EXREP_ROOT_EDMS_NM))+UUID.randomUUID().toString());
		
		if(!eXrepClient.copyFile(newPageVO.getVolume_id(), pageVO.getContent_path(), newPageVO.getContent_path(),true))	{
			throw processException("doc.fail.regist.exrep");
		}
		
		return newPageVO;
	}
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 첨부관련 정보(첨부수/첨부파일용량) 변경처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : DocPageInfoUpdate
	 * @param pageList
	 * @param documentVO
	 * @param documentDao
	 * @return int
	 */
	public int DocPageInfoUpdate(List<PageVO> pageList,DocumentVO documentVO,DocumentDao documentDao) {
		
		int result = 0;
		long page_size = 0;
				
		if(pageList != null && pageList.size() > 0 )	{
			for(PageVO pageVO : pageList)	{			
				page_size += pageVO.getPage_size();						
			}
			documentVO.setPage_total(page_size);
			documentVO.setPage_cnt(pageList.size());
		}else {
			documentVO.setPage_total(0);
			documentVO.setPage_cnt(0);
		}
			
		// XR_DOCUMENT PAGE_CNT/PAGE_TOTAL 변경처리
		documentVO.setUpdate_action(Constant.ACTION_PAGE);		
					
		result = documentDao.documentUpdate(documentVO);

		return result;
	}

	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서수정 - SAME
	 * 2. 처리내용 : XR_PAGE IS_DELETED=T && XR_FILED DELETE
	 * </pre>
	 * @Method Name : pageInfoUpdate
	 * @param delPageList
	 * @param documentVO
	 * @param pageDao void
	 */
	public void pageInfoUpdate(List<String> delPageList ,DocumentVO documentVO,PageDao pageDao) throws Exception {	
		
		int result = 0;
		
		if(delPageList != null && delPageList.size() > 0) {
			
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("is_deleted",Constant.T);
			param.put("doc_id",documentVO.getDoc_id());
			
			for(String page_id : delPageList)	{
				
				// XR_PAGE IS_DELETED=T :: parameter - is_deleted,page_id
				param.put("page_id",page_id);
				result = pageDao.pageInfoUpdate(param);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// XR_FILED DELETE :: parameter - doc_id,page_id
				result = pageDao.xrFiledDelete(param);					
				if(result == 0)	{	throw processException("common.system.error");	}
			}			
		}		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서첨부파일 DB등록처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : docPageInsert
	 * @param pageList
	 * @param documentVO
	 * @param pageDao
	 * @throws Exception void
	 */
	public void docPageInsert(List<PageVO> pageList,DocumentVO documentVO,PageDao pageDao) throws Exception{
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("doc_id", documentVO.getDoc_id());
		
		int result = 0;
		if(pageList != null && pageList.size() > 0 )	{
			for(PageVO pageVO : pageList)	{
				result = pageDao.writePage(pageVO);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// page_no는 insert query에서 처리
				param.put("page_id",pageVO.getPage_id());
				result = pageDao.writeXrFiled(param);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// XR_PAGE_HT CREATE 이력은 SKIP :: XR_PAGE_HT 이력은 조회용으로만 사용처리함
			}
		}
	}
	

	@Override
	public List<HashMap<String,Object>> favoriteDocValidList(HashMap<String, Object> map, SessionVO sessionVO) throws Exception{
		
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		
		/***********************************************************************************************************
		 * 주) 즐겨찾기 문서 처리 파라미터정의 JsonArry 형식이며 아래의 필수값 포함해야됨.
		 * docList=[
		 * {"doc_id":"DOC000000007536","root_id":""},
		 * {"doc_id":"DOC000000007535","root_id":""},
		 * {"doc_id":"DOC000000007534","root_id":""}]
		 ***********************************************************************************************************/
		
		//doc_id, user_id, folder_id, root_id
		String docList = map.get("docList") != null ? map.get("docList").toString() : "";
		String folderId = map.get("folder_id") != null ? map.get("folder_id").toString() : "";
		
		//1.입력 유효성 체크 (즐겨찾기 삭제 / 추가)
		if (StringUtil.getMapString(map, Constant.TYPE).equals(Constant.ACTION_DELETE_FAVORITES)) {
			if(docList.equals("")){
				throw processException("common.required.error");
			}
		} else {
			if(docList.equals("") || folderId.equals("")){
				throw processException("common.required.error");
			}
		}
		
		// 2.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0) {
			for(int j=0; j<jsonArray.size(); j++){
				HashMap<String,Object> docInfo = new HashMap<String,Object>();
				String root_id = jsonArray.getJSONObject(j).getString("root_id").toString().equals("") ? jsonArray.getJSONObject(j).getString("doc_id").toString() : jsonArray.getJSONObject(j).getString("root_id").toString();
				
				docInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
				docInfo.put("user_id", sessionVO.getSessId());
				docInfo.put("folder_id", folderId);
				docInfo.put("root_id", root_id);
				
				ret.add(docInfo);
			}
		}
		return ret;
	}
	
	@Override
	public Map<String, Object> favoriteInsert(List<HashMap<String,Object>> docList, HashMap<String,Object>map, SessionVO sessionVO) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		for(HashMap<String,Object> imap : docList){
			
			// 1. 기존의 즐겨찾기 삭제
			documentDao.deleteFavoriteDoc(imap);
			
			// 2. 문서 즐겨찾기 추가
			documentDao.favoriteDocWrite(imap);
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}

	


	@Override
	public Map<String, Object> favoriteDelete(List<HashMap<String, Object>> docList, HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		for(HashMap<String,Object> imap : docList){
			
			// 1. 즐겨찾기 삭제
			documentDao.deleteFavoriteDoc(imap);
			
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);

		return resultMap;
	}

	@Override
	public List<HashMap<String, Object>> cancelCheckoutValidList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		
			
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();	

		/***********************************************************************************************************
		 * 주) 체크아웃취소 처리 파라미터정의 JsonArry 형식이며 아래의 필수값 포함해야됨.
		 * docList=[
		 * {"doc_id":"DOC000000007536","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007535","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007534","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"}]
		 ***********************************************************************************************************/
		String docList =  map.get("docList") != null ? map.get("docList").toString() : "";
				
		// 1.입력값 유효성 체크 
		if(docList.equals(""))	{
			throw processException("common.required.error");
		}
		
		// 2.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0 ) {		
			for(int j=0;j < jsonArray.size();j++) {				 
				HashMap<String, Object> docInfo = new HashMap<String, Object>();
				
				docInfo.put("doc_id",jsonArray.getJSONObject(j).getString("doc_id").toString());
				docInfo.put("root_id",jsonArray.getJSONObject(j).getString("root_id").toString());
				docInfo.put("is_locked",jsonArray.getJSONObject(j).getString("is_locked").toString());
				docInfo.put("doc_type",jsonArray.getJSONObject(j).getString("doc_type").toString());
				 
				 
				 // 4. 문서잠금 여부 체크
				if(docInfo.get("is_locked").toString().equals(Constant.T)) {					
					docInfo.put("is_locked", Constant.F);
				} else {
					// checkout cancel 대상이 아니다.
					throw processException("common.required.error");					
				}
				ret.add(docInfo);
			}
		}
		return ret;
	}

	@Override
	public List<HashMap<String, Object>> restoreValidList(HashMap<String,Object> map) throws Exception {
	
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
		
		/***********************************************************************************************************
		 * 주) 문서(삭제/완전삭제/휴지통) 처리 파라미터정의 JsonArry 형식이며 아래의 필수값 포함해야됨.
		 * docList=[
		 * {"doc_id":"DOC000000007536","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007535","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007534","root_id":"","is_locked":"F","type_id","XR_DOCUMENT"}]
		 ***********************************************************************************************************/
		String docList =  map.get("docList") != null ? map.get("docList").toString() : "";
		String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		FolderVO rootFolderInfo = new FolderVO();		
		rootFolderInfo.setFolder_id(targetFolderId);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);	
		FolderVO getFolder = folderDao.getFolderStorage(rootFolderInfo);
		long getRootQuota = getFolder.getStorage_quota();
		long getRootUsage = getFolder.getStorage_usage();
		
		// 1.입력값 유효성 체크
		if(docList.equals("") ||  targetFolderId.equals(""))	{
			throw processException("common.required.error");
		}				
		//.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0 ) {		
			 for(int j=0;j < jsonArray.size();j++)	{				 
				 
				 HashMap<String, Object> docInfo = new HashMap<String, Object>();
				 
				 docInfo.put("doc_id",jsonArray.getJSONObject(j).getString("doc_id").toString());
				 docInfo.put("doc_name", jsonArray.getJSONObject(j).getString("doc_name").toString());
				 docInfo.put("root_id",jsonArray.getJSONObject(j).getString("root_id").toString());
				 docInfo.put("type_id",jsonArray.getJSONObject(j).getString("doc_type").toString());
				 //개인문서함인지 부서/프로젝트함인지 구별하기위한 map_id를 docInfo에 담기
				 docInfo.put("map_id",jsonArray.getJSONObject(j).getString("map_id").toString());
				 
				    // 휴지통에서 복원시 문서와Storage 용량 계산 ::  [3003]
					List<PageVO> getPageList = new ArrayList<PageVO>();			
					getPageList = pageDao.comDocPageList(docInfo);
										
					long totalSize = 0; //복원될 문서의 용량
					for(int i=0; i<getPageList.size(); i++){
						totalSize = totalSize + getPageList.get(i).getPage_size();
					}					
					if(getRootQuota > -1) {
						if((getRootQuota - getRootUsage) < totalSize) {
							throw processException("storage.quota.overflow");
						}
					}					
				 ret.add(docInfo);
			 }
		}

		return ret;
	}
	

	@Override
	public Map<String, Object> restoreDocument(List<HashMap<String, Object>> docList ,HashMap<String,Object> map,SessionVO sessionVO) 
			throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
		
		// 휴지통 문서 복원 시 만기문서가 있을 경우 설정된 보존기간
		int getExpiredYear = ConfigData.getInt("DELETE_EXPIRED_DEFAULT_YEAR");
		
		// 0. 이동할 Folder의 ACL_ID 얻기		
		FolderVO folderVO = new FolderVO();
		folderVO.setFolder_id(targetFolderId);
		FolderVO getFldInfo = folderDao.getFolderAcl(folderVO);
		String folderAcl = getFldInfo.getAcl_id();
		for(HashMap<String,Object> dMap : docList) {
			
			/*****************************************************************************************
			// 주) 휴지통 문서 복원 
			 * XR_DOCUMENT : DOC_STUATS D => C 로 변경처리
			 * XR_LINKED 기존 데이터 삭제처리
			 * XR_LINKED 새로운 데이터 추가처리
			*****************************************************************************************/
			// 1.모든 버전 문서 목록 리스트 가져오기
			String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
			String doc_id = dMap.get("doc_id").toString();
			allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_TABLE);
							
			for(DocumentVO vo : allVersionList)	{
			
				// 1.기존 DOCUMENT 정보 얻기
				DocumentVO oriDoc = documentDao.getDocumentInfo(dMap);
				String oriFolderId = oriDoc.getFolder_id();
							
				// 2.DOC_STATUS 확인 :: DOC_STATUS == 'C' 이면 EXCEPTION
				if(!oriDoc.getDoc_status().equals(Constant.D)){
				//	throw processException("common.required.error");
				}
				
				// 3.동일 문서명 정보 / CHECK 
				HashMap<String, Object> docInfo = new HashMap<String, Object>();
				docInfo.put("folder_id", targetFolderId);
				docInfo.put("doc_id", vo.getDoc_id());
				docInfo.put("doc_name", vo.getDoc_name());
				docInfo.put("is_current", vo.getIs_current());
				docInfo.put("doc_status", vo.getDoc_status());

				String getDocId = vo.getDoc_id();
				String getDocName = vo.getDoc_name();
				String newName = getDocName;
				if(documentDao.isExitsDocName(docInfo) > 0) {
					newName = getUniqueDocumentName(getDocName, targetFolderId, Constant.ACTION_RESTORE);
				}
				
				// 4.복원할 문서 정보
				docInfo.put("type", Constant.ACTION_RESTORE);
				vo.setUpdate_action(Constant.ACTION_RESTORE);
				vo.setDoc_id(getDocId);
				vo.setDoc_name(newName);
				vo.setDoc_status(Constant.C);
				vo.setFolder_id(targetFolderId);
				vo.setAcl_id(folderAcl);
				vo.setDeleter_id(null);
				vo.setDeleter_name(null);
				vo.setDelete_date(null);
				
				// 휴지통 문서중 만기문서가 존재할때
				if(oriDoc.getIs_expired().toString().equals(Constant.T)) {
					//is_expired : T -> F
					vo.setIs_expired(Constant.F);					
					vo.setPreservation_year(getExpiredYear);					
				} else {
					vo.setIs_expired(null);				
				}						
				
				// 문서복원시 xr_folder의 storage_usage 업데이트 처리 [3006]
				//개인문서함일 경우에는 업데이트될 폴더아이디가 최상의루트폴더 아이디. 즉 sessionId
				dMap.put("user_id", sessionVO.getSessId());
				dMap.put("target_folder_id", targetFolderId);
				
				// 복원시에는 total_size가 없어서 0으로 처리	
				map.put("total_size", 0);
				dMap.put("total_size", map.get("total_size").toString());
				storageUsageUpdateControl(Constant.ACTION_RESTORE, folderDao, vo, dMap);
				
				// 5.XR_DOCUMENT :: UPDATE
				resultMap = documentUpdate(docInfo, vo, sessionVO);
				
				// 6.XR_LINKED :: DELETE => INSERT
				HashMap<String,Object> linked = new  HashMap<String, Object>();
				linked.put("doc_id",vo.getDoc_id());
				linked.put("folder_id", oriFolderId);
				
				folderDao.xrLinkedDelete(linked);
				
				linked.put("folder_id", targetFolderId);
				folderDao.writeXrLinked(linked);		
				
				// 7. XR_DOCUMENT_HT :: ACTION_ID = RESTORE
				// XR_DOCUMENT_HT 등록할 Beford_nm, After_nm 값 셋팅
				HashMap<String, Object> htMap = new HashMap<String, Object>();
				htMap = moveDocumentHT(oriFolderId, targetFolderId);

				// 8. XR_DOCUMENT_HT 등록처리 :: ACTION - MOVE
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
				docHistoryWrite(historyDao, doc_seq, root_id, vo.getDoc_id(), Constant.ACTION_RESTORE, vo.getDoc_type(), vo.getDoc_name(), vo.getVersion_no(), htMap, sessionVO);
				
			}

		}	// END OF FOR		

		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;		
	}
	

	@Override
	public String getUniqueDocumentName(String resourceName, String folderId, String actionStatus) throws Exception{

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);		
		String uniqueDocName = resourceName;
		String uniqueFileName = "";
		String uniqueExtName = "";
		
		// Return Value Initialization.
		String ret = null;
		
		//[3002] 버전복원을 같은폴더에 중복으로 복원할경우 처리
    	if (uniqueDocName.contains(".")) {    		
	    	uniqueFileName = resourceName.substring(0, resourceName.lastIndexOf("."));  // 원본 이름(확장자 제외)
	    	uniqueExtName = resourceName.substring(resourceName.lastIndexOf("."), resourceName.length());  // 원본 확장자
	    	if(uniqueExtName.contains("]")){	    		
	    		uniqueFileName = uniqueDocName;  // 원본 이름(확장자 제외)
		    	uniqueExtName = "";  // 원본 확장자
	    	}	    	
    	} else {    		
    		uniqueFileName = uniqueDocName;  // 원본 이름(확장자 제외)
	    	uniqueExtName = "";  // 원본 확장자
    	}
    	// 여기서 이름이 비슷한 문서들을 전부 가져온다.
    	HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("uniqueFileName", uniqueFileName);
		param.put("uniqueExtName", uniqueExtName);
		param.put("folderId", folderId);
    	List<String> targetDocNameList = documentDao.selectSimilarDocumentListByDocNameAndFolderID(param);
    	
    	
    	// 명령을 구분하여 첨자를 설정.
    	if(actionStatus.equals(Constant.ACTION_CREATE)) {
    		uniqueFileName = uniqueFileName + " - 등록본";
    	} else if(actionStatus.equals(Constant.ACTION_COPY)) {
    		uniqueFileName = uniqueFileName + " - 복사본";
    	} else if(actionStatus.equals(Constant.ACTION_MOVE)) {
    		uniqueFileName = uniqueFileName + " - 이동본";
    	} else if(actionStatus.equals(Constant.ACTION_RESTORE)) {
    		uniqueFileName = uniqueFileName + " - 복원본";
    	} else if(actionStatus.equals(Constant.ACTION_ADMIN_RESTORE)) {
    		uniqueFileName = uniqueFileName + " - 관리자복원본"; // [2000]
    	}
    	
    	int index = 0;
		boolean isUnique = false;
		do {
			if (index > 0) {
				ret = uniqueFileName + " (" + index + ")" + uniqueExtName;
			} else {
				ret = uniqueFileName + uniqueExtName;
			}
			for (String file : targetDocNameList) {
				isUnique = ret.equals(file);
				if (isUnique) break;
			}
			index++;
		} while(isUnique);
		
		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 :  문서정보 등록 및 수정처리
	 * 2. 처리내용 : XR_DOCUMENT INSERT/UPDATE 처리
	 * </pre>
	 * @Method Name : documentWrite
	 * @param isType
	 * @param version_type
	 * @param documentVO
	 * @param documentDao
	 * @throws Exception void
	 */
	public void documentWrite(String isType,String version_type,long pageTotal,DocumentVO documentVO,
			DocumentDao documentDao) throws Exception {
		
		int result = 0;
		
		if(isType.equals(Constant.INSERT) || 
				version_type.equals(Constant.VERSION_MINOR_VERSION) || version_type.equals(Constant.VERSION_MAJOR_VERSION) )	{
			
			// 문서등록 :: XR_DOCUMENT INSERT
			documentVO.setPage_total(pageTotal);
			result = documentDao.writeDocument(documentVO);
			if(result == 0)	{	throw processException("common.system.error");	}
			
			// 문서수정 :: VERSION_TYPE=MAJOR/MINOR 이전버전 LOCK 해제
			if(version_type.equals(Constant.VERSION_MINOR_VERSION) || version_type.equals(Constant.VERSION_MAJOR_VERSION)){
				
				DocumentVO preDocumentVO = new DocumentVO();
				
				preDocumentVO.setIs_current(Constant.F);
				preDocumentVO.setDoc_id(documentVO.getPre_doc_id());
				preDocumentVO.setIs_locked(Constant.F);
				preDocumentVO.setUpdate_action(Constant.ACTION_MODIFY);
				result = documentDao.documentUpdate(preDocumentVO);
				if(result == 0)	{	throw processException("common.system.error");	}
			}
			
		}else {
			// 문서수정 && VERSION_TYPE=SAME
			documentVO.setUpdate_action(Constant.VERSION_SAME_VERSION);
			result = documentDao.documentUpdate(documentVO);
			if(result == 0)	{	throw processException("common.system.error");	}
			
		}
		
	}

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 ROOT_ID 값 설정하기
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : getRootId
	 * @param isType
	 * @param version_type
	 * @param documentVO
	 * @param documentDao
	 * @return String
	 */
	public String getRootId(String isType,String version_type,DocumentVO documentVO,DocumentDao documentDao)	{
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		String root_id = "";
		
		if(isType.equals(Constant.INSERT)) {
			if(documentVO.getRoot_id() != null && documentVO.getRoot_id().length() > 0)	{
				root_id = documentVO.getRoot_id();
			}else {
				root_id = documentVO.getDoc_id();
			}
		}else {						
			param.put("table_nm",Constant.DOC_TABLE);
			param.put("doc_id",documentVO.getPre_doc_id());
			DocumentVO docVO = documentDao.commonDocDetail(param);
			
			if(docVO.getRoot_id() != null && docVO.getRoot_id().length() > 0)	{
				root_id = docVO.getRoot_id();
			}else {
				root_id = docVO.getDoc_id();
			}
		}
		
		return root_id;
	}

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서등록 XR_LINKED 파일 등록처리
	 * 2. 처리내용 : 내문서이면서 동일버전 수정인 경우는 제외한다.
	 * </pre>
	 * @Method Name : xrLinkedWrite
	 * @param isType
	 * @param version_type
	 * @param documentVO
	 * @param folderDao
	 * @throws Exception void
	 */
	public void xrLinkedWrite(String isType,String version_type,DocumentVO documentVO,FolderDao folderDao) throws Exception {
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		int result = 0;
		
		// 문서가 등록된 경우에만 처리한다.
		if(isType.equals(Constant.INSERT) || 
				version_type.equals(Constant.VERSION_MINOR_VERSION) || version_type.equals(Constant.VERSION_MAJOR_VERSION) )	{
			
			param.put("folder_id",documentVO.getFolder_id());
			param.put("doc_id",documentVO.getDoc_id());
			result = folderDao.writeXrLinked(param);
			if(result == 0)	{	throw processException("common.system.error");	}	
		}
		
		// MYPAGE && 문서수정 VERSION_TYPE : SAME SKIPPED 		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서등록&수정 확장문서유형 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : extendTypeWrite
	 * @param map
	 * @param version_type
	 * @param attrList
	 * @param documentVO
	 * @param typeDao
	 * @throws Exception void
	 */
	@Override
	public void extendTypeWrite(HashMap<String,Object> map,String version_type,List<HashMap<String,Object>> attrList,
			DocumentVO documentVO,TypeDao typeDao) throws Exception {
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		int result = 0;
		
		// 확장문서인지, 기본문서인지 구분할 의미 없음
//		if(map.get("is_extended") != null && map.get("is_extended").toString().equals(Constant.T))	{
			
		// 1. 문서확장속성
		if(attrList != null && attrList.size() > 0 )	{ 
			
			// 2. 문서유형 속성 정보
			param.put("type_id",documentVO.getDoc_type());
			TypeVO typeVO = typeDao.typeDetailInfo(param);
			
			param.clear();
			param.put("tbl_name",typeVO.getTbl_name());
			param.put("doc_id",documentVO.getDoc_id());

			//문서등록 && 문서수정(메이저/마이너) 신규 등록처리
			if(!version_type.equals(Constant.VERSION_SAME_VERSION))	{				
				result = typeDao.insertAttrValue(param);
				if(result == 0)	{	throw processException("common.system.error");	}				
			}
			
			// 문서등록 & 문서수정 공통처리 :: 확장문서 테이블 변경처리
			for(HashMap<String,Object> attrMap : attrList)	{

				Set<Map.Entry<String, Object>> set = attrMap.entrySet();
				Iterator<Map.Entry<String, Object>> it = set.iterator();
				
				// 확장객체수만큼 반복처리 수행한다.
				while(it.hasNext()) {
				
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					param.put("attr_id",entry.getKey());
					param.put("attr_value",entry.getValue());
					
					result = typeDao.updateAttrValue(param);
					if(result == 0)	{	throw processException("common.system.error");	}
					
				}	// END OF WHILE
			}	// END OF FOR
		}	// END OF IF attrList			
//		}	// END OF IF MAP	
	}
	
	//[1000]
	@Override
	public List<HashMap<String, Object>> moveCopyDocValidList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		return moveCopyDocValidList(map,sessionVO,false,null);
	}
	//[1000]
	@Override
	public List<HashMap<String, Object>> moveCopyDocValidList(HashMap<String,Object> map, SessionVO sessionVO, boolean isExternal, String rootFolderID) throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);		
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);	
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();	

		/***********************************************************************************************************
		 * 주) 문서 이동/복사 처리 파라미터정의 JsonArry 형식이며 아래의 필수값 포함해야됨.
		 * delDocList=[
		 * {"doc_id":"DOC000000007536","doc_name":"문서1","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007535","doc_name":"문서2","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"},
		 * {"doc_id":"DOC000000007534","doc_name":"문서3","root_id":"","is_locked":"F","doc_type","XR_DOCUMENT"}]
		 ***********************************************************************************************************/
		String docList =  map.get("docList") != null ? map.get("docList").toString() : "";
		String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
		String type = map.get("type") != null ? map.get("type").toString() : "";
				
		// 1.입력값 유효성 체크 
		if(docList.equals("") ||  targetFolderId.equals(""))	{
			throw processException("common.required.error");
		}
		
		// 2. 이동/복사 할 폴더(targetFolder)의 Quota/Usage정보 조회		
		HashMap<String,Object> folderParam = new HashMap<String,Object>();
		folderParam.put("folder_id", targetFolderId);
		
		FolderVO getFolderInfo = new FolderVO();
		getFolderInfo = folderDao.folderDetail(folderParam);
		// 문서 이동시 개인문서함인지 부서/프로젝트함인지 구별하기 위해 map_id추가	[3007]
		map.put("map_id",getFolderInfo.getMap_id());
		
		// 2.1 폴더 문서저장여부 체크 [2002] Valid 체크 추가
		if(getFolderInfo.getIs_save().equals(Constant.NO))	{
			 throw processException("doc.fail.folder.not.save");				
		}
		
		String targetFolderRootID = "";
		if(getFolderInfo.getMap_id().equals("MYPAGE")) {
			targetFolderRootID = sessionVO.getSessId().toString();
			map.put("root_folder_id",targetFolderRootID);
		} else {
			//[1000]
			if(!isExternal){
			// 같은 루트폴더 아래 이동시 용량계산무시 하려고 만들어진 코드  주석처리	[3007]
			//	FolderVO folderFullPath = folderService.getRootFolder(getFolderInfo.getMap_id(), getFolderInfo.getFolder_id(), getFolderInfo);
			//	targetFolderRootID = folderFullPath.getFolder_id().toString()				
				targetFolderRootID = targetFolderId;				
			}else{
				targetFolderRootID = rootFolderID;
			}
			
		}	
		
		FolderVO rootFolderInfo = new FolderVO();		
		rootFolderInfo.setFolder_id(targetFolderRootID);
		FolderVO getFolder = folderDao.getFolderStorage(rootFolderInfo);
		
		long getRootQuota = getFolder.getStorage_quota();
		long getRootUsage = getFolder.getStorage_usage();
		
		
		// 2.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0 ) {		
			for(int j=0;j < jsonArray.size();j++) {				 
				HashMap<String, Object> docInfo = new HashMap<String, Object>();

				//[3002] 버전복원할 때 문서명  ex)문서명-[버전1.1]
				String getDocName = null;
				if(type.equals(Constant.ACTION_VERSIONBACK)) {
					getDocName =(jsonArray.getJSONObject(j).getString("doc_name").toString()).trim()+"-[버전"+(jsonArray.getJSONObject(j).getString("version_no").toString())+"]";	
				}else{
					getDocName = (jsonArray.getJSONObject(j).getString("doc_name").toString()).trim();		
				}
				
				docInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
				docInfo.put("root_id", jsonArray.getJSONObject(j).getString("root_id").toString());
				docInfo.put("doc_name", getDocName);
				docInfo.put("is_locked", jsonArray.getJSONObject(j).getString("is_locked").toString());
				docInfo.put("doc_type", jsonArray.getJSONObject(j).getString("doc_type").toString());
				docInfo.put("folder_id", jsonArray.getJSONObject(j).getString("folder_id").toString());
				docInfo.put("is_current", Constant.T);
				docInfo.put("doc_status", Constant.C);				
				docInfo.put("is_inherit_acl", jsonArray.getJSONObject(j).getString("is_inherit_acl").toString());

				HashMap<String, Object> searchInfo = new HashMap<String, Object>();				
				searchInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
				searchInfo.put("doc_name", getDocName);
				searchInfo.put("folder_id", targetFolderId);
				searchInfo.put("is_current", Constant.T);
				searchInfo.put("doc_status", Constant.C);		
				
				// 3. 이동/복사 폴더 체크
				if(docInfo.get("folder_id").equals(targetFolderId)){
					throw processException("doc.fail.folder.equal");
				}
				// 4.문서잠금 여부 체크
				if(docInfo.get("is_locked").toString().equals(Constant.T)) {
					if(type.equals(Constant.ACTION_MOVE)) {
						throw processException("doc.fail.move.locked");
					} else if (type.equals(Constant.ACTION_COPY)) {
						throw processException("doc.fail.copy.locked");
					}
				}
				
				// 5.동일 문서명 방지 처리 로직					
				if(documentDao.isExitsDocName(searchInfo) > 0){
					String newName = getUniqueDocumentName(getDocName, targetFolderId, type);
					docInfo.put("doc_name", newName);
				}
				
				// 6.다중분류문서 체크
				List<DocumentVO> multiDoc = new ArrayList<DocumentVO>();
				multiDoc = documentDao.multiDocument(docInfo);
				
				// xr_linked에 하나의 doc_id가 여러 folder_id에 있을 때 : 다중분류문서
				if(multiDoc.size() > 1){
					//이동 시 다중문서가 포함된 폴더로 이동할 경우 Exception
					for(DocumentVO search : multiDoc){
						String multiFldId = search.getFolder_id();
						// 이동할 folder_id가 다중분류 문서가 속해있는 folder_id일 경우 : 이동 금지 Exception
						if(targetFolderId.equals(multiFldId)){
							throw processException("doc.fail.multi.doc");
						}
					}
				}				
				
				// 같은 루트폴더 아래 이동시 용량계산무시 하기위한 처리 코드 주석처리[3007]
				// 버전복원일때는 folder_id를 안넘김으로 분기처리		[3002]		
//				String oriFolderRootID = "";						
//				if(!type.equals(Constant.ACTION_VERSIONBACK)) {
//					HashMap<String,Object> oriFolderParam = new HashMap<String, Object>();
//					oriFolderParam.put("folder_id",jsonArray.getJSONObject(j).getString("folder_id").toString());
//					
//					FolderVO oriFolderInfo =  folderDao.folderDetail(oriFolderParam);
//					
//					if(oriFolderInfo.getMap_id().equals("MYPAGE")) {
//						oriFolderRootID = sessionVO.getSessId().toString();
//					} else {
//						FolderVO oriFullPath = folderService.getRootFolder(oriFolderInfo.getMap_id(), oriFolderInfo.getFolder_id(), oriFolderInfo);
//						oriFolderRootID = oriFullPath.getFolder_id().toString();
//					}
//				}				
				
				// 7. 폴더 Storage Quota와 문서 Size 체크
				// 이동시  문서와Storage 용량 계산 ::  [3003]
				if(type.equals(Constant.ACTION_MOVE)){
					// 같은 루트폴더 아래 이동시 용량계산무시 :: 기존 로직 주석 - 이동시 매번 용량계산을 해주기위함	[3007]
					//if(!oriFolderRootID.equals(targetFolderRootID)) {
					
					//개인문서함일 경우 용량체크를 하지않아도 된다.
					if(!getFolderInfo.getMap_id().equals("MYPAGE")) {
						HashMap<String,Object> pageInfo = new HashMap<String, Object>();
						pageInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
						List<PageVO> getPageList = new ArrayList<PageVO>();			
						getPageList = pageDao.comDocPageList(pageInfo);						
						
						long totalSize = 0;
						for(int i=0; i<getPageList.size(); i++){
							totalSize = totalSize + getPageList.get(i).getPage_size();
						}						
						if(getRootQuota > -1) {
							if((getRootQuota - getRootUsage) < totalSize) {
								throw processException("storage.quota.overflow");
							}
						}	
					}
					//}	
					
					//복사시 문서와Storage 용량 계산 ::  [3003]
					//버전복원의기능은 복사와 같기 때문에 or조건추가,  [3002] 
				} else if (type.equals(Constant.ACTION_COPY ) || type.equals(Constant.ACTION_VERSIONBACK )) {
					HashMap<String,Object> pageInfo = new HashMap<String, Object>();
					pageInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
					List<PageVO> getPageList = new ArrayList<PageVO>();			
					getPageList = pageDao.comDocPageList(pageInfo);
										
					long totalSize = 0; //복사될 문서의 용량
					for(int i=0; i<getPageList.size(); i++){
						totalSize = totalSize + getPageList.get(i).getPage_size();
					}					
					if(getRootQuota > -1) {
						if((getRootQuota - getRootUsage) < totalSize) {
							throw processException("storage.quota.overflow");
						}
					}				
				}
				
				ret.add(docInfo);
			}
		}
		return ret;
	}
	
	@Override
	public List<HashMap<String, Object>> tempDocValidList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		String docList = map.get("docList") != null ? map.get("docList").toString() : "";
		String type = map.get("is_type") != null ? map.get("is_type").toString() : "";
		
		// 1.입력값 유효성 체크
		if(docList.equals("")){
			throw processException("common.required.error");
		}
		
		// 2.JsonArray To List
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0 ) {				

			map.put("user_id", sessionVO.getSessId().toString());
			
			if (type.equals(Constant.INSERT)){
				// 3.임시작업함 문서 개수 체크
				int serverCnt = documentDao.tempDocumentCount(map);
				int insertCnt = jsonArray.size();
				
				if((insertCnt + serverCnt) > 30){
					throw processException("doc.fail.tempwork.doc.max");
				}
			}			
			
			for(int j=0; j < jsonArray.size(); j++)	{				 
				HashMap<String, Object> docInfo = new HashMap<String, Object>();
				docInfo.put("doc_id",jsonArray.getJSONObject(j).getString("doc_id").toString());
				docInfo.put("root_id",jsonArray.getJSONObject(j).getString("root_id").toString());
				docInfo.put("is_locked",jsonArray.getJSONObject(j).getString("is_locked").toString());
				docInfo.put("user_id", sessionVO.getSessId().toString());
				
				if (type.equals(Constant.INSERT)){
					// 4.문서잠금 여부 체크
					if(docInfo.get("is_locked").toString().equals(Constant.T)) {					
						throw processException("doc.fail.tempwork.locked");
					}
				}
				
				ret.add(docInfo);
			}
		}
		return ret;
	}
	
	@Override
	public Map<String, Object> tempDocInsert(List<HashMap<String,Object>> docList, HashMap<String,Object>map, SessionVO sessionVO) throws Exception{

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 1.임시문서함 추가 대상 문서 목록 리스트 구하기
		for(HashMap<String,Object> iMap : docList){
			
			// 2.root_id가 null일경우 doc_id로 대체
			String root_id = iMap.get("root_id").toString().equals("") ? iMap.get("doc_id").toString() : iMap.get("root_id").toString();
			
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("is_type", "DOC_CHECK");
			param.put("root_id", root_id);
			param.put("user_id", sessionVO.getSessId());
			
			// 3.XR_TEMP_DOC에 이미 같은 문서가 존재할 경우 DELETE -> INSERT
			if(documentDao.tempDocumentCount(param) > 0){
				documentDao.tempDocDelete(param);
			} 
			
			documentDao.tempDocInsert(param);
			
			resultMap.put("result",Constant.RESULT_TRUE);
		}
		return resultMap;
	}
	
	@Override
	public List<HashMap<String,Object>> authWasteDocValidList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception{
		List<DocumentVO> list = new ArrayList<DocumentVO>();
		List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		
		map.put("deleter_id", sessionVO.getSessId());
		map.put("doc_status", Constant.D);
		
		list = documentDao.authWasteAllDocList(map);
		
		for(DocumentVO vo : list){
			HashMap<String, Object> docInfo = new HashMap<String,Object>();
			
			docInfo.put("doc_id", vo.getDoc_id());
			docInfo.put("root_id",vo.getRoot_id());
			docInfo.put("is_locked",vo.getIs_locked());
			docInfo.put("type_id",vo.getDoc_type());
			
			// 2. 문서잠금 여부 체크
			 if(docInfo.get("is_locked").toString().equals(Constant.T)) {					
				 throw processException("doc.fail.terminate.locked");
			 }
			 
			 // 3.문서유형정보가 삭제불가인지 체크한다. :: parameter : type_id
			 TypeVO typeVO = typeDao.typeDetailInfo(docInfo);
			 if(typeVO.getIs_modify().equals(Constant.F))	{
				 String[] args = new String[]{typeVO.getType_name()};
				 throw processException("doc.fail.terminate.type.delete",args);					 					 
			 }
			 
			ret.add(docInfo);
		}
		return ret;
	}
		
	@Override
	public List<AttrVO> extendedAttrListByDocType(HttpServletRequest request,String doc_type) throws Exception {
		
		List<AttrVO>  attrList = new ArrayList<AttrVO>();
		
		TypeDao typeDao = sqlSession.getMapper(TypeDao.class);
		
		HashMap<String, Object> param = new HashMap<String, Object>();			
		
		String search_word = "";
		
		// 1.문서유형 속성ITEM 가져온다.
		param.put("type_id",doc_type);
		param.put("is_extended",Constant.T);
		param.put("is_search", Constant.T);
		attrList =  typeDao.attrList(param);
		
		// 2.속성ITEM에 해당하는 입력값을 가져온다.
		if(attrList != null && attrList.size() > 0 ) {
			
			
			for(int k=0; k<attrList.size(); k++) {
				
				AttrVO attrVO = attrList.get(k);
				// CHECK 인 경우 다중선택이 가능하여 분리하여 입력된 값을 받아서 처리한다.
				if(attrVO.getDisplay_type().equals(Constant.DISPLAY_TYPE_CHECK)) {
					
					// CHECKBOX 가 하나도 선택되지 않은 경우 예외처리
					String[] values = request.getParameterValues(attrVO.getAttr_id());
					StringBuffer buf = new StringBuffer();
					if(values == null)	{
						search_word = "";
					}else {						
						for(int i = 0; i < values.length; i++){
							buf.append(values[i]);
							buf.append("%");
						}
						
						search_word = buf.toString();
						if(search_word.lastIndexOf("%") != -1 )	{
							search_word = search_word.substring(0,search_word.length()-1);
						}
					}
					
					// ATTR_ID 값 설정(DISPLAY_TYPE=CHECK)
					attrVO.setSearch_word(search_word);
					attrList.set(k, attrVO);
					buf.setLength(0);
					
				} else if(attrVO.getDisplay_type().equals(Constant.DISPLAY_TYPE_SELECT)) {
					//DISPLAY_TYPE=SELECT
					search_word = request.getParameter(attrVO.getAttr_id());
					if(!StringUtil.isEmpty(search_word) && search_word.equals(Constant.DOC_TABLE_ALL_TYPE))
						search_word = "";
						
				}else {
					// ATTR_ID 값 설정(DISPLAY_TYPE=INPUT/RADIO)
					search_word = request.getParameter(attrVO.getAttr_id());
					attrVO.setSearch_word(search_word);
					attrList.set(k, attrVO);				
				}

			}
		}
		return attrList;
	}
	
	@Override
	public Map<String, Object> urlLinkInfo(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ConfDao confDao = sqlSession.getMapper(ConfDao.class);
		CaseInsensitiveMap urlConfig = new CaseInsensitiveMap();

		// 1.URL 유효기간 설정
		urlConfig = confDao.urlConfigDetail();
		
		// 기본 데이터 스크립트 누락 대비
		if(urlConfig != null)	{
			resultMap.put("expired",urlConfig.get("val").toString());
		}else {
			resultMap.put("expired",0);
		}
							
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}
	
	@Override
	public Map<String,Object> tempDocDelete(List<HashMap<String,Object>> docList, HashMap<String,Object>map, SessionVO sessionVO) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 1.임시문서함 삭제 대상 문서 목록 리스트 구하기
		for(HashMap<String,Object> iMap : docList){
			
			// 2.root_id가 null일경우 doc_id로 대체
			String root_id = iMap.get("root_id").toString().equals("") ? iMap.get("doc_id").toString() : iMap.get("root_id").toString();
			
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("root_id", root_id);
			param.put("user_id", sessionVO.getSessId());
			
			// 3.XR_TEMP_DOC 문서 DELETE 				
			documentDao.tempDocDelete(param);		
			resultMap.put("result",Constant.RESULT_TRUE);
		}
		return resultMap;
	}
	
	@Override
	public Map<String,Object> tempRefDocIsUsing(HashMap<String,Object> map) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 1. 관련문서에 메인문서가 있는지 확인한다.
		String isType = map.get("is_type") != null ? map.get("is_type").toString() : "";
		String ref_doc_id = map.get("ref_doc_id") != null ? map.get("ref_doc_id").toString() : "";
		String ref_root_id = (map.get("ref_root_id") != null && !map.get("ref_root_id").equals("")) ? map.get("ref_root_id").toString() : ref_doc_id;
		
		if(isType.equals("CHECK")) {
			HashMap<String, Object> docInfo = new HashMap<String,Object>();
			docInfo.put("root_doc_id", ref_root_id);

			if(documentDao.isUsingRefDocId(docInfo) > 0 ) {
				resultMap.put("isUsing", Constant.RESULT_TRUE);
			} else {
				resultMap.put("isUsing", Constant.RESULT_FALSE);
			}
		}
		return resultMap;
	}

	@Override
	public List<HashMap<String,Object>> tempRefValidList(HashMap<String,Object>map) throws Exception {
		List<HashMap<String,Object>> ret = new ArrayList<HashMap<String,Object>>();
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		String subDocList = map.get("subDocList") != null ? map.get("subDocList").toString() : "";
		String ref_doc_id = map.get("ref_doc_id") != null ? map.get("ref_doc_id").toString() : "";
		String ref_root_id = (map.get("ref_root_id") != null && !map.get("ref_root_id").equals("")) ? map.get("ref_root_id").toString() : ref_doc_id;
		
		if(subDocList.equals("") || ref_root_id.equals("") || ref_root_id.length() == 0){
			throw processException("common.required.error");
		}
		
		JSONArray jsonArray = JSONArray.fromObject(subDocList);
		if(jsonArray.size() > 0){
			for(int j=0; j<jsonArray.size(); j++){

				HashMap<String, Object> docInfo = new HashMap<String, Object>();
				// 1. XR_REF_DOC : root_ref_id를 구한다
				String subDocId = jsonArray.getJSONObject(j).getString("doc_id") != null ? jsonArray.getJSONObject(j).getString("doc_id").toString() : "";
				String subRootId = (jsonArray.getJSONObject(j).getString("root_id") != null && !jsonArray.getJSONObject(j).getString("root_id").equals(""))? jsonArray.getJSONObject(j).getString("root_id").toString() : subDocId;

				docInfo.put("select_action", "INSERT");
				docInfo.put("root_doc_id", ref_root_id);
				docInfo.put("root_ref_id", subRootId);

				if(documentDao.isUsingRefDocId(docInfo) > 0){
					// 이미 등록되어있는 경우 등록 리스트에서 제외한다.
					continue;
				} 				
				ret.add(docInfo);				
			}
		}
		return ret;
	}
	
	@Override
	public Map<String,Object> tempRefInsert(List<HashMap<String, Object>> docList, HashMap<String,Object> map) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 임시작업함 > 관련문서 등록
		for(HashMap<String,Object> iMap : docList) {
			documentDao.writeRefDoc(iMap);
		}
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
		
	}
	
	@Override
	public HashMap<String,Object> getPageListByTempDocList(HashMap<String,Object>map) throws Exception {
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
		
		String docList = map.get("docList") != null ? map.get("docList").toString() : "";
		
		if(docList.equals("")){
			throw processException("common.required.error");
		}
		
		JSONArray jsonArray = JSONArray.fromObject(docList);
		List<HashMap<String,Object>> doc_list = new ArrayList<HashMap<String,Object>>();
		
		// 1. 문서 리스트 구성
		if(jsonArray.size() > 0){
			for(int i = 0; i < jsonArray.size(); i++){
				HashMap<String,Object> docInfo = new HashMap<String,Object>();
				docInfo.put("doc_id", jsonArray.getJSONObject(i).getString("doc_id").toString());
				doc_list.add(docInfo);
			}
		}
		
		List<CaseInsensitiveMap> getPageList = new ArrayList<CaseInsensitiveMap>();
		List<CaseInsensitiveMap> allPage = new ArrayList<CaseInsensitiveMap>();
		
		// 2. 문서의 첨부파일 목록 구성
		if(doc_list.size() > 0){
			for(HashMap<String,Object> pMap : doc_list){
				getPageList = pageDao.xrPageList(pMap);
				allPage.addAll(getPageList);
			}
		}
		
		// 3. 페이지 리스트 구성
		/***********************************************************************************************************
		 * pageList=[{"page_id":"PAG000000007536"},{"page_id":"PAG000000007535"}] 
		 ***********************************************************************************************************/
		JSONArray setPageList = new JSONArray();
		JSONArray setPageInfo = JSONArray.fromObject(allPage);
		if(setPageInfo.size() > 0){
			for(int j = 0; j < setPageInfo.size(); j++) {
				JSONObject obj = new JSONObject();
				obj.put("page_id", setPageInfo.getJSONObject(j).getString("page_id").toString());
				setPageList.add(obj);				
			}
		}		
		resultMap.put("pageList", setPageList);
		
		return resultMap;			
	}
	
	@Override
	public Map<String, Object> documentCommentList(HashMap<String, Object> map) throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<CommentVO> ret  = new ArrayList<CommentVO>();
		int total = 0;
		
		total = documentDao.docCommentPagingCount(String.valueOf(map.get("root_id")));
		ret = documentDao.docCommentList(map);
		
		//resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		//resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}


	@Override
	public int docCommentAction(HashMap<String, Object> map, SessionVO sessionVO)
			throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		int ret = 0;
		String kbn = String.valueOf(map.get("kbn"));
		
		if(kbn.equalsIgnoreCase(Constant.UPDATE)){
			ret = documentDao.docCommentUpdate(map);
		}else if(kbn.equalsIgnoreCase(Constant.REPLY)){
			List<CommentVO> commVoList  = new ArrayList<CommentVO>();
			commVoList = documentDao.docCommentList(map);
			
			CommentVO commVo  = new CommentVO();
			for(CommentVO vo : commVoList)	{
				if(vo.getCom_id().equals(map.get("com_id"))){
					commVo = vo;
				}
			}
			
			int commentid_seq = commonService.commonNextVal(Constant.COUNTER_ID_COMMENT);
			String newCommentID = CommonUtil.getStringID(Constant.ID_PREFIX_COMMENT, commentid_seq);		
			
			HashMap<String,Object> param = new  HashMap<String, Object>();
			//param.put("com_id", newCommentID);
			commVo.setCom_id(newCommentID);
			param.put("doc_root_id", commVo.getDoc_root_id());
			param.put("com_step", commVo.getCom_step());
			//의견에 대한 답글(추가의견)의 최신값 얻기
			int maxOrder = documentDao.checkMaxOrder(param);

			commVo.setCom_order(String.valueOf(maxOrder + 1));
			commVo.setCreator_id(sessionVO.getSessId());
			commVo.setCreator_name(sessionVO.getSessName());
			commVo.setContent(map.get("content").toString());
			
			ret = documentDao.docCommentWrite(commVo);
			
			
		}else if(kbn.equalsIgnoreCase(Constant.DELETE)){
			HashMap<String,Object> param = new  HashMap<String, Object>();
			param.put("com_id",  map.get("com_id"));
			param.put("doc_root_id",  map.get("root_id"));
			param.put("creator_id", sessionVO.getSessId());
			param.put("com_step", map.get("com_step"));
			param.put("content", "삭제된 의견입니다.");
			
			//의견에 대한 답글(추가의견)의 최신값 얻기
			int maxOrder = documentDao.checkMaxOrder(param);
			
			if(maxOrder > 0) {
				// 삭제할 의견이 원글인지 답글인지 얻기
				int comment_type = documentDao.selectIsMainComment(param);
				
				if(comment_type > 0) {
					// 답글일때 삭제처리
					ret = documentDao.deleteCommentRow(param);
					
					// 원글이 삭제상태인지 여부얻기
					int deleteStatus = documentDao.checkCommentStatus(param);
					
					if(deleteStatus > 0) {
						maxOrder = documentDao.checkMaxOrder(param);
						if(maxOrder == 0) {
							// 원글이 삭제상태이고 답글이 없으면 삭제처리
							param.put("com_id",  "");
							ret = documentDao.deleteCommentRow(param);
						}
					}
				} else {
					// 답글이 있는 원글일 경우 "삭제된 의견입니다" 라는 comment를 달아준다.
					ret = documentDao.docCommentDelete(param);
				}
				
			} else {
				// 원글만 있을때 삭제처리
				ret = documentDao.deleteCommentRow(param);
			}
			
		}else{//신규 등록
			Map<String, Object> resultMap = new HashMap<String, Object>();
			CommentVO commVo  = new CommentVO();
			
			int commentid_seq = commonService.commonNextVal(Constant.COUNTER_ID_COMMENT);
			String newCommentID = CommonUtil.getStringID(Constant.ID_PREFIX_COMMENT, commentid_seq);		
			
			HashMap<String,Object> param = new  HashMap<String, Object>();
			//param.put("com_id", newCommentID);
			param.put("doc_root_id", map.get("root_id"));

			commVo.setCom_id(newCommentID);
			commVo.setDoc_root_id( map.get("root_id").toString());
			//의견에 대한 답글(추가의견)의 최신값 얻기
			int maxStep = documentDao.checkMaxStep(param); 
			commVo.setCom_step(String.valueOf(maxStep+1));
			commVo.setCreator_id(sessionVO.getSessId());
			commVo.setCreator_name(sessionVO.getSessName());
			commVo.setParent_creator_name(sessionVO.getSessName());
			commVo.setContent(map.get("content").toString());
			
			int result = 0;
			result = documentDao.docCommentWrite(commVo);
			
			return result;
		}
		
		
		return ret;
	}
	
	@Override
	public Map<String, Object> mainDocumentBasicList(HashMap<String, Object> map) throws Exception {
		
		WorkDocumentDao workDocumentDao = sqlSession.getMapper(WorkDocumentDao.class);
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		docList =  workDocumentDao.mainDocumentList(map);
		setDocumentAcl( docList, map);		// 문서권한 설정	
		
		resultMap.put("records",docList.size());
		resultMap.put("list",docList);
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}


	/**
	 * 
	 * <pre>
	 * 1. 개용 : 
	 * 2. 처리내용 : 문서 조회수 update
	 * </pre>
	 * @Method Name : docReadCountUpdate
	 * @param map
	 * @return List<NoteVO>
	 */
	public int docReadCountUpdate(HashMap<String, Object> map) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		int result = 0;
	
		result = documentDao.docReadCountUpdate(map);
		if(result == 0)	{	throw processException("common.system.error");	}
		
		return result;
	}
	
	@Override
	public List<RecentlyObjectVO> recentlyDocumentList(HashMap<String, Object> map ) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		List<RecentlyObjectVO> list = documentDao.recentlyDocumentList(map);
		return list;
	}
	

	@Override
	public Map<String, Object> recentlyDocumentDelete(String idx) throws Exception {
		CommonDao commonDao = sqlSession.getMapper(CommonDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		int ret = 0;
		
		param.put("recently_id", idx);
		
		ret = commonDao.deleteRecently(param);
		
		if(ret == 1) {
			resultMap.put("result",Constant.RESULT_TRUE);
		} else {
			resultMap.put("result",Constant.RESULT_FALSE);
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> selectCurrentDocID(HashMap<String, Object> map) throws Exception {
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String current_doc_id = documentDao.selectCurrentDocID(map);
		
		if(current_doc_id != null) {
			resultMap.put("current_doc_id",current_doc_id);
		} else {
			resultMap.put("current_doc_id",map.get("doc_root_id"));
		}
		
		resultMap.put("result",Constant.RESULT_TRUE);
		
		return resultMap;
	}

	@Override
	public void processFileUpdate(DocumentVO documentVo) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		// process에서 호출되는 것은 무조건 동일 버전
		// process 완료 후는 문서에서 진행된다.
		List<PageVO> pageList = new ArrayList<PageVO>();				// 신규추가 파일 리스트
		
		// 1. 삭제 대상이 있는지?
		if(documentVo.getDelFileList() != null && documentVo.getDelFileList().size() > 0) {
			List<String> delPageList = new ArrayList<String>();				// 이전버전 삭제대상 파일 리스트
			for(HashMap<String,Object> pageInfo : documentVo.getDelFileList()) {		
				delPageList.add(pageInfo.get("page_id").toString());
			}
			
			pageInfoUpdate(delPageList, documentVo, pageDao);			// XR_PAGE IS_DELETED=T UPDATE / XR_FILED DELETE
		}
		
		// 2. 추가 대상이 있는지?
		if(documentVo.getInsertFileList() != null && documentVo.getInsertFileList().size() > 0) {
			List<PageVO> insertPageList =  insertNewPage(documentVo);
			docPageInsert(insertPageList, documentVo, pageDao);
		}
		
		// 3. 갱신 된 파일 정보로 xr_document page_cnt, page_total 수정
		// 변경된 첨부파일 목록을 가져온다.
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("doc_id",documentVo.getDoc_id());
		pageList = pageDao.comDocPageList(param);
							
		if(DocPageInfoUpdate(pageList, documentVo, documentDao) == 0)	{
			// 결국엔 doc_id가 null이거나 DB에 저장된 값이 없는 경우
			throw processException("common.system.error");	
		}
		
	}

	@Override
	public Map<String, Object> registDocReadRequest(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> reqInfo = new HashMap<String, Object>();

		String reqList = map.get("reqList") != null ? map.get("reqList").toString() : "";
		
		int result = 0;
		
		JSONArray jsonArray = JSONArray.fromObject(reqList);
		
		if(jsonArray.size() > 0 ) {		
		 for(int j=0;j < jsonArray.size();j++)	{
			 reqInfo.put("doc_id", jsonArray.getJSONObject(j).getString("doc_id").toString());
			 reqInfo.put("req_period", jsonArray.getJSONObject(j).getString("req_period").toString());
			 reqInfo.put("req_comment", jsonArray.getJSONObject(j).getString("req_comment").toString());
		 }
		 
		 reqInfo.put("table_nm", Constant.DOC_TABLE);
		 // 1. 문서상세
		 DocumentVO docVO = documentDao.commonDocDetail(reqInfo);
		 
		 // 2. XR_READREQUEST 등록처리
		 int newID = commonService.commonNextVal(Constant.COUNTER_ID_READREQUSET);
		 String newReqID = CommonUtil.getStringID(Constant.ID_PREFIX_READREQUEST, newID);	
		 
		 reqInfo.put("req_id", newReqID);
		 reqInfo.put("req_userid", sessionVO.getSessId());
		 reqInfo.put("req_username", sessionVO.getSessName());
		 
		 String folder_type = null;
		 String map_id = null;
		 FolderVO folVO = null;
		 reqInfo.put("folder_id", docVO.getFolder_id());
		 // 문서가 있는 폴더의 상위를 찾아가면서 부서폴더인지 확인한다.
		 while(true) {
			 
			 folVO = folderDao.folderDetail(reqInfo);
			 
			 map_id = folVO.getMap_id();
			 if(map_id.equals(Constant.MAP_ID_PROJECT)) break;
			 
			 folder_type = folVO.getFolder_type();
			 if(folder_type.equals(Constant.FOLDER_TYPE_DEPT)) {
				 break;
			 }
			 reqInfo.put("folder_id", folVO.getParent_id());
		 }
		 
		 // map_id가 프로젝트이면 전체문서관리자인 사용자에게 요청을 한다.
		 if(map_id.equals(Constant.MAP_ID_PROJECT)) {
			 reqInfo.put("group_id", Constant.MAP_ID_PROJECT);
		 } else {
			 reqInfo.put("group_id", CommonUtil.getChangedResourceIDByPrefix(folVO.getFolder_id(), Constant.ID_PREFIX_GROUP));
		 }
		 result = documentDao.registDocReadRequest(reqInfo);
		 
		 if(result == 0)	{	throw processException("common.system.error");	}
		 
		 // 3. XR_DOCUMENT_HT 등록처리 ::  ACTION : READREQUEST
		 long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);		
		 result = docHistoryWrite(historyDao, doc_seq,docVO.getRoot_id(), docVO.getDoc_id(), "READREQUEST", docVO.getDoc_type(), 
				docVO.getDoc_name(), docVO.getVersion_no(), sessionVO);
		 if(result == 0)	{	throw processException("common.system.error");	}
		 
		 } else {
			 throw processException("common.system.error");	
		 }
		
		 resultMap.put("result",Constant.RESULT_TRUE);
		 return resultMap;
	}

	@Override
	public Map<String, Object> docReadRequestList(HashMap<String, Object> map) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<DocumentVO> docList = new ArrayList<DocumentVO>();
		List<GroupVO> groupList = null;
		List<String> groupIdList = new ArrayList<String>();
		int total = 0;
		
		MyDocumentDao myDocumentDao = sqlSession.getMapper(MyDocumentDao.class);
		GroupDao groupDao = sqlSession.getMapper(GroupDao.class);
		
		String role_id = map.get("role_id").toString();
		if(!role_id.equals(Constant.USER_ROLE_SUPER)) {
			// 본부관리자 role일 경우 하위부서의 열람요청 내역까지 가져온다.
			if(role_id.equals(Constant.USER_ROLE_HEAD)) {
				groupList = groupDao.childGroupList(map);
				for(GroupVO vo : groupList) {
					groupIdList.add(vo.getGroup_id());
				}
			}
			
			// 부서관리자 role일 경우는 자기부서 id에 대해서 열람요청 내역을 가져온다.
			groupIdList.add(map.get("parentId").toString()); // 현재 사용자가 속한 그룹id를 리스트에 넣는다.
			map.put("groupList", groupIdList);
		}
		
		total = myDocumentDao.docReadRequestListCnt(map);
		
		if("show_date".equals(map.get("orderCol").toString())) {
			map.put("orderCol", "CREATE_DATE");
		}
		
		// 1. 문서 목록을 가져 온다.
		docList = myDocumentDao.docReadRequestList(map);
		
		for (int i = 0; i < docList.size(); i++) {
			docList.get(i).setShow_date(docList.get(i).getCreate_date());
			docList.set(i, docList.get(i));
		}
		
		// 2. 문서에 대한 권한을 셋팅 한다. 엑셀저장은 매핑 안함
		String oper = map.get("oper") != null ? map.get("oper").toString() : "";
		if(!oper.equals(Constant.EXCEL_FORMAT)) {
			setDocumentAcl( docList, map);
		}

		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",docList);
		
		// 3. Ajax Paging 
		String strLink = "javascript:exsoftMypageFunc.event.docFunctions.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);	
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> docReadApproveControl(HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
		NoteDao noteDao = sqlSession.getMapper(NoteDao.class);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> reqInfo = new HashMap<String, Object>();
		int result = 0;
		
		String req_id = null;
		String req_userid = null;
		String doc_access = null;
		String req_period = null;
		String doc_id = null;
		String comment = null;
		String creator_name = null;
		String str_note_from = "";
		
		// JsonArray to List
		String docList =  map.get("docList") != null ? map.get("docList").toString() : "";
		JSONArray jsonArray = JSONArray.fromObject(docList);
		if(jsonArray.size() > 0 ) {
			for(int j=0;j < jsonArray.size();j++) {
				
				req_id = jsonArray.getJSONObject(j).getString("req_id").toString();
				req_userid = jsonArray.getJSONObject(j).getString("req_userid").toString();
				doc_access = jsonArray.getJSONObject(j).getString("doc_access").toString();
				req_period = jsonArray.getJSONObject(j).getString("req_period").toString();
				doc_id = jsonArray.getJSONObject(j).getString("doc_id").toString();
				comment = jsonArray.getJSONObject(j).getString("comment").toString();
				creator_name = jsonArray.getJSONObject(j).getString("creator_name").toString();
				
				String history_action_id = "";
				
				// 1. XR_READREQUEST 정보갱신
				reqInfo.put("doc_access", doc_access);
				reqInfo.put("req_id", req_id);
				reqInfo.put("confirm_userid", sessionVO.getSessId());
				reqInfo.put("req_period", Integer.parseInt(req_period));
				
				result = documentDao.updateDocReadRequest(reqInfo);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// 2. 승인/반려로 분기하여 처리
				// 승인일때
				if("S".equals(doc_access)) {
					
					// 추가접근자에 요청자를 추가한다.
					// 추가접근자 목록얻기
					reqInfo.clear();
					reqInfo.put("doc_id", doc_id);
					List<AclExItemVO> exAclItemList = aclService.exAclItemList(reqInfo);
					
					// 추가접근자로 이미 등록된 내용이 있다면 삭제하고 다시 등록.
					reqInfo.put("accessor_id", req_userid);
					for(AclExItemVO aclExItemVo : exAclItemList) {
						if(req_userid.equals(aclExItemVo.getAccessor_id())) {
							aclService.aclExItemDelete(reqInfo, doc_id);
						}
					}
					
					AclExItemVO newAclExitenVo = new AclExItemVO();
					newAclExitenVo.setDoc_id(doc_id);
					newAclExitenVo.setAccessor_id(req_userid);
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
					
					// XR_DOCUMENT_HT 등록처리 ::  ACTION : READAPPROVE
					history_action_id = "READAPPROVE";
					
				// 반려일때
				} else {
					// XR_DOCUMENT_HT 등록처리 ::  ACTION : READREJECT
					history_action_id = "READREJECT";
				}
				
				// 3. XR_DOCUMENT_HT 등록처리
				// 3-1. 문서상세
				reqInfo.clear();
				reqInfo.put("doc_id", doc_id);
				reqInfo.put("table_nm", Constant.DOC_TABLE);
				DocumentVO docVO = documentDao.commonDocDetail(reqInfo);
				
				// 3-2. 히스토리등록
				long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);		
				result = docHistoryWrite(historyDao, doc_seq,docVO.getRoot_id(), docVO.getDoc_id(), history_action_id, docVO.getDoc_type(), 
						docVO.getDoc_name(), docVO.getVersion_no(), sessionVO);
				if(result == 0)	{	throw processException("common.system.error");	}
				
				// 4. 쪽지처리
				// 4-1. 요청자 (쪽지 받는사람)
				str_note_from = creator_name + ";";
				
				// 4-2. XR_NOTE, XR_NOTEMANAGE, XR_NOTE_QUEUE :: INSERT
				List <HashMap<String, Object>> noteList = new ArrayList<HashMap<String,Object>>();
			
				reqInfo.clear();
				reqInfo.put("accessor_id", req_userid);					
				reqInfo.put("content", comment);
				reqInfo.put("note_from",  str_note_from);	
				
				noteList.clear();
				noteList.add(reqInfo);
				
				noteservice.noteListForInserting(noteList, reqInfo, sessionVO);			
				
			} // END OF jsonArray FOR
				
			resultMap.put("result",Constant.RESULT_TRUE);
		} // END OF IF
		
		return resultMap;
	}
	
	 //[3000] 
		@Override
		public boolean expiredComeAlarmList() throws Exception {		
			DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);						
			List<DocumentVO> expiredComeAlarmList = null;
			HashMap<String,Object> param = new  HashMap<String, Object>();		
			
			//만기문서 사전알람 배치에서 message처리를 위한 true,false
			boolean isexpiredCom = false;
			
			//1. 사전알림 대상자 리스트목록			
			//1-1 사전알람일 가져오기				
			ConfDao confDao = sqlSession.getMapper(ConfDao.class);
			List<ConfVO> confList =  new ArrayList<ConfVO>();
			param.put("skey", Constant.SYSTEM_EXPIRECOME_DAY);
			confList = confDao.sysConfigDetail(param);
			param.put("expiredcomeday",confList.get(0).getSval());
			
			//1-2 사전알람일 대상자 리스트 가져오기
			expiredComeAlarmList = documentDao.expiredComeAlarmList(param);			
			String noteFromID = null;	
			
			//2. 만기문서  사전알림 대상자에게 알람쪽지 보내기
			if(expiredComeAlarmList != null && expiredComeAlarmList.size() >0){						
						
				for(DocumentVO docVO : expiredComeAlarmList) {
					//2-1 쪽지를 받을 사전알람일 대상자 ID
					noteFromID = docVO.getCreator_id();
				
					//2-2 noteListForInserting 를  호출하여 XR_NOTE, XR_NOTE_QUER 테이블에 등록
					HashMap<String, Object> reqInfo = new HashMap<String, Object>();
					List <HashMap<String, Object>> noteList = new ArrayList<HashMap<String,Object>>();
					SessionVO sessionVO = new SessionVO();
				
					sessionVO.setSessId("edmsadmin");				
					
					reqInfo.clear();
					reqInfo.put("accessor_id", noteFromID);					
					reqInfo.put("content", "만기예정인문서가 존재합니다.\n 나의문서>내만기문서>만기사전문서 에서 확인해주세요 ");
					reqInfo.put("note_from",  noteFromID);	
					
					noteList.clear();
					noteList.add(reqInfo);
					
					noteservice.noteListForInserting(noteList, reqInfo, sessionVO);					
					
					isexpiredCom = true;
				}
			}					
			return isexpiredCom;
		}	
				
		//[3001]
		@Override
		public boolean expireDocAlarmList() throws Exception {
			DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);		
			List<DocumentVO> expiredDocAlarmList = null;
			HashMap<String,Object> param = new  HashMap<String, Object>();	
			
			//만기문서 자동알람 배치에서 message처리를 위한 true,false
			boolean isexpiredDoc = false;
			
			//1. 만기문서알림 대상자 리스트목록	
			//1-2 만기문서 대상자 리스트 가져오기
			expiredDocAlarmList = documentDao.expiredDocAlarmList(param);
			 String noteFromID = null;
			 
			//2. 만기문서 알림 대상자에게 알람쪽지 보내기
			if(expiredDocAlarmList != null && expiredDocAlarmList.size() >0){	
				
				for(DocumentVO docVO : expiredDocAlarmList) {
					//2-1 쪽지를 받을 만기문서알람 대상자 ID
					noteFromID = docVO.getCreator_id();
				
					//2-2 noteListForInserting 를  호출하여 XR_NOTE_QUER 테이블에 등록
					HashMap<String, Object> reqInfo = new HashMap<String, Object>();
					List <HashMap<String, Object>> noteList = new ArrayList<HashMap<String,Object>>();
					SessionVO sessionVO = new SessionVO();
					
					sessionVO.setSessId("edmsadmin");		
					
					reqInfo.clear();
					reqInfo.put("accessor_id", noteFromID);						
					reqInfo.put("content", "만기된문서가 존재합니다 확인해주세요.\n 나의문서>내만기문서>만기만료문서 에서 확인해주세요 ");
					reqInfo.put("note_from",  noteFromID);	
					
					noteList.clear();
					noteList.add(reqInfo);
					
					noteservice.noteListForInserting(noteList, reqInfo, sessionVO);
					isexpiredDoc = true;
				}
			}		 	
			return isexpiredDoc;
		}

		@Override
		public Map<String, Object> selectDocReadRequest(HashMap<String, Object> map) throws Exception {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
			
			DocumentVO docVO = documentDao.selectDocReadRequest(map);
			if(docVO != null) {
				resultMap.put("result",Constant.RESULT_TRUE);
				resultMap.put("reqInfo",docVO);
			} else {
				throw processException("common.system.error");
			}
			
			return resultMap;
		}
		
		@Override
		// [2000]
		public Map<String, Object> restoreDocumentForAdmin(List<HashMap<String, Object>> docList, HashMap<String, Object> map, SessionVO sessionVO) throws Exception {
			DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
			HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
			FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
			
			List<DocumentVO> allVersionList = new ArrayList<DocumentVO>();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			HashMap<String, Object> docInfo = new HashMap<String, Object>();

			String targetFolderId = map.get("targetFolderId") != null ? map.get("targetFolderId").toString() : "";
			
			// 개인문서함 / 업무문서함 구분
			String type = map.get("type") != null ? map.get("type").toString() : "";
			
			// 업무문서함일때
			if(type.equals(Constant.MAP_ID_DEPT)) {
				// 휴지통 문서 복원 시 만기문서가 있을 경우 설정된 보존기간
				int getExpiredYear = ConfigData.getInt("DELETE_EXPIRED_DEFAULT_YEAR");
				
				// 0. 이동할 Folder의 ACL_ID 얻기		
				FolderVO folderVO = new FolderVO();
				folderVO.setFolder_id(targetFolderId);
				FolderVO getFldInfo = folderDao.getFolderAcl(folderVO);
				String folderAcl = getFldInfo.getAcl_id();
				for(HashMap<String,Object> dMap : docList) {
					
					/*****************************************************************************************
					// 주) 휴지통 문서 복원 
					 * XR_DOCUMENT : DOC_STUATS D => C 로 변경처리
					 * XR_LINKED 기존 데이터 삭제처리
					 * XR_LINKED 새로운 데이터 추가처리
					*****************************************************************************************/
					// 1.모든 버전 문서 목록 리스트 가져오기
					String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
					String doc_id = dMap.get("doc_id").toString();
					allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_DEL_TABLE);
									
					for(DocumentVO vo : allVersionList)	{
					
						// 1.기존 DOCUMENT 정보 얻기
						docInfo.put("table_nm", Constant.DOC_DEL_TABLE);
						docInfo.put("doc_id", vo.getDoc_id());
						//DocumentVO oriDoc = documentDao.commonDocDetail(docInfo);
						DocumentVO oriDoc = documentDao.restoreDocDetail(docInfo); //[1006]
						
						// 2.DOC_STATUS 확인 :: DOC_STATUS == 'C' 이면 EXCEPTION
//						if(!oriDoc.getDoc_status().equals(Constant.D)){
//						//	throw processException("common.required.error");
//						}
						
						// 3.동일 문서명 정보 / CHECK 
						docInfo.put("folder_id", targetFolderId);
						docInfo.put("doc_id", vo.getDoc_id());
						docInfo.put("doc_name", vo.getDoc_name());
						docInfo.put("is_current", vo.getIs_current());
						docInfo.put("doc_status", vo.getDoc_status());

						String getDocName = vo.getDoc_name();
						String newName = getDocName;
						if(documentDao.isExitsDocName(docInfo) > 0) {
							newName = getUniqueDocumentName(getDocName, targetFolderId, Constant.ACTION_ADMIN_RESTORE);
						}
						
						// 4.복원할 문서 정보
						vo.setDoc_id(oriDoc.getDoc_id());
						vo.setDoc_name(newName);
						vo.setRoot_id(oriDoc.getRoot_id());
						vo.setPage_cnt(oriDoc.getPage_cnt());
						vo.setIs_current(oriDoc.getIs_current());
						vo.setIs_locked(Constant.F);
						vo.setVersion_no(oriDoc.getVersion_no());
						vo.setDoc_status(Constant.C);
						vo.setAcl_id(folderAcl);
						vo.setFolder_id(targetFolderId);
						vo.setCreator_id(oriDoc.getCreator_id());
						vo.setCreator_name(oriDoc.getCreator_name());
						vo.setOwner_id(oriDoc.getOwner_id());
						vo.setAuthor_list(oriDoc.getAuthor_list());
						vo.setRef_id(oriDoc.getRef_id());
						
						// 휴지통 문서중 만기문서가 존재할때
						if(oriDoc.getIs_expired().toString().equals(Constant.T)) {
							//is_expired : T -> F
							vo.setIs_expired(Constant.F);
							vo.setPreservation_year(getExpiredYear);					
						} else {
							vo.setIs_expired(Constant.F);
						}						
						
						// 5.XR_DOCUMENT :: INSERT
						documentWrite(Constant.INSERT, "", oriDoc.getPage_total(), vo, documentDao);
						
						// 6.XR_LINKED :: INSERT
						docInfo.put("doc_id",vo.getDoc_id());
						docInfo.put("folder_id", targetFolderId);
						folderDao.writeXrLinked(docInfo);
						
						// 7.XR_DOCUMENT_DEL :: DELETE
						docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_DEL_TABLE);
						
						// 8. XR_DOCUMENT_HT 등록처리 :: ACTION - MOVE
						long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
						docHistoryWrite(historyDao, doc_seq, root_id, vo.getDoc_id(), Constant.ACTION_ADMIN_RESTORE, vo.getDoc_type(), vo.getDoc_name(), vo.getVersion_no(), sessionVO);
						
					}

				}	// END OF FOR
				
			// 개인문서함일때
			} else {
				for(HashMap<String,Object> dMap : docList) {
					
					/*****************************************************************************************
					// 주) 휴지통 문서 복원 
					 * XR_DOCUMENT : DOC_STUATS D => C 로 변경처리
					 * XR_LINKED 기존 데이터 삭제처리
					 * XR_LINKED 새로운 데이터 추가처리
					*****************************************************************************************/
					// 1.모든 버전 문서 목록 리스트 가져오기
					String root_id = dMap.get("root_id").toString().equals("") ? dMap.get("doc_id").toString() : dMap.get("root_id").toString();
					String doc_id = dMap.get("doc_id").toString();
					allVersionList = docAllVersionList(documentDao,doc_id,Constant.DOC_DEL_TABLE);
					
					// 2.개인문서함 루트 하위에 휴지통복원함 폴더 생성
					FolderVO folderVO = new FolderVO();
					folderVO.setFolder_name_ko("휴지통복원함");
					folderVO.setParent_id(targetFolderId);
					
					// 동일한 부모 폴더 아래 동일 폴더명이 존재하는 검사
					int ret = folderDao.folderIsExistByFolderNameAndParentID(folderVO);
					if(ret == 0) {
						
						int folder_id = commonService.commonNextVal(Constant.COUNTER_ID_FILE);
						folderVO.setFolder_id(CommonUtil.getStringID(Constant.ID_PREFIX_FOLDER, folder_id));
						folderVO.setFolder_type(Constant.FOLDER_TYPE_DOCUMENT);
						folderVO.setMap_id(Constant.MAP_ID_MYPAGE);
						folderVO.setAcl_id("acl_public_owner");
						folderVO.setCreator_id(targetFolderId);
						folderVO.setSort_index(0);
						
						folderDao.folderWrite(folderVO);
					} else {
						docInfo.put("parent_id", targetFolderId);
						docInfo.put("folder_name_ko", "휴지통복원함");
						folderVO = folderDao.folderDetail(docInfo);
					}
					
					// 선택된 폴더에 문서등록 & XR_DOCUMENT_DEL에서 삭제처리
					for(DocumentVO vo : allVersionList)	{
					
						// 1.기존 DOCUMENT 정보 얻기
						docInfo.put("table_nm", Constant.DOC_DEL_TABLE);
						docInfo.put("doc_id", vo.getDoc_id());
						DocumentVO oriDoc = documentDao.commonDocDetail(docInfo);
									
						// 2.DOC_STATUS 확인 :: DOC_STATUS == 'C' 이면 EXCEPTION
						if(!oriDoc.getDoc_status().equals(Constant.D)){
						//	throw processException("common.required.error");
						}
						
						// 3.동일 문서명 정보 / CHECK 
						docInfo.put("folder_id", folderVO.getFolder_id());
						docInfo.put("doc_id", vo.getDoc_id());
						docInfo.put("doc_name", vo.getDoc_name());
						docInfo.put("is_current", vo.getIs_current());
						docInfo.put("doc_status", vo.getDoc_status());

						String getDocName = vo.getDoc_name();
						String newName = getDocName;
						if(documentDao.isExitsDocName(docInfo) > 0) {
							newName = getUniqueDocumentName(getDocName, folderVO.getFolder_id(), Constant.ACTION_ADMIN_RESTORE);
						}
						
						// 4.복원할 문서 정보
						vo.setDoc_id(oriDoc.getDoc_id());
						vo.setDoc_name(newName);
						vo.setRoot_id(oriDoc.getRoot_id());
						vo.setPage_cnt(oriDoc.getPage_cnt());
						vo.setIs_current(oriDoc.getIs_current());
						vo.setIs_locked(Constant.F);
						vo.setVersion_no(oriDoc.getVersion_no());
						vo.setDoc_status(Constant.C);
						vo.setAcl_id(oriDoc.getAcl_id());
						vo.setFolder_id(folderVO.getFolder_id());
						vo.setCreator_id(oriDoc.getCreator_id());
						vo.setCreator_name(oriDoc.getCreator_name());
						vo.setOwner_id(targetFolderId);
						vo.setAuthor_list(oriDoc.getAuthor_list());
						vo.setRef_id(oriDoc.getRef_id());
						
						// 5.XR_DOCUMENT :: INSERT
						documentWrite(Constant.INSERT, "", oriDoc.getPage_total(), vo, documentDao);
						
						// 6.XR_LINKED :: INSERT
						docInfo.put("doc_id",vo.getDoc_id());
						docInfo.put("folder_id", folderVO.getFolder_id());
						folderDao.writeXrLinked(docInfo);
						
						// 7.XR_DOCUMENT_DEL :: DELETE
						docCommonDelete(documentDao,vo.getDoc_id(),Constant.DOC_DEL_TABLE);
						
						// 8. XR_DOCUMENT_HT 등록처리 :: ACTION - MOVE
						long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);			
						docHistoryWrite(historyDao, doc_seq, root_id, vo.getDoc_id(), Constant.ACTION_ADMIN_RESTORE, vo.getDoc_type(), vo.getDoc_name(), vo.getVersion_no(), sessionVO);
						
					}

				}	// END OF FOR

			} // END OF IF

			resultMap.put("result",Constant.RESULT_TRUE);
			
			return resultMap;
		}


		
	}
