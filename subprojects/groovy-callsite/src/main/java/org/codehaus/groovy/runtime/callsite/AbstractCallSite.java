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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaProperty;
import org.apache.groovy.internal.metaclass.GroovyObjectOverrides;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.ArrayUtil;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

/**
 * Base class for all classic (non-{@code invokedynamic}) call sites.
 * <p>
 * Prefer invokedynamic call sites for new code. Planned for
 * deprecation/removal in a future Groovy version.
 */
public class AbstractCallSite implements CallSite {

    /**
     * Position of this site in {@link #array}.
     */
    protected final int index;

    /**
     * Name of the call or property this site resolves.
     */
    protected final String name;

    /**
     * {@link CallSiteArray} that owns this site and stores it at {@link #index}.
     */
    protected final CallSiteArray array;

    /**
     * Creates a site for {@code name} at {@code index} in {@code array}.
     */
    public AbstractCallSite(final CallSiteArray array, final int index, final String name) {
        this.name = name;
        this.index = index;
        this.array = array;
    }

    /**
     * Creates a site that copies the name, index, and owning array from {@code prev}.
     */
    public AbstractCallSite(final CallSite prev) {
        this.name = prev.getName();
        this.index = prev.getIndex();
        this.array = prev.getArray();
    }

    /**
     * Returns this site's position in {@link #getArray()}.
     */
    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Returns the {@link CallSiteArray} that owns this site.
     */
    @Override
    public CallSiteArray getArray() {
        return array;
    }

    /**
     * Returns the call or property name this site resolves.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards {@code args} to {@link #call(Object, Object[])}.
     */
    @Override
    public final Object callSafe(final Object receiver, final Object[] args) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, args);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards to {@link #call(Object)} with no arguments.
     */
    @Override
    public final Object callSafe(final Object receiver) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards {@code arg1} to {@link #call(Object, Object)}.
     */
    @Override
    public final Object callSafe(final Object receiver, final Object arg1) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards {@code arg1} and {@code arg2} to
     * {@link #call(Object, Object, Object)}.
     */
    @Override
    public final Object callSafe(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards {@code arg1}, {@code arg2}, and {@code arg3} to
     * {@link #call(Object, Object, Object, Object)}.
     */
    @Override
    public final Object callSafe(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards {@code arg1} through {@code arg4} to
     * {@link #call(Object, Object, Object, Object, Object)}.
     */
    @Override
    public Object callSafe(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3, arg4);
    }

    /**
     * Forwards this site, {@code receiver}, and {@code args} to
     * {@link CallSiteArray#defaultCall(CallSite, Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCall(this, receiver, args);
    }

    /**
     * Forwards to {@link #call(Object, Object[])} with an empty argument
     * array ({@link CallSiteArray#NOPARAM}).
     */
    @Override
    public Object call(final Object receiver) throws Throwable {
        return call(receiver, CallSiteArray.NOPARAM);
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} to
     * {@link CallSite#call(Object, Object)} on that site. Otherwise packs
     * {@code arg1} into an array and forwards to {@link #call(Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1);
        }
        return call(receiver, ArrayUtil.createArray(arg1));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, and {@code arg2} to
     * {@link CallSite#call(Object, Object, Object)} on that site.
     * Otherwise packs those arguments and forwards to
     * {@link #call(Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, {@code arg2}, and
     * {@code arg3} to {@link CallSite#call(Object, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #call(Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2, arg3);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} through {@code arg4} to
     * {@link CallSite#call(Object, Object, Object, Object, Object)} on that
     * site. Otherwise packs those arguments and forwards to
     * {@link #call(Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2, arg3, arg4);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    /**
     * Forwards this site, the {@link GroovyObject} {@code receiver}, and
     * {@code args} to
     * {@link CallSiteArray#defaultCallCurrent(CallSite, GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(final GroovyObject receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallCurrent(this, receiver, args);
    }

    /**
     * Forwards to {@link #callCurrent(GroovyObject, Object[])} with an empty
     * argument array ({@link CallSiteArray#NOPARAM}).
     */
    @Override
    public Object callCurrent(final GroovyObject receiver) throws Throwable {
        return callCurrent(receiver, CallSiteArray.NOPARAM);
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} to
     * {@link CallSite#callCurrent(GroovyObject, Object)} on that site.
     * Otherwise packs {@code arg1} into an array and forwards to
     * {@link #callCurrent(GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, and {@code arg2} to
     * {@link CallSite#callCurrent(GroovyObject, Object, Object)} on that
     * site. Otherwise packs those arguments and forwards to
     * {@link #callCurrent(GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, {@code arg2}, and
     * {@code arg3} to
     * {@link CallSite#callCurrent(GroovyObject, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #callCurrent(GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2, arg3);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} through {@code arg4} to
     * {@link CallSite#callCurrent(GroovyObject, Object, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #callCurrent(GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2, arg3, arg4);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    /**
     * Forwards this site, the {@link Class} {@code receiver}, and {@code args}
     * to {@link CallSiteArray#defaultCallStatic(CallSite, Class, Object[])}.
     */
    @Override
    public Object callStatic(final Class receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    /**
     * Forwards to {@link #callStatic(Class, Object[])} with an empty argument
     * array ({@link CallSiteArray#NOPARAM}).
     */
    @Override
    public Object callStatic(final Class receiver) throws Throwable {
        return callStatic(receiver, CallSiteArray.NOPARAM);
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} to
     * {@link CallSite#callStatic(Class, Object)} on that site. Otherwise
     * packs {@code arg1} into an array and forwards to
     * {@link #callStatic(Class, Object[])}.
     */
    @Override
    public Object callStatic(final Class receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, and {@code arg2} to
     * {@link CallSite#callStatic(Class, Object, Object)} on that site.
     * Otherwise packs those arguments and forwards to
     * {@link #callStatic(Class, Object[])}.
     */
    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, {@code arg2}, and
     * {@code arg3} to
     * {@link CallSite#callStatic(Class, Object, Object, Object)} on that
     * site. Otherwise packs those arguments and forwards to
     * {@link #callStatic(Class, Object[])}.
     */
    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2, arg3);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} through {@code arg4} to
     * {@link CallSite#callStatic(Class, Object, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #callStatic(Class, Object[])}.
     */
    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2, arg3, arg4);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    /**
     * Forwards this site, the class to construct ({@code receiver}), and
     * {@code args} to
     * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
     */
    @Override
    public Object callConstructor(final Object receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }

    /**
     * Forwards to {@link #callConstructor(Object, Object[])} with an empty
     * argument array ({@link CallSiteArray#NOPARAM}).
     */
    @Override
    public Object callConstructor(final Object receiver) throws Throwable {
        return callConstructor(receiver, CallSiteArray.NOPARAM);
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} to
     * {@link CallSite#callConstructor(Object, Object)} on that site.
     * Otherwise packs {@code arg1} into an array and forwards to
     * {@link #callConstructor(Object, Object[])}.
     */
    @Override
    public Object callConstructor(final Object receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, and {@code arg2} to
     * {@link CallSite#callConstructor(Object, Object, Object)} on that site.
     * Otherwise packs those arguments and forwards to
     * {@link #callConstructor(Object, Object[])}.
     */
    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver}, {@code arg1}, {@code arg2}, and
     * {@code arg3} to
     * {@link CallSite#callConstructor(Object, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #callConstructor(Object, Object[])}.
     */
    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2, arg3);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    /**
     * When {@link #array} holds a different site at {@link #index},
     * forwards {@code receiver} and {@code arg1} through {@code arg4} to
     * {@link CallSite#callConstructor(Object, Object, Object, Object, Object)}
     * on that site. Otherwise packs those arguments and forwards to
     * {@link #callConstructor(Object, Object[])}.
     */
    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2, arg3, arg4);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    static boolean noCoerce(final ParameterTypes metaMethod, final Object[] args) {
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

    static boolean noWrappers(final Object[] args) {
        for (int i = 0; i != args.length; i += 1)
            if (args[i] instanceof Wrapper)
                return false;
        return true;
    }

    /**
     * Reads the property named by {@link #getName()} from {@code receiver}
     * through the site {@link #acceptGetProperty(Object)} returns.
     */
    @Override
    public Object callGetProperty(final Object receiver) throws Throwable {
        return acceptGetProperty(receiver).getProperty(receiver);
    }

    /**
     * Reads the property named by {@link #getName()}. A {@code null}
     * receiver is read from {@link NullObject} through
     * {@link InvokerHelper#getProperty(Object, String)}, and a
     * {@link GroovyRuntimeException} from that read is rethrown as the
     * throwable from {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Any other receiver is read through the site
     * {@link #acceptGroovyObjectGetProperty(Object)} returns.
     */
    @Override
    public Object callGroovyObjectGetProperty(final Object receiver) throws Throwable {
        if (receiver == null) {
            try {
                return InvokerHelper.getProperty(NullObject.getNullObject(), name);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return acceptGroovyObjectGetProperty(receiver).getProperty(receiver);
        }
    }

    /**
     * Returns the property-read site {@link #createGetPropertySite(Object)}
     * selects for {@code receiver}.
     */
    public CallSite acceptGetProperty(final Object receiver) {
        return createGetPropertySite(receiver);
    }

    /**
     * Returns the property-read site
     * {@link #createGroovyObjectGetPropertySite(Object)} selects for
     * {@code receiver}.
     */
    public CallSite acceptGroovyObjectGetProperty(final Object receiver) {
        return createGroovyObjectGetPropertySite(receiver);
    }

    /**
     * Selects a property-read site for {@code receiver}. A {@code null}
     * receiver yields a {@link NullCallSite}. A {@link GroovyObject} is
     * passed to {@link #createGroovyObjectGetPropertySite(Object)}. A
     * {@link Class} yields a class metaclass site. Any other receiver
     * yields a POJO metaclass site.
     */
    protected final CallSite createGetPropertySite(final Object receiver) {
        if (receiver == null) {
            return new NullCallSite(this);
        } else if (receiver instanceof GroovyObject) {
            return createGroovyObjectGetPropertySite(receiver);
        } else if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class<?>) receiver);
        }
        return createPojoMetaClassGetPropertySite(receiver);
    }

    /**
     * Selects a property-read site for {@code receiver}. When
     * {@link GroovyObjectOverrides#hasCustomGetProperty(Class)} is
     * {@code false} and {@code receiver}, cast to {@link GroovyObject},
     * has a {@link MetaClassImpl} metaclass, returns a POGO metaclass site.
     * Otherwise a {@link Class} receiver yields a class metaclass site, and
     * any other receiver yields a {@link PogoGetPropertySite} that calls
     * the object's own {@code getProperty}.
     */
    protected final CallSite createGroovyObjectGetPropertySite(final Object receiver) {
        Class<?> aClass = receiver.getClass();
        // hasCustomGetProperty, the criterion of the invokedynamic sites, not the
        // overridesGetProperty of ScriptBytecodeAdapter.getProperty; the two differ on purpose
        // (see GroovyObjectOverrides). A getProperty that is synthetic or marked @Internal, as
        // the GroovyObject default is, leaves the read to the metaclass. A class that cannot be
        // inspected counts as having its own, as on the invokedynamic sites, so it is not given
        // a metaclass site that would skip a real override: the object's getProperty answers,
        // as it did when the earlier lookup threw NoSuchMethodException
        if (!GroovyObjectOverrides.hasCustomGetProperty(aClass) && ((GroovyObject) receiver).getMetaClass() instanceof MetaClassImpl)
            return createPogoMetaClassGetPropertySite((GroovyObject) receiver);
        if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class<?>) receiver);
        } else {
            return createPogoGetPropertySite(aClass);
        }
    }

    /**
     * Throws {@link UnsupportedOperationException}. Property sites override
     * this to read the property named by {@link #getName()}.
     */
    @Override
    public Object getProperty(final Object receiver) throws Throwable {
        throw new UnsupportedOperationException();
    }

    private CallSite createPojoMetaClassGetPropertySite(final Object receiver) {
        final int version = ClassInfo.getClassInfo(receiver.getClass()).getVersion();
        final MetaClass metaClass = InvokerHelper.getMetaClass(receiver);

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PojoMetaClassGetPropertySite(this);
        } else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(receiver.getClass(), receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePojoFieldSite(this, (MetaClassImpl) metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePojoPropertySite(this, (MetaClassImpl) metaClass, effective);
            } else {
                site = new PojoMetaClassGetPropertySite(this);
            }
        }

        installGetPropertySiteIfCurrent(site, receiver.getClass(), version);
        return site;
    }

    /**
     * Installs the freshly created property site only when the receiver
     * class's MOP version did not move while the site was being created
     * (GROOVY-12307): a change landing mid-creation can pair a pre-change
     * property selection with a post-change version stamp (or, for the
     * identity-guarded Pogo sites, with a still-current MetaClass reference),
     * which would dispatch stale until an unrelated future change. The site
     * still serves the current invocation; the next call re-creates.
     */
    private void installGetPropertySiteIfCurrent(final CallSite site, final Class<?> receiverClass, final int preVersion) {
        if (ClassInfo.getClassInfo(receiverClass).getVersion() == preVersion) {
            array.array[index] = site;
        }
    }

    private CallSite createClassMetaClassGetPropertySite(final Class<?> aClass) {
        CallSite site = new ClassMetaClassGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    private CallSite createPogoMetaClassGetPropertySite(final GroovyObject receiver) {
        final int version = ClassInfo.getClassInfo(receiver.getClass()).getVersion();
        MetaClass metaClass = receiver.getMetaClass();

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PogoMetaClassGetPropertySite(this, metaClass);
        } else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(this.array.owner, receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePogoFieldSite(this, metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePogoPropertySite(this, metaClass, effective);
            } else {
                site = new PogoMetaClassGetPropertySite(this, metaClass);
            }
        }

        installGetPropertySiteIfCurrent(site, receiver.getClass(), version);
        return site;
    }

    private CallSite createPogoGetPropertySite(final Class<?> aClass) {
        CallSite site = new PogoGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards to {@link #callGetProperty(Object)}.
     */
    @Override
    public final Object callGetPropertySafe(final Object receiver) throws Throwable {
        if (receiver == null) return null;
        return callGetProperty(receiver);
    }

    /**
     * Returns {@code null} when {@code receiver} is {@code null};
     * otherwise forwards to {@link #callGroovyObjectGetProperty(Object)}.
     */
    @Override
    public final Object callGroovyObjectGetPropertySafe(final Object receiver) throws Throwable {
        if (receiver == null) return null;
        return callGroovyObjectGetProperty(receiver);
    }
}
