/**
 * [2000][신규개발]	20150824	이재민 : 프로젝트부서 등록/수정/조회시 관리부서 추가
 * [2001][신규개발]	20150824	이재민 : 프로젝트탭 선택시 부서명이아니라 프로젝트명으로 나오게 수정
 * */

var registGroupWindow = {
		
		groupInfo : new Object(),
		groupWriteBinder : new DataBinder("#p_group_form"),

		// 0. 초기화
		init : function(fullPath, parentId, mapId) {

			// 등록버튼 클릭, 하위부서 추가 - 창 닫기
    	    $('.window_close').bind("click", function(e){
    	    	e.preventDefault();
    	    	$(this).parents('.subDept_add').addClass('hide');
    	    	$('.subDept_add_wrapper').addClass('hide');
    	    });

    	    // 등록버튼 클릭, 하위부서 추가 -  창 닫기 : 음영진 부분 클릭 시 닫기
    	    $('.subDept_add_wrapper').bind("click", function(){
    	    	$(this).addClass('hide');
    	    	$('.subDept_add').addClass('hide');
    	    });

			// 팝업창 form clear
    	    exsoft.util.common.formClear("p_group_form");

			// parameter 저장
    	    registGroupWindow.groupInfo.type = "groupWrite";
    	    registGroupWindow.groupInfo.fullPath = fullPath;
    	    registGroupWindow.groupInfo.parentId = parentId;
    	    registGroupWindow.groupInfo.mapId = mapId;
    	    registGroupWindow.groupInfo.groupNameKo = "";

    	    // DataBinder 처리
			registGroupWindow.groupWriteBinder.set("parent_id",parentId);
			registGroupWindow.groupWriteBinder.set("p_group_full_path",fullPath);
			registGroupWindow.groupWriteBinder.set("map_id",mapId);
			registGroupWindow.groupWriteBinder.set("p_group_status","C");
			registGroupWindow.groupWriteBinder.set("p_sort_index",0);
			
			// [2001] map id에 따른 화면구성 초기화
			if(mapId == "MYDEPT") {
				$("#g_name").text("부서명");
				$("#g_name_en").text("부서명(영문)");
				$("#g_full_path").text("부서경로");
				$("#tr_manage_group").addClass("hide");
				$("#tr_manage_use").addClass("hide");
			} else {
				$("#g_name").text("프로젝트명");
				$("#g_name_en").text("프로젝트명(영문)");
				$("#g_full_path").text("프로젝트경로");
				$("#tr_manage_group").removeClass("hide");
				$("#tr_manage_use").removeClass("hide");
				
				// 관리부서 사용여부 초기화
				$("#is_manage_use").prop("checked", true);
				$("#p_manage_group").val("");
				$("#btn_manage_group").prop("disabled", true);
				$("#btn_manage_group").addClass("disabled");
			}

			exsoft.util.common.ddslick('#p_group_status','use_yn','p_group_status',79, function(){});

			exsoft.util.layout.divLayerOpen("subDept_add_wrapper", "subDept_add");

			exsoft.util.filter.maxNumber();
		},

		// 1. 팝업
		open : {
		},

		//2. layer + show
		layer : {
		},

		//3. 닫기 + hide
		close : function() {
			exsoft.util.layout.divLayerClose("subDept_add_wrapper", "subDept_add");
		},

		//4. 화면 이벤트 처리
		event : {

			// 부서경로 선택
			selectGroup : function() {
				if(groupManager.mapId == "MYDEPT") {
        			selectGroupWindow.init.initPage(registGroupWindow.callback, "MYDEPT");
        		} else {
        			selectGroupWindow.init.initPage(registGroupWindow.callback, "PROJECT");
        		}
			},
			
			// [2000] 관리부서 선택
			selectManageGroup : function() {
				selectGroupWindow.init.initPage(registGroupWindow.callbackForManageGroup, "MYDEPT");
			},
			
			// [2000] 관리부서 사용여부 체크박스 클릭 (체크시 미사용)
			checkManageNoUse : function() {
				if($("#is_manage_use").is(":checked") ) {
					$("#p_manage_group").prop("disabled", true);
					$("#p_manage_group").addClass("disabled");
					$("#btn_manage_group").prop("disabled", true);
					$("#btn_manage_group").addClass("disabled");
				} else {
					$("#p_manage_group").prop("disabled", false);
					$("#p_manage_group").removeClass("disabled");
					$("#btn_manage_group").prop("disabled", false);
					$("#btn_manage_group").removeClass("disabled");
				}
			},

			// 버튼 : 그룹 저장
			registGroupSubmit : function() {

				// 필수항목 유효성 체크
				if (!registGroupWindow.event.validationForm()) {
					return;
				}

				registGroupWindow.groupWriteBinder.set("type","insert");
				registGroupWindow.groupWriteBinder.set("group_name_ko",$("#p_group_name_ko").val());
				registGroupWindow.groupWriteBinder.set("group_name_en",$("#p_group_name_en").val());
				registGroupWindow.groupWriteBinder.set("sort_index",$("#p_sort_index").val());
				registGroupWindow.groupWriteBinder.set("group_status",exsoft.util.layout.getSelectBox('p_group_status','option'));
				registGroupWindow.groupWriteBinder.set("p_group_full_path", $("#p_group_full_path").val());

				// Server Call
				var jsonObject = registGroupWindow.groupWriteBinder.getDataToJson();
				exsoft.util.ajax.ajaxDataFunctionNoLodingWithCallback(jsonObject, exsoft.contextRoot + "/admin/groupInfoManager.do", "groupWrite",
					function(data, param) {
						if(data.result == "success") {
							groupManager.event.refreshTree();		// 그릅관리 트리를 초기화 한다
						} else {
							jAlert(data.message, "그룹관리", 7);
						}
				});

				// 창닫기
				registGroupWindow.close();
			},

			// Form Validation
			validationForm : function() {

				// 부서명 체크 (group_name_ko컬럼만 NN이므로 해당 컬럼만 체크)
				if ($("#p_group_name_ko").val().length == 0) {
					jAlert("부서명을 입력해주세요.","확인", 6);
					return false;
				}

				// 정렬순서 체크
				if ($("#p_sort_index").val().length == 0) {
					jAlert("정렬순서를 입력해주세요.","확인", 6);
					return false;
				}

				return true;
			},

			// 부서명 변경 시 부서경로 변경 이벤트 핸들링
			groupNameChanged : function() {
				registGroupWindow.groupInfo.groupNameKo = $("#p_group_name_ko").val();
				$("#p_group_full_path").val(registGroupWindow.groupInfo.fullPath + " > " + registGroupWindow.groupInfo.groupNameKo);
			},

		},

		//5. 화면 UI 변경 처리
		ui : {
		},

		//6. callback 처리
		callback : function(returnGroup) {
			// 부서경로 > 선택버튼 > 그룹선택팝업 callback
			registGroupWindow.groupWriteBinder.set("parent_id",returnGroup[0].original.id);
			registGroupWindow.groupInfo.fullPath = returnGroup[0].fullPath.join(" > ");
			$("#p_group_full_path").val(registGroupWindow.groupInfo.fullPath + " > " + registGroupWindow.groupInfo.groupNameKo);
		},
		
		callbackForManageGroup : function(returnGroup) {
			// [2000] 관리부서선택 > 선택버튼 > 그룹선택팝업 callback
			registGroupWindow.groupWriteBinder.set("manage_group_id",returnGroup[0].original.id);
			$("#p_manage_group").val(returnGroup[0].fullPath.join(" > "))
		}
}