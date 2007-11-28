
class NoPackageTest extends GroovyTestCase {

    void testClassDef() {
        assert getClass().name == "NoPackageTest"
    }
}
