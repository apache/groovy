package groovy.util

import java.io.File

class AntTest extends GroovyTestCase {
    
    void testAnt() {
        ant = new AntBuilder()

        // lets just call one task
        ant.echo("hello")
        
        // here"s an example of a block of Ant inside GroovyMarkup
        ant.sequential {
            echo("inside sequential")
            
            myDir = "target/AntTest/"
            
            mkdir(dir:myDir) 
            copy(todir:myDir) {
                fileset(dir:"src/test") {
                    include(name:"**/*.groovy")
                }
            }
            
            echo("done")
        }
        
        // now lets do some normal Groovy again
        file = new File("target/AntTest/groovy/util/AntTest.groovy")
        assert file.exists()
    }
}
