/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy

/**
 * check that the new asImmutable() method works
 * as specified in GROOVY-623
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class ImmutableModificationTest extends GroovyTestCase {
    void testCollectionAsImmutable() {
        def challenger = ["Telson", "Sharna", "Darv", "Astra"]
        def hopefullyImmutable = challenger.asImmutable()
        try {
            challenger.add("Angel One")
            challenger << "Angel Two"

            // @todo fail("'challenger' is supposed to be an immutable collection.")

        } catch (UnsupportedOperationException e) {
            // success if this exception is thrown
            assert 4 == challenger.size()
        }
    }
}