package groovy

/**
 * Unit test for File GDK methods
 *
 * @author Marc Guillemot
 * @version $Revision: 4996 $
 */
class FileTest extends GroovyTestCase {

	def baseDir = new File("target/test-resources/filetest")

	void setUp()
	{
		createFolder "emptyFolder"
    	createFile "folder1/Readme"
    	createFile "folder1/build.xml"
    	createFile "folder2/myDoc.doc"
    	createFile "folder2/myDoc.odt"
    	createFile "folder2/subfolder/file1.groovy"
    	createFile "folder2/subfolder/file2.groovy"
    	createFile "folder3/subfolder/file3.groovy"
    	createFile "foo"
    	createFile "foo.txt"
	}

	void testEachFile() {
		def collectedFiles = []
		baseDir.eachFile { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["emptyFolder", "folder1", "folder2", "folder3", "foo", "foo.txt"]
    	
    	assertEquals expected, collectedFiles
    }

	void testEachDir() {
		def collectedFiles = []
		baseDir.eachDir { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["emptyFolder", "folder1", "folder2", "folder3"]
    	
    	assertEquals expected, collectedFiles
    }

	void testEachFileMatch() {
		def collectedFiles = []
		baseDir.eachFileMatch ~/fo.*/, { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["folder1", "folder2", "folder3", "foo", "foo.txt"]
    	
    	assertEquals expected, collectedFiles
    }

	void testEachDirMatch() {
		def collectedFiles = []
		baseDir.eachDirMatch ~/fo.*/, { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["folder1", "folder2", "folder3"]
    	
    	assertEquals expected, collectedFiles
    }

	void testEachFileRecurse() {
		def collectedFiles = []
		baseDir.eachFileRecurse { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["Readme", "build.xml", "emptyFolder",  
    	 "file1.groovy", "file2.groovy", "file3.groovy", "folder1", "folder2", "folder3", 
    	 "foo", "foo.txt",
    	 "myDoc.doc", "myDoc.odt", 
    	 "subfolder", "subfolder"]
    	
    	assertEquals expected, collectedFiles
    }
	
	void testEachDirRecurse() {
		def collectedFiles = []
		baseDir.eachDirRecurse { it -> collectedFiles << it.name }
		collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

    	def expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder",]
    	
    	assertEquals expected, collectedFiles
    }

	def createFile(path)
	{
		def f = new File(baseDir, path)
		f.parentFile.mkdirs()
		f.createNewFile()
	}

	def createFolder(path)
	{
		def f = new File(baseDir, path)
		f.mkdirs()
	}
}
