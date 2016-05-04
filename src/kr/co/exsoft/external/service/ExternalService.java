package kr.co.exsoft.external.service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.vo.AttrVO;
import kr.co.exsoft.document.vo.TypeVO;



/**
 * External 서비스 인터페이스
 * @author 패키지 개발팀
 * @since 2014.07.17
 * @version 3.0
 *
 */
@Transactional
public interface ExternalService {

	/**
	 * <pre>
	 * 1. 개요 : 외부연계 샘플 조회 처리
	 * 2. 처리내용 : 
	 * - 사용자ID를 가지고 사용자 정보 조회 
	 * </pre>
	 * @Method Name : externalUserDetail
	 * @param map
	 * @return CaseInsensitiveMap
	 * @throws Exception
	 */
	public CaseInsensitiveMap externalUserDetail(HashMap<String,Object> map) throws Exception ;
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부사용자 등록 처리 샘플
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalUserWrite
	 * @param map
	 * @return int
	 * @throws Exception
	 */
	public int externalUserWrite(HashMap<String,Object> map) throws Exception ;
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부사용자 등록 Transaction 처리 샘플
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : externalUserWriteTx
	 * @param map
	 * @return int
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor ={ Exception.class,SQLException.class})
	public int externalUserWriteTx(HashMap<String,Object> map) throws Exception ;
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 외부사용자-부서 베치 등록 처리 샘플
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : batchUserWrite
	 * @param userList
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor ={ Exception.class,SQLException.class})
	public void batchUserWrite(List<HashMap<String,Object>> userList) throws Exception ;
	
	/**
	 *  [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개용 : 외부연계 문서관련 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : interfaceTypeDocument
	 * @param map
	 * @param files
	 * @param sessionVO
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	public Map<String, Object> interfaceTypeDocument(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception;
	
	/**
	 *  [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개용 : 외부연계 파일관련 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : interfaceTypePage
	 * @param map
	 * @param files
	 * @param sessionVO
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	public Map<String, Object> interfaceTypePage(HashMap<String,Object> map, MultipartFile[] files, SessionVO sessionVO) throws Exception;	
	
	/**
	 *  [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개용 : 외부연계 파일관련 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : interfaceTypePage
	 * @param map
	 * @param sessionVO
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	public Map<String, Object> interfaceTypeGroup(HashMap<String,Object> map, SessionVO sessionVO) throws Exception;
	
	/**
	 *  [1001][EDMS-REQ-070~81]	20150824	최은옥
	 * <pre>
	 * 1. 개용 : 외부연계 session 정보 생성
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : interfaceSessionVO
	 * @param request
	 * @param user_id
	 * @return SessionVO
	 * @throws Exception
	 */
	public SessionVO interfaceSessionVO(HttpServletRequest request, String user_id) throws Exception;

	/**
	 * [1001][EDMS-REQ-070~81]	20150914	최은옥
	 * <pre>
	 * 1. 개요 : 외부연계 코드조회처리
	 * 2. 처리내용 : 
	 * - 연계코드를  가지고 연계코드 테이블 정보 조회 
	 * </pre>
	 * @Method Name : externalPageList
	 * @param map
	 * @return CaseInsensitiveMap
	 * @throws Exception
	 */
	public Map<String, Object> externalPageList(HashMap<String,Object> map) throws Exception ;
	/**
	 * [1001][EDMS-REQ-070~81]	20150914	최은옥
	 * <pre>
	 * 1. 개요 : 외부연계 코드상세조회처리
	 * 2. 처리내용 : 
	 * - 사용자ID를 가지고 사용자 정보 조회 
	 * </pre>
	 * @Method Name : externalUserDetail
	 * @param map
	 * @return CaseInsensitiveMap
	 * @throws Exception
	 */
	public Map<String, Object> externalDetailList(HashMap<String,Object> map) throws Exception ;
	/**
	 * [1001][EDMS-REQ-070~81]	20150915	최은옥
	 * <pre>
	 * 1. 개요 : 외부연계 코드인서트,업데이트처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalManagerUpdate
	 * @param map
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> externalManagerUpdate(HashMap<String,Object> map,SessionVO sessionVO, String kbn)throws Exception ;
}
