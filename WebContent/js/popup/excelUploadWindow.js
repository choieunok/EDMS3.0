var excelUploadWindow = {
		
		oper : null,   // 폴더 가져오기 call한 업무함 - workDoc / myDoc
		current_id : null,   // 폴더 가져오기 call한 노드id
		
		// 0. 초기화
		init : {
		},

		// 1. 팝업
		open : {
		},

		//2. layer + show
		layer : {
		},

		//3. 닫기 + hide
		close : function() {
			
		},

		//4. 화면 이벤트 처리
		event : {
			// 그룹일괄등록 메인
			groupUploadView : function() {
	    	    // 부서 일괄업로드 -  창 닫기 : 음영진 부분 클릭 시 닫기
	    	    $('.batch_upload_wrapper').bind("click", function(){
	    	    	$(this).addClass('hide');
	    	    	$('.batch_upload').addClass('hide');
	    	    });
	    	    
				$("#upFile").val("");		// input type=file 초기화
				
				exsoft.util.layout.divLayerOpen("batch_upload_wrapper", "batch_upload");
			},

			// 그룹다운로드
			groupDownload : function() {		
				$(location).attr("href", exsoft.contextRoot + "/common/downExcel.do?type=GROUP");
			},


			//사용자일괄등록 메인
			userUploadView : function() {
	    	    // 사용자 일괄업로드 -  창 닫기 : 음영진 부분 클릭 시 닫기
	    	    $('.user_batchUpload_wrapper').bind("click", function(){
	    	    	$(this).addClass('hide');
	    	    	$('.user_batchUpload').addClass('hide');
	    	    });
	    	    
				$("#userUpFile").val("");		// input type=file 초기화
				
				exsoft.util.layout.divLayerOpen("user_batchUpload_wrapper", "user_batchUpload");
			},

			// 사용자다운로드
			userDownload : function() {		
				$(location).attr("href", exsoft.contextRoot + "/common/downExcel.do?type=USER");
			},
			
			// 폴더 가져오기
			folderUploadView : function() {
	    	    // 폴더 가져오기 -  창 닫기 : 음영진 부분 클릭 시 닫기
	    	    $('.folder_batchUpload_wrapper').bind("click", function(){
	    	    	$(this).addClass('hide');
	    	    	$('.folder_batchUpload').addClass('hide');
	    	    });
	    	    
	    	    $("#folderUpFile").val("");		// input type=file 초기화
	    	    
				exsoft.util.layout.divLayerOpen("folder_batchUpload_wrapper", "folder_batchUpload");
				
			}
			
			
		},

		//5. 화면 UI 변경 처리
		ui : {
		},

		//6. callback 처리
		callback : {
		},
}