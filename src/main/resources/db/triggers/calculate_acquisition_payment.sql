CREATE OR REPLACE TRIGGER trg_acquisition_payment
BEFORE INSERT ON achizitii
FOR EACH ROW
DECLARE
    v_available clienti.disponibil%TYPE;
    v_total NUMBER;
BEGIN
    IF :NEW.pret IS NULL OR :NEW.pret <= 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Price must be greater than zero');
    END IF;

    IF :NEW.suma_incasata IS NULL OR :NEW.suma_incasata < 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Collected amount must be non-negative');
    END IF;

    SELECT disponibil INTO v_available
    FROM clienti
    WHERE id = :NEW.client_id
    FOR UPDATE;

    v_total := v_available + :NEW.suma_incasata;
    :NEW.suma_incasata := LEAST(v_total, :NEW.pret);

    UPDATE clienti
    SET disponibil = v_total - :NEW.suma_incasata
    WHERE id = :NEW.client_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20003, 'Client not found');
END;
/
