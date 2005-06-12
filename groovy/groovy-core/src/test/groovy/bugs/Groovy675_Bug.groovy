package groovy.bugs

/**
 *  @author Pilho Kim
 *  @version $Revision$
 */ 
class Groovy675_Bug extends GroovyTestCase {
    void testStringAndGString() {
	assert "\\"!="\\\\" 
	assert "\\\$"=="\\"+"\$" 
	assert "\\"+"\\\\" == "\\"+"\\"+"\\" && "\\\\"+"\\" == "\\"+"\\"+"\\"
	assert ("\\\\"+"\\").length() == 3
	assert "\\3 \$1\$2" == "\\" + "3" + " " + "\$" + "1" + "\$" + "2"
	assert "\\\\3 \\\$1\$2" == "\\" + "\\" + "3" + " " + "\\"+ "\$" + "1" + "\$" + "2"
	assert "\\\\\\3 \\\\\$1\$2" == "\\" + "\\\\" + "3" + " " + "\\\\"+ "\$" + "1" + "\$" + "2"
	assert "\\\\\\\\3 \\\\\\\$1\$2" == "\\\\" + "\\\\" + "3" + " " + "\\\\\\"+ "\$" + "1" + "\$" + "2"

	assert "\\\\" == "\\" + "\\"
	assert "\\\\".length() == 2

	def z = 100 + 200
	assert "\\\\ \\ ${z}" == "\\\\ \\ 300"
	assert "\\\\ \\ ${z}" == "\\" + "\\" + " " + "\\" + " " + "300"
	assert "Hello\\, \\World\\".charAt(4) == "o".charAt(0)
	assert "Hello\\, \\World\\".charAt(5) == "\\".charAt(0)
	assert "Hello\\, \\World\\".charAt(6) == ",".charAt(0)
    }
}
