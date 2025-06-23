/*
 * Copyright (c) 2002-2020, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.apache.groovy.groovysh.jline

import groovy.inspect.Inspector

class ObjectInspector {
    public static final String FIELD_NAME = 'name'
    public static final String FIELD_PARAMETERS = 'parameters'
    public static final String FIELD_MODIFIERS = 'modifiers'
    public static final String FIELD_RETURN = 'return'
    public static final List<String> METHOD_COLUMNS = ['language', FIELD_MODIFIERS, 'this', FIELD_RETURN, FIELD_NAME
                                                       , FIELD_PARAMETERS,'exception','8']
    public static final List<String> GLOBAL_META_METHODS = ['print', 'println', 'printf', 'sprintf', 'sleep']
    private static final List<String> OPERATOR_META_METHODS = ['plus', 'minus', 'multiply', 'div', 'mod', 'power', 'or', 'and'
                                                               , 'xor', 'asType', 'call', 'getAt', 'putAt', 'isCase', 'leftShift'
                                                               , 'rightShift', 'rightShiftUnsigned', 'next', 'previous', 'positive'
                                                               , 'negative', 'bitwiseNegate'
             /* other methods to exclude from completion */    , 'asBoolean' , 'toBoolean' , 'addShutdownHook']
    def obj
    def inspector
    def types = []

    ObjectInspector(def obj) {
        this.obj = obj
        types.add(obj.getClass().getSimpleName())
        addInterfaces(obj.getClass())
        def superClass = obj.getClass().getSuperclass()
        while (superClass) {
            types.add(superClass.getSimpleName())
            addInterfaces(superClass)
            superClass = superClass.getSuperclass()
        }
        this.inspector = new Inspector(obj)
    }

    private void addInterfaces(def clazz) {
        clazz.interfaces.each{types.add(it.getSimpleName())}
    }

    List<Map<String, String>> methods() {
        def out = []
        inspector.methods.each {
           def mdef = [:]
           for (int i = 0; i < it.size(); i++) {
               mdef.put(METHOD_COLUMNS.get(i), it[i])
           }
           out.add(mdef)
        }
        out
    }

    List<Map<String, String>> metaMethods() {
        metaMethods(true)
    }

    List<Map<String, String>> metaMethods(boolean includeOperatorMethods) {
        def out = []
        def added = []
        types.each { type ->
            inspector.metaMethods.each {
                def mdef = [:]
                for (int i = 0; i < it.size(); i++) {
                    mdef.put(METHOD_COLUMNS.get(i), it[i])
                }
                if (type == mdef.this && !added.contains(mdef.name + mdef.parameters)) {
                    if (!GLOBAL_META_METHODS.contains(mdef.name)
                            && (includeOperatorMethods || !OPERATOR_META_METHODS.contains(mdef.name))) {
                        added.add(mdef.name + mdef.parameters)
                        out.add(mdef)
                    }
                }
            }
        }
        out
    }

    def properties() {
        def out = [:]
        def props = ['propertyInfo', 'publicFields', 'classProps']
        props.each {
           def val = inspector.properties.get(it)
           out.put(it, val)
        }
        out
    }
}
