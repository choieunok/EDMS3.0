<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/registGroupWindow.js"></script>
<!-- 
	등록버튼 클릭, 하위부서 추가
	[2000][신규개발]	20150824	이재민 : 프로젝트부서 등록/수정/조회시 관리부서 추가
	[2001][신규개발]	20150824	이재민 : 프로젝트탭 선택시 부서명이아니라 프로젝트명으로 나오게 수정
 -->
<div class="subDept_add_wrapper hide"></div>
<div class="subDept_add hide">
	<div class="window_title subDept_add_title">
		하위부서 추가
        <a href="#" class="window_close"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
    </div>
    <div class="subDept_add_cnts">
    	<form id="p_group_form">
    	<input type="hidden" id="parent_id" name="parent_id" data-bind="parent_id" />
		<input type="hidden" id="map_id" name="map_id" data-bind="map_id" />
		<input type="hidden" id="group_full_path" name="group_full_path" data-bind="group_full_path" />
		<input type="hidden" id="manage_group_id" name="manage_group_id" data-bind="manage_group_id" />
	    	<table>
	    		<colgroup>
	    			<col width="150"/>
	    			<col width="350"/>
	    		</colgroup>
	    		<tr>
	    			<!-- [2001] -->
	    			<th><span id="g_name">부서명</span><span class="required">*</span></th>
	    			<td><input type="text" id="p_group_name_ko" data-bind="p_group_name_ko"  class="" placeholder="부서명을 입력하세요" onchange="registGroupWindow.event.groupNameChanged();" maxlength="32"></td>
	    		</tr>
	    		<tr>
	    			<!-- [2001] -->
	    			<th><span id="g_name_en">부서명(영문)</span></th>
	    			<td><input type="text" id="p_group_name_en" data-bind="p_group_name_en" placeholder="부서명(영문)을 입력하세요" maxlength="32"/></td>
	    		</tr>
	    		<tr>
	    			<th>정렬순서</th>
	    			<td><input type="text" id="p_sort_index" data-bind="p_sort_index" maxlength="4" placeholder="숫자를 입력해주세요" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')"></td>
	    		</tr>
	    		<tr>
	    			<th>사용여부</th>
	    			<td>
	    				<select id="p_group_status" data-bind="p_group_status">
	    					<option value="C">사용</option>
	    					<option value="D">미사용</option>
	    				</select>
	    			</td>
	    		</tr>
	    		<tr>
	    			<!-- [2001] -->
	    			<th><span id="g_full_path">부서경로</span></th>
	    			<td>
	    			<input type="text" id="p_group_full_path" data-bind="p_group_full_path" class="readonly" placeholder="" readonly="readonly" >
	    			<button type="button" onclick="javascript:registGroupWindow.event.selectGroup();" class="btn1" ><span>선택</span></button>
	    			</td>
	    		</tr>
	    		<!-- [2000] start -->
	    		<tr id="tr_manage_use" class="hide">
	    			<th>관리부서 사용여부</th>
	    			<td><input type="checkbox" id="is_manage_use" 
	    			onclick="javascript:registGroupWindow.event.checkManageNoUse();" />&nbsp;미사용</td>
	    		</tr>
	    		<tr id="tr_manage_group" class="hide">
	    			<th>관리부서 선택</th>
	    			<td>
	    				<input type="text" id="p_manage_group" data-bind="p_manage_group" class="readonly" placeholder="" readonly="readonly" >
	    				<button type="button" id="btn_manage_group" onclick="javascript:registGroupWindow.event.selectManageGroup();" class="btn1" ><span>선택</span></button>
	    			</td>
	    		</tr>
	    		<!-- [2000] end -->
	    	</table>
    	</form>
    </div>
   	<div class="btnGrp">
   		<button type="button" id="" class="subDept_add_btn bold" onclick="javascript:registGroupWindow.event.registGroupSubmit();">확인</button>
   		<button type="button" id="" class="subDept_cancel_btn cancel" onclick="javascript:registGroupWindow.close();">취소</button>
   	</div>
</div>