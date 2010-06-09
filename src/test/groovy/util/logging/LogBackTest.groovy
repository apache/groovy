package groovy.util.logging

import java.lang.reflect.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.ast.*
import org.codehaus.groovy.transform.*
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.classic.spi.LoggingEvent

/**
 * @author Hamlet D'Arcy
 * @author Francesco Durbin
 * @author Tomasz Bujok
 */

class LogBackTest extends GroovyTestCase {

    List<LoggingEvent> events = new ArrayList<LoggingEvent>()
    OutputStreamAppender appender = new OutputStreamAppender()

    protected void setUp() {
        super.setUp()
        Logger logger = LoggerFactory.getLogger("MyClass")
        appender.setOutputStream(new ByteArrayOutputStream())

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory()
        appender.setContext(lc)
        appender.setLayout(new LogbackInterceptingLayout(events))
//        appender.setImmediateFlush(true)
        appender.start()

        logger.addAppender(appender)
        logger.setLevel(Level.ALL)
    }

    protected void tearDown() {
        super.tearDown()
        Logger logger = LoggerFactory.getLogger("MyClass")
        logger.detachAppender(appender)
    }

    public void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.LogBack
              class MyClass {
                //private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyClass)
              } ''')

        assert clazz.declaredFields.find { Field field ->
        field.name == "log" &&
                Modifier.isPrivate(field.getModifiers()) &&
                Modifier.isStatic(field.getModifiers()) &&
                Modifier.isTransient(field.getModifiers()) &&
                Modifier.isFinal(field.getModifiers())
        }
    }

    public void testClassAlreadyHasLogField() {
  
        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.LogBack
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    public void testLogInfo() {

        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.LogBack
            class MyClass {
                
                def loggingMethod() {
                    log.error ("error called")
                    log.warn  ("warn called")
                    log.info  ("info called")
                    log.debug ("debug called")
                    log.trace ("trace called")
                }               
            }
            new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        assert events.size() == 5
        assert events[0].level == Level.ERROR
        assert events[0].message == "error called"
        assert events[1].level == Level.WARN
        assert events[1].message == "warn called"
        assert events[2].level == Level.INFO
        assert events[2].message == "info called"
        assert events[3].level == Level.DEBUG
        assert events[3].message == "debug called"
        assert events[4].level == Level.TRACE
        assert events[4].message == "trace called"
    }
}

class LogbackInterceptingLayout<E> extends LayoutBase<E> {

    List<LoggingEvent> events = new ArrayList<LoggingEvent>()

    public LogbackInterceptingLayout(List<E> events) {
        this.events = events
    }

    public String doLayout(E event) {
        if (event instanceof LoggingEvent) {
            events.add(event)
        } else {
            throw new RuntimeException("Unable to intercept logging events - probably API has changed")
        }
        event
    }
}


