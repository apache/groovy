class HeredocsTest extends GroovyTestCase {

    void testHeredocs() {
        name = "James"
        s = <<<EOF
abcd
efg

hijk
     
hello ${name}
        
EOF
        println s
		assert s != null
		assert s instanceof GString

		assert s.contains("i")
		assert s.contains("James")
    }
    
    void testDollarEscaping() {
        /** @todo use EOF again */
        s = <<<EOF
hello $${name}
EOF
		println s
		assert s != null
		assert s.contains("$")
		c = s.count("$")
		assert c == 1
    }
}
