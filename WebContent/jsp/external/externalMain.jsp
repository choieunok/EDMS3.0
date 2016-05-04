<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">

<title>eXrep ECM</title>
<!-- 키값 설정 -->
<script type="text/javascript">
var contextRoot = "${contextRoot}";
var userId = "${userId}";
var windowType = 'userLayout';
var theme = "themeBlue";

</script>

<!-- 기본 js 선언 -->
<script type="text/javascript" src="${contextRoot}/js/plugins/jquery/jquery-2.1.3.min.js"></script>  
<script type="text/javascript" src="${contextRoot}/js/common/common.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/include.js"></script>
<script type="text/javascript" src="${contextRoot}/js/external/externalFunc.js"></script>
<c:choose>
<c:when test="${language == 'KO'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_ko.js"></script></c:when>
<c:when test="${language == 'EN'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_en.js"></script></c:when>
</c:choose>


</head>



<form name="treePopCall">
	<input type="hidden" name="emp_no">
	<input type="hidden" name="externalCheck" value="T">
	<input type="hidden" name="login_type" value="user">
	<br/><br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<button onclick="javascript:externalFunc.open.treePop();">폴더테스트</button>
</form>


<form name="registDocCall">
	<input type="hidden" name="emp_no">
	<input type="hidden" name="externalCheck" value="T">
	<input type="hidden" name="login_type" value="user">
	<br/><br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<button onclick="javascript:externalFunc.open.registDocPop();">문서테스트</button>
</form>

</html>