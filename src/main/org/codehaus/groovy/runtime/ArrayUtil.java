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

/**
* This is a generated class used internally during the writing of bytecode within the CallSiteWriter logic.
* This is not a class exposed to users, as is the case with almost all classes in the org.codehaus.groovy packages.
* <p>
* The purpose is the reduction of the size of the bytecode. Consider creating a three element Object[] with null values:
* <pre>
*  ANEWARRAY java/lang/Object    
*  DUP
*  ICONST_0
*  ACONST_NULL
*  AASTORE
*  DUP
*  ICONST_1
*  ACONST_NULL
*  AASTORE
*  DUP
*  ICONST_2
*  ACONST_NULL
*  AASTORE
* </pre>
* with ArrayUtils you can have it like this:
* <pre>
*  ACONST_NULL
*  ACONST_NULL
*  ACONST_NULL
*  INVOKESTATIC ArrayUtils.createArray(Object,Object,Object)
* </pre>
* The number of needed instructions is thus reduced from 15 to 4. For every entry we save 3 bytecode instructions.
* This allows better readable bytecode and it allows the JIT to see less bytecode to optimize, helping under the
* inlining threshold here or there.
* <p>
* So even though the class is ugly, there are good reason to have this in Groovy, even if the class makes
* absolutely no sense in normal Java. But it is not used in normal Java, but from the bytecode. 
*/ 
public class ArrayUtil {
    private static final Object[] EMPTY = new Object[0]
            ;

    public static Object[] createArray() {
        return EMPTY;
    }

    public static Object[] createArray(Object... objects) {
        return objects;
    }
}
