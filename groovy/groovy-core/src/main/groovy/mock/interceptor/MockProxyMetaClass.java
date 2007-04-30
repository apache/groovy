package groovy.mock.interceptor;

import groovy.lang.*;

import java.beans.IntrospectionException;

/**
 * The ProxyMetaClass for the MockInterceptor.
 * Instance and class methods are intercepted, but constructors are not to allow mocking of aggregated objects.
 * @author Dierk Koenig
 */

public class MockProxyMetaClass extends ProxyMetaClass {

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public MockProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
        super(registry, theClass, adaptee);
    }

    /**
     * convenience factory method for the most usual case.
     */
    public static MockProxyMetaClass make(Class theClass) throws IntrospectionException {
        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new MockProxyMetaClass(metaRegistry, theClass, meta);
    }


    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        if (null == interceptor) {
            throw new RuntimeException("cannot invoke without interceptor");
        }
        return interceptor.beforeInvoke(object, methodName, arguments);
    }

    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        if (null == interceptor) {
            throw new RuntimeException("cannot invoke without interceptor");
        }
        return interceptor.beforeInvoke(object, methodName, arguments);
    }

    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        if (null == interceptor) {
            throw new RuntimeException("cannot invoke without interceptor");
        }
        if(interceptor instanceof PropertyAccessInterceptor) {
            return ((PropertyAccessInterceptor)interceptor).beforeGet(object,property);
        }
        else {
            return super.getProperty(aClass,object,property,b,b);
        }


    }

    public void setProperty(Class aClass, Object object, String property, Object newValue, boolean b, boolean b1) {
        if (null == interceptor) {
            throw new RuntimeException("cannot invoke without interceptor");
        }

        if(interceptor instanceof PropertyAccessInterceptor) {
            ((PropertyAccessInterceptor)interceptor).beforeSet(object,property, newValue);
        }
        else {
            super.setProperty(aClass,object,property,newValue,b,b);
        }

    }

    /**
     * Unlike general impl in superclass, ctors are not intercepted but relayed
     */
    public Object invokeConstructor(final Object[] arguments) {
        return adaptee.invokeConstructor(arguments);
    }
    
}
