#!bin/bashrc
/opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Pass@word'

SELECT name, database_id, create_date FROM sys.databases;  
GO  

USE Microsoft.eShopOnContainers.Services.OrderingDb;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';
