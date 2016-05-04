<%@page import="com.initech.eam.api.NXNLSAPI"%>
<%@page import="com.initech.eam.smartenforcer.SECode"%>
<%@page import="java.util.Vector"%>
<%@page import="com.initech.eam.nls.CookieManager"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.log4j.PropertyConfigurator"%>
<%@page import="com.initech.eam.api.NXContext"%>
<%!
/**[INISAFE NEXESS JAVA AGENT]**********************************************************************
* 업무시스템 설정 사항 (업무 환경에 맞게 변경)
***************************************************************************************************/


/***[SERVICE CONFIGURATION]***********************************************************************/
	private String SERVICE_NAME = "EDMS";
	private String SERVER_URL 	= "http://edms.hanaw.com";
	private String SERVER_PORT = "8080";
	private String ASCP_URL = SERVER_URL + ":" + SERVER_PORT + "/edms/login_exec.jsp";
/*************************************************************************************************/


/***[SSO CONFIGURATION]**]***********************************************************************/
	private String NLS_URL 		 = "http://ssodev.hanaw.com";
	private String NLS_PORT 	 = "15580";
	private String NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/clientLogin.jsp";
	private String NLS_LOGOUT_URL= NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
	private String NLS_ERROR_URL = NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
	private static String ND_URL1 = "http://ssodev.hanaw.com:15480";
	private static String ND_URL2 = "http://ssodev.hanaw.com:15480";

	private static Vector PROVIDER_LIST = new Vector();

	private static final int COOKIE_SESSTION_TIME_OUT = 3000000;

	// 인증 타입 (ID/PW 방식 : 1, 인증서 : 3)
	private String TOA = "1";
	private String SSO_DOMAIN = ".hanaw.com";

	private static final int timeout = 15000;
	private static NXContext context = null;
	static{
		//PropertyConfigurator.configureAndWatch("D:/INISafeNexess/site/4.1.0/메리츠증권/src/Web/WebContent/WEB-INF/logger.properties");
		List<String> serverurlList = new ArrayList<String>();
		serverurlList.add(ND_URL1);
		serverurlList.add(ND_URL2);

		context = new NXContext(serverurlList,timeout);
		CookieManager.setEncStatus(true);

		PROVIDER_LIST.add("ssodev.hanaw.com");
		PROVIDER_LIST.add("sso.hanaw.com");
	}

	// 통합 SSO ID 조회
	public String getSsoId(HttpServletRequest request) {
		String sso_id = null;
		
		System.out.println("getSsoId");
		System.out.println(SECode.USER_ID);
		
		sso_id = CookieManager.getCookieValue(SECode.USER_ID, request);
		
		System.out.println("return sso_id?!!   " + sso_id);
		System.out.println("*=======================================");
		
		return sso_id;
	}
	// 통합 SSO 로그인페이지 이동
	public void goLoginPage(HttpServletResponse response)throws Exception {
		System.out.println("goLoginPage");
		
		CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
		CookieManager.addCookie(SECode.R_TOA, TOA, SSO_DOMAIN, response);
		response.sendRedirect(NLS_LOGIN_URL);
		
		System.out.println("*=======================================");
	}

	// 통합인증 세션을 체크 하기 위하여 사용되는 API
	public String getEamSessionCheck(HttpServletRequest request,HttpServletResponse response){
		System.out.println("getEamSessionCheck");
		
		String retCode = "";
		try {
			retCode = CookieManager.verifyNexessCookie(request, response, 10, COOKIE_SESSTION_TIME_OUT,PROVIDER_LIST);
		} catch(Exception npe) {
			npe.printStackTrace();
		}
		
		System.out.println("return retCode   " + retCode);
		System.out.println("*=======================================");
		
		return retCode;
	}

	// SSO 에러페이지 URL
	public void goErrorPage(HttpServletResponse response, int error_code)throws Exception {
		System.out.println("goErrorPage");
		
		CookieManager.removeNexessCookie(SSO_DOMAIN, response);
		CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
		response.sendRedirect(NLS_ERROR_URL + "?errorCode=" + error_code);
		
		System.out.println("*=======================================");
	}

%>
