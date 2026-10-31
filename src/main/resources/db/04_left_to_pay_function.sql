CREATE OR REPLACE FUNCTION left_to_pay (
    p_cnp IN clienti.cnp%TYPE,
    p_product IN achizitii.produs%TYPE
) RETURN NUMBER AS
    v_client_id clienti.id%TYPE;
    v_count NUMBER;
    v_remaining NUMBER;
BEGIN
    IF p_cnp IS NULL OR TRIM(p_product) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20004, 'CNP and product are required');
    END IF;
    SELECT id INTO v_client_id FROM clienti WHERE cnp = p_cnp;
    SELECT COUNT(*), SUM(pret - suma_incasata) INTO v_count, v_remaining
    FROM achizitii WHERE client_id = v_client_id AND produs = p_product;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Product not purchased by client');
    END IF;
    RETURN v_remaining;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20003, 'Client not found');
END;
