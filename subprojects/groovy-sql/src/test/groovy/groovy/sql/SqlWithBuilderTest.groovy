package groovy.sql

/**
 * @author Brian McCallister
 * @version $Revision$
 */
class SqlWithBuilderTest extends SqlHelperTestCase {

    void testSqlQuery() {
         def sql = createSql()
         println "Created ${sql}"
        
         def doc = new NodeBuilder()
        
         doc.people {
             sql.eachRow("select * from PERSON") {
                 doc.person(first: it.firstname, last: it.lastname, location: it.location_name)
             }
         }
        
         sql.close()
    }
}

