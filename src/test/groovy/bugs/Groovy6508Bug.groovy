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
package groovy.bugs

import groovy.test.GroovyTestCase

import java.util.concurrent.Callable

/**
 * LK-TODO
 */
class Groovy6508Bug extends GroovyTestCase {

    void testInnerClassAccessingFieldWithCustomGetter() {
        PropertyGetterOverride x = new PropertyGetterOverride()
        assert x.field == x.getFieldViaInner()
    }

    void testInnerClassAccessingBaseFieldProperty() {
        BaseFieldBearerSub sub = new BaseFieldBearerSub();
        assert sub.baseField == sub.getBaseFieldViaInner()
    }

}

class BaseFieldBearer {
    String baseField = 'baseValue'
}

class BaseFieldBearerSub extends BaseFieldBearer {

    /** Access baseField from our super class by using an inner class instance **/
    String getBaseFieldViaInner() {
        new Callable<String>() {
            String call() {
                // Previous versions of Groovy would fault here, unable to access
                // an *attribute* called baseField, rather than looking for a property
                baseField
            }
        }.call();
    }
}

class PropertyGetterOverride {

    String field = 'fieldAttributeValue'

    /** A property-getter override for field */
    String getField() {
        'fieldPropertyValue'
    }

    /** Access field property from our class by using an inner class */
    String getFieldViaInner() {
        new Callable<String>() {
            String call() {
                // Previous versions of Groovy will access the attribute directly here
                // rather than the property; i.e., the custom getter would not be respected
                field
            }
        }.call()
    }
}

