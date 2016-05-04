package kr.co.exsoft.eframework.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.common.dao.ConfDao;
import kr.co.exsoft.common.vo.ConfVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.eframework.library.LocaleLibrary;
import kr.co.exsoft.eframework.util.ConfigData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;




// 로그인 강제차단처리
import java.io.Reader;
import java.util.HashMap;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import kr.co.exsoft.user.dao.UserDao;
import kr.co.exsoft.user.vo.LoginLogVO;

/**
 * 로그인 체크 Interceptor 클래스
 * @author 패키지 개발팀
 * @since 2014.07.15
 * @version 3.0
 *
 * [0000][기능변경]	2015-08-28	박상진	 : sqlSession 별도 생성 없이 bean 객체 이용으로 변경
 * [2000][EDMS-REQ-036]		2015-08-31	이재민 : 강제로그아웃처리
 * 
 */
@Service
public class LoginCheckInterceptor extends HandlerInterceptorAdapter {

	protected static final Log logger = LogFactory.getLog(LoginCheckInterceptor.class);
		
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;	// [0000]
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		logger.info("LoginCheckInterceptor preHandle");
		
		boolean ret = true;
				
		try {
			
			HttpSession session = request.getSession();
			
			// 0,관리자 강제 접속차단 처리체크(XR_LOING_LOG) --- 사용자/관리자 공통
			SessionVO chkVO= (SessionVO)session.getAttribute("sessionVO");
			
			// [0000] start... 기존 소스 주석
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
			// [0000] end... 기존 소스 주석
			
			UserDao userDao = sqlSession.getMapper(UserDao.class);
			ConfDao confDao = sqlSession.getMapper(ConfDao.class);
			
			HashMap<String,Object> map = new HashMap<String,Object>(); 	
			map.put("user_id",chkVO.getSessId());
			// [2000] Start
			// 강제로그아웃설정 조회
			List<ConfVO> confList =  new ArrayList<ConfVO>();
			map.put("skey", Constant.SYS_TYPE_FORCE_LOGOUT);
			confList = confDao.sysConfigDetail(map);
			
			if("N".equals(confList.get(0).getSval())) {
				//세션아이디세팅
				map.put("session_id",chkVO.getSessionId());
			}
			// [2000] End
			LoginLogVO loginLogVO = userDao.loginLogDetail(map);
			if(loginLogVO != null) {
				logger.info("sessionBean exist");
			}else {
				if( !isAjax(request))	{
					response.sendRedirect("/loginFrm.do");
					return false;
				}else {
					reponseAjaxMessage(response,"common.session.error");		
					return false;
				}
			}
	
			// 1.세션체크 
			if(session.getAttribute("sessionVO") == null) {
				
				logger.info("sessionBean not exist");
				
				if( !isAjax(request))	{
					response.sendRedirect("/login.do");
					return false;
				}else {
					reponseAjaxMessage(response,"common.session.error");
					return false;
				}

			}			
			
			// 2.관리자/사용자 페이지 접속 구분에 따른 권한 체크(통계페이지 사용자로 변경처리 적용)
			if(request.getRequestURL().indexOf("/admin/") > -1 ) {
				
				// 2.1 ROLE_ID 체크
				SessionVO sessionVO = (SessionVO)session.getAttribute("sessionVO");

				// 2.2 관리자 ROLE 체크
				if(sessionVO.getSessRole_id() != null && sessionVO.getSessRole_id().equals(Constant.USER_ROLE)) {					
					if( !isAjax(request))	{
						response.sendRedirect("/common/errorMessage.do?code=common.connect.error");		
						return false;
					}else {
						reponseAjaxMessage(response,"common.connect.error");
						return false;
					}
				}				
						
			}
			

		}catch(Exception e) {
			logger.debug(e.getMessage());			
		}

		return ret;
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : Ajax Session 
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : isAjax
	 * @param request
	 * @return boolean
	 */
	public boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

	/**
	 * 
	 * <pre>
	 * 1. 개요 : Ajax 에러메세지 출력
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : reponseAjaxMessage
	 * @param response
	 * @param ExtSuccessMessage
	 * @throws IOException
	 */
	protected void reponseAjaxMessage(HttpServletResponse response,String ExtSuccessMessage) throws IOException{
		
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE"));
		
		String errorMsg = "";
		
		try{
			
			response.setContentType("text/xml");
			response.setCharacterEncoding("utf-8");
			java.io.PrintWriter out = response.getWriter();
		
			errorMsg =  messageSource.getMessage("common.session.error",new Object[0],locale);				

			out.print("<RESPONSES>");
			out.print("<RESPONSE>");
			out.print("<RESULT>" + ExtSuccessMessage + "</RESULT>");
			out.print("<MESSAGE><![CDATA[" +	errorMsg + "]]></MESSAGE>");
			out.print("</RESPONSE>");
			out.print("</RESPONSES>");
			out.flush();
			out.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
