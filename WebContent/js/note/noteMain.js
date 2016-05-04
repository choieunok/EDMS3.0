
/**
 * 쪽지 관련 스크립트
 */





$(function(){
	
	
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
var jsonArr = [];
var exsoftNoteFunc = {
		
		nPage :1,

		initAction : {

			//url : exsoft.contextRoot,
			
			//선택탭 영역에 따른 액션 분기
			noteSelectAction : function(index) {

				exsoftNoteFunc.nPage = 1;
				$("#searchKeyword").val("");
				if(index==0){
					exsoftNoteFunc.event.talkboxInit("","");
				}else if(index==1){
					exsoftNoteFunc.event.reciveInit("","");
				}else if(index==2){
					exsoftNoteFunc.event.sendInit("","");
				}else if(index==3){
					exsoftNoteFunc.event.saveboxInit("","");
				}
			},
			
			//선택된 탭의 액션 리프레쉬
			noteRefresh : function() {
				exsoftNoteFunc.nPage = 1;
				
				$("#searchKeyword").val("");
				var select_index = ($('span[class*=selected]').index());				
				if(select_index==0){
					exsoftNoteFunc.event.talkboxInit("","");
				}else if(select_index==1){
					exsoftNoteFunc.event.reciveInit("","");
				}else if(select_index==2){
					exsoftNoteFunc.event.sendInit("","");
				}else if(select_index==3){
					exsoftNoteFunc.event.saveboxInit("","");
				}
			},
			
		},
		
		//NOTE 관련 Action.
		event : {
			//받은 편지함 초기화
			reciveInit : function(content,rsender_name) {
				$('#noteRecive').empty();
				$('#noteSend').empty();
				$('#noteSave').empty();
				$('#noteTalk').empty();
				var buffer = "";
				//var strLink = "javascript:exsoftNoteFunc.event.gridPageSend";
				exsoft.util.ajax.ajaxDataFunctionWithCallback({note_name:"Receive",content:content,rsender_name:rsender_name,nPage:exsoftNoteFunc.nPage}, exsoft.contextRoot+"/note/noteReceiveSendList.do", "note", function(data, e) {
					
					$(data.list).each(function(index){
						//$("#newNoteCnt").html((data.list[0].newnote_cnt));
						buffer += "<tr>";
						buffer += "<td class='tooltip'><img src='"+exsoft.contextRoot+"/img/icon/mynote_receive.png' alt='' title=''></td>";
						buffer += "<input type='hidden' id='note_save' name='note_save' value='"+data.list[index].note_save+"'>";
						buffer += "<input type='hidden' id='note_manageid' name='note_manageid' value='"+data.list[index].manage_id+"'>";
						buffer += "<input type='hidden' id='note_id' name='note_id' value='"+data.list[index].note_id+"'>";
						buffer += "<input type='hidden' id='root_id' name='root_id' value='"+data.list[index].root_id+"'>";
						buffer += "<input type='hidden' id='creator_id' name='creator_id' value='"+data.list[index].creator_id+"'>";
						buffer += "<input type='hidden' id='rsender_name' name='rsender_name' value='"+data.list[index].rsender_name+"'>";
						buffer += "<td class='subject' colspan='3'  onclick=\"javascript:exsoftNoteFunc.show_hide.showContent("+index+");\">";					
						buffer += "<span class='title' >["+data.list[index].rsender_name+"]</span>";
						//보여지는 내용은 10 까지만
						var content ="";
						if(data.list[index].content.length > 10) {
							 content = data.list[index].content.substring(0,10) + "...";
						 }else {
							 content = data.list[index].content;
						 }	
						//읽지 않은 쪽지 Bold
						if(data.list[index].note_read=='Y') {
							buffer += "<span class='abbr_contents' onclick=\"javascript:exsoftNoteFunc.event.updateNoteRead("+index+");\">"+content+"</span>";
						}else{
							buffer += "<span class='abbr_contents' onclick=\"javascript:exsoftNoteFunc.event.updateNoteRead("+index+");\"><b>"+content+"</b></span>";
						}
						
						buffer += "</td>";
						buffer += "<td class='noteDate'>"+data.list[index].create_date+"</td>";
						buffer += "<td class='listMenu'>";
						buffer += "<img src='"+exsoft.contextRoot+"/img/icon/note_compose.png' alt='' title='답장' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm('myNoteReply','noteReplyFrm','RE','"+index+"');\" >";
						
						buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_inbox.png' alt='' title='보관함이동' onclick=\"javascript:exsoftNoteFunc.event.updateNoteSave('"+index+"');\" >";
						buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_bin.png' alt='' title='삭제' onclick=\"javascript:exsoftNoteFunc.event.deleteSelectNote('"+index+"','Receive');\" >";
						
						buffer += "</td>";
						buffer += "</tr>";	
						buffer += "<tr id='trhide' class='preview hide '>";
						buffer += "<td class='tooltip'></td>";
						buffer += "<td class='subject' colspan='5'>";
						buffer += "<span class='abbr_contents' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm('myNoteWrite','noteWriteFrm');\">"+data.list[index].content+"</span>";
	             		buffer += "</td>";
	             		buffer += "</tr>";   		    	
						
					});
					$('#noteRecive').empty().append(buffer);

					// Paging Navigation											
					exsoft.util.grid.gridPager("#recivePageing",data);
			
					
				});	
			
			}, //받은 편지함 초기화 END
			
			quickPager : function(options) {
				$(options.holder).empty();
				
				var defaults = {
		            pageSize: 6,
		            naviSize: 2,
		            currentPage: 1,
					holder: "",
					select:""
		    	};
		    	var options = $.extend(defaults, options);
			  	
				//leave this
				var selector = options.select; //$("#noteRecive");
				var totalRecords = selector.children().length;//$(this).children().length;
				//var totalRecords =(selector.children().length)/2;// selector.children().length;;//$(this).children().length;
				
				var pageCounter = 1;

				selector.children().each(function(i){
					
					if(i < pageCounter*options.pageSize && i >= (pageCounter-1)*options.pageSize) {
						$(this).addClass("page"+pageCounter);
					}
					else {
						$(this).addClass("page"+(pageCounter+1));
						pageCounter ++;
					}					
				});
				
				//show/hide the appropriate regions 
				selector.children().hide();
				$(".page"+options.currentPage).show();
				$(".preview").hide();  //내용보기 라인 숨기기
				$(".chat_view").hide();
				
				//first check if there is more than one page. If so, build nav
				if(pageCounter > 1) {
					//Build pager navigation
					var pageNav = "<ul class='pg_navi'>";	
					for (i=1;i<=pageCounter;i++){
						if (i==options.currentPage) {
							pageNav += "<li class=\"currentPage curr pageNav"+i+"\"'><a rel='"+i+"' href='javascript:void(0);'>"+i+"</a></li>";		
						} else {
							pageNav += "<li class='pageNav"+i+"'><a rel='"+i+"' href='javascript:void(0);'>"+i+"</a></li>";
						}
						// 1 2 ... 4 5 next prev 6 ... 10 next prev 11.... 20 next prev ... next ...
						if (0 == (i % options.naviSize)) {
							 
							pageNav += "<li class='next-"+(i+1)+"'><a rel='"+(i+1)+"' href='javascript:void(0);'>next</a></li>";
							pageNav += "<li class='prev-"+i+"'><a rel='"+i+"' href='javascript:void(0);'>prev</a></li>";
						}
					}
					pageNav += "</ul>";
					
					if(options.holder == "") {
						selector.after(pageNav);
					}
					else {
						$(options.holder).append(pageNav);
					}					
					var start = 1;
					var end = options.naviSize;
					
					// all hide and show start to end page with navigation.					
					//$('.pageNav').children().hide();
					$(".pg_navi").find('a').hide();
					for (i=start; i<=end; i++) {
						if (i == end) {
							$('.prev-'+i).find('a').hide();
							$('.next-'+(i+1)).find('a').show();
							
						} else if (i == start) {
							$('.next-'+(i+1)).find('a').hide();
							$('.prev-'+i).find('a').show();
						}
						$('.pageNav'+i).find('a').show();
						//$('.pageNav'+i).show();						
					}
					$('.curr').find('a').show();		
					
					//pager navigation behaviour
					//$('.myNote_cnts').find('a.inbox_myNote').bind("click", function(e){
					//$(".pageNav a").bind("click", function() {
					$(".pg_navi").find('a').bind("click", function() {
					//pagemove: function(options) {			
						//grab the REL attribute 
						
						var clickedLink = $(this).attr("rel");
						/*if(clickedLink <= (totalRecords/naviSize)){
							
						}*/
						if($(this).text()=='prev') {
							end = Math.ceil(clickedLink / options.naviSize) * options.naviSize;
							start = end - options.naviSize+1;
							
							//$('.pageNav').children().hide();
							$(".pg_navi").find('a').hide();
							for (var i=start; i<=end; i++) {
								if (i == end) {
									$('.prev-'+i).find('a').hide();
									$('.next-'+(i+1)).find('a').show();
								} else if (i == start) {
									$('.next-'+(i+1)).find('a').hide();
									$('.prev-'+(i-1)).find('a').show();
								}								
								$('.pageNav'+i).find('a').show();
							}
							
						} else if ($(this).text() == 'next') {
							start = Math.floor(clickedLink / options.naviSize) * options.naviSize + 1;
							end = start + (options.naviSize-1);
							//$('.pageNav').children().hide();
							$(".pg_navi").find('a').hide();
							
							for (i=start; i<=end; i++) {
								if (i == end) {
									$('.prev-'+i).find('a').hide();
									//if( (totalRecords/naviSize) != end){
									$('.next-'+(i+1)).find('a').show();										
									//}
								} else if (i == start) {
									$('.next-'+(i+1)).find('a').hide();
									$('.prev-'+(i-1)).find('a').show();
								}
								$('.pageNav'+i).find('a').show();									
							}
						}
						
						options.currentPage = clickedLink;
						//remove current current (!) page
						//$("li.currentPage").removeClass("currentPage");
						$("li.curr").removeClass("curr");
						//Add current page highlighting
						//$("ul.pg_navi").find("a[rel='"+clickedLink+"']").parent("li").addClass("currentPage");
						$("ul.pg_navi").find("a[rel='"+clickedLink+"']").parent("li").addClass("curr");
						//hide and show relevant links		
						selector.children().hide();
						selector.find(".page"+clickedLink).show();
						$(".preview").hide();//내용보기 라인 숨기기
						$(".chat_view").hide();
						
						return false;
					});
					
				}
					  
			},//paging END
			
			
			
			//보낸편지함 초기화
			sendInit : function(content,note_from) {	
				$('#noteRecive').empty();
				$('#noteSend').empty();
				$('#noteSave').empty();
				$('#noteTalk').empty();
				var buffer = "";
				exsoft.util.ajax.ajaxDataFunctionWithCallback({note_name:"Send",content:content,note_from:note_from,nPage:exsoftNoteFunc.nPage}, exsoft.contextRoot+"/note/noteReceiveSendList.do", "note", function(data, e) {
					
					$(data.list).each(function(index){
										
						buffer += "<tr>";
						buffer += "<td class='tooltip'><img src='"+exsoft.contextRoot+"/img/icon/mynote_send.png' alt='' title=''></td>";
						
						
						buffer += "<input type='hidden' id='note_save' name='note_save' value='"+data.list[index].note_save+"'>";
						buffer += "<input type='hidden' id='note_manageid' name='note_manageid' value='"+data.list[index].manage_id+"'>";
						buffer += "<input type='hidden' id='note_id' name='note_id' value='"+data.list[index].note_id+"'>";
						buffer += "<input type='hidden' id='root_id' name='root_id' value='"+data.list[index].root_id+"'>";	
						buffer += "<input type='hidden' id='content' name='content' value='"+data.list[index].content+"'>";	
						
						buffer += "<td class='subject' onclick=\"javascript:exsoftNoteFunc.show_hide.showContent("+index+");\">";					
						buffer += "<span class='title' >[ "+(data.list[index].note_from).replace(/;/gi," ")+"]</span>";
						var content ="";
						if(data.list[index].content.length > 10) {
							 content = data.list[index].content.substring(0,10) + "...";
						 }else {
							 content = data.list[index].content;
						 }	 
						buffer += "<span class='abbr_contents'>"+content+"</span>";
						
						
						buffer += "</td>";
						buffer += "<td class='noteDate'>"+data.list[index].create_date+"</td>";
						buffer += "<td class='listMenu'>";
						buffer += "<img src='"+exsoft.contextRoot+"/img/icon/note_compose.png' alt='' title='전달' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm('myNoteForward','noteForwardFrm','Trans','"+index+"');\" >";
							
						buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_inbox.png' alt='' title='보관함이동' onclick=\"javascript:exsoftNoteFunc.event.updateNoteSave('"+index+"');\" >";
						buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_bin.png' alt='' title='삭제' onclick=\"javascript:exsoftNoteFunc.event.deleteSelectNote('"+index+"','Send');\" >";
						
						buffer += "</td>";
						buffer += "</tr>";	
						buffer += "<tr class='preview hide'>";
						buffer += "<td class='tooltip'></td>";
						buffer += "<td class='subject' colspan='3'>";
						buffer += "<span class='abbr_contents' >"+data.list[index].content+"</span>";
	             		buffer += "</td>";
	             		buffer += "</tr>";   		    	
						
					});
					$('#noteSend').empty().append(buffer);
					//paging

					exsoft.util.grid.gridPager("#sendPageing",data);
					
				});	
			
			},//보낸편지함 초기화 END
			
			
			//쪽지 보관함 초기화
			saveboxInit : function(content,rsender_name) {
				$('#noteRecive').empty();
				$('#noteSend').empty();
				$('#noteSave').empty();
				$('#noteTalk').empty();
				var buffer = "";
				exsoft.util.ajax.ajaxDataFunctionWithCallback({note_name:"Save",content:content,rsender_name:rsender_name,nPage:exsoftNoteFunc.nPage}, exsoft.contextRoot+"/note/noteReceiveSendList.do", "note", function(data, e) {
					/*if($(data.list).size()==0){
						buffer += "검색결과가 존재하지 않습니다.";
					}*/
					$(data.list).each(function(index){
						var note_type= data.list[index].note_type;				
						buffer += "<tr>";
						buffer += "<td class='tooltip'>";
						if(note_type=="S"){
							buffer += "<img src='"+exsoft.contextRoot+"/img/icon/mynote_send.png' alt='' title=''></td>";
						}else{
							buffer += "<img src='"+exsoft.contextRoot+"/img/icon/mynote_receive.png' alt='' title=''>";
						}

						buffer += "</td>";
						buffer += "<input type='hidden' id='note_save' name='note_save' value='"+data.list[index].note_save+"'>";
						buffer += "<input type='hidden' id='note_manageid' name='note_manageid' value='"+data.list[index].manage_id+"'>";
						buffer += "<input type='hidden' id='note_id' name='note_id' value='"+data.list[index].note_id+"'>";
						buffer += "<input type='hidden' id='root_id' name='root_id' value='"+data.list[index].root_id+"'>";	
						
						buffer += "<td class='subject' onclick=\"javascript:exsoftNoteFunc.show_hide.showContent("+index+");\">";					
						buffer += "<span class='title' >["+data.list[index].rsender_name+"]</span>";
						var content ="";
						if(data.list[index].content.length > 10) {
							 content = data.list[index].content.substring(0,10) + "...";
						 }else {
							 content = data.list[index].content;
						 }	 
						buffer += "<span class='abbr_contents'>"+content+"</span>";
						
						buffer += "</td>";
						buffer += "<td class='noteDate'>"+data.list[index].create_date+"</td>";
						buffer += "<td class='listMenu'>";
						buffer += "<img src='"+exsoft.contextRoot+"/img/icon/note_compose.png' alt='' title='답장' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm('myNoteWrite','noteWriteFrm','RE','"+data.list[index].rsender_name+"');\" >";
							
						buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_bin.png' alt='' title='삭제' onclick=\"javascript:exsoftNoteFunc.event.deleteSelectNote('"+index+"','Save');\" >";
						
						buffer += "</td>";
						buffer += "</tr>";	
						buffer += "<tr class='preview hide'>";
						buffer += "<td class='tooltip' ></td>";
						buffer += "<td class='subject' colspan='3'  >";
						buffer += "<span class='abbr_contents' >"+data.list[index].content+"</span>";
	             		buffer += "</td>";
	             		buffer += "</tr>";   		    	
						
					});
					$('#noteSave').empty().append(buffer);
					//paging
					exsoft.util.grid.gridPager("#savePageing",data);
					/*
					var options ={
							pageSize: 20,
							naviSize: 10,
							currentPage: 1,
							holder: "#savePageing",
							select: $("#noteSave")
					};
					exsoftNoteFunc.event.quickPager(options);*/
				});	
				
			
			},//쪽지 보관함 초기화 END
			
			

			//대화함 초기화
			talkboxInit : function(content,rsender_name) {
				$('#noteRecive').empty();
				$('#noteSend').empty();
				$('#noteSave').empty();
				$('#noteTalk').empty();
				var buffer = "";
				exsoft.util.ajax.ajaxDataFunctionWithCallback({note_name:"talk",content:content,rsender_name:rsender_name,nPage:exsoftNoteFunc.nPage}, exsoft.contextRoot+"/note/noteSelectTalk.do", "note", function(data, e) {
					
				
					var note_id ="";
					$(data.list).each(function(index){
						note_id = data.list[index].note_id;
						if(data.list[index].note_id == data.list[index].root_id){
							buffer += "<tr>";
							buffer += "<td class='tooltip'><img src='"+exsoft.contextRoot+"/img/icon/mynote_tooltip.png' alt='' title=''></td>";

							
							buffer += "<td class='subject' onclick=\"javascript:exsoftNoteFunc.show_hide.showTalkContent('show_cnt"+data.list[index].root_id+"');\">";					
							buffer += "<span class='title' >["+(data.list[index].note_from).replace(/;/gi,"")+"]</span>";
							var content ="";
							if(data.list[index].content.length > 10) {
								 content = data.list[index].content.substring(0,10) + "...";
							 }else {
								 content = data.list[index].content;
							 }	 
							buffer += "<span class='abbr_contents'>"+content+"</span>";
							
							buffer += "</td>";
							buffer += "<td class='noteDate'>"+data.list[index].create_date+"</td>";
							buffer += "<td class='listMenu'>";
							buffer += "<a href=\"javascript:void(0);\" class=\"note_reply\">";
							buffer += "<img src='"+exsoft.contextRoot+"/img/icon/note_compose.png' alt='' title='새쪽지' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm('myNoteWrite','noteWriteFrm','RE','"+data.list[index].rsender_name+"');\" >";
							buffer += "</a><a href=\"javascript:void(0);\" class=\"delete_myNote_chat\">";
							buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_bin.png' alt='' title='삭제' onclick=\"javascript:exsoftNoteFunc.event.deleteSelectNote('"+index+"','TalkAll');\" >";
							buffer += "</a>";
							buffer += "</td>";
							buffer += "</tr>";
						}
						
						if(note_id == data.list[index].root_id){
							buffer += "<tr class='chat_view hide' id='show_cnt"+data.list[index].root_id+"'>";
							buffer += "<td class='subject' colspan='4'>";
						}
							
											
						$(data.list).each(function(index2){
							
							if(note_id == data.list[index2].root_id){
								//rowcount++;
								
								//var notekbn = data.list[index2].note_kbn;	
								//var note_type= data.list[index2].note_type;	
								buffer += "<input type='hidden' id='note_save' name='note_save' value='"+data.list[index2].note_save+"'>";
								buffer += "<input type='hidden' id='note_manageid' name='note_manageid' value='"+data.list[index2].manage_id+"'>";
								buffer += "<input type='hidden' id='note_id' name='note_id' value='"+data.list[index2].note_id+"'>";
								buffer += "<input type='hidden' id='root_id' name='root_id' value='"+data.list[index2].root_id+"'>";			
								
								//수신자 발신자 좌우 구분
								if(exsoft.user.user_id == data.list[index2].creator_id){
									buffer += "<div class='me'>";							
								}else{
									buffer += "<div class='opposite'>";	
								}
								
								buffer += "<div>";
								buffer += "<span class='chat_user'>"+data.list[index2].rsender_name+"</span>";	
								buffer += "<span>"+data.list[index2].create_date+"</span>";
								
								buffer += " <img src='"+exsoft.contextRoot+"/img/icon/note_bin.png' alt='' title='삭제' onclick=\"javascript:exsoftNoteFunc.event.deleteSelectNote('"+index2+"','Talk');\" >";						
								//buffer += "<a href=\"#\"><img src='"+url+"/img/icon/note_bin1.png' alt='' title='휴지통' onclick=\"javascript:exsoftNoteFunc.newOpen.noteCommonFrm();\" ></a>";
								buffer += "</div>";
								buffer += "<div class='chat_box'>"+data.list[index2].content;

								buffer += "<div class='tooltip_tail'></div>";
								buffer += "</div>";
								buffer += "</div>";	
								
							}
							
							
					});//data index2 END
						
						if(note_id == data.list[index].root_id){
							buffer += "</tr></td>";
						}
					});//data index END
					
					$('#noteTalk').empty().append(buffer);
					
					//paging
					
					
					exsoft.util.grid.gridPager("#talkPageing",data);
					
					/*
					var options ={
							pageSize: 20,
							naviSize: 10,
							currentPage: 1,
							holder: "#talkPageing",
							select: $("#noteTalk")
					};
					exsoftNoteFunc.event.quickPager(options);*/
						
				});	
			},//대화함 초기화 END
			
			//쪽지 보관함 update
			updateNoteSave : function(rowindex) {
				var mId ="";
				var nsave ="";
				
				var select_index = ($('span[class*=selected]').index());
				if(select_index==0){
					mId = $("#TalkTAB [id='note_manageid']").eq(rowindex).val();
					nsave = $("#TalkTAB [id='note_save']").eq(rowindex).val();
				}else if(select_index==1){
					mId = $("#ReciveTAB [id='note_manageid']").eq(rowindex).val();
					nsave = $("#ReciveTAB [id='note_save']").eq(rowindex).val();
				}else if(select_index==2){
					mId = $("#SendTAB [id='note_manageid']").eq(rowindex).val();
					nsave = $("#SendTAB [id='note_save']").eq(rowindex).val();
				}else if(select_index==3){
					mId = $("#SaveTAB [id='note_manageid']").eq(rowindex).val();
					nsave = $("#SaveTAB [id='note_save']").eq(rowindex).val();
				}				
				
				//var mId = $("[name=recevenote_manageid]").eq(rowindex).val();
				//alert(mId);
				//var nsave = $("[name=note_save]").eq(rowindex).val();
				if(nsave=='Y'){

					jAlert('이미 보관함에 보관중인 쪽지 입니다.', "확인", 0);
					return false;
				}
				exsoft.util.ajax.ajaxDataFunctionWithCallback({manage_id:mId}, exsoft.contextRoot+"/note/noteSaveUpdate.do", "note", 
						function(data,e) {
							
							if(data.result == "true")	{
								jAlert('쪽지를 보관함에 저장했습니다.', "확인", 0);
								exsoftNoteFunc.initAction.noteRefresh();
							}else {
								jAlert(data.message, "확인", 8);
							}					
					});
				
			},// 쪽지 보관함 업데이트 END

			//쪽지 읽음 update
			updateNoteRead : function(rowindex) {
				var mId ="";
				
				mId = $("#ReciveTAB [id='note_manageid']").eq(rowindex).val();
				exsoft.util.ajax.ajaxDataFunctionWithCallback({manage_id:mId}, exsoft.contextRoot+"/note/noteReadUpdate.do", "note", 
						function(data,e) {
							
							if(data.result == "true")	{
								//exsoftNoteFunc.initAction.noteRefresh();
							}else {
								jAlert(data.message, "확인", 8);
							}					
					});
				
			},// 쪽지 읽음 업데이트 END
			//쪽지 삭제
			deleteSelectNote : function(rowindex,kbn) {
				
				var mId ="";
				var nId ="";
				var rId = "";
				var nsave = "";
				var del_kbn = "";
				var select_index = ($('span[class*=selected]').index());
				if(select_index==0){
					mId = $("#TalkTAB [id='note_manageid']").eq(rowindex).val();
					nId = $("#TalkTAB [id='note_id']").eq(rowindex).val();
					rId = $("#TalkTAB [id='root_id']").eq(rowindex).val();
					nsave = $("#TalkTAB [id='note_save']").eq(rowindex).val();
					
				}else if(select_index==1){
					mId = $("#ReciveTAB [id='note_manageid']").eq(rowindex).val();
					nId = $("#ReciveTAB [id='note_id']").eq(rowindex).val();
					rId = $("#ReciveTAB [id='root_id']").eq(rowindex).val();
					nsave = $("#ReciveTAB [id='note_save']").eq(rowindex).val();
					
				}else if(select_index==2){		

					mId = $("#SendTAB [id='note_manageid']").eq(rowindex).val();
					nId = $("#SendTAB [id='note_id']").eq(rowindex).val();
					rId = $("#SendTAB [id='root_id']").eq(rowindex).val();
					nsave = $("#SendTAB [id='note_save']").eq(rowindex).val();
					
				}else if(select_index==3){
					mId = $("#SaveTAB [id='note_manageid']").eq(rowindex).val();
					nId = $("#SaveTAB [id='note_id']").eq(rowindex).val();
					rId = $("#SaveTAB [id='root_id']").eq(rowindex).val();
					nsave = $("#SaveTAB [id='note_save']").eq(rowindex).val();
					
				}
				//보관함에 보관된 쪽지는 삭제 불가
				if((kbn != "Save") && (nsave == "Y")){
					jAlert("보관함에 보관된 쪽지는 삭제할 수 없습니다.", "확인", 0);
					return false;
				}
				var show_massage = "";
				if(kbn == "TalkAll" ){
					del_kbn="ALL";
					show_massage= "전체 대화 내역을 삭제하시겠습니까?";
				}else{
					show_massage= "쪽지를 삭제하시겠습니까?";
				}
				jConfirm(show_massage, "확인", 6, function(r){
					if(r){
						exsoft.util.ajax.ajaxDataFunctionWithCallback({manage_id:mId,note_id:nId,root_id:rId,del_kbn:del_kbn}, exsoft.contextRoot+"/note/noteDelete.do", "note", 
								function(data,e) {
									
									if(data.result == "true")	{
										jAlert('쪽지를 삭제했습니다.', "확인", 0);

										//화면 갱신 처리
										exsoftNoteFunc.initAction.noteRefresh();
									}else {
										  jAlert(data.message, "확인", 8);
									}
								
							});
					}
				});
				
			},//쪽지 삭제 END
			
			
			//쪽지 insert
			insertNoteWrite : function(divId,formId) {
				
				//var objForm = document.noteWriteFrm;
				//$("#"+formId).find("#noteReciver").text(0); //글자 수 초기화
				// validation check
				//if (objForm.noteReciver.value.length == 0) {
				if ($("#"+formId).find("#noteReciver").val().length == 0) {
				  	jAlert("받는 사람을 입력(선택)하세요.", "확인", 0);
					return false;
			    }
				
				//if (objForm.noteContent.value.length == 0 || objForm.noteContent.value.length == 0) {
				if ($("#"+formId).find("#noteContent").val().length == 0 || $("#"+formId).find("#noteContent").val() == null) {
				  	jAlert("쪽지 내용을 입력 하세요.", "확인", 0);
					return false;
			    }
				var reciver = $("#"+formId).find("#noteReciver").val();
				var content = $("#"+formId).find("#noteContent").val();
				
				var jsonArr = [];
				var jsonArrIndex = 0;
				
				//var result = new Object();
				var inputData = $("#"+formId).find("#reciverArrayList").val();
				//var jsonObject = { "reciveList":JSON.stringify(result)};
				var selectList = inputData.split("|");
				var senderCheck = false;
				for(var i=0;i<selectList.length;i++) {
					
					result = selectList[i].split("#");
					if(result.length == 3)	{						
						var rowData = {accessor_id : "",  accessor_isgroup : ""};
						rowData['accessor_id'] = result[1];
						rowData['accessor_isgroup'] =  result[0] == 'GROUP' ? 'T' : 'F';
						
						//쪽지 발신자가 본인과 동일하면
						if(exsoft.user.user_id == result[1] ){
							
							senderCheck = true;
						}
						
						if(rowData.accessor_id){
							jsonArr[jsonArrIndex] = rowData;
							jsonArrIndex ++;
						}
					}
				}
				
				if(senderCheck){
					jAlert('내게 보내기 기능은 제공하지 않습니다.', "확인", 6);
					return;
				}
				
				var jsonObject = { "reciveList":JSON.stringify(jsonArr),"content":content,"note_from":reciver};
				
				exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot+"/note/noteSendControl.do", 'note',
						function(data, e){
							if(data.result == 'true'){
								jAlert('쪽지를 발송을 완료했습니다', "확인", 0);
								$("#"+formId).find("#noteReciver").val("");
								exsoft.util.layout.popDivLayerClose(divId);
								//base.gridRefresh('authWasteGridList','${contextRoot}/mypage/authDocumentList.do');
							} else {
								jAlert(data.message, "확인", 8);
							}
						}
					); 
			
			},//쪽지 insert END
			
			
			
			searchKeyword : function(){

				exsoftNoteFunc.nPage = 1; //현재 페이지 초기화
				var index = exsoft.util.layout.getSelectBox('myNote_srch_type','option');
				var content ="";
				var rsender_name="";
				if(index==0){
					//내용세팅
					content = $("#searchKeyword").val();
				}else{
					rsender_name= $("#searchKeyword").val();
				}
				
				var select_index = ($('span[class*=selected]').index());
				if(select_index==0){
					exsoftNoteFunc.event.talkboxInit(content,rsender_name);
				}else if(select_index==1){
					exsoftNoteFunc.event.reciveInit(content,rsender_name);
				}else if(select_index==2){
					exsoftNoteFunc.event.sendInit(content,rsender_name);
				}else if(select_index==3){
					exsoftNoteFunc.event.saveboxInit(content,rsender_name);
				}
				
			},
			
			
			//쪽지 Reply
			insertNoteReWrite : function() {
				
				var objForm = document.noteReplyFrm;
				
				// validation check
				if (objForm.noteReReciver.value.length == 0) {
				  	jAlert("받는 사람을 입력(선택)하세요.", "확인", 0);
					return false;
			    }
				
				if (objForm.noteReContent.value.length == 0 || objForm.noteReContent.value.length == 0) {
				  	jAlert("쪽지 내용을 입력 하세요.", "확인", 0);
					return false;
			    }
				
				var root_id = $("#noteReplyroot_id").val();
				var creator_id = $("#noteReplycreator_id").val();
				var note_from_userid= $('#noteReplycreator_id').val();				
				var reciver = objForm.noteReReciver.value;
				var content = objForm.noteReContent.value ;
				
				
				
				var paramObject = { "root_id":root_id,"creator_id":creator_id,"note_from_userid":note_from_userid,"content":content,"note_from":reciver};
				
				exsoft.util.ajax.ajaxDataFunctionWithCallback(paramObject, exsoft.contextRoot+"/note/noteReSendControl.do", 'note',
						function(data, e){
							if(data.result == 'true'){
								jAlert('쪽지를 발송을 완료했습니다', "확인", 0);
								exsoft.util.layout.popDivLayerClose('myNoteReply');
								//base.gridRefresh('authWasteGridList','${contextRoot}/mypage/authDocumentList.do');
							} else {
								jAlert(data.message, "확인", 8);
							}
						}
					); 
				
				
			},//쪽지 Reply END	
			
			
			// 페이지이동 처리(공통)
			gridPager : function(page) {	
				var index = exsoft.util.layout.getSelectBox('myNote_srch_type','option');
				var content ="";
				var rsender_name="";
				if(index==0){
					//내용세팅
					content = $("#searchKeyword").val();
				}else{
					rsender_name= $("#searchKeyword").val();
				}
				
				exsoftNoteFunc.nPage = page;
				
				var select_index = ($('span[class*=selected]').index());				
				if(select_index==0){
					exsoftNoteFunc.event.talkboxInit(content,rsender_name);
				}else if(select_index==1){
					exsoftNoteFunc.event.reciveInit(content,rsender_name);
				}else if(select_index==2){
					exsoftNoteFunc.event.sendInit(content,rsender_name);
				}else if(select_index==3){
					exsoftNoteFunc.event.saveboxInit(content,rsender_name);
				}
			},		
			
			// 페이지이동 처리(공통)
			gridPagerSend : function(page) {
				//$("#sendPageing").setGridParam({page:page}).trigger("reloadGrid");
				$("#sendPageing").setGridParam({page:page,postData:{is_search:'true',page_init:'false'}}).trigger("reloadGrid");
			},
			// 페이지이동 처리(공통)
			gridPageSave : function(page) {
				$("#savePageing").setGridParam({page:page,postData:{is_search:'true',page_init:'false'}}).trigger("reloadGrid");
			},
			
			// 엔터키 입력시
			enterKeyPress : function(e) {
				if (e.keyCode == 13) {
					exsoftNoteFunc.event.searchKeyword();
					return false;
				}
			},
			
			
		},//event END
		
		
		show_hide : {

			// 쪽지 내용보기 show/hide
			showTalkContent : function(rowindex) {
				$(exsoft.util.common.getIdFormat(rowindex)).toggle();				
			},
			

			// 쪽지 내용보기 show/hide
			showContent : function(rowindex) {
				$('.preview:eq('+ rowindex +')').toggle();
			},
			
		},
		
		

		newOpen : {

			// 쪽지보내기 사용자선택
			userSelectUrl : "/note/noteUserSelect.do",
			userSelectTarget : "noteUserFrm",
			
			//contextRoot : "",
			// 사용자 환경설정 새창 CALL
			userSelect : function() {
				this.contextRoot = exsoft.contextRoot;
				this.openWindow(this.userSelectTarget,920,630);
				this.formInit(document.noteUserFrm,this.contextRoot+this.userSelectUrl,this.userSelectTarget);

			},

			// 새창 띄우기
			openWindow : function(targetName,width,height) {
				var win= "";
				win = window.open("",targetName,"width="+width+", height="+height+", toolbar=no, menubar=no, scrollbars=no, resizable=no" );
				win.focus();			// 새창의 경우 항상 맨위로
				
			},
		
			formInit : function(formName,url,targetName)	{	

				var frm = formName;
				frm.action = url;
				frm.method = "post";
				frm.target = targetName;
				frm.submit();
				
				
			},
			
			// 쪽지쓰기(전달/답장)
			noteCommonFrm : function(divClass,formId,kbn,index) {
				
				$("#"+formId).find("span.current_count").text(0); //글자 수 초기화
				
				$("."+divClass).removeClass('hide');
				exsoft.util.common.formClear("#"+formId);
				
				//답신일 경우 값 셋팅
				if(kbn=='RE'){
					$("#noteReplyroot_id").val($("#ReciveTAB [id='root_id']").eq(index).val());
					$("#noteReplycreator_id").val($("#ReciveTAB [id='creator_id']").eq(index).val());
					$('#noteReReciver').val($("#ReciveTAB [id='rsender_name']").eq(index).val());	
				}
				//전달일 경우
				if(kbn=='Trans'){
					 $("#"+formId).find("textarea").val($("#SendTAB [id='content']").eq(index).val());//forward 시 본문 내용셋팅
//					 exsoftNoteFunc.ui.contentLength('noteForwardFrm'); //글자수 셋팅
					 var $textarea = $('#noteForwardFrm textarea');
					 $textarea.next().children('span').text($textarea.val().length);
				}
			},
		},
		
		ui : {
		}//ui
		
		
		
		
};