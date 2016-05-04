<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
	Usage : 
	- 폴더등록(수정)
 -->
<div class="subFolder_add_wrapper hide"></div>
<div class="subFolder_add hide">
	<div class="subFolder_add_title">
		<label id="registFolderWindowTitle">폴더 등록</label> 
		<a href="#" class="subFolder_add_close">
			<img src="${contextRoot}/img/icon/window_close.png"  onclick="folderWindow.close();">
		</a>
	</div>
	<div class="subFolder_add_wrap">
		<div class="subFolder_add_cnts" id="registFolder">
			<input type="hidden" data-bind="folder_id">
			<input type="hidden" data-bind="parent_id">
			<input type="hidden" data-bind="acl_id">
			<input type="hidden" data-bind="type">
			<input type="hidden" data-bind="map_id">
			<table class="no_bg">
				<tr>
					<th>폴더명 <span class="required">*</span></th>
					<td colspan="3">
						<input type="text" class="doc_title" data-bind="folder_name_ko" maxlength="255" ex-valid="require" ex-display="폴더명">
					</td>
				</tr>
				<tr>
					<th>폴더명(영문)</th>
					<td colspan="3"><input type="text" class="doc_title_eng" maxlength="255" data-bind="folder_name_en"></td>
				</tr>
				<tr>
					<th>상위폴더경로</th>
					<td colspan="3">
						<input type="text" class="doc_folder readonly" readonly="readonly" data-bind="full_path">
						<button type="button" class="doc_folder_srch" onclick="folderWindow.event.selectParentFolder();">선택</button>
					</td>
				</tr>
				<tr>
					<th>문서저장 여부</th>
					<td>
						<select id="register_doc_save" data-bind="is_save" data-select='true'>
							<option value="Y">가능</option>
							<option value="N">불가능</option>
						</select>
					</td>
					<th>사용여부</th>
					<td>
						<select id="register_doc_use" data-bind="folder_status" data-select='true'>
							<option value="C">사용</option>
							<option value="D">미사용</option>
						</select>
					</td>
				</tr>
				<tr>
					<th>저장문서유형</th>
					<td id='registFolderType_template'>
						<select id="registFolderType" data-bind="is_type" data-select='true'>
							<option value="ALL_TYPE">모든문서</option> <!-- 다이내믹 적용 -->
						</select>
					</td>
					<th>정렬순서 <span class="required">*</span></th>
					<td>
						<input type="text" class="folder_storage_limit" data-bind="sort_index" maxlength="4"
						onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">
					</td>
				</tr>
				<tr id="registFolder_storage_quota">
					<th>스토리지 할당량</th>
					<td colspan="3">
						<label><input type="checkbox" id="storage_quotaCheckBox" onclick="folderWindow.event.changeQuotaCheck(this);">체크시 용량제한 없음</label> 
						<input type="text" class="folder_storage_limit" data-bind="storage_quota" maxlength="5"
						onkeydown="return exsoft.util.filter.numInput(event);" >GB
					</td>
				</tr>
				<tr id="registFolder_inherit_acl">
					<th>권한변경 여부</th>
					<td colspan="3">
						<label><input type="checkbox" id="inherit_acl_chk" data-bind="is_inherit_acl" value="T">상위폴더 권한 변경 시 현재 권한 유지</label>
					</td>
				</tr>
				<tr id="registFolder_share">
					<th>공유폴더 여부</th>
					<td colspan="3">
						<label><input type="checkbox" id="share_chk" data-bind="is_share" value="T">권한을 부여받은 사용자에게 ‘공유받은 폴더’ 제공</label>
					</td>
				</tr>
			</table>
			<div class="subFolder_auth" id="registFolder_changefolderacl">
				<a class="dropDown_txt"> 
					<label id="aclName"></label> 
					<span class="dropDown_arrow down"></span>
				</a>
				<div class="subFolder_auth_cnts hide">
				<div>
					<button type="button" onclick="folderWindow.event.changeFolderAcl();">권한변경</button>
					<label id="acl_batch"><input type="checkbox" id="is_acl_batch" class=""> 하위 폴더/문서 권한 일괄변경</label>
				</div>
					<table id="registFolderAclList">
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
			<div class="subFolder_add_btnGrp">
				<button type="button" onclick="folderWindow.event.submit();" id="registFolderSubmitBtn">등록</button>
				<button type="reset" onclick="folderWindow.close();">취소</button>
			</div>
		</div>
		<div class="subFolder_add_recent" id="recentlyFolderDiv">
			<div class="recent_folder_title">최근폴더</div>
			<div class="recent_folder_wrap">
				<ul class="recent_folderList" id="recentFolderList">
				</ul>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript" src="${contextRoot}/js/popup/registFolderWindow.js"></script>
<script type="text/javascript">
jQuery(function() {
	
	//select box ddslick 사용
	//folderWindow.initDdslick();
	
});

jQuery(function() {

    // 폴더생성,폴더수정: 음영진 부분 클릭 시 닫기
    $('.subFolder_add_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.subFolder_add').addClass('hide');
    });
});
</script>


