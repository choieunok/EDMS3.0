/**
 * [2000][신규개발]	2015-08-25	이재민 : 프로젝트부서 등록/수정/조회시 관리부서 추가
 * [2001][신규개발]	2015-08-25	이재민 : 프로젝트탭 선택시 부서명이아니라 프로젝트명으로 나오게 수정
 * [2002][로직수정]	2015-09-04	이재민 : 그룹 수정시 정렬순서가 수정되지않는 현상 수정
 * [2003][신규개발]	20160308	이재민 : 부서수정시 dept_cd가 수정가능하게 화면수정
	(정종철 차장님이 부서-사용자배치시 하나INS및 기타 개발자부서 등록시 계정계 dept_cd에 맞게 수정가능하도록 수정요청함)
 * [1004][로직수정]	2016-03-10	eunok : 그리드 전체 리스트가 표지 되지 않는 현상 수정 
 * [1005][로직수정]	2016-03-25	eunok : 모든 유저가 리스트 표시되게 수정 
 * */

var groupManager = {
		
		nPage :1,

		groupTree : null,			// 부서 JStree 오브젝트
		projectTree : null,			// 프로젝트 JStree 오브젝트
		currentTree : null,			// 현재 선택된 JStree 오브젝트

		group : null,				// 부서 정보를 담는 오브젝트
		isChanged : false,			// 현재 부서 정보가 변경됐는지 여부
		mapId : "MYDEPT",			// 현재 선택된 탭 ID

		pageSize : null,
		full_path : "",
		current_group_id : "",

		groupBinder : new DataBinder("#group_form"),

		// 0. 초기화
        init : {

        	initPage : function(map_id, page_size) {

        		exsoft.util.common.ddslick('#group_status','use_yn','group_status',79, function(){});
        		groupManager.mapId = map_id;
        		groupManager.pageSize = page_size;
        		
        		// 부서원 목록 Grid 초기화
        		groupManager.init.initGroupUserList();

        		// 트리를 초기화 한다
        		groupManager.init.initTree(groupManager.mapId);
        	},

        	tabControl : function(tab_id) {

        		if(tab_id == "tab_group") {
        			$("#upload_btn").removeClass("hide");
        			$("#upload_sample").removeClass("hide");

        			$("#tab_group").addClass("selected");
        			$("#tab_project").removeClass("selected");

        			$("#groupTree").removeClass("hide");
        			$("#projectTree").addClass("hide");

        			groupManager.mapId = "MYDEPT";
        		} else {
        			$("#upload_btn").addClass("hide");
        			$("#upload_sample").addClass("hide");

        			$("#tab_project").addClass("selected");
        			$("#tab_group").removeClass("selected");

        			$("#projectTree").removeClass("hide");
        			$("#groupTree").addClass("hide");

        			groupManager.mapId = "PROJECT";
        		}
        		
        		// [2001] map id에 따른 화면구성
        		if(groupManager.mapId == "MYDEPT") {
        			$("#g_m_name").text("부서명");
        			$("#g_m_name_en").text("부서명(영문)");
        			$("#g_m_full_path").text("부서경로");
        			$("#tr_manage_info").addClass("hide");
        			$("#targetUserList").css("top","350px"); // [2003] tr추가에 따른 높이값 변경
        		} else {
        			$("#g_m_name").text("프로젝트명");
        			$("#g_m_name_en").text("프로젝트명(영문)");
        			$("#g_m_full_path").text("프로젝트경로");
        			$("#tr_manage_info").removeClass("hide");
        			$("#targetUserList").css("top","380px"); // [2003] tr추가에 따른 높이값 변경
        		}

        		// 초기화가 안된 트리를 초기화 시킨다
        		groupManager.init.initTree(groupManager.mapId);
        	},

        	initTree : function(mapId) {
        		if (mapId == "MYDEPT") {
        			if (groupManager.groupTree == undefined) {
        				treeOption = {
            					divId : "#groupTree",
            					context : exsoft.contextRoot,
            					url : "/group/groupList.do",
            					mapId : Constant.MAP_MYDEPT
            			};
        				groupManager.groupTree = new XFTree(treeOption);
        				groupManager.groupTree.callbackSelectNode = groupManager.callback.selectNode;
        				groupManager.groupTree.init();
        			} else {
        				groupManager.groupTree.refresh();
        			}
        			groupManager.currentTree = groupManager.groupTree;
        		} else {
        			if (groupManager.projectTree == undefined) {
        				treeOption = {
            					divId : "#projectTree",
            					context : exsoft.contextRoot,
            					url : "/group/groupList.do",
            					mapId : Constant.MAP_PROJECT
            			};
        				groupManager.projectTree = new XFTree(treeOption);
        				groupManager.projectTree.callbackSelectNode = groupManager.callback.selectNode;
        				groupManager.projectTree.init();
        			} else {
        				groupManager.projectTree.refresh();
        			}
        			groupManager.currentTree = groupManager.projectTree;
        		}
        	},

        	initGroupUserList : function() {
				// Grid 초기화
        		$('#userList').jqGrid('GridUnload');//[1005]
        		
        		$("#userList").jqGrid({
        			url:exsoft.contextRoot + '/admin/groupUserList.do',
        			mtype:"post",
        			datatype:'json',
        			jsonReader:{
        				page:'page',total:'total',root:'list'
        			},
        			colNames:['사용자명','사용자 ID','직위','역할','메일','잠금','부서명'],
        			colModel:[
        				{name:'user_name_ko',index:'user_name_ko',width:30, editable:false,sortable:true,resizable:true,hidden:false,align:'center'},
        				{name:'user_id',index:'user_id',width:30, editable:false,sortable:true,resizable:true,hidden:false,align:'center'},
        				{name:'position_nm',index:'position_nm',width:30, editable:false,sortable:true,resizable:true,hidden:false,align:'center'},
        				{name:'role_nm',index:'role_nm',width:50, editable:false,sortable:false,resizable:true,hidden:false,align:'center'},
        				{name:'email',index:'email',width:30, editable:false,sortable:false,resizable:true,hidden:false,align:'center'},
        				{name:'user_status_nm',index:'user_status_nm',width:30, editable:false,sortable:true,resizable:true,hidden:false,align:'center'},
        				{name:'group_nm',index:'group_nm',width:30, editable:false,sortable:false,resizable:true,hidden:false,align:'center'}
        			],
        			autowidth:true,
        			height:"auto",
        			viewrecords: true,multiselect:true,sortable: true,shrinkToFit:true,gridview: true,
        			sortname : "user_name_ko",
        			sortorder:"asc",
        			viewsortcols:'vertical',
        			rowNum : "records",//[1005] groupManager.pageSize ->"records" 변경 (서버의 결과 수)
        			emptyDataText: "데이터가 없습니다.",
        			caption:'사용자 목록',
        			pagerpos: 'center',
        		    pginput: true,    				
        			loadError:function(xhr, status, error) {
        				exsoft.util.error.isErrorChk(xhr);
        			 }
        			,loadBeforeSend: function() {
        				exsoft.util.grid.gridTitleBarHide('userList');
        				exsoft.util.grid.gridNoDataMsgInit('userList');
        			}
        			,loadComplete: function(data) {

						if ($("#userList").getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('userList','no_data');
						}else {
							exsoft.util.grid.gridViewRecords('userList');
						}
						exsoft.util.grid.gridInputInit(false);
        				exsoft.util.grid.gridResize('userList','targetUserList',40);
        			}
        		});
        	},
        	
        
			
			
        },

        // 1. 팝업
        open : {
        },

        //2. layer + show
        layer : {
        },

        //3. 닫기 + hide
        close : {
        },

        //4. 화면 이벤트 처리
        event : {
        	// 트리 갱신
			refreshTree : function() {
				groupManager.currentTree.refresh();
			},

        	// 등록버튼클릭시
        	registGroup : function() {
        		registGroupWindow.init(groupManager.full_path, groupManager.current_group_id, groupManager.mapId);
        	},

        	// 이동버튼 클릭시
        	moveGroup : function() {
        		if(groupManager.mapId == "MYDEPT") {
        			selectGroupWindow.init.initPage(groupManager.callback.moveGroupCallback, "MYDEPT");
        		} else {
        			selectGroupWindow.init.initPage(groupManager.callback.moveGroupCallback, "PROJECT");
        		}
        	},

        	// 삭제버튼 클릭시
        	deleteGroup : function() {
        		jConfirm('정말 삭제하시겠습니까?', "그룹관리", 2,
    				function(ret){
    					if(ret) {
    						exsoft.util.ajax.ajaxDataFunctionWithCallback({type:"delete", group_id : groupManager.group.group_id}, exsoft.contextRoot + "/admin/groupInfoManager.do", "groupDelete", function(data, param) {

    							if (data.result == "success") {
    								var parent = groupManager.currentTree.getCurrentNodeParentId();

    								groupManager.currentTree.refreshNodeForAddChildren();

    								// 삭제할 노드의 부모가 가진 ChildrenCnt를 1감소 (자식이 1개일 경우 화면 처리를 위함)
    								groupManager.currentTree.removeChildCnt(parent);

    								// 삭제할 노드의 부모 노드아이콘 상태를 업데이트 한다
    								groupManager.currentTree.updateNodeIcon(parent);

    								// 삭제할 노드의 부모 노드를 선택한다
    								groupManager.currentTree.selectNode(parent);

    								jAlert("그룹을 삭제했습니다.", "그룹관리", 8);
    							} else {
    								jAlert(data.message, "그룹관리", 7);
    							}

    						});
    					}
    				}
    			);
        	},

        	// 저장버튼 클릭시
        	updateGroup : function() {
        		// 변경된 부분이 있는지 확인
        		if (groupManager.event.isChangedGroupInfo()) {

        			// setJsonArray -> 서버소스 변경필요
        			groupManager.groupBinder.set("type","update");
        			groupManager.groupBinder.set("is_changed_parent","FALSE");		// 부서이동의 경우에 추가 체크 옵션
        			groupManager.groupBinder.set("user_id_list",exsoft.util.grid.gridSelectDataAllRow("userList", "user_id"));
        			groupManager.groupBinder.set("group_status",exsoft.util.layout.getSelectBox('group_status','option'));
        			groupManager.groupBinder.set("sort_index", $("#sort_index").val()); // [2002] 입력한 sort_inex값을 바인더에 세팅

        			var jsonObject = groupManager.groupBinder.getDataToJson();
        			var jsonData ="";//[1004]
        			
        			exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject ,exsoft.contextRoot + '/admin/groupInfoManager.do',groupManager.currentTree.getCurrentNode(),function(data,node){
        				if (data.result == "success") {
        					// 수정된 노드의 Text를 트리에 업데이트한다
        					groupManager.currentTree.setNodeText(node.id, groupManager.groupBinder.get("group_name_ko"));

        					// 화면을 갱신한다
        					groupManager.currentTree.selectNodeForInvoke(node.id);
        					jsonData = { groupId : node.id , ispage:"T"};//[1004]
        					
        					jAlert("변경된 내용이 저장됐습니다.", "그룹관리", 8);
        				} else {
        					jAlert(data.message, "그룹관리", 7);
        				}
            		});
        		} else {
        			jAlert("변경된 내용이 없습니다.", "그룹관리", 6);
        		}
        		

        		
        	},

        	// 변경된 내용이 있는지 검증
        	isChangedGroupInfo : function() {
        		var valid = false;
        		
        		// Form validation
        		if(groupManager.group.group_name_ko != $("#group_name_ko").val()) {
        			valid = true; // 그룹명 변경(한글)
    			} else if(groupManager.group.group_name_en != $("#group_name_en").val()) {
    				valid = true; // 그룹명 변경(영어)
				} else if(groupManager.group.sort_index != $("#sort_index").val()) {
					valid = true; // 정렬순서 변경
				} else if(groupManager.group.group_status != $("#group_status").val()) {
					valid = true; // 그룹 상태 변경
				} else if(groupManager.isChanged)	{
					valid = true; // 사용자 추가/삭제
				}

        	 	return valid;
        	},

        	// 추가버튼 클릭시
        	addUser : function() {
        		selectMultiUserWindow.init(groupManager.pageSize, groupManager.callback.selectGroupUserCallback);
        	},

        	// 제거버튼 클릭시
        	removeUser : function() {
        		if (!exsoft.util.grid.gridSelectCheck("userList")) {
        			jAlert("제외할 사용자를 선택해주세요.", "그룹관리", 6);
        			return;
        		}

        		// 그룹이 수정됨을 체크
        		isChanged = true;

        		exsoft.util.grid.gridDeleteRow("userList", null, null, true);

        	},
        	
        	// 일괄업로드 버튼 클릭시
        	groupExcelUpload : function() {
        		excelUploadWindow.current_id = groupManager.group.group_id;
        		excelUploadWindow.event.groupUploadView();
        	},
        	
        	// [2000] 관리부서 사용여부 체크박스 클릭 (체크시 미사용)
			checkManageNoUse : function() {
				if($("#m_is_manage_use").is(":checked") ) {
					$("#manage_group").prop("disabled", true);
					$("#manage_group").addClass("disabled");
					$("#btn_select_group").prop("disabled", true);
					$("#btn_select_group").addClass("disabled");
					$("#manage_group").val("");
					groupManager.groupBinder.set("manage_group_id","");
				} else {
					$("#manage_group").prop("disabled", false);
					$("#manage_group").removeClass("disabled");
					$("#btn_select_group").prop("disabled", false);
					$("#btn_select_group").removeClass("disabled");
					$("#manage_group").val("");
				}
			},
			
			// [2000] 관리부서 선택
			selectManageGroup : function() {
				selectGroupWindow.init.initPage(groupManager.callback.callbackForManageGroup, "MYDEPT");
			},
		
			
        },

        //5. 화면 UI 변경 처리
        ui : {
        },

        //6. callback 처리
        callback : {
        	selectNode : function(e, data) {

        		var jsonData = { groupId : data.node.id , ispage:"T"};

        		// 부서 기본정보 조회
        		exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonData, exsoft.contextRoot + "/admin/groupDetail.do", "groupDetail", function(groupInfo, param) {
        			// 서버의 처리가 성공일 경우에만 로직 활용.
        			if (groupInfo.result == "success") {

        				groupManager.group = groupInfo.groupDetail;
        				groupManager.full_path = data.node.full_path.join(" > ");
        				groupManager.current_group_id = groupManager.group.group_id;

        				// 삭제버튼 좌측에 부서명출력
        				$("#group_name_title").text(groupManager.group.group_name_ko);
        				
        				// [2000] 조회시 관리부서관련 세팅
        				if(groupManager.group.manage_group_name != null && 
        						groupManager.group.manage_group_name != "") {
        					$("#m_is_manage_use").prop("checked", false);
        					$("#manage_group").val(groupManager.group.manage_group_name);
        					$("#btn_select_group").prop("disabled", false);
        					$("#btn_select_group").removeClass("disabled");
        				} else {
        					// 관리부서 정보가 없을경우 미사용에 체크하여 diable상태로 둠
        					$("#m_is_manage_use").prop("checked", true);
        					$("#manage_group").val("");
        					$("#btn_select_group").prop("disabled", true);
        					$("#btn_select_group").addClass("disabled");
        				}

        				groupManager.groupBinder.set("group_id",groupManager.group.group_id);
        				groupManager.groupBinder.set("group_name_ko",groupManager.group.group_name_ko);
        				groupManager.groupBinder.set("group_name_en",groupManager.group.group_name_en);
        				groupManager.groupBinder.set("sort_index",groupManager.group.sort_index);
        				groupManager.groupBinder.set("create_date",groupManager.group.create_date);
        				groupManager.groupBinder.set("group_full_path",data.node.full_path.join(" > "));
        				groupManager.groupBinder.set("parent_id",groupManager.group.parent_id);
        				groupManager.groupBinder.set("map_id",groupManager.group.map_id);
        				groupManager.groupBinder.set("group_status",groupManager.group.group_status);
        				groupManager.groupBinder.set("dept_cd",groupManager.group.dept_cd); // [2003]

        			} else {
        				jAlert(groupInfo.message, "그룹관리", 7);
        			}
        		});

        		// 부서원 목록 조회
        		exsoft.util.grid.gridPostDataRefresh('#userList', exsoft.contextRoot + '/admin/groupUserList.do', jsonData);
        	},

        	// 이동 팝업 화면 콜백
        	moveGroupCallback : function(returnGroup) {
        		var isLoop;
        		var params = new Object();

        		try {
        			if (returnGroup.length > 0) {
        				isLoop = false;

        				// 이동하려는 대상 폴더가 자신의 하위 폴더인지 체크함
        				$(returnGroup[0].parentIdList).each(function(index) {
        					if (this == groupManager.group.group_id) {
        						jAlert("자신의 하위 부서로 이동할 수 없습니다.", "그룹관리", 6);
        						isLoop = true;
        					}
        				})

        				// 자신의 하위 부서로 이동이 아닐경우엔 이동 처리를 한다
        				if (!isLoop) {

        					params.type = "move";
        					params.group_id = groupManager.group.group_id;
        					params.parent_id = returnGroup[0].id;
        					params.map_id = returnGroup[0].mapId;

        					exsoft.util.ajax.ajaxDataFunctionWithCallback(params, exsoft.contextRoot + "/admin/groupInfoManager.do", "groupMove", function(data, param) {
        						// 폴더 갱신
        						groupManager.currentTree.refresh();
        					});
        				}
        			}
        		} finally {
        			isLoop = null;
        			params = null;
        		}
        	},

        	// 구성원추가 콜백
        	selectGroupUserCallback : function(userRowList) {

        		exsoft.util.grid.gridNoDataMsgInit('userList');

        		// 선택된 사용자 목록을 추가함
        		$(userRowList).each(function() {
        			// 중복데이터 필터링
        			var userIdList = exsoft.util.grid.gridSelectArrayDataAllRow("userList", "user_id", "user_id");
        			var isDuplicate = false;
        			var row = this;

        			$(userIdList).each(function(i) {
        				if (this.user_id == row.user_id)
        					isDuplicate = true;
        			})

        			if (!isDuplicate) {
        				// 그리드에 사용자 추가
        				$("#userList").jqGrid("addRowData", this.user_id, this);

        				// 그룹 정보가 변경됨을 체크
        				isChanged = true;
        			}
        		});
        	},
        	
        	callbackForManageGroup : function(returnGroup) {
    			// [2000] 관리부서선택 > 선택버튼 > 그룹선택팝업 callback
        		groupManager.groupBinder.set("manage_group_id",returnGroup[0].original.id);
    			$("#manage_group").val(returnGroup[0].fullPath.join(" > "))
    		}

        },
}