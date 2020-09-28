/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.ant

import groovy.test.GroovyTestCase
import groovy.xml.NamespaceBuilder
import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper
import org.apache.tools.ant.UnknownElement
import org.junit.Assert

/**
 * Tests for the <groovy> task.
 */
class AntTest extends GroovyTestCase {

    void testAnt() {
        def ant = new AntBuilder()
        // let's just call one task
        ant.echo('hello')
        // here's an example of a block of Ant inside GroovyMarkup
        ant.sequential {
            echo('inside sequential')
            def myDir = 'build/AntTest/'
            mkdir(dir: myDir)
            copy(todir: myDir) {
                fileset(dir: 'src/test/groovy') {
                    include(name: '**/*.groovy')
                }
            }
            echo('done')
        }
        // now let's do some normal Groovy again
        def file = new File('build/AntTest/groovy/ant/AntTest.groovy')
        assert file.exists()
    }

    void testFileIteration() {
        def ant = new AntBuilder()
        // let's create a scanner of filesets
        def scanner = ant.fileScanner {
            fileset(dir: 'src/test/groovy') {
                include(name: '**/Ant*.groovy')
            }
        }
        // now let's iterate over
        def found = false
        for (f in scanner) {
            println("Found file ${f}")
            found = true
            assert f instanceof File
            assert f.name.endsWith('.groovy')
        }
        assert found
    }

    void testPathBuilding() {
        def ant = new AntBuilder()
        def value = ant.path {
            fileset(dir: 'xdocs') {
                include(name: '*.wiki')
            }
        }
        assert value != null
        assertEquals org.apache.tools.ant.types.Path, value.getClass()
    }

    void testTaskContainerExecutionSequence() {
        SpoofTaskContainer.getSpoof().length = 0
        def antFile = new File('src/test-resources/groovy/ant/AntTest.xml')
        assertTrue "Couldn't find ant test script", antFile.exists()
        // run it with ant, to be sure that our assumptions are correct
        def project = new Project()
        project.init()
        ProjectHelper.projectHelper.parse(project, antFile)
        project.executeTarget(project.defaultTarget)

        def expectedSpoof =
            '''SpoofTaskContainer ctor
in addTask
configuring UnknownElement
SpoofTask ctor
begin SpoofTaskContainer execute
begin SpoofTask execute
tag name from wrapper: spoof
attributes map from wrapper: [foo:123]
param foo: 123
end SpoofTask execute
end SpoofTaskContainer execute
'''
        println SpoofTaskContainer.getSpoof().toString()
        assertEquals expectedSpoof, SpoofTaskContainer.getSpoof().toString()
        SpoofTaskContainer.spoof.length = 0

        def ant = new AntBuilder()
        def PATH = 'task.path'

        // and now run it with the AntBuilder        
        ant.path(id: PATH) { ant.pathelement(location: 'classes') }
        ['spoofcontainer': SpoofTaskContainer, 'spoof': SpoofTask].each { pair ->
            ant.taskdef(name: pair.key, classname: pair.value.name, classpathref: PATH)
        }
        ant.spoofcontainer {
            ant.spoof(foo: 123)
        }
        assertEquals expectedSpoof, SpoofTaskContainer.getSpoof().toString()

        // now run it with AntBuilder using Namespaces (test for GROOVY-1070)
        def antNS = new AntBuilder()
        SpoofTaskContainer.resetSpoof()

        // and now run it with the AntBuilder
        antNS.path(id: PATH) {antNS.pathelement(location: 'classes')}
        ['spoofcontainer': SpoofTaskContainer, 'spoof': SpoofTask].each { pair ->
            antNS.taskdef(name: pair.key, classname: pair.value.name, classpathref: PATH,
                    uri: 'testNS')
        }
        def testNS = NamespaceBuilder.newInstance(antNS, 'testNS', 'testNSprefix')
        testNS.spoofcontainer {
            testNS.spoof(foo: 123)
        }
        assertEquals expectedSpoof, SpoofTaskContainer.getSpoof().toString()
    }

    /** Checks that we can access dynamically (through Ant's property task) defined properties in Groovy scriptlets  */
    void testDynamicProperties() {
        def antBuilder = new AntBuilder()
        antBuilder.property(name: 'testProp1', value: 'TEST 1')
        antBuilder.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
        antBuilder.groovy('''
            ant.property(name: 'testProp2', value: 'TEST 2')
            assert properties.testProp1 == project.properties.testProp1
            assert properties.testProp2 == project.properties.testProp2
        ''')
    }

    /**
     * Test access to AntBuilder properties
     */
    void testAntBuilderProperties() {
        def ant = new AntBuilder()
        assertNull ant.project.properties.'myProp'
        ant.property(name: 'myProp', value: 'blabla')
        assert ant.project.properties.'myProp' == 'blabla'
    }

    /**
     * Tests that the AntBuilder can handle conditions (conditions aren't tasks)
     * (test for GROOVY-824)
     */
    void testCondition() {
        def ant = new AntBuilder()
        ant.condition(property: 'containsHi') {
            contains([string: 'hi', substring: 'hi'])
        }
        assert ant.project.properties['containsHi'] == 'true'
        ant.condition(property: 'equalsHi', else: 'false') {
            Equals([arg1: 'hi', arg2: 'bye'])
        }
        assert ant.project.properties['equalsHi'] == 'false'
    }

    /**
     * Tests that using the AntBuilder within the <groovy> task doesn't cause double execution
     * (test for GROOVY-1602)
     */
    void testAntBuilderWithinGroovyTask() {
        def antFile = new File('src/test-resources/groovy/ant/AntTest.xml')
        assertTrue "Couldn't find ant test script", antFile.exists()

        def project = new Project()
        project.init()
        ProjectHelper.projectHelper.parse(project, antFile)

        def customListener = new SimpleListener()
        project.addBuildListener customListener

        project.executeTarget('testAntBuilderWithinGroovyTask')

        def expectedSpoof =
            '''started: taskdef[name:groovy, classname:org.codehaus.groovy.ant.Groovy]
finished: taskdef[name:groovy, classname:org.codehaus.groovy.ant.Groovy]
started: echo[message:before groovy task]
finished: echo[message:before groovy task]
started: groovy[:]
started: echo[message:ant builder within groovy task]
finished: echo[message:ant builder within groovy task]
finished: groovy[:]
started: echo[message:after groovy task]
finished: echo[message:after groovy task]
'''

        assertEquals expectedSpoof, customListener.spoof.toString()
    }

    /**
     * Test usage of import
     */
    void testImport() {
        def antFile = new File('src/test-resources/groovy/ant/AntTest_import.xml')
        assertTrue "Couldn't find ant test script", antFile.exists()

        def ant = new AntBuilder()
        def customListener = new SimpleListener()
        ant.project.addBuildListener customListener

        ant.'import'(file: antFile.absolutePath)
        def expectedSpoof = """\
started: import[file:${antFile.absolutePath}]
started: echo[message:outside targets, at the top]
finished: echo[message:outside targets, at the top]
finished: import[file:${antFile.absolutePath}]
"""
        assertEquals expectedSpoof, customListener.spoof.toString()

        customListener.spoof.length = 0
        ant.project.executeTarget('firstTarget')
        expectedSpoof = '''\
started: echo[message:inside firstTarget]
finished: echo[message:inside firstTarget]
'''
        assertEquals expectedSpoof, customListener.spoof.toString()

        customListener.spoof.length = 0
        ant.target(name: 'myTestTarget', depends: '2ndTarget') {
            echo(message: "echo from AntBuilder's target foo")
        }
        expectedSpoof = '''\
started: echo[message:inside 2ndTarget]
finished: echo[message:inside 2ndTarget]
started: echo[message:echo from AntBuilder's target foo]
finished: echo[message:echo from AntBuilder's target foo]
'''
        assertEquals expectedSpoof, customListener.spoof.toString()

        // test that the previously created target can be called
        customListener.spoof.length = 0
        ant.project.executeTarget('myTestTarget')
        assertEquals expectedSpoof, customListener.spoof.toString()
    }

    void testDefineTarget_groovy2900() {
        def ant = new AntBuilder()
        def project = ant.project
        def customListener = new SimpleListener()
        project.addBuildListener customListener
        ant.defineTarget(name: 'myTestTarget') {
            echo(message: "myTestTarget")
        }
        ant.defineTarget(name: 'myTestTarget2', depends: 'myTestTarget') {
            echo(message: "myTestTarget2")
        }
        ant.target(name: 'myTestTarget3', depends: 'myTestTarget') {
            ant.defineTarget(name: 'myTestTarget4') {
                echo(message: "myTestTarget4")
            }
            echo(message: "myTestTarget3")
        }
        ant.defineTarget(name: 'myTestTarget5', depends: 'myTestTarget4') {
            echo(message: "myTestTarget5 should not appear")
        }
        project.executeTarget('myTestTarget2')
        project.executeTarget('myTestTarget4')
        assert customListener.spoof.toString() == '''\
started: echo[message:myTestTarget]
finished: echo[message:myTestTarget]
started: echo[message:myTestTarget3]
finished: echo[message:myTestTarget3]
started: echo[message:myTestTarget]
finished: echo[message:myTestTarget]
started: echo[message:myTestTarget2]
finished: echo[message:myTestTarget2]
started: echo[message:myTestTarget4]
finished: echo[message:myTestTarget4]
'''
    }
}

class SimpleListener extends org.apache.tools.ant.DefaultLogger {
    def spoof = new StringBuffer()

    void taskStarted(BuildEvent event) {
        if (!(event.task instanceof UnknownElement)) Assert.fail("Task is already configured. Listeners won't have a chance to alter UnknownElement for additional configuration");
        spoof << "started: " + logTask(event.task) + "\n"
    }

    void taskFinished(BuildEvent event) {
        spoof << "finished: " + logTask(event.task) + "\n"
    }

    private String logTask(task) {
        task.taskName + task.wrapper.attributeMap
    }
}
