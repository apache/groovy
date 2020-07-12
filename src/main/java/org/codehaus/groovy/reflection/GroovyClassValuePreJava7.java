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

import org.codehaus.groovy.util.Finalizable;
import org.codehaus.groovy.util.ManagedConcurrentMap;
import org.codehaus.groovy.util.ReferenceBundle;

/** Approximation of Java 7's {@link java.lang.ClassValue} that works on earlier versions of Java.
 * Note that this implementation isn't as good at Java 7's; it doesn't allow for some GC'ing that Java 7 would allow.
 * But, it's good enough for our use.
 *
 * @param <T>
 */
@Deprecated
class GroovyClassValuePreJava7<T> implements GroovyClassValue<T> {
	private static final ReferenceBundle weakBundle = ReferenceBundle.getWeakBundle();

	private class EntryWithValue extends ManagedConcurrentMap.EntryWithValue<Class<?>,T>{

		public EntryWithValue(GroovyClassValuePreJava7Segment segment, Class<?> key, int hash) {
			super(weakBundle, segment, key, hash, computeValue.computeValue(key));
		}

		@Override
		public void setValue(T value) {
			if(value!=null) super.setValue(value);
		}

		@Override
		public void finalizeReference() {
			T value = getValue();
			if (value instanceof Finalizable) {
				((Finalizable) value).finalizeReference();
			}
			super.finalizeReference();
		}
	}

	private class GroovyClassValuePreJava7Segment extends ManagedConcurrentMap.Segment<Class<?>,T> {

        private static final long serialVersionUID = 1289753977947029168L;

        GroovyClassValuePreJava7Segment(ReferenceBundle bundle, int initialCapacity) {
			super(bundle, initialCapacity);
		}

		@Override
		protected EntryWithValue createEntry(Class<?> key, int hash,
				T unused) {
			return new EntryWithValue(this, key, hash);
		}
	}

	private class GroovyClassValuePreJava7Map extends ManagedConcurrentMap<Class<?>,T> {

		public GroovyClassValuePreJava7Map() {
			super(weakBundle);
		}

		@Override
		protected GroovyClassValuePreJava7Segment createSegment(Object segmentInfo, int cap) {
			ReferenceBundle bundle = (ReferenceBundle) segmentInfo;
			if (bundle==null) throw new IllegalArgumentException("bundle must not be null ");
			return new GroovyClassValuePreJava7Segment(bundle, cap);
		}

	}

	private final ComputeValue<T> computeValue;

	private final GroovyClassValuePreJava7Map map = new GroovyClassValuePreJava7Map();

	public GroovyClassValuePreJava7(ComputeValue<T> computeValue){
		this.computeValue = computeValue;
	}

	@Override
	public T get(Class<?> type) {
		// the value isn't use in the getOrPut call - see the EntryWithValue constructor above
		T value = ((EntryWithValue)map.getOrPut(type, null)).getValue();
		//all entries are guaranteed to be EntryWithValue. Value can only be null if computeValue returns null
		return value;
	}

	@Override
	public void remove(Class<?> type) {
		map.remove(type);
	}

}
