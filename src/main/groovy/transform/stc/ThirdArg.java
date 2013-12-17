/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.stc;

public class ThirdArg extends PickAnyArgumentHint {
    public ThirdArg() {
        super(2,-1);
    }

    public static class FirstGenericType extends PickAnyArgumentHint {
        public FirstGenericType() {
            super(2,0);
        }
    }
    public static class SecondGenericType extends PickAnyArgumentHint {
        public SecondGenericType() {
            super(2,1);
        }
    }
    public static class ThirdGenericType extends PickAnyArgumentHint {
        public ThirdGenericType() {
            super(2,2);
        }
    }
}
