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
package org.codehaus.groovy.runtime;

import org.junit.Assert;
import org.junit.Test;

public class EncodingGroovyMethodsTest {
    @Test
    public void md5() throws Exception {
        Assert.assertEquals("e99a18c428cb38d5f260853678922e03", EncodingGroovyMethods.md5("abc123"));
        Assert.assertEquals("e99a18c428cb38d5f260853678922e03", EncodingGroovyMethods.md5("abc123".getBytes("UTF-8")));
    }

    @Test
    public void sha256() throws Exception {
        Assert.assertEquals("6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090", EncodingGroovyMethods.sha256("abc123"));
        Assert.assertEquals("6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090", EncodingGroovyMethods.sha256("abc123".getBytes("UTF-8")));
    }

    @Test
    public void digest() throws Exception {
        Assert.assertEquals("e99a18c428cb38d5f260853678922e03", EncodingGroovyMethods.digest("abc123", "MD5"));
        Assert.assertEquals("e99a18c428cb38d5f260853678922e03", EncodingGroovyMethods.digest("abc123".getBytes("UTF-8"), "MD5"));

        Assert.assertEquals("0ade7c2cf97f75d009975f4d720d1fa6c19f4897", EncodingGroovyMethods.digest("9", "SHA-1"));
        Assert.assertEquals("0716d9708d321ffb6a00818614779e779925365c", EncodingGroovyMethods.digest("17", "SHA-1"));
        Assert.assertEquals("0a57cb53ba59c46fc4b692527a38a87c78d84028", EncodingGroovyMethods.digest("28", "SHA-1"));
        Assert.assertEquals("0286dd552c9bea9a69ecb3759e7b94777635514b", EncodingGroovyMethods.digest("43", "SHA-1"));
        Assert.assertEquals("08a35293e09f508494096c1c1b3819edb9df50db", EncodingGroovyMethods.digest("93", "SHA-1"));
    }
}
