import groovy.sql.TestHelper

/**
 * @author Jonathan Carlson
 * @version $Revision$
 */
class ForAndSqlBug extends GroovyTestCase {
    
    void testBugInNormalMethod() {
        sql = TestHelper.makeSql()
        
        li = ["a", "b"]
        for (x in li) {
            sql.eachRow("SELECT count(*) FROM FOOD") { e ->
            	println " ${x}"

	            assert x != null
            }
        }
    }

    void testBugInsideScript() {
        assertScript( """
import groovy.sql.TestHelper
sql = TestHelper.makeSql()

li = ["a", "b"]
for (x in li) {
    sql.eachRow("SELECT count(*) FROM FOOD") { e ->
    	println " \${x}"
    	
    	assert x != null
    }
}
""")        
	}

}
