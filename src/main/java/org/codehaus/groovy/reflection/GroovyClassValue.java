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

/** Abstraction for Java version dependent ClassValue implementations.
 * @see java.lang.ClassValue
 *
 * @param <T>
 */
public interface GroovyClassValue<T> {
	
	interface ComputeValue<T>{
		T computeValue(Class<?> type);
	}
	
	T get(Class<?> type);

	void remove(Class<?> type);

	/**
	 * Whether an association's value can be collected while its key class is
	 * still alive (GROOVY-12281, {@code -Dgroovy.use.classvalue=soft}). The
	 * default — a value lives exactly as long as its key class — answers
	 * {@code false}. Implementations answering {@code true} must honor
	 * {@link #pin} so callers can exempt values whose state cannot be rebuilt
	 * by recomputation.
	 *
	 * @return {@code true} if values may be collected before their key class
	 */
	default boolean valuesReclaimable() {
		return false;
	}

	/**
	 * Keeps {@code value} strongly reachable <em>from its own key's
	 * association</em> until {@link #unpin} or {@link #remove}. The value then
	 * lives exactly as long as {@code type} — like a plain
	 * {@code java.lang.ClassValue} association: an immortal key retains it,
	 * a collectible key releases it together with its loader. Implementations
	 * must not root the value globally, which would extend a collectible key's
	 * lifetime to the runtime's. No-op unless {@link #valuesReclaimable()}.
	 *
	 * @param type  the key class
	 * @param value the current value for {@code type}
	 */
	default void pin(Class<?> type, T value) {
	}

	/**
	 * Reverts {@link #pin}: the association holds {@code value} reclaimably
	 * again. No-op when {@code value} is not the currently pinned value, and
	 * unless {@link #valuesReclaimable()}.
	 *
	 * @param type  the key class
	 * @param value the value to release
	 */
	default void unpin(Class<?> type, T value) {
	}
}
