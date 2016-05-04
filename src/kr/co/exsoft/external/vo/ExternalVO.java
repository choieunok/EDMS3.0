package kr.co.exsoft.external.vo;
import kr.co.exsoft.eframework.vo.VO;
/**
 * 폴더 VO
 * @author 패키지 개발팀
 * @since 2015.08.25
 * @version 3.0
 *
 */
public class ExternalVO  extends VO {

	// 테이블 객체(XR_INTERFACE_CODE)
	private String work_code;		
	
	
	private String work_description;	
	private String edms_savepath;		
	private String folder_id;										// 폴더ID
	
	public ExternalVO() {	
		this.work_code = "";
		this.work_description = "";
		this.edms_savepath = "";	
		this.folder_id = "";
	}

	public String getWork_code() {
		return work_code;
	}

	public void setWork_code(String work_code) {
		this.work_code = work_code;
	}

	public String getWork_description() {
		return work_description;
	}

	public void setWork_description(String work_description) {
		this.work_description = work_description;
	}

	public String getEdms_savepath() {
		return edms_savepath;
	}

	public void setEdms_savepath(String edms_savepath) {
		this.edms_savepath = edms_savepath;
	}
	public String getFolder_id() {
		return folder_id;
	}
	public void setFolder_id(String folder_id) {
		this.folder_id = folder_id;
	}

	
}
