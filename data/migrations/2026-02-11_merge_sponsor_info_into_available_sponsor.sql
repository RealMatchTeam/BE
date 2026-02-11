ALTER TABLE brand_available_sponsor
    ADD COLUMN shipping_type VARCHAR(50) NULL;

ALTER TABLE brand_sponsor_item
    ADD COLUMN sponsor_id BIGINT NULL;

UPDATE brand_available_sponsor bas
JOIN brand_sponsor_info bsi ON bas.id = bsi.sponsor_id
SET bas.shipping_type = bsi.shipping_type;

UPDATE brand_sponsor_item bsi_item
JOIN brand_sponsor_info bsi ON bsi_item.sponsor_info_id = bsi.id
SET bsi_item.sponsor_id = bsi.sponsor_id;

ALTER TABLE brand_sponsor_item
    DROP FOREIGN KEY FKet9dtmuqr2ba3moigtsxiabq2,
    DROP COLUMN sponsor_info_id,
    MODIFY sponsor_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_brand_sponsor_item_sponsor
        FOREIGN KEY (sponsor_id) REFERENCES brand_available_sponsor(id);

DROP TABLE brand_sponsor_info;
