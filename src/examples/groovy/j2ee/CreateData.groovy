println("Called with context: " + context)

db = context.lookup("/client/tools/DatabaseHome").create()

println("About to do queries on: " + db) 

queries = [  "CREATE TABLE account ( ssn CHAR(11) PRIMARY KEY, first_name CHAR(20), last_name CHAR(20), balance INT)",			 
"CREATE TABLE entity ( id INT PRIMARY KEY AUTO INCREMENT, first_name CHAR(20), last_name CHAR(20) )"]

for (sql in queries) {
    println("evaluating: " + sql)
	db.execute(sql)
}

println("creating entity bean")

context.lookup("/client/tests/entity/bmp/BasicBmpHome").create("Groovy Dain")

println("Done")

"OK"