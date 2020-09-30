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
package org.codehaus.groovy.util;

public class ManagedReference<T> implements Finalizable {
    private static final ReferenceManager NULL_MANAGER = new ReferenceManager(null){};
    private final Reference<T,ManagedReference<T>> ref;
    private final ReferenceManager manager;
    
    public ManagedReference(ReferenceType type, ReferenceManager rmanager, T value) {
        if (rmanager==null) rmanager = NULL_MANAGER;
        this.manager = rmanager;
        this.ref = type.createReference(value, this, rmanager.getReferenceQueue());
        rmanager.afterReferenceCreation(ref);
    }
    
    public ManagedReference(ReferenceBundle bundle, T value) {
        this(bundle.getType(),bundle.getManager(),value);
    }
    
    public final T get() {
        return ref.get();
    }
    
    public final void clear() {
        ref.clear();
        manager.removeStallEntries();
    }
    
    @Override
    public void finalizeReference(){
        ref.clear();
    }
}   