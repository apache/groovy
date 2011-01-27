/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.json

/**
 * The <code>Matching</code> enum provides three values used by the lexer,
 * to say if the content currently read on the reader is matching a certain token,
 * whether it's a possible match (ie. more input needed for fully matching the pattern),
 * or if it doesn't match at all.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
enum Matching {
    
    /** The current read input cannot match the pattern */
    NO,

    /** The current read input can match the pattern if more input is provided */
    POSSIBLE,

    /** The current read input matches the pattern */
    YES
}