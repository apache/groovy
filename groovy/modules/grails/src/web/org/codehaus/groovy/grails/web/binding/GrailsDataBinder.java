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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.*;

/**
 * A data binder that handles binding dates that are specified with a "struct"-like syntax in request parameters.
 * For example for a set of fields defined as:
 *
 * <code>
     * <input type="hidden" name="myDate_year" value="2005" />
     * <input type="hidden" name="myDate_month" value="6" />
     * <input type="hidden" name="myDate_day" value="12" />
     * <input type="hidden" name="myDate_hour" value="13" />
     * <input type="hidden" name="myDate_minute" value="45" />
 * </code>
 *
 * This would set the property "myDate" of type java.util.Date with the specified values.
 *
 * @author Graeme Rocher
 * @since 05-Jan-2006
 */
public class GrailsDataBinder extends ServletRequestDataBinder {
    private static final Log LOG = LogFactory.getLog(GrailsDataBinder.class);
    /**
     * Create a new GrailsDataBinder instance.
     *
     * @param target     target object to bind onto
     * @param objectName objectName of the target object
     */
    public GrailsDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    /**
     * Utility method for creating a GrailsDataBinder instance
     *
     * @param target
     * @param objectName
     * @param request
     * @return A GrailsDataBinder instance
     */
    public static GrailsDataBinder createBinder(Object target, String objectName, HttpServletRequest request) {
        GrailsDataBinder binder = createBinder(target,objectName);
        binder.registerCustomEditor( Date.class, new CustomDateEditor(DateFormat.getDateInstance( DateFormat.SHORT, RequestContextUtils.getLocale(request) ),true) );
        return binder;
    }

    /**
     * Utility method for creating a GrailsDataBinder instance
     *
     * @param target
     * @param objectName
     * @return A GrailsDataBinder instance
     */
    public static GrailsDataBinder createBinder(Object target, String objectName) {
        GrailsDataBinder binder = new GrailsDataBinder(target,objectName);
        binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor());
        binder.registerCustomEditor( String.class, new StringMultipartFileEditor());
        binder.registerCustomEditor( Currency.class, new CurrencyEditor());
        binder.registerCustomEditor( Locale.class, new LocaleEditor());
        binder.registerCustomEditor( TimeZone.class, new TimeZoneEditor());
        return binder;
    }

    public void bind(ServletRequest request) {
        MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
        checkMultipartFiles(request, mpvs);

        checkStructuredDateDefinitions(request,mpvs);
        super.doBind(mpvs);
    }

    private void checkStructuredDateDefinitions(ServletRequest request, MutablePropertyValues mpvs) {

        PropertyValue[] pvs = mpvs.getPropertyValues();
        for (int i = 0; i < pvs.length; i++) {
            PropertyValue pv = pvs[i];

            try {
                String propertyName = pv.getName();
                Class type = super.getBeanWrapper().getPropertyType(propertyName);
                // if its a date check that it hasn't got structured parameters in the request
                // this is used as an alternative to specifying the date format
                if(type == Date.class) {
                    try {
                        int year = Integer.parseInt(request.getParameter(propertyName + "_year"));
                        int month = Integer.parseInt(request.getParameter(propertyName + "_month"));
                        int day = Integer.parseInt(request.getParameter(propertyName + "_day"));
                        int hour = Integer.parseInt(request.getParameter(propertyName + "_hour"));
                        int minute = Integer.parseInt(request.getParameter(propertyName + "_minute"));

                        Calendar c = new GregorianCalendar(year,month - 1,day,hour,minute);
                        mpvs.setPropertyValueAt(new PropertyValue(propertyName,c.getTime()),i);
                    }
                    catch(NumberFormatException nfe) {
                         LOG.warn("Unable to parse structured date from request for date ["+propertyName+"]",nfe);
                    }
                }
            }
            catch(InvalidPropertyException ipe) {
                // ignore
            }
        }
    }
}
