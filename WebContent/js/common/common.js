/**
 * EDMS HTML5 Global Common
 * 
 * [2000][로직수정, 쿼리수정]	2015-09-03	이재민 : 나의문서 > 내만기문서 >  만기만료문서 - 상세조회시 수정버튼, 추가기능 hide
 * [2001][로직수정, 쿼리수정]	2015-09-03	이재민 : 나의문서 > 휴지통 - 상세조회시 수정버튼, 삭제버튼, 추가기능 hide
 * [2002][로직수정]	2015-09-03	이재민 : 추가기능 > 엑셀이력관리 버튼은 이력탭일때만 나올수있게 수정
 * [2003][로직수정]	2015-09-03	이재민 : 문서상세 > 버전탭 - 버전삭제시도시 js에러수정 및 최신버전문서 is_current T로 만드는 로직 추가
 * [2004][로직수정]	2015-09-07	이재민 : 문서상세 > 버전탭 - 권한이 없거나 만기만료문서일때 버전삭제, 문서복원기능 버튼 hide
 * [2005][로직수정]	2015-09-15	이재민 : 좌측상단문서등록 시 부서함에 문서등록할 때 doc_type이 null로 들어가서 수정처리
 * [3000][EDMS-REQ-015]	2015-09-02	성예나	 :	버전복원 버튼 생성. 
 * [3001][EDMS-REQ-040]	2015-09-16	성예나	 :	문서상세보기>삭제 시 map_id 필요.
 * [3002][EDMS-REQ-040]	2015-09-16	성예나	 :	문서 수정 부/주 버전 증가시 폴더 용량 체크 할때 필요한 값.
 * [3003][기능추가]	2015-09-18	성예나	 : 데이터 업로드중 로딩바 처리
 * [3004][기능추가]	2015-09-21	성예나	 : 문서등록시 용량초과alert이 뜨면 문서등록창 close 기능추가
 * [1000][EDMS-REQ-070~81]	2015-09-10	최은옥	 :	문서이력 수행장소 추가.
 * [1006]2016-03-25	최은옥	 :	문서등록 오류시 로딩바 없앰.
 */

if( typeof exsoft == 'undefined') {
	window.exsoft = {};
}

// context path
exsoft.contextRoot = contextRoot;
exsoft.theme = theme;
exsoft.notyDivId = null;
exsoft.menuType = null;

$.extend(exsoft, {
	/**
	 * Global 객체 정의(namespace pattern)
	 * @param name : namespace 명
	 */
	namespace : function ( name ) {
		var names = name.split(".");
		var topClass = exsoft;
		var i=0;

		for ( i=( names[0]=='exsoft'? 1 : 0); i<names.length; i++ ) {
			topClass[ names[i]] = topClass[names[i]] || {}; // typeof short-hand 방식
			topClass = topClass[names[i]];
		}
	},

	/**
	 * prototype만 상속 받을 시 사용함
	 * @param parent : 상속 받을 namespace명
	 * @param child : 상속 받은 객체에 추가 정의
	 *
	 */
	prototypeExtend : function( parent, child) {
		var fn = function() {}
		fn.prototype = $.extend( true, {}, parent.prototype, child);

		var sub = function(){};
		sub.prototype = new fn();
		//sub.prototype.constructor = sub;
		//sub.superClass = parent.prototype;

		//prototype으로 정의되지 않은 상속은 DOM에 매달리지 않으므로 직접 붙여준다.
		var instance =  new sub();
		instance.prototype = sub.prototype;
		instance.prototype.superClass = parent.prototype;
		return instance;
	},
	
	cleanLeak : function(){
		workDocList = null;
		myDocList = null;
		processWrite = null;
		selectAclWindow = null;
		documentListLayerWindow = null;
		selectAccessorWindow = null;
		selectMultiFolderWindow = null;
		registMail = null;
		registFavoriteFolderWindow = null;
		registAclWindow = null;
		registFolderWindow = null;
		selectSingleFolderWindow = null;
		selectSingleUserWindow = null;
		docDetailSearch = null;
		configFavoriteFolder = null;
		configRelDoc = null;
		extendDecade = null;
		selectFavoriteFolderWindow = null;	
		documentListLayerWindow = null;
		processCoworkWindow = null;
		processView = null;
		base64 = null;
		myPageDocList = null;
		processList = null;
		processDetailSearch = null;
		statistics = null;
		selectGroupWindow = null;	

	}
});

exsoft.namespace('user');
exsoft.namespace("document");
exsoft.namespace("process");
exsoft.namespace("common.bind");
exsoft.namespace("common.file");
/***********************************************
 * loginUser
 **********************************************/
/**
 * user 관련 util
 *
 * @namespace : exsoft.user
 *
 */
exsoft.user = {
		user_id : null,
		user_name : null,
		acl_menu_part : null,
		manage_group_id : null,
		manage_group_nm : null,
		user_email : null,

}; // exsoft.util.error end...

/***********************************************
 * document
 **********************************************/
/**
 * 문서 common
 * namespace로 관리
 */

exsoft.document = {
		//binder : new DataBinder("#documentWrite"),

    	commDocBinder : null,
    	commDocUBinder : null,

		actionType : null,
		exAclItems : [],
		//삭제대상 파일
		dJsonArr : [],
		dJsonArrIndex : 0,

		wFileUploadJsonArr : new Array(),
		//문서 수정 시 첨부파일 관련
		deleteFileList : new Array(),

		//관련문서
		wRefDoc :0,
		wDefaultRefDocCnt : 10,
		uRefDoc : 0,
		init : {
			//1 문서 기본
			docDetailInit : function(docId){

				exsoft.document.prototype.gDocId = docId;
				$(".url_email").addClass("hide");
				$(".doc_auth_cnts").addClass('hide');
        		$(".relative_docs_wrapper").addClass('hide');
        		$("#ext_archExcel").addClass("hide"); // [2002]
        		
				$("#docDetailModify").removeClass('hide');
				$("#docDetailDelete").removeClass('hide');
				$("#docDetail_extFunc").removeClass("hide"); // [2000]
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:docId}, exsoft.contextRoot+"/document/documentDetail.do", "select",
        				function(data,e) {
        					//창 열림여부 체크        			
        					if((exsoft.util.common.getAclLevel(data.documentVO.acl_level)) >= (exsoft.util.common.getAclLevel("READ"))) {
	        					if($(".doc_detail").hasClass("hide")) {        						
	        						exsoft.util.layout.divLayerOpen('doc_detail_wrapper','doc_detail',true);
	        					}
        					}else{
        						jAlert("해당문서를 읽을 권한이 없습니다. 열람 요청후 조 회하세요",'경고',6); 
        					}
    						if((exsoft.util.common.getAclLevel(data.documentVO.acl_level)) < (exsoft.util.common.getAclLevel("UPDATE"))) {
    							$("#docDetailModify").addClass('hide');
    		    			}
    						if((exsoft.util.common.getAclLevel(data.documentVO.acl_level)) < (exsoft.util.common.getAclLevel("DELETE"))) {
    		    				$("#docDetailDelete").addClass('hide');
    		    			}
    						// [2000]
    						if(data.documentVO.is_expired == "T") {
    							// 내만기문서 > 만기만료문서일때 수정버튼, 추가기능 hide
    							$("#docDetailModify").addClass('hide');
    							$("#docDetail_extFunc").addClass("hide");
    						}
    						
    						// [2001]
    						if(data.documentVO.doc_status == "D") {
    							//나의문서 > 휴지통일때 수정,삭제버튼, 추가기능 hide
    							$("#docDetailModify").addClass('hide');
    							$("#docDetailDelete").addClass('hide');
    							$("#docDetail_extFunc").addClass("hide");
    						}

    		    			
    						//웹 소켓 연결    						
    		        		exsoft.util.websocket.connect(exsoft.user.user_id, exsoft.document.callback.webSocketCallback);

    						//기본 탭 선택  ============================
    				        var targetFrm = $("#defaultDoc").parent().parent().parent().find('div[class^="tab_form"]');
    				        targetFrm.addClass('hide');
    				        targetFrm.eq(0).removeClass('hide');
    				        $('.tab_element').removeClass('selected');
    				        $("#defaultDoc").addClass('selected');
    				        //=======================================

        					exsoft.document.event.printPageList(data);//첨부파일
        					exsoft.document.event.printDocumentVO(data);//기본정보

        					// 권한 Setting
        					$("#docDetailAclName").html(data.aclDetail.acl_name); //권한

        					exsoft.util.table.tableDocumentAclItemPrintList('detail_docAclItemList', data.aclItemList);
        					exsoft.util.table.tableDocumentAclItemPrintList('detail_docExAclItemList', data.aclExItemList);

        					exsoft.document.event.printRefDoc(data);//관련문서

        					//의견 총수
        					$(".opinion_cnt").text(data.commentCnt);

        					// 협업관련
        					if(data.process_id != null && data.process_id != "") {
        						$(".coop_infomation").removeClass("hide");
        						$(".coop_approvalResult").removeClass("hide");
        						exsoft.document.processView(data.process_id);
        					} else {
        						$(".coop_infomation").addClass("hide");
        						$(".coop_approvalResult").addClass("hide");
        					}

        			});
        	},
        	//==============================================================
        	//최신문서 클릭시 초기값 세팅
        	docRecentInit : function(docId){
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:docId}, exsoft.contextRoot+"/document/documentDetail.do", "select",
        		function(data,e) {

        			// FORM 히든값 설정
        			exsoft.document.commDocBinder.set("folder_id", data.documentVO.folder_id);
        			exsoft.document.commDocBinder.set("map_id", data.documentVO.map_id);
        			exsoft.document.commDocBinder.set("acl_id", data.documentVO.acl_id);


        			// 문서제목 : doc_name
        			document.documentWrite.doc_name.value = data.documentVO.doc_name;
        			// 기본폴더 : folder_path
        			document.documentWrite.folder_path.value = data.folderPath;
        			
        			//보존년한
        			exsoft.document.commDocBinder.set("preservation_year", data.documentVO.preservation_year);     
        			
        			
        			if(data.isChangeType == "FALSE") {
        				$('#register_docType').ddslick('disable');
        				exsoft.document.commDocBinder.set("isChangeType","FALSE");

        			}else {
        				$('#register_docType').ddslick('enable');
        				exsoft.document.commDocBinder.set("isChangeType","TRUE");
        			}
        			exsoft.document.commDocBinder.set("folderIsType",data.is_type);

        			// 문서유형 : type_name
        			exsoft.document.commDocBinder.set("doc_type",data.documentVO.doc_type);
					// 확장문서유형
					exsoft.document.event.setExtendTypeAttrItem(data.documentVO.doc_type);
        			

        			if(data.documentVO.map_id != "MYPAGE")	{
        				//보안등급, 조회등급
						$("#sercurityView").removeClass('hide');
						$("#inheritacl").removeClass('hide'); //권한유지
						$("#isShareView").removeClass('hide'); //문서공유
						$("#classificationlist").removeClass('hide');//다차원분류
						$("#keyWord").removeClass('hide');//키워드
						$(".doc_auth").removeClass('hide');// 권한
						$(".doc_relative").removeClass('hide'); //관련문서
						
        				// 보안등급 : security_level
        				$("#documentWrite input[name='security_level']:radio[value="+data.documentVO.security_level+"]").prop('checked', 'checked');
        				// 조회등급 : access_grade
        				$("#documentWrite select[name=access_grade]").val(data.documentVO.access_grade);

        				// 공유 : is_share_chk
        				if(data.documentVO.is_share == "T")	{
        					$("#documentWrite :checkbox[name='is_share_chk']").prop("checked",true);
        				}else {
        					$("#documentWrite :checkbox[name='is_share_chk']").prop("checked",false);
        				}
        				// 권한변경유무 : uAclLockView is_inherit_acl_chk
        				if(data.documentVO.is_inherit_acl == "T")	{
        					$("#documentWrite :checkbox[name='is_inherit_acl_chk']").prop("checked",true);
        				}else {
        					$("#documentWrite :checkbox[name='is_inherit_acl_chk']").prop("checked",false);
        				}
        			}else{
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

        			}
        			
					// 권한
					$("#wAclName").html(data.aclDetail.acl_name);
					exsoft.util.table.tableDocumentAclItemPrintList('#docmentWrite_acl', data.aclItemList);

					//  권한 추가 접근자 목록
					exsoft.util.table.tableDocumentAclItemPrintList('#docmentWrite_extAcl', data.aclExItemList);

					
        		});
        	},
        	
        	//=======================================================================
        	
        	
        	//1 문서 기본
			docWriteInit : function(folder_id,folder_path,calledpage,isLeft){

				exsoft.document.wRefDoc = 0;
				exsoft.document.wFileUploadJsonArr = new Array(); // 업로드 파일 목록
        		exsoft.document.deleteFileList = new Array();  // 삭제대상 파일 목록
        		
        		$(".doc_auth_cnts").addClass('hide');
        		$(".doc_relative_cnts").addClass('hide');
 
				
				// 최근 등록 문서 목록
				exsoft.document.event.getRecentlyDocumentList(isLeft);
				
				//개인함이나 업무문서함에서 leftTop에있는 문서등록을 할 경우 폴더경로 안나오게수정
				if(folder_id != "" && folder_id != null && isLeft == true){

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
									$("#documentWrite select[name=preservation_year]").val(0);

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
	    				exsoft.document.commDocBinder.set("access_grade", "140");
					}
					exsoft.util.layout.divLayerOpen('doc_register_wrapper','doc_register',true);
					
					//좌측상단 문서등록 시 부서함에 문서등록할 때 doc_type이 null로 들어가서 수정처리	[2005]
					exsoft.document.commDocBinder.set("doc_type","XR_DOCUMENT");
					$('#register_docType').ddslick('enable');
					exsoft.document.commDocBinder.set("isChangeType","TRUE");
	
					// 확장문서유형
					exsoft.document.event.setExtendTypeAttrItem("XR_DOCUMENT");
					
				}	       		

        	},
        	//문서 Update 초기처리
        	docUpdateInit : function(){
        		var doc_id = exsoft.document.commDocUBinder.get("doc_id");
        		exsoft.document.exAclItems = [];
        		//삭제파일 초기화
        		dJsonArr = [];
        		dJsonArrIndex = 0;
				exsoft.document.uRefDoc=0;//관련문서 목록 초기화
				var pageCnt = 0;

        		exsoft.document.wFileUploadJsonArr = new Array(); // 업로드 파일 목록
        		exsoft.document.deleteFileList = new Array();  // 삭제대상 파일 목록


				exsoft.document.init.initUSetDdslick();
				
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:doc_id},exsoft.contextRoot+"/document/docCommonUpdate.do", "update",
        			function(data,e){
	        			if(data.result == "true")	{
	        				exsoft.util.layout.divLayerOpen('doc_modify_wrapper','doc_modify',true);

	        				//웹 소켓 연결
    		        		exsoft.util.websocket.connect(exsoft.user.user_id, null);


	        				exsoft.document.init.initUSetDdslick(data.documentVO.doc_type);
	        				
	                		$('#uMultiFolder').empty();
	                		$('#uRefDocTable').empty();

	                		//전역변수 설정
	                		exsoft.document.prototype.gRootId = data.documentVO.root_id;
	                		exsoft.document.prototype.owner_id = data.documentVO.owner_id;

	        				// 설명 : uIframe_editor - 개인/업무함 공통
	        				$("#temp_content").html(data.documentVO.doc_description.replace('&lt;','<').replace('& lt;','<').replace('&gt;', '>').replace('& gt;', '>'));
	        				$('#uIframe_editor').attr("src",exsoft.contextRoot+"/editor/doc_description.jsp");
	        				// 문서제목 : doc_name
	        				document.documentUpdate.doc_name.value = data.documentVO.doc_name;

	        				// 기본폴더 : folder_path
	        				document.documentUpdate.folder_path.value = data.folderPath;
	        				//보존년한
	        				exsoft.document.commDocUBinder.set("preservation_year", data.documentVO.preservation_year);
	        				
	        				// 문서유형 : type_name
	        				$("#uType_name").html(data.documentVO.type_name);
	        				exsoft.document.commDocUBinder.set("doc_type",data.documentVO.doc_type);
	        				// 문서확장속성  :: 개인/업무함 공통 uDocAttrView

	        				// 확장문서유형
	        				$('#documentUpdate_docAttrView tbody').empty();
							if(data.documentVO.is_system == "F" && data.attrList.length > 0){
								exsoft.util.table.tableExtendTypeItemPrintList('documentUpdate_docAttrView', data.attrList,"U");
			        			if(data.attrList != undefined && data.attrList.size != 0){
			        				$('#documentUpdate_docAttrView').removeClass('hide');
		        					exsoft.document.commDocUBinder.set("is_extended", "T");
		        					
									// table에 select box가 존재하면 ddslick을 해준다.
				    				var $extendType = $('#documentUpdate_docAttrView' + ' tbody').find('input, select');
				    				
				    				$($extendType).each(function(idx){
				    					var name = $(this).attr('name');
				    					if($(this).is('select')){
				    						$(this).attr('id', name);
				    						$(this).attr('data-select', 'true');
				    						exsoft.util.common.ddslick(name,'srch_type1',name,120, function(divId, selectedData){
				    							exsoft.document.commDocUBinder.set(name, selectedData.selectedData.value);
				    						});
				    					}else{
				    						$(this).attr('data-bind', name);
				    					}		    					
				    				});
				    				exsoft.document.commDocUBinder.bindingElement(); // data-bind 전체 bind
									 
								 }else{
									$('#documentUpdate_docAttrView').addClass('hide');
			        				exsoft.document.commDocUBinder.set("is_extended", "F");
								 }
							}
							//===================================================================
							
							
							
	        				// 문서버전보기
	        				$("#uVersionView").html("Ver "+data.documentVO.version_no);
	        				if(data.documentVO.map_id == "MYPAGE")	{
	        					//보안등급, 조회등급
								$("#usercurityView").addClass('hide');
								$("#uaclLockView").addClass('hide'); //권한유지
								$("#ushareChkView").addClass('hide'); //문서공유
								$("#uclassificationlist").addClass('hide');//다차원분류
								$("#ukeyWord").addClass('hide');//키워드
								$(".doc_auth").addClass('hide');// 권한
								$(".doc_relative").addClass('hide'); //관련문서

								exsoft.document.ui.setVersionConfig('version_type','Y','SAME');

	        					//base.versionTypeConfig('workspaceVersion','version_type','${mypage.IS_USE}','${mypage.VAL}');
	        				}else{
								exsoft.document.ui.setVersionConfig('version_type','Y','MINOR');

	        					//base.versionTypeConfig('workspaceVersion','version_type','${workspace.IS_USE}','${workspace.VAL}');

	        		    		exsoft.document.commDocUBinder.set("access_grade",data.documentVO.access_grade);
	        		    		exsoft.document.commDocUBinder.set("keyword",data.documentVO.keyword);

	        					// 보안등급 : security_level
	        		    		$("#documentUpdate input[name='security_level']:radio[value="+data.documentVO.security_level+"]").prop('checked', 'checked');

	        					// 조회등급 : access_grade
	        					$("#documentUpdate select[name=access_grade]").val(data.documentVO.access_grade);

	        					// 다차원분류 : uMultiFolderView / uMultiFolder
	        					if(data.multiFolderList.length > 0)	{
	        						$(".doc_classification_list").removeClass('hide');
	        						var buffer="";
	        						for(var m=0;m<data.multiFolderList.length;m++) {

	        							var divNames = exsoft.util.common.uniqueId();
	        							buffer += "<li id="+divNames+">";
	        							buffer += "<input type='hidden' class='' name='multi_folder' class='' value='"+data.multiFolderList[m].folder_id+"'>";
	        							buffer += "<input type='text' style='width:95%;' name='multi_folder_path' readonly value='"+data.multiFolderList[m].folder_path+"'>";
	        							buffer += "<img src='"+exsoft.contextRoot+"/img/icon/recent_doc_del.png' onclick=\"javascript:exsoft.util.common.removeDivIds('"+divNames+"');\">";
	        							buffer += "</li>";

	        						}

	        						$('#uMultiFolder').append(buffer);
	        					}

	        					// 문서유형정보
	        					//document.documentUpdate.folderIsType.value = data.is_type;
	        					exsoft.document.commDocUBinder.set("folderIsType", data.is_type);

	        					// 키워드 : uKeywordView / keyword
	        					exsoft.document.commDocUBinder.set("keyword", data.documentVO.keyword);

	        					// 공유 : is_share_chk
	        					if(data.documentVO.is_share == "T")	{
	        						$("#documentUpdate :checkbox[name='is_share_chk']").prop("checked",true);
	        					}else {
	        						$("#documentUpdate :checkbox[name='is_share_chk']").prop("checked",false);
	        					}

	        					// 권한변경유무 : uAclLockView is_inherit_acl_chk
	        					if(data.documentVO.is_inherit_acl == "T")	{
	        						$("#documentUpdate :checkbox[name='is_inherit_acl_chk']").prop("checked",true);
	        					}else {
	        						$("#documentUpdate :checkbox[name='is_inherit_acl_chk']").prop("checked",false);
	        					}

	        					// 부서권한 : uWorkSpaceView uAclDocTable
	        					$("#uAclName").html(data.aclDetail.acl_name);
	        					exsoft.util.table.tableDocumentAclItemPrintList('#docmentUpdate_acl', data.aclItemList);

	        					//  권한 추가 접근자 목록
	        					exsoft.document.exAclItems = data.aclExItemList;
	        					exsoft.util.table.tableDocumentAclItemPrintList('#docmentUpdate_extAcl', data.aclExItemList);

	        					// 관련문서 :
	        					if(data.refDocumentList.length > 0)	{
	        						exsoft.document.event.refDocData(data.refDocumentList);
	        						$("#uRefDocCnt").html("("+data.refDocumentList.length+")");
	        					}
	        				
	        				}
	        			
	        				
	        				// 보존년한 : preservation_year
	        				$("#documentUpdate select[name=preservation_year]").val(data.documentVO.preservation_year);

	        				// 등록자/소유자/등록일
	        				$("#uCreator_name").html(data.documentVO.creator_name +"(" + data.documentVO.owner_name + ")");
	        				$("#uCreate_date").html(data.documentVO.create_date);

	        				//if(data.documentVO.map_id != "MYPAGE")	{}	// END OF 업무문서함의 경우

	        				var totalPageSize = 0;

	        				// 첨부파일 :: 개인/업무함 공통
	        				exsoft.util.layout.layerEmpty("uAttacheTable");
	        				if (data.pageList != undefined && data.pageList.length > 0) {

	        					$('#uAttachCnt').text(data.pageList != undefined ? data.pageList.length : 0);
        		    			exsoft.util.table.printPageList('uAttacheTable', data, 'exsoft.document.callback.pageCallback', false, true); // callbackname으로 넘긴다.

        	        			// 기존 파일 목록을 플러그인에 담는다 :: 중복파일 금지

	        					$(data.pageList).each(function(index){
	        						//alert(data.pageList[index].page_name +":"+data.pageList[index].page_size);
        	        				exsoft.common.file.prototype.wUploadObj.dbFileNames.push(data.pageList[index].page_name);
        	        				totalPageSize +=  data.pageList[index].page_size;
	        					});

	        					pageCnt = data.pageList.length;

	        				}//첨부파일 처리 END

    	        			// 파일 플러그인 설정값 변경처리
    	        			exsoft.common.file.prototype.wUploadObj.updateMaxFileCounter(exsoft.common.file.prototype.wUploadObj.MaxFileCounter()-pageCnt);
    	    				exsoft.common.file.prototype.wUploadObj.updateMaxFileSize(exsoft.common.file.prototype.wUploadObj.MaxFileSize()-totalPageSize);


	        				// FORM 히든값 설정
	        				exsoft.document.commDocUBinder.set("ref_id", data.documentVO.ref_id);
	        				exsoft.document.commDocUBinder.set("root_id", data.documentVO.root_id);
	        				exsoft.document.commDocUBinder.set("doc_type", data.documentVO.doc_type);
	        				exsoft.document.commDocUBinder.set("folder_id", data.documentVO.folder_id);
	        				exsoft.document.commDocUBinder.set("map_id", data.documentVO.map_id);
	        				exsoft.document.commDocUBinder.set("version_no", data.documentVO.version_no);
	        				exsoft.document.commDocUBinder.set("owner_id", data.documentVO.owner_id);
	        				exsoft.document.commDocUBinder.set("acl_id", data.documentVO.acl_id);
	        				
	        			}else {
	        				jAlert(data.message,'확인',7);
	        				return false;
	        			}

        		});
        	},

        	initFDdslick : function(){
    			//검색 selectbox
    			exsoft.util.common.ddslick('#register_docType', 'srch_type1', 'doc_type', 120, function(divId, selectedData){
    			
    				exsoft.document.commDocBinder.set("doc_type", selectedData.selectedData.value);
    				exsoft.document.event.setExtendTypeAttrItem(selectedData.selectedData.value);
    			});
    			
    			// 보존년한 selectbox
    			exsoft.util.common.ddslick('#register_preservationyear', 'srch_type1', 'preservation_year', 58, function(){});
    			// 조회등급 selectbox
    			exsoft.util.common.ddslick('#register_accessgrade', 'srch_type1', 'access_grade', 100, function(){});
    			
    			// 좌측메뉴 문서등록 클릭시 조회등륵 사원으로 세팅
    			exsoft.document.commDocBinder.set("access_grade", "140");

    		},
    		
        	initDdslick : function(){
    			//검색 selectbox
    			exsoft.util.common.ddslick('#register_docType', 'srch_type1', 'doc_type', 120, function(divId, selectedData){
    			
    				exsoft.document.commDocBinder.set("doc_type", selectedData.selectedData.value);
    				//문서유형에 맞는 확장 속성을 표시 한다.
    				// 1.폴더에 문서등록 권한이 있는지 체크한다.
    				exsoft.document.event.setExtendTypeAttrItem(selectedData.selectedData.value);
    			});
    			// 보존년한 selectbox
    			exsoft.util.common.ddslick('#register_preservationyear', 'srch_type1', 'preservation_year', 58, function(divId, selectedData){

    				exsoft.document.commDocBinder.set("preservation_year", selectedData.selectedData.value);
    			});
    			// 조회등급 selectbox
    			exsoft.util.common.ddslick('#register_accessgrade', 'srch_type1', 'access_grade', 100, function(divId, selectedData){
    				exsoft.document.commDocBinder.set("access_grade", selectedData.selectedData.value);
    			});
    			
    			// 업무문서함 등록버튼 클릭시 조회등륵 사원으로 세팅
    			exsoft.document.commDocBinder.set("access_grade", "140");

    		},

    		initUDdslick : function(){ 
    			// 보존년한 selectbox
    			exsoft.util.common.ddslick('#modify_doc_preserve', 'srch_type1', 'preservation_year', 58, function(divId, selectedData){
    				exsoft.document.commDocUBinder.set("preservation_year", selectedData.selectedData.value);
    			});
    			// 조회등급 selectbox
    			exsoft.util.common.ddslick('#modify_doc_inquiry', 'srch_type1', 'access_grade', 100, function(divId, selectedData){
    				exsoft.document.commDocUBinder.set("access_grade", selectedData.selectedData.value);
    			});

    		},
    		initUSetDdslick : function(type){ 
    			// 보존년한 selectbox
    			exsoft.util.common.ddslick('#modify_doc_preserve', 'srch_type1', 'preservation_year', 58, function(divId, selectedData){

    				exsoft.document.commDocUBinder.set("preservation_year", selectedData.selectedData.value);
    			});
    			// 조회등급 selectbox
    			exsoft.util.common.ddslick('#modify_doc_inquiry', 'srch_type1', 'access_grade', 100, function(divId, selectedData){
    				exsoft.document.commDocUBinder.set("access_grade", selectedData.selectedData.value);
    			});
    		}

		},

		// 협업관련
		processView : function(processId) {
			exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({actionType:Constant.ACTION_VIEW, process_id:processId},exsoft.contextRoot + '/process/processControl.do',null,function(data, param){
    			if(data.result == 'true'){
    				var processVo = data.processVo;
        			var processExecutorList = data.processExecutorList;
        			var user_id = exsoft.user.user_id;

        			// 협업 기본 정보 set
        			$('#docProcessView_info tr:nth-child(1)').find('span').text(processVo.name);					// 업무명
        			$('#docProcessView_info tr:nth-child(2)').find('span').text(processVo.creator_name);			// 업무요청자
        			$('#docProcessView_info tr:nth-child(3)').find('span').text(processVo.expect_date);			// 업무완료 예정일
        			$('#docProcessView_info tr:nth-child(4)').find('span').text(processVo.content);				// 업무 요청 내용
        			$('#docProcessView_info tr:nth-child(5)').find('span').text('');
        			$('#docProcessView_info tr:nth-child(6)').find('span').text('');
        			$('#docProcessView_info tr:nth-child(7)').find('span').text('');
        			$('#docProcessView_info tr:nth-child(8)').find('span').text('');

        			// 협업자 정보 set
        			var isModifyNdelete = false;
        			$(processExecutorList).each(function(idx){
        				var executor_name = this.executor_name;
        				var executor_id = this.executor_id;
        				switch (this.type) {
    						case Constant.PROCESS.TYPE_AUTHOR:exsoft.document.doFunction.setExecutorName(5, executor_name);break;
    						case Constant.PROCESS.TYPE_COAUTHOR:exsoft.document.doFunction.setExecutorName(6, executor_name);break;
    						case Constant.PROCESS.TYPE_APPROVER:exsoft.document.doFunction.setExecutorName(7, executor_name);break;
    						case Constant.PROCESS.TYPE_RECEIVER:exsoft.document.doFunction.setExecutorName(8, executor_name);break;
    						default:break;
						};
        			});


        			// 승인자, 수신자 tooltip 설정
        			exsoft.document.doFunction.setTooltip('processView_approver', 'processView_approverTooltip', processVo, Constant.PROCESS.TYPE_APPROVER);
        			exsoft.document.doFunction.setTooltip('processView_receiver', 'processView_receiverTooltip', processVo, Constant.PROCESS.TYPE_RECEIVER);

    				// 처리현황
        			exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:processId}, exsoft.contextRoot+'/document/documentCommentList.do', null, function(data, param){
            			$('.approvalResult_cnt').text(data.records);
            			$('#docProcessView_situation').empty();

            			if(data.result == 'true'){
            				$(data.list).each(function(idx){
            					var liContent = '<li class="approvalResult_list">';
            					    liContent += '<p><span class="bold">'+this.creator_name +'</span>';
            					    liContent += '<span>' + this.create_date + '</span></p>';
            					    liContent += '<p>' + this.content+'</p></li>'
            					$('#docProcessView_situation').append(liContent);
            				});
            			}else{
            				$('#docProcessView_situation').append('<li class="approvalResult_list"><p>등록된 데이터가 없습니다.</p></li>');
            			}
            		});

    			}else{
    				jAlert(data.message,'확인',8);
    			}


    		});
		},

		// 협업관련
		doFunction : {
			// 협업자 정보 set
        	setExecutorName : function(idx, name){
        		var currentText = $('#docProcessView_info tr:nth-child('+idx+')').find('span').text();
        		$('#docProcessView_info tr:nth-child('+idx+')').find('span').text(function(idx){
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
		},

		open : {
			// 메일 수신자 선택
			userSelectUrl : "/document/reciverUserSelect.do",
			userSelectTarget : "mailReciverUserFrm",

			// 쪽지보내기 사용자선택
			docVersionDetailUrl : "/document/docVersionDetail.do",
			docVersionDetailTarget : "docVersionDetailFrm",

			// 메일수신자 선택 - 창열기
			reciverDetail : function() {
				this.contextRoot = exsoft.contextRoot;
				this.openWindow(this.userSelectTarget,760,630);
				this.formUserInit(document.mailReciverUserFrm,this.contextRoot+this.userSelectUrl,this.userSelectTarget);
			},
			// 사용자 환경설정 새창 CALL
			versionDetail : function(docId) {
				this.contextRoot = exsoft.contextRoot;
				this.openWindow(this.docVersionDetailTarget,680,450);
				this.formInit(document.docVersionDetailFrm,this.contextRoot+this.docVersionDetailUrl,docId,this.docVersionDetailTarget);
			},
			// 새창 띄우기
			openWindow : function(targetName,width,height) {
				var win= "";
				win = window.open("",targetName,"width="+width+", height="+height+", toolbar=no, menubar=no, scrollbars=no, resizable=no" );
				win.focus();			// 새창의 경우 항상 맨위로
			},

			formInit : function(formName,url,docId,targetName)	{

				var frm = formName;
				frm.action = url;
				frm.method = "post";
				frm.target = targetName;
				frm.docId.value = docId;
				frm.submit();
			},

			formUserInit : function(formName,url,targetName)	{

				var frm = formName;
				frm.action = url;
				frm.method = "post";
				frm.target = targetName;
				frm.submit();
			},

		},

		layer : {
			// 문서상세조회 Layer OPEN
			docCommonFrm : function(wrapperClass,layerClass,docId) {

				exsoft.document.init.docDetailInit(docId);

			},
			// 문서등록 Layer OPEN
			docWriteCommonFrm : function(wrapperClass,layerClass,folder_id, folder_path,calledpage,isLeft) {
				exsoft.document.init.docWriteInit(folder_id,folder_path,calledpage,isLeft);
			},
			// 문서수정 Layer OPEN
			docUpdateCommonFrm : function(wrapperClass,layerClass,doc_id) {
				if($("#is_locked_val").val() == "T") {
    				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
    				return false;
				}
				exsoft.util.common.formClear('documentUpdate');

				exsoft.document.commDocUBinder = new DataBinder("#documentUpdate");
				exsoft.document.commDocUBinder.set("actionType", Constant.ACTION_UPDATE);
				exsoft.document.actionType = 'U';

        		// 문서유형 select-box
				exsoft.document.init.initUDdslick();
        		$("#documentUpdate").validation.clearShadowBox();
        		// 파일 관련
        		exsoft.common.file.init.initSettings('documentupdatefileuploader', exsoft.document.callback.fileUupload);


        		if(doc_id=="" || doc_id.length ==0){
        			// 권한 충족 여부 확인 (문서 ACL 권한이 버튼 요구 레벨 이상이어야 한다)
        			if((exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel)) < (exsoft.util.common.getAclLevel("UPDATE"))) {
            			jAlert("문서 수정 권한이 없습니다.",'수정',6);
            			return;
            		}else{

            			//문서 상세에서 불렸을때 문서 상세 창닫기
            			exsoft.document.close.closeWindow('doc_detail_wrapper','doc_detail');

    					exsoft.document.commDocUBinder.set("doc_id",exsoft.document.prototype.gDocId);
            		}
				}else{
					exsoft.document.commDocUBinder.set("doc_id",doc_id);
				}
				exsoft.document.commDocUBinder.set("isType","update");

				exsoft.document.init.docUpdateInit();
				//exsoft.util.layout.divLayerOpen(wrapperClass,layerClass,true);
			},
		},

		event : {

			// 관련문서 화면 출력처리
			refDocData :function(refDocumentList) {

				var buffer = "";
				for(var i=0;i<refDocumentList.length;i++) {
					exsoft.document.uRefDoc++;
					buffer += "<tr id='{0}'><td><input type='checkbox'  name='uRefDocIdx' id='uRefDocIdx' value='{0}'/></td>".format(refDocumentList[i].doc_id);
					buffer += "<td><a href=\"javascript:exsoft.document.open.versionDetail('{0}');\">{1}</td>".format(refDocumentList[i].doc_id, refDocumentList[i].doc_name);
					buffer += "<td>{0}</td>".format(refDocumentList[i].creator_name);
					buffer += "<td>{0}</td>".format(refDocumentList[i].create_date);
					buffer += "</tr>";
				}

				$('#uRefDocTable').append(buffer);
			},

			//최근 문서 가져오기
			getRecentlyDocumentList : function(isLeft) {
				var mapId = null;
				if(isLeft==true){
				if(exsoft.util.layout.currentTopMenu()==Constant.TOPMENU.MYWORK){
					mapId = Constant.MAP_MYPAGE;
				}else if(exsoft.util.layout.currentTopMenu()==Constant.TOPMENU.WORKSPACE ){
					mapId = Constant.MAP_MYDEPT;					
				}
			}
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"mapId" : mapId}, exsoft.contextRoot+"/document/recentlyDocumentList.do", '', function(data, param){
					if (data.result == "true") {	
						exsoft.document.ui.recentDocumentList(data.recentlyDocumentList);
					} else {
						jAlert(data.message,'확인',7);
					}
				});
			},
			//최근 문서 삭제하기
			deleteRecentlyDocument : function(idx) {
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({idx : idx}, exsoft.contextRoot+"/document/recentlyDocumentDelete.do", idx, function(data, deleteIdx){
					if (data.result == "true") {
						exsoft.document.ui.removeRecentDocument(deleteIdx);
					} else {
						jAlert(data.message,'확인',7);
					}
				});
			},
			
			
			// 폴더찾기 호출 : 기본폴더 선택
			selectFolderFind : function() {
				
				var workType = "";
				var map_id = exsoft.document.commDocBinder.get("map_id");
				if(map_id == Constant.MAP_MYPAGE){
					workType = Constant.WORK_MYPAGE;
				}else if(map_id ==Constant.MAP_MYDEPT){
					workType = Constant.WORK_MYDEPT;
				}else if(map_id ==Constant.MAP_PROJECT){
					workType = Constant.WORK_PROJECT;
				}	
				selectSingleFolderWindow.init(exsoft.document.callback.folderFind,map_id,workType,true,"ALL_TYPE");

			},

			// 관련문서 창 호출
			selectRelDocWindow : function(actionType) {
				if(actionType =="I"){
					selectrelativeDocWindow.init(exsoft.document.callback.relDocWindow);
				}else{
					selectrelativeDocWindow.init(exsoft.document.callback.relUDocWindow);
				}
			},

			// 문서 권한 변경 버튼 클릭시
        	changeDocumentAcl : function(actionType) {
        		var obj = {
    					current_acl_id : "",
    					current_acl_name : "",
    					parent_folder_id : "",
    					folder_id : "",
    					type : "document"
    				};
        		//문서 등록
        		if(actionType=="I"){
        			obj.current_acl_id = exsoft.document.commDocBinder.get("acl_id");
            		if(obj.current_acl_id == null || obj.current_acl_id == ''){
            			jAlert('기본폴더 먼저 선택하세요.','폴더선택',6);
            			return false;
            		}else{
            			selectAclWindow.initDocument(obj.current_acl_id, Constant.ACL.TYPE_DOC, exsoft.document.exAclItems,exsoft.document.callback.selectAcl);
            		}

            		obj.folder_id = exsoft.document.commDocBinder.get("folder_id");
        			obj.current_acl_name = $("#wAclName").html();
        		//문서 수정
        		}else{
        			obj.current_acl_id= exsoft.document.commDocUBinder.get("acl_id");
            		if(obj.current_acl_id == null || obj.current_acl_id == ''){
            			jAlert('기본폴더 먼저 선택하세요.','폴더선택',6);
            			return false;
            		}else{
            			selectAclWindow.initDocument(obj.current_acl_id, Constant.ACL.TYPE_DOC, exsoft.document.exAclItems,exsoft.document.callback.selectuAcl);
            		}

            		obj.folder_id = exsoft.document.commDocUBinder.get("folder_id");
        			obj.current_acl_name = $("#uAclName").html();
        		}
        		
        		selectAclWindow.initInherit(obj);
        	},
			// 문서 유형에 따른 확장속성 표시  click.ddslick
        	setExtendTypeAttrItem : function(selectValue){
        		var jsonObject = {"type_id":selectValue,"is_extended":"T"};
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject,exsoft.contextRoot+'/type/attrList.do', '#documentWrite_docAttrView', function(data, param){
					exsoft.util.table.tableExtendTypeItemPrintList('documentWrite_docAttrView', data.list, exsoft.document.actionType);
					 if(data.records != 0){
						 $(param).removeClass('hide');
						 exsoft.document.commDocBinder.set("is_extended", 'T');

						// table에 select box가 존재하면 ddslick을 해준다.
	    				var $extendType = $('#documentWrite_docAttrView tbody').find('input, select');
	    				$($extendType).each(function(idx){
	    					var name = $(this).attr('name');

	    					if($(this).is('select')){
	    						$(this).attr('id', name);
	    						$(this).attr('data-select', 'true');
	    						exsoft.util.common.ddslick(name,'srch_type1',name,120, function(divId, selectedData){
	    							exsoft.document.commDocBinder.set(name, selectedData.selectedData.value);
	    						});
	    					}else{
	    						$(this).attr('data-bind', name);
	    					}
	    				});
	    				exsoft.document.commDocBinder.bindingElement(); // data-bind 전체 bind

					 }else{
						 $(param).addClass('hide');
						 exsoft.document.commDocBinder.set("is_extended", 'F');
					 }

				}); // 확장속성 set
        	},
        	// 문서 유형에 따른 확장속성 표시  click.ddslick
        	setExtendUTypeAttrItem : function(selectValue){
        		var jsonObject = {"type_id":selectValue,"is_extended":"T"};
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject,exsoft.contextRoot+'/type/attrList.do', '#documentUpdate_docAttrView', function(data, param){
					exsoft.util.table.tableExtendTypeItemPrintList('documentUpdate_docAttrView', data.list, exsoft.document.actionType);
					 if(data.records != 0){
						 $(param).removeClass('hide');
						 exsoft.document.commDocUBinder.set("is_extended", 'T');

						// table에 select box가 존재하면 ddslick을 해준다.
	    				var $extendType = $('#documentUpdate_docAttrView tbody').find('input, select');
	    				$($extendType).each(function(idx){
	    					var name = $(this).attr('name');

	    					if($(this).is('select')){
	    						$(this).attr('id', name);
	    						$(this).attr('data-select', 'true');
	    						exsoft.util.common.ddslick(name,'srch_type1',name,120, function(divId, selectedData){
	    							exsoft.document.commDocUBinder.set(name, selectedData.selectedData.value);
	    						});
	    					}else{
	    						$(this).attr('data-bind', name);
	    					}
	    				});
	    				exsoft.document.commDocUBinder.bindingElement(); // data-bind 전체 bind

					 }else{
						 $(param).addClass('hide');
						 exsoft.document.commDocUBinder.set("is_extended", 'F');
					 }

				}); // 확장속성 set
        	},

			// 다차원분류 선택
			registDocSelectMultiFolderFind : function(actionType) {
				if(actionType =="I"){
					var doc_type = exsoft.document.commDocBinder.get("folderIsType");
					var map_id = exsoft.document.commDocBinder.get("map_id");
					selectMultiFolderWindow.init(exsoft.document.event.registDocMultiFolderFind, map_id, "WORK_MYDEPT", true, doc_type);
				}else{
					var doc_type = exsoft.document.commDocUBinder.get("folderIsType");
					var map_id = exsoft.document.commDocUBinder.get("map_id");
					selectMultiFolderWindow.init(exsoft.document.event.updateDocMultiFolderFind, map_id, "WORK_MYDEPT", true, doc_type);
				}
			},

			// 다차원 분류 선택
			registDocMultiFolderFind :  function(obj)	{
				//$("#multiFolder").empty(); // 다차원분류 선택 폴더 초기화
				exsoft.document.event.multiFolderAdd(obj,'documentWrite',document.documentWrite.folder_id.value,'multiFolder');
			},
			// 다차원 분류 선택 (수정)
			updateDocMultiFolderFind :  function(obj)	{
				exsoft.document.event.multiFolderAdd(obj,'documentUpdate',document.documentUpdate.folder_id.value,'uMultiFolder');
			},

			/**
			 * 문서등록/수정 다차원 분류 추가
			 * @param folderArr - 다차원분류 선택 폴더 리스트 Array
			 * @param divIds - 다차원분류 ID
			 * @param defaultFolderValue - 기본폴더ID
			 * @param formId - 폼ID
			 */
			multiFolderAdd : function(obj,formId,defaultFolderValue,divIds) {
				var buffer = "";
			
				$.each(obj, function(index, result) {

					// 기본폴더가 아니고 이미 추가된 다차원 분류가 아닌 경우에만 입력처리한다.
					if(!exsoft.document.event.chkMultiFolderList(formId,'multi_folder',this.id) && this.id != defaultFolderValue )	{

						var divNames = exsoft.util.common.uniqueId();
						
						buffer += "<li id='"+divNames+"'>";
						buffer += "<input type='hidden' name='multi_folder' value='"+this.id+"'>";
						buffer += "<input type='text' style='width:95%;' name='multi_folder_path' readonly value='"+this.fullPath.join("/")+"'>";
						buffer += "<img src='"+exsoft.contextRoot+"/img/icon/recent_doc_del.png' onclick=\"javascript:exsoft.util.common.removeDivIds('"+divNames+"');\">";
						
						buffer += "</li>";
					}

				});

				$(".doc_classification_list").removeClass("hide");
				$('#'+divIds).append(buffer);
			},


			/**
			 * 문서 등록/수정 다차원 분류 중복 체크
			 * @param formId
			 * @param inputName
			 * @param value
			 * @returns {Boolean}
			 */
			chkMultiFolderList : function(formId,inputName,value){

				var ret = false;

				$("#"+formId + " "+ " input[name='"+inputName+"']").each(function() {
					if(this.value == value)	{
						ret = true;
						return false;		// break
					}
				});

				return ret;
			},
			// 문서 수정 취소버튼 처리
			docUpdateCancel : function() {
				jConfirm('수정작업을 취소하시겠습니까? <br>"확인" 선택시 잠금상태가 해제됩니다.', "확인", 6, function(r){
					if(r){
						// CANCEL_CHECKOUT 처리후
						var jsonArr = [];
						var jsonArrIndex = 0;
						var jsonData = {doc_id:"",root_id:"",is_locked:"",doc_type:""};

						 jsonData['doc_id'] =  document.documentUpdate.doc_id.value;
						 jsonData['root_id'] =  document.documentUpdate.root_id.value;
						 jsonData['is_locked'] = "T";	// (주)문서의 현재상태
						 jsonData['doc_type'] =  document.documentUpdate.doc_type.value;

						 jsonArr[0] = jsonData;

						 exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({"type":"CANCEL_CHECKOUT", "docList":JSON.stringify(jsonArr),"doc_id":document.documentUpdate.doc_id.value},
								exsoft.contextRoot+"/document/documentControl.do", "cancel_checkout",
								function(data,e)	{
							 		exsoft.document.close.layerClose(true,'doc_modify_wrapper','doc_modify');
						});
					}else {
						exsoft.document.close.layerClose(true,'doc_modify_wrapper','doc_modify');
					};
				});


			},

			//1-1. 첨부파일 출력
        	printPageList : function(data) {
        		//URL 메일송부를 위한 파일
        		exsoft.document.prototype.gPageList = data.pageList;
        		
        		//1. 파일 카운트 처리 data.pageList.length
    			$('.attach_cnt').text(data.pageList != undefined ? data.pageList.length : 0);
    			// 체크아웃, 파일삭제 ture/false는 ACL에 따라 체크 해서 넣어 주세요.
    			// data에 documentVo 포함 되었는지 확인하시고 포함되었으면 Vo에서 권한 가져와 구현 하면 됩니다.
    			var checkout = false;
    			var checkdel = false;

    			if((exsoft.util.common.getAclLevel(data.documentVO.acl_level)) >= (exsoft.util.common.getAclLevel("UPDATE"))) {
    				checkout = true;
    			}
    			if((exsoft.util.common.getAclLevel(data.documentVO.acl_level)) >= (exsoft.util.common.getAclLevel("DELETE"))) {
    				checkdel = true;
    			}
    			
    			// 첨부파일이 없는 문서이면 관련버튼들을 숨김처리.
    			if(data.pageList == undefined || data.pageList.length == 0) {
    				$(".attach_docs_wrapper .download_btnGrp").addClass("hide");
    			} else {
    				$(".attach_docs_wrapper .download_btnGrp").removeClass("hide");
    			}

    			exsoft.util.table.printPageList('detail_pageList', data, 'exsoft.document.callback.pageCallback', checkout, false); // callbackname으로 넘긴다.

        	},

			//조회수(xr_document.read_cnt)update
        	updateReadCount : function(readcnt) {
        		var cnt = parseInt(readcnt) + 1;
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:exsoft.document.prototype.gDocId,readcnt:cnt}, exsoft.contextRoot+"/document/doReadCountUpdate.do", "comment",
	            		function(data, e) {
		        			if(data.result == "true")	{

							}else {
								jAlert(data.message, "확인", 8);
							}
	        		});
        	},


        	//1-2 문서 기본정보 출력
        	printDocumentVO : function(data) {
        		//데이터 초기화
        		$('#docDetailBasicInfo').empty();
        		var docVO = data.documentVO;
        		var buffer = "";

        		// 다차원분류 초기화
        		$("#multiFolder").empty();

        		// 개인문서함 > 관련문서 hide
        		if(docVO.map_id != "MYPAGE"){
        			$(".doc_detail_relative").removeClass('hide');
        		}
        		//조회수 갱신
        		exsoft.document.event.updateReadCount(docVO.read_cnt);

        		//첨부파일 총량
        		$(".attach_size").html("("+exsoft.util.common.bytesToSize(docVO.page_total,1)+")");

        		// 전역변수 설정
        		exsoft.document.prototype.getDocVO = docVO;
        		exsoft.document.prototype.gDocId = docVO.doc_id;
        		exsoft.document.prototype.gRootId = docVO.root_id == "" ? docVO.doc_id : docVO.root_id;
        		exsoft.document.prototype.gAclLevel = docVO.acl_level == "" ? "NONE" : docVO.acl_level;
        		exsoft.document.prototype.gAcl_checkoutCancel = docVO.acl_checkoutCancel;
        		exsoft.document.prototype.gFolderPath = data.folderPath;			// URL 복사

        		exsoft.document.prototype.gOwnerId = docVO.owner_id;

        		//문서이름
        		$(".title").text(docVO.doc_name);
        		
        		var preservation_year = docVO.preservation_year == "0" ? "영구" : docVO.preservation_year + " 년";

        		buffer += "<tr><th>문서명</th><td colspan='3'>"+docVO.doc_name+"</td></tr>";
        		buffer += "<tr><th>기본폴더</th><td colspan='3'>"+data.folderPath+"</td></tr>";
        	
        		buffer += "<tr><th>문서유형</th><td>"+docVO.type_name+"</td>";
        		buffer += "<th>보존연한</th><td>"+preservation_year + "</td></tr>";

        		
        		buffer += "<tr><th>보안등급</th><td>"+exsoft.util.common.findCodeName(data.securityList,docVO.security_level)+"</td>";
        		buffer += "<th>조회등급</th><td>"+exsoft.util.common.findCodeName(data.positionList,docVO.access_grade)+ "</td></tr>";

        		buffer += "<tr><th>등록자(소유자)</th><td>"+docVO.creator_name + " [" + docVO.owner_name + "]</td>";
        		buffer += "<th>등록일</th><td>"+docVO.create_date+ "</td></tr>";
        		buffer += "<tr><th>수정자</th><td>"+docVO.updater_name + "</td>";
        		buffer += "<th>수정일</th><td>"+docVO.update_date+ "</td></tr>";


        		if(docVO.map_id != "MYPAGE") {
        			if(data.multiFolderList.length > 0){
        				buffer += "<tr><th>다차원 분류</th><td colspan='3'>";
        				for(var m=0; m < data.multiFolderList.length; m++) {
        					buffer += data.multiFolderList[m].folder_path+"</br>";
        				}

        				buffer += "</td></tr>";
        			}
        		}

        		buffer += "<tr><th>키워드</th><td colspan='3'>"+docVO.keyword+"</td></tr>";

        		// DaumEditor View mode
        		buffer += "<tr><th>설명</th><td colspan='3'>";
        		buffer += "<span style='display: none'><TEXTAREA id='vtemp_content'></TEXTAREA></span>";
        		buffer += "<iframe src='' id='vIframe_editor' name='vIframe_editor' style='border: 0 solid transparent; padding: 10px 0 0; margin: 0; height: 120px; width: 99%'></iframe>";
        		       		       		
        		
        		$('#docDetailBasicInfo').append(buffer);
        		
        		$("#vtemp_content").html(docVO.doc_description.replace('&lt;','<').replace('& lt;','<').replace('&gt;', '>').replace('& gt;', '>'));
        		$('#vIframe_editor').attr("src",exsoft.contextRoot + "/editor/doc_view.jsp");
        		
        		// 확장문서유형
        		exsoft.util.table.tableRemoveAll('#documentView_docAttrView');        		
        		$('.detail_extend_wrapper').addClass('hide');
				if(docVO.is_system == "F" && data.attrList.length > 0)	{

					$('.doc_detail_extend').removeClass('hide');
					exsoft.util.table.tableExtendTypeItemPrintList('documentView_docAttrView', data.attrList,"V");
				}else{
					$('.doc_detail_extend').addClass('hide');
				 }
        		       		
        		
        		
        		
        		//상단 Version + 잠금 아이콘 표시
        		$("#detail_doc_version").html("Ver " + docVO.version_no);
        		if (docVO.is_locked != "T") {
        			$("#btn_detail_checkout, #detail_docLockIcon").hide();
        		} else {
        			$("#btn_detail_checkout, #detail_docLockIcon").show();
        			$("#lockerInfo").text("반출자 :" + docVO.lock_owner_name);
        			$("#lockerDate").text("반출일시 :" + docVO.lock_date );
        		}
        	},

        	//1-3. 관련 문서 출력
        	printRefDoc : function(data) {
        		//데이터 초기화
        		$('#docDetailRefInfo').empty();
        		var refDocList = data.refDocumentList;
        		var tableId = "#docDetailRefInfo";
        		var buffer = "";
        		exsoft.util.table.tableRemoveAll(tableId);
        		// 데이터가 없을 경우 관련문서를 표시하지 않는다
        		if(refDocList.length == 0 ){
        			$(".doc_detail_relative").addClass("hide");
        		}else{

	        		$(refDocList).each(function(index) {		// 관련 문서 갯수만큼 루프

						buffer += "<tr id='{0}'>";
						buffer += "<td><a href=\"javascript:exsoft.document.open.versionDetail('{0}');\">{1}</td>".format(refDocList[index].doc_id, refDocList[index].doc_name);
						buffer += "<td>{0}</td>".format(refDocList[index].creator_name);
						buffer += "<td>{0}</td>".format(refDocList[index].create_date);
						buffer += "</tr>";
	        		});


	        		$('#docRelativeTotal').html(" ("+refDocList.length+")");
	        		$('#docDetailRefInfo').append(buffer);

        		}
        	},

        	// 2. 문서의 모든 버전을 가져오고 화면에 표시한다
        	getDocumentVersionList : function() {
        		// 파라미터 추가
        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:exsoft.document.prototype.gDocId,table_nm:"XR_DOCUMENT"}, exsoft.contextRoot+"/document/documentVersionList.do", "version",
        			function(data, e) {

        			exsoft.document.event.printVersionList(data);
        		});
        	},

        	showContent : function(rowindex) {
				$('.preview:eq('+ rowindex +')').toggle();
			},

        	//2-1. 버전 목록 출력
        	printVersionList : function(data) {
        		//데이터 초기화
        		$("#detaildocVersionList").empty();
				$("#docVerAttach").empty();
				
				// [2004] 버전삭제, 버전복원버튼 초기화
				$("#ver_delete").removeClass("hide");
				$("#ver_back").removeClass("hide");

        		var versionList = data.docAllVersionList;
        		var buffer = "";
        		var ulId = '';
        		var verFileCntId = '';
        		
        		$(versionList).each(function(index) {
        			buffer = "";
        			verFileCntId = 'detail_versionPageCnt_' + index;
					ulId = 'detail_versionpageList_' + index;

        			if (versionList[index].is_current == "T") {
        				buffer += "<tr class='current' onClick=\"javascript:exsoft.document.event.showContent("+index+");\">";
        			}else{
        				buffer += "<tr class='' onClick=\"javascript:exsoft.document.event.showContent("+index+");\">";
        			}
					//buffer += "<input type='hidden' id='docVersionid' value='"+versionList[index].doc_id+"'>";
        			buffer += "<td>"+versionList[index].version_no+"버전</td>";// 버전
        			buffer += "<td>"+versionList[index].doc_name+"</td>";// 문서명
        			buffer += "<td>"+versionList[index].creator_name+"</td>";// 등록자
        			buffer += "<td>"+versionList[index].create_date+"</td>";// 등록일자
        			buffer += "</tr>";
        			buffer += "<tr class='preview'><td colspan='4'>";

					buffer += "<div class='doc_ver_attach'>";
					buffer += "<div class='doc_ver_title'>";
					buffer += "<span>";
					buffer += "<strong>버전파일</strong> : <span class='ver_file_cnt'>"+versionList[index].pageList.length+"</span>";
					buffer += "</span>";
					buffer += "<div class='ver_btn_wrapper'>";
					// [2003] versionDelete 메소드에 root_id 파라미터 추가
					buffer += "<button type='button' id='ver_delete' onClick=\"javascript:exsoft.document.event.versionDelete('" +versionList[index].doc_id + "', '" +versionList[index].root_id + "')\">버전삭제</button>";
					buffer += "<button type='button' onClick=\"javascript:exsoft.document.open.versionDetail('" +versionList[index].doc_id + "')\" >상세조회</button>";
					// [3000] 버전복원시 docList에 들어갈 값8가지
					buffer += "<button type='button' id='ver_back' onClick=\"javascript:documentListLayerWindow.event.versionBack('" +versionList[index].doc_id + "', '" +versionList[index].doc_name + "', '" +versionList[index].is_locked + "'," +
							" '" +versionList[index].root_id + "', '" +versionList[index].doc_type + "', '" +versionList[index].is_inherit_acl + "', '" +versionList[index].folder_id + "', '" +versionList[index].version_no + "')\" >버전복원</button>";			
					buffer += "</div></div>";
					buffer += "<div class='doc_ver_wrapper' id='docVerAttach'>";
					// 버튼 영역
					if (versionList[index].pageList != undefined && versionList[index].pageList.length > 0) {
						//파일 다운로드
    					buffer += "<div class='download_btnGrp'>";
    					buffer += "<button type='button' onclick=\"javascript:exsoft.document.event.attachToggleCheck(true,'"+ulId+"_checkbox');\">전체선택</button>";
    				 	buffer += " <button type='button' onclick=\"javascript:exsoft.document.event.attachToggleCheck(false,'"+ulId+"_checkbox');\">전체해제</button>";
    					buffer += " <button type='button' onclick=\"javascript:exsoft.document.event.viewattachSave('"+ulId+"');\">저장</button>";
//    					buffer += " <button type='button' onclick=\"javascript:exsoft.document.event.viewattachDelete('detail_versionpageList_checkbox');\">삭제</button>";
    					buffer += "</div>";

					}

					buffer += "<ul id='"+ulId+"'></ul>";
					buffer += '</div></div></td></tr>';
					$("#detaildocVersionList").append(buffer);

					// 첨부파일이 있을경우 :: 주의! tag append 전에 printPageList 호출하면 화면에 안 그려짐
    				if (versionList[index].pageList != undefined && versionList[index].pageList.length > 0) {
    					exsoft.util.table.printPageListVer(ulId, versionList[index], 'exsoft.document.callback.pageCallbackVer', false, false); // callbackname으로 넘긴다.

    				}

        		});

        		$(".preview").hide();
        		
        		// [2004] Start
        		var expired_status = exsoft.document.prototype.getDocVO.is_expired;
        		// 문서에 대한 권한이 삭제권한 이하일때
        		if (exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel) < exsoft.util.common.getAclLevel("DELETE") || expired_status == "T") {
        			$("#ver_delete").addClass("hide");
        		}
        		
        		// 문서에 대한 권한이 수정권한 이하일때
        		if (exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel) < exsoft.util.common.getAclLevel("UPDATE") || expired_status == "T") {
        			$("#ver_back").addClass("hide");
        		}
        		// [2004] End
        		
        	},
        	// 문서 삭제
        	detail_deleteDocument : function() {
        		// 권한 충족 여부 확인 (문서 ACL 권한이 버튼 요구 레벨 이상이어야 한다)
        		if (exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel) < exsoft.util.common.getAclLevel("DELETE")) {
        			jAlert("문서 삭제 권한이 없습니다.",'삭제',7);
        			return;
        		} else {
        			if(exsoft.document.prototype.getDocVO.lock_status == 'T') {
        				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
        				return false;
        			}
        			if($("#is_locked_val").val() == "T") {
        				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
        				return false;
        			}
        			var jsonArr = [{
        				doc_id : exsoft.document.prototype.getDocVO.doc_id
        				, root_id : exsoft.document.prototype.getDocVO.root_id
        				, is_locked : exsoft.document.prototype.getDocVO.lock_status
        				, doc_type : exsoft.document.prototype.getDocVO.doc_type
        				, map_id : exsoft.document.prototype.getDocVO.map_id		//[3001]

        			}];
        			documentListLayerWindow.event.documentDeleteSend(jsonArr, "DETAIL");
        		}

        	},
        	// 2-2. 특정 버전을 삭제한다
        	versionDelete : function(docId, root_id) { // [2003] root_id 파라미터 추가
        		if (docId == exsoft.document.prototype.gRootId) {
        			jAlert("기본 문서 버전은 삭제할 수 없습니다.",'경고',6);
        			return;
        		}

        		jConfirm("선택한 버전을 삭제하시겠습니까?", "확인", 2, function(r){
					if(r){
		        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:docId, root_id : root_id}, exsoft.contextRoot+"/document/deleteVersion.do", "deleteVersion",
		            		function(data, e) {
			        			if(data.result == "true")	{
									jAlert('버전을 삭제 했습니다.', "확인", 8);
									// 상세창 refresh
									exsoft.document.init.docDetailInit(data.doc_id);
								}else {
									jAlert(data.message, "확인", 7);
								}
		        		});

					};
				});

        	},

        	//3. 문서의 이력정보를 가져오고 화면에 표시한다
        	getDocumentHistoryList : function() {
        		if($('#detaildocHistoryList')[0].grid != undefined)	{
    				exsoft.util.grid.gridPostDataRefresh('detaildocHistoryList',
    						exsoft.contextRoot+'/document/docHistoryList.do', {doc_id:exsoft.document.prototype.gDocId,strLink:'javascript:exsoft.document.event.gridPage',isReference:'true',is_search:'false',page_init:'true'});
    			}else {
    				exsoft.document.event.historyGridList();
    			}
        	},

        	// 문서상세보기 :: 문서이력보기
        	historyGridList : function() {
				$('#detaildocHistoryList').jqGrid({
					url:exsoft.contextRoot+'/document/docHistoryList.do',
					mtype:"post",
					datatype:'json',
					jsonReader:{
						page:'page',total:'total',root:'list'
					},
					colNames:['일시','수행작업','작업자','버전','수행장소','비고'],
					colModel:[
					          

						{name:'action_date',index:'action_date',width:80, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'action_name',index:'action_id',width:70, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'actor_nm',index:'actor_nm',width:60, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'version_no',index:'version_no',width:50, editable:false,sortable:true,resizable:true,align:'center'},
						{name:'action_place',index:'action_place',width:80, editable:false,sortable:true,resizable:true,align:'left',
							formatter : function (cellValue, option, rowObject) {
								return cellValue.length > 15 ? cellValue.substring(0,15) + "..." : cellValue ;							
							}
						}, //[1000]
						{name:'etc',index:'etc',width:120, editable:false,sortable:false,resizable:true,align:'center',
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
					sortname : "action_date",
					sortorder:"desc",
					rowNum : 15,
					emptyDataText: "조회된 결과가 없습니다.",
					loadtext: "조회중",
					caption:'문서이력',
					postData : {doc_id:exsoft.document.prototype.gDocId,strLink:'javascript:exsoft.document.event.gridPage',isReference:'true'}
					,loadError:function(xhr, status, error) {
						exsoft.util.grid.isErrorChk(xhr);
					 }
					,loadBeforeSend: function() {
						exsoft.util.grid.gridNoDataMsgInit('detaildocHistoryList');
						exsoft.util.grid.gridTitleBarHide('detaildocHistoryList');
					}
					,loadComplete: function(data) {

						if ($("#detaildocHistoryList").getGridParam("records")==0) {
							exsoft.util.grid.gridNoRecords('detaildocHistoryList','no_data');
						}else {
							exsoft.util.grid.gridViewRecords('detaildocHistoryList');
						}

						exsoft.util.grid.gridInputInit(false);
						//exsoft.util.grid.gridResize('detaildocHistoryList','targetDocHistoryList',20,0);
						var width = jQuery("#targetDocHistoryList").width();
						$("#detaildocHistoryList").setGridWidth(width-0);
						exsoft.util.grid.gridPager("#historyGridPager",data);
					}
				});
			},

			// 페이지이동 처리(공통)
			gridPage : function(page) {
				$("#detaildocHistoryList").setGridParam({page:page,postData:{is_search:'false',page_init:'false'}}).trigger("reloadGrid");
			},

        	// 4. 문서의 댓글을 가져오고 화면에 표시한다
        	getDocumentCommentList : function() {
        		// 파라미터 추가
        		$(".opinion_cnt_wrapper").find("textarea").prop("placeholder", "의견을 입력해주세요.");
        		$(".account_nm").html(exsoft.user.user_name);

        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:exsoft.document.prototype.gRootId,table_nm:"XR_COMMENT"}, exsoft.contextRoot+"/document/documentCommentList.do", "comment",
        			function(data, e) {
        			//exsoft.util.table.tablePrintList('detaildocCommentList', data.list, false, true);
        			exsoft.document.event.printCommentList(data);
        		});
        	},

        	//4-1. 문서 댓글 목록 출력
        	printCommentList : function(data) {
        		$("#detaildocCommentList").empty();
        		//var historyList = data.docHistoryList;
        		var buffer="";
        		// 댓글 갯수만큼 루프
        		$(data.list).each(function(index) {
        			//수정
        			buffer += "<tr id='comTR'>";

        			buffer += "<input type='hidden' id='com_id' value='"+this.com_id+"'>";
        			buffer += "<input type='hidden' id='content' value='"+this.content+"'>";
        			buffer += "<input type='hidden' id='com_step' value='"+this.com_step+"'>";
        			buffer += "<input type='hidden' id='com_creator_id' value='"+this.creator_id+"'>";
        			if(this.com_order != 0) {
        				buffer += "<td>└ "+this.creator_name+"</td>"; // 이름       + 댓글표시
        			}else{
        				buffer += "<td>"+this.creator_name+"</td>"; // 이름
        			}
        			buffer += "<td>"+this.parent_creator_name+"</td>"; //  수행작업
        			buffer += "<td >"+this.content+"</td>"; //  내용
        			buffer += "<td>"+this.create_date+"</td>"; //  등록일
        			buffer += "</tr>";

        		});
        		$("#detaildocCommentList").append(buffer);
        	},

        	commentAction  : function(kbn){
    			exsoft.document.prototype.commentKbn = kbn;

    			var obj = $("#detaildocCommentList").find(".current");
    			var com_id= obj.find("input[id='com_id']").val();
    			var content= obj.find("input[id='content']").val();
    			var comstep= obj.find("input[id='com_step']").val();
    			var com_creator_id= obj.find("input[id='com_creator_id']").val();

    			if(kbn==Constant.ACTION_UPDATE){//갱신일때
    				if(exsoft.user.user_id == com_creator_id) {
    					$(".opinion_writeform").removeClass("hide");
        				$(".opinion_cnt_wrapper").find("textarea").val(content);
    				} else {
    					jAlert('의견 등록자가 아니면 수정할 수 없습니다.', "확인", 6);
    				}
    			}else if(kbn==Constant.ACTION_REPLY){//댓글일때
    				exsoft.document.prototype.commentKbn = kbn;
    				$(".opinion_writeform").removeClass("hide");
    				$(".opinion_cnt_wrapper").find("textarea").prop("placeholder", content + " 에 대한 답변");
    			}else if(kbn==Constant.ACTION_DELETE){//삭제일때
    				if(exsoft.user.user_id == com_creator_id) {
    					jConfirm("의견을 삭제하시겠습니까?", "확인", 2, function(r){
        					if(r){
        		        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:exsoft.document.prototype.gRootId,com_id:com_id,kbn:kbn,com_step:comstep}, exsoft.contextRoot+"/document/documentCommentUpdate.do", "comment",
        		            		function(data, e) {
        			        			if(data.result == "true")	{
        									jAlert('의견을 삭제 했습니다.', "확인", 8);
        									exsoft.document.event.getDocumentCommentList();
        								}else {
        									jAlert(data.message, "확인", 7);
        								}
        		        		});

        					};
        				});
    				} else {
    					jAlert('의견 등록자가 아니면 삭제할 수 없습니다.', "확인", 6);
    				}
    				
    			}

        	},

			//4-2. 문서 댓글
        	docCommentUpdate : function() {
        		var obj = $("#detaildocCommentList").find(".current");
    			var com_id= obj.find("input[id='com_id']").val();
    			var content= $(".opinion_cnt_wrapper").find("textarea").val();

    			if(exsoft.document.prototype.commentKbn==null){
    				//신규 의견 등록
    				jConfirm('의견을 등록하시겠습니까?', "확인", 6, function(r){
    					if(r){
    		        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:exsoft.document.prototype.gRootId,content:content,kbn:exsoft.document.prototype.commentKbn}, exsoft.contextRoot+"/document/documentCommentUpdate.do", "comment",
    		            		function(data, e) {
    			        			if(data.result == "true")	{
    									jAlert('의견을 등록 했습니다.', "확인", 8);
    									exsoft.document.event.getDocumentCommentList();
    									$(".opinion_cnt_wrapper").find("textarea").val("");
    									$(".opinion_cnt_wrapper").find("textarea").prop("placeholder", "의견을 입력해주세요.");
    								}else {
    									jAlert(data.message, "확인", 7);
    								}
    		        		});
    					};
    				});
    			}else if(exsoft.document.prototype.commentKbn== Constant.ACTION_UPDATE){
    				//4-2. 문서 댓글 수정
        			jConfirm('의견을 수정하시겠습니까?', "확인", 6, function(r){
        				if(r){
    		        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:exsoft.document.prototype.gRootId,com_id:com_id,content:content,kbn:Constant.ACTION_UPDATE}, exsoft.contextRoot+"/document/documentCommentUpdate.do", "comment",
    		            		function(data, e) {
    			        			if(data.result == "true")	{
    			        				jAlert('의견을 수정 했습니다.', "확인", 8);
    			        				exsoft.document.event.getDocumentCommentList();
    			        				$(".opinion_cnt_wrapper").find("textarea").val("");
    			        				$(".opinion_cnt_wrapper").find("textarea").prop("placeholder", "의견을 입력해주세요.");
    								}else {
    									jAlert(data.message, "확인", 7);
    								}
    		        		});
    					};
        			});
    			}else if(exsoft.document.prototype.commentKbn== Constant.ACTION_REPLY){
    				//4-2. 문서 답글 추가
        			jConfirm('답글을 등록하시겠습니까?', "확인", 6, function(r){
        				if(r){
    		        		exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({root_id:exsoft.document.prototype.gRootId,com_id:com_id,content:content,kbn:Constant.ACTION_REPLY}, exsoft.contextRoot+"/document/documentCommentUpdate.do", "comment",
    		            		function(data, e) {
    			        			if(data.result == "true")	{
    			        				jAlert('답글을 등록 했습니다.', "확인", 8);
    			        				exsoft.document.event.getDocumentCommentList();
    			        				$(".opinion_cnt_wrapper").find("textarea").val("");
    			        				$(".opinion_cnt_wrapper").find("textarea").prop("placeholder", "의견을 입력해주세요.");
    								}else {
    									jAlert(data.message, "확인", 7);
    								}
    		        		});
    					};
        			});
    			}
    			
    			exsoft.document.prototype.commentKbn = null;
			},


        	//문서 잠금 해제
        	Detail_DocumentUnLock : function() {
        		if(exsoft.document.prototype.gAcl_checkoutCancel == 'T') {
        			var jsonArr = [{
        				doc_id : exsoft.document.prototype.getDocVO.doc_id
        				, root_id : exsoft.document.prototype.getDocVO.root_id
        				, is_locked : exsoft.document.prototype.getDocVO.lock_status
        				, doc_type : exsoft.document.prototype.getDocVO.doc_type
        			}];
        			documentListLayerWindow.event.documentCancelCheckoutSend(jsonArr, 'DETAIL');
        		} else {
        			jAlert('반출(잠금) 해제 권한이 없습니다','반출',6);
        		}


        	},
        	//문서 복사
        	detail_copyDocument : function(){

    			if((exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel)) < (exsoft.util.common.getAclLevel("UPDATE"))) {
        			jAlert("문서 복사 권한이 없습니다.",'복사',6);
        			return false;
        		}
        		if(exsoft.document.prototype.getDocVO.lock_status == 'T') {
        			jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'복사',6);
        			return false;
        		}
        		if($("#is_locked_val").val() == "T") {
    				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
    				return false;
    			}

        		var jsonArr = [{
        			doc_id : exsoft.document.prototype.getDocVO.doc_id
        			, doc_name : exsoft.document.prototype.getDocVO.doc_name
        			, is_locked : exsoft.document.prototype.getDocVO.lock_status
        			, root_id : exsoft.document.prototype.getDocVO.root_id
        			, doc_type : exsoft.document.prototype.getDocVO.doc_type
        			, is_inherit_acl : exsoft.document.prototype.getDocVO.is_inherit_acl
        			, folder_id : exsoft.document.prototype.getDocVO.folder_id
        		}];
        		documentListLayerWindow.event.documentDetailCopy("DETAIL", jsonArr);
        	},

        	// 문서 이동
        	detail_moveDocument : function(){
    			if((exsoft.util.common.getAclLevel(exsoft.document.prototype.gAclLevel)) < (exsoft.util.common.getAclLevel("UPDATE"))) {
        			jAlert("문서 이동 권한이 없습니다.",'이동',6);
        			return false;
        		}

        		if(exsoft.document.prototype.getDocVO.lock_status == 'T') {
        			jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'이동',6);
        			return false;
        		}
        		
        		if($("#is_locked_val").val() == "T") {
    				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.",'경고',6);
    				return false;
    			}        		

        		var jsonArr = [{
        			doc_id : exsoft.document.prototype.getDocVO.doc_id
        			, doc_name : exsoft.document.prototype.getDocVO.doc_name
        			, is_locked : exsoft.document.prototype.getDocVO.lock_status
        			, root_id : exsoft.document.prototype.getDocVO.root_id
        			, doc_type : exsoft.document.prototype.getDocVO.doc_type
        			, is_inherit_acl : exsoft.document.prototype.getDocVO.is_inherit_acl
        			, folder_id : exsoft.document.prototype.getDocVO.folder_id
        		}];
        		documentListLayerWindow.event.documentDetailMove("DETAIL", jsonArr);
        		//selectSingleFolderWindow.init(documentListLayerWindow.popupFolderCallback, getMapId, getWorkType, true, getDocType);
        	},

        	// 문서 즐겨찾기 추가
        	documentAddFavorite : function(){
        		var jsonArr = [];
        		var jsonArrIndex = 0;

        		var rowData = {doc_id:"", root_id:""};

        		//jsonObject
        		rowData['doc_id'] = exsoft.document.prototype.gDocId;
        		rowData['root_id'] =  exsoft.document.prototype.gRootId;
        		if(rowData.doc_id){
        			jsonArr[jsonArrIndex] = rowData;
        		//	jsonArrIndex++;
        		}

        		if(jsonArr.length > 0) {
        			documentListLayerWindow.event.documentAddFavoriteSend(rowData);
        			return;
        		} else {
        			jAlert("즐겨찾기 문서를 구성하는 중 오류가 발생했습니다.", "즐겨찾기 추가", 7);
        		}

        	},

        	// 작업카트 추가 - 추가기능/버튼 일때 사용 (multi Selected)
        	documentTempwork : function(){
        		var jsonArr = [];
        		var jsonArrIndex = 0;


    			var rowData = {doc_id:"", root_id:""};

    			if(exsoft.document.prototype.getDocVO.lock_status == 'T') {
    				jAlert("체크아웃한 문서가 존재합니다.\n 체크인 후 다시 작업하시기 바랍니다.", "작업카트 추가", 6);
    				return false;
    			}

    			//jsonObject
    			rowData['doc_id'] = exsoft.document.prototype.gDocId;
    			rowData['root_id'] = exsoft.document.prototype.gRootId;
    			rowData['is_locked'] = exsoft.document.prototype.getDocVO.lock_status;

    			if(rowData.doc_id){
    				jsonArr[jsonArrIndex] = rowData;
    				jsonArrIndex++;
    			}
    				//}

    			if(jsonArr.length > 0){
    				documentListLayerWindow.event.documentTempworkSend(jsonArr);
    			} else {
    				jAlert("작업카트에 문서를 추가하는 중 오류가 발생했습니다.", "작업카트 추가", 7);
    			}

			},

		

        	sendOperation : function() {
        		// 문서첨부파일 처리
        		var buffer = "";
        		var params = "";

        		$("#copy_doc_name").html(exsoft.document.prototype.getDocVO.doc_name + " " + exsoft.document.prototype.getDocVO.version_no) ;
        		$("#copy_folderPath").html(exsoft.document.prototype.gFolderPath);
        		$("#copy_creator_name").html(exsoft.document.prototype.getDocVO.creator_name + " [" + exsoft.document.prototype.getDocVO.owner_name + "]");
        		$("#copy_create_date").html(exsoft.document.prototype.getDocVO.create_date);

        		$(exsoft.document.prototype.gPageList).each(function(index) {

    				if(exsoft.document.prototype.setUrlValue == 0)	{
    					params = this.page_id + "#" + "9999-12-31";
    				}else {
    					params = this.page_id + "#" + exsoft.util.date.addDate("d",exsoft.document.prototype.setUrlValue, exsoft.util.date.todayStr(),"-");
    				}
    				buffer += "<br><a href='" + exsoft.document.prototype.gServerUrl + base64Encode(params) + "'>" + this.page_name + "</a><br>";

    				params = "";

    			});

        		$("#copy_file_list").html(buffer);


        		if($(".url_email_title").html() =="URL 메일 전송"){
        			//Email유효성 체크
        			if($(".email_receiver").val().length !=0 && !exsoft.util.check.emailCheck($(".email_receiver").val()))	{
        				jAlert('이메일주소가 정확하지 않습니다. 다시 입력해주세요.','확인',6);
						return false;
					}

        			var postData = {
        					subject : "EDMS 문서 발송",//$("#email_subject").val(),
        					receiver_address : $(".email_receiver").val(),
        					messageText : $(".url_paste_cnts").html()
        			};

        			exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(postData, exsoft.contextRoot+'/common/sendURLMail.do', "sendURLMail", function(data, param) {
        				if (data.result == "success") {
        					jAlert("메일 발송 완료", "URL메일송부", 8);

        					exsoft.util.layout.popDivLayerClose('url_email');
        				} else {
        					jAlert(data.message, "URL메일송부", 7);
        					return;
        				}
        			});
        		}else{
            		exsoft.util.common.copyToClipboard('copyToUrl');
					jAlert("URL 복사 완료", "URL메일송부", 8);
					exsoft.util.layout.popDivLayerClose('url_email');

        		}


        	},
			// URL 복사 & 메일 송부
        	docDetailsendUrl : function(kbn) {
				var jsonObject = { "type":"INFO"};

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+'/document/copyUrlLink.do', 'urlInfo',
						function(data, e){
							if(data.result == 'true'){

								if(data.expired == 0)	{
									$("#urlDate1").prop("disabled",false);
								}else {
									$("#urlDate1").prop("disabled",true);
								}
								$("#urlExpired").val(data.expired);
								// URL 복사 전역변수
								exsoft.document.prototype.gUrlExpired = data.expired;
								exsoft.document.prototype.gServerUrl =data.urlInfo;

							} else {
								jAlert(data.message,'확인',7);
							}
						}
					);
				if(kbn =='mail'){
					$(".url_email").removeClass("hide");
					$(".url_email_title").html("URL 메일 전송");
					$("#urlCopyOption").removeClass('hide');
					$(".url_email_confirm").html("문서의 URL을 메일로 발송하시겠습니까?");
				}else{
					$(".url_email").removeClass("hide");
					$(".url_email_title").html("URL 복사");
					$("#urlCopyOption").addClass('hide');
					$(".url_email_confirm").html("문서의 URL을 복사하시겠습니까?");
				}
				
				
				
				
				

			},
        	// 폴더 기본 권한 set
        	setAclItem : function(acl_id){
        		exsoft.util.ajax.ajaxDataFunctionWithCallback({"acl_id" : acl_id}, exsoft.contextRoot+"/permission/aclItemList.do", "", function(data, acl_id) {
        			// 기본 접근자세팅
        			exsoft.util.table.tableDocumentAclItemPrintList('#docmentWrite_acl', data.list);

        			// 권한명 set
    				$("#wAclName").html(data.aclDetail.acl_name);
				});

        	},
        	// exRep ECM에 파일 등록을 성공하였을 경우 후 처리
        	setUploadFile : function(data){
        		// 파일 업로드 성공 처리 :: 성공한 파일개수 증가 및 성공 값 array 담기
        		exsoft.document.wFileUploadJsonArr.push({orgFile:data.orgFile,contentPath:data.contentPath,fileSize:data.fileSize,volumeId:data.volumeId});
        		exsoft.common.file.prototype.wUploadObj.upCounter += 1;
        	},

        	// 등록 취소 시 기존에 등록한 파일을 삭제 한다.
        	deleteUploadFile : function(){
        		if(exsoft.document.wFileUploadJsonArr.length >0){
        			var jsonObject = {"fileList":JSON.stringify(exsoft.document.wFileUploadJsonArr)};
        			exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot+"/common/fileDelete.do", null, function(){});
        		}
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

                				exsoft.document.close.layerClose(false,'doc_register_wrapper','doc_register');

                				/*******************************************************************
                				 * 문서등록 화면 갱신처리
                				 ********************************************************************/
                				switch(exsoft.util.layout.currentTopMenu()) {

                					case Constant.TOPMENU.MYDOC :			// 나의문서
                						exsoftMypageFunc.event.docFunctions.refreshDocumentList();
                						break;
                					case Constant.TOPMENU.MYWORK :		// 개인문서
                						exsoft.util.grid.gridRefresh('myDocList',exsoft.contextRoot+'/document/workDocumentList.do');
                						break;
                					case Constant.TOPMENU.WORKSPACE :	// 업무문서함
                						exsoft.util.grid.gridRefresh('workDocList',exsoft.contextRoot+'/document/workDocumentList.do');
                						break;
                					case Constant.TOPMENU.MAIN :			// 메인 최신문서 목록 활성화인 경우
                						if(!$("#mainNewDoc").hasClass("down"))	{
                							exsoftLayoutFunc.init.mainDocList('mainNewDocTr','NEWDOC');
                						}
                						break;

                					default: break;
                				}

                			}else {
                				jAlert(data.message,"문서등록",7);
                				exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register');	// [3004]
                			}                			
        				});
        			}else {
        				// 대상 파일을 업로드 한다.
        				exsoft.util.ajax.loadingShow();		// [3003]
        				$("#loading_message").show();
    					exsoft.common.file.prototype.wUploadObj.startUpload();
        			}

        		} 

			},



        	//문서갱신 처리
			documentUpdateSubmit : function(){
				if ($("#documentUpdate").validation()) {

        			/**********************************************************************
            		// fileCounter :: 업로드 영역에 있는 파일 수
            		// 1 : 첨부파일 없는 문서등록 처리
            		// 2이상 : 첨부파일 업로드 후 문서등록 처리
            		// upCounter :: 대상 파일 중 업로드 성공한 파일 수
            		**********************************************************************/
					// page_cnt :: 첨부파일수
					var objForm = document.documentUpdate;
					objForm.page_cnt.value = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;

        			if(exsoft.common.file.prototype.wUploadObj.fileCounter == 1 ||
        					(exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{

        				exsoft.document.commDocUBinder.set("doc_name",document.documentUpdate.doc_name.value); //타이틀
                		var jsonObject = exsoft.document.commDocUBinder.getDataToJson();
                		jsonObject.fileList = JSON.stringify(exsoft.document.wFileUploadJsonArr);
                		jsonObject.page_cnt = exsoft.common.file.prototype.wUploadObj.fileCounter - 1;
                		// 삭제할 첨부파일 리스트
                		//if(dJsonArrIndex > 0)	{
                			jsonObject.dFileList =  JSON.stringify(exsoft.document.deleteFileList);//JSON.stringify(dJsonArr);
                		//}

                		jsonObject.security_level = $("#documentUpdate input[name=security_level]:radio:checked").val();
                		jsonObject.acl_id = $("#documentUpdate input[name=acl_id]").val();
                		jsonObject.keyword = $("#documentUpdate input[name=keyword]").val();
                		jsonObject.version_type = $("#documentUpdate input[name='version_type']:checked").val();

                		// doc_description :: 문서설명
                		jsonObject.doc_description = uIframe_editor.Editor.getContent();

                		// 업무문서함에 경우
                		if($("#documentUpdate input[name=map_id]").val() != "MYPAGE"){
                		
                		
                			// is_inherit_acl :: 상위권한 상속여부
                    		jsonObject.is_inherit_acl = exsoft.document.util.getFormCheckBoxValue('documentUpdate','is_inherit_acl_chk','TF');
                    		//공유받은 문서
                    		jsonObject.is_share = exsoft.document.util.getFormCheckBoxValue('documentUpdate','is_share_chk','TF');
                    		// 추가 접근자 목록
                    		jsonObject.aclExItem_list = JSON.stringify(exsoft.document.exAclItems);  // json array 넘김

                    		// jsonRefList :: 관련문서 리스트 refDocIdx
                    	    var jsonRefArr =  exsoft.document.util.getRefDocList('uRefDocIdx');
                    	    jsonObject.jsonRefList = JSON.stringify(jsonRefArr);

                    	    // jsonMultiFolders :: 다차원 분류 리스트 : multi_folder
                    	    var jsonMultiFolderArr = exsoft.document.util.getMultiFolderList('documentUpdate','multi_folder');
                    	    jsonObject.jsonMultiFolders = JSON.stringify(jsonMultiFolderArr);
                		}
                		
                		//	문서 수정 부/주 버전 증가시 폴더 용량 체크 할때 필요한 값.	[3002]
                		var currSize = 0;
                		var currFileSize = 0;
                		$("#uAttacheTable li").each(function( index ) {
                			currFileSize = Number($(this).find('input[name=uAttacheTable_checkbox]').val().split("|")[1]);
                			
                			currSize += currFileSize;
                		});
                		
                		jsonObject.currentFileSize = currSize;


        				// 수정처리
                		exsoft.util.ajax.ajaxDataFunctionWithCallback(jsonObject ,exsoft.contextRoot + '/document/docSubmit.do',null,function(data, param){

                			if(data.result == "true")	{

                				exsoft.document.close.layerClose(false,'doc_modify_wrapper','doc_modify');

                				/*******************************************************************
                				 * 문서수정 화면 갱신처리
                				 ********************************************************************/
                				switch(exsoft.util.layout.currentTopMenu()) {

                					case Constant.TOPMENU.MYDOC :			// 나의문서
                						exsoftMypageFunc.event.docFunctions.refreshDocumentList();
                						break;
                					case Constant.TOPMENU.MYWORK :		// 개인문서
                						exsoft.util.grid.gridRefresh('myDocList',exsoft.contextRoot+'/document/workDocumentList.do');
                						break;
                					case Constant.TOPMENU.WORKSPACE :	// 업무문서함
                						exsoft.util.grid.gridRefresh('workDocList',exsoft.contextRoot+'/document/workDocumentList.do');
                						break;
                					case Constant.TOPMENU.MAIN :			// 메인 최신문서 목록 활성화이며 동일버전  수정처리가 아닌경우 목록 갱신처리
                						if(!$("#mainNewDoc").hasClass("down") && jsonObject.version_type != "SAME")	{
                							exsoftLayoutFunc.init.mainDocList('mainNewDocTr','NEWDOC');
                						}
                						break;

                					default: break;
                				}

                			}else {
                				jAlert(data.message,"문서수정",7);
                				exsoft.document.close.layerClose(true,'doc_register_wrapper','doc_register');	// [3004]
                			}
        				});
        			}else {
        				// 대상 파일을 업로드 한다.
        				exsoft.util.ajax.loadingShow();		// [3003]
        				$("#loading_message").show();		
    					exsoft.common.file.prototype.wUploadObj.startUpload();
        			}

        		}

			},


        	//파일 전체 선택, 전체 해제
        	attachToggleCheck : function(type,selectname){
        		$("input[name="+selectname+"]:checkbox").prop("checked", type);
        	},

     

        	//파일삭제
        	attachDelete : function(selectname){
        		var $chkObject = $("input[name="+selectname+"]:checkbox:checked");
        		// page_id|this.page_size|this.page_name
        		if($chkObject.length > 0){
        			$($chkObject).each(function(idx){

        				exsoft.document.event.setDeleteFilePlugin($(this).val());
        				$(this).parent().remove();

//        				// checkbox get value :: page_id|this.page_size|this.page_name
//        				var currentValues = $(this).val().split("|");
//
//        				// 삭제 대상 array에 등록
//        				var dJsonData = {page_id:""};
//        				dJsonData['page_id'] = currentValues[0];
//        				exsoftProcessWrite.deleteFileList.push(dJsonData);
//
//        				// 첨파일수 -1 카운트
//        				var pageCnt = $('.approvalRequest_attach_cnt').html();
//        				$('.approvalRequest_attach_cnt').text(pageCnt-1);
//        				$(this).parent().remove();
//
//        				// 파일 플로그인 설정값 변경처리
//        				exsoft.common.file.prototype.wUploadObj.updateMaxFileCounter(exsoft.common.file.prototype.wUploadObj.MaxFileCounter()+1);
//        				exsoft.common.file.prototype.wUploadObj.updateMaxFileSize(exsoft.common.file.prototype.wUploadObj.MaxFileSize()+currentValues[1]);
//
//        				// 기존 목록에서 삭제된 파일은 파일 추가를 할 수 있게끔 처리
//            			var fileArray = new Array();
//            			fileArray.push(currentValues[2]);
//            			exsoft.common.file.prototype.wUploadObj.removeDbExistingFileName(fileArray);

        			});

        		}else{
        			jAlert('삭제할 파일을 선택하세요.','삭제',6);
        		}
        	},
        	
        	//파일저장
        	viewattachSave : function(selectname){
        		exsoft.common.file.event.attachSave(selectname, exsoft.document.prototype.gDocId);
        	},

        	// 문서 수정 시 첨부파일 삭제 관련 파일 플로그인 작업
        	setDeleteFilePlugin : function(deleteValue) {
        		// checkbox get value :: page_id|this.page_size|this.page_name
				var currentValues = deleteValue.split("|");

				// 삭제 대상 array에 등록
				var dJsonData = {page_id:""};
				dJsonData['page_id'] = currentValues[0];
				exsoft.document.deleteFileList.push(dJsonData);

				// 첨파일수 -1 카운트
				var pageCnt = $('#uAttachCnt').html();
				$('#uAttachCnt').text(pageCnt-1);

				// 파일 플로그인 설정값 변경처리
				exsoft.common.file.prototype.wUploadObj.updateMaxFileCounter(exsoft.common.file.prototype.wUploadObj.MaxFileCounter()+1);
				exsoft.common.file.prototype.wUploadObj.updateMaxFileSize(exsoft.common.file.prototype.wUploadObj.MaxFileSize()+currentValues[1]);

				// 기존 목록에서 삭제된 파일은 파일 추가를 할 수 있게끔 처리
    			var fileArray = new Array();
    			fileArray.push(currentValues[2]);
    			exsoft.common.file.prototype.wUploadObj.removeDbExistingFileName(fileArray);
        	},
        	// 관련문서 목록 삭제
			refDocDel : function(actionType) {
				var checkboxname = "";
				var objForm = "";
				var refDocCnt = "";
				if(actionType =="I"){
					objForm = document.documentWrite;
					checkboxname = "wRefDocIdx";
					refDocCnt = exsoft.document.wRefDoc;
				} else{
					objForm = document.documentUpdate;
					checkboxname = "uRefDocIdx";
					refDocCnt = exsoft.document.uRefDoc;
				}
				if(exsoft.document.util.checkValCount(objForm,checkboxname) == 0) {
					jAlert("삭제할 관련문서가 없습니다.",'삭제',6);
					return false;
				}else {

					$('input:checkbox[name='+checkboxname+']').each(function() {
						if(this.checked){
							$("#"+this.value).remove();
							refDocCnt--;

						}
					 });

					if(refDocCnt == 0 )	{
						var buffer = "";
						buffer += "<tr><td colspan='4'>데이터가 없습니다.</td></tr>";
						$('#refDocTable').append(buffer);
						exsoft.util.common.checkboxInit(checkboxname);
					}
				}
				//관련문서 건수 세팅
				if(actionType =="I"){
					$(".relative_docs_cnt").html("("+refDocCnt+")");
				}else{
					$("#uRefDocCnt").html("("+refDocCnt+")");
				}


			},


		},//event END
		 //3. 닫기 + hide
        close : {
        	closeWindow : function(wrapperClass, layerClass) {
        		exsoft.util.layout.divLayerClose(wrapperClass,layerClass);
        	},

        	layerClose : function(isFileDelete,wrapperClass,layerClass){
        		if(isFileDelete){
        			exsoft.document.event.deleteUploadFile();
        		}
        		exsoft.common.file.prototype.wUploadObj.cancelAll();
        		exsoft.util.layout.divLayerClose(wrapperClass,layerClass);

        		//if(!isFileDelete){
					/*******************************************************************
					 * 문서등록 화면 갱신처리
					 ********************************************************************/
					switch(exsoft.util.layout.currentTopMenu()) {
	
						case Constant.TOPMENU.MYDOC :			// 나의문서
							exsoftMypageFunc.event.docFunctions.refreshDocumentList();
							break;
						case Constant.TOPMENU.MYWORK :		// 개인문서
							exsoft.util.grid.gridRefresh('myDocList',exsoft.contextRoot+'/document/workDocumentList.do');
							break;
						case Constant.TOPMENU.WORKSPACE :	// 업무문서함
							exsoft.util.grid.gridRefresh('workDocList',exsoft.contextRoot+'/document/workDocumentList.do');
							break;
						case Constant.TOPMENU.MAIN :			// 메인 최신문서 목록 활성화인 경우
							if(!$("#mainNewDoc").hasClass("down"))	{
								exsoftLayoutFunc.init.mainDocList('mainNewDocTr','NEWDOC');
							}
							break;
	
						default: break;
					}
        		//}
		 		
        		
        	},
        },

		ui : {

			recentDocumentList : function(list) {
				$("#recentDocumentList").empty();
				var buffer = "";

				$(list).each(function() {
					buffer += "<li id='{0}'>".format(this.idx);
					buffer += '<a href="javascript:exsoft.document.init.docRecentInit(\''+this.target_id+'\');">'+this.display_name+'</a>';
					buffer += "<a href='javascript:void(0);' class='recent_del' onclick='exsoft.document.event.deleteRecentlyDocument(\"{0}\")'><img src='{1}/img/icon/recent_doc_del.png'></a>".format(this.idx, exsoft.contextRoot);
					buffer += "</li>";
				});

				$("#recentDocumentList").append(buffer);
			},

			removeRecentDocument : function(idx) {
				$("#"+idx).remove();
			},


			//문서 상세보기 선택탭 영역에 따른 액션 분기
			docDetailSelectAction : function(index) {
				$("#ext_archExcel").addClass("hide"); // [2002]
				if(index==0){
					exsoft.document.init.docDetailInit(exsoft.document.prototype.gDocId);
				}else if(index==1){
					exsoft.document.event.getDocumentVersionList();
				}else if(index==2){
					exsoft.document.event.getDocumentHistoryList();
					$("#ext_archExcel").removeClass("hide"); // [2002]
				}else if(index==3){
					exsoft.document.event.getDocumentCommentList();
				}
			},

			imgExtension : function(page_name) {
	        	var ext = page_name.lastIndexOf(".");
				var extnm = page_name.substring(ext+1);
				var imgext = 'no_file.png';  // no_file.png 의미 없음 호출하는 곳에서 처리해야 함.
				if(extnm=="xls" || extnm=="xlsx"){
					imgext = "xls.png";
				}else if(extnm=="ppt"|| extnm=="pptx" ){
					imgext = "ppt.png";
				}else if(extnm=="hwp"|| extnm=="hwp" ){
					imgext = "hwp.png";
				}else if(extnm=="doc"|| extnm=="docx" ){
					imgext = "doc.png";
				}else{
					imgext = extnm+'.png';
				}
				return imgext;
        	},
        	// 버전설정 설정처리(isType:m-개인,w-업무)
			setVersionConfig : function(radioNm,isUse,isVal){// (is_use,fval,isType){ //divIds,radioNm,isUse,isVal

				/*$("input:radio[name='"+isType+"Version']:radio[value='"+fval+"']").prop("checked",true);
				if(is_use== "Y") {
					$("#"+isType+"VersionChk").prop("checked",true);
				}else {
					$("#"+isType+"VersionChk").prop("checked",false);
					$("input:radio[id='"+isType+"Version']").each(function(i) {
						$(this).attr("disabled", true);
					})
				}*/
				/*if(isUse == "Y"){
					$("#"+divIds).show();
				}else {
					$("#"+divIds).hide();	}*/

				$("input:radio[name='version_type']:radio[value='"+isVal+"']").prop("checked", true);
			},
			
				
		},
		util : {

			/**
			 * checkbox 선택된 개수
			 * @param objForm 폼명
			 * @param strName 체크박스명
			 * @returns {Number}
			 * @usage base.checkValCount(document.frm,'idx');
			 */
			checkValCount : function(objForm,strName) {
				var count = 0;
				for(var ii=0; ii<objForm.elements.length; ii++) {
					if(objForm.elements[ii].name == strName) {
						if(objForm.elements[ii].checked) {
							count = count+1;
						}
					}
				}
				return count;
			},

			/**
			 * 문서 등록/수정시 다차원분류 목록을 JsonArray로 반환한다.
			 * @param inputName
			 * @returns {Array}
			 */
			getMultiFolderList : function(formId,inputName){

				var jsonArr = [];
				var jsonArrIndex = 0;

				$("#"+formId + " "+  "input[name='"+inputName+"']").each(function() {
					var jsonData = {folder_id:""};
					jsonData['folder_id'] =  $(this).val();

					 if(jsonData.folder_id){
						 jsonArr[jsonArrIndex] = jsonData;
						 jsonArrIndex++;
					 }

				 });

				return jsonArr;

			},
			/***
			 * Form CheckBox 선택여부에 따른 Value 값 설정
			 * @param formId
			 * @param checkBox
			 * @param returnType
			 * @returns {String}
			 */
			getFormCheckBoxValue : function(formId,checkBox,returnType)	{

				var checked = false;

				if($("#"+formId + " "+  ":checkbox[name='"+checkBox+"']").is(":checked") ) {
					checked = true;
				}

				if(returnType == "TF")	{
					if(checked)	return "T";
					return "F";
				}else {
					if(checked)	return "Y";
					return "N";
				}

			},
			/**
			 * 문서 등록/수정시 관련문서 목록을 JsonArray로 반환한다.
			 * @param checkBox
			 */
			getRefDocList : function(checkBox){

				var jsonArr = [];
				var jsonArrIndex = 0;

				$("input:checkbox[name='"+checkBox+"']").each(function() {
					var jsonData = {root_ref_id:""};
					 jsonData['root_ref_id'] =  $(this).val();

					 if(jsonData.root_ref_id){
						 jsonArr[jsonArrIndex] = jsonData;
						 jsonArrIndex++;
					 }

				 });

				return jsonArr;
			},

		},
		callback : {

			// 문서등록 권한변경 선택 CallBack  :: 파라미터 정의는 selectAclWindow.js 참조
			selectAcl : function(aclInfo) {

				$("#wAclName").html(aclInfo.aclDetail.acl_name);
				exsoft.document.commDocBinder.set("acl_id", aclInfo.aclId);
				exsoft.document.exAclItems = aclInfo.extAclItems;
				exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_acl', aclInfo.aclItems);				// 기본권한
				exsoft.util.table.tableDocumentAclItemPrintList('docmentWrite_extAcl', aclInfo.extAclItems);		// 확장권한

			},

			// 문서수정 권한변경 선택 CallBack :: 파라미터 정의는 selectAclWindow.js 참조
			selectuAcl : function(aclInfo) {

				$("#uAclName").html(aclInfo.aclDetail.acl_name);
				exsoft.document.commDocUBinder.set("acl_id", aclInfo.aclId);
				exsoft.document.exAclItems = aclInfo.extAclItems;
				exsoft.util.table.tableDocumentAclItemPrintList('docmentUpdate_acl', aclInfo.aclItems);			// 기본권한
				exsoft.util.table.tableDocumentAclItemPrintList('docmentUpdate_extAcl', aclInfo.extAclItems);	// 확장권한

			},


			// 관련문서 목록 추가
			relDocWindow : function(returnObjects){
				var documentList = new Array();
				// 1. 선택 문서 중복제거
				$(returnObjects).each(function(index) {
					var returnDoc = this;
					var isExists = false;

					// 기존의 목록에 있는지 체크함
					$("#RefDocTable input[type=checkbox]").each(function(index) {
						if (this.value == returnDoc.doc_id) {
							isExists = true;
						}
					});

					// 중복이 아닐경우 추가할 목록에 구성함
					if (!isExists) {
						documentList.push(returnDoc);
					}
				});

				// 2. 한도 초과 확인
				if (exsoft.document.wRefDoc + documentList.length > exsoft.document.wDefaultRefDocCnt) {
					jAlert('관련문서는 최대 {0}개까지 가능합니다.'.format(exsoft.document.wDefaultRefDocCnt),'관련문서',6);
					return false;
				}

				// 3. 문서등록 초기상태인 경우나 문서등록 취소 후 다시 창을 띄운 경우
				if(exsoft.document.wRefDoc == 0)	{
					//$('#uRefDocTable tr:gt(0)').remove();
				}

				//exsoft.document.wRefDoc += documentList.length;

				// 4. 목록에 선택한 문서를 추가한다
				var buffer = "";

				$(documentList).each(function(i) {
					exsoft.document.wRefDoc++;
					buffer += "<tr id='{0}'><td><input type='checkbox'  name='wRefDocIdx' id='wRefDocIdx' value='{0}'/></td>".format(documentList[i].root_id == "" ? documentList[i].doc_id : documentList[i].root_id);
					buffer += "<td><a href=\"javascript:exsoft.document.open.versionDetail('{0}');\">{1}</td>".format(documentList[i].doc_id, documentList[i].doc_name);
					buffer += "<td>{0}</td>".format(documentList[i].creator_name);
					buffer += "<td>{0}</td>".format(documentList[i].create_date);
					buffer += "</tr>";
				});
				$(".relative_docs_cnt").html("("+exsoft.document.wRefDoc+")"); //관련문서 건수 세팅


				$('#RefDocTable').append(buffer);
			},

			// 관련문서수정 목록 추가
			relUDocWindow : function(returnObjects){
				var documentList = new Array();
				// 1. 선택 문서 중복제거
				$(returnObjects).each(function(index) {
					var returnDoc = this;
					var isExists = false;

					// 기존의 목록에 있는지 체크함
					$("#uRefDocTable input[type=checkbox]").each(function(index) {
						if (this.value == returnDoc.doc_id) {
							isExists = true;
						}
					});

					// 중복이 아닐경우 추가할 목록에 구성함
					if (!isExists) {
						documentList.push(returnDoc);
					}
				});

				// 2. 한도 초과 확인
				if (exsoft.document.uRefDoc + documentList.length > exsoft.document.wDefaultRefDocCnt) {
					jAlert('관련문서는 최대 {0}개까지 가능합니다.'.format(exsoft.document.wDefaultRefDocCnt),'관련문서',6);
					return false;
				}

				// 3. 문서등록 초기상태인 경우나 문서등록 취소 후 다시 창을 띄운 경우
				if(exsoft.document.uRefDoc == 0)	{
				}

				//exsoft.document.uRefDoc += documentList.length;

				// 4. 목록에 선택한 문서를 추가한다
				var buffer = "";

				$(documentList).each(function(i) {
					exsoft.document.uRefDoc++;
					buffer += "<tr id='{0}'><td><input type='checkbox'  name='uRefDocIdx' id='uRefDocIdx' value='{0}'/></td>".format(documentList[i].root_id == "" ? documentList[i].doc_id : documentList[i].root_id);
					buffer += "<td><a href=\"javascript:exsoft.document.open.versionDetail('{0}');\">{1}</td>".format(documentList[i].doc_id, documentList[i].doc_name);
					buffer += "<td>{0}</td>".format(documentList[i].creator_name);
					buffer += "<td>{0}</td>".format(documentList[i].create_date);
					buffer += "</tr>";

				});

				$("#uRefDocCnt").html("("+exsoft.document.uRefDoc+")"); //관련문서 건수 세팅
				$('#uRefDocTable').append(buffer);
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

			// 파일 처리 callback
			fileupload : function(files,data,xhr){
				exsoft.document.event.setUploadFile(data);
				// 전체 파일이 올라 갔을 경우
				if((exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{
					exsoft.document.event.documentSubmit();
				}
			},

			// 파일 처리 callback
			fileUupload : function(files,data,xhr){
				exsoft.document.event.setUploadFile(data);
				// 전체 파일이 올라 갔을 경우
				if((exsoft.common.file.prototype.wUploadObj.fileCounter -1) == exsoft.common.file.prototype.wUploadObj.upCounter)	{
					exsoft.document.event.documentUpdateSubmit();
				}
			},

			/**
    		 * 파일 처리 관련 websocket 호출
    		 * @param type : event 처리 type
    		 * @param pageId - 파일ID
    		 * @param pageName - 파일명
    		 */
        	pageCallback : function(type, pageId, pageName, currentObject, doc_id){
        		
        		if(type == 'view'){
        			exsoft.util.websocket.view(exsoft.document.prototype.gDocId != null ? exsoft.document.prototype.gDocId : doc_id,pageId,pageName);
        		}else if(type == 'down'){
        			exsoft.util.websocket.clearDownload();
        			exsoft.util.websocket.addToDownload(exsoft.document.prototype.gDocId,pageId,pageName);
        			exsoft.util.websocket.doDownload();
        		}else if(type == 'check_out'){
        			var versionNo = (exsoft.document.prototype.getDocVO != undefined && exsoft.document.prototype.getDocVO != null) ? exsoft.document.prototype.getDocVO.version_no : '1.0';
        			exsoft.util.websocket.checkout(exsoft.document.prototype.gDocId,pageId,pageName,versionNo,'T',exsoft.document.prototype.gOwnerId, false);
        		}else if(type == 'delete'){
        			var currentValue = $(currentObject).parent().parent().children('input:checkbox').val();
        			exsoft.document.event.setDeleteFilePlugin(currentValue);
					$(currentObject).parent().parent().remove();
        		}
        	}, // pageCallback end...
        	
        	//버전 파일보기용으로 추가
        	pageCallbackVer : function(type, pageId, pageName, currentObject, doc_id){
        	
        		if(type == 'view'){
        			exsoft.util.websocket.view(doc_id,pageId,pageName);
        		}else if(type == 'down'){
        			exsoft.util.websocket.clearDownload();
        			exsoft.util.websocket.addToDownload(doc_id,pageId,pageName);
        			exsoft.util.websocket.doDownload();
        		}else if(type == 'check_out'){
        			var versionNo = (exsoft.document.prototype.getDocVO != undefined && exsoft.document.prototype.getDocVO != null) ? exsoft.document.prototype.getDocVO.version_no : '1.0';
        			exsoft.util.websocket.checkout(doc_id,pageId,pageName,versionNo,'T',exsoft.document.prototype.gOwnerId, false);
        			
        		}else if(type == 'delete'){
        			var currentValue = $(currentObject).parent().parent().children('input:checkbox').val();
        			exsoft.document.event.setDeleteFilePlugin(currentValue);
					$(currentObject).parent().parent().remove();
        		}
        	}, // pageCallback end...
        	
        	//
        	webSocketCallback : function(action, result){
        		if (action='CHECKOUT') {
    				//편집완료 후 변경사항이 있어서 체크인 된 경우 : CHECKIN_SUCCESS or CHECKIN_FAIL
    				//편집완료 후 변경사항이 없어서 체크아웃 취소가 호출된 경우  : CANCEL_SUCCESS or CANCEL_FAIL
        			//체크아웃 싪패 시 ][CHECKOUT_FAIL]
    				//do refresh
    				if( result == 'CHECKIN_SUCCESS' || result == 'CANCEL_SUCCESS'){
    					$("#btn_detail_checkout, #detail_docLockIcon").hide();
    					$("#is_locked_val").val("F");
    					
    					//체크인 완료기 기존 버젼에 대한 파일이 갱신안되니, 창 닫고 메인을 갱신 처리함.
    					if( result == 'CHECKIN_SUCCESS'){
    						//창닫고 Refresh
	    					exsoft.document.close.closeWindow('doc_detail_wrapper','doc_detail');
	            			
	        				/*******************************************************************
	        				 * 화면 갱신처리
	        				 ********************************************************************/
	        				switch(exsoft.util.layout.currentTopMenu()) {
	
	        					case Constant.TOPMENU.MYDOC :			// 나의문서
	        						exsoftMypageFunc.event.docFunctions.refreshDocumentList();
	        						break;
	        					case Constant.TOPMENU.MYWORK :		// 개인문서
	        						exsoft.util.grid.gridRefresh('myDocList',exsoft.contextRoot+'/document/workDocumentList.do');
	        						break;
	        					case Constant.TOPMENU.WORKSPACE :	// 업무문서함
	        						exsoft.util.grid.gridRefresh('workDocList',exsoft.contextRoot+'/document/workDocumentList.do');
	        						break;
	        					case Constant.TOPMENU.MAIN :			// 메인 최신문서 목록 활성화인 경우
	        						if(!$("#mainNewDoc").hasClass("down"))	{
	        							exsoftLayoutFunc.init.mainDocList('mainNewDocTr','NEWDOC');
	        						}
	        						break;
	
	        					default: break;
	        				}
    					}
    					
    					
    				}
    				
    				if(result == 'CHECKOUT_SUCCESS'){
    					// 체크아웃 시 화면 reset
    					if (exsoft.document.prototype.getDocVO.lock_status != "T"){
    						//체크아웃 취소 버튼& 자물쇠 아이콘 표시
    						$("#btn_detail_checkout, #detail_docLockIcon").show();
            				var dt = new Date();
            				
            				// Display the month, day, and year. getMonth() returns a 0-based number.
            				var month = dt.getMonth()+1;
            				var day = dt.getDate();
            				var year = dt.getFullYear();
            				var today = year+'-'+month+'-'+day;
            				
            				$("#is_locked_val").val("T");
                    		$("#lockerInfo").text("반출자 :" + exsoft.user.user_name);
                    		$("#lockerDate").text("반출일시 :" + today );
                    		
            			}
    				}
    			}
        	}, // webSocketCallback end...
		}//callback END

}; // exsoft.document end...

exsoft.document.prototype = {
		gDocId : null,
		gRootId : null,
		gAclLevel : null,
		gAcl_checkoutCancel : null,
		getDocVO : null,
		gPageList : null,				// URL 복사
		gFolderPath : null,				// URL 복사
		gUpdateCallback : null,			// 수정 화면 전환 후 사용되는 콜백
		commentKbn : null,
		gOwnerId : null,

		gUrlExpired : null,
		gServerUrl : null,
		copy_type : null,
		setUrlValue : 0,


}; // exsoft.document.prototype end...
/***********************************************
 * Preview
 **********************************************/
exsoft.namespace("preview");
exsoft.preview = {
		binder : null,
		event : {
			getPreview : function(docId, acl_level) {

				if (exsoft.preview.binder == null) {
					exsoft.preview.binder = new DataBinder("#workDocListBody");
				}

				if(docId != "")	{
					if(exsoft.util.common.getAclLevel(acl_level) < (exsoft.util.common.getAclLevel("READ"))) {
						$(".aside_title").addClass("hide");
						$(".aside_cnts").addClass("hide");
						$(".bottom_title").addClass("hide");
						$(".bottom_cnts").addClass("hide");
						$(".aside_noread_title").removeClass("hide");
						$(".aside_noread_cnts").removeClass("hide");
						$(".bottom_noread_title").removeClass("hide");
						$(".bottom_noread_cnts").removeClass("hide");
						
						$(".bottom_cnts_wrapper .doc_detail_name, .bottom_cnts_wrapper .doc_detail_info, .bottom_cnts_wrapper .doc_detail_auth").addClass("hide");
						$(".cnts_sub_wrapper .doc_detail_name, .cnts_sub_wrapper .doc_detail_info, .cnts_sub_wrapper .doc_detail_auth").addClass("hide");
					} else {
						$(".aside_title").addClass("hide");
						$(".aside_cnts").addClass("hide");
						$(".bottom_title").addClass("hide");
						$(".bottom_cnts").addClass("hide");
						$(".aside_noread_title").addClass("hide");
						$(".aside_noread_cnts").addClass("hide");
						$(".bottom_noread_title").addClass("hide");
						$(".bottom_noread_cnts").addClass("hide");
						
						$(".bottom_cnts_wrapper .doc_detail_name, .bottom_cnts_wrapper .doc_detail_info, .bottom_cnts_wrapper .doc_detail_auth").removeClass("hide");
						$(".cnts_sub_wrapper .doc_detail_name, .cnts_sub_wrapper .doc_detail_info, .cnts_sub_wrapper .doc_detail_auth").removeClass("hide");
						
						exsoft.preview.ajax.documentDetail(docId);
					}
				}
			}
		},
		ajax : {
			documentDetail : function(docId) {
				var isNoData = true;
				var docVO = null;

				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback({doc_id:docId}, exsoft.contextRoot+"/document/documentDetail.do", "select", function(data,e) {

					// 문서에 대한 정보
					docVO = data.documentVO;
					if(docVO != null) {
						exsoft.preview.binder.set("doc_name_ko", docVO.doc_name);

						exsoft.preview.binder.set("folderPath", data.folderPath);
						exsoft.preview.binder.set("type_name", docVO.type_name);
						exsoft.preview.binder.set("preservation_year", docVO.preservation_year == "0" ? "영구" : docVO.preservation_year + "년");
						exsoft.preview.binder.set("security_level", exsoft.util.common.findCodeName(data.securityList,docVO.security_level));
						exsoft.preview.binder.set("access_grade", exsoft.util.common.findCodeName(data.positionList,docVO.access_grade));
						exsoft.preview.binder.set("creator_name", docVO.creator_name + " [" + docVO.owner_name + "]");
						exsoft.preview.binder.set("create_date", docVO.create_date);
						exsoft.preview.binder.set("updater_name", docVO.updater_name);
						exsoft.preview.binder.set("update_date", docVO.update_date);
						// 개인문서함일때는 다차원분류를 binding해주지 않음
						if(docVO.map_id != "MYPAGE") {
							exsoft.preview.binder.set("multiFolderList", exsoft.preview.functions.getMultiFolderList(data.multiFolderList));
						}
						exsoft.preview.binder.set("preview_keyword", docVO.keyword);

						var description = docVO.doc_description.replace('&lt;','<').replace('& lt;','<').replace('&gt;', '>').replace('& gt;', '>');
						// 설명
						$("#preview_content").html(description);
						$('#preview_Iframe_editor').attr("src",exsoft.contextRoot + "/editor/doc_preview.jsp");
						$('#preview_Iframe_editor2').attr("src",exsoft.contextRoot + "/editor/doc_preview.jsp");

						// 권한 Setting
						exsoft.preview.binder.set("aclName", data.aclDetail.acl_name);

						exsoft.util.table.tableDocumentAclItemPrintList('detail_docAclItemListHorizon', data.aclItemList);
						exsoft.util.table.tableDocumentAclItemPrintList('detail_docAclItemListVertical', data.aclItemList);
						exsoft.util.table.tableDocumentAclItemPrintList('detail_docExAclItemListHorizon', data.aclExItemList);
						exsoft.util.table.tableDocumentAclItemPrintList('detail_docExAclItemListVertical', data.aclExItemList);

						exsoft.preview.ui.hidePreviewSample();
					} else {
						isNoData = true;
					}

    			});

				if(isNoData) {
					exsoft.preview.binder.set("doc_name_ko", "");

					exsoft.preview.binder.set("folderPath", "");
					exsoft.preview.binder.set("type_name", "");
					exsoft.preview.binder.set("preservation_year", "");
					exsoft.preview.binder.set("security_level", "");
					exsoft.preview.binder.set("access_grade", "");
					exsoft.preview.binder.set("creator_name", "");
					exsoft.preview.binder.set("create_date", "");
					exsoft.preview.binder.set("updater_name", "");
					exsoft.preview.binder.set("update_date", "");
					exsoft.preview.binder.set("multiFolderList", "");
					exsoft.preview.binder.set("preview_keyword", "");

					// 설명
					$("#preview_content").html("");
					$('#preview_Iframe_editor').attr("src",exsoft.contextRoot + "/editor/doc_preview.jsp");

					// 권한 Setting
					exsoft.preview.binder.set("aclName", "");

					exsoft.util.table.tableDocumentAclItemPrintList('detail_docAclItemListHorizon', "");
					exsoft.util.table.tableDocumentAclItemPrintList('detail_docAclItemListVertical', "");
					exsoft.util.table.tableDocumentAclItemPrintList('detail_docExAclItemListHorizon', "");
					exsoft.util.table.tableDocumentAclItemPrintList('detail_docExAclItemListVertical', "");
				}

				exsoft.preview.ui.hidePreviewSample();
			}

		},

		functions : {
			getMultiFolderList : function(multiFolderList) {
				var _ret = "";

				$(multiFolderList).each(function() {
					_ret += "{0}<br/>".format(this.folder_path);
				});

				return _ret;
			}
		},
		ui : {
			// 화면 분할 기본 화면 숨기기
			hidePreviewSample : function() {
				$(".aside_title").addClass("hide");
				$(".aside_cnts").addClass("hide");
				$(".bottom_title").addClass("hide");
				$(".bottom_cnts").addClass("hide");
				$(".aside_noread_title").addClass("hide");
				$(".aside_noread_cnts").addClass("hide");
				$(".bottom_noread_title").addClass("hide");
				$(".bottom_noread_cnts").addClass("hide");

				$(".bottom_cnts_wrapper .doc_detail_name, .bottom_cnts_wrapper .doc_detail_info, .bottom_cnts_wrapper .doc_detail_auth").removeClass("hide");
				$(".cnts_sub_wrapper .doc_detail_name, .cnts_sub_wrapper .doc_detail_info, .cnts_sub_wrapper .doc_detail_auth").removeClass("hide");
			},
			
			// 화면분할 기본화면 보이기
			hideDocDetail : function() {
				$(".aside_title").removeClass("hide");
				$(".aside_cnts").removeClass("hide");
				$(".bottom_title").removeClass("hide");
				$(".bottom_cnts").removeClass("hide");
				$(".aside_noread_title").addClass("hide");
				$(".aside_noread_cnts").addClass("hide");
				$(".bottom_noread_title").addClass("hide");
				$(".bottom_noread_cnts").addClass("hide");

				$(".bottom_cnts_wrapper .doc_detail_name, .bottom_cnts_wrapper .doc_detail_info, .bottom_cnts_wrapper .doc_detail_auth").addClass("hide");
				$(".cnts_sub_wrapper .doc_detail_name, .cnts_sub_wrapper .doc_detail_info, .cnts_sub_wrapper .doc_detail_auth").addClass("hide");
			},
		}
}

/***********************************************
 * process
 **********************************************/
/**
 * 협업프로세스 common
 * namespace로 관리
 */
exsoft.process = {

		write : function(wrapClass, divClass){
			exsoft.util.common.formClear('processWrite');
			exsoft.util.layout.divLayerOpen(wrapClass,divClass,true);

			exsoftProcessWrite.init.initProcessWrite();
			exsoft.util.layout.lyrPopupWindowResize($('.'+divClass));
		},

		modify : function(wrapClass, divClass, process_id, doc_root_id){
			exsoft.util.common.formClear('processWrite');
			exsoft.util.layout.divLayerOpen(wrapClass,divClass,true);

			exsoftProcessWrite.init.initProcessModify(process_id, doc_root_id);
			exsoft.util.layout.lyrPopupWindowResize($('.'+divClass));
		},

}; // exsoft.process end...

exsoft.process.prototype = {

}; // exsoft.process.prototype end...

/***********************************************
 * 공통 bind 관련
 **********************************************/
/**
 * 공통 bind 관련 common
 * namespace로 관리
 */
exsoft.common.bind = {
		// 좌측 메뉴 펼치기/숨기기
		leftMenuToggle : function(){
			$("body").off("click", '.toggle_tree_menu');
			$("body").on("click", '.toggle_tree_menu', function(e){
				// 운영중 오류 날경우 $('.cnts_tbl_wrapper div:nth-child(1)).attr('id') 에서 gbox_ 만 제외하면 됨
				var tableId = $('.cnts_list div:nth-child(1) div:nth-child(3) div:nth-child(3) div:nth-child(1) table').attr('id');
				var targetId = $('.cnts_tbl_wrapper').attr('id');
				var width = $('.lnb_menu').width();

				if(!$(this).hasClass('toggle_hide')) {
					$('.contents').animate({
						'left':0,
						'width':'100%'
					}, 300, function(){
						$('.lnb_menu').addClass('hide');
						$('.toggle_tree_menu').addClass('toggle_hide');
					});
					if (typeof tableId != 'undefined' && typeof targetId != 'undefined')
						exsoft.util.grid.gridIsLeftMenuResize(tableId,targetId,28,-width);
				} else {
					$('.lnb_menu').removeClass('hide');
					$('.contents').removeAttr('style');
					$('.toggle_tree_menu').removeClass('toggle_hide');
					if (typeof tableId != 'undefined' && typeof targetId != 'undefined')
						exsoft.util.grid.gridIsLeftMenuResize(tableId,targetId,28,0);
				}
			});
		},

		//퀵메뉴 펼치기/숨기기
		quickMenuToggle : function(){
			$('a.quick_menu').unbind("click");
			$('a.quick_menu').bind("click", function(e){
				e.preventDefault();
				var target = $(this).find('span[class^="quick_menu"]');
				var div = $('.quick_sub_wrapper');

				if(!target.hasClass('toggle_hide')) {
					div.animate({width:0}, 500, function(){
						target.addClass('toggle_hide');
					});
				} else {
					div.animate({width:750}, 500, function(){
						target.removeClass('toggle_hide');
					});
				}
			});
		},

		//화면 상하좌우 분활 icon 선택(레이아웃 선택)
		layoutViewToggle : function(){
			$('body').off('click', '.layout_view a');
			$('body').on('click', '.layout_view a', function(e){
				e.preventDefault();
				var dropDown_menu = $(this).parent().find('.layout_view_dropDown');
				if(dropDown_menu.hasClass('hide')){
					dropDown_menu.removeClass('hide');
					$(this).addClass('clicked');

					var listViewType = $.cookie('listViewType');
					if(listViewType == 'undefined') {
						listViewType = "list_only";
					}
					$('.layout_view_dropDown > ul').find('li#'+listViewType).addClass('checked');
				} else {
					dropDown_menu.addClass('hide');
					$(this).removeClass('clicked');
				}
				$(".download_all").addClass("hide");
			});
		},

		//화면 상하좌우 분화에 따라 화면 보이기
		layoutViewDivide : function(){
			//테이블 메뉴 - 레이아웃 선택 드롭다운 체크 선택변경
			$('body').off('click', '[class^="layout_view_dropDown"] > ul li > a');
			$('body').on('click', '[class^="layout_view_dropDown"] > ul li > a', function(e){
				e.preventDefault();
				var li = $('.layout_view_dropDown > ul').find('li');
				var parent = $(this).parents('.layout_view_dropDown');
				li.removeClass('checked');

				$(this).parent().addClass('checked');
				exsoft.common.bind.doFunction.setListView($(this).parent().attr('id'));
				parent.addClass('hide');
			});
		},
		/**
		 * Event 관련 함수
		 */
		event : {
			// 화면 좌우 분활 시 마우스 드래그 시 화면 분활 비율 설정
			layoutDragHorizontal : function() {
				$('body').off('mousedown', '[class^="horizontal_draggable"]');
				$('body').on('mousedown', '[class^="horizontal_draggable"]', function(e){
					var cntsList = $('.cnts_list');
					var parent = $(this).parent();
					var cntsListStartWidth = cntsList.width();
					var startWidth = parent.width();
					var pX = e.pageX;

					$(document).on('mouseup', function(e){
						$(document).off('mouseup').off('mousemove');
					});

					$(document).on('mousemove', function(me){
						var mx = (me.pageX - pX);
						parent.css({width: startWidth - mx});
						cntsList.css({width: cntsListStartWidth + mx});
					});
				});
			},

			// 화면 상하 분활 시 마우스 드래그 시 화면 분활 비율 설정
			layoutDragVertical : function() {
				$('body').off('mousedown', '[class^="vertical_draggable"]');
				$('body').on('mousedown', '[class^="vertical_draggable"]', function(e){
					var cntsList = $('.cnts_list');
					var parent = $(this).parent();
					var cntsListStartHeight = cntsList.height();
					var startHeight = parent.height();
					var pY = e.pageY;

					$(document).on('mouseup', function(e){
						$(document).off('mouseup').off('mousemove');
					});

					$(document).on('mousemove', function(me){
						var my = (me.pageY-pY);
						parent.css({height:startHeight-my});
						cntsList.css({height:cntsListStartHeight+my});
					});
				});
			},

			// 문서 상세조회 의견 contextMenu
			commentCentextMenu : function(){
				$('body .opinion_wrapper > table').off('mousedown', 'tbody > tr > td');
				$('body .opinion_wrapper > table').on('mousedown', 'tbody > tr > td',function(e){
        			var context_menu = $('.opinion_contextMenu');
        			if(e.which == 3) {
        				var offsetX = e.pageX - $('.opinion_wrapper').offset().left;
        				var offsetY = e.pageY - $('.opinion_wrapper').offset().top;
        				context_menu.css({
        					left:offsetX,
        					top:offsetY
        				});

        				context_menu.removeClass('hide');
        				$(this).parents('table').find('tr').removeClass('current');
        				$(this).parent().addClass('current');
        			} else if(e.which == 1) {
        				context_menu.addClass('hide');
        			}
        		});
			},

			// div 드롭다운(동적 사용을 위해 on 사용) :: 메뉴 펼치기/숨기기
			divDropDown : function(){
				$("body").off("click", 'a[class="dropDown_txt"]');
				$("body").on("click", 'a[class="dropDown_txt"]', function(e){
					e.preventDefault();
					var span = $(this).parent().find("span");
					var divLength = $(this).parent().children('div').children('div').length;

					if(span.hasClass("down")) {
						span.removeClass("down");
						span.addClass("up");

						$(this).parent().children('div').removeClass('hide');

						if(divLength > 0) {
							$(this).next().removeClass('hide');
						}

					} else {
						span.removeClass("up");
						span.addClass("down");

						$(this).parent().children('div').addClass('hide');

						if(divLength > 0) {
							$(this).next().addClass('hide');
						}

					}
				});
			},
			// 관련문서 컨텍스트메뉴
			relativeDownloadContext : function(){
				$('.download_detail').find('a.contextMenu').bind("click", function(e){
			    	e.preventDefault();
			    	$('div[class^="relative_download_context"]').addClass('hide');
			    	$(this).parent().find('div[class^="relative_download_context"]').removeClass('hide');
				});
			},


			// 텝이동
			urlEmailClose : function(){
			    //URL메일발송 - 창 닫기
			    $('.url_email_close').bind("click", function(e){
			    	e.preventDefault();
			    	$(this).parents('.url_email').addClass('hide');
			    });
			},
			// 문서 상세조회 - 창 닫기
			docDetailWindowClose : function(){
			  $('.doc_detail_close').bind("click", function(e){
			      e.preventDefault();
			      $(this).parents('.doc_detail').addClass('hide');
			      $('.doc_detail_wrapper').addClass('hide');
			  });
			}
		},

		/**
		 * 일반함수
		 */
		doFunction : {
			// 목록 화면 분활 종류 설정
			setListView : function(listViewType){
				
				var tableId = null;
				
				switch(exsoft.menuType) {
					case "myDocMenu" :
						tableId = "mypageDocList";
						break;
					case "myWorkMenu" :
						tableId = "myDocList";
						break;
					case "workSpaceMenu":
						tableId = "workDocList";
						break;
				}

				if(listViewType == "list_only") {
					$('[class*="cnts_aside"]').addClass('hide');
					$('[class*="cnts_bottom"]').addClass('hide');

					$('[class*="cnts_list"]').css({
						width:'100%',
						height:'100%',
						'max-width':'100%',
						'max-height':'100%'
					});
					
					$('#' + tableId).jqGrid('setGridParam', {loadui:"enable"}).trigger("reloadGrid");

				} else if(listViewType == "horizontal_divide") { // 좌우 분활
					$('[class*="cnts_bottom"]').addClass('hide');
					$('[class*="cnts_aside"]').removeClass('hide');
					$('[class*="cnts_list"]').css({
						'height':'100%',
						'max-height':'100%'
					});

					var cntsListWidth = $('[class*="cnts_list"]').width();
					var cntsListMinWidth = parseInt($('[class*="cnts_list"]').css('min-width').replace('px', ''), 10);
					var asideWidth = $('[class*="cnts_aside"]').width();
					var tblWrapWidth = $('cnts_tbl_wrapper').width();

					$('[class*="cnts_list"]').width(tblWrapWidth - asideWidth);
					$('[class*="cnts_list"]').css({'max-width' : tblWrapWidth - 200});
					$('[class*="cnts_aside"]').css({'max-width' : tblWrapWidth - 700});
					
					$('#' + tableId).jqGrid('setGridParam', {loadui:"disable"}).trigger("reloadGrid");

				} else if(listViewType == "vertical_divide") { // 상하 분활
					$('[class*="cnts_aside"]').addClass('hide');
					$('[class*="cnts_bottom"]').removeClass('hide');
					$('[class*="cnts_list"]').css({
						'width':'100%',
						'max-width':'100%'
					});

					var cntsListHeight = $('[class*="cnts_list"]').height();
					var cntsListMinHeight = parseInt($('[class*="cnts_list"]').css('min-height').replace('px', ''), 10);
					var bottomHeight = $('[class*="cnts_bottom"]').height();
					var tblWrapHeight = $('.cnts_tbl_wrapper').height();

					$('[class*="cnts_list"]').css({
						height:tblWrapHeight - 66,
						'max-height':tblWrapHeight - 66
					});
					$('[class*="cnts_bottom"]').css({'max-height' : tblWrapHeight - cntsListMinHeight});
					
					$('#' + tableId).jqGrid('setGridParam', {loadui:"disable"}).trigger("reloadGrid");
				}

				$.cookie('listViewType', listViewType);
			},

			// 화면분활 쿠키값 보이기
			layoutViewCookie : function(){
				var listViewType = $.cookie('listViewType');
				if(listViewType == 'undefined') {
					listViewType = "list_only";
				}
				$(".download_all").addClass("hide");

				exsoft.common.bind.doFunction.setListView(listViewType);
			},

		},

}; // exsoft.common.bind end...

exsoft.common.bind.prototype = {

}; // exsoft.common.bind.prototype end...

/***********************************************
 * file upload 관련
 **********************************************/
/**
 *  file upload 관련 common
 * namespace로 관리
 */
exsoft.common.file = {

		wSettings : null,
		wMaxFileSize : 2048,
		wTotalFileSize : 4096,
		wMaxFileCount : 10,
		wExtList : 'exe;bat;dll;ocx;',

		init : {
			initSettings : function(id, callback){
				exsoft.common.file.prototype.fileuploadDivId = id;
				// 환경설정 적용값:: 파일등록개수/파일사이즈/전체사이즈/확장자목록
				exsoft.util.ajax.ajaxDataFunctionWithCallback(null,exsoft.contextRoot + '/common/configFileInfo.do',null,function(data, param){
					exsoft.common.file.wMaxFileSize =  exsoft.util.common.fileConfig(data.FILESIZE.skey, data.FILESIZE.is_use, data.FILESIZE.sval, exsoft.common.file.prototype.wDefaultFileSize);
					exsoft.common.file.wTotalFileSize = exsoft.util.common.fileConfig(data.FILETOTAL.skey, data.FILETOTAL.is_use, data.FILETOTAL.sval, exsoft.common.file.prototype.wDefaultFileTotal);
					exsoft.common.file.wMaxFileCount = exsoft.util.common.fileConfig(data.FILECNT.skey, data.FILECNT.is_use, data.FILECNT.sval, exsoft.common.file.prototype.wDefaultFileCnt);
					exsoft.common.file.wExtList =  exsoft.util.common.fileConfig(data.EXT.skey, data.EXT.is_use, data.EXT.sval,"*");

					exsoft.common.file.wSettings = null;
					exsoft.common.file.prototype.wUploadObj = null;

					exsoft.common.file.wSettings = {
							url: exsoft.contextRoot+"/common/fileUpload.do",
							multiple:true,													// multiple file selection is allowed.
							autoSubmit:false,
							dragDrop:true,
							fileName: "wFiles",
							formData: {"uniqStr":exsoft.util.common.uniqueId()},
							maxFileSize:exsoft.common.file.wMaxFileSize,					// 파일최대크기(1GB=1024*1000*1000) :: -1 제한없음
							totalFileSize:exsoft.common.file.wTotalFileSize,				// 총파일사이즈제한 :: -1 제한없음
							maxFileCount:exsoft.common.file.wMaxFileCount,					// Allowed Maximum number of files to be uploaded :: -1 제한없음
							allowedTypes:exsoft.common.file.wExtList,						// 허용하지 않는 확장자 리스트 :: * 제한없음
							returnType:"json",
							onSuccess:function(files,data,xhr){
								callback(files,data,xhr);
							},
							onError: function(files,status,errMsg)
							{
								exsoft.util.ajax.loadingHide(); //[1006]
								$("#loading_message").hide();
							},
							showProgress:true,
							showDone: false,
							showError: true,												// 에러메세지 출력(파일사이즈,파일개수)
							multiDragErrorStr: "드래그 & 드롭 파일은 허용되지 않습니다.",
							extErrorStr:"허용되지 않는 확장자입니다.",
							sizeErrorStr:"파일당 최대크기를 초과하였습니다.",
							totalSizeErrorStr:"파일최대크기를 초과하였습니다.",
							uploadErrorStr:"업로드를 실패하였습니다.",
							serverErrorStr : "서버URL 주소가 잘못되었거나 서버가 응답하지 않습니다.",
							duplicateErrorStr : "동일한 파일이 존재합니다.",
							dragdropWidth: 150 // file box 넓이
					};

					//파일추가 영역이 호출 시 지속적으로 생김. dragDrop:true일 경우 기존 영역 삭제. false일 경우 그때 가서 고민
					$('.ajax-upload-dragdrop').remove();
					exsoft.common.file.prototype.wUploadObj = $(exsoft.util.common.getIdFormat(id)).uploadFile(exsoft.common.file.wSettings);
					exsoft.common.file.prototype.wUploadObj.cancelAll();
				}); // ajax 호출 끝
			}
		}, //init end..

		event : {

			// 파일목록 화면에서 파일 전체 선택, 전체 해제
        	attachToggleCheck : function(ulId, type){
        		$("input[name="+ulId+"_checkbox]:checkbox").prop("checked", type);
        	},
        	
        	      	

        	//파일저장
        	attachSave : function(ulId, doc_id){
        		var $chkObject = $("input[name="+ulId+"_checkbox]:checkbox:checked");

        		if($chkObject.length > 0){
        			exsoft.util.websocket.clearDownload();
        			$($chkObject).each(function(idx){
        				var arrValue = $(this).val().split('|');
            			exsoft.util.websocket.addToDownload(doc_id,arrValue[0],arrValue[2]);
            		});
        			exsoft.util.websocket.doDownload();
        		}else{
        			jAlert('저장할 파일을 선택하세요.','저장',6);
        		}
        	},

		}, //event end
}; // exsoft.common.file end...

exsoft.common.file.prototype = {
		fileuploadDivId : null,  // document, process 구분해야 함
		wUploadObj : null,

		//config.properties 값
		wDefaultFileCnt : null,
		wDefaultFileSize : null,
		wDefaultFileTotal : null,
};  // exsoft.common.file.prototype end...