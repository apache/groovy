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
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.springframework.validation.Errors;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU;

 /**
 *  A  tag lib that provides tags for working with form controls
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */

class FormTagLib extends ApplicationTagLib {
    /**
     *  General linking to controllers, actions etc. Examples:
     *
     *  <g:form action="myaction">...</gr:form>
     *  <g:form controller="myctrl" action="myaction">...</gr:form>
     */
    @Property form = { attrs, body ->
        out << "<form action=\""
        // create the link
        createLink(attrs)

        out << '\" '
        // process remaining attributes
        attrs.each { k,v ->

            out << k << "=\"" << v << "\" "
        }
        out << ">"
        // output the body
        body()

        // close tag
        out << "</form>"
    }
    /**
     * A simple date picker that renders a date as selects
     * eg. <g:datePicker name="myDate" value="${new Date()}" />
     */
    @Property datePicker = { attrs ->
        def value = (attrs['value'] ? attrs['value'] : new Date())
        def name = attrs['name']
        def c = new GregorianCalendar();
        c.setTime(value)
        def day = c.get(GregorianCalendar.DAY_OF_MONTH)
        def month = c.get(GregorianCalendar.MONTH)
        def year = c.get(GregorianCalendar.YEAR)
        def hour = c.get(GregorianCalendar.HOUR_OF_DAY)
        def minute = c.get(GregorianCalendar.MINUTE)
		def dfs = new java.text.DateFormatSymbols(request.getLocale())


        out << "<input type='hidden' name='${name}' value='struct' />"

		// create day select
        out.println "<select name='${name}_day'>"

        for(i in 1..(day-1)) {
               out.println "<option value='${i}'>${i}</option>"
        }
        out.println "<option value='${day}' selected='selected'>${day}</option>"
        for(i in (day+1)..31) {
               out.println "<option value='${i}'>${i}</option>"
        }
        out.println '</select>'
		// create month select
		out.println "<select name='${name}_month'>"
		dfs.months.eachWithIndex { m,i ->
			if(m) {
				def monthIndex = i + 1
				out << "<option value='${i}'"
				if(month == i) out << 'selected="selected"'
				out << '>'
				out << m
				out.println '</option>'
			}
		}
		out.println '</select>'
		// create year select
		out.println "<select name='${name}_year'>"
        for(i in (year - 100)..(year-1)) {
			out.println "<option value='${i}'>${i}</option>"
        }
        out.println "<option value='${year}' selected='selected'>${year}</option>"
        for(i in (year + 1)..(year+100)) {
            out.println "<option value='${i}'>${i}</option>"
        }
		out.println '</select>'
		// do hour select
		out.println "<select name='${name}_hour'>"
        for(i in 0..23) {
			def h = '' + i
			if(i < 10) h = '0' + h
			out << "<option value='${h}' "
			if(hour == h) out << "selected='selected'"
			out << '>' << h << '</option>'
			out.println()
		}
		out.println '</select> :'

		// do minute select
		out.println "<select name='${name}_minute'>"
        for(i in 0..59) {
			def m = '' + i
			if(i < 10) m = '0' + m
			out << "<option value='${m}' "
			if(minute == m) out << "selected='selected'"
			out << '>' << m << '</option>'
			out.println()
		}
		out.println '</select>'
    }

    /**
     *  A helper tag for creating TimeZone selects
     * eg. <g:timeZoneSelect name="myTimeZone" value="${tz}" />
     */
    @Property timeZoneSelect = { attrs ->
        attrs['from'] = TimeZone.getAvailableIDs();
        attrs['value'] = (attrs['value'] ? attrs['value'].ID : TimeZone.getDefault().ID )

        // set the option value as a closure that formats the TimeZone for display
        attrs['optionValue'] = {
            TimeZone tz = TimeZone.getTimeZone(it);
            def shortName = tz.getDisplayName(tz.inDaylightTime(date),TimeZone.SHORT);
            def longName = tz.getDisplayName(tz.inDaylightTime(date),TimeZone.LONG);

            def offset = tz.rawOffset;
            def hour = offset / (60*60*1000);
            def min = Math.abs(offset / (60*1000)) % 60;

            return "${shortName}, ${longName} ${hour}:${min}"
        }

        // use generic select
        select( attrs )
    }

    /**
     *  A helper tag for creating locale selects
     *
     * eg. <g:localeSelect name="myLocale" value="${locale}" />
     */
    @Property localeSelect = {attrs ->
        attrs['from'] = Locale.getAvailableLocales()
        attrs['value'] = (attrs['value'] ? attrs['value'] : request.getLocale() )
        // set the key as a closure that formats the locale
        attrs['optionKey'] = { "${it.language}_${it.country}" }
        // set the option value as a closure that formats the locale for display
        attrs['optionValue'] = { "${it.language}, ${it.country},  ${it.displayName}" }

        // use generic select
        select( attrs )
    }

    /**
     * A helper tag for creating currency selects
     *
     * eg. <g:currencySelect name="myCurrency" value="${currency}" />
     */
    @Property currencySelect = { attrs, body ->
        if(!attrs['from']) {
            attrs['from'] = ['EUR', 'XCD','USD','XOF','NOK','AUD','XAF','NZD','MAD','DKK','GBP','CHF','XPF','ILS','ROL','TRL']
        }
        def currency = (attrs['value'] ? attrs['value'] : Currency.getInstance( request.getLocale() ))
        attrs['value'] = currency.currencyCode
        // invoke generic select
        select( attrs )
    }

    /**
     * A helper tag for creating HTML selects
     *
     * Examples:
     * <g:select name="user.age" from="${18..65}" value="${age}" />
     * <g:select name="user.company.id" from="${Company.list()}" value="${user?.company.id}" optionKey="id" />
     */
    @Property select = { attrs ->
        def from = attrs.remove('from')
        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def value = attrs.remove('value')

        out << "<select name='${attrs.remove('name')}' "
        // process remaining attributes
        attrs.each { k,v ->
            out << k << "=\"" << v << "\" "
        }
        out << '>'
        out.println()
        // create options from list
        if(from) {
            from.each {
                out << '<option '
                if(optionKey) {
                    if(optionKey instanceof Closure) {
                         out << 'value="' << optionKey(it) << '" '
                    }
                    else {
                        out << 'value="' << it.properties[optionKey] << '" '
                    }

                    if(it.properties[optionKey] == value) {
                        out << 'selected="selected" '
                    }
                }
                else {
                    out << "value='${it}' "
                    if(it == value) {
                        out << 'selected="selected" '
                    }
                }
                out << '>'
                if(optionValue) {
                    if(optionValue instanceof Closure) {
                         out << optionValue(it)
                    }
                    else {
                        out << it.properties[optionValue]
                    }
                }
                else {
                   out << it
                }
                out << '</option>'
                out.println()
            }
        }
        // close tag
        out << '</select>'
    }

    /**
     * A helper tag for creating checkboxes
     **/
    @Property checkBox = { attrs ->
          def value = attrs.remove('value')
          def name = attrs.remove('name')
          if(!value) value = false
          out << '<input type="hidden"'
          out << "name='_${name}' />"
          out << '<input type="checkbox" '
          out << "name='${name}' "
          if(attrs['value']) {
                out << 'checked="checked" '
          }
          out << "value='${value}' "
        // process remaining attributes
        attrs.each { k,v ->
            out << k << "=\"" << v << "\" "
        }
        // close the tag, with no body
        out << ' />'

    }

}