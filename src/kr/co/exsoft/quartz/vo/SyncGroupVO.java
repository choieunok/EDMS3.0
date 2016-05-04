package kr.co.exsoft.quartz.vo;

import kr.co.exsoft.eframework.vo.VO;

/**
 * 조직도연계 그룹(부서) VO
 * @author 패키지 개발팀
 * @since 2015.09.07
 * @version 3.0
 *
 * [2000][EDMS-REQ-036]	2015-09-07	이재민 : 조직도연계처리
 */
public class SyncGroupVO extends VO {

	// 테이블 객체(XR_GROUP/XR_GROUIP_HT)
	private long group_seq;						// 그룹변경 IDX - XR_GROUP_HT
    private String dept_cd;							// 부서 ALIAS ID (연계시 사용됨 / 미연계시 group_id와 동일)
    private String group_name_ko;				// 부서명_한국어
    private String group_status;					// 부서상태 = C:활성 D:비활성(폐쇄)
    private String parent_id;						// 상위 부서 ID = 최상위 루트인 경우 GROUP_ID = PARENT_ID 동일함.
    private int sort_index;							// 정렬 순서
    private String insert_date;					// EAI에서 XR_SYNCGROUP 테이블로 전송일
        
    public SyncGroupVO() {
    	
    	this.dept_cd = "";
    	this.group_name_ko = "";
    	this.group_status = "C";					
    	this.parent_id = "";
    	this.sort_index = 0;
    	this.insert_date = "";
    }

	public long getGroup_seq() {
		return group_seq;
	}

	public void setGroup_seq(long group_seq) {
		this.group_seq = group_seq;
	}

	public String getDept_cd() {
		return dept_cd;
	}

	public void setDept_cd(String dept_cd) {
		this.dept_cd = dept_cd;
	}

	public String getGroup_name_ko() {
		return group_name_ko;
	}

	public void setGroup_name_ko(String group_name_ko) {
		this.group_name_ko = group_name_ko;
	}

	public String getGroup_status() {
		return group_status;
	}

	public void setGroup_status(String group_status) {
		this.group_status = group_status;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public int getSort_index() {
		return sort_index;
	}

	public void setSort_index(int sort_index) {
		this.sort_index = sort_index;
	}

	public String getInsert_date() {
		return insert_date;
	}

	public void setInsert_date(String insert_date) {
		this.insert_date = insert_date;
	}
    
}

