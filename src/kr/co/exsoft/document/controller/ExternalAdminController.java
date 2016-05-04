package kr.co.exsoft.document.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import kr.co.exsoft.eframework.util.ConfigData;
import kr.co.exsoft.eframework.util.CommonUtil;
import kr.co.exsoft.document.service.TypeService;
import kr.co.exsoft.common.service.CommonService;
import kr.co.exsoft.common.vo.SessionVO;
import kr.co.exsoft.eframework.configuration.Constant;
import kr.co.exsoft.eframework.exception.BizException;
import kr.co.exsoft.eframework.library.LocaleLibrary;
import kr.co.exsoft.external.service.ExternalService;
import kr.co.exsoft.external.vo.ExternalVO;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springmodules.validation.commons.DefaultBeanValidator;

import kr.co.exsoft.document.vo.*;

/**
 * External Admin Controller
 * @author 패키지 개발팀
 * @since 2015.09.14
 * @version 3.0
 *
 */
@Controller
@SessionAttributes("sessionVO")
@RequestMapping("/admin")
public class ExternalAdminController {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private ExternalService externalService;
	
    @Autowired
    private DefaultBeanValidator beanValidator;
    
	protected static final Log logger = LogFactory.getLog(ExternalAdminController.class);

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 외부연계코드관리
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : localExtManager
	 * @param sessionVO
	 * @param model
	 * @param map
	 * @return String
	 */
	@RequestMapping("externalManager.do")
	public String typeManager(@ModelAttribute SessionVO sessionVO,Model model,@RequestParam HashMap<String,Object> map) {
		
		//Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		Map<String, Object> menuInfo = new HashMap<String, Object>();
		
		param.put("menu_cd",map.get("menu_cd") != null ?  map.get("menu_cd").toString() : "" );
		param.put("role_id",sessionVO.getSessRole_id());	

		String part = "";			// ALL/GROUP/TEAM - 관리자 페이지마다 다르게 적용처리한다.
		
		try {
			
			// 1. 관리자 ROLE 접근권한
			part = commonService.getMenuAuth(param);
			
			// 2. 페이지 네비게이션 :: 상위메뉴명 / 현재메뉴명

			menuInfo = commonService.pageMenuInfo(param);
			
		}catch(BizException e){	
			logger.error(e.getMessage());
		}catch(Exception e)	{										
			logger.error(e.getMessage());
		}
		
		// 접근권한이 없거나 메뉴코드가 없는 경우 403 ERROR 페이지 이동 처리
		if(part.equals("") || param.get("menu_cd").toString().equals(""))	{			
			return "error/403";
		}
		
		// call by reference
		CommonUtil.setSessionToModel(model, sessionVO);
		
		model.addAttribute("menuInfo",menuInfo);
		model.addAttribute("topSelect",Constant.TOPMENU_DOCUMENT);
		model.addAttribute("subSelect",Constant.DOCUMENT_EXTERNALMANAGER);
		
		return "docadmin/externalManager";
	}

	/**
	 * 
	 * <pre>
	 * 1. 개용 : 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : externalList
	 * @param model
	 * @param sessionVO
	 * @param map
	 * @param request
	 * @return Map<String,Object>
	 */
	@RequestMapping(value="/externalList.do", method=RequestMethod.POST)
	@ResponseBody 
	public Map<String,Object> externalPageList(Model model, @ModelAttribute SessionVO sessionVO, @RequestParam HashMap<String,Object> map,
			HttpServletRequest request) {
		
		Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		// 입력 파라미터 유효성 체크
		param.put("contextRoot",sessionVO.getSessContextRoot());
		param.put("strIndex",map.get("strIndex") != null ? map.get("strIndex") : "" );
		param.put("strKeyword",map.get("strKeyword") != null ? map.get("strKeyword") : "" );
		param.put("orderCol",map.get("sidx") != null ? map.get("sidx") : "WORK_CODE");
		param.put("orderType",map.get("sord") != null ? map.get("sord") : "ASC");
		param.put("page_size",map.get("rows") != null ? map.get("rows") : sessionVO.getSessPage_size());		
		param.put("is_admin",Constant.T);
		param.put("nPage",CommonUtil.getPage(map));
		
		try {
			//인터페이스 코드 목록 가져오기
			resultMap = externalService.externalPageList(param);
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());				
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
		}
						
		return resultMap;
	}
	/**
		 * 
		 * <pre>
		 * 1. 개용 : 문서유형관리 상세 정보 가져오기.
		 * 2. 처리내용 : 
		 * </pre>
		 * @Method Name : typeInfo
		 * @param model
		 * @param sessionVO
		 * @param map
		 * @param request
		 * @return Map<String,Object>
		 */
		@RequestMapping(value="/externalInfo.do", method=RequestMethod.POST)
		@ResponseBody 
		public Map<String,Object> typeInfo(Model model, @ModelAttribute SessionVO sessionVO, @RequestParam HashMap<String,Object> map,
				HttpServletRequest request) {
			
			Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			try {
				
				resultMap = externalService.externalDetailList(map);
				resultMap.put("result",Constant.RESULT_TRUE);
			}catch(BizException e){
				resultMap.put("result",Constant.RESULT_FALSE);
				resultMap.put("message",e.getMessage());				
			}catch(Exception e)	{										
				resultMap.put("result",Constant.RESULT_FALSE);
				resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			}
									
			return resultMap;
		
		}
		
		
		/**
		 * [1001]
		 * <pre>
		 * 1. 개용 : 문서유형 등록/수정/삭제 처리 
		 * 2. 처리내용 : 
		 * </pre>
		 * @Method Name : externalControl
		 * @param model
		 * @param sessionVO
		 * @param map
		 * @param request
		 * @return Map<String,Object>
		 */
		@RequestMapping(value="/externalControl.do", method=RequestMethod.POST)
		@ResponseBody 
		public Map<String,Object> typeControl(Model model, @ModelAttribute SessionVO sessionVO, @RequestParam HashMap<String,Object> map,
				BindingResult bindingResult,HttpServletRequest request) {
			
			Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			// 업무구분자 - 인터펭이스 등록/수정/삭제
			String type = map.get(Constant.TYPE) != null ? map.get(Constant.TYPE).toString() : "";
			
			try {
			
				if(type.equals(Constant.INSERT))	{
					// 인터페이스코드 테이블 삽입처리
					resultMap = externalService.externalManagerUpdate(map, sessionVO, Constant.INSERT);						
				}else if(type.equals(Constant.UPDATE))	{
					// 인터페이스코드 테이블 수정처리
					resultMap = externalService.externalManagerUpdate(map, sessionVO,Constant.UPDATE );
				}else if(type.equals(Constant.DELETE))	{
					// 인터페이스코드 테이블 삭제처리
					resultMap = externalService.externalManagerUpdate(map, sessionVO,Constant.DELETE );					
				}
				
						
			}catch(BizException e){
				resultMap.put("result",Constant.RESULT_FALSE);
				resultMap.put("message",e.getMessage());				
			}catch(Exception e)	{										
				resultMap.put("result",Constant.RESULT_FALSE);
				resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
			}

			return resultMap;
		
		}
		
	/**
	 * 
	 * <pre>
	 * 1. 개용 : 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : typeList
	 * @param model
	 * @param sessionVO
	 * @param map
	 * @param request
	 * @return Map<String,Object>
	 *//*
	@RequestMapping(value="/typeList.do", method=RequestMethod.POST)
	@ResponseBody 
	public Map<String,Object> typePageList(Model model, @ModelAttribute SessionVO sessionVO, @RequestParam HashMap<String,Object> map,
			HttpServletRequest request) {
		
		Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		// 입력 파라미터 유효성 체크
		param.put("contextRoot",sessionVO.getSessContextRoot());
		param.put("strIndex",map.get("strIndex") != null ? map.get("strIndex") : "" );
		param.put("strKeyword",map.get("strKeyword") != null ? map.get("strKeyword") : "" );
		param.put("orderCol",map.get("sidx") != null ? map.get("sidx") : "TYPE_NAME");
		param.put("orderType",map.get("sord") != null ? map.get("sord") : "ASC");
		param.put("page_size",map.get("rows") != null ? map.get("rows") : sessionVO.getSessPage_size());		
		param.put("is_admin",Constant.T);
		param.put("nPage",CommonUtil.getPage(map));
		
		try {
				
			// 문서유형 목록 가져오기
			resultMap = typeService.typePageList(param);			
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());				
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
		}
						
		return resultMap;
	}
	
	*
	
	*//**
	 * 
	 * <pre> 
	 * 1. 개용 : 문서유형 속성 리스트 가져오기 
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : attrList
	 * @param model
	 * @param sessionVO
	 * @param map
	 * @param request
	 * @return Map<String,Object>
	 *//*
	@RequestMapping(value="/attrList.do", method=RequestMethod.POST)
	@ResponseBody 
	public Map<String,Object> attrList(Model model, @ModelAttribute SessionVO sessionVO, @RequestParam HashMap<String,Object> map,
			HttpServletRequest request) {
		
		Locale locale = LocaleLibrary.setLocale(sessionVO.getSessLanguage() != null  ? sessionVO.getSessLanguage() : ConfigData.getString("LANGUAGE"));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
							
			resultMap = typeService.attrList(map);			
			
		}catch(BizException e){
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",e.getMessage());				
		}catch(Exception e)	{										
			resultMap.put("result",Constant.RESULT_FALSE);
			resultMap.put("message",messageSource.getMessage("common.system.error",new Object[0],locale));			
		}
								
		return resultMap;
	
	}
	
	*/
}
