package groovy.gpath

/**
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {

    void testFind() {
        def tree = createTree()
        assert tree.person.find { it['@name'] == 'James' }.location[0]['@name'] == 'London'
    }

    void testFindAll() {
        def tree = createTree()
        def peopleWithNameBob = tree.person.findAll { it['@name'] != 'Bob' }
        assert peopleWithNameBob.size() == 1
    }

    void testCollect() {
        def tree = createTree()
        def namesOfAllPeople = tree.person.collect { it['@name'] }
        assert namesOfAllPeople == ['James', 'Bob']
    }

    protected def createTree() {
        def builder = NodeBuilder.newInstance()
        def root = builder.people() {
            person(name:'James') {
                location(name:'London')
                projects {
                    project(name:'geronimo')
                }
            }
            person(name:'Bob') {
                location(name:'Atlanta')
                projects {
                    project(name:'drools')
                }
            }
        }
        assert root != null
        return root
    }
}
