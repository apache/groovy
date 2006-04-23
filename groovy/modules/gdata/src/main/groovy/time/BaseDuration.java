/*
 * Created on Apr 22, 2006
 *
 * Copyright 2006 John G. Wilson
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package groovy.time;

import java.util.Date;

public abstract class BaseDuration {
    protected final long millis;

    protected BaseDuration(final long millis) {
        this.millis = millis;
    }
    
    public long getMillis() {
        return this.millis;
    }
    public abstract Date getAgo();
    
    public abstract From getFrom();
    
    public static abstract class From {
        public abstract Date getNow();
    }
}
