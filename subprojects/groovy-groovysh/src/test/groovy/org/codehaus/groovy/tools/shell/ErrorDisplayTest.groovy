package org.codehaus.groovy.tools.shell

import jline.ConsoleReader

/**
 * Created with IntelliJ IDEA.
 * User: kruset
 * Date: 4/2/13
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorDisplayTest extends ShellRunnerTestSupport {

    void testInput() {
        readerStubber.demand.readLine() { "foo" }
        shellMocker.use {
            readerStubber.use {
                Groovysh shellMock = new Groovysh()
                ConsoleReader readerStub = new ConsoleReader()

                InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, { ">" })
                shellRunner.reader = readerStub
                assertEquals("foo", shellRunner.readLine())
            }
        }
    }

    void testError() {
        readerStubber.demand.readLine() { throw new StringIndexOutOfBoundsException() }
        shellMocker.use {
            readerStubber.use {
                Groovysh shellMock = new Groovysh()
                ConsoleReader readerStub = new ConsoleReader()

                InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, { ">" })
                shellRunner.reader = readerStub
                assertEquals("", shellRunner.readLine())
            }
        }
    }

//    void testError2() {
//        readerStubber.demand.readLine() { throw new Throwable(){}}
//        shellMocker.use { readerStubber.use {
//            Groovysh shellMock = new Groovysh()
//            ConsoleReader readerStub = new ConsoleReader()
//
//            InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, {">"})
//            shellRunner.reader = readerStub
//            assertEquals("", shellRunner.readLine())
//        }}
//    }

}
