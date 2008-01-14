package org.codehaus.groovy.runtime;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

public class CallSiteArray {
    private final CallSite[] array;

    public static final Object [] NOPARAM = new Object[0];
    private final Class owner;
    private final int size;

    public CallSiteArray(Class owner, int size) {
        this.owner = owner;
        this.size = size;
        array = new CallSite[size];
    }

    public final Object callSafe (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            if (receiver == null)
                return null;

            return getCallSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object call (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            if (receiver == null)
                receiver = NullObject.getNullObject();

            return getCallSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callCurrent (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            return getCallCurrentSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
    
    private CallSite getCallSite(int index, String name, Object receiver, Object [] args) {
        final CallSite[] array = getArray();
        CallSite site = array[index];
        if (site != null && site.accept(receiver, args)) {
            return site;
        }
        site = createCallSite(name, receiver, args);
        array[index] = site;
        return site;
    }

    private CallSite getCallCurrentSite(int index, String name, Object receiver, Object [] args) {
        CallSite site = getArray()[index];
        if (site != null && site.accept(receiver, args)) {
            return site;
        }
        site = createCallCurrentSite(name, receiver, args, owner);
        getArray()[index] = site;
        return site;
    }

    private static CallSite createCallSite(String name, Object receiver, Object[] args) {
        if (receiver instanceof Class)
          return new ClassSite(name, receiver.getClass());

        if (!(receiver instanceof GroovyObject)) {
            return createPojoSite(name, receiver, args);
        }

        return createPogoSite(name, receiver, args);
    }

    private static CallSite createCallCurrentSite(String name, Object receiver, Object[] args, Class sender) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptingSite(name, receiver);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass.getClass() == MetaClassImpl.class) {
          Class [] params = MetaClassHelper.convertToTypeArray(args);
          MetaMethod metaMethod = ((MetaClassImpl)metaClass).getMethodWithCachingInternal(sender, name, params);
          if (metaMethod != null)
             return new PogoViaMetaMethodSite(name, (MetaClassImpl) metaClass, metaMethod, params);
        }

        return new PogoViaMetaClassSite(name, metaClass);
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    private static CallSite createPojoSite(String name, Object receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver.getClass());

        if (metaClass.getClass() == MetaClassImpl.class) {
          Class [] params = MetaClassHelper.convertToTypeArray(args);
          MetaMethod metaMethod = ((MetaClassImpl)metaClass).getMethodWithCachingInternal(metaClass.getTheClass(), name, params);
          if (metaMethod != null)
             return createPojoViaMetaMethodSite(metaClass, metaMethod, name, params, args);
        }

        return new PojoViaMetaClassSite(name, metaClass.getTheClass());
    }

    // an idea here is to avoid unwrap/coerceArguments and try/catch if possible
    private static PojoViaMetaMethodSite createPojoViaMetaMethodSite(MetaClass metaClass, MetaMethod metaMethod, String name, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PojoViaMetaMethodSiteNoUnwrap(name, (MetaClassImpl) metaClass, metaMethod, params);
                else
                    return new PojoViaMetaMethodSiteNoUnwrapNoCoerce(name, (MetaClassImpl) metaClass, metaMethod, params);
            }
        }
        return new PojoViaMetaMethodSite(name, (MetaClassImpl) metaClass, metaMethod, params);
    }

    private static boolean noCoerce(MetaMethod metaMethod, Object[] args) {
        final CachedClass[] paramClasses = metaMethod.getParameterTypes();
        if (paramClasses.length != args.length)
          return false;
        
        for (int i = 0; i < paramClasses.length; i++) {
            CachedClass paramClass = paramClasses[i];
            if (args[i] != null && !paramClass.isDirectlyAssignable(args[i]))
              return true;
        }
        return false;
    }

    private static boolean noWrappers(Object[] args) {
        for (int i = 0; i != args.length; ++i)
          if (args [i] instanceof Wrapper)
            return false;
        return true;
    }

    private static CallSite createPogoSite(String name, Object receiver, Object[] args) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptingSite(name, receiver);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass.getClass() == MetaClassImpl.class) {
          Class [] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = ((MetaClassImpl)metaClass).getMethodWithCachingInternal(metaClass.getTheClass(), name, params);
          if (metaMethod != null)
             return new PogoViaMetaMethodSite(name, (MetaClassImpl) metaClass, metaMethod, params);
        }

        return new PogoViaMetaClassSite(name, metaClass);
    }

    public CallSite[] getArray() {
        return array;
    }

    private abstract static class CallSite {
        final String name;

        public CallSite(String name) {
            this.name = name;
        }

        abstract Object call(Object receiver, Object [] args);

        abstract boolean accept(Object receiver, Object[] args);
    }

    private abstract static class MetaClassSite extends CallSite {
        final MetaClass metaClass;

        public MetaClassSite(String name, MetaClass metaClass) {
            super(name);
            this.metaClass = metaClass;
        }
    }

    private static class ClassSite extends MetaClassSite {
        ClassSite(String name, Class receiverClass) {
            super(name, InvokerHelper.getMetaClass(receiverClass));
        }

        final Object call(Object receiver, Object [] args) {
            return metaClass.invokeStaticMethod(receiver, name, args);
        }

        boolean accept(Object receiver, Object[] args) {
            return receiver.getClass() == metaClass.getTheClass();
        }
    }

    private static class PojoViaMetaClassSite extends CallSite {
        final Class klazz;

        PojoViaMetaClassSite(String name, Class klazz) {
            super(name);
            this.klazz = klazz;
        }

        final Object call(Object receiver, Object [] args) {
            return InvokerHelper.getMetaClass(klazz).invokeMethod(receiver, name, args);
        }

        final boolean accept(Object receiver, Object[] args) {
            return receiver.getClass() == klazz; // klazz match receiver
        }
    }

    private static class PojoViaMetaMethodSite extends MetaClassSite {
        final MetaMethod metaMethod;
        final Class [] params;

        public PojoViaMetaMethodSite(String name, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(name, metaClass);
            this.params = params;
            this.metaMethod = metaMethod;
        }

        Object call(Object receiver, Object[] args) {
            MetaClassHelper.unwrap(args);
            return metaMethod.doMethodInvoke(receiver,  args);
        }

        final boolean accept(Object receiver, Object[] args) {
            return receiver.getClass() == metaClass.getTheClass() // meta class match receiver
//               && ((MetaClassImpl)metaClass).getTheCachedClass().getMetaClassForClass() == metaClass // metaClass still be valid
               && MetaClassHelper.sameClasses(params, args, false); // right arguments
        }
    }

    private static class PojoViaMetaMethodSiteNoUnwrap extends PojoViaMetaMethodSite {

        public PojoViaMetaMethodSiteNoUnwrap(String name, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(name, metaClass, metaMethod, params);
        }

        final Object call(Object receiver, Object[] args) {
            return metaMethod.doMethodInvoke(receiver,  args);
        }
    }

    private static class PojoViaMetaMethodSiteNoUnwrapNoCoerce extends PojoViaMetaMethodSite {

        public PojoViaMetaMethodSiteNoUnwrapNoCoerce(String name, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(name, metaClass, metaMethod, params);
        }

        final Object call(Object receiver, Object[] args) {
            return metaMethod.invoke(receiver,  args);
        }
    }

    private static class PogoViaMetaMethodSite extends MetaClassSite {
        final MetaMethod metaMethod;
        final Class [] params;

        public PogoViaMetaMethodSite(String name, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(name, metaClass);
            this.params = params;
            this.metaMethod = metaMethod;
        }

        final Object call(Object receiver, Object[] args) {
            MetaClassHelper.unwrap(args);
            return metaMethod.doMethodInvoke(receiver,  args);
        }

        final boolean accept(Object receiver, Object[] args) {
            return receiver.getClass() == metaClass.getTheClass() // meta class match receiver
               && ((GroovyObject)receiver).getMetaClass() == metaClass // metaClass still be valid
               && MetaClassHelper.sameClasses(params, args, false); // right arguments
        }
    }

    private static class PogoViaMetaClassSite extends MetaClassSite {
        public PogoViaMetaClassSite(String name, MetaClass metaClass) {
            super(name, metaClass);
        }

        final Object call(Object receiver, Object[] args) {
            try {
                return metaClass.invokeMethod(receiver, name, args);
            } catch (MissingMethodException e) {
                GroovyObject groovy = (GroovyObject) receiver;
                if (e.getMethod().equals(name) && receiver.getClass() == e.getType()) {
                    return groovy.invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
                }
                throw e;
            }
        }

        boolean accept(Object receiver, Object[] args) {
            return receiver instanceof GroovyObject && ((GroovyObject)receiver).getMetaClass() == metaClass;
        }
    }

    private static class PogoInterceptingSite extends CallSite {
        private final Class klazz;

        public PogoInterceptingSite(String name, Object receiver) {
            super(name);
            klazz = receiver.getClass();
        }

        final Object call(Object receiver, Object[] args) {
          return ((GroovyObject)receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
        }

        boolean accept(Object receiver, Object[] args) {
            return receiver.getClass() == klazz;
        }
    }
}
