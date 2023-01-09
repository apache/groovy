/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
/**
 * Refer to pleac.sourceforge.net if wanting accurate comparisons with PERL.
 * Original author has included tweaked examples here solely for the purposes
 * of exercising the Groovy compiler.
 * In some instances, examples have been modified to avoid additional
 * dependencies or for dependencies not in common repos.
 */

// @@PLEAC@@_3.0
//----------------------------------------------------------------------------------
// use Date to get the current time
println new Date()
// => Mon Jan 01 07:12:32 EST 2007
// use Calendar to compute year, month, day, hour, minute, and second values
cal = Calendar.instance
println 'Today is day ' + cal.get(Calendar.DAY_OF_YEAR) + ' of the current year.'
// => Today is day 1 of the current year.
// there are other Java Date/Time packages with extended capabilities, e.g.:
//     http://joda-time.sourceforge.net/
// there is a special Grails (grails.org) time DSL (see below)
//----------------------------------------------------------------------------------
import static java.util.Calendar.*
import java.time.*
println "Today is day ${cal[DAY_OF_YEAR]} of the current year."
println LocalDateTime.now()
// => 2022-08-24T17:59:41.359654800
println "Today is day ${LocalDateTime.now().dayOfYear} of the current year."

// @@PLEAC@@_3.1
//----------------------------------------------------------------------------------
cal = instance
Y = cal.get(YEAR)
M = cal.get(MONTH) + 1
D = cal.get(DATE)
println "The current date is $Y $M $D"
// => The current date is 2006 04 28
//----------------------------------------------------------------------------------
(_E, Y, M, _WY, _WM, D) = instance
println "The current date is $Y $M $D"
// => The current date is 2022 07 24
(Y, M, D) = instance[1, 2, 5]
println "The current date is $Y $M $D"
// => The current date is 2022 07 24

// @@PLEAC@@_3.2
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = instance
// set time zone using long or short timezone values
cal.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
cal.timeZone = TimeZone.getTimeZone("UTC")
// set date fields one at a time
cal[MONTH] = DECEMBER
// or several together
//cal.set(year, month - 1, day, hour, minute, second)
// get time in seconds since EPOCH
long time = cal.time.time / 1000
println time
// => 1196522682
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.3
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = instance
// set time
cal.time = new Date(time * 1000)
// get date fields
println "Dateline: ${cal[HOUR_OF_DAY]}:${cal[MINUTE]}:${cal[SECOND]}-${cal[YEAR]}/${cal[MONTH]}/${cal[DAY_OF_MONTH]}"
// => Dateline: 7:33:16-2007/1/1
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.4
//----------------------------------------------------------------------------------
import java.text.SimpleDateFormat
long difference = 100
long after = time + difference
long before = time - difference

// any field of a calendar is incrementable via add() and roll() methods
cal = instance
df = new SimpleDateFormat()
printCal = {cal -> df.format(cal.time)}
cal.set(2000, 0, 1, 00, 01, 0)
assert printCal(cal) == '1/01/00 00:01'
// roll minute back by 2 but don't adjust other fields
cal.roll(MINUTE, -2)
assert printCal(cal) == '1/01/00 00:59'
// adjust hour back 1 and adjust other fields if needed
cal.add(HOUR, -1)
assert printCal(cal) == '31/12/99 23:59'

// larger example
cal.timeZone = TimeZone.getTimeZone("UTC")
cal.set(1973, 0, 18, 3, 45, 50)
cal.add(DAY_OF_MONTH, 55)
cal.add(HOUR_OF_DAY, 2)
cal.add(MINUTE, 17)
cal.add(SECOND, 5)
assert printCal(cal) == '14/03/73 16:02'

// alternatively, work with epoch times
long birthTime = 96176750359       // 18/Jan/1973, 3:45:50 am
long interval = 5 +                // 5 second
                17 * 60 +          // 17 minute
                2  * 60 * 60 +     // 2 hour
                55 * 60 * 60 * 24  // and 55 day
then = new Date(birthTime + interval * 1000)
assert df.format(then) == '14/03/73 16:02'

// Alternatively, the Google Data module has a category with DSL-like time support:
// http://docs.codehaus.org/display/GROOVY/Google+Data+Support
// which supports the following syntax
// def interval = 5.seconds + 17.minutes + 2.hours + 55.days
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.5
//----------------------------------------------------------------------------------
bree = 361535725  // 16 Jun 1981, 4:35:25
nat  =  96201950  // 18 Jan 1973, 3:45:50
difference = bree - nat
println "There were $difference seconds between Nat and Bree"
// => There were 265333775 seconds between Nat and Bree
seconds    =  difference % 60
difference = (difference - seconds) / 60
minutes    =  difference % 60
difference = (difference - minutes) / 60
hours      =  difference % 24
difference = (difference - hours)   / 24
days       =  difference % 7
weeks      = (difference - days)    /  7
println "($weeks weeks, $days days, $hours:$minutes:$seconds)"
// => (438 weeks, 4 days, 23:49:35)
//----------------------------------------------------------------------------------
cal = getInstance(TimeZone.getTimeZone("UTC"))
cal.set(1981, 5, 16)  // 16 Jun 1981
date1 = cal.time
cal.set(1973, 0, 18)  // 18 Jan 1973
date2 = cal.time
difference = Math.abs(date2.time - date1.time)
days = difference / (1000 * 60 * 60 * 24)
assert days == 3071
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.6
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = instance
cal.set(1981, 5, 16)
yearDay = cal.get(DAY_OF_YEAR);
year = cal.get(YEAR);
yearWeek = cal.get(WEEK_OF_YEAR);
df1 = new SimpleDateFormat("dd/MMM/yy")
df2 = new SimpleDateFormat("EEEE")
print(df1.format(cal.time) + ' was a ' + df2.format(cal.time))
println " and was day number $yearDay and week number $yearWeek of $year"
// => 16/Jun/81 was a Tuesday and was day number 167 and week number 25 of 1981
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.7
//----------------------------------------------------------------------------------
input = "1998-06-03"
df1 = new SimpleDateFormat("yyyy-MM-dd")
date = df1.parse(input)
df2 = new SimpleDateFormat("MMM/dd/yyyy")
println 'Date was ' + df2.format(date)
// => Date was Jun/03/1998
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.8
//----------------------------------------------------------------------------------
import java.text.DateFormat
df = new SimpleDateFormat('E M d hh:mm:ss z yyyy')
cal.set(2007, 0, 1)
println 'Customized format gives: ' + df.format(cal.time)
// => Mon 1 1 09:02:29 EST 2007 (differs depending on your timezone)
df = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE)
println 'Customized format gives: ' + df.format(cal.time)
// => lundi 1 janvier 2007
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.9
//----------------------------------------------------------------------------------
// script:
println 'Press return when ready'
before = System.currentTimeMillis()
input = new BufferedReader(new InputStreamReader(System.in)).readLine()
after = System.currentTimeMillis()
elapsed = (after - before) / 1000
println "You took $elapsed seconds."
// => You took2.313 seconds.

// take mean sorting time
size = 500; number = 100; total = 0
for (i in 0..<number) {
    array = []
    size.times{ array << Math.random() }
    doubles = array as double[]
    // sort it
    long t0 = System.currentTimeMillis()
    Arrays.sort(doubles)
    long t1 = System.currentTimeMillis()
    total += (t1 - t0)
}
println "On average, sorting $size random numbers takes ${total / number} milliseconds"
// => On average, sorting 500 random numbers takes 0.32 milliseconds
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.10
//----------------------------------------------------------------------------------
delayMillis = 50
Thread.sleep(delayMillis)
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.11
//----------------------------------------------------------------------------------
// this could be done more simply using JavaMail's getAllHeaderLines() but is shown
// in long hand for illustrative purposes
sampleMessage = '''Delivered-To: alias-someone@somewhere.com.au
Received: (qmail 27284 invoked from network); 30 Dec 2006 15:16:26 -0000
Received: from unknown (HELO lists-outbound.sourceforge.net) (66.35.250.225)
  by bne012m.server-web.com with SMTP; 30 Dec 2006 15:16:25 -0000
Received: from sc8-sf-list2-new.sourceforge.net (sc8-sf-list2-new-b.sourceforge.net [10.3.1.94])
    by sc8-sf-spam2.sourceforge.net (Postfix) with ESMTP
    id D8CCBFDE3; Sat, 30 Dec 2006 07:16:24 -0800 (PST)
Received: from sc8-sf-mx1-b.sourceforge.net ([10.3.1.91]
    helo=mail.sourceforge.net)
    by sc8-sf-list2-new.sourceforge.net with esmtp (Exim 4.43)
    id 1H0fwX-0003c0-GA
    for pleac-discuss@lists.sourceforge.net; Sat, 30 Dec 2006 07:16:20 -0800
Received: from omta05ps.mx.bigpond.com ([144.140.83.195])
    by mail.sourceforge.net with esmtp (Exim 4.44) id 1H0fwY-0005D4-DD
    for pleac-discuss@lists.sourceforge.net; Sat, 30 Dec 2006 07:16:19 -0800
Received: from win2K001 ([138.130.127.127]) by omta05ps.mx.bigpond.com
    with SMTP
    id <20061230151611.XVWL19269.omta05ps.mx.bigpond.com@win2K001>;
    Sat, 30 Dec 2006 15:16:11 +0000
From: someone@somewhere.com
To: <pleac-discuss@lists.sourceforge.net>
Date: Sun, 31 Dec 2006 02:14:57 +1100
Subject: Re: [Pleac-discuss] C/Posix/GNU - @@pleac@@_10x
Content-Type: text/plain; charset="us-ascii"
Content-Transfer-Encoding: 7bit
Sender: pleac-discuss-bounces@lists.sourceforge.net
Errors-To: pleac-discuss-bounces@lists.sourceforge.net

----- Original Message -----
From: someone@somewhere.com
To: otherperson@somewhereelse.com
Cc: <pleac-discuss@lists.sourceforge.net>
Sent: Wednesday, December 27, 2006 9:18 AM
Subject: Re: [Pleac-discuss] C/Posix/GNU - @@pleac@@_10x

I really like that description of PLEAC.
'''
expected = '''
Sender                    Recipient                 Time              Delta
<origin>                  somewhere.com             01:14:57 06/12/31 
win2K001                  omta05ps.mx.bigpond.com   01:14:57 06/12/31 1m 14s
omta05ps.mx.bigpond.com   mail.sourceforge.net      01:16:11 06/12/31 8s
sc8-sf-mx1-b.sourceforge. sc8-sf-list2-new.sourcefo 01:16:19 06/12/31 1s
sc8-sf-list2-new.sourcefo sc8-sf-spam2.sourceforge. 01:16:20 06/12/31 4s
unknown                   bne012m.server-web.com    01:16:24 06/12/31 1s
'''

class MailHopDelta {
    def headers, firstSender, firstDate, out

    MailHopDelta(mail) {
        extractHeaders(mail)
        out = new StringBuffer()
        def m = (mail =~ /(?m)^Date:\s+(.*)/)
        firstDate = parseDate(m[0][1])
        firstSender = (mail =~ /(?m)^From.*\@([^\s>]*)/)[0][1]
        out('Sender Recipient Time Delta'.split(' '))
    }

    def parseDate(date) {
        try {
            return new SimpleDateFormat('EEE, dd MMM yyyy hh:mm:ss Z').parse(date)
        } catch(java.text.ParseException ex) {}
        try {
            return new SimpleDateFormat('dd MMM yyyy hh:mm:ss Z').parse(date)
        } catch(java.text.ParseException ex) {}
        try {
            return DateFormat.getDateInstance(DateFormat.FULL).parse(date)
        } catch(java.text.ParseException ex) {}
        DateFormat.getDateInstance(DateFormat.LONG).parse(date)
    }

    def extractHeaders(mail) {
        headers = []
        def isHeader = true
        def currentHeader = ''
        mail.split('\n').each{ line ->
            if (!isHeader) return
            switch(line) {
                case ~/^\s*$/:
                    isHeader = false
                    if (currentHeader) headers << currentHeader
                    break
                case ~/^\s+.*/:
                    currentHeader += line; break
                default:
                    if (currentHeader) headers << currentHeader
                    currentHeader = line
            }
        }
    }

    def out(line) {
        out << line[0][0..<[25,line[0].size()].min()].padRight(26)
        out << line[1][0..<[25,line[1].size()].min()].padRight(26)
        out << line[2].padRight(17) + ' '
        out << line[3] + '\n'
    }

    def prettyDate(date) {
        new SimpleDateFormat('hh:mm:ss yy/MM/dd').format(date)
    }

    def process() {
        out(['<origin>', firstSender, prettyDate(firstDate), ''])
        def prevDate = firstDate
        headers.grep(~/^Received:\sfrom.*/).reverseEach{ hop ->
            def from = (hop =~ /from\s+(\S+)|\((.*?)\)/)[0][1]
            def by   = (hop =~ /by\s+(\S+\.\S+)/)[0][1]
            def hopDate = parseDate(hop[hop.lastIndexOf(';')+2..-1])
            out([from, by, prettyDate(prevDate), prettyDelta(hopDate.time - prevDate.time)])
            prevDate = hopDate
        }
        return out.toString()
    }

    def prettyField(secs, sign, ch, multiplier, sb) {
        def whole = (int)(secs / multiplier)
        if (!whole) return 0
        sb << '' + (sign * whole) + ch + ' '
        return whole * multiplier
    }

    def prettyDelta(millis) {
        def sign = millis < 0 ? -1 : 1
        def secs = (int)Math.abs(millis/1000)
        def sb = new StringBuffer()
        secs -= prettyField(secs, sign, 'd', 60 * 60 * 24, sb)
        secs -= prettyField(secs, sign, 'h', 60 * 60, sb)
        secs -= prettyField(secs, sign, 'm', 60, sb)
        prettyField(secs, sign, 's', 1, sb)
        return sb.toString().trim()
    }
}

assert '\n' + new MailHopDelta(sampleMessage).process() == expected
//----------------------------------------------------------------------------------
