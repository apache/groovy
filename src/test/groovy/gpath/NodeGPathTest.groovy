

/**
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {
    
    void testSimpleGPathExpressions() {
        tree = createTree()

        assert tree.person.find { it['@name'] == 'James' }.location[0]['@name'] == 'London'
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
