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
package org.codehaus.groovy.classgen

def types = ['Integer', 'Long', 'Float', 'Double']

def getMath (a,b) {
    if (a == 'Double' || b == 'Double' || a == 'Float' || b == 'Float')
      return 'FloatingPointMath'

    if (a == 'Long' || b == 'Long')
      return 'LongMath'

    'IntegerMath'
}

println '''
public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
    NumberMath m = NumberMath.getMath((Number)receiver, (Number)args[0]);
'''

types.each {
    a ->
    print """
    if (receiver instanceof $a) {"""
    types.each {
        b ->
        print """
        if (args[0] instanceof $b)
            return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                public final Object invoke(Object receiver, Object[] args) {
                    return ${getMath(a,b)}.INSTANCE.addImpl(($a)receiver,($b)args[0]);
                }

                public final Object invokeBinop(Object receiver, Object arg) {
                    return ${getMath(a,b)}.INSTANCE.addImpl(($a)receiver,($b)arg);
                }
            };
        """
    }
    println '}'
}

println '''
    return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
        public final Object invoke(Object receiver, Object[] args) {
            return math.addImpl((Number)receiver,(Number)args[0]);
        }

        public final Object invokeBinop(Object receiver, Object arg) {
            return math.addImpl((Number)receiver,(Number)arg);
        }
}
'''

for (i in 2..256) {
    print "public Object invoke$i (Object receiver, "
    printParams(i)
    println "Object a$i) {"
    print '  return invoke (receiver, new Object[] {'
    printArgs(i)
    println "a$i} );"
    println '}'
}

private void printParams(int i) {
    for (j in 1..(i - 1)) {
        print "Object a$j, "
    }
}

private void printArgs(int i) {
    for (j in 1..(i - 1)) {
        print "a$j, "
    }
}
