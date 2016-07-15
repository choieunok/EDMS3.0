package kr.co.exsoft.external.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.exsoft.net.SizedInputStream;

import kr.co.exsoft.common.dao.HistoryDao;
import kr.co.exsoft.common.service.CacheService;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.DocumentHtVO;
import kr.co.exsoft.common.vo.HistoryVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.dao.DocumentDao;
import kr.co.exsoft.document.dao.PageDao;
import kr.co.exsoft.document.dao.TypeDao;
import kr.co.exsoft.document.service.DocumentService;
import kr.co.exsoft.document.vo.AttrItemVO;
import kr.co.exsoft.document.vo.AttrVO;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.document.vo.PageVO;
import kr.co.exsoft.document.vo.TypeVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.configuration.ConstantInterfaceErrorCode;
import kr.co.exsoft.eframework.exception.ExrepClientException;
import kr.co.exsoft.eframework.library.ExsoftAbstractServiceImpl;
import kr.co.exsoft.eframework.repository.EXrepClient;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.util.PagingAjaxUtil;
import kr.co.exsoft.eframework.util.PatternUtil;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.eframework.util.UtilFileApp;
import kr.co.exsoft.external.controller.ExternalPublicController;
import kr.co.exsoft.external.dao.ExternalDao;
import kr.co.exsoft.external.vo.ExternalVO;
import kr.co.exsoft.folder.dao.FolderDao;
import kr.co.exsoft.folder.service.FolderService;
import kr.co.exsoft.folder.vo.FolderVO;
import kr.co.exsoft.permission.dao.AclDao;
import kr.co.exsoft.permission.service.AclService;
import kr.co.exsoft.permission.vo.AclExItemVO;
import kr.co.exsoft.permission.vo.AclItemVO;
import kr.co.exsoft.permission.vo.AclVO;
import kr.co.exsoft.user.dao.GroupDao;
import kr.co.exsoft.user.dao.UserDao;
import kr.co.exsoft.user.vo.GroupVO;
import kr.co.exsoft.user.vo.UserVO;


/***
 * External 서비스 구현 부분 - 외부 DB 미사용시 @Autowired @Qualifier 주석처리하세요.
 * @author 패키지 개발팀
 * @since 2014.07.17
 * @version 3.0
 *
 * [1000][EDMS-REQ-070~81]	2015-08-24	최은옥	 : 외부시스템 연계
 * [1001][EDMS-REQ-070~81]	2015-09-14	최은옥	 : 외부시스템 연계코드관리(관리자 화면)
 * [1007][EDMS-REQ-070~81]	2016-04-28	최은옥	 : 외부시스템 폴더 생성시 '관리자'로 생성되게 수정
 * [2000][로직 수정]	2016-07-01	이재민 : 페이지 다운로드시 맞는 doc_id에 존재하지않는 page_id를 입력하면 exception발생하게 수정
 * 
 */
@Service("externalService")
public class ExternalServiceImpl extends ExsoftAbstractServiceImpl implements ExternalService {

	@Autowired
	@Qualifier("sqlSessionImp")
	private SqlSession sqlSessionImp;
	
	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	
	@Autowired
	@Qualifier("sqlSessionBatch")
	private SqlSession sqlSessionBatch;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private AclService aclService;
	
	@Autowired
	private FolderService folderService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private DocumentService documentService;

	
	private final boolean SQL_BATCH_SESSION	=	true;
	private final boolean SQL_SESSION		=	false;
	
	private String[] errorCd = new String[]{};
	protected static final Log logger = LogFactory.getLog(ExternalServiceImpl.class);
	
	@Override
	public CaseInsensitiveMap externalUserDetail(HashMap<String,Object> map) throws Exception {
		
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		
		CaseInsensitiveMap ret = new CaseInsensitiveMap();
		
		ret = externalDao.externalUserDetail(map);
	
		if (ret == null)	
			throw processException("result.nodata.msg");
		
		return ret;
	}
	
	@Override
	public void batchUserWrite(List<HashMap<String,Object>> userList) throws Exception {
	
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		
		for(HashMap<String,Object> map : userList) {
			
			externalDao.externalGroupedWrite(map);			
		}
	
	}

	@Override
	public int externalUserWrite(HashMap<String,Object> map) throws Exception  {
		
		int ret = 0;
		
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		
		ret = externalDao.externalUserWrite(map);
		
		return ret;
	}
	
	@Override
	public int externalUserWriteTx(HashMap<String,Object> map) throws Exception {
		
		int ret = 0;
		
		ExternalDao externalDao = sqlSessionImp.getMapper(ExternalDao.class);
		
		ret = externalDao.externalUserWrite(map);
		
		if(ret != 1)
			throw processException("result.insert.fail");
		
		ret = externalDao.externalGroupedWrite(map);
		
		if(ret != 1)
			throw processException("result.insert.fail");
		
		return ret;
	}
	
	// [1000]
	@Override
	public Map<String, Object> interfaceTypeDocument(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String action = map.get("action") != null ? map.get("action").toString() : "";
		String user_id = map.get("authId") != null ? map.get("authId").toString() : "";
				
		//엑션코드 없으면 exception
		if(StringUtil.isEmpty(action)){
			throw processException("there is no action");
		}
		
		map.put("user_id", user_id);
		
		switch(action){
			//신규문서 등록(첨부파일이 존재하는 문서)
			case Constant.INTERFACE_ACTION_CREATEDOC: resultMap = ifActCreateDoc(map, files, sessionVO); break;
			
			//신규문서 등록(첨부파일이 없는 문서)
			case Constant.INTERFACE_ACTION_CREATEDOC_WITHOUTFILE: resultMap = ifActCreateDoc(map, null, sessionVO); break;

			//문서 조회
			case Constant.INTERFACE_ACTION_SELECTDOC: resultMap = ifActSelectDoc(map, sessionVO); break;	
			
			//문서 삭제	
			case Constant.INTERFACE_ACTION_DELETEDOC: resultMap = ifActDeleteDoc(map, sessionVO); break;	
			
			//문서 수정	
			case Constant.INTERFACE_ACTION_UPDATEDOC: resultMap = ifActUpdateDoc(map, sessionVO); break;	

			//문서 이동	
			case Constant.INTERFACE_ACTION_MOVEDOC: resultMap = ifActMoveDoc(map, sessionVO); break;
			default: 
				errorCd = new String[]{ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,"action"};
				throw processException(ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,errorCd);	
		}
						
		return resultMap;
	}
	
	/**
	 * <pre>
	 * 1. 개요 : 외부연계 문서 등록 처리
	 * 2. 처리내용 :  
	 * </pre>
	 * @Method Name : ifActCreateDoc
	 * @param map
	 * @param files
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> ifActCreateDoc(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();			
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		ExternalDao externalDao = sqlSession.getMapper(ExternalDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		UserDao userDao = sqlSession.getMapper(UserDao.class);	
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		AclDao aclDao = sqlSession.getMapper(AclDao.class);

		int result = 0; //DB 결과 return

		// 사용자 정보 그룹 찾기, 필수값 체크 1 (유저명)
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);		
		
		// 사용자 정보 체크
		if (resultVO == null)	{
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESSUSER_ERROR_1001};
			throw processException(ConstantInterfaceErrorCode.DBACCESSUSER_ERROR_1001,errorCd);
		}

		// 그룹+프로젝트 그룹 ID			
		List<String> dualGroup = new ArrayList<String>();
		dualGroup = dualGroupList(map, SQL_SESSION);
		dualGroup.add(resultVO.getGroup_id());
		map.put("group_id_list",dualGroup);
		
		//외부연계 파일 EDMS 저장 위치 가져오기 
		CaseInsensitiveMap ret = new CaseInsensitiveMap();	
		ret = interfaceCodeValue(map);	
		if (ret == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_INTERFACE_CODE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
				
		//문서 저장할 EDMS 경로 가져오기
		String folderId = String.valueOf(ret.get("FOLDER_ID"));
		
		
		//현재날짜 기준으로 폴더 생성=================================================
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;		  
		String str = String.format("%04d/%02d",year,month);
		//해당 기간계 공용폴더 + 요청 Path +  년도  + 월
		map.put("folderPath", map.get("folderPath").toString() +"/" +str );
		
		String lastFolderId = checkRequestFolder(folderId,map,folderDao,aclDao);

		//==================================================================
		
		
		//문서명에 특수문자 포함 여부 체크	
		String doc_name = map.get("name") != null ? map.get("name").toString() : null ;
		//필수값 체크 2 (문서명)
		if(StringUtil.isEmpty(doc_name)){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"name"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		
		boolean spacialChk = PatternUtil.webfolderCheck(map.get("name") != null ? map.get("name").toString(): null) ;
		if( spacialChk ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.MISNAMING_ERROR_2004};
			throw processException(ConstantInterfaceErrorCode.MISNAMING_ERROR_2004,errorCd);
		}
			
		EXrepClient eXrepClient = new EXrepClient();	// eXrep C/S Client 생성. 
		
		//등록할 폴더의 문서 등록 권한 체크 ========================================
		map.put("folder_id",lastFolderId);
		FolderVO folderVO = folderDao.folderDetail(map);
			
		//  폴더 문서저장여부 체크
		if(folderVO.getIs_save().equals(Constant.NO))	{
			errorCd = new String[]{ConstantInterfaceErrorCode.NOSAVEFOLDER_ERROR_2008};
			throw processException(ConstantInterfaceErrorCode.NOSAVEFOLDER_ERROR_2008,errorCd);							
		}
		
		String docPart="";
		HashMap<String, Object> param1 = new HashMap<String, Object>();	
		if(folderVO.getMap_id().equals(Constant.MAP_ID_DEPT))	{
			param1.put("menu_cd",Constant.USER_FOLDER_MENU_CODE);
			param1.put("role_id",resultVO.getRole_id());
			docPart = commonService.getMenuAuth(param1);
		}
		
		// ROLE권한자 & 일반권한자 모두 체크
		if(!isFolderDocCreateAuth(map,resultVO,docPart,folderDao,aclDao)) {
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACLSAVEFOLDER_ERROR_2009};
			throw processException(ConstantInterfaceErrorCode.NOACLSAVEFOLDER_ERROR_2009,errorCd);				
		}
		//===============================================================
		
		//동일명 등록 불가
		String existName = map.get("existName") != null ? map.get("existName").toString() : null ;
		//필수값 체크 3 (동일명 존재여부)
		if(StringUtil.isEmpty(existName)){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"existName"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);	
		}
		//'F'일 경우, 동일폴더내 동일명 금지 'T'일 경우, 동일 폴더 내 동일명 허가, F나 T 이외의 값일 경우에도 Exception
		switch(existName){
			case Constant.F:
				param.put("is_current",Constant.T);
				param.put("doc_status",Constant.C);
				param.put("doc_name",map.get("name").toString());
				param.put("folder_id", lastFolderId);

				if(documentDao.isExitsDocName(param) > 0)	{
					errorCd = new String[]{ConstantInterfaceErrorCode.EXSISTNAME_ERROR_2006,};
					throw processException(ConstantInterfaceErrorCode.EXSISTNAME_ERROR_2006,errorCd);	
				}
				break;
			case Constant.T:
				break;				
			default:
				errorCd = new String[]{ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,"existName"};
				throw processException(ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,errorCd);	
				//break;			
		}
	
		//DocumentVO 객체 생성
		DocumentVO documentVO = new DocumentVO();
		//신규 Doc_id
		int doc_id =  commonService.commonNextVal(Constant.COUNTER_ID_FILE);
		documentVO.setDoc_id(CommonUtil.getStringID(Constant.ID_PREFIX_DOCUMENT, doc_id));
		documentVO.setRef_id(documentVO.getDoc_id().replaceAll(Constant.ID_PREFIX_DOCUMENT,Constant.ID_PREFIX_REF)); // REF_ID => DOC -> REF 변경처리
		
		// 문서 등록자 설정			
		documentVO.setCreator_id(map.get("authId").toString());
		documentVO.setCreator_name(resultVO.getUser_name_ko());
		documentVO.setDoc_name(map.get("name").toString());
		documentVO.setDoc_type(Constant.DOC_TABLE);
		
		// 업무 등록의 경우 대표작성자가 owner가 된다
		documentVO.setOwner_id(map.get("authId").toString());			
		documentVO.setRoot_id(null);

		// IS_CURRENT/IS_LOCKED/VERSION_NO/IS_EXPIRED/		
		documentVO.setIs_current(Constant.T);
		documentVO.setIs_expired(Constant.F);
		documentVO.setIs_inherit_acl(Constant.T);
		documentVO.setFolder_id(lastFolderId);
		// EX_ACL_ID :: 사용안함 별도 테이블 처리 => XR_EXACLITEM
		String aclType = map.get("aclType") != null ? map.get("aclType").toString() : null ; //필수값 체크는 이전 메소드에서
		//필수값 체크여부4 ( 권한타입)
		if(StringUtil.isEmpty(aclType)){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"aclType"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
		}

		HashMap<String, Object> reqInfo = new HashMap<String, Object>();
		reqInfo.put("user_id", map.get("authId").toString());
		 
		//UserVO userVO = userDao.userGroupDetail(reqInfo);
		List<AclExItemVO> aclExItemList = new ArrayList<AclExItemVO>(); 

		//추가 접근자 잘라오기
		String[] extacl = map.get("aclList") != null ? map.get("aclList").toString().split("\\|") : null ;
		int deptchk = 0;
		
		switch(aclType){
			//전사
			case Constant.ACL_ACL_TYPE_ALL:
//				documentVO.setAcl_id(Constant.ACL_ID_WORLD);
				documentVO.setAcl_id(Constant.ACL_ID_INTERFACE_WORLD); // 전사연계기본권한으로 변경
				break;
			//부서
			case Constant.ACL_ACL_TYPE_DEPT:
				//dept 일때 문서에 폴더 권한 상속
				documentVO.setAcl_id(folderVO.getAcl_id());				
				break;
			//부서내 업무파트
			case Constant.INTERFACE_ACLTYPE_PART:
				//part 일때 기본권한 없음 Setting
				documentVO.setAcl_id(Constant.INTERFACE_ACL_ID_DEFAULT);
				if(extacl != null) {
					for(String accessor_id : extacl) {
						AclExItemVO tempAclExIemVO = new AclExItemVO();
						map.put("dept_id", accessor_id);
						deptchk = externalDao.chkDeptCnt(map);

						//부서가 존재하지 않으면 Exception
						if(deptchk==0){
							errorCd = new String[]{ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,accessor_id};
							throw processException(ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,errorCd);	
						}

						tempAclExIemVO.setDoc_id(documentVO.getDoc_id());
						tempAclExIemVO.setAccessor_id(accessor_id);
						tempAclExIemVO.setAccessor_isgroup(Constant.T);
						tempAclExIemVO.setAccessor_isalias(Constant.T);
						tempAclExIemVO.setAct_browse(Constant.T);
						tempAclExIemVO.setAct_read(Constant.T);
						tempAclExIemVO.setAct_create(Constant.T);
						tempAclExIemVO.setAct_delete(Constant.T);
						tempAclExIemVO.setAct_update(Constant.T);
						tempAclExIemVO.setAct_cancel_checkout(Constant.T);
						tempAclExIemVO.setAct_change_permission(Constant.T);
						aclExItemList.add(tempAclExIemVO);

					}
				 }else{
					 //aclType이 PART이고 aclLis가 없는 경우 필수값 오류
					errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"aclList"};
					throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
				 }
				
			break;
			//권한 개별
			case Constant.INTERFACE_ACLTYPE_ETC:
				//개별일때 기본권한 없음 Setting
				documentVO.setAcl_id(Constant.INTERFACE_ACL_ID_DEFAULT);
				if(extacl != null) {
					int userchk=0;
					HashMap<String,Object> userparam = new HashMap<String,Object>();
					for(String accessor_id : extacl) {
						AclExItemVO tempAclExIemVO = new AclExItemVO();
						userparam.put("strKeyword", accessor_id);
						userchk = userDao.userExists(userparam);							
						//유저와 부서가 존재하지 않으면 Exception							
						if(userchk==0){
							map.put("dept_id", accessor_id);
							deptchk = externalDao.chkDeptCnt(map);
							if(deptchk==0){
								errorCd = new String[]{ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,accessor_id};
								throw processException(ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,errorCd);	
							}else{
								tempAclExIemVO.setAccessor_isgroup(Constant.T);
								tempAclExIemVO.setAccessor_isalias(Constant.F);
							}
						}else{
							UserVO isUser = new UserVO();
							isUser = existUser(accessor_id);		
							
							// 존재하는 사용자 인지 체크
							if (isUser == null)	{
								errorCd = new String[]{ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,accessor_id};
								throw processException(ConstantInterfaceErrorCode.NOPARTUSER_ERROR_2005,errorCd);
							}
							
							tempAclExIemVO.setAccessor_isgroup(Constant.F);
							tempAclExIemVO.setAccessor_isalias(Constant.F);
						}
						
						tempAclExIemVO.setDoc_id(documentVO.getDoc_id());
						tempAclExIemVO.setAccessor_id(accessor_id);
						tempAclExIemVO.setAct_browse(Constant.T);
						tempAclExIemVO.setAct_read(Constant.T);
						tempAclExIemVO.setAct_create(Constant.T);
						tempAclExIemVO.setAct_delete(Constant.T);
						tempAclExIemVO.setAct_update(Constant.T);
						tempAclExIemVO.setAct_cancel_checkout(Constant.T);
						tempAclExIemVO.setAct_change_permission(Constant.T);
						aclExItemList.add(tempAclExIemVO);

					}
				 }else{
					 //aclType이 PART이고 aclLis가 없는 경우 필수값 오류
					 errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"aclList"};
					 throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
				 }	
			break;
			
		}
		//추가 접근자 Setting
		documentVO.setAclExItemList(aclExItemList);

		// 2.6 AUTHOR_LIST  :: 미입력시 등록자 성명 입력처리
		documentVO.setAuthor_list(resultVO.getUser_name_ko());

		//첨부파일
		long pageTotal = 0L;
		int cnt=0;
		List<HashMap<String,Object>> fileList =  new ArrayList<HashMap<String,Object>>();
		String contentPath = "";
		long fileSize = 0L;
		
		String action = map.get("action") != null ? map.get("action").toString() : null ; //필수값 체크는 이전 메소드에서
		if(action.equalsIgnoreCase(Constant.INTERFACE_ACTION_CREATEDOC) && files == null && files.length == 0){
        	//필수값 체크 - 첨부파일이 있는 문서등록의 경우 첨부파일이 존재하지 않으면 필수값 오류
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"fileUpload"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
        }
		if (files != null && files.length > 0) {
			try {
				eXrepClient.connect();
				
	            for (MultipartFile multipartFile : files) {
	            	HashMap<String, Object> fileparam = new HashMap<String, Object>();
	            	pageTotal += multipartFile.getSize();//원본 파일  전체 사이즈
	            	fileSize = multipartFile.getSize();//원본 파일 개별 사이즈
	            	cnt++;
	            	contentPath = CommonUtil.getContentPathByDate(ConfigData.getString(Constant.EXREP_ROOT_EDMS_NM))+UUID.randomUUID().toString();
					
	                fileparam.put("orgFile",multipartFile.getOriginalFilename());
	                fileparam.put("contentPath",contentPath);
	                fileparam.put("fileSize",multipartFile.getSize());
	                fileparam.put("volumeId",ConfigData.getString(Constant.EXREP_VOLUME_NM));
	                
	                if(!eXrepClient.putFile(new SizedInputStream(multipartFile.getInputStream(), fileSize),ConfigData.getString(Constant.EXREP_VOLUME_NM), contentPath,true)) {										
						//저장소 파일 등록중 오류
						errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3002};
						throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3002,errorCd);	
					}
	                
	                fileList.add(fileparam);					
	            }
			} catch (ExrepClientException e) {
				//저장소 연결 중 오류
				logger.debug(StringUtil.getErrorTrace(e));	
				errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3003};
				throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3003,errorCd);	
			} finally {
				try{
					eXrepClient.disconnect();
				}catch(Exception e){
					//저장소 연결 해제중 오류
					logger.debug(StringUtil.getErrorTrace(e));	
					errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3004};
					throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3004,errorCd);	
				}
			}
        }
        documentVO.setInsertFileList(fileList);
        documentVO.setPage_cnt(cnt);
        documentVO.setPage_total(pageTotal);
        
		// 4. 문서와 Storage 용량 계산
		String rootFolderId = "";
        HashMap<String,Object> folderParam = new HashMap<String, Object>();
        folderParam.put("folder_id",folderId);
		
		FolderVO getFolderInfo = new FolderVO();
		getFolderInfo = folderDao.folderDetail(folderParam);
		
		FolderVO folderFullPath = folderService.getRootFolder(getFolderInfo.getMap_id(), getFolderInfo.getFolder_id(), getFolderInfo);
					
		rootFolderId = folderFullPath.getFolder_id().toString();
		
		FolderVO parentInfo = new FolderVO();
		FolderVO getFolder = new FolderVO();
		parentInfo.setFolder_id(rootFolderId);
		getFolder = folderDao.getFolderStorage(parentInfo);
		
		long quota = getFolder.getStorage_quota();
		long usage = getFolder.getStorage_usage();
		if(quota > -1){
			if((quota - usage) < pageTotal){				
				errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3001};
				throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3001,errorCd);	
			}
		}

		// 문서설명 HTML ESCAPE			
		String doc_description = map.get("description") != null ? map.get("description").toString() : "";
		documentVO.setDoc_description(StringUtil.java2Html(doc_description));
		
		//반환값 Setting
		resultMap.put("docId", documentVO.getDoc_id());
		
		map.put("isType","insert");
		map.put("version_type","NEW");	
		
		
		List<PageVO> pageList = new ArrayList<PageVO>();				// 신규추가 파일 리스트	
		
		// 1.PageVO 객체 생성  :: 첨부파일 존재시 이미 exrep에 등록 됨 (신규/수정)
        List<PageVO> insertPageList =  insertPage(documentVO);
	
       

		pageList.addAll(insertPageList);
		
		// 문서등록 :: XR_DOCUMENT INSERT
		result = documentDao.writeDocument(documentVO);
		if(result == 0)	{
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT(INSERT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);	
		}
		
		//xr_linked 추가
		xrLinkedWrite(Constant.INSERT,null,documentVO,folderDao); 
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_LINKED(INSERT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);		
		}
		
		// 신규등록파일 DB처리 :: XR_PAGE/XR_FILED INSERT
		result = 0;
		HashMap<String,Object> repage = new HashMap<String,Object>(); // [1000]2015/10/19 상품관리 추가요건
		List<HashMap<String,Object>> repageList = new ArrayList<HashMap<String,Object>>();// [1000]2015/10/19 상품관리 추가요건
		if(pageList != null && pageList.size() > 0 )	{
			param.put("doc_id", documentVO.getDoc_id());
			for(PageVO pageVO : pageList)	{
				result = pageDao.writePage(pageVO);
				//=========================================
				//  [1000]2015/10/19 상품관리 추가요건
				//========================================= START
				repage = new HashMap<String,Object>();
				repage.put("pageId",String.valueOf(pageVO.getPage_id()));
				repage.put("pageName",pageVO.getPage_name());
				repage.put("pageSize",String.valueOf(pageVO.getPage_size()));
				repageList.add(repage);
				//========================================= END
				
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_PAGE(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);		
				}
				
				// page_no는 insert query에서 처리
				param.put("page_id",pageVO.getPage_id());
				result = pageDao.writeXrFiled(param);
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_FILED(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);		
				}					
			}
		}
		//=========================================		
		resultMap.put("pageCnt",pageList != null ? String.valueOf(pageList.size()) : "0" ); // [1000]2015/10/23 상품관리 추가요건
		resultMap.put("pageList",repageList); // [1000]2015/10/19 상품관리 추가요건
		//=========================================
		// 2.7.2 추가 접근자 등록
		for(AclExItemVO aclExItemVO : documentVO.getAclExItemList()){
			aclExItemVO.setDoc_id(documentVO.getDoc_id());
			aclService.aclExItemWrite(null, aclExItemVO);
		}
		
		// XR_DOCUMENT_HT 등록처리 ::  ACTION : CREATE
		String action_type = Constant.ACTION_CREATE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,documentVO.getRoot_id(), documentVO.getDoc_id(), action_type, documentVO.getDoc_name(),sessionVO);
		
		
		return resultMap;
	}
	
	public CaseInsensitiveMap interfaceCodeValue(HashMap<String,Object> map) throws Exception {
		return interfaceCodeValue(map,false);
	}
	
	//xr_interface_code 값 가져오기
	public CaseInsensitiveMap interfaceCodeValue(HashMap<String,Object> map, boolean sqlbatch) throws Exception {
		ExternalDao externalDao = null;
		if(!sqlbatch){
			externalDao = sqlSession.getMapper(ExternalDao.class);
		}else{
			externalDao = sqlSessionBatch.getMapper(ExternalDao.class);
		} 
		
		CaseInsensitiveMap result = new CaseInsensitiveMap();
		result = externalDao.externalInterface(map);
		
		return result;
	}
	
	//문서 조회
	public Map<String, Object> ifActSelectDoc(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> convertResultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();	

		String[] errorCd = new String[]{};
		
		String doc_id = map.get("docId") != null ? map.get("docId").toString() : null ;
		if(StringUtil.isEmpty(doc_id)  ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);		
		}

		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION);		

		//해당 문서의 Read권한이 있는지확인 BROWSE:1, Read:2, UPDATE:3, DELETE:4 
		boolean aclCheck = isExistAcl(aclList, Constant.ACL_INT_READ);
		
		//읽기 권한 체크
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_READ};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);		
		}
		
		//1-1 param set
		param.put("doc_id", doc_id);
		param.put("table_nm", Constant.DOC_TABLE);
		param.put("actionType", null);
		
		//1-2. sessionVO set
//		SessionVO sessionVO = new SessionVO();
//		sessionVO.setSessId(map.get("authId").toString());
		/**
		 * vo.setActor_id(sessionVO.getSessId());
		vo.setActor_nm(sessionVO.getSessName());
		vo.setGroup_id(sessionVO.getSessGroup_id());
		vo.setGroup_nm(sessionVO.getSessGroup_nm());
		vo.setConnect_ip(sessionVO.getSessRemoteIp());
		vo.setAction_place(Constant.ACTION_PLACE);
		 */
		
		//docCommonView 문서정보 가져오기		
		resultMap = selectDoc(param, sessionVO);
		
		DocumentVO docVO = new DocumentVO();
		docVO = (DocumentVO)resultMap.get("documentVO");
		
		convertResultMap.put("docId",docVO.getDoc_id());
		convertResultMap.put("name",docVO.getDoc_name());
		convertResultMap.put("docTypeId",docVO.getDoc_type());
		
		if(docVO.getDoc_type().equals(Constant.DOC_TABLE)){
			convertResultMap.put("docTypeName","일반문서");
		}else{
			convertResultMap.put("docTypeName","");
			
		}
		
		
		HashMap<String, Object> param2 = new HashMap<String, Object>();
		param2.put("folder_id", docVO.getFolder_id());
		String folderFullpath = commonService.folderFullPath(param2);
		
		convertResultMap.put("fileCount",docVO.getPage_cnt());
		convertResultMap.put("description",docVO.getDoc_description());
		convertResultMap.put("location",folderFullpath);//
		convertResultMap.put("createId",docVO.getCreator_id());
		convertResultMap.put("createName",docVO.getCreator_name());
		convertResultMap.put("createDate",docVO.getCreate_date());
		
		
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
		// XR_DOCUMENT_HT 등록처리 ::  ACTION : READ
		String action_type = Constant.ACTION_READ;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(), docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		
		return convertResultMap;
	}
	
	/**
	 * <pre>
	 * 1. 개요 : [1000] 외부연계 문서 등록 처리
	 * 2. 처리내용 :  
	 * </pre>
	 * @Method Name : ifActDeleteDoc
	 * @param map
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> ifActDeleteDoc(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();		

		String doc_id = map.get("docId") != null ? map.get("docId").toString() : null ;
		if(StringUtil.isEmpty(doc_id)  ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
		}	

		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_BATCH_SESSION);
		
		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_BATCH_SESSION, resultVO);

		//해당 문서의 Read권한이 있는지확인 BROWSE:1, Read:2, UPDATE:3, DELETE:4 
		boolean aclCheck = isExistAcl(aclList, Constant.ACL_INT_DELETE);
		
		//2. 삭제 권한 체크
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_DELETE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);	
		}
				
		//3. 문서 삭제
		//3-1. 삭제 문서 set
		param.put("root_id", "");
		param.put("doc_id", doc_id);
		
		List<HashMap<String, Object>>  docList = new ArrayList<HashMap<String,Object>>();
		docList.add(param);
		
		//3-2. sessionVO set
		setSessionVO(sessionVO, resultVO);
//		SessionVO sessionVO = new SessionVO();
//		sessionVO.setSessId(map.get("authId").toString());
		/**
		 * vo.setActor_id(sessionVO.getSessId());
		vo.setActor_nm(sessionVO.getSessName());
		vo.setGroup_id(sessionVO.getSessGroup_id());
		vo.setGroup_nm(sessionVO.getSessGroup_nm());
		vo.setConnect_ip(sessionVO.getSessRemoteIp());
		vo.setAction_place(Constant.ACTION_PLACE);
		 */
		
		CaseInsensitiveMap ret = new CaseInsensitiveMap();	
		ret = interfaceCodeValue(map,SQL_BATCH_SESSION);	
		if (ret == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_INTERFACE_CODE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
		String actionPlace = "";		
		//히스토리 액션
		actionPlace = "["+ String.valueOf(ret.get("WORK_CODE")) +"]" + ret.get("WORK_DESCRIPTION").toString();
		
		// TODO : 히스토리 place 외부 처리 필요	
		resultMap = documentService.terminateDocument(docList, null, sessionVO, true,actionPlace);
		
		return resultMap;
	}

	//문서 수정
	public Map<String, Object> ifActUpdateDoc(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		int result=0;
		
		String doc_id = map.get("docId") != null ? map.get("docId").toString() : null ;
		if(StringUtil.isEmpty(doc_id)  ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);		
		}
		
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
		
		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION, resultVO);

		//해당 문서의 Read권한이 있는지확인 BROWSE:1, Read:2, UPDATE:3, DELETE:4 
		boolean aclCheck = isExistAcl(aclList, Constant.ACL_INT_UPDATE);
		
		//갱신 권한 체크
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_UPDATE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);	
		}
		
		DocumentVO docVO = new DocumentVO();
		docVO.setDoc_id(doc_id);
		//문서명에 특수문자 포함 여부 체크
		boolean spacialChk = PatternUtil.webfolderCheck(map.get("name") != null ? map.get("name").toString(): null) ;
	
		if( spacialChk ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.MISNAMING_ERROR_2004};
			throw processException(ConstantInterfaceErrorCode.MISNAMING_ERROR_2004,errorCd);	
		}
		docVO.setDoc_name(map.get("name").toString());
		
		String doc_description = map.get("description") != null ? map.get("description").toString() : "";
		docVO.setDoc_description(StringUtil.java2Html(doc_description));
		docVO.setUpdater_id(resultVO.getUser_id());
		docVO.setUpdater_name(resultVO.getUser_name_ko());
		docVO.setUpdate_action(Constant.INTERFACE_DOCUMENT_UPDATE_ACTION);
		//문서 수정
		result = documentDao.documentUpdate(docVO);
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT(UPDATE)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);	
		}
		
		
		// XR_DOCUMENT_HT 등록처리 ::  ACTION : READ
		String action_type = Constant.ACTION_UPDATE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(), docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		
		return resultMap;
	}
	
	//문서 조회
	public Map<String, Object> selectDoc(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		//docCommonView 내 XR_DOCUMENT_HT 등록처리방지를 위한 Flag isExternal = true;
		resultMap = documentService.docCommonView(map, sessionVO, true);
		return resultMap;
	}
	
	//문서이동
	public Map<String, Object> ifActMoveDoc(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		FolderDao folderDao = sqlSession.getMapper(FolderDao.class);
		AclDao aclDao = sqlSession.getMapper(AclDao.class);

		String[] errorCd = new String[]{};
		
		String doc_id = map.get("docId") != null ? map.get("docId").toString() : null ;
		if(StringUtil.isEmpty(doc_id)  ) {
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);			
		}	

		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
		
		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION, resultVO);

		//해당 문서의 Read권한이 있는지확인 BROWSE:1, Read:2, UPDATE:3, DELETE:4 
		boolean aclCheck = isExistAcl(aclList, Constant.ACL_INT_UPDATE);
		
		//갱신 권한 체크
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_UPDATE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);	
		}
		
		//해당 doc_id의 문서 정보 가져오기
		HashMap<String, Object> param = new HashMap<String, Object>();	
		//1-1 param set
		param.put("doc_id", doc_id);
		param.put("table_nm", Constant.DOC_TABLE);
		param.put("actionType", null);
		
		
		resultMap = selectDoc(param, sessionVO);//documentService.docCommonView(param, sessionVO, isExternal);
		DocumentVO docVO = new DocumentVO();
		docVO = (DocumentVO)resultMap.get("documentVO");
		
		
		//외부연계 파일 EDMS 저장 위치 가져오기 
		CaseInsensitiveMap ret = new CaseInsensitiveMap();
		ret = interfaceCodeValue(map);
		if (ret == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"INTERFACE_CODE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);	
		}

		String folderId = String.valueOf(ret.get("FOLDER_ID"));
		
		//최종 이동할 폴더ID 가져오기(요청 폴더가 존재하지 않으면 새로 생성)
		String lastFolderId = checkRequestFolder(folderId,map,folderDao,aclDao);
		
		//docList 
		List<HashMap<String,Object>> docList = new ArrayList<HashMap<String,Object>>();		
		HashMap<String, Object> param2 = new HashMap<String, Object>();	
		param2.put("doc_id", docVO.getDoc_id().toString());
		if(StringUtil.isEmpty(docVO.getRoot_id().toString())){
			param2.put("root_id", "");
		}else{
			param2.put("root_id", docVO.getRoot_id().toString());
		}
		param2.put("doc_name", docVO.getDoc_name().toString());
		param2.put("is_locked", docVO.getIs_locked().toString());
		param2.put("doc_type", docVO.getDoc_type().toString());
		param2.put("folder_id", docVO.getFolder_id().toString());
		param2.put("is_inherit_acl", docVO.getIs_inherit_acl().toString());
		
		docList.add(param2);
		
		JSONArray jsonArray = JSONArray.fromObject(docList);
		
		map.put("docList", jsonArray);
		map.put("targetFolderId",lastFolderId);		
		map.put("type",Constant.ACTION_MOVE);
		
		List<HashMap<String,Object>> docList2 = new ArrayList<HashMap<String,Object>>();		
		
		docList2 = documentService.moveCopyDocValidList(map, sessionVO, true, folderId);
		resultMap = documentService.moveDocListUpdate(docList2,map,sessionVO,true);		
		
		
    	// XR_DOCUMENT_HT 등록처리 ::  ACTION : UPDATE
		String action_type = Constant.ACTION_MOVE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(),docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		return resultMap;
	}
	
	

	// [1000]
	// 파일 추가, 파일 수정
	@Override
	public Map<String, Object> interfaceTypePage(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();		
	
		String action = map.get("action")!= null ? map.get("action").toString() : "";
		
		if(action.equals("")){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"action"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}	

		map.put("user_id", map.get("authId").toString());
		
		switch(action){		
		
			//첨부파일 조회
			case Constant.INTERFACE_ACTION_SELECTPAGELIST: resultMap = ifActSelectPageList(map, sessionVO); break;
				
			//첨부파일 추가
			case Constant.INTERFACE_ACTION_APPENDPAGE:	resultMap = ifActAppendPage(map,files, sessionVO); break;

			//첨부파일 수정
			case Constant.INTERFACE_ACTION_UPDATEPAGE: resultMap = ifActUpdatePage(map,files, sessionVO); break;
			
			//첨부파일 삭제
			case Constant.INTERFACE_ACTION_DELETEPAGE: resultMap = ifActDeletePage(map, sessionVO); break;
			
			//첨부파일 다운로드
			case Constant.INTERFACE_ACTION_DOWNLOADPAGE: resultMap = ifActDownloadPage(map, sessionVO); break;
			default: 
				errorCd = new String[]{ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,"action"};
				throw processException(ConstantInterfaceErrorCode.VALIDATE_ERROR_2007,errorCd);	
		}
				
		return resultMap;
	}
	
	//[1000]
	//첨부파일 조회
	public Map<String, Object> ifActSelectPageList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		// 입력값 유효성 체크 :: 문서ID
		boolean isValidate = isValidate(map,false);
		if(!isValidate){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		String doc_id = map.get("docId").toString();
		
		map.put("doc_id", doc_id);
		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION);		
		boolean aclCheck = isExistAcl(aclList,Constant.ACL_INT_READ);
		
		//파일 첨부 권한  체크 Read
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_READ};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);
		}
		
		// 문서첨부파일정보 :: XR_FILED/XR_PAGE :: parameter : doc_id
		List<PageVO> pageList = pageDao.comDocPageList(map);
		
		HashMap<String,Object> repage = new HashMap<String,Object>();
		List<HashMap<String,Object>> repageList = new ArrayList<HashMap<String,Object>>();
		if(pageList.size()!=0 && pageList != null){			
			for(PageVO pageVO:pageList){
				repage = new HashMap<String,Object>();
				repage.put("pageId",pageVO.getPage_id());
				repage.put("pageName",pageVO.getPage_name());
				repage.put("pageSize",String.valueOf(pageVO.getPage_size()));
				repageList.add(repage);
			}
			
		}else{
			repage.put("page","There are no page");
			repageList.add(repage);
		}
		resultMap.put("pageCnt",String.valueOf(pageList.size()));
		resultMap.put("pageList",repageList);
		
		return resultMap;
	}
	
	//[1000]
	//첨부파일 추가
	public Map<String, Object> ifActAppendPage(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
	
		// 입력값 유효성 체크 :: 문서ID
		boolean isValidate = isValidate(map,false);
		if(!isValidate){
			throw processException("common.required.error");	
		}
		String doc_id = map.get("docId").toString();
		
		//해당 문서의 UPDATE권한이 있는지확인 
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION);		
		boolean aclCheck = isExistAcl(aclList,Constant.ACL_INT_UPDATE);
		
		//파일 첨부 권한  체크 UPDATE
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_UPDATE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);
		}	
		
		map.put("doc_id", doc_id);
		map.put("table_nm", Constant.DOC_TABLE);
		DocumentVO documentVO = new DocumentVO();
		documentVO = documentDao.commonDocDetail(map);	

		int result = 0;		
		
		DocumentVO docVO = new DocumentVO();
		docVO = documentVO;

		//추가 파일을 엑스랩에 등록
		List<HashMap<String,Object>> fileList = putExrepFile(files);
		long pageTotal = 0L;
        if (files != null && files.length > 0) {
            for (MultipartFile multipartFile : files) {
            	pageTotal += multipartFile.getSize();//원본 파일  전체 사이즈         
            }            
        }
        docVO.setInsertFileList(fileList);
        docVO.setPage_cnt(documentVO.getPage_cnt() + files.length);
        docVO.setPage_total(documentVO.getPage_total() + pageTotal);
        
        
        List<PageVO> pageList = new ArrayList<PageVO>();				// 신규추가 파일 리스트	
		
		// 1.PageVO 객체 생성  :: 첨부파일 존재시 이미 exrep에 등록 됨 (신규/수정)
        List<PageVO> insertPageList =  insertPage(documentVO);
		
		pageList.addAll(insertPageList);		
		
		docVO.setUpdate_action(Constant.ACTION_PAGE);		
		result = documentDao.documentUpdate(docVO);
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT(UPDATE)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
		
		
		// 신규등록파일 DB처리 :: XR_PAGE/XR_FILED INSERT
		result = 0;
		HashMap<String, Object> param = new HashMap<String, Object>();	
		HashMap<String,Object> repage = new HashMap<String,Object>(); // [1000]2015/10/23상품관리 추가요건
		List<HashMap<String,Object>> repageList = new ArrayList<HashMap<String,Object>>();// [1000]2015/10/23 상품관리 추가요건
		if(pageList != null && pageList.size() > 0 )	{
			param.put("doc_id", documentVO.getDoc_id());
			for(PageVO pageVO : pageList)	{
				result = pageDao.writePage(pageVO);
				
				//=========================================
				//  [1000]2015/10/23 상품관리 추가요건
				//========================================= START
				repage = new HashMap<String,Object>();
				repage.put("pageId",String.valueOf(pageVO.getPage_id()));
				repage.put("pageName",pageVO.getPage_name());
				repage.put("pageSize",String.valueOf(pageVO.getPage_size()));
				repageList.add(repage);
				//========================================= END
				
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_PAGE(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
				}
				
				// page_no는 insert query에서 처리
				param.put("page_id",pageVO.getPage_id());
				result = pageDao.writeXrFiled(param);
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_FIELD(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
				}					
			}
		}else{
			//첨부파일이 없으면 Exception[입력값 유효성]			
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"fileUpload"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		//=========================================		
		resultMap.put("pageCnt",pageList != null ? String.valueOf(pageList.size()) : "0" ); // [1000]2015/10/23 상품관리 추가요건
		resultMap.put("pageList",repageList); // [1000]2015/10/23 상품관리 추가요건
		//=========================================
		
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
    	// XR_DOCUMENT_HT 등록처리 ::  ACTION : UPDATE
		String action_type = Constant.ACTION_UPDATE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(), docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		
		return resultMap;
	}

	
	//[1000]
	//첨부파일 수정
	public Map<String, Object> ifActUpdatePage(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		// 입력값 유효성 체크 :: 문서ID & 페이지ID
		boolean isValidate = isValidate(map,true);
		if(!isValidate){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"doc_id,pageId"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		String doc_id = map.get("docId").toString();
		String page_id = map.get("pageId").toString();

		//해당 문서의 Read권한이 있는지확인 
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION);		
		boolean aclCheck = isExistAcl(aclList,Constant.ACL_INT_READ);
		
		//파일 첨부 권한  체크 UPDATE
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_UPDATE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);
		}		
		
		map.put("doc_id", doc_id);
		map.put("page_id", page_id);
		map.put("table_nm", Constant.DOC_TABLE);
		DocumentVO documentVO = new DocumentVO();
		documentVO = documentDao.commonDocDetail(map);	

		int result = 0;	
		
		//해당 페이지의 상세 정보 가져오기 
		PageVO pageVO = pageDao.pageDetailInfo(map);
		map.put("volume_id", pageVO.getVolume_id());
		map.put("contentPath", pageVO.getContent_path());

		//엑스랩의 파일을 삭제하기위해 xr_deletefile_queue에 등록한다.
		result = commonService.insertDeleteFileQueue(map);
		
		long delFileSize = pageVO.getPage_size();
				
		
		CaseInsensitiveMap ret = new CaseInsensitiveMap();		
		ret = pageDao.xrFileList(map);
		if (ret == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_PAGE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);			
		}

		//page_no 가져오기
		String pageNo = String.valueOf(ret.get("PAGE_NO"));
		
		//xr_filed 삭제
		result = pageDao.xrFiledDelete(map);				
		
		// 동일ID 페이지 삭제 xr_page플래그 처리
		HashMap<String,Object> param2 = new  HashMap<String, Object>();
		param2.put("is_deleted",Constant.T	);
		param2.put("page_id",page_id);	
		// XR_PAGE IS_DELETED FLAG 처리 :: 물리적 파일 삭제는 배치 프로그램에서 수행함 parameter : doc_id , is_deleted				
		pageDao.pageInfoUpdate(param2);
		
				
		//파일을 엑스랩에 등록
		List<HashMap<String,Object>> fileList = putExrepFile(files);
		long pageTotal = 0L;
        if (files != null && files.length > 0) {
            for (MultipartFile multipartFile : files) {
            	pageTotal += multipartFile.getSize();//원본 파일  전체 사이즈         
            }  
        }
        
		DocumentVO docVO = new DocumentVO();
        docVO.setInsertFileList(fileList);
        docVO.setDoc_id(doc_id);
        docVO.setPage_cnt(documentVO.getPage_cnt());//기존의 파일을 지우고 다시 하나 등록. 기존 파일의 갯수는 변화 없음
		docVO.setPage_total(documentVO.getPage_total() + pageTotal - delFileSize);
		
		List<PageVO> pageList = new ArrayList<PageVO>();				// 신규추가 파일 리스트	

		// 1.PageVO 객체 생성  :: 첨부파일 존재시 이미 exrep에 등록 됨 (신규/수정)
		List<PageVO> insertPageList =  insertPage(docVO);

		pageList.addAll(insertPageList);


		docVO.setUpdate_action(Constant.ACTION_PAGE);		
		result = documentDao.documentUpdate(docVO);
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT(UPDATE)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}


		//  DB처리 :: XR_PAGE INSERT
		result = 0;
		HashMap<String, Object> param = new HashMap<String, Object>();	
		
		
		if(pageList != null && pageList.size() > 0 )	{
			param.put("doc_id", documentVO.getDoc_id());
			for(PageVO pgVO : pageList)	{
				result = pageDao.writePage(pgVO);
				
				
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_PAGE(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
				}
				
				// xr_filed 의  페이지 ID update
				param.put("page_id",pgVO.getPage_id());
				param.put("page_no",pageNo); //기존의 pge_no를 넣어준다.
				
				result = pageDao.writeXrFiledInterFace(param);
				if(result == 0)	{	
					errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_FIELD(INSERT)"};
					throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
				}					
			}
		}else{
			//첨부파일이 존재하지 않을시에 exception[입력값 유효성]
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"fileUpload"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);	
		}
	
				
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
    	// XR_DOCUMENT_HT 등록처리 ::  ACTION : UPDATE
		String action_type = Constant.ACTION_UPDATE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(), docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		return resultMap; 
	}
	
	
	//[1000]
	//첨부파일 삭제
	public Map<String, Object> ifActDeletePage(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		DocumentDao documentDao = sqlSession.getMapper(DocumentDao.class);
		PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		// 입력값 유효성 체크 :: 문서ID & 페이지ID
		boolean isValidate = isValidate(map,true);
		if(!isValidate){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"docId,pageId"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
		}
		String doc_id = map.get("docId").toString();
		String page_id = map.get("pageId").toString();
		
		UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
		//해당 문서의 Delete권한이 있는지확인 
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION, resultVO);		
		boolean aclCheck = isExistAcl(aclList,Constant.ACL_INT_UPDATE);

		//파일 첨부 권한  체크 UPDATE
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_UPDATE};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);	
		}
		
		map.put("doc_id", doc_id);
		map.put("page_id", page_id);
		map.put("table_nm", Constant.DOC_TABLE);
		DocumentVO documentVO = new DocumentVO();
		documentVO = documentDao.commonDocDetail(map);	

		int result = 0;	
		
		//xr_filed 삭제
		result = pageDao.xrFiledDelete(map);
		//해당 페이지의 상세 정보 가져오기 
		PageVO pageVO = pageDao.pageDetailInfo(map);
		map.put("volume_id", pageVO.getVolume_id());
		map.put("contentPath", pageVO.getContent_path());
		
		//엑스랩의 파일을 삭제하기위해 xr_deletefile_queue에 등록한다.
		result = commonService.insertDeleteFileQueue(map);
		
		long delFileSize = pageVO.getPage_size();		
		
		HashMap<String,Object> param2 = new  HashMap<String, Object>();
		param2.put("is_deleted",Constant.T	);
		param2.put("page_id",page_id);	
		// XR_PAGE IS_DELETED FLAG 처리 :: 물리적 파일 삭제는 배치 프로그램에서 수행함 parameter : doc_id , is_deleted				
		pageDao.pageInfoUpdate(param2);
		
		
		DocumentVO docVO = new DocumentVO();      
        docVO.setDoc_id(doc_id);
        docVO.setPage_cnt(documentVO.getPage_cnt() - 1);// 기존 파일에서 해당 파일 하나만 삭제
		docVO.setPage_total(documentVO.getPage_total() - delFileSize);
		
		docVO.setUpdate_action(Constant.ACTION_PAGE);		
		result = documentDao.documentUpdate(docVO);
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT(UPDATE)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
		
		
    	// XR_DOCUMENT_HT 등록처리 ::  ACTION : UPDATE
		String action_type = Constant.ACTION_UPDATE;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,"", doc_id, action_type, docVO.getDoc_name(),sessionVO);
		
		return resultMap; 
	}
	
	//[1000]
	//첨부파일 다운로드
	public Map<String, Object> ifActDownloadPage(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		File toServeUp = null;
		
		PageDao pageDao = sqlSession.getMapper(PageDao.class);

		// 입력값 유효성 체크 :: 문서ID
		boolean isValidate = isValidate(map,true);
		if(!isValidate){			
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"docId,pageId"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);	
		}
		String doc_id = map.get("docId").toString();
		String page_id = map.get("pageId").toString();
		map.put("doc_id", doc_id);
		map.put("page_id", page_id);
		
		//해당유저에 대한 문서의 권한 체크
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		aclList = aclUserDocList(map, SQL_SESSION);		
		boolean aclCheck = isExistAcl(aclList,Constant.ACL_INT_READ);

		//파일 첨부 권한  체크 Read
		if(!aclCheck){
			errorCd = new String[]{ConstantInterfaceErrorCode.NOACL_ERROR_2003,Constant.ACL_READ};
			throw processException(ConstantInterfaceErrorCode.NOACL_ERROR_2003,errorCd);
		}

		PageVO ret = new PageVO();
		ret = pageDao.pageDetailInfo(map);
		
		if(ret == null)	{
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_PAGE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
		
		// [2000] Start
		CaseInsensitiveMap filedRet = new CaseInsensitiveMap();		
		filedRet = pageDao.xrFileList(map);
		if (filedRet == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"INVALID PAGE_ID"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);			
		}
		// [2000] End
    	
    	// EXREP 연계해서 파일정보 가져오기. :: eXrep C/S Client 생성.  
    	EXrepClient eXrepClient = new EXrepClient();

    	//다운로드할 폴더 설정
    	String downPath = ConfigData.getString("FILE_DOWN_PATH") + UUID.randomUUID().toString() + "/";
    	String downFile = ret.getPage_name();
    	String savePath = downPath + downFile;
		try {
			UtilFileApp.createDir(downPath);
		} catch (Exception e) {
			errorCd = new String[]{ConstantInterfaceErrorCode.CREATEDIR_ERROR_2002};
			throw processException(ConstantInterfaceErrorCode.CREATEDIR_ERROR_2002,errorCd);
		}				
			
    	try {
    		// eXrep C/S 접속.
        	eXrepClient.connect();
        				
	    	if(eXrepClient.getFile(ret.getVolume_id(), ret.getContent_path() , savePath)) {
	    		toServeUp = new File(savePath); 
	    	}
		
    	} catch (ExrepClientException e) {
			errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3003};
			throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3003,errorCd);
		}finally{
			try{
				eXrepClient.disconnect();
			}catch(Exception e){
				errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3004};
				throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3004,errorCd);
			}
		}
    	
		resultMap.put("file",toServeUp);
    	resultMap.put("downFile",downFile);
    	
    	
    	UserVO resultVO = new UserVO();
		resultVO = userGroup(map, SQL_SESSION);
		
		Map<String, Object> docresultMap = new HashMap<String, Object>();

		map.put("table_nm", Constant.DOC_TABLE);
		map.put("actionType", null);
		docresultMap = selectDoc(map, sessionVO);
		DocumentVO docVO = new DocumentVO();
		docVO = (DocumentVO)docresultMap.get("documentVO");
		// XR_DOCUMENT_HT 등록처리 ::  ACTION : READ
		String action_type = Constant.ACTION_READ;
		setSessionVO(sessionVO, resultVO);
		interfaceHistory(map,docVO.getRoot_id(), docVO.getDoc_id(), action_type, docVO.getDoc_name(),sessionVO);
		
		return resultMap;
	}
	
	//[1000]
	//입력값 유효성 체크
	public boolean isValidate(HashMap<String,Object> map, boolean pageKbn) throws Exception {
		boolean isOk = true;
		String doc_id = map.get("docId") != null ? map.get("docId").toString() : null ;
		String page_id = map.get("pageId") != null ? map.get("pageId").toString() : null ;
		String work_type = map.get("workType") != null ? map.get("workType").toString() : null ;
		String auth_id = map.get("authId") != null ? map.get("authId").toString() : null ;
		
		if(pageKbn){
			if(StringUtil.isEmpty(doc_id) || StringUtil.isEmpty(work_type) || StringUtil.isEmpty(auth_id) || StringUtil.isEmpty(page_id) ) {
				isOk = false;
			}
		}else{
			if(StringUtil.isEmpty(doc_id) || StringUtil.isEmpty(work_type) || StringUtil.isEmpty(auth_id) ) {
				isOk = false;
			}
		}		
		return isOk;
	}
	
	//[1000]
	//해당 유저의 문서에 대한 권한을 가져온다.
	public List<CaseInsensitiveMap> aclUserDocList (HashMap<String,Object> map, boolean isSqlBatch) throws Exception {
		return aclUserDocList(map, isSqlBatch, null);
	}
	
	public List<CaseInsensitiveMap> aclUserDocList (HashMap<String,Object> map, boolean isSqlBatch, UserVO userVO) throws Exception {
		List<CaseInsensitiveMap> aclList = new ArrayList<CaseInsensitiveMap>();
		
		AclDao aclDao;
		if(isSqlBatch){
			aclDao = sqlSessionBatch.getMapper(AclDao.class);
		}else{
			aclDao = sqlSession.getMapper(AclDao.class);
		}
		
		UserVO resultVO;
		if(userVO != null){
			resultVO = userVO;
		}else{
			// 사용자 그룹 찾기
			resultVO = new UserVO();
			resultVO = userGroup(map, isSqlBatch);
			// 사용자 정보 체크
			if (resultVO == null)	{
				errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESSUSER_ERROR_1001};
				throw processException(ConstantInterfaceErrorCode.DBACCESSUSER_ERROR_1001,errorCd);
			}
		}

		// 그룹+프로젝트 그룹 ID			
		List<String> dualGroup = dualGroupList(map, isSqlBatch);
		dualGroup.add(resultVO.getGroup_id());

		map.put("group_id_list",dualGroup);
		map.put("user_id",map.get("authId"));
		List<String> doc_idList = new ArrayList<String>();
		doc_idList.add(map.get("docId").toString());
		map.put("doc_idList", doc_idList);

		//해당유저에 대한 문서의 권한
		aclList = aclDao.alcListByDocumentIDs(map);
		
		return aclList;		
	}
	
	//[1000]
	//첨부파일 엑스랩 등록
	public List<HashMap<String,Object>> putExrepFile(MultipartFile[] files) throws Exception {
		// 파일을 엑스랩에 등록
		EXrepClient eXrepClient = new EXrepClient();	// eXrep C/S Client 생성. 
	
		String contentPath = "";
		long fileSize = 0L;
		List<HashMap<String,Object>> fileList =  new ArrayList<HashMap<String,Object>>();
		if (files != null && files.length > 0) {
			
			for (MultipartFile multipartFile : files) {
				HashMap<String, Object> fileparam = new HashMap<String, Object>();
				fileSize = multipartFile.getSize();//원본 파일 개별 사이즈
				
				contentPath = CommonUtil.getContentPathByDate(ConfigData.getString(Constant.EXREP_ROOT_EDMS_NM))+UUID.randomUUID().toString();

				fileparam.put("orgFile",multipartFile.getOriginalFilename());
				fileparam.put("contentPath",contentPath);
				fileparam.put("fileSize",multipartFile.getSize());
				fileparam.put("volumeId",ConfigData.getString(Constant.EXREP_VOLUME_NM));

				try {
					eXrepClient.connect();

					if(!eXrepClient.putFile(new SizedInputStream(multipartFile.getInputStream(), fileSize),ConfigData.getString(Constant.EXREP_VOLUME_NM), contentPath,true)) {										
						errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3001};
						throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3001,errorCd);
					}
				} catch (ExrepClientException e) {
					logger.debug(StringUtil.getErrorTrace(e));
					errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3003};
					throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3003,errorCd);
				}

				fileList.add(fileparam);					
			}

			try{
				eXrepClient.disconnect();
			}catch(Exception e){
				logger.debug(StringUtil.getErrorTrace(e));	
				errorCd = new String[]{ConstantInterfaceErrorCode.STORAGE_ERROR_3004};
				throw processException(ConstantInterfaceErrorCode.STORAGE_ERROR_3004,errorCd);
			}	
		}
		return fileList ; 
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 문서 등록/수정시 신규 첨부파일
	 * 2. 처리내용 : 물리적 파일은 이미 exRep ECM에 등록되어 있음.
	 * </pre>
	 * @Method Name : insertPage
	 * @param documentVO
	 * @return
	 * @throws Exception List<PageVO>
	 */
	public List<PageVO> insertPage(DocumentVO documentVO) throws Exception {
		
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
		pageVO.setVolume_id(pageInfo.get("volumeId").toString()); 
		pageVO.setContent_path(pageInfo.get("contentPath").toString());
	
		return pageVO;
	}
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 
	 * 2. 처리내용 : 요청폴더 생성
	 * </pre>
	 * @Method Name : checkRequestFolder
	 * @param parentFid
	 * @param map
	 * @param folderDao
	 * @return
	 * @throws Exception 
	 */
	public String checkRequestFolder(String parentFid,  HashMap<String,Object> map, FolderDao folderDao,AclDao aclDao) throws Exception {
		String result = "";
		//요청폴더 존재 여부 체크
		String[] extfolder = map.get("folderPath").toString() != null ?  map.get("folderPath").toString().split("/") : null ;

		FolderVO folderVO = new FolderVO();
		HashMap<String, Object> param = new HashMap<String, Object>();	
		String parentFolderID= parentFid;
		int folder_id=0;
		if(extfolder != null ) {
			for(String folder_name : extfolder) {
				if( StringUtil.isEmpty(folder_name) ) continue;
				
				param.put("parent_id",parentFolderID);
				param.put("folder_name_ko",folder_name);

				// 동일한 부모 폴더 아래 동일 폴더명이 존재하는 검사
				folderVO = folderDao.folderDetail(param);
				
				// 동일한 폴더명이 존재하는 경우
				if( folderVO != null) {
					if(!folderVO.getParent_id().equals(folderVO.getFolder_id()) && !folderVO.getParent_id().equals("") ) {
						parentFolderID = folderVO.getFolder_id().toString();
						//result = 1;
						result = folderVO.getFolder_id().toString();
						continue;
					}
					
				}else{
					param.clear();
					param.put("folder_id",parentFolderID);
					folderVO = folderDao.folderDetail(param);
					
					FolderVO folderVO2 = new FolderVO();
					
					folderVO2.setMap_id(folderVO.getMap_id().toString());
					folderVO2.setAcl_id(folderVO.getAcl_id().toString());
					
					folder_id = commonService.commonNextVal(Constant.COUNTER_ID_FILE);
					folderVO2.setFolder_id(CommonUtil.getStringID(Constant.ID_PREFIX_FOLDER, folder_id));
					
					//folderVO2.setCreator_id(map.get("authId").toString()); //사용자 ID
					folderVO2.setCreator_id(Constant.SYSTEM_ACCOUNT); //관리자로 등록 [1007]
					
					folderVO2.setStorage_usage(0L);
					
					folderVO2.setParent_id(folderVO.getFolder_id().toString());
					folderVO2.setFolder_name_ko(folder_name);
					folderVO2.setIs_inherit_acl(folderVO.getIs_inherit_acl());
					
					folderVO2.setFolder_type(
						StringUtil.isEmpty(folderVO.getFolder_type()) ? Constant.FOLDER_TYPE_DOCUMENT : folderVO.getFolder_type()
					);
					folderDao.folderWrite(folderVO2);	
					
					parentFolderID = folderVO2.getFolder_id().toString();
					
					result = folderVO2.getFolder_id().toString();
				}
			}
		}
		
		
		return result;
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
			if(result == 0)	{	
				errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_LINKED(INSERT)"};
				throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
			}	
		}
		
	}
		
	
	
	
	
////////////////////////////////////////////////////////////////////////	
	
	
	
	
		
		
		
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 프로젝트/겸직 대상 부서 목록 가져오기
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : dualGroupList
	 * @param map
	 * @return
	 * @throws Exception List<String>
	 */
	public List<String> dualGroupList(HashMap<String,Object> map, boolean isSqlBatch) throws Exception{

		List<String> ret = new ArrayList<String>();
		GroupDao groupDao;
		if(isSqlBatch){
			groupDao = sqlSessionBatch.getMapper(GroupDao.class);
		}else{
			groupDao = sqlSession.getMapper(GroupDao.class);
		}

		HashMap<String,Object> map2 = new HashMap<String,Object>();		
		map2.put("is_default",Constant.F);
		map2.put("map_id",Constant.MAP_ID_PROJECT);
		map2.put("user_id",map.get("authId"));

		ret = groupDao.dualGroupList(map);

		return ret;
	}
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 해당유저의 소속 그룹 가져오기
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : userGroup
	 * @param map
	 * @return UserVO
	 * @throws Exception List<String>
	 */
	public UserVO userGroup(HashMap<String,Object> map, boolean isSqlBatch) throws Exception {

		UserVO resultVO = new UserVO();
		UserDao userDao;
		if(isSqlBatch){
			userDao = sqlSessionBatch.getMapper(UserDao.class);
		}else{
			userDao = sqlSession.getMapper(UserDao.class);
		}

		// 사용자 소속 그룹 가져오기
		UserVO userVO= new UserVO();
		String authId = map.get("authId") != null ? map.get("authId").toString() : null ;
		
		userVO.setEmp_no(authId);
		resultVO = userDao.userLogin(userVO);

		return resultVO;
	}
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 해당유저의 소속 그룹 가져오기
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : userGroup
	 * @param map
	 * @return UserVO
	 * @throws Exception List<String>
	 */
	public UserVO existUser(String userId) throws Exception {

		UserVO resultVO = new UserVO();
		UserDao userDao = sqlSession.getMapper(UserDao.class);
		
		// 사용자 소속 그룹 가져오기
		UserVO userVO= new UserVO();		
		
		userVO.setEmp_no(userId);
		resultVO = userDao.userLogin(userVO);

		return resultVO;
	}
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 해당문서의 권한 가져오기
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : isExistAcl
	 * @param aclList , aclLevel
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isExistAcl(List<CaseInsensitiveMap> aclList , int aclLevel) throws Exception {
		boolean reAcl = false;
		int acl = 0;
		for(CaseInsensitiveMap caseMap : aclList) {

			if(caseMap.get("ACT_DELETE").toString().equals(Constant.T)) {
				acl = Constant.ACL_INT_DELETE;
			} else if(caseMap.get("ACT_UPDATE").toString().equals(Constant.T)) {
				acl = Constant.ACL_INT_UPDATE;
			} else if(caseMap.get("ACT_READ").toString().equals(Constant.T)) {
				acl = Constant.ACL_INT_READ;
			} else if(caseMap.get("ACT_BROWSE").toString().equals(Constant.T)) {
				acl = Constant.ACL_INT_BROWSE;
			} else {
				acl = Constant.ACL_INT_NONE;
			}
		}
		
		reAcl  = (acl >= aclLevel);

		return reAcl;
		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 폴더내 문서등록 권한 여부 체크
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : isFolderDocCreateAuth
	 * @param map
	 * @param userVO
	 * @param docPart
	 * @param folderDao
	 * @param aclDao
	 * @return
	 * @throws Exception boolean
	 */
	public boolean isFolderDocCreateAuth(HashMap<String, Object> map,UserVO userVO, String docPart,FolderDao folderDao,AclDao aclDao) 
			throws Exception {
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		boolean isAuth = false;
		
		if(docPart != null && !docPart.equals(""))	{
			// 권한관리 사용자(ALL/GROUP/TEAM)
			if(docPart.equals(Constant.MENU_ALL))	{
				isAuth = true;		// 전체권한인 경우
			} else if(docPart.equals(Constant.MENU_GROUP) || docPart.equals(Constant.MENU_TEAM))	{
				// 관리대상 폴더에 속하는지 체크한다. 
				isAuth = cacheService.menuAuthByFolderID((String)map.get("folder_id"), userVO.getManage_group());
			} 			
		}else {
			// 일반사용자
			param.put("user_id",map.get("authId"));
			param.put("folder_id",map.get("folder_id"));
			param.put("is_type",Constant.D);
			
			// 그룹+프로젝트 그룹 ID			
			List<String> dualGroup = dualGroupList(map, SQL_SESSION);
			dualGroup.add(userVO.getGroup_id());
			param.put("group_id_list",dualGroup);
			
			AclItemVO aclItemVO = aclDao.isAuthCheck(param);
			
			if(aclItemVO != null && aclItemVO.getAct_create().equals(Constant.T)) {
				isAuth = true;
			}
			
		}
		
		return isAuth;
	}
	
	
	// [1000]
	// 인터페이스 히스토리
	public void interfaceHistory(HashMap<String, Object> map, String root_id, String doc_id, String action_type, String doc_name, SessionVO sessionVO) throws Exception {
		HistoryDao historyDao = sqlSession.getMapper(HistoryDao.class);
			
		long doc_seq = commonService.commonNextVal(Constant.COUNTER_ID_DOCUMENT_HT);	
		
		int result = 0;
		String type_id = Constant.DOC_TABLE;
		String version_no = Constant.DEFAULT_VERSION_NO;
		
		
		if(StringUtil.isEmpty(root_id))	{
			root_id = doc_id;
		}
		
		CaseInsensitiveMap ret = new CaseInsensitiveMap();	
		ret = interfaceCodeValue(map);	
		if (ret == null){
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_INTERFACE_CODE(SELECT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
		String actionPlace = "";			
		
		//히스토리 액션
		actionPlace = "["+ String.valueOf(ret.get("WORK_CODE")) +"]" + ret.get("WORK_DESCRIPTION").toString();
		
		
		DocumentHtVO documentHtVO = CommonUtil.setDocumentHistory(doc_seq, root_id, doc_id, action_type, type_id, doc_name,version_no,sessionVO,true,actionPlace);
		result = historyDao.documentHtWrite(documentHtVO);			
		
		if(result == 0)	{	
			errorCd = new String[]{ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,"XR_DOCUMENT_HT(INSERT)"};
			throw processException(ConstantInterfaceErrorCode.DBACCESS_ERROR_1002,errorCd);
		}
	
	}
	// Start... 외부연계 부서파트 관련
	// [1000]
	@Override
	public Map<String, Object> interfaceTypeGroup(HashMap<String, Object> map, SessionVO sessionVO)
			throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap = interfaceGroupList(map, sessionVO);
		
		return resultMap;
	}
	
	// [1000]
	public Map<String, Object> interfaceGroupList(HashMap<String,Object> map, SessionVO sessionVO) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		GroupDao groupDao = sqlSession.getMapper(GroupDao.class);
		
		String action = map.get("action")!= null ? map.get("action").toString() : "";
		String groupId = map.get("groupId")!= null ? map.get("groupId").toString() : "";

		if(StringUtil.isEmpty(action)){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"action"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		if(StringUtil.isEmpty(groupId)){
			errorCd = new String[]{ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,"groupId"};
			throw processException(ConstantInterfaceErrorCode.REQUIRED_ERROR_2001,errorCd);
		}
		
		GroupVO groupVO = new GroupVO();
		groupVO = groupDao.managegroup(groupId);
		
		HashMap<String,Object> group = new HashMap<String,Object>();		
		List<HashMap<String,Object>> groupList = new ArrayList<HashMap<String,Object>>();
		if(groupVO != null){
			
			group = new HashMap<String,Object>();
			group.put("partId",groupVO.getManage_group_id());
			group.put("partName",groupVO.getManage_group_name());
			groupList.add(group);
		}
		
		resultMap.put("groupPartList ", groupList);
		return resultMap;
	}		
	// End Start... 외부연계 부서파트 관련

	//[1000]
	@Override
	public SessionVO interfaceSessionVO(HttpServletRequest request, String user_id) throws Exception {
		// TODO : sessionVO set 
		SessionVO sessionVO = new SessionVO();
		sessionVO.setSessId(user_id);
//		sessionVO.setSessName();
//		sessionVO.setSessGroup_id();
//		sessionVO.setSessGroup_nm();
		sessionVO.setSessRemoteIp(request.getRemoteAddr());
		
		return sessionVO;
	}
	
	public void setSessionVO(SessionVO sessionVO, UserVO userVO) throws Exception {
		sessionVO.setSessName(userVO.getUser_name_ko());
		sessionVO.setSessGroup_id(userVO.getGroup_id());
		sessionVO.setSessGroup_nm(userVO.getGroup_nm());
	}
	//[1001]
	@Override
	public Map<String, Object> externalPageList(HashMap<String, Object> map) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<CaseInsensitiveMap> ret = new ArrayList<CaseInsensitiveMap>();
		int total = 0;
		
		ExternalDao externalDao = sqlSession.getMapper(ExternalDao.class);
		
		total = externalDao.externalPagingCount(map);
		ret = externalDao.externalPagingList(map);
		
		resultMap.put("page",map.get("nPage").toString());
		resultMap.put("records",total);
		resultMap.put("total",CommonUtil.getTotPageSize(total,Integer.parseInt(map.get("page_size").toString())));	
		resultMap.put("list",ret);
		
		// Ajax Paging 
		String strLink = "javascript:exsoftAdminExternalFunc.event.gridPage";
		String contextRoot = map.get("contextRoot") != null ? map.get("contextRoot").toString() : "";
		PagingAjaxUtil pagingInfo = new PagingAjaxUtil(Integer.parseInt(map.get("nPage").toString()),total,Integer.parseInt(map.get("page_size").toString()),10,strLink,contextRoot);		
		resultMap.put("pagingInfo",pagingInfo);		
		
		return resultMap;
	}
	//[1001]
	@Override
	public Map<String, Object> externalDetailList(HashMap<String, Object> map) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		CaseInsensitiveMap ret = new CaseInsensitiveMap();

		ExternalDao externalDao = sqlSession.getMapper(ExternalDao.class);
		ret = externalDao.externalDetailList(map);

		resultMap.put("list",ret);
		return resultMap;
	}
	//[1001]
	@Override
	public Map<String, Object> externalManagerUpdate(HashMap<String,Object> map, SessionVO sessionVO, String kbn) throws Exception {
	
		ExternalDao externalDao = sqlSession.getMapper(ExternalDao.class);		
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int result = 0;		
				
		// 1.XR_INTERFACE_CODE 수정
		if(!kbn.equals(Constant.DELETE)){
			param.put("work_code",map.get("work_code").toString().toUpperCase());			
			param.put("work_description",map.get("work_description").toString().toUpperCase());
			param.put("folder_id",map.get("folder_id").toString());
		}
		
		if(kbn.equals(Constant.UPDATE)){
			result = externalDao.interfaceUpdate(param);			
		}else if(kbn.equals(Constant.INSERT)){
			result = externalDao.interfaceInsert(param);		
		}else{
			List<String> ret = new ArrayList<String>();
			String workCodeList =  map.get("WorkCodeList") != null ? map.get("WorkCodeList").toString() : "";
			
			// 1.입력값 유효성 체크
			if(workCodeList.equals("") ||  workCodeList.equals(""))	{
				throw processException("common.required.error");
			}
			
			JSONArray jsonArray = JSONArray.fromObject(workCodeList);
			if(jsonArray.size() > 0 ) {		
				 for(int j=0;j < jsonArray.size();j++)	{				 
					 ret.add(jsonArray.getJSONObject(j).getString("work_code").toString());
				 }
			}
			
			for(String work_code : ret ){
				param.put("work_code", work_code);
				result = externalDao.interfaceDelete(param);
				
			}
			
		}
		
		if(result == 0)	{	throw processException("common.system.error");	}
		
		resultMap.put("result",Constant.RESULT_TRUE);
				
		return resultMap;
		
	}

	
}
