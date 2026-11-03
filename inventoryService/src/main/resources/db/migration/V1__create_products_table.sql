CREATE TABLE product (
                        id uuid PRIMARY KEY,
                        name varchar(255) NOT NULL,
                        available_quantity integer NOT NULL,
                        CONSTRAINT product_available_quantity_check CHECK (available_quantity >= 0)
);
