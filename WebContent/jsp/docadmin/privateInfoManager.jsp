<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
	개인정보 유출문서 관리
 -->
 <style>
 .grant_transfer_wrapper{position:absolute;top:0;left:0;background-color:rgba(0,0,0,0.5);z-index:240}
 </style>
<div class="cnt_wrapper">
<div class="depth_navigation">${menuInfo.su_menu_nm} &gt; ${menuInfo.menu_nm}</div>

<div class="transfer_sub_wrapper">
	<div class="sub_left">
		<div class="srch_form">
			<form id="">
				<table>					
<!-- 					<tr> -->
<!-- 						<th>검색대상</th> -->
<!-- 						<td> -->
<!-- 							<label><input type="radio" id="MYWORK" name="search" checked onclick="javascript:exsoftAdminDocFunc.ui.checkBoxChanged('MYWORK',event);">개인문서함</label> -->
<!-- 							<label class="mar_left_10px"><input type="radio" id="WORKSPACE" name="search" onclick="javascript:exsoftAdminDocFunc.ui.checkBoxChanged('WORKSPACE',event);">업무문서함</label> -->
<!-- 						</td> -->
<!-- 					</tr> -->
<!-- 					<tr> -->
<!-- 						<th>소유자</th> -->
<!-- 						<td> -->
<!-- 							<input type="text" id="user_name" class="readonly" maxlength="20" readonly="readonly"> -->
<!-- 							<input type="hidden" id="user_id"/> -->
<!-- 							<button type="button" id="" class="" onclick="javascript:selectSingleUserWindow.init.initSingleUserWindow(exsoftAdminDocFunc.callback.returnSelectUser);">선택</button> -->
<!-- 						</td> -->
<!-- 					</tr> -->
					<tr>
						<th>등록일</th>
						<td>							
							<input type="text" id="sdate" name="" class="cal" value="" placeholder="">							
							~							
							<input type="text" id="edate" name="" class="cal" value="" placeholder="">
						</td>
					</tr>
					<tr>
						<th>제목</th>
						<td>
							<input type="text" id="doc_name" name="" class="" placeholder="제목을 입력하세요" maxlength="50"
							onkeypress="javascript:return exsoftAdminDocFunc.event.enterKeyPress(event, 'owner');">
							<button type="button" id="" class="menuAcl_roleName_find" onClick="javascript:exsoftAdminDocFunc.search.searchDocList();"></button>
						</td>
					</tr>
				</table>
			</form>
		</div>
		<div class="srch_result_wrapper" id="ownerDocGridTarget">
			<div class="transfer_btnGrp">
<!-- 				<button type="button" id="" class="btn2" onclick="javascript:exsoftAdminDocFunc.open.changeOwner();">소유권 변경</button> -->
				
				<div class="menu_imgBtnGrp1">
					<a href="javascript:void(0);" class="menu_refresh" onclick='javascript:exsoft.util.grid.gridRefresh("ownerDocGrid","${contextRoot}/admin/documentList.do");'><img src="${contextRoot}/img/icon/menu_refresh.png" alt="" title=""></a>
				</div>
			</div>
			<!-- Refresh 버튼 -->
			<div class="srch_result" id="">
				<table id="ownerDocGrid"></table>
			</div>
		</div>
		<!--  PAGER 위치 -->
		<div class="pg_subWrapper">
			<div class="pg_navi_wrapper">
				<ul class="pg_navi" id="ownerDocGridPager"></ul>
			</div>
		</div>
	</div>
	<jsp:include page="/jsp/docadmin/docDetail.jsp" /><!--  만기문서관리/소유권변경/휴지통관리 상세화면 공통 -->
	</div>
</div>

<script type="text/javascript" src="${contextRoot}/js/docadmin/docadmin.js"></script>
<script type="text/javascript">
jQuery(function() {				
	exsoft.util.filter.maxNumber();			// maxlength
	exsoftAdminLayoutFunc.init.menuInit();
	exsoftAdminLayoutFunc.init.menuSelected('${topSelect}','${subSelect}');
	
	exsoftAdminDocFunc.init.initPage('#ownerDocGrid','/admin/documentList.do','${pageSize}','${part}');
	exsoftAdminDocFunc.event.ownerGrid();
	
	exsoft.util.grid.gridResize('ownerDocGrid','ownerDocGridTarget',120,0);
	exsoft.util.grid.gridResize('detail_docHistoryList','targetDocHistoryList',85,0);
	
});
</script>
