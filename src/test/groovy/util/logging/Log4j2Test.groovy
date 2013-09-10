package groovy.util.logging

import java.lang.reflect.Field
import java.lang.reflect.Modifier

import org.apache.log4j.spi.Filter
import org.apache.log4j.spi.LoggingEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.layout.PatternLayout

class Log4j2Test extends GroovyTestCase {

    // Log4j2 requires at least Java 1.6
    static final boolean javaVersionGreaterThan1_5 = true
    static {
        if (System.getProperty("java.version").startsWith("1.5.")) {
            javaVersionGreaterThan1_5 = false
        }
    }

    Log4j2InterceptingAppender appender
    Logger logger

    protected void setUp() {
        super.setUp()
        if(javaVersionGreaterThan1_5) {
            PatternLayout layout = PatternLayout.createLayout("%m", null, null, "UTF-8", "false")
            appender = new Log4j2InterceptingAppender('MyAppender', null, layout)
            logger = LogManager.getLogger('MyClass')
            logger.addAppender(appender)
            logger.setLevel(Level.ALL)
        }
    }

    protected void tearDown() {
        super.tearDown()
        if(javaVersionGreaterThan1_5) {
            logger.removeAppender(appender)
        }
    }

    void testPrivateFinalStaticLogFieldAppears() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Log4j2
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
    }

    void testClassAlreadyHasLogField() {
        if(javaVersionGreaterThan1_5) {
            shouldFail(RuntimeException) {
                Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j2()
                class MyClass {
                    String log
                } ''')

                assert clazz.newInstance()
            }
        }
    }

    void testClassAlreadyHasNamedLogField() {
        if(javaVersionGreaterThan1_5) {
            shouldFail(RuntimeException) {
                Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j2('logger')
                class MyClass {
                    String logger
                } ''')

                assert clazz.newInstance()
            }
        }
    }

    void testLogInfo() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
            class MyClass {

                def loggingMethod() {
                    log.fatal ("fatal called")
                    log.error ("error called")
                    log.warn  ("warn called")
                    log.info  ("info called")
                    log.debug ("debug called")
                    log.trace ("trace called")
                }
            }
            new MyClass().loggingMethod() ''')

            clazz.newInstance().run()

            int ind = 0
            def events = appender.getEvents()
            assert events.size() == 6
            assert events[ind].level == Level.FATAL
            assert events[ind].message.message == "fatal called"
            assert events[++ind].level == Level.ERROR
            assert events[ind].message.message == "error called"
            assert events[++ind].level == Level.WARN
            assert events[ind].message.message == "warn called"
            assert events[++ind].level == Level.INFO
            assert events[ind].message.message == "info called"
            assert events[++ind].level == Level.DEBUG
            assert events[ind].message.message == "debug called"
            assert events[++ind].level == Level.TRACE
            assert events[ind].message.message == "trace called"
        }
    }

    void testLogFromStaticMethods() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log4j2
            class MyClass {
                static loggingMethod() {
                  log.info   ("(static) info called")
                }
            }
            MyClass.loggingMethod()""")

            clazz.newInstance().run()

            def events = appender.getEvents()
            assert events.size() == 1
            assert events[0].level == Level.INFO
            assert events[0].message.message == "(static) info called"
        }
    }

    void testLogInfoForNamedLogger() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2('logger')
            class MyClass {

                def loggingMethod() {
                    logger.fatal ("fatal called")
                    logger.error ("error called")
                    logger.warn  ("warn called")
                    logger.info  ("info called")
                    logger.debug ("debug called")
                    logger.trace ("trace called")
                }
            }
            new MyClass().loggingMethod() ''')

            clazz.newInstance().run()

            int ind = 0
            def events = appender.getEvents()
            assert events.size() == 6
            assert events[ind].level == Level.FATAL
            assert events[ind].message.message == "fatal called"
            assert events[++ind].level == Level.ERROR
            assert events[ind].message.message == "error called"
            assert events[++ind].level == Level.WARN
            assert events[ind].message.message == "warn called"
            assert events[++ind].level == Level.INFO
            assert events[ind].message.message == "info called"
            assert events[++ind].level == Level.DEBUG
            assert events[ind].message.message == "debug called"
            assert events[++ind].level == Level.TRACE
            assert events[ind].message.message == "trace called"
        }
    }

    void testLogGuard() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
            class MyClass {
                def loggingMethod() {
                    log.setLevel(org.apache.logging.log4j.Level.OFF)
                    log.fatal (prepareLogMessage())
                    log.error (prepareLogMessage())
                    log.warn  (prepareLogMessage())
                    log.info  (prepareLogMessage())
                    log.debug (prepareLogMessage())
                    log.trace (prepareLogMessage())
                }

                def prepareLogMessage() {
                  log.getAppender('MyAppender')?.isLogGuarded = false
                  return "formatted log message"
                }
            }
            new MyClass().loggingMethod() ''')

            clazz.newInstance().run()

            assert appender.isLogGuarded == true
        }
    }
}

class Log4j2InterceptingAppender extends AbstractAppender {
    List<LoggingEvent> events
    boolean isLogGuarded = true

    Log4j2InterceptingAppender(String name, Filter filter, Layout<String> layout){
        super(name, filter, layout)
        this.events = new ArrayList<LoggingEvent>()
    }

    @Override
    public void append(LogEvent event) {
        events.add(event)
    }
}
