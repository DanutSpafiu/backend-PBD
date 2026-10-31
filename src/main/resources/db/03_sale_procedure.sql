CREATE OR REPLACE PROCEDURE sell_product (
    p_cnp IN clienti.cnp%TYPE,
    p_product IN achizitii.produs%TYPE,
    p_date IN achizitii.data_achizitie%TYPE,
    p_price IN achizitii.pret%TYPE,
    p_collected IN achizitii.suma_incasata%TYPE
) AS
    v_client_id clienti.id%TYPE;
BEGIN
    IF p_cnp IS NULL OR NOT REGEXP_LIKE(p_cnp, '^[0-9]{13}$') THEN
        RAISE_APPLICATION_ERROR(-20004, 'CNP must contain 13 digits');
    END IF;
    IF TRIM(p_product) IS NULL OR LENGTH(p_product) > 255 OR p_date IS NULL THEN
        RAISE_APPLICATION_ERROR(-20005, 'Product and acquisition date are required');
    END IF;
    IF p_price IS NULL OR p_price <= 0 OR p_price <> ROUND(p_price, 2)
       OR p_collected IS NULL OR p_collected < 0 OR p_collected <> ROUND(p_collected, 2) THEN
        RAISE_APPLICATION_ERROR(-20006, 'Invalid price or collected amount');
    END IF;
    SELECT id INTO v_client_id FROM clienti WHERE cnp = p_cnp;
    INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
    VALUES (v_client_id, p_product, TRUNC(p_date), p_price, p_collected);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20003, 'Client not found');
END;
/
