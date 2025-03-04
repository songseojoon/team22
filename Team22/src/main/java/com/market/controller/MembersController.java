package com.market.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.itwillbs.util.GeoLocation;
import com.itwillbs.util.Ip;
import com.itwillbs.util.JsonParser;
import com.itwillbs.util.KakaoLoginBO;
import com.itwillbs.util.NaverLoginBO;
import com.itwillbs.util.UploadFileUtils;
import com.market.domain.CPageDTO;
import com.market.domain.CustomerserviceVO;
import com.market.domain.MemberVO;
import com.market.domain.Pay_chargeVO;
import com.market.domain.ProductVO;
import com.market.domain.TradeVO;
import com.market.service.CustomerserviceService;
import com.market.service.MailSendService;
import com.market.service.MemberService;


@Controller
@RequestMapping(value = "/members")
public class MembersController {
	
	
	@Inject
	private MemberService service;
	@Inject
	private CustomerserviceService CsService;
	
	@Autowired
	private MailSendService mailService;

	@Inject
	@Named("uploadPath")
	private String uploadPath;
	
	private String apiResult = null;
	
	// NaverLoginBO
	@Inject
	private NaverLoginBO naverLoginBO;
	
	// KakaoLoginBO
	@Inject
	private KakaoLoginBO kakaoLoginBO;
	
	private static final Logger logger = LoggerFactory.getLogger(MembersController.class);
	
	//http://localhost:8080/main
	
	// 로그인  + 간편 로그인
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginGET(Model model, HttpSession session) {
		
		// 네이버 아이디로 인증 URL을 생성하기 위하여 naverLoginBO클래스의 getAuthorizationUrl메소드 호출 
		String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
		logger.info("네이버 : " + naverAuthUrl);

		model.addAttribute("naverurl", naverAuthUrl);
		
		// 카카오
		String kakaoAuthUrl = kakaoLoginBO.getAuthorizationUrl(session);
		logger.info("카카오 : " + kakaoAuthUrl);
		
		model.addAttribute("kakaourl", kakaoAuthUrl);		
 
		return "/members/loginForm";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginPOST(Model model, HttpSession session,
				MemberVO vo ) {
		  
		if(service.loginMember(vo) != null) { 
			session.setAttribute("id", vo.getMember_id()); 
			} 
		else { service.memberJoin(vo);
			session.setAttribute("id", vo.getMember_id()); 
		}
		
		return "redirect:/main";
	}
	
	// 네이버 로그인 성공시 callback 호출 
	@RequestMapping(value = "callback", method = { RequestMethod.GET,RequestMethod.POST })
	public String callbackNaver(Model model, HttpSession session, MemberVO vo, 
								@RequestParam String code, @RequestParam String state) throws Exception {
	  
		logger.info(" 네이버 로그인 성공 callbackNaver ");
		JsonParser json = new JsonParser();
		  
		OAuth2AccessToken oauthToken = naverLoginBO.getAccessToken(session, code, state);
		  
		// 로그인 사용자 정보를 읽어옴
		String apiResult = naverLoginBO.getUserProfile(oauthToken);
		  
		vo = json.changeJson(apiResult);
		  
		logger.info("apiResult {}",apiResult);
		  
		if(service.loginMember(vo) != null) { 
			session.setAttribute("id", vo.getMember_id());
		} else{ 
			service.memberJoin(vo);
			session.setAttribute("id", vo.getMember_id());
		}
	
		model.addAttribute("result", apiResult);
		  
		return "redirect:/main"; 
	}
	
	// 카카오 로그인 성공시 callback 호출 
	@RequestMapping(value = "callbackKakao", method = { RequestMethod.GET,RequestMethod.POST })
	public String callbackKakao(Model model, HttpSession session, MemberVO vo, 
								@RequestParam String code, @RequestParam String state) throws Exception {
		
		logger.info(" 카카오 로그인 성공 callbackKakao ");
				
		OAuth2AccessToken oauthToken;
		oauthToken = kakaoLoginBO.getAccessToken(session, code, state);	
		
		apiResult = kakaoLoginBO.getUserProfile(oauthToken);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(apiResult);
		
		logger.info("jsonObjcet : " + jsonObject);
		
		JSONObject responseObj = (JSONObject) jsonObject.get("kakao_account");
		JSONObject responseObj2 = (JSONObject) responseObj.get("profile");
		
		String kakaoId = "K_" + (String) responseObj.get("email").toString().split("@")[0];
		String kakaoPw = "5678";
		String email = (String) responseObj.get("email");
		String name = (String) responseObj2.get("nickname");
		
		vo.setMember_id(kakaoId);
		
		if (service.loginMember(vo) != null) {
		    session.setAttribute("id", vo.getMember_id());
		} else {
		    vo.setMember_pass(kakaoPw);
		    vo.setMember_email(email);
		    vo.setMember_name(name);
		    vo.setMember_nickname(name);
			    if (!service.isDuplicated(vo.getMember_id())) { // 등록된 아이디가 없을 경우
			        service.memberJoin(vo); // DB에 정보 등록
			    }
		    session.setAttribute("id", vo.getMember_id());
		}
		
		return "redirect:/main"; 
	}

	
	//濡쒓렇�븘�썐
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutGET(HttpSession session) {
		session.invalidate();
		
		return "redirect:/main";
	}
	// 留덉씠�럹�씠吏�
	@RequestMapping(value = "/myPage", method = RequestMethod.GET)
	public String myPageGET(Model model, HttpSession session)throws Exception{
		String id = (String)session.getAttribute("id");
		model.addAttribute("memberInfo",service.memberInfo(id));
		session.setAttribute("memberInfo",service.memberInfo(id));

		logger.info("@@@@@@@@@@@@@@@@@@@@@id"+id);
		
		model.addAttribute("count",service.countTrade(id));
	 	  model.addAttribute("score",service.memberScore(id));
	 	  
	
		
		return "/members/myPage";
	}
	// 페이충전 
	@RequestMapping(value = "/payInfo",method = RequestMethod.POST)
	@ResponseBody
	public void payInfo(Model model, HttpSession session,Integer amount)throws Exception{
		String id = (String)session.getAttribute("id");
		logger.info("@@@@@@@@@@@@@@@@@@@@@id"+id);
		logger.info("@@@@@@@@@@@@"+amount); 
		  Pay_chargeVO vo = new Pay_chargeVO();
		  vo.setMember_id(id); vo.setCharge_amount(amount); 
		  
		  if(service.savePayCharge(vo) > 0) 
			 service.memberPayCharge(vo);
		  
		  service.memberInfo(id);
		  
	}
	
	// 페이충전 확인페이지
	@RequestMapping(value = "/payInfo",method = RequestMethod.GET)
	public void payInfoGET(Model model, HttpSession session,@RequestParam("money") Integer amount)throws Exception{
		String id = (String)session.getAttribute("id");

		model.addAttribute("memberInfo",service.memberInfo(id));
		model.addAttribute("amount",amount);
	}
	
	// 페이충전 내역
	@RequestMapping(value = "/chargingDetails",method = RequestMethod.GET)
	public String chargingDetails(HttpSession session,Model model)throws Exception{
		String id = (String)session.getAttribute("id");
		model.addAttribute("chargingDetails",service.chargingDetails(id));
		return "/members/drawDetails";
	}
	
	// 페이충전 내역
	@RequestMapping(value = "/drawDetails",method = RequestMethod.GET)
	public void drawDetails(HttpSession session,Model model)throws Exception{
		String id = (String)session.getAttribute("id");
//		List<Map<String,Object>> chargingDetails = service.chargingDetails(id);
		model.addAttribute("drawDetails",service.drawDetails(id));
		model.addAttribute("chargingDetails",service.chargingDetails(id));
		
		List drawDetails = service.drawDetails(id);
		List chargingDetails = service.chargingDetails(id);

		List combinedList = new ArrayList<>();
		combinedList.addAll(drawDetails);
		combinedList.addAll(chargingDetails);

		model.addAttribute("combinedDetails", combinedList);

	}
	

	// �쉶�썝媛��엯-�젙蹂댁엯�젰
	@RequestMapping(value = "/insert", method=RequestMethod.GET)
	public String insertGET() {
		logger.info("insertGET() �샇異�");
		return "/members/insertForm";
	}
	// �쉶�썝媛��엯-�젙蹂댁쿂由�
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public String insertPOST(MemberVO vo,MultipartFile file,RedirectAttributes rttr)throws Exception {
		logger.info(vo+"toString");
	
		String imgUploadPath = uploadPath + File.separator + "imgUpload";
		String ymdPath = UploadFileUtils.calcPath(imgUploadPath);
		String fileName = null;

		if(file != null  && !file.isEmpty()) {
		 fileName =  UploadFileUtils.fileUpload(imgUploadPath, file.getOriginalFilename(), file.getBytes(), ymdPath); 
		} else {
		 fileName = uploadPath + File.separator + "images" + File.separator + "none.png";
		}

		vo.setMember_pic(File.separator + "imgUpload" + ymdPath + File.separator + fileName);
		rttr.addFlashAttribute("result","O");
		
		service.memberJoin(vo);
		
		return "redirect:/members/login";
				
	}

	// �븘�씠�뵒 以묐났泥댄겕
    @RequestMapping(value="/idCheck")
    @ResponseBody
    public boolean idCheck(@RequestBody String member_id) throws Exception {
        return service.isDuplicated(member_id);
    }
    
    // �땳�꽕�엫 以묐났泥댄겕 
    @RequestMapping(value="/nickCheck")
    @ResponseBody
    public boolean nickCheck(@RequestBody String member_nickname) throws Exception{
    	return service.isCopy(member_nickname);
    }
    
    // �떎瑜� �쉶�썝 �뙋留� 紐⑸줉
    @RequestMapping(value = "/memberInfo", method = RequestMethod.GET)
    public String memberInfoGET(Model model, HttpServletRequest request) throws Exception {
    	String id =  request.getParameter("mem_id");
    	List<ProductVO> memProdList = service.memProdList(id);
    	List<Map<String,Object>> userInfo = service.userInfo(id);
		model.addAttribute("memProdList",memProdList);
		model.addAttribute("userInfo",userInfo);
    	return "/members/memberInfo";
    }
    // �떎瑜� �쉶�썝 由щ럭 紐⑸줉
    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String memberReviewGET(Model model,HttpServletRequest request) throws Exception {
    	String id =  request.getParameter("mem_id");
    	List<Map<String,Object>> memReview = service.memSellReview(id);
    	List<Map<String,Object>> userInfo = service.userInfo(id);
    	
    	logger.info("@@@@@@@@@@@@@@@memReivew"+memReview);
    	logger.info("@@@@@@@@@@@@@@@muserInfow"+userInfo);
    	model.addAttribute("memReview",memReview);
    	model.addAttribute("userInfo",userInfo);
    	return "/members/memReview";
    }
    
    // �쉶�썝 �젙蹂� �닔�젙 �럹�씠吏�
    @RequestMapping(value = "/memberUpdate", method = RequestMethod.GET)
    public String memberUpdateGET(HttpSession session,Model model) throws Exception {
    	String id = (String)session.getAttribute("id");
    	
    	model.addAttribute("memberInfo",service.memberInfo(id));
    	
    	if (id == null) {
    		return "redirect:/members/login";
    	}
    	
    	return "/members/updateForm";
    }
    
    // �쉶�썝 �젙蹂� �닔�젙 �엯�젰
    @RequestMapping(value = "/memberUpdate", method = RequestMethod.POST)
    public String memberUpdatePOST(MemberVO vo, MultipartFile file, HttpServletRequest req,RedirectAttributes rttr) throws Exception {
    	
    	
    	logger.info("@@@@@@@@@@@@@@memberUpdate"+vo);
		/*
		 * File convFile = new File(file.getOriginalFilename());
		 * file.transferTo(convFile);
		 * 
		 * if(file.getOriginalFilename() != null&& !file.isEmpty()) { new
		 * File(uploadPath + req.getParameter("member_pic")).delete();
		 * 
		 * String imgUploadPath = uploadPath + File.separator + "imgUpload"; String
		 * ymdPath = UploadFileUtils.calcPath(imgUploadPath); String fileName =
		 * UploadFileUtils.fileUpload(imgUploadPath, file.getOriginalFilename(),
		 * file.getBytes(), ymdPath);
		 * 
		 * vo.setMember_pic(File.separator + "imgUpload" + ymdPath + File.separator +
		 * fileName);
		 * 
		 * } else {
		 * 
		 * vo.setMember_pic(req.getParameter("member_pic"));
		 * 
		 * }
		 */
    	String imgUploadPath = uploadPath + File.separator + "imgUpload";
		String ymdPath = UploadFileUtils.calcPath(imgUploadPath);
		String fileName = null;

		if(file != null  && !file.isEmpty()) {
		 fileName =  UploadFileUtils.fileUpload(imgUploadPath, file.getOriginalFilename(), file.getBytes(), ymdPath); 
		} else {
		 fileName = uploadPath + File.separator + "images" + File.separator + "none.png";
		}

		vo.setMember_pic(File.separator + "imgUpload" + ymdPath + File.separator + fileName);
		rttr.addFlashAttribute("result","O");
    	 
    	 rttr.addFlashAttribute("update","update");
    	 service.memberInfoUpdate(vo);
    	 
    	
    	return "redirect:/members/myPage";
    		
    }
    // 占쎈툡占쎌뵠占쎈탵 筌≪뼐由�
    @RequestMapping(value="/findIdView", method=RequestMethod.GET)
	public String findIdView() throws Exception{
		return"/members/findIdView";
	}
	
	@RequestMapping(value="/findId", method=RequestMethod.POST)
	public String findId(MemberVO memberVO,Model model) throws Exception{
		logger.info("memberEmail"+memberVO.getMember_email());
				
		if(service.findIdCheck(memberVO.getMember_email())==0) {
		model.addAttribute("msg", "占쎌뵠筌롫뗄�뵬占쎌뱽 占쎌넇占쎌뵥占쎈퉸雅뚯눘苑�占쎌뒄");
		return "/members/findIdView";
		}else {
		model.addAttribute("member", service.findId(memberVO.getMember_email()));
		return "/members/findId";
				
		}
	}
	
	
	// 충전,출금내역
	@RequestMapping(value="/pay", method=RequestMethod.GET)
	public String payList() throws Exception{
		
		return "/members/pay";
	}
	@RequestMapping(value="/payCharge", method=RequestMethod.GET)
	public String payCharges(HttpSession session,Model model) throws Exception{
		String id = (String)session.getAttribute("id");
		
		model.addAttribute("memberInfo",service.memberInfo(id));
		
		return "/members/payCharge";
	}
	
	@RequestMapping(value="/payWithdraw", method=RequestMethod.GET)
	public String payWithdraw(HttpSession session,Model model,MemberVO vo) throws Exception{
		String id = (String)session.getAttribute("id");
		model.addAttribute("memberInfo",service.memberInfo(id));
		
		return "/members/payWithdraw";
	}
	
	  @RequestMapping(value="/payWithdraw", method=RequestMethod.POST) 
	  public String payWithdrawPOST(HttpSession session,Model model,@RequestParam Map<String,Object> vo,
			  @RequestParam("withdraw_amount")Integer amount) throws Exception{ 
	  String id = (String)session.getAttribute("id");
	  
	  logger.info("@@@@@@@@@@@@@@@@"+vo);
	  vo.put("member_id", id);
	  service.payWithdraw(vo);
	  Pay_chargeVO pVo = new Pay_chargeVO();
	  pVo.setMember_id(id); pVo.setWithdraw_amount(amount); 
	  if(service.savePayWithdraw(pVo) > 0 );
	  
	  model.addAttribute("payWithdraw",vo.get("withdraw_amount")); 
	  model.addAttribute("memberInfo",service.memberInfo(id));
	  
	  logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@vo"+vo);
	  return "/members/payWithdrawInfo";
	  
	  }
	 
	
    
	@RequestMapping(value = "/updatePwCk", method = RequestMethod.GET)
    public String updatePwCkGET() {
    	return "/members/updatePwCk";
    }
	@RequestMapping(value = "/updatePwCk", method = RequestMethod.POST)
	public String updatePwCkPOST(Model model,@RequestParam("member_pass") String member_pass,
					@RequestParam("member_id")String id,RedirectAttributes rttr)throws Exception {
			MemberVO vo = service.memberInfo(id);
			
			if(vo == null || !vo.getMember_pass().equals(member_pass)) {
				return "/members/updatePwCk";
			}else {
				rttr.addFlashAttribute("result","O");
				return "redirect:/members/memberUpdate";
			}
		 
	}
	
	@RequestMapping(value = "/deletePwCk", method = RequestMethod.GET)
    public String deletePwCkGET() {
		
    	return "/members/deletePwCk";
    }
	@RequestMapping(value = "/deletePwCk", method = RequestMethod.POST)
	public String deletePwCkPOST(Model model,@RequestParam("member_pass") String member_pass,
					@RequestParam("member_id")String id,RedirectAttributes rttr)throws Exception {
			MemberVO vo = service.memberInfo(id);
			if(vo == null || !vo.getMember_pass().equals(member_pass)) {
				return "/members/deletePwCk";
			}else {
				rttr.addFlashAttribute("result","O");
				return "redirect:/members/remove";
			}
			
	}
	@RequestMapping(value = "/remove", method = RequestMethod.GET)
	public String removeGET(HttpSession session,Model model) throws Exception{
		
		String id = (String)session.getAttribute("id");
    	
    	model.addAttribute("memberInfo",service.memberInfo(id));
    	
	    	if (id == null) {
	    		return "redirect:/members/login";
	    	}
			
			return "/members/removeForm";
		}
	
	
	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	public String removePOST(RedirectAttributes rttr,
							@RequestParam("member_id")String id,
							@RequestParam("member_pass") String member_pass,
							Model model,HttpSession session) throws Exception{
		
		MemberVO vo = service.memberInfo(id);
		logger.info("@@@@@@@@@@@@@@memberdelete"+vo);
		
		if(vo == null || !vo.getMember_pass().equals(member_pass)) {
			model.addAttribute("memberInfo", vo) ;
			rttr.addFlashAttribute("result",false);
			return "/members/removeForm";
		}else {
			service.removeMember(vo);
			session.invalidate();
			rttr.addFlashAttribute("result","delete");
			return "redirect:/main";
		}
    	 
    	
	}
	
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String getMemberLocation(Model model, HttpServletRequest request) throws Exception{
	    try {
	        // 현재 위치 정보 가져오기
	        GeoLocationController geoLocationController = new GeoLocationController();
	        String ip = new Ip().getIp(request);
	        String location = geoLocationController.getLocationFromIp(ip);
	        		
	        logger.info("@@@@@@@@@@@@@@location: " + location);
	        // 위도, 경도 정보 추출
	        String[] latlng = location.split(",");
	        double lat = Double.parseDouble(latlng[0]);
	        double lng = Double.parseDouble(latlng[1]);

	        logger.info("@@@@@@@@@@@@@@lat"+lat);
	        logger.info("@@@@@@@@@@@@@@lng"+lng);
	        // 위도, 경도를 이용하여 주소 추출
	        String address = GeoLocation.getAddress(lat, lng);
	        logger.info("@@@@@@@@@@@@@@address"+address);
	        // 모델에 주소 정보 추가
	        model.addAttribute("address", address);
	    } catch (IOException e) {
	        // 예외 처리
	    }

	    return "/main";
	}

	// 비밀번호 찾기 
		@RequestMapping(value = "/findPw", method = RequestMethod.GET)
		public String findPwGET() throws Exception{
			
			return "/members/findPw";
		}
		@RequestMapping(value = "/findPw", method = RequestMethod.POST)
		@ResponseBody
		public String findPwPOST(@RequestParam(value = "id",required = false) String id, @RequestParam(value="email", required = false) String email,HttpServletResponse response) throws Exception{
			System.out.println(id);
			System.out.println(email);
			MemberVO vo = new MemberVO();
			vo.setMember_id(id);
			vo.setMember_email(email);
			service.findPw(response, vo);
			
			return "이메일로 임시 비밀번호를 발송하였습니다.";
		}

		// 이메일 인증
			@GetMapping("/mailCheck")
			@ResponseBody
			public String mailCheck(@RequestParam("email") String email ) {
			System.out.println("이메일 인증 요청이 들어옴!");
				System.out.println("이메일 인증 이메일 : " + email);
				if(email == null || email.isEmpty()) {
					throw new IllegalArgumentException("이메일 주소가 유효하지 않습니다.");
				}
				String result = mailService.joinEmail(email);
			return result;
			}
			@RequestMapping(value = "/myCs", method = RequestMethod.GET)
		    public String myCs(HttpSession session,Model model,@RequestParam("num") int num,
		    		@RequestParam(value = "searchType",required = false, defaultValue = "title") String searchType,
		    		@RequestParam(value = "keyword",required = false, defaultValue = "") String keyword) throws Exception{
				String id = (String)session.getAttribute("id");
				
				CPageDTO dto = new CPageDTO();
			   dto.setNum(num);
			   dto.setCount(CsService.searcountCs(searchType, keyword));
			   dto.setSearchType(searchType);
			   dto.setKeyword(keyword);
			   
				List<CustomerserviceVO> cvoList = service.myCs(id);
				List<CustomerserviceVO> boardList = CsService.boardList(dto.getDisplayPost(), dto.getPostNum(), searchType, keyword);
				
			   model.addAttribute("myCs", cvoList);
			   model.addAttribute("boardList",boardList);
				
			   model.addAttribute("dto",dto);
			   model.addAttribute("select",num);
				
				return "/members/myCs";
		    }	
	
	
}
