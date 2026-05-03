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

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

/**
 * Manages the integration of mixin classes into expandable metaclasses.
 * <p>
 * Associates a mixin class with instances of an expandable metaclass, enabling per-instance
 * mixin functionality. Handles mixin instance creation and registration of mixin methods/properties.
 */
public class MixinInMetaClass {

    private final ExpandoMetaClass emc;
    private final CachedClass mixinClass;
    private final CachedConstructor mixinConstructor;
    private final Map<Object, Object> mixinAssociations =
        new ManagedIdentityConcurrentMap<>(ManagedIdentityConcurrentMap.ReferenceType.SOFT);

    private MixinInMetaClass(final ExpandoMetaClass emc, final CachedClass mixinClass) {
        this.emc = requireNonNull(emc);
        this.mixinClass = requireNonNull(mixinClass);
        this.mixinConstructor = stream(mixinClass.getConstructors()).filter(it -> it.isPublic() && it.getParameterTypes().length == 0).findFirst()
                .orElseThrow(() -> new GroovyRuntimeException("No default constructor for class " + mixinClass.getName() + "! Can't be mixed in."));
    }

    /**
     * Returns or creates a mixin instance for the given object.
     * Creates a new mixin instance on first access, then caches and reuses it for the same object.
     *
     * @param object the object to associate with a mixin instance
     * @return the mixin instance for this object
     */
    public synchronized Object getMixinInstance(final Object object) {
        return mixinAssociations.computeIfAbsent(object, (final Object owner) -> {
            var mixinInstance = mixinConstructor.invoke(MetaClassHelper.EMPTY_ARRAY);
            new MixedInMetaClass(mixinInstance, owner);
            return mixinInstance;
        });
    }

    /**
     * Sets or clears the mixin instance associated with an object.
     * Pass {@code null} to remove the mixin association.
     *
     * @param object the object to associate or disassociate with a mixin
     * @param mixinInstance the mixin instance to associate, or {@code null} to clear
     */
    public synchronized void setMixinInstance(final Object object, final Object mixinInstance) {
        if (mixinInstance != null) {
            mixinAssociations.put(object, mixinInstance);
        } else {
            mixinAssociations.remove(object);
        }
    }

    /**
     * Returns the cached class for the expandable metaclass that owns this mixin.
     *
     * @return the cached class for the instance
     */
    public CachedClass getInstanceClass() {
        return emc.getTheCachedClass();
    }

    /**
     * Returns the cached class of the mixin class itself.
     *
     * @return the cached mixin class
     */
    public CachedClass getMixinClass() {
        return mixinClass;
    }

    /**
     * Integrates mixin classes into the specified metaclass.
     * Each mixin class provides methods that are mixed into the target class.
     *
     * @param self the metaclass to mix methods into
     * @param categoryClasses the classes providing mixin methods
     */
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

        ExpandoMetaClass emc = (ExpandoMetaClass) self;
        List<MetaMethod> toRegister = new ArrayList<>();
        for (Class<?> categoryClass : categoryClasses) {
            final CachedClass cachedCategoryClass = ReflectionCache.getCachedClass(categoryClass);
            final MixinInMetaClass mixin = new MixinInMetaClass(emc, cachedCategoryClass);
            if (!emc.addMixinClass(mixin)) {
                continue; // GROOVY-11775
            }

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

                if (method instanceof CachedMethod cachedMethod && hasAnnotation(cachedMethod, Internal.class))
                    continue;

                if (method.isStatic()) {
                    if (method instanceof CachedMethod cachedMethod)
                        staticMethod(self, toRegister, cachedMethod);
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

    /**
     * Checks equality with another object based on the expandable metaclass and mixin class.
     *
     * @param that the object to compare with
     * @return {@code true} if this mixin represents the same metaclass and mixin; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object that) {
        return (that == this)
            || (that instanceof MixinInMetaClass mmc
                && emc.equals(mmc.emc) && mixinClass.equals(mmc.mixinClass));
    }

    /**
     * Returns the hash code based on the mixin class.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return mixinClass.hashCode(); // GROOVY-11775
    }
}
