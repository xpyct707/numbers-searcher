# numbers-searcher
Spring-based SOAP web-service
Setup and run:
1. Create database and table via 'database/create_structure.sql' (PostgreSQL 10 is required).
2. Build the whole project via 'mvn clean install' from root folder. (Maven 3.3.9+ is required).
3. Unzip 'web/target/numbers-searcher.zip' to any directory.
4. Unzip 'data-files-generator/target/data-files-generator.zip' to the same directory.
5. Run files-generator.bat or files-generator.sh to create data files.
6. Run run-service.bat or run-service.sh to run the web-service.
7. The web-service is available on http://localhost:8080/ws/numbers-searcher.wsdl.