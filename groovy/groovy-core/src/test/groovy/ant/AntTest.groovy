package groovy.ant

import java.io.File

class AntTest extends GroovyTestCase {
    
    void testAnt() {
        ant = new AntBuilder()
        
        ant.echo('hello')
        
        /** @todo change to concise syntax when its available */
        ant.sequential() {
			echo('inside sequential')
			
			myDir = 'src/target/dummy'
			
			mkdir(['dir':myDir])
			copy(['todir':myDir]) {
			    fileset(['dir':'src/test']) {
			        include(['name':'**/*.groovy'])
			    }
			}

            echo('done')
        }
        
        file = new File('src/target/dummy/groovy/ant/AntTest.groovy')
        assert file.exists()
    }
}