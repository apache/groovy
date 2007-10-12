package groovy.util

/**
    Make sure FilenNameFinder uses Ant filesets correctly.
    @author Dierk Koenig
*/
class fileNameFinderTest extends GroovyLogTestCase {

    void testFilesInTestDirArePickedUp() {
        def finder = new FileNameFinder()
        def files = finder.getFileNames('src/test','*')
        assert files, 'There should be files in the src/test'
    }
}