
class NoPackageTest
	extends GroovyTestCase
{
    void testClassDef()
    {
        assert getClass().equals( Class.forName( "NoPackageTest" ) );
    }
}
