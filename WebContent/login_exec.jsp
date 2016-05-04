<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="./sso/config.jsp" %>
<%

	String uurl = null;

	//http://nlstest.initech.com:8090/agent/sso/login_exec.jsp : 꼭 도메인으로 호출해야 된다.
	System.out.println("*=======================================");
	//1.SSO ID 수신
	String sso_id = getSsoId(request);
	System.out.println("*================== [login_exec.jsp]  sso_id = "+sso_id);
	if (sso_id == null) {
		goLoginPage(response);
		return;
	} else {

		//4.쿠키 유효성 확인 :0(정상)
		String retCode = getEamSessionCheck(request,response);
	
		if(!retCode.equals("0")){
			goErrorPage(response, Integer.parseInt(retCode));
			return;
		}
		//
		//5.업무시스템에 읽을 사용자 아이디를 세션으로 생성
		String EAM_ID = (String)session.getAttribute("SSO_ID");
		if(EAM_ID == null || EAM_ID.equals("")) {
			System.out.println("insider if   " + EAM_ID);
			session.setAttribute("SSO_ID", sso_id);
		}
		System.out.println("outside if   " + EAM_ID);
		out.println("SSO 인증 성공!!");

		//6.업무시스템 페이지 호출(세션 페이지 또는 메인페이지 지정)  --> 업무시스템에 맞게 URL 수정!
		response.sendRedirect("sso/ssoEdmsLogin.jsp"); // SSO 로그인된 user_id로 EDMS로그인
		//out.println("인증성공");
	}
%>
