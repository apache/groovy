<html>
 <head>
 <title>Test Database Access</title>
 </head>
 <body>
 <p>^p@{page import=&quot;groovy.sql.Sql, test.groovy.sql.TestHelper&quot;}</p>
 <p>^p%{sql = TestHelper.makeSql()<br>
 type = 'cheese'<br>
 sql.queryEach(&quot;select * from FOOD where type = ${type}&quot;) 
 { }%</p>
 <p>Found cheese ${it.name}</p>
 <p>^p%{ } }%</p>
 </body>
</html>