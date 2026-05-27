-- Cleanup duplicate lots: keep the one with the most recent state, remove the other
-- Run this if you have duplicate (proveedor_nif, id_lot) rows in lots_proveidor

-- 1. Check for duplicates
SELECT proveedor_nif, id_lot, COUNT(*), GROUP_CONCAT(estat)
FROM lots_proveidor
GROUP BY proveedor_nif, id_lot
HAVING COUNT(*) > 1;

-- 2. Delete duplicates keeping the first one (you may need to adjust which one to keep)
-- This keeps the first row and deletes the rest
DELETE t1 FROM lots_proveidor t1
INNER JOIN lots_proveidor t2
WHERE 
    t1.proveedor_nif = t2.proveedor_nif AND
    t1.id_lot = t2.id_lot AND
    t1.id < t2.id;

-- 3. Add unique constraint explicitly (JPA should create it, but just in case)
ALTER TABLE lots_proveidor ADD CONSTRAINT uk_lot_proveedor UNIQUE (proveedor_nif, id_lot);
