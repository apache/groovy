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
package groovy.transform

import java.lang.annotation.Documented

/**
 * An annotation which is just a shortcut for @CompileStatic(TypeCheckingMode.SKIP).
 * This can be used for example if you statically compile a full class but you want to skip
 * some methods without having to use the full annotation.
 *
 * @since 2.1.0
 */
@Documented
@AnnotationCollector(processor = 'org.codehaus.groovy.transform.CompileDynamicProcessor')
@interface CompileDynamic {
}