class OrderByTest extends GroovyTestCase {

    void testSortByOneField() {
        builder = new NodeBuilder()
        tree = builder.people {
            person(name:'James', cheese:'Edam', location:'London')
            person(name:'Bob', cheese:'Cheddar', location:'Atlanta')
            person(name:'Chris', cheese:'Red Leicester', location:'London')
            person(name:'Joe', cheese:'Brie', location:'London')
        }
        
        people = tree.children()
        
        /** @todo parser should allow this syntax sugar
        order = new OrderBy { it.get('@cheese') }
        */
        order = new OrderBy( { it.get('@cheese') } )
        sorted = people.sort(order)
        
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
        builder = new NodeBuilder()
        tree = builder.people {
            person(name:'James', cheese:'Edam', location:'London')
            person(name:'Bob', cheese:'Cheddar', location:'Atlanta')
            person(name:'Chris', cheese:'Red Leicester', location:'London')
            person(name:'Joe', cheese:'Brie', location:'London')
        }
        
        people = tree.children()

        order = new OrderBy([ { it.get('@location') }, { it.get('@cheese') } ])
        sorted = people.sort(order)
        
        assert sorted.get(0).get('@name') == 'Bob'
        assert sorted.get(1).get('@name') == 'Joe'
        assert sorted.get(2).get('@name') == 'James'
        assert sorted.get(3).get('@name') == 'Chris'
    }
}
