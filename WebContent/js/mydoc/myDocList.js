/**
 * myDocList.js
 * 
 * [2000][로직수정]	2015-09-14	이재민	 : 업무/개인문서함 - Drag&Drop기능 - 여러개문서 선택후 이동가능하게 수정
 */
var	myDocList = {

	/**
	 * member variables
	 */
	workType : Constant.WORK_MYPAGE,
	folderId : null,
	folderPath : null,

	tree : {
		mypageTree : null
	},

	// 0. 초기화
	init : {
		isInitialized : false,

		//page 초기화
		initPage : function(pageSize,menuType){
			// 화면분할시 로딩중팝업을 위한 값세팅
			exsoft.menuType = menuType;
			
			// 트리 초기화
			myDocList.treeFunctions.initTree();

			// UI 초기화
			myDocList.init.initUi(pageSize);

			// 그리드 초기화
			myDocList.grid.initGrid();
		},

		// 화면 구성 요소 초기화 (ddslick 등)
		initUi : function(pageSize) {
			if (!myDocList.init.isInitialized) {
				//검색 selectbox
				exsoft.util.common.ddslick('#myDoc_select', 'srch_type1', '', 79, function(divId, selectedData){
				});

				// 목록 출력 갯수
				exsoft.util.common.ddslick('#myDocListRowCount', 'srch_type1', '', 68, function(divId, selectedData){
					$("#myDocList").setGridParam({page:1, rowNum:selectedData.selectedData.value, postData:{is_search:'false',page_init:'true'}}).trigger("reloadGrid");
				});


				//depth navigation
				$('.depth_navi > span').mouseover(function(){
					var path = $(this).parent().find(".depth_navi_path");
					if(!path.is(":visible")) {
						path.removeClass('hide');
					}
				}).mouseout(function(){
					var path = $(this).parent().find(".depth_navi_path");
					if(path.is(":visible")) {
						path.addClass('hide');
					}
				});

				myDocList.grid.pageSize = pageSize;


				// 트리 리프레시 이벤트 연동
				$("#treeRefresh").bind("click", function() {
					 myDocList.treeFunctions.refresh();
				});

				// workspace 탭 숨김 / mypage 탭 보임
				$("[data-group=treeTabWorkspace]").each(function(idx) {
					$(this).addClass("hide").removeClass("focus");
				});
				$("[data-group=treeTabMypage]").each(function(idx) {
					$(this).removeClass("hide").addClass("focus");
				});


				// workspace 트리 숨김 / mypage 트리 보임
				$("[data-group=workspaceTree]").each(function(idx) {
					$(this).addClass("hide");
				});

				$("[data-group=mypageTree]").each(function(idx) {
					$(this).removeClass("hide");
				});

				// 페이지목록 값 설정
				exsoft.util.layout.setSelectBox('myDocListRowCount',myDocList.grid.pageSize);

				isInitialized = true;
			}
		}
	},

	treeContextAction : {
		// Tree Context : 폴더 조회Callback
		detailFolder : function(node) {
			exsoft.util.layout.divLayerOpen(detailfolderWindow.wrapperClass, detailfolderWindow.layerClass);
			detailfolderWindow.callback = myDocList.callback.refreshTree;
			detailfolderWindow.initForm(node, node.id);

		},
		// Tree Context : 폴더 생성 Callback
		createFolder : function(node) {
			exsoft.util.layout.divLayerOpen(folderWindow.wrapperClass, folderWindow.layerClass);
			folderWindow.callback = myDocList.callback.refreshTree;
			folderWindow.initForm(node);
		},

		// Tree Context : 폴더 수정 Callback
		modifyFolder : function(node) {
			exsoft.util.layout.divLayerOpen(folderWindow.wrapperClass, folderWindow.layerClass);
			folderWindow.callback = myDocList.callback.refreshTree;
			folderWindow.initForm(node, node.id);

		},

		// Tree Context : 폴더 이동 Callback
		moveFolder : function(node) {
			selectSingleFolderWindow.init(myDocList.callback.moveFolder, Constant.MAP_MYPAGE, Constant.WORK_MYPAGE, true, "ALL_TYPE");
		},

		// Tree Context : 폴더 삭제 Callback
		deleteFolder : function(node) {
			selectSingleFolderWindow.callback = myDocList.callback.refreshTree;

			var jo = {
					folder_id : node.id,
					folder_name_ko : node.text,
					type : "DELETE"
			}

			jConfirm("폴더를 삭제 하시겠습니까?", "폴더 삭제", 2, function(r) {

				if (r) {

					exsoft.util.ajax.ajaxDataFunctionWithCallback(jo, exsoft.contextRoot+"/folder/folderControl.do", "", function(data) {

						if (data.result == "true") {
							myDocList.treeFunctions.refresh();
						} else {
							jAlert(data.message,'확인',7);
						}
					});
				}
			});
		},
		
		// Tree Context : 폴더 내보내기 callback
		excelPrintFolder : function(node) {
			jConfirm("하위 폴더구조도 함께 내보내겠습니까? \n 취소시 해당 폴더의 정보만 조회합니다.", "폴더 내보내기", 1, function(ret) {
				
				var oper = null;
				// 하위도 같이
				if(ret) oper = "hierarchy";
				// 자기자신만
				else oper = "self";
				
				$(location).attr("href", exsoft.contextRoot + "/folder/excelPrintFolder.do?folder_id="+node.id+"&oper="+oper);
			});
		},
		
		// Tree Context : 폴더 가져오기 callback
		excelUploadFolder : function(node) {
			excelUploadWindow.oper = "myDoc";
			excelUploadWindow.current_id = node.id;
			excelUploadWindow.event.folderUploadView();
		},

		// Tree Context : 즐겨찾기 추가 callback
		addFavoriteFolder : function(node) {

			// 1. 이미 추가된 폴더인지 체크
			exsoft.util.ajax.ajaxDataFunctionWithCallback({folder_id : node.id, type : "CHECK_EXISTS"}, exsoft.contextRoot+"/folder/favoriteControl.do", "", function(data, param) {
				if (data.result == "true") {
					var jsonObject = {
						folder_id : node.id,
						folder_nm : node.text,
						mode : "ADD_FAVORITE",
						only_virtual : "Y"
					}

					// 즐겨찾기 부모 선택창 팝업 (selectFavoriteFolderWindow.jsp 필요함)
					selectFavoriteFolderWindow.init(jsonObject, false, function(returnObject) {
						returnObject.type = "ADD_TO_FAVORITES";

						exsoft.util.ajax.ajaxDataFunctionWithCallback(returnObject, exsoft.contextRoot+"/folder/favoriteControl.do", "", function(data, param) {
							if (data.result == "true") {
								jAlert("즐겨찾기 폴더 등록 완료",'즐겨찾기',8);
							} else {
								jAlert(data.message,'확인',7);
							}
						});
					});

				} else {
					jAlert(data.message,'즐겨찾기',6);
					return;
				}
			});
		}
	},

	grid : {
		initGrid : function() {
			// 초기화 여부
			if ($("#myDocList")[0].grid != undefined) {
				$('#myDocList').jqGrid('GridUnload');
			}

			var _postData = {
					folder_id : myDocList.folderId,
					strIndex : exsoft.util.layout.getSelectBox('myDoc_select','option'),
					strKeyword1 : exsoft.util.common.sqlInjectionReplace($("#strKeyword1").val()),
					work_type : Constant.WORK_MYPAGE
			}

			// Grid 세팅
			$('#myDocList').jqGrid({
				url: exsoft.contextRoot + '/document/workDocumentList.do',
				mtype:"post",
				datatype:'json',
				loadui:"disable",
				jsonReader:{
					page:'page',total:'total',root:'list'
				},
				cmTemplate: { title: false }, // tooltip 제거
				colNames : ['doc_id','page_cnt','relation_doc','is_locked','doc_name','type_name','creator_name','create_date',
				            'acl_create','acl_changePermission','acl_checkoutCancel','root_id','doc_type','lock_date','lock_owner','is_inherit_acl','lock_status', 'folder_id','acl_level'],
				colModel : [
					{name:'doc_id', index:'doc_id', width:1, editable:false, sortable:false, key:true, align:'center', hidden:true},
					{name:'page_cnt', index:'page_cnt', width:10, editable:false, sortable:false, resizable:true, align:'center',
						formatter : function(cellValue, option, rowObject) {
							return cellValue > 0 ? "<li class='icon' id='file_"+rowObject.doc_id+"'><img src='"+ exsoft.contextRoot +"/img/icon/attach.png' class='attach_file'></li>" : "";
						}
					},
					{name:'relation_doc', index:'relation_doc', width:10, editable:false, sortable:false, resizable:true, align:'center',
						formatter : function(cellValue, option, rowObject) {
							return cellValue > 0 ? "<li class='icon' id='relation _"+rowObject.doc_id+"'><img src='"+ exsoft.contextRoot +"/img/icon/link.png' class='relative_docs'></li>" : "";
						}
					},
					{name:'is_locked', index:'is_locked', width:10, editable:false, sortable:false, resizable:true, align:'center',
						formatter : function(cellValue, option, rowObject) {
							if(cellValue == 'T'){
								return "<li class='icon'><img src='"+ exsoft.contextRoot +"/img/icon/lock1.png' onclick='javascript:myDocList.event.DocumentUnLock(\""+rowObject.doc_id+"\",\""+rowObject.doc_type+"\",\""+rowObject.acl_checkoutCancel+"\")' class='doc_lock'></li>";
							}else{
								return "";
							}
						},
						cellattr : function(rowId, cellValue, rowObject) {
							var tooltip = '<p>반출자 : '+rowObject.lock_owner+'</p>';
							tooltip += '<p>반출일시 : '+rowObject.lock_date+'</p>';
							var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'myDocList\',\''+tooltip+'\',\'true\',event)"';
						    var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';

						    return rowObject.is_locked == 'T' ? mouseMove+' '+mouseOut : "";
						}
					},
					{name:'doc_name', index:'doc_name', width:150, editable:false, sortable:true, resizable:true,
						formatter : function(cellValue, option, rowObject){

							if(rowObject.page_extension_img != "/img/extension/no_file.png") {
								return	"<img src='{0}{1}' class='extension' onError='javascript:this.src=\"{0}/img/extension/no_file.png\"' >".format(exsoft.contextRoot, rowObject.page_extension_img) +
								"<a href='#' onclick='exsoft.preview.event.getPreview(\"{0}\",\"{1}\")'>{2}</a>".format(rowObject.doc_id, rowObject.acl_level, cellValue.length > 50 ? cellValue.substring(0,50) + "..." : cellValue) +
								"<a href='#' onclick='myDocList.event.popDocDetail(\"{0}\")'><img src='{1}/img/icon/new_window.png' class='new_window'></a>".format(rowObject.doc_id, exsoft.contextRoot);
							}else {
								return "<a href='#' onclick='exsoft.preview.event.getPreview(\"{0}\",\"{1}\")' style='margin-left:19px'>{2}</a>".format(rowObject.doc_id, rowObject.acl_level, cellValue.length > 50 ? cellValue.substring(0,50) + "..." : cellValue) +
								"<a href='#' onclick='myDocList.event.popDocDetail(\"{0}\")'><img src='{1}/img/icon/new_window.png' class='new_window'></a>".format(rowObject.doc_id, exsoft.contextRoot);
							}


						},
						cellattr : function(rowId, cellValue, rowObject) {
							var tooltip = '<p>'+rowObject.doc_name+'</p>';
							var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'myDocList\',\''+tooltip+'\',\'false\',event)"';
						    var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';

						    // 문서제목 마우스오버시 제목에 대한 툴팁제공 - 황상무님의 요청에의해 제거
						    //return mouseMove+' '+mouseOut;
						}
					},
					{name:'type_name', index:'type_name', width:40, editable:false, sortable:true, resizable:true, align:'center'},
					{name:'creator_name', index:'creator_name', width:30, editable:false, sortable:true, resizable:true, align:'center'},
					{name:'create_date', index:'create_date', width:30, editable:false, sortable:true, resizable:true, align:'center'},
					{name:'acl_level', index:'acl_level', width:20, editable:false, sortable:false, resizable:true, align:'center',
						formatter : function(cellValue, option) {
							return "<li class='previlege'><img src='"+ exsoft.contextRoot +"/img/icon/prev_"+ (cellValue.toLowerCase()).substring(0,1) +".png' class='previlege_grade'><label class='hide'>" + exsoft.util.grid.getAclItemTitle(cellValue) + "</label</li>";
						},
						cellattr: function (rowId, cellValue, rowObject) {
							var tooltip = '<p>소유자 : '+rowObject.owner_name+'</p>';
								tooltip += '<p>기본권한 : '+ exsoft.util.grid.getAclItemTitle(rowObject.acl_level) + '</p>';
								tooltip += '<p>반출취소 : '+(rowObject.acl_checkoutCancel == 'T' ? "가능" : "없음")+'</p>';
								tooltip += '<p>권한변경 : '+(rowObject.acl_changePermission == 'T' ? "가능" : "없음")+'</p>';
							var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'myDocList\',\''+tooltip+'\',\'true\',event)"';
						    var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';

						    return mouseMove+' '+mouseOut;
			            }
					},

					{name:'acl_create', index:'acl_create', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'acl_changePermission', index:'acl_changePermission', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'acl_checkoutCancel', index:'acl_checkoutCancel', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'root_id', index:'root_id', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'doc_type', index:'doc_type', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'lock_date', index:'lock_date', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'lock_owner', index:'lock_owner', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'is_inherit_acl', index:'is_inherit_acl', width:1, editable:false, sortable:false, align:'center', hidden:true},
					{name:'lock_status',index:'lock_status',width:1,editable:false,sortable:false,align:'center',hidden:true},
					{name:'folder_id',index:'folder_id',width:1, editable:false,sortable:false,align:'center',hidden:true},
				],
				autowidth:true,viewrecords: true,multikey: "ctrlKey",multiselect:true,sortable: true,shrinkToFit:true,gridview: true,
				height:"auto",
				sortname : "create_date",
				sortorder:"desc",
				scrollOffset: 0,
				viewsortcols:'vertical',
				rowNum : myDocList.grid.pageSize,
				emptyDataText: "데이터가 없습니다.",
				caption:'문서목록',
				postData : _postData,
				onCellSelect : function(rowid, iCol, cellcontent, e) {

					var setCol = "";
					var preview = 'doc_preview';
					var file = 'attach_file';
					var relation = 'relative_docs';
					var lock = 'doc_lock';

					if(~cellcontent.indexOf(preview)){
						setCol = preview;
					} else if(~cellcontent.indexOf(file)) {
						setCol = file;
					} else if (~cellcontent.indexOf(relation)) {
						setCol = relation;
					} else if (~cellcontent.indexOf(lock)) {
						setCol = lock;
					}

					if(iCol == 0){
						// 체크시 row값을 set한다.(선택시 : rowid셋팅, 해제시 : rowid제거)
						$("#myDocList").jqGrid('setSelection',rowid);
					} else if(setCol == preview){
					} else if(setCol == file && cellcontent != ''){
						var row = $("#myDocList").getRowData(rowid);
						documentListLayerWindow.open.openAttachWindow(row);
					} else if(setCol == relation && cellcontent != ''){
						var row = $("#myDocList").getRowData(rowid);
						documentListLayerWindow.open.openRelationWindow(row);
					} else if(setCol == lock && cellcontent != ''){
					}
				},
				loadBeforeSend: function() {
					exsoft.util.grid.gridTitleBarHide('myDocList');
					exsoft.util.grid.gridNoDataMsgInit('myDocList');
				}
				,loadComplete: function(data) {

					if ($("#myDocList").getGridParam("records")==0) {
						exsoft.util.grid.gridNoDataMsg("myDocList","nolayer_data");
					}else {
						exsoft.util.grid.gridViewRecords('myDocList');
					}

					exsoft.util.grid.gridInputInit(false);

					exsoft.util.grid.gridPager("#myDocPager",data);

					$("tr.jqgrow", this).contextMenu('documentListLayer_context_menu', {
						bindings: {
				            // 수정
				            'documentListLayer_update' : function(trigger) {
				            	var row = $("#myDocList").getRowData(trigger.id);
				            	var aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(row.acl_level));

				            	if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("UPDATE")) {
				            		jAlert("문서 수정 권한이 없습니다.", "수정", 6);
				            		return false;
				            	}

				            	exsoft.document.layer.docUpdateCommonFrm("doc_modify_wrapper","doc_modify",trigger.id);
				            	//documentUpdate(trigger.id, fRefreshDocumentList);
							},
							// 삭제
							'documentListLayer_delete': function(trigger) {
								var row = $("#myDocList").getRowData(trigger.id);
								var aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(row.acl_level));
								var jsonArr = [{
									doc_id : row.doc_id
									, root_id : row.root_id
									, is_locked : row.lock_status
									, doc_type : row.doc_type
									, map_id : Constant.MAP_MYPAGE
								}];

								if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("DELETE")) {
	        						jAlert("문서 삭제 권한이 없습니다.", "삭제", 6);
	        						return false;
	        					}

								documentListLayerWindow.gObjectID = "myDocList";
								documentListLayerWindow.event.documentDeleteSend(jsonArr, "ONLY");
				            },
							// 이동
				            'documentListLayer_move': function(trigger) {
				            	var row = $("#myDocList").getRowData(trigger.id);
				            	var aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(row.acl_level));
				            	var jsonArr = [{
				            		doc_id : row.doc_id
				            		, doc_name : exsoft.util.common.stripHtml(row.doc_name)
				            		, is_locked : row.lock_status
				            		, root_id : row.root_id
				            		, doc_type : row.doc_type
				            		, is_inherit_acl : row.is_inherit_acl
				            		, folder_id : myDocList.folderId
				            	}];

				            	if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("UPDATE")) {
		        					jAlert("문서 이동 권한이 없습니다.", "이동", 6);
		        					return false;
		        				}

				            	documentListLayerWindow.gObjectID = "myDocList";
				            	documentListLayerWindow.gWorkType = null;
								documentListLayerWindow.event.documentMove("ONLY", jsonArr);
				            },
				            // 복사
				            'documentListLayer_copy': function(trigger) {
				            	var row = $("#myDocList").getRowData(trigger.id);
				            	var aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(row.acl_level));
				            	var jsonArr = [{
				            		doc_id : row.doc_id
				            		, doc_name : exsoft.util.common.stripHtml(row.doc_name)
				            		, is_locked : row.lock_status
				            		, root_id : row.root_id
				            		, doc_type : row.doc_type
				            		, is_inherit_acl : row.is_inherit_acl
				            		, folder_id : myDocList.folderId
				            	}];

				            	if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("UPDATE")) {
		        					jAlert("문서 복사 권한이 없습니다.", "복사", 6);
		        					return false;
		        				}

				            	documentListLayerWindow.gObjectID = "myDocList";
				            	documentListLayerWindow.gWorkType = null;
								documentListLayerWindow.event.documentCopy("ONLY", jsonArr);
				            } ,
				            // 즐겨찾기 추가
				            'documentListLayer_favorite_add' : function(trigger) {
								var row = $('#myDocList').getRowData(trigger.id);
								var jsonArr = [{
									doc_id : row.doc_id
									,root_id : row.root_id
								}];

								documentListLayerWindow.event.documentAddFavoriteSend(jsonArr);
							},
				            // 작업카트 추가
							'documentListLayer_work_add': function(trigger) {
				            	var row = $("#myDocList").getRowData(trigger.id);
				            	var jsonArr = [{
				            		doc_id : row.doc_id
				            		, root_id : row.root_id
				            		, is_locked : row.lock_status
				            	}];

				            	documentListLayerWindow.event.documentTempworkSend(jsonArr);
				            } ,
							// 체크아웃 취소
				            'documentListLayer_checkout_cancel':function(trigger) {
								var row = $('#myDocList').getRowData(trigger.id);
								var jsonArr = [{
									doc_id : row.doc_id
									, root_id : row.root_id
									, is_locked : row.lock_status
									, doc_type : row.doc_type
								}];

								documentListLayerWindow.gObjectID = "myDocList";
								documentListLayerWindow.event.documentCancelCheckoutSend(jsonArr, "ONLY");
							},

				        },
				        onContextMenu: function(event) {
				        	var row = $('#myDocList').getRowData(event.currentTarget.id);
							$("#documentListLayer_update").removeClass('hide');
			        		$("#documentListLayer_delete").removeClass('hide');
			        		$("#documentListLayer_move").removeClass('hide');
			        		$("#documentListLayer_copy").removeClass('hide');
			        		$("#documentListLayer_favorite_add").removeClass('hide');

			        		if (row.lock_status == "T")
			        			$("#documentListLayer_checkout_cancel").removeClass('hide');
			        		else
			        			$("#documentListLayer_checkout_cancel").addClass("hide");
				            return true;
				        }
					});

					// Grid Resize
					exsoft.util.grid.gridResize('myDocList','myDocListTarget',28,0);
					
					// Drag&Drop jqgrid row to jstree
					myDocList.grid.makeGridRowsDraggable($("#" + this.id));

				}
				,loadError:function(xhr, status, error) {
					exsoft.util.error.isErrorChk(xhr);
				 }
				
			});

		 	// 컬럼 헤더 정렬 및 다국어 변경 처리
			var headerData = '{"doc_id":"doc_id","page_cnt":"<img src=\'{0}/img/icon/attach.png\' class=\'attach_file\' style=\'margin-top:5px;margin-bottom:5px;\'>","relation_doc":"<img src=\'{0}/img/icon/link.png\' class=\'relative_docs\' style=\'margin-top:5px;margin-bottom:5px;\'>","is_locked":"<img src=\'{0}/img/icon/lock.png\' class=\'doc_lock\' style=\'margin-top:5px;margin-bottom:5px;\'>","doc_name":"제목","type_name":"문서유형","creator_name":"등록자","create_date":"등록일","acl_level":"권한"}'.format(exsoft.contextRoot);
			exsoft.util.grid.gridColumHeader('myDocList',headerData,'center');
			headerData = null;
		},

		refresh : function(page) {
			$("#myDocList").setGridParam({page:page, postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
		},
		
		// Drag시 문서명 박스 생성
		makeGridRowsDraggable : function(grid) {
		    myDocList.grid.createDroppableElements();
		    $("#myDocList").val(new Date().getTime());
		    var $searchResultsGrid = grid;
		    var searchResultsRows = $("#myDocList .ui-row-ltr");

		    searchResultsRows.draggable({ appendTo: "body" }); searchResultsRows.draggable({
		        create: function (event, ui) { }
		    });
		   
		    searchResultsRows.draggable("option", "helper", "clone").draggable({
		        revert: "true",
		        appendTo: 'body',
		        snap: "true",
		        cursorAt: {
		            top: 10,
		            left: -5
		        },
		        helper: function (event) {
		            //get a hold of the row id
		            var rowId = $(this).attr('id');

		            //my code
		            var rowData = jQuery("#myDocList").getRowData(rowId);
		            Id = "  문서명 : " + rowData['doc_name'];
		            //set the data on this to the value to grab when you drop into input box
		            $(this).data('colValue', Id);
		            var dialog = ($('#DragableWidget').length > 0) ? $('#DragableWidget') :
		                         $('<div id="DragableWidget" class="draggedValue ui-widget-header ui-corner-all"></div>').appendTo('body');
		            dialog.html(Id);
		            return dialog;
		        }
		        , start: function (event, ui) {
		            //fade the grid
		            $(this).parent().fadeTo('fast', 0.5);
		        }
		        , stop: function (event, ui) {
		            $(this).parent().fadeTo(0, 1);
		        }
		    });
		},

		// 폴더에 drop시 이벤트
		createDroppableElements : function() {
		    $("#mypageTree").droppable({
		        tolerance: 'pointer',
		        hoverClass: 'active',
		        activate: function (event, ui) {
		            $(this).addClass("over");
		        },
		        deactivate: function (event, ui) {
		            $(this).removeClass("over");
		        },

		        drop: function (ev, ui) {
		        	var _item = ev.target;
		        	var target_folder_id = $(_item).attr("aria-activedescendant");
		        	
		        	// [2000] Start 여러개의 문서를 선택할 때와 한개만 이동하는 두개 방법으로 분기
		        	var jsonArr = [];
        			var jsonArrIndex = 0;
        			var aclLevel = null;
		        	var id = $("#workDocList").getGridParam('selarrrow');
		        	
		        	if(id.length > 0) {
		        		// 문서를 여러개 체크하여 이동시
		        		for(var i = 0; i < id.length; i++){
		        			var rowData = {doc_id : "", doc_name : "", is_locked : "",	
		        					root_id : "", doc_type : "", is_inherit_acl : "", folder_id : ""};
		        			var rowId = $("#workDocList").getRowData(id[i]);
		        			
		        			//jsonObject
		        			rowData['doc_id'] = rowId.doc_id;
		        			rowData['doc_name'] = exsoft.util.common.stripHtml(rowId.doc_name);
		        			rowData['is_locked'] = rowId.lock_status;
		        			rowData['root_id'] = rowId.root_id;
		        			rowData['doc_type'] = rowId.doc_type;
		        			rowData['is_inherit_acl'] = rowId.is_inherit_acl;
		        			rowData['folder_id'] = workDocList.folderId;
		        			
		        			aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(rowId.acl_level));
		        			
		        			if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("UPDATE")) {
		        				jAlert("문서 이동 권한이 없는 문서가 존재합니다.", "이동", 6);
		        				return false;
		        			}
		        			
		        			if(rowData.doc_id){
		        				jsonArr[jsonArrIndex] = rowData;
		        				jsonArrIndex ++;
		        			}
		        		}
		        	} else {
		        		// 문서하나만 Drag&Drop하여 이동시
		        		var doc_id = ui.draggable[0].id;
			        	var rowId = $("#workDocList").getRowData(doc_id);
			        	aclLevel = exsoft.util.common.getAclItemTitleEn(exsoft.util.common.stripHtml(rowId.acl_level));
			        	
			        	jsonArr = [{
		            		doc_id : rowId.doc_id
		            		, doc_name : exsoft.util.common.stripHtml(rowId.doc_name)
		            		, is_locked : rowId.lock_status
		            		, root_id : rowId.root_id
		            		, doc_type : rowId.doc_type
		            		, is_inherit_acl : rowId.is_inherit_acl
		            		, folder_id : workDocList.folderId
		            	}];
			        	
			        	if(exsoft.util.common.getAclLevel(aclLevel) < exsoft.util.common.getAclLevel("UPDATE")) {
	    					jAlert("문서 이동 권한이 없습니다.", "이동", 6);
	    					return false;
	    				}
		        	}
		        	// [2000] end
		        	
		        	if(jsonArr.length > 0) {
		    			jConfirm("문서를 이동 하시겠습니까?" , "이동", 0, function(ret) {
		    				var jsonObject = {"type":"MOVE", "targetFolderId":target_folder_id, "docList":JSON.stringify(jsonArr)};
							if(ret) {
								exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot + '/document/documentControl.do', "",
									function(data, e){
										if(data.result == 'true'){
											exsoft.util.grid.gridRefresh("myDocList", exsoft.contextRoot + '/document/workDocumentList.do');
											jAlert("문서 이동 완료되었습니다.", "이동", 8);
										} else {
											jAlert(data.message, "이동", 6);
										}
									}
								);
							}
	    				});
	    			}
		        }
		    });
		}
	},

	treeFunctions : {
		initTree : function() {
			var treeOption = {
				context : exsoft.contextRoot,
				contextAction : myDocList.treeContextAction,
				url : "/folder/folderList.do",
			};

			if (myDocList.tree.mypageTree === null) {
				treeOption.divId = "#mypageTree";
				treeOption.mapId = Constant.MAP_MYPAGE;
				treeOption.workType = Constant.WORK_MYPAGE;

				myDocList.tree.mypageTree = new XFTree(treeOption);
				myDocList.tree.mypageTree.template_context(); // 우클릭 메뉴
				myDocList.tree.mypageTree.template_dnd(); // drag&drop
				myDocList.tree.mypageTree.callbackSelectNode = myDocList.callback.selectTreeNode;
				myDocList.tree.mypageTree.callbackMoveNodeForDnD = myDocList.callback.moveNodeForDnD;
				myDocList.tree.mypageTree.init(); //부서 rootId는 서버에서 처리
			} else {
				// 해당 폴더 문서목록 새로고침
				myDocList.tree.mypageTree.refresh();
			}
		},

		refresh : function() {
			myDocList.tree.mypageTree.refresh();
		}
	},

	ui : {
		setNavigationText : function(nodeTitle, path) {
			$("#nav_title").text(nodeTitle);
			$("#nav_fullpath").text(path);
		}
	},

	event : {

		// 검색
		searchDocument : function() {

			var _post = {
				strIndex : exsoft.util.common.getDdslick("#myDoc_select"),
				strKeyword1 : exsoft.util.common.sqlInjectionReplace($("#strKeyword1").val()),
				folder_id : myDocList.folderId,
				is_search : 'true',
				work_type : Constant.WORK_MYPAGE
			};

			exsoft.util.grid.gridPostDataInitRefresh("myDocList", exsoft.contextRoot + "/document/workDocumentList.do", _post);
		},

		popDocDetail : function(docId) {
			exsoft.document.layer.docCommonFrm('doc_detail_wrapper', 'doc_detail', docId);
		},

		docWritePopup : function() {
			exsoft.document.layer.docWriteCommonFrm('doc_register_wrapper','doc_register', myDocList.folderId, myDocList.folderPath, "MYPAGE",true);
		},
		
		// 자물쇠아이콘 클릭시 문서 잠금 해제
    	DocumentUnLock : function(docID,docType,checkoutCancel) {
    		
    		if(checkoutCancel == "T") {
    			var jsonArr = [{
    				doc_id : docID
    				, root_id : null
    				, is_locked : "T"
    				, doc_type : docType
    			}];
    			
    			documentListLayerWindow.gObjectID = "myDocList";
    			documentListLayerWindow.event.documentCancelCheckoutSend(jsonArr, 'null');
    		} else {
    			jAlert('반출(잠금) 해제 권한이 없습니다','반출',6);
    		}
    	},

	},

	callback : {
		// registFolderWindow.js 에서 등록/수정 후 호출하는 callback
		refreshTree : function (e, data) {
			myDocList.treeFunctions.refresh();
		},
		moveFolder : function (parentFolder) {
			var _tree = myDocList.tree.mypageTree;

			// 1. 이동 대상 폴더가 현재 폴더와 동일한 위치인지
			if (parentFolder.original.parentId == _tree.getCurrentNodeParentId()) {
				jAlert("동일한 위치로 이동할 수 없습니다.",'이동',7);
				return;
			}

			for (var i = 0; i < parentFolder.parentIdList.length; i++) {
				if (parentFolder.parentIdList[i] == _tree.getCurrentNodeId() || parentFolder.id == _tree.getCurrentNodeParentId()) {
					jAlert("현재 폴더 및 현재 폴더 하위로 이동할 수 없습니다.",'이동',7);
					return;
				}
			}

			var targetRootFolder = parentFolder.parentGroup == null ? parentFolder.id : parentFolder.parentGroup.id;

			var jsonObject = {
					type : "MOVE",
					folder_id : _tree.getCurrentNodeId(),
					folder_name_ko : _tree.getCurrentNodeName(),
					parent_id : parentFolder.id,
					map_id : parentFolder.mapId,
					parentGroup_id : targetRootFolder,
					root_folder_change : "T"
			};

			exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot+"/folder/folderControl.do", "", function(data, param) {
				if (data.result == "true") {
					myDocList.treeFunctions.refresh();
				}
			});
		},
		selectTreeNode : function (e, data) {
			myDocList.folderId = data.node.id;
			myDocList.grid.page = 1;
			documentListLayerWindow.gCurrentFolderId = data.node.id;
			docDetailSearch.functions.changeFolderId(data.node.id);
			
			// 상세검색 postdata세팅
			docDetailSearch.folder_id = myDocList.folderId;
			docDetailSearch.url = "/document/workDocumentList.do";

			// 1. 목록 조회
			myDocList.event.searchDocument();

			// 2. Navigation 변경
			myDocList.ui.setNavigationText(data.node.text, data.node.full_path.join(" < "));
			myDocList.folderPath = data.node.full_path.join("/");
			
			// 3. 화면분할 초기화면 setting
			
			// 쿠키값에 설정된 화면 상하좌우 분활 자동으로 보이기
			exsoft.common.bind.doFunction.layoutViewCookie();
			exsoft.preview.ui.hideDocDetail();
		},
		moveNodeForDnD : function(e,data) {
			var parent_node_info = $(myDocList.tree.mypageTree.divId).jstree("get_node", data.parent);
			var selected_node_acl_level = data.node.original.acl_level;
			var parent_node_acl_level = parent_node_info.original.acl_level;

			if((exsoft.util.common.getAclLevel(selected_node_acl_level) < exsoft.util.common.getAclLevel("UPDATE")) && 
					(exsoft.util.common.getAclLevel(parent_node_acl_level) < exsoft.util.common.getAclLevel("UPDATE"))) {
				data.instance.refresh();
				jAlert("폴더 이동 권한이 없습니다.", "이동", 6);
				return false;
			}
			var jsonObject = {
					type : "MOVE",
					folder_id : data.node.id,
					folder_name_ko : data.node.text,
					parent_id : data.parent,
					map_id : data.node.original.map_id,
					parentGroup_id : data.parent,
					root_folder_change : "T"
			};
	
			exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot+"/folder/folderControl.do", "", function(data, param) {
				if (data.result == "true") {
					myDocList.treeFunctions.refresh();
				}
			});
		}
	}
}
