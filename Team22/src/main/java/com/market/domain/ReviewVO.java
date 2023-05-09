package com.market.domain;

import lombok.Data;

@Data
public class ReviewVO {

	private int rv_num;
	private String reviewee;
	private String reviewer;
	private int rv_score;
	private String rv_content;
	private String rv_cate;
	private int prod_num;
	private int au_num;
	private int ran_num;
	
	
	
}
