package groovy.util.logging

import java.lang.reflect.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.ast.*
import org.codehaus.groovy.transform.*
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * @author Tomasz Bujok
 */
class Log4jTest extends GroovyTestCase {

  Log4jInterceptingAppender appender
  Logger logger

  protected void setUp() {
    super.setUp()
    appender = new Log4jInterceptingAppender()
    appender.setName('MyAppender')
    logger = Logger.getLogger('MyClass')
    logger.addAppender(appender)
    logger.setLevel(Level.ALL)
  }

  protected void tearDown() {
    super.tearDown()
    logger.removeAllAppenders()
  }

  public void testPrivateFinalStaticLogFieldAppears() {

    Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Log4j
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
                @groovy.util.logging.Log4j()
                class MyClass {
                    String log
                } ''')

      assert clazz.newInstance()
    }
  }

  public void testClassAlreadyHasNamedLogField() {

    shouldFail {

      Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j('logger')
                class MyClass {
                    String logger
                } ''')

      assert clazz.newInstance()
    }
  }

  public void testLogInfo() {

    Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j
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

    Script s = (Script) clazz.newInstance()
    s.run()

    int ind = 0
    def events = appender.getEvents()
    assert events.size() == 6
    assert events[ind].level == Level.FATAL
    assert events[ind].message == "fatal called"
    assert events[++ind].level == Level.ERROR
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

    void testLogFromStaticMethods() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log4j
            class MyClass {
                static loggingMethod() {
                  log.info   ("(static) info called")
                }
            }
            MyClass.loggingMethod()""")

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        assert events.size() == 1
        assert events[0].level == Level.INFO
        assert events[0].message == "(static) info called"
    }

  public void testLogInfoForNamedLogger() {

    Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j('logger')
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

    Script s = (Script) clazz.newInstance()
    s.run()

    int ind = 0
    def events = appender.getEvents()
    assert events.size() == 6
    assert events[ind].level == Level.FATAL
    assert events[ind].message == "fatal called"
    assert events[++ind].level == Level.ERROR
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
            @groovy.util.logging.Log4j
            class MyClass {
                def loggingMethod() {
                    log.setLevel(org.apache.log4j.Level.OFF)
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

    Script s = (Script) clazz.newInstance()
    s.run()

    assert appender.isLogGuarded == true
  }
}

public class Log4jInterceptingAppender extends AppenderSkeleton {
  List<LoggingEvent> events
  boolean isLogGuarded = true

  public Log4jInterceptingAppender() {
    this.events = new ArrayList<LoggingEvent>()
  }

  public List<LoggingEvent> getEvents() {
    return events
  }

  protected void append(LoggingEvent logEvent) {
    events.add(logEvent)
  }

  void close() {
  }

  boolean requiresLayout() {
    return false;
  }
}

