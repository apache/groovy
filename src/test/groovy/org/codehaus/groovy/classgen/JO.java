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
package org.codehaus.groovy.classgen;

import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.asm.util.ASMifier;

import java.lang.ref.SoftReference;

public class JO {
    public static SoftReference staticMetaClass;

    MetaClass getStaticMetaClass (Object obj) {
        MetaClass mc;
        if (staticMetaClass == null || (mc = (MetaClass) staticMetaClass.get()) == null ) {
            mc = InvokerHelper.getMetaClass(obj);
            staticMetaClass = new SoftReference(mc);
        }
        return mc;
    }

    public static void main(String[] args) throws Exception {
        ASMifier.main(new String[]{"build/classes/groovy/swing/SwingBuilder.class"});
//        ASMifierClassVisitor.main(new String[]{"build/classes/org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite.class"});
//        ASMifierClassVisitor.main(new String[]{"build/test-classes/spectralnorm.class"});
//        ASMifierClassVisitor.main(new String[]{"build/test-classes/groovy/bugs/CustomMetaClassTest.class"});
    }
}
