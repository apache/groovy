package groovy.json.internal


class DatesTest extends GroovyTestCase{

    // GROOVY-7462
    void testDatesFactory() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        Thread.sleep(1) // lets get some time between calling constructors

        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 == d2
    }

    void testDatesFactoryWithDefaultMs() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59,0)
        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 == d2
    }

    void testDatesFactoryEnforceDefaultMs() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59,1)
        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 != d2
    }
}
