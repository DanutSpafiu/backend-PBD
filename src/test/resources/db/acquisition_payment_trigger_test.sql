-- Run after installing calculate_acquisition_payment.sql in the same schema.
-- All test data changes are rolled back; identity sequence gaps can remain.
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
DECLARE
    v_client_id clienti.id%TYPE;
    v_id achizitii.id%TYPE;
    v_paid achizitii.suma_incasata%TYPE;
    v_available clienti.disponibil%TYPE;

    PROCEDURE check_payment(p_available NUMBER, p_collected NUMBER, p_price NUMBER,
                            p_expected_paid NUMBER, p_expected_available NUMBER) IS
    BEGIN
        UPDATE clienti SET disponibil = p_available WHERE id = v_client_id;
        INSERT INTO achizitii (client_id, produs, data_achizitie, pret, suma_incasata)
        VALUES (v_client_id, 'TRIGGER-TEST', DATE '2026-10-28', p_price, p_collected)
        RETURNING id INTO v_id;

        SELECT suma_incasata INTO v_paid FROM achizitii WHERE id = v_id;
        SELECT disponibil INTO v_available FROM clienti WHERE id = v_client_id;
        IF v_paid <> p_expected_paid OR v_available <> p_expected_available THEN
            RAISE_APPLICATION_ERROR(-20010, 'Incorrect payment or client credit');
        END IF;
    END;
BEGIN
    SAVEPOINT trigger_tests;
    SELECT MIN(id) INTO v_client_id FROM clienti;
    IF v_client_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20011, 'Tests require an existing client');
    END IF;
    check_payment(30, 50, 100, 80, 0);
    check_payment(100, 0, 80, 80, 20);
    check_payment(30, 100, 100, 100, 30);
    check_payment(30, 70, 100, 100, 0);
    check_payment(0, 0, 100, 0, 0);
    ROLLBACK TO trigger_tests;
    DBMS_OUTPUT.PUT_LINE('PASS: 5 trigger cases; test changes rolled back');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO trigger_tests;
        RAISE;
END;
/
