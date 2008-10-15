package groovy

class ShellTest extends GroovyTestCase {

    void testReadAndWriteVariable() {
        def shell = new GroovyShell()
        
        shell.foo = 1
        
        def value = shell.evaluate("""
println('foo is currently ' + foo)
foo = 2 
println('foo is now ' + foo)                
return foo
""", "Dummy1.groovy")

        assert value == 2
        assert shell.foo == 2 , "Value is now ${shell.foo}"
	}
    
    void testDefineNewVariable() {
        def shell = new GroovyShell()
        
        def value = shell.evaluate( """
bar = 3 
println('bar is now ' + bar)                
return bar
""", "Dummy2.groovy")

        assert value == 3
        assert shell.bar == 3 , "Value is now ${shell.bar}"
    }

    void testArgs() {
        def seventyfive = new GroovyShell().run("args[0] + args[1]", "StringSummerScript", ['7', '5'])
        assert seventyfive == '75'
        def twelve = new GroovyShell().run("args*.toInteger().sum()", "NumberSummerScript", ['7', '5'])
        assert twelve == 12
    }
}