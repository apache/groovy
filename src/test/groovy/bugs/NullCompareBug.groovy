/**
 * @version $Revision: 1.3 $
 */
class NullCompareBug extends GroovyTestCase {
    
    void testBug() {
        assert "dog" > null
        assert null < "dog"
        assert null == null
    }
}