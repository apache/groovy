import com.google.gdata.client.*
import com.google.gdata.client.calendar.*
import com.google.gdata.data.*
import com.google.gdata.data.extensions.*
import com.google.gdata.util.*

import groovy.google.gdata.GDataCategory
import org.codehaus.groovy.runtime.TimeCategory;

def myId = System.properties.id
def myPassword = System.properties.pass
def feedUrl = "http://www.google.com/calendar/feeds/$myId/private/full"

use (TimeCategory, GDataCategory) {
    def myService = new CalendarService("codehausGroovy-groovyExampleApp-1")

    myService.userCredentials = [myId, myPassword]

    myService.getFeed(feedUrl, 1.week.ago, 4.weeks.from.now, 20).entries.each {entry ->
        entry.times.each {time ->
             println "${entry.title.text} From: ${time.startTime.toUiString()} To: ${(time.endTime.toUiString())}"
        }
    }

    myService.getFeed(feedUrl, 1.year.ago, 2.years, 20).entries.each {entry ->
        entry.times.each {time ->
            println "${entry.title.text} From: ${time.startTime.toUiString()} To: ${(time.endTime.toUiString())}"
        }
    }
}
