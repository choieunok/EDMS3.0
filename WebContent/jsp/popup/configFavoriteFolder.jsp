<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript" src="${contextRoot}/js/popup/configFavoriteFolder.js"></script>
<!-- 
	Usage : 즐겨찾기 폴더구성	
 -->
<div class="favorite_configChoose_wrapper hide"></div>
<div class="favorite_configChoose hide">
	<div class="favorite_configChoose_title">
		즐겨찾기 폴더 구성
		<a href="#" class="favorite_configChoose_close"><img src="${contextRoot}/img/icon/window_close.png" alt="" title=""></a>
	</div>
	<div class="favorite_configChoose_cnts">
		<div class="folder_disposition">
<!-- 			<button type="button" class="disposition_upward" onclick="javascript:configFavoriteFolder.event.fGoPrevNode();">위로</button> -->
<!-- 			<button type="button" class="disposition_downward" onclick="javascript:configFavoriteFolder.event.fGoNextNode();">아래로</button> -->
			<a href="javascript:void(0);" onclick='javascript:configFavoriteFolder.event.fGoPrevNode();'>▲</a>
			<a href="javascript:void(0);" onclick='javascript:configFavoriteFolder.event.fGoNextNode();'>▼</a>
		</div>
		<div id="favorite_folder_tree"></div>

	</div>
	<div class="favorite_btnGrp">
		<button type="button" class="favorite_save_btn" class="" onclick="javascript:configFavoriteFolder.event.okButtonClick();">닫기</button>
		<!-- <button type="button" class="favorite_cancel_btn" class=""  -->
		<!-- onclick="javascript:exsoftMypageFunc.treeFunctions.cancelButtonClick();">취소</button> -->
	</div>
</div>