import java.io.File

class ScriptTest extends GroovyTestCase {
    void testScripts() {
        file = new File("src/test/groovy/script")
        file.eachFile {
            name = it.name
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
    
    protected runScript(file) {
        println("Running script: " + file)
        
        shell = new GroovyShell()
        args = ['a', 'b', 'c']
        
        shell.run(file, args)

        /** @todo this doesn't work when ran in an IDE?
        try {
	        shell.run(file, args)
        } 
        catch (Exception e) {
            println("Caught: " + e)
        }
         */
    }
}