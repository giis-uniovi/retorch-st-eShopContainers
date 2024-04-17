#!bin/bashrc
docker exec -it sqldata_tjobeshoptesting /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Pass@word'
#Para ver lo que hay en la BBDD
USE [Microsoft.eShopOnContainers.Services.CatalogDb];SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';GO


QUERY="USE [Microsoft.eShopOnContainers.Services.CatalogDb]; SELECT COUNT (*) FROM Catalog;"

USE [Microsoft.eShopOnContainers.Services.CatalogDb]; SELECT COUNT(*) FROM nombre_de_tabla;


#SCRIPT
#!/bin/bash

# Nombre de la base de datos (con "." en el nombre)
DB_NAME="[Microsoft.eShopOnContainers.Services.CatalogDb]"

# Nombre de la tabla en la base de datos
TABLE_NAME="Catalog"

# Comando para contar los registros en la tabla especificada
QUERY="USE $DB_NAME; SELECT COUNT (*) FROM $TABLE_NAME;"
QUERY="USE [Microsoft.eShopOnContainers.Services.CatalogDb]; SELECT COUNT (*) FROM Catalog;"

# Ejecutar la consulta SQL dentro del contenedor MSSQL en ejecución
docker exec -it sqldata_tjobeshoptesting /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Pass@word' -Q "$QUERY"




/opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Pass@word'

SELECT name, database_id, create_date FROM sys.databases;
GO  

SELECT name FROM sys.databases;
GO

USE [Microsoft.eShopOnContainers.Services.OrderingDb];
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';
SELECT * FROM Microsoft.eShopOnContainers.Services.OrderingDb.tables