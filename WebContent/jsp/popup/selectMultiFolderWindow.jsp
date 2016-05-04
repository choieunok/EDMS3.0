<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/selectMultiFolderWindow.js"></script>
<!-- 
	Usage : 
	- 문서등록/수정시 다차원 분류 찾기 
	- 개인/업무/나의문서함 등
 -->
 <script type="text/javascript">
	exsoft.util.common.ddslick("#doc_multifolder_list", 'doc_folder_list', '', 262, selectMultiFolderWindow.callback.changedDocFolderList);
</script>

<div class="multifolder_choose_wrapper hide"></div>
<div class="doc_multifolder_choose hide">
	<div class="doc_folder_title doc_multifolder_choose_title">
		폴더 선택 
		<a href="javascript:void(0);" class="doc_folder_close"><img src="${contextRoot}/img/icon/window_close1.png" onclick="javascript:selectMultiFolderWindow.close();" alt="" title=""></a>
	</div>
	<div class="doc_folder_cnts">
		<div class="doc_folder_box">
			<span id="lb_multiFolderWorkspace" class="box_lbl" style="margin-top:6px;margin-bottom:5px;margin-right:5px;">문서함</span> 
			<select id="doc_multifolder_list">
				<option value="WORK_MYDEPT">부서</option>
				<option value="WORK_ALLDEPT">전사</option>
				<option value="WORK_PROJECT">프로젝트</option>				
			</select>
		</div>

		<div id="multiFolderMypageTree" class="doc_folder_tree"></div>
		<div id="multiFolderMydeptTree" class="doc_folder_tree hide"></div>
		<div id="multiFolderAlldeptTree" class="doc_folder_tree hide"></div>
		<div id="multiFolderProjectTree" class="doc_folder_tree hide"></div>


		<div class="doc_folder_btnGrp1">
			<button type="button" class="add_folderList" onclick="javascript:selectMultiFolderWindow.event.multiFolderAddFolder();">추가</button>
			<button type="button" class="sub_folderList" onclick="javascript:selectMultiFolderWindow.event.multiFolderRemoveFolder();">제외</button>
		</div>
		<div class="doc_folder_chooseList">
			<div class="folder_chooseList_title">[선택폴더]</div>
			
			<div class="folder_chooseList_cnts"  >
				<ul id="multiFolderSelectedfolderList" ></ul>
			</div>
		</div>
		<div class="doc_folder_btnGrp2">
			<button type="button" class="confirm" onclick="javascript:selectMultiFolderWindow.event.submit();">확인</button>
			<button type="button" class="cancel" onclick="javascript:selectMultiFolderWindow.close();">취소</button>
		</div>
	</div>
</div>

<script type="text/javascript">
	jQuery(function() {

	    // 문서수정,문서등록>다차원분류 : 음영진 부분 클릭 시 닫기
	    $('.multifolder_choose_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.doc_multifolder_choose').addClass('hide');
	    });
	});
	</script>