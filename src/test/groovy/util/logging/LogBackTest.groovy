package groovy.util.logging

import java.lang.reflect.*
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.layout.EchoLayout

/**
 * @author Hamlet D'Arcy
 * @author Francesco Durbin
 * @author Tomasz Bujok
 */

class LogBackTest extends GroovyTestCase {

    LogbackInterceptingAppender appender
    Logger logger

    protected void setUp() {
        super.setUp()
        logger = LoggerFactory.getLogger("MyClass")

        appender = new LogbackInterceptingAppender()
        appender.setOutputStream(new ByteArrayOutputStream())
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory()
        appender.setContext(lc)
        appender.setName("MyAppender")
        appender.setLayout(new EchoLayout())
        appender.start()

        logger.addAppender(appender)
        logger.setLevel(Level.ALL)
    }

    protected void tearDown() {
        super.tearDown()
        logger.detachAppender(appender)
    }

    public void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.LogBack
              class MyClass {
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

        def events = appender.getEvents()
        int ind = 0
        assert events.size() == 5
        assert events[ind].level == Level.ERROR
        assert events[ind].message == "error called"
        assert events[++ind].level == Level.WARN
        assert events[ind].message == "warn called"
        assert events[++ind].level == Level.INFO
        assert events[ind].message == "info called"
        assert events[++ind].level == Level.DEBUG
        assert events[ind].message == "debug called"
        assert events[++ind].level == Level.TRACE
        assert events[ind].message == "trace called"
    }

    public void testLogGuard() {
        Class clazz = new GroovyClassLoader().parseClass('''
           @groovy.util.logging.LogBack
            class MyClass {
                def loggingMethod() {
                    log.setLevel(ch.qos.logback.classic.Level.ERROR)
                    log.trace (prepareLogMessage())
                }

                def prepareLogMessage() {
                  log.getAppender("MyAppender")?.isLogGuarded = false
                  return "formatted log message"
                }
            }
            new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        assert appender.isLogGuarded == true
    }

}

class LogbackInterceptingAppender<E> extends OutputStreamAppender<E> {

    List<LoggingEvent> events = new ArrayList<LoggingEvent>()

    boolean isLogGuarded = true

    public List<LoggingEvent> getEvents() {
        return events
    }

    protected void append(E event) {
        if (event instanceof LoggingEvent) {
            events.add(event)
        } else {
            throw new RuntimeException("Unable to intercept logging events - probably API has changed")
        }
        super.append(event)
    }

}