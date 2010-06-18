package groovy.bugs

class Groovy4247Bug extends GroovyTestCase {
    void testNewFileRenameToMethod() {
        def file = new File('blah_blah')

        def methods = file.metaClass.respondsTo(file, "renameTo", [String] as Class[])
        assert methods.size() == 1

        def renameToMethod = methods[0]

        try {
            renameToMethod.invoke(file, [null] as Object[])
            fail('The renameTo() with null parameter should have failed with NPE')
        } catch(ex) {
            assert ex instanceof NullPointerException
        }
    }
}
