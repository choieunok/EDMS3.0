// [3000]	2015-09-21	성예나	 : 권한변경여부,공유폴더 여부의 FALSE,TRUE 부분 자연스럽게 수정
// [3001]	2015-09-21	성예나	 : 스토리지 사용량 추가
var detailfolderWindow = {
		binder : new DataBinder("#detailFolder"),
		wrapperClass : "fol_detail_wrapper",
		layerClass : "fol_detail",
		aclTableId : "#detailFolderAclList",
		folderTypeId : "#detailFolderType",
		aclDetail : null,
		callback : null,
		node : null,
		folderId : null,
		
		// 폼 초기화
		initForm : function(node, folderId, kbn) {

			if(kbn != "admin"){
	    		$("#folHistoryexcel").addClass('hide'); //엑셀 출력 버튼 초기화
				//기본 탭 선택 초기화  ==========================
		        var targetFrm = $("#defaultFol").parent().parent().parent().find('div[class^="tab_form"]');
		        targetFrm.addClass('hide');
		        targetFrm.eq(0).removeClass('hide');
		        $('.tab_element').removeClass('selected');
		        $("#defaultFol").addClass('selected');
		        //=======================================
		        
				detailfolderWindow.node = node;
				detailfolderWindow.folderId = folderId;
				// 수정 모드일 경우 폴더정보를 조회해서 화면에 출력
				detailfolderWindow.ajax.getFolderInfo(folderId);
				detailfolderWindow.initFormCommon(node);
	
				// 개인문서함 및 권한변경 권한 없을 경우 권한변경, 일괄변경 hide 처리
				if(node.original.map_id == Constant.MAP_MYPAGE || node.original.acl_changePermission != 'T'){
					$("#detailFolder_inherit_acl").hide();
					$("#detailFolder_share").hide();
					$("#detailFolder_changefolderacl").hide();
				}
			}else{
				// 관리자인 경우 이력탭만 출력
				$("#folTitle").html("폴더 이력 조회");
				//기본 탭 선택 초기화  ==========================
		        var targetFrm = $("#historyFol").parent().parent().parent().find('div[class^="tab_form"]');
		        targetFrm.addClass('hide');
		        $("#defaultFol").addClass('hide');
		        targetFrm.eq(1).removeClass('hide');
		        $('.tab_element').removeClass('selected');
		        $("#historyFol").addClass('selected');
		        //=======================================
		        detailfolderWindow.node = node;
				detailfolderWindow.folderId = folderId;
				// 수정 모드일 경우 폴더정보를 조회해서 화면에 출력
				detailfolderWindow.ajax.getFolderInfo(folderId);
				detailfolderWindow.initFormCommon(node);
				
				detailfolderWindow.event.getFolderHistoryList();
			}


		},

		clearForm : function() {
			detailfolderWindow.aclDetail = null;
			$("#detailFolder input[data-bind=folder_id]").val("");
			$("#detailFolder input[data-bind=storage_quota]").val("0");
			$("#detailFolder input[data-bind=storage_usage]").val("0");	//[3001]
			$("#detailFolder input[data-bind=folder_name_ko]").val("");
			$("#detailFolder input[data-bind=folder_name_en]").val("");
			$("#detailFolder input[data-bind=sort_index]").val("");

		},

		open : function() {
			exsoft.util.layout.divLayerOpen(detailfolderWindow.wrapperClass,detailfolderWindow.layerClass);
		},

		close : function() {
			exsoft.util.layout.divLayerClose(detailfolderWindow.wrapperClass,detailfolderWindow.layerClass);
		},

		// 폼 초기화 공통 메서드
		initFormCommon : function(node) {
			detailfolderWindow.binder.set("full_path", node.full_path.join("/"));
			detailfolderWindow.ajax.getTypes(node);
			detailfolderWindow.ajax.getAclDetail(node.original.acl_id);

			//selectbox 처리
			detailfolderWindow.initDdslick();

		},

		initDdslick : function(){
			// 저장 여부
			exsoft.util.common.ddslick('#detail_doc_save', 'detail_doc_save', 'is_save', 90, function(divId, selectedData){
				detailfolderWindow.binder.set("is_save", selectedData.selectedData.value);
			});

			// 사용여부
			exsoft.util.common.ddslick('#detail_doc_use', 'detail_doc_use', 'folder_status', 90, function(divId, selectedData){
				detailfolderWindow.binder.set("folder_status", selectedData.selectedData.value);
			});

		},

		initGetTypes : function(data, param){
			$(detailfolderWindow.folderTypeId).remove();

			$('#detailFolderType_template').append('<select id="detailFolderType" data-bind="is_type" data-select="true">');
			$.each(data.typeList, function(){

				$(detailfolderWindow.folderTypeId).append("<option value='{0}'>{1}</option>".format(this.type_id, this.type_name));
			});
			$('#detailFolderType_template').append('</select>');
		},

		// 서버 Ajax 처리
		ajax : {
			getAclDetail : function(aclId) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"acl_id" : aclId}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data, acl_id) {
					detailfolderWindow.aclDetail = data.aclDetail;
					detailfolderWindow.binder.set("acl_id", data.aclDetail.acl_id);

					exsoft.util.table.tableFolderAclItemPrintList(detailfolderWindow.aclTableId, data.list);
			   		detailfolderWindow.ui.changeAclName(data.aclDetail.acl_name);
				});
			},
			getTypes : function(node) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback('', exsoft.contextRoot+'/folder/makeTypeSelectbox.do', '', function(data, param){
					$.when(detailfolderWindow.initGetTypes(data, param)).then(exsoft.util.common.ddslick('#detailFolderType', 'detail_saveDoc_type', 'is_type', 90, function(divId, selectedData){
						detailfolderWindow.binder.set("is_type", selectedData.selectedData.value);
					})).done(detailfolderWindow.binder.set("is_type", node.original.is_type));
				});
			},

			getFolderInfo : function(folderId, callType) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({folder_id : folderId}, exsoft.contextRoot+"/folder/folderDetail.do", '', function(data, param){
					if (data.result == "true") {

						//  권한변경여부		//[3000]					
						if(data.folderDetail.is_inherit_acl == "T"){
							data.folderDetail.is_inherit_acl = "상위폴더 권한 변경시 현재권한 유지";
						}else{
							data.folderDetail.is_inherit_acl = "상위폴더 권한변경시 현재권한 유지안함 ";
						}
						// 공유여부			//[3000]				
						if(data.folderDetail.is_share == "T"){
							data.folderDetail.is_share = "권한을 부여받은 사용자에게'공유받은 폴더'제공";
						}else{
							data.folderDetail.is_share = "권한을 부여받은 사용자에게'공유받은 폴더'제공하지 않음";
						}
						
						//Data Binding
						detailfolderWindow.binder.binding(data.folderDetail);

						//selectbox 선택 불가 (disable)
						$('#detail_doc_save').ddslick('disable');
						$('#detail_doc_use').ddslick('disable');
						$('#detailFolderType').ddslick('disable');


						//스토리지 할당량 Setting
						var _quota = detailfolderWindow.binder.get('storage_quota');
						if (_quota == "-1") {
							 $("[data-bind=storage_quota]").html("무제한");
						} else {
							// BTYE -> GB 로 단위 변환
							 $("[data-bind=storage_quota]").html( parseInt(_quota/1024/1024/1024) + " GB");
						}

						$("#detail_doc_save").prop("disabled",true);

						// 스토리지 사용량 Setting	[3001] START
						var _usage = detailfolderWindow.binder.get('storage_usage');
						
						if(_usage <= 1024){		//1kb미만일 때
						$("[data-bind=storage_usage]").html( parseInt(_usage) + "B");
						}else if(_usage <= 1048576){		//1mb미만일 때
						$("[data-bind=storage_usage]").html( parseInt(_usage/1024) + "KB");
						}else if(_usage <= 1073741824){		//1gb미만일 때 
						$("[data-bind=storage_usage]").html( parseInt(_usage/1024/1024) + "MB");
						}else{
						$("[data-bind=storage_usage]").html( parseInt(_usage/1024/1024/1024) + "GB");
						}
						// [3001] END
						
					} else {
						jAlert(data.message, "폴더", 8);
					}
					
					
				});
			},

		
		},

		// 화면 Event 처리
		event : {			
				
			// 폴더의 이력정보를 가져오고 화면에 표시한다
        	getFolderHistoryList : function() {
        		$("#folHistoryexcel").removeClass('hide');
        		if($('#detailfolHistoryList')[0].grid != undefined)	{
    				exsoft.util.grid.gridPostDataRefresh('detailfolHistoryList',
    						exsoft.contextRoot+'/folder/folHistoryList.do', {folder_id:detailfolderWindow.folderId,is_search:'false',page_init:'true'});
    			}else {
    				detailfolderWindow.event.historyGridList();
    			}
        	},

        	// 폴더이력보기
        	historyGridList : function() {
				$('#detailfolHistoryList').jqGrid({
					url:exsoft.contextRoot+'/folder/folHistoryList.do',
					mtype:"post",
					datatype:'json',
					jsonReader:{
						page:'page',total:'total',root:'list'
					},
					colNames:['일시','수행작업','작업자','비고'],
					colModel:[
						{name:'basic_date',index:'basic_date',width:120, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'action_nm',index:'action_nm',width:90, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'actor_nm',index:'actor_nm',width:80, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'action_place',index:'action_place',width:170, editable:false,sortable:false,resizable:true,align:'center',
							formatter : function (cellValue, option, rowObject) {
  			        		  var noteStr = "";
  			        		  if (rowObject.action_id == "MOVE") {
  			        			  noteStr = "[{0}]폴더에서 [{1}]폴더로 이동".format(rowObject.before_nm, rowObject.after_nm);

  			        		  } else if (rowObject.action_id == "CHANGE_CREATOR") {
  			        			  noteStr = "[{0}]에서 [{1}]로 소유권 이전".format(rowObject.before_nm, rowObject.after_nm);
  			        		  }
  			        		  return noteStr;
  			        	  }

						},
					],
					autowidth:true,
					height:"auto",
					viewrecords: true,multiselect:false,sortable: true,shrinkToFit:true,gridview: true,
					sortname : "basic_date",
					sortorder:"desc",
					rowNum : 15,
					emptyDataText: "조회된 결과가 없습니다.",
					loadtext: "조회중",
					caption:'폴더이력',
					postData : {folder_id:detailfolderWindow.folderId}
					,loadError:function(xhr, status, error) {
						exsoft.util.grid.isErrorChk(xhr);
					 }
					,loadBeforeSend: function() {
						exsoft.util.grid.gridNoDataMsgInit('detailfolHistoryList');
						exsoft.util.grid.gridTitleBarHide('detailfolHistoryList');
					}
					,loadComplete: function(data) {

						if ($("#detailfolHistoryList").getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('detailfolHistoryList','no_data');
						}else {
							exsoft.util.grid.gridViewRecords('detailfolHistoryList');
						}

						exsoft.util.grid.gridInputInit(false);
						var width = jQuery("#targetFolHistoryList").width();
						$("#detailfolHistoryList").setGridWidth(width-0);
						exsoft.util.grid.gridPager("#historyfolderGridPager",data);
					}
				});
			},

			// 페이지이동 처리(공통)
			gridPage : function(page) {
				$("#detailfolHistoryList").setGridParam({page:page,postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
			},

			
		},
		
		ui : {
			changeAclName : function(aclName) {
				$("#folaclName").html(aclName);
			},
			
			//폴더 상세보기 선택탭 영역에 따른 액션 분기
			folDetailSelectAction : function(index) {
				if(index==0){
					detailfolderWindow.initForm(detailfolderWindow.node,detailfolderWindow.folderId);
				}else{
					detailfolderWindow.event.getFolderHistoryList();
				}
			},
		},
}