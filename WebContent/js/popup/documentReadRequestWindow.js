var documentReadRequestWindow = {
	
		current_doc_id : null,
	
    // 0. 초기화
    init : function(doc_id, doc_name) {
	    // 음영진 부분 클릭 시 닫기
	    $('.doc_request_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.doc_request').addClass('hide');
	    });
	    
	    // 화면에 보여질 값 세팅
	    documentReadRequestWindow.current_doc_id = doc_id;
	    $("#req_doc_name").html(doc_name);
	    
	    $("#doc_request_period").removeClass("hide");
	    $("#req_period_detail").addClass("hide");
	    $("#req_btn_group").removeClass("hide");
	    $("#req_btn_group_detail").addClass("hide");
	    $("#reason_text_count").removeClass("hide");
	    
	    documentReadRequestWindow.open();
    },
    
    // 열람사유 클릭시 조회화면
    commentDetail : function(req_id) {
    	
    	exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"req_id" : req_id}, exsoft.contextRoot + '/document/selectDocReadRequest.do', '',
		function(data, e) {
    		if(data.result == 'true') {
    			// 화면에 보여질 값 세팅
    			$("#req_doc_name").html(data.reqInfo.doc_name);
    			$("#req_period_detail").html(data.reqInfo.req_period_name);
    			$("#requestReason").val(data.reqInfo.req_comment);
    		}
    	});
    	
    	$("#doc_request_period").addClass("hide");
	    $("#req_period_detail").removeClass("hide");
	    $("#req_btn_group").addClass("hide");
	    $("#req_btn_group_detail").removeClass("hide");
	    $("#requestReason").prop("disabled", true);
	    $("#reason_text_count").addClass("hide");
	    
	    documentReadRequestWindow.open();
    },

    // 1. 팝업
    open : function() {
    	exsoft.util.layout.divLayerOpen("doc_request_wrapper", "doc_request");
    },

    //2. layer + show
    layer : {
    },

    //3. 닫기 + hide
    close : function() {
    	exsoft.util.layout.divLayerClose("doc_request_wrapper", "doc_request");
    },

    //4. 화면 이벤트 처리
    event : {
    	submit : function() {
    		var jsonArr = [];
    		var jsonArrIndex = 0;
    		
    		if(exsoft.util.layout.getSelectBox('doc_request_period','option') == 0) {
				jAlert("열람요청기간을 선택하세요.", "경고", 6);
				return false;
			}
    		
    		if ($("#requestReason").val().length == 0 || $("#requestReason").val() == null) {
			  	jAlert("열람요청사유를 입력하세요.", "경고", 6);
				return false;
		    }
			
			var rowData = {doc_id:"", req_period:"", req_comment:""};

			//jsonObject
			rowData['doc_id'] = documentReadRequestWindow.current_doc_id;
			rowData['req_period'] = exsoft.util.layout.getSelectBox('doc_request_period','option');
			rowData['req_comment'] = $("#requestReason").val();

			if(rowData.doc_id){
				jsonArr[jsonArrIndex] = rowData;
				jsonArrIndex++;
			}

			if(jsonArr.length > 0){
				documentReadRequestWindow.event.documentReadRequestSend(jsonArr);
			} else {
				jAlert("열람요청하는 중 오류가 발생했습니다.", "열람요청", 7);
			}
    	},
    	
    	documentReadRequestSend : function(docJsonArr) {
    		jConfirm('상기 내용으로 열람요청 하시겠습니까?', "확인", 8, function(ret) {
    			var jsonObject = {"reqList":JSON.stringify(docJsonArr)};
    			if(ret) {

    				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot + '/document/registDocReadRequest.do', '',
    				function(data, e){
    					if(data.result == 'true'){
    						//exsoft.util.common.noty("작업카트 추가 완료");
    						jAlert("열람요청 완료.", "열람요청", 8);
    						documentReadRequestWindow.close();
    						exsoft.util.grid.gridRefresh('workDocList', exsoft.contextRoot + '/document/workDocumentList.do');

    					} else {
    						jAlert(data.message, "열람요청", 7);
    					}
    				});
    			}
    		});
    	}
    },

    //5. 화면 UI 변경 처리
    ui : {
    },

    //6. callback 처리
    callback : {
    }
	
}