const baseUrl = "http://localhost:8080";
const auctionId = location.href.substr(location.href.lastIndexOf('?') + 1)
let auctionType;
let clientVersion = 0;
let stompClient;

$(document).ready(function () {
    getAuction();
    connect();
    $("#price-submit").on("click", bidAction);
    $(".price-input").on('keyup', function () {
        var _this = this;
        numberFormat(_this)
    })
});
function connect(){
    let socket = new SockJS(`${baseUrl}/auction-websocket`);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log("connect");
        stompClient.subscribe(`/topic/auctions/${auctionId}/event`, function (content) {
            console.log(response)
        });
    }, function(message) {
        // check message for disconnect
        if(!stompClient.connected){
            console.log(message);
        }
    });
}
function bidAction() {
    $inputPrice = $(".price-input");
    let inputPriceValue = uncomma($inputPrice.val());
    let sendUrl = `${baseUrl}/auctions/${auctionId}/event`;
    let reqBody = {
        "auctionEventType" : "bid",
        "price" : inputPriceValue,
        "version" : clientVersion
    }
    $.ajax({
        async: true, // false 일 경우 동기 요청으로 변경
        type: "POST",
        contentType: "application/json",
        url: sendUrl,
        data: JSON.stringify(reqBody),
        dataType: 'json',
        success: function (response) {
            console.log(response);
        }
    });
    $inputPrice.val("");
}

function getAuction() {
    let sendUrl = `${baseUrl}/auctions/${auctionId}`
    $.ajax({
        type: "GET",
        cache: false,
        url: sendUrl,
        success: function (response) {
            console.log(response)
            $(".detail-title")[0].innerText = response.product.name;
            $(".start-time")[0].innerText = (new Date(response.auction.startTime)).format('yyyy-MM-dd(KS) HH:mm:ss') + " ~ ";
            $(".end-time")[0].innerText = (new Date(response.auction.endTime)).format('yyyy-MM-dd(KS) HH:mm:ss');
            $(".current-price")[0].innerText = response.auctionEvents[0].price;
            //$(".detail-enroll-person-data")[0].innerText = responseBody.auctionEvents[0].userId+ " (" + responseBody.auctionEvents[0].biddingTime + ")";
            $(".detail-description")[0].innerText = response.product.spec;

        }
    });
}

function showBidResult(success) {
    if (success) {
        $("#detail-bid-success").addClass("bid-status")
        $("#detail-bid-fail").removeClass("bid-status")
    } else {
        $("#detail-bid-success").removeClass("bid-status")
        $("#detail-bid-fail").addClass("bid-status")
    }
}

function comma(str) {
    str = String(str);
    return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
}

// 콤마 풀기
function uncomma(str) {
    str = String(str);
    return str.replace(/[^\d]+/g, '');
}

function numberFormat(obj) {
    obj.value = comma(uncomma(obj.value));
}