const jwtToken = window.localStorage.getItem("com.ryulth.auction.account");
const baseUrl = "http://localhost:8080";
$(document).ready(function () {
    if (jwtToken == null) {
        let popUrl = `naverlogin.html`;
        window.open(popUrl, 'naverloginpop', 'titlebar=1, resizable=1, scrollbars=yes, width=600, height=550');
    }
    $("#enroll-submit").on("click", enrollProduct);
    $(".lower-price-input").on('keyup', numberFormatEvent);
    $(".upper-price-input").on('keyup', numberFormatEvent);
    $auctionTypeInput = $(".auction-type");
    
    $(".basic-auction-button").on("click" ,function(e){
        $auctionTypeInput.val("일반 경매")
        auctionType= "basic";
    })
    $(".live-auction-button").on("click" ,function(e){
        $auctionTypeInput.val("실시간 경매")
        auctionType= "live";
    })
    
});

function enrollProduct() {
    let productName = $(".title-input").val();
    let productSpec = $(".spec-input").val();
    let lowerLimit = $(".lower-price-input").val();
    let upperLimit = $(".upper-price-input").val();
    let sendUrl = `${baseUrl}/product`;
    let reqBody = {
        "name": productName,
        "spec": productSpec,
        "lowerLimit": uncomma(lowerLimit),
        "upperLimit": uncomma(upperLimit)
    };

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
            enrollAuction(response.product.id);
            console.log(response.product.id);
        }
    });
}

function enrollAuction(productId) {
    let sendUrl = `${baseUrl}/auction`;
    let goingTime = $(".going-input").val();
    let reqBody = {
        "auctionType": auctionType,
        "productId": productId,
        "goingTime" : goingTime

    };
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
            let auctionId = response.id;
            window.open(`http://127.0.0.1:5500/bid-page.html?${auctionId}`, "_blank");
            window.location.reload();
        }
    });
}

function numberFormatEvent() {
    var _this = this;
    numberFormat(_this)
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