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
package org.codehaus.groovy.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An abstract base class for a key used for comparators and Map keys to lookup a method by
 * name and parameter types
 */
public abstract class MethodKey {

    private int hash;
    private final String name;
    private final Class sender;
    private final boolean isCallToSuper;
    
    public MethodKey(Class sender, String name, boolean isCallToSuper) {
        this.sender = sender;
        this.name = name;
        this.isCallToSuper = isCallToSuper;
    }

    /**
     * Creates an immutable copy that we can cache. 
     */
    public MethodKey createCopy() {
        int size = getParameterCount();
        Class[] paramTypes = new Class[size];
        for (int i = 0; i < size; i++) {
            paramTypes[i] = getParameterType(i);
        }
        return new DefaultMethodKey(sender, name, paramTypes, isCallToSuper);
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        else if (that instanceof MethodKey) {
            return equals((MethodKey) that);
        }
        return false;
    }

    public boolean equals(MethodKey that) {
      int size;
      if (sender!=that.sender) return false;
      if (isCallToSuper!=that.isCallToSuper) return false;
      if (!name.equals(that.name)) return false;
      if ((size = getParameterCount()) != that.getParameterCount()) return false;
      
      for (int i = 0; i < size; i++) {
          if (getParameterType(i) != that.getParameterType(i)) {
              return false;
          }
      }
      return true;
    }

    public int hashCode() {
        if (hash == 0) {
            hash = createHashCode();
            if (hash == 0) {
                hash = 0xcafebabe;
            }
        }
        return hash;
    }

    public String toString() {
        return super.toString() + "[name:" + name + "; params:" + getParamterTypes();
    }

    public String getName() {
        return name;
    }

    public List getParamterTypes() {
        int size = getParameterCount();
        if (size <= 0) {
            return Collections.EMPTY_LIST;
        }
        List params = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            params.add(getParameterType(i));
        }
        return params;
    }

    public abstract int getParameterCount();
    public abstract Class getParameterType(int index);

    protected int createHashCode() {
        int answer = name.hashCode();
        int size = getParameterCount();

        /** @todo we should use the real Josh Bloch algorithm here */

        // can't remember the exact Josh Bloch algorithm and I've not got the book handy
        // but its something like this IIRC
        for (int i = 0; i < size; i++) {
            answer *= 37;
            answer += 1 + getParameterType(i).hashCode();
        }
        answer *= 37;
        answer += isCallToSuper?1:0;
        answer *= 37;
        answer += 1 + sender.hashCode();
        return answer;
    }
}
