const jwtToken = window.localStorage.getItem("com.ryulth.auction.account");
const baseUrl = "http://localhost:8080";
const auctionId = location.href.substr(location.href.lastIndexOf('?') + 1)
let userId;
let auctionType;
let clientVersion = 0;
let stompClient;
getAuction();

$(document).ready(function () {
    if (jwtToken == null) {
        let popUrl = `naverlogin.html`;
        window.open(popUrl, 'naverloginpop', 'titlebar=1, resizable=1, scrollbars=yes, width=600, height=550');
    } else {
        connect();
        $("#price-submit").on("click", bidAction);
        $(".price-input").on('keyup', function (e) {
            var _this = this;
            numberFormat(_this)
            if (event.keyCode === 13) {
                event.preventDefault();
                $("#price-submit").click();
            }
        })
    }
});

function connect() {
    let socket = new SockJS(`${baseUrl}/auction-websocket`);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log("connect");
        stompClient.subscribe(`/topic/auctions/${auctionId}/event`, function (response) {
            console.log("wevsocket")
            let responseBody = JSON.parse(response.body);
            console.log(responseBody);
            receiveAuctionEvent(responseBody.auctionEvents);
        });
    }, function (message) {
        if (!stompClient.connected) {
            console.log(message);
        }
    });
}

function receiveAuctionEvent(auctionEvents) {
    $(".current-price")[0].innerText = comma(getCurrentPrice(auctionEvents));
    $(".detail-enroll-person-data")[0].innerText = auctionEvents[auctionEvents.length - 1].nickName + " (" + auctionEvents[auctionEvents.length - 1].eventTime + ")";
    clientVersion = auctionEvents[auctionEvents.length - 1].version;
    if (auctionEvents.length === 1 & userId === auctionEvents[auctionEvents.length - 1].userId) {
        showBidResult(true);
    } else if (userId !== auctionEvents[auctionEvents.length - 1].userId) {

    } else {
        showBidResult(false);
    }
}
function getCurrentPrice(auctionEvents) {
    if (auctionType == "basic") {
        return auctionEvents[auctionEvents.length - 1].price;
    } else if (auctionType == "live") {
        if(auctionEvents.length===1){
            return parseInt(uncomma($(".current-price")[0].innerText)) + auctionEvents[0].price;
        }
        let sum = 0 ;
        auctionEvents.forEach(function (item, idex, array) {
            sum+=item.price;
        });
        return sum;
    } else {
        return -1;
    }
}

function bidAction() {
    $inputPrice = $(".price-input");
    let inputPriceValue = uncomma($inputPrice.val());
    let sendUrl = `${baseUrl}/auctions/${auctionId}/event`;
    let reqBody = {
        "auctionEventType": "bid",
        "price": inputPriceValue,
        "version": clientVersion
    }
    $.ajax({
        async: true, // false 일 경우 동기 요청으로 변경
        type: "POST",
        contentType: "application/json",
        url: sendUrl,
        beforeSend: function (request) {
            request.setRequestHeader("Authorization", jwtToken);
        },
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
        beforeSend: function (request) {
            request.setRequestHeader("Authorization", jwtToken);
        },
        success: function (response) {
            console.log(response)
            userId = response.userId;
            console.log(userId);
            auctionType = response.auction.auctionType;
            let auctionEvents = response.auctionEvents;
            clientVersion = auctionEvents[auctionEvents.length-1].version;
            $(".detail-title")[0].innerText = response.product.name;
            $(".start-time")[0].innerText = (new Date(response.auction.startTime)).format('yyyy-MM-dd(KS) HH:mm:ss') + " ~ ";
            $(".end-time")[0].innerText = (new Date(response.auction.endTime)).format('yyyy-MM-dd(KS) HH:mm:ss');
            $(".current-price")[0].innerText = comma(getCurrentPrice(response.auctionEvents));
            $(".detail-enroll-person-data")[0].innerText = auctionEvents[auctionEvents.length - 1].nickName + " (" + auctionEvents[auctionEvents.length - 1].eventTime + ")";
            $(".detail-description")[0].innerText = response.product.spec;
        },
        error: function (response) {
            alert("error");
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

function uncomma(str) {
    str = String(str);
    return str.replace(/[^\d]+/g, '');
}

function numberFormat(obj) {
    obj.value = comma(uncomma(obj.value));
}