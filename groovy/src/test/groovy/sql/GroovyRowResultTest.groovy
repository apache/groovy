package groovy.sql

import java.util.LinkedHashMap

class GroovyRowResultTest extends GroovyTestCase {

    void testMap() {
        def row = createRow();
        def row2 = createRow();
        
        /**
         * Test for implementing Map
         */ 
        assert row instanceof Map, "GroovyRowResult doesn't implement Map interface"
        
        /**
         * Test for put and accessing the new property
         */ 
        row.put("john","Doe")
        assert row.john=="Doe"
        assert row["john"]=="Doe"
        assert row['john']=='Doe'
        assert row.containsKey("john")
        assert !row2.containsKey("john")
        assert row.containsValue("Doe")
        assert !row2.containsKey("Doe")

        /**
         * Test for equality (1) and size
         */ 
        assert row!=row2, "rows unexpectedly equal"
        assert row.size()==7
        assert row2.size()==6
        
        /**
         * Test for remove, equality (2) and isEmpty (1)
         */ 
        row.remove("john")        
        assert row==row2, "rows different after remove"
        assert !row.isEmpty(), "row empty after remove"
        
        /**
         * Test for clear, equality (3) and isEmpty (2)
         */ 
        row.clear()
        row2.clear()
        assert row==row2, "rows different after clear"
        assert row.isEmpty(), "row not empty after clear"
    }
    
    void testProperties() {
        def row = createRow()
        assert row.miXed == "quick"
        assert row.lower == "brown"
        assert row.upper == "fox"
        assert row.UPPER == "fox"
        
        try {
            assert row.LOWER == "brown"
            assert false
        } catch (MissingPropertyException mpe) {
        } catch (Exception e) {
            assert false
        }

        try {
            println row.foo
            assert false
        } catch (MissingPropertyException mpe) {
        } catch (Exception e) {
            assert false
        }

        /**
         * This is for GROOVY-1296
         */
        assert row.nullMixed==null
        assert row[1]==null
        assert row.nulllower==null
        assert row[3]==null
        assert row.NULLUPPER==null
        assert row[5]==null
        
    } 
    
    void testOrder() {
        def row = createRow()
        assert row[0] == "quick" 
        assert row[1] == null 
        assert row[2] == "brown" 
        assert row[3] == null 
        assert row[4] == "fox" 
        assert row[5] == null 
        assert row[27] == null 
        assert row[-1] == null 
        assert row[-2] == "fox" 
    }

    protected def createRow() {
        def map = new LinkedHashMap()
        assert map != null , "failed to load LinkedHashMap class"

        map.put("miXed", "quick")
        map.put("nullMixed", null)
        map.put("lower", "brown")
        map.put("nulllower", null)
        map.put("UPPER", "fox")
        map.put("NULLUPPER", null)

        def row = new GroovyRowResult(map)
        assert row != null , "failed to load GroovyRowResult class"

        return row
    }
    
}