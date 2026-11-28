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
package groovy.jmx;

import java.net.URI;

import groovy.jmx.OverloadedMBean.BaseNode;
import groovy.jmx.OverloadedMBean.ChildNode;

public class Overloaded implements OverloadedMBean {

    @Override
    public String redeploy(String url) {
        return "string";
    }

    @Override
    public String redeploy(URI uri) {
        return "uri";
    }

    @Override
    public String redeploy(BaseNode info) {
        return "base";
    }

    @Override
    public String pair(String text, int count) {
        return "string-int:" + text + ":" + count;
    }

    @Override
    public String pair(int count, String text) {
        return "int-string:" + count + ":" + text;
    }

    @Override
    public String num(short value) {
        return "short";
    }

    @Override
    public String num(int value) {
        return "int";
    }

    @Override
    public String num(long value) {
        return "long";
    }

    @Override
    public String num(float value) {
        return "float";
    }

    @Override
    public String num(double value) {
        return "double:" + value;
    }

    @Override
    public String num(String value) {
        return "string";
    }

    @Override
    public String id(long value) {
        return "long";
    }

    @Override
    public String id(long[] values) {
        return "long[]:" + values.length;
    }

    @Override
    public String id(long value, int depth) {
        return "long-int";
    }

    @Override
    public String id(long[] values, int depth) {
        return "long[]-int:" + values.length;
    }

    @Override
    public String label(String value) {
        return "string:" + value;
    }

    @Override
    public String label(URI uri) {
        return "uri";
    }

    @Override
    public String seq(String value) {
        return "string";
    }

    @Override
    public String seq(CharSequence value) {
        return "charsequence";
    }

    @Override
    public String real(double value) {
        return "double:" + value;
    }

    @Override
    public String real(String value) {
        return "string";
    }

    @Override
    public String box(Integer value) {
        return "Integer";
    }

    @Override
    public String box(int value) {
        return "int";
    }

    @Override
    public String flag(Boolean value) {
        return "Boolean";
    }

    @Override
    public String flag(boolean value) {
        return "boolean";
    }

    @Override
    public String shape(String value) {
        return "string";
    }

    @Override
    public String shape(Object value) {
        return "object";
    }

    @Override
    public String refs(String[] value) {
        return "String[]";
    }

    @Override
    public String refs(Object[] value) {
        return "Object[]";
    }

    @Override
    public String bytes(byte[] value) {
        return "byte[]";
    }

    @Override
    public String bytes(int[] value) {
        return "int[]";
    }

    @Override
    public String only(int value) {
        return "only:" + value;
    }

    @Override
    public String child(BaseNode value) {
        return "base";
    }

    @Override
    public String child(ChildNode value) {
        return "child";
    }

    @Override
    public String wrap(Object value) {
        return "object";
    }

    @Override
    public String wrap(int value) {
        return "int";
    }

    @Override
    public String code(char value) {
        return "char";
    }

    @Override
    public String code(short value) {
        return "short";
    }

    @Override
    public String code(int value) {
        return "int";
    }

    @Override
    public String bit(boolean value) {
        return "boolean";
    }

    @Override
    public String bit(int value) {
        return "int";
    }

    @Override
    public String tiny(byte value) {
        return "byte";
    }

    @Override
    public String tiny(short value) {
        return "short";
    }

    @Override
    public String tiny(int value) {
        return "int";
    }

    @Override
    public String mix(String text, double value) {
        return "string-double:" + text + ":" + value;
    }

    @Override
    public String mix(URI uri, String text) {
        return "uri-string:" + text;
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public String echo(String value) {
        return "echo:" + value;
    }
}
