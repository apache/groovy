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

import groovy.transform.Undefined;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method or property can be used to set a CLI option.
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
     * The short name of this option. Defaults to the name of member being annotated if the longName is empty.
     *
     * @return the short name of this option
     */
    String shortName() default "";

    /**
     * The long name of this option. Defaults to the name of member being annotated.
     *
     * @return the long name of this option
     */
    String longName() default "";

    /**
     * The value separator for this multi-valued option. Only allowed for array-typed arguments.
     *
     * @return the value separator for this multi-valued option
     */
    String valueSeparator() default "";

    /**
     * Whether this option can have an optional argument.
     * Only supported for array-typed arguments to indicate that the array may be empty.
     *
     * @return true if this array-typed option can have an optional argument (i.e. could be empty)
     */
    boolean optionalArg() default false;

    /**
     * How many arguments this option has.
     * A value greater than 1 is only allowed for array-typed arguments.
     * Ignored for boolean options which are assumed to have a default of 0
     * or if {@code numberOfArgumentsString} is set.
     *
     * @return the number of arguments
     */
    int numberOfArguments() default 1;

    /**
     * How many arguments this option has represented as a String.
     * Only allowed for array-typed arguments.
     * Overrides {@code numberOfArguments} if set.
     * The special values of '+' means one or more and '*' as 0 or more.
     *
     * @return the number of arguments (as a String)
     */
    String numberOfArgumentsString() default "";

    /**
     * The default value for this option as a String; subject to type conversion and 'convert'.
     * Ignored for Boolean options.
     *
     * @return the default value for this option
     */
    String defaultValue() default "";

    /**
     * A conversion closure to convert the incoming String into the desired object
     *
     * @return the closure to convert this option's argument(s)
     */
    Class convert() default Undefined.CLASS.class;
}
