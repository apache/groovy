package groovy.bugs

/**
 * Mixes variables, closures and method calls in markup
 *
 * @version $Revision$
 */
class MarkupAndMethodBug extends GroovyTestCase {
    
    void testBug() {
        def tree = createTree()
        def name = tree.person[0]['@name']
        assert name == 'James'
    }
    
    protected def createTree() {
        def builder = NodeBuilder.newInstance()
        
        def root = builder.people() {
            person(name:getTestName()) {
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
    
    protected def getTestName() {
        "James"
    }
}
