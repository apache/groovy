/** 
 * Tests the min() and max() functions
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MinMaxTest extends GroovyTestCase {

    void testSimpleMinMax() {
        list = [5, 2, 6, 1, 9, 8]
        
        n = list.min()
        assert n == 1
        
        n = list.max()
        assert n == 9
    }
    
    void testMinMaxWithComparator() {
		people = getPeople()
		
		// lets find the maximum by name
		
		order = new OrderBy( { it.get('@cheese') } )
		
		println("People ${people}")
		
		p = people.min(order)
		
		println("Found ${p}")
		
		assert p.get("@name") == "Joe" : "found person ${p}"
		
		p = people.max(order)
		assert p.get("@name") == "Chris" : "found person ${p}"
	}
    
    getPeople() {
        builder = new NodeBuilder()
        tree = builder.people {
            person(name:'James', cheese:'Edam', location:'London')
            person(name:'Bob', cheese:'Cheddar', location:'Atlanta')
            person(name:'Chris', cheese:'Red Leicester', location:'London')
            person(name:'Joe', cheese:'Brie', location:'London')
        }
        
        return tree.children()
    }

}