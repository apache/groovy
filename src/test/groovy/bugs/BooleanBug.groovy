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
    
    void testBug2() {
        BooleanBean bean = new BooleanBean(name:'Gromit', foo:false)
        value = isApplicableTo(bean)
        assert value
    }
    
    public boolean isApplicableTo(BooleanBean field) {
        return !field.isFoo();
    }

}

class BooleanBean {
    String name
    boolean foo
}