/**
 * 협업 등록/수정 JavaScript
 * [3000][기능추가]	2015-09-18	성예나	 : 데이터 업로드중 로딩바 처리
 * [3001][기능추가]	2015-09-21	성예나	 : 문서등록시 용량초과alert이 뜨면 문서등록창 close 기능추가
 */
var exsoftProcessWrite = {
		binder : new DataBinder("#processWrite"),
		coworkList : {},
		wFileUploadJsonArr : new Array(),
		actionType : "C",
		defaultDocType : null,
		exAclItems : new Array(),
		
		// 협업 수정 시 첨부파일 관련
		deleteFileList : new Array(),

		// 0. 초기화
        init : {
        	// 협업 등록
        	initProcessWrite : function(){
        		exsoftProcessWrite.binder.set("actionType", Constant.ACTION_CREATE);
        		exsoftProcessWrite.binder.set("process_id", '');
        		exsoftProcessWrite.binder.set("doc_root_id", '');
        		exsoftProcessWrite.actionType = 'C'
        			
        		// 파일 플러그인 관련
        		exsoft.common.file.init.initSettings('processfileuploader', exsoftProcessWrite.callback.fileupload);
        		$('.coop_detailView_attach').addClass('hide');
        		$('#processWrite_attachFileList').empty();
        		exsoftProcessWrite.wFileUploadJsonArr = new Array(); //// 업로드 파일 목록
        		
        		
        		// title 변경
            	$('.coop_register_title').children('span').text('업무 등록');
        		$('.coop_register_btnGrp button:nth-child(1)').children('span').text('등록');
        		
        		// 문서유형 select-box
        		exsoftProcessWrite.init.initDdslick();
        		
        		// 권한명 set
    			$('#processWriteAclName').text('권한');
        		
        		$("#processWrite").validation.clearShadowBox();
        		exsoft.util.table.tableDocumentAclItemPrintList('processWrite_acl', null);
        		exsoft.util.table.tableDocumentAclItemPrintList('processWrite_extAcl', null);

        		exsoftProcessWrite.coworkList = {};
        		exsoftProcessWrite.exAclItems = new Array();
        		
        		//최근 업무 목록 표시
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(null,exsoft.contextRoot + '/process/processRecentlyList.do',null,function(data, param){
        			var $ul = $('.coop_recent_wrap ul');
        			$ul.empty();
        			$(data.list).each(function(idx){
        				var name = this.name.length > 15 ? this.name.substr(0,15)+'...' : this.name;
        				var strHtml = '';
        				strHtml += '<li id="'+this.recently_id+'">';
        				strHtml += '<a href="javascript:exsoftProcessWrite.event.setInfoByRecent(\''+this.process_id+'\');">'+name+'</a>'
        				strHtml += '<a href="javascript:exsoftProcessWrite.event.deleteRecentRow(\''+this.recently_id+'\');" class="coop_recent_del"><img src="'+exsoft.contextRoot+'/img/icon/recent_doc_del.png"></a>'
        				strHtml += '</li>';
        				
        				$ul.append(strHtml);
        			});
        		});
        		
        		// 초기화 작업
        		exsoftProcessWrite.binder.set("requestorName", exsoft.user.user_name);
        		exsoftProcessWrite.binder.set("requestorId", exsoft.user.user_id);
        		exsoftProcessWrite.binder.set("actionType", Constant.ACTION_CREATE);
        		
        		// 문서유형 기본값으로 설정 한다.
        		if(exsoftProcessWrite.defaultDocType != null){
        			exsoftProcessWrite.doFunction.setExtendTypeAttrItem(exsoftProcessWrite.defaultDocType);
        			exsoftProcessWrite.binder.set("doc_type", exsoftProcessWrite.defaultDocType);
        			$('#processWrite_docType').ddslick('enable');  // 문서유형 selectbox 선택 가능하게 변경
        		}
        		
        		exsoftProcessWrite.binder.bindingElement(); // data-bind 전체 bind
        	},
        	
        	// 협업 수정
        	initProcessModify : function(process_id, doc_root_id){
        		//웹 소켓 연결
        		exsoft.util.websocket.connect(exsoft.user.user_id, null);
        		
        		exsoftProcessWrite.binder.set("actionType", Constant.ACTION_UPDATE);
        		exsoftProcessWrite.binder.set("process_id", process_id);
        		exsoftProcessWrite.binder.set("doc_root_id", doc_root_id);
        		exsoftProcessWrite.actionType = 'U';
        		//alert(process_id);
        		
        		// 파일 플러그인 관련
        		exsoft.common.file.init.initSettings('processfileuploader', exsoftProcessWrite.callback.fileupload);
        		$('.coop_detailView_attach').removeClass('hide');
        		$('#processWrite_attachFileList').empty();
        		exsoftProcessWrite.wFileUploadJsonArr = new Array(); // 업로드 파일 목록
        		exsoftProcessWrite.deleteFileList = new Array();  // 삭제대상 파일 목록
        		
        		// title 변경
        		$('.coop_register_title').children('span').text('업무 수정');
        		$('.coop_register_btnGrp :button:nth-child(1)').children('span').text('수정');
        		
        		exsoftProcessWrite.exAclItems = new Array();
        		exsoftProcessWrite.event.setInfoByRecent(process_id, true);
        		
        		// 문서유형, 파일추가
        		$('#processWrite_docAttrView tbody').empty();
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:doc_root_id}, exsoft.contextRoot+"/document/documentDetail.do", '#processWrite_docAttrView', function(data, tableId) {
        			// ====== 확장문서 속성
        			var docVO = data.documentVO;
        			exsoftProcessWrite.defaultDocType = docVO.doc_type;
                	exsoftProcessWrite.binder.set("doc_type", exsoftProcessWrite.defaultDocType);
        			
        			exsoft.util.table.tableExtendTypeItemPrintList(tableId, data.attrList, "U");
        			if(data.attrList != undefined && data.attrList != 0){
						 $(tableId).removeClass('hide');
						 exsoftProcessWrite.binder.set("is_extended", 'T');
						 
						// table에 select box가 존재하면 ddslick을 해준다.
	    				var $extendType = $(tableId + ' tbody').find('input, select');
	    				$($extendType).each(function(idx){
	    					var name = $(this).attr('name');
	    					
	    					if($(this).is('select')){
	    						$(this).attr('id', name);
	    						$(this).attr('data-select', 'true');
	    						exsoft.util.common.ddslick(name,'srch_type1',name,120, function(divId, selectedData){
	    							exsoftProcessWrite.binder.set(name, selectedData.selectedData.value);
	    						});
	    					}else{
	    						$(this).attr('data-bind', name);
	    					}		    					
	    				});
	    				exsoftProcessWrite.binder.bindingElement(); // data-bind 전체 bind
						 
					 }else{
						 $(tableId).addClass('hide');
						 exsoftProcessWrite.binder.set("is_extended", 'F');
					 }
        			
//        			// ======= 파일 카운트 처리 data.pageList.length
        			var pageCnt = data.pageList != undefined ? data.pageList.length : 0;
        			$('.approvalRequest_attach_cnt').text(pageCnt);
        			exsoft.util.table.printPageList('processWrite_attachFileList', data, 'exsoftProcessWrite.callback.pageCallback', false, true); // callbackname으로 넘긴다.
        			
        			// 기존 파일 목록을 플러그인에 담는다 :: 중복파일 금지
        			var totalPageSize = 0;
        			$(data.pageList).each(function(idx){
        				exsoft.common.file.prototype.wUploadObj.dbFileNames.push(this.page_name);
        				totalPageSize += this.page_size;
        			});
        			
        			// 파일 플러그인 설정값 변경처리			
        			exsoft.common.file.prototype.wUploadObj.updateMaxFileCounter(exsoft.common.file.prototype.wUploadObj.MaxFileCounter()-pageCnt);
    				exsoft.common.file.prototype.wUploadObj.updateMaxFileSize(exsoft.common.file.prototype.wUploadObj.MaxFileSize()-totalPageSize);

    			});	
        		
        		// 문서유형 select-box  exsoftProcessWrite.defaultDocType
        		exsoftProcessWrite.init.initDdslick('disable');  

        	},  // initProcessModify end...
        	
        	/**
    		 * select box :: ddslick 사용
    		 */    		
    		initDdslick : function(type){
    			//검색 selectbox		
    			exsoft.util.common.ddslick('#processWrite_docType', 'srch_type1', 'doc_type', 120, function(divId, selectedData){
    				exsoftProcessWrite.binder.set("doc_type", selectedData.selectedData.value);
    				// 최초 default값 set :: 용도는 등록창 닫고 새로 열었을 경우 문서유형 기본값으로 변경
    				if(exsoftProcessWrite.defaultDocType == null){
    					exsoftProcessWrite.defaultDocType = selectedData.selectedData.value;
    				}
    				//문서유형에 맞는 확장 속성을 표시 한다.
    				exsoftProcessWrite.doFunction.setExtendTypeAttrItem(selectedData.selectedData.value);
    				
    			});	// 문서유형 selectbox
    			
    			if(type != undefined || type === 'disable'){
    				$('#processWrite_docType').ddslick('disable');
    			}else{
    				$('#processWrite_docType').ddslick('enable');
    			}
    				
    		}, // initDdslick end...
        },
        
        // 1. 팝업
        open : {
        	// 협업자 팝업
        	processCoworkWindow : function(){
        		exsoftProcessCoworkWindow.init.initProcessCoworkWindow(exsoftProcessWrite.coworkList, exsoftProcessWrite.callback.processCoworkWindow);
        	},
        	
        	// 기본폴더 선택
			selectFolderWindow : function() {
				selectSingleFolderWindow.init(exsoftProcessWrite.callback.selectFolderWindow,Constant.MAP_MYDEPT, Constant.WORK_MYDEPT, true, "ALL_TYPE");
			},

			// 권한 선택
			// 문서 권한 변경
        	chagneDocumentAcl : function() {
        		var acl_id = exsoftProcessWrite.binder.get("acl_id");
        		
        		if(acl_id == null || acl_id == ''){
        			jAlert('기본폴더 먼저 선택하세요.','폴더',1);
        		}else{
        			// 기본권한 set
        			selectAclWindow.initDocument(acl_id, Constant.ACL.TYPE_DOC, exsoftProcessWrite.exAclItems,exsoftProcessWrite.callback.selectAcl);
        			
        			var obj = {
        					current_acl_id : acl_id,
        					current_acl_name : $('#processWriteAclName').text(),
        					parent_folder_id : "",
        					folder_id : exsoftProcessWrite.binder.get("folder_id"),
        					type : "document"
        				};
        			
        			// 추가접근자 set
        			selectAclWindow.initInherit(obj);
        		}
        	},
        },
        
        //2. layer + show
        layer : {
        	
        },
        
        //3. 닫기 + hide
        close : {
        	layerClose : function(isFileDelete){
        		isFileDelete = (isFileDelete == undefined || isFileDelete == null) ? false : isFileDelete;
        		if(isFileDelete){
        			// exRep ECM에 등록된 물리적 파일 삭제 대상으로 등록
        			exsoftProcessWrite.doFunction.deleteUploadFile()
        		}
        		
        		// fileupload plug-in 파일 초기화 :: 서버에 올라 등록되지 않은 첨부파일을 clear();
        		exsoft.common.file.prototype.wUploadObj.cancelAll();
        		exsoft.util.layout.divLayerClose('coop_register_wrapper','coop_register');
        	},
        },
        
        //4. 화면 이벤트 처리
        event : {
        	// 최근 업무 등록  목록에서 선택 시 기본 정보 set
        	setInfoByRecent : function(process_id, isModify){
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({process_id : process_id},exsoft.contextRoot + '/process/selectProcessRecently.do',null,function(data, param){
        			if(data.result == 'true'){
        				// 업무명
        				exsoftProcessWrite.binder.set("name", data.processName);
        				
        				// 기본폴더
        				exsoftProcessWrite.binder.set("full_path", data.full_path);
                		exsoftProcessWrite.binder.set("folder_id", data.folder_id);
                		exsoftProcessWrite.binder.set("map_id", data.map_id);
                		exsoftProcessWrite.binder.set("acl_id", data.acl_id);
                		
                		if(isModify){  //processInfo
                			exsoftProcessWrite.binder.set("requestorName", data.processInfo.creator_name);
                    		exsoftProcessWrite.binder.set("expect_date", data.processInfo.expect_date);
                    		exsoftProcessWrite.binder.set("content", data.processInfo.content);
                		}
                		
                		// 권한 셋팅
                		exsoftProcessWrite.doFunction.setAclItem(data.acl_id);
                		
                		// 추가 접근자 셋팅
                		exsoftProcessWrite.doFunction.setExtAclItem(data.processInfo.doc_root_id);
                		
        				
        				// 협업자 set
        				var coworkerObj = {};        				
        				coworkerObj.authorList = new Array();
        				coworkerObj.coauthorList = new Array();
        				coworkerObj.approverList = new Array();
        				coworkerObj.receiverList = new Array();
        				
        				$(data.list).each(function(idx){
        					switch (this.type) {
							case Constant.PROCESS.TYPE_AUTHOR:coworkerObj.authorList.push(this);break;
							case Constant.PROCESS.TYPE_COAUTHOR:
								this.sort_index -= 1; // 공동 작성자는 server에서 sort_index값을 1씩 증가하기에 여기서는 1씩 감소
								coworkerObj.coauthorList.push(this);
								break;
							case Constant.PROCESS.TYPE_APPROVER:coworkerObj.approverList.push(this);break;
							case Constant.PROCESS.TYPE_RECEIVER:coworkerObj.receiverList.push(this);break;
							default:break;
							}
        				});
        				exsoftProcessWrite.callback.processCoworkWindow(coworkerObj);
        			}else{
        				jAlert(data.message,'확인',7);
        			}
        		});
        	},
        	
        	//파일 전체 선택, 전체 해제
        	attachToggleCheck : function(type){
        		exsoft.common.file.event.attachToggleCheck('processWrite_attachFileList', type);
        	},
        	
        	//파일저장
        	attachSave : function(){
        		exsoft.common.file.event.attachSave('processWrite_attachFileList', exsoftProcessViewFunc.doc_root_id);
        	},
        	
        	//파일삭제
        	attachDelete : function(){
        		var $chkObject = $("input[name=processWrite_attachFileList_checkbox]:checkbox:checked");
        		// page_id|this.page_size|this.page_name
        		if($chkObject.length > 0){
        			$($chkObject).each(function(idx){
        				
        				exsoftProcessWrite.doFunction.setDeleteFilePlugin($(this).val());
        				$(this).parent().remove();
        				
        			});
        			
        		}else{
        			jAlert('삭제할 파일을 선택하세요.','삭제',0);
        		}
        	},
        	
        	// 최근 업무 등록 목록 삭제
        	deleteRecentRow : function(recently_id){
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({recently_id : recently_id},exsoft.contextRoot + '/common/deleteRecently.do',null,function(data, param){
        			if(data.result == 'true'){
        				$('.coop_recent_wrap ul').find(exsoft.util.common.getIdFormat(recently_id)).remove();
        			}else{
        				jAlert(data.message,'확인',7);
        			}
        		});
        	},
        	
        	// 등록/수정 버튼  :: exsoftProcessWrite.actionType
        	submit : function(){
        		
        		if(exsoftProcessWrite.actionType == 'C'){
        			exsoftProcessWrite.event.submitCreate();
        		}else if(exsoftProcessWrite.actionType == 'U'){
        			exsoftProcessWrite.event.submitUpdate();
        		}else{
        			jAlert('작업 Type값이 올바르지 않습니다.','확인',6);
        		}
        		
        	},
        	
        	// 업무(협업) 등록
            submitCreate : function(){
            	if ($("#processWrite").validation()) {
        			/**********************************************************************
            		// fileCounter :: 업로드 영역에 있는 파일 수
            		// 1 : 첨부파일 없는 문서등록 처리
            		// 2이상 : 첨부파일 업로드 후 문서등록 처리
            		// upCounter :: 대상 파일 중 업로드 성공한 파일 수
            		**********************************************************************/
        			if(exsoft.common.file.prototype.wUploadObj.fileCounter == 1 ||
        					(exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{
        			     				
        				// 1. 협업자 정보를 jsonArray에 담아서 서버 호출 한다.
                		exsoftProcessWrite.binder.setJsonArray("authorList", exsoftProcessWrite.coworkList.authorList);
                		exsoftProcessWrite.binder.setJsonArray("coauthorList", exsoftProcessWrite.coworkList.coauthorList);
                		exsoftProcessWrite.binder.setJsonArray("approverList", exsoftProcessWrite.coworkList.approverList);
                		exsoftProcessWrite.binder.setJsonArray("receiverList", exsoftProcessWrite.coworkList.receiverList);
                		
                		// 2. 확장권한을 set 한다.
                		exsoftProcessWrite.binder.setJsonArray("aclExItem_list", exsoftProcessWrite.exAclItems);

                		var jsonObject = exsoftProcessWrite.binder.getDataToJson(); 
                		jsonObject.fileList = JSON.stringify(exsoftProcessWrite.wFileUploadJsonArr);
                		jsonObject.page_cnt = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;
                		
                		
            			exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject ,exsoft.contextRoot + '/process/processControl.do',null,function(data, param){
                			if(data.result == 'true'){
                				 jAlert(data.message,'확인',8);
                				exsoftProcessWrite.close.layerClose(false);
                				//processList 변경된 내용 refresh
                				if($('#processDocGridList').length > 0){
                					exsoft.util.grid.gridRefresh('processDocGridList',exsoft.contextRoot + '/process/processList.do');                					
                				}
                				
                				// 메인화면 협업업무현황 업데이트
    	        				exsoftLayoutFunc.init.mainProcessCountInfo();
    	        				exsoftLayoutFunc.init.mainProcessList("WRITE_ING");
                			}else{
                				jAlert(data.message,'확인',7);
                				exsoft.document.close.layerClose(true,'coop_register_wrapper','coop_register');	//[3001]
                			}
                		});
        			}else {
        				// 대상 파일을 업로드 한다.
        				exsoft.util.ajax.loadingShow();		// [3000]
        				$("#loading_message").show();
    					exsoft.common.file.prototype.wUploadObj.startUpload();	
        			}
        			
        		} 
            },
            
            //업무(협업) 수정
            submitUpdate : function(){
            	if ($("#processWrite").validation()) {
        			/**********************************************************************
            		// fileCounter :: 업로드 영역에 있는 파일 수
            		// 1 : 첨부파일 없는 문서등록 처리
            		// 2이상 : 첨부파일 업로드 후 문서등록 처리
            		// upCounter :: 대상 파일 중 업로드 성공한 파일 수
            		**********************************************************************/
        			if(exsoft.common.file.prototype.wUploadObj.fileCounter == 1 ||
        					(exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{
        			     				
        				// 1. 협업자 정보를 jsonArray에 담아서 서버 호출 한다.
                		exsoftProcessWrite.binder.setJsonArray("authorList", exsoftProcessWrite.coworkList.authorList);
                		exsoftProcessWrite.binder.setJsonArray("coauthorList", exsoftProcessWrite.coworkList.coauthorList);
                		exsoftProcessWrite.binder.setJsonArray("approverList", exsoftProcessWrite.coworkList.approverList);
                		exsoftProcessWrite.binder.setJsonArray("receiverList", exsoftProcessWrite.coworkList.receiverList);
                		
                		// 2. 확장권한을 set 한다.
                		exsoftProcessWrite.binder.setJsonArray("aclExItem_list", exsoftProcessWrite.exAclItems);

                		var jsonObject = exsoftProcessWrite.binder.getDataToJson(); 
                		jsonObject.fileList = JSON.stringify(exsoftProcessWrite.wFileUploadJsonArr);
                		jsonObject.page_cnt = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;
                		jsonObject.dFileList = JSON.stringify(exsoftProcessWrite.deleteFileList);
                		
                		
            			exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject ,exsoft.contextRoot + '/process/processControl.do',null,function(data, param){
                			if(data.result == 'true'){
                				 jAlert(data.message,'확인',8);
                				exsoftProcessWrite.close.layerClose(false);
                				//processList 변경된 내용 refresh
                				if($('#processDocGridList').length > 0){
                					exsoft.util.grid.gridRefresh('processDocGridList',exsoft.contextRoot + '/process/processList.do');                					
                				}
                				
                				// 메인화면 협업업무현황 업데이트
    	        				exsoftLayoutFunc.init.mainProcessCountInfo();
    	        				exsoftLayoutFunc.init.mainProcessList("WRITE_ING");
                			}else{
                				jAlert(data.message,'확인',7);
                				exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register');	//[3001]
                			}
                		});
        			}else {
        				// 대상 파일을 업로드 한다.
        				exsoft.util.ajax.loadingShow();		//[3000]
        				$("#loading_message").show();
    					exsoft.common.file.prototype.wUploadObj.startUpload();	
        			}
        			
        		}
            },
        	
        }, // event end...
        
        //5. 화면 UI 변경 처리
        ui : {
        	getWorkerList : function(bindName, arrayObj){
        		var textList = '';
        		$(arrayObj).each(function(index){
        			textList += this.user_nm+';';
        		});
        		exsoftProcessWrite.binder.set(bindName, textList);
        	}
        		
        },

        //6. callback 처리
        callback : {
        	// 협업자 목록 set
        	processCoworkWindow : function(coworkerObj){
        		exsoftProcessWrite.coworkList.authorList = coworkerObj.authorList;
        		exsoftProcessWrite.coworkList.coauthorList = coworkerObj.coauthorList;
        		exsoftProcessWrite.coworkList.approverList = coworkerObj.approverList;
        		exsoftProcessWrite.coworkList.receiverList = coworkerObj.receiverList;
        		
        		//협업자 셋팅 필요
        		exsoftProcessWrite.ui.getWorkerList('coworkAuthor', exsoftProcessWrite.coworkList.authorList);
        		exsoftProcessWrite.ui.getWorkerList('coworkCoauthor', exsoftProcessWrite.coworkList.coauthorList);
        		exsoftProcessWrite.ui.getWorkerList('coworkApprover', exsoftProcessWrite.coworkList.approverList);
        		exsoftProcessWrite.ui.getWorkerList('coworkReceiver', exsoftProcessWrite.coworkList.receiverList);
        		
        	},

        	// 기본폴더 set
        	selectFolderWindow : function(nodeInfo) {
        		exsoftProcessWrite.binder.set("full_path", nodeInfo.full_path.join("/"));
        		exsoftProcessWrite.binder.set("folder_id", nodeInfo.id);
        		exsoftProcessWrite.binder.set("map_id", nodeInfo.mapId);
        		exsoftProcessWrite.binder.set("acl_id", nodeInfo.original.acl_id);
        		
        		// 문서유형 set
        		if(nodeInfo.original.is_type == 'ALL_TYPE'){
        			$('#processWrite_docType').ddslick('enable');
        		}else{
        			$('#processWrite_docType').ddslick('disable');
        			exsoftProcessWrite.binder.set("doc_type", nodeInfo.original.is_type);
    				
    				//문서유형에 맞는 확장 속성을 표시 한다.
    				exsoftProcessWrite.doFunction.setExtendTypeAttrItem(nodeInfo.original.is_type);
        		}

        		// 권한 셋팅
        		exsoftProcessWrite.doFunction.setAclItem(nodeInfo.original.acl_id);
			},
			
			// 파일 처리 callback
			fileupload : function(files,data,xhr){
				exsoftProcessWrite.doFunction.setUploadFile(data);
				// 전체 파일이 올라 갔을 경우
				if((exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{	
					exsoftProcessWrite.event.submit();
				}
			},
			
			// 권한 변경 선택 콜백
			selectAcl : function(aclInfo) {

				exsoftProcessWrite.binder.set("acl_id", aclInfo.aclId);
				exsoftProcessWrite.exAclItems = aclInfo.extAclItems;
				
				// 기본권한
				exsoft.util.table.tableDocumentAclItemPrintList('processWrite_acl', aclInfo.aclItems);
				
				// 확장권한
				exsoft.util.table.tableDocumentAclItemPrintList('processWrite_extAcl', aclInfo.extAclItems);
				
				// 권한명 set
    			$('#processWriteAclName').text(aclInfo.aclDetail.acl_name);
			},
			
			/**
    		 * 파일 처리 관련 websocket 호출
    		 * @param type : event 처리 type
    		 * @param pageId - 파일ID
    		 * @param pageName - 파일명
    		 */
        	pageCallback : function(type, pageId, pageName, currentObject){
        		var doc_root_id = exsoftProcessWrite.binder.get("doc_root_id");
        		if(type == 'view'){
        			exsoft.util.websocket.view(doc_root_id,pageId,pageName);
        		}else if(type == 'down'){
        			exsoft.util.websocket.clearDownload();
        			exsoft.util.websocket.addToDownload(doc_root_id,pageId,pageName);
        			exsoft.util.websocket.doDownload();        			
        		}else if(type == 'delete'){
        			//alert(pageId + ' : ' + $(currentObject).parent().parent().children('input[name=processWrite_attachFileList_checkbox]:checkbox').val());
					var currentValue = $(currentObject).parent().parent().children('input[name=processWrite_attachFileList_checkbox]:checkbox').val();
					exsoftProcessWrite.doFunction.setDeleteFilePlugin(currentValue);
					$(currentObject).parent().parent().remove();
        		}
        	}, // pageCallback end...
                
        },
        
        // 7. 내부 함수 처리
        doFunction : {
        	// exRep ECM에 파일 등록을 성공하였을 경우 후 처리
        	setUploadFile : function(data){
        		// 파일 업로드 성공 처리 :: 성공한 파일개수 증가 및 성공 값 array 담기
        		exsoftProcessWrite.wFileUploadJsonArr.push({orgFile:data.orgFile,contentPath:data.contentPath,fileSize:data.fileSize,volumeId:data.volumeId});
        		exsoft.common.file.prototype.wUploadObj.upCounter += 1;
        	},
        	
        	// 등록 취소 시 기존에 등록한 파일을 삭제 한다.
        	deleteUploadFile : function(){
        		if(exsoftProcessWrite.wFileUploadJsonArr.length > 0){
        			var jsonObject = {"fileList":JSON.stringify(exsoftProcessWrite.wFileUploadJsonArr)};
        			exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+"/common/fileDelete.do", null, function(){})
        		}
        	},
        	
        	// 폴더 기본 권한 set
        	setAclItem : function(acl_id){
        		exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : acl_id}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data, acl_id) {
        			// 기본 접근자
        			exsoft.util.table.tableDocumentAclItemPrintList('processWrite_acl', data.list);
        			
        			// 권한명 set
        			$('#processWriteAclName').text(data.aclDetail.acl_name);
	                  
				})
        		
        	},
        	
        	// 문서에 대한 추가 접근자 set
        	setExtAclItem : function(doc_id){
        		exsoft.util.ajax.ajaxDataFunctionWithCallback({"doc_id" : doc_id}, exsoft.contextRoot+"/permission/exAclItemList.do", "", function(data, param) {
        			// 추가 접근자
        			exsoft.util.table.tableDocumentAclItemPrintList('processWrite_extAcl', data.list);
				})
        		
        	},
        	
        	// 문서 유형에 따른 확장속성 표시  click.ddslick
        	setExtendTypeAttrItem : function(selectValue){
        		var jsonObject = {"type_id":selectValue,"is_extended":"T"};
        		$('#processWrite_docAttrView tbody').empty();
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject,exsoft.contextRoot+'/type/attrList.do', '#processWrite_docAttrView', function(data, tableId){
					// 확장속성 table을 그린다.
					exsoft.util.table.tableExtendTypeItemPrintList(tableId, data.list, exsoftProcessWrite.actionType);
					 if(data.records != 0){
						 $(tableId).removeClass('hide');
						 exsoftProcessWrite.binder.set("is_extended", 'T');
						 
						// table에 select box가 존재하면 ddslick을 해준다.
	    				var $extendType = $(tableId + ' tbody').find('input, select');
	    				$($extendType).each(function(idx){
	    					var name = $(this).attr('name');
	    					
	    					if($(this).is('select')){
	    						$(this).attr('id', name);
	    						$(this).attr('data-select', 'true');
	    						exsoft.util.common.ddslick(name,'srch_type1',name,120, function(divId, selectedData){
	    							exsoftProcessWrite.binder.set(name, selectedData.selectedData.value);
	    						});
	    					}else{
	    						$(this).attr('data-bind', name);
	    					}		    					
	    				});
	    				exsoftProcessWrite.binder.bindingElement(); // data-bind 전체 bind
						 
					 }else{
						 $(tableId).addClass('hide');
						 exsoftProcessWrite.binder.set("is_extended", 'F');
					 }
					 
				}); // 확장속성 set
        	},
        	
        	// 업무 수정 시 첨부파일 삭제 관련 파일 플로그인 작업
        	setDeleteFilePlugin : function(deleteValue) {
        		// checkbox get value :: page_id|this.page_size|this.page_name
				var currentValues = deleteValue.split("|");
				
				// 삭제 대상 array에 등록
				var dJsonData = {page_id:""};
				dJsonData['page_id'] = currentValues[0];
				exsoftProcessWrite.deleteFileList.push(dJsonData);
				
				// 첨파일수 -1 카운트
				var pageCnt = $('.approvalRequest_attach_cnt').html();
				$('.approvalRequest_attach_cnt').text(pageCnt-1);
				
				// 파일 플로그인 설정값 변경처리
				exsoft.common.file.prototype.wUploadObj.updateMaxFileCounter(exsoft.common.file.prototype.wUploadObj.MaxFileCounter()+1);
				exsoft.common.file.prototype.wUploadObj.updateMaxFileSize(exsoft.common.file.prototype.wUploadObj.MaxFileSize()+currentValues[1]);
    			
				// 기존 목록에서 삭제된 파일은 파일 추가를 할 수 있게끔 처리
    			var fileArray = new Array();
    			fileArray.push(currentValues[2]);
    			exsoft.common.file.prototype.wUploadObj.removeDbExistingFileName(fileArray);
        	},
        	
        },

}

