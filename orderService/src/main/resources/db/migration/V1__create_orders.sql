CREATE TABLE orders (
                        id         uuid                        PRIMARY KEY,
                        created_at timestamp(6)                NOT NULL,
                        status     varchar(255)                NOT NULL,
                        CONSTRAINT orders_status_check
                            CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED'))
);

CREATE TABLE order_line (
                            id         uuid    PRIMARY KEY,
                            product_id uuid    NOT NULL,
                            quantity   integer NOT NULL,
                            order_id   uuid    NOT NULL,
                            CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders (id)
);