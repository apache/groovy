

/**
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {
    
    void testSimpleGPathExpressions() {
        tree = createTree()

        assert tree.person.find { it.attribute('name') == 'James' }.location.get(0).attribute('name') == 'London'
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
