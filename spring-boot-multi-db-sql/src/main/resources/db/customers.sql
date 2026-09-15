CREATE TABLE customers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL
);

INSERT INTO customers (name, email) VALUES ('홍길동', 'hong@example.com');
