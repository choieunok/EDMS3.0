/**
 * 협업 진행단계 관련
 * [3000][EDMS-REQ-040]	2015-09-15 	성예나 : 업무문서삭제시 메인화면 협업업무현황 리스트 Refresh
 */
var exsoftProcessViewFunc = {

		process_id : '',
		doc_root_id : '',
		isModify : false,				// 속성수정
		isDelete : false,				// 삭제
		isApproveRequest : false,		// 승인요청
		isApprove : false,				// 승인
		isApproveReject : false,		// 반려
		isFileModify : false,			// 파일수정
		isContent : false,				// 내용 사용 여부
		owner_id : '',					// 문서 owner(대표작성자)
		is_locked : '',					// 현재 수정 중인 상태인가?
		mainProcessAction : '',   	// 메인화면 협업업무현황 리스트

		 // 0. 초기화
        init : {
        	initProcessView : function(processId, doc_root_id){
        		//웹 소켓 연결
        		exsoft.util.websocket.connect(exsoft.user.user_id, exsoftProcessViewFunc.callback.webSocketCallback);


        		exsoftProcessViewFunc.process_id = processId;
        		exsoftProcessViewFunc.doc_root_id = doc_root_id;

//        		 $('.coopUser_detail_wrapper').removeClass('hide');
//				 $('.coopUser_detail').removeClass('hide');

				exsoft.util.layout.divLayerOpen('coopUser_detail_wrapper','coopUser_detail',true);

        		exsoft.util.layout.lyrPopupWindowResize($(".coopUser_detail"));

        		exsoftProcessViewFunc.doFunction.setProcessRole(false,false,false,false,false,false,false);

        		///////////////////////////////////
        		// 1. 협업정보 가져오기
        		///////////////////////////////////
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({actionType:Constant.ACTION_VIEW, process_id:processId},exsoft.contextRoot + '/process/processControl.do',null,function(data, param){
        			if(data.result == 'true'){
        				var processVo = data.processVo;
            			var processExecutorList = data.processExecutorList;
            			var user_id = exsoft.user.user_id;

            			// 현 단계 이미지 색상 칠하기
            			$('#processView_step li').removeClass('current');
            			$('#processView_step li:nth-child('+processVo.status_number+')').addClass('current');

            			// 승인요청, 승인, 반려 내용 초기화
            			$('#processView_content').text('');

            			// 협업 기본 정보 set
//            			$('.coopUser_sub_wrapper').children('span').text(processVo.name);
            			$('.coopUser_sub_wrapper span:nth-child(2)').text(processVo.name);
            			$('#processView_info tr:nth-child(1)').find('span').text(processVo.name);					// 업무명
            			$('#processView_info tr:nth-child(3)').find('span').text(processVo.creator_name);			// 업무요청자
            			$('#processView_info tr:nth-child(4)').find('span').text(processVo.expect_date);			// 업무완료 예정일
            			$('#processView_info tr:nth-child(5)').find('span').text(processVo.content);				// 업무 요청 내용

            			//processVo.creator_id 요청자 exsoft.user.user_id
            			if(user_id == processVo.creator_id){ // 요청자
        					exsoftProcessViewFunc.doFunction.setProcessRole(true,true,false,false,false,true,false);
        				};

            			$('#processView_info tr:nth-child(6)').find('span').text('');
            			$('#processView_info tr:nth-child(7)').find('span').text('');
            			$('#processView_info tr:nth-child(8)').find('span').text('');
            			$('#processView_info tr:nth-child(9)').find('span').text('');

            			// 협업자 정보 set
            			var isModifyNdelete = false;
            			$(processExecutorList).each(function(idx){
            				var executor_name = this.executor_name;
            				var executor_id = this.executor_id;
            				switch (this.type) {
	    						case Constant.PROCESS.TYPE_AUTHOR:exsoftProcessViewFunc.doFunction.setExecutorName(6, executor_name);break;
	    						case Constant.PROCESS.TYPE_COAUTHOR:exsoftProcessViewFunc.doFunction.setExecutorName(7, executor_name);break;
	    						case Constant.PROCESS.TYPE_APPROVER:exsoftProcessViewFunc.doFunction.setExecutorName(8, executor_name);break;
	    						case Constant.PROCESS.TYPE_RECEIVER:exsoftProcessViewFunc.doFunction.setExecutorName(9, executor_name);break;
	    						default:break;
    						};

    						// 현 단계 및 사용자에 맞는 추가기능 set
                			// 1:작성, 2:승인, 3:보완, 4:완료
    						// isModify,isDelete,isApproveRequest,isApprove,isApproveReject,isFileModify,isContent
                			if(processVo.status_number == 1 || processVo.status_number == 3){
                				if(user_id == executor_id && this.type == Constant.PROCESS.TYPE_AUTHOR){ // 작성자
                					exsoftProcessViewFunc.owner_id = executor_id;  // 문서의 owner 지정
                					exsoftProcessViewFunc.doFunction.setProcessRole(true,true,true,false,false,true,true);
                				}else if(user_id == executor_id && this.type == Constant.PROCESS.TYPE_COAUTHOR){ // 공동 작성자
                					exsoftProcessViewFunc.doFunction.setProcessRole(false,false,false,false,false,true,false);
                				}
                			}else if(processVo.status_number == 2){
                				if(this.type == Constant.PROCESS.TYPE_APPROVER){
                					if(user_id == executor_id && this.status == Constant.PROCESS.EXECUTOR_START){
                						exsoftProcessViewFunc.doFunction.setProcessRole(true,false,false,true,true,true,true);
                					}else if(user_id == executor_id && this.status != Constant.PROCESS.EXECUTOR_START){
                						exsoftProcessViewFunc.doFunction.setProcessRole(true,false,false,false,false,false,false);
                					}

                				}
                			}

                			// 단계에 상관없이 요청자, 대표작성자는 수정, 삭제 권한을 부여
                			if(user_id == processVo.creator_id || (user_id == executor_id && this.type == Constant.PROCESS.TYPE_AUTHOR)){
                				exsoftProcessViewFunc.isModify = true;
                				exsoftProcessViewFunc.isDelete = true;
                			}

            			});


            			// 승인자, 수신자 tooltip 설정
            			exsoftProcessViewFunc.doFunction.setTooltip('processView_approver', 'processView_approverTooltip', processVo, Constant.PROCESS.TYPE_APPROVER);
            			exsoftProcessViewFunc.doFunction.setTooltip('processView_receiver', 'processView_receiverTooltip', processVo, Constant.PROCESS.TYPE_RECEIVER);

            			// 기능 버튼 hide 제거
            			//, , , , , (requestApproval_wordcnts)
            			exsoftProcessViewFunc.isModify ? $('#processView_modify').removeClass('hide') : $('#processView_modify').addClass('hide');
        				exsoftProcessViewFunc.isDelete ? $('#processView_delete').removeClass('hide') : $('#processView_delete').addClass('hide');
        				exsoftProcessViewFunc.isApproveRequest ? $('#processView_approveRequest').removeClass('hide') : $('#processView_approveRequest').addClass('hide');
        				exsoftProcessViewFunc.isApprove ? $('#processView_approve').removeClass('hide') : $('#processView_approve').addClass('hide');
        				exsoftProcessViewFunc.isApproveReject ? $('#processView_approveReject').removeClass('hide') : $('#processView_approveReject').addClass('hide');
        				exsoftProcessViewFunc.isContent ? $('#processView_content').removeClass('hide') : $('#processView_content').addClass('hide');
        				exsoftProcessViewFunc.isContent ? $('#processView_content').next('.requestApproval_wordcnts').removeClass('hide') : $('#processView_content').next('.requestApproval_wordcnts').addClass('hide');

        				// 승인요청 목록 처리
        				exsoftProcessViewFunc.ui.printProcessSituation(processId);

        			}else{
        				jAlert(data.message,'작업실패',7);
        			}


        		});

        		///////////////////////////////////
        		// 2. 문서정보 가져오기
        		///////////////////////////////////
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:doc_root_id}, exsoft.contextRoot+"/document/documentDetail.do", null, function(data,param) {

					//exsoft.document.event.printPageList(data);//첨부파일
					//exsoft.document.event.printDocumentVO(data);//기본정보
        			//

        			// 확장문서 속성
        			exsoft.util.table.tableExtendTypeItemPrintList('processView_docType', data.attrList, "V");

        			//1. 파일 카운트 처리 data.pageList.length
        			$('.approvalRequest_attach_cnt').text(data.pageList != undefined ? data.pageList.length : 0);
        			// 첨부파일이 없는 문서이면 관련버튼들을 숨김처리.
        			if(data.pageList == undefined || data.pageList.length == 0) {
        				$(".approvalRequest_docs_wrapper .download_btnGrp").addClass("hide");
        			} else {
        				$(".approvalRequest_docs_wrapper .download_btnGrp").removeClass("hide");
        			}
        			exsoft.util.table.printPageList('processView_attachFileList', data, 'exsoftProcessViewFunc.callback.pageCallback', true, true); // callbackname으로 넘긴다.


        			var docVO = data.documentVO;
        			exsoftProcessViewFunc.is_locked = docVO.is_locked;

        			// 잠금여부 확인 :: processView_tooltip
        			if(docVO.is_locked == 'T'){
        				$('.coopUser_sub_wrapper span:nth-child(1)').removeClass('hide');
        				$("#p_is_locked_val").val("T");

        				var tooltip = '<p>반출자 : '+docVO.lock_owner_name+'</p>';
        				tooltip += '<p>반출일시 : '+docVO.lock_date+'</p>';

        				$('#processView_tooltip').empty();
        				$('#processView_tooltip').append(tooltip);
        			}else{
        				$('.coopUser_sub_wrapper span:nth-child(1)').addClass('hide');
        				$("#p_is_locked_val").val("F");
        				$('#processView_tooltip').empty();
        			}

        			$('#processView_info tr:nth-child(2)').find('span').text(data.folderPath); 		// 기본폴더
        			$('#processView_info tr:nth-child(10)').find('span').text(docVO.type_name);		// 문서유형

        			// 권한 Setting
					$("#processView_aclName").html(data.aclDetail.acl_name); //권한

					exsoft.util.table.tableDocumentAclItemPrintList('processView_acl', data.aclItemList);
					exsoft.util.table.tableDocumentAclItemPrintList('processView_extAcl', data.aclExItemList);
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
        	approveAction : function(action_type) {
        		if(exsoftProcessViewFunc.isModify == true && $("#processWriteFrm").validation()){
        			var jsonObject = {actionType : action_type,
        							  process_id : exsoftProcessViewFunc.process_id,
        							  doc_root_id : exsoftProcessViewFunc.doc_root_id,
        							  content : $('#processView_content').val()};
	        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/process/processControl.do', null, function(data, param){
	        			if(data.result == 'true'){

	        				// 리스트 새로 고침, processList.jsp에 ID 정의 되어 있음
	        				if($('#processDocGridList').length > 0){
	        					exsoft.util.grid.gridRefresh('processDocGridList',exsoft.contextRoot+'/process/processList.do');
	        				}

	        				exsoft.util.layout.divLayerClose('coopUser_detail_wrapper','coopUser_detail');
	        				jAlert(data.message,'작업성공',8);
	        				
	        				// 메인화면 협업업무현황 업데이트
	        				exsoftLayoutFunc.init.mainProcessCountInfo();
	        				exsoftLayoutFunc.init.mainProcessList(exsoftProcessViewFunc.mainProcessAction);
	        			}else{
	        				
	        				jAlert(data.message,'작업실패',7);
	        			}

	        		});
        		}
        	},

        	//파일 전체 선택, 전체 해제
        	attachToggleCheck : function(type){
        		//$("input[name=processView_attachFileList_checkbox]:checkbox").prop("checked", type);
        		exsoft.common.file.event.attachToggleCheck('processView_attachFileList', type);
        	},

        	//파일저장
        	attachSave : function(){
//        		var $chkObject = $("input[name=processView_attachFileList_checkbox]:checkbox:checked");
//
//        		if($chkObject.length > 0){
//        			exsoft.util.websocket.clearDownload();
//        			$($chkObject).each(function(idx){
//        				var arrValue = $(this).val().split('|');
//            			exsoft.util.websocket.addToDownload(exsoftProcessViewFunc.doc_root_id,arrValue[0],arrValue[1]);
//            		});
//        			exsoft.util.websocket.doDownload();
//        		}else{
//        			jAlert('저장할 파일을 선택하세요.');
//        		}

        		exsoft.common.file.event.attachSave('processView_attachFileList', exsoftProcessViewFunc.doc_root_id);
        	},

        	//파일삭제
        	attachDelete : function(){
        		var $chkObject = $("input[name=processView_attachFileList_checkbox]:checkbox:checked");

        		if($chkObject.length > 0){
        			var page_ids = $($chkObject).map(function(){
        				var arrValue = $(this).val().split('|');
        				return arrValue[0];
        			}).get().join('|');

        			if($("#p_is_locked_val").val() == "T") {
        				jAlert("체크아웃한 파일이 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
        				return false;
        			}
        			jConfirm("선택한 파일을 삭제하시겠습니까?", "첨부파일 삭제", 2, function(ret){
        				if(ret){
        					var jsonObject = {actionType : Constant.ACTION_PAGE_DELETE, page_ids : page_ids, doc_root_id : exsoftProcessViewFunc.doc_root_id};
        					exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/process/processControl.do', null, function(data,param){
        						if(data.result == 'true'){
        							var pageCnt = $('.approvalRequest_attach_cnt').html();
        							$('.approvalRequest_attach_cnt').text(pageCnt-$chkObject.length);

        							$($chkObject).parent().remove();
        						}
        					});
        				}
        			});

        		}else{
        			jAlert('삭제할 파일을 선택하세요.','삭제',0);
        		}
        	},

        	// 협업 삭제
        	deleteProcess : function(){
        		if(exsoftProcessViewFunc.isDelete){
        			if($("#p_is_locked_val").val() == "T") {
        				jAlert("체크아웃한 파일이 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
        				return false;
        			}
        			jConfirm("협업 업무를 삭제하시겠습니까?", "협업업무 삭제", 2, function(ret){
        				if(ret){
        					var processData = {process_id:exsoftProcessViewFunc.process_id, doc_root_id:exsoftProcessViewFunc.doc_root_id};

        					var jsonArr = [processData];
        					var jsonObject = {actionType : Constant.ACTION_DELETE,
//        								  process_id : exsoftProcessViewFunc.process_id,
//        								  doc_root_id : exsoftProcessViewFunc.doc_root_id,
        							processData : JSON.stringify(jsonArr),
        							deleteType : "process"};
        					exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/process/processControl.do', null, function(data,param){
        						if(data.result == 'true'){   
        							//메인화면 협업업무현황 리스트 Refresh		[3000]
        							 exsoftLayoutFunc.init.mainProcessList(exsoftProcessViewFunc.mainProcessAction);
        							// 리스트 새로 고침, processList.jsp에 ID 정의 되어 있음
        							exsoft.util.grid.gridRefresh('processDocGridList',exsoft.contextRoot+'/process/processList.do');
        							exsoft.util.layout.divLayerClose('coopUser_detail_wrapper','coopUser_detail');
        						}else{
        							jAlert(data.message,'오류',7);
        						}
        					});
        				}
        			});
        		}else{
        			jAlert('삭제 권한이 없습니다.\n 정상적인 경로를 통해 작업하세요.','경고',6);
        		}

        	},

        	// 협업 업무 수정
        	modifyProcess : function(){
        		if($("#p_is_locked_val").val() == "T") {
    				jAlert("체크아웃한 파일이 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
    				return false;
    			}
        		if(exsoftProcessViewFunc.isModify){
        			exsoft.util.layout.divLayerClose('coopUser_detail_wrapper','coopUser_detail');
        			exsoft.process.modify("coop_register_wrapper","coop_register", exsoftProcessViewFunc.process_id, exsoftProcessViewFunc.doc_root_id);
        		}else{
        			jAlert('수정 권한이 없습니다.\n 정상적인 경로를 통해 작업하세요.','경고',6);
        		}

        	},
        },

        //5. 화면 UI 변경 처리
        ui : {
        	// 처리현황(xr_comment) 내용
        	printProcessSituation : function(process_id){
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:process_id}, exsoft.contextRoot+'/document/documentCommentList.do', null, function(data, param){
        			$('.approvalResult_cnt').text(data.records);
        			$('#processView_situation').empty();

        			if(data.result == 'true'){
        				$(data.list).each(function(idx){
        					var liContent = '<li class="approvalResult_list">';
        					    liContent += '<p><span class="bold">'+this.creator_name +'</span>';
        					    liContent += '<span>' + this.create_date + '</span></p>';
        					    liContent += '<p>' + this.content+'</p></li>'
        					$('#processView_situation').append(liContent);
        				});
        			}else{
        				$('#processView_situation').append('<li class="approvalResult_list"><p>등록된 데이터가 없습니다.</p></li>');
        			}
        		});
        	},

        	// 문서 잠금열쇠 툴팁
        	tooltip : function(event){
        		$("#processView_tooltip").removeClass("hide");
        	    $("#processView_tooltip").css('left','15px');
        	},

        },

        //6. callback 처리
        callback : {
        	/**
    		 * 파일 처리 관련 websocket 호출
    		 * @param type : event 처리 type
    		 * @param pageId - 파일ID
    		 * @param pageName - 파일명
    		 */
        	pageCallback : function(type, pageId, pageName, currentObject){
        		if(type == 'view'){
        			exsoft.util.websocket.view(exsoftProcessViewFunc.doc_root_id,pageId,pageName);
        		}else if(type == 'down'){
        			exsoft.util.websocket.clearDownload();
        			exsoft.util.websocket.addToDownload(exsoftProcessViewFunc.doc_root_id,pageId,pageName);
        			exsoft.util.websocket.doDownload();
        		}else if(type == 'check_out'){
        			exsoft.util.websocket.checkout(exsoftProcessViewFunc.doc_root_id,pageId,pageName,'1.0','T',exsoftProcessViewFunc.owner_id, true);

        		}else if(type == 'delete'){
        			if($("#p_is_locked_val").val() == "T") {
        				jAlert("체크아웃한 파일이 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
        				return false;
        			}
        			jConfirm("선택한 파일을 삭제하시겠습니까?", "첨부파일 삭제", 2, function(ret){

        				if(ret){
        					var jsonObject = {actionType : 'PAGE', page_ids : pageId, doc_root_id:exsoftProcessViewFunc.doc_root_id};
        					exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/process/processControl.do', null, function(data,param){
        						if(data.result == 'true'){
        							var pageCnt = $('.approvalRequest_attach_cnt').html()-1;
        							$('.approvalRequest_attach_cnt').text(pageCnt);
        							$(currentObject).parent().parent().remove();
        						}
        					});
        				}
        			});
        		}
        	}, // pageCallback end...

        	webSocketCallback : function(action, result){
        		if (action='CHECKOUT') {
    				//편집완료 후 변경사항이 있어서 체크인 된 경우 : CHECKIN_SUCCESS or CHECKIN_FAIL
    				//편집완료 후 변경사항이 없어서 체크아웃 취소가 호출된 경우  : CANCEL_SUCCESS or CANCEL_FAIL
        			//체크아웃 싪패 시 ][CHECKOUT_FAIL]
    				//do refresh
    				if( result == 'CHECKIN_SUCCESS' || result == 'CANCEL_SUCCESS'){
    					$('.coopUser_sub_wrapper span:nth-child(1)').addClass('hide');
        				$('#processView_tooltip').empty();
        				$("#p_is_locked_val").val("F");
    				}

    				if(result == 'CHECKOUT_SUCCESS'){
    					// 체크아웃 시 화면 reset
            			if(exsoftProcessViewFunc.is_locked != 'T'){
            				$('.coopUser_sub_wrapper span:nth-child(1)').removeClass('hide');
            				$("#p_is_locked_val").val("T");

            				var dt = new Date();

            				// Display the month, day, and year. getMonth() returns a 0-based number.
            				var month = dt.getMonth()+1;
            				var day = dt.getDate();
            				var year = dt.getFullYear();
            				var today = year+'-'+month+'-'+day;

            				var tooltip = '<p>반출자 : '+exsoft.user.user_name+'</p>';
            				tooltip += '<p>반출일시 : '+ today +'</p>';

            				$('#processView_tooltip').empty();
            				$('#processView_tooltip').append(tooltip);
            			}
    				}
    			}
        	}, // webSocketCallback end...

        },

        //7. doFunction
        doFunction : {
        	// 협업자 정보 set
        	setExecutorName : function(idx, name){
        		var currentText = $('#processView_info tr:nth-child('+idx+')').find('span').text();
        		$('#processView_info tr:nth-child('+idx+')').find('span').text(function(idx){
        			var tempStr = currentText + ',' + name;
        			var firstComma = tempStr.substring(0,1);
        			if(firstComma == ','){
        				tempStr = tempStr.substring(1);
        			}
					return tempStr;
				});
        	},

        	// 승인자, 수신자 현황 툴팁 set
        	setTooltip : function(id, tooltipId, processVo, type){

				var tooltip = '';
				var count = 0;
				var status = '';
				var executorArray = new Array();

				if(Constant.PROCESS.TYPE_APPROVER == type){
					count = processVo.approval_count;
					status = '승인완료';
					executorArray = processVo.approval_list;
				}else{
					count = processVo.receiver_count;
					status = '열람완료';
					executorArray = processVo.receiver_list;
				}

				if(executorArray.length > 0 > 0){
        			$(exsoft.util.common.getIdFormat(id)).text('['+count+']');
        			var tempList = executorArray;
					    $.each(tempList, function(){
					    	var excutors = this.split('|');
						    if(excutors[1] == status){
							    tooltip += '<p>● '+excutors[0]+' : '+excutors[1]+'</p>';
						    }else{
							    tooltip += '<p>○ '+excutors[0]+' : '+excutors[1]+'</p>';
						    }
					    });

					    $(exsoft.util.common.getIdFormat(tooltipId)).html(tooltip);
				}
			},

			// 현 단계 및 사용자에 맞는 추가기능 set
			// isModify,isDelete,isApproveRequest,isApprove,isApproveReject,isFileModify,isContent
        	setProcessRole : function(isModify,isDelete,isApproveRequest,isApprove,isApproveReject,isFileModify,isContent){
        		exsoftProcessViewFunc.isModify = isModify != undefined ? isModify : false;							// 속성수정
				exsoftProcessViewFunc.isDelete = isDelete != undefined ? isDelete : false;							// 삭제
				exsoftProcessViewFunc.isApproveRequest = isApproveRequest != undefined ? isApproveRequest : false;	// 승인요청
				exsoftProcessViewFunc.isApprove = isApprove != undefined ? isApprove : false;						// 승인
				exsoftProcessViewFunc.isApproveReject = isApproveReject != undefined ? isApproveReject : false;		// 반려
				exsoftProcessViewFunc.isFileModify = isFileModify != undefined ? isFileModify : false;				// 파일수정
				exsoftProcessViewFunc.isContent = isContent != undefined ? isContent : false;						// 내용 사용 여부
        	},


        }

}