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

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;
import org.codehaus.groovy.reflection.v7.GroovyClassValueJava7;

class GroovyClassValueFactory {
	/**
	 * Escape hatch for deployments where {@code java.lang.ClassValue} pins
	 * class loaders (JDK-8136353): associations on immortal classes never
	 * release their value's loader, leaking every Groovy copy a container
	 * deploys and undeploys (GROOVY-12142). Set
	 * {@code -Dgroovy.use.classvalue=false} at JVM startup to use a weak-key
	 * map instead; the default remains ClassValue for its per-Class fast path.
	 */
	private static final String CLASSVALUE_MODE = SystemUtil.getSystemPropertySafe("groovy.use.classvalue", "true");

	/**
	 * GROOVY-12281 investigation prototypes; see {@link #createGroovyClassValue}.
	 * Callers needing behavior differences never consult the mode: they ask the
	 * created store for its capabilities ({@link GroovyClassValue#valuesReclaimable}).
	 */
	private static final boolean SOFT_MODE = "soft".equalsIgnoreCase(CLASSVALUE_MODE);
	private static final boolean HYBRID_MODE = "hybrid".equalsIgnoreCase(CLASSVALUE_MODE);

	public static <T> GroovyClassValue<T> createGroovyClassValue(ComputeValue<T> computeValue) {
		// GROOVY-12281 investigation prototypes: "hybrid" routes platform-loader keys to the
		// weak-key map and everything else to ClassValue, so immortal platform keys never
		// pin the value's loader while user classes keep the per-class fast path (measured,
		// declined); "soft" keeps ClassValue for all keys but holds values softly with
		// resurrection and ephemeron pinning, so immortal keys hold no strong chain to the
		// value's loader.
		if (HYBRID_MODE) {
			return new GroovyClassValueHybrid<>(computeValue);
		}
		if (SOFT_MODE) {
			return new GroovyClassValueSoft<>(computeValue);
		}
		return Boolean.parseBoolean(CLASSVALUE_MODE)
				? new GroovyClassValueJava7<>(computeValue)
				: new GroovyClassValueMapBased<>(computeValue);
	}
}
