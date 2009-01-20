package org.codehaus.groovy.runtime;

import java.io.File
import java.io.Reader

/** 
 * Test File append and left shift methods in Groovy
 * 
 * @author <a href="mailto:joachim.baumann@xinaris.de">Joachim Baumann</a>
 * @version $Revision$
 */
class FileAppendTest extends GroovyTestCase {
	/**
	 * The following instances are used in testing the file writes
	 */
	static text = """
			<groovy>
			  <things>
			    <thing>Jelly Beans</thing>
			  </things>
			  <music>
			    <tune>The 59th Street Bridge Song</tune>
			  </music>
			  <characters>
			    <character name="Austin Powers">
			       <enemy>Dr. Evil</enemy>
			       <enemy>Mini Me</enemy>
			    </character>
			  </characters>
			</groovy>
			"""
	static gPathResult = new XmlSlurper().parseText(text)
	static gPathWriteTo;

    public FileAppendTest ()
    {
		StringWriter sw = new StringWriter()
		gPathResult.writeTo(sw)
		gPathWriteTo = sw.toString()
	}
	
	// see below for class definition
	def testInstance = new TestClass()

	// Our file instance
	def File file;
	
	void setUp() {
		// Setup guarantees us that we use a non-existent file
		file = File.createTempFile("unitTest", ".txt") 
		assert file.exists() == true
		//println file.canonicalPath
		assert file.length() == 0L
	}
	void tearDown() {
		// we remove our temporary file
		file.deleteOnExit()
	}

	void testAppendString(){
		def expected
		
		// test new
		file.append(text)
		expected = text
		assert hasContents(file, expected)
		
		// test existing
		file.append(text)
		expected += text
		assert hasContents(file, expected)
	}
	 		
	void testAppendObjectToString(){
		def expected
		
		// test new
		file.append(testInstance)
		expected = testInstance.toString()
		assert hasContents(file, expected)
		
		// test existing
		file.append(testInstance)
		expected += testInstance.toString()
		assert hasContents(file, expected)
	}

	void testappendWritable(){
		def expected
		
		// test new
		file.append(gPathResult)
		expected = gPathWriteTo
		assert hasContents(file, expected)
		
		// test existing
		file.append(gPathResult)
		expected += gPathWriteTo
		assert hasContents(file, expected)
	}

	void testappendMixed(){
		def expected
		
		// test new
		file.append(text)
		expected = text
		assert hasContents(file, expected)
		
		file.append(testInstance)
		expected += testInstance.toString()
		assert hasContents(file, expected)
		
		file.append(gPathResult)
		expected += gPathWriteTo
		assert hasContents(file, expected)
		
		// test existing
		file.append(gPathResult)
		expected += gPathWriteTo
		assert hasContents(file, expected)

		file.append(testInstance)
		expected += testInstance.toString()
		assert hasContents(file, expected)

		file.append(text)
		expected += text
		assert hasContents(file, expected)
	}

	void testLeftShiftString(){
		def expected
		
		// test new
		file << text
		expected = text
		assert hasContents(file, expected)
		
		// test existing
		file << text
		expected += text
		assert hasContents(file, expected)
	}
			
	void testLeftShiftObjectToString(){
		def expected
		
		// test new
		file << testInstance
		expected = testInstance.toString()
		assert hasContents(file, expected)
		
		// test existing
		file << testInstance
		expected += testInstance.toString()
		assert hasContents(file, expected)
	}

	void testLeftShiftWritable(){
		def expected
		
		// test new
		file << gPathResult
		expected = gPathWriteTo
		assert hasContents(file, expected)
		
		// test existing
		file << gPathResult
		expected += gPathWriteTo
		assert hasContents(file, expected)
	}		 

	void testLeftShiftMixed(){
		def expected
		
		// test new
		file << text
		expected = text
		assert hasContents(file, expected)
		
		file << testInstance
		expected += testInstance.toString()
		assert hasContents(file, expected)
		
		file << gPathResult
		expected += gPathWriteTo
		assert hasContents(file, expected)
		
		// test existing
		file << gPathResult
		expected += gPathWriteTo
		assert hasContents(file, expected)

		file << testInstance
		expected += testInstance.toString()
		assert hasContents(file, expected)

		file << text
		expected += text
		assert hasContents(file, expected)
	}
	
	void testByteArrayAppend() {
		def total = []

		def array = [0x0,0x1,0x2] 
		total.addAll array
		file.append array as byte[]
		assert hasContents(file, total)

		array = [0x3,0x4] 
		total.addAll array
		file.append array as byte[]
		assert hasContents( file, total )
	}

	void testBinaryAppend() {
		def total = []

		def array = [0x0,0x1,0x2] 
		total.addAll array

		file.append new ByteArrayInputStream( array as byte[] )
		assert hasContents( file, total )

		//test leftShift here as well, which should simply be an alias for 'append'

		array = [0x5,0x6,0x7,0x8]
		total.addAll array
		file << new ByteArrayInputStream( array as byte[] )
		assert hasContents( file, total )
	}

	boolean hasContents(File f, String expected)
	{
		assert file.length() == expected.length()
		// read contents the Java way
		char[] cbuf = new char[expected.length()];
		def fileReader = new FileReader(file)
		try {
			fileReader.read(cbuf)
			return expected == String.valueOf(cbuf)
		}
		finally { fileReader?.close() }
	}

	boolean hasContents(File f, List expected) // list of bytes
	{
		assert file.length() == expected.size()
		byte[] buf = new byte[expected.size()];
		def fis = new FileInputStream(file)
		try {
			fis.read(buf)
			return (expected as byte[]) == buf
		}
		finally { fis?.close() }
	}
}

class TestClass {
	def testString = "TestThis"
	public String toString() {
		super.toString() + ": " + testString
	}
}
