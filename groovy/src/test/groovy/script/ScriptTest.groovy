package groovy.script

class ScriptTest extends GroovyTestCase {
    void testScripts() {
        def file = new File("src/test/groovy/script")
        file.eachFile {
            def name = it.name
            if (name.endsWith('.groovy')) {
                if (name.startsWith('ScriptTest')) {
                    //
                }
				else {                        
                	runScript(it) 
				}
            } 
        }
    }
    
    protected def runScript(file) {
        println("Running script: " + file)
        
        def shell = new GroovyShell()
        def args = ['a', 'b', 'c']
        
        try {
	        shell.run(file, args)
        } 
        catch (Exception e) {
            println("Caught: " + e)
        }
    }
}