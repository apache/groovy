/** 
 * Tests the various new Groovy methods
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class GroovyMethodsTest extends GroovyTestCase {

	property count
	
    void testJoin() {
        assert [2, 4, 6].join("-") == "2-4-6"
        assert ["edam", "cheddar", "brie"].join(", ") == '"edam", "cheddar", "brie"'
        
        print( ["abc", 5, 2.34].join(", ") )
    }
    
    void testTimes() {
        count = 0
        5.times { i | count = count + i }
        assert count == 10
        
        count = 0
        temp = 5
        temp.times { i | count = count + i }
        
        assert count == 10
    }
    
    void testArraySubscript() {
        list = [1, 2, 3, 4]
        array = list.toArray()
        
        value = array[2]
        
        assert value == 3
        
        array[0] = 9
        
       	assert array[0] == 9
    }
}
