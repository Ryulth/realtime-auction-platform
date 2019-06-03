# Backend

#### Basic Spec

* Java 8 (Spring Boot 2+)
* Jpa
* MySql 5.7
* Redis 5.0 +
  * Streams 사용을 위해 5.0 이상 버젼 사용 [spring-data-redis 패키지 Streams](<https://github.com/spring-projects/spring-data-redis/blob/master/src/main/asciidoc/reference/redis-streams.adoc>)
  * Streams의 [XADD](<https://redis.io/commands/xadd>)
* *Deploy 시 (nginx & docker)

#### Technique

* WebSocket
* EventSourcing Pattern
* Jwt Token








