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
package groovy.lang;

import java.util.concurrent.Callable;

/**
 * A special "marker" style interface allowing Groovy classes to implement both
 * Runnable and Callable yet give preference to Runnable (for backwards compatibility)
 * for APIs having both Runnable and Callable methods. You should generally NOT use this
 * method in your own code.
 *
 * @see java.util.concurrent.Callable
 * @since 1.8.0
 */
public interface GroovyCallable<V> extends Callable<V> { }
