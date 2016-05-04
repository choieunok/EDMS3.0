<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/selectMultiUserWindow.js"></script>
<!-- 그룹관리 > 구성원 추가 -->
<div class="grpDeptUser_add_wrapper hide"></div>
   	<div class="grpDeptUser_add hide">
   		<div class="grpDeptUser_add_title">
   			구성원 추가
            <a href="#" class="grpDeptUser_add_close"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
        </div>
        <div class="grpDeptUser_add_cnts">
	        <div class="grpDeptUser_left">
	        	<div class="grpDeptUser_leftTop">
	        		<div class="grpDeptUser_deptTree1">		        		
		        		<div id="pop_groupTree" class="tree_cnts"></div><!-- 트리 영역 -->
		        	</div>
	        	</div>	        	
	        	<div class="grpDeptUser_leftBottom">
	        		<div class="srch_form right">
	        			<form>
	        				<label>부서원명
	        					<input type="text" id="strKeyword" name="" class="" placeholder="검색어를 입력하세요"
	        					onkeypress="javascript:return selectMultiUserWindow.event.enterKeyPress(event);">
	        				</label>
	        				<button type="button" id="" class="btn2" onclick="javascript:selectMultiUserWindow.event.searchGroupUser();">검색</button>
	        			</form>
	        		</div>
	        		<div class="srch_result_list">
	        			<table id="pop_userList"></table>	<!-- 결과 그리드 -->
	        		</div>
	        	</div>	        	
	        </div>	        
	        <div class="grpDeptUser_center">
	        	<div class="grpDeptUser_addSubBtnGrp">
	        		<a href="javascript:void(0);" onclick="javascript:selectMultiUserWindow.event.appendUser();"><img src="${contextRoot}/img/icon/add_user.png" alt="" title=""></a>	
	        		<a href="javascript:void(0);" onclick="javascript:selectMultiUserWindow.event.removeUser();"><img src="${contextRoot}/img/icon/sub_user.png" alt="" title=""></a>	
	        	</div>
	        </div>
	        
	        <div class="grpDeptUser_right">	        	
	        	<div class="grpDeptUser_list1">
	        		<table id="memberList"></table><!-- 그리드 영역 -->
	        	</div>
	        </div>
        </div>
        <div class="grpDeptUser_add_btnGrp">
        	<button type="button" id="" class="" onclick="javascript:selectMultiUserWindow.event.submit();">저장</button>
        	<button type="button" id="" class="" onclick="javascript:selectMultiUserWindow.close();">취소</button>
        </div>
    </div>
    
    <script type="text/javascript">
	jQuery(function() {

	    //  구성원추가: 음영진 부분 클릭 시 닫기
	    $('.grpDeptUser_add_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.grpDeptUser_add').addClass('hide');
	    });
	});
	</script>
    
   	<!-- 구성원 추가 끝 -->