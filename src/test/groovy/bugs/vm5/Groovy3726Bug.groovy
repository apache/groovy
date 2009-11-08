package groovy.bugs.vm5

import java.lang.reflect.*

class Groovy3726Bug extends GroovyTestCase {
    void testVolatilePropertiesResultingInBridgeMethods() {
    	def scriptStr, clazz, fooGetter, fooSetter
    	GroovyClassLoader cl = new GroovyClassLoader();
    	
    	scriptStr = """
    		public class GroovyBean3726A {
    			@Lazy volatile String foo = "anything"
    		}
    	"""
    	clazz = cl.parseClass(scriptStr, 'GroovyBean3726A.groovy')
    	
    	fooGetter = clazz.getMethod('get$foo')
    	assertFalse fooGetter.isBridge()
    	assertFalse Modifier.isVolatile(fooGetter.modifiers)
    	
    	scriptStr = """
    		public class GroovyBean3726B {
    			volatile String foo = "anything"
    		}
    	"""
    	clazz = cl.parseClass(scriptStr, 'GroovyBean3726B.groovy')
    	
    	fooGetter = clazz.getMethod('getFoo')
    	assertFalse fooGetter.isBridge()
    	assertFalse Modifier.isVolatile(fooGetter.modifiers)
    	
    	fooSetter = clazz.getMethod('setFoo', [String] as Class[])
    	assertFalse fooSetter.isBridge()
    	assertFalse Modifier.isVolatile(fooSetter.modifiers)
    }

    void testTransientPropertiesResultingInVarArgsMethods() {
    	def scriptStr, clazz, barGetter, barSetter
    	GroovyClassLoader cl = new GroovyClassLoader();
    	
    	scriptStr = """
    		public class GroovyBean3726C {
    			@Lazy transient String bar = "anything"
    		}
    	"""
    	clazz = cl.parseClass(scriptStr, 'GroovyBean3726C.groovy')
    	
    	barGetter = clazz.getMethod('get$bar')
    	assertFalse barGetter.isVarArgs()
    	assertFalse Modifier.isTransient(barGetter.modifiers)
    	
    	scriptStr = """
    		public class GroovyBean3726D {
    			transient String bar = "anything"
    		}
    	"""
    	clazz = cl.parseClass(scriptStr, 'GroovyBean3726D.groovy')
    	
    	barGetter = clazz.getMethod('getBar')
    	assertFalse barGetter.isVarArgs()
    	assertFalse Modifier.isTransient(barGetter.modifiers)
    	
    	barSetter = clazz.getMethod('setBar', [String] as Class[])
    	assertFalse barSetter.isVarArgs()
    	assertFalse Modifier.isTransient(barSetter.modifiers)
    }
}
