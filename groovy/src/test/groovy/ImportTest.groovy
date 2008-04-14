package groovy

class ImportTest extends GroovyTestCase {

    void testImportAll() {
        def file = new File("foo.txt")
        assert file instanceof File
        assert file.getClass().name == "java.io.File"
    }
    
    void testImportByName() {
        def x = [:]
        assert x instanceof Map
        /**
         * For maps, map.getClass() should be used instead of map.class,
         * when map has no member, named as "class"
         */
        assert x.getClass() != null
        assert x.getClass().name.startsWith("java.util.")
        
        def y = [1, 2, 3]
        assert y instanceof List
        assert y.getClass().name.startsWith("java.util.")
    }
}
