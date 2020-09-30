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
package org.codehaus.groovy.runtime.memoize;

/**
 * A NullObject pattern implementation for ProtectionStorage
 * Doesn't protect any resources.
 * Used when the user doesn't mind to eventually have the whole memoize cache emptied by gc.
 */
public final class NullProtectionStorage implements ProtectionStorage{

    /**
     * Does nothing
     *
     * @param key   The key of the element to renew
     * @param value A value to newly associate with the key
     */
    @Override
    public void touch(final Object key, final Object value) { }
}
