-- DatabaseConfig가 orders_db 연결에 실행하는 주문 테이블 초기화 SQL입니다.
CREATE TABLE orders (
    -- INSERT에 ID를 지정하지 않아도 DB가 자동 생성하는 기본 키입니다.
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- 상품 DB를 참조하지 않고 품목명을 문자열로 저장합니다. JSON에서는 itemName입니다.
    item_name VARCHAR(100) NOT NULL,
    -- DB에 직접 입력하는 경우에도 주문 수량은 1 이상이어야 합니다.
    quantity INTEGER NOT NULL CHECK (quantity >= 1)
);

-- 서버를 처음 실행해도 GET으로 확인할 수 있도록 예제 행 한 개를 넣습니다.
INSERT INTO orders (item_name, quantity) VALUES ('키보드', 2);
