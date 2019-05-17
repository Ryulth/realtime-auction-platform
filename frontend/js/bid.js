const jwtToken = window.localStorage.getItem("com.ryulth.auction.account");
const baseUrl = "http://localhost:8080";
const auctionId = location.href.substr(location.href.lastIndexOf('?') + 1)
let userId;
let auctionType;
let clientVersion = 0;
let stompClient;
let maxCount;
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
    setAuctionStatus(auctionEvents);
    let lastIndex = (auctionEvents[auctionEvents.length - 1].auctionEventType == "CLOSE") ? auctionEvents.length - 2 : auctionEvents.length - 1;
    if (lastIndex !== -1) {
        $(".current-price")[0].innerText = comma(getCurrentPrice(auctionEvents, lastIndex));
    }
    setBiddingPrice();
    setBiddingTable(auctionEvents);
    $(".detail-enroll-person-data")[0].innerText = auctionEvents[lastIndex].nickName + " (" + auctionEvents[lastIndex].eventTime + ")";
    clientVersion = auctionEvents[auctionEvents.length - 1].version;
    if (auctionEvents.length === 1 & userId === auctionEvents[auctionEvents.length - 1].userId) {
        showBidResult(true);
    } else if (userId !== auctionEvents[auctionEvents.length - 1].userId) {
        showBidResult(false);
    } else {
        showBidResult(false);
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
            console.log(response);
            userId = response.userId;
            auctionType = response.auction.auctionType;
            setAuctionType();
            if(auctionType === "firstcome"){
                $(".current-price")[0].innerText = response.product.count;
                maxCount = response.product.count;
            }
            let auctionEvents = response.auctionEvents;
            let lastIndex = (auctionEvents[auctionEvents.length - 1].auctionEventType === "CLOSE") ? auctionEvents.length - 2 : auctionEvents.length - 1;
            clientVersion = auctionEvents[auctionEvents.length - 1].version;
            setAuctionStatus(auctionEvents);
            $(".detail-title")[0].innerText = response.product.name;
            $(".start-time")[0].innerText = (new Date(response.auction.startTime)).format('yyyy-MM-dd(KS) HH:mm:ss') + " ~ ";
            $(".end-time")[0].innerText = (new Date(response.auction.endTime)).format('yyyy-MM-dd(KS) HH:mm:ss');

            if (lastIndex !== -1) {
                $(".current-price")[0].innerText = comma(getCurrentPrice(response.auctionEvents, lastIndex));
            }
            setBiddingPrice();
            $(".detail-enroll-person-data")[0].innerText = 
                `${auctionEvents[lastIndex].nickName} ( ${(new Date(auctionEvents[lastIndex].eventTime)).format('yyyy-MM-dd(KS) HH:mm:ss')} )`;
            $(".detail-description")[0].innerText = response.product.spec;
            setBiddingTable(auctionEvents);
        },
        error: function (response) {
            alert("error");
        }
    });
}

function getCurrentPrice(auctionEvents, lastIndex ) {
    if (auctionType == "basic") {
        return auctionEvents[lastIndex].price;
    } else if (auctionType == "live") {
        if (auctionEvents.length === 1) {
            return parseInt(uncomma($(".current-price")[0].innerText)) + auctionEvents[0].price;
        }
        let sum = 0;
        auctionEvents.forEach(function (item, idex, array) {
            sum += item.price;
        });
        return sum;
    } else if(auctionType === "firstcome"){
        if(auctionEvents.length == 1 && auctionEvents[0].auctionEventType == "BID"){
            return parseInt(uncomma($(".current-price")[0].innerText)) -1;
        }
        let cnt = maxCount;
        auctionEvents.forEach(function (item, idex, array) {
            if(item.auctionEventType === "BID"){
                cnt --;
            }
        });
        return cnt;
    }
}

function setAuctionType() {
    if (auctionType == "basic") {
        $(".auction-type")[0].innerText = "일반 경매";
    } else if(auctionType == "live"){
        $(".auction-type")[0].innerText = "실시간 경매";
        $(".price-input").prop("readonly", true);
    } else if(auctionType == "firstcome"){
        $(".auction-type")[0].innerText = "선착순 경매";
        $(".current-price-text")[0].innerText = "현재수량: "
        $(".price-input").prop("readonly", true);
    }

}

function setBiddingPrice() {
    if (auctionType === "live") {
        let biddingPrice = parseInt(parseInt(uncomma($(".current-price")[0].innerText)) / 10);
        $(".price-input").val(biddingPrice)
    }
}

function setAuctionStatus(auctionEvents) {
    if (auctionEvents[auctionEvents.length - 1].auctionEventType === "CLOSE") {
        $(".auction-status").removeClass("status-ongoing");
        $(".auction-status").addClass("status-close");
        $(".auction-status")[0].innerText = "경매 종료";
    }
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

function setBiddingTable(auctionEvents) {
    if (auctionEvents.length !== 1) {
        $("#bid-history-body").empty();
    }
    if (auctionEvents.length === 1 && auctionEvents[0].auctionEventType === "ENROLL") {
        $("#bid-history-body").empty();
    }
    auctionEvents.forEach(function (item, idex, array) {
        if (item.auctionEventType !== "CLOSE" && item.auctionEventType !== "ENROLL") {
            let itemPrice = (auctionType === "live") ? "+ " +comma(item.price) : comma(item.price);
            let newRow = `<tr>     
            <td>${item.nickName}</td>
            <td>${(new Date(item.eventTime)).format('yyyy-MM-dd(KS) HH:mm:ss')}</td>
            <td>${itemPrice}</td>
            </tr>`;
            $("#bid-history-body").prepend(newRow);
        }
    });

}