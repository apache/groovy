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

import org.codehaus.groovy.runtime.ProcessResult
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *  Cross platform tests for the DGM#execute() family of methods.
 */
final class ExecuteTest {

    private String getCmd() {
        def cmd = "ls -l"
        if (System.properties.'os.name'.startsWith('Windows ')) {
            cmd = "cmd /c dir"
        }
        return cmd
    }

    private String getEchoCmd() {
        if (System.properties.'os.name'.startsWith('Windows ')) {
            return "cmd /c echo"
        }
        return "echo"
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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
    @Test
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

    // --- toProcessBuilder ---

    @Test
    void testToProcessBuilderFromString() {
        def pb = cmd.toProcessBuilder()
        assert pb instanceof ProcessBuilder
        def process = pb.start()
        process.waitForProcessOutput()
        assert process.exitValue() == 0
    }

    @Test
    void testToProcessBuilderFromArray() {
        def pb = cmd.split(' ').toProcessBuilder()
        assert pb instanceof ProcessBuilder
        def process = pb.start()
        process.waitForProcessOutput()
        assert process.exitValue() == 0
    }

    @Test
    void testToProcessBuilderFromList() {
        def pb = Arrays.asList(cmd.split(' ')).toProcessBuilder()
        assert pb instanceof ProcessBuilder
        def process = pb.start()
        process.waitForProcessOutput()
        assert process.exitValue() == 0
    }

    // --- waitForResult ---

    @Test
    void testWaitForResultFromString() {
        ProcessResult result = "$echoCmd hello".execute().waitForResult()
        assert result.ok
        assert result.exitCode == 0
        assert result.out.trim().contains('hello')
        assert result.err.isEmpty()
    }

    @Test
    void testWaitForResultFromArray() {
        ProcessResult result = (echoCmd.split(' ') + 'hello').execute().waitForResult()
        assert result.ok
        assert result.out.trim().contains('hello')
    }

    @Test
    void testWaitForResultFromList() {
        ProcessResult result = ([*echoCmd.split(' '), 'hello'] as List).execute().waitForResult()
        assert result.ok
        assert result.out.trim().contains('hello')
    }

    @Test
    void testWaitForResultWithTimeout() {
        ProcessResult result = "$echoCmd fast".execute().waitForResult(30, TimeUnit.SECONDS)
        assert result.ok
        assert result.out.trim().contains('fast')
    }

    @Test
    void testWaitForResultToString() {
        ProcessResult result = "$echoCmd test".execute().waitForResult()
        def str = result.toString()
        assert str.startsWith('ProcessResult(exitCode=')
        assert str.contains('out=')
        assert str.contains('err=')
    }

    // --- execute with Map options ---

    @Test
    void testExecuteWithDirOption() {
        def tmpDir = File.createTempDir()
        try {
            ProcessResult result = cmd.execute(dir: tmpDir).waitForResult()
            assert result.ok
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithDirOptionAsString() {
        def tmpDir = File.createTempDir()
        try {
            ProcessResult result = cmd.execute(dir: tmpDir.absolutePath).waitForResult()
            assert result.ok
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithEnvOption() {
        List<String> java = [
                System.getProperty('java.home') + '/bin/java',
                '-classpath',
                System.getProperty('java.class.path'),
                'groovy.ui.GroovyMain',
                '-e',
                "println(System.getenv('MY_TEST_VAR'))"
        ]
        ProcessResult result = java.execute(env: [MY_TEST_VAR: 'groovy_test']).waitForResult()
        assert result.ok
        assert result.out.trim().contains('groovy_test')
    }

    @Test
    void testExecuteWithRedirectErrorStream() {
        List<String> java = [
                System.getProperty('java.home') + '/bin/java',
                '-classpath',
                System.getProperty('java.class.path'),
                'groovy.ui.GroovyMain',
                '-e',
                "System.err.println('errMsg'); println('outMsg')"
        ]
        ProcessResult result = java.execute(redirectErrorStream: true).waitForResult()
        assert result.out.contains('errMsg')
        assert result.out.contains('outMsg')
    }

    @Test
    void testExecuteMapFromArray() {
        def tmpDir = File.createTempDir()
        try {
            ProcessResult result = cmd.split(' ').execute(dir: tmpDir).waitForResult()
            assert result.ok
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithOutputFile() {
        def tmpDir = File.createTempDir()
        try {
            def outFile = new File(tmpDir, 'output.txt')
            def process = "$echoCmd captured".execute(outputFile: outFile)
            process.waitFor()
            assert outFile.text.trim().contains('captured')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithInputFile() {
        def tmpDir = File.createTempDir()
        try {
            def inFile = new File(tmpDir, 'input.txt')
            inFile.text = 'hello from file'
            def catCmd = System.properties.'os.name'.startsWith('Windows ')
                    ? ['cmd', '/c', 'more']
                    : ['cat']
            def result = catCmd.execute(inputFile: inFile).waitForResult()
            assert result.ok
            assert result.out.contains('hello from file')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithDirOptionAsPath() {
        def tmpDir = File.createTempDir()
        try {
            ProcessResult result = cmd.execute(dir: tmpDir.toPath()).waitForResult()
            assert result.ok
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithOutputFileAsPath() {
        def tmpDir = File.createTempDir()
        try {
            def outPath = tmpDir.toPath().resolve('output.txt')
            def process = "$echoCmd captured".execute(outputFile: outPath)
            process.waitFor()
            assert outPath.text.trim().contains('captured')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithInputFileAsPath() {
        def tmpDir = File.createTempDir()
        try {
            def inFile = Path.of(tmpDir.absolutePath, 'input.txt')
            inFile.text = 'hello from path'
            def catCmd = System.properties.'os.name'.startsWith('Windows ')
                    ? ['cmd', '/c', 'more']
                    : ['cat']
            def result = catCmd.execute(inputFile: inFile).waitForResult()
            assert result.ok
            assert result.out.contains('hello from path')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithAppendOutput() {
        def tmpDir = File.createTempDir()
        try {
            def outFile = new File(tmpDir, 'append.txt')
            outFile.text = 'existing\n'
            "$echoCmd appended".execute(appendOutput: outFile).waitFor()
            def content = outFile.text
            assert content.contains('existing')
            assert content.contains('appended')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithAppendErrorAsPath() {
        def tmpDir = File.createTempDir()
        try {
            def errPath = tmpDir.toPath().resolve('errors.txt')
            errPath.text = ''
            List<String> java = [
                    System.getProperty('java.home') + '/bin/java',
                    '-classpath',
                    System.getProperty('java.class.path'),
                    'groovy.ui.GroovyMain',
                    '-e',
                    "System.err.println('errMsg')"
            ]
            java.execute(appendError: errPath).waitFor()
            assert errPath.text.contains('errMsg')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithErrorFile() {
        def tmpDir = File.createTempDir()
        try {
            def errFile = new File(tmpDir, 'stderr.txt')
            List<String> java = [
                    System.getProperty('java.home') + '/bin/java',
                    '-classpath',
                    System.getProperty('java.class.path'),
                    'groovy.ui.GroovyMain',
                    '-e',
                    "System.err.println('error output')"
            ]
            java.execute(errorFile: errFile).waitFor()
            assert errFile.text.contains('error output')
        } finally {
            tmpDir.deleteDir()
        }
    }

    @Test
    void testExecuteWithOutputFileAsString() {
        def tmpDir = File.createTempDir()
        try {
            def outPath = new File(tmpDir, 'output.txt').absolutePath
            def process = "$echoCmd stringpath".execute(outputFile: outPath)
            process.waitFor()
            assert new File(outPath).text.trim().contains('stringpath')
        } finally {
            tmpDir.deleteDir()
        }
    }

    // --- pipeline ---

    @Test
    void testPipelineWithStrings() {
        if (System.properties.'os.name'.startsWith('Windows ')) return
        def procs = ["echo one two three", "wc -w"].pipeline()
        assert procs.size() == 2
        def result = procs.last().waitForResult()
        assert result.ok
        assert result.out.trim() == '3'
    }

    @Test
    void testPipelineWithLists() {
        if (System.properties.'os.name'.startsWith('Windows ')) return
        def procs = [["echo", "alpha beta"], ["wc", "-w"]].pipeline()
        def result = procs.last().waitForResult()
        assert result.ok
        assert result.out.trim() == '2'
    }

    @Test
    void testPipelineWithProcessBuilders() {
        if (System.properties.'os.name'.startsWith('Windows ')) return
        def pb1 = "echo hello world".toProcessBuilder()
        def pb2 = ["wc", "-w"].toProcessBuilder()
        def procs = [pb1, pb2].pipeline()
        def result = procs.last().waitForResult()
        assert result.ok
        assert result.out.trim() == '2'
    }

    @Test
    void testPipelineWithMixedTypes() {
        if (System.properties.'os.name'.startsWith('Windows ')) return
        def procs = ["echo one two three", ["wc", "-w"]].pipeline()
        def result = procs.last().waitForResult()
        assert result.ok
        assert result.out.trim() == '3'
    }

    // --- onExit ---

    @Test
    void testOnExitCallback() {
        def latch = new CountDownLatch(1)
        def capturedCode = -1
        def process = "$echoCmd done".execute().onExit { proc ->
            capturedCode = proc.exitValue()
            latch.countDown()
        }
        assert process instanceof Process
        latch.await(10, TimeUnit.SECONDS)
        assert capturedCode == 0
    }
}
