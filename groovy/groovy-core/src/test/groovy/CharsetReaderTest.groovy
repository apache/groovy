import java.io.File

class CharsetReaderTest extends GroovyTestCase {

	void testSamples() {
		referenceLines = ["a grave    à", "e acute    é", "e grave    è", "i umlaut   ï", "o circ     ô", "u grave    ù", "c cedil    ç", "n tilde    ñ"]

		dir = new File("src/test/groovy")
		dir.eachFile{ file |
			name = file.getName()
			if (name ==~ "charset-.*\\.txt") {
			    println("file: ${name}")
			    
				readLines = file.readLines()
				
				println("readlines     : ${readLines}")
				println("referenceLines: ${referenceLines}")
				
				assert readLines == referenceLines
			}
		}
	}
}