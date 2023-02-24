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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10461 {
    @Test
    void testParseClassWithOptionalDependencies() {
        assertScript '''
            @GrabExclude('org.codehaus.groovy:groovy')
            @GrabExclude('org.codehaus.groovy:groovy-xml')
            @GrabExclude('org.codehaus.groovy:groovy-json')

            @Grab('com.fasterxml.jackson.core:jackson-databind:2.14.2')
            import com.fasterxml.jackson.databind.ObjectMapper

            @Grab('io.rest-assured:rest-assured:4.4.0')
            import io.restassured.RestAssured
            import io.restassured.config.RestAssuredConfig
            import io.restassured.config.ObjectMapperConfig
            import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory

            def factory = new Jackson2ObjectMapperFactory() {
                @Override
                ObjectMapper create(java.lang.reflect.Type type, String charset) {
                    ObjectMapper mapper = new ObjectMapper()
                    //mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                    //mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false)
                    //mapper.setSerializationInclusion(NON_EMPTY)
                    return mapper
                }
            }

            @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={
                def mce = node.rightExpression.arguments[0]
                def cce = mce.objectExpression
                cce.type.methods // like Spock
            })
            RestAssuredConfig config = RestAssured.config().objectMapperConfig(
                    new ObjectMapperConfig().jackson2ObjectMapperFactory(factory))
        '''
    }
}
