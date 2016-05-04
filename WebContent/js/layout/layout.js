$(function(){

    $('.quickMenu_close').bind("click", function(e){
    	e.preventDefault();
    	$(this).parents('.quickMenu').addClass('hide');
    	$('.quickMenu_wrapper').addClass('hide');
    });

    //환경설정 > 퀵메뉴 추가 - 창 닫기 : 음영진 부분 클릭 시 닫기
    $('.quickMenu_wrapper').bind("click", function(){
    	$(this).addClass('hide');
    	$('.quickMenu').addClass('hide');
    });

});
/**
 * Layout 관련 스크립트
 */

var exsoftLayoutFunc = {

		// 사용자 환경설정
		userConfigUrl : "/user/userConfig.do",
		userConfigTarget : "popFrm",

		// 쪽지관리
		noteMainUrl : "/note/noteMain.do",
		noteMainTarget : "popNoteFrm",

		// 퀵메뉴관리
		quickMenuCnt : 5,

		// 시스템관리
		userRole : "CREATOR",
		adminUrl : "/adminPage.do",
		adminTarget : "adminFrm",

		tabHeight : {
			'myinfo' : '572',
			'passwdConf' : '592',
			'myconfig' :  '552',
		},

		init : {

			formInit : function(formName,url,tabType,targetName){
				var frm = formName;
				frm.action = url;
				frm.method = "post";
				frm.target = targetName;
				frm.tabType.value = tabType;
				frm.submit();
			},

			// 퀵메뉴 선택 초기화
			quickMenuInit : function() {
				$("input[name=quickMenu]:checkbox").each(function()	{
						$(this).prop("checked",false);
				});
			},

			// userTopMenu 퀵메뉴 초기화
			quickTopMenuInit : function() {
				exsoftLayoutFunc.event.quickMenuProc('top','header');

			},

			// 메인문서목록 리스트 가져오기
			mainDocList : function(tableId,actionType) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"actionType":actionType}, exsoft.contextRoot + "/document/mainDocumentList.do" , "mainDocList",
						function(data,e) {
							if(data.result == "true"){
								// 문서상세보기 링크 추가 TODD
								/*******************************************************************************************
								 * 권한정보 : acl_level / acl_create / acl_checkoutCancel / acl_change_Permission
								 *******************************************************************************************/
								exsoft.util.table.tablePrintMainList(tableId, data.list, false, true,true,exsoft.contextRoot);
								exsoftLayoutFunc.ui.addTableNoData(tableId,3);;
							}else {
								exsoftLayoutFunc.ui.addTableNoData(tableId,3);
							}
					});
			},

			// 메인 받은쪽지 현황
			mainNoteList : function(tableId) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"note_name":"Receive"}, exsoft.contextRoot + "/note/noteReceiveSendList.do" , "noteList",
						function(data,e) {
							if(data.result == "true"){
								exsoft.util.table.tablePrintNoteList(tableId, data.list, false, true);
								exsoftLayoutFunc.ui.addTableNoData(tableId,2);
								$('#'+tableId).delegate('tr', 'click', function (event) {
									exsoftLayoutFunc.open.noteMain('RECEIVE');
								});

							}else {
								exsoftLayoutFunc.ui.addTableNoData(tableId,2);
							}
					});

			},

			//메인화면 협업 건수 가져오기
			mainProcessCountInfo : function(){

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({'type' : Constant.PROCESS.WRITE_ING_MENU},exsoft.contextRoot +'/process/processCount.do',
						'#mainContent_processWrite', exsoftLayoutFunc.callback.infoCount);

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({'type' : Constant.PROCESS.APPROVAL_ING_MENU},exsoft.contextRoot +'/process/processCount.do',
						'#mainContent_processApprove', exsoftLayoutFunc.callback.infoCount);

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({'type' : Constant.PROCESS.MODIFY_ING_MENU},exsoft.contextRoot +'/process/processCount.do',
						'#mainContent_processModify', exsoftLayoutFunc.callback.infoCount);

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({'type' : Constant.PROCESS.RECEIVE_MENU},exsoft.contextRoot +'/process/processCount.do',
						'#mainContent_processInformed', exsoftLayoutFunc.callback.infoCount);

			},

			//메인화면 협업 리스트 가져오기
			mainProcessList : function(actionType) {
				var type = null;
				switch(actionType) {
					case "WRITE_ING" :
						type = " ( 작성중 )";
						break;
					case "APPROVAL_ING" :
						type = " ( 승인중 )";
						break;
					case "MODIFY_ING" :
						type = " ( 보완중 )";
						break;
					case "RECEIVE" :
						type = " ( 미열람 )";
						break;
				}
				$("#mainContent_ing").html(type);
				
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"type":actionType, "is_main":true}, exsoft.contextRoot+'/process/processList.do' , "mainContent_processList",
						function(data,tableId) {
							if(data.result == "true" && data.list.length > 0){
								$(exsoft.util.common.getIdFormat(tableId) + " tbody").empty();

								// 출력할 데이터의 Row를 추가하고 값을 설정한다.
								$(data.list).each(function(index){
									exsoft.util.table.tableAddRow(tableId);

									var tdCols = $(exsoft.util.common.getIdFormat(tableId) + " thead tr:eq(0) th");
									$(tdCols).each(function(tdIndex) {
										var colName = $(this).attr("name");
										var doc_root_id = data.list[index]["doc_root_id"];
										var process_id = data.list[index]["process_id"];

										if(colName != null && tdIndex<5) {
											if(tdIndex == 0 && colName == 'name'){
												$(exsoft.util.common.getIdFormat(tableId) + " tr:last td:eq(" + tdIndex + ")")
												.html(data.list[index][colName].length > 13 ?
														"<a onclick='javascript:exsoftLayoutFunc.init.callPopup(\"{0}\",\"{1}\",\"{2}\");'>".format(process_id, doc_root_id, actionType) + data.list[index][colName].substring(0,13)+"...</a>"
														: "<a onclick='javascript:exsoftLayoutFunc.init.callPopup(\"{0}\",\"{1}\",\"{2}\");'>".format(process_id, doc_root_id, actionType) + data.list[index][colName]+"</a>");
												// 협업상세화면 js에 값넘김처리
												exsoftProcessViewFunc.mainProcessAction = actionType;
											} else {
												$(exsoft.util.common.getIdFormat(tableId) + " tr:last td:eq(" + tdIndex + ")").html(data.list[index][colName]);
											}

										}
									});
								});


							}else {
								$(exsoft.util.common.getIdFormat(tableId) + " tbody").empty();
								$(exsoft.util.common.getIdFormat(tableId)+' tbody').append('<tr><td class="nodata" width="298px;" colspan="3">데이터가 없습니다.</td></tr>');
							}
					});
			},
			
			// 제목클릭시 팝업화면
			callPopup : function(process_id, doc_root_id, actionType) {
				// 작성중, 승인중, 보완중 일때 (processView)
				if(actionType != "RECEIVE") {
					exsoftProcessViewFunc.init.initProcessView(process_id,doc_root_id);
				// 미열람일때 (documentDetail)
				} else {
					exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"doc_root_id":doc_root_id}, exsoft.contextRoot+'/document/selectCurrentDocID.do', null, function(docId_data,param){
						if(docId_data.result == 'true'){
							exsoft.document.layer.docCommonFrm("doc_detail_wrapper","doc_detail",docId_data.current_doc_id);
						}
					});
				}
			}

		},

		open : {

			// 새창 띄우기
			openWindow : function(targetName,width,height,resizable) {
				var win;
				win = window.open("",targetName,"width="+width+", height="+height+", toolbar=no, menubar=no, scrollbars=no, resizable="+resizable );
				win.focus();			// 새창의 경우 항상 맨위로
			},


			// 사용자 환경설정 새창 CALL
			userConfig : function(tabType) {
				exsoftLayoutFunc.open.openWindow(exsoftLayoutFunc.userConfigTarget,740,exsoftLayoutFunc.tabHeight[tabType],"no");
				exsoftLayoutFunc.init.formInit(document.popFrm,exsoft.contextRoot+exsoftLayoutFunc.userConfigUrl,tabType,exsoftLayoutFunc.userConfigTarget);
				$(".info_dropDown_menu").addClass("hide");
			},

			// 쪽지관리 메인 새창 CALL
			noteMain : 	function(tabType) {

				exsoftLayoutFunc.open.openWindow(exsoftLayoutFunc.noteMainTarget,730,690,"no");
				exsoftLayoutFunc.init.formInit(document.popNoteFrm,exsoft.contextRoot+exsoftLayoutFunc.noteMainUrl,tabType,exsoftLayoutFunc.noteMainTarget);
				$(".note_dropDown_menu").addClass("hide");
			},

			// 시스템관리 페이지 새창 CALL
			adminUrl : function(roleId) {
				if(exsoftLayoutFunc.userRole == roleId) {
					jAlert('접근권한이 없습니다.','확인',7);
					return false;
				}
				exsoftLayoutFunc.open.openWindow(exsoftLayoutFunc.adminTarget,1024,730,"yes");
				exsoftLayoutFunc.init.formInit(document.adminFrm,exsoft.contextRoot+exsoftLayoutFunc.adminUrl,roleId,exsoftLayoutFunc.adminTarget);
			},

			// 통합검색 Window
			searchDetail : function() {
				if($("#searchDetailView").hasClass('hide')) {
					$("#searchDetailView").removeClass('hide');
					exsoft.util.common.ddslick('#strDocType', 'srch_type6', '', 80, function(){});
					exsoft.util.common.formClear("#searchForm");
					$("#strSdate").datepicker({dateFormat:'yy-mm-dd'});
					$("#strEdate").datepicker({dateFormat:'yy-mm-dd'});
				} else {
					$("#searchDetailView").addClass('hide');
				}
			},

			// LeftMenu 문서등록 변경처리
			docWriteForm : function() {

				switch(exsoft.util.layout.currentTopMenu()) {
					case Constant.TOPMENU.MYWORK :		// 개인문서
						exsoft.document.layer.docWriteCommonFrm('doc_register_wrapper','doc_register',myDocList.folderId,myDocList.folderPath);
						break;
					case Constant.TOPMENU.WORKSPACE :	// 업무문서함
						exsoft.document.layer.docWriteCommonFrm('doc_register_wrapper','doc_register',workDocList.folderId,workDocList.folderPath);
						break;
					default :
						exsoft.document.layer.docWriteCommonFrm('doc_register_wrapper','doc_register');
						break;
				}
			}

		},

		layer : {

		},

		close : {

			searchClose : function() {
				$("#searchDetailView").addClass('hide');
			}
		},

		event : {


			// 퀵메뉴 클릭시 컨텐츠 이동처리
			goContent : function(mode) {
				exsoft.util.layout.goUserContent(exsoft.contextRoot+'/mypage/myPageDocList.do?myMenuType='+mode);
			},

			// 좌측상단 협업파트 바로가기처리
			goProcessContent : function(processType) {
				exsoft.util.layout.goUserContent(exsoft.contextRoot+'/process/processLayout.do?processType='+processType);
			},

			// 통합검색 PAGE 이동처리
			goSearch : function() {
				exsoft.util.layout.goUserContent(exsoft.contextRoot+'/search/searchList.do');

			},

			// 퀵메뉴 Layout CALL
			quickMenuConfig : function(wrapperClass,layerClass) {
				exsoft.util.layout.divLayerOpen(wrapperClass,layerClass);
				exsoftLayoutFunc.event.quickMenuProc('select','footer');
			},

			// 퀵메뉴 저장처리
			quickMenuUpdate : function() {

				var jsonArrIndex = 0;

				// Validation
				var chkLength = exsoft.util.common.checkBoxCheckedLength('quickMenu');
				if(chkLength > 0 && chkLength > exsoftLayoutFunc.quickMenuCnt) {
					jAlert('퀵메뉴는 최대 5개까지 선택가능합니다.','확인',6);
					return false;
				}

				var jsonArr = [];

				$("input[name='quickMenu']:checked").each(function(index,val){
					var rowData = {menu_cd:""};
					rowData['menu_cd'] = val.defaultValue;
					jsonArr[jsonArrIndex] = rowData;
					jsonArrIndex++;
				});

				var jsonObject = {"type":"update","menu_cd":JSON.stringify(jsonArr)};				// type:update

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot + "/user/quickMenu.do" , "quickMenu",

					function(data,e) {

						if(data.result == "true"){
							exsoftLayoutFunc.event.quickMenuProc('top','header');		// TOP 메뉴 변경처리 함수 CALL
							jAlert('퀵메뉴가 변경처리 되었습니다.','확인',8);
						}else {
							jAlert('퀵메뉴 저장하는데 실패했습니다','확인',7);
							exsoft.util.layout.divLayerClose('quickMenu_wrapper', 'quickMenu')
						}

				});

			},

			// 퀵메뉴 데이터 가져오기
			quickMenuProc : function(type,location) {

				var jsonObject = {"type":type};
				var buffer = "";

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot + "/user/quickMenu.do" , "quickMenu",

					function(data,e) {

						if(data.result == "true"){

							if(data.quickMenuListCnt > 0)	{

								if(location == "footer")	{

									$(".quickMenu_sub_wrapper").empty();

									for(var m in data.quickMenuList) {
										// 기본값
										buffer += "<label><input type='checkbox' name='quickMenu' value='"+data.quickMenuList[m].menu_cd+"'><span>"+data.quickMenuList[m].menu_nm+"</span></label>";
									}

									$(".quickMenu_sub_wrapper").append(buffer);

									if(data.userSelectMenuCnt > 0) {

										for(var n in data.userSelectMenu) {
											$("input[name=quickMenu]:checkbox").each(function()	{
												if($(this).val() == data.userSelectMenu[n].menu_cd )	{
													$(this).prop("checked",true);
												}

											});
										}
									}

								}else 	if(location == "header")	{
									$(".quick_sub_menu").empty();
									for(var m in data.quickMenuList) {
										buffer += "<li><a href=\"javascript:exsoftLayoutFunc.event.goContent('"+data.quickMenuList[m].menu_cd+"')\">"+data.quickMenuList[m].menu_nm+"</a></li>";		// 기본값
									}
									$(".quick_sub_menu").append(buffer);
								}
							}else {
								if(location == "header")	{
									$(".quick_sub_menu").empty();
								}
							}


						}else {
							jAlert('퀵메뉴 로드하는데 실패했습니다','확인',7);
							exsoft.util.layout.divLayerClose('quickMenu_wrapper', 'quickMenu')
						}

				});

			},
			//알게이트 에이전트 설치
			rgaterun : function(){
				$(location).attr("href", exsoft.contextRoot + "/files/EDMS_AGENT[1.0.0.62].exe");
			}
		},

		ui : {

			addTableNoData : function(tableId,colspan){
        		if($(exsoft.util.common.getIdFormat(tableId)+' tbody').children('tr').length == 0){
        			$(exsoft.util.common.getIdFormat(tableId)+' tbody').append('<tr id="'+tableId+'_noData"><td colspan='+colspan+' class="nodata">데이터가 없습니다.</td></tr>');
        		}
        	},

		},

		callback : {

			// 새쪽지, 신규문서, 승인대상문서, 업무작성중문서
			infoCount : function(data, param){

				if(param == "#tempDocNewCnt" || param == "#newDocCnt")	{		// 작업카트,최신문서
					if(data.records > 99)	{
						$(param).text('99+');
					}else {
						$(param).text(data.records);
					}
				}else {
					$(param).text(data.count);
				}
			},

			// 새쪽지 개수 및 목록 표시
			noteInfo : function(data,param) {

				$(param).text(data.count);

				$("#newNoteList").empty();

				var buffer = "";
				var content = "";

				if(data.count == 0)	{
					buffer += "<li><a href=\"javascript:exsoftLayoutFunc.open.noteMain('RECEIVE');\"><span class='note_title'>새 쪽지가 없습니다.</span></a></li>";
				}else {

					for (var n in data.list) {
						var totalContent = "["+data.list[n].rsender_name+"]"+data.list[n].content; 
						// TODO :: 쪽지선택시 해당 쪽지 바로 열기 적용
						if(totalContent.length > 14) {
							 content =totalContent.substring(0,14) + "..."; 
						 }else {
							 content = totalContent; 
						 }

						buffer += "<li><a href=\"javascript:exsoftLayoutFunc.open.noteMain('RECEIVE');\">";
						buffer += "<span class='note_title'>"+content+"</span></a></li>";

						if(n == 4) break;			// 미리목록개수 5개
					}

				}

				$("#newNoteList").append(buffer);
			}

		}
}