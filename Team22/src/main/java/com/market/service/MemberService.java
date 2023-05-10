package com.market.service;

import java.util.List;
import java.util.Map;

import com.market.domain.MemberVO;
import com.market.domain.ProductVO;

public interface MemberService {
	public MemberVO loginMember(MemberVO vo);

	public void memberJoin(MemberVO vo);
	
	//아이디 중복체크
	public boolean isDuplicated(String member_id);
		
	//닉네임 중복체크
	public boolean isCopy(String member_nickname);
	// 다른 회원 판매 목록
	public List<ProductVO> memProdList(String id) throws Exception;
	// 다른 회원 리뷰
	public List<Map<String,Object>> memSellReview(String id) throws Exception;
	// 회원 상세 불러오기
	public MemberVO memberInfo(String id)throws Exception;
	// 회원 수정
	public void memberInfoUpdate(MemberVO vo)throws Exception;
	// 회원사진수정
	public void modifyUimage(String id, String memberPicPath) throws Exception;
	
	public List<MemberVO> findId(String memberEmail)throws Exception;
	
	public int findIdCheck(String memberEmail)throws Exception;
	// 회원탈퇴
	public void removeMember(MemberVO vo)throws Exception;
	// 유저 프로필정보
	public List<Map<String,Object>> userInfo(String id)throws Exception;
	//평점
	public Double memberScore(String id) throws Exception;
	
}
