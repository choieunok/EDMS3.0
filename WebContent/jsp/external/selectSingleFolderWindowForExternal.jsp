<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!-- 
	Usage : 외부연계 폴더선택
 -->
 <html>
 <head>
 <title>폴더선택</title>
 
 <script type="text/javascript" src="${contextRoot}/js/popup/selectSingleFolderWindow.js"></script>
 <script type="text/javascript" src="${contextRoot}/js/external/externalFunc.js"></script>
 <!-- 키값 설정 -->
 <script type="text/javascript">
 var contextRoot = "${contextRoot}";
 var theme = "themeBlue";
 var windowType = 'userLayout';
 </script>

 <!-- 기본 js 선언 -->
 <script type="text/javascript" src="${contextRoot}/js/plugins/jquery/jquery-2.1.3.min.js"></script>
 <script type="text/javascript" src="${contextRoot}/js/common/common.js"></script>
 <script type="text/javascript" src="${contextRoot}/js/common/include.js"></script>
 <c:choose>
 <c:when test="${language == 'KO'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_ko.js"></script></c:when>
 <c:when test="${language == 'EN'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_en.js"></script></c:when>
 </c:choose>
 <script type="text/javascript">
	exsoft.util.common.ddslick("doc_folder_list", 'doc_folder_list', '', 260, selectSingleFolderWindow.callback.changedDocFolderList);
 </script>
 </head>
 
 <body onload="javascript:externalFunc.init.folderWindowInit();">
 	<form name="registDocCall">
		<input type="hidden" name="emp_no">
		<input type="hidden" name="login_type" value="user">
		<input type="hidden" name="folder_id">
		<input type="hidden" name="folder_path">
		<input type="hidden" name="calledpage">
		<input type="hidden" name="isLeft">
	</form>
	
	<!-- <div class="ex_folder_choose_wrapper"></div> -->
<div class="ex_doc_folder_choose">
<!-- 	<div class="doc_folder_title doc_folder_choose_title"> -->
<%-- 		<a href="#" class="doc_folder_close" onclick="selectSingleFolderWindow.close();"><img src="${contextRoot}/img/icon/window_close1.png"></a> --%>
<!-- 	</div> -->
	<div class="doc_folder_cnts">
		<div class="doc_folder_box">
			<span id="lb_singleFolderWorkspace" class="box_lbl">문서함</span> 
			<select id="doc_folder_list">
				<option value="WORK_MYDEPT">부서</option>
				<option value="WORK_ALLDEPT">전사</option>
				<option value="WORK_PROJECT">프로젝트</option>
			</select>
		</div>
		<div id="singleFolderMypageTree"	class="doc_folder_tree2"></div>
		<div id="singleFolderMydeptTree"	class="doc_folder_tree2 hide"></div>
		<div id="singleFolderAlldeptTree"	class="doc_folder_tree2 hide"></div>
		<div id="singleFolderProjectTree"	class="doc_folder_tree2 hide"></div>
		<div id="singleFolderFreeDocTree"	class="doc_folder_tree2 hide"></div>
		<div class="doc_folder_btnGrp2">
			<button type="button" class="confirm" onclick="selectSingleFolderWindow.event.submit();">확인</button>
<!-- 			<button type="button" class="cancel" onclick="exsoft.util.layout.divLayerClose('folder_choose_wrapper', 'doc_folder_choose')">취소</button> -->
		</div>
	</div>
</div>
</body>
 </html>
