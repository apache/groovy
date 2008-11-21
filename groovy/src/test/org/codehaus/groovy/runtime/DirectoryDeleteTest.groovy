package org.codehaus.groovy.runtime;

/**
 * Test File.deleteDir() method in Groovy
 *
 * @author <a href="mailto:j.heldmann@web.de">Joachim Heldmann</a>
 * @version $Revision: 7320 $
 */
class DirectoryDeleteTest extends GroovyTestCase {

    void testDeleteDir(){
        def file = File.createTempFile("deleteDirTest", "")

        // deleteDir for existing file should return false
        assert !file.deleteDir()

        // deleteDir for non existing file should return true
        file.delete();
        assert file.deleteDir()

        // create and delete empty directory
        def dir = new File(file.getPath())
        assert dir.mkdir()
        assert dir.deleteDir()

        // create and delete directory with file
        dir = new File(file.getPath())
        assert dir.mkdir()
        new File(dir, "test.txt").write("Test")
        assert dir.deleteDir()

        // create and delete directory with subdirectory and file
        dir = new File(file.getPath())
        assert dir.mkdir()
        new File(dir, "test.txt").write("Test")
        def subdir = new File(dir, "subdir")
        subdir.mkdir()
        new File(subdir, "testsubdir.txt").write("Test")
        assert dir.deleteDir()
    }
}
