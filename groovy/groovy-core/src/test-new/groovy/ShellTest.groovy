class ShellTest extends GroovyTestCase {

    void testReadAndWriteVariable() {
        shell = new GroovyShell()
        
        shell.foo = 1
        
        value = shell.evaluate(<<<EOF
println('foo is currently ' + foo)
foo = 2 
println('foo is now ' + foo)                
return foo
EOF, "Dummy1.groovy")

        
        assert value == 2
        assert shell.foo == 2 , "Value is now ${shell.foo}"
	}
    
    void testDefineNewVariable() {
        shell = new GroovyShell()
        
        value = shell.evaluate( <<<EOF2
bar = 3 
println('bar is now ' + bar)                
return bar
EOF2, "Dummy2.groovy")

        
        assert value == 3
        assert shell.bar == 3 , "Value is now ${shell.bar}"
    }
}