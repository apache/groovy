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
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertySource {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertySources {
    PropertySource[] value();
}

@PropertySources({
    @PropertySource("classpath:1.properties"),
    @PropertySource("file:2.properties")
})
public class Test {
}

def propertySources = Test.getAnnotation(PropertySources).value()
assert 2 == propertySources.length
def values = propertySources.collect { it.value() }
assert values.contains('classpath:1.properties')
assert values.contains('file:2.properties')

@PropertySources({
    @PropertySource("classpath:1.properties")
})
public class Test2 {
}
def propertySources2 = Test2.getAnnotation(PropertySources).value()
assert 1 == propertySources2.length
def values2 = propertySources.collect { it.value() }
assert values2.contains('classpath:1.properties')
