<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
	<< 아래의 나의문서(내문서) 페이지를 하나의 페이지에서 통합구현처리한다.>>
	1.내소유문서
	2.내수정중인문서
	3.내만기문서
	4.휴지통
	5.즐겨찾기폴더문서
	6.공유받은문서
	7.공유받은폴더문서
	8.작업카트(구:임시문서함)
	9.최신문서함(1차고도화 신규)
	
	[3000][EDMS-REQ-033],[EDMS-REQ-034]	2015-08-27	성예나	 : 만기사전문서,만기만료문서 리스트 구분 selectbox 추가
 -->
<div class="cnt_wrapper" id="workDocListBody">
	<jsp:include page="/jsp/layout/myPageMenu.jsp" />	  
	<!-- 나의문서 좌측메뉴 -->
	<div class="contents">
		<div class="cnts_srch_wrapper">
			<div class="toggle_tree_menu"></div>
			<div class="cnts_srch_detail">
				<div class="cnts_srch">
					<select id="myPageDoc_folder">
						<option value="MYDEPT" selected>업무문서함</option>
						<option value="MYPAGE">개인문서함</option>
					</select> 
					<select id="myPageDoc_srchType">
						<option value="doc_name">제목</option>
						<option value="doc_description">내용</option>
						<option value="creator_name">등록자</option>
						<option value="creator_id">등록자ID</option>
					</select>
					<select id="docReq_srchType" class="hide">
						<option>선택</option>
						<option value="doc_request">미승인</option>
						<option value="doc_approve">승인</option>
						<option value="doc_reject">반려</option>
					</select> 
					<select id="docAprv_srchType" class="hide">
						<option value="doc_name">제목</option>
						<option value="req_name">요청자</option>
					</select>  
					<input type="text" class="srch_keyword" id="strKeyword1" name="srch_keyword" placeholder="검색어를 입력하세요" 
					onkeypress="javascript:return exsoftMypageFunc.event.docFunctions.enterKeyPress(event);">
					<button type="button" class="srch_btn" onclick="javascript:exsoftMypageFunc.event.docFunctions.searchDocument();"></button>
				</div>
				<jsp:include page="/jsp/popup/docDetailSearch.jsp" /><!-- 상세검색 Layer 공통영역으로 분리 -->
			</div>
			<div class="depth_navi">
				<span id="myPageTitle"></span>
			</div>
		</div>
		<div class="cnts_tbl_menu">
			<div class="tbl_menu_left">
				<!-- 메뉴구분에 따라 동적으로 처리됨  -->
				<!--[3000]  -->
					<select class ="expiredDoc_check hide"  id="expiredDoc_check" >
                    <option value="expiredDoc" selected>만기만료문서</option>
					<option value="expiredCome" >만기사전문서</option>
					</select>
					
				<button type="button" class="tbl_reg" id="tbl_reg" class="hide" onclick="exsoft.document.layer.docWriteCommonFrm('doc_register_wrapper','doc_register','','','MYPAGE');">등록</button>
				<button type="button" class="tbl_del" id="tbl_del" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.documentDelete(exsoftMypageFunc.listType);">삭제</button>
				<button type="button" class="grant_transfer_btn" id="grant_transfer_btn" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.changeOwner('SELECT_DOC');">소유권이전</button>
				<button type="button" class="grant_transferAll_btn" id="grant_transferAll_btn" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.changeOwner('ALL_DOC');">소유권이전(전체)</button>
				<button type="button" class="checkout_cancel" id="checkout_cancel" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.documentCancelCheckout();">체크아웃취소</button>
				<button type="button" class="extend_preserve_btn" id="extend_preserve_btn" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.extendPreserve();">보존기간 연장</button>
				<button type="button" class="tbl_restore" id="tbl_restore" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.documentRestore();">복원</button>
				<button type="button" class="tbl_empty" id="tbl_empty" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.allDocumentDelete();">비우기</button>
				<button type="button" class="exclude_favorite" id="exclude_favorite" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.deleteFavoriteDocument();">즐겨찾기 제외</button>
				<button type="button" class="exclude_temp" id="exclude_temp" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.tempDocumentDelete();">작업카트 제외</button>
				<button type="button" class="set_relative_docs" id="set_relative_docs" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.tempRefDocList();">관련문서 설정</button>
				<button type="button" class="url_mailsend" id="url_mailsend" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.sendEmail();">URL 메일송부</button>
				<button type="button" class="down_load_file" id="down_load_file" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.documentDownLoad();">다운로드</button>
				<button type="button" class="add_favorite" id="add_favorite" class="hide" onclick="javascript:exsoftMypageFunc.event.docFunctions.addFavorite();">즐겨찾기 추가</button>
				<button type="button" class="aprv_rjct" id="aprv_rjct" class="hide" onclick="javascript:exsoftMypageFunc.event.approveOrRejectAll();">일괄 승인/반려</button>
				<div id="docListLayer_add" class="tbl_extFunction hide">
					<a class="dropDown_txt"> <label>추가기능</label>
						<span class="dropDown_arrow down" id="docListDropDown"></span>
					</a>

					<div class="extFunction_dropDown_wrapper hide" id="docListDropDownWrapper">
						<ul class="mypage_extFunction_dropDown_menu"></ul>
					</div>
				</div>
				
			</div>
			<div class="tbl_menu_right">
				<ul class="tbl_thumbMenu">
					<li class="excel_download" onclick="javascript:exsoft.util.grid.excelDown('mypageDocList', '${contextRoot}/mypage/authDocumentList.do');"><a href="#" class="menu"></a></li>
					<li class="layout_view"><a href="#" class="menu"></a>
						<div class="layout_view_dropDown hide">
							<ul>
								<li id="list_only"><a href="#">목록만 보기</a></li>
								<li id="horizontal_divide"><a href="#">좌우 분할로 보기</a></li>
								<li id="vertical_divide"><a href="#">상하 분할로 보기</a></li>
							</ul>
						</div>
					</li>
					<li class="menu_refresh" onclick="javascript:exsoftMypageFunc.event.docFunctions.refreshDocumentList();"><a href="#" class="menu"></a></li>
				</ul>
				<select id="mypageRows">
					<option value="5">5개</option>
					<option value="10" selected>10개</option>
                    <option value="15">15개</option>
                    <option value="20">20개</option>
                    <option value="30">30개</option>
                    <option value="50">50개</option>
				</select>
			</div>
		</div>
		<div class="cnts_tbl_wrapper" id="targetMypageDocList">
			<div class="cnts_list"><table id="mypageDocList"></table></div>
			<!-- 문서목록 리스트 -->
		</div>

		<div class="cnts_aside hide">
			<div class="horizontal_draggable"></div>
			<div class="aside_cnts_wrapper">
				<div class="cnts_sub_wrapper">
					<div class="aside_title">
                 			다음 설정을 통하여 편리하게 문서를 관리해보세요.
                   	</div>
                   	<div class="aside_cnts">
                   		<p>
                   			- 분할모드 첫화면 첫번째 문서 자동 보기 설정을 할 수 있습니다.
                   		</p>
                   	</div>
                   	<div class="aside_noread_title">
                   		해당 문서를 읽을 권한이 없습니다.
                   	</div>
                   	<div class="aside_noread_cnts">
                   		<p>- 열람요청후 확인하시기 바랍니다.</p>
                   	</div>
                   	<!-- preview document -->
                   	<div class="tab_form">
                   		<strong><label class="doc_detail_name hide" data-bind="doc_name_ko"></label></strong>
	                   	<div class="doc_detail_info hide">
							<span class="partitionDocInfo">
								<strong><label>문서 정보</label></strong>
							</span>
							<div class="tab_form">
								<table>
									<tr>
										<th>기본폴더</th>
										<td colspan="3"><label data-bind="folderPath"></label></td>
									</tr>
									<tr>
										<th>문서유형</th>
										<td><label data-bind="type_name"></label></td>
										<th>보존연한</th>
										<td><label data-bind="preservation_year"></label></td>
									</tr>
									<tr>
										<th>보안등급</th>
										<td><label data-bind="security_level"></label></td>
										<th>조회등급</th>
										<td><label data-bind="access_grade"></label></td>
									</tr>
									<tr>
										<th>등록자(소유자)</th>
										<td><label data-bind="creator_name"></label></td>
										<th>등록일</th>
										<td><label data-bind="create_date"></label></td>
									</tr>
									<tr>
										<th>수정자</th>
										<td><label data-bind="updater_name"></label></td>
										<th>수정일</th>
										<td><label data-bind="update_date"></label></td>
									</tr>
									<tr>
										<th>다차원 분류</th>
										<td colspan="3"><label data-bind="multiFolderList"></label></td>
									</tr>
									<tr>
										<th>키워드</th>
										<td colspan="3"><label data-bind="preview_keyword"></label></td>
									</tr>
									<tr>
										<th>설명</th>
										<td colspan="3">
											<span style='display: none'><TEXTAREA id='preview_content'></TEXTAREA></span>
											<iframe src='' id='preview_Iframe_editor' style='border: 0 solid transparent; padding: 10px 0 0; margin: 0; height: 120px; width: 99%'></iframe>
										</td>
									</tr>
								</table>
							</div>
						</div>
						<div class="doc_detail_auth hide">
							<a class="dropDown_txt">
								<strong>권한 : </strong><label data-bind="aclName"></label>
								<span class="dropDown_arrow down"></span>
							</a>
							<div class="doc_auth_cnts hide" >
								<table id="detail_docAclItemListHorizon">
									<thead>
				       					<tr><th colspan="5">기본 접근자</th></tr>
				         				<tr>
					         				<th>접근자</th>
					         				<th>기본권한</th>
					         				<th>문서등록</th>
					         				<th>반출취소</th>
					         				<th>권한변경</th>
				         				</tr>
				         			</thead>
				         			<tbody></tbody>
								</table>
								<table id="detail_docExAclItemListHorizon">
									<thead>
				       					<tr><th colspan="5">추가 접근자</th></tr>
				         				<tr>
					         				<th>접근자</th>
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
				</div>
			</div>
		</div>
		<div class="cnts_bottom hide" style="z-index:1">
			<div class="vertical_draggable"></div>
			<div class="bottom_cnts_wrapper">
				<div class="bottom_title">
					다음 설정을 통하여 편리하게 문서를 관리해보세요.
				</div>
				<div class="bottom_cnts">
					<p>
						- 분할모드 첫화면 첫번째 문서 자동 보기 설정을 할 수 있습니다.
					</p>
               	</div>
               	<div class="bottom_noread_title">
               		해당 문서를 읽을 권한이 없습니다.
               	</div>
               	<div class="bottom_noread_cnts">
               		<p>- 열람요청후 확인하시기 바랍니다.</p>
               	</div>
				<!-- preview document -->
				<div class="tab_form">
					<strong><label class="doc_detail_name hide" data-bind="doc_name_ko"></label></strong>
	                   	<div class="doc_detail_info hide">
							<span class="partitionDocInfo">
								<strong><label>문서 정보</label></strong>
							</span>
							<div class="tab_form">
								<table>
									<tr>
										<th>기본폴더</th>
										<td colspan="3"><label data-bind="folderPath"></label></td>
									</tr>
									<tr>
										<th>문서유형</th>
										<td><label data-bind="type_name"></label></td>
										<th>보존연한</th>
										<td><label data-bind="preservation_year"></label></td>
									</tr>
									<tr>
										<th>보안등급</th>
										<td><label data-bind="security_level"></label></td>
										<th>조회등급</th>
										<td><label data-bind="access_grade"></label></td>
									</tr>
									<tr>
										<th>등록자(소유자)</th>
										<td><label data-bind="creator_name"></label></td>
										<th>등록일</th>
										<td><label data-bind="create_date"></label></td>
									</tr>
									<tr>
										<th>수정자</th>
										<td><label data-bind="updater_name"></label></td>
										<th>수정일</th>
										<td><label data-bind="update_date"></label></td>
									</tr>
									<tr>
										<th>다차원 분류</th>
										<td colspan="3"><label data-bind="multiFolderList"></label></td>
									</tr>
									<tr>
										<th>키워드</th>
										<td colspan="3"><label data-bind="preview_keyword"></label></td>
									</tr>
									<tr>
										<th>설명</th>
										<td colspan="3">
											<span style='display: none'><TEXTAREA id='preview_content'></TEXTAREA></span>
											<iframe src='' id='preview_Iframe_editor2' style='border: 0 solid transparent; padding: 10px 0 0; margin: 0; height: 120px; width: 99%'></iframe>
										</td>
									</tr>
								</table>
						</div>
					</div>
					<div class="doc_detail_auth hide">
							<a class="dropDown_txt">
								<strong>권한 : </strong><label data-bind="aclName"></label>
								<span class="dropDown_arrow down"></span>
							</a>
							<div class="doc_auth_cnts hide" >
								<table id="detail_docAclItemListVertical">
									<thead>
				       					<tr><th colspan="5">기본 접근자</th></tr>
				         				<tr>
					         				<th>접근자</th>
					         				<th>기본권한</th>
					         				<th>문서등록</th>
					         				<th>반출취소</th>
					         				<th>권한변경</th>
				         				</tr>
				         			</thead>
				         			<tbody></tbody>
								</table>
								<table id="detail_docExAclItemListVertical">
									<thead>
				       					<tr><th colspan="5">추가 접근자</th></tr>
				         				<tr>
					         				<th>접근자</th>
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
			</div>
		</div>
		
		<div class="pg_navi_wrapper">
           	<ul class="pg_navi" id="mypagePager"></ul>    
		</div>
	</div>
</div>
<!-- Tool Tip Layer -->
<div class='tooltip hide'></div>

<!-- Div Layer add -->
<script type="text/javascript" src="${contextRoot}/js/mypage/myPageDocList.js"></script>
<script type="text/javascript">
jQuery(function() {
	
	exsoftMypageFunc.init.initPage("${menuType}","${myMenuType}",'myPageTitle',"${tempDocDownloadMax}", '${pageSize}', '${role_id}');
	exsoft.util.grid.gridResize('mypageDocList','targetMypageDocList',28,0);

	// Tree 메뉴 처리	
    $('.mydoc_shared_folder').on('click', function () {
    	$("#mydoc_shared_folderTree").slideToggle("fast");
	}).find('.refresh').on('click', function (e) {
		e.stopPropagation();
	});

    $('.mydoc_favorite').on('click', function () {
    	$("#mydoc_favorite_tree").slideToggle("fast");
	}).find('.refresh').on('click', function (e) {
		e.stopPropagation();
	});

    // 상세검색 초기화
    docDetailSearch.targetGridId = "mypageDocList";
 // 사용되는 레이아웃 POP DEFINE
	exsoft.util.layout.lyrPopupWindowResize($(".doc_detail"));
	exsoft.util.layout.lyrPopupWindowResize($(".doc_register"));
	$(window).resize(function(){		 
		exsoft.util.layout.lyrPopupWindowResize($(".doc_detail"));
		exsoft.util.layout.lyrPopupWindowResize($(".doc_register"));
	});
	
	 //탭 요소 클릭 시 폼 변경
    $('.tab_element').bind("click", function(){
        var idx = $(this).index();
        var targetFrm = $(this).parent().parent().parent().find('div[class^="tab_form"]');
        targetFrm.addClass('hide');
        targetFrm.eq(idx).removeClass('hide');

        $('.tab_element').removeClass('selected');
        $(this).addClass('selected');
    }); 
});
</script>

<jsp:include page="/jsp/popup/documentListLayerWindow.jsp" /><!-- 목록관련 공통적용화면 --> 
<jsp:include page="/jsp/popup/extendDecade.jsp" /><!--  보존기간연장 Layer -->
<%-- <jsp:include page="/jsp/popup/selectSingleFolderWindow.jsp"/> --%><!-- 복원시 폴더트리 -->
<jsp:include page="/jsp/popup/registFavoriteFolderWindow.jsp"/><!-- 즐겨찾기 폴더 등록(수정) -->
<jsp:include page="/jsp/popup/selectFavoriteFolderWindow.jsp"/><!-- 즐겨찾기 선택 화면(폴더/문서)  -->
<jsp:include page="/jsp/popup/configFavoriteFolder.jsp"/><!-- 즐겨찾기 폴더구성	 -->
<jsp:include page="/jsp/popup/selectSingleUserWindow.jsp" />	<!-- 소유권변경 - 사용자 선택 -->
<jsp:include page="/jsp/popup/registMail.jsp" /><!-- 작업카트 - URL메일송부 -->
<jsp:include page="/jsp/popup/configRelDoc.jsp" /><!-- 작업카트 - 관련문서 설정 -->
<jsp:include page="/jsp/popup/documentApproveCommentWindow.jsp"/><!-- 승인/반려 사유 팝업 -->
<jsp:include page="/jsp/popup/documentReadRequestWindow.jsp"/><!-- 열람요청팝업 -->