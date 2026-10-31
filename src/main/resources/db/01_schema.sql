CREATE TABLE clienti (
    id NUMBER GENERATED ALWAYS AS IDENTITY,
    nume VARCHAR2(15 CHAR) NOT NULL,
    prenume VARCHAR2(20 CHAR) NOT NULL,
    cnp VARCHAR2(13 CHAR) NOT NULL,
    adresa VARCHAR2(255 CHAR),
    telefon VARCHAR2(9 CHAR) NOT NULL,
    disponibil NUMBER(12,2) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_clienti PRIMARY KEY (id),
    CONSTRAINT uq_clienti_cnp UNIQUE (cnp),
    CONSTRAINT uq_clienti_telefon UNIQUE (telefon),
    CONSTRAINT ck_clienti_nume CHECK (TRIM(nume) IS NOT NULL),
    CONSTRAINT ck_clienti_prenume CHECK (TRIM(prenume) IS NOT NULL),
    CONSTRAINT ck_clienti_cnp CHECK (REGEXP_LIKE(cnp, '^[0-9]{13}$')),
    CONSTRAINT ck_clienti_telefon CHECK (REGEXP_LIKE(telefon, '^[0-9]{9}$')),
    CONSTRAINT ck_clienti_disponibil CHECK (disponibil >= 0)
);

CREATE TABLE achizitii (
    id NUMBER GENERATED ALWAYS AS IDENTITY,
    client_id NUMBER NOT NULL,
    produs VARCHAR2(255 CHAR) NOT NULL,
    data_achizitie DATE NOT NULL,
    pret NUMBER(12,2) NOT NULL,
    suma_incasata NUMBER(12,2) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_achizitii PRIMARY KEY (id),
    CONSTRAINT fk_achizitii_clienti FOREIGN KEY (client_id) REFERENCES clienti(id),
    CONSTRAINT ck_achizitii_produs CHECK (TRIM(produs) IS NOT NULL),
    CONSTRAINT ck_achizitii_pret CHECK (pret > 0),
    CONSTRAINT ck_achizitii_incasata CHECK (suma_incasata BETWEEN 0 AND pret)
);

CREATE INDEX ix_achizitii_client_data ON achizitii(client_id, data_achizitie);
