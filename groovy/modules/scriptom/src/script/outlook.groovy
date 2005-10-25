// Example how to access MS Outlook from Groovy Scriptom for reading
// tasks and appointments for further processing. --Dierk Koenig

import org.codehaus.groovy.scriptom.ActiveXProxy

namespace = new ActiveXProxy("Outlook.Application").getNamespace('MAPI')

CALENDAR_FOLDER =  9
TASK_FOLDER =     13

appointmentAttribs = '''
	start end duration subject location alldayevent 
	organizer importance sensitivity body'''

taskAttribs = '''
	body importance lastmodificationtime mileage noaging saved sensitivity size 
	subject unread actualwork complete contactnames datecompleted delegationstate 
	delegator duedate isrecurring ordinal owner ownership percentcomplete recipients 
	remindertime reminderoverridedefault responsestate role startdate status 
	statusoncompletionrecipients statusupdaterecipients teamtask totalwork 
	getrecurrencepattern statusreport'''

print(CALENDAR_FOLDER, appointmentAttribs)
print(TASK_FOLDER,     taskAttribs)


def print (folder, attributes) {
	entries = namespace.getDefaultFolder(folder)
	attribute_list = attributes.split(/\s+/).findAll{it.size()>0}
	count = entries.Items.count.value
	for (i in 1..count) {
		entry = entries.Items.item(i)
		attribute_list.each {attr -> println attr +"\t:\t" + entry[attr].value }
		println '----'
	}
}
