/**
 * @version $Revision$
 */
class BytecodeBug extends GroovyTestCase {

    //Integer count = 0
    count = 0
    
    void testBytecodeBug() {
		getCollection().each { count += it }       
    }
    
    getCollection() {
        [1, 2, 3, 4]
    }
}