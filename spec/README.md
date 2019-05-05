# REALTIME AUCTION

기술 스택

* backend - java(springboot)
* frontend - vanilla javascript
* db - mySQL

* deploy - docker
* etc - Websocket , EventSourcing Pattern 

## 시나리오

* 일반경매 (일반적인 자선경매 같은 방식)

사회자가 존재 (프로그램) 한다. 

턴 이 존재하며 한 턴당 time limit 가 존재한다. 

각 턴에 가장 빨리 입찰 한 사람에게 입찰 기회를 부여한다.

입찰 가는 버튼으로 존재하며 만약 custom 입찰 시 금액이 크면 입찰을 시켜준다.

