/**
 * 쪽지 관련 스크립트
 * [1006] 20160314 eunok 리스트에 직급추가
 */

var gChecked_ids = [];		// jstree 체크된 목록 리스트 :: 공통
var gJstreeLoad = "";			// jstree 로드여부 :: 공통
var selectGroupUserWindow = {

		
		callback : null,
		isValidation : false,
		srcGroupUserInfo : null,
		currentWorkType : "",
		
		open : function() {
			exsoft.util.layout.divLayerOpen("noteuserTree", "note_group");
		},
		
		close : function() {
			exsoft.util.layout.divLayerClose("noteuserTree", "note_group");
		},
		
		tree : {
			groupuserTree : null
		},
		
		treeFunctions : {
			initTree : function(workType) {
				var treeOption = {						
						context : exsoft.contextRoot,
						url : "/group/groupList.do"
					};
				// 부서 문서함
				treeOption.divId = "#selectGroupTreeDiv";
				
				if (selectGroupUserWindow.tree.groupuserTree == null) {
					
					
					selectGroupUserWindow.tree.groupuserTree = new XFTree(treeOption);
										
					// 부서 TreeNode 선택
					selectGroupUserWindow.tree.groupuserTree.callbackSelectNode = function (e, data) {
						
						// 부서-사용자 Tree 리스트
						$('#treeGroupUserList').empty();
						var buffer = "";

						exsoft.util.ajax.ajaxDataFunctionWithCallback({context:exsoft.contextRoot,groupId:data.node.id}, exsoft.contextRoot+"/user/groupUserList.do", "note", function(data1, e) {
							$(data1.list).each(function(index){
								buffer += "<tr>";
								buffer += "<td><input type='checkbox' id='adduser' class='' name='adduser' class=''></td>";
								buffer += "<td>"+data1.list[index].position_nm+"</td>";//[1006]
								buffer += "<td>"+data1.list[index].user_nm+"</td>";
								buffer += "<td>"+data1.list[index].user_id+"</td>";
								buffer += "<td>"+data1.list[index].group_nm+"</td>";
								buffer += "<td class='hidden'>"+data1.list[index].email+"</td>";
								buffer += "</tr>";
							
							});
							$('#treeGroupUserList').append(buffer);	
							
							
						});
						
					};

					selectGroupUserWindow.tree.groupuserTree.callbackLoadNode = function (e, data) {
						gJstreeLoad = e.type;
					},
					
					// jsTree check box list
					selectGroupUserWindow.tree.groupuserTree.callbackAllSelectNodeData = function (selectedNodeData) {
						gChecked_ids = selectedNodeData;
					},
					
					selectGroupUserWindow.tree.groupuserTree.init();
				} else {
					selectGroupUserWindow.tree.groupuserTree.refresh();
				}
				
			
			},
		},//treefunction END
		
		init : {
			initAction : function(srcGroupUser, isValidation, callback) {
				selectGroupUserWindow.srcGroupUserInfo = srcGroupUser;
				selectGroupUserWindow.isValidation = isValidation;
				selectGroupUserWindow.callback = callback;
				selectGroupUserWindow.treeFunctions.initTree(selectGroupUserWindow.currentWorkType);
				selectGroupUserWindow.open();
	
			},
			
		},//init END
			
		
		ui : {
			
			//체크박스 선택 해제
			allCheckUnCheckBox : function(id,name){
				if($("#"+id).prop("checked")){
					$("input[name="+name+"]:checkbox").prop("checked", true);
				}else{
					$("input[name="+name+"]:checkbox").prop("checked", false);
				}
			},
			
		},//ui END
		
		event : {
			
			// 사용자명 & 그룹명으로 해당 유저 찾기
			searchUserGroupName : function(){
				//검색 조건 공백 제거
				var usernm =$.trim($('.filter_note_name').val());
				var groupnm= $.trim($('.filter_depart_name').val());
				if(usernm=="" && groupnm==""){
					jAlert("검색 조건을 입력해 주세요", "확인", 6);
				}
				var buffer="";
				$('#treeGroupUserList').empty();	
				exsoft.util.ajax.ajaxDataFunctionWithCallback({groupName:groupnm,userName:usernm}, exsoft.contextRoot+"/user/searchUserList.do", "note", function(data, e) {
					$(data.list).each(function(index){
						buffer += "<tr>";
						buffer += "<td><input type='checkbox' class='' id='adduser' name='adduser' class=''></td>";
						buffer += "<td>"+data.list[index].position_nm+"</td>";//[1006]
						buffer += "<td>"+data.list[index].user_name_ko+"</td>";
						buffer += "<td>"+data.list[index].user_id+"</td>";
						buffer += "<td>"+data.list[index].group_nm+"</td>";
						buffer += "<td class='hidden'>"+data.list[index].email+"</td>";
						buffer += "</tr>";
					
					});
					$('#treeGroupUserList').append(buffer);						
				});
			},
			
			//선택한 유저 오른쪽으로 복사
			selectUseradd : function(){
								
				var target = $("#treeGroupUserList").find("tr:has(:checked)");
				if(target.size() ==0){
					jAlert("추가할 사용자를 선택해 주세요", "확인", 6);
				}
				else {
					for ( var i = target.size() - 1; i >= 0; i--) { 
						//열 복제
						var item = target.eq(i).clone();
						item.find("td:eq(0)").removeAttr("checked"); 
						
						//tr 클론으로 체크박스 이름도 복제되기 때문에 전체선택/삭제를 위해 name을 바꿔준다.
						item.find("#adduser").attr("name","cloneAdduser");
						this.selectUsercopy(item);
					}
				}
			},
			//선택한 유저 복사
			selectUsercopy : function (item) {	
				var appended = false; 
				
				var targettext = item.find("td").eq(2).text(); 
				var trs = $("#selectUserGroupList").find("tr");
				//기존에 추가된 사용자 인지 체크
				for ( var i = trs.size() - 1; i >= 0; i--) { 
					if (trs.eq(i).find("td").eq(2).text()==targettext) { 
						item.find(":checked").removeAttr("checked"); 

						appended = true; 
						jAlert("이미 추가된 사용자는 추가하지 않습니다.", "확인", 6);
						break; 
					} 
				} 
				//기존 추가된 사용자가 아니면 추가한다.
				if (!appended) { 
					item.find(":checked").removeAttr("checked"); 
					$("#selectUserGroupList").append(item); 
				} 
				return true; 
			},
			
			// 선택된 사용자 목록 삭제
			selectUserDelete : function () {
				$("#selectedUserList").prop("checked",false);
				
				var target = $("#selectUserGroupList").find("tr:has(:checked)");
				if(target.size() ==0){
					jAlert("삭제할 사용자를 선택해 주세요", "선택", 6);
				}
				$("#selectUserGroupList input[type='checkbox']:checked").parent().parent().remove();

			},
			
			//받는 사람을 부모창에 Setting
			reciverSet : function(){
				var trs = $("#selectUserGroupList").find("tr");
				if(trs.size() ==0){
					jAlert("받는 사람을 추가해 주세요", "확인", 6);

					return false;
				}
				var reciveNM = "";
				var reciveID="";
				
				//부모창에 기존에 가지고 있던 유저 가져오기
				
				var inputData = "";
				
				for ( var i = trs.size() - 1; i >= 0; i--) { 
					
					//받는 사람 => USER#유저 아이디#유저명|
					inputData += "USER"+ "#" +trs.eq(i).find("td").eq(3).text()+ "#" +trs.eq(i).find("td").eq(2).text()+ "|";
					
					reciveNM += trs.eq(i).find("td").eq(2).text()+";";//받는 사람 이름
					reciveID += trs.eq(i).find("td").eq(3).text()+";";//받는 사람 아이디		 
				} 
				
				//부모창 받는 사람에 값 Setting
				$('#noteWriteFrm', opener.document).find("#noteReciver").val(reciveNM);
				$('#noteWriteFrm', opener.document).find("#reciverArrayList").val(inputData);
				$('#noteForwardFrm', opener.document).find("#noteReciver").val(reciveNM);
				$('#noteForwardFrm', opener.document).find("#reciverArrayList").val(inputData);
				
				//현재창 닫기
				exsoft.util.layout.windowClose();
			},
			
			setAccessorList : function(inputData,reciveNM,trs) {
				var selectList = inputData.split("|");
				var result = new Array();
				var row = "";
				
				var jsonArr="";
				
				
				for(var i=0;i<selectList.length;i++) {
					
					result = selectList[i].split("#");
					
					
					if(result.length == 3)	{
			 			
						//jsonObject 
						rowData['accessor_id'] = result[2];
						rowData['accessor_name'] = result[1];			
						rowData['accessor_isgroup'] = result[0];
						
						
						row = new Object();
						
						row.accessor_id = result[1];
						row.accessor_isgroup = result[0] == 'GROUP' ? 'T' : 'F';
						row.accessor_isalias = result[0] == 'ALL' ? 'T' : 'F';
						row.accessor_name = result[2];
						
						
						jsonArr = [{
							accessor_isgroup : result[0],
							accessor_id :  result[1],
							accessor_name : result[2]
							
						}];
						
					}
				}
				
				//부모창 받는 사람에 값 Setting
				$('#noteReciver', opener.document).val(reciveNM);
				$('#reciverArrayList', opener.document).val(jsonArr);
				
				if(trs.size() ==0){
					jAlert("받는 사람을 추가해 주세요", "확인", 6);

					return false;
				}else{

					//현재창 닫기
					exsoft.util.layout.windowClose();
				}
				
			},//setAccessorList END
			
			// 엔터키 입력시
			enterKeyPress : function(e) {
				if (e.keyCode == 13) {
					selectGroupUserWindow.event.searchUserGroupName();
					return false;
				}
			},
			
		},//event END
	};