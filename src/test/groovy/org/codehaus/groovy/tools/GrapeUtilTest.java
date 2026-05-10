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
package org.codehaus.groovy.tools;

import junit.framework.TestCase;
import java.util.Map;

public class GrapeUtilTest extends TestCase {

    public void testGetIvyParts1(){
        String allStr = "@M";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 3;
    }

    public void testGetIvyParts2(){
        String allStr = "a@";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 2;
    }

    public void testGetIvyParts3(){
        String allStr = "@";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 2;
    }

    public void testGetIvyParts4(){
        String allStr = ":k:@M";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 4;
    }

    public void testMavenShorthand_groupModuleVersion(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("com.example:foo:1.2.3");
        assert "com.example".equals(parts.get("group"));
        assert "foo".equals(parts.get("module"));
        assert "1.2.3".equals(parts.get("version"));
    }

    public void testMavenShorthand_versionDefaultsToWildcard(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("com.example:foo");
        assert "com.example".equals(parts.get("group"));
        assert "foo".equals(parts.get("module"));
        assert "*".equals(parts.get("version"));
    }

    public void testMavenShorthand_withClassifierAndExt(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("com.example:foo:1.2.3:jdk15@zip");
        assert "com.example".equals(parts.get("group"));
        assert "foo".equals(parts.get("module"));
        assert "1.2.3".equals(parts.get("version"));
        assert "jdk15".equals(parts.get("classifier"));
        assert "zip".equals(parts.get("ext"));
    }

    public void testIvyShorthand_groupModuleVersion(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("com.example#foo;1.2.3");
        assert "com.example".equals(parts.get("group"));
        assert "foo".equals(parts.get("module"));
        assert "1.2.3".equals(parts.get("version"));
    }

    public void testIvyShorthand_dottedAndHyphenatedNames(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("org.apache.commons#commons-lang3;3.9");
        assert "org.apache.commons".equals(parts.get("group"));
        assert "commons-lang3".equals(parts.get("module"));
        assert "3.9".equals(parts.get("version"));
    }

    public void testIvyShorthand_versionRange(){
        Map<String, Object> parts = GrapeUtil.getIvyParts("com.example#foo;[1.0,2.0)");
        assert "com.example".equals(parts.get("group"));
        assert "foo".equals(parts.get("module"));
        assert "[1.0,2.0)".equals(parts.get("version"));
    }

    public void testNullInput_returnsEmpty(){
        Map<String, Object> parts = GrapeUtil.getIvyParts(null);
        assert parts.isEmpty();
    }
}
