<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<script type="text/javascript" src="${contextRoot}/js/common/base64.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/documentVersionDetail.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/documentListLayerWindow.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/selectFavoriteFolderWindow.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/initbind.js"></script>

<script type="text/javascript" src="${contextRoot}/js/popup/detailFolderWindow.js"></script>

<!-- 
	Usage :  폴더기본조회
	[3000]	2015-09-21	성예나	 : 권한변경여부,공유폴더 여부의 FALSE,TRUE 부분 자연스럽게 수정하기위해 기존의 label 제거
	[3001]	2015-09-21	성예나	 : 스토리지 사용량 추가
 -->
 

<div class="fol_detail_wrapper hide"></div>

<div class="fol_detail hide">
		<div class="fol_detail_title">
			<span id="folTitle">폴더 상세조회</span>
			
			<a href="javascript:void(0);" class="fol_detail_close">
				<img src="${contextRoot}/img/icon/window_close2.png"  onclick="detailfolderWindow.close();">
			</a>
		
		
		</div>
		
		<div class="fol_detail_cnts">
		
			<div class="cnts_title"> <span class="title"></span>
				
			</div>
			<div class="tab_menu">
				<div class="tab_elem_wrapper">
					<span class="tab_element selected" id="defaultFol" onclick="javascript:detailfolderWindow.ui.folDetailSelectAction(0);">기본</span>
					<span class="tab_element" id="historyFol" onclick="javascript:detailfolderWindow.ui.folDetailSelectAction(1);">이력</span> 
				 
				</div>
				<div>			
					<button type="button" id="folHistoryexcel" class="excel_button hide" onclick="javascript:exsoft.util.grid.excelDown('detailfolHistoryList','${contextRoot}/folder/folHistoryList.do');"></button>
				</div>
			</div>
			
			
			<!-- 폴더 기본정보 -->
			<div class="tab_form" id="detailFolder">
			<input type="hidden" data-bind="folder_id">
			<input type="hidden" data-bind="parent_id">
			<input type="hidden" data-bind="acl_id">
			<input type="hidden" data-bind="type">
			<input type="hidden" data-bind="map_id">
			<table class="no_bg">
				<tr>
					<th>폴더명 </th>
					<td colspan="3">
					<label data-bind="folder_name_ko" maxlength="255" ex-valid="require" ex-display="폴더명"></label>
						<!-- <input type="text" class="doc_title readonly" data-bind="folder_name_ko" maxlength="255" ex-valid="require" ex-display="폴더명"> -->
					</td>
				</tr>
				<tr>
					<th>폴더명(영문)</th>
					<td colspan="3"><label data-bind="folder_name_en" maxlength="255"  ex-display="폴더명(영문)"></label></td>
				</tr>
				<tr>
					<th>상위폴더경로</th>
					<td colspan="3"><label data-bind="full_path"></label></td>
				</tr>
				<tr>
					<th>문서저장 여부</th>
					<td>
						<select id="detail_doc_save" data-bind="is_save" data-select='true'>
							<option value="Y">가능</option>
							<option value="N">불가능</option>
						</select>
					</td>
					<th>사용여부</th>
					<td>
						<select id="detail_doc_use" data-bind="folder_status" data-select='true'>
							<option value="C">사용</option>
							<option value="D">미사용</option>
						</select>
					</td>
				</tr>
				<tr>
					<th>저장문서유형</th>
					<td id='detailFolderType_template'>
						<select id="detailFolderType" data-bind="is_type" data-select='true'>
							<option value="ALL_TYPE">모든문서</option> <!-- 다이내믹 적용 -->
						</select>
					</td>
					<th>정렬순서</th>
					<td>
						<label data-bind="sort_index"  maxlength="4" ></label>
						<!-- <input type="text" class="folder_storage_limit" data-bind="" maxlength="4"
						onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')"> -->
					</td>
				</tr>
				<tr>
					<th>스토리지 할당량</th>
					<td>
						<!-- <label><input type="checkbox" id="storage_quotaCheckBox" onclick="folderWindow.event.changeQuotaCheck(this);">체크시 용량제한 없음</label>  -->
						<label data-bind="storage_quota" maxlength="5" ></label>
					</td>
					<!-- [3001 ]-->
					<th>스토리지 사용량</th>
					<td colspan="3">
						<!-- <label><input type="checkbox" id="storage_quotaCheckBox" onclick="folderWindow.event.changeQuotaCheck(this);">체크시 용량제한 없음</label>  -->
						<label data-bind="storage_usage" maxlength="5" ></label>
					</td>
				</tr>
				<tr id="detailFolder_inherit_acl">
					<th>권한변경 여부</th>
					<td colspan="3">
					<!-- [3000]  -->
						<B><label data-bind="is_inherit_acl" id="inherit_acl_chk" ></label></B>
					</td>
				</tr>
				<tr id="detailFolder_share">
					<th>공유폴더 여부</th>
					<td colspan="3">
					<!-- [3000]  -->
						<B><label data-bind="is_share" id="share_chk" ></label></B>
					</td>
				</tr>
			</table>
			<div class="subFolder_auth" id="detailFolder_changefolderacl">
				<a class="dropDown_txt"> 
					<label id="folaclName">권한정보</label> 
					<span class="dropDown_arrow down"></span>
				</a>
				<div class="subFolder_auth_cnts hide">
				
					<table id="detailFolderAclList">
       					<thead>
       						<tr>
	         				<th rowspan="2">접근자</th>
	         				<th colspan="3">폴더권한</th>
	         				<th colspan="4">문서권한</th>
	         				</tr>
	         				<tr>
		         				<th>기본권한</th>
		         				<th>폴더등록</th>
		         				<th>권한변경</th>
		         				<th>기본권한</th>
		         				<th>문서등록</th>
		         				<th>반출취소</th>
		         				<th>권한변경</th>
	         				</tr>
       					</thead>
	       				<tbody></tbody>
       				</table>
				</div>
			</div>
						
			</div>		
			
			<!-- 폴더 이력 -->
			<div class="tab_form hide">
				<div class="archive_wrapper_"  id="targetFolHistoryList">				
					<table id="detailfolHistoryList"></table>	
				</div>
				<!-- 폴더 이력 페이징 -->
				<div class="pg_navi_wrapper">
					<ul class="pg_navi" id="historyfolderGridPager"></ul>
				</div> 
			</div>
			
			
			
	</div>
</div>
<script type="text/javascript">
jQuery(function() {
	
	// Layer 관련 이벤트 정의
	exsoft.util.layout.divLayerOn('fol_detail_wrapper','fol_detail','fol_detail_close');

});
</script>
