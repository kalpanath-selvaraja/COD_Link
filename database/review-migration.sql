-- Apply once to an existing CODLink MySQL 8 database, with the application stopped.
-- Back up first and run preflight.sql. No existing business values are rewritten.
ALTER TABLE parcels
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN assigned_courier_id BIGINT NULL,
    ADD COLUMN delivered_by_id BIGINT NULL,
    ADD CONSTRAINT fk_parcel_assigned_courier FOREIGN KEY (assigned_courier_id) REFERENCES courier (courier_id),
    ADD CONSTRAINT fk_parcel_delivered_by FOREIGN KEY (delivered_by_id) REFERENCES courier (courier_id);

ALTER TABLE cod_collections
    ADD COLUMN collection_branch_id BIGINT NULL,
    ADD CONSTRAINT fk_collection_branch FOREIGN KEY (collection_branch_id) REFERENCES branches (branch_id);

ALTER TABLE payments
    ADD COLUMN request_reference VARCHAR(100) NULL,
    ADD COLUMN collection_id BIGINT NULL,
    ADD CONSTRAINT uq_payment_reference UNIQUE (request_reference),
    ADD CONSTRAINT uq_payment_collection UNIQUE (collection_id),
    ADD CONSTRAINT fk_payment_collection FOREIGN KEY (collection_id) REFERENCES cod_collections (collection_id);
