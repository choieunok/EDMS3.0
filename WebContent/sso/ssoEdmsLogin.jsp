<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="java.net.URLDecoder"%>
<%
	String user_id = (String)session.getAttribute("SSO_ID");
	String edmsServer = request.getLocalName() + ":" + request.getLocalPort() + request.getContextPath();
	
	System.out.println(edmsServer);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Search</title>
<link href="common/common.css" REL="StyleSheet" TYPE="text/css">
<script SRC="common/common.js"></script>
<script SRC="common/prototype.js"></script>
<script>
	function goEdms() {
		var frm = document.frm;
		frm.action = "http://" + "<%= edmsServer%>" + "/agentProcess.do";			// 사이트에 맞게 변경하세요.
		frm.method = "post";
		frm.emp_no.value = "<%=user_id%>"; 
		frm.submit();
	}
</script>
</head>
<body onload="javascript:goEdms();"></body>
</html>
<form name="frm">
	<input type="hidden" name="emp_no">
	<input type="hidden" name="externalCheck" value="T">
	<input type="hidden" name="login_type" value="user">
</form>
