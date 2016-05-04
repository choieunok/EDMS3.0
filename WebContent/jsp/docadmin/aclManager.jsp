<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!--
	권한관리
	권한등록/권한복사/ACL사용자추가화면 포함 :: 기존 사용자 공통화면이 있는 경우 공통화면을 사용하세요 
 -->
<div class="cnt_wrapper">
	<div class="depth_navigation">
		문서관리 &gt; 권한(ACL) 관리
	</div>
	
	<div class="acl_sub_wrapper">
		<div class="sub_left">
			<div class="srch_area">
				<div class="srch_form">
					<form>
						<table>
							<tr>
								<th>공개범위</th>
								<td>
									<select id="strIndex">
										<option value="">전체</option>
										<option value="ALL">전사</option>
										<option value="TEAM">부서</option>
										<option value="DEPT">하위부서포함</option>
										<option value="PRIVATE">사용자</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>공개대상</th>
								<td>
									<input type="text" class="srch_auth_keyword" id="strKeyword1"
									onkeypress="javascript:return aclManager.event.enterKeyPress(event);">
								</td>
							</tr>
							<tr>
								<th>권한명</th>
								<td>
									<input type="text" class="srch_auth_keyword" id="strKeyword2"
									onkeypress="javascript:return aclManager.event.enterKeyPress(event);">
									<button type="button" class="menuAcl_roleName_find btn1" onclick="aclManager.event.searchAclList();"></button>
								</td>
							</tr>
						</table>
					</form>
				</div>
				<div class="srch_result_wrapper" id="targetAclGridList">
					<div class="left">
						<button type="button" class="register_auth_btn btn1" onclick="aclManager.event.createAclPopup();">등록</button>
						<button type="button" class="delete_auth_btn btn1" onclick="aclManager.event.deleteAcl('list');">삭제</button>
						<button type="button" class="copy_auth_btn btn1" onclick="aclManager.event.copyAcl('list')">복사</button>
						<div class="menu_imgBtnGrp1">
							<a href="#" class="menu_refresh" onclick="aclManager.grid.refreshAclList();"><img src="${contextRoot }/img/icon/menu_refresh.png" alt="" title=""></a>
						</div>
					</div>
					<div class="srch_result" id="">
						<table id="aclGridList">
						</table>
					</div>
				</div>
			</div>
			<div class="pg_subWrapper">
				<div class="pg_navi_wrapper">
					<ul id="aclGridPager" class="pg_navi">
						<!-- 문서목록 Pager -->
					</ul>
	           	</div>	
			</div>
		</div>
		
		<div class="sub_right" id="dataPage">
			<div class="left" style="margin-bottom:5px;">
				<button type="button" id="" name="" class="delete_auth_btn btn1" onclick="aclManager.event.deleteAcl('view');">삭제</button>
				<button type="button" id="" name="" class="copy_auth_btn btn1" onclick="aclManager.event.copyAcl('view')">복사</button>
			</div>
			<form name="form_details" id='form_details'>
				<div class="doc_config_info">
					<input type="hidden" name="type" id="type" value='update'> 
					<input type="hidden" id="creator_id">
					<input type="hidden" name='acl_id' id="acl_id" /> 
					<input type="hidden" name="aclItemArrayList" id='aclItemArrayList' />
					<input type='hidden' name='open_id' id='open_id' /> 
					<input type='hidden' name='open_isgroup' id='open_isgroup' /> 
					<input type='hidden' name='sort_index' id='acl_sort_index'/>
					<input type='hidden' name='src_acl_name' id='src_acl_name'/>
					<input type='hidden' name='acl_type' id='acl_type'/>
					<table>
						<tr>
							<th>권한명</th>
							<td colspan="3">
								<input type="text" id="acl_name" name="acl_name" class="doc_authName">
							</td>
						</tr>
						<tr>
							<th>공개범위</th>
							<td colspan="3">
								<select id="acl_typeDdslick">
									<option value="ALL">전사</option>
									<option value="TEAM">부서</option>
									<option value="DEPT">하위부서포함</option>
									<option value="PRIVATE">공유안함</option>
								</select>
								<input type="text" id="open_name" name="open_name" class="doc_openRange readonly" readonly="readonly" style="height:19px;">
								<button type="button" class="btn1" style="height:22px;" onclick="aclManager.event.openRangeWindow();">선택</button>
							</td>
						</tr>
						<tr>
							<th>등록자</th>
							<td><label id="creator_name"></label></td>
							<th>등록일자</th>
							<td><label id="create_date"></label></td>
						</tr>
					</table>
					
				</div>
				
				<div class="acl_config_list"  id="targetAclItemGridList">
					<div class="config_list_btnGrp">
						<button type="button" id="" name="" class="add_aclUser btn1" onclick="aclManager.event.addAccessorWindow();">추가</button>
						<button type="button" id="" name="" class="delete_aclUser btn1" onclick="aclManager.event.deleteAccessor();">삭제</button>
					</div>
					
					<div class="acl_config_tbl">
						<table id="aclItemGridList"></table>
					</div>
				</div>
			</form>
		</div>
		<div class="sub_right" id="emptyPage" style="display:none;">
			<div class="emptypage" >
				<div class="nodata"></div>
			</div>
		</div>
	</div>
	<div class="btnGrp">
		<button type="button" class="btn1 bold" onclick="aclManager.event.submit();">저장</button>
		<button type="button" class="btn1" onclick="aclManager.event.cancelProc();">취소</button>
	</div>
</div>
<script type="text/javascript" src="${contextRoot}/js/docadmin/aclManager.js"></script>
<script type="text/javascript">
jQuery(function() {		
		
	exsoft.util.filter.maxNumber();			// maxlength
	exsoftAdminLayoutFunc.init.menuInit();
	exsoftAdminLayoutFunc.init.menuSelected('${topSelect}','${subSelect}');
	
	aclManager.init("${pageSize}");
	
	exsoft.util.grid.gridResize('aclGridList','targetAclGridList',110,0);
	exsoft.util.grid.gridResize('aclItemGridList','targetAclItemGridList',90,0);
});
</script>
<jsp:include page="/jsp/popup/registAclWindow.jsp"/>
<jsp:include page="/jsp/popup/selectAccessorWindow.jsp"/>
<jsp:include page="/jsp/popup/selectSingleUserWindow.jsp"/>
<jsp:include page="/jsp/popup/selectGroupWindow.jsp"/>