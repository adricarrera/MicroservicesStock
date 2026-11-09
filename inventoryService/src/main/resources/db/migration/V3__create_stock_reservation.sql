CREATE TABLE stock_reservation (
                         id uuid PRIMARY KEY,
                         order_id uuid NOT NULL UNIQUE,
                         status varchar(255) NOT NULL CHECK (status IN ('RESERVED', 'REJECTED')),
                         created_at timestamp(6) NOT NULL
);