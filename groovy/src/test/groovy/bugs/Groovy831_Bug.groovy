package groovy.bugs

/**
 * Test for fixing the Jira issue GROOVY-831
 *
 * @author Pilho Kim
 * @version $Revision$
 */
class Groovy831_Bug extends GroovyTestCase {
    
    String[] cities = ['Seoul', 'London', 'Wasington']
    int[] intArrayData = [1, 3, 5]

    public String[] countries = [ 'Republic of Korea', 'United Kingdom', 'United State of America']
    public  int[] intArray  = [ 2, 4, 6 ]

    void testSetFieldProperty() {
        assert cities.size() == 3
        assert cities[0] == 'Seoul'
        assert cities[1] == 'London'
        assert cities[2] == 'Wasington'
        assert intArrayData.size() == 3
        assert intArrayData[0] == 1
        assert intArrayData[1] == 3
        assert intArrayData[2] == 5
    }

    void testSetFieldVariable() {
        assert countries.size() == 3
        assert countries[0] == 'Republic of Korea'
        assert countries[1] == 'United Kingdom'
        assert countries[2] == 'United State of America'
        assert intArray[0] == 2
        assert intArray[1] == 4
        assert intArray[2] == 6
    }
}

