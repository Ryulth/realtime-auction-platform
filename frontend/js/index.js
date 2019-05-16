const baseUrl = "http://localhost:8080";
getAuctions();

function getAuctions() {
    let sendUrl = `${baseUrl}/auctions`
    $.ajax({
        type: "GET",
        cache: false,
        url: sendUrl,
        success: function (response) {
            makeList(response.auctions);
        },
        error: function (response) {
            alert("error");
        }
    });
}

function makeList(auctions) {
    $auctionList = $("#product-list");
    console.log($auctionList)
    auctions.forEach(function (item, idex, array) {
        let data = `
        <li>
            <a href=http://127.0.0.1:5500/bid-page.html?${item.auctionId}>
                <div class="product">
                    <div class="image-section">
                        <img src="./images/product_1.jpg" class="product-img">
                        <span class="bottom-right"><img src="./images/like.png" class="like-button"></span>
                    </div>
                    <div class="content-section">
                        <div class="product-text">
                            <div class="product-name">
                                <div class="word-break">${item.product.name}</div>
                            </div>
                            <div class="product-owner">등록자: ${item.nickName}</div>
                        </div>
                        <div class="product-price">
                            <div class="now-participate"><span class="bottom-right">시작 가격</span></div>
                            <span class="now-price">${item.product.lowerLimit}<span style="font-size:16px">원</span></span>
                        </div>
                    </div>
                </div>
            </a>
        </li>
        `
        $auctionList.append(data);
    });
}