# Gradle · Spring Boot · 3개 DB SQL 예제

하나의 Spring Boot 애플리케이션에서 **서로 독립된 H2 메모리 DB 3개**를 연결합니다. JPA 없이 `JdbcTemplate`과 직접 작성한 SQL로 데이터를 조회하고 생성합니다.

## 실행

JDK 21이 필요합니다. Gradle은 설치하지 않아도 되며, 첫 실행 시 Wrapper가 Gradle과 의존성을 다운로드합니다.

```bash
cd spring-boot-multi-db-sql
./gradlew bootRun
```

Windows에서는 `gradlew.bat bootRun`을 사용합니다. 기본 포트는 `8080`입니다.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Swagger UI에서 요청 스키마와 예제를 확인하고 `Try it out`으로 호출할 수 있습니다.

## 구성

- Java 21, Spring Boot 4.1.0
- Gradle 9.5.1 Wrapper, Groovy DSL
- Spring MVC, Jakarta Validation, JDBC, HikariCP, H2
- springdoc-openapi 3.1.1 (Swagger UI)

각 연결은 `application.yml`의 별도 JDBC URL을 사용합니다. `DatabaseConfig`가 `DataSource` 3개와 `JdbcTemplate` 3개를 등록하고, 각 저장소는 생성자의 `@Qualifier`로 사용할 연결을 지정합니다.

| API | SQL 저장소 | JdbcTemplate → DataSource | H2 DB | 초기화 SQL |
| --- | --- | --- | --- | --- |
| `/api/products` | `ProductRepository` | `productsJdbcTemplate` → `productsDataSource` | `products_db` | `db/products.sql` |
| `/api/customers` | `CustomerRepository` | `customersJdbcTemplate` → `customersDataSource` | `customers_db` | `db/customers.sql` |
| `/api/orders` | `OrderRepository` | `ordersJdbcTemplate` → `ordersDataSource` | `orders_db` | `db/orders.sql` |

호출 흐름은 `Controller → Repository → JdbcTemplate → DataSource → 해당 H2 DB`입니다. 기본 연결을 임의로 선택하지 않도록 모든 연결 주입에 `@Qualifier`를 사용합니다.

각 DataSource를 생성할 때 해당 SQL 파일로 테이블과 초기 행 1개를 준비한 뒤 API에 연결합니다. 자동 단일 DB SQL 초기화는 비활성화했습니다. `DB_CLOSE_DELAY=-1`로 유휴 연결 교체 중에도 DB를 유지합니다. 데이터는 JVM 메모리에만 있으므로 **프로세스를 종료하고 재시작하면 초기 데이터로 돌아갑니다**. 상품·고객·주문 사이에 외래 키나 DB 간 조인, 공통 트랜잭션은 없습니다. 주문 생성은 상품 재고를 변경하지 않습니다.

## API 및 입력 규칙

| 메소드 | 경로 | 동작 | 성공 상태 |
| --- | --- | --- | --- |
| GET | `/api/products` | 상품 목록 | 200 |
| POST | `/api/products` | 상품 생성 | 201 |
| GET | `/api/customers` | 고객 목록 | 200 |
| POST | `/api/customers` | 고객 생성 | 201 |
| GET | `/api/orders` | 주문 목록 | 200 |
| POST | `/api/orders` | 주문 생성 | 201 |

- `id`: DB가 자동 생성합니다. POST 입력에 포함하지 않습니다.
- 상품: `name`은 공백이 아닌 문자열(최대 100자), `price`는 0 이상(정수부 최대 10자리, 소수부 최대 2자리), `stock`은 0~2,147,483,647 정수입니다.
- 고객: `name`은 공백이 아닌 문자열(최대 100자), `email`은 이메일 형식(최대 254자)입니다.
- 주문: `itemName`은 공백이 아닌 문자열(최대 100자), `quantity`는 1~2,147,483,647 정수입니다. SQL 컬럼 `item_name`은 JSON의 `itemName`으로 매핑됩니다.
- POST의 모든 필드는 필수입니다. 필수값 누락, 잘못된 형식·범위, 잘못된 JSON은 400으로 반환합니다.
- 입력은 `PreparedStatement`의 `?` 파라미터에 바인딩합니다.

## curl 예제: 6개 API

아래 순서대로 실행하면 각 POST로 생성한 행을 바로 다음 GET에서 확인할 수 있습니다.

### 1. 상품 생성

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"마우스","price":29900,"stock":20}'
```

응답: `201 Created`

```json
{"columns":["id","name","price","stock"],"rows":[{"id":2,"name":"마우스","price":29900.00,"stock":20}]}
```

### 2. 상품 목록

```bash
curl -i http://localhost:8080/api/products
```

응답: `200 OK`

```json
{"columns":["id","name","price","stock"],"rows":[{"id":1,"name":"키보드","price":59000.00,"stock":10},{"id":2,"name":"마우스","price":29900.00,"stock":20}]}
```

### 3. 고객 생성

```bash
curl -i -X POST http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{"name":"김민수","email":"minsu@example.com"}'
```

응답: `201 Created`

```json
{"columns":["id","name","email"],"rows":[{"id":2,"name":"김민수","email":"minsu@example.com"}]}
```

### 4. 고객 목록

```bash
curl -i http://localhost:8080/api/customers
```

응답: `200 OK`

```json
{"columns":["id","name","email"],"rows":[{"id":1,"name":"홍길동","email":"hong@example.com"},{"id":2,"name":"김민수","email":"minsu@example.com"}]}
```

### 5. 주문 생성

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"itemName":"마우스","quantity":2}'
```

응답: `201 Created`

```json
{"columns":["id","itemName","quantity"],"rows":[{"id":2,"itemName":"마우스","quantity":2}]}
```

### 6. 주문 목록

```bash
curl -i http://localhost:8080/api/orders
```

응답: `200 OK`

```json
{"columns":["id","itemName","quantity"],"rows":[{"id":1,"itemName":"키보드","quantity":2},{"id":2,"itemName":"마우스","quantity":2}]}
```

위 ID는 재시작 직후 각 POST를 한 번씩 호출한 경우의 예시입니다. GET은 ID 오름차순이며, POST는 생성한 행 한 개를 같은 테이블 응답 형식으로 반환합니다. `columns`는 행 유무에 관계없이 유지됩니다. 예를 들어 상품이 없는 경우는 다음과 같습니다.

```json
{"columns":["id","name","price","stock"],"rows":[]}
```

입력 오류는 `application/problem+json` 응답으로 반환합니다. 예를 들어 주문 수량이 0이면 다음과 같은 400 응답을 받습니다. 검증 메시지는 실행 환경의 언어에 따라 달라질 수 있습니다.

```json
{
  "type":"about:blank",
  "title":"Bad Request",
  "status":400,
  "detail":"입력값을 확인해 주세요.",
  "instance":"/api/orders",
  "errors":[{"field":"quantity","message":"must be greater than or equal to 1"}]
}
```

## 검증

```bash
./gradlew test
```

실제 H2 연결을 사용하는 Spring Boot + MockMvc 통합 테스트로 다음을 검증합니다.

- 6개 API의 상태 코드와 응답 형식, 초기 데이터
- POST로 생성한 행의 GET 조회 및 다른 두 DB 데이터 유지
- 각 DB의 이름과 보유 테이블 분리
- 빈 목록에서도 `columns` 유지
- 필수값·이메일·숫자 범위·JSON 오류의 400 응답 및 데이터 유지
- 숫자 하한값과 SQL 특수문자 입력 처리
- Swagger UI 리다이렉트·페이지·설정 접근과 OpenAPI의 6개 작업 등록

테스트 보고서: `build/reports/tests/test/index.html`

## 참고 문서

- [Spring Boot Gradle 플러그인](https://docs.spring.io/spring-boot/gradle-plugin/getting-started.html)
- [springdoc-openapi 공식 문서](https://springdoc.org/)
