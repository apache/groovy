/*
 * Copyright 2003-2014 the original author or authors.
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

import java.text.SimpleDateFormat

/**
 * @author Andrey Bloschetsov
 */
class DateFormatThreadLocalTest extends GroovyTestCase {

    void testFormat() {
        // 14 March 2014, 9:18:12 in Moscow
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone('Europe/Moscow'))
        now.set(year: 2014, month: Calendar.MARCH, date: 14, hourOfDay: 9, minute: 18, second: 12)

        SimpleDateFormat formatter = new DateFormatThreadLocal().get()
        assert formatter.format(now.getTime()) == '2014-03-14T05:18:12+0000' // When in Moscow 9am in London 5am
    }
}