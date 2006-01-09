/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.binding;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Graeme Rocher
 * @since 05-Jan-2006
 */
public class GrailsDataBinderTests extends TestCase {

    class TestBean {
        private Date myDate;

        public Date getMyDate() {
            return myDate;
        }

        public void setMyDate(Date myDate) {
            this.myDate = myDate;
        }
    }
    public void testBindStructuredDate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("myDate","struct");
        request.addParameter("myDate_year","2006");
        request.addParameter("myDate_month","1");
        request.addParameter("myDate_day","31");
        request.addParameter("myDate_hour","16");
        request.addParameter("myDate_minute","45");

        TestBean testBean = new TestBean();
        GrailsDataBinder binder = new GrailsDataBinder(testBean,"testBean");
        binder.bind(request);

        assertNotNull(testBean.getMyDate());
        System.out.println(testBean.getMyDate());
        Calendar c = new GregorianCalendar();
        c.setTime(testBean.getMyDate());

        assertEquals(2006,c.get(Calendar.YEAR));
        assertEquals(0,c.get(Calendar.MONTH));
        assertEquals(31,c.get(Calendar.DAY_OF_MONTH));
        assertEquals(16,c.get(Calendar.HOUR_OF_DAY));
        assertEquals(45,c.get(Calendar.MINUTE));

    }
}
