package groovy.util

/**
    Make sure FileNameFinder uses Ant filesets correctly.
    @author Dierk Koenig
    @author Paul King
*/
class fileNameFinderTest extends GroovyLogTestCase {

    void testFilesInTestDirArePickedUp() {
        def finder = new FileNameFinder()
        def files1 = finder.getFileNames('src/test','*')
        assert files1, 'There should be files in src/test'
        // now collect all those not starting with the letter 'J'
        def files2 = finder.getFileNames('src/test','*','J*')
        assert files2, 'There should be files in src/test'
        assert files1.size() > files2.size()
    }
}
