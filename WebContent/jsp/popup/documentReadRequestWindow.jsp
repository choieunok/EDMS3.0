<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/documentReadRequestWindow.js"></script>
<!-- 
	Usage : 
	- 업무문서함 - 추가기능 (열람요청)
		
 -->
<script type="text/javascript">
	exsoft.util.common.ddslick("doc_request_period", 'doc_request_period', '', 100, function(){});
</script>
<div class="doc_request_wrapper hide"></div>
<div class="doc_request hide">
	<div class="doc_request_title">
		문서 열람 요청
		<a href="#" class="doc_request_close" onclick="documentReadRequestWindow.close();"><img src="${contextRoot}/img/icon/window_close1.png"></a>
	</div>
	<div class="doc_request_cnts">
		<table>
			<tr>
			<td>문&nbsp;&nbsp;서&nbsp;&nbsp;명 : </td>
			<td><label id="req_doc_name"></label></td>
			</tr>
			<tr>
			<td>열람요청기간 : </td>
			<td>
			<select id="doc_request_period">
				<option value="0">선택</option>
				<option value="7">1주일</option>
				<option value="30">1달</option>
				<option value="90">3달</option>
			</select>
			<label id="req_period_detail" class="hide"></label>
			</td>
			</tr>
			<tr>
			<td>열람요청사유 : </td>
			<td>
			<textarea id="requestReason"  name="request_reason"onKeyUp='javascript:exsoft.util.common.limitStringCheck(this,"1000")'></textarea>
       		<p id="reason_text_count" class="note_count"><span class="current_count" id="current_textcnt"></span>/1000자</p>
			</td>
			</tr>
		</table>
		
		<div id="req_btn_group" class="doc_request_btnGrp">
			<button type="button" class="confirm" onclick="documentReadRequestWindow.event.submit();">열람요청</button>
			<button type="button" class="cancel" onclick="documentReadRequestWindow.close();">취소</button>
		</div>
		<div id="req_btn_group_detail" class="doc_request_btnGrp class">
			<button type="button" class="cancel" onclick="documentReadRequestWindow.close();">닫기</button>
		</div>
	</div>
</div>
