package groovy

/**
 * check that the new filterLine() method on InputStream is ok
 * (and indirectly test newReader() method on InputStream)
 * as specified in GROOVY-624 and GROOVY-625
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class FilterLineTest extends GroovyTestCase {
	def myFile
	def myInput
	def myOutput

	void setUp() {
	    myFile = new File("src/test/groovy/FilterLineTest.groovy")
		myInput = new FileInputStream(myFile)
		myOutput = new CharArrayWriter()
	}

	void testFilterLineOnFileReturningAWritable() {
		def writable = myFile.filterLine() {it.contains("testFilterLineOnFileReturningAWritable")}
		writable.writeTo(myOutput)
		assert 3 == myOutput.toString().count("testFilterLineOnFileReturningAWritable")
	}

	void testFilterLineOnFileUsingAnOutputStream() {
		myFile.filterLine(myOutput) {it.contains("testFilterLineOnFileUsingAnOutputStream")}
		assert 3 == myOutput.toString().count("testFilterLineOnFileUsingAnOutputStream")
	}

	void testFilterLineOnInputStreamReturningAWritable() {
		def writable = myInput.filterLine() {it.contains("testFilterLineOnInputStreamReturningAWritable")}
		writable.writeTo(myOutput)
		assert 3 == myOutput.toString().count("testFilterLineOnInputStreamReturningAWritable")
	}

	void testFilterLineOnInputStreamUsingAnOutputStream() {
		myInput.filterLine(myOutput) {it.contains("testFilterLineOnInputStreamUsingAnOutputStream")}
		assert 3 == myOutput.toString().count("testFilterLineOnInputStreamUsingAnOutputStream")
	}
}