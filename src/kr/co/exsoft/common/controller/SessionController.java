package kr.co.exsoft.common.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import kr.co.exsoft.eframework.exception.BizException;
import kr.co.exsoft.eframework.library.LocaleLibrary;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.util.UserCookie;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.util.ARIAUtil;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.common.service.SessionService;
import kr.co.exsoft.user.service.UserService;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.user.vo.UserVO;

/**
 * 세션처리 컨트롤러
 * @author 패키지 개발팀
 * @since 2014.08.01
 * @version 3.0
 *
 */
@Controller
@SessionAttributes("sessionVO")
public class SessionController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private MessageSource messageSource;
	
	
	protected static final Log logger = LogFactory.getLog(SessionController.class);

	/**
	 * 로그인 폼 - 관리자 강제접속 종료처리 후
	 * @param model
	 * @param request
	 * @return String
	 */
	@RequestMapping("/loginFrm.do")
	public String loginFrm(Model model, HttpServletRequest request) {
		
		model.addAttribute("expire",ConfigData.getString("COOKIE.EXPIRE"));
		model.addAttribute("emp_no",UserCookie.getUserCookie(request, "emp_no"));		// GET Cookie
		
		return "layout/loginForm";
	}
	
	
	/**
	 * 로그인 폼
	 * @param model
	 * @param request
	 * @return String
	 */
	@RequestMapping("/login.do")
	public String view(Model model, HttpServletRequest request) {
		
		SessionVO sessionVO = (SessionVO) request.getSession().getAttribute("sessionVO");

		if(sessionVO != null)	{

			if(sessionVO.getSessContent().equals(Constant.SESSION_USER)) {				
				return "redirect:/document/userLayout.do";	
			}else if(sessionVO.getSessContent().equals(Constant.SESSION_ADMIN) ) {				
				return "redirect:/admin/adminLayout.do";
			}else {				
				return "redirect:/document/userLayout.do";	
			}
			
		}

		model.addAttribute("expire",ConfigData.getString("COOKIE.EXPIRE"));
		model.addAttribute("emp_no",UserCookie.getUserCookie(request, "emp_no"));		// GET Cookie
		
		
		return "layout/loginForm";
	}
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 로그인 후 페이지 이동처리
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : initPage
	 * @param sessionVO
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/loginResponse.do")
	public String initPage(@ModelAttribute SessionVO sessionVO,Model model, HttpServletRequest request) {
			
		if(sessionVO != null)	{

			if(sessionVO.getSessContent().equals(Constant.SESSION_USER)) {
				return "redirect:/document/userLayout.do";			
			}else if(sessionVO.getSessContent().equals(Constant.SESSION_ADMIN)) {
				return "redirect:/admin/adminLayout.do";
			}else {
				return "redirect:/document/userLayout.do";			
			}
			
		}
		
		return "layout/loginForm";
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 사용자 화면에서 시스템관리 메뉴 선택시
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : systemPage
	 * @param sessionVO
	 * @param model
	 * @param request
	 * @return String
	 */
	@RequestMapping("/adminPage.do")
	public String systemPage(@ModelAttribute SessionVO sessionVO,Model model, HttpServletRequest request) {
			
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		if(sessionVO != null)	{
			
			// 시스템관리 메뉴 접근시 권한(ROLE)를 다시한번 체크한다.
			if(sessionVO.getSessRole_id() != null && sessionVO.getSessRole_id().equals(Constant.USER_ROLE)) {					
				CommonUtil.setErrorMsg(model, Constant.ERROR_403, messageSource.getMessage("common.connect.error",new Object[0],locale),sessionVO.getSessContextRoot());
				return "error/message";
			}else {
				return "redirect:/admin/adminLayout.do";
			}

		}
		
		CommonUtil.setErrorMsg(model, Constant.ERROR_403, messageSource.getMessage("common.connect.error",new Object[0],locale),request.getContextPath());
		return "error/message";
	}
	
	
	/**
	 * 
	 * <pre>
	 * 1. 개요 : 로그인 처리
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : loginProcess
	 * @param model
	 * @param userVO
	 * @param request
	 * @return Map
	 */
	@RequestMapping(value="/loginProcess.do", method=RequestMethod.POST)
	@ResponseBody 
	public Map<String,Object> loginProcess(Model model, @ModelAttribute UserVO userVO, HttpServletRequest request,HttpServletResponse response) {
			
		
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("login_type",Constant.NORMAL_LOGIN_TYPE);									// 로그인타입 = NORMAL - 일반웹접속 / SSO - Single Sign On 접속		
		param.put("connect_type",Constant.CONNECT_TYPE_LOGIN);				
		
		String keepid = request.getParameter("keepid");
		String keepEmpNo = request.getParameter("keepEmpNo");
		
		UserVO resultVO = new UserVO();

		try {
						
			// 0.라이센스 체크 - 라이센스 로직 추가
			commonService.checkUserLicense();

			// 1.사용자 로그인처리
			resultVO = userService.userLogin(userVO,request);
					
			// 2 사용자 패스워드 체크
			if(!userVO.getUser_pass().equals(ARIAUtil.ariaDecrypt(resultVO.getUser_pass(),resultVO.getUser_id())) )	{			
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.password.error");
				param.put("error_content",messageSource.getMessage("login.fail.password.error",new Object[0],locale));
				userService.userLogFailWrite(resultVO,param,request);
				throw new BizException(param.get("error_content").toString());
			}

			// 3.1 관리자화면 일반사용자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) &&
					resultVO.getRole_id().equals(Constant.USER_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.user.error");
				param.put("error_content",messageSource.getMessage("login.fail.user.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 3.2 사용자 화면 시스템관리자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_USER) && 
					resultVO.getRole_id().equals(Constant.SYSTEM_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.sysadmin.error");
				param.put("error_content",messageSource.getMessage("login.fail.sysadmin.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 4.사용자 세션처리
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN))	{
				param.put("content",Constant.SESSION_ADMIN);
			}else {
				param.put("content",Constant.SESSION_USER);
			}
				
			// 5.사용자 세션 저장처리
			param.put("cert_yn",Constant.YES);
			param.put("contextRoot",request.getContextPath());
			SessionVO sessionVO = sessionService.setSessionVO(resultVO, request, param);
			model.addAttribute("sessionVO",sessionVO);		

			// 6.쿠키 처리
			if(keepid != null && keepid.equals("1")) {
				if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) ) {
					UserCookie.setUserCookie(response,request.getServerName(),"emp_no",keepEmpNo, 60 * 60 * 24 * 365,"");
				}else {
					UserCookie.setUserCookie(response,request.getServerName(),"emp_no",resultVO.getEmp_no(), 60 * 60 * 24 * 365,"");
				}
			}else {
				UserCookie.setUserCookie(response,request.getServerName(),"emp_no","",0,"");
			}

			// 7. 결과값 처리
			resultMap.put("result",Constant.RESULT_TRUE);		
			resultMap.put("page",userVO.getLogin_type());
			resultMap.put("message",Constant.RESULT_SUCCESS);				
			
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());				
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
		}
				
		return resultMap;
		
	}
	
	/***
	 * 
	 * <pre>
	 * 1. 개요 : 로그아웃 처리
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : logout
	 * @param sessionVO
	 * @param sessionStatus
	 * @param request
	 * @return String
	 */
	@RequestMapping("/logout.do")
	public String logout(@ModelAttribute SessionVO sessionVO, SessionStatus sessionStatus, HttpServletRequest request) {
				
		// session listener 제거
		HttpSession session = request.getSession();
		session.setAttribute("externalCheck", "F");//valueunbound 처리위한  flag
		session.removeAttribute("listener");		
		sessionStatus.setComplete();
		
		HashMap<String,Object> param = new  HashMap<String, Object>();
		
		try {
		
			param.put("cert_yn",Constant.NOTHING);
			param.put("login_type",Constant.NORMAL_LOGIN_TYPE);						
			param.put("connect_type",Constant.CONNECT_TYPE_LOGOUT);		
			
			sessionService.sessionOut(session, sessionVO, request, param);
			
		}catch(Exception e)	{
			logger.error(e.getMessage());
		}
		
		///return "layout/loginForm";
		return "redirect:/login.do";	
	}
	
	/**
	 * 
	 * <pre>
	 * 1. 개용 : IE Memory Leak 대비 페이지 Reload 처리위한 함수 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : userLayout
	 * @param sessionVO
	 * @param model
	 * @param map
	 * @param request
	 * @return String
	 */
    @RequestMapping("/userPage.do")
    public String userLayout(@ModelAttribute SessionVO sessionVO,Model model,@RequestParam HashMap<String,Object> map,HttpServletRequest request) {
                                            
              // 좌측 메뉴 URL /document/workDocList.do => /user/mainContent.do 변경
              String url = map.get("href") != null ? map.get("href").toString() : sessionVO.getSessContextRoot()+"/user/mainContent.do";
              
              CommonUtil.setSessionToModel(model, sessionVO);
              
              model.addAttribute("contents",url);                                 
              model.addAttribute("user_name",sessionVO.getSessName());                         
              model.addAttribute("role_id",sessionVO.getSessRole_id());
              model.addAttribute("role_nm",sessionVO.getSessRole_nm());
              model.addAttribute("user_role",Constant.USER_ROLE);
              model.addAttribute("language",sessionVO.getSessLanguage());
                                        
              return "userLayout";
    }
    
    
	/**
	 * 
	 * <pre>
	 * 1. 개요 : AGNET 로그인 처리
	 * 2. 처리내용 :
	 * </pre>
	 * @Method Name : agentProcess
	 * @param model
	 * @param userVO
	 * @param request
	 * @return Map
	 */
	@RequestMapping(value="/agentProcess.do", method=RequestMethod.POST)
	public String agentProcess(Model model, @ModelAttribute UserVO userVO, HttpServletRequest request,HttpServletResponse response) {
				
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("login_type",Constant.SSO_LOGIN_TYPE);									// 로그인타입 = NORMAL - 일반웹접속 / SSO - Single Sign On 접속		
		param.put("connect_type",Constant.CONNECT_TYPE_LOGIN);		
		
		UserVO resultVO = new UserVO();

		try {
			
			// 0.라이센스 체크 - 라이센스 로직 추가
			commonService.checkUserLicense();
						
			// 1.사용자 로그인처리 			
			resultVO = userService.userLogin(userVO,request);

			
			// 2.1 관리자화면 일반사용자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) &&
					resultVO.getRole_id().equals(Constant.USER_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.user.error");
				param.put("error_content",messageSource.getMessage("login.fail.user.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 2.2 사용자 화면 시스템관리자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_USER) && 
					resultVO.getRole_id().equals(Constant.SYSTEM_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.sysadmin.error");
				param.put("error_content",messageSource.getMessage("login.fail.sysadmin.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 3.사용자 세션처리
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN))	{
				param.put("content",Constant.SESSION_ADMIN);
			}else {
				param.put("content",Constant.SESSION_USER);
			}
				
			// 4.사용자 세션 저장처리
			param.put("cert_yn",Constant.YES);
			param.put("contextRoot",request.getContextPath());
			SessionVO sessionVO = sessionService.setSessionVO(resultVO, request, param);
			model.addAttribute("sessionVO",sessionVO);		

			// 5. 결과값 처리
			resultMap.put("result",Constant.RESULT_TRUE);		
			resultMap.put("page",userVO.getLogin_type());
			resultMap.put("message",Constant.RESULT_SUCCESS);				
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());		
			return "layout/loginForm";
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			return "layout/loginForm";
		}
				
		return "redirect:/loginResponse.do";
		
	}
	
	@RequestMapping(value="/agentNoteProcess.do", method=RequestMethod.POST)
	public String agentNoteProcess(Model model, @ModelAttribute UserVO userVO, HttpServletRequest request,HttpServletResponse response) {
				
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("login_type",Constant.SSO_LOGIN_TYPE);									// 로그인타입 = NORMAL - 일반웹접속 / SSO - Single Sign On 접속		
		param.put("connect_type",Constant.CONNECT_TYPE_LOGIN);		
		
		UserVO resultVO = new UserVO();

		try {
						
			// 0.라이센스 체크 - 라이센스 로직 추가
			commonService.checkUserLicense();
						
			// 1.사용자 로그인처리 			
			resultVO = userService.userLogin(userVO,request);

			
			// 2.1 관리자화면 일반사용자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) &&
					resultVO.getRole_id().equals(Constant.USER_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.user.error");
				param.put("error_content",messageSource.getMessage("login.fail.user.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 2.2 사용자 화면 시스템관리자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_USER) && 
					resultVO.getRole_id().equals(Constant.SYSTEM_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.sysadmin.error");
				param.put("error_content",messageSource.getMessage("login.fail.sysadmin.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 3.사용자 세션처리
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN))	{
				param.put("content",Constant.SESSION_ADMIN);
			}else {
				param.put("content",Constant.SESSION_USER);
			}
				
			// 4.사용자 세션 저장처리
			param.put("cert_yn",Constant.YES);
			param.put("contextRoot",request.getContextPath());
			SessionVO sessionVO = sessionService.setSessionVO(resultVO, request, param);
			model.addAttribute("sessionVO",sessionVO);		

			// 5. 결과값 처리
			resultMap.put("result",Constant.RESULT_TRUE);		
			resultMap.put("page",userVO.getLogin_type());
			resultMap.put("message",Constant.RESULT_SUCCESS);				
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());		
			return "layout/loginForm";
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			return "layout/loginForm";
		}
				
		return "redirect:/note/noteMain.do";
		
	}
	
		
}
