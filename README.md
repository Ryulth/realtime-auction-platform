## 주제 : 6. 쇼핑 실시간 상품 경매 플랫폼
우수참가자로 선정시 'NAVER'에서 '채용전환형 인턴십'의 참여기회가 주어집니다.

### 주제선정 배경
* 기본적인 Web 기술을 다루는 능력과 문제에 대한 이해와 해결능력을 보기 위해 주제 선정

### 개발 요구사항 (필수)
- 기본적인 웹 구성 (경매 상품 등록 / 경매 상품 조회)
- 사용자가 상품을 구매하는 기능 개발
- 구매에 더 나아가서 경매 기능 개발 (동시성 처리)

### 개발 요구사항(선택)
- 여러 상품 동시 경매 진행
- 항목에 없는 내용을 구현 가능

### 참고사항
- 사용자 인증은 고려하지 않아도 됨

### Technology 
- Git
- jQuery
- React
- Vue
- Spring
- Node 
- NoSQL(MongoDB, Redis 등)
- RDBMS(MySql, oracle 등)
- MQ (Kafka, RabbitMq 등)
- 등 자유롭게 선택 가능

### 개발언어
- Java
- Javascript
- Kotlin
- TypeScript
- 등 자유롭게 선택 가능

### 기타사항
- 오픈소스 라이브러리 사용 가능

## 선택한 기술 스택
- FRONTEND
  - Vanilla Javascript
  - jQuery
- BACKEND
  - Java 8 +
  - Spring Boot V2.0 +
  - JpaRepository
  - MySQL 5.7 + 
  - Redis 5.0 +
- ETC
  - GIT
  - WebSocket
  - EventSourcing Pattern
  - Jwt Token

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
