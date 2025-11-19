/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.reflection;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.transform.Internal;
import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap;
import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.metaclass.MixedInMetaClass;
import org.codehaus.groovy.runtime.metaclass.MixinInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MixinInstanceMetaProperty;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.hash;

public class MixinInMetaClass {
    final ExpandoMetaClass emc;
    final CachedClass mixinClass;
    final CachedConstructor constructor;
    private final Map<Object, Object> managedIdentityConcurrentMap =
        new ManagedIdentityConcurrentMap<>(ManagedIdentityConcurrentMap.ReferenceType.SOFT);

    public MixinInMetaClass(ExpandoMetaClass emc, CachedClass mixinClass) {
        this.emc = emc;
        this.mixinClass = mixinClass;
        this.constructor = findDefaultConstructor(mixinClass);

        emc.addMixinClass(this);
    }

    private static CachedConstructor findDefaultConstructor(CachedClass mixinClass) {
        for (CachedConstructor cc : mixinClass.getConstructors()) {
            if (cc.isPublic() && cc.getParameterTypes().length == 0) {
                return cc;
            }
        }

        throw new GroovyRuntimeException("No default constructor for class " + mixinClass.getName() + "! Can't be mixed in.");
    }

    public synchronized Object getMixinInstance(Object object) {
        Object mixinInstance = managedIdentityConcurrentMap.get(object);
        if (mixinInstance == null) {
            mixinInstance = constructor.invoke(MetaClassHelper.EMPTY_ARRAY);
            new MixedInMetaClass(mixinInstance, object);
            managedIdentityConcurrentMap.put(object, mixinInstance);
        }
        return mixinInstance;
    }

    public synchronized void setMixinInstance(Object object, Object mixinInstance) {
        if (mixinInstance == null) {
            managedIdentityConcurrentMap.remove(object);
        } else {
            managedIdentityConcurrentMap.put(object, mixinInstance);
        }
    }

    public CachedClass getInstanceClass() {
        return emc.getTheCachedClass();
    }

    public CachedClass getMixinClass() {
        return mixinClass;
    }

    public static void mixinClassesToMetaClass(MetaClass self, final List<Class> categoryClasses) {
        final Class<?> selfClass = self.getTheClass();

        if (self instanceof HandleMetaClass) {
            self = (MetaClass) ((HandleMetaClass) self).replaceDelegate();
        }

        if (!(self instanceof ExpandoMetaClass)) {
            if (self instanceof DelegatingMetaClass && ((DelegatingMetaClass) self).getAdaptee() instanceof ExpandoMetaClass) {
                self = ((DelegatingMetaClass) self).getAdaptee();
            } else {
                throw new GroovyRuntimeException("Can't mixin methods to meta class: " + self);
            }
        }

        ExpandoMetaClass emc = (ExpandoMetaClass) self;
        List<MetaMethod> toRegister = new ArrayList<>();
        for (Class<?> categoryClass : categoryClasses) {
            final CachedClass cachedCategoryClass = ReflectionCache.getCachedClass(categoryClass);
            final MixinInMetaClass mixin = new MixinInMetaClass(emc, cachedCategoryClass);

            final MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(categoryClass);
            for (MetaProperty mp : metaClass.getProperties()) {
                if (emc.getMetaProperty(mp.getName()) == null) {
                    emc.registerBeanProperty(mp.getName(), new MixinInstanceMetaProperty(mp, mixin));
                }
            }
            for (MetaProperty mp : cachedCategoryClass.getFields()) {
                if (emc.getMetaProperty(mp.getName()) == null) {
                    emc.registerBeanProperty(mp.getName(), new MixinInstanceMetaProperty(mp, mixin));
                }
            }
            for (MetaMethod method : metaClass.getMethods()) {
                if (!method.isPublic())
                    continue;

                if (method instanceof CachedMethod && method.isSynthetic())
                    continue;

                if (method instanceof CachedMethod && hasAnnotation((CachedMethod) method, Internal.class))
                    continue;

                if (method.isStatic()) {
                    if (method instanceof CachedMethod)
                        staticMethod(self, toRegister, (CachedMethod) method);
                } else if (method.getDeclaringClass().getTheClass() != Object.class || "toString".equals(method.getName())) {
                  //if (emc.pickMethod(method.getName(), method.getNativeParameterTypes()) == null) {
                        toRegister.add(new MixinInstanceMetaMethod(method, mixin));
                  //}
                }
            }
        }

        for (MetaMethod mm : toRegister) {
            if (mm.getDeclaringClass().isAssignableFrom(selfClass)) {
                emc.registerInstanceMethod(mm);
            } else {
                emc.registerSubclassInstanceMethod(mm);
            }
        }
    }

    private static boolean hasAnnotation(final CachedMethod method, final Class<? extends Annotation> annotationClass) {
        return method.getAnnotation(annotationClass) != null;
    }

    private static void staticMethod(final MetaClass self, final List<MetaMethod> arr, final CachedMethod method) {
        CachedClass[] paramTypes = method.getParameterTypes();

        if (paramTypes.length == 0)
            return;

        NewInstanceMetaMethod metaMethod;
        if (paramTypes[0].isAssignableFrom(self.getTheClass())) {
            if (paramTypes[0].getTheClass() == self.getTheClass())
                metaMethod = new NewInstanceMetaMethod(method);
            else
                metaMethod = new NewInstanceMetaMethod(method) {
                    @Override
                    public CachedClass getDeclaringClass() {
                        return ReflectionCache.getCachedClass(self.getTheClass());
                    }
                };
            arr.add(metaMethod);
        } else {
            if (self.getTheClass().isAssignableFrom(paramTypes[0].getTheClass())) {
                metaMethod = new NewInstanceMetaMethod(method);
                arr.add(metaMethod);
            }
        }
    }

    @Override
    public boolean equals(final Object that) {
        return (that == this)
            || (that instanceof MixinInMetaClass
                && emc.equals(((MixinInMetaClass) that).emc)
                && mixinClass.equals(((MixinInMetaClass) that).mixinClass)
                && constructor.equals(((MixinInMetaClass) that).constructor));
    }

    @Override
    public int hashCode() {
        return hash(emc, mixinClass, constructor);
    }
}
