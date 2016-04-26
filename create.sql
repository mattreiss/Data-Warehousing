USE grocerydb;

DROP VIEW IF EXISTS base_cube;

CREATE VIEW base_cube AS
SELECT s.store_state, p.category, SUM(dollar_sales) dollar_sales
FROM `Sales Fact` sf, Product p, Store s
WHERE sf.product_key = p.product_key AND sf.store_key = s.store_key
GROUP BY s.sales_region, p.category;

DROP VIEW IF EXISTS all_cube;

CREATE VIEW all_cube AS
SELECT s.sales_region, s.store_state, s.city, p.category, p.subcategory, p.brand, t.year, t.month, pr.display_type, dollar_sales, unit_sales, dollar_cost, customer_count
FROM `Sales Fact` sf, Product p, Store s, Time t, Promotion pr
WHERE sf.product_key = p.product_key AND sf.store_key = s.store_key AND sf.time_key = t.time_key AND sf.promotion_key = pr.promotion_key;
