package kr.co.exsoft.external.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.service.SessionService;
import kr.co.exsoft.common.vo.CodeVO;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.document.dao.PageDao;
import kr.co.exsoft.document.service.TypeService;
import kr.co.exsoft.document.vo.DocumentVO;
import kr.co.exsoft.document.vo.PageVO;
import kr.co.exsoft.document.vo.TypeVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.exception.BizException;
import kr.co.exsoft.eframework.exception.ExrepClientException;
import kr.co.exsoft.eframework.library.DownloadView;
import kr.co.exsoft.eframework.library.ExcelFileView;
import kr.co.exsoft.eframework.library.LocaleLibrary;
import kr.co.exsoft.eframework.repository.EXrepClient;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.util.StringUtil;
import kr.co.exsoft.eframework.util.UtilFileApp;
import kr.co.exsoft.external.service.ExternalService;
import kr.co.exsoft.user.service.UserService;
import kr.co.exsoft.user.vo.UserVO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.exsoft.net.SizedInputStream;

/**
 * 외부연계 컨트롤
 * @author 패키지 개발팀
 * @since 2014.07.17
 * @version 3.0
 *
 * [1001][EDMS-REQ-070~81]	2015-08-24	최은옥	 : 외부시스템 연계
 */

@Controller
@RequestMapping("/external")
public class ExternalPublicController {

	@Autowired
	private ExternalService externalService;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private MessageSource messageSource;
	
    @Autowired
    private DefaultBeanValidator beanValidator;
    
    @Autowired
	private UserService userService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private TypeService typeService;
	
	@Autowired
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
    
	protected static final Log logger = LogFactory.getLog(ExternalPublicController.class);
	protected static final Log interfacelogger = LogFactory.getLog(ExternalPublicController.class);


	/**
	 * 
	 * <pre>
	 * 1. 개용 : URL 링크복사 다운로드 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : urlDownLoad
	 * @param model
	 * @param map
	 * @param sessionVO
	 * @return ModelAndView
	 */
	@RequestMapping("/urlDownLoad.do")
	public ModelAndView urlDownLoad(Model model, @RequestParam HashMap<String,Object> map) {
		
		List<PageVO> pageList = new ArrayList<PageVO>();
		PageVO pageVO = new PageVO();
		
		try {
			
			// 1.다운로드 대상 목록 및 PageVO 객체 구하기
			pageVO = commonService.urlPageInfo(map);
			pageList.add(pageVO);
						
			// 2.다운로드 VIEW 		
			model.addAttribute("isZip",Constant.F);
			model.addAttribute("pageList",pageList);
					
		}catch(Exception e) {
			logger.error(e.getMessage());
			model.addAttribute("message",e.getMessage());
			return new ModelAndView("error/404");
		}
		
		return new ModelAndView(new DownloadView());
	}

	@RequestMapping(value = "/restful.agent", method = RequestMethod.POST)
	@ResponseBody
	public List<HashMap<String,Object>> agentInterface(Model model, @RequestBody HashMap<String,Object> paramMap) {
		// param값을 @RequestBody로 받는 이유는 json값을 추출하기 위해
		
		//TODO : [stephan]암복호화 부터 agent 분기까지 신경 써야 함.
		
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> data1 = new HashMap<String,Object>();
		data1.put("user_id", paramMap.get("user_id"));
		data1.put("resource_id", paramMap.get("resource_id"));
		data1.put("resource_type", paramMap.get("resource_type"));
		data1.put("action_id", paramMap.get("action_id"));
         
	    list.add(data1);
	    
		// parameter 정보
		for( String key : paramMap.keySet() ){
			HashMap<String,Object> paramData = new HashMap<String,Object>();
			paramData.put(key,paramMap.get(key));
			list.add(paramData);
	    }
	
	    return list;
	}
	
	
	
	
	
	/** Restful Test Start... */
	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
	public String getMovie(@PathVariable String name, ModelMap model) {
 
		model.addAttribute("movie", name);
		return "user/restFul";
 
	}
 
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getDefaultMovie(ModelMap model) {
 
		model.addAttribute("movie", "this is default movie");
		return "/user/restFul";

	}
	
	// Get 방식
	@RequestMapping(value = "/restful.jsonget/{key}", method = RequestMethod.GET)
	@ResponseBody
	public List<HashMap<String,Object>> getTest(Model model, @PathVariable String key, @RequestParam HashMap<String,Object> map) {
	
		logger.info("json return Sample");
		
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> data1 = new HashMap<String,Object>();
		data1.put("name", "a : " + key);
         
	    HashMap<String,Object> data2 = new HashMap<String,Object>();
		data2.put("userid", "b : " + key);
         
	    list.add(data1);
		list.add(data2);
	
	        return list;
	}
	
	// Post 방식
	@RequestMapping(value = "/restful.jsonpost", method = RequestMethod.POST)
	@ResponseBody
	public List<HashMap<String,Object>> postTest(Model model, @RequestParam HashMap<String,Object> map) {
	
		logger.info("json return Sample");
		
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> data1 = new HashMap<String,Object>();
		data1.put("name", "a : " + map.get("key"));
         
	    HashMap<String,Object> data2 = new HashMap<String,Object>();
		data2.put("userid", "b : "  + map.get("userid"));
         
	    list.add(data1);
		list.add(data2);
	
	    return list;
	}
	
	// Head 방식
	@RequestMapping(value = "/restful.jsonhead", method = RequestMethod.HEAD)
	@ResponseBody
	public List<HashMap<String,Object>> haedTest(Model model, @RequestHeader String key) {
	
		logger.info("json return Sample");
		
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> data1 = new HashMap<String,Object>();
		data1.put("name", "a : " + key);
         
	    HashMap<String,Object> data2 = new HashMap<String,Object>();
		data2.put("userid", "b : " + key);
         
	    list.add(data1);
		list.add(data2);
	
	        return list;
	}
	
	// Multipart single upload
	@RequestMapping(value="/restful.singleUpload", method=RequestMethod.POST)
	@ResponseBody
    public String singleFileUploadTest(@RequestParam("uploadBox") MultipartFile file){ // <input type="file" name="uploadBox">
            String name = "File not found";
        if (!file.isEmpty()) {
            try {
            	name = file.getName();
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File(name + "-uploaded")));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + " into " + name + "-uploaded !";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
	
	// Multipart multi upload
	@RequestMapping(value = "/restful.multiUpload", method = RequestMethod.POST)
	@ResponseBody
    public String multiFileUploadTest( @ModelAttribute("uploadForm") ExternalMultiFileUpload uploadForm, Model map) 
    		throws IllegalStateException, IOException {
        
		String saveDirectory = "d:/temp/";
 
        List<MultipartFile> crunchifyFiles = uploadForm.getFiles();
        List<String> fileNames = new ArrayList<String>();
 
        if (null != crunchifyFiles && crunchifyFiles.size() > 0) {
            for (MultipartFile multipartFile : crunchifyFiles) {
 
                String fileName = multipartFile.getOriginalFilename();
                if (!"".equalsIgnoreCase(fileName)) {
                    // Handle file content - multipartFile.getInputStream()
                    multipartFile.transferTo(new File(saveDirectory + fileName));
                    fileNames.add(fileName);
                }
            }
        } else {
        	return "File not found";
        }
 
        map.addAttribute("files", fileNames);
        return "uploadfilesuccess";
    }
	
	@RequestMapping(value = "/restful.postwithfile", method = RequestMethod.POST)
	@ResponseBody
    //public List<HashMap<String,Object>> uploadMultipleFileHandler(
	public Map<String,Object> uploadMultipleFileHandler(
    		@RequestHeader HashMap<String,Object> headMap, 
    		@RequestParam HashMap<String,Object> paraMap, 
    		@RequestParam("fileUpload") MultipartFile[] files,
    		HttpServletRequest request)
    				throws IllegalStateException, IOException {


		System.out.println("=============================");
		System.out.println(paraMap.get("folderPath").toString());
		System.out.println("=============================");
		
		//List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		Map<String,Object> list = new HashMap<String, Object>();
		Map<String,Object> pagelist = new HashMap<String, Object>();
		HashMap<String,Object> resultMap = new HashMap<String,Object>();
	
        Map<String, Object> resultDBMap = new HashMap<String, Object>();
        PageVO pageVO = new PageVO();
        PageDao pageDao = sqlSession.getMapper(PageDao.class);
        
        String orgFileName = "";
		long fileSize = 0L;
		
		EXrepClient eXrepClient = new EXrepClient();
		List<HashMap<String,Object>> pageIds = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> resultData = null;
		int cnt=0;
        if (null != files && files.length > 0) {
            for (MultipartFile multipartFile : files) {
            	cnt++;
                orgFileName = multipartFile.getOriginalFilename();//원본 파일명
                fileSize = multipartFile.getSize();//원본 파일 사이즈
                                
                String contentPath = "";
               // try {
					//eXrepClient.connect();
					//contentPath = CommonUtil.getContentPathByDate(ConfigData.getString(Constant.EXREP_ROOT_EDMS_NM))+UUID.randomUUID().toString();
					
					//if(eXrepClient.putFile(new SizedInputStream(multipartFile.getInputStream(), fileSize),ConfigData.getString(Constant.EXREP_VOLUME_NM), contentPath,true)) {										
						// 성공
					//	resultDBMap.put("result", "success");
					//}
				//} catch (ExrepClientException e) {
					// TODO Auto-generated catch block
					//실패
				//	resultDBMap.put("result", "fail");
					
				//	e.printStackTrace();
				//}
                
                //페이지 아이디 가져와서 셋팅하기
                try {
					pageVO.setPage_id(CommonUtil.getStringID(Constant.ID_PREFIX_PAGE, commonService.commonNextVal(Constant.COUNTER_ID_PAGE)));
					
					
					
					//json에 DB에 등록된 PageId 셋팅
					resultData = new HashMap<String,Object>();
					resultData.put("stephan", "1234kb"+String.valueOf(cnt));
					resultData.put("pageId"+String.valueOf(cnt),pageVO.getPage_id());
		            //list.add(resultData);	
					pageIds.add(resultData);
					
					//list.put("pageId"+String.valueOf(cnt),pageVO.getPage_id());
		            
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
                
                //PAGE 정보 셋팅
                pageVO.setPage_name(orgFileName);
                pageVO.setPage_extension(orgFileName.substring(orgFileName.lastIndexOf(".")+1, orgFileName.length()));  // 원본 확장자
                pageVO.setVolume_id(ConfigData.getString(Constant.EXREP_VOLUME_NM));
                pageVO.setContent_path(contentPath);
                pageVO.setPage_size(fileSize);
             
                int result = 0;
        		result = pageDao.writePage(pageVO);
        		if(result == 0)	{
        			System.out.println("XR_PAGE DB INSERT FAIL");
        		}else{
        			System.out.println("XR_PAGE DB INSERT SUCCESS");        			
        		}
        		
                /*if (!"".equalsIgnoreCase(orgFileName)) {
                    // Handle file content - multipartFile.getInputStream()
                    multipartFile.transferTo(new File(saveDirectory + orgFileName));
                    fileNames.add(orgFileName);
                }*/
        		
        		
            }

            list.put("pageIds",pageIds);
            
        } else {
        	//resultMap.put("result", "File not found");
        	//list.add(resultMap);
        	list.put("result","File not found");
        	return list;
        }
        
        //resultMap.put("result", "SUCCESS");
    	//list.add(resultMap); 
    	list.put("result","SUCCESS"); 
	    return list;
 
    }
	
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/restful.postwithfiledown", method = RequestMethod.POST,produces="applicaion/json; charset=utf8")
    @ResponseBody
   	//@PathVariable String fileID
   	public ResponseEntity<byte[]> downloadFile(
    		HttpServletRequest request,HttpServletResponse response) {    	
    	
   		String errorMsg = "";
    	//클라이언트로 부터 읽어들인 PageId값
   		String pageId = request.getParameter("pageId");
    	PageDao pageDao = sqlSession.getMapper(PageDao.class);
		
		PageVO ret = new PageVO();
    	HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("page_id",pageId);
		ret = pageDao.pageDetailInfo(param);		
		
    	HttpHeaders headers = new HttpHeaders();
    	File toServeUp = null;
    	InputStream inputStream =null;
    	
    	// EXREP 연계해서 파일정보 가져오기. :: eXrep C/S Client 생성.  
    	EXrepClient eXrepClient = new EXrepClient();

    	//다운로드할 폴더 설정
    	String downPath = ConfigData.getString("FILE_DOWN_PATH") + UUID.randomUUID().toString() + "/";
    	String downFile = ret.getPage_name();//"word.docx";
    	String savePath = downPath + downFile;
		try {
			UtilFileApp.createDir(downPath);
		} catch (SecurityException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
            errorMsg = "Can't Not Create Directory";
			response.setHeader("errorMsg", errorMsg);
		}				
		
    	try {
    		// 5. eXrep C/S 접속.
        	eXrepClient.connect();
        				
	    	if(eXrepClient.getFile(ret.getVolume_id(), ret.getContent_path(), savePath)) {
	    		toServeUp = new File(savePath);
	    		System.out.println("FILE DOWN LOAD COMPLITE");
	    	}

	    	// 6. eXrep C/S Client Close
	    	eXrepClient.disconnect();
			
		} catch (ExrepClientException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
            errorMsg = "Not Connected FileServer";
			response.setHeader("errorMsg", errorMsg);
		}
    	
    	try{
    		
    		inputStream = new FileInputStream(toServeUp);
    		
    	}catch(FileNotFoundException e){
			e.printStackTrace();	
            errorMsg = "Not Found DownLoadFile";
			response.setHeader("errorMsg", errorMsg);
		}
    	
    	InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
    	Long fileSize = toServeUp.length();
    	headers.setContentLength(fileSize.intValue());
    	
    	response.setContentType("application/octer-stream;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
    	try {
    		//파일명 인코드
			response.setHeader("filename", java.net.URLEncoder.encode(downFile,"UTF-8") );
    	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			errorMsg = "Can't Not Encoding File";
			response.setHeader("errorMsg", errorMsg);
		}
    	
    	
    	return new ResponseEntity(inputStreamResource,headers,HttpStatus.OK);
   	}
   	
	/** Restful Test End... 
	 * @throws Exception */
	
	@RequestMapping("/externalMain.do")
	public String externalMain(Model model, HttpServletRequest request) throws Exception {
		
		model.addAttribute("contextRoot", request.getContextPath());
		model.addAttribute("userId","user028");
		
		return "external/externalMain";
	}
		
	/**
	 * <pre>
	 * 1. 개용 : 외부연계시 트리팝업
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalTreeCall
	 * @param model
	 * @param userVO
	 * @param request
	 * @param response
	 * @return String
	 */
	@RequestMapping(value="/externalTreeCall.do", method=RequestMethod.POST)
	public String externalTreeCall(Model model, @ModelAttribute UserVO userVO, HttpServletRequest request,HttpServletResponse response) {
		
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		param.put("login_type",Constant.SSO_LOGIN_TYPE);									// 로그인타입 = NORMAL - 일반웹접속 / SSO - Single Sign On 접속		
		param.put("connect_type",Constant.CONNECT_TYPE_LOGIN);		
	
		UserVO resultVO = new UserVO();
		
		try {

			// 0.라이센스 체크 - 라이센스 로직 추가
			commonService.checkUserLicense();
						
			// 1.사용자 로그인처리 	
			resultVO = userService.userLogin(userVO,request);
			
			// 2.1 관리자화면 일반사용자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) &&
					resultVO.getRole_id().equals(Constant.USER_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.user.error");
				param.put("error_content",messageSource.getMessage("login.fail.user.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 2.2 사용자 화면 시스템관리자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_USER) && 
					resultVO.getRole_id().equals(Constant.SYSTEM_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.sysadmin.error");
				param.put("error_content",messageSource.getMessage("login.fail.sysadmin.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 3.사용자 세션처리
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN))	{
				param.put("content",Constant.SESSION_ADMIN);
			}else {
				param.put("content",Constant.SESSION_USER);
			}
				
			// 4.사용자 세션 저장처리
			param.put("cert_yn",Constant.YES);
			param.put("contextRoot",request.getContextPath());			
			SessionVO sessionVO = sessionService.setSessionVO(resultVO, request, param);			
			model.addAttribute("sessionVO",sessionVO);

			// 5. 결과값 처리
			resultMap.put("result",Constant.RESULT_TRUE);		
			resultMap.put("page",userVO.getLogin_type());
			resultMap.put("message",Constant.RESULT_SUCCESS);				
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());		
			//return "layout/loginForm";
			return "external/selectSingleFolderWindowForExternal";
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			//return "layout/loginForm";
			return "external/selectSingleFolderWindowForExternal";
		}
		
		model.addAttribute("contextRoot", request.getContextPath());
		
		
		return "external/selectSingleFolderWindowForExternal";
		
	}
	
	/**
	 * <pre>
	 * 1. 개용 : 외부연계 문서등록
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalRegistDocCall
	 * @param model
	 * @param userVO
	 * @param request
	 * @param response
	 * @return String
	 */
	@RequestMapping(value="/externalRegistDocCall.do", method=RequestMethod.POST)
	public String externalRegistDocCall(Model model, @ModelAttribute UserVO userVO, HttpServletRequest request,HttpServletResponse response) {
				
		Locale locale = LocaleLibrary.setLocale(ConfigData.getString("LANGUAGE")); 	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String,Object> param = new  HashMap<String, Object>();
		
		List<TypeVO> typeList = new ArrayList<TypeVO>();
		List<CodeVO> preservation = new ArrayList<CodeVO>();
		List<CodeVO> sercurity = new ArrayList<CodeVO>();
		List<CodeVO> position = new ArrayList<CodeVO>();
		
		param.put("login_type",Constant.SSO_LOGIN_TYPE);									// 로그인타입 = NORMAL - 일반웹접속 / SSO - Single Sign On 접속		
		param.put("connect_type",Constant.CONNECT_TYPE_LOGIN);		
		
		
		UserVO resultVO = new UserVO();

		
		try {
						
			// 0.라이센스 체크 - 라이센스 로직 추가
			commonService.checkUserLicense();
						
			// 1.사용자 로그인처리 	

			resultVO = userService.userLogin(userVO,request);
			
			// 문서관련처리 start
			param.put("is_doc", Constant.T);
			param.put("is_hidden", Constant.T); // 쿼리가 != 비교여서 T를 넘김
			typeList = typeService.typeList(param);
			
			// 보존년한 : 파라미터 - gcode_id	
			param.put("gcode_id", Constant.CODE_PRESERVATION_YEAR);
			preservation = commonService.codeList(param);
			
			// 보안등급 : 파라미터 - gcode_id 
			param.put("gcode_id", Constant.CODE_SECURITY_LEVEL);
			sercurity = commonService.codeList(param);
			
			// 조회등급 : 파라미터 - gcode_id,is_use 
			param.put("gcode_id", Constant.CODE_POSITION);
			param.put("is_use", Constant.YES);
			position = commonService.codeList(param);
			
			model.addAttribute("typeList",typeList);
			model.addAttribute("preservation_year", preservation);
			model.addAttribute("sercurity", sercurity);
			model.addAttribute("position", position);
			model.addAttribute("folder_id", request.getParameter("folder_id") != null ? request.getParameter("folder_id") : "");
			model.addAttribute("folder_path", request.getParameter("folder_path"));
			model.addAttribute("calledpage", request.getParameter("calledpage"));
			model.addAttribute("isLeft", request.getParameter("isLeft"));
			// 문서관련처리 end

			
			// 2.1 관리자화면 일반사용자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN) &&
					resultVO.getRole_id().equals(Constant.USER_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.user.error");
				param.put("error_content",messageSource.getMessage("login.fail.user.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 2.2 사용자 화면 시스템관리자 접속 계정 체크
			if(userVO.getLogin_type().equals(Constant.SESSION_USER) && 
					resultVO.getRole_id().equals(Constant.SYSTEM_ROLE))	{
				
				// XR_CONNECT_LOG 객체 구하기.					
				param.put("cert_yn",Constant.NO);
				param.put("error_cd","login.fail.sysadmin.error");
				param.put("error_content",messageSource.getMessage("login.fail.sysadmin.error",new Object[0],locale));						
				userService.userLogFailWrite(resultVO,param,request);
				
				throw new BizException(param.get("error_content").toString());
			}
			
			// 3.사용자 세션처리
			if(userVO.getLogin_type().equals(Constant.SESSION_ADMIN))	{
				param.put("content",Constant.SESSION_ADMIN);
			}else {
				param.put("content",Constant.SESSION_USER);
			}
				
			// 4.사용자 세션 저장처리
			param.put("cert_yn",Constant.YES);
			param.put("contextRoot",request.getContextPath());
			
			SessionVO sessionVO = sessionService.setSessionVO(resultVO, request, param);			
			model.addAttribute("sessionVO",sessionVO);

			// 6. 결과값 처리
			resultMap.put("result",Constant.RESULT_TRUE);		
			resultMap.put("page",userVO.getLogin_type());
			resultMap.put("message",Constant.RESULT_SUCCESS);				
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());		
			//return "layout/loginForm";
			return "external/registDocWindowForExternal";
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			//return "layout/loginForm";
			return "external/registDocWindowForExternal";
		}
		
		model.addAttribute("contextRoot", request.getContextPath());
				
		return "external/registDocWindowForExternal";
		
	}
	
	/**
	 * <pre>
	 * 1. 개용 :[1000] 외부시스템 연계
	 * 2. 처리내용 : 파일 관련 multipart Form 호출
	 * </pre>
	 * @Method Name : interfaceMultipartHandler
	 * @param paraMap
	 * @param files
	 * @param request
	 * @return Map<String,Object>
	 */
	@RequestMapping(value = "/interfaceMultipartForm", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> interfaceMultipartHandler(
    		@RequestParam HashMap<String,Object> paraMap, 
    		@RequestParam("fileUpload") MultipartFile[] files,
    		HttpServletRequest request) {

		interfacelogger.info("==================== interfaceMultipartHandler =====================");
		//로그에 클라이언트 파라메터 값 추가하기
		
		Map<String,Object> list = new HashMap<String, Object>();
			
		// parameter값에 개행을 없앤다
		StringUtil.setHashMapDeleteNewLine(paraMap);
		String type = paraMap.get("type")!= null ? paraMap.get("type").toString() : "";
		String action = paraMap.get("action")!= null ? paraMap.get("action").toString() : "";
		String authId = paraMap.get("authId")!= null ? paraMap.get("authId").toString() : "";
		
		//필수 값 체크 타입& 액션 & 유저아이디
		if(StringUtil.isEmpty(type) || StringUtil.isEmpty(action) || StringUtil.isEmpty(authId)){
			list.put("status",Constant.RESULT_FALSE);
			list.put("message","실패"+":[-2001] [type|action|authid] 필수값이 존재하지 않습니다. 필수값 유무를 확인하세요.");
		    return list;
		}

		
		interfacelogger.info("External PARAMETER : 1) type = "+ type +": 2) action = "+action +": 3) authId = "+ authId);
		try {				

			SessionVO sessionVO = externalService.interfaceSessionVO(request, authId);			

			//대소문자 구열없애기
			if(type.equalsIgnoreCase(Constant.INTERFACE_TYPE_DOCUMENT)){
				type = Constant.INTERFACE_TYPE_DOCUMENT;
			}else if(type.equalsIgnoreCase(Constant.INTERFACE_TYPE_PAGE)){
				type = Constant.INTERFACE_TYPE_PAGE;
			}
			
			switch(type){
				//문서
				case Constant.INTERFACE_TYPE_DOCUMENT:
					
						list = externalService.interfaceTypeDocument(paraMap, files, sessionVO);
						list.put("message","성공");	
						list.put("status",Constant.RESULT_SUCCESS);
						list.put("docId",list.get("docId").toString());						
						System.out.println("============================");
						System.out.println("docId = "+ list.get("docId").toString());
						System.out.println("============================");
						
					break;
				//파일
				case Constant.INTERFACE_TYPE_PAGE:
					
						list = externalService.interfaceTypePage(paraMap, files, sessionVO);
						list.put("message","성공");	
						list.put("status",Constant.RESULT_SUCCESS);
						list.put("pageList",list.get("pageList"));	
						System.out.println("============================");
						System.out.println("pageList = "+list.size());
						System.out.println("============================");
					
					break;
				
				default:
					list.put("message","실패"+":[-2007] type 값이 유효하지 않습니다.");
					list.put("status",Constant.RESULT_FALSE);
					break;
			}
		}catch(BizException e){		
			list.put("message","실패"+":"+e.getMessage());
			list.put("status",Constant.RESULT_FALSE);
			System.out.println("============================");
			System.out.println("BizException = "+ e.getMessage());
			System.out.println("============================");		

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		} catch (Exception e) {
			list.put("message","실패"+":"+e.getMessage());
			list.put("status",Constant.RESULT_FALSE);	
			System.out.println("============================");
			System.out.println("Exception = " + e.getMessage());
			System.out.println("============================");	
			
			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	   
		} 

		return list;

    }
	
	/**
	 * <pre>
	 * 1. 개용 :[1000] 외부시스템 연계
	 * 2. 처리내용 : 일반 text form 호출
	 * </pre>
	 * @Method Name : interfaceTextHandler
	 * @param paraMap
	 * @param request
	 * @return Map<String,Object>
	 */
	@RequestMapping(value = "/interfaceTextForm", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> interfaceTextHandler(
    		@RequestParam HashMap<String,Object> paraMap, 
    		HttpServletRequest request) {

		interfacelogger.info("==================== interfaceTextHandler =====================");
		Map<String,Object> list = new HashMap<String, Object>();
		
		// parameter값에 개행을 없앤다
		StringUtil.setHashMapDeleteNewLine(paraMap);
		String type = paraMap.get("type")!= null ? paraMap.get("type").toString() : "";
		String action = paraMap.get("action")!= null ? paraMap.get("action").toString() : "";
		String authId = paraMap.get("authId")!= null ? paraMap.get("authId").toString() : "";
		//필수 값 체크 타입& 액션 & 유저아이디
		if(StringUtil.isEmpty(type) || StringUtil.isEmpty(action) || StringUtil.isEmpty(authId)){
			list.put("status",Constant.RESULT_FALSE);
			list.put("message","실패"+":[-2001] [type|action|authid] 필수값이 존재하지 않습니다. 필수값 유무를 확인하세요.");
		    return list;
		}
		
		
		interfacelogger.info("External PARAMETER : 1) type = "+ type +": 2) action = "+action +": 3) authId = "+ authId);
		try {
			
			SessionVO sessionVO = externalService.interfaceSessionVO(request, authId);
			
			//대소문자 구열없애기
			if(type.equalsIgnoreCase(Constant.INTERFACE_TYPE_DOCUMENT)){
				type = Constant.INTERFACE_TYPE_DOCUMENT;
			}else if(type.equalsIgnoreCase(Constant.INTERFACE_TYPE_PAGE)){
				type = Constant.INTERFACE_TYPE_PAGE;
			}else if(type.equalsIgnoreCase(Constant.INTERFACE_TYPE_GROUP)){
				type = Constant.INTERFACE_TYPE_GROUP;
			}
			
			switch(type){
				//문서
				case Constant.INTERFACE_TYPE_DOCUMENT:
					
						list = externalService.interfaceTypeDocument(paraMap, null, sessionVO);
						list.put("message","성공");	
						list.put("status",Constant.RESULT_SUCCESS);
						
					break;
				//파일
				case Constant.INTERFACE_TYPE_PAGE:
					
						list = externalService.interfaceTypePage(paraMap, null, sessionVO);
						list.put("message","성공");	
						list.put("status",Constant.RESULT_SUCCESS);
						//페이지 조회일 경우에만 pageList 반환
						if(action.equalsIgnoreCase(Constant.INTERFACE_ACTION_SELECTPAGELIST)){
							list.put("pageList",list.get("pageList"));	
						}
					break;
				case Constant.INTERFACE_TYPE_GROUP:
					
					list = externalService.interfaceTypeGroup(paraMap,  sessionVO);
					list.put("message","성공");	
					list.put("status",Constant.RESULT_SUCCESS);
					//페이지 조회일 경우에만 pageList 반환
					if(action.equalsIgnoreCase(Constant.INTERFACE_ACTION_SELECTPAGELIST)){
						list.put("pageList",list.get("pageList"));	
					}
				break;
				default:				
					list.put("message","실패"+":[-2007] type 값이 유효하지 않습니다.");
					list.put("status",Constant.RESULT_FALSE);
				break;
			}
		}catch(BizException e){		
			list.put("message","실패"+":"+e.getMessage());
			list.put("status",Constant.RESULT_FALSE);
			System.out.println("============================");
			System.out.println("BizException = "+ e.getMessage());
			System.out.println("============================");

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
			
		} catch (Exception e) {
			list.put("message","실패"+":"+e.getMessage());
			list.put("status",Constant.RESULT_FALSE);	
			System.out.println("============================");
			System.out.println("Exception = " + e.getMessage());
			System.out.println("============================");	

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		} 

		return list;

    }
		
	/**
	 * <pre>
	 * 1. 개용 :[1000] 외부시스템 연계
	 * 2. 처리내용 : 첨부파일 다운로드
	 * </pre>
	 * @Method Name : interfaceFiledownloadHandler
	 * @param paraMap
	 * @param response
	 * @return  ResponseEntity<byte[]>
	 */	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/interfaceFiledownload", method = RequestMethod.POST,produces="applicaion/json; charset=utf8")
	@ResponseBody
	public ResponseEntity<byte[]> interfaceFiledownloadHandler(
			@RequestParam HashMap<String,Object> paraMap, 
    		HttpServletRequest request,HttpServletResponse response) {

		interfacelogger.info("==================== interfaceFiledownloadHandler =====================");
		
   		String errorMsg = "";

    	HttpHeaders headers = new HttpHeaders();
    	File toServeUp = null;
    	InputStream inputStream = null;
   		
    	
    	Map<String,Object> list = new HashMap<String, Object>();
    	String downFile = "";
    	InputStreamResource inputStreamResource = null;
    	try{
    		
    		SessionVO sessionVO = externalService.interfaceSessionVO(request, paraMap.get("authId").toString());
    		list = externalService.interfaceTypePage(paraMap, null, sessionVO);
    		toServeUp = (File)list.get("file");
    		downFile = list.get("downFile").toString();
    		
    		inputStream = new FileInputStream(toServeUp);
    		
        	inputStreamResource = new InputStreamResource(inputStream);
        	Long fileSize = toServeUp.length();
        	headers.setContentLength(fileSize.intValue());
        	
        	response.setContentType("application/octer-stream;charset=utf-8");
        	response.setCharacterEncoding("utf-8");
        	//파일명 인코드
			response.setHeader("filename", java.net.URLEncoder.encode(downFile,"UTF-8") );
			
    	}catch(BizException e){
			response.setHeader("errorMsg", e.getMessage());

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		}catch(FileNotFoundException e){
            errorMsg = "Not Found DownLoadFile";
			response.setHeader("errorMsg", errorMsg);

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		} catch (UnsupportedEncodingException e) {
			errorMsg = "Can't Not Encoding FileName";
			response.setHeader("errorMsg", errorMsg);

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		}catch (Exception e) {
            errorMsg = "Not Connected FileServer";
			response.setHeader("errorMsg", errorMsg);

			interfacelogger.debug(StringUtil.getHashMap(paraMap));
			interfacelogger.debug(StringUtil.getErrorTrace(e));	
		} 
    	
    	
    	return new ResponseEntity(inputStreamResource,headers,HttpStatus.OK);
   	}
	
	

	@RequestMapping(value = "/postwithfile", method = RequestMethod.POST)
	@ResponseBody
    //public List<HashMap<String,Object>> uploadMultipleFileHandler(
	public Map<String,Object> uploadMultipleFileHandlerTest(
    		@RequestHeader HashMap<String,Object> headMap, 
    		@RequestParam HashMap<String,Object> paraMap, 
    		@RequestParam("fileUpload") MultipartFile[] files,
    		HttpServletRequest request)
    				throws IllegalStateException, IOException {

		Map<String,Object> list = new HashMap<String, Object>();
		
        
    	list.put("result","SUCCESS"); 
	    return list;
 
    }
	

	/**	
	//-----------------------------------------------------------------------------------
	// [1003] 20160115 하나대투 요구사항 테스트 url로 파일 다운로드
	 * http://localhost:8080/EDMS3.0/external/pageDownLoad.do?params=PAG000000000027
	//-----------------------------------------------------------------------------------
	 * <pre>
	 * 1. 개용 : PAGE 다운로드 처리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : pageDownLoad
	 * @param model
	 * @param map
	 * @param sessionVO
	 * @return ModelAndView
	 */
	@RequestMapping("/pageDownLoad.do")
	public ModelAndView pageDownLoad(Model model, @RequestParam HashMap<String,Object> map) {
		
		List<PageVO> pageList = new ArrayList<PageVO>();
		PageVO pageVO = new PageVO();
		
		try {
			
			// 1.다운로드 대상 목록 및 PageVO 객체 구하기
			pageVO = commonService.pagePageInfo(map);
			pageList.add(pageVO);
						
			// 2.다운로드 VIEW 		
			model.addAttribute("isZip",Constant.F);
			model.addAttribute("pageList",pageList);
					
		}catch(Exception e) {
			logger.error(e.getMessage());
			model.addAttribute("message",e.getMessage());
			return new ModelAndView("error/404");
		}
		
		return new ModelAndView(new DownloadView());
	}
	
	
}
