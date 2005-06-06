package groovy.lang

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StringBufferWriter

/**
* Test for the Interceptor Interface usage as implemented by the
* TracingInterceptor. Makes also use of the ProxyMetaClass and
* shows the collaboration.
* As a side Effect, the ProxyMetaClass is also partly tested.
* @author Dierk Koenig
**/
class InterceptorTest extends GroovyTestCase{

    def Interceptor logInterceptor
    def StringBuffer log
    def interceptable   // the object to intercept method calls on

    void setUp() {
        logInterceptor = new TracingInterceptor()
        log = new StringBuffer()
        logInterceptor.writer = new StringBufferWriter(log)
        // we intercept calls from Groovy to the java.lang.String object
        interceptable = 'Interceptable String'
    }
    ProxyMetaClass initProxy(){
        proxy = ProxyMetaClass.getInstance(interceptable.class)
        proxy.setInterceptor(logInterceptor)
        proxy.register()
        return proxy
    }

    void testUnIntercepted() {
        interceptable.size()                // GDK method
        interceptable.length()              // JDK method
        interceptable.startsWith('I',0)     // with parameters
        assertEquals 0, log.length()        // todo: StringBuffer should have a size() method !
    }

    void testSimpleInterception() {
        initProxy()
        assertEquals 20, interceptable.size()
        assertEquals 20,interceptable.length()
        assertTrue interceptable.startsWith('I',0)
        assertEquals(
"""Interceptor before java.lang.String.size()
Interceptor after java.lang.String.size()
Interceptor before java.lang.String.length()
Interceptor after java.lang.String.length()
Interceptor before java.lang.String.startsWith(java.lang.String, java.lang.Integer)
Interceptor after java.lang.String.startsWith(java.lang.String, java.lang.Integer)
""", log.toString())
    }

    void testNoInterceptionAfterUnregister() {
        proxy = initProxy()
        proxy.unRegister()

        interceptable.size()
        interceptable.length()
        interceptable.startsWith('I',0)
        assertEquals 0, log.length()
    }

    void testNoInterceptionWithNullInterceptor() {
        proxy = initProxy()
        proxy.setInterceptor(null)

        interceptable.size()
        interceptable.length()
        interceptable.startsWith('I',0)
        assertEquals 0, log.length()
    }

    void testConstructorInterception() {
        initProxy()
        new String('some string')
        assertEquals(
"""Interceptor before java.lang.String.ctor(java.lang.String)
Interceptor after java.lang.String.ctor(java.lang.String)
""", log.toString())
    }

    void testStaticMethodInterception() {
        initProxy()
        assertEquals 'true', String.valueOf(true)
        assertEquals(
"""Interceptor before java.lang.String.valueOf(java.lang.Boolean)
Interceptor after java.lang.String.valueOf(java.lang.Boolean)
""", log.toString())
    }

    void testNoInterceptionOfGroovyClasses(){
        slicer = new groovy.mock.example.CheeseSlicer()
        proxy = ProxyMetaClass.getInstance(slicer.class)
        proxy.setInterceptor(logInterceptor)
        proxy.register()

        slicer.sliceSomeCheese('')

        assertEquals 0, log.length()
        // 	at gjdk.groovy.lang.InterceptorTest_GroovyReflector.invoke(Unknown Source)
    }


}



