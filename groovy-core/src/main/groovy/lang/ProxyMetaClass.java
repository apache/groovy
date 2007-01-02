package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.IntrospectionException;

/**
 * As subclass of MetaClass, ProxyMetaClass manages calls from Groovy Objects to POJOs.
 * It enriches MetaClass with the feature of making method invokations interceptable by
 * an Interceptor. To this end, it acts as a decorator (decorator pattern) allowing
 * to add or withdraw this feature at runtime.
 * See groovy/lang/InterceptorTest.groovy for details.
 * @author Dierk Koenig
 */
public class ProxyMetaClass extends MetaClassImpl {

    protected MetaClass adaptee = null;
    protected Interceptor interceptor = null;

    /**
     * convenience factory method for the most usual case.
     */
    public static ProxyMetaClass getInstance(Class theClass) throws IntrospectionException {
        MetaClassRegistry metaRegistry = InvokerHelper.getInstance().getMetaRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new ProxyMetaClass(metaRegistry, theClass, meta);
    }
    /**
     * @param adaptee   the MetaClass to decorate with interceptability
     */
    public ProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
        super(registry, theClass);
        this.adaptee = adaptee;
        if (null == adaptee) throw new IllegalArgumentException("adaptee must not be null");
        super.initialize();
    }
    
    public synchronized void initialize() {
        this.adaptee.initialize();
    }

    /**
     * Use the ProxyMetaClass for the given Closure.
     * Cares for balanced register/unregister.
     * @param closure piece of code to be executed with registered ProxyMetaClass
     */
    public void use(Closure closure){
        registry.setMetaClass(theClass, this);
        
        try {
            closure.call();
        } finally {
            registry.setMetaClass(theClass, adaptee);
        }
    }

     /**
     * Use the ProxyMetaClass for the given Closure.
     * Cares for balanced setting/unsetting ProxyMetaClass.
     * @param closure piece of code to be executed with ProxyMetaClass
     */
    public void use(GroovyObject object, Closure closure){
        object.setMetaClass(this);
        
        try {
            closure.call();
        } finally {
            object.setMetaClass(adaptee);
        }
    }

    /**
     * @return the interceptor in use or null if no interceptor is used
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * @param interceptor may be null to reset any interception
     */
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable(){
            public Object call() {
                return adaptee.invokeMethod(object, methodName, arguments);
            }
        });
    }
    /**
     * Call invokeStaticMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable(){
            public Object call() {
                return adaptee.invokeStaticMethod(object, methodName, arguments);
            }
        });
    }

    /**
     * Call invokeConstructor on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeConstructor(final Object[] arguments) {
        return doCall(theClass, "ctor", arguments, interceptor, new Callable(){
            public Object call() {
                return adaptee.invokeConstructor(arguments);
            }
        });
    }

    // since Java has no Closures...
    private interface Callable{
        Object call();
    }
    private Object doCall(Object object, String methodName, Object[] arguments, Interceptor interceptor, Callable howToInvoke) {
        if (null == interceptor) {
            return howToInvoke.call();
        }
        Object result = interceptor.beforeInvoke(object, methodName, arguments);
        if (interceptor.doInvoke()) {
            result = howToInvoke.call();
        }
        result = interceptor.afterInvoke(object, methodName, arguments, result);
        return result;
    }
}
