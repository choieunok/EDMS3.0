<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/selectAccessorWindow.js"></script>
<!-- 
	Usage :
		문서등록 권한 추가 접근자 화면(문서권한)
		권한관리 등록/수정 권한추가 화면(폴더/문서)				 
 -->
 
 <!-- 구성원 추가 끝 시작 -->
<div class="grpDeptUser_add_wrapper hide"></div>
<div class="grpDeptUser_add hide">
	<div class="grpDeptUser_add_title">
		부서/사용자 추가
		<a href="#" class="grpDeptUser_add_close" onclick="selectAccessorWindow.close();"><img src="${contextRoot}/img/icon/window_close.png"></a>
	</div>
	<div class="grpDeptUser_add_cnts">
		<div class="grpDeptUser_left">
			<div class="grpDeptUser_leftTop">
				<div class="grpDeptUser_deptTree">
					<div class="grpDeptUser_subtitle">
						<span class="ft_left">구분</span>
						<select id="selectAccessorWindowMapList" style="height:19px">
							<option value="GROUP">부서</option>
							<option value="PROJECT">프로젝트</option>
						</select>
					</div>
	        		
	        		<!-- 트리 영역 -->
					<div class="tree_cnts" id="accessorWindow_groupTree"></div>
					<div class="tree_cnts hide" id="accessorWindow_projectTree"></div>
				</div>
	        	
				<div class="grpDeptUser_deptUserList" >
					<div class="grpDeptUser_subtitle">사용자 목록</div>					
					<!-- 그리드 영역 -->
					<div class="srch_result_list" id="accessorWindowGroupUserGridTarget">
						<table id="initTreeGroupUser"></table>
					</div>
				</div>
			</div>
			
			<div class="grpDeptUser_leftBottom">
				<div class="srch_form right">
					<label>사용자/부서
						<input type="text" id="searchAccessorKeyword"placeholder="검색어를 입력하세요"
						onkeypress="javascript:return selectAccessorWindow.event.enterKeyPress(event);">
					</label>
					<button type="button" class="btn2" onclick="selectAccessorWindow.event.searchAccessor();">검색</button>
				</div>
				
				<!-- 결과 그리드 -->
				<div class="srch_result_list" id="searchUserListTarget">
					<table id="initSearchGroupUser">
					</table>
				</div>
			</div>
        	
		</div>
        
		<div class="grpDeptUser_center">
			<div class="grpDeptUser_addSubBtnGrp">
				<a href="#" onclick="selectAccessorWindow.event.addAccessor();"><img src="${contextRoot}/img/icon/add_user.png"></a>	
			</div>
		</div>
        
		<div class="grpDeptUser_right">
			<!-- 그리드 영역 :: 높이조정처리-->
			<div class="grpDeptUser_list1" style="overflow-y:auto;">
				<table id=""><tr><th>사용자/부서</th></tr></table>
				<ul class="chosen_user_list" id="accesorWindowSelectedList"></ul>
			</div>
			
			<!-- 권한 영역 -->
			<div class="grpDeptUser_acl">
				<table>
					<thead>
					<tr>
        				<th colspan="5">권한설정</th>
        			</tr>
					<tr>
						<th>구분</th>
						<th>기본권한</th>
						<th>등록</th>
						<th>반출취소</th>
						<th>권한변경</th>
					</tr>
					</thead>
					<tbody>
						<tr id="selectAccessorWindowFolderTr">
							<th>폴더권한</th>
							<td>
								<select id="selectAccessorWindowDefaultFolderAcl">
									<option value="READ" selected="selected">조회</option>
									<option value="UPDATE">수정</option>
									<option value="BROWSE">목록</option>
									<option value="DELETE">삭제</option>
								</select>
							</td>
							<td><input type="checkbox" id="accessorWindowFolActCreate"></td>
							<td>해당없음</td>
							<td><input type="checkbox" id="accessorWindowFolActChangePermission"></td>
						</tr>
						<tr>
							<th>문서권한</th>
							<td>
								<select id="selectAccessorWindowDefaultDocAcl">
									<option value="READ" selected="selected">조회</option>
									<option value="UPDATE">수정</option>
									<option value="BROWSE">목록</option>
									<option value="DELETE">삭제</option>
								</select>
							</td>
							<td><input type="checkbox" id="accessorWindowDocActCreate"></td>
							<td><input type="checkbox" id="accessorWindowDocActCancelCheckout"></td>
							<td><input type="checkbox" id="accessorWindowDocActChangePermission"></td>
						</tr>
					</tbody>
        		</table>
			</div>
		</div>
	</div>
	<div class="grpDeptUser_add_btnGrp">
		<button type="button" onclick="selectAccessorWindow.event.submit();">저장</button>
		<button type="button" onclick="selectAccessorWindow.close();">취소</button>
	</div>
</div>

<script type="text/javascript">
	jQuery(function() {

	    // 문서수정,문서등록>권한변경>추가접근자추가> : 음영진 부분 클릭 시 닫기
	    $('.grpDeptUser_add_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.grpDeptUser_add').addClass('hide');
	    });
	});
	</script>

<!-- 구성원 추가 끝 -->