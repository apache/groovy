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
/**
 * @Groovydoc
 * class AA
 */
class AA {
    /**
     * @Groovydoc
     * field SOME_FIELD
     */
    public static final int SOME_FIELD = 1;

    /**
     * @Groovydoc
     * constructor AA
     */
    public AA() {

    }

    /**
     * @Groovydoc
     * method m
     */
    public void m() {

    }

    /**
     * @Groovydoc
     * class InnerClass
     */
    class InnerClass {

    }


}

/**
 * @Groovydoc
 * annotation BB
 */
@interface BB {

}

assert AA.class.getAnnotation(groovy.lang.Groovydoc).value().contains('class AA')
assert AA.class.getMethod('m', new Class[0]).getAnnotation(groovy.lang.Groovydoc).value().contains('method m')
assert AA.class.getConstructor().getAnnotation(groovy.lang.Groovydoc).value().contains('constructor AA')
assert AA.class.getField('SOME_FIELD').getAnnotation(groovy.lang.Groovydoc).value().contains('field SOME_FIELD')
assert AA.class.getDeclaredClasses().find {it.simpleName.contains('InnerClass')}.getAnnotation(groovy.lang.Groovydoc).value().contains('class InnerClass')
assert BB.class.getAnnotation(groovy.lang.Groovydoc).value().contains('annotation BB')