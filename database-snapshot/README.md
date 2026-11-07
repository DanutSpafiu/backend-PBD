# Copia bazei actuale

`restore.sql` reproduce schema aplicatiei exportata pe 7 noiembrie 2026:
4 clienti si 6 achizitii, inclusiv cele doua inregistrari Siemens existente.
Include ID-urile originale, relatiile, sumele, disponibilul, constrangerile,
indexurile existente, procedura, functia si triggerul citite direct din Oracle.
Nu include utilizatori Oracle, parole sau configuratia calculatorului sursa.

Snapshot-ul este distinct de seed-ul proiectului, care contine doar cele cinci
achizitii din enunt. Pentru aceasta copie NU rula `install.sql` sau `02_seed.sql`.

## Restaurare la coleg

1. Cloneaza repository-ul si instaleaza Docker Desktop si un JDK compatibil cu
   Java 23. Creeaza un container nou, alegand propriile parole in locul exemplelor:

```powershell
docker run -d --name db-pbd -p 1522:1521 -e ORACLE_PASSWORD=Admin_AlegeOParola -e APP_USER=pbd -e APP_USER_PASSWORD=App_AlegeOParola -v pbd-oracle-data:/opt/oracle/oradata gvenzl/oracle-free:23.26.3
```

2. Asteapta pornirea bazei (`docker logs db-pbd`), apoi acorda privilegiile
   contului aplicatiei:

```powershell
docker exec -it -u oracle db-pbd sqlplus / as sysdba
```

In consola SQLPlus:

```sql
ALTER SESSION SET CONTAINER = FREEPDB1;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER TO pbd;
ALTER USER pbd QUOTA 64M ON users;
EXIT
```

3. Importa snapshot-ul intr-o schema NOUA, GOALA. Comanda SQLPlus solicita parola
   contului pbd, aleasa la crearea containerului:

```powershell
docker cp database-snapshot/restore.sql db-pbd:/tmp/pbd-restore.sql
docker exec -it db-pbd sqlplus pbd@//localhost:1521/FREEPDB1 @/tmp/pbd-restore.sql
```

Alternativ, conecteaza SQL Developer la localhost:1522, service FREEPDB1, user pbd
si ruleaza `restore.sql` cu F5 (Run Script). La final trebuie sa apara 4 clienti,
6 achizitii si trei obiecte cu status VALID.

4. Creeaza `.env` in radacina proiectului, cu parola proprie a contului pbd:

```properties
DB_URL=jdbc:oracle:thin:@//localhost:1522/FREEPDB1
DB_USERNAME=pbd
DB_PASSWORD=App_AlegeOParola
```

5. Porneste backend-ul:

```powershell
mvn spring-boot:run
```

API: http://localhost:8080/api/clienti. Lista rutelor este in README-ul principal.

## Detalii de restaurare

Triggerul este creat dupa inserarea datelor istorice, astfel incat platile si
disponibilul sa nu fie recalculate. ID-urile sunt importate explicit, apoi
coloanele identity sunt repornite peste ID-ul maxim si readuse la modul original.
Cache-urile interne si pozitia exacta a secventelor din vechea baza nu se copiaza.
Schema originala nu este modificata de export sau de testarea restaurarii.

Pentru actualizarea snapshot-ului dupa modificari ale bazei sursa:

```powershell
./scripts/export-database.ps1
./scripts/test-database-snapshot.ps1
```

`snapshot.json` retine valorile exportate pentru compararea automata a copiei.
Snapshot-ul contine datele clientilor si este destinat partajarii acestui proiect.
