var configFavoriteFolder = {
	favoriteFolderTreeForPopup : null,
	initFavoriteTreeForPopup : false,
	
    // 0. 초기화
    init : {
    	initWindow : function() {
    		//즐겨찾기 구성 - 창 닫기
    	    $('.favorite_configChoose_close').bind("click", function(e){
    	    	e.preventDefault();
    	    	$(this).parents('.favorite_configChoose').addClass('hide');
    	    	$('.favorite_configChoose_wrapper').addClass('hide');
    	    });

    	    //즐겨찾기 구성 -  창 닫기 : 음영진 부분 클릭 시 닫기
    	    $('.favorite_configChoose_wrapper').bind("click", function(){
    	    	$(this).addClass('hide');
    	    	$('.favorite_configChoose').addClass('hide');
    	    });
    	    
    		if (configFavoriteFolder.favoriteFolderTreeForPopup == undefined) {
    			// 즐겨찾기 폴더 편집
    			treeOption = {
    					divId : "#favorite_folder_tree",
    					context : exsoft.contextRoot,
    					url : "/folder/favoriteFolderList.do",
    					contextAction : configFavoriteFolder.event.treeContextAction
    			};
    			configFavoriteFolder.favoriteFolderTreeForPopup = new XFTree(treeOption);
    			configFavoriteFolder.favoriteFolderTreeForPopup.template_context();
    			configFavoriteFolder.favoriteFolderTreeForPopup.isFavoriteFolder = true;
    			configFavoriteFolder.favoriteFolderTreeForPopup.init();
    		}
    		configFavoriteFolder.open.layerOpen();
    		
    	}
    },

    // 1. 팝업
    open : {
    	layerOpen : function() {
    		exsoft.util.layout.divLayerOpen("favorite_configChoose_wrapper", "favorite_configChoose");
    	}
    },

    //2. layer + show
    layer : {
    },

    //3. 닫기 + hide
    close : {
    },

    //4. 화면 이벤트 처리
    event : {
    	// 즐겨찾기 편집창 컨텍스트메뉴
		treeContextAction : {
			// 즐겨찾기 등록
			createFavoriteFolder : function(node) {
				exsoft.util.layout.divLayerOpen(favoriteFolderWindow.wrapperClass, favoriteFolderWindow.layerClass);
				favoriteFolderWindow.callback = configFavoriteFolder.event.refreshTree;
				favoriteFolderWindow.initForm(node);
				favoriteFolderWindow.type = "CREATE";
			},

			// 즐겨찾기 이름 변경
			modifyFavoriteFolder : function(node) {
				exsoft.util.layout.divLayerOpen(favoriteFolderWindow.wrapperClass, favoriteFolderWindow.layerClass);
				favoriteFolderWindow.callback = configFavoriteFolder.event.refreshTree;
				favoriteFolderWindow.initForm(node, node.id);
				favoriteFolderWindow.type = "UPDATE";
			},

			// 즐겨찾기 이동
			moveFavoriteFolder : function(node) {
				var jsonObject = {
						folder_id : node.id,
						folder_nm : node.text,
						parent_id : node.parent,
						mode : "SELECT",
						only_virtual : "Y"
				};

				// 즐겨찾기 부모 선택창 팝업 (selectFavoriteFolderWindow.jsp 필요함)
				selectFavoriteFolderWindow.init(jsonObject, false, function(returnObject) {

					var postdata = {
							type : "MOVE",
							source_folder_id : returnObject.folder_id,
							source_parent_id : returnObject.parent_id,
							source_sorts : returnObject.targetNode.original.sorts,
							target_folder_id : returnObject.target_folder_id
					};

					exsoft.util.ajax.ajaxDataFunctionWithCallback(postdata, exsoft.contextRoot+"/folder/favoriteControl.do", "", function(data, param) {
						if (data.result == "true") {
							jAlert("즐겨찾기 이동 완료.", "즐겨찾기 이동", 8);
							configFavoriteFolder.event.getCurrentFavoriteTreeForPopup().refresh();
						} else {
							jAlert(data.message, "즐겨찾기 이동", 7);
						}
					});
				});
			},

			// 즐겨찾기 삭제
			deleteFavoriteFolder : function(node) {
				var postdata = {
						folder_id : node.id,
						folder_name_ko : node.text,
						type : "DELETE"
				}

				jConfirm("폴더를 삭제 하시겠습니까?", "폴더 삭제", 2, function(ret) {

					if (ret) {

						exsoft.util.ajax.ajaxDataFunctionWithCallback(postdata, exsoft.contextRoot+"/folder/favoriteControl.do", "", function(data) {

							if (data.result == "true") {
								jAlert("즐겨찾기 삭제 완료.", "폴더 삭제", 8);
								configFavoriteFolder.event.getCurrentFavoriteTreeForPopup().refresh();
							} else {
								jAlert(data.message, "폴더 삭제", 7);
							}
						})
					}
				});
			}
		},
		
    	// 즐겨찾기 구성 - 저장버튼 클릭시
    	okButtonClick : function() {
    		exsoft.util.layout.divLayerClose("favorite_configChoose_wrapper", "favorite_configChoose");
    		exsoftMypageFunc.event.treeFunctions.getCurrentFavoriteTree().refresh();
    	},
    	
    	// 즐겨찾기 폴더 위로 이동 (형제 노드간 이동만 허가함)
    	fGoPrevNode : function() {
    		configFavoriteFolder.favoriteFolderTreeForPopup.moveToPrev();
    	},
    	
    	// 즐겨찾기 폴더 아래로 이동 (형제 노드간 이동만 허가함)
    	fGoNextNode : function() {
    		configFavoriteFolder.favoriteFolderTreeForPopup.moveToNext();
    	},
    	
    	getCurrentFavoriteTreeForPopup : function() {
    		return configFavoriteFolder.favoriteFolderTreeForPopup;
    	},
    	
    	// 즐겨찾기 폴더 새로고침
    	refreshTree : function (e, data) {
    		configFavoriteFolder.event.getCurrentFavoriteTreeForPopup().refresh();
    	}
    },

    //5. 화면 UI 변경 처리
    ui : {
    },

    //6. callback 처리
    callback : {
    }
	
}