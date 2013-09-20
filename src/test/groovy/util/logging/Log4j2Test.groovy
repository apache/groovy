package groovy.util.logging

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class Log4j2Test extends GroovyTestCase {

    // Log4j2 requires at least Java 1.6
    static final boolean javaVersionGreaterThan1_5 = true
    static {
        if (System.getProperty("java.version").startsWith("1.5.")) {
            javaVersionGreaterThan1_5 = false
        }
    }

    Class appenderClazz
    def appender
    def logger

    protected void setUp() {
        super.setUp()
        if(javaVersionGreaterThan1_5) {
            appenderClazz = new GroovyClassLoader().parseClass('''
                class Log4j2InterceptingAppender extends org.apache.logging.log4j.core.appender.AbstractAppender {
                    List<org.apache.log4j.spi.LoggingEvent> events
                    boolean isLogGuarded = true
                
                    Log4j2InterceptingAppender(String name, org.apache.log4j.spi.Filter filter, org.apache.logging.log4j.core.Layout<String> layout){
                        super(name, filter, layout)
                        this.events = new ArrayList<org.apache.log4j.spi.LoggingEvent>()
                    }
                
                    @Override
                    public void append(org.apache.logging.log4j.core.LogEvent event) {
                        events.add(event)
                    }
                }''')
            
            def layoutClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.core.layout.PatternLayout')
            def layout = layoutClazz.metaClass.invokeStaticMethod(layoutClazz, 'createLayout', ["%m", null, null, "UTF-8", "false"] as Object[])
     
            appender = appenderClazz.newInstance(['MyAppender', null, layout] as Object[])
            def logManagerClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.LogManager')
            logger = logManagerClazz.metaClass.invokeStaticMethod(logManagerClazz, 'getLogger', 'MyClass')
            logger.addAppender(appender)
            def levelClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.Level')
            def allLevel = levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'ALL')
            logger.setLevel(allLevel)
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
            def levelClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.Level')
            assert events[ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'FATAL')
            assert events[ind].message.message == "fatal called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'ERROR')
            assert events[ind].message.message == "error called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'WARN')
            assert events[ind].message.message == "warn called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'INFO')
            assert events[ind].message.message == "info called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'DEBUG')
            assert events[ind].message.message == "debug called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'TRACE')
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
            def levelClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.Level')
            assert events[0].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'INFO')
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
            def levelClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.Level')
            assert events[ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'FATAL')
            assert events[ind].message.message == "fatal called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'ERROR')
            assert events[ind].message.message == "error called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'WARN')
            assert events[ind].message.message == "warn called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'INFO')
            assert events[ind].message.message == "info called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'DEBUG')
            assert events[ind].message.message == "debug called"
            assert events[++ind].level == levelClazz.metaClass.invokeStaticMethod(levelClazz, 'toLevel', 'TRACE')
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
    
    void testDefaultCategory() {
        if(javaVersionGreaterThan1_5) {
            Class clazz = new GroovyClassLoader().parseClass("""
                @groovy.util.logging.Log4j2
                class MyClass {
                    static loggingMethod() {
                      log.info("info called")
                    }
                }""")

            def s = clazz.newInstance()
            s.loggingMethod()

            assert appender.getEvents().size() == 1
        }
    }

    public void testCustomCategory() {
        if(javaVersionGreaterThan1_5) {
            def layoutClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.core.layout.PatternLayout')
            def layout = layoutClazz.metaClass.invokeStaticMethod(layoutClazz, 'createLayout', ["%m", null, null, "UTF-8", "false"] as Object[])
            def appenderForCustomCategory = appenderClazz.newInstance(['Appender4CustomCategory', null, layout] as Object[])

            def logManagerClazz = new GroovyClassLoader().loadClass('org.apache.logging.log4j.LogManager')
            def loggerForCustomCategory = logManagerClazz.metaClass.invokeStaticMethod(logManagerClazz, 'getLogger', 'customCategory')
            loggerForCustomCategory.addAppender(appenderForCustomCategory)

            Class clazz = new GroovyClassLoader().parseClass("""
                @groovy.util.logging.Log4j2(category='customCategory')
                class MyClass {
                    static loggingMethod() {
                      log.error("error called")
                    }
                }""")
            def s = clazz.newInstance()

            s.loggingMethod()

            assert appenderForCustomCategory.getEvents().size() == 1
            assert appender.getEvents().size() == 0
        }
    }
}
