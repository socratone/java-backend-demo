-- DatabaseConfig가 products_db 연결에 실행하는 상품 테이블 초기화 SQL입니다.
CREATE TABLE products (
    -- INSERT에 ID를 지정하지 않아도 DB가 자동 생성하는 기본 키입니다.
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    -- 전체 12자리 중 소수부 2자리를 사용하며, 가격과 재고의 음수를 DB에서도 차단합니다.
    price DECIMAL(12, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
);

-- 서버를 처음 실행해도 GET으로 확인할 수 있도록 예제 행 한 개를 넣습니다.
INSERT INTO products (name, price, stock) VALUES ('키보드', 59000, 10);
