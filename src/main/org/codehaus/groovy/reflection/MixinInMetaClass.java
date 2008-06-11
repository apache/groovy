package org.codehaus.groovy.reflection;

import groovy.lang.*;

import java.lang.reflect.Modifier;
import java.util.WeakHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

public class MixinInMetaClass extends WeakHashMap {
    final CachedClass instanceClass;
    final CachedClass mixinClass;
    final CachedConstructor constructor;

    public MixinInMetaClass(CachedClass cachedClass, CachedClass mixinClass) {
        this.instanceClass = cachedClass;
        this.mixinClass = mixinClass;

        for(CachedConstructor constr : mixinClass.getConstructors()) {
            if (!Modifier.isPublic(constr.getModifiers()))
              continue;

            CachedClass[] classes = constr.getParameterTypes();
            if (classes.length != 0)
                continue;

            constructor = constr;
            return;
        }

        throw new GroovyRuntimeException("No default constructor for class " + mixinClass.getName());
    }

    public synchronized Object getMixinInstance (Object object) {
        Object mixinInstance = get(object);
        if (mixinInstance == null) {
            mixinInstance = constructor.invoke(MetaClassHelper.EMPTY_ARRAY);
            put (object, mixinInstance);
        }
        return mixinInstance;
    }

    public CachedClass getInstanceClass() {
        return instanceClass;
    }

    public static void mixinClassesToMetaClass(MetaClass self, List categoryClasses) {
        final Class selfClass = self.getTheClass();
        ArrayList arr = findCategoryMethods(self, categoryClasses);

        if (arr.isEmpty())
          return;

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
        for (Object res : arr) {
            final MetaMethod metaMethod = (MetaMethod) res;
            if (metaMethod.getDeclaringClass().isAssignableFrom(selfClass))
              mc.registerInstanceMethod(metaMethod);
            else {
              mc.registerSubclassInstanceMethod(metaMethod);
            }
        }
    }

    public static ArrayList findCategoryMethods(final MetaClass self, List categoryClasses) {
        ArrayList arr = new ArrayList(4);
        for (Iterator it = categoryClasses.iterator(); it.hasNext(); ) {
            Class categoryClass = (Class) it.next();
            CachedMethod[] methods = ReflectionCache.getCachedClass(categoryClass).getMethods();

            MixinInMetaClass mixin = null;
            for (int i = 0; i < methods.length; i++) {
                CachedMethod method = methods[i];
                final int mod = method.getModifiers();
                if (Modifier.isPublic(mod) && !method.getCachedMethod().isSynthetic()) {
                    if (Modifier.isStatic(mod)) {
                        staticMethod(self, arr, method);
                    }
                    else {
                        if(self.pickMethod(method.getName(), method.getNativeParameterTypes()) == null) {
                            if (mixin == null)
                              mixin = new MixinInMetaClass(ReflectionCache.getCachedClass(self.getTheClass()), ReflectionCache.getCachedClass(categoryClass));
                            arr.add(new MixinInstanceMetaMethod(method, mixin));
                        }
                    }
                }
            }
        }
        return arr;
    }

    private static void staticMethod(final MetaClass self, ArrayList arr, final CachedMethod method) {
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
}
