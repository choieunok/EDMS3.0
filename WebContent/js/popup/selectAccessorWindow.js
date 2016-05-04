/**
 * 문서추가 접근자 선택화면 : 소스 변경처리(2015.05.17)
 * 
 * [2000][화면수정]	2015-09-11	이재민	 : 추가접근자선택 팝업 - 전체접근자 체크박스 추가
 * 
 * [1004] 20160311 eunok grid 전체 목록이 나오지 않는 현상 수정
 */
var selectAccessorWindow = {

		callback : null,
		accessorCheckedIds : null,			// Tree에서 선택된 접근자 Id목록
		accessorParentList : null,			// 현재 grid상의 접근자 목록
		isInitialized : false,
		currentType : "",
		accessorList : [],						// 추가권한 목록 리스트

		tree : {

			groupTree : null,					// 그룹 트리 오브젝트
			projectTree : null,					// 프로젝트 트리 오브젝트

			initTree : function (type) {

				var _treeName = type == "GROUP" ? "groupTree" : "projectTree";
				var _treeOpt = {
						divId : "accessorWindow_" + _treeName,
						context : exsoft.contextRoot,
						url : "/group/groupList.do",
						mapId : type != "GROUP" ? Constant.MAP_PROJECT : Constant.MAP_MYDEPT,
						workType : type != "GROUP" ? Constant.WORK_PROJECT : Constant.WORK_ALLDEPT,
						onlyCheckbox : true,

				};


				if (type == "GROUP") {

					if (selectAccessorWindow.tree.groupTree == null) {
						selectAccessorWindow.tree.groupTree = new XFTree(_treeOpt);
						selectAccessorWindow.tree.groupTree.template_multiCheck(false);
						selectAccessorWindow.tree.groupTree.callbackSelectNode = selectAccessorWindow.callbackFunctions.selectTreeNode;
						selectAccessorWindow.tree.groupTree.callbackAllSelectNodeData = selectAccessorWindow.callbackFunctions.checkedChangeTreeNode;
						selectAccessorWindow.tree.groupTree.init();
					} else {
						selectAccessorWindow.tree.groupTree.refresh();
					}
				} else {
					if (selectAccessorWindow.tree.projectTree == null) {
						selectAccessorWindow.tree.projectTree = new XFTree(_treeOpt);
						selectAccessorWindow.tree.projectTree.template_multiCheck(false);
						selectAccessorWindow.tree.projectTree.callbackSelectNode = selectAccessorWindow.callbackFunctions.selectTreeNode;
						selectAccessorWindow.tree.projectTree.callbackAllSelectNodeData = selectAccessorWindow.callbackFunctions.checkedChangeTreeNode;
						selectAccessorWindow.tree.projectTree.init();
					} else {
						selectAccessorWindow.tree.projectTree.refresh();
					}
				}

			}
		},

		// 초기화 함수
		init : function(callback, parentGridId, type, callType) {
			
			selectAccessorWindow.callback = callback;
			selectAccessorWindow.functions.getAccessorParentList(parentGridId);
			selectAccessorWindow.initDataList(type);

			// 폴더권한 HIDE 처리
			if (type == Constant.ACL.TYPE_DOC) {
				$("#selectAccessorWindowFolderTr").hide();
			} else {
				$("#selectAccessorWindowFolderTr").show();
			}
			
			// [2000] 체크박스 초기화
			$("#checkAccessorAll").prop("checked", false);

			selectAccessorWindow.open(callType);
		},

		// 그리드 목록 및 선택 사용자 목록 초기화 처리 기능 추가
		initDataList : function(type) {

			selectAccessorWindow.grid.initTreeGroupUser();
			selectAccessorWindow.grid.initSearchGroupUser();

			exsoft.util.common.ddslick("#selectAccessorWindowMapList", "selectAccessorWindowMapList", "", 100, function(divId,selectedData){
				// 콤보박스 이벤트
				selectAccessorWindow.event.changeMap(selectedData.selectedData.value);
				selectAccessorWindow.ui.treeVisible(selectedData.selectedData.value);
				selectAccessorWindow.currentType = selectedData.selectedData.value;
			});

			exsoft.util.common.ddslick("#selectAccessorWindowDefaultFolderAcl", "selectAccessorWindowDefaultFolderAcl", "", 80,
					function(divId,selectedData){});

			exsoft.util.common.ddslick("#selectAccessorWindowDefaultDocAcl", "selectAccessorWindowDefaultDocAcl", "", 80,
					function(divId,selectedData){});

			// 추가 접근자 목록 초기화
			selectAccessorWindow.accessorList  = new Array();

			$("#accesorWindowSelectedList").empty();
			$("#searchAccessorKeyword").val("");

			// 폴더권한선택 초기화 :: SELECT BOX 초기화 보류
			if (type == Constant.ACL.FOLDER) {
				$("#accessorWindowFolActCreate").prop("checked",false);
				$("#accessorWindowFolActChangePermission").prop("checked",false);
			}

			// 문서권한선택 초기화(폴더/문서 공통)
			$("#accessorWindowDocActCreate").prop("checked",false);
			$("#accessorWindowDocActCancelCheckout").prop("checked",false);
			$("#accessorWindowDocActChangePermission").prop("checked",false);

		},

		open : function(callType) {
			exsoft.util.layout.divLayerOpen("grpDeptUser_add_wrapper", "grpDeptUser_add", true, callType);
		},

		close : function() {
			exsoft.util.layout.divLayerClose("grpDeptUser_add_wrapper", "grpDeptUser_add");
		},

		grid : {

			// Tree 선택 사용자 리스트
			initTreeGroupUser : function(postData) {
				if ($("#initTreeGroupUser")[0].grid == undefined) {
					$("#initTreeGroupUser").jqGrid({
						url: exsoft.contextRoot + "/user/groupUserList.do",
						mtype:"post",
						datatype:"json",
						postData : postData,
						list : "",
						jsonReader:{
							root:"list"
						},
						colNames:["user_id","사용자명"],
						colModel:[
							{name:"user_id",index:"user_id",width:10, editable:false,sortable:true,resizable:true,hidden:true},
							{name:"user_nm",index:"user_nm",width:160, editable:false,sortable:true,resizable:true,hidden:false,align:"left"}
						],
						autowidth:true,height:"auto",
						viewrecords: true,multiselect:true,sortable: true,shrinkToFit:true,gridview: true,
						sortname : "user_nm",
						sortorder:"desc",
						scrollOffset : 0,
						viewsortcols:'vertical',
						//rowNum : 15,//[1004]코멘트 처리
						height : 200,//[1004]추가
						emptyDataText: "데이터가 없습니다.",
						caption:"사용자 목록",
						pagerpos: "center",
					    pginput: true,
						loadError:function(xhr, status, error) {
							exsoft.util.error.isErrorChk(xhr);
						 }
						// [4001] START
						,beforeProcessing : function(data) {
					    	if (data.records > 0) {
					    		$("#initTreeGroupUser").setGridParam({rowNum:data.records});
					    	}
		                 }
						// [4001] END
						,loadBeforeSend: function() {
							exsoft.util.grid.gridNoDataMsgInit("initTreeGroupUser");
							exsoft.util.grid.gridTitleBarHide("initTreeGroupUser");
						}
						,loadComplete: function() {

							exsoft.util.grid.gridInputInit(false); 

							if ($("#initTreeGroupUser").getGridParam("records")==0) {
								exsoft.util.grid.gridNoDataMsg("initTreeGroupUser","nolayer_data");
							}
						}
					});
				} else {

					exsoft.util.grid.gridPostDataRefresh("initTreeGroupUser", exsoft.contextRoot + "/user/groupUserList.do", postData);
				}
			},

			// 검색 사용자 리스트
			initSearchGroupUser : function(postData) {
				if ($("#initSearchGroupUser")[0].grid == undefined) {
					$("#initSearchGroupUser").jqGrid({
						url:exsoft.contextRoot + "/user/searchGroupUser.do",
						mtype:"post",
						postData : postData,
						datatype:"json",
						list : "",
						jsonReader:{
							root:"list"
						},
						colNames:["is_group","is_group_nm","성명/부서명","사용자/부서ID"],
						colModel:[
							{name:"is_group",index:"is_group",width:10, editable:false,sortable:true,resizable:true,hidden:true},
							{name:"is_group_nm",index:"is_group_nm",width:10, editable:false,sortable:true,resizable:true,hidden:true,align:"center"},
							{name:"unique_nm",index:"unique_nm",width:30, editable:false,sortable:false,resizable:true,hidden:false,align:"center"},
							{name:"unique_id",index:"unique_id",width:30, editable:false,sortable:true,resizable:true,hidden:false,align:"center"}
						],
						autowidth:true,
						height:"auto",
						viewrecords: true,multiselect:true,sortable: true,shrinkToFit:true,gridview: true,
						sortname : "unique_nm",
						sortorder:"desc",
	    				scroll:false, // virtual Scrolling
						//rowNum : 15,
						//rowList : exsoft.util.grid.listArraySize(),
						emptyDataText: "데이터가 없습니다.",
						caption:"확장자 목록",
						pagerpos: "center",
					    pginput: true,
						loadError:function(xhr, status, error) {
							exsoft.util.error.isErrorChk(xhr);
						 }
						// [4001] START
						,beforeProcessing : function(data) {
					    	if (data.records > 0) {
					    		$("#initSearchGroupUser").setGridParam({rowNum:data.records});
					    	}
		                 }
						// [4001] END
						,loadBeforeSend: function() {
							exsoft.util.grid.gridNoDataMsgInit("initSearchGroupUser");
							exsoft.util.grid.gridTitleBarHide("initSearchGroupUser");
						}
						,loadComplete: function() {
							
							exsoft.util.grid.gridInputInit(false); // 페이지 창 숫자만  test
							exsoft.util.grid.gridResize('initSearchGroupUser','searchUserListTarget',20,0);
							
							if ($("#initSearchGroupUser").getGridParam("records")==0) {
								exsoft.util.grid.gridNoDataMsg("initSearchGroupUser","nolayer_data");
							}
						}
					});
				} else {
					exsoft.util.grid.gridPostDataRefresh("initSearchGroupUser",exsoft.contextRoot + "/user/groupUserList.do", postData);
				}
			}
		},

		event : {

			// Tree 선택 변경
			changeMap : function(type) {
				selectAccessorWindow.tree.initTree(type);
			},

			// 사용자|부서 검색처리
			searchAccessor : function() {

				var postData = {strIndex : "ALL",strKeyword : exsoft.util.common.sqlInjectionReplace($("#searchAccessorKeyword").val())};

				if (postData.strKeyword.length == 0) {
					jAlert("검색어를 입력하세요.","접근자관리",6);
					return false;
				}

				exsoft.util.grid.gridPostDataRefresh("initSearchGroupUser", exsoft.contextRoot + "/user/searchGroupUser.do", postData);
			},

			// 추가 접근자 추가 버튼 처리
			addAccessor : function() {

				/**
				 * 좌측 선택 그리드
				 * 1. initSearchGroupUser
				 * 2. initTreeGroupUser
				 *
				 * 선택된 트리 아이템
				 * 1. selectAccessorWindow.accessorCheckedIds
				 */

				// accesorWindowSelectedList 선택자를 뿌릴 곳
				var _select = {
						treeGroup : selectAccessorWindow.functions.getTreeIds().join(","),			// 선택된 부서 트리 ID 목록
						treeGroupUser : $("#initTreeGroupUser").getGridParam("selarrrow"),		// 선택된 부서 사용자 ID 목록
						searchGroupUser : $("#initSearchGroupUser").getGridParam("selarrrow"),	// 선택된 검색 결과 ID 목록
						checkAllUser : $("#checkAccessorAll").is(":checked") ? "T" : "F",		// 전사 체크 여부
				}

				if (_select.treeGroup.length == 0 &&_select.treeGroupUser.length == 0 &&_select.searchGroupUser.length == 0 && _select.checkAllUser == "F") {
					jAlert("권한을 적용할 사용자나 부서를 선택하세요.","접근자관리",6);
					return false;
				}

				// 1-1. 전사 체크
				if (_select.checkAllUser == "T") {
					selectAccessorWindow.accessorList.push({type : "ALL", id : "WORLD", name : "전체"});
				}

				// 1-2. 부서 트리
				$(_select.treeGroup.split(",")).each(function() {
					var _tem = this.split("#");
					if (_tem.length != 2) return true;
					selectAccessorWindow.accessorList.push({type : "GROUP", id : _tem[0], name : _tem[1]});
				});

				// 1-3. 사용자 목록
				$(_select.treeGroupUser).each(function() {
					var _row = $("#initTreeGroupUser").getRowData(this);
					selectAccessorWindow.accessorList.push({type : "USER", id : _row.user_id, name : _row.user_nm});
				});

				// 1-4. 검색 목록
				$(_select.searchGroupUser).each(function() {
					var _row = $("#initSearchGroupUser").getRowData(this);
					selectAccessorWindow.accessorList.push({type : _row.is_group, id : _row.unique_id, name : _row.unique_nm});
				});

				// 2. 아이템 추가
				$(selectAccessorWindow.accessorList).each(function() {
					// 2-1. 이미 있는지 확인
					if (!selectAccessorWindow.functions.checkDuplicate(this.id)) {
						// 2-2. 추가
						selectAccessorWindow.ui.addSelectedAccessor(this);
					}
				});
				
				// 추가후 배열 초기화
				selectAccessorWindow.accessorList = [];
			},

			submit : function() {

				var userList = [];

				$("#accesorWindowSelectedList span").each(function() {
					var _row = {
						accessor_id : $(this).data("id"),
						accessor_isgroup : $(this).data("type") == "GROUP" ? "T" : "F",
						accessor_isalias : $(this).data("type") == "ALL" ? "T" : "F",
						accessor_name : $(this).data("name"),
						fol_default_acl : exsoft.util.common.getDdslick("#selectAccessorWindowDefaultFolderAcl"),
						fol_act_create : $("#accessorWindowFolActCreate").is(":checked") ? "T" : "F",
						fol_act_change_permission : $("#accessorWindowFolActChangePermission").is(":checked") ? "T" : "F",
						doc_default_acl : exsoft.util.common.getDdslick("#selectAccessorWindowDefaultDocAcl"),
						doc_act_create : $("#accessorWindowDocActCreate").is(":checked") ? "T" : "F",
						doc_act_cancel_checkout : $("#accessorWindowDocActCancelCheckout").is(":checked") ? "T" : "F",
						doc_act_change_permission : $("#accessorWindowDocActChangePermission").is(":checked") ? "T" : "F"
					};
					userList.push(_row);
				});

				if(userList.length == 0) {
					jAlert("권한을 적용할 사용자나 부서를 추가하세요.","접근자관리",6);
					return false;
				}

				selectAccessorWindow.callback(userList);
				selectAccessorWindow.close();
			},

			// 엔터키입력시
			enterKeyPress : function(e) {
				if (e.keyCode == 13) {
					selectAccessorWindow.event.searchAccessor();
					return false;
				}
			}

		},

		ajax : {

		},

		functions : {
			/*
			 * 부모폼에 이미 있는 "추가 접근자"목록을 리스트화 해서 저장 함
			 * - 사용자/부서 추가 시 중복 데이터를 걸러내기 위함임.
			 */
			getAccessorParentList : function(parentGridId) {
				var arrayId = [];
				var rowIDs = $("#"+parentGridId).jqGrid("getDataIDs");
				var _cnt = rowIDs.length;

				for (var i = 0; i < _cnt; i++) {
					var row =$("#"+parentGridId).getRowData(rowIDs[i]);
					arrayId[i] = row.accessor_id;
				}
				// 현재 grid상의 접근자 목록
				selectAccessorWindow.accessorParentList = arrayId.join(",");
			},

			checkDuplicate : function(itemId) {
				var _isDuplicate = false;

				// 1. 이미 선택된 값을 체크해본다
				_isDuplicate = $("#accesorWindowSelectedList span").filter(function() {return $(this).data("id") == itemId ? true : false}).length > 0 ? true : false;

				// 2. 부모창에서 가지고있는 값을 비교해 본다.
				if (_isDuplicate == false)
					_isDuplicate = $.inArray(itemId, selectAccessorWindow.accessorParentList) > -1 ? true : false;

				return _isDuplicate;
			},

			getTreeIds : function() {
				if (selectAccessorWindow.currentType == "GROUP") {
					return selectAccessorWindow.tree.groupTree.getCheckedNodesWithText();
				} else {
					return selectAccessorWindow.tree.projectTree.getCheckedNodesWithText();
				}
			}
		},
		ui : {
			resetSearchKeyword : function() {
				$("#searchAccessorKeyword").val("");
			},

			addSelectedAccessor : function(obj) {
				var _str = "<li>";
					_str += "<span class='chosen_user' data-id='{0}' data-name='{1}' data-type='{2}'> {1}</span>".format(obj.id, obj.name, obj.type);
					_str += "<a href='#' class='remove' onclick='selectAccessorWindow.ui.removeSelectedAccessor(\"{0}\")'><img src='{1}/img/icon/window_close3.png'></a>".format(obj.id, exsoft.contextRoot);
					_str += "</li>";

				$("#accesorWindowSelectedList").append(_str);
			},

			removeSelectedAccessor : function(itemId) {
				$("#accesorWindowSelectedList span").filter(function() {
					return ($(this).data("id") == itemId) ? true : false;
				}).parent().remove();
			},

			treeVisible : function(type) {
				if (type == "GROUP") {
					$("#accessorWindow_groupTree").removeClass("hide");
					$("#accessorWindow_projectTree").addClass("hide");
				} else {
					$("#accessorWindow_groupTree").addClass("hide");
					$("#accessorWindow_projectTree").removeClass("hide");
				}

			}
		},

		callbackFunctions : {
			selectTreeNode : function(e, data) {
				selectAccessorWindow.grid.initTreeGroupUser({groupId:data.node.id});
			},

			checkedChangeTreeNode : function(selectedNodedata) {
				selectAccessorWindow.accessorCheckedIds = selectedNodedata;
			}
		}
}