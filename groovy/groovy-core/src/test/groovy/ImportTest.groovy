import java.io.*
import java.util.Map
import java.util.List

class ImportTest extends GroovyTestCase {

    void testImportAll() {
        def file = new File("foo.txt")
        assert file instanceof File
        assert file.getClass().name == "java.io.File"
    }
    
    void testImportByName() {
        def x = [:]
        assert x instanceof Map
        /** @todo 
        assert x.class != null 
        */
        assert x.getClass().name.startsWith("java.util.")
        
        def y = [1, 2, 3]
        assert y instanceof List
        assert y.getClass().name.startsWith("java.util.")
    }
}
