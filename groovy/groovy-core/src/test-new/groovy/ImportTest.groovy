import java.io.*
import java.util.Map, List

class ImportTest extends GroovyTestCase {

    void testImportAll() {
        file = new File("foo.txt")
		assert file instanceof File
		assert file.class.name == "java.io.File"
    }
    
    void testImportByName() {
        x = [:]
        assert x instanceof Map
        /** @todo 
        assert x.class != null 
        */
        assert x.getClass().name.startsWith("java.util.")
        
        y = [1, 2, 3]
        assert y instanceof List
        assert y.getClass().name.startsWith("java.util.")
    }
}
