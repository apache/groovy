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
package org.apache.groovy.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry of services that implement the {@link GroovyRunner} interface.
 * <p>
 * This registry makes use of the {@link ServiceLoader} facility. The
 * preferred method for registering new {@link GroovyRunner} providers
 * is to place them in a provider-configuration file in the resource
 * directory {@code META-INF/services}. The preferred method for accessing
 * the registered runners is by making use of the {@code Iterable}
 * interface using an enhanced for-loop.
 * <p>
 * For compatibility with previous versions, this registry implements the
 * {@link Map} interface. All {@code null} keys and values will be ignored
 * and no exception thrown, except where noted.
 * <p>
 * By default the registry contains runners that are capable of running
 * {@code JUnit 3} and {@code JUnit 4} test classes if those libraries
 * are available to the class loader.
 *
 * @since 2.5.0
 */
public class GroovyRunnerRegistry implements Map<String, GroovyRunner>, Iterable<GroovyRunner> {

    /*
     * Implementation notes
     *
     * GroovySystem stores a static reference to this instance so it is
     * important to make it fast to create as possible. GroovyRunners are
     * only used to run scripts that GroovyShell does not already know how
     * to run so defer service loading until requested via the iterator or
     * map access methods.
     *
     * The Map interface is for compatibility with the original definition
     * of GroovySystem.RUNNER_REGISTRY. At some point it would probably
     * make sense to dispense with associating a String key with a runner
     * and provide register/unregister methods instead of the Map
     * interface.
     */

    private static final GroovyRunnerRegistry INSTANCE = new GroovyRunnerRegistry();

    private static final Logger LOG = Logger.getLogger(GroovyRunnerRegistry.class.getName());

    // Lazily initialized and loaded, should be accessed internally using getMap()
    private volatile Map<String, GroovyRunner> runnerMap;

    /*
     * Cached unmodifiable List used for iteration. Any method that mutates
     * the runnerMap must set to null to invalidate the cache. Volatile is
     * used because reads for DCL are faster than a lock/unlock.
     * The values are cached in order to speed up iteration and avoid
     * allocation of new collections on each call to the iterator.
     */
    private volatile List<GroovyRunner> cachedValues;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    /**
     * Returns a reference to the one and only registry instance.
     *
     * @return registry instance
     */
    public static GroovyRunnerRegistry getInstance() {
        return INSTANCE;
    }

    // package-private for use in testing to avoid calling ServiceLoader.load
    GroovyRunnerRegistry(Map<? extends String, ? extends GroovyRunner> runners) {
        // Preserve insertion order
        runnerMap = new LinkedHashMap<>();
        putAll(runners);
    }

    private GroovyRunnerRegistry() {
    }

    /**
     * Lazily initialize and load the backing Map. A {@link LinkedHashMap}
     * is used to preserve insertion order.
     * <p>
     * Do not call while holding a read lock.
     *
     * @return backing registry map
     */
    private Map<String, GroovyRunner> getMap() {
        Map<String, GroovyRunner> map = runnerMap;
        if (map == null) {
            writeLock.lock();
            try {
                if ((map = runnerMap) == null) {
                    runnerMap = map = new LinkedHashMap<>();
                    load(null);
                }
            } finally {
                writeLock.unlock();
            }
        }
        return map;
    }

    /**
     * Loads {@link GroovyRunner} instances using the {@link ServiceLoader} facility.
     *
     * @param classLoader used to locate provider-configuration files and classes
     */
    public void load(ClassLoader classLoader) {
        Map<String, GroovyRunner> map = runnerMap; // direct read
        if (map == null) {
            map = getMap(); // initialize and load (recursive call), result ignored
            if (classLoader == null) {
                // getMap() already loaded using a null classloader
                return;
            }
        }
        writeLock.lock();
        try {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            cachedValues = null;
            loadDefaultRunners();
            loadWithLock(classLoader);
        } catch (SecurityException se) {
            LOG.log(Level.WARNING, "Failed to get the context ClassLoader", se);
        } catch (ServiceConfigurationError sce) {
            LOG.log(Level.WARNING, "Failed to load GroovyRunner services from ClassLoader " + classLoader, sce);
        } finally {
            writeLock.unlock();
        }
    }

    private void loadDefaultRunners() {
        register(DefaultRunners.junit3TestRunner());
        register(DefaultRunners.junit3SuiteRunner());
        register(DefaultRunners.junit4TestRunner());
    }

    private void loadWithLock(ClassLoader classLoader) {
        ServiceLoader<GroovyRunner> serviceLoader = ServiceLoader.load(GroovyRunner.class, classLoader);
        for (GroovyRunner runner : serviceLoader) {
            register(runner);
        }
    }

    /**
     * Registers the given instance with the registry. This is
     * equivalent to {@link #put(String, GroovyRunner)} with a
     * {@code key} being set to {@code runner.getClass().getName()}.
     *
     * @param runner the instance to add to the registry
     */
    private void register(GroovyRunner runner) {
        put(runner.getClass().getName(), runner);
    }

    /**
     * Returns an iterator for all runners that are registered.
     * The returned iterator is a snapshot of the registry at
     * the time the iterator is created. This iterator does not
     * support removal.
     *
     * @return iterator for all registered runners
     */
    @Override
    public Iterator<GroovyRunner> iterator() {
        return values().iterator();
    }

    /**
     * Returns the number of registered runners.
     *
     * @return number of registered runners
     */
    @Override
    public int size() {
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns {@code true} if the registry contains no runners, else
     * {@code false}.
     *
     * @return {@code true} if no runners are registered
     */
    @Override
    public boolean isEmpty() {
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns {@code true} if a runner was registered with the
     * specified key.
     *
     * @param key for the registered runner
     * @return {@code true} if a runner was registered with given key
     */
    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns {@code true} if registry contains the given
     * runner instance.
     *
     * @param runner instance of a GroovyRunner
     * @return {@code true} if the given runner is registered
     */
    @Override
    public boolean containsValue(Object runner) {
        if (runner == null) {
            return false;
        }
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.containsValue(runner);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the registered runner for the specified key.
     *
     * @param key used to lookup the runner
     * @return the runner registered with the given key
     */
    @Override
    public GroovyRunner get(Object key) {
        if (key == null) {
            return null;
        }
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Registers a runner with the specified key.
     *
     * @param key to associate with the runner
     * @param runner the runner to register
     * @return the previously registered runner for the given key,
     *          if no runner was previously registered for the key
     *          then {@code null}
     */
    @Override
    public GroovyRunner put(String key, GroovyRunner runner) {
        if (key == null || runner == null) {
            return null;
        }
        Map<String, GroovyRunner> map = getMap();
        writeLock.lock();
        try {
            cachedValues = null;
            return map.put(key, runner);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a registered runner from the registry.
     *
     * @param key of the runner to remove
     * @return the runner instance that was removed, if no runner
     *          instance was removed then {@code null}
     */
    @Override
    public GroovyRunner remove(Object key) {
        if (key == null) {
            return null;
        }
        Map<String, GroovyRunner> map = getMap();
        writeLock.lock();
        try {
            cachedValues = null;
            return map.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds all entries from the given Map to the registry.
     * Any entries in the provided Map that contain a {@code null}
     * key or value will be ignored.
     *
     * @param m entries to add to the registry
     * @throws NullPointerException if the given Map is {@code null}
     */
    @Override
    public void putAll(Map<? extends String, ? extends GroovyRunner> m) {
        Map<String, GroovyRunner> map = getMap();
        writeLock.lock();
        try {
            cachedValues = null;
            for (Map.Entry<? extends String, ? extends GroovyRunner> entry : m.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clears all registered runners from the registry and resets
     * the registry so that it contains only the default set of
     * runners.
     */
    @Override
    public void clear() {
        Map<String, GroovyRunner> map = getMap();
        writeLock.lock();
        try {
            cachedValues = null;
            map.clear();
            loadDefaultRunners();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Set of all keys associated with registered runners.
     * This is a snapshot of the registry and any subsequent
     * registry changes will not be reflected in the set.
     *
     * @return an unmodifiable set of keys for registered runners
     */
    @Override
    public Set<String> keySet() {
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            if (map.isEmpty()) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(new LinkedHashSet<>(map.keySet()));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a collection of all registered runners.
     * This is a snapshot of the registry and any subsequent
     * registry changes will not be reflected in the collection.
     *
     * @return an unmodifiable collection of registered runner instances
     */
    @Override
    public Collection<GroovyRunner> values() {
        List<GroovyRunner> values = cachedValues;
        if (values == null) {
            Map<String, GroovyRunner> map = getMap();
            // racy, multiple threads may set cachedValues but rather have that than take a write lock
            readLock.lock();
            try {
                if ((values = cachedValues) == null) {
                    cachedValues = values = Collections.unmodifiableList(new ArrayList<>(map.values()));
                }
            } finally {
                readLock.unlock();
            }
        }
        return values;
    }

    /**
     * Returns a set of entries for registered runners.
     * This is a snapshot of the registry and any subsequent
     * registry changes will not be reflected in the set.
     *
     * @return an unmodifiable set of registered runner entries
     */
    @Override
    public Set<Entry<String, GroovyRunner>> entrySet() {
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            if (map.isEmpty()) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(new LinkedHashSet<>(map.entrySet()));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        Map<String, GroovyRunner> map = getMap();
        readLock.lock();
        try {
            return map.toString();
        } finally {
            readLock.unlock();
        }
    }

}
