/**
 * 권한변경(문서/폴더) : 소스 변경처리(2015.05.17)
 */
var selectAclWindow = {

		type : null,					// 권한 선택 팝업 타입 "FOLDER", "DOC" 두개의 타입으로 Show/Hide할 Div를 결정함
		aclType : "public",		// 공개권한(public) / 개인권한(private) 탭 분리
		srcAclId : null,				// 원본 Acl ID
		callback : null,				// 반환함수
		selAclInfo : {
			aclId : null,				// 선택 Acl ID
			aclItems : null,			// 선택 Acl Item 목록
			aclDetail : null,		// 상세 Acl 정보
			extAclItems : null,	// 추가 접근자 Acl Item 목록
		},
		extParams : {
			    "successfunc" : null,
			    "url" : 'clientArray',
			    "extraparam" : {},
			    "aftersavefunc" : function( response ) {},
			    "errorfunc": null,
			    "afterrestorefunc" : null,
			    "restoreAfterError" : true,
			    "mtype" : "POST"
		},


		// 0. 초기화
        init : function(aclId, type, callback, callType){

        	// 전역변수 설정
        	selectAclWindow.type = type;
        	selectAclWindow.callback = callback;
        	selectAclWindow.srcAclId = aclId;
        	selectAclWindow.selAclInfo.aclId = aclId;

        	// Layer open
        	selectAclWindow.open(callType);
        	selectAclWindow.tabSelectInit();

        	if($(selectAclWindow.functions.getTargetAclTableId(true))[0].grid != undefined)	{
        		exsoft.util.grid.gridPostDataRefresh(selectAclWindow.functions.getTargetAclTableId(true), exsoft.contextRoot + '/permission/aclList.do', {type:selectAclWindow.aclType});
        	}else {
        		selectAclWindow.grid.aclList();															// 권한목록 GRID
        	}
        	selectAclWindow.ajax.getAclDetail(aclId);												// 상세 Acl 정보 & 선택 Acl Item 목록
        	selectAclWindow.ui.setEnableRadio(aclId == "" ? true : false);					// ACL_ID가 없는 경우 Radio 선택 비활성화 처리

        	// 현재권한 및 상위권한 선택 :: initinhreit 에서 처리함
        },

        // 권한탭 초기화
        tabSelectInit : function() {
        	$("#public").removeClass("selected");
        	$("#private").removeClass("selected");
        	$("#public").addClass("selected");
        	selectAclWindow.aclType = "public";
        },

        // 권한변경 초기화 함수(TYPE에 따른 분기처리)
        initDocument : function(aclId, type, exAclItems, callback, callType) {
        	selectAclWindow.init(aclId, type, callback, callType);
        	// 김선재 대리 미구현 부분 :: 2014.05.17 추가 구현처리
        	//if(selectAclWindow.type == Constant.ACL.TYPE_DOC )	{
        	if(type == Constant.ACL.TYPE_DOC )	{
        		$('#selAclWindowDocExtAclItemList').jqGrid('GridUnload');
        		selectAclWindow.grid.extAclList(exAclItems);
        	}
        },

        // 현재권한 및 상속권한 김선재 대리 미구현 부분 수정적용 :: 2015.05.17
        initInherit : function(obj) {

        	var jsonObject = {
        			type : obj.type,
        			folder_id : obj.folder_id,
        			parent_folder_id : obj.parent_folder_id
        	};

        	// 기본권한사용안함처리 기능 김선재 대리 미구현 부분 : 문서권한변경 요청시
        	exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot+"/permission/aclInheritDetail.do", obj, function(data, param){

        		// 현재권한 및 상속권한 표시처리
    			$(selectAclWindow.functions.getTargetFormId() + " #pCurAclName").text(param.current_acl_name);
    			$(selectAclWindow.functions.getTargetFormId() + " #pInheritAclName").text(data.aclDetail.acl_name);
    			$(selectAclWindow.functions.getTargetFormId() + " #pCurAclId").val(param.current_acl_id);
    			$(selectAclWindow.functions.getTargetFormId() + " #pInheritAclId").val(data.aclDetail.acl_id);

        		if(param.type == "document" && param.current_acl_id == "default") {
        			$(selectAclWindow.functions.getTargetFormId()  + " #checkDefaultAcl").prop("checked",true)
        			selectAclWindow.ui.setUncheckAclScope(false);
        		}else {
        			$(selectAclWindow.functions.getTargetFormId() + " #pCurAclId").prop("checked",true)
        		}

        	})
        },


        // 1. TYPE에 따른 Layer open 처리
        open : function(callType) {
        	if (selectAclWindow.functions.isDocType())
        		exsoft.util.layout.divLayerOpen("folder_authModify_wrapper", "doc_folder_authModify", true, callType);
        	else if (selectAclWindow.functions.isFolderType())
        		exsoft.util.layout.divLayerOpen("subFolder_authModify_wrapper", "subFolder_authModify", true);
        },

        // 2. TYPE에 따른 Layer open 닫기
        close : function() {
        	if (selectAclWindow.functions.isDocType())
        		exsoft.util.layout.divLayerClose("folder_authModify_wrapper", "doc_folder_authModify");
        	else if (selectAclWindow.functions.isFolderType())
        		exsoft.util.layout.divLayerClose("subFolder_authModify_wrapper", "subFolder_authModify");
        },

        // Ajax
        ajax : {

        	// 선택권한 상세정보 및 Items
        	getAclDetail : function(aclId) {

        		if(aclId == "")	return;

				exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : aclId}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data, param) {

					selectAclWindow.selAclInfo.aclItems = data.list;					// 선택 Acl Item 목록
					selectAclWindow.selAclInfo.aclDetail = data.aclDetail;			// 상세 Acl 정보

					if (selectAclWindow.functions.isDocType()) {
						exsoft.util.table.tableDocumentAclItemPrintList(selectAclWindow.functions.getTargetAclDetailTableId(true), data.list);
					} else if (selectAclWindow.functions.isFolderType()){
						exsoft.util.table.tableFolderAclItemPrintList(selectAclWindow.functions.getTargetAclDetailTableId(true), data.list);
					}
				});
			},

        },

        // 함수 목록
        functions : {

        	// 문서유형타입인지 체크
        	isDocType : function() {
        		if (selectAclWindow.type === Constant.ACL.TYPE_DOC) {
        			return true;
        		}
        		return false;
        	},
        	// 폴더권한타입인지 체크
        	isFolderType : function() {
        		if (selectAclWindow.type === Constant.ACL.TYPE_FOLDER) {
        			return true;
        		}
        		return false;
        	},

        	// 권한목록 GridId
        	getTargetAclTableId : function(isSelector) {
        		var prefix = isSelector ? "#" : "";
        		if (selectAclWindow.functions.isDocType()) {
        			return prefix + "selAclWindowDocAclList";
        		} else if (selectAclWindow.functions.isFolderType()) {
        			return prefix + "selAclWindowFolderAclList"
        		}
        	},

        	// 권한속성ITEM GridId
        	getTargetAclDetailTableId : function(isSelector) {
        		var prefix = isSelector ? "#" : "";
        		if (selectAclWindow.functions.isDocType()) {
        			return prefix + "selAclWindowDocAclItemList";
        		} else if (selectAclWindow.functions.isFolderType()) {
        			return prefix + "selAclWindowFolderAclItemList"
        		}
        	},

        	// Layer TargetId
        	getTargetFormId : function() {
        		if (selectAclWindow.functions.isDocType()) {
        			return "#pRegDoc";
        		} else if (selectAclWindow.functions.isFolderType()) {
        			return "#pRegFolder";
        		}
        	}
        },

        grid : {

        	// 권한목록 갱신처리
        	aclListRefresh : function() {
        		exsoft.util.grid.gridRefresh(selectAclWindow.functions.getTargetAclTableId(), exsoft.contextRoot + '/permission/aclList.do');
        	},

        	// 사용 가능한 ACL 목록 :: 갱신처리 기능 추가
        	aclList : function() {
        		$(selectAclWindow.functions.getTargetAclTableId(true)).jqGrid({
        			url: exsoft.contextRoot+'/permission/aclList.do',
        			mtype:"post",
        			datatype:'json',
        			jsonReader:{
        				root:'list'
        			},
        			postData:{
        				acl_id : selectAclWindow.selAclInfo.aclId,		// ACL_ID
        				type : selectAclWindow.aclType					// 공개 or 개인
        			},
        			colNames:['','권한명','공개대상','공개범위','정렬','공개범위ID','공개대상ID','그룹여부','등록자'],
        			colModel:[
        				{name:'acl_id',index:'acl_id',width:5, align:'center',editable:false,sortable:false,key:true,edittype:'radio',
        					   formatter:function(cellValue, option) {
        						   return '<input type="radio" name="radio_selectAcl" value="'+cellValue+'" onclick="selectAclWindow.event.selectAclList(\'' + cellValue + '\')"/>';
        					   },hidden:false
        				},
        				{name:'acl_name',index:'acl_name',width:80, editable:false,sortable:true,resizable:true},
        				{name:'open_name',index:'open_name',width:50, editable:false,sortable:true,resizable:true,align:'center'},
        				{name:'acl_type_name',index:'acl_type_name',width:30, editable:false,sortable:false,resizable:true,align:'center',hidden:true},
        				{name:'sort_index',index:'sort_index',width:3, editable:false,hidden:true},
        				{name:'acl_type',index:'acl_type',width:30, editable:false,hidden:false,align:'center',
        					formatter:function(cellValue, option) {
        						 switch(cellValue){
        						 	case 'ALL' : return '전사'; break;
        						 	case 'DEPT' : return '하위부서포함'; break;
        						 	case 'TEAM' : return '부서'; break;
        						 	case 'PRIVATE' : return '공유안함'; break;
        						 };
        					   }
        				},
        				{name:'open_id',index:'open_id',width:3, editable:false,hidden:true},
        				{name:'open_isgroup',index:'open_isgroup',width:3, editable:false,hidden:true},
        				{name:'creator_id',index:'creator_id',width:3, editable:false,hidden:true},
        			],
        			autowidth:true,
        			height:190,
        			viewrecords: true,multiselect:false,sortable:true,shrinkToFit:true,gridview: true,
        			sortname:"sort_index", // 최초 정렬은 하위부서포함>부서>전사>개인
        			sortorder:"asc",
        			scroll:true,
        			scrollOffset : 0,
        			viewsortcols:'vertical',
        			rowNum :50,
        			emptyDataText: "데이터가 없습니다.",
        			caption:'권한 목록'
        			,onSelectRow: function(rowid,status){
        				selectAclWindow.event.selectAclList(rowid);
        			}
        			,beforeSelectRow: function(rowid, e) {
        				var $radio = $(e.target).closest('tr').find('input[type="radio"]');
        		        $radio.prop('checked', 'checked');
        		        $(selectAclWindow.functions.getTargetAclTableId(true)).jqGrid('setSelection',rowid);
        		        return true;
        			}
        			,loadBeforeSend: function() {
        				exsoft.util.grid.gridNoDataMsgInit(selectAclWindow.functions.getTargetAclTableId());
        				exsoft.util.grid.gridTitleBarHide(selectAclWindow.functions.getTargetAclTableId());
        			}
        			,loadComplete: function() {

        				$("#tabAclList").removeClass("hide");			// 퍼블리셔 작업 JS 영향으로 인해서 추가해줌 :: 2015.05.17

        				if ($(selectAclWindow.functions.getTargetAclTableId(true)).getGridParam("records")==0) {
        					exsoft.util.grid.gridNoDataMsg(selectAclWindow.functions.getTargetAclTableId(),'no_data');
        				}

        				exsoft.util.grid.gridInputInit(false);

        			}
        			,loadError:function(xhr, status, error) {
        				exsoft.util.error.isErrorChk(xhr);
        			}
        		});

        	},

        	// 확장ACL 목록 리스트 :: 권한변경 선택 후 재 권한변경 선택 에러 수정처리
        	extAclList : function(list) {
        		$("#selAclWindowDocExtAclItemList").jqGrid({
        			data: list,
        			datatype:'local',
        			colNames:['accessor_id', 'accessor_isgroup', 'accessor_isalias', '접근자','기본권한','문서등록','반출취소','권한변경'],
        			colModel:[
        						{name:'accessor_id',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,key:true,hidden:true},
        						{name:'accessor_isgroup',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,hidden:true},
        						{name:'accessor_isalias',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,hidden:true},
        						{name:'accessor_name',index:'accessor_name',width:30, editable:false,sortable:false,resizable:true,align:'center'},
        						{name:'doc_default_acl',index:'doc_default_acl',width:30, editable:true,sortable:false,resizable:true,align:'center',edittype:'select',
        							editoptions:{
        								value:"NONE:없음;DELETE:삭제;UPDATE:수정;READ:조회;BROWSE:목록"
        							},formatter:'select'
        						},
        						{name:'doc_act_create',index:'doc_act_create',width:30, editable:true,sortable:false,resizable:true,align:'center',
        							edittype:'checkbox',
        							editoptions:{value:'T:F'},
        							fomatter:'checkbox'
        						},
        						{name:'doc_act_cancel_checkout',index:'doc_act_cancel_checkout',width:30, editable:true,sortable:false,resizable:true,align:'center',
        							edittype:'checkbox',
        							editoptions:{value:'T:F'},
        							fomatter:'checkbox'
        						},
        						{name:'doc_act_change_permission',index:'doc_act_change_permission',width:30, editable:true,sortable:false,resizable:true,align:'center',
        							edittype:'checkbox',
        							editoptions:{value:'T:F'},
        							fomatter:'checkbox'
        						},
        					],
        			autowidth:true,
        			viewrecords: true,
        			multiselect:true,
        			sortable: true,
        			shrinkToFit:true,
        			scrollOffset: 0,  // 스크롤 위치 조정. 0으로 지정 안할 경우 테이블 우측에 여백 생김
        			gridview: true,
        			emptyDataText: "데이터가 없습니다.",
        			caption:'추가 접근자 목록'
        			,loadBeforeSend: function() {
        				exsoft.util.grid.gridNoDataMsgInit('selAclWindowDocExtAclItemList');
        				exsoft.util.grid.gridTitleBarHide('selAclWindowDocExtAclItemList');
        			}
        			,loadComplete: function(data) {

        				if ($("#selAclWindowDocExtAclItemList").getGridParam("records") ==0) {
        					exsoft.util.grid.gridNoDataMsg('selAclWindowDocExtAclItemList','nolayer_data');
        					mRowId = 0;
        				}else {
        					// 편집모드로 변경
        					var rowIDs = $("#selAclWindowDocExtAclItemList").jqGrid('getDataIDs');
        					for(var i=0; i<rowIDs.length; i++){
        						$('#selAclWindowDocExtAclItemList').editRow(rowIDs[i],true);
        					}
        				}

        				exsoft.util.grid.gridInputInit(true); // 시차 문제로 gridTitleBarHide 적용 안될 시 true
        			}
        			,loadError: function (jqXHR, textStatus, errorThrown) {
        				exsoft.util.error.isErrorChk(jqXHR);
        		    }
        			,onCellSelect: function(rowid, iCol,cellcontent,e){
        				$('#selAclWindowDocExtAclItemList').editRow(rowid,false);
        		 	}
        			,onSelectRow: function(rowid,status,e){
        				var edited = exsoft.util.grid.gridEditMode('selAclWindowDocExtAclItemList',rowid);
        				// false 이면 row 저장처리
        				if (!status) {
        					$('#selAclWindowDocExtAclItemList').jqGrid('saveRow', rowid, selectAclWindow.extParams );
        				} else {
        					if(edited == "0") {
        						$('#selAclWindowDocExtAclItemList').editRow(rowid,false);
        					}
        				}
        		     }

        		});
        	}
        },

        // 3. 화면 이벤트 처리
        event : {
        	// 현재권한 / 상속권한 범위 설정 처리
        	selectAclScope : function(scope) {
        		selectAclWindow.selAclInfo.aclId = scope.value;
        		selectAclWindow.event.selectAclList(scope.value);
        		selectAclWindow.ui.uncheckDefaultAclCheckbox();
        	},

        	// ACL 목록에서 ACL을 선택했을때 처리
        	selectAclList : function(aclId) {
        		selectAclWindow.selAclInfo.aclId = aclId;
        		selectAclWindow.ajax.getAclDetail(aclId);
        		selectAclWindow.ui.uncheckDefaultAclCheckbox();
        	},

        	// 검색처리 화면 미연계 수정처리
        	searchAcl : function() {
        		var postData = {
        				strKeyword2 : exsoft.util.common.sqlInjectionReplace($(selectAclWindow.functions.getTargetFormId() + " #strKeyword2").val()),
        				is_search : "true"
        		};

        		exsoft.util.grid.gridPostDataRefresh(selectAclWindow.functions.getTargetAclTableId(true), exsoft.contextRoot + '/permission/aclList.do', postData);
        	},

        	// 확인
        	submit : function() {

        		if(selectAclWindow.selAclInfo.aclId == "") {
        			jAlert("변경할 권한을 선택하세요","권한변경",6);
        			return false;
        		}

        		// 추가접근자 값 :: 김선재 대리 미구현 부분 :: 2014.05.17 추가 구현처리
        		if(selectAclWindow.type == Constant.ACL.TYPE_DOC) {
        			selectAclWindow.selAclInfo.extAclItems =  exsoft.util.grid.gridSetAclItemToJsonArray("selAclWindowDocExtAclItemList", selectAclWindow.extParams )
        		}
        		selectAclWindow.callback(selectAclWindow.selAclInfo)
        		selectAclWindow.close();
        	},

			// 탭변경 :: 김선재 대리 미구현 부분 :: 2014.05.17 추가 구현처리
        	// ecm.js 속성으로 인해 개인권한 탭 선택시 문제 발생해서 제거처리함
			changeTab : function(tabType, obj) {

				// 문서등록(수정),폴더등록(수정) 적용 확인
				$($(obj).parent().children()).each(function() {
					if (this == obj) {
						$(this).addClass("selected");
					} else {
						$(this).removeClass("selected");
					}
				});

				selectAclWindow.aclType = tabType;

				exsoft.util.grid.gridPostDataRefresh(selectAclWindow.functions.getTargetAclTableId(true), exsoft.contextRoot + '/permission/aclList.do', {type:selectAclWindow.aclType});

			},

			// 권한 등록
			createAclPopup : function() {
				registAclWindow.init(selectAclWindow.callbackFunctions.registAclWindowCallback, "create");
			},

			// 권한 수정
			modifyAclPopup : function() {
				//등록자와 로그인한 사용자가 동일한지 체크 한다.
				if(selectAclWindow.selAclInfo.aclDetail.creator_id != exsoft.user.user_id){
					jAlert('권한 수정은 권한 등록자만 수정 가능합니다.',"권한수정",6);
					return;
				}

				registAclWindow.init(selectAclWindow.callbackFunctions.registAclWindowCallback, "modify", selectAclWindow.selAclInfo.aclDetail, selectAclWindow.selAclInfo.aclItems);
			},

			// 권한 삭제
			deleteAclPopup : function() {
				var id = $(selectAclWindow.functions.getTargetAclTableId(true)).jqGrid("getGridParam", "selrow");
				var jsonArr = [];
				var rowData = {acl_id:""};

				if (id == null) {
					jAlert("'현재 적용 권한' 및 '상속 권한'은 삭제할 수 없습니다.","권한삭제",6);
					return;
				} else {
					rowData['acl_id'] = id;
					jsonArr[0] = rowData;

					jConfirm('권한을 삭제하시겠습니까?', '권한 삭제', 2, function(isConfirm){
						try {
							var jsonObject = { "type":"delete", "acl_idList":JSON.stringify(jsonArr)};
							if(isConfirm) {
								exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject, exsoft.contextRoot + '/permission/aclControl.do', id, function(data, param){
									if(data.result == "true") {
										exsoft.util.grid.gridRefresh(selectAclWindow.functions.getTargetAclTableId(), exsoft.contextRoot + '/permission/aclList.do');
										selectAclWindow.ui.setDefaultAclScope();
									} else {
										jAlert(data.message,'확인',7);
									}
								});
							}
						}finally{
							jsonObject = null;
							jsonArr = null;
						}
					});
				}
			},

			// 권한 복사
			copyAclPopup : function() {
				// 호출하는 페이지에서 registAclWindow.jsp include 해야 함.
				var acl_id = $(selectAclWindow.functions.getTargetFormId() + " input[name=radio_selectAcl]:checked").val();

				if(acl_id == undefined || acl_id == null)
					acl_id = $(selectAclWindow.functions.getTargetAclTableId(true)).jqGrid("getGridParam", "selrow" );

				exsoft.util.ajax.ajaxDataFunctionWithCallback({'acl_id' : acl_id}, exsoft.contextRoot + "/permission/aclItemList.do", acl_id, function(data, param) {
					registAclWindow.init(selectAclWindow.callbackFunctions.registAclWindowCallback, "copy", data.aclDetail, data.list);
				});
			},


			// 접근자 추가
			addAccessor : function() {
				// 접근자 추가 윈도우 팝업
				selectAccessorWindow.init(selectAclWindow.callbackFunctions.addAccessor, "selAclWindowDocExtAclItemList", selectAclWindow.type);
			},

			// 접근자 제거
			removeAccessor : function() {
				exsoft.util.grid.gridDeleteRow('selAclWindowDocExtAclItemList', '','', true);
			},

			// 기본권한 사용안함 체크
			checkDefaultAcl : function(obj) {
				if(obj.checked == true) {
					selectAclWindow.ui.setUncheckAclScope(false);
					selectAclWindow.ui.uncheckGridRadio();
					selectAclWindow.selAclInfo.aclId = "default";		// 	기본권한사용안함 acl_id = default 누락 추가 처리 :: 2014.05.17
					selectAclWindow.ajax.getAclDetail('default');			// 기본권한사용안함 acl_id = default
				} else {
					selectAclWindow.selAclInfo.aclId = "";
					selectAclWindow.ui.setDefaultAclScope();
					selectAclWindow.ui.uncheckGridRadio();
				}
			}
        },

        // 4. 화면 UI 변경 처리
        ui : {
        	// "현재 적용 권한"으로 라디오 버튼 초기화
        	setDefaultAclScope : function() {
        		var _formId = selectAclWindow.functions.getTargetFormId();
        		var _selector = _formId + " #pCurAclId";
        		$(_selector).prop("checked", true);
        	},

        	// "현재 적용 권한", "상속 권한" 라디어 버튼 언체 :: ID오류 수정처리(2015.05.17)
        	setUncheckAclScope : function() {
        		var _formId = selectAclWindow.functions.getTargetFormId();
        		var _selector = "{0} {1}, {0} {2}".format(_formId, "#pCurAclId", "#pInheritAclId");
        		$(_selector).prop("checked", false);
        	},

        	// "현재 적용 권한", 상속 권한" 라디오 버튼 활성 / 비활성 :: ID오류 수정처리(2015.05.17)
        	setEnableRadio : function(bEnabled) {
        		var formId = selectAclWindow.functions.getTargetFormId();
        		var selector = "{0} {1}, {0} {2}".format(formId, "#pCurAclId", "#pInheritAclId");
        		$(selector).prop("disabled", bEnabled);
        	},

        	// 그리드의 라디오 버튼 언체크
        	uncheckGridRadio : function() {
        		$(selectAclWindow.functions.getTargetFormId() + " input[name=radio_selectAcl]").each(function(idx){
        			if($(this).is(':checked')){
        				$(this).prop('checked', false);
        				return false; // break 효과
        			}

        		});
        	},

        	// 기본권한 사용안함 언체크
        	uncheckDefaultAclCheckbox : function() {
        		var _chk = $("#checkDefaultAcl");
        		if (_chk.is(":checked")) {
        			_chk.prop("checked", false);
        		}
        	}

        },

        // 5. 콜백
        callbackFunctions : {
        	// 권한 등록 콜백
        	registAclWindowCallback : function(data, param) {
        		if (data.result != "true") {
        			jAlert(data.message,'확인',8);
        			return;
        		}

        		var _msg = "[{0}] 권한 {1}했습니다.".format(data.acl_name, param == "create" ? "등록" : "수정");

        		exsoft.util.grid.gridRefresh(selectAclWindow.functions.getTargetAclTableId(), exsoft.contextRoot + "/permission/aclList.do");
        		exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : data.acl_id}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data2, param) {
        			if (selectAclWindow.functions.isDocType()) {
        				selectAclWindow.selAclInfo.aclId = data.acl_id;
						selectAclWindow.selAclInfo.aclItems = data2.list;
						selectAclWindow.selAclInfo.aclDetail = data2.aclDetail;
						exsoft.util.table.tableDocumentAclItemPrintList(selectAclWindow.functions.getTargetAclDetailTableId(true), data2.list);
					} else if (selectAclWindow.functions.isFolderType()){
						selectAclWindow.selAclInfo.aclId = data.acl_id;
						selectAclWindow.selAclInfo.aclItems = data2.list;
						selectAclWindow.selAclInfo.aclDetail = data2.aclDetail;
						exsoft.util.table.tableFolderAclItemPrintList(selectAclWindow.functions.getTargetAclDetailTableId(true), data2.list);
					}
        		});
        		selectAclWindow.ui.setDefaultAclScope();
        	},

        	// 접근자 추가 선택 윈도우 콜백
        	addAccessor : function(extAclList) {
        		exsoft.util.grid.gridNoDataMsgInit('selAclWindowDocExtAclItemList');	// 데이터없음 초기화
        		selectAclWindow.selAclInfo.extAclItems = extAclList;
        		exsoft.util.grid.gridSetAclItemAddCallback('selAclWindowDocExtAclItemList', extAclList);
        	},

        }

}