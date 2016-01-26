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
package org.codehaus.groovy.tools.groovydoc.testfiles;

public class GeneratePropertyFromGetSet {

    public void setStr(String str) {
    }

    public String getStr() {
        return "";
    }

    public String getStr1() {
        return "";
    }

    public void setStr1(String str) {
    }

    public int getInt() {
        return 0;
    }

    public void setInt(int integer) {
    }

    public void setShouldNotBePresent(int str) {
    }

    private void set_public_get_private_set(String public_get_private_set) {
    }

    public String get_public_get_private_set() {
        return "";
    }

    public void set_private_get_public_set(String private_get_public_set) {
    }

    private String get_private_get_public_set() {
        return "";
    }

    private void set_private_get_private_set(String private_get_private_set) {
    }

    private String get_private_get_private_set() {
    }

    public void setTestBoolean(boolean a) {
    }

    public boolean isTestBoolean() {
    }

    public boolean isTestBoolean2() {
    }

    public void setTestBoolean2(boolean a){
    }

    public void set(String prop) {
    }

    public String get() {
        return "";
    }

    public boolean is(Object another) {
        return true;
    }
}
