/**
 * @version $Revision$
 */
class BooleanBug extends GroovyTestCase {
    
    void testBug() {
        x = new BooleanBean(name:'James', foo:true)
        y = new BooleanBean(name:'Bob', foo:false)

        assert x.foo
        assert ! y.foo
        y.foo = true
        assert y.foo
    }
}

class BooleanBean {
    String name
    boolean foo
}