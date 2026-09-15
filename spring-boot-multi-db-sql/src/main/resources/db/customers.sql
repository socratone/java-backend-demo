-- DatabaseConfig가 customers_db 연결에 실행하는 고객 테이블 초기화 SQL입니다.
CREATE TABLE customers (
    -- INSERT에 ID를 지정하지 않아도 DB가 자동 생성하는 기본 키입니다.
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    -- 이메일은 최대 254자로 저장합니다. 이메일 형식 검사는 요청 객체에서 수행합니다.
    email VARCHAR(254) NOT NULL
);

-- 서버를 처음 실행해도 GET으로 확인할 수 있도록 예제 행 한 개를 넣습니다.
INSERT INTO customers (name, email) VALUES ('홍길동', 'hong@example.com');
