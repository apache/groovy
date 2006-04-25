import com.google.gdata.client.*
import com.google.gdata.client.calendar.*
import com.google.gdata.data.*
import com.google.gdata.data.extensions.*
import com.google.gdata.util.*

import groovy.google.gdata.GDataCategory
import org.codehaus.groovy.runtime.TimeCategory

def myId = System.properties.id
def myPassword = System.properties.pass
def feedUrl = "http://www.google.com/calendar/feeds/$myId/private/full"

use (TimeCategory, GDataCategory) {
    def myService = new CalendarService("codehausGroovy-groovyExampleApp-1")

    myService.userCredentials = [myId, myPassword]
    
    //
    // List existing entries
    //

    //
    //  Get at most 20 events in the period starting 1 week ago and ending 4 weeks in the future
    //
    myService.getFeed(feedUrl, 1.week.ago, 4.weeks.from.today, 20).entries.each {entry ->
        entry.times.each {time ->
             println "${entry.title.text} From: ${time.startTime.toUiString()} To: ${(time.endTime.toUiString())}"
        }
    }

    //
    //  Get at most 20 events in the period starting 1 year ago lasting 2 years
    //
    myService.getFeed(feedUrl, 1.year.ago, 2.years, 20).entries.each {entry ->
        entry.times.each {time ->
            println "${entry.title.text} From: ${time.startTime.toUiString()} To: ${(time.endTime.toUiString())}"
        }
    }
  
    
    //
    // Add an entry
    //
    
    // Use standard groovy magic to set the properties after construction
    def me = new Person(name: "John Wilson", email: "tugwilson@gmail.com", uri: "http://eek.ook.org")
    
    //
    // Need special magic in the GDataCategory to do this
    //
    // title and content are treated as plain text. If you want XHTML or XML then pass a closure or a
    // Buildable object and it will run it in a builder context
    //
    // Note that we can't use title and content in the Catagory as they are already properties of the class. 
    // Later I'll create a custom MetaClass for EventEntry which will let us use these names. Until then we'll mangle them
    //
    // author can be a single Person or a list of Person
    //
    // time can be a single When or a list of them
    //
    def newEntry = new EventEntry(title1: "This is a test event", content1: "this is some content", author: me,
                                  time: new When(start: 1.hour.from.now, end: 2.hours.from.now))
                                  
    myService.insert(feedUrl, newEntry)
}
