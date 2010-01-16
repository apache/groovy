/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.execute

/**
 *  Cross platform tests for the DGM#execute() family of methods.
 *
 *  @author Paul King
 */
class ExecuteTest extends GroovyTestCase {
    private String getCmd() {
        def cmd = "ls -l"
        if (System.properties.'os.name'.startsWith('Windows ')) {
            cmd = "cmd /c dir"
        }
        return cmd
    }

    void testExecuteCommandLineProcessUsingAString() {
        println "Executing String command: $cmd"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmd.execute()
        process.waitForProcessOutput sbout, sberr
        process.waitFor()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessUsingAStringArray() {
        def cmdArray = cmd.split(' ')
        println "Executing String[] command: $cmdArray"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmdArray.execute()
        process.waitForProcessOutput sbout, sberr
        process.waitFor()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessUsingAList() {
        List<String> cmdList = Arrays.asList(cmd.split(' '))
        println "Executing List<String> command: $cmdList"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmdList.execute()
        process.waitForProcessOutput sbout, sberr
        process.waitFor()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineProcessAndUseWaitForOrKill() {
        def cp = System.getProperty('java.class.path')
        def java = System.getProperty('java.home') + """/bin/java -version"""
        println "Executing this command for two cases:\n$java"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = java.execute()
        def tout = process.consumeProcessOutputStream(sbout)
        def terr = process.consumeProcessErrorStream(sberr)
        process.waitForOrKill(5000)
        tout.join()
        terr.join()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Heaps of time case: Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        // written to stderr normally but cater for any JVM by looking at both
        assert (sberr.toString() + sbout.toString()).toLowerCase().contains('java')
        assert value == 0

        sbout = new StringBuffer()
        sberr = new StringBuffer()
        process = java.execute()
        tout = process.consumeProcessOutputStream(sbout)
        terr = process.consumeProcessErrorStream(sberr)
        process.waitForOrKill(20)
        tout.join()
        terr.join()
        value = process.exitValue()
        count = sbout.toString().readLines().size()
        println "Insufficient time case: Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert value != 0 // should have been killed
    }

    void testExecuteCommandLineUnderWorkingDirectory() {
        println "Executing command in dir '..': $cmd"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = cmd.execute(null, new File('..'))
        process.waitForProcessOutput sbout, sberr
        process.waitFor()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert count > 1
        assert value == 0
    }

    void testExecuteCommandLineWithEnvironmentProperties() {
        def cp = System.getProperty('java.class.path')
        def java = System.getProperty('java.home') + """/bin/java -classpath $cp groovy.ui.GroovyMain -e "println(System.getenv('foo'))" """
        println "Executing this command:\n$java"
        def props = ["foo=bar"]
        println "With these props: $props"
        StringBuffer sbout = new StringBuffer()
        StringBuffer sberr = new StringBuffer()
        def process = java.execute(props, null)
        process.waitForProcessOutput sbout, sberr
        process.waitFor()
        def value = process.exitValue()
        int count = sbout.toString().readLines().size()
        println "Exit value: $value, Err lines: ${sberr.toString().readLines().size()}, Out lines: $count"
        assert sbout.toString().contains('bar')
        assert value == 0
    }

}