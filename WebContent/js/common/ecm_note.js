$(function(){
	//탭 요소 클릭 시 폼 변경
    $('.tab_element').bind("click", function(){
        var idx = $(this).index();
        var targetFrm = $(this).parent().parent().parent().find('div[class^="tab_form"]');
        targetFrm.addClass('hide');
        targetFrm.eq(idx).removeClass('hide');

        $('.tab_element').removeClass('selected');
        $(this).addClass('selected');
    });

	//쪽지검색 검색분류항목
	$('#myNote_srch_type').ddslick({
		width:93,
		background:"rgba(255, 255, 255, 0)",
		onSelected: function(selectedData){}
	});

    $('.note_reply').bind("click", function(){
    	var tabIdx = $('[class^="tab_element"][class$="selected"]').index();

    	//답장을 보낼 대상자 정보를 받아서
    	//뿌려주고 show 시킴
    	//tabIdx == 2 : 보낸 쪽지함
    	//보낸쪽지함은 전달로, 나머지는 답장 창 호출
    	if(tabIdx == 2) {
    		$('.myNoteForward').removeClass('hide');
    	} else {
    		$('.myNoteReply').removeClass('hide');
    	}

    });

    //쪽지 전달 - 창 닫기
    $('.myNoteForward_close').bind("click", function(e){
    	e.preventDefault();
    	$(this).parents('.myNoteForward').addClass('hide');
//    	$('.note_choose_wrapper').addClass('hide');
    });

    //쪽지 답장 - 창 닫기
    $('.myNoteReply_close').bind("click", function(e){
    	e.preventDefault();
    	$(this).parents('.myNoteReply').addClass('hide');
//    	$('.note_choose_wrapper').addClass('hide');
    });

	//쪽지 관리 - 대화함 내용 지우기
	$('.myNote_cnts').find('a.delete_myNote_chat').bind("click", function(e){
		e.preventDefault();
		jConfirm("전체 대화 내역을 삭제하시겠습니까?", "확인", 2, function(r){

		});
	});

	//쪽지 관리 - 쪽지 지우기
	$('.myNote_cnts').find('a.delete_myNote').bind("click", function(e){
		e.preventDefault();
		jConfirm("쪽지를 삭제하시겠습니까?", "확인", 2, function(r){

		});
	});

	//쪽지 관리 - 쪽지 보관
	$('.myNote_cnts').find('a.inbox_myNote').bind("click", function(e){
		e.preventDefault();
		jConfirm("선택한 쪽지를 보관함에 보관 하시겠습니까?", "확인", 6, function(r){

		});
	});
});

