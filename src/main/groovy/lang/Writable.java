/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.lang;

import java.io.IOException;
import java.io.Writer;


/**
 * Represents an object which is capable of writing itself to a text stream
 * in a more efficient format than just creating a toString() representation
 * of itself. This mechanism is particularly useful for templates and such like.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public interface Writable {

    /**
     * writes this object to the given stream
     */
    Writer writeTo(Writer out) throws IOException;
        
}
