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
package org.codehaus.groovy.ast

import org.codehaus.groovy.ast.tools.GenericsUtils

import static org.codehaus.groovy.ast.GenericsType.GenericsTypeName

/**
 * Various tests aimed at testing the {@link GenericsType} class.
 */
public class GenericsTypeTest extends GenericsTestCase {

    void testSimpleGenericsType() {
        // <Number>
        def generics = extractTypesFromCode('List<Number> type').generics[0]
        assert generics.toString() == 'java.lang.Number'
        assert generics.isCompatibleWith(ClassHelper.Number_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.Integer_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.OBJECT_TYPE)

        // <? extends Number>
        generics = extractTypesFromCode('List<? extends Number> type').generics[0]
        assert generics.toString() == '? extends java.lang.Number'
        assert generics.isCompatibleWith(ClassHelper.Number_TYPE)
        assert generics.isCompatibleWith(ClassHelper.Integer_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.OBJECT_TYPE)

        // <? super Number>
        generics = extractTypesFromCode('List<? super Number> type').generics[0]
        assert generics.toString() == '? super java.lang.Number'
        assert generics.isCompatibleWith(ClassHelper.Number_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.Integer_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.STRING_TYPE)
        assert generics.isCompatibleWith(ClassHelper.OBJECT_TYPE)

    }

    void testMultipleUpperBounds() {
        def generics = extractTypesFromCode('''public <T extends AbstractMap & SortedMap> List<T> type(){}''').generics[0]
        assert generics.toString() == 'T extends java.util.AbstractMap & java.util.SortedMap'
        assert generics.isCompatibleWith(ClassHelper.make(TreeMap))
        assert !generics.isCompatibleWith(ClassHelper.make(AbstractMap))
        assert !generics.isCompatibleWith(ClassHelper.make(SortedMap))
    }

    void testMultipleUpperBounds2() {
        def generics = extractTypesFromCode('''public <U,V,T extends AbstractMap<U,V> & SortedMap<U,V>> List<T> type(){}''').generics[2]
        def type = extractTypesFromCode('''TreeMap<String,String> type''').type
        assert generics.toString() == 'T extends java.util.AbstractMap<U, V> & java.util.SortedMap<U, V>'

        assert generics.isCompatibleWith(ClassHelper.make(TreeMap))
        assert !generics.isCompatibleWith(ClassHelper.make(AbstractMap))
        assert !generics.isCompatibleWith(ClassHelper.make(SortedMap))
        assert generics.isCompatibleWith(type)
    }

    void testNestedGenerics() {
        def listStringType = extractTypesFromCode('List<String> type').type
        def listInteger = extractTypesFromCode('List<Integer> type').type
        def stringListType = extractTypesFromCode('org.codehaus.groovy.ast.GenericsTypeTest.StringList type').type
        def integerListType = extractTypesFromCode('org.codehaus.groovy.ast.GenericsTypeTest.IntegerList type').type
        def typeinfo = extractTypesFromCode('List<? extends List<String>> type')
        def generics = typeinfo.generics[0]
        assert generics.toString() == '? extends java.util.List<java.lang.String>'
        assert generics.isCompatibleWith(ClassHelper.LIST_TYPE)
        assert generics.isCompatibleWith(ClassHelper.make(LinkedList))
        assert !generics.isCompatibleWith(ClassHelper.OBJECT_TYPE)
        assert generics.isCompatibleWith(listStringType)
        assert !generics.isCompatibleWith(listInteger)
        assert generics.isCompatibleWith(stringListType)
        assert !generics.isCompatibleWith(integerListType)

        def classNode = extractTypesFromCode('List<Integer> type').type
        assert !generics.isCompatibleWith(classNode)
    }

    void testNestedGenerics2() {
        def listOfStringListType = extractTypesFromCode('List<List<String>> type').type
        def listOfIntegerListType = extractTypesFromCode('List<List<Integer>> type').type
        def listOfStringListType2 = extractTypesFromCode('List<org.codehaus.groovy.ast.GenericsTypeTest.StringList> type').type
        def listOfIntegerListType2 = extractTypesFromCode('List<org.codehaus.groovy.ast.GenericsTypeTest.IntegerList> type').type
        def typeinfo = extractTypesFromCode('Set<? extends List<List<String>>> type')
        def generics = typeinfo.generics[0]
        assert generics.toString() == '? extends java.util.List<java.util.List<java.lang.String>>'
        assert generics.isCompatibleWith(listOfStringListType)
        assert !generics.isCompatibleWith(listOfIntegerListType)
        assert !generics.isCompatibleWith(listOfStringListType2) // strict matching (not List<? extends List<String>>)
        assert !generics.isCompatibleWith(listOfIntegerListType2)
    }

    void testGenericSignatureFromSort() {
        def generics = extractTypesFromCode('public <T extends Comparable<? super T>> List<T> type(){}').generics[0]
        def weirdComparable = extractTypesFromCode('List<org.codehaus.groovy.ast.GenericsTypeTest.WeirdComparable> type').type
        assert generics.isCompatibleWith(ClassHelper.COMPARABLE_TYPE)
        assert !generics.isCompatibleWith(ClassHelper.Number_TYPE) // Number does not implement Comparable
        assert generics.isCompatibleWith(ClassHelper.Integer_TYPE) // Integer implements Comparable<Integer>
        assert !generics.isCompatibleWith(weirdComparable) // WeirdComparable implements Comparable<Integer>
    }

    void testMultipleGenericArguments() {
        def generics = extractTypesFromCode('List<Map<String,String>> type').generics[0]
        def stringToString = extractTypesFromCode('Map<String,String> type').type
        def stringToInt = extractTypesFromCode('Map<String,Integer> type').type
        assert generics.toString() == 'java.util.Map<java.lang.String, java.lang.String>'
        assert generics.isCompatibleWith(ClassHelper.MAP_TYPE)
        assert generics.isCompatibleWith(stringToString)
        assert !generics.isCompatibleWith(stringToInt)
    }

    void testMultipleNestedGenericArguments() {
        def generics = extractTypesFromCode('List<Map<String,Map<String,Integer>>> type').generics[0]
        def correctType = extractTypesFromCode('Map<String,Map<String,Integer>> type').type
        def incorrectType = extractTypesFromCode('Map<String,Map<Integer,Integer>> type').type
        assert generics.toString() == 'java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>'
        assert generics.isCompatibleWith(ClassHelper.MAP_TYPE)
        assert generics.isCompatibleWith(correctType)
        assert !generics.isCompatibleWith(incorrectType)
    }

    void testPlaceholderExtract() {
        def type = extractTypesFromCode("List<String> type").type
        def placeholders = GenericsUtils.extractPlaceholders(type)
        assert placeholders[new GenericsTypeName('E')]?.type == ClassHelper.STRING_TYPE
    }

    void testPlaceholderExtract2() {
        def type = extractTypesFromCode("Map<String,Integer> type").type
        def placeholders = GenericsUtils.extractPlaceholders(type)
        assert placeholders[new GenericsTypeName('K')]?.type == ClassHelper.STRING_TYPE
        assert placeholders[new GenericsTypeName('V')]?.type == ClassHelper.Integer_TYPE
    }

    void testPlaceholderExtract3() {
        def type = extractTypesFromCode("List<Map<String,Integer>> type").type
        def placeholders = GenericsUtils.extractPlaceholders(type)
        assert placeholders[new GenericsTypeName('E')]?.type == ClassHelper.MAP_TYPE
        placeholders = GenericsUtils.extractPlaceholders(placeholders[new GenericsTypeName('E')].type)
        assert placeholders[new GenericsTypeName('K')]?.type == ClassHelper.STRING_TYPE
        assert placeholders[new GenericsTypeName('V')]?.type == ClassHelper.Integer_TYPE
    }

    // ------------------ Support methods -------------------------

    protected static class StringList extends LinkedList<String> {}
    protected static class IntegerList extends LinkedList<Integer> {}
    protected static class WeirdComparable implements Comparable<Integer> {
        int compareTo(Integer o) {
            return 0
        }
    }
}
