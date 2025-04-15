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
package groovy.execute

import groovy.test.GroovyTestCase
/**
 *  Cross platform tests for the DGM#execute() family of methods.
 */
final class ExecuteTest extends GroovyTestCase {

    private String getCmd() {
        def cmd = "ls -l"
        if (System.properties.'os.name'.startsWith('Windows ')) {
            cmd = "cmd /c dir"
        }
        return cmd
    }

    void testExecuteCommandLineProcessUsingAString() {
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmd.execute()
        process.waitForProcessOutput sbout, sberr
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessUsingAStringArray() {
        def cmdArray = cmd.split(' ')
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmdArray.execute()
        process.waitForProcessOutput sbout, sberr
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessUsingAList() {
        List<String> cmdList = Arrays.asList(cmd.split(' '))
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmdList.execute()
        process.waitForProcessOutput sbout, sberr
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessAndUseWaitForOrKill() {
        List<String> javaArgs = [System.getProperty('java.home') + "/bin/java",
                "-classpath",
                System.getProperty('java.class.path'),
                "groovy.ui.GroovyMain",
                "-e",
                "sleep(2000); println('Done'); System.exit(0)"]

        String[] java = javaArgs.toArray()
        println "Executing this command for two cases:\n${java.join(' ')}"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = java.execute()
        def tout = process.consumeProcessOutputStream(sbout)
        def terr = process.consumeProcessErrorStream(sberr)
        process.waitForOrKill(60000)
        tout.join()
        terr.join()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
//        assert sbout.toString().contains('Done'), "Expected 'Done' but found: " + sbout.toString() + " with error: " + sberr.toString()
        assert value == 0

        sbout = new StringBuffer()
        sberr = new StringBuffer()
        process = java.execute()
        process.pipeTo(process)
        tout = process.consumeProcessOutputStream(sbout)
        terr = process.consumeProcessErrorStream(sberr)
        process.waitForOrKill(500)
        tout.join()
        terr.join()
        value = process.exitValue()
        count = sbout.toString().readLines().size()
        assert !sbout.toString().contains('Done')
        assert value != 0 // should have been killed
    }

    void testExecuteCommandLineUnderWorkingDirectory() {
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmd.execute(null, new File('..'))
        process.waitForProcessOutput sbout, sberr
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineWithEnvironmentProperties() {
        List<String> java = [
                System.getProperty('java.home') + '/bin/java',
                '-classpath',
                System.getProperty('java.class.path'),
                'groovy.ui.GroovyMain',
                '-e',
                "println(System.getenv('foo'))"
        ]

        println "Executing this command:\n${java.join(' ')}"
        def process = java.execute(['foo=bar'], null)
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.waitForProcessOutput(out, err)

        assert out.toString().contains('bar')
        assert process.exitValue() == 0
    }

    // GROOVY-9392
    void testExecuteCommandLineProcessWithGroovySystemClassLoader() {
        List<String> java = [
                System.getProperty('java.home') + '/bin/java',
                '-classpath',
                System.getProperty('java.class.path'),
                '-Djava.system.class.loader=groovy.lang.GroovyClassLoader',
                'groovy.ui.GroovyMain',
                '-e',
                "println('hello')"
        ]

        println "Executing this command:\n${java.join(' ')}"
        def process = java.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.waitForProcessOutput(out, err)

        // Run on JDK21ea, we got the following warning:
        // '[0.054s][warning][cds] Archived non-system classes are disabled because the java.system.class.loader property is specified (value = "groovy.lang.GroovyClassLoader"). To use archived non-system classes, this property must not be set\nhello\n'
        assert out.toString().contains('hello')
        assert process.exitValue() == 0
    }
}
