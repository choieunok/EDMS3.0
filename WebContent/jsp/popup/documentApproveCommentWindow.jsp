<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/documentApproveCommentWindow.js"></script>
<!-- 
	Usage : 
	- 나의문서 > 열람 승인 문서 - 승인/반려 사유 기입팝업
		
 -->
<div class="doc_aprv_comment_wrapper hide"></div>
<div class="doc_aprv_comment hide">
	<div id="comment_window_title" class="doc_aprv_comment_title">
		<a href="#" class="doc_aprv_comment_close" onclick="documentApproveCommentWindow.close();"><img src="${contextRoot}/img/icon/window_close1.png"></a>
	</div>
	<div class="doc_aprv_comment_cnts">
		<textarea id="approve_comment"  name="approve_comment"onKeyUp='javascript:exsoft.util.common.limitStringCheck(this,"1000")'></textarea>
   		<p class="note_count"><span class="current_count" id="current_textcnt"></span>/1000자</p>
		
		<div class="doc_aprv_comment_btnGrp">
			<button type="button" class="confirm" onclick="documentApproveCommentWindow.event.submit();">확인</button>
			<button type="button" class="cancel" onclick="documentApproveCommentWindow.close();">취소</button>
		</div>
	</div>
</div>
