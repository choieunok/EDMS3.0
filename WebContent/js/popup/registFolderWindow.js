var folderWindow = {
		binder : new DataBinder("#registFolder"),
		wrapperClass : "subFolder_add_wrapper",
		layerClass : "subFolder_add",
		aclTableId : "#registFolderAclList",
		folderTypeId : "#registFolderType",
		aclDetail : null,
		callback : null,

		// 폼 초기화(생성 / 수정)
		initForm : function(node, folderId) {
			if (folderId === undefined) {
				// 생성 모드일 경우
				folderWindow.clearForm();
				folderWindow.binder.bindingElement();
				folderWindow.binder.set("parent_id", node.id);
				folderWindow.binder.set("map_id", node.original.map_id);
				folderWindow.binder.set("type", "CREATE");
				folderWindow.binder.set("sort_index", "0");
				folderWindow.initFormCommon(node);
				folderWindow.ui.setRecentlyListVisible(true);
				$("#acl_batch").addClass("hide");
			} else if (folderId !== undefined) {
				// 수정 모드일 경우 폴더정보를 조회해서 화면에 출력
				folderWindow.ajax.getFolderInfo(folderId);
				folderWindow.initFormCommon(node);
				folderWindow.ui.setRecentlyListVisible(false);
				$("#acl_batch").removeClass("hide");
			}
			
			// 일괄변경 체크박스 초기화
			$("input:checkbox[id='is_acl_batch']").prop("checked", false);

			folderWindow.ui.setModeStrings();
			// 개인문서함 및 권한변경 권한 없을 경우 권한변경, 일괄변경 hide 처리
			if(node.original.map_id == Constant.MAP_MYPAGE || node.original.acl_changePermission != 'T'){
				$("#registFolder_inherit_acl").hide();
				$("#registFolder_share").hide();
				$("#registFolder_changefolderacl").hide();
				$("#registFolder_storage_quota").hide();
			}


		},

		clearForm : function() {
			folderWindow.aclDetail = null;
			$("#registFolder input[data-bind=folder_id]").val("");

			$("#registFolder input[data-bind=storage_quota]").val("0");
			$("#registFolder input[data-bind=folder_name_ko]").val("");
			$("#registFolder input[data-bind=folder_name_en]").val("");
			$("#registFolder input[data-bind=sort_index]").val("");

			if ($("#storage_quotaCheckBox").is(":checked"))
				$("#storage_quotaCheckBox").click();
		},

		open : function() {
			exsoft.util.layout.divLayerOpen("subFolder_add_wrapper", "subFolder_add");
		},

		close : function() {
			exsoft.util.layout.divLayerClose("subFolder_add_wrapper", "subFolder_add");
		},

		// 폼 초기화 공통 메서드
		initFormCommon : function(node) {
			folderWindow.binder.set("full_path", node.full_path.join("/"));
			folderWindow.ajax.getTypes(node);
			folderWindow.ajax.getAclDetail(node.original.acl_id);

			//selectbox 처리
			folderWindow.initDdslick();

			// 최근 사용 폴더 목록
			folderWindow.ajax.getRecentlyFolderList();
		},

		initDdslick : function(){
			// 저장 여부
			exsoft.util.common.ddslick('#register_doc_save', 'register_doc_save', 'is_save', 90, function(divId, selectedData){
				//$("[data-bind=is_save]").val(selectedData.selectedData.value);
				folderWindow.binder.set("is_save", selectedData.selectedData.value);

			});

			// 사용여부
			exsoft.util.common.ddslick('#register_doc_use', 'register_doc_use', 'folder_status', 90, function(divId, selectedData){
				//$("[data-bind=folder_status]").val(selectedData.selectedData.value);
				folderWindow.binder.set("folder_status", selectedData.selectedData.value);
			});

		},

		initGetTypes : function(data, param){
			$(folderWindow.folderTypeId).remove();

			//registFolderType_template
			$('#registFolderType_template').append('<select id="registFolderType" data-bind="is_type" data-select="true">');
			$.each(data.typeList, function(){

				$(folderWindow.folderTypeId).append("<option value='{0}'>{1}</option>".format(this.type_id, this.type_name));
			});
			$('#registFolderType_template').append('</select>');
		},

		// 서버 Ajax 처리
		ajax : {
			getAclDetail : function(aclId) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : aclId}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data, acl_id) {
					folderWindow.aclDetail = data.aclDetail;
					folderWindow.binder.set("acl_id", data.aclDetail.acl_id);

					exsoft.util.table.tableFolderAclItemPrintList(folderWindow.aclTableId, data.list);
			   		folderWindow.ui.changeAclName(data.aclDetail.acl_name);
				})
			},
			getTypes : function(node) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback('', exsoft.contextRoot+'/folder/makeTypeSelectbox.do', '', function(data, param){
					$.when(folderWindow.initGetTypes(data, param)).then(exsoft.util.common.ddslick('#registFolderType', 'register_saveDoc_type', 'is_type', 90, function(divId, selectedData){
						folderWindow.binder.set("is_type", selectedData.selectedData.value);
					})).done(folderWindow.binder.set("is_type", node.original.is_type));
				});
			},

			getFolderInfo : function(folderId, callType) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback({folder_id : folderId}, exsoft.contextRoot+"/folder/folderDetail.do", '', function(data, param){
					if (data.result == "true") {
						$('#inherit_acl_chk').prop('checked',data.folderDetail.is_inherit_acl == 'T');		// 권한변경여부
						$('#share_chk').prop('checked',data.folderDetail.is_share == 'T');					// 공유여부
						
						folderWindow.binder.binding(data.folderDetail);
						
						// 최근목록에서 세팅했으면 등록모드로 갈수있게 분기
						if(callType != "" && callType != null) {
							folderWindow.binder.set("type", "CREATE");
						} else {
							folderWindow.binder.set("type", "UPDATE");
						}

						var _quota = folderWindow.binder.get('storage_quota');

						if (_quota == "-1") {
							var cbStorageQuota = $("#storage_quotaCheckBox");
							if (!cbStorageQuota.is(":checked")) {
								cbStorageQuota.click();
							} else {
								var quotaObj = $("[data-bind=storage_quota]");
								quotaObj.prop("readonly", true);
								quotaObj.prop("disabled", true);
								quotaObj.addClass("readonly");
								quotaObj.removeAttr("ex-valid");
								quotaObj.val("무제한");
							}
						} else {
							var cbStorageQuota = $("#storage_quotaCheckBox");
							if (cbStorageQuota.is(":checked")) {
								cbStorageQuota.prop("checked", false);
								var quotaObj = $("[data-bind=storage_quota]");
								quotaObj.removeClass("readonly");
								quotaObj.prop("readonly", false);
								quotaObj.prop("disabled", false);
								quotaObj.attr("ex-valid", "digit");
							}
							folderWindow.binder.set("storage_quota", parseInt(_quota/1024/1024/1024)); // BTYE -> GB 로 단위 변환
						}
						
					} else {
						jAlert(data.message, "폴더", 8);
					}
				});
			},
			//최근 폴더 가져오기
			getRecentlyFolderList : function() {
				var mapId = null;
				if(exsoft.util.layout.currentTopMenu()==Constant.TOPMENU.MYWORK){
					mapId = Constant.MAP_MYPAGE;
				}else if(exsoft.util.layout.currentTopMenu()==Constant.TOPMENU.WORKSPACE ){
					mapId = Constant.MAP_MYDEPT;					
				}
				
				exsoft.util.ajax.ajaxDataFunctionWithCallback({"mapId" : mapId}, exsoft.contextRoot+"/folder/recentlyFolderList.do", '', function(data, param){
					if (data.result == "true") {
						folderWindow.ui.recentFolderList(data.recentlyFolderList);
					} else {
						jAlert(data.message, "폴더", 8);
					}
				});
			},

			deleteRecentlyFolder : function(idx) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback({idx : idx}, exsoft.contextRoot+"/folder/recentlyFolderDelete.do", idx, function(data, deleteIdx){
					if (data.result == "true") {
						folderWindow.ui.removeRecentFolder(deleteIdx);
					} else {
						jAlert(data.message, "폴더", 8);
					}
				});
			}
		},

		// 화면 Event 처리
		event : {
			// 폴더 등록/수정
			submit : function() {
				if ($("#registFolder").validation()) {
					var _quota = folderWindow.binder.getDataToJson().storage_quota;

					if (_quota != "-1") {
						folderWindow.binder.set("storage_quota", _quota*1024*1024*1024); // GB -> BTYE 로 단위 변환
					}
					
					// 하위폴더 권한 일괄변경 체크박스
					if($("input:checkbox[id='is_acl_batch']").is(":checked") == true) {
						folderWindow.binder.set("is_acl_batch","on");
					}

					exsoft.util.ajax.ajaxDataFunctionWithCallback(folderWindow.binder.getDataToJson(), exsoft.contextRoot+"/folder/folderControl.do", "",
					function(data) {

						if(data.result == "true") {
							// 콜백 호출
							folderWindow.callback();
							exsoft.util.layout.divLayerClose(folderWindow.wrapperClass, folderWindow.layerClass);
							// 일괄변경 체크박스 초기화
							$("input:checkbox[id='is_acl_batch']").prop("checked", false);
						} else {
							jAlert(data.message, "폴더", 8);
						}
					})
				} 

			},
			// 스토리지 용량제한 체크/체크해제
			changeQuotaCheck : function(obj) {
				var checked = $(obj).is(":checked");
				var quotaObj = $("[data-bind=storage_quota]");

				quotaObj.prop("readonly", checked);
				quotaObj.prop("disabled", checked);

				if (checked) {
					quotaObj.addClass("readonly");
					quotaObj.removeAttr("ex-valid");
					folderWindow.binder.set("storage_quota", "-1");
				} else {
					quotaObj.removeClass("readonly");
					quotaObj.attr("ex-valid", "digit");
					folderWindow.binder.set("storage_quota", "");
				}

				quotaObj.val(checked ? "무제한" : "0");
			},

			// 상위 폴더 선택
			selectParentFolder : function() {
				if (folderWindow.binder.get("map_id") == "MYPAGE") {
					selectSingleFolderWindow.init(folderWindow.callbackFunctions.selectParentFolder, Constant.MAP_MYPAGE, Constant.WORK_MYPAGE,false, "ALL_TYPE");
				} else {
					selectSingleFolderWindow.init(folderWindow.callbackFunctions.selectParentFolder);

				}
			},

			// 권한 변경 선택
			changeFolderAcl : function() {

				var _folderInfo = folderWindow.binder.getDataToJson();
				var _aclId = folderWindow.binder.get("acl_id");
				selectAclWindow.init(_aclId, Constant.ACL.TYPE_FOLDER, folderWindow.callbackFunctions.selectAcl);

				var _obj = {
					current_acl_id : folderWindow.aclDetail.acl_id,
					current_acl_name : folderWindow.aclDetail.acl_name,
					parent_folder_id : folderWindow.binder.get("parent_id"),
					folder_id : "",
					type : "folder"
				};

				selectAclWindow.initInherit(_obj);
			},

			// 최근 폴더 삭제
			deleteRecentlyFolder : function(idx) {
				folderWindow.ajax.deleteRecentlyFolder(idx);
			}

		},
		callbackFunctions : {
			selectParentFolder : function(nodeInfo) {
				folderWindow.binder.set("full_path", nodeInfo.full_path.join("/"));
				folderWindow.binder.set("parent_id", nodeInfo.id);
			},
			selectAcl : function(aclInfo) {
				folderWindow.ajax.getAclDetail(aclInfo.aclId);
			}
		},
		ui : {
			changeAclName : function(aclName) {
				$("#aclName").html(aclName);
			},

			recentFolderList : function(list) {
				$("#recentFolderList").empty();
				var _str = "";

				$(list).each(function() {
					_str += "<li id='{0}'>".format(this.idx);
					_str += "<a href='#' onclick='folderWindow.ajax.getFolderInfo(\"{0}\", \"RECENT\");'>{1}</a>".format(this.target_id, this.display_name);
					_str += "<a href='#' class='recent_del' onclick='folderWindow.event.deleteRecentlyFolder(\"{0}\")'><img src='{1}/img/icon/recent_doc_del.png'></a>".format(this.idx, exsoft.contextRoot);
					_str += "</li>";
				});

				$("#recentFolderList").append(_str);
			},

			removeRecentFolder : function(idx) {
				$("#"+idx).remove();
			},

			setRecentlyListVisible : function(visible) {
				var _tar = $("#recentlyFolderDiv");

				if (visible) {
					_tar.show();
					$("#registFolder").css("width",759);
				} else {
					_tar.hide();
					$("#registFolder").css("width",993);
				}
			},

			setModeStrings : function() {
				if (folderWindow.binder.get("type") == "CREATE") {
					$("#registFolderWindowTitle").html("폴더 등록");
					$("#registFolderSubmitBtn").html("등록");
				} else {
					$("#registFolderWindowTitle").html("폴더 수정");
					$("#registFolderSubmitBtn").html("수정");
				}
			},

		},
}