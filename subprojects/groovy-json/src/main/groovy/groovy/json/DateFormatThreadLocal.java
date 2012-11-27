/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.json;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom thread local holding a <code>SimpleDateFormat</code>,
 * so that the <code>JsonOutput</code> class used by <code>JsonBuilder</code>
 * can be thread-safe when outputting dates and calendars.
 *
 * @author Guillaume Laforge
 */
public class DateFormatThreadLocal extends ThreadLocal<SimpleDateFormat> {
    @Override
    protected SimpleDateFormat initialValue() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        return formatter;
    }
}
