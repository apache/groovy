class SmallTreeTest extends GroovyTestCase {
    
    @Property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( {
            elem1('hello1')
        })
        
        assert root != null
        
        println(root)
    }
}