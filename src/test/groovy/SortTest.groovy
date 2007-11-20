package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class SortTest extends GroovyTestCase {

    // GROOVY-1956
    void testSortWithNull() {
        // normal case, should sort in place and return result
        def x = [1, 2, 3, 1, 2, 3, null, 'a', null]
        assert x.is(x.sort())
        def y = x.sort()
        assert (y == x && x == [null, null, 1, 1, 2, 2, 3, 3, 'a'])

        // transitivity
        x = [1, 2, 3, 1, 2, 3, null, 'a', null]
        x.unique().sort()
        y = [1, 2, 3, 1, 2, 3, null, 'a', null]
        y.sort().unique()
        assert (x == y && y == [null, 1, 2, 3, 'a'])
    }

    void testSortWithOrderBy() {
        def list = getPeople()
        def order = new OrderBy( { it.cheese } )
        list.sort(order)
        assert list[0].name == 'Joe'
        assert list[-1].name == 'Chris'
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']
    }
    
    void testSortWithClosure() {
        def list = getPeople()
        list.sort { it.cheese }
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']
    }

    void testSortClassHierarchy() {
        def listOfFoo = [
                new Foo(5),
                new Foo(7),
                new Bar(4),
                new Bar(6)
                ]
        def sorted = listOfFoo.sort()
        assert sorted.collect{ it.class } == [Bar, Foo, Bar, Foo]
        assert sorted.collect{ it.key } == (4..7).toList()
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

class Foo implements Comparable {
    int key
    Foo(int key) { this.key = key }
    int compareTo(Object rhs) { key - rhs.key }
    String toString() { this.class.name + ": " + key }
}

class Bar extends Foo {
    public Bar(int x) {super(x)}
}
