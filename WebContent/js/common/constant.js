/**
 * EDMS HTML5 Global const valiables
 * 
 * @comment
 * [2005][신규개발]	2016-03-31	이재민 : 관리자 > 폴더관리 - 하위폴더 일괄 삭제처리
 *	(상품관리 - 한경준 대리님 / 정종철 차장님(운영자)이 기능추가를 요구하심)
 */
var Constant = {
		/**
		 * Tree 관련
		 */
		MAP_MYPAGE	: "MYPAGE",
		MAP_MYDEPT	: "MYDEPT",
		MAP_ALLDEPT	: "ALLDEPT",
		MAP_PROJECT : "PROJECT",

		WORK_MYPAGE	 : "WORK_MYPAGE",
		WORK_MYDEPT	 : "WORK_MYDEPT",
		WORK_ALLDEPT : "WORK_ALLDEPT",
		WORK_PROJECT : "WORK_PROJECT",
		WORK_FREEDOC : "WORK_FREEDOC",

		// 업무(협업) 관련
		PROCESS : {
			ING_MENU			: "ING",				// 진행중 문서(완료 이전 문서)
			REQUEST_MENU		: "REQUEST",			// 요청문서
			WRITE_ING_MENU 		: "WRITE_ING",			// 작성중 문서
			APPROVAL_ING_MENU 	: "APPROVAL_ING",		// 승인중 문서
			WRITE_END_MENU 		: "WRITE_END",			// 작성한 문서
			APPROVAL_END_MENU 	: "APPROVAL_END",		// 승인한 문서
			RECEIVE_MENU 		: "RECEIVE",			// 수신문서
			MODIFY_ING_MENU		: "MODIFY_ING",			// 메인화면 보완중 문서
			//업무요청(Q)>작성중(W)>승인중(A)>승인완료(AE)>보완중(M)>완료(E)
			REQUEST 		: 'Q', 			//업무요청
			WRITE_ING 		: 'W',			//작성중
			WRITE_END 		: 'WE',			//작성완료
			APPROVAL_ING 	: 'A',			//승인중
			APPROVAL_END 	: 'AE',			//승인완료
			MODIFY_ING 		: 'M',			//보완중
			MODIFY_END 		: 'ME',			//보완종료
			PROCESS_END 	: 'E',			//업무종료
			RECEIVE_ING 	: 'R',			//열람중
			RECEIVE_END 	: 'RE',			//열람완료
			TYPE_REQUESTOR	: "Q",		// 업무 요청자
			TYPE_AUTHOR		: "R",		// Responsible(대표 작성자)
			TYPE_COAUTHOR	: "C",		// 공동 작성자
			TYPE_APPROVER	: "A",		// Accountable(의사결정권자::승인자)
			TYPE_RECEIVER	: "I",		// Informed(사후에 결과를 통보 반는자::수신자)
			EXECUTOR_START	: "S",		// 실행자 해당 단계 시작
			EXECUTOR_END	: "E",		// 실행자 해당 단계 종료

		},

		// 권한 선택 팝업 관련
		ACL : {
			TYPE_FOLDER	: "FOLDER",		// 폴더 권한 선택 팝업
			TYPE_DOC	: "DOCUMENT",	// 문서 권한 선택 팝업

			TYPE_ALL	: "ALL",
			TYPE_TEAM	: "TEAM",
			TYPE_DEPT	: "DEPT",
			TYPE_PRIVATE: "PRIVATE",

			ACL_NONE	: "NONE",
			ACL_READ	: "READ",
			ACL_DELETE	: "DELETE",
			ACL_UPDATE	: "UPDATE",
			ACL_BROWSE	: "BROWSE",
		},

		// 메뉴 권한 관련
		MENU : {
			MENU_ALL : "ALL",
			MENU_GROUP : "GROUP",
			MENU_TEAM : "TEAM"
		},

		// ROLE 관련
		ROLE : {
			SYSTEM_ROLE : "SYSTEM_OPERATOR",
		},

		// 쪽지관련
		ALLDATA : "ALL",

		LIST_TYPE : {
			DOCUMENT_LIST_TYPE_TEMPDOC : "TEMPDOC",
			DOCUMENT_LIST_TYPE_RECENTLYDOC : "RECENTLYDOC"
		},

		// action type
		ACTION_CREATE : "CREATE",
		ACTION_UPDATE : "UPDATE",
		ACTION_DELETE : "DELETE",
		ACTION_REPLY : "REPLY",
		ACTION_VIEW : "VIEW",
		ACTION_PAGE_DELETE : 'PAGE',
		ACTION_SUBFOL_DELETE : "SUBFOL_DELETE", // [2005]

		TOPMENU : {
			MYDOC : "myDocMenu",
			MYWORK : "myWorkMenu",
			WORKSPACE : "workSpaceMenu",
			PROCESS : "workProcessMenu",
			STATISTICS : "statisticsMenu",
			MAIN : "mainLayout"
		},
		
		//통계 파이차트에 뿌려주는 데이터 한정갯수  15개
		PIE_NUM:15,
}