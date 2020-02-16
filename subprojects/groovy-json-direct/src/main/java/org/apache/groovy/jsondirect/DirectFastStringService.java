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
package org.apache.groovy.jsondirect;

import org.apache.groovy.json.FastStringService;

import static org.apache.groovy.jsondirect.DirectFastStringServiceFactory.STRING_VALUE_FIELD_OFFSET;
import static org.apache.groovy.jsondirect.DirectFastStringServiceFactory.WRITE_TO_FINAL_FIELDS;
import static org.apache.groovy.jsondirect.DirectFastStringServiceFactory.UNSAFE;

/**
 * Internal class for fast processing of Strings during JSON parsing - direct field writing version.
 * Works for JDK 7 and 8 for most JDK implementations but uses the Unsafe mechanism of Java.
 */
public class DirectFastStringService implements FastStringService {
    @Override
    public char[] toCharArray(String string) {
        return (char[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
    }

    @Override
    public String noCopyStringFromChars(char[] chars) {
        if (WRITE_TO_FINAL_FIELDS) {
            String string = new String();
            UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, chars);
            return string;
        } else {
            return new String(chars);
        }
    }
}
