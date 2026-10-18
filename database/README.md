# Review database upgrade

These scripts target MySQL 8 and the entity schema on the original main branch.
Run them during a maintenance window with the application stopped. They are manual
one-time scripts, not automatic startup migrations.

1. Back up the database and confirm it can be restored.
2. Run `preflight.sql` in the selected database and retain the results.
3. If any new migration columns already exist, inspect the schema before continuing.
   Do not rerun the ALTER statements blindly. MySQL DDL commits independently, so a
   failed script may have applied earlier statements.
4. Resolve duplicate invoices/collections and invalid pricing through a reviewed
   data correction process. The scripts do not delete or rewrite those records.
5. Run `review-migration.sql` once, then verify the new columns, foreign keys and
   unique constraints. Existing parcel versions start at zero; other new fields
   are nullable to preserve historical records.
6. Start Review with `spring.jpa.hibernate.ddl-auto=validate` and check startup logs.
7. Confirm a test booking, claim, delivery, receipt and reconciliation in a copy of
   the upgraded database before returning the live system to service.

## Historical data

Old payments may have been generated automatically without money being received.
The migration leaves their `request_reference` and `collection_id` null. New
receipts and shipment resolution are blocked for invoices containing those legacy
payments until an administrator checks them against actual receipt evidence.
Do not mass-fill references or treat old PREPAID/COD methods as proof of payment.
After review, use a unique reference for each verified receipt and link verified
COD shipping receipts to their actual collections. Incorrect amounts need an
explicit, backed-up accounting correction, not a blind migration update.

Old unbatched COD collections have no branch snapshot. Reconciliation pauses while
any such row lacks `collection_branch_id`. Establish the original collection
branch from records before setting it. A courier's current branch is insufficient
evidence. Existing reconciled batches are preserved.

Existing parcels are unassigned after migration. Unresolved parcels can be claimed
or assigned normally. For previously delivered parcels awaiting collection,
review the delivery-attempt records and set the actual delivering and assigned
courier together before recording cash. Never invent a delivery identity.

## Rollback

Stop the application. Before any new Review business activity, the previous app
can use the old fields, but use the backup for a verified rollback. After new
partial receipts, assignments or collections exist, restore only through a planned
accounting/data reconciliation. Do not drop the added columns or run old automatic
payment code over new receipts.

## Validation performed

The additive migration was run against a disposable MySQL 8.4 database containing
historical prepaid/COD payments, delivered/returned parcels, invoices and an
unbatched collection. Original business values were compared before and after and
were unchanged. The application's MySQL concurrency tests use a separate database.
The user's existing database was not accessed or changed.
