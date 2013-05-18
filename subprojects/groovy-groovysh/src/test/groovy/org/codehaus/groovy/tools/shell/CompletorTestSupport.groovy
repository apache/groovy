package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.tools.shell.completion.IdentifierCompletor
import org.codehaus.groovy.tools.shell.completion.ReflectionCompletor

/**
 * Created with IntelliJ IDEA.
 * User: kruset
 * Date: 4/6/13
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class CompletorTestSupport extends GroovyTestCase {

    BufferManager bufferManager = new BufferManager()
    IO testio
    BufferedOutputStream mockOut
    BufferedOutputStream mockErr
    MockFor groovyshMocker
    MockFor reflectionCompletorMocker

    MockFor idCompletorMocker

    void setUp() {
        super.setUp()
        mockOut = new BufferedOutputStream(
                new ByteArrayOutputStream());

        mockErr = new BufferedOutputStream(
                new ByteArrayOutputStream());

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        reflectionCompletorMocker = new MockFor(ReflectionCompletor)

        idCompletorMocker = new MockFor(IdentifierCompletor)

        groovyshMocker = new MockFor(Groovysh)
        groovyshMocker.demand.createDefaultRegistrar() {{shell -> null}}
        groovyshMocker.demand.getClass() { Groovysh.class }
        groovyshMocker.demand.getRegistry(0..1) { new CommandRegistry() }
        for (i in 1..19) {
            groovyshMocker.demand.getIo(0..1) {testio}
            groovyshMocker.demand.leftShift(0..1) {}
            groovyshMocker.demand.getIo(0..1) {testio}
        }
        groovyshMocker.demand.getBuffers(0..1) {bufferManager}

    }
}
