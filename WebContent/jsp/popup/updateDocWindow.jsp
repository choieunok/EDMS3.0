<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/common/databinder.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/validator.js"></script>
<!-- 
	Usage : 문서수정 Main
 -->
  <script type="text/javascript">
jQuery(function() {	

	 //문서수정 - 창 닫기
    /* $('.doc_modify_close').bind("click", function(e){
    	e.preventDefault();
    	$(this).parents('.doc_modify').addClass('hide');
    	$('.doc_modify_wrapper').addClass('hide');

		//exsoft.document.close.layerClose(true,'doc_modify_wrapper','doc_modify');
    }); */

    //문서수정 - 창 닫기 : 음영진 부분 클릭 시 닫기
    $('.doc_modify_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.doc_modify').addClass('hide');
    	exsoft.document.close.layerClose(true,'doc_modify_wrapper','doc_modify');
    });
	 
	// 창에 대한 리사이즈시 중앙으로 오게하기위한 조치
	 exsoft.util.layout.lyrPopupWindowResize($(".doc_modify"));
	$(window).resize(function(){		 
		exsoft.util.layout.lyrPopupWindowResize($(".doc_modify"));
	});
	
});
</script>
 <!-- 문서수정 시작 -->
   	<div class="doc_modify_wrapper hide"></div>
		<div class="doc_modify hide">
     		<div class="doc_modify_title">
              문서 수정
              <a href="javascript:void(0);" class="doc_modify_close"><img src="${contextRoot}/img/icon/window_close.png" onclick="javascript:exsoft.document.event.docUpdateCancel()" alt="" title=""></a>
         	</div>	
        	<div class="doc_modify_cnts">
        	<form id='documentUpdate' name='documentUpdate'>
        	
				<input type="hidden" name="isType" value="update" data-bind="isType">		<!-- 업무구분 -->
				<input type="hidden" name="doc_id" data-bind="doc_id">						<!-- 문서ID -->
				<input type="hidden" name="doc_type" data-bind="doc_type">					<!-- 문서유형 -->
				<input type="hidden" name="folderIsType" data-bind="folderIsType">			<!-- 폴더 저장 문서유형 -->
				<input type="hidden" name="is_extended" data-bind="is_extended">				
				<input type="hidden" name="ref_id" data-bind="ref_id">			
				<input type="hidden" name="root_id" data-bind="root_id">				
				<input type="hidden" name="folder_id" data-bind="folder_id">			
				<input type="hidden" name="map_id" data-bind="map_id">			
				<input type="hidden" name="version_no" data-bind="version_no">	
				<input type="hidden" name="owner_id" data-bind="owner_id">	
				<input type="hidden" name="acl_id" data-bind="acl_id">	
				<input type="hidden" name="page_cnt">					<!-- 첨부파일수 -->	
				<input type="hidden" name="dFileList">					<!-- 삭제할 페이지 리스트 -->
        		<div class="divide_line">
	        		<table>
	       			<tr>
	   				<th>제목</th>
	   				<td colspan="3"><input type="text" class="doc_title" name="doc_name" class=""  data-bind="doc_name" ex-valid="require" placeholder="문서관리 지침"></td>
	       			</tr>
	       			<tr>
	   				<th>버전설정</th>
	   				<td colspan="3">
	   					<span class="current_doc_ver" id="uVersionView">Ver 1.0</span>
	   					<input type="radio" name="version_type" value="SAME" />현재버전 유지
	   					<input type="radio" name="version_type" value="MINOR" />부 버전 증가 
	   					<input type="radio" name="version_type" value="MAJOR" />주 버전 증가
	   					
	   				</td>
	       			</tr>
	       			</table>
	       		</div>
	      		<div class="divide_bottom">
	      			<table>
	       			<tr>
	   				<th>기본폴더
	   					<span class="required">*</span>
	   				</th>
	   				<td colspan="3">
	   					<input type="text" class="doc_folder readonly" name="folder_path" data-bind="folder_path" class="" value="" readonly="readonly">
	   				</td>
	       			</tr>
	    				<tr>
	   				<th>문서유형
	   					<span class="required">*</span>
	   				</th>
	   				<td id="uType_name"></td>
	   				<th>보존연한
	   					<span class="required">*</span>
	   				</th>
	   					<td>
	   						<select id="modify_doc_preserve" name='preservation_year'  data-select='true' >
				     			<option value="1">1년</option>
								<option value="2">2년</option>
								<option value="3">3년</option>
								<option value="5">5년</option>
								<option value="10">10년</option>
								<option value="0" selected>영구</option>
				     		</select>
	   					
						</td>
							
	   				
	       			</tr>
	       			<tr id="usercurityView">
	   				<th>보안등급
	   					<span class="required">*</span>
	   				</th>
	   				<td>
	   				
	   					<c:choose>
							<c:when test="${fn:length(sercurity) > 0}">
								<c:set var="count" value="${fn:length(sercurity) }"></c:set>
								<c:forEach items="${sercurity}" var="m" varStatus="code_id">
									<c:choose>
									<c:when test="${m.code_id == 'COMMON'}">
											<input type="radio" name="security_level" value="${m.code_id}" checked />${m.code_nm}</c:when>
									<c:otherwise>
										<input type="radio" name="security_level" value="${m.code_id}" />${m.code_nm}</c:otherwise>
									</c:choose>
								</c:forEach>
							</c:when>
						</c:choose>
	   				</td>
	   				<th>조회등급
	   					<span class="required">*</span>
	   				</th>
	   				<td>
	   					<select id="modify_doc_inquiry" name='access_grade'  data-select='true' >
							<c:choose>
								<c:when test="${fn:length(position) > 0}">
									<c:set var="count" value="${fn:length(position) }"></c:set>
									<c:forEach items="${position}" var="m" varStatus="code_id">
										<option value="${m.code_id}">${m.code_nm}</option>
									</c:forEach>
								</c:when>
							</c:choose>
						</select>
	   				</td>
	       			</tr>
	       			<tr id="uaclLockView">
	   				<th>권한변경여부</th>
	   				<td>
	   					<label><input type="checkbox" class="" id="uinheritacl" name="is_inherit_acl_chk" class="">상위폴더 권한변경시에도 현재 권한 유지</label>
	   				</td>
	   				<th>등록자[소유자]</th>
	   				<td id="uCreator_name">홍길동[홍길동]</td>
	       			</tr>
	       			<tr id="ushareChkView">
	       			<th>문서공유여부</th>
	   				<td>
	   					<label><input type="checkbox" class="" id="uisshare" name="is_share_chk" class="">권한을 부여받은 사용자에게 '공유받은 문서' 제공</label>
	   				</td>
	   				<th >등록일</th>
	   				<td id="uCreate_date">2014.09.15 15:12:33</td>
	   				</tr>
	   				
	   				
	   				
	       			<tr id="uclassificationlist">
	   				<th>다차원 분류</th>
	   				<td colspan="3">
	   					<div class="doc_classification_list hide">
	   						<ul id="uMultiFolder">	   						
	   						</ul>
	   					</div>
	   					<button type="button" class="doc_classification_btn" onclick="javascript:exsoft.document.event.registDocSelectMultiFolderFind('U');">선택</button>
	   				</td>
	       			</tr>
    				<tr id="ukeyWord">
	   				<th>키워드</th>
	   				<td colspan="3">
	   					<input type="text" class="doc_keyword_filled" name="keyword" data-bind="keyword"><br>
	   					<span class="keyword_instruction">여러개의 키워드 등록 시 ','로 구분해주세요</span>
	   				</td>
	       			</tr>
	       			<tr>
	  				<th>설명</th>
	  				<td colspan="3">
	  					<span style='display: none'><TEXTAREA id="temp_content" ></TEXTAREA></span>
	  					<iframe src="" id="uIframe_editor" name="uIframe_editor" style="border: 0 solid transparent; padding: 0; margin: 0; height: 150px; width: 99%"></iframe>
	  				</td>
	       			</tr>
	        		</table>
	        	</div>
		       	<!-- 확장문서유형 START -->
				<table class="hide" id="documentUpdate_docAttrView">
					<thead></thead>
					<tbody></tbody>
				</table>
				<!-- 확장문서유형 END  -->
			
			
        		<div class="doc_auth">
        			<a class="dropDown_txt">
	                  	<label>권한 : <span class="normal" id="uAclName">연구소 부서권한</span></label>
	                  	<span class="dropDown_arrow down"></span>
             		</a>
                 	<button type="button"  onclick="exsoft.document.event.changeDocumentAcl('U');">권한변경</button>
        			<div class="doc_auth_cnts hide">
        				<!-- <ul>
        					<li>
	        					<label>
		         					<input type="checkbox" name="" class="" value="">
		         					권한부여받은 사용자에게 '공유받은 문서'제공
		         				</label>
	         				</li>
        				</ul> -->
        				<table id="docmentUpdate_acl">						
						<thead>
								<tr>
									<th>기본 접근자</th>
									<th>기본권한</th>
									<th>문서등록</th>
									<th>반출취소</th>
									<th>권한변경</th>
								</tr>
							</thead>
		         			<tbody></tbody>
			         		</table>
			         		<table id="docmentUpdate_extAcl">
		         				<thead>
			         				<tr>
				         				<th>추가 접근자</th>
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
        		<div class="doc_relative">
        			<a class="dropDown_txt"><label>관련문서 <span id="uRefDocCnt"></span></label><span class="dropDown_arrow down"></span></a>
        			

        			
        			<div class="doc_relative_cnts hide">
        				<table>
        				<colgroup>
        					<col style="width:5%">
							<col style="width:55%">
							<col style="width:20%">
							<col style="width:20%">
        				</colgroup>
        				<thead>
        				<tr>
        				<td colspan="4" class="right">
								<button type="button" class="relative_docs_add" onclick='javascript:exsoft.document.event.selectRelDocWindow("U");'>추가</button>
        						<button type="button" class="relative_docs_remove" onclick="javascript:exsoft.document.event.refDocDel('U');">삭제</button>
        				</td>        				
        				</tr>
        				<tr colspan="4">
        				<td><input type="checkbox" id="uRef" onclick="javascript:exsoft.util.common.allCheckBox('uRef','uRefDocIdx');"></td>
        				<td class="center" >제목</td>
        				<td class="center" >등록자</td>
        				<td class="center" >등록일</td>        				
        				</tr>
        				</thead>
        				<tbody id="uRefDocTable"></tbody>
        				</table>
        			</div>
        		</div>
        		
        		<!-- 첨부파일 STRART -->
	            <div class="doc_attach">
	            	<a class="dropDown_txt">
						<label>
							<strong>첨부파일</strong>
							(<span class="approvalRequest_attach_cnt" id="uAttachCnt"></span>)
						</label>
						<span class="dropDown_arrow down"></span>
					</a>
		            <div class="doc_attach_cnts hide">
						<div class="download_btnGrp">
							<button type="button" onclick="javascript:exsoft.document.event.attachToggleCheck(true,'uAttacheTable_checkbox');">전체선택</button>
							<button type="button" onclick="javascript:exsoft.document.event.attachToggleCheck(false,'uAttacheTable_checkbox');">전체해제</button>
							<button type="button" onclick="javascript:exsoft.document.event.viewattachSave('uAttacheTable');">저장</button>						
							<button type="button" onclick="javascript:exsoft.document.event.attachDelete('uAttacheTable_checkbox');">삭제</button>
						</div>
						<ul id='uAttacheTable'>
						</ul>
					</div>
				</div>
	            <!-- 첨부파일 END -->
        		
	         	
	         	<!-- 파일첨부 -->
	       		<div class="coop_detail">
		        	<div class="coop_detail_cnts">
		        		<div id="documentupdatefileuploader">파일추가</div>
		        		<div id="totalSize"></div> 
		       	 	</div>
		        </div>
         		
         		</form>
     	</div>
     	<div class="doc_modify_btnGrp">
       		<button type="submit" class="modify_docs" onclick="javascript:exsoft.document.event.documentUpdateSubmit();">수정</button>
       		<button type="button" class="cancel_modify" onclick="javascript:exsoft.document.event.docUpdateCancel()">취소</button> 
       		<!-- <button type="button" class="cancel_modify" >취소</button> -->
       	</div>	
     </div>
   	<!-- 문서수정 끝 -->