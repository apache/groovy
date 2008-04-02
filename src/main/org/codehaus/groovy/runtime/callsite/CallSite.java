/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Method;

/**
 * Base class for all call sites
 *
 * @author Alex Tkachman
 */
public abstract class CallSite {
    protected final int index;
    public final String name;
    protected final CallSiteArray array;
    private MetaClass metaClass;

    public CallSite(CallSiteArray array, int index, String name) {
        this.name = name;
        this.index = index;
        this.array = array;
    }

    public CallSite(CallSite prev) {
        this.name = prev.name;
        this.index = prev.index;
        this.array = prev.array;
    }

    /**
     * Call method 'name' of receiver with given arguments
     *
     * @param receiver receiver
     * @param args arguments
     * @return result of invocation
     */
    public abstract Object invoke(Object receiver, Object [] args);

    public Object invokeBinop(Object receiver, Object arg) {
        if (receiver == null)
          receiver = NullObject.getNullObject();

        return invoke(receiver, new Object[] {arg});
    }

    /**
     * Check if receiver/arguments are "exactly the same" as when this site was created.
     *
     * Exact meaning of "exactly the same" depends on type of the site.
     * For example, for GroovyInterceptable it is enough to check that receiver is GroovyInterceptable
     * but for site with meta method we need to be sure that classes of arguments are exactly the same
     * in the strongest possible meaning.
     *
     * @param receiver receiver
     * @param args arguments
     * @return if receiver/arguments are valid for this site
     */
    public CallSite acceptCall(Object receiver, Object[] args) {
        throw new UnsupportedOperationException();
    }

    public CallSite acceptCurrent(Object receiver, Object[] args) {
        throw new UnsupportedOperationException();
    }

    public CallSite acceptConstructor(Object receiver, Object[] args) {
        throw new UnsupportedOperationException();
    }

    public CallSite acceptStatic(Object receiver, Object[] args) {
        throw new UnsupportedOperationException();
    }

    public CallSite acceptBinop(Object receiver, Object arg) {
        try {
            final Object[] args = {arg};
            return acceptCall(receiver, args);
        }
        catch (NullPointerException e) {
            if (receiver == null)
              return acceptBinop(NullObject.getNullObject(), arg );
            throw e;
        }
    }

    public final Object callSafe(Object receiver, Object[] args) throws Throwable {
        if (receiver == null)
            return null;

        return acceptCall(receiver, args).invoke(receiver, args);
    }

    public Object call(Object receiver, Object[] args) {
        return acceptCall(receiver, args).invoke(receiver, args);
    }

    public Object callCurrent (Object receiver, Object [] args) throws Throwable {
        return acceptCurrent(receiver, args).invoke(receiver, args);
    }
    
    public Object callBinop (Object receiver, Object arg) {
        return acceptBinop(receiver, arg).invokeBinop(receiver, arg);
    }

    public final Object callConstructor (Object receiver, Object [] args) throws Throwable {
        return acceptConstructor((Class) receiver, args).invoke(receiver, args);
    }

    final CallSite createCallStaticSite(Class receiver, Object[] args) {
        CallSite site;
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        if (metaClass instanceof MetaClassImpl) {
            site = ((MetaClassImpl)metaClass).createStaticSite(this, args);
        }
        else
          site = new StaticMetaClassSite(this, metaClass);

        array.array [index] = site;
        return site;
    }

    protected final CallSite createCallConstructorSite(Class receiver, Object[] args) {
       MetaClass metaClass = InvokerHelper.getMetaClass(receiver);

       CallSite site;
       if (metaClass instanceof MetaClassImpl) {
           site = ((MetaClassImpl)metaClass).createConstructorSite(this, args);
       }
       else
         site = new MetaClassConstructorSite(this, metaClass);

       array.array [index] = site;
       return site;
    }

    protected final CallSite createCallCurrentSite(Object receiver, Object[] args, Class sender) {
        CallSite site;
        if (receiver instanceof GroovyInterceptable)
          site = new PogoInterceptableSite(this);
        else {
            MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
            if (metaClass instanceof MetaClassImpl) {
                site = ((MetaClassImpl)metaClass).createPogoCallCurrentSite(this, sender, args);
            }
            else
              site = new PogoMetaClassSite(this, metaClass);
        }

        array.array [index] = site;
        return site;
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    final CallSite createPojoSite(Object receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver.getClass());

        if (metaClass instanceof MetaClassImpl) {
          return ((MetaClassImpl)metaClass).createPojoCallSite(this, receiver, args);
        }

        return new PojoMetaClassSite(this, metaClass);
    }

    final CallSite createPogoSite(Object receiver, Object[] args) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptableSite(this);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallSite(this, args);
        }

        return new PogoMetaClassSite(this, metaClass);
    }

    static boolean noCoerce(ParameterTypes metaMethod, Object[] args) {
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

    static boolean noWrappers(Object[] args) {
        for (int i = 0; i != args.length; ++i)
          if (args [i] instanceof Wrapper)
            return false;
        return true;
    }

    public boolean wantProvideCallSite() {
        return false;
    }

    /**
     * Call site which never accept any receiver/arguments.
     * We use it as initial value for any call site.
     * It allow us to avoid additional null check on each call
     */
    static class DummyCallSite extends CallSite {
        public DummyCallSite(CallSiteArray array, int index, String name) {
            super(array, index,name);
        }

        public Object invoke(Object receiver, Object[] args) {
            return null;
        }

        public final CallSite acceptCall(Object receiver, Object[] args) {
            return createCallSite(receiver, args);
        }

        public final CallSite acceptCurrent(Object receiver, Object[] args) {
            return createCallCurrentSite(receiver, args, array.owner);
        }

        public final CallSite acceptConstructor(Object receiver, Object[] args) {
            return createCallConstructorSite((Class) receiver, args);
        }

        public final CallSite acceptStatic(Object receiver, Object[] args) {
            return createCallStaticSite((Class) receiver, args);
        }
    }

    protected final CallSite createCallSite(Object receiver, Object[] args) {
        CallSite site;
        if (receiver == null)
          return new CallSite(this) {
              public Object invoke(Object receiver, Object[] args) {
                      return InvokerHelper.invokeMethod(NullObject.getNullObject(), name, args);
              }
          };
        
        if (receiver instanceof Class)
          site = createCallStaticSite((Class) receiver, args);
        else if (receiver instanceof GroovyObject) {
            site = createPogoSite(receiver, args);
        } else {
            site = createPojoSite(receiver, args);
        }

        array.array[index] = site;
        return site;
    }

    public Object callGetProperty (Object receiver) {
        final Class aClass = receiver.getClass();
        if (receiver instanceof GroovyObject) {
            try {
                final Method method = aClass.getMethod("getProperty", String.class);
                if (method != null && method.isSynthetic())
                  return createPogoMetaClassGetPropertySite ((GroovyObject)receiver).callGetProperty(receiver);
            } catch (NoSuchMethodException e) {
            }
            return createPogoGetPropertySite (aClass).callGetProperty(receiver);
        }
        if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite ((Class) receiver).callGetProperty(receiver);
        }
        return createMetaClassGetPropertySite (aClass).callGetProperty(receiver);
    }

    private CallSite createMetaClassGetPropertySite(final Class aClass) {
        CallSite site = new CallSite (this) {
            MetaClass metaClass = InvokerHelper.getMetaClass(aClass);

            public Object invoke(Object receiver, Object[] args) {
                return null;
            }

            public Object callGetProperty(Object receiver) {
                if (receiver.getClass() != aClass)
                  return super.callGetProperty(receiver);
                return metaClass.getProperty(receiver, name);
            }
        };

        array.array[index] = site;
        return site;
    }

    private CallSite createClassMetaClassGetPropertySite(final Class aClass) {
        CallSite site = new CallSite (this) {
            final MetaClass metaClass = InvokerHelper.getMetaClass(aClass);

            public Object invoke(Object receiver, Object[] args) {
                return null;
            }

            public Object callGetProperty(Object receiver) {
                if (receiver != aClass)
                  return super.callGetProperty(receiver);

                return metaClass.getProperty(aClass, name);
            }
        };

        array.array[index] = site;
        return site;
    }

    private CallSite createPogoMetaClassGetPropertySite(final GroovyObject receiver) {
        final MetaClass metaClass = receiver.getMetaClass();

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class) {
            site = new CallSite(this) {
                public Object invoke(Object receiver, Object[] args) {
                    return null;
                }

                public Object callGetProperty(Object receiver) {
                    if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
                      return super.callGetProperty(receiver);

                    return metaClass.getProperty(receiver, name);
                }
            };
        }
        else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(metaClass.getClass(), receiver, name, false);
            if (effective != null) {
                site = new CallSite(this) {
                    public Object invoke(Object receiver, Object[] args) {
                        return null;
                    }

                    public Object callGetProperty(Object receiver) {
                        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
                          return super.callGetProperty(receiver);

                        if (GroovyCategorySupport.hasCategoryInAnyThread())
                            return metaClass.getProperty(receiver, name);

                        return effective.getProperty(receiver);
                    }
                };
            }
            else {
                site = new CallSite(this) {
                    public Object invoke(Object receiver, Object[] args) {
                        return null;
                    }

                    public Object callGetProperty(Object receiver) {
                        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
                          return super.callGetProperty(receiver);

                        return metaClass.getProperty(receiver, name);
                    }
                };
            }
        }

        array.array[index] = site;
        return site;
    }

    private CallSite createPogoGetPropertySite(final Class aClass) {
        CallSite site = new CallSite (this) {
            public Object invoke(Object receiver, Object[] args) {
                return null;
            }

            public Object callGetProperty(Object receiver) {
                if (receiver.getClass() != aClass)
                  return super.callGetProperty(receiver);

                return ((GroovyObject)receiver).getProperty(name);
            }
        };

        array.array[index] = site;
        return site;
    }

    public final Object callGetPropertySafe (Object receiver) {
        if (receiver == null)
          return null;
        else
          return callGetProperty(receiver);
    }
}
