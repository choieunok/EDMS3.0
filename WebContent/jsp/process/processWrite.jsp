<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="coop_register_wrapper hide"></div>
	<div class="coop_register hide">
  		<div class="coop_register_title">
        	<span>업무 등록</span>
        	<a href="javascript:exsoftProcessWrite.close.layerClose(true);" class="coop_register_close"><!-- exRep에 등록한 파일을 삭제 한다. -->
        		<img src="${contextRoot}/img/icon/window_close.png" alt="" title="">
        	</a>
        </div>	
         <div class="coop_register_wrap">
         	<div class="coop_register_cnts">
         		<form id='processWrite'>
	         		<input type="hidden" name='folder_id' data-bind="folder_id">
	         		<input type="hidden" name='map_id' data-bind="map_id">
	         		<input type="hidden" name='acl_id' data-bind="acl_id">
	         		<input type="hidden" name='requestorId' data-bind="requestorId">
	         		<input type="hidden" name='actionType' data-bind="actionType" ex-valid="require">
	         		<input type="hidden" name='is_extended' data-bind="is_extended">
	         		<input type="hidden" name='process_id' data-bind="process_id">
	         		<input type="hidden" name='doc_root_id' data-bind="doc_root_id">

	         		<table>
	         		<colgroup>
	         			<col width="100"/>
	         			<col width="348"/>
	         			<col width="150"/>
	         			<col width="140"/>
	         		</colgroup>
	       			<tr>
	   				<th>업무명<span class="required">*</span></th>
	   				<td colspan="3"><input type="text" class="coop_title" name="name" data-bind='name' ex-valid="require" ex-display="업무명"></td>
	       			</tr>
	       			<tr>
	   				<th>기본폴더<span class="required">*</span></th>
	   				<td colspan="3">
	   					<input type="text" class="coop_default_folder  readonly" name="full-path" data-bind='full_path' ex-valid="require" ex-display="기본폴더" disabled>
	   					<button type="button" class="coop_choose_folder" onclick='javascript:exsoftProcessWrite.open.selectFolderWindow();'>선택</button>
	   				</td>
	       			</tr>
	       			<tr>
	   				<th>업무요청자</th>
  					<td><input type="text" class="coop_requester readonly" name="requestorName" data-bind='requestorName' disabled></td>
	   				<th class="center">업무완료 예정일<span class="required">*</span></th>
	   				<td>
	   					<input type="text" id="datepicker5" class="srch_coop_endDate input-text calend" name="expect_date" data-bind='expect_date' ex-valid="require" readonly="readonly" ex-display="업무완료예정일">
	   					<button type="button" class="cal_btn"></button>
	   				</td>
	       			</tr>
	       			<tr>
	   				<th>업무요청 내용</th>
	   				<td colspan="3"><textarea class="coop_request_cnts" name="content" data-bind='content' onKeyUp='javascript:exsoft.util.common.limitStringCheck(this,"1000")' ex-valid="require" ex-display="업무요청내용" style="border:solid 1px #e5e5e5"></textarea>
	   				<p class="coop_request_wordcnts"><span class="word_count">0</span>/1000자</p>
	   				</td>
	       			</tr>
	       			<tr>
	   				<th>협업자 선택</th>
	   				
	   				<td colspan="3"><button type="button" class="choose_coop_user" onclick='javascript:exsoftProcessWrite.open.processCoworkWindow();'>협업자 선택</button></td>
	       			</tr>
	       			<tr>
	   				<th>작성자
	   					<span class="required">*</span>
	   				</th>
	   				<td colspan="3"><input type="text" class="coop_user readonly" name="coworkAuthor" data-bind='coworkAuthor' ex-valid="require" ex-display="작성자" disabled></td>
	       			</tr>
	       			<tr>
	   				<th>공동작성자</th>
	   				<td colspan="3"><input type="text" class="coop_additional_user readonly" name="coworkCoauthor" data-bind='coworkCoauthor' disabled></td>
	       			</tr>
	       			<tr>
	   				<th>승인자
	   					<span class="required">*</span>
	   				</th>
	   				<td colspan="3"><input type="text" class="coop_approve_user readonly" name="coworkApprover" data-bind='coworkApprover' ex-valid="require" ex-display="승인자" disabled></td>
	       			</tr>
	       			<tr>
	   				<th>수신자</th>
	   				<td colspan="3"><input type="text" class="coop_receive_user readonly" name="coworkReceiver" data-bind='coworkReceiver' disabled></td>
	       			</tr>
	       			<tr>
		       			<th>문서유형</th>
		   				<td>
		   					<select id="processWrite_docType" data-select='true'>
								<c:choose>
									<c:when test="${fn:length(typeList) > 0}">
										<c:set var="count" value="${fn:length(typeList) }"></c:set>
										<c:forEach items="${typeList}" var="m" varStatus="type_id">
											<c:choose>
												<c:when test="${m.is_base == 'T'}">
													<option value="${m.type_id}" selected="selected">${m.type_name}</option>
												</c:when>
												<c:otherwise>
													<option value="${m.type_id}">${m.type_name}</option>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</c:when>
								</c:choose>
							</select>
						</td>
	       			</tr>
	         		</table>
	         		
	         		<!-- 확장문서유형 START -->
		            <table class="hide" id="processWrite_docAttrView">
		            	<colgroup>
		            		<col width="100">
		            		<col width="638">
		            	</colgroup>
		            	<thead></thead>
		            	<tbody></tbody>    
		            </table>
		            <!-- 확장문서유형 END  -->
		            
		            <!-- 첨부파일 STRART -->
		            <div class="coop_detailView_attach hide">
		            	<a class="dropDown_txt">
							<label>
								<strong>첨부파일</strong>
								(<span class="approvalRequest_attach_cnt"></span>)
							</label>
							<span class="dropDown_arrow down"></span>
						</a>
			            <div class="coop_detailView_attach_cnts hide">
							<div class="download_btnGrp">
								<!-- <input type="checkbox" id="" name="" class=""> -->
							 	<button type="button" onclick='javascript:exsoftProcessWrite.event.attachToggleCheck(true);'>전체선택</button>
								<button type="button" onclick='javascript:exsoftProcessWrite.event.attachToggleCheck(false);'>전체해제</button>
								<button type="button" onclick='javascript:exsoftProcessWrite.event.attachSave();'>저장</button>
								<button type="button" onclick='javascript:exsoftProcessWrite.event.attachDelete();'>삭제</button>
							</div>
							<ul id='processWrite_attachFileList'>
							</ul>
						</div>
					</div>
		            <!-- 첨부파일 END -->
	         		
	         		<!-- 부서 권한 -->
	         		<div class="doc_auth">
	         			<a class="dropDown_txt">
		                  	<label id='processWriteAclName'></label>
		                  	<span class="dropDown_arrow down"></span>
		                </a>
		                <button type="button" onclick="exsoftProcessWrite.open.chagneDocumentAcl();">권한변경</button>
	         			<div class="doc_auth_cnts hide">
	         				<table id='processWrite_acl'>
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
			         		<table id="processWrite_extAcl">
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
	         		
	         		<div class="coop_detail">
	         			<div class="coop_detail_cnts">
	         				<div id="processfileuploader">파일추가</div>
	         				<div id="totalSize"></div> 
	         			</div>
	         		</div>
         		
         		</form>
         	</div>
         	<div class="coop_register_btnGrp">
          		<button type="button" onclick="javascript:exsoftProcessWrite.event.submit();"><span>등록</span></button>
          		<button type="reset" onclick="javascript:exsoftProcessWrite.close.layerClose(true);">취소</button><!-- exRep에 등록한 파일을 삭제 한다. -->
          	</div>
    	</div>
    	<div class="coop_register_recent">
       		<div class="recent_coop_title">
       			최근 등록한 업무
       		</div>
       		<div class="coop_recent_wrap">
       			<ul class="coop_recent_list"></ul>
       		</div>
       	</div>
    </div>
<!-- script add -->
<script type="text/javascript" src="${contextRoot}/js/process/processWrite.js"></script>    
<script type="text/javascript">
jQuery(function() {
	$("#datepicker5").datepicker({dateFormat:'yy-mm-dd'});
	$('.coop_register_cnts').find('.cal_btn').bind("click", function(){
		$("#datepicker5").datepicker("show");
	});
	
	$(window).resize(function(){		 
		exsoft.util.layout.lyrPopupWindowResize($(".coop_register"));
	});

});


jQuery(function() {

    // 업무등록 : 음영진 부분 클릭 시 닫기
    $('.coop_register_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.coop_register').addClass('hide');
    });
});

</script>  
<jsp:include page="/jsp/process/processCoworkWindows.jsp"/>