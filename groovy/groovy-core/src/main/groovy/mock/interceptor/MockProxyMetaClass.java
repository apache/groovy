package groovy.mock.interceptor;

import groovy.lang.ProxyMetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaClass;

import java.beans.IntrospectionException;

import org.codehaus.groovy.runtime.InvokerHelper;

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
        MetaClassRegistry metaRegistry = InvokerHelper.getInstance().getMetaRegistry();
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

    /**
     * Unlike general impl in superclass, ctors are not intercepted but relayed
     */
    public Object invokeConstructor(final Object[] arguments) {
        return adaptee.invokeConstructor(arguments);
    }
    
    /**
     * Unlike general impl in superclass, ctors are not intercepted but relayed
     */
    public Object invokeConstructorAt(final Class at, final Object[] arguments) {
        return adaptee.invokeConstructorAt(at, arguments);
    }

}
