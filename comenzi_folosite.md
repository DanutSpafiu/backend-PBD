# Comenzi SQL folosite pentru crearea bazei de date

## Clienti
```sql 
CREATE TABLE CLIENTI (
                         ID NUMBER GENERATED ALWAYS AS IDENTITY,
                         NUME VARCHAR2(15) NOT NULL,
                         PRENUME VARCHAR2(20) NOT NULL,
                         CNP VARCHAR2(13) NOT NULL,
                         ADRESA VARCHAR2(255),
                         TELEFON VARCHAR2(9) NOT NULL,
                         DISPONIBIL NUMBER(12,2) DEFAULT 0 NOT NULL,

                         CONSTRAINT PK_CLIENTI PRIMARY KEY (ID),
                         CONSTRAINT UQ_CLIENTI_CNP UNIQUE (CNP),
                         CONSTRAINT UQ_CLIENTI_TELEFON UNIQUE (TELEFON),

                         CONSTRAINT CK_CLIENTI_CNP
                             CHECK (REGEXP_LIKE(CNP, '^[0-9]{13}$')),

                         CONSTRAINT CK_CLIENTI_TELEFON
                             CHECK (REGEXP_LIKE(TELEFON, '^[0-9]{9}$')),

                         CONSTRAINT CK_CLIENTI_DISPONIBIL
                             CHECK (DISPONIBIL >= 0)
);
```

## Achizitii

```sql
CREATE TABLE ACHIZITII (
                           ID NUMBER GENERATED ALWAYS AS IDENTITY,
                           CLIENT_ID NUMBER NOT NULL,
                           PRODUS VARCHAR2(255) NOT NULL,
                           DATA_ACHIZITIE DATE NOT NULL,
                           PRET NUMBER(12,2) NOT NULL,
                           SUMA_INCASATA NUMBER(12,2) DEFAULT 0 NOT NULL,

                           CONSTRAINT PK_ACHIZITII PRIMARY KEY (ID),

                           CONSTRAINT FK_ACHIZITII_CLIENTI
                               FOREIGN KEY (CLIENT_ID)
                                   REFERENCES CLIENTI(ID),

                           CONSTRAINT CK_ACHIZITII_PRET
                               CHECK (PRET > 0),

                           CONSTRAINT CK_ACHIZITII_INCASATA
                               CHECK (SUMA_INCASATA >= 0)
);

```
