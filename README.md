# Dokumentacja Wdrożeniowa Systemu HealthApp
Niniejszy dokument opisuje procedurę konfiguracji środowiska oraz uruchomienia systemu monitorowania zdrowia opartego na architekturze Event Sourcing w środowisku lokalnym.

### 1. Wymagania Środowiskowe
   Do poprawnej kompilacji i uruchomienia artefaktu wymagane są:

* Java Development Kit (JDK) 17 lub nowszy.

* Apache Maven 3.8.x (zarządca zależności i cyklu życia projektu).

* Dostęp do portu 8080 (domyślny port serwera osadzonego Tomcat).

### 2. Architektura Danych i Persystencja
   System wykorzystuje dwutorowe podejście do zarządzania danymi:

Flyway (Database Migration Tool): Odpowiada za automatyczne wersjonowanie schematu bazy danych. Przy starcie aplikacji system weryfikuje sumy kontrolne skryptów w lokalizacji src/main/resources/db/migration i w razie potrzeby aplikuje brakujące zmiany.

Spring Data JPA: Wykorzystuje walidację schematu (hibernate.ddl-auto=validate), co gwarantuje pełną synchronizację encji Java z tabelami bazy danych.

### 3. Procedura Uruchomieniowa
   Krok 1: Budowa projektu

W celu pobrania bibliotek zewnętrznych oraz zbudowania pliku wykonywalnego, należy wykonać komendę:

`mvn clean install`

Krok 2: Konfiguracja parametrów (opcjonalnie)

Domyślna konfiguracja (src/main/resources/application.properties) wykorzystuje bazę danych H2 w trybie in-memory. W przypadku chęci zmiany na środowisko produkcyjne (np. PostgreSQL), należy zmodyfikować parametry spring.datasource.*.

Krok 3: Inicjalizacja serwera aplikacji

Uruchomienie aplikacji następuje poprzez wywołanie:

`mvn spring-boot:run`

Podczas inicjalizacji kontekstu Spring, Flyway automatycznie utworzy niezbędne tabele.

Find user documentation under this [link](http://localhost:8080/swagger-ui/index.html#/) (works only after starting the app).