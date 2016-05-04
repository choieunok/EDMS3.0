/**
 * 협업프로세스 JavaScript
 */
var exsoftProcessFunc = {

		pageTitle : '',
		pageTitleId : '',
		pageSize : '',
		processType : Constant.PROCESS.ING_MENU,

		// 협업문서 우측 메뉴명
		pageNaviTitle : function() {
			$("#"+this.pageTitleId).html(this.pageTitle);
		},

		// 협업문서 우측메뉴명
		ConstPageName : {
			'ING' : '진행중 문서',
			'REQUEST' : '요청한 문서',
			'WRITE_ING' : '작성중 문서',
			'APPROVAL_ING' : '승인중 문서',
			'WRITE_END' :  '작성한 문서',
			'APPROVAL_END' : '승인한 문서',
			'RECEIVE' : '수신문서',
		},
		
		// 좌측메뉴 ID 정의
		MenuBtnids : {
			'ING' : 'process_ing',
			'REQUEST' : 'process_request',
			'WRITE_ING' : 'process_writeIng',
			'APPROVAL_ING' : 'process_appIng',
			'WRITE_END' :  'process_writeEnd',
			'APPROVAL_END' : 'process_appEnd',
			'RECEIVE' : 'process_receive',
		},

        // 0. 초기화
        init : {

        	// 협업문서 초기화 함수
    		initPage : function(menuType, processType, pageTitleId, pageSize) {
    			exsoftProcessFunc.init.initDdslick(); // select box 초기화
    			exsoftProcessFunc.init.initMenuSelect(exsoftProcessFunc.MenuBtnids,processType); // 메뉴클릭시 색깔변경
    			
    			exsoft.util.layout.topMenuSelect(menuType); // 상단 협업 메뉴 선택 표시

    			exsoftProcessFunc.pageTitle = exsoftProcessFunc.ConstPageName[processType];
    			exsoftProcessFunc.pageTitleId = pageTitleId;
    			//exsoftProcessFunc.init.contextRoot = exsoft.contextRoot;
    			exsoftProcessFunc.pageNaviTitle(exsoftProcessFunc.pageTitle);
    			exsoftProcessFunc.pageSize = pageSize;
    			
    			exsoftProcessFunc.processType = processType;
    			
    			// 협업단계가 작성중일 경우만 삭제 버튼이 보임 tbl_coop_del
    			if(exsoftProcessFunc.processType == Constant.PROCESS.WRITE_ING_MENU){
    				$('.tbl_coop_del').removeClass('hide');
    			}else{
    				$('.tbl_coop_del').addClass('hide');
    			}
    			
    			// 상세검색 postdata세팅
    			processDetailSearch.processType = processType;
    			processDetailSearch.url = "/process/processList.do";
    			
    			exsoftProcessFunc.event.processDocGridList();
    		},
    		
    		// 협업좌측 메뉴 선택시
    		menuInitPage : function(processType) {
    			exsoftProcessFunc.init.initMenuSelect(exsoftProcessFunc.MenuBtnids,processType); // 메뉴클릭시 색깔변경
    			
    			exsoftProcessFunc.pageTitle = exsoftProcessFunc.ConstPageName[processType];
    			exsoftProcessFunc.pageNaviTitle(exsoftProcessFunc.pageTitle);
    			exsoftProcessFunc.processType = processType;
    			
    			// 상세검색 postdata세팅
    			processDetailSearch.processType = exsoftProcessFunc.processType;
    			processDetailSearch.url = "/process/processList.do";
    			
    			// 협업단계가 작성중일 경우만 삭제 버튼이 보임 tbl_coop_del
    			if(exsoftProcessFunc.processType == Constant.PROCESS.WRITE_ING_MENU){
    				$('.tbl_coop_del').removeClass('hide');
    			}else{
    				$('.tbl_coop_del').addClass('hide');
    			}
    			
    			exsoft.util.grid.gridPostDataInitRefresh('#processDocGridList', exsoft.contextRoot+'/process/processList.do', {type:processType});

    		},
    		
    		// 메뉴 상태 표시 초기화
			initMenuSelect : function(MenuBtnids,processType) {

				$.each(exsoftProcessFunc.MenuBtnids, function(key, val) {

					if(processType == key){
						$("."+val).addClass("selected");
					}else {
						$("."+val).removeClass("selected");
					}
				});

			},
    		
    		/**
    		 * select box :: ddslick 사용
    		 */    		
    		initDdslick : function(){
    			//검색 selectbox		
    			exsoft.util.common.ddslick('#processList_select', 'srch_type1', '', 79, function(divId){});	// 검색 selectbox
    			exsoft.util.common.ddslick('#tbl_rowCount', 'tbl_rowCount', '', 68, function(divId){}); // 목록 selectbox
    		},
    		
    		initBind : function() {
    			
    		}
    		
        },
        
        // 1. 팝업
        open : {
                
        },
        
        //2. layer + show
        layer : {
        	// 협업 업무 상세조회 호출하기
    		processView : function(wrapperClass,layerClass,formId) {
    			exsoft.util.layout.divLayerOpen(wrapperClass,layerClass);
    			exsoft.util.common.formClear(formId);
    		},
    		
        },
        
        //3. 닫기 + hide
        close : {
                
        },
        
        //4. 화면 이벤트 처리
        event : {
        	// 목록 리스트
        	processDocGridList : function(){
        		$('#processDocGridList').jqGrid({
        			url: exsoft.contextRoot+'/process/processList.do',
					mtype:"post",
					datatype:'json',
					postData : {
						strIndex:exsoft.util.layout.getSelectBox('processList_select','option'),
						strKeyword1:exsoft.util.common.sqlInjectionReplace($("input[name=strKeyword]").val()),
						sdate:$("#sdate").val(),
						edate:$("#edate").val(),
						type:exsoftProcessFunc.processType
					},
					jsonReader:{
						page:'page',total:'total',root:'list'
					},
					cmTemplate: { title: false }, // tooltip 제거
					colNames:['process_id','doc_root_id', 'creator_id', 'author_id', 'name','status_nm','expect_date','complete_date', 'author_nm', 'approval_count', 'receiver_count'],  
					colModel:[
					   {name:'process_id',index:'process_id',width:10, align:'center',editable:false,sortable:false,key:true,hidden:true},
					   {name:'doc_root_id',index:'doc_root_id',width:10, align:'center',editable:false,sortable:false,key:false,hidden:true},
					   {name:'creator_id',index:'creator_id',width:10, align:'center',editable:false,sortable:false,key:false,hidden:true}, // 요청자ID
					   {name:'author_id',index:'author_id',width:10, align:'center',editable:false,sortable:false,key:false,hidden:true}, // 대표 작성자ID   
					   {name:'name',index:'name',width:100, align:'left',editable:false,sortable:true,key:false,
//						   formatter : function (cellValue, option, rowObject) {
//								return '<span stype="cursor:pointer" onclick=\'javascript:exsoftProcessFunc.event.processView("'+rowObject.process_id+'","'+rowObject.doc_root_id+'")\'>' + cellValue +'</span>';
//							},
						   cellattr: function (rowId, cellValue, rowObject) {
							   var tooltip = '<p>'+rowObject.name+'</p>';
							   var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'processDocGridList\',\''+tooltip+'\',\'false\',event)"';
							   var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';
								   
							   return mouseMove+' '+mouseOut;
				            }						   
					   },
					   {name:'status_nm',index:'name',width:20, align:'center',editable:false,sortable:false,key:false},
					   {name:'expect_date',index:'expect_date',width:20, align:'center',editable:false,sortable:true,key:false},
					   {name:'complete_date',index:'complete_date',width:20, align:'center',editable:false,sortable:true,key:false},
					   {name:'author_nm',index:'author_nm',width:20, align:'center',editable:false,sortable:true,key:false,
						   cellattr: function (rowId, cellValue, rowObject) {
							   //작성자 rowObject.write_list
							   var tooltip = '';
							   if(rowObject.write_list.length > 0){
								   var tempList = rowObject.write_list;
								   $.each(tempList, function(){
									   var writers = this.split('|');
									   if(writers[1] == '작성완료'){
										   tooltip += '<p>● '+writers[0]+' : '+writers[1]+'</p>';
									   }else{
										   tooltip += '<p>○ '+writers[0]+' : '+writers[1]+'</p>';										   
									   }
								   });
							   }else{
								   tooltip = '<p>○ '+cellValue+'</p>';
							   }
							   // mouseOver 시 마우스 안따라 다님(고정)
							   var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'processDocGridList\',\''+tooltip+'\',\'false\',event)"';
							   var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';
								   
							   return mouseMove+' '+mouseOut;
				            }								   
					   },
					   {name:'approval_count',index:'approval_count',width:20, align:'center',editable:false,sortable:false,key:false,
						   cellattr: function (rowId, cellValue, rowObject) {
								// 승인자 rowObject.approval_list
							   var tooltip = '';
							   if(rowObject.approval_list.length > 0){
								   var tempList = rowObject.approval_list;
								   $.each(tempList, function(){
									   var approvlers = this.split('|');
									   if(approvlers[1] == '승인완료'){
										   tooltip += '<p>● '+approvlers[0]+' : '+approvlers[1]+'</p>';
									   }else{
										   tooltip += '<p>○ '+approvlers[0]+' : '+approvlers[1]+'</p>';										   
									   }
								   });
							   }else{
								   tooltip = '<p>○ 승인자 없음</p>';
							   }
							   
							   var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'processDocGridList\',\''+tooltip+'\',\'true\',event)"';
							   var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';
								   
							   return mouseMove+' '+mouseOut;
				            }						   
					   },
					   {name:'receiver_count',index:'receiver_count',width:20, align:'center',editable:false,sortable:false,key:false,
						   cellattr: function (rowId, cellValue, rowObject) {
							   //수신자
							   var tooltip = '';
							   if(rowObject.receiver_list.length > 0){
								   var tempList = rowObject.receiver_list;
								   $.each(tempList, function(){
									   var receivers = this.split('|');
									   if(receivers[1] == '열람완료'){
										   tooltip += '<p>● '+receivers[0]+' : '+receivers[1]+'</p>';
									   }else{
										   tooltip += '<p>○ '+receivers[0]+' : '+receivers[1]+'</p>';										   
									   }
								   });
							   }else{
								   tooltip = '<p>○ 열람자 없음</p>';
							   }
							   
							   var mouseMove = 'onmousemove="javascript:exsoft.util.common.tooltip(\'processDocGridList\',\''+tooltip+'\',\'true\',event)"';
							   var mouseOut = 'onmouseout="javascript:$(\'.tooltip\').addClass(\'hide\')"';
								   
							   return mouseMove+' '+mouseOut;
				            }						   						   
					   },
					],
					viewrecords: true,
					multiselect:true,
					multikey: 'ctrlKey',
					sortable: true,
					shrinkToFit:true,
					gridview: true,
					autowidth:true,
					height:'auto',
					sortname : 'create_date',
					sortorder:'desc',
					scrollOffset: 0,
					viewsortcols:'vertical',
					emptyDataText: "데이터가 없습니다.",
					rowNum : exsoftProcessFunc.pageSize,
					loadBeforeSend: function() {
						exsoft.util.grid.gridTitleBarHide('processDocGridList');
						exsoft.util.grid.gridNoDataMsgInit('processDocGridList');
					}
					,onCellSelect : function(rowid,iCol,cellcontent,e){
						// 추후 다른 컬럼에 대한 이벤트 필요시 하위 코드 이용
						var cm = $('#processDocGridList').jqGrid("getGridParam", "colModel");
					    var colNameAttr = cm[iCol];
						
					    if(iCol == 0){
					    	// 체크시 row값을 set한다.(선택시 : rowid셋팅, 해제시 : rowid제거)
							$("#processDocGridList").jqGrid('setSelection',rowid);
					    }else if(colNameAttr.name == 'name'){
					    	var row = $("#processDocGridList").getRowData(rowid);
					    	if(row.status_nm != "완료") {
					    		exsoftProcessFunc.event.processView(row.process_id, row.doc_root_id);
					    	} else {
					    		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"doc_root_id":row.doc_root_id}, exsoft.contextRoot+'/document/selectCurrentDocID.do', null, function(data,param){
    								if(data.result == 'true'){
    									exsoft.document.layer.docCommonFrm('doc_detail_wrapper', 'doc_detail', data.current_doc_id);
    								}
    							});
					    		
					    	}
					    	
					    }

					}					
					,loadComplete: function(data) {
						if ($('#processDocGridList').getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('processDocGridList','no_data');
						}else {
							exsoft.util.grid.gridViewRecords('processDocGridList');
						}
						exsoft.util.grid.gridInputInit(false);

						exsoft.util.grid.gridPager("#processDocGridPager",data);
					}
					,loadError:function(xhr, status, error) {
						exsoft.util.error.isErrorChk(xhr);
					 }
        		});
        		
        		// 컬럼 헤더 정렬 및 다국어 변경 처리  //프로세스id|업무명|상태|완료예정일|최종변경일|작성자|승인|열람
				var headerData = '{"process_id":"키값1","doc_root_id":"키값2","name":"제목","status_nm":"상태","expect_date":"완료예정일","complete_date":"최종수정일","author_nm":"작성자","approval_count":"승인","receiver_count":"열람"}';
				exsoft.util.grid.gridColumHeader('processDocGridList',headerData,'center');
				
        	}, // End of Grid .....
        	
        	// 페이지이동 처리(공통)
			gridPage : function(nPage)	{
				$('#processDocGridList').setGridParam({page:nPage,postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
			},

			// 페이지목록 선택시(공통)
			rowsPage : function(rowNum) {
				$('#processDocGridList').setGridParam({rowNum:rowNum});		// 페이지목록 설정값 변경처리
				$('#processDocGridList').setGridParam({page:1,postData:{is_search:'false',page_init:'true'}}).trigger("reloadGrid");
			},
			
			// 협업문서 조회
			processView : function(processId, doc_root_id){
				 // processView.js의 init 호출
				 exsoftProcessViewFunc.init.initProcessView(processId, doc_root_id);
			},
			
			// 협업문서 삭제
			deleteProcess : function(){
				var jsonArr = [];
        		var jsonArrIndex = 0;
        		
        		if(exsoftProcessFunc.processType == Constant.PROCESS.WRITE_ING_MENU){
        			if(!exsoft.util.grid.gridSelectCheck('processDocGridList')) {
        				jAlert("삭제할 협업업무를 선택하세요", "협업업무 삭제", 2);
        				return false;
        			} else {
        				jConfirm("협업업무를 삭제하시겠습니까?", "협업업무 삭제", 2, function(ret){
        					if(ret){
        						var process_ids = $('#processDocGridList').getGridParam('selarrrow');
        						
        						for(var i = 0; i < process_ids.length; i++){
        							var processData = {process_id:"", doc_root_id:""};
        							var rowData = $('#processDocGridList').getRowData(process_ids[i]);
        							if(rowData.creator_id != exsoft.user.user_id && rowData.author_id != exsoft.user.user_id){
        								jAlert("협업업무는 요청자 및 대표작성만 삭제 가능합니다.", "삭제", 6);
        								return false;
        							}
        							
        							processData.process_id = rowData.process_id;
        							processData.doc_root_id = rowData.doc_root_id;
        							
        							jsonArr[jsonArrIndex] = processData;
        							jsonArrIndex++;
        							
        						} // for end...
        						
        						if(jsonArr.length > 0){
        							var jsonObject = {actionType : Constant.ACTION_DELETE, 
        									processData : JSON.stringify(jsonArr),
        									deleteType : "process"};
        							exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/process/processControl.do', null, function(data,param){
        								if(data.result == 'true'){
        									// 리스트 새로 고침, processList.jsp에 ID 정의 되어 있음
        									exsoft.util.grid.gridRefresh('processDocGridList',exsoft.contextRoot+'/process/processList.do');
        								}else{
        									jAlert(data.message,'확인',7);
        								}
        							});
        						}
        						
        					} // if end...
        				});
        			}
        		}else{
        			jAlert('삭제 권한이 없습니다.\n 정상적인 경로를 통해 작업하세요.','삭제',6);
        		} // if end...
        	}, // deleteProcess end...
        	
        	// 검색
        	searchProcess : function() {
        		_post = {
					strIndex:exsoft.util.layout.getSelectBox('processList_select','option'),
					strKeyword1:exsoft.util.common.sqlInjectionReplace($("input[name=strKeyword]").val()),
					sdate:$("#sdate").val(),
					edate:$("#edate").val(),
					type:exsoftProcessFunc.processType
				};
				
				exsoft.util.grid.gridPostDataInitRefresh("processDocGridList", exsoft.contextRoot + "/process/processList.do", _post);
        	},
			
			// 엔터키 입력시
			enterKeyPress : function(e) {
				if (e.keyCode == 13) {
					exsoftProcessFunc.event.searchProcess();
					return false;
				}
			},
        },

        //5. 화면 UI 변경 처리
        ui : {

        },

        //6. callback 처리
        callback : {
                
        },

}

