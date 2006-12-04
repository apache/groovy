package groovy.sql

import groovy.xml.MarkupBuilder 

/**
 * @author Brian McCallister
 * @version $Revision$
 */
class SqlWithBuilderTest extends TestHelper {

    void testSqlQuery() {
         def sql = createSql()
         println "Created ${sql}"
        
         def doc = new MarkupBuilder()
        
         doc.people {
             sql.eachRow("select * from PERSON") {
                 doc.person(first: it.firstname, last: it.lastname, location: it.location_name)
             }
         }
        
         sql.close()
    }
}

