var aclManager = {

		currentAclId : null,
		gParameters : {
			    "successfunc" : null,
			    "url" : 'clientArray',
			    "extraparam" : {},
			    "aftersavefunc" : function( response ) {},
			    "errorfunc": null,
			    "afterrestorefunc" : null,
			    "restoreAfterError" : true,
			    "mtype" : "POST"
		},

		init : function(pageSize) {

			aclManager.grid.pageSize = pageSize;
			aclManager.grid.initAclGridList();
			aclManager.grid.initAclItemGridList();

			exsoft.util.common.ddslick('#strIndex', 'srch_type', '', 120, function(divId, selectedData){});

			exsoft.util.common.ddslick('#acl_typeDdslick', 'srch_type', '', 120, function(divId, selectedData){});

		},

		ajax : {

			// 상세조회
			aclDetail : function(aclId) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : aclId}, exsoft.contextRoot+"/admin/aclDetail.do", "", function(data, param) {
					//XR_ACL Info View
					$('#acl_id').val(data.aclDetail.acl_id);				// hidden
					$('#src_acl_name').val(data.aclDetail.acl_name);		// hidden
					$('#open_id').val(data.aclDetail.open_id);				// hidden
					$('#open_isgroup').val(data.aclDetail.open_isgroup);	// hidden
					$('#creator_id').val(data.aclDetail.creator_id);		// hidden
					$('#acl_sort_index').text(data.aclDetail.sort_index);	// hidden

					$('#acl_name_title').text(data.aclDetail.acl_name);
					$('#acl_name').val(data.aclDetail.acl_name);

					exsoft.util.layout.setSelectBox('acl_typeDdslick', data.aclDetail.acl_type);
					$('#acl_type').val(data.aclDetail.acl_type);
					$('#open_name').val(data.aclDetail.open_name);
					$('#create_date').text(data.aclDetail.create_date);
					$('#creator_name').text(data.aclDetail.creator_name);

					// XR_ACLITEM 속성 리스트
					aclManager.grid.refreshAclItemList();

				});
			},

			deleteAcl : function(aclList) {
				if (aclList.length > 0) {
					jConfirm("권한을 삭제하시겠습니까?", "권한 삭제", 2, function(r) {
						if (r) {
							var _post = {"type":"delete", "acl_idList" : JSON.stringify(aclList)};
							exsoft.util.ajax.ajaxDataFunctionWithCallback(_post, exsoft.contextRoot + "/permission/aclControl.do", "", function(data, param) {
								if(data.result == "true"){
									jAlert(data.message,'확인',8);
									aclManager.grid.refreshAclList();
								}else {
									jAlert(data.message,'확인',7);
								}
							});
						}
					});
				}
			}
		},

		grid : {
			pageSize : 0,
			mRowId : 0,			// 수정컬럼ID
			mList : 0,
			paging : function (pageNum) {
				$("#aclGridList").setGridParam({page:pageNum,postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
			},
			refreshAclList : function() {
				exsoft.util.grid.gridRefresh('aclGridList', exsoft.contextRoot + '/permission/aclList.do');
			},
			refreshAclItemList : function() {
				if($('#aclItemGridList')[0].grid != undefined)	{
					exsoft.util.grid.gridPostDataRefresh('aclItemGridList',exsoft.contextRoot+'/permission/aclItemList.do', {acl_id:aclManager.currentAclId});
				}
			},
			initAclGridList : function() {
				$('#aclGridList').jqGrid({
					url: exsoft.contextRoot + '/permission/aclList.do',
					mtype:"post",
					datatype:'json',
					jsonReader:{
						page:'page',total:'total',root:'list'
					},
					colNames:['acl_id','권한명','공개대상','공유범위','공유범위','정렬'],
					colModel:[
						{name:'acl_id',index:'acl_id',width:5, align:'center',editable:false,sortable:false,key:true,hidden:true},
						{name:'acl_name',index:'acl_name',width:130, editable:false,sortable:true,resizable:true},
						{name:'open_name',index:'open_name',width:70, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'acl_type_name',index:'acl_type_name',width:5, editable:false,sortable:false,resizable:true,align:'center',hidden:true},
						{name:'acl_type',index:'acl_type',width:70, editable:false,sortable:true,resizable:true,align:'center',
							 formatter:function(cellValue, option) {
								 switch(cellValue){
								 	case 'ALL' : return '전사'; break;
								 	case 'DEPT' : return '하위부서포함'; break;
								 	case 'TEAM' : return '부서'; break;
								 	case 'PRIVATE' : return '공유안함'; break;
								 };
							   }
						},
						{name:'sort_index',index:'sort_index',width:3, editable:false,hidden:true},
					],
					autowidth:true,
					viewrecords: true,multiselect:true,sortable:true,shrinkToFit:true,gridview: true,
					scrollOffset:0,
					sortname:"sort_index", // 최초 정렬은 하위부서포함>부서>전사>개인
					sortorder:"asc",
					multikey: "ctrlKey",
					viewsortcols:'vertical',
					rowNum : 10,//aclManager.grid.pageSize,	
					rowList:exsoft.util.grid.listArraySize(),
					emptyDataText: "데이터가 없습니다.",
					caption:'권한 목록'
					,onCellSelect : function(rowid,iCol,cellcontent,e){

						if(iCol == 0){
							// 체크시 row값을 set한다.(선택시 : rowid셋팅, 해제시 : rowid제거)
							$("#aclGridList").jqGrid('setSelection',rowid);
						} else {
							aclManager.currentAclId = rowid;
							aclManager.ajax.aclDetail(rowid);
						}

					}
					,loadBeforeSend: function() {
						exsoft.util.grid.gridNoDataMsgInit('aclGridList');
						exsoft.util.grid.gridTitleBarHide('aclGridList');
					}
					,loadComplete: function(data) {

						if ($("#aclGridList").getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('aclGridList','nolayer_data');
							$(".btnGrp").addClass("hide");
						}else {
							exsoft.util.grid.gridViewRecords('aclGridList');
							$(".btnGrp").removeClass("hide");

							// 조회화면 DISPLAY
							var rowId = $("#aclGridList").getDataIDs()[0];
							aclManager.currentAclId = $("#aclGridList").getRowData(rowId).acl_id;
							aclManager.ajax.aclDetail(aclManager.currentAclId);
						}

						exsoft.util.grid.gridPager("#aclGridPager",data);

						exsoft.util.grid.gridInputInit(false); // 페이지 창 숫자만  test
						exsoft.util.grid.gridResize('aclGridList','targetAclGridList',100,0);
					}
					,loadError:function(xhr, status, error) {
						exsoft.util.error.isErrorChk(xhr);
					 }

				});
			},

			initAclItemGridList : function() {

				$('#aclItemGridList').jqGrid({
					list : "",
					mtype:"post",
					datatype:'json',
					jsonReader:{
						root:'list'
					},
					colNames:['accessor_id', 'accessor_isgroup', 'accessor_isalias', '접근자','기본권한','폴더등록','권한변경','기본권한','문서등록','반출취소','권한변경'],
					colModel:[
						{name:'accessor_id',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,key:true,hidden:true},
						{name:'accessor_isgroup',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,key:true,hidden:true},
						{name:'accessor_isalias',index:'accessor_id',width:5, align:'center',editable:false,sortable:false,key:true,hidden:true},
						{name:'accessor_name',index:'accessor_name',width:30, editable:false,sortable:false,resizable:true,align:'center',hidden:false},
						{name:'fol_default_acl',index:'fol_default_acl',width:30, editable:true,sortable:false,resizable:true,align:'center',edittype:'select',
							editoptions:{
								value:"DELETE:삭제;UPDATE:수정;READ:조회;BROWSE:목록"
							},formatter:'select' //formatter의 역활은 value값으로 grid에 표시함.
						},
						{name:'fol_act_create',index:'fol_act_create',width:30, editable:true,sortable:false,resizable:true,align:'center',
							edittype:'checkbox',
							editoptions:{value:'T:F'},
							fomatter:'checkbox'
						},
						{name:'fol_act_change_permission',index:'fol_act_change_permission',width:30, editable:true,sortable:false,resizable:true,align:'center',
							edittype:'checkbox',
							editoptions:{value:'T:F'},
							fomatter:'checkbox'
						},
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
					scrollOffset: 0,
					gridview: true,
					postData : {acl_id:aclManager.currentAclId},
					emptyDataText: "데이터가 없습니다.",
					caption:'접근자 목록'
					,loadBeforeSend: function() {
						exsoft.util.grid.gridNoDataMsgInit('aclItemGridList');
						exsoft.util.grid.gridTitleBarHide('aclItemGridList');
					}
					,loadComplete: function(data) {

						if ($("#aclItemGridList").getGridParam("records") ==0) {
							exsoft.util.grid.gridNoDataMsg('aclItemGridList','nolayer_data');
							aclManager.grid.mRowId = 0;
						}else {

							var rowIDs = $("#aclItemGridList").jqGrid('getDataIDs');
							aclManager.grid.mRowId = rowIDs[rowIDs.length-1];
						}

						aclManager.grid.mList = aclManager.grid.mRowId;		// atrr_id 수정못하게 처리위한 변수
						exsoft.util.grid.gridResize('aclItemGridList','targetAclItemGridList',90,0);

						// 접근자 정렬처리
						$("span.ui-jqgrid-resize.ui-jqgrid-resize-ltr").each(function(){
							$(this).css("height", "");
						});
					}
					,onCellSelect: function(rowid, iCol,cellcontent,e){
						if(exsoft.util.grid.gridIsRowDataExist('aclItemGridList', rowid, 'accessor_id', 'OWNER')) {
							jAlert('소유자는 수정할 수 없습니다.','권한관리',6);
							$('#aclItemGridList').jqGrid('setSelection',rowid,false); ////checkbox 해제
						} else {
							$('#aclItemGridList').editRow(rowid,false);
						}
				 	}
					,onSelectRow: function(rowid,status,e){
						if(!exsoft.util.grid.gridIsRowDataExist('aclItemGridList', rowid, 'accessor_id', 'OWNER')) {
							// 에디터모드인지 체크
							var edited = exsoft.util.grid.gridEditMode('aclItemGridList',rowid);
							// false 이면 row 저장처리
							if(!status) {
								$('#aclItemGridList').jqGrid('saveRow', rowid, aclManager.gParameters );
							}else {
								if(edited == "0") {
									$('#aclItemGridList').editRow(rowid,false);
								}
							}
						}
				     }
				});

				// 헤더 colspan
				$("#aclItemGridList").jqGrid('destroyGroupHeader');
				jQuery("#aclItemGridList").jqGrid('setGroupHeaders', {
				  useColSpanStyle: true,
				  groupHeaders:[
					{startColumnName: 'fol_default_acl', numberOfColumns: 3, titleText: '폴더권한'},
					{startColumnName: 'doc_default_acl', numberOfColumns: 4, titleText: '문서권한'}
				  ]
				});

			}
		},

		event : {

			// 수정취소 처리 기능추가 2015.05.18
			cancelProc : function() {
				aclManager.ajax.aclDetail($("#acl_id").val());
			},

			// 수정처리
			submit : function() {

				var gridIds = "aclItemGridList";
				var objForm = document.form_details;

				// validation check
				if (objForm.acl_name.value.length == 0) {
				  	jAlert("권한명을 입력하세요.",'권한관리',6);
					return false;
			    }

				if (objForm.open_id.value.length == 0) {
				  	jAlert("공개범위를 선택하세요.",'권한관리',6);
					return false;
			    }

				$("#acl_type").val(exsoft.util.common.getDdslick('#acl_typeDdslick'));

				// AclItem json 형태로 변형
				var jsonArr = exsoft.util.grid.gridSetAclItemToJsonArray(gridIds, aclManager.gParameters);

				// 접근자는 중복될 수 없다. 접근자 추가 기능에서 필터링 함
				// 서버 데이터 전송처리
				objForm.aclItemArrayList.value = JSON.stringify(jsonArr);

				objForm.type.value = "update";
				exsoft.util.ajax.ajaxFunctionWithCallback('form_details',exsoft.contextRoot+'/permission/aclControl.do','', function(data, param) {
					if(data.result == "true"){
						jAlert("정상적으로 수정되었습니다",'권한관리',8);
						aclManager.grid.refreshAclList();
					}else {
						jAlert(data.message,'권한관리',7);
					}

				});
			},

			searchAclList : function() {
				var postData = {
						strIndex: exsoft.util.common.getDdslick("#strIndex"),
						strKeyword1:exsoft.util.common.sqlInjectionReplace($("#strKeyword1").val()),
						strKeyword2:exsoft.util.common.sqlInjectionReplace($("#strKeyword2").val()),
						is_search:'true'
				};
				exsoft.util.grid.gridPostDataRefresh('aclGridList',exsoft.contextRoot+'/permission/aclList.do',postData);
			},

			// 권한 등록
			createAclPopup : function() {
				registAclWindow.init(aclManager.callbackFunctions.registAclWindowCallback, "create");
			},

			// 권한 삭제
			deleteAcl : function(type) {
				var jsonArr = [];
				var rowData = {acl_id:""};

				if(type == 'view'){
					rowData['acl_id'] = aclManager.currentAclId;
					jsonArr[0] = rowData;
				} else {
					if(!exsoft.util.grid.gridSelectCheck('aclGridList'))	{
						jAlert("삭제할 권한을 선택하세요.",'권한관리',6);
						return false;
					}else {

						var id = $("#aclGridList").getGridParam('selarrrow');

						for (var i = 0; i < id.length; i++) {
							var rowId = $("#aclGridList").getRowData(id[i]);
							rowData['acl_id'] = rowId.acl_id;

							if(rowData.acl_id){
								jsonArr[i] = rowData;
							}
						}
					}
				}

				aclManager.ajax.deleteAcl(jsonArr);
			},

			// 권한 복사
			copyAcl : function(type) {

				if (type == "view") {
					aclManager.functions.copyAclProcess(aclManager.currentAclId);
				} else {

					var checkCount = exsoft.util.grid.gridSelectCnt('aclGridList');

					if( checkCount == 1)	{
						aclManager.functions.copyAclProcess(exsoft.util.grid.gridSelectData('aclGridList', 'acl_id').replace(",",""));
					}else if( checkCount == 0)	{
						jAlert('복사할 권한을 선택하세요.','권한관리',6);
						return false;
					}else	{
						jAlert('권한 복사는 한 개의 권한을 선택해야 합니다.','권한관리',6);
						return false;
					}
				}
			},

			// 공개 범위 선택 팝업
			openRangeWindow : function() {
				if (exsoft.util.common.getDdslick('#acl_typeDdslick') == "PRIVATE") {
					selectSingleUserWindow.init.initSingleUserWindow(aclManager.callbackFunctions.selectUserCallback)
				} else {
					selectGroupWindow.init.initPage(aclManager.callbackFunctions.selectGroupCallback, "GROUP");
				}

			},

			// 사용자 추가
			addAccessorWindow : function() {
				selectAccessorWindow.init(aclManager.callbackFunctions.addAccessorWindowCallback, "aclItemGridList");
			},

			// 사용자 제거
			deleteAccessor : function() {
				exsoft.util.grid.gridDeleteRow("aclItemGridList", 'OWNER','소유자는 삭제할 수 없습니다.', false);
				exsoft.util.grid.gridCheckBoxInit("aclItemGridList");
			},

			// 엔터키 입력시
			enterKeyPress : function(e) {
				if (e.keyCode == 13) {
					aclManager.event.searchAclList();
					return false;
				}
			},
		},

		ui : {

		},

		functions : {

			copyAclProcess : function(aclId) {

				var aclObject = new Object();
				var arrAclItem = new Array();
				var jsonData = {'acl_id' : aclId};

				exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonData, exsoft.contextRoot+"/admin/aclDetail.do", aclId, function(data, param) {

					//param은  ajaxDataFunctionWithCallback의 acl_id가 param임.
					aclObject.acl_name = data.aclDetail.acl_name;
					aclObject.acl_type = data.aclDetail.acl_type;
					aclObject.open_id = data.aclDetail.open_id;
					aclObject.open_name = data.aclDetail.open_name;
					aclObject.open_isgroup = data.aclDetail.open_isgroup;
					aclObject.sort_index = data.aclDetail.sort_index;

					exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonData, exsoft.contextRoot+"/permission/aclItemList.do", aclId, function(data, param) {
						//param은  ajaxDataFunctionWithCallback의 acl_id가 param임.
						for(var idx in data.list) {
							arrAclItem[idx] = data.list[idx];
						}
						arrAclItem.length;
						//callback 처리 시차 발생으로 최종 callback에서 호출
						registAclWindow.init(aclManager.callbackFunctions.registAclWindowCallback, 'copy', aclObject, arrAclItem);
					});
				});
			}
		},
		callbackFunctions : {
			registAclWindowCallback : function(data, param) {
				if (data.result != "true") {
        			jAlert(data.message,'권한관리',7);
        			return;
        		}

        		var _msg = "[{0}] 권한 {1}했습니다.".format(data.acl_name, param == "create" ? "등록" : "수정");

        		jAlert(_msg,'권한관리',8);

        		aclManager.grid.refreshAclList();
			},

			addAccessorWindowCallback : function(aclItemRowList) {
				exsoft.util.grid.gridSetAclItemAddCallback('aclItemGridList', aclItemRowList);
			},

			selectUserCallback : function(userRowList) {
				//popup에서 넘겨주는 키 group_nm, user_name_ko, user_id, position_nm, role_nm, email, 	user_status_nm
				$('#open_name').val(userRowList.user_name_ko);
				$('#open_id').val(userRowList.user_id);
				$('#open_isgroup').val('F');
			},

			selectGroupCallback : function(groupInfo) {
				var type =  $('#acl_type').val();
				var map_id = groupInfo[0].mapId;
				if( map_id == 'PROJECT' && type == 'DEPT') {
					jAlert('프로젝트 그룹은 하위부서포함 공개범위를 선택할 수 없습니다.','권한관리',6);
					return;
				}

				$('#open_name').val(groupInfo[0].text);
				$('#open_id').val(groupInfo[0].id);
				$('#open_isgroup').val('T');
			}
		}
}