/**
 * [1001][EDMS-REQ-070~81]	2015-09-14	최은옥	 : 외부시스템 연계코드관리(관리자 화면)
 * 인터페이스 코드관련 스크립트
 */
var exsoftAdminExternalFunc = {

		buttons : ['atrrDelBtn','attrUpdateBtn','attrItemAdd','attrItemDel','is_hidden','attrCancelBtn'],
		gRowData : [ { attr_id:"", attr_name:"", attr_size:100,sort_index:0,is_mandatory:"F",is_editable:"T",is_search:"F",display_type:"INPUT",config:"" } ],
		gRowId : 0,
		mRowId : 0,
		gWorkCode : "",
		mList : 0,
		gridId : "",
		gridUrl : "",
		pageSize : "",
		gAttrItemIdx : 0,			// 항목설정 INDEX 값
		gRowItemId : "",			// 항목설정 BTN의 GRID ROW 정보
		gAttrItemType : "",

		binder : new DataBinder("#frmView"),

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

		init : {

			// 로그인 이력 초기 함수
			initPage : function(gridId,gridUrl,pageSize) {

				// 메인 GRID
				exsoftAdminExternalFunc.gridId = gridId;
				exsoftAdminExternalFunc.gridUrl = gridUrl;
				exsoftAdminExternalFunc.pageSize = pageSize;

			},

			// 버튼 상태 변경처리 :: TODO
			btnStateChange : function(flag) {

				$.each(exsoftAdminExternalFunc.buttons,function(index,item){
					 if(flag == 'T')	{
						 $("#"+item).prop('disabled', true);
						 $("#"+item).addClass("disabled");
					 }else {
						 $("#"+item).prop('disabled', false);
						 $("#"+item).removeClass("disabled");
					 }
				 });
			},
		},

		open : {

			//연계코드등록
			externalAdd : function() {
				exsoft.util.layout.divLayerOpen('register_docuType_wrapper', 'register_docuType');
				exsoft.util.common.formClear('frm');
				//exsoftAdminExternalFunc.event.fAttrGridList();
				exsoft.util.grid.gridInputInit(false);
			}
		},

		layer : {

		},

		close : {

		},

		event : {
			// 폴더찾기 호출 : 기본폴더 선택
			selectFolderFind : function(form) {
				
				var workType = Constant.WORK_DEPT;
				var map_id = Constant.MAP_DEPT;
				if(form=='frmView'){
					selectSingleFolderWindow.init(exsoftAdminExternalFunc.callback.folderFind1,map_id,workType,false,"ALL_TYPE");
				}else{
					selectSingleFolderWindow.init(exsoftAdminExternalFunc.callback.folderFind2,map_id,workType,false,"ALL_TYPE");
				}
				

			},

			// 연계코 목록 GRID :: TO DO 컬럼헤더 사이즈 조정
			fExternalListGrid : function() {
				$("#externalGridList").jqGrid({
					url:exsoft.contextRoot+exsoftAdminExternalFunc.gridUrl,
					mtype:"post",
					datatype:'json',
					jsonReader:{
						page:'page',toatl:'toatl',root:'list'
					},
					colNames:['work_code','work_description','folder_id','folder_nm'],
					colModel:[
						{name:'work_code',index:'work_code',width:20, editable:false,sortable:true,resizable:true,align:'left'},
					    {name:'work_description',index:'work_description',width:30, editable:false,sortable:true,resizable:true,align:'left'},
					    {name:'folder_id',index:'folder_id',width:40, editable:false,sortable:false,resizable:true,align:'left',hidden:true},
					    {name:'folder_nm',index:'folder_nm',width:40, editable:false,sortable:false,resizable:true,align:'center'}
					   ],
					autowidth:true,height:"auto",viewrecords: true,
					multiselect:true,
					height:"auto",
					sortname : "work_code",
					sortorder:"desc",
					sortable: true,
					shrinkToFit:true,
					gridview: true,
					multikey: "ctrlKey",
					viewsortcols:'vertical',
					rowNum : exsoftAdminExternalFunc.pageSize,
					emptyDataText: "데이터가 없습니다.",
					caption:'외부연계 코드 목록'
					,onCellSelect : function(rowid,iCol,cellcontent,e){

						exsoft.util.grid.checkBox(e);

						if(iCol == 0){
							$("#externalGridList").jqGrid('setSelection',rowid);
						}else {
							document.frmView.folder_path.value="";						
							exsoftAdminExternalFunc.ui.externalViewRefresh('externalGridList',rowid);
						}
					}
					,loadBeforeSend: function() {
						exsoft.util.grid.gridTitleBarHide('externalGridList');
						exsoft.util.grid.gridNoDataMsgInit('externalGridList');
					}
					,loadComplete: function(data) {

						if ($("#externalGridList").getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('externalGridList','no_data');
							//$(".btnGrp").addClass("hide");
						}else {
							exsoft.util.grid.gridViewRecords('externalGridList');
							$(".btnGrp").removeClass("hide");

							// 조회화면 DISPLAY
							var rowId = $("#externalGridList").getDataIDs()[0];
							exsoftAdminExternalFunc.gWorkCode = $("#externalGridList").getRowData(rowId).type_id;
							exsoftAdminExternalFunc.ui.externalViewRefresh('externalGridList',rowId);
							exsoft.util.grid.gridPager("#externalGridPager",data);
						}

						exsoft.util.grid.gridInputInit(false);
						exsoft.util.grid.gridResize('externalGridList','targetExternalGrid',20,0);
					}
					,loadError:function(xhr, status, error) {
						exsoft.util.error.isErrorChk(xhr);
			        }
				});

				var headerData = '{"work_code":"연계코드","work_description":"연계명","folder_id":"폴더ID","folder_nm":"폴더명"}';
			 	exsoft.util.grid.gridColumHeader('externalGridList',headerData,'center');

			},


			// 페이지이동 처리(공통)
			gridPage : function(nPage)	{
				$(exsoftAdminExternalFunc.gridId).setGridParam({page:nPage,postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
			},

			// 문서유형 검색처리
			searchFunc : function() {
				var postData = {strKeyword:exsoft.util.common.sqlInjectionReplace($("#strKeyword").val()),strIndex:'WORK_CODE',is_search:'true'} ;
				exsoft.util.grid.gridPostDataRefresh(exsoftAdminExternalFunc.gridId,exsoft.contextRoot+exsoftAdminExternalFunc.gridUrl,postData);
			},

			// 문서유형 삭제처리
			externalDel : function() {

				var jsonArr = [];
				var jsonArrIndex = 0;

				if(!exsoft.util.grid.gridSelectCheck('externalGridList'))	{
					jAlert("삭제할 연계코드를 선택하세요.",'확인',6);
					return false;
				}else {

					var id = $("#externalGridList").getGridParam('selarrrow');

					for (var i = 0; i < id.length; i++) {

						var rowData = {work_code:""};
						var rowId = $("#externalGridList").getRowData(id[i]);
						rowData['work_code'] = rowId.work_code;			// jsonObject

						if(rowData.work_code){
							jsonArr[jsonArrIndex] = rowData;
							jsonArrIndex++;
						}

					}

					if (id.length > 0) {
						jConfirm('선택한  연계코드를 삭제하시겠습니까?', '확인',2,
							function(r){
								var jsonObject = { "type":"delete", "WorkCodeList":JSON.stringify(jsonArr)};
								if(r) {
									exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot + "/admin/externalControl.do" , "externalDelete",
											function(data,e) {
												if(data.result == "true"){
													exsoft.util.grid.gridRefresh('externalGridList',exsoft.contextRoot+exsoftAdminExternalFunc.gridUrl);
												}else {
													jAlert(data.message,'확인',7);
												}

										});
								}
							});
					 }

				}


			},

			// 인터페이스 코드 저장처리
			applyExternalWrite : function(type) {
				
				var objForm = null;

				if(type == "regist")	{
					objForm = document.frm;
				}else {
					objForm = document.frmView;
				}

				 if (objForm.work_code.value.length == 0) {
				    	jAlert("연계 코드를 입력하세요.","확인",6);
						return false;
				    }

				    if (objForm.work_description.value.length == 0) {
				    	jAlert("연계명을 입력하세요.","확인",6);
						return false;
				    }

				    if (objForm.folder_id.value.length == 0) {
				    	jAlert("폴더ID를 선택하세요.","확인",6);
						return false;
				    }
					// 서버 데이터 전송처리
				    exsoftAdminExternalFunc.binder.set("work_code",objForm.work_code.value);
				    exsoftAdminExternalFunc.binder.set("work_description",objForm.work_description.value);
				    exsoftAdminExternalFunc.binder.set("folder_id",objForm.folder_id.value);
				  
				    if(type == "regist")	{
				    	
				    	exsoftAdminExternalFunc.binder.set("type","insert");
				    	exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(exsoftAdminExternalFunc.binder.getDataToJson(),exsoft.contextRoot +'/admin/externalControl.do','externalInsert',
				    			exsoftAdminExternalFunc.callback.returnAjaxDataFunction);
				    	
				    }else {
				    	exsoftAdminExternalFunc.binder.set("type","update");
				    	exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(exsoftAdminExternalFunc.binder.getDataToJson(),exsoft.contextRoot +'/admin/externalControl.do','externalUpdate',
				    			exsoftAdminExternalFunc.callback.returnAjaxDataFunction);
				    }

					

			},


		},

		ui : {

			
			// 상세화면 보기
			externalViewRefresh : function(gridIds,rowid) {
				exsoftAdminExternalFunc.gWorkCode = $("#"+gridIds).getRowData(rowid).work_code;
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({ "strKeyword":exsoftAdminExternalFunc.gWorkCode}, exsoft.contextRoot + "/admin/externalInfo.do" , "extenalDetail",exsoftAdminExternalFunc.callback.returnAjaxDataFunction);
			},

			// 문서유형 수정 후 상세보기 갱신
			attrDetailCall : function(type_id) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({ "strKeyword":work_code}, exsoft.contextRoot + "/admin/externalInfo.do" , "extenalDetail",exsoftAdminExternalFunc.callback.returnAjaxDataFunction);
			},
			
		},//ui end

		callback : {
			// 기본폴더 set
        	folderFind1 : function(nodeInfo) {
        		document.frmView.folder_id.value=nodeInfo.id;
        		document.frmView.folder_path.value=nodeInfo.full_path.join("/");
			},
			folderFind2 : function(nodeInfo) {
        		document.frm.folder_id.value=nodeInfo.id;
        		document.frm.folder_path.value=nodeInfo.full_path.join("/");
			},
			returnAjaxDataFunction : function(data,param) {

				if(param == 'externalInsert'){
					if(data.result == "true") {
						exsoft.util.grid.gridRefresh('externalGridList',exsoft.contextRoot+exsoftAdminExternalFunc.gridUrl);
						exsoft.util.layout.divLayerClose('register_docuType_wrapper','register_docuType');
					}else {
						jAlert(data.message,"확인",7);
					}
				}else if(param == 'externalUpdate'){
					if(data.result == "true") {
						jAlert("수정완료 되었습니다.","확인",8);
						exsoft.util.grid.gridRefresh('externalGridList',exsoft.contextRoot+exsoftAdminExternalFunc.gridUrl);
						//exsoftAdminExternalFunc.ui.attrDetailCall(exsoftAdminExternalFunc.gWorkCode);
					}else {
						jAlert(data.message,"확인",7);
					}
				}else if(param == 'extenalDetail'){

					if(data.result == "true"){
						// 조회 내용 출력
						exsoftAdminExternalFunc.binder.set("work_code",data.list.work_code);
						exsoftAdminExternalFunc.binder.set("work_description",data.list.work_description);
						exsoftAdminExternalFunc.binder.set("folder_id",data.list.folder_id);


					}else {
						jAlert(data.message,'확인',7);
					}
				}

			}
		}
}