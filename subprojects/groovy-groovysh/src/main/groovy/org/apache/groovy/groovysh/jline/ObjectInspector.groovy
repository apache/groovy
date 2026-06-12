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

/**
 * Adapts Groovy's inspector output into structures used by groovysh commands and completions.
 */
class ObjectInspector {
    /** Field name key used in method and property descriptions. */
    public static final String FIELD_NAME = 'name'
    /** Parameter list key used in method descriptions. */
    public static final String FIELD_PARAMETERS = 'parameters'
    /** Modifier key used in method descriptions. */
    public static final String FIELD_MODIFIERS = 'modifiers'
    /** Return type key used in method descriptions. */
    public static final String FIELD_RETURN = 'return'
    /** Column order used when converting inspector method rows into maps. */
    public static final List<String> METHOD_COLUMNS = ['language', FIELD_MODIFIERS, 'this', FIELD_RETURN, FIELD_NAME
                                                       , FIELD_PARAMETERS,'exception','8']
    /** Meta-method names that are treated as globally available and hidden from results. */
    public static final List<String> GLOBAL_META_METHODS = ['print', 'println', 'printf', 'sprintf', 'sleep']
    private static final List<String> OPERATOR_META_METHODS = ['plus', 'minus', 'multiply', 'div', 'mod', 'power', 'or', 'and'
                                                               , 'xor', 'asType', 'call', 'getAt', 'putAt', 'isCase', 'leftShift'
                                                               , 'rightShift', 'rightShiftUnsigned', 'next', 'previous', 'positive'
                                                               , 'negative', 'bitwiseNegate'
             /* other methods to exclude from completion */    , 'asBoolean' , 'toBoolean' , 'addShutdownHook']
    /** Object being described. */
    def obj
    /** Groovy inspector backing the extracted metadata. */
    def inspector
    /** Type hierarchy names collected for the inspected object. */
    def types = []

    /**
     * Creates an inspector facade for the supplied object.
     *
     * @param obj object to inspect
     */
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

    /**
     * Returns declared methods as maps keyed by {@link #METHOD_COLUMNS}.
     *
     * @return method descriptors derived from the Groovy inspector
     */
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

    /**
     * Returns meta-methods while filtering operator-style entries.
     *
     * @return filtered meta-method descriptors
     */
    List<Map<String, String>> metaMethods() {
        metaMethods(true)
    }

    /**
     * Returns meta-methods visible on the inspected object.
     *
     * @param includeOperatorMethods whether operator-style meta-methods should be included
     * @return filtered meta-method descriptors
     */
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

    /**
     * Returns grouped property information from the Groovy inspector.
     *
     * @return property metadata keyed by inspector category
     */
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
