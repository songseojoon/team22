package com.market.controller;


import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.market.domain.MemberVO;
import com.market.domain.ProductVO;
import com.market.service.MemberService;

@Controller
@RequestMapping(value = "/members")
public class MembersController {
	
	

	@Inject
	private MemberService service;

	private static final Logger logger = LoggerFactory.getLogger(MembersController.class);
	
	//http://localhost:8080/main
	// 로그인 - 정보입력
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginGET() {
		
		return "/members/loginForm";
	}
	// 로그인 - 정보처리
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginPOST(HttpSession session, MemberVO vo) {
		
		MemberVO result = service.loginMember(vo);
		
		if(result != null) {
			session.setAttribute("id", result.getMember_id());
			
			return "redirect:/main";
		}else {
			return "redirect:/members/login";
		}
		
	}
	//로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutGET(HttpSession session) {
		session.invalidate();
		
		return "redirect:/main";
	}
	// 마이페이지
	@RequestMapping(value = "/myPage", method = RequestMethod.GET)
	public String myPageGET() {
		
		
		return "/members/myPage";
	}
	

	// 회원가입-정보입력
	@RequestMapping(value = "/insert", method=RequestMethod.GET)
	public String insertGET() {
		logger.info("insertGET() 호출");
		logger.info(" /insert 주소에 연결된 view페이지(./members/insertForm.jsp)�� �̵� ");
		return "/members/insertForm";
	}
//	// 회원가입-정보처리
//	@RequestMapping(value="/insert", method = RequestMethod.POST)
//	public String insertPOST(MemberVO vo) {
//		logger.info("insertPOST() 호출");
//		logger.info(vo+"toString");
//		
//
//		service.memberJoin(vo);
//		
//		return "redirect:/members/login";
//				
//	}
	
	// 회원가입-정보처리
		@RequestMapping(value="/insert", method = RequestMethod.POST)
		public String insertPOST(MemberVO vo) throws Exception{
			logger.info("insertPOST() 호출");
			logger.info(vo+"toString");
			int result = service.idChk(vo);
			try {
				if(result ==1 ) {
					return "/members/insert";
				}else if(result ==0) {
					service.memberJoin(vo);
					
				}
			}catch(Exception e) {
				throw new RuntimeException();
			}
			return "redirect:/login";

		
		}
	// 아이디 중복 체크
	@ResponseBody
	@RequestMapping(value="/idChk", method=RequestMethod.POST)
	public int idChk(MemberVO vo) throws Exception{
		int result= service.idChk(vo);
		return result;
	}
	
	// 다른 회원 정보 및 판매중인 상품
	@RequestMapping(value = "/memberInfo", method = RequestMethod.GET)
	public void memProdListGET(@RequestParam("mem_id") String mem_id, Model model) throws Exception{

		logger.info("전달정보 id: "+ mem_id);
		List<ProductVO> memProdList = service.memProdList(mem_id);
		
		model.addAttribute("memProdList",memProdList);
		
	}
	
	// 다른 회원 판매후기
	
		@RequestMapping(value = "/review", method = RequestMethod.GET)
		public String memReview(Model model,HttpSession session,
						@RequestParam("mem_id") String id) throws Exception {
			logger.info("id판매후기@@@@@@@@@@@@@@@@@@@@@@: "+ id);
			List<Map<String,Object>> memReview = service.memSellReview(id);
			model.addAttribute("memReview",memReview);
			
			return "/members/memReview";
		}
	
	// 회원 수정	
		@RequestMapping(value = "/memberUpdate",method = RequestMethod.GET)
		public String memberUpdateGET(HttpSession session) {
			String id = (String)session.getAttribute("id");
			if(id == null) {
				return "redirect:/login";
			}
			
			return "/members/updateForm";
		}
		
		
		
		
}
