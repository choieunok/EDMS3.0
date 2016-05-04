/*
 *  폴더관리
 *  
 *  @comment
 *  [2005][신규개발]	20160331	이재민 : 관리자 > 폴더관리 - 하위폴더 일괄 삭제처리
 *	 (상품관리 - 한경준 대리님 / 정종철 차장님(운영자)이 기능추가를 요구하심)
 * */

var folderManager = {

		gWorkType : "WORK_MYDEPT",			// 현재 선택된 탭 workType
		groupTree : null,								// 그룹 트리
		projectTree : null,								// 프로젝트 트리
		gFolderTree : null, 							// 나의 부서 JStree object

		binder : new DataBinder("#form_details"),

		// 0. 초기화
		init : function(workType) {

			var treeOption = null;
			// 트리 초기화
			if (workType == Constant.WORK_MYDEPT) {

				treeOption = {
					divId : "#myDeptFolderTree",
					context : exsoft.contextRoot,
					url : "/folder/folderList.do",
					mapId : Constant.MAP_MYDEPT,
					workType : Constant.WORK_MYDEPT
				};

				if (folderManager.groupTree == undefined) {
					folderManager.groupTree = new XFTree(treeOption);
					folderManager.groupTree.callbackSelectNode = folderManager.callback.selectFolder;
					folderManager.groupTree.init(); //부서 rootId는 서버에서 처리
				} else {
					folderManager.groupTree.refresh();
				}

				folderManager.gFolderTree = folderManager.groupTree;

			} else {

				treeOption = {
					divId : "#projectFolderTree",
					context : exsoft.contextRoot,
					url : "/folder/folderList.do",
					mapId : Constant.MAP_PROJECT,
					workType : Constant.WORK_PROJECT
				};

				if (folderManager.projectTree == undefined) {
					folderManager.projectTree = new XFTree(treeOption);
					folderManager.projectTree.callbackSelectNode = folderManager.callback.selectFolder;
					folderManager.projectTree.init();
				} else {
					folderManager.projectTree.refresh();

				}
				folderManager.gFolderTree = folderManager.projectTree;
			}
			
			// 일괄변경 체크박스 초기화
			$("input:checkbox[id='is_acl_batch']").prop("checked", false);

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

			// 구분 셀렉트박스 변경시
			changeWorkType : function(work_type) {

				if(work_type == "WORK_MYDEPT") {
					$("#myDeptFolderTree").removeClass("hide");
					$("#projectFolderTree").addClass("hide");
				} else {
					$("#projectFolderTree").removeClass("hide");
					$("#myDeptFolderTree").addClass("hide");
				}

				folderManager.init(work_type);
			},

			// 스토리지 할당량 체크박스 클릭시
			folderQuotaCheckBox : function(){

				if($("input:checkbox[id='folder_storage_quota_chk']").is(":checked") == false) {
					$('#storage_quota').prop("readonly", false);
					$('#storage_quota').prop("disabled", false);
					$('#storage_quota').removeClass("readonly");
					$('#storage_quota').val(0);
				} else {
					$('#storage_quota').prop("readonly", true);
					$('#storage_quota').prop("disabled", true);
					$('#storage_quota').addClass("readonly");
					$('#storage_quota').val("무제한");
				}
			},

			//폴더 상세조회 수정처리
			submitUpdate : function() {

				if(folderManager.event.validate('form_details')) {

					folderManager.binder.set("type","UPDATE");
					folderManager.binder.set("is_save",exsoft.util.layout.getSelectBox('is_save','option'));
					folderManager.binder.set("folder_status",exsoft.util.layout.getSelectBox('folder_status','option'));
					folderManager.binder.set("is_type",exsoft.util.layout.getSelectBox('is_type','option'));

					// 권한변경 및 공유폴더 여부 Binder 처리
					if($("input:checkbox[id='is_inherit_acl_chk']").is(":checked") == false) {
						folderManager.binder.set("is_inherit_acl","F")
					}else {	folderManager.binder.set("is_inherit_acl","T")	}

					if($("input:checkbox[id='is_share_chk']").is(":checked") == false) {
						folderManager.binder.set("is_share","F")
					}else {	folderManager.binder.set("is_share","T")	}
					
					if($("input:checkbox[id='folder_storage_quota_chk']").is(":checked") == false) {
						folderManager.binder.set("storage_quota",$('#storage_quota').val()*1024*1024*1024);
					} else {
						folderManager.binder.set("storage_quota","-1");
					}
					
					// 하위폴더 권한 일괄변경 체크박스
					if($("input:checkbox[id='is_acl_batch']").is(":checked") == true) {
						folderManager.binder.set("is_acl_batch","on");
					}

					// ServerCall
					exsoft.util.ajax.ajaxDataFunctionWithCallback(folderManager.binder.getDataToJson(),
							exsoft.contextRoot + '/folder/folderControl.do',folderManager.gFolderTree.getCurrentNode(),
						function(data, node) {
							if(data.result == 'true') {
								folderManager.gFolderTree.setNodeText(node.id, folderManager.binder.get("folder_name_ko"));
								folderManager.gFolderTree.selectNodeForInvoke(node.id);
								// 일괄변경 체크박스 초기화
								$("input:checkbox[id='is_acl_batch']").prop("checked", false);
								jAlert(data.message, "폴더관리",8);
							}else {
								jAlert(data.message, "폴더관리",7);
							}
						});
				}
			},

			//폴더 상세조회 취소 버튼
			cancelUpdate : function() {

				exsoft.util.ajax.ajaxDataFunctionWithCallback(folderManager.binder.getDataToJson(),
						exsoft.contextRoot + "/folder/folderDetail.do", "folderDetail", folderManager.callback.showDetail);
			},

			// 폴더 수정 유효성 체크
			validate : function(formId) {

				var obj = $('#'+formId);

				if($.trim(obj.find('input[name=folder_name_ko]').val()) === ''){
					jAlert('폴더명을 입력 하세요.', "폴더관리",6);
					return false;
				}

				if($.trim(obj.find('input[name=sort_index]').val()) === ''){
					jAlert('정렬값을 입력 하세요.', "폴더관리",6);
					return false;
				}

				if($.trim(obj.find('input[name=storage_quota]').val()) === ''){
					jAlert('스토리지 할당량을 입력 하세요.', "폴더관리",6);
					return false;
				}

				return true;

			},

			// 폴더등록 POP CALL
			registFolder : function() {

				$('#doc_folder_list').ddslick('enable');

				var node = folderManager.gFolderTree.getCurrentNode();

				folderWindow.callback = folderManager.callback.refreshTree;
				folderWindow.initForm(node);
				folderWindow.binder.set("type", Constant.ACTION_CREATE);
				folderWindow.open();

			},

			// 폴더이동 버튼 처리
			moveFolder : function() {

				$('#doc_folder_list').ddslick('disable');

				var folder_type = $('#folder_type').val();
				var folder_work_type = exsoft.util.layout.getSelectBox('folder_work_type','option');

				if(folder_type == 'DOCUMENT') {

					if(folder_work_type == Constant.WORK_MYDEPT) {
						map_id = Constant.MAP_MYDEPT;
					} else {
						map_id = Constant.MAP_PROJECT;
					}

					selectSingleFolderWindow.init(folderManager.callback.moveFolderCallback,map_id, folder_work_type,false,"ALL_TYPE");

				} else {
					jAlert('부서 및 프로젝트 Type 폴더는 이동 할 수 없습니다.', "폴더관리", 7);
					return false;
				}

			},

			 // 삭제처리 Proc
			 deleteFolder : function() {

				if($('#folder_type').val() != 'DOCUMENT') {
					jAlert('부서 및 프로젝트 Type 폴더는 삭제 할 수 없습니다.', "폴더관리", 7);
					return false;
				}

				jConfirm("정말 삭제하시겠습니까?", "폴더관리",2, function(ret) {
					if(ret) {

						var jsonData = {
								type : Constant.ACTION_DELETE,
								folder_id : $("#folder_id").val(),
								folder_name_ko : $("#folder_name_ko").val()
							};

							// 폴더삭제처리 서버CALL
							exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonData, exsoft.contextRoot + "/folder/folderControl.do", "folderDelete",
							function(data, param) {
								if(data.result == 'true'){
									folderManager.callback.refreshTree();
									jAlert('폴더 삭제에 성공하였습니다.', "폴더관리",8);
								}else {
									jAlert(data.message, "폴더관리",7);
								}
							});
					}
				});
			},

			// 권한변경버튼 클릭시
			changeAcl : function() {
				selectAclWindow.init($("#acl_id").val(), Constant.ACL.TYPE_FOLDER, folderManager.callback.selectAclSubmit);
				var obj = {
					current_acl_id : $('#acl_id').val(),
					current_acl_name : $('#acl_name_title').text(),
					parent_folder_id : $('#parent_id').val(),
					folder_id : "",
					type : "folder"
				}
				selectAclWindow.initInherit(obj);
			},
			
			//폴더 이력
			showHistory : function(){
				detailfolderWindow.initForm(folderManager.gFolderTree.getCurrentNode(), folderManager.gFolderTree.getCurrentNode().id,"admin");
				detailfolderWindow.open();				
			},
			
			// [2005]
			deleteSubFolders : function() {
				jConfirm("정말 " + $("#folder_name_ko").val() + " 하위폴더들을 삭제하시겠습니까? \n(삭제된 문서는 관리자휴지통으로 이동후 별도의 폐기처리가 없을시, \n설정일수(default:6개월) 이후에 자동 삭제처리 됩니다.)", "폴더관리",2, function(ret) {
					if(ret) {
						folderManager.binder.set("type",Constant.ACTION_SUBFOL_DELETE);

						// 하위폴더 일괄삭제처리 서버CALL
						exsoft.util.ajax.ajaxDataFunctionWithCallback(folderManager.binder.getDataToJson(), exsoft.contextRoot + "/folder/folderControl.do", "folderDelete",
						function(data, param) {
							if(data.result == 'true'){
								folderManager.callback.refreshTree();
								jAlert(data.message, "폴더관리",8);
							}else {
								jAlert(data.message, "폴더관리",7);
							}
						});
					}
				});
			}
		
		},

		//5. 화면 UI 변경 처리
		ui : {
			// 기본권한, 추가권한 출력
			aclItemData :  function(data,divIds) {
				exsoft.util.table.tableFolderAclItemPrintList('aclDocTable',data.aclItemList);
			},

			// 저장문서유형 세팅.
			docTypeData : function(data, param) {

				$("#is_type").remove();

				$('#docType_template').append('<select id="is_type" data-bind="is_type" data-select="true">');
				$.each(data.typeList, function(){
					$("#is_type").append("<option value='{0}'>{1}</option>".format(this.type_id, this.type_name));
				});
				$('#docType_template').append('</select>');
			}
		},

		//6. callback 처리
		callback : {

			// 폴더트리 폴더선택 처리
			selectFolder : function(e, data) {
				exsoft.util.ajax.ajaxDataFunctionWithCallback({folder_id : data.node.id},
						exsoft.contextRoot + "/folder/folderDetail.do", "folderDetail", folderManager.callback.showDetail);
			},

			// 폴더관리 상세정보 조회
			showDetail : function(folderInfo, param) {

				if(folderInfo.result == 'true'){

					var folder = folderInfo.folderDetail;

					$("#folder_name_title").html(folder.folder_name_ko);
					$("#acl_name_title").html(folderInfo.aclDetail.acl_name);
					$("#folder_full_path").text(folderManager.gFolderTree.getCurrentNodeFullPath().join(" > "));
					$("#create_date").text(folder.create_date);
					$("#create_name").text(folder.creator_name);
					$("#storage_usage").text(exsoft.util.common.bytesToSize(folder.storage_usage, 1));
					$('#is_inherit_acl_chk').prop('checked',folder.is_inherit_acl == 'T');		// 권한변경여부
					$('#is_share_chk').prop('checked',folder.is_share == 'T');					// 공유여부

					var node = folderManager.gFolderTree.getCurrentNode();

					/*********************************************************************************************************
					 * 부서폴더/프로젝트폴더 OR 프로젝트 하위 폴더 2 DEPTH 인 경우 스토리지 할당량 변경가능함
					 * 그외 폴더는 스토리지 할당량 변경불가
					 *********************************************************************************************************/
					if( ( folder.map_id == folder.folder_type ) ||
							( folder.folder_type == 'DOCUMENT' && folder.map_id == "PROJECT" && node.parents.length < 3 )) {

						$('#folder_storage_quota_chk').prop("disabled", false);

						if(folder.storage_quota != -1) {
							$('#folder_storage_quota_chk').prop("checked", false);
							$('#storage_quota').val(folder.storage_quota/1024/1024/1024);
							$('#storage_quota').prop("readonly", false);
							$('#storage_quota').prop("disabled", false);
							$('#storage_quota').removeClass("readonly");
						} else {
							$('#folder_storage_quota_chk').prop("checked", true);
							$('#storage_quota').val("무제한");
							$('#storage_quota').prop("readonly", true);
							$('#storage_quota').prop("disabled", true);
							$('#storage_quota').addClass("readonly");
						}

					} else {
						$('#folder_storage_quota_chk').prop("disabled", true);
						$('#folder_storage_quota_chk').prop("checked", true);
						$('#storage_quota').val("무제한");
						$('#storage_quota').prop("readonly", true);
						$('#storage_quota').prop("disabled", true);
						$('#storage_quota').addClass("readonly");
					}

					// DataBinder 설정
					exsoft.util.ajax.ajaxDataFunctionWithCallback('', exsoft.contextRoot+'/folder/makeTypeSelectbox.do', '', function(data, param){
						$.when(folderManager.ui.docTypeData(data, param)).then(exsoft.util.common.ddslick('#is_type', 'srch_type1', 'is_type', 120, function(divId, selectedData){
							folderManager.binder.set("is_type", selectedData.selectedData.value);
						})).done(folderManager.binder.set("is_type", folder.is_type));
					});

					folderManager.binder.set("folder_name_ko", folder.folder_name_ko);
					folderManager.binder.set("folder_name_en", folder.folder_name_en);
					folderManager.binder.set("is_save", folder.is_save);
					folderManager.binder.set("folder_status", folder.folder_status);
					folderManager.binder.set("sort_index", folder.sort_index);
					folderManager.binder.set("is_inherit_acl", folder.is_inherit_acl);
					folderManager.binder.set("is_share", folder.is_share);

					// HIDDEN 값 설정
					folderManager.binder.set("folder_id", folder.folder_id);
					folderManager.binder.set("parent_id", folder.parent_id);
					folderManager.binder.set("map_id", folder.map_id);
					folderManager.binder.set("acl_id", folder.acl_id);
					folderManager.binder.set("folder_type", folder.folder_type);

					folderManager.ui.aclItemData(folderInfo,"aclDocTable");			// 기본권한 :: default - 기본권한 사용안함 권한

				} else {
					jAlert(data.message, "폴더관리", 0);
				}
			},

			// 트리 새로고침
			refreshTree : function (e, data) {
				folderManager.gFolderTree.refresh();
			},

			// 권한변경이후 콜백
			selectAclSubmit : function(aclItemList) {
				folderManager.binder.set("acl_id", aclItemList.aclId);
				$("#acl_name_title").html(aclItemList.aclDetail.acl_name);
				exsoft.util.table.tableFolderAclItemPrintList('aclDocTable',aclItemList.aclItems);
			},

			// 폴더트리에서 이동 버튼 callback 함수
			moveFolderCallback : function(returnFolder) {

				if (returnFolder != null) {
					var isLoop = false;

					// 이동하려는 대상 폴더가 자신 및 자신의 하위 폴더인지 체크함
					var current_folder_id = $("#folder_id").val();
					$(returnFolder.parentIdList).each(function(index) {
						if (this == current_folder_id ) {
							jAlert("현재 폴더 및 현재 폴더 하위로 이동할 수 없습니다.", "폴더관리", 8);
							isLoop = true;
							return false;
						}
					});

					var current_folder_object = folderManager.gFolderTree;
					var originalRootFolder = current_folder_object.getFolderGroupId(current_folder_object.selectedNode[0]);
					var targetRootFolder;
					var changeRootFolder = 'F';

					if(returnFolder.mapId == "PROJECT" && returnFolder.parentGroup == null){
						targetRootFolder = returnFolder.id;
					} else {
						targetRootFolder = returnFolder.parentGroup.id;
					}

					if(originalRootFolder.id != targetRootFolder) {
						changeRootFolder = 'T';
					}

					if (!isLoop) {

						$("#move_folder_id").val(current_folder_id);  										// 변경할 현재 ID 저장
						$("#move_parent_id").val(returnFolder.id); 										// 선택된 부모 ID 저장
						$("#move_map_id").val(returnFolder.mapId); 									// 선택된 맵 ID 저장
						$("#move_folder_name_ko").val($("#folder_name_ko").val());			 	 // 변경할 폴더 이름
						$("#parentGroup_id").val(targetRootFolder);
						$("#root_folder_change").val(changeRootFolder);

						exsoft.util.ajax.ajaxFunctionWithCallback('form_move', exsoft.contextRoot + '/folder/folderControl.do','folderUpdate',
						function(data, param) {
							if(data.result == 'true'){
								folderManager.gFolderTree.refreshNodeForAddChildren(data.refresh_id);
								folderManager.gFolderTree.refreshNodeForAddChildren(data.target_refresh_id);
								jAlert(data.message, "폴더관리", 8);
							}else {
								jAlert(data.message, "폴더관리", 7);
							}
						});
					}
				}
			}
		},
}