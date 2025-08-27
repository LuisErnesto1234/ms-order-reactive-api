CREATE TABLE categories(
                           id_category BIGSERIAL NOT NULL PRIMARY KEY,
                           name_category VARCHAR(200)
);

CREATE TABLE products(
                         id_product BIGSERIAL NOT NULL PRIMARY KEY,
                         name_product VARCHAR(200) NOT NULL,
                         description_producto TEXT NOT NULL,
                         price NUMERIC(10, 2) NOT NULL DEFAULT 0,
                         stock INTEGER NOT NULL,
                         id_category BIGINT NOT NULL,
                         FOREIGN KEY (id_category) REFERENCES categories(id_category)
);

CREATE TABLE orders (
                        id_order BIGSERIAL PRIMARY KEY,
                        order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                        subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
                        igv NUMERIC(12,2) NOT NULL DEFAULT 0,
                        total NUMERIC(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
                             id_order_item BIGSERIAL PRIMARY KEY,
                             id_order BIGINT NOT NULL
                                 REFERENCES orders(id_order) ON DELETE CASCADE,
                             id_product BIGINT NOT NULL
                                 REFERENCES products(id_product),

                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price NUMERIC(12,2) NOT NULL,
                             subtotal NUMERIC(12,2)
);