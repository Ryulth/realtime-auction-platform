# REALTIME AUCTION

기술 스택

* backend - java(springboot)
* frontend - vanilla javascript
* db - mySQL

* deploy - docker
* etc - Websocket , EventSourcing Pattern 

## 시나리오

* 인터넷 경매(비딩 경매, BIDDING)

마감 기간만 정해져 있고 한번의 입찰마다 제한 시간은 존재 하지 않는다. 

결국 기간 전까지 제일 높은 가격을 부르는 사람에게 가는 형식.

동시성 처리는 버전 충돌이 날시 가격만 비교하면 되는 방식.



* 경쟁 경매 (COMPETE)

일반적인 오프라인 경매 방식

사회자가 존재 (프로그램) 한다. 

턴 이 존재하며 한 턴당 time limit 가 존재한다. 

각 턴에 가장 빨리 입찰 한 사람에게 입찰 기회를 부여한다.

입찰 가는 버튼으로 존재하며 만약 custom 입찰 시 금액이 크면 입찰을 시켜준다.

