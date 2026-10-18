-- Read only. Run before the migration and retain the results for review.
SELECT VERSION() AS mysql_version;
SELECT table_name, column_name FROM information_schema.columns
WHERE table_schema = DATABASE() AND column_name IN
('version', 'assigned_courier_id', 'delivered_by_id', 'collection_branch_id', 'request_reference', 'collection_id')
ORDER BY table_name, column_name;

-- Existing payments could have been generated without a receipt. Verify each one.
SELECT p.payment_id, p.invoice_id, p.method, p.amount, p.paid_at, i.amount AS invoice_amount
FROM payments p JOIN invoices i ON i.invoice_id = p.invoice_id ORDER BY p.invoice_id, p.payment_id;
SELECT invoice_id, COUNT(*) AS receipt_count, SUM(amount) AS total FROM payments
GROUP BY invoice_id HAVING COUNT(*) > 1;
SELECT shipment_id, COUNT(*) AS invoice_count FROM invoices GROUP BY shipment_id HAVING COUNT(*) > 1;
SELECT parcel_id, COUNT(*) AS collection_count FROM cod_collections GROUP BY parcel_id HAVING COUNT(*) > 1;

-- A courier's current branch is not proof of the branch that collected historical cash.
SELECT c.collection_id, c.parcel_id, c.courier_id, c.batch_id, c.amount
FROM cod_collections c WHERE c.batch_id IS NULL ORDER BY c.collection_id;
SELECT parcel_id, status, shipping_fee, return_fee FROM parcels
WHERE shipping_fee IS NULL OR shipping_fee < 0 OR return_fee < 0;
SELECT rule_id, zone_id, is_cod, weight_bracket, rate FROM pricing_rules ORDER BY zone_id, is_cod, rule_id;
