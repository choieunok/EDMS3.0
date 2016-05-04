<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>		
<nav class="lnb_menu">
	<jsp:include page="/jsp/layout/userLeftTop.jsp" /><!-- 목록관련 공통적용화면 --> 
	<section class="lnb_tree">
		<div class="coop_tree_menu">
			<div class="tree_menu_list1">
				<span>협업관리</span>
			</div>
		</div>
		<div class="lnb_tree_list tree">
			<div class="coop_menu expand">
				<a href="#" class="bold">내 업무 진행함</a>
			</div>
			<div class="coop_submenu">
				<ul>
					<li class="process_ing selected"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.ING_MENU);">진행중 문서</a></li>
					<li class="process_writeIng"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.WRITE_ING_MENU);">작성중 문서</a></li>
					<li class="process_appIng"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.APPROVAL_ING_MENU);">승인중 문서</a></li>
				</ul>
			</div>
			<div class="coop_menu expand">
				<a href="#" class="bold">내 업무 완료함</a>
			</div>
			<div class="coop_submenu">
				<ul>
					<li class="process_request"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.REQUEST_MENU);">요청한 문서</a></li>
					<li class="process_writeEnd"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.WRITE_END_MENU);">작성한 문서</a></li>
					<li class="process_appEnd"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.APPROVAL_END_MENU);">승인한 문서</a></li>
					<li class="process_receive"><a href="javascript:exsoftProcessFunc.init.menuInitPage(Constant.PROCESS.RECEIVE_MENU);">수신문서</a></li>
				</ul>
			</div>
		</div>
	</section>
</nav>
<script type="text/javascript">
jQuery(function() {
	$('.coop_menu').off('click'); // 중복 bind 방지를 위한 코드
	$('.coop_menu').on("click", function(e){
		var menu = $(this);
		var subMenu = menu.next();
	
		if(!subMenu.hasClass('hide')){
			subMenu.addClass('hide');
			menu.removeClass('expand').addClass('fold');
		} else {
			subMenu.removeClass('hide');
			menu.removeClass('fold').addClass('expand');
		}
	});
});
</script>