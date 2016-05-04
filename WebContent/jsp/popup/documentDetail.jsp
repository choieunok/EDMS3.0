<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<script type="text/javascript" src="${contextRoot}/js/common/base64.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/documentVersionDetail.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/documentListLayerWindow.js"></script>
<script type="text/javascript" src="${contextRoot}/js/popup/selectFavoriteFolderWindow.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/initbind.js"></script>

<!-- 
	Usage : 
	- 문서기본조회
	- URL복사 & 메일전송
	
	[2000][로직수정]	2015-09-03	이재민 : 추가기능 > 엑셀이력관리 버튼은 이력탭일때만 나올수있게 수정
	[2001][로직수정]	2015-09-03	이재민 : 나의문서 > 작업카트 - 추가기능 > 작업카트추가 hide처리
 -->
 
<form name="docVersionDetailFrm">
	<input type="hidden" name="docId">
</form>

<form name="mailReciverUserFrm"></form>

<div class="doc_detail_wrapper hide"></div>

<div class="doc_detail hide">
		<div class="doc_detail_title">
			문서 상세조회
			<a href="javascript:void(0);" class="doc_detail_close">
				<img src="${contextRoot}/img/icon/window_close2.png" alt="" title="">
			</a>
		</div>
		
		<div class="doc_detail_cnts">
		
			<div class="cnts_title"> <span class="title"></span>
				<input type="hidden" id="is_locked_val"/>
				<div class="cnts_abbr_info"> <span class="cnts_version" id="detail_doc_version"></span> <span class="cnts_locked"> <img id="detail_docLockIcon" src="${contextRoot}/img/icon/lock1.png" alt="" title="">
					<div class="locked_info hide" >
						<p id="lockerInfo"></p>
						<p id="lockerDate"></p>
					</div>
					</span>
				</div>
			</div>
			<div class="tab_menu">
				<div class="tab_elem_wrapper">
					<span class="tab_element selected" id="defaultDoc" onclick="javascript:exsoft.document.ui.docDetailSelectAction(0);">기본</span>
					<span class="tab_element" onclick="javascript:exsoft.document.ui.docDetailSelectAction(1);">버전</span> 
					<span class="tab_element" onclick="javascript:exsoft.document.ui.docDetailSelectAction(2);">이력</span> 
					<span class="tab_element" onclick="javascript:exsoft.document.ui.docDetailSelectAction(3);">의견(<span class="opinion_cnt">3</span>)</span> 
				</div>
				<div class="tab_btn_wrapper">
					<button type="button" id="docDetailModify" onclick='javascript:exsoft.document.layer.docUpdateCommonFrm("doc_modify_wrapper","doc_modify","");'>수정</button>
					<button type="button" id="docDetailDelete" class="delete" onclick="javascript:exsoft.document.event.detail_deleteDocument();">삭제</button>
					<!-- <button type="button" class="delete" onclick="javascript:exsoft.document.event.versionDelete(exsoft.document.prototype.gDocId);">삭제</button> -->
					<div class="tbl_extFunction" id="docDetail_extFunc"> <a class="dropDown_txt">
						<label>추가기능</label>
						<span class="dropDown_arrow down"></span> </a>
						<div class="extFunction_dropDown_wrapper hide">
							<ul class="extFunction_dropDown_menu">
								<li><a href="javascript:void(0);" class="move" onclick="javascript:exsoft.document.event.detail_moveDocument();">이동</a></li>
								<li><a href="javascript:void(0);" class="copy" onclick="javascript:exsoft.document.event.detail_copyDocument();">복사</a></li>
								<li><a href="javascript:void(0);" class="favorite" onclick="javascript:exsoft.document.event.documentAddFavorite();">즐겨찾기 추가</a></li>
								<!-- [2001] 작업카트추가 li에 id부여 -->
								<li id="ext_tempWork"><a href="javascript:void(0);" class="tempbox" onclick="javascript:exsoft.document.event.documentTempwork();">작업카트 추가</a></li> 
								<!-- [2000] 엑셀이력관리 li에 id부여 -->
								<li id="ext_archExcel"><a href="javascript:void(0);" class="archive_excel" onclick="javascript:exsoft.util.grid.excelDown('detaildocHistoryList','${contextRoot}/document/docHistoryList.do');">엑셀 이력관리</a></li>
								
							</ul>
						</div>
					</div>
					<button type="button" class="cancel_checkout" id="btn_detail_checkout" onclick="javascript:exsoft.document.event.Detail_DocumentUnLock();">체크아웃취소</button>
					
					<!-- <button type="button" class="btn_urlCopy" onclick="javascript:exsoft.document.event.sendUrlCopy();">URL복사</button> -->
					<button type="button" class="btn_urlCopy hide" onclick='javascript:exsoft.document.event.docDetailsendUrl("copy");'>URL복사</button>
					<button type="button" class="url_email_send hide" id="url_email_send" class="url_email_send hide" onclick='javascript:exsoft.document.event.docDetailsendUrl("mail");'>URL 메일송부</button>
				</div>
			</div>
			<div class="tab_form">
			
			
			<div class="doc_detail_attach">
				<a class="dropDown_txt">
					<label>
						<strong>첨부파일</strong>
						<span class="attach_cnt"></span>
						<span class="attach_size"></span>
					</label>
				</a>
				<span class="dropDown_arrow up"></span>
				<div class="attach_docs_wrapper">
					<div class="download_btnGrp">
					 	<button type="button" onclick="javascript:exsoft.document.event.attachToggleCheck(true,'detail_pageList_checkbox');">전체선택</button>
						<button type="button" onclick="javascript:exsoft.document.event.attachToggleCheck(false,'detail_pageList_checkbox');">전체해제</button>
						<button type="button" onclick="javascript:exsoft.document.event.viewattachSave('detail_pageList');">저장</button>						
						<!-- <button type="button" onclick="javascript:exsoft.document.event.viewattachDelete('detail_pageList_checkbox');">삭제</button> -->
					</div>				
					<ul id='detail_pageList'></ul>
				</div>
			</div>
				<div class="doc_detail_info">
					<a class="dropDown_txt">
						<label><strong>문서 정보</strong></label>
					</a>
					<span class="dropDown_arrow up"></span>
					<div class="detail_info_wrapper">
						<table id="docDetailBasicInfo" class="">
							
						</table>
					</div>
					
				</div>
				
				<div class="doc_detail_extend">
					<a class="dropDown_txt">
						<strong>확장유형 </strong>
						<span class="dropDown_arrow down"></span>
					</a>
					<div class="detail_extend_wrapper" >
						<table id="documentView_docAttrView">
							<thead></thead>
							<tbody></tbody>
						</table>
					</div>
				</div>
				
				
				<div class="doc_detail_auth">
					<a class="dropDown_txt">
						<strong>권한 : </strong><label id="docDetailAclName"></label>
						<span class="dropDown_arrow down"></span>
					</a>
					<div class="doc_auth_cnts hide" >
						<table id="detail_docAclItemList">
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
						<table id="detail_docExAclItemList">
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
				
				<!-- 관련문서 -->
				<div class="doc_detail_relative">
				<a class="dropDown_txt">
					<strong>관련문서</strong><label id="docRelativeTotal"></label>
					<span class="dropDown_arrow down"></span>					
				</a>
				
				<div class="relative_docs_wrapper hide">					
					<table class="rowtable_liteGray">
						<colgroup>
							<col style="width:60%">
							<col style="width:20%">
							<col style="width:20%">
        				</colgroup>
					
						<thead>
						<tr>
							<th>제목</th>
							<th>등록자</th>
							<th>등록일</th>
						</tr>
						</thead>
						<tbody id="docDetailRefInfo"></tbody>
					</table>
					
					<!-- <ul id='docDetailRefInfo'></ul> -->
				</div>
			</div>
			
				<!-- 협업정보 start -->
				<div class="coop_infomation hide">
					<a class="dropDown_txt">
						<label><strong>협업정보</strong></label>
					</a>
					<span class="dropDown_arrow down"></span>
					<div class="coop_infomation_wrapper hide">
						<table id='docProcessView_info'>
						<colgroup>
							<col width="117"/>
							<col width="528"/>
						</colgroup>
						<tr><th>업무명</th><td><span></span></td></tr>
						<tr><th>업무요청자</th><td><span></span></td></tr>
						<tr><th>업무완료 예정일</th><td><span></span></td></tr>
						<tr><th>업무요청 내용</th><td><span></span></td></tr>
						<tr><th>작성자</th><td><span></span></td></tr>
						<tr><th>공동작성자</th><td><span></span></td></tr>
						<tr><th>승인자</th>
							<td>
							<div class='approvalResult_receiver'>
								<a id='processView_approver' style='cursor:pointer;' onmousemove='javascript:$("#processView_approverTooltip").removeClass("hide");' onmouseout='javascript:$("#processView_approverTooltip").addClass("hide");'></a>
								<span></span>
								<div id='processView_approverTooltip' class="approvalResult_tooltip hide"></div>
							</div>
						</td>
						</tr>
						<tr>
						<th>수신자</th>
						<td>
							<div class='approvalResult_receiver'>
								<a id='processView_receiver' style='cursor:pointer;' onmousemove='javascript:$("#processView_receiverTooltip").removeClass("hide");' onmouseout='javascript:$("#processView_receiverTooltip").addClass("hide");'></a>
								<span></span>
								<div id='processView_receiverTooltip' class="approvalResult_tooltip hide"></div>
							</div>
						</td>
						</tr>
						</table>
					</div>
				</div>
				<!-- 협업정보 end -->
				
				<!-- 처리현황 start -->
				<div class="coop_approvalResult hide">
					<a class="dropDown_txt">
						<label>
							<strong>처리현황</strong>
							(<span class="approvalResult_cnt"></span>)
						</label>
					</a>
					<span class="dropDown_arrow down"></span>
					<div class="approvalResult_wrapper hide">
						<ul id='docProcessView_situation'></ul>
					</div>
				</div>
				<!-- 처리현황 end -->
			</div>
			<div class="tab_form hide">
				<div class="doc_version" >
				
					<table>
						<colgroup>
						<col width="65"/>
						<col width="292"/>
						<col width="95"/>
						<col width="144"/>
						</colgroup>
						<thead>
							<tr>
								<th>버전</th>
								<th>문서명</th>
								<th>등록자</th>
								<th>등록일</th>
							</tr>
						</thead>
						<tbody id="detaildocVersionList">
							
						</tbody>
					</table>
				</div>
				
			</div>
			
			
			
			<div class="tab_form hide">
				<div class="archive_wrapper_"  id="targetDocHistoryList">
				
					<table id="detaildocHistoryList"></table>	
				</div>
				<div class="pg_navi_wrapper">
					<ul class="pg_navi" id="historyGridPager"></ul>
				</div> 
			</div>
			<div class="tab_form hide">
				<div class="opinion_wrapper">
					<table>
						<!-- <colgroup>
						<col width="85"/>
						<col width="84"/>
						<col width="316"/>
						<col width="134"/>
						</colgroup> -->
						<thead>
							<tr>								
								<th>의견 등록자</th>
								<th>답글 대상자</th>
								<th>내용</th>
								<th>등록일시
									<a href="javascript:void(0);"><img src="${contextRoot}/img/icon/head_dropdown.png" alt="" title=""></a>
								</th>
							</tr>
						</thead>
						<tbody  id="detaildocCommentList"></tbody>
					</table>
					<div class="opinion_contextMenu hide">
						<ul>
							<li><a href="javascript:exsoft.document.event.commentAction('REPLY');" class="opinion_reply"><span class="left"></span><span class="right">답글</span></a></li>
							<li><a href="javascript:exsoft.document.event.commentAction('UPDATE');" class="opinion_modify"><span class="left"></span><span class="right">수정</span></a></li>
							<li><a href="javascript:exsoft.document.event.commentAction('DELETE');" class="opinion_delete"><span class="left"></span><span class="right">삭제</span></a></li>
						</ul>
					</div>
				</div>
				<div class="opinion_writeform">
					<form>
						<div class="opinion_account">
							<img src="${contextRoot}/img/icon/user_info.png" alt="">
							<span class="account_nm">홍길동</span>
							<div class="opinion_btnGrp">
								<button type="button" class="refresh">새로고침</button>
								<button type="button" class="register" onclick="javascript:exsoft.document.event.docCommentUpdate();">등록</button>
								<!-- <button type="button" class="delete">삭제</button> -->
							</div>
						</div>
						<div class="opinion_cnt_wrapper">
							<textarea class="" name=""></textarea>
						</div>
					</form>
				</div>
			</div>
			<!-- URL 붙여넣기, 메일 내용 -->
			<div class="url_paste hide">
				<div class="url_paste_title">
					URL 붙여넣기 &amp; 메일 내용
					<a href="javascript:void(0);" class="url_paste_close"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
				</div>
				<div class="url_paste_cnts"  id="copyToUrl">
					<table style="border:1px solid #ccc; background:#fff; border-collapse:collapse;font-family:verdana,arial; font-size: 12px; ">
						<colgroup>
						<col width="101"/>
						<col width="126"/>
						<col width="97"/>
						<col width="115"/>
						</colgroup>
						<tr>
							<th  style="text-align:left; background:#f7f7f7; border:1px solid #ccc;">문서제목</th>
							<td colspan="3" id="copy_doc_name"  style="text-align:left; border:1px solid #ccc;">Anydocu_gluesys11103번째 문서 1.1</td>
						</tr>
						<tr>
							<th  style="text-align:left; background:#f7f7f7; border:1px solid #ccc;">폴더</th>
							<td colspan="3" id="copy_folderPath"  style="text-align:left; border:1px solid #ccc;">/엑스소프트/영업팀/영업팀/하위_11</td>
						</tr>
						<tr>
							<th  style="text-align:left; background:#f7f7f7; border:1px solid #ccc;">등록자[소유자]</th>
							<td id ="copy_creator_name"  style="text-align:left; border:1px solid #ccc;">김종민 [김주혁]</td>
							<th style="text-align:left; background:#f7f7f7; border:1px solid #ccc;">등록일</th>
							<td id="copy_create_date"  style="text-align:left; border:1px solid #ccc;">2015-02-10</td>
						</tr>
						<tr>
							<th  style="text-align:left; background:#f7f7f7; border:1px solid #ccc;">파일</th>
							<td colspan="3" id="copy_file_list">
								<a href="javascript:void(0);" class="doc_download"  style="text-align:left; border:1px solid #ccc;">워드문서.docx</a>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<!-- URL 메일 전송  -->
			<div class="url_email hide" id="docDetailUrlEmail">
				<div class="url_email_title">URL 메일 전송
					<a href="javascript:void(0);" class="url_email_close"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
				</div>
				<div class="url_email_cnts">
					<table>
						<colgroup><col width="70"/><col width="310"/></colgroup>
						<tr>
							<th>조회기간</th>
							<td>
								<label><input type="radio" name="urlDate" class="" id="urlDate1" value="">제한없음</label>
								<label>
									<input type="radio" name="urlDate" class="" id="urlDate2"  value="limit" checked>
									<input type="text" name="" id="urlExpired" class="duration_limit" >일로 제한
								</label>
							</td>
						</tr>
						<tr id="urlCopyOption" class="hide">
							<th>메일수신자</th>
							<td>
								<input type="text" name="" class="email_receiver" placeholder="메일수신자를 입력해주세요.">
								<button type="button" class="email_find"  onclick="javascript:exsoft.document.open.reciverDetail();">찾기</button>
							</td>
						</tr>
					</table>
					<div class="url_email_confirm">문서의 URL을 메일로 발송하시겠습니까?</div>
					<div class="url_email_btnGrp">
						<button type="button"  onclick="javascript:exsoft.document.event.sendOperation();">확인</button>
						<button type="button" onclick="javascript:exsoft.util.layout.popDivLayerClose('url_email');">취소</button>
					</div>
				</div>
			</div>
	</div>
</div>
<script type="text/javascript">
jQuery(function() {
	
	// Layer 관련 이벤트 정의
	exsoft.util.layout.divLayerOn('doc_detail_wrapper','doc_detail','doc_detail_close');

	//문서 상세조회 - 자물쇠 모양 over시 정보 표출
    $('.cnts_locked').find('img').mouseover(function(){
    	$('.locked_info').removeClass('hide');
    }).mouseout(function(){
    	$('.locked_info').addClass('hide');
    });


});
</script>
