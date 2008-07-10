package org.codehaus.groovy.reflection;

import groovy.lang.*;

import java.lang.reflect.Modifier;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

public class MixinInMetaClass extends WeakHashMap {
    final ExpandoMetaClass emc;
    final CachedClass mixinClass;
    final CachedConstructor constructor;

    public MixinInMetaClass(ExpandoMetaClass emc, CachedClass mixinClass) {
        this.emc = emc;
        this.mixinClass = mixinClass;

        constructor = findDefaultConstructor(mixinClass);
        emc.addMixinClass(this);
    }

    private CachedConstructor findDefaultConstructor(CachedClass mixinClass) {
        for(CachedConstructor constr : mixinClass.getConstructors()) {
            if (!Modifier.isPublic(constr.getModifiers()))
              continue;

            CachedClass[] classes = constr.getParameterTypes();
            if (classes.length == 0)
               return constr;
        }

        throw new GroovyRuntimeException("No default constructor for class " + mixinClass.getName() + "! Can't be mixed in.");
    }

    public synchronized Object getMixinInstance (Object object) {
        Object mixinInstance = get(object);
        if (mixinInstance == null) {
            mixinInstance = constructor.invoke(MetaClassHelper.EMPTY_ARRAY);
            new MyDelegatingMetaClass(mixinInstance, object);
            put (object, mixinInstance);
        }
        return mixinInstance;
    }

    public synchronized void setMixinInstance (Object object, Object mixinInstance) {
        if (mixinInstance == null) {
            remove(object);
        }
        else {
          put (object, mixinInstance);
        }
    }

    public CachedClass getInstanceClass() {
        return emc.getTheCachedClass();
    }

    public CachedClass getMixinClass() {
        return mixinClass;
    }

    public static void mixinClassesToMetaClass(MetaClass self, List<Class> categoryClasses) {
        final Class selfClass = self.getTheClass();

        if (self instanceof HandleMetaClass) {
            self = (MetaClass) ((HandleMetaClass)self).replaceDelegate();
        }

        if (!(self instanceof ExpandoMetaClass)) {
            if (self instanceof DelegatingMetaClass && ((DelegatingMetaClass) self).getAdaptee() instanceof ExpandoMetaClass) {
                self = ((DelegatingMetaClass) self).getAdaptee();
            } else {
                throw new GroovyRuntimeException("Can't mixin methods to meta class: " + self);
            }
        }

        ExpandoMetaClass mc = (ExpandoMetaClass)self;

        ArrayList<MetaMethod> arr = new ArrayList<MetaMethod> ();
        for (Class categoryClass : categoryClasses) {

            final CachedClass cachedCategoryClass = ReflectionCache.getCachedClass(categoryClass);
            final MixinInMetaClass mixin = new MixinInMetaClass(mc, cachedCategoryClass);

            CachedMethod[] methods = cachedCategoryClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                CachedMethod method = methods[i];
                final int mod = method.getModifiers();
                if (Modifier.isPublic(mod) && !method.getCachedMethod().isSynthetic()) {
                    if (Modifier.isStatic(mod)) {
                        staticMethod(self, arr, method);
                    }
                    else {
                        if(self.pickMethod(method.getName(), method.getNativeParameterTypes()) == null) {
                            arr.add(new MixinInstanceMetaMethod(method, mixin));
                        }
                    }
                }
            }
        }

        if (arr.isEmpty())
          return;

        for (Object res : arr) {
            final MetaMethod metaMethod = (MetaMethod) res;
            if (metaMethod.getDeclaringClass().isAssignableFrom(selfClass))
              mc.registerInstanceMethod(metaMethod);
            else {
              mc.registerSubclassInstanceMethod(metaMethod);
            }
        }
    }

    private static void staticMethod(final MetaClass self, ArrayList<MetaMethod> arr, final CachedMethod method) {
        CachedClass[] paramTypes = method.getParameterTypes();

        if (paramTypes.length == 0)
            return;

        NewInstanceMetaMethod metaMethod;
        if (paramTypes[0].isAssignableFrom(self.getTheClass())) {
            if (paramTypes[0].getTheClass() == self.getTheClass())
                metaMethod = new NewInstanceMetaMethod(method);
            else
                metaMethod = new NewInstanceMetaMethod(method) {
                    public CachedClass getDeclaringClass() {
                        return ReflectionCache.getCachedClass(self.getTheClass());
                    }
                };
            arr.add(metaMethod);
        }
        else {
            if (self.getTheClass().isAssignableFrom(paramTypes[0].getTheClass())) {
                metaMethod = new NewInstanceMetaMethod(method);
                arr.add(metaMethod);
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MixinInMetaClass)) return false;
        if (!super.equals(o)) return false;

        MixinInMetaClass that = (MixinInMetaClass) o;

        if (mixinClass != null ? !mixinClass.equals(that.mixinClass) : that.mixinClass != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (emc != null ? emc.hashCode() : 0);
        result = 31 * result + (mixinClass != null ? mixinClass.hashCode() : 0);
        result = 31 * result + (constructor != null ? constructor.hashCode() : 0);
        return result;
    }

    private static class MyDelegatingMetaClass extends DelegatingMetaClass {
        final WeakReference owner;

        public MyDelegatingMetaClass(Object instance, Object owner) {
            super(InvokerHelper.getMetaClass(instance.getClass()));
            this.owner = new WeakReference(owner);
            DefaultGroovyMethods.setMetaClass(instance, this);
        }

        public Object getProperty(Object instance, String property) {
            if ("mixinOwner".equals(property))
              return owner.get();

            return super.getProperty(instance, property);
        }
    }
}
