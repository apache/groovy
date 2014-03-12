package metaprogramming

import groovy.time.TimeCategory

class CategoryTest extends GroovyTestCase {

    void testApplyTimeCategory() {
        // tag::time_category[]
        use(TimeCategory)  {
            println 1.minute.from.now       // <1>
            println 10.hours.ago

            def someDate = new Date()       // <2>
            println someDate - 3.months
        }
        // end::time_category[]
    }

    void testCategoryAnnotation() {
        assertScript '''
            // tag::time_category_anno[]
            class Distance {
                def number
                String toString() { "${number}m" }
            }

            @Category(Number)
            class NumberCategory {
                Distance getMeters() {
                    new Distance(number: this)
                }
            }

            use (NumberCategory)  {
                assert 42.meters.toString() == '42m'
            }
            // end::time_category_anno[]
        '''
    }
}
