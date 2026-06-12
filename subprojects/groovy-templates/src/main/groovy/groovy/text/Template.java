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
package groovy.text;

import groovy.lang.Writable;

import java.util.Map;


/**
 * A template is a block of text with an associated binding that can be output to a writer or evaluated to a string.
 */
public interface Template {
    /**
     * Creates a writable view of this template using an empty binding.
     *
     * @return a writable object that renders this template
     */
    Writable make();

    /**
     * Creates a writable view of this template using the supplied binding.
     *
     * @param binding values available to template expressions; may be {@code null} if the implementation permits it
     * @return a writable object that renders this template with the given binding
     */
    Writable make(Map binding);
}
