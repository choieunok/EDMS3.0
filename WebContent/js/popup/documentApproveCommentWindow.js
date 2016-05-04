var documentApproveCommentWindow = {
	
		appr_code : null,   // 승인/반려 코드
		type : null,           // 일괄기능 / 목록에서 한개만하는 기능
		jsonData : null,     // 결과반환시 같이 넘겨줄 값
		callback : null,      // 확인버튼클릭시 결과반환 함수
	
    // 0. 초기화
    init : function(code, type, jsonData, callback) {
	    // 음영진 부분 클릭 시 닫기
	    $('.doc_aprv_comment_wrapper').bind("click", function(){
	    	$(this).addClass('hide');
	    	$('.doc_aprv_comment').addClass('hide');
	    });
	    
	    if(code == "S") {
	    	$("#comment_window_title").html("문서 승인 사유");
	    } else {
	    	$("#comment_window_title").html("문서 반려 사유");
	    }
	    
	    $("#approve_comment").val("");
	    
	    documentApproveCommentWindow.appr_code = code;
	    documentApproveCommentWindow.type = type;
	    documentApproveCommentWindow.jsonData = jsonData;
	    documentApproveCommentWindow.callback = callback;
	    documentApproveCommentWindow.open();
    },

    // 1. 팝업
    open : function() {
    	exsoft.util.layout.divLayerOpen("doc_aprv_comment_wrapper", "doc_aprv_comment");
    },

    //2. layer + show
    layer : {
    },

    //3. 닫기 + hide
    close : function() {
    	exsoft.util.layout.divLayerClose("doc_aprv_comment_wrapper", "doc_aprv_comment");
    },

    //4. 화면 이벤트 처리
    event : {
    	submit : function() {
    		var jsonArr = [];
    		var jsonArrIndex = 0;
    		var rowData = null;
    		
    		if ($("#approve_comment").val().length == 0 || $("#approve_comment").val() == null) {
			  	jAlert("{0} 사유를 입력하세요.".format(documentApproveCommentWindow.appr_code == "S" ? "승인" : "반려"), "경고", 6);
				return false;
		    }
    		
    		// 목록에서 한개만하는 기능일때
    		if(documentApproveCommentWindow.type == "ONLY") {
    			
    			rowData = documentApproveCommentWindow.jsonData;
    			rowData.comment = $("#approve_comment").val();
    			
    			if(rowData.doc_id){
					jsonArr[jsonArrIndex] = rowData;
					jsonArrIndex ++;
				}
    			
    		// 일괄기능일때
    		} else {
    			var id = documentApproveCommentWindow.jsonData;
    			
				for(var i = 0; i < id.length; i ++){
    				var rowData = {req_id : "", req_userid : "", doc_id : "",	doc_access : "", req_period : "", comment : ""};
    				var rowId = $("#mypageDocList").getRowData(id[i]);
    				
    				rowData['req_id'] = rowId.req_id;
    				rowData['req_userid'] = rowId.req_userid;
    				rowData['doc_id'] = rowId.doc_id;
    				rowData['doc_access'] = documentApproveCommentWindow.appr_code;
    				rowData['req_period'] = rowId.req_period;
    				rowData['comment'] = $("#approve_comment").val();
    				rowData['creator_name'] = rowId.creator_name;
    				
    				if(rowData.doc_id){
    					jsonArr[jsonArrIndex] = rowData;
    					jsonArrIndex ++;
    				}
    			}
    		}

			if(jsonArr.length > 0){
				documentApproveCommentWindow.callback(jsonArr);
				documentApproveCommentWindow.close();
			} else {
				jAlert("{0}처리하는 중 오류가 발생했습니다.".format(documentApproveCommentWindow.appr_code == "S" ? "승인" : "반려"), "오류", 7);
			}
    	}
    },

    //5. 화면 UI 변경 처리
    ui : {
    },

    //6. callback 처리
    callback : {
    }
	
}