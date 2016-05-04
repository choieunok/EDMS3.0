package kr.co.exsoft.quartz.vo;

import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.vo.VO;

/**
 * 조직도연계 사용자 정보 VO
 * @author 패키지 개발팀
 * @since 2015.09.07
 * @version 3.0
 * 
 * [2000][EDMS-REQ-036]	2015-09-07	이재민 : 조직도연계처리
 */
public class SyncUserVO extends VO {
    	
	// 테이블 객체(XR_USER/XR_USER_HT)
	private long user_seq;								// 사용자변경 IDX - XR_USER_HT
    private String user_id;								// 사용자 ID
    private String emp_no;								// 사원번호
    private String dept_cd;							// 부서 ALIAS ID (연계시 사용됨 / 미연계시 group_id와 동일)
    private String user_name_ko;						// 사용자명	한국어
    private String user_pass;							// 사용자 패스워드(ARIA 암호화)
    private String user_status;							// 사용자상태 = C:활성(재직) D:비활성(퇴사)
    private String is_default_dept;						// T:DPET_CD가 기본 부서, F:DEPT_CD가 겸직 부서
    private String user_type;							// 유저구분 = 0:내부사용자 1:외부사용자
    private String insert_date;					// EAI에서 XR_SYNCUSER 테이블로 전송일
    
    // 테이블 객체(XR_USER_DT)
    private String jobtitle;								// 직위(직책) (팀장,팀원,본부장 등등에 해당하는 CDOE값)
    private String position;								// 직급 (사원,대리,과장 등등에 해당하는 CODE값)
    private String email;								// 이메일
    private String telephone;							// 전화번호
    
    public SyncUserVO() {
    	
    	this.user_seq = 0;
    	this.user_id = "";
    	this.emp_no = "";
    	this.dept_cd = "";
    	this.user_name_ko = "";
    	this.user_pass = "";
    	this.user_status = "C";
    	this.is_default_dept = "T";
    	this.user_type = "0";
    	this.jobtitle = "900"; // 900=직책없음 (팀원 직책이 없으므로 코드900으로 생성)
    	this.position = ConfigData.getString("DEFAULT.POS");
    	this.email = "";
    	this.telephone = "";
    }

	public long getUser_seq() {
		return user_seq;
	}

	public void setUser_seq(long user_seq) {
		this.user_seq = user_seq;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getEmp_no() {
		return emp_no;
	}

	public void setEmp_no(String emp_no) {
		this.emp_no = emp_no;
	}

	public String getDept_cd() {
		return dept_cd;
	}

	public void setDept_cd(String dept_cd) {
		this.dept_cd = dept_cd;
	}

	public String getUser_name_ko() {
		return user_name_ko;
	}

	public void setUser_name_ko(String user_name_ko) {
		this.user_name_ko = user_name_ko;
	}

	public String getUser_pass() {
		return user_pass;
	}

	public void setUser_pass(String user_pass) {
		this.user_pass = user_pass;
	}

	public String getUser_status() {
		return user_status;
	}

	public void setUser_status(String user_status) {
		this.user_status = user_status;
	}

	public String getIs_default_dept() {
		return is_default_dept;
	}

	public void setIs_default_dept(String is_default_dept) {
		this.is_default_dept = is_default_dept;
	}

	public String getUser_type() {
		return user_type;
	}

	public void setUser_type(String user_type) {
		this.user_type = user_type;
	}

	public String getInsert_date() {
		return insert_date;
	}

	public void setInsert_date(String insert_date) {
		this.insert_date = insert_date;
	}

	public String getJobtitle() {
		return jobtitle;
	}

	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
}
