## REALTIME AUCTION

### 기술 스택 

- FRONTEND
  - Vanilla Javascript
  - jQuery
  - bootstrap
- BACKEND
  - Java 8 +
  - Spring Boot V2.0 +
  - JpaRepository
  - MySQL 5.7 + 
  - Redis 5.0 +
- ETC
  - GIT
  - WebSocket

### 시나리오

#### [참조링크](<https://namu.wiki/w/eBay#s-5.3>)

* 일반 경매, 비딩 경매 (BasicAuctionService.class)

마감 기간만 정해져 있고 한번의 입찰마다 제한 시간은 존재 하지 않는다. 

결국 기간 전까지 제일 높은 가격을 부르는 사람에게 가는 형식.

동시성 처리는 동시 입찰 시 가격만 비교 해주면 된다.



* 라이브 경매, 경쟁 경매 (LiveAuctionService.class)

일반적인 오프라인 경매 방식

사회자 (프로그램)가 존재 한다. 

각 턴에 가장 빨리 입찰 한 사람에게 입찰 기회를 부여한다.

동시성 처리는 버전 충돌이 날시 선착순에 의거해 처리한다.