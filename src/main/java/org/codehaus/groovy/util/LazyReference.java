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

/**
 * Soft reference with lazy initialization under lock
 */
public abstract class LazyReference<T> extends LockableObject {
    private static final ManagedReference INIT = new ManagedReference(ReferenceType.HARD,null,null){};
    private static final ManagedReference NULL_REFERENCE = new ManagedReference(ReferenceType.HARD,null,null){};
    private static final long serialVersionUID = -828564509716680325L;
    private ManagedReference<T> reference = INIT;
    private final ReferenceBundle bundle;
    
    public LazyReference(ReferenceBundle bundle) { 
        this.bundle = bundle;
    }
    
    public T get() {
        ManagedReference<T> resRef = reference;
        if (resRef == INIT) return getLocked(false);
        if (resRef == NULL_REFERENCE) return null;
        T res = resRef.get();
        // res== null means it got collected
        if (res==null) return getLocked(true);
        return res;
    }

    private T getLocked (boolean force) {
        lock ();
        try {
            ManagedReference<T> resRef = reference;
            if (!force && resRef != INIT) return resRef.get();
            T res = initValue();
            if (res == null) {
                reference = NULL_REFERENCE;
            } else {
                reference = new ManagedReference<>(bundle, res);
            }
            return res;
        } finally {
            unlock();
        }
    }

    public void clear() {
        reference = INIT;
    }

    public abstract T initValue();

    public String toString() {
        T res = reference.get();
        if (res == null)
          return "<null>";
        else
          return res.toString();
    }
}
