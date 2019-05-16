const jwtToken = window.localStorage.getItem("com.ryulth.auction.account");
const baseUrl = "http://localhost:8080";

$(document).ready(function () {
    // if (jwtToken == null) {
    //     let popUrl = `naverlogin.html`;
    //     window.open(popUrl, 'naverloginpop', 'titlebar=1, resizable=1, scrollbars=yes, width=600, height=550');
    // }
    $("#enroll-submit").on("click", enrollProduct);
    $(".lower-price-input").on('keyup', numberFormatEvent);
    $(".upper-price-input").on('keyup', numberFormatEvent);
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
        "lowerLimit": lowerLimit,
        "upperLimit": upperLimit
    }
    $.ajax({
        async: true, // false 일 경우 동기 요청으로 변경
        type: "POST",
        contentType: "application/json",
        url: sendUrl,
        data: JSON.stringify(reqBody),
        dataType: 'json',
        success: function (response) {
            enrollAuction(response.product.id);
            console.log(response.product.id);
        }
    });
    //console.log(`${productName}/${productSpec}/${lowerLimit}/${upperLimit}/${startTime}/${endTime}`)
}

function enrollAuction(productId) {
    let sendUrl = `${baseUrl}/auction`;
    let startTime = $(".start-input").val().replace("T"," ") + ":00";
    let endTime = $(".end-input").val().replace("T"," ") +":00";
    alert($(".start-input").val().replace("T"," "));
    let reqBody = {
        "auctionType": "basic",
        "productId" : productId,
        "startTime": startTime,
        "endTime": endTime
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
            alert("test")
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