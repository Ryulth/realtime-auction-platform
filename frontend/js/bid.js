let auctionType;
const auctionId = location.href.substr(location.href.lastIndexOf('?') + 1)
$(document).ready(function () {
    getAuction();
    $("#price-submit").bind("click", bidAction);
});

function bidAction(){
    alert("클릭 이벤트 발생");
}
function showBidResult(success){
    if(success){
        $("#detail-bid-success").addClass("bid-status")
        $("#detail-bid-fail").removeClass("bid-status")
    }
    else{
        $("#detail-bid-success").removeClass("bid-status")
        $("#detail-bid-fail").addClass("bid-status")
    }
}

function getAuction(){
    responseBody = {
        "auction" : {
            "id" : 1,
            "productId" : 1,
            "price" : 1000,
            "version" : 1,
            "onAuction" : 1,
            "auctionType" : "basic",
            "startTime" : "1994-04-03 12:11",
	        "endTime" : "1994-04-03 12:11"
        },
        "product" : {
            "id" : 1,
            "name" : "아이언맨 마스크",
            "spec" : "한정판",
            "upperLimit" : 10000,
            "lowerLimit" : 100,
            "onSale" : 1,
            "startTime" : "1994-04-03 12:11",
            "endTime" : "1994-04-03 12:11"
        },
        "auctionEvents" : [
            { 
                "userId" : "꼰지",
                "biddingTime" : "1994-04-03 12:11:12",
                "version" : 1,
                "price" : 20000
            }
        ]
    }
    //const responseBody = JSON.parse(response);
    $(".detail-title")[0].innerText = responseBody.product.name;
    $(".start-time")[0].innerText = responseBody.auction.startTime + " ~ ";
    $(".end-time")[0].innerText = responseBody.auction.endTime;
    $(".current-price")[0].innerText = responseBody.auctionEvents[0].price;
    $(".detail-enroll-person-data")[0].innerText = responseBody.auctionEvents[0].userId+ " (" + responseBody.auctionEvents[0].biddingTime + ")";
    $(".detail-description")[0].innerText = responseBody.product.spec;
    console.log(responseBody.auction)

    
}