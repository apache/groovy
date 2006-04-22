import com.google.gdata.client.*
import com.google.gdata.client.calendar.*
import com.google.gdata.data.*
import com.google.gdata.data.extensions.*
import com.google.gdata.util.*

import groovy.google.gdata.GDataCategory

def myId = System.properties.id
def myPassword = System.properties.pass
def feedUrl = "http://www.google.com/calendar/feeds/$myId/private/full"

use (GDataCategory) {
    def myService = new CalendarService("codehausGroovy-groovyExampleApp-1")

    myService.userCredentials = [myId, myPassword]

    def myFeed = myService.getFeed(feedUrl)
    
    myFeed.entries.each {entry ->
        entry.times.each {time ->
            println "${entry.title.text} From: ${time.startTime.toUiString()} To: ${(time.endTime.toUiString())}"
            println "${(time.endTime - time.startTime + 2.weeks + 1.day + 5.seconds).toUiString()}"
            println "${(time.endTime - time.startTime + + 1.year + 7.months + 2.weeks + 1.day + 5.seconds).toUiString()}"
        }
    }
}
