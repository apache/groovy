

/**
 * Mixes variables, closures and method calls in markup
 *
 * @version $Revision$
 */
class MarkupAndMethodBug extends GroovyTestCase {
    
    void testBug() {
        tree = createTree()

        name = tree.person[0]['@name']
        assert name == 'James'
    }
    
    protected def createTree() {
        builder = NodeBuilder.newInstance()
        
        root = builder.people() {
            person(name:getName()) {
            /*
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
            */
            }
        }
        
        assert root != null
        
        return root
    }
    
    protected def getName() {
        "James"
    }
}
