package kr.co.exsoft.external.dao;

import java.util.HashMap;
import java.util.List;
import kr.co.exsoft.quartz.vo.SyncGroupVO;
import kr.co.exsoft.quartz.vo.SyncUserVO;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.springframework.stereotype.Repository;

/**
 * External 매퍼클래스
 * @author 패키지 개발팀
 * @since 2014.07.21
 * @version 3.0
 * 
 * @comment
 * [2000][EDMS-REQ-036]	2015-09-07	이재민 : 조직도연계처리
 */
@Repository(value = "externalDao")
public interface ExternalDao {

	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부 사용자 정보 조회 Sample
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalUserDetail
	 * @param map
	 * @return
	 */
	public CaseInsensitiveMap externalUserDetail(HashMap<String,Object> map);

	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부 사용자 등록 처리 Sample
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalUserWrite
	 * @param map
	 * @return
	 */
	public int externalUserWrite(HashMap<String,Object> map);
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부 사용자 소속부서 등록 처리 Sample
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalGroupedWrite
	 * @param map
	 * @return
	 */
	public int externalGroupedWrite(HashMap<String,Object> map);
	
	
	/**
	 * [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개요 : 외부연계 파일 EDMS 저장 위치 
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalEdmsSavePath
	 * @param String
	 * @return String
	 * @throws Exception
	 */
	public String externalFolderPath(String work_code);

	/**
	 * [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개요 : 외부연계 파일 EDMS 저장 위치 
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalEdmsSavePath
	 * @param String
	 * @return String
	 * @throws Exception
	 */
	public CaseInsensitiveMap externalInterface(HashMap<String,Object> map);

	/**
	 * [1001][EDMS-REQ-070~81]	20150914	최은옥
	 * <pre>
	 * 1. 개요 : 요청부서 존재 유무 처리
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : chkDeptCnt
	 * @param map
	 * @return
	 */
	public int chkDeptCnt(HashMap<String,Object> map);
	/**
	 * [1001][EDMS-REQ-070~81]	20150914	최은옥
	 * <pre>
	 * 1. 개용 : 문서유형 목록 Count
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : typePagingCount
	 * @param map
	 * @return int
	 */
	public int externalPagingCount(HashMap<String,Object> map);

	/**
	 * [1001][EDMS-REQ-070~81]	20150914	최은옥
	 * <pre>
	 * 1. 개용 : 연계코드 페이지 리스트
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalPagingList
	 * @param map
	 * @return List<CaseInsensitiveMap>
	 */
	public List<CaseInsensitiveMap> externalPagingList(HashMap<String,Object> map);
	/**
	 * [1001][EDMS-REQ-070~81]	20150915	최은옥
	 * <pre>
	 * 1. 개용 : 연계코드 상세 리스트
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalDetailList
	 * @param map
	 * @return CaseInsensitiveMap
	 */
	public CaseInsensitiveMap externalDetailList(HashMap<String,Object> map);
	/**
	 * [1001][EDMS-REQ-070~81]	20150915	최은옥
	 * <pre>
	 * 1. 개용 : 연계코드 업데이트
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : interfaceUpdate
	 * @param map
	 * @return int
	 */
	public int interfaceUpdate(HashMap<String,Object> map);
	/**
	 * [1001][EDMS-REQ-070~81]	20150915	최은옥
	 * <pre>
	 * 1. 개용 : 연계코드 insert
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalManagerWrite
	 * @param map
	 * @return int
	 */
	public int interfaceInsert(HashMap<String,Object> map);
	/**
	 * [1001][EDMS-REQ-070~81]	20150915	최은옥
	 * <pre>
	 * 1. 개용 : 연계코드 insert
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalManagerWrite
	 * @param map
	 * @return int
	 */
	public int interfaceDelete(HashMap<String,Object> map);
	
	/***
	 * 
	 * <pre>
	 * 1. 개용 : 그룹(부서)연계 동기화 목록 얻기
	 * 2. 처리내용 : [2000]
	 * </pre>
	 * @Method Name : syncGroup
	 * @return List<SyncGroupVO>
	 */
	public List<SyncGroupVO> syncGroup();
	
	/***
	 * 
	 * <pre>
	 * 1. 개용 : 사용자연계 동기화 목록 얻기
	 * 2. 처리내용 : [2000]
	 * </pre>
	 * @Method Name : syncUser
	 * @return List<SyncUserVO>
	 */
	public List<SyncUserVO> syncUser();
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 10일이 지난 data 삭제하기
	 * 2. 처리내용 : [2000]
	 * </pre>
	 * @Method Name : deleteExpiredSyncData
	 * @param map
	 * @return int
	 */
	public int deleteExpiredSyncData(HashMap<String,Object> map);
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : EAI에서 넘어오는 ROOT의 parent_id가 null이라서 dept_cd를 parent_id로 넣어준다
	 * 2. 처리내용 : [2000]
	 * </pre>
	 * @Method Name : updateSynGroupRootParentId
	 * @return int
	 */
	public int updateSynGroupRootParentId();
}
