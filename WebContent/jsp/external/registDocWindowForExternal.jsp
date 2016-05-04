<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!-- 
	Usage : 외부연계 문서등록
 -->
<html>
<head>
<title>문서등록</title>

<script type="text/javascript" src="${contextRoot}/js/external/externalFunc.js"></script>

<!-- 키값 설정 -->
<script type="text/javascript">
var contextRoot = "${contextRoot}";
var theme = "themeBlue";
var windowType = 'userLayout';
</script>

<!-- 기본 js 선언 -->
<script type="text/javascript" src="${contextRoot}/js/plugins/jquery/jquery-2.1.3.min.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/common.js"></script>
<script type="text/javascript" src="${contextRoot}/js/common/include.js"></script>
<script type="text/javascript" src="${contextRoot}/js/plugins/jquery/jquery.form.js"></script>
<c:choose>
<c:when test="${language == 'KO'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_ko.js"></script></c:when>
<c:when test="${language == 'EN'}"><script type="text/javascript" src="${contextRoot}/js/common/messages/messages_en.js"></script></c:when>
</c:choose>
</head>

<body onload="javascript:externalFunc.init.docWriteInit('${folder_id}', '${folder_path }', '${calledpage }', '${isLeft }');">
<!-- <div class="doc_register_wrapper hide"></div> -->
<div>
	<!--
	<div class="doc_register_title">
		<a href="javascript:void(0);" onclick="javascript:exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register')" class="doc_register_close">
			<img src="${contextRoot}/img/icon/window_close.png" alt="" title="">
		</a>
	</div>
	-->
	<div class="doc_register_wrap">
		<div class="ex_doc_register_cnts">
		
		<form id='documentWrite' name='documentWrite'>
			<input type="hidden" name='folder_id' data-bind="folder_id">
	        <input type="hidden" name='map_id' data-bind="map_id">
			<input type="hidden" name="acl_id" data-bind="acl_id"><!-- 기본ACL -->
	        <input type="hidden" name='is_extended' data-bind="is_extended">
			<input type="hidden" name="isChangeType" data-bind="isChangeType">				<!-- 문서유형 변경유무 -->
			<input type="hidden" name="folderIsType" data-bind="folderIsType">				<!-- 폴더 저장 문서유형 -->
			<input type="hidden" name="doc_description">			<!-- 문서설명 -->
			<input type="hidden" name="is_share">					<!-- 공유여부 -->
			<input type="hidden" name="page_cnt">					<!-- 첨부파일수 -->	
			<input type="hidden" name="jsonMultiFolders">			<!-- 다차원 분류 -->
			
			<table id="registDoc">
			
				<tr>
					<th>제목 <span class="required">*</span></th>
					<td colspan="3">
						<input type="text" id="" name="doc_name" class="doc_title" value="" data-bind="doc_name" ex-valid="require" ex-display="제목" style="width:600px;">
					</td>
				</tr>
				<tr>
					<th>기본폴더 <span class="required">*</span></th>
					<td colspan="3">
						<input type="text" class="doc_folder  readonly" name="folder_path" data-bind='folder_path' ex-valid="require" ex-display="기본폴더" disabled>
						<button type="button" class="doc_folder_srch" onclick="javascript:externalFunc.event.folderFind();">선택</button>
					</td>
				</tr>
				<tr>
					<th>문서유형 <span class="required">*</span></th>
					<td>
						<select id="register_docType"  name="doc_type" data-select='true'>
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
					<th>보존연한 <span class="required">*</span></th>
					<td>
						<select id="register_preservationyear"  name="preservation_year" data-select='true'>
						<c:choose>
						<c:when test="${fn:length(preservation_year) > 0}">
						<c:set var="count" value="${fn:length(preservation_year) }"></c:set>
							<c:forEach items="${preservation_year}" var="m" varStatus="code_id">	   							
								<c:choose>
						 		<c:when test="${m.code_id == '0'}"><option value="${m.code_id}" selected>${m.code_nm}</option></c:when>
								<c:otherwise><option value="${m.code_id}">${m.code_nm}</option></c:otherwise>
								</c:choose>						
							</c:forEach>
		            	</c:when>
				 		</c:choose>
		                </select>
					</td>
				</tr>
				<tr id="sercurityView">
					<th>보안등급 <span class="required">*</span></th>
					<td>	
						<c:choose>               
						<c:when test="${fn:length(sercurity) > 0}">
						<c:set var="count" value="${fn:length(sercurity) }"></c:set>
							<c:forEach items="${sercurity}" var="m" varStatus="code_id">	   	
								<c:choose>
						 		<c:when test="${m.code_id == 'COMMON'}"><input type="radio" name="security_level" value="${m.code_id}" checked/>${m.code_nm}</c:when>
								<c:otherwise><input type="radio" name="security_level" value="${m.code_id}"/>${m.code_nm}</c:otherwise>
								</c:choose>						
							</c:forEach>	       
		            	</c:when>
			 		</c:choose>
				
					</td>
					<th>조회등급 <span class="required">*</span></th>
					<td>
						<select id="register_accessgrade"  name="access_grade" data-select='true'>
						<c:choose>
						<c:when test="${fn:length(position) > 0}">
						<c:set var="count" value="${fn:length(position) }"></c:set>
							<c:forEach items="${position}" var="m" varStatus="code_id">	   							
								<c:choose>
						 		<c:when test="${m.code_id == '0'}"><option value="${m.code_id}" selected>${m.code_nm}</option></c:when>
								<c:otherwise><option value="${m.code_id}">${m.code_nm}</option></c:otherwise>
								</c:choose>						
							</c:forEach>
		            	</c:when>
				 		</c:choose>
		                </select>
		                
					</td>
				</tr>
				<tr id="inheritacl">
					<th>권한유지</th>
					<td colspan="3">
						<label><input type="checkbox" id="inheritacl" name="is_inherit_acl_chk" class="">상위폴더 권한변경시에도 현재 권한 유지</label>
					</td>
				</tr>
				<tr id="isShareView">
	            	<th>문서공유여부</th>
	              	<td colspan="3"><input type="checkbox" name="is_share_chk" /><span class="checkbox">권한부여받은 사용자에게 '공유받은 문서' 제공</span></td>
            	</tr>
            
				<tr id="classificationlist">
					<th>다차원 분류</th>
					<td colspan="3">
						<div class="doc_classification_list hide" >
							<ul id="multiFolder"></ul>						
						</div>
						<button type="button" class="doc_classification_btn" onclick="javascript:externalFunc.open.multiFolderFind();">선택</button>
					</td>
				</tr>
				<tr id = "keyWord">
					<th>키워드</th>
					<td colspan="3">
						<input type="text" class="doc_keyword" name="keyword" style="width:600px;" placeholder="여러개의 키워드 등록시 ','로 구분해주세요">
					</td>
				</tr>
				<tr>
					<th>설명</th>
					<td colspan="3">
					<iframe src="" id="iframe_editor" name="iframe_editor" style="border:0 solid transparent ;padding:0; margin:0;height:150px;width:98%"></iframe>
					</td>
				</tr>
			</table>
			
			<!-- 확장문서유형 START -->
			<table class="hide" id="documentWrite_docAttrView">
				<thead></thead>
				<tbody></tbody>
			</table>
			<!-- 확장문서유형 END  -->
			
			<div class="doc_auth">
				<a class="dropDown_txt"> <label id="wAclName">권한</label> <span class="dropDown_arrow down"></span></a>
				<button type="button" onclick="javascript:externalFunc.open.changeAcl();">권한변경</button>
				<div class="doc_auth_cnts hide">
					<table id="docmentWrite_acl">						
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
			         		<table id="docmentWrite_extAcl">
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
			
			<!--외부연계는 관련문서 안씀 
			<div class="doc_relative">
				<a class="dropDown_txt"><label>관련문서 <span class="relative_docs_cnt"></span></label><span class="dropDown_arrow down"></span></a>
				<div class="doc_relative_cnts hide">
					<table >
        			<colgroup>
        				<col style="width:5%">
						<col style="width:55%">
						<col style="width:20%">
						<col style="width:20%">
        			</colgroup>
        			<thead>
        			<tr>
        				<td colspan="4" class="right">
        					<button type="button" class="relative_docs_add" onclick="javascript:exsoft.document.event.selectRelDocWindow('I');">추가</button>
        					<button type="button" class="relative_docs_remove" onclick="javascript:exsoft.document.event.refDocDel('I');">제외</button>
        				</td>
        			</tr>
        			<tr colspan="4">
        				<td><input type="checkbox" name="" id="wRef" onclick="javascript:exsoft.util.common.allCheckBox('wRef','wRefDocIdx');"></td>
        				<td class="center" >제목</td>
        				<td class="center" >등록자</td>
        				<td class="center" >등록일</td>        				
        			</tr>
        			
        			</thead>
        			<tbody id="RefDocTable"></tbody>
					</table>
				</div>
			</div>
			 -->
			
       		<!-- 파일첨부 -->
       		<div class="coop_detail">
	        <div class="coop_detail_cnts">
	        <div id="documentfileuploader">파일추가</div>
	        <div id="totalSize"></div> 
	        </div>
	        </div>
       		</form>
		</div>
		
		<div class="doc_register_btnGrp">
			<button type="submit" onclick="javascript:externalFunc.event.documentSubmit();">등록</button>
<!-- 			<button type="reset" onclick="javascript:exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register')">취소</button> -->
		</div>
	</div>
	<div class="doc_register_recent">
		<div class="recent_docs_title">최근문서</div>
		<div class="recent_list_wrap">
			<ul class="recent_list" id="recentDocumentList">				
			</ul>
		</div>
	</div>
</div>

<form name="treePopCall">
	<input type="hidden" name="emp_no">
	<input type="hidden" name="login_type" value="user">
	<input type="hidden" name="type">
</form>

</body>
</html>

<jsp:include page="/jsp/external/selectAclWindowForExternal.jsp"/> <!-- 권한변경 -->
<jsp:include page="/jsp/external/selectAccessorWindowForExternal.jsp"/> <!-- 권한 등록시 접근자 추가창 -->
<jsp:include page="/jsp/external/selectMultiFolderWindowForExternal.jsp"/> <!-- 다중분류체계 -->
<jsp:include page="/jsp/external/selectSingleFolderWindowForExternalDoc.jsp"/> <!-- 기본폴더 선택 -->
