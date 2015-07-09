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
package groovy.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is a CLI option.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Option {
    /**
     * The description of this option
     *
     * @return the description of this option
     */
    String description() default "";

    /**
     * The short name of this option
     *
     * @return the short name of this option
     */
    String shortName() default "";

    /**
     * The long name of this option
     *
     * @return the long name of this option
     */
    String longName() default "";

    /**
     * The value separator for this multi-valued option
     *
     * @return the value separator for this multi-valued option
     */
    char valueSeparator() default 0;

    /**
     * The default value for this option
     *
     * @return the default value for this option
     */
    String defaultValue() default "";
}
