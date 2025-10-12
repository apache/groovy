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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MixinInMetaClass {

    private final ExpandoMetaClass emc;
    private final CachedClass mixinClass;
    private final CachedConstructor mixinConstructor;
    private final Map<Object, Object> mixinAssociations =
        new ManagedIdentityConcurrentMap<>(ManagedIdentityConcurrentMap.ReferenceType.SOFT);

    public MixinInMetaClass(final ExpandoMetaClass emc, final CachedClass mixinClass) {
        this.emc = emc;
        this.mixinClass = mixinClass;
        this.mixinConstructor = findDefaultConstructor(mixinClass);

        emc.addMixinClass(this);
    }

    private static CachedConstructor findDefaultConstructor(final CachedClass mixinClass) {
        for (CachedConstructor ctor : mixinClass.getConstructors()) {
            if (ctor.isPublic() && ctor.getParameterTypes().length == 0) {
                return ctor;
            }
        }

        throw new GroovyRuntimeException("No default constructor for class " + mixinClass.getName() + "! Can't be mixed in.");
    }

    public synchronized Object getMixinInstance(final Object object) {
        return mixinAssociations.computeIfAbsent(object, (final Object owner) -> {
            var mixinInstance = mixinConstructor.invoke(MetaClassHelper.EMPTY_ARRAY);
            new MixedInMetaClass(mixinInstance, owner);
            return mixinInstance;
        });
    }

    public synchronized void setMixinInstance(final Object object, final Object mixinInstance) {
        if (mixinInstance != null) {
            mixinAssociations.put(object, mixinInstance);
        } else {
            mixinAssociations.remove(object);
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

        if (self instanceof HandleMetaClass hmc) {
            self = (MetaClass) hmc.replaceDelegate();
        }

        if (!(self instanceof ExpandoMetaClass)) {
            if (self instanceof DelegatingMetaClass dmc && dmc.getAdaptee() instanceof ExpandoMetaClass) {
                self = dmc.getAdaptee();
            } else {
                throw new GroovyRuntimeException("Can't mixin methods to meta class: " + self);
            }
        }

        ExpandoMetaClass mc = (ExpandoMetaClass) self;
        List<MetaMethod> toRegister = new ArrayList<>();
        for (Class<?> categoryClass : categoryClasses) {
            final CachedClass cachedCategoryClass = ReflectionCache.getCachedClass(categoryClass);
            final MixinInMetaClass mixin = new MixinInMetaClass(mc, cachedCategoryClass);

            final MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(categoryClass);
            for (MetaProperty mp : metaClass.getProperties()) {
                if (self.getMetaProperty(mp.getName()) == null) {
                    mc.registerBeanProperty(mp.getName(), new MixinInstanceMetaProperty(mp, mixin));
                }
            }
            for (MetaProperty mp : cachedCategoryClass.getFields()) {
                if (self.getMetaProperty(mp.getName()) == null) {
                    mc.registerBeanProperty(mp.getName(), new MixinInstanceMetaProperty(mp, mixin));
                }
            }
            for (MetaMethod method : metaClass.getMethods()) {
                if (!method.isPublic())
                    continue;

                if (method instanceof CachedMethod && method.isSynthetic())
                    continue;

                if (method instanceof CachedMethod cachedMethod && hasAnnotation(cachedMethod, Internal.class))
                    continue;

                if (method.isStatic()) {
                    if (method instanceof CachedMethod cachedMethod)
                        staticMethod(self, toRegister, cachedMethod);
                } else if (method.getDeclaringClass().getTheClass() != Object.class || "toString".equals(method.getName())) {
                  //if (self.pickMethod(method.getName(), method.getNativeParameterTypes()) == null) {
                        toRegister.add(new MixinInstanceMetaMethod(method, mixin));
                  //}
                }
            }
        }

        for (MetaMethod mm : toRegister) {
            if (mm.getDeclaringClass().isAssignableFrom(selfClass)) {
                mc.registerInstanceMethod(mm);
            } else {
                mc.registerSubclassInstanceMethod(mm);
            }
        }
    }

    private static boolean hasAnnotation(final CachedMethod method, final Class<Internal> annotationClass) {
        return method.getAnnotation(annotationClass) != null;
    }

    private static void staticMethod(final MetaClass self, List<MetaMethod> mm, final CachedMethod method) {
        CachedClass[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0) {
            Class<?> selfClass = self.getTheClass();
            if (paramTypes[0].isAssignableFrom(selfClass)) {
                if (paramTypes[0].getTheClass() == selfClass) {
                    mm.add(new NewInstanceMetaMethod(method));
                } else {
                    mm.add(new NewInstanceMetaMethod(method) {
                        @Override
                        public CachedClass getDeclaringClass() {
                            return ReflectionCache.getCachedClass(selfClass);
                        }
                    });
                }
            } else if (selfClass.isAssignableFrom(paramTypes[0].getTheClass())) {
                mm.add(new NewInstanceMetaMethod(method));
            }
        }
    }

    @Override
    public boolean equals(final Object that) {
        return that == this
            || (that instanceof MixinInMetaClass mc
                && Objects.equals(mixinClass, mc.mixinClass));
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (emc != null ? emc.hashCode() : 0);
        result = 31 * result + (mixinClass != null ? mixinClass.hashCode() : 0);
        result = 31 * result + (mixinConstructor != null ? mixinConstructor.hashCode() : 0);
        return result;
    }
}
