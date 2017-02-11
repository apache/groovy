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
package org.apache.groovy.stress.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class GCUtils {

    private GCUtils() { }

    public static void gc() {
        Reference<Object> dummy = new WeakReference<Object>(new Object());
        System.gc();
        int max = 0;
        while (dummy.get() != null && max++ < 10) {
            System.gc();
        }
        if (dummy.get() != null) {
            throw new Error("GC attempt failed");
        }
    }
}
