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
package org.codehaus.groovy.classgen.asm;

import org.objectweb.asm.MethodVisitor;

public class MethodCallerMultiAdapter {
    private MethodCaller[] methods;
    boolean skipSpreadSafeAndSafe;

    public static final int MAX_ARGS = 0;

    public static MethodCallerMultiAdapter newStatic(Class theClass, String baseName, boolean createNArgs, boolean skipSpreadSafeAndSafe) {
        MethodCallerMultiAdapter mcma = new MethodCallerMultiAdapter();
        mcma.skipSpreadSafeAndSafe = skipSpreadSafeAndSafe;
        if (createNArgs) {
            int numberOfBaseMethods = mcma.numberOfBaseMethods();
            mcma.methods = new MethodCaller[(MAX_ARGS + 2) * numberOfBaseMethods];
            for (int i = 0; i <= MAX_ARGS; i++) {
                mcma.methods[i * numberOfBaseMethods] = MethodCaller.newStatic(theClass, baseName + i);
                if (skipSpreadSafeAndSafe) continue;
                mcma.methods[i * numberOfBaseMethods + 1] = MethodCaller.newStatic(theClass, baseName + i + "Safe");
                mcma.methods[i * numberOfBaseMethods + 2] = MethodCaller.newStatic(theClass, baseName + i + "SpreadSafe");
            }
            mcma.methods[(MAX_ARGS + 1) * numberOfBaseMethods] = MethodCaller.newStatic(theClass, baseName + "N");
            if (!skipSpreadSafeAndSafe) {
                mcma.methods[(MAX_ARGS + 1) * numberOfBaseMethods + 1] = MethodCaller.newStatic(theClass, baseName + "N" + "Safe");
                mcma.methods[(MAX_ARGS + 1) * numberOfBaseMethods + 2] = MethodCaller.newStatic(theClass, baseName + "N" + "SpreadSafe");
            }

        } else if (!skipSpreadSafeAndSafe) {
            mcma.methods = new MethodCaller[]{
                    MethodCaller.newStatic(theClass, baseName),
                    MethodCaller.newStatic(theClass, baseName + "Safe"),
                    MethodCaller.newStatic(theClass, baseName + "SpreadSafe")
            };
        } else {
            mcma.methods = new MethodCaller[]{
                    MethodCaller.newStatic(theClass, baseName)
            };
        }
        return mcma;
    }

    /**
     * @param methodVisitor
     * @param numberOfArguments a value &gt; 0 describing how many arguments are additionally used for the method call
     * @param safe
     * @param spreadSafe
     */
    public void call(MethodVisitor methodVisitor, int numberOfArguments, boolean safe, boolean spreadSafe) {
        int offset = 0;
        if (safe && !skipSpreadSafeAndSafe) offset = 1;
        if (spreadSafe && !skipSpreadSafeAndSafe) offset = 2;
        if (numberOfArguments > MAX_ARGS || numberOfArguments < 0) {
            offset += (MAX_ARGS + 1) * numberOfBaseMethods();
        } else {
            offset += numberOfArguments * numberOfBaseMethods();
        }
        methods[offset].call(methodVisitor);
    }

    private int numberOfBaseMethods() {
        if (skipSpreadSafeAndSafe) return 1;
        return 3;
    }
}