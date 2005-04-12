

/**
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {
    
    void testSimpleGPathExpressions() {
        tree = createTree()

        assert tree.person.find { it['@name'] == 'James' }.location[0]['@name'] == 'London'
    }
    
    void testFindAll() {
        tree = createTree()
        
        coll = tree.person.findAll { it['@name'] != 'Bob' }
        assert coll.size() == 1
    }
    
    void testCollect() {
        tree = createTree()
        
        coll = tree.person.collect { it['@name'] }
        assert coll == ['James', 'Bob']
    }
    
    protected createTree() {       
        builder = NodeBuilder.newInstance()
        
        root = builder.people() {
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
