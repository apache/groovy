package groovy.util

class OrderByTest extends GroovyTestCase {

    void testSortByOneField() {
        def builder = new NodeBuilder()
        def tree = builder.people {
            person(name:'James', cheese:'Edam', location:'London')
            person(name:'Bob', cheese:'Cheddar', location:'Atlanta')
            person(name:'Chris', cheese:'Red Leicester', location:'London')
            person(name:'Joe', cheese:'Brie', location:'London')
        }
        
        def people = tree.children()
        
        /** @todo parser should allow this syntax sugar
        def order = new OrderBy { it.get('@cheese') }
        */
        def order = new OrderBy( { it.get('@cheese') } )
        def sorted = people.sort(order)
        
        assert sorted.get(0).get('@name') == 'Joe'
        assert sorted.get(1).get('@name') == 'Bob'
        assert sorted.get(2).get('@name') == 'James'
        assert sorted.get(3).get('@name') == 'Chris'
        
        order = new OrderBy( { it.get('@name') } )
        sorted = people.sort(order)
        
        assert sorted.get(0).get('@name') == 'Bob'
        assert sorted.get(1).get('@name') == 'Chris'
        assert sorted.get(2).get('@name') == 'James'
        assert sorted.get(3).get('@name') == 'Joe'
    }


    void testSortByMultipleFields() {
        def builder = new NodeBuilder()
        def tree = builder.people {
            person(name:'James', cheese:'Edam', location:'London')
            person(name:'Bob', cheese:'Cheddar', location:'Atlanta')
            person(name:'Chris', cheese:'Red Leicester', location:'London')
            person(name:'Joe', cheese:'Brie', location:'London')
        }
        
        def people = tree.children()

        def order = new OrderBy([ { it.get('@location') }, { it.get('@cheese') } ])
        def sorted = people.sort(order)
        
        assert sorted.get(0).get('@name') == 'Bob'
        assert sorted.get(1).get('@name') == 'Joe'
        assert sorted.get(2).get('@name') == 'James'
        assert sorted.get(3).get('@name') == 'Chris'
    }
}
