<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
 [3000][EDMS-REQ-033]	2015-08-19	성예나	 : 만기 문서 사전 알림[관리자]
 [3001][EDMS-REQ-033]	2015-08-19	성예나	 : 만기 문서 사전 알림[관리자]
 [2000][EDMS-REQ-036]	2015-08-31	이재민 : 강제로그아웃처리 여부 관리
 [2001][EDMS-REQ-036]	2015-08-31	이재민 : 버전, 휴지통, URL, 만기문서알림, 중복로그인설정을 기본설정으로 통합
 -->
<!-- 
	시스템관리 - 환경설정 관리
 -->
 <div class="cnt_wrapper">
	<div class="depth_navigation">
		시스템 관리 &gt; 환경설정 관리
	</div>
	
	<div class="config_menu">
		<ul>
			<li><a href="#" class="selected">문서관리</a>	</li>
			<li><a href="#" class="hide">문서중앙화설정</a></li>
			<li><a href="#" class="hide">FC설정</a></li>
		</ul>
	</div>
	
	<div class="config_sub_wrapper">
		<div class="tab_menu">
			<div class="tab_elem_wrapper">
				<span class="tab_element" id="basicConfig" onclick="javascript:exsoftAdminConfFunc.ui.tabSelectFunc('basicConfig');">기본설정</span> <!-- [2001] -->
				<span class="tab_element" id="fileConfig" onclick="javascript:exsoftAdminConfFunc.ui.tabSelectFunc('fileConfig');">파일설정</span>
				<span class="tab_element" id="auditConfig" onclick="javascript:exsoftAdminConfFunc.ui.tabSelectFunc('auditConfig');">열람감사설정</span> 
			</div>
		</div>
		<form name="frm" id="frm">
		<input type="hidden" name="send_report_mail">
		<input type="hidden" name="pis_use">
		<input type="hidden" name="sis_use">
		<input type="hidden" name="is_fileCnt">
		<input type="hidden" name="is_fileSize">
		<input type="hidden" name="is_fileTotal">
		<input type="hidden" name="type">
		<input type="hidden" name="stype">
		<input type="hidden" name="is_mVersion">
		<input type="hidden" name="is_wVersion">
		<input type="hidden" name="alarm">
		
		<!-- [2001] Start -->
		<div class="tab_form hide"  id="basicConfigFrm">
			<table>
				<colgroup>
				<col width="205">
				<col width="1023">
				</colgroup>
				
				<!-- 버전설정 -->
				<tr>
					<th>개인문서 버전관리 사용</th>
					<td>
						<p>
							<input type="checkbox" name="mVersionChk" id="mVersionChk" >개인문서 버전관리 사용			
						</p>
						<c:choose>               
						<c:when test="${fn:length(versionList) > 0}">
							<c:forEach items="${versionList}" var="m" varStatus="code_id"> 
						 	<label><input type="radio" name="mVersion" id="mVersion" value="${m.code_id}">${m.code_nm}</label>
							</c:forEach>	   
						</c:when>
						</c:choose>
					</td>
				</tr>
				<tr>
					<th>업무문서 버전관리 사용</th>
					<td>
						<p>
							<input type="checkbox" name="wVersionChk" id="wVersionChk" >업무문서 버전관리 사용				
						</p>
						<c:choose>               
						<c:when test="${fn:length(versionList) > 0}">
                			<c:forEach items="${versionList}" var="m" varStatus="code_id"> 
                			<label><input type="radio" name="wVersion" id="wVersion" value="${m.code_id}">${m.code_nm}</label>
							</c:forEach>	   
						</c:when>
                		</c:choose>
					</td>
				</tr>
				
				<!-- 휴지통설정 -->
				<tr>
					<th>나의문서 휴지통 자동 비우기</th>
					<td>
						<label><input type="checkbox" name="p_check" id="p_check" onclick="javascript:exsoftAdminConfFunc.ui.isUseChange('p_check','#pdecade');">삭제 후</label>
						<label><input type="text" name="pdecade" id="pdecade"  class="limit" maxlength="4" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">일 지난 문서 자동 비우기</label>
					</td>
				</tr>
				<tr>
					<th>시스템관리자 휴지통 자동 비우기</th>
					<td>
						<label><input type="checkbox" name="s_check" id="s_check" onclick="javascript:exsoftAdminConfFunc.ui.isUseChange('s_check','#sdecade');">삭제 후</label>
						<label><input type="text" name="sdecade" id="sdecade" class="limit" maxlength="4" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">일 지난 문서 자동 비우기</label>
					</td>
				</tr>
				
				<!-- URL유통설정 -->
				<tr>
					<th>URL 유통 시 조회 유효기간</th>
					<td>
						<label><input type="text" name="expired" id="expired" data-bind="expired" maxlength="3"  class="limit" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">일로 제한 (0일 경우 제한 없음)</label>
					</td>
				</tr>
				
				<!-- [3000] -->
				<tr>
					<th>만기문서 사전 알림설정</th>
					<td>
						<p><input type="text" name="expiredComeDay" id="expiredComeDay" data-bind="expiredComeDay" maxlength="3"  class="limit" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">일전 알람 메세지 발송</p>
					   <label><input type="radio" name="cExpiredAlarm"   value ="Y" >사전알림 사용</label>
            		   <label><input type="radio" name="cExpiredAlarm"   value ="N">사전알림 미사용</label>
						
					</td>
				</tr>
				<!-- [3001] -->
				<tr>
					<th>만기문서 만료 알림설정</th>
					<td>						
					   <label><input type="radio" name="lExpiredAlarm"   value ="Y" >만료알림 사용</label>
            		   <label><input type="radio" name="lExpiredAlarm"   value ="N">만료알림 미사용</label>
						
					</td>
				</tr>
				
				<!-- [2000] 중복로그인설정 -->
				<tr>
					<th>중복로그인 설정</th>
					<td>						
					   <label><input type="radio" name="f_logout" value ="Y" >중복로그인 허용</label>
            		   <label><input type="radio" name="f_logout" value ="N">중복로그인 불가</label>
					</td>
				</tr>
			</table>
		</div>
		<!-- [2001] End -->
		
		<div class="tab_form hide"  id="fileConfigFrm">		
			<table>
				<colgroup>
				<col width="205">
				<col width="1023">
				</colgroup>
				<tr>
					<th>등록파일제한</th>
					<td>
						<input type="text"  name='ext' id='ext' class="limit_extension" maxlength="255">
						<span class="dim">등록된 확장자는 첨부할 수 없습니다.</span>
					</td>
				</tr>
				<tr>
					<th>첨부파일 갯수 제한</th>
					<td>
						<input type="checkbox" name="cfileCnt" id="cfileCnt" onclick="javascript:exsoftAdminConfFunc.ui.isUseChange('cfileCnt','#fileCnt');">
						문서당 첨부파일을
						<input type="text" name="fileCnt" id="fileCnt" class="limit" maxlength="2" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">
						개로 제한합니다.
					</td>
				</tr>
				<tr>
					<th>첨부파일 사이즈 제한</th>
					<td>
						<input type="checkbox" name="cfileSize" id="cfileSize" onclick="javascript:exsoftAdminConfFunc.ui.isUseChange('cfileSize','#fileSize');">
						<input type="text"  name="fileSize" id="fileSize" class="limit" maxlength="4" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">
						MB 이상은 등록을 제한합니다. (최대 2GB)
					</td>
				</tr>
				<tr>
					<th>문서당 첨부파일 총 사이즈 제한</th>
					<td>
						<input type="checkbox"  name="cfileTotal" id="cfileTotal" onclick="javascript:exsoftAdminConfFunc.ui.isUseChange('cfileTotal','#fileTotal');">
						<input type="text" name="fileTotal" id="fileTotal" class="limit" maxlength="4" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">
						MB 이상은 등록을 제한합니다.
					</td>
				</tr>
			</table>
		</div>
		
		<div class="tab_form hide" id="auditConfigFrm">
			<table>
				<colgroup>
				<col width="205">
				<col width="1023">
				</colgroup>
				<tr>
					<th>첨부파일 열람건수 기준</th>
					<td>
						<p><input type="text"  name="read_count_threshold" id="read_count_threshold" data-bind="read_count_threshold" class="limit" onkeydown="return exsoft.util.filter.numInput(event);" onkeyup="this.value=this.value.replace(/[^0-9]/g,'')">건</p>
						<label><input type="checkbox" name="is_mail" id="is_mail" data-bind="send_report_mail">메일 자동 전송</label>
					</td>
				</tr>
				<tr>
					<th>메일 수신자</th>
					<td>
						<textarea name="report_mail_receiver_address" id="report_mail_receiver_address" data-bind="report_mail_receiver_address" class="textAreaClsss"></textarea>
						<span class="dim">* 다수의 메일 수신자 지정 시 세미콜론(;)으로 구분합니다.</span>
					</td>
				</tr>
			</table>
		</div>
		
		</form>
	</div>	
	
	<div class="btnGrp">
		<button type="button" class="btn1 left hide">기본 설정으로</button>
		<button type="button" class="btn1 bold" onclick="javascript:exsoftAdminConfFunc.event.configUpdate();">확인</button>
		<button type="button" class="btn1" onclick="javascript:exsoftAdminConfFunc.ui.cancelFunc();">취소</button>
	</div>		
</div>
<script type="text/javascript" src="${contextRoot}/js/sysadmin/confManager.js"></script>
 <script type="text/javascript">
jQuery(function() {		
		
	exsoft.util.filter.maxNumber();			// maxlength
	exsoftAdminLayoutFunc.init.menuInit();
	exsoftAdminLayoutFunc.init.menuSelected('${topSelect}','${subSelect}');
	exsoftAdminConfFunc.init.initPage('${defaultFileSize}','${productInfo}','basicConfig');
	
});
</script>