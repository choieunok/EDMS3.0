<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
	협업업무 상세조회
 -->
<div class="coopUser_detail_wrapper hide"></div>
<div class="coopUser_detail hide">
  	<form name="processWriteFrm" id="processWriteFrm">
   		<div class="coopUser_detail_title">
   			업무 상세조회
			<a href="javascript:exsoft.util.layout.divLayerClose('coopUser_detail_wrapper','coopUser_detail');" class="coopUser_detail_close">
			<img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
		</div>
		<div class="coopUser_detail_cnts">
			<div class="coopUser_sub_wrapper">
				<span class="cnts_locked hide"> 
					<input type="hidden" id="p_is_locked_val"/>
					<img src="${contextRoot}/img/icon/lock1.png" onmousemove='javascript:exsoftProcessViewFunc.ui.tooltip(event);' onmouseout='javascript:$("#processView_tooltip").addClass("hide")'>
					<div id='processView_tooltip' class="locked_info hide" ></div>
				</span>
				<span class="bold"></span>
				<div>
					<button type="button" id="processView_modify" class="requestApproval_btn hide" onclick='javascript:exsoftProcessViewFunc.event.modifyProcess();'>수정</button>
					<button type="button" id="processView_delete" class="requestApproval_btn hide" onclick='javascript:exsoftProcessViewFunc.event.deleteProcess();'>삭제</button>
				</div>			
			</div>
			<div class="coop_process_procedure">
				<!-- 각 단계인 경우 class=current -->
				<ul id='processView_step'>
					<li class="coop_process">작성단계</li>
					<li class="coop_process">승인단계</li>
					<li class="coop_process">보완단계</li>
					<li class="coop_process">완료</li>
				</ul>
			</div>
			
			<!-- 첨부파일 영역 -->
			<div class="coop_approvalRequest_attach">
				<a class="dropDown_txt">
					<label>
						<strong>첨부파일</strong>
						(<span class="approvalRequest_attach_cnt"></span>)
					</label>
				</a>
				<span class="dropDown_arrow up"></span>
				<div class="approvalRequest_docs_wrapper">
					<div class="download_btnGrp">
						<!-- <input type="checkbox" id="" name="" class=""> -->
					 	<button type="button" onclick='javascript:exsoftProcessViewFunc.event.attachToggleCheck(true);'>전체선택</button>
						<button type="button" onclick='javascript:exsoftProcessViewFunc.event.attachToggleCheck(false);'>전체해제</button>
						<button type="button" onclick='javascript:exsoftProcessViewFunc.event.attachSave();'>저장</button>
						<button type="button" onclick='javascript:exsoftProcessViewFunc.event.attachDelete();'>삭제</button>
					</div>
					<ul id='processView_attachFileList'>
						<%-- <li class="attach_docs_list">
							<input type="checkbox" id="" name="" class="">
							<a href="#">
								<img src="${contextRoot}/img/icon/ppt.png">
								회의록.docx
							</a>
							<div class="download_detail">
								<span class="download_filesize">1.3MB</span>
								<a href="javascript:void(0);" class="download" onclick='javascript:exsoftProcessViewFunc.event.test();'><img src="${contextRoot}/img/icon/attach_download1.png" alt="파일보기"></a>
								<a href="javascript:void(0);" class="check_in"><img src="${contextRoot}/img/icon/attach_checkin.png" alt="파일수정완료"></a>
								<a href="javascript:void(0);" class="check_out"><img src="${contextRoot}/img/icon/attach_checkout.png" alt="파일수정"></a>
								<a href="javascript:void(0);" class="check_out disabled"><img src="${contextRoot}/img/icon/attach_disabled.png" alt="" title=""></a>
							</div>
						</li>
						<li class="attach_docs_list">
							<input type="checkbox" id="" name="" class="">
							<a href="#">
								<img src="${contextRoot}/img/icon/ppt.png" alt="" title="">
								회의록.docx
							</a>
							<div class="download_detail">
								<span class="download_filesize">1.3MB</span>
								<a href="#" class="download"><img src="${contextRoot}/img/icon/attach_download1.png" alt="" title=""></a>
								<a href="#" class="check_in"><img src="${contextRoot}/img/icon/attach_checkin.png" alt="" title=""></a>
								<a href="#" class="check_out"><img src="${contextRoot}/img/icon/attach_checkout.png" alt="" title=""></a>
								<a href="#" class="check_out_disabled"><img src="${contextRoot}/img/icon/attach_disabled.png" alt="" title=""></a>
							</div>
						</li> --%>
					</ul>
				</div>
			</div>
			
			<div class="coop_approvalRequest_form">
				<!-- 
					-- 작성단계 : 승인요청
					-- 승인단계 : 승인/반려
					-- 보완단계 : 승인요청
				 -->
				<button type="button" id="processView_approveRequest" class="requestApproval_btn hide" onclick='javascript:exsoftProcessViewFunc.event.approveAction("APPROVEREQUEST");'>승인요청</button>
				<button type="button" id="processView_approve" class="requestApproval_btn hide" onclick='javascript:exsoftProcessViewFunc.event.approveAction("APPROVE");'>승인</button>
				<button type="button" id="processView_approveReject" class="requestApproval_btn hide" onclick='javascript:exsoftProcessViewFunc.event.approveAction("APPROVEREJECT");'>반려</button>
				<textarea class="requestApproval_cnts  hide" id='processView_content' onKeyUp='javascript:exsoft.util.common.limitStringCheck(this,"1000")' ex-valid="require" style="border:solid 1px #e5e5e5"></textarea>
				<p class="requestApproval_wordcnts hide"><span class="word_count">0</span>/1000자</p>
			</div>
			
			<div class="coop_infomation">
				<a class="dropDown_txt">
					<label><strong>협업정보</strong></label>
				</a>
				<span class="dropDown_arrow down"></span>
				<div class="coop_infomation_cnts hide">
					<table id='processView_info'>					
					<colgroup><col width="117"/><col width="528"/></colgroup>
					<tr><th>업무명</th><td><span></span></td></tr>
					<tr><th>기본폴더</th><td><span></span></td></tr>
					<tr><th>업무요청자</th><td><span></span></td></tr>
					<tr><th>업무완료 예정일</th><td><span></span></td></tr>
					<tr><th>업무요청 내용</th><td><span></span></td></tr>
					<tr><th>작성자</th><td><span></span></td></tr>
					<tr><th>공동작성자</th><td><span></span></td></tr>
					<tr>
						<th>승인자</th>					
						<td>
							<div class='approvalResult_receiver'>
							<a id='processView_approver' style='cursor:pointer;' onmousemove='javascript:$("#processView_approverTooltip").removeClass("hide");' onmouseout='javascript:$("#processView_approverTooltip").addClass("hide");'></a>
							<span></span><div id='processView_approverTooltip' class="approvalResult_tooltip hide"></div>
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
					<tr><th>문서유형</th><td><span></span></td></tr>					 
					</table>
		            <table id="processView_docType" style="margin-top: 1px;">
		            	<colgroup>
							<col width="118"/>
							<col width="527"/>
						</colgroup>
		            	<thead></thead>
		            	<tbody></tbody>    
		            </table>
				</div>
			</div>
			<div class="coop_currAuth_info">
				<a class="dropDown_txt">
					<label><strong id='processView_aclName'></strong></label>
				</a>
				<span class="dropDown_arrow down"></span>
				<div class="doc_auth_cnts hide">
					<table id='processView_acl'>
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
	         		<table id="processView_extAcl">
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
			
			<div class="coop_approvalResult">
				<a class="dropDown_txt">
					<label>
						<strong>처리현황</strong>
						(<span class="approvalResult_cnt"></span>)
					</label>
				</a>
				<span class="dropDown_arrow down"></span>
				<div class="approvalResult_wrapper hide">
					<ul id='processView_situation'>
						<!-- <li class="approvalResult_list">
							<p>
								<span class="bold">김요청</span>
								<span>2015-02-01 18:00</span>
							</p>
							<p>
								보고서 작성이 완료되었습니다. 팀장님 승인 부탁드립니다.
							</p>
						</li>
						<li class="approvalResult_list">
							<p>
								<span class="bold">김팀장</span>
								<span>2015-02-01 18:00</span>
							</p>
							<p>
								승인완료 합니다.
							</p>
						</li> -->
					</ul>
				</div>
			</div>
			<!-- 완료 -->	
	</div>
	</form>		
</div>
<!-- script add -->
<script type="text/javascript" src="${contextRoot}/js/process/processView.js"></script>    
<script type="text/javascript">
jQuery(function() {
	
	exsoft.util.layout.lyrPopupWindowResize($(".coopUser_detail"));
	$(window).resize(function(){		 
		exsoft.util.layout.lyrPopupWindowResize($(".coopUser_detail"));
	});

});

jQuery(function() {

    //  업무상세조회: 음영진 부분 클릭 시 닫기
    $('.coopUser_detail_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.coopUser_detail').addClass('hide');
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