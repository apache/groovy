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

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.LogManager
import org.apache.log4j.varia.NullAppender
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class LogImprovementsASTTransformsTest {

    @Test
    void testLogASTTransformation() {
        assertScript '''
// tag::log_spec[]
@groovy.util.logging.Log
class Greeter {
    void greet() {
        log.info 'Called greeter'
        println 'Hello, world!'
    }
}
// end::log_spec[]
def g = new Greeter()
g.greet()
        '''
        assertScript '''
// tag::log_equiv[]
import java.util.logging.Level
import java.util.logging.Logger

class Greeter {
    private static final Logger log = Logger.getLogger(Greeter.name)
    void greet() {
        if (log.isLoggable(Level.INFO)) {
            log.info 'Called greeter'
        }
        println 'Hello, world!'
    }
}
// end::log_equiv[]
def g = new Greeter()
g.greet()

'''
    }

    @Test
    void testCommonsASTTransformation() {
        assertScript '''
// tag::commons_spec[]
@groovy.util.logging.Commons
class Greeter {
    void greet() {
        log.debug 'Called greeter'
        println 'Hello, world!'
    }
}
// end::commons_spec[]
def g = new Greeter()
g.greet()
        '''
        assertScript '''
// tag::commons_equiv[]
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

class Greeter {
    private static final Log log = LogFactory.getLog(Greeter)
    void greet() {
        if (log.isDebugEnabled()) {
            log.debug 'Called greeter'
        }
        println 'Hello, world!'
    }
}
// end::commons_equiv[]
def g = new Greeter()
g.greet()
'''
    }

    @Test
    void testLog4jASTTransformation() {
        LogManager.resetConfiguration()
        BasicConfigurator.configure(new NullAppender())
        try {
            assertScript '''
// tag::log4j_spec[]
@groovy.util.logging.Log4j
class Greeter {
    void greet() {
        log.debug 'Called greeter'
        println 'Hello, world!'
    }
}
// end::log4j_spec[]
def g = new Greeter()
g.greet()
        '''
            assertScript '''
// tag::log4j_equiv[]
import org.apache.log4j.Logger

class Greeter {
    private static final Logger log = Logger.getLogger(Greeter)
    void greet() {
        if (log.isDebugEnabled()) {
            log.debug 'Called greeter'
        }
        println 'Hello, world!'
    }
}
// end::log4j_equiv[]
def g = new Greeter()
g.greet()
'''
        } finally {
            LogManager.resetConfiguration()
        }
    }

    @Test
    void testLog4j2ASTTransformation() {
                assertScript '''
    // tag::log4j2_spec[]
    @groovy.util.logging.Log4j2
    class Greeter {
        void greet() {
            log.debug 'Called greeter'
            println 'Hello, world!'
        }
    }
    // end::log4j2_spec[]
    def g = new Greeter()
    g.greet()
            '''

        assertScript '''
    // tag::log4j2_equiv[]
    import org.apache.logging.log4j.LogManager
    import org.apache.logging.log4j.Logger

    class Greeter {
        private static final Logger log = LogManager.getLogger(Greeter)
        void greet() {
            if (log.isDebugEnabled()) {
                log.debug 'Called greeter'
            }
            println 'Hello, world!'
        }
    }
    // end::log4j2_equiv[]
    def g = new Greeter()
    g.greet()
    '''
    }

    @Test
    void testSlf4jASTTransformation() {
        assertScript '''
// tag::slf4j_spec[]
@groovy.util.logging.Slf4j
class Greeter {
    void greet() {
        log.debug 'Called greeter'
        println 'Hello, world!'
    }
}
// end::slf4j_spec[]
def g = new Greeter()
g.greet()
        '''

        assertScript '''
// tag::slf4j_equiv[]
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class Greeter {
    private static final Logger log = LoggerFactory.getLogger(Greeter)
    void greet() {
        if (log.isDebugEnabled()) {
            log.debug 'Called greeter'
        }
        println 'Hello, world!'
    }
}
// end::slf4j_equiv[]
def g = new Greeter()
g.greet()
'''
    }

    @Test
    void testPlatformLogASTTransformation() {
        assertScript '''
// tag::platformlog_spec[]
@groovy.util.logging.PlatformLog
class Greeter {
    void greet() {
        log.info 'Called greeter'
        println 'Hello, world!'
    }
}
// end::platformlog_spec[]
def g = new Greeter()
g.greet()
        '''

        assertScript '''
// tag::platformlog_equiv[]
import java.lang.System.Logger
import java.lang.System.LoggerFinder
import static java.lang.System.Logger.Level.INFO

class Greeter {
    private static final transient Logger log =
        LoggerFinder.loggerFinder.getLogger(Greeter.class.name, Greeter.class.module)
    void greet() {
        log.log INFO, 'Called greeter'
        println 'Hello, world!'
    }
}
// end::platformlog_equiv[]
def g = new Greeter()
g.greet()
'''
    }
}
