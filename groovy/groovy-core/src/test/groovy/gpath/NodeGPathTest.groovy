

/**
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {
    
    void testSimpleGPathExpressions() {
        tree = createTree()

        //println("People: " + tree.people)

        x = tree.people.person
        
        println("type of person: " + x.getClass() + " value: " + x)
        
        x.each { println("each called with: " + it + " of type: " + it.getClass() ); return true }
        
        x.find { println("find called with: " + it + " of type: " + it.getClass() ); return true }
        
        println("Location: " + tree.people.person.find { it.name() == 'James' }.location.attribute('name'))
        //println("Person: " + tree.people.person)
        //println("location: " + tree.people.person.find { it.name == 'James' }.location )
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
        
        println(root)
        
        return root
    }
}
