package org.codehaus.groovy.runtime;

import groovy.lang.*;

public class CallSiteArray {
    private final CallSite [] array;

    public CallSiteArray(int size) {
        array = new CallSite[size];
    }

    private CallSite getCallSite(int index, String name, Object receiver, Object [] args) {
        CallSite site = array [index];
        if (site != null && site.accept(receiver, args)) {
            return site;
        }
        site = createCallSite(name, receiver, args);
        array [index] = site;
        return site;
    }

    private static CallSite createCallSite(String name, Object receiver, Object[] args) {
        if (receiver instanceof Class)
          return new ClassCall(name, receiver, args);

        if (!(receiver instanceof GroovyObject))
          return new PojoCall(name, receiver, args);

        if (receiver instanceof GroovyInterceptable)
          return new InterceptingCall(name, receiver, args);

        return new PogoCall(name, receiver, args);
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

    private static class ClassCall extends CallSite {
        ClassCall(String name, Object receiver, Object[] args) {
            super(name, receiver, args);
        }

        final Object call(Object receiver, Object [] args) {
            Class theClass = (Class) receiver;
            MetaClass metaClass = InvokerHelper.metaRegistry.getMetaClass(theClass);
            return metaClass.invokeStaticMethod(receiver, name, InvokerHelper.asArray(args));
        }
    }

    private static class PojoCall extends CallSite {
        final MetaClass metaClass;
        final MetaMethod metaMethod;
        final Class [] params;

        public PojoCall(String name, Object receiver, Object[] args) {
            super(name, receiver, args);
            metaClass = InvokerHelper.metaRegistry.getMetaClass(receiverClass);
            params = MetaClassHelper.convertToTypeArray(args);
            metaMethod = metaClass.pickMethod(name, params);
        }

        final Object call(Object receiver, Object[] args) {
            MetaClassHelper.unwrap(args);
            return metaMethod.doMethodInvoke(receiver,  args);
        }

        boolean accept(Object receiver, Object[] args) {
            return super.accept(receiver, args) && MetaClassHelper.sameClasses(params, args, false);
        }
    }

    private static class PogoCall extends CallSite {
        public PogoCall(String name, Object receiver, Object[] args) {
            super(name, receiver, args);
        }

        final Object call(Object receiver, Object[] args) {
            GroovyObject groovy = (GroovyObject) receiver;
            try {
                return groovy.getMetaClass().invokeMethod(receiver, name, InvokerHelper.asArray(args));
            } catch (MissingMethodException e) {
                if (e.getMethod().equals(name) && receiver.getClass() == e.getType()) {
                    // in case there's nothing else, invoke the object's own invokeMethod()
                    return groovy.invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
                }
                throw e;
            }
        }
    }

    private static class InterceptingCall extends CallSite {
        public InterceptingCall(String name, Object receiver, Object[] args) {
            super(name, receiver, args);
        }

        final Object call(Object receiver, Object[] args) {
          return ((GroovyObject)receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
        }
    }

    private abstract static class CallSite {

        public static final CallSite INSTANCE = new CallSite("",new Object(),null) {
            final Object call(Object receiver, Object[] args) {
                return null;
            }

            boolean accept(Object receiver, Object[] args) {
                return false;
            }
        };

        final String name;
        final Class  receiverClass;

        CallSite (String name, Object receiver, Object [] args) {
            this.name = name;
            receiverClass = receiver.getClass();
        }

        abstract Object call(Object receiver, Object [] args);

        boolean accept(Object receiver, Object[] args) {
            return receiverClass == receiver.getClass();
        }
    }
}
