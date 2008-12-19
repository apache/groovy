import groovy.xml.MarkupBuilder

class BuilderPerfTest extends GroovyTestCase {
    void formatAsXml(Writer writer) {
        def builder = new MarkupBuilder(writer)

        builder.Bookings {
            Booking {
                Origin("Auckland")
                Destination("Wellington")
                PassengerName("Mr John Smith")
            }
            Payment {
                From("J Smith")
                Amount(42)
            }
        }
    }

    void testMe () {
        long start = System.currentTimeMillis()
        def writer
        5000.times({
            writer = new StringWriter()
            formatAsXml(writer)
        })
        println "Took ${System.currentTimeMillis() - start} millis"
    }
}
