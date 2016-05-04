<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 

 	[1001][EDMS-REQ-070~81]	2015-09-14	최은옥	 : 외부시스템 연계코드관리(관리자 화면)
	인터페이스코드관리
 --> externalGridList
<div class="cnt_wrapper">
	<div class="depth_navigation">${menuInfo.su_menu_nm} > ${menuInfo.menu_nm}</div>
	
	<div class="doc_sub_wrapper">
		<div class="sub_leftex"  >
			<div class="srch_form">
				<span class="srch_doc_lbl">연계코드</span>
				<input type="text" name="strKeyword" id="strKeyword" class="srch_doc_keyword" maxlength="20" onkeydown="javascript:if(event.keyCode == 13) { exsoftAdminExternalFunc.event.searchFunc(); return false;}" >				
				<button type="button" class="menuAcl_roleName_find btn1" onclick="javascript:exsoftAdminExternalFunc.event.searchFunc();"></button>
			</div>
			<div class="srch_result_wrapper">
				<div class="left">
					<button type="button" id="" name="" class="btn1" onclick="javascript:exsoftAdminExternalFunc.open.externalAdd();">등록</button>
					<button type="button" id="" name="" class="btn1" onclick="javascript:exsoftAdminExternalFunc.event.externalDel();">삭제</button>					
					<div class="menu_imgBtnGrp1">
						<a href="javascript:void(0);" class="menu_refresh" onclick="javascript:exsoft.util.grid.gridRefresh('externalGridList','${contextRoot}/admin/externalList.do')">
						<img src="${contextRoot}/img/icon/menu_refresh.png" alt="" title=""></a>
					</div>
				</div>
				<div class="srch_result" id="targetExternalGrid">
					<table id="externalGridList"></table>					
				</div>
			</div>
			<div class="pg_subWrapper">
				<div class="pg_navi_wrapper">
					<ul class="pg_navi" id="externalGridPager"></ul> 	            
	           	</div>					
			</div>
		</div>

		<div class="sub_rightex" id="dataPage">
		<form name='frmView' id="frmView">
			<input type="hidden" name="attrArrayList"> 
			<input type="hidden" name="type">
			<div class="doc_config_info">
				<table>
					<colgroup>
						<col width="118"/>
						<col width="764"/>
					</colgroup>
					<tr>
					<th class="normal">연계코드<span class="required">*</span></th>
					<td><input type="text"  name="work_code" data-bind="work_code" class="docType_id" readonly></td>
					</tr>
					<tr>
					<th class="normal">연계명<span class="required">*</span></th>
					<td><input type="text" name="work_description" id="work_description" data-bind="work_description" class="docType_name"></td>
					</tr>
					<tr>
					<th class="normal">폴더ID<span class="required">*</span></th>
					<td><input type="text" name="folder_id" id="folder_id" data-bind="folder_id" class="docType_id" readonly>
					<button type="button" id="" name="" class="btn1" onclick="javascript:exsoftAdminExternalFunc.event.selectFolderFind('frmView');">선택</button>	
					</td>
					</tr>
					<tr>
					<th class="normal">폴더경로</th>
					<td><input type="text" name="folder_path" id="folder_path" data-bind="folder_path" class="docType_id" readonly>
					</td>
					</tr>
				</table>
			</div>
			
			
		</form>	
		</div>
		<div class="sub_rightex" id="emptyPage" style="display:none;">
			<div class="emptypage" >
				<div class="nodata"></div>
			</div>
		</div>	
	</div>
	<div class="btnGrp">
		<button type="button" class="btn1 bold"  id="attrUpdateBtn" onclick="javascript:exsoftAdminExternalFunc.event.applyExternalWrite('update');">저장</button>
		<!-- <button type="button" class="btn1"  id="attrCancelBtn" onclick="javascript:exsoftAdminExternalFunc.ui.cancelProc();">취소</button> -->
	</div>
</div>
<!-- 문서유형 등록 시작 -->
 <div class="register_docuType_wrapper hide"></div>
	<div class="register_docuType hide">
		<div class="window_title">
			연계코드 등록
         <a href="javascript:void(0);" class="window_close" onclick="javascript:exsoft.util.layout.divLayerClose('register_docuType_wrapper','register_docuType');"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
     </div>
    <form name="frm" id="frm">
	<input type="hidden" name="attrArrayList">
	<input type="hidden" name="type">
     <div class="register_docuType_cnts">
     	<table>
     		<tr>
     			<th>연계코드</th>
     			<td><input type="text" id="work_code" name="work_code"  data-bind="work_code" class="" maxlength="30" onkeypress="return exsoft.util.filter.inputBoxFilter('[A-Za-z0-9]');"  
     			maxlength="30" onkeyup="this.value=this.value.replace(/[^a-zA-Z]/g,'')" onfocusout="this.value=this.value.replace(/[^a-zA-Z]/g,'')" style="ime-mode:disabled; text-transform: uppercase;"></td>
     			<th>연계명</th>
     			<td><input type="text" id="" name="work_description" data-bind="work_description" class="" maxlength="20"></td>
     		</tr>
     		<tr>
     			<th>폴더 ID</th>
     			<td>
     				<input type="text" name="folder_id" id="folder_id" data-bind="folder_id" class="" readonly>
					<button type="button" id="" name="" class="btn1" onclick="javascript:exsoftAdminExternalFunc.event.selectFolderFind('frm');">선택</button>	
     			</td>
     			<th>폴더경로</th>
     			<td><input type="text" name="folder_path" id="folder_path" data-bind="folder_path" class="" readonly></td>
     		</tr>
     	</table>     
     </div>
     </form>
    	<div class="btnGrp">
    		<button type="button" id="" class="register_docuType_btn bold" onclick="javascript:exsoftAdminExternalFunc.event.applyExternalWrite('regist');">저장</button>
    		<button type="button" id="" class="cancel_docuType_btn cancel"  onclick="javascript:exsoft.util.layout.divLayerClose('register_docuType_wrapper','register_docuType');">취소</button>
    	</div>
 </div> 
 
 <script type="text/javascript">
	jQuery(function() {

	    //  : 음영진 부분 클릭 시 닫기
	    $('.register_docuType_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.register_docuType').addClass('hide');
	    });
	});
	</script>
 
<!-- 문서유형 등록 끝 -->
	   	
<!-- 항목 설정 시작 -->
<div class="register_docuCateType_wrapper hide"></div>
<div class="register_docuCateType hide">
	<div class="window_subtitle">
		항목 설정
        <a href="javascript:void(0);" class="window_close" onclick="javascript:exsoft.util.layout.divLayerClose('register_docuCateType_wrapper','register_docuCateType');"><img src="${contextRoot}/img/icon/window_close1.png" alt="" title=""></a>
    </div>
    <div class="register_docuCateType_cnts">
    	<div class="register_docuCateType_btnGrp">
    		<button type="button" id="" class="" onclick="javascript:exsoftAdminExternalFunc.ui.attrItemAdd();" >추가</button>
    		<button type="button" id="" class="" onclick="javascript:exsoftAdminExternalFunc.ui.attrItemDel();">삭제</button>
    	</div>
   		<table id="attrItemList">
    	<col style="width:10%">
		<col style="width:60%">
		<col style="width:30%">
		<tr>
			<th>
			<input type="checkbox" id="itemCheck" onclick="javascript:exsoft.util.common.allCheckBox('itemCheck','itemIdx');" /></th>
			<th>항목명</th>
			<th class="center">기본여부</th>
		</tr>
    	</table>
    </div>
   	<div class="btnGrp">
   		<button type="button" id="" class="register_docuType_btn bold" onclick="javascript:exsoftAdminExternalFunc.ui.applyAttrItem();">확인</button>
   		<button type="button" id="" class="cancel_docuType_btn cancel" onclick="javascript:exsoft.util.layout.divLayerClose('register_docuCateType_wrapper','register_docuCateType');">취소</button>
   	</div>
</div>
<!-- 항목 설정 끝 -->
<script type="text/javascript" src="${contextRoot}/js/docadmin/externalManager.js"></script>
<script type="text/javascript">  
jQuery(function() {		
	
	exsoft.util.filter.numLine();
	exsoft.util.filter.maxNumber();			// maxlength
	exsoftAdminLayoutFunc.init.menuInit();
	exsoftAdminLayoutFunc.init.menuSelected('${topSelect}','${subSelect}');

	exsoftAdminExternalFunc.init.initPage('#externalGridList','/admin/externalList.do','${pageSize}');
	exsoftAdminExternalFunc.event.fExternalListGrid();	
	exsoft.util.grid.gridResize('externalGridList','targetExternalGrid',20,0);
	/* exsoft.util.grid.gridResize('attrViewList','targetAttrViewList',20,0);

	// CheckBox Browser Bug Fix
	$('#cb_externalGridList').click(function(e) {
		exsoft.util.grid.checkBox(e);
    });
	
	$('#cb_attrViewList').click(function(e) {
		exsoft.util.grid.checkBox(e);
    });
	
	// 사용되는 레이아웃 POP DEFINE	

	exsoft.util.layout.lyrPopupWindowResize($(".register_docuType"));			
	exsoft.util.layout.lyrPopupWindowResize($(".register_docuCateType"));		
	exsoft.util.layout.lyrPopupWindowResizeArr([$(".register_docuType"),$(".register_docuCateType")]);	              
	$(window).resize(function(){		 
		exsoft.util.layout.lyrPopupWindowResize($(".register_docuType"));		
		exsoft.util.layout.lyrPopupWindowResize($(".register_docuCateType"));		
		exsoft.util.layout.lyrPopupWindowResizeArr([$(".register_docuType"),$(".register_docuCateType")]);
	});
	 */
});

jQuery(function() {

    //  : 음영진 부분 클릭 시 닫기
    $('.register_docuCateType_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.register_docuCateType').addClass('hide');
    });
});
</script>

<jsp:include page="/jsp/popup/selectSingleFolderWindow.jsp"/> <!-- 기본폴더 선택 -->
 