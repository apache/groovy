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
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Canonical(
        includes = ['a', 'b'], excludes = ['c']
)
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass('Lulz')
@interface FunnyAnnotation {
    public static final String SOME_CONSTANT2 = 'SOME_CONSTANT2';
    String SOME_CONSTANT = 'SOME_CONSTANT';

    /* This is a comment
    */
    String name() default ""

    /**
     * This has a default, too
     */
    boolean synchronize() default false

    boolean synchronize2() default
            false
}

@interface a {

}

@interface b {
    String name()
}

@interface C {
    String name() default ''
}