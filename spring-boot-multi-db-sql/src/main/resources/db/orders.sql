CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1)
);

INSERT INTO orders (item_name, quantity) VALUES ('키보드', 2);
