package groovy.sql

import groovy.xml.MarkupBuilder 

/**
 * @author Thomas Heller
 * @version $Revision$
 */
class SqlWithTypedResultsTest extends TestHelper {

    void testSqlQuery() {
         def sql = createEmptySql()
         
         sql.execute("create table groovytest ( anint integer, astring varchar )");

         def groovytest = sql.dataSet("groovytest")
         groovytest.add( anint:1, astring:"Groovy" )
         groovytest.add( anint:2, astring:"rocks" )

         // this line messes up things:
         /** @todo this fails
         Integer id
		 */
         Integer id = 0
		 
         sql.eachRow("SELECT * FROM groovytest ORDER BY anint") { 
         	println "found ${it.astring} for id ${it.anint}"
         	
         	id = it.anint 
         }

         assert id == 2
        
         sql.close()
    }
}

