/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.memoize;

/**
 * Protects stored resources from eviction from memory following the LRU (Last Recently Used) strategy.
 * The touch method can be used to renew an element and move it to the from the LRU queue.
 *
 * @author Vaclav Pech
 */
interface ProtectionStorage {
    void touch(Object key, Object value);
}
