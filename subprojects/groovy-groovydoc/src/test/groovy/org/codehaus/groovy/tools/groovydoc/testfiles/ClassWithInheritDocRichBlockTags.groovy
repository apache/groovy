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
package org.codehaus.groovy.tools.groovydoc.testfiles

import java.io.IOException

class InheritDocRichTagBase {
    /**
     * Base transform description that should not leak into block tags.
     * @param value groovy parent parameter description with {@code VALUE_TOKEN}
     * @return groovy parent return description with {@code RETURN_TOKEN}
     * @throws IllegalArgumentException groovy parent illegal-argument description with {@code BAD_VALUE_TOKEN}
     * @exception java.io.IOException groovy parent IO description with {@code IO_VALUE_TOKEN}
     */
    String transform(String value) throws IOException { value.toUpperCase() }
}

class InheritDocRichTagChild extends InheritDocRichTagBase {
    /**
     * Groovy rich child summary.
     * @param value child prefix {@inheritDoc} child suffix
     * @return leading {@inheritDoc} trailing
     * @throws java.lang.IllegalArgumentException before {@inheritDoc} after
     * @exception IOException wrapped {@inheritDoc} done
     */
    @Override
    String transform(String value) throws IOException { value.toLowerCase() }
}
