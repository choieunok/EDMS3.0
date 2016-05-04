<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
	그룹관리
	[2000][신규개발]	20150825	이재민 : 프로젝트부서 등록/수정/조회시 관리부서 추가
	[2001][신규개발]	20150825	이재민 : 프로젝트탭 선택시 부서명이아니라 프로젝트명으로 나오게 수정
	[2003][신규개발]	20160308	이재민 : 부서수정시 dept_cd가 수정가능하게 화면수정
	(정종철 차장님이 부서-사용자배치시 하나INS및 기타 개발자부서 등록시 계정계 dept_cd에 맞게 수정가능하도록 수정요청함)
 -->
 <div class="cnt_wrapper">
	<nav class="lnb_menu">
		<div class="lnb_menu_tab">
			<ul>
				<li id="tab_group" class="menu_dept selected"><a href="javascript:void(0);" onclick="javascript:groupManager.init.tabControl('tab_group');">부서</a></li>
				<li id="tab_project" class="menu_project" ><a href="javascript:void(0);" onclick="javascript:groupManager.init.tabControl('tab_project');">프로젝트</a></li>
			</ul>
		</div>
		<div class="lnb_tree_menu">
			<div class="lnb_tree_tab">
				<ul>
					<li><a href="javascript:void(0);" onclick="javascript:groupManager.event.registGroup();">등록</a></li>
					<li><a href="javascript:void(0);" onclick="javascript:groupManager.event.moveGroup();">이동</a></li>
					<li><a href="javascript:void(0);" onclick="javascript:groupManager.event.deleteGroup();">삭제</a></li>
					<li id="upload_btn"><a href="javascript:void(0);" onclick="javascript:groupManager.event.groupExcelUpload();">일괄 업로드</a></li>
					<li id="upload_sample"><a href="javascript:void(0);" onclick="javascript:excelUploadWindow.event.groupDownload();">업로드 샘플</a></li>
				</ul>
			</div>
			<div class="lnb_tree_refresh"><a href="javascript:void(0);" class="refresh" onclick="javascript:groupManager.event.refreshTree();"></a></div><!-- TODO REFRESH-->
		</div>
		<div class="lnb_systemTree"><!-- CSS TODO -->
			<div id="groupTree"></div>
			<div id="projectTree" class="hide"></div>
		</div>
    </nav>

	<div class="contents">
		<div class="depth_navigation">${menuInfo.su_menu_nm}  &gt; ${menuInfo.menu_nm}</div>
		<div class="btnGrp left">
			<button type="button" class="delete_group btn1" onclick="javascript:groupManager.event.deleteGroup();">삭제</button>
		</div>
		<div class="cnts_sub_wrapper" style="border-bottom:1px solid #fff">
			<div class="group_info">
				<form id="group_form" name="form_details">
					<input type="hidden" id="group_id" name="group_id" data-bind="group_id" />
					<input type="hidden" id="parent_id" name="parent_id" data-bind="parent_id" />
					<input type="hidden" id="map_id" name="map_id" data-bind="map_id" />
					<input type="hidden" id="user_id_list" name="user_id_list" data-bind="user_id_list" />
					<input type="hidden" id="manage_group_id" name="manage_group_id" data-bind="manage_group_id" />
					<table>
					<colgroup>
						<col width="150"/>
						<col width="830"/>
					</colgroup>
					<%--
					<tr>
					<th><span id="group_name_title" class="bold"></span></th>
					<th><button type="button" class="delete_group btn1" onclick="javascript:groupManager.event.deleteGroup();">삭제</button></th>
					</tr>
					--%>
					<tr>
					<!-- [2001] -->
					<th><span id="g_m_name">부서명</span><span class="required">*</span></th>
					<td><input type="text" id="group_name_ko" name="group_name_ko" data-bind="group_name_ko" maxlength="32" class=""></td>
					</tr>
					<tr>
					<!-- [2001] -->
					<th><span id="g_m_name_en">부서명(영문)</span></th>
					<td><input type="text" id="group_name_en" name="group_name_en" data-bind="group_name_en" maxlength="32" class=""></td>
					</tr>
					<!-- [2003] -->
					<tr>
					<th>부서코드</th>
					<td><input type="text" id="dept_cd" name="dept_cd" data-bind="dept_cd" maxlength="16" class=""></td>
					</tr>
					<tr>
					<th>정렬순서</th>
					<td><input type="text" id="sort_index" name="sort_index" data-bind="sort_index" maxlength="4" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')"></td>
					</tr>
					<tr>
					<th>사용여부</th>
					<td>
						<select id="group_status" data-bind="group_status">
							<option value="C">사용</option>
							<option value="D">미사용</option>
						</select>
					</td>
					</tr>
					<tr>
					<th>등록일</th>
					<td><span id="create_date" class="darkgraytext" data-bind="create_date" ></span></td>
					</tr>
					<tr>
					<!-- [2001] -->
					<th><span id="g_m_full_path">부서경로</span></th>
					<td><span id="group_full_path" class="darkgraytext" data-bind="group_full_path" ></span></td>
					</tr>
					<!-- [2000] start -->
					<tr id="tr_manage_info">
						<th>관리부서</th>
						<td>
						<input type="text" id="manage_group" name="manage_group" data-bind="manage_group" maxlength="32" class="readonly" placeholder="" readonly="readonly">
						<button type="button" id="btn_select_group" onclick="javascript:groupManager.event.selectManageGroup();" class="btn1" ><span>선택</span></button>
						&nbsp;&nbsp;<input type="checkbox" id="m_is_manage_use" onclick="javascript:groupManager.event.checkManageNoUse();" />&nbsp;미사용
						</td>
					</tr>
					<!-- [2000] end -->
					</table>
				</form>
			</div>
			
			<div class="group_list" id="targetUserList">
				<table>
				<colgroup>
					<col width="150" />
					<col width="830"/>
				</colgroup>
				<tr>
				<th><span class="bold">부서원목록</span></th>
				<th>
					<button type="button" id="" name="" class="add_groupList bold btn1" onclick="javascript:groupManager.event.addUser();">추가</button>
					<button type="button" id="" name="" class="remove_groupList btn1" onclick="javascript:groupManager.event.removeUser();">제외</button>
					<!-- 
					<button type="button" id="" name="" class="bold btn1" onclick="javascript:groupManager.event.updateGroup();">저장</button>
					<button type="button" id="" name="" class="btn1" onclick="javascript:groupManager.init.initPage('MYDEPT', '${pageSize}');">취소</button>
					 -->
				</th>
				</tr>
				</table>
				
				<div class="cnts_list">
					<!-- jqGrid 영역 -->
					<table id="userList"></table>
					
					
                </div>
                
		
			</div>
		</div>
		<div class="btnGrp">
			<button type="button" class="btn1 bold" onclick="javascript:groupManager.event.updateGroup();">저장</button>
			<button type="button" class="btn1" onclick="javascript:groupManager.init.initPage('MYDEPT', '${pageSize}');">취소</button>
		</div>
	</div>
</div>
<script type="text/javascript" src="${contextRoot}/js/sysadmin/groupManager.js"></script>
<script type="text/javascript">
jQuery(function() {	
	// 상단메뉴 선택 변경처리
	exsoftAdminLayoutFunc.init.menuInit();
	exsoftAdminLayoutFunc.init.menuSelected('${topSelect}','${subSelect}');
	
	groupManager.init.initPage('MYDEPT', '${pageSize}');
	
	exsoft.util.grid.gridResize('userList','targetUserList',40);
	exsoft.util.filter.maxNumber(); 

});
</script>

<jsp:include page="/jsp/popup/registGroupWindow.jsp" /><!-- 등록버튼 클릭, 하위부서 추가 -->
<jsp:include page="/jsp/popup/selectGroupWindow.jsp" /><!-- 부서 선택 -->
<jsp:include page="/jsp/popup/excelUploadWindow.jsp" /><!-- 엑셀 일괄 업로드 -->
<jsp:include page="/jsp/popup/selectMultiUserWindow.jsp" /><!-- 구성원추가 -->