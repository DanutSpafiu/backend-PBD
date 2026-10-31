INSERT INTO clienti(nume, prenume, cnp, adresa, telefon, disponibil)
VALUES ('Ion', 'Adi', '1234567890123', 'Timis', '123456789', 0);
INSERT INTO clienti(nume, prenume, cnp, adresa, telefon, disponibil)
VALUES ('Achim', 'Gheorghe', '1234567890124', 'Resita', '234567890', 0);
INSERT INTO clienti(nume, prenume, cnp, adresa, telefon, disponibil)
VALUES ('Dima', 'Alex', '1234567890125', 'Arad', '326774434', 0);
INSERT INTO clienti(nume, prenume, cnp, adresa, telefon, disponibil)
VALUES ('Duma', 'Mihai', '1234567890121', 'Deva', '576325767', 0);

INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
SELECT id, 'Indesit', DATE '2007-01-10', 66, 66 FROM clienti WHERE cnp = '1234567890123';
INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
SELECT id, 'Siemens', DATE '2003-02-01', 100, 100 FROM clienti WHERE cnp = '1234567890124';
INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
SELECT id, 'Bosch', DATE '2005-12-07', 120, 20 FROM clienti WHERE cnp = '1234567890125';
INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
SELECT id, 'LG', DATE '2005-12-11', 55, 37 FROM clienti WHERE cnp = '1234567890121';
INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata)
SELECT id, 'Sony', DATE '2006-05-14', 7.99, 1.99 FROM clienti WHERE cnp = '1234567890123';
COMMIT;
