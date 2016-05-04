/**
 * [3000][기능추가]	2015-09-18	성예나	 : 데이터 업로드중 로딩바 처리
 * [3001][기능추가]	2015-09-21	성예나	 : 문서등록시 용량초과alert이 뜨면 문서등록창 close 기능추가
 * 
 * */
var externalFunc = {
		
	binder : null,
		
	// 0. 초기화
	init : {
		// 폴더트리초기화
		folderWindowInit : function() {
			selectSingleFolderWindow.init(externalFunc.callback.selectNodeCallback, Constant.MAP_MYDEPT, Constant.WORK_MYDEPT, true);
		},
		
		// 문서등록초기화
		docWriteInit : function(folder_id,folder_path,calledpage,isLeft){
			
			exsoft.document.wRefDoc = 0;
			exsoft.document.wFileUploadJsonArr = new Array(); // 업로드 파일 목록
    		exsoft.document.deleteFileList = new Array();  // 삭제대상 파일 목록
    		
    		$(".doc_auth_cnts").addClass('hide');
    		$(".doc_relative_cnts").addClass('hide');
			
			// 최근 등록 문서 목록
			exsoft.document.event.getRecentlyDocumentList(isLeft);
			
			//개인함이나 업무문서함에서 leftTop에있는 문서등록을 할 경우 폴더경로 안나오게수정
			if(folder_id != "" && folder_id != null && isLeft == "true"){
				
				var jsonObject = { "folder_id":folder_id,"folder_path":folder_path};

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject,exsoft.contextRoot+"/document/docCommonRegist.do", "docRegistForm",

					function(data,e) {

					
						if(data.result == "true"){

			        		exsoft.document.exAclItems = [];

							// 2.문서등록 폼 초기화 처리

							exsoft.util.common.formClear('documentWrite');
							
							exsoft.document.commDocBinder = new DataBinder("#documentWrite");
							exsoft.document.commDocBinder.set("actionType", Constant.ACTION_CREATE);
							exsoft.document.actionType = 'C';

			        		$("#documentWrite").validation.clearShadowBox();
			        		exsoft.common.file.init.initSettings('documentfileuploader', exsoft.document.callback.fileupload);

							$(".attach_cnt").html('');
							$(".attach_size").html('');
							$("#multiFolder").empty();

							$("#wAclName").html("");
							$(".relative_docs_cnt").html(""); //관련문서 건수 세팅

			        		exsoft.document.commDocBinder.set("isType","insert");
							exsoft.document.commDocBinder.set("version_type","NEW"); //버전타입

			        		$('#iframe_editor').attr("src",exsoft.contextRoot+"/editor/doc_description.jsp");		// 다음에디터 리로드 및 내용초기화
			        		$("#temp_content").html('');

			        		exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_acl', null);
			        		exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_extAcl', null);
			        		$('#RefDocTable').empty();

							exsoft.document.init.initDdslick();

			        		exsoft.util.layout.divLayerOpen('doc_register_wrapper','doc_register',true);


							// 3. 공통처리 - 기본문서유형선택 및 map_id
							// 기본폴더ID/MAP_ID :: 폴더선택 화면에서 변경시 폴더ID는 변경됨
							exsoft.document.commDocBinder.set("folder_id",folder_id);
							exsoft.document.commDocBinder.set("folder_path",folder_path);
							exsoft.document.commDocBinder.set("map_id",data.folderVO.map_id);
							
							// 4. 문서유형 선택유무 제어
							exsoft.document.commDocBinder.set("doc_type",data.defaultType);
							
							if(data.isChangeType == "FALSE") {
								$('#register_docType').ddslick('disable');
								exsoft.document.commDocBinder.set("isChangeType","FALSE");
							}else {
								$('#register_docType').ddslick('enable');
								exsoft.document.commDocBinder.set("isChangeType","TRUE");
							}
							exsoft.document.commDocBinder.set("folderIsType",data.is_type);

							// 확장문서유형
							exsoft.document.event.setExtendTypeAttrItem(data.defaultType);
							if(data.folderVO.map_id == "MYPAGE")	{
								// 보존년한 영구 기본선택
								$("#documentWrit select[name=preservation_year]").val(0);

								//보안등급, 조회등급
								$("#sercurityView").addClass('hide');
								$("#inheritacl").addClass('hide'); //권한유지
								$("#isShareView").addClass('hide'); //문서공유
								$("#classificationlist").addClass('hide');//다차원분류
								$("#keyWord").addClass('hide');//키워드
								$(".doc_auth").addClass('hide');// 권한
								$(".doc_relative").addClass('hide'); //관련문서

							}else {
								
								// 권한 :: 권한속성 아이템
								exsoft.document.commDocBinder.set("acl_id", data.defaultAcl);
								$("#wAclName").html(data.aclDetail.acl_name);
								exsoft.document.commDocBinder.set("acl_id", data.aclDetail.acl_id);
								// is_share :: 문서공유여부
								documentWrite.is_share.value = exsoft.document.util.getFormCheckBoxValue('documentWrite','is_share_chk','TF');

								exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_acl',data.aclItemList);
								exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_extAcl',data.aclExItemList);

								// 보존년한 5년 기본선택
								$("#documentWrite select[name=preservation_year]").val(5);
							}
							

						}else {
							jAlert(data.message,'문서등록',6);
						}
				});

			}else{

				exsoft.util.common.formClear('documentWrite');

				exsoft.document.commDocBinder = new DataBinder("#documentWrite");
				exsoft.document.commDocBinder.set("actionType", Constant.ACTION_CREATE);
				exsoft.document.actionType = 'C';

        		$("#documentWrite").validation.clearShadowBox();
        		exsoft.common.file.init.initSettings('documentfileuploader', exsoft.document.callback.fileupload);

        		exsoft.document.init.initFDdslick();
        		
				$(".attach_cnt").html('');
				$(".attach_size").html('');
				$("#multiFolder").empty();

				$("#wAclName").html("권한");

				$(".relative_docs_cnt").html(""); //관련문서 건수 세팅

				exsoft.document.commDocBinder.set("isType","insert");
				exsoft.document.commDocBinder.set("version_type","NEW"); //버전타입
				$('#iframe_editor').attr("src",exsoft.contextRoot+"/editor/doc_description.jsp");		// 다음에디터 리로드 및 내용초기화
    			$("#temp_content").html('');

    			exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_acl', null);
    			exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_extAcl', null);
    			$('#RefDocTable').empty();
    			
    			exsoft.document.commDocBinder.set("preservation_year", 0);
    			
				if(calledpage=="myWorkMenu"){
					exsoft.document.commDocBinder.set("map_id","MYPAGE");
					//보안등급, 조회등급
					$("#sercurityView").addClass('hide');
					$("#inheritacl").addClass('hide'); //권한유지
					$("#isShareView").addClass('hide'); //문서공유
					$("#classificationlist").addClass('hide');//다차원분류
					$("#keyWord").addClass('hide');//키워드
					$(".doc_auth").addClass('hide');// 권한
					$(".doc_relative").addClass('hide'); //관련문서
				}else{
    				//보안등급, 조회등급
					$("#sercurityView").removeClass('hide');
					$("#inheritacl").removeClass('hide'); //권한유지
					$("#isShareView").removeClass('hide'); //문서공유
					$("#classificationlist").removeClass('hide');//다차원분류
					$("#keyWord").removeClass('hide');//키워드
					$(".doc_auth").removeClass('hide');// 권한
					$(".doc_relative").removeClass('hide'); //관련문서
					
					exsoft.document.commDocBinder.set("map_id","MYDEPT");
    				exsoft.document.commDocBinder.set("access_grade", "P001");
				}
        		//exsoft.util.layout.divLayerOpen('doc_register_wrapper','doc_register',true);
			}
    	},
    	
    	// folder_id가 없을때 ddslick초기화
    	initFDdslick : function(){
			//검색 selectbox
			exsoft.util.common.ddslick('#register_docType', 'srch_type1', 'doc_type', 120, function(divId, selectedData){
			
				exsoft.document.commDocBinder.set("doc_type", selectedData.selectedData.value);
				exsoft.document.event.setExtendTypeAttrItem(selectedData.selectedData.value);
			});
			
			// 보존년한 selectbox
			exsoft.util.common.ddslick('#register_preservationyear', 'srch_type1', 'preservation_year', 70, function(){});
			// 조회등급 selectbox
			exsoft.util.common.ddslick('#register_accessgrade', 'srch_type1', 'access_grade', 100, function(){});

		},
		
		// folder_id가 있을때 ddslick초기화
    	initDdslick : function(){
			//검색 selectbox
			exsoft.util.common.ddslick('#register_docType', 'srch_type1', 'doc_type', 120, function(divId, selectedData){
			
				exsoft.document.commDocBinder.set("doc_type", selectedData.selectedData.value);
				//문서유형에 맞는 확장 속성을 표시 한다.
				// 1.폴더에 문서등록 권한이 있는지 체크한다.
				exsoft.document.event.setExtendTypeAttrItem(selectedData.selectedData.value);
			});
			// 보존년한 selectbox
			exsoft.util.common.ddslick('#register_preservationyear', 'srch_type1', 'preservation_year', 70, function(divId, selectedData){

				exsoft.document.commDocBinder.set("preservation_year", selectedData.selectedData.value);
			});
			// 조회등급 selectbox
			exsoft.util.common.ddslick('#register_accessgrade', 'srch_type1', 'access_grade', 100, function(divId, selectedData){
				exsoft.document.commDocBinder.set("access_grade", selectedData.selectedData.value);
			});

		},
		
		// 문서등록 - 권한변경팝업
		aclWindowInit : function(acl_id, folder_id, parent_folder_id, current_acl_name) {
			var obj = {
        			type : "document",
        			folder_id : folder_id,
        			parent_folder_id : parent_folder_id,
        			current_acl_id : acl_id,
					current_acl_name : current_acl_name,
        	};
			selectAclWindow.initDocument(acl_id, Constant.ACL.TYPE_DOC, exsoft.document.exAclItems,exsoft.document.callback.selectAcl);
			selectAclWindow.initInherit(obj);
		},
		
		accessorWindowInit : function() {
			// 접근자 추가 윈도우 팝업
			selectAccessorWindow.init(parent.externalFunc.callback.addAccessor, "selAclWindowDocExtAclItemList", selectAclWindow.type);
		}
		
	},

	// 1. 팝업
	open : {
		// 폴더선택팝업
		treePop : function(user_id) {
			var frm = document.treePopCall;
			window.open('', "treePopCall", 'width=360,height=500,top=0,left=0,scrollbars=no,status=no,toolbar=no,menubar=no,location=no,resizable=yes');
			frm.action = exsoft.contextRoot + "/external/externalTreeCall.do";
			frm.method = "post";
			frm.target = "treePopCall";
			frm.emp_no.value = "user013";
			frm.submit();
		},
		
		// 문서등록팝업
		registDocPop : function(selectedNode, user_id) {
			var frm = document.registDocCall;
			window.open('', "registDocCall", 'width=1100,height=750,top=0,left=0,scrollbars=no,status=no,toolbar=no,menubar=no,location=no,resizable=yes');
			frm.action = exsoft.contextRoot + "/external/externalRegistDocCall.do";
			frm.method = "post";
			frm.target = "registDocCall";
			frm.emp_no.value = "user013";
			if(selectedNode != null) {
				frm.folder_id.value = selectedNode.id;
				frm.folder_path.value = selectedNode.full_path.join("/");
				frm.calledpage.value = selectedNode.mapId;
				frm.isLeft.value = true;
			}
			frm.submit();
		},
		
		// 다차원분류 팝업
		multiFolderFind : function() {
			var doc_type = exsoft.document.commDocBinder.get("folderIsType");
			var map_id = exsoft.document.commDocBinder.get("map_id");
			selectMultiFolderWindow.init(exsoft.document.event.registDocMultiFolderFind, map_id, "WORK_MYDEPT", true, doc_type, "external");
		},
		
		// 권한변경 팝업
		changeAcl : function() {
			var obj = {
					current_acl_id : "",
					current_acl_name : "",
					parent_folder_id : "",
					folder_id : "",
					type : "document"
				};
    		//문서 등록
			obj.current_acl_id = exsoft.document.commDocBinder.get("acl_id");
    		if(obj.current_acl_id == null || obj.current_acl_id == ''){
    			jAlert('기본폴더 먼저 선택하세요.','폴더선택',6);
    			return false;
    		}else{
    			selectAclWindow.initDocument(obj.current_acl_id, Constant.ACL.TYPE_DOC, exsoft.document.exAclItems,exsoft.document.callback.selectAcl, "external");
    		}

    		obj.folder_id = exsoft.document.commDocBinder.get("folder_id");
			obj.current_acl_name = $("#wAclName").html();
    		
    		selectAclWindow.initInherit(obj);
		},
		
		// 접근자 추가 팝업
		addAccessor : function() {
			selectAccessorWindow.init(selectAclWindow.callbackFunctions.addAccessor, "selAclWindowDocExtAclItemList", selectAclWindow.type, "external");
		},
		
	},

	//2. layer + show
	layer : {
	},

	//3. 닫기 + hide
	close : {
	},

	//4. 화면 이벤트 처리
	event : {
		// 기본폴더 선택
		folderFind : function() {
			selectSingleFolderWindow.init(externalFunc.callback.folderFind, Constant.MAP_MYDEPT, Constant.WORK_MYDEPT, true, "", "external");
		},
		
		//문서등록 처리
		documentSubmit : function(){

			if ($("#documentWrite").validation()) {
				 // jsonMultiFolders :: 다차원 분류 리스트 : multi_folder
			    //var jsonMultiFolderArr = exsoft.document.event.getMultiFolderList('documentWrite','multi_folder');
				//objForm.jsonMultiFolders.value = JSON.stringify(jsonMultiFolderArr);
    			/**********************************************************************
        		// fileCounter :: 업로드 영역에 있는 파일 수
        		// 1 : 첨부파일 없는 문서등록 처리
        		// 2이상 : 첨부파일 업로드 후 문서등록 처리
        		// upCounter :: 대상 파일 중 업로드 성공한 파일 수
        		**********************************************************************/
				// page_cnt :: 첨부파일수
				var objForm = document.documentWrite;
				objForm.page_cnt.value = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;


    			if(exsoft.common.file.prototype.wUploadObj.fileCounter == 1 ||
    					(exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{

    				exsoft.document.commDocBinder.set("doc_name",document.documentWrite.doc_name.value); //버전타입
            		var jsonObject = exsoft.document.commDocBinder.getDataToJson();
            		jsonObject.fileList = JSON.stringify(exsoft.document.wFileUploadJsonArr);
            		jsonObject.page_cnt = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;
            		jsonObject.security_level = $("#documentWrite input[name=security_level]:radio:checked").val();
            		jsonObject.acl_id = $("#documentWrite input[name=acl_id]").val();
            		jsonObject.keyword = $("#documentWrite input[name=keyword]").val();

            		// doc_description :: 문서설명
            		jsonObject.doc_description = exsoft.util.common.editorContent();

            		//$("#documentWrite select[name=doc_type]").prop("disabled",false);
            		$('#register_docType').ddslick('enable');

            		// 업무문서함에 경우
            		if($("#documentWrite input[name=map_id]").val() != "MYPAGE"){
            			// is_inherit_acl :: 상위권한 상속여부
                		jsonObject.is_inherit_acl = exsoft.document.util.getFormCheckBoxValue('documentWrite','is_inherit_acl_chk','TF');
                		//공유받은 문서
                		jsonObject.is_share = exsoft.document.util.getFormCheckBoxValue('documentWrite','is_share_chk','TF');
                		// 추가 접근자 목록
                		jsonObject.aclExItem_list = JSON.stringify(exsoft.document.exAclItems);  // json array 넘김

                		// jsonRefList :: 관련문서 리스트 refDocIdx
                	    var jsonRefArr =  exsoft.document.util.getRefDocList('wRefDocIdx');
                	    jsonObject.jsonRefList = JSON.stringify(jsonRefArr);

                	    // jsonMultiFolders :: 다차원 분류 리스트 : multi_folder
                	    var jsonMultiFolderArr = exsoft.document.util.getMultiFolderList('documentWrite','multi_folder');
                	    jsonObject.jsonMultiFolders = JSON.stringify(jsonMultiFolderArr);
            		}


    				// 등록처리
            		exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject ,exsoft.contextRoot + '/document/docSubmit.do',null,function(data, param){
            			if(data.result == "true")	{
            				jAlert("문서가 등록되었습니다", "문서등록", 0, function(ret) {
            					if(ret) {
            						window.close();
            					}
            				});

            			}else {
            				jAlert(data.message,"문서등록",7);
            				exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register');	//[3001]
            			}
    				});
    			}else {
    				// 대상 파일을 업로드 한다.
    				exsoft.util.ajax.loadingShow();		// [3000]
    				$("#loading_message").show();
					exsoft.common.file.prototype.wUploadObj.startUpload();
					jAlert("문서가 등록되었습니다", "문서등록", 0, function(ret) {
    					if(ret) {
    						window.close();
    					}
    				});
    			}

    		} 

		},
	},

	//5. 화면 UI 변경 처리
	ui : {
	},

	//6. callback 처리
	callback : {
		selectNodeCallback : function(selectedNode) {
			// 폴더선택창에서 확인버튼 클릭시 창이 닫히면서 문서등록창이 팝업됨
			externalFunc.open.registDocPop(selectedNode);
			window.close();
		},
		
		// 기본폴더 set
    	folderFind : function(nodeInfo) {
    		exsoft.document.commDocBinder.set("folder_path", nodeInfo.full_path.join("/"));
    		exsoft.document.commDocBinder.set("folder_id", nodeInfo.id);
    		//exsoft.document.commDocBinder.set("map_id", nodeInfo.map_id);
    		exsoft.document.commDocBinder.set("acl_id", nodeInfo.original.acl_id);
    		exsoft.document.exAclItems = nodeInfo.exAclItems;
    		
    		if(nodeInfo.original.is_type == 'ALL_TYPE'){
    			$('#register_docType').ddslick('enable');
        		exsoft.document.commDocBinder.set("folderIsType",nodeInfo.original.is_type);
    		}else{
    			$('#register_docType').ddslick('disable');
    			exsoft.document.commDocBinder.set("doc_type", nodeInfo.original.is_type);

				//문서유형에 맞는 확장 속성을 표시 한다.
    			exsoft.document.event.setExtendTypeAttrItem(nodeInfo.original.is_type);
        		exsoft.document.commDocBinder.set("folderIsType",nodeInfo.original.is_type);
    		}
    		// 권한 셋팅
    		exsoft.document.event.setAclItem(nodeInfo.original.acl_id);

		},
		
		// 접근자 추가 선택 윈도우 콜백
    	addAccessor : function(extAclList) {
    		console.info("addAccessor");
    		console.info(extAclList);
    		exsoft.util.grid.gridNoDataMsgInit('selAclWindowDocExtAclItemList');	// 데이터없음 초기화
    		selectAclWindow.selAclInfo.extAclItems = extAclList;
    		exsoft.util.grid.gridSetAclItemAddCallback('selAclWindowDocExtAclItemList', extAclList);
    		//window.close();
    	},
	}
}