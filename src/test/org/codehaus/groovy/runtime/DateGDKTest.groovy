package org.codehaus.groovy.runtime

import java.text.DateFormat
import java.text.SimpleDateFormat
/**
 * @author tnichols
 */
public class DateGDKTest extends GroovyTestCase {
	
	public void testGDKDateMethods() {
		Locale defaultLocale = Locale.default
		TimeZone defaultTZ = TimeZone.default
		Locale locale = Locale.UK
		Locale.setDefault locale // set this otherwise the test will fail if your locale isn't the same
		TimeZone.setDefault TimeZone.getTimeZone( 'Etc/GMT' )
		Date d = new Date(0)
		println d.dateString
		println d.timeString
		println d.dateTimeString
		assertEquals '1970-01-01', d.format( 'yyyy-MM-dd' )
		assertEquals DateFormat.getDateInstance(DateFormat.SHORT,locale).format(d), d.dateString
		assertEquals '01/01/70', d.dateString
		assertEquals DateFormat.getTimeInstance(DateFormat.MEDIUM,locale).format(d), d.timeString
		assertEquals '00:00:00', d.timeString
		assertEquals DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM,locale).format(d), d.dateTimeString
		assertEquals '01/01/70 00:00:00', d.dateTimeString
		
		Locale.default = defaultLocale
		TimeZone.setDefault defaultTZ
	}
}
