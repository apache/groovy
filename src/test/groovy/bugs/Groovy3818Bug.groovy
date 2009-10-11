package groovy.bugs

class Groovy3818Bug extends GroovyTestCase {
    void testCreatingSimilarSetandMapWithComparator() {
        def scompare = { a, b -> a.id <=> b.id } as Comparator

        def set = new TreeSet( scompare )

        set << [name: "Han Solo",       id: 1]
        set << [name: "Luke Skywalker", id: 2]
        set << [name: "L. Skywalker",   id: 3]

        def result = set.findAll { elt -> elt.name =~ /Sky/ }
        assert result.size() == 2

        result = set.grep { elt -> elt.name =~ /Sky/ }
        assert result.size() == 2

        def mcompare = { a, b -> a.id <=> b.id } as Comparator

        def map = new TreeMap( mcompare )

        map[[name: "Han Solo", id: 1]] = "Dummy"
        map[[name: "Luke Skywalker", id: 2]] = "Dummy"
        map[[name: "L. Skywalker",   id: 3]] = "Dummy"

        result = map.findAll { elt ->elt.key.name =~ /Sky/ }
        assert result.size() == 2
    }
}
