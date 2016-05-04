<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/relDocWindow.js"></script>

<!-- 
	Usage :
		문서등록(수정)시 관련문서 추가 선택 화면			 
 -->
 <script type="text/javascript">
jQuery(function() {	
	// 달력을 생성 한다.
	$("#relativeAdd_datepicker1").datepicker({dateFormat:'yy-mm-dd'});
	$('#cal1').bind("click", function(){
		$("#relativeAdd_datepicker1").datepicker("show");
	});	
	$("#relativeAdd_datepicker2").datepicker({dateFormat:'yy-mm-dd'});
	$('#cal2').bind("click", function(){
		$("#relativeAdd_datepicker2").datepicker("show");
	});	

});
</script>
<div class="doc_relativeAdd_wrapper hide"></div>
<div class="doc_relativeAdd hide">
	<div class="doc_relativeAdd_title">
		관련문서 추가
		<a href="javascript:void(0);" class="doc_relativeAdd_close">
			<img src="${contextRoot}/img/icon/window_close.png" alt="" title="" onclick="javascript:selectrelativeDocWindow.close();">
		</a>
	</div>
	<div class="doc_relativeAdd_cnts">
		<div class="relativeAdd_left">
			<div class="tree_menu">
				<ul class="tree_menu_list">
					<li id="tabMapMydept"><a href="javascript:void(0);" onclick="javascript:selectrelativeDocWindow.event.relDoc_changeMap('WORK_MYDEPT')" class="focus">부서</a></li>
					<li id="tabMapAlldept"><a href="javascript:void(0);" onclick="javascript:selectrelativeDocWindow.event.relDoc_changeMap('WORK_ALLDEPT')" class="focus">전사</a></li>
					<li id="tabMapProject"><a href="javascript:void(0);" onclick="javascript:selectrelativeDocWindow.event.relDoc_changeMap('WORK_PROJECT')" class="focus">프로젝트</a></li>
				</ul>
				<div class="tree_menu_more">
					<ul class="menu_more_list">
						<li><a href="javascript:void(0);"><img src="${contextRoot}/img/icon/tree_more.png" alt="" title=""></a></li>
						<li><a href="javascript:void(0);"><img src="${contextRoot}/img/icon/tree_refresh.png" onclick="javascript:selectrelativeDocWindow.event.relDoc_refreshFolderTree()" alt="새로고침" title=""></a></li>
					</ul>
				</div>
			</div>
			<div id="relativeAddMypageTree" class="relativeAdd_tree_list"></div>
			<div id="relativeAddMydeptTree" class="relativeAdd_tree_list hide"></div>
			<div id="relativeAddAlldeptTree" class="relativeAdd_tree_list hide"></div>
			<div id="relativeAddProjectTree" class="relativeAdd_tree_list hide"></div>						
		</div>
		<div class="relativeAdd_right">
			<div class="relativeAdd_docs_list">
				<span class="relativeAdd_docs_category" id="relDoc_folderTitle"></span>
				<div class="relativeAdd_srch_form">
					<form class="">
						<table class="relativeAdd_srch_tbl">
							<tr>
								<td>
									<span class="relativeAdd_regdate_lbl">제목</span>
									<input type="text" id="relDoc_strKeyword1" name="" class="relativeAdd_srch_txt border-radius-right" placeholder="검색어를 입력하세요" style="width:150px;margin-left:10px;margin-right:10px;">
									<span class="relativeAdd_regdate_lbl">등록기간</span>
									<input type="text" id="relativeAdd_datepicker1" class="input-text calend" readonly="readonly" style="width:63px;"><span>-</span> 
									<input type="text" id="relativeAdd_datepicker2" class="input-text calend" readonly="readonly" style="width:63px;">
									<button type="button" class="relativeAdd_doc_srch" onclick="javascript:selectrelativeDocWindow.event.relDoc_searchDocument()">검색</button>
									<button type="button" class="relativeAdd_doc_allsrch" onclick="javascript:selectrelativeDocWindow.event.relDoc_documentListByFolderId()">전체검색</button>
								</td>
							</tr>
						</table>
					</form>
				</div>
				<div class="relativeAdd_list" id="target_relDoc_document_gridList" style="border-bottom:1px solid #b7b7b7;">
					<table class="relativeAdd_list_tbl" id="relDoc_document_gridList"></table>					
				</div>				
				
				<div class="relativeAdd_pg_navigation">
					<ul id="relDoc_documentGridPager" class="pg_navi"></ul>					
				</div>
				
				<div class="relativeAdd_list_btnGrp">
					<button type="button" class="relativeAdd_selected"  onclick="javascript:selectrelativeDocWindow.event.relDoc_addDocument();">추가</button>
					<button type="button" class="relativeAdd_selected" onclick="javascript:selectrelativeDocWindow.event.relDoc_removeDocument();">제외</button>
				</div>
			</div>
			
			<div class="relativeAdd_docs_selected">
				<span class="bold">선택문서</span>
				<div class="relativeAdd_selected_list" id="relDoc_targetSelectGrid">
					<table class="relativeAdd_selectedList_tbl" id="relDoc_select_gridList">
						
					</table>
				</div>
			</div>
			<div class="relativeAdd_btnGrp">
				<button type="button" class="relativeAdd_save" class="" onClick="javascript:selectrelativeDocWindow.submit();">확인</button>
				<button type="button" class="relativeAdd_cancel" class="" onclick="javascript:selectrelativeDocWindow.close();">취소</button>
			</div>
		</div>
	</div>
</div>

