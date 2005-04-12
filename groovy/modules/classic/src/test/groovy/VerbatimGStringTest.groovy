class VerbatimGStringTest extends GroovyTestCase {

    void testWithOneVariable() {
        
        name = <<<EOF
Bob
EOF
        
        template = <<<EOF
hello ${name} how are you?
EOF
				
		assert template instanceof GString
											 
	 	count = template.getValueCount()
		assert count == 1
		assert template.getValue(0) == "Bob"
											 
		string = template.toString()
		assert string == "hello Bob how are you?"
	}
    
    void testWithVariableAtEnd() {
        name = <<<EOS
Bob
EOS
        template = <<<EOS
hello ${name}
EOS

        string = template.toString()
        
        assert string == "hello Bob"
    }
    
    void testWithVariableAtBeginning() {
        name = <<<EOS
Bob
EOS

        template = <<<EOS
${name} hey,
hello
EOS
        string = template.toString()
        
        assert fixEOLs(string) == "Bob hey,\nhello"
    }

    void testWithJustVariable() {
        name = <<<EOS
Bob
EOS

        template = <<<EOS
${name}
EOS
        string = template.toString()
        
        assert string == "Bob"
    }

	void testInterestingCases() {
		name = <<<EOSEOSEOS
Bob
EOS
EOSEOSEO
EOSEOSEOS

		assert fixEOLs(name) == "Bob\nEOS\nEOSEOSEO"
		
		perl = <<<__END__
Sam
__END__

		assert perl == "Sam"
	}
}
