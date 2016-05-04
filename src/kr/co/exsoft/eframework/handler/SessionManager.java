package kr.co.exsoft.eframework.handler;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import kr.co.exsoft.user.dao.UserDao;
import kr.co.exsoft.user.vo.LoginLogVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.util.StringUtil;

/***
 * 세션 생성/소멸 리스너
 * @author 패키지 개발팀
 * @since 2014.07.15
 * @version 3.0
 *
 * [0000][기능변경]	2015-08-28	박상진	 : sqlSession bean 객체 생성 안됨. 기존 읽기 방식 싱글톤 패턴 적용
 *
 */
public class SessionManager implements HttpSessionBindingListener {

	protected static final Log logger = LogFactory.getLog(SessionManager.class);
	
	private static List<HttpSession> sessionList;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA);
	
	public SessionManager() {
		sessionList = new ArrayList<HttpSession>();
	}
	
	@Override
	public void valueBound(HttpSessionBindingEvent arg0) {

		// 세션에 값이 등록될때 호출
		
		// 세션목록에 추가
		HttpSession session = arg0.getSession();
		
		
		sessionList.add(session);
		
		String creationTime = sdf.format(new Date(session.getCreationTime()));
		
		logger.info("HttpSession is created.");
		logger.info("Session ID: " + session.getId());
		logger.info("Creation Time: " + creationTime);
		logger.info("Timeout: " + session.getMaxInactiveInterval() + " seconds");

	}
	
	@Override
	public void valueUnbound(HttpSessionBindingEvent arg0) {
		
		// 세션에 값이 지워질때 (무효화시) 호출
		logger.info("SessionManager : session destroy");
		
		// 세션종료전에 XR_LOGIN_LOG 테이블 정보 삭제.
		HttpSession session = arg0.getSession();
		
		if(session != null) {
			
		    // 세션 목록에서 제거.
			sessionList.remove(session);
					
			// [start] end... 기존 소스 주석
//			SqlSessionFactory sqlSessionFactory = null;
//			
//			try {
//				Reader reader = Resources.getResourceAsReader("../config/mybatis-application.xml");
//				sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
//				reader.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			SqlSession sqlSession = sqlSessionFactory.openSession(true);
			SqlSessionFactory sqlSessionFactory = SingletonSqlFactory.getInstance();
			SqlSession sqlSession = sqlSessionFactory.openSession(true);
			// [0000] end... 기존 소스 주석

			UserDao userDao = sqlSession.getMapper(UserDao.class);
			
			HashMap<String,Object> map = new HashMap<String,Object>(); 
			map.put("session_id",session.getId());
			LoginLogVO loginLogVO = userDao.loginLogDetail(map);
			
			//로그인로거가 존재하고 외부시스템이 아닐때 xr_login_log 삭제
			String extCheck = StringUtil.isNull((String)session.getAttribute("externalCheck")) ? Constant.F : (String)session.getAttribute("externalCheck") ;//외부연계 체크 = 외부연계:true, EDMS:false
			if(loginLogVO != null && !extCheck.equals("T") ) {
				userDao.loginLogDelete(map);
			}
		
		}
		
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 생성된 세션 사용자 ID 목록을 구한다.
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : getSessionUserIdList
	 * @return
	 */
	public static List<String> getSessionUserIdList() {
		 
		 List<String> user_id_list = new ArrayList<String>();
		 
		 try {
			 
			 for (HttpSession currentSession : sessionList) {
				 
				 SessionVO sessionVO = (SessionVO)currentSession.getAttribute("sessionVO");
				
				 if(sessionVO != null && !user_id_list.contains(sessionVO.getSessId())) {
					 user_id_list.add(sessionVO.getSessId());
				 }
			 }
		 }catch(Exception e)	{
			 logger.error(e.getMessage());
		 }
		 
		 return user_id_list;
	 }
}
