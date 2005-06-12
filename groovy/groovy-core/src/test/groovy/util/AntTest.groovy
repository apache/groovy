package groovy.util

import java.io.File

class AntTest extends GroovyTestCase {
    
    void testAnt() {
        def ant = new AntBuilder()

        // lets just call one task
        ant.echo("hello")
        
        // here"s an example of a block of Ant inside GroovyMarkup
        ant.sequential {
            echo("inside sequential")
            
            def myDir = "target/AntTest/"
            
            mkdir(dir:myDir) 
            copy(todir:myDir) {
                fileset(dir:"src/test") {
                    include(name:"**/*.groovy")
                }
            }
            
            echo("done")
        }
        
        // now lets do some normal Groovy again
        def file = new File("target/AntTest/groovy/util/AntTest.groovy")
        assert file.exists()
    }
    
    void testFileIteration() {
        def ant = new AntBuilder()
        
        // lets create a scanner of filesets
        def scanner = ant.fileScanner {
            fileset(dir:"src/test") {
                include(name:"**/Ant*.groovy")
            }
        }
        
        // now lets iterate over 
        def found = false
        for (f in scanner) {
            println("Found file ${f}")
            
            found = true
            
            assert f instanceof File
            assert f.name.endsWith(".groovy")
        }
        assert found
    }
    
    void testJunitTask() {
        def ant = new AntBuilder()
        
        ant.junit {
            test(name:'groovy.util.SomethingThatDoesNotExist')
        }
    }
    
    void testPathBuilding() {
        def ant = new AntBuilder()
        
        def value = ant.path {
            fileset(dir:"xdocs") {
                include(name:"*.wiki")
            }
        }

        assert value != null

        println "Found path of type ${value.getClass().name}"
        println value
    }

    void testTaskContainerAddTaskIsCalled() {
        def ant = new AntBuilder()
        taskContainer = ant.parallel(){ // "Parallel" serves as a sample TaskContainer
            ant.echo()                  // "Echo" without message to keep tests silent
        }
        // not very elegant, but the easiest way to get the ant internals...
        assert taskContainer.dump() =~ 'nestedTasks=\\[org.apache.tools.ant.taskdefs.Echo@\\w+\\]'
    }

    void testTaskContainerExecutionSequence() {
        def ant = new AntBuilder()
        SpoofTaskContainer.getSpoof().length = 0
        def PATH = 'task.path'
        ant.path(id:PATH){ant.pathelement(location:'classes')}
        ['spoofcontainer':'SpoofTaskContainer', 'spoof':'SpoofTask'].each{ pair ->
            ant.taskdef(name:pair.key, classname:'groovy.util.'+pair.value, classpathref:PATH)
        }
        ant.spoofcontainer(){
            ant.spoof()
        }
        def expectedSpoof =
            "SpoofTaskContainer ctor\n"+
            "SpoofTask ctor\n"+
            "in addTask\n"+
            "begin SpoofTaskContainer execute\n"+
            "begin SpoofTask execute\n"+
            "end SpoofTask execute\n"+
            "end SpoofTaskContainer execute\n"
        assertEquals expectedSpoof, SpoofTaskContainer.getSpoof().toString()
    }

    
}
