<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE HTML>
<html>
<head>
<title>My Page</title>
<style type="text/css">

.review{
	text-align: center;
	font-family: "Noto Sans KR", sans-serif;
	font-weight: bold;
	font-size: 2em;
}

 h1{ 
         font-family: 'Oswald', sans-serif; 
         font-size: 30px; 
         color: #216182; 
     } 
     label { 
         display: block; 
         margin-top: 20px; 
         letter-spacing: 2px; 
     } 

input, textarea { 
         width: 439px; 
         height: 27px; 
         background-color: #efefef; 
         border-radius: 6px; 
         border: 1px solid #dedede; 
         padding: 10px; 
         margin-top: 3px; 
         font-size: 0.9em; 
         color: #3a3a3a; 
     }

input:focus, textarea:focus{ 
             border: 1px solid #97d6eb; 
         } 
         
textarea{ 
         height: 60px; 
         background-color: #efefef; 
     } 
#submit{ 
         width: 127px; 
         height: 48px; 
         text-align: center; 
         border: none; 
         margin-top: 20px; 
         cursor: pointer; 
     } 
#cancel { 
         width: 127px; height: 48px; 
         text-align: center; 
         border: none; 
         margin-top: 20px; 
         cursor: pointer; 
     } 
     
.modal { 
         position: fixed; 
         left: 0; 
         top: 0; 
         width: 100%; 
         height: 100%; 
         background-color: rgba(0, 0, 0, 0.5); 
         opacity: 0; 
         visibility: hidden; 
         transform: scale(1.1); 
         transition: visibility 0s linear 0.25s, opacity 0.25s 0s, transform 0.25s; 
     } 
.close-button { 
         float: right; 
         width: 1.5rem; 
         line-height: 1.5rem; 
         text-align: center; 
         cursor: pointer; 
         border-radius: 0.25rem; 
         background-color: lightgray; 
} 

.modal-content { 
         position: absolute; 
         top: 50%; 
         left: 50%; 
         transform: translate(-50%, -50%); 
         background-color: white; 
         padding: 1rem 1.5rem; 
         width: 500px; 
         height: 300px; 
         border-radius: 0.5rem; 
     } 
.close-button:hover { 
       background-color: darkgray; 
       font-weight: bold;
} 
.show-modal { 

         opacity: 1; 
         visibility: visible; 
         transform: scale(1.0); 
         transition: visibility 0s linear 0s, opacity 0.25s 0s, transform 0.25s; 
}
.script{
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-decoration: none;
  color: 769fcd;
  font-weight: bold;
  }

</style>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
<link rel="stylesheet" href="/resources/assets/css/main.css" />
<link rel="stylesheet" href="/resources/assets/css/myPage.css" />
</head>
<body>

<!-- Wrapper -->
<div id="wrapper">

	<!-- Main -->
	<div id="main">
		<div class="inner">

		<!-- Header -->
		<%@ include file="../include/header.jsp" %>
		<%@ include file="../include/myPage.jsp" %>			
										
			<div class="infoContainer">
				<a href="/trade/buyReview" class="item">구매후기</a> 
				<a href="/trade/sellReview" class="item">판매후기</a>
			</div>    
						
			<section>
			<div class="review">
				<c:if test="${empty tSellReview }">
					등록된 판매 후기가 없습니다.
				</c:if>
			</div>
				<c:forEach var = "vo" items="${tSellReview }">
				<article>
					<div class="shippingStatusContainer">
						<table class="bbsList">
							<thead class="head">
								<tr>
									<th>상품</th>
									<th>상품명</th>	
									<th>리뷰내용</th>
									<th>구매자</th>
								</tr>
							</thead>
							<tbody class="body">
								<c:forEach var = "vo" items="${tSellReview }">
									<td><img src="/resources/images/${vo.product_pic.split(',')[0] }" 
								        onerror="this.src='/resources/images/default_product.jpg'" width="80px" height="60px">
									</td>
									<td>
										<span onclick="window.location.href='/product/prodInfo?product_num=${vo.product_num }&seller=${vo.reviewee}'">
										${vo.product_title}
										</span>
									</td>
									<td>
										${vo.rv_content}

									</td>
									<td>
										${vo.member_nickname}
									</td>
								</c:forEach>
							</tbody>
							
						
						</table>
							
							
							
     					<%-- 	<div class="item">
						        <div>
   		        				<a href="/members/memberInfo?mem_id=${vo.reviewer} ">
						          <div>${vo.member_nickname}님의 후기</div></a>
						        </div>
						        </div>	
						        <div class="item">
						        <div>
						        <img src="/resources/images/${vo.product_pic.split(',')[0] }" 
						        onerror="this.src='/resources/images/default_product.jpg'" alt="" width="100px" height="80px">
						        </div>
						        </div>						
								<div class="item">
						        <div>
   		        				<a href="/product/prodInfo?product_num=${vo.product_num }&seller=${vo.reviewee}">
   		        				
						          <div>${vo.rv_content}</div></a>
						        </div>
						        </div>								
							
							</c:if>
							</div>
 --%>
				</div>
			</article>
			</c:forEach>
		</section>
	</div>
	</div>
		<%@ include file="../include/sidebar.jsp" %>
</div>

<script type="text/javascript">
	//alert("${result}");
	var result = "${result}";
	
	if(result == "ok"){
		alert("구매후기 작성 완료!");		
	}
	
	 var modal = document.querySelector(".modal"); 
     // var trigger = document.querySelector(".trigger");  
      var trigger = document.querySelectorAll(".trigger");  
      var closeButton = document.querySelector(".close-button"); 
      var cancelButton = document.querySelector("#cancel");

      console.log(trigger);
      for( var i = 0; i < trigger.length; i++ ){
     	 trigger[i].addEventListener("click", toggleModal); 
			}
     //console.log(modal);

     function toggleModal() { 
          modal.classList.toggle("show-modal"); 
      }

     function windowOnClick(event) { 
          if (event.target === modal) { 
              toggleModal(); 
          } 
      }

      /* trigger.addEventListener("click", toggleModal);  */
      closeButton.addEventListener("click", toggleModal); 
      cancel.addEventListener("click", toggleModal); 
      window.addEventListener("click", windowOnClick); 
</script>


	</body>
</html>