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
package groovy.io;

/**
 * Represents special return values for the 'preDir', 'postDir' and 'visit'/supplied Closures used with
 * {@link org.codehaus.groovy.runtime.ResourceGroovyMethods#traverse(java.io.File, java.util.Map, groovy.lang.Closure)}
 * and related methods to control subsequent traversal behavior.
 */
public enum FileVisitResult {
    /** Continue processing; the default */
    CONTINUE,
    /** Skip processing sibling files/directories within the current directory being processed */
    SKIP_SIBLINGS,
    /** Do not process the child files/subdirectories within the current directory being processed */
    SKIP_SUBTREE,
    /** Do not process any more files */
    TERMINATE
}
