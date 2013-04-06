package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor

/**
 * Created with IntelliJ IDEA.
 * User: kruset
 * Date: 4/6/13
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class CompletorTestSupport extends GroovyTestCase {


    IO testio
    BufferedOutputStream mockOut
    BufferedOutputStream mockErr
    MockFor groovyshMocker

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
        groovyshMocker = new MockFor(Groovysh)
        groovyshMocker.demand.createDefaultRegistrar() {{shell -> null}}
        groovyshMocker.demand.getClass() { Groovysh.class }

        for (i in 1..19) {
            groovyshMocker.demand.getIo(0..1) {testio}
            groovyshMocker.demand.leftShift(0..1) {}
            groovyshMocker.demand.getIo(0..1) {testio}
        }

    }
}
