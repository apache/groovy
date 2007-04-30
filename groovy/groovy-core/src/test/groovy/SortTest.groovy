package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class SortTest extends GroovyTestCase {

    void testSortWithOrderBy() {
        def list = getPeople()
        def order = new OrderBy( { it.cheese } )
        list.sort(order)
        
        assert list[0].name == 'Joe'
        assert list[-1].name == 'Chris'
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']

        println "Sorted by cheeee"
        list.each { println it.dump() }
    }
    
    void testSortWithClosure() {
        def list = getPeople()
        list.sort { it.cheese }
        
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']

        println "Sorted by cheeee"
        list.each { println it.dump() }
    }
    
    def getPeople() {
        def answer = []
        answer << new Expando(name:'James', cheese:'Edam', location:'London')
        answer << new Expando(name:'Bob', cheese:'Cheddar', location:'Atlanta')
        answer << new Expando(name:'Chris', cheese:'Red Leicester', location:'London')
        answer << new Expando(name:'Joe', cheese:'Brie', location:'London')
        return answer
    }

}