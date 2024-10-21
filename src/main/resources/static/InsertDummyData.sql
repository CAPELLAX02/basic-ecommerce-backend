-- First, create two users (IDs are going to be automatically 1 and 2), and then run the following script.

DELETE FROM web_order_quantities;
DELETE FROM web_order;
DELETE FROM inventory;
DELETE FROM product;
DELETE FROM address;

INSERT INTO product (name, short_description, long_description, price)
VALUES
    ('Product #1', 'Product one short description.', 'This is a very long description of product #1.', 5.50),
    ('Product #2', 'Product two short description.', 'This is a very long description of product #2.', 10.56),
    ('Product #3', 'Product three short description.', 'This is a very long description of product #3.', 2.74),
    ('Product #4', 'Product four short description.', 'This is a very long description of product #4.', 15.69),
    ('Product #5', 'Product five short description.', 'This is a very long description of product #5.', 42.59);

INSERT INTO inventory (product_id, quantity)
VALUES
    ((SELECT id FROM product WHERE name = 'Product #1'), 5),
    ((SELECT id FROM product WHERE name = 'Product #2'), 8),
    ((SELECT id FROM product WHERE name = 'Product #3'), 12),
    ((SELECT id FROM product WHERE name = 'Product #4'), 73),
    ((SELECT id FROM product WHERE name = 'Product #5'), 2);

INSERT INTO address (address_line_1, city, country, user_id)
VALUES
    ('123 Tester Hill', 'Testerton', 'England', 1),
    ('312 Spring Boot', 'Hibernate', 'England', 2);

INSERT INTO web_order (address_id, user_id)
VALUES
    ((SELECT id FROM address WHERE user_id = 1 ORDER BY id DESC LIMIT 1), 1),
    ((SELECT id FROM address WHERE user_id = 1 ORDER BY id DESC LIMIT 1), 1),
    ((SELECT id FROM address WHERE user_id = 1 ORDER BY id DESC LIMIT 1), 1),
    ((SELECT id FROM address WHERE user_id = 2 ORDER BY id DESC LIMIT 1), 2),
    ((SELECT id FROM address WHERE user_id = 2 ORDER BY id DESC LIMIT 1), 2);

INSERT INTO web_order_quantities (order_id, product_id, quantity)
VALUES
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC LIMIT 1), (SELECT id FROM product WHERE name = 'Product #1'), 5),
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC LIMIT 1), (SELECT id FROM product WHERE name = 'Product #2'), 5),
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC OFFSET 1 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #3'), 5),
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC OFFSET 1 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #2'), 5),
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC OFFSET 1 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #5'), 5),
    ((SELECT id FROM web_order WHERE user_id = 1 ORDER BY id DESC OFFSET 2 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #3'), 5),
    ((SELECT id FROM web_order WHERE user_id = 2 ORDER BY id DESC LIMIT 1), (SELECT id FROM product WHERE name = 'Product #4'), 5),
    ((SELECT id FROM web_order WHERE user_id = 2 ORDER BY id DESC LIMIT 1), (SELECT id FROM product WHERE name = 'Product #2'), 5),
    ((SELECT id FROM web_order WHERE user_id = 2 ORDER BY id DESC OFFSET 1 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #3'), 5),
    ((SELECT id FROM web_order WHERE user_id = 2 ORDER BY id DESC OFFSET 1 LIMIT 1), (SELECT id FROM product WHERE name = 'Product #1'), 5);