package groovy

import java.text.SimpleDateFormat
import java.time.*
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class DateTimeTest extends GroovyTestCase {

    void testDurationPlusMinusPositiveNegative() {
        def duration = Duration.ofSeconds(10)
        def longer = duration + 5
        def shorter = duration - 5

        assert longer.seconds == 15
        assert shorter.seconds == 5
        assert (++longer).seconds == 16
        assert (--shorter).seconds == 4
    }

    void testInstantPlusMinusPositiveNegative() {
        def epoch = Instant.ofEpochMilli(0)

        def twoSecPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecPastEpoch - 1

        assert oneSecPastEpoch.epochSecond == 1
        assert twoSecPastEpoch.epochSecond == 2
        assert (++twoSecPastEpoch).epochSecond == 3
        assert (--oneSecPastEpoch).epochSecond == 0
    }

    void testLocalDatePlusMinusPositiveNegative() {
        def epoch = LocalDate.of(1970, Month.JANUARY, 1)

        def twoDaysPastEpoch = epoch + 2
        def oneDayPastEpoch = twoDaysPastEpoch - 1

        assert oneDayPastEpoch.dayOfMonth == 2
        assert twoDaysPastEpoch.dayOfMonth == 3
        assert (++twoDaysPastEpoch).dayOfMonth == 4
        assert (--oneDayPastEpoch).dayOfMonth == 1
    }

    void testLocalDateTimePlusMinusPositiveNegative() {
        def epoch = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0)

        def twoSecsPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecsPastEpoch - 1

        assert oneSecPastEpoch.second == 1
        assert twoSecsPastEpoch.second == 2
        assert (++twoSecsPastEpoch).second == 3
        assert (--oneSecPastEpoch).second == 0
    }

    void testLocalTimePlusMinusPositiveNegative() {
        def epoch = LocalTime.of(0, 0, 0, 0)

        def twoSecsPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecsPastEpoch - 1

        assert oneSecPastEpoch.second == 1
        assert twoSecsPastEpoch.second == 2
        assert (++twoSecsPastEpoch).second == 3
        assert (--oneSecPastEpoch).second == 0
    }

    void testOffsetDateTimePlusMinusPositiveNegative() {
        def epoch = OffsetDateTime.of(LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0),
                ZoneOffset.ofHours(0))

        def twoSecsPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecsPastEpoch - 1

        assert oneSecPastEpoch.second == 1
        assert twoSecsPastEpoch.second == 2
        assert (++twoSecsPastEpoch).second == 3
        assert (--oneSecPastEpoch).second == 0
    }

    void testOffsetTimePlusMinusPositiveNegative() {
        def epoch = OffsetTime.of(LocalTime.of(0, 0, 0, 0),
                ZoneOffset.ofHours(0))

        def twoSecsPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecsPastEpoch - 1

        assert oneSecPastEpoch.second == 1
        assert twoSecsPastEpoch.second == 2
        assert (++twoSecsPastEpoch).second == 3
        assert (--oneSecPastEpoch).second == 0
    }

    void testPeriodPlusMinusPositiveNegative() {
        def fortnight = Period.ofDays(14)

        def fortnightAndTwoDays = fortnight + 2
        def fortnightAndOneDay = fortnightAndTwoDays - 1

        assert fortnightAndOneDay.days == 15
        assert fortnightAndTwoDays.days == 16
        assert (++fortnightAndTwoDays).days == 17
        assert (--fortnightAndOneDay).days == 14
    }

    void testYearPlusMinusPositiveNegative() {
        def epoch = Year.of(1970)

        def twoYearsAfterEpoch = epoch + 2
        def oneYearAfterEpoch = twoYearsAfterEpoch - 1

        assert oneYearAfterEpoch.value == 1971
        assert twoYearsAfterEpoch.value == 1972
        assert (++twoYearsAfterEpoch).value == 1973
        assert (--oneYearAfterEpoch).value == 1970
    }

    void testYearMonthPlusMinusPositiveNegative() {
        def epoch = YearMonth.of(1970, Month.JANUARY)

        def twoMonthsAfterEpoch = epoch + 2
        def oneMonthAfterEpoch = twoMonthsAfterEpoch - 1

        assert oneMonthAfterEpoch.month == Month.FEBRUARY
        assert twoMonthsAfterEpoch.month == Month.MARCH
        assert (++twoMonthsAfterEpoch).month == Month.APRIL
        assert (--oneMonthAfterEpoch).month == Month.JANUARY
    }

    void testZonedDateTimePlusMinusPositiveNegative() {
        def epoch = ZonedDateTime.of(LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0),
                ZoneId.systemDefault())

        def twoSecsPastEpoch = epoch + 2
        def oneSecPastEpoch = twoSecsPastEpoch - 1

        assert oneSecPastEpoch.second == 1
        assert twoSecsPastEpoch.second == 2
        assert (++twoSecsPastEpoch).second == 3
        assert (--oneSecPastEpoch).second == 0
    }

    void testDayOfWeekPlusMinus() {
        def mon = DayOfWeek.MONDAY

        assert mon + 4 == DayOfWeek.FRIDAY
        assert mon - 4 == DayOfWeek.THURSDAY
    }

    void testMonthPlusMinus() {
        def jan = Month.JANUARY

        assert jan + 4 == Month.MAY
        assert jan - 4 == Month.SEPTEMBER
    }

    void testDurationPositiveNegative() {
        def positiveDuration = Duration.ofSeconds(3)
        assert (-positiveDuration).seconds == -3

        def negativeDuration = Duration.ofSeconds(-5)
        assert (+negativeDuration).seconds == 5
    }

    void testDurationMultiplyDivide() {
        def duration = Duration.ofSeconds(60)

        assert (duration / 2).seconds == 30
        assert (duration * 2).seconds == 120
    }

    void testPeriodPositiveNegative() {
        def positivePeriod = Period.of(1,2,3)
        Period madeNegative = -positivePeriod
        assert madeNegative.years == -1 : "All Period fields should be made negative"
        assert madeNegative.months == -2
        assert madeNegative.days == -3

        def negativePeriod = Period.of(-1,2,-3)
        Period madePositive = +negativePeriod
        assert madePositive.years == 1 : "Negative Period fields should be made positive"
        assert madePositive.months == 2 : "Positive Period fields should remain positive"
        assert madePositive.days == 3
    }

    void testPeriodMultiply() {
        def period = Period.of(1,1,1)
        Period doublePeriod = period * 2
        assert doublePeriod.years == 2
        assert doublePeriod.months == 2
        assert doublePeriod.days == 2
    }

    void testTemporalGetAt() {
        def epoch = Instant.ofEpochMilli(0)
        assert epoch[ChronoField.INSTANT_SECONDS] == 0
    }

    void testTemporalAmountGetAt() {
        def duration = Duration.ofHours(10)
        assert duration[ChronoUnit.SECONDS] == 36_000
    }

    void testZoneOffsetGetAt() {
        def offset = ZoneOffset.ofTotalSeconds(360)
        assert offset[ChronoField.OFFSET_SECONDS] == 360
    }

    void testTemporalRightShift() {
        def epoch = Instant.ofEpochMilli(0)
        def dayAfterEpoch = epoch + (60 * 60 * 24)
        Duration instantDuration = epoch >> dayAfterEpoch
        assert instantDuration == Duration.ofDays(1)
    }

    void testLocalDateRightShift() {
        def localDate1 = LocalDate.of(2000, Month.JANUARY, 1)
        def localDate2 = localDate1.plusYears(2)
        Period localDatePeriod = localDate1 >> localDate2
        assert localDatePeriod.years == 2
    }

    void testYearRightShift() {
        def year1 = Year.of(2000)
        def year2 = Year.of(2018)
        Period yearPeriod = year1 >> year2
        assert yearPeriod.years == 18
    }

    void testYearMonthRightShift() {
        def yearMonth1 = YearMonth.of(2018, Month.JANUARY)
        def yearMonth2 = YearMonth.of(2018, Month.MARCH)
        Period yearMonthPeriod = yearMonth1 >> yearMonth2
        assert yearMonthPeriod.months == 2
    }

    void testUptoDowntoWithSecondsDefaultUnit() {
        def epoch = Instant.ofEpochMilli(0)

        int uptoSelfIterations = 0
        epoch.upto(epoch) {
            ++uptoSelfIterations
            assert it == epoch  : 'upto closure should be provided with arg'
        }
        assert uptoSelfIterations == 1 : 'Iterating upto same value should call closure once'

        int downtoSelfIterations = 0
        epoch.downto(epoch) {
            ++downtoSelfIterations
            assert it == epoch : 'downto closure should be provided with arg'
        }
        assert downtoSelfIterations == 1 : 'Iterating downto same value should call closure once'

        int uptoPlusOneIterations = 0
        Instant endUp = null
        epoch.upto(epoch + 1) {
            ++uptoPlusOneIterations
            endUp = it
        }
        assert uptoPlusOneIterations == 2 : 'Iterating upto Temporal+1 value should call closure twice'
        assert endUp.epochSecond == 1 : 'Unexpected upto final value'

        int downtoPlusOneIterations = 0
        Instant endDown = null
        epoch.downto(epoch - 1) {
            ++downtoPlusOneIterations
            endDown = it
        }
        assert downtoPlusOneIterations == 2 : 'Iterating downto Temporal+1 value should call closure twice'
        assert endDown.epochSecond == -1 : 'Unexpected downto final value'
    }

    void testUptoDowntoWithYearsDefaultUnit() {
        // non-ChronoUnit.SECOND iterations
        def endYear = null
        Year.of(1970).upto(Year.of(1971)) { year -> endYear = year }
        assert endYear.value == 1971
    }

    void testUptoDownWithMonthsDefaultUnit() {
        def endYearMonth = null
        YearMonth.of(1970, Month.JANUARY).upto(YearMonth.of(1970, Month.FEBRUARY)) { yearMonth ->
            endYearMonth = yearMonth
        }
        assert endYearMonth.month == Month.FEBRUARY
    }

    void testUptoDowntoWithDaysDefaultUnit() {
        def endLocalDate = null
        LocalDate.of(1970, Month.JANUARY, 1).upto(LocalDate.of(1970, Month.JANUARY, 2)) { localDate ->
            endLocalDate = localDate
        }
        assert endLocalDate.dayOfMonth == 2
    }

    void testUptoDowntoWithIllegalReversedArguments() {
        def epoch = Instant.ofEpochMilli(0)
        try {
            epoch.upto(epoch - 1) {
                fail('upto() should fail when passed earlier arg')
            }
        } catch (GroovyRuntimeException e) {}
        try {
            epoch.downto(epoch + 1) {
                fail('downto() should fail when passed earlier arg')
            }
        } catch (GroovyRuntimeException e) {}
    }

    void testUptoDowntoWithCustomUnit() {
        LocalDateTime ldt1 = LocalDateTime.of(2018, Month.FEBRUARY, 11, 22, 9, 34)
        LocalDateTime ldt2 = ldt1.plusMinutes(1)

        int upIterations = 0
        LocalDateTime endUp = null
        ldt1.upto(ldt2, ChronoUnit.DAYS) {
            ++upIterations
            endUp = it
        }
        assert upIterations == 2
        assert endUp.dayOfMonth == 12 : "Upto should have iterated by DAYS"

        int downIterations = 0
        LocalDateTime endDown = null
        ldt2.downto(ldt1, ChronoUnit.YEARS) {
            ++downIterations
            endDown = it
        }
        assert downIterations == 2
        assert endDown.year == 2017 : "Downto should have iterated by YEARS"
    }

    void testInstantToDateToCalendar() {
        def epoch = Instant.ofEpochMilli(0).plusNanos(999_999)

        def date = epoch.toDate()
        def cal = epoch.toCalendar()
        assert cal.time == date
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS')
        sdf.timeZone = TimeZone.getTimeZone('GMT')
        assert sdf.format(date) == '1970-01-01 00:00:00.000'
    }

    void testLocalDateToDateToCalendar() {
        def ld = LocalDate.of(2018, Month.FEBRUARY, 12)

        Calendar cal = ld.toCalendar()
        assert cal.get(Calendar.YEAR) == 2018
        assert cal.get(Calendar.MONTH) == Calendar.FEBRUARY
        assert cal.get(Calendar.DAY_OF_MONTH) == 12
        assert cal.timeZone.getID() == TimeZone.default.getID()

        Date date = ld.toDate()
        assert date.format('yyyy-MM-dd') == '2018-02-12'
    }

    void testLocalDateTimeToDateToCalendar() {
        def ldt = LocalDateTime.of(2018, Month.FEBRUARY, 12, 22, 26, 30, 123_999_999)

        Calendar cal = ldt.toCalendar()
        assert cal.get(Calendar.YEAR) == 2018
        assert cal.get(Calendar.MONTH) == Calendar.FEBRUARY
        assert cal.get(Calendar.DAY_OF_MONTH) == 12
        assert cal.get(Calendar.HOUR_OF_DAY) == 22
        assert cal.get(Calendar.MINUTE) == 26
        assert cal.get(Calendar.SECOND) == 30
        assert cal.get(Calendar.MILLISECOND) == 123
        assert cal.timeZone.getID() == TimeZone.default.getID()

        Date date = ldt.toDate()
        assert date.format('yyyy-MM-dd HH:mm:ss.SSS') == '2018-02-12 22:26:30.123'
    }

    void testLocalTimeToDateToCalendar() {
        def today = Calendar.instance
        def lt = LocalTime.of(22, 38, 20, 9_999_999)

        Calendar cal = lt.toCalendar()
        assert cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) : 'LocalTime.toCalendar() should have current year'
        assert cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) : 'LocalTime.toCalendar() should have current month'
        assert cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) : 'LocalTime.toCalendar() should have current day'
        assert cal.get(Calendar.HOUR_OF_DAY) == 22
        assert cal.get(Calendar.MINUTE) == 38
        assert cal.get(Calendar.SECOND) == 20
        assert cal.get(Calendar.MILLISECOND) == 9
        assert cal.timeZone.getID() == TimeZone.default.getID()

        Date date = lt.toDate()
        assert date.format('HH:mm:ss.SSS') == '22:38:20.009'
    }

    void testOffsetDateTimeToDateToCalendar() {
        def ld = LocalDate.of(2018, Month.FEBRUARY, 12)
        def lt = LocalTime.of(22, 46, 10, 16_000_001)
        def offset = ZoneOffset.ofHours(-5)
        def odt = OffsetDateTime.of(ld, lt, offset)

        Calendar cal = odt.toCalendar()
        assert cal.get(Calendar.YEAR) == 2018
        assert cal.get(Calendar.MONTH) == Calendar.FEBRUARY
        assert cal.get(Calendar.DAY_OF_MONTH) == 12
        assert cal.get(Calendar.HOUR_OF_DAY) == 22
        assert cal.get(Calendar.MINUTE) == 46
        assert cal.get(Calendar.SECOND) == 10
        assert cal.get(Calendar.MILLISECOND) == 16
        assert cal.timeZone.getOffset(System.currentTimeMillis()) == -5 * 60 * 60 * 1000

        Date date = odt.toDate()
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS Z')
        sdf.timeZone = cal.timeZone
        assert sdf.format(date) == '2018-02-12 22:46:10.016 -0500'
    }

    void testOffsetTimeToDateToCalendar() {
        def lt = LocalTime.of(22, 53, 2, 909_900_009)
        def offset = ZoneOffset.ofHours(-4)
        def ot = OffsetTime.of(lt, offset)
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone('GMT-4'))

        Calendar cal = ot.toCalendar()
        assert cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) : 'OffsetTime.toCalendar() should have current year'
        assert cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) : 'OffsetTime.toCalendar() should have current month'
        assert cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) : 'OffsetTime.toCalendar() should have current day'
        assert cal.get(Calendar.HOUR_OF_DAY) == 22
        assert cal.get(Calendar.MINUTE) == 53
        assert cal.get(Calendar.SECOND) == 2
        assert cal.get(Calendar.MILLISECOND) == 909
        assert cal.timeZone.getOffset(System.currentTimeMillis()) == -4 * 60 * 60 * 1000

        Date date = ot.toDate()
        def sdf = new SimpleDateFormat('HH:mm:ss.SSS Z')
        sdf.timeZone = cal.timeZone
        assert sdf.format(date) == '22:53:02.909 -0400'
    }

    void testZonedDateTimeToDateToCalendar() {
        def ldt = LocalDateTime.of(2018, Month.FEBRUARY, 13, 20, 33, 57)
        def zoneId = ZoneId.ofOffset('GMT', ZoneOffset.ofHours(3))
        def zdt = ZonedDateTime.of(ldt, zoneId)

        Calendar cal = zdt.toCalendar()
        assert cal.get(Calendar.YEAR) == 2018
        assert cal.get(Calendar.MONTH) == Calendar.FEBRUARY
        assert cal.get(Calendar.DAY_OF_MONTH) == 13
        assert cal.get(Calendar.HOUR_OF_DAY) == 20
        assert cal.get(Calendar.MINUTE) == 33
        assert cal.get(Calendar.SECOND) == 57
        assert cal.get(Calendar.MILLISECOND) == 0
        assert cal.timeZone.getOffset(System.currentTimeMillis()) == 3 * 60 * 60 * 1000

        Date date = zdt.toDate()
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS Z')
        sdf.timeZone = cal.timeZone
        assert sdf.format(date) == '2018-02-13 20:33:57.000 +0300'
    }

    void testZoneOffsetExtensionProperties() {
        def offset = ZoneOffset.ofHoursMinutesSeconds(3,4,5)
        assert offset.hours == 3
        assert offset.minutes == 4
        assert offset.seconds == 5

        def negOffset = ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3)
        assert negOffset.hours == -1
        assert negOffset.minutes == -2
        assert negOffset.seconds == -3
    }

    void testZoneOffsetToZimeZone() {
        TimeZone utcTz = ZoneOffset.UTC.toTimeZone()
        assert utcTz.getID() == 'GMT'

        TimeZone noSecsTz = ZoneOffset.ofHoursMinutes(1, 30).toTimeZone()
        assert noSecsTz.getID() == 'GMT+01:30'

        TimeZone secsTz = ZoneOffset.ofHoursMinutesSeconds(-4, -15, -30).toTimeZone()
        assert secsTz.getID() == 'GMT-04:15'
    }

    void testZoneIdExtensionProperties() {
        def offset = ZoneOffset.ofHours(7)
        def zoneId = ZoneId.ofOffset('GMT', offset)

        assert zoneId.offset.totalSeconds == offset.totalSeconds
        assert zoneId.getOffset(Instant.now()).totalSeconds == offset.totalSeconds
        assert zoneId.shortName == 'GMT+07:00'
        assert zoneId.fullName == 'GMT+07:00'

        ZoneId ny = ZoneId.of('America/New_York')
        assert ny.getShortName(Locale.US) == 'ET'
        assert ny.getFullName(Locale.US) == 'Eastern Time'
    }

    void testZoneIdToTimeZone() {
        ZoneId ny = ZoneId.of('America/New_York')

        assert ny.toTimeZone() == TimeZone.getTimeZone(ny)
    }

    void testYearExtensionProperties() {
        def year = Year.of(2009)
        assert year.era == 1
        assert year.yearOfEra == 2009
    }

    void testDayOfWeekExtensionProperties() {
        assert DayOfWeek.SUNDAY.weekend
        assert DayOfWeek.MONDAY.weekday
    }

    void testYear_Month_leftShift() {
        def a = Year.now()
        def b = Month.JULY

        YearMonth x = a << b
        YearMonth y = b << a
        assert x == y
    }

    void testYear_MonthDay_leftShift() {
        def a = Year.now()
        def b = MonthDay.now()

        LocalDate x = a << b
        LocalDate y = b << a
        assert x == y
    }

    void testMonthDay_leftShift() {
        LocalDate d = MonthDay.of(Month.FEBRUARY, 13) << 2018
        assert d.year == 2018
        assert d.month == Month.FEBRUARY
        assert d.dayOfMonth == 13
    }

    void testMonth_leftShift() {
        MonthDay md = Month.JANUARY << 10
        assert md.month == Month.JANUARY
        assert md.dayOfMonth == 10
    }

    void testLocalDate_LocalTime_leftShift() {
        def a = LocalDate.now()
        def b = LocalTime.now()

        LocalDateTime x = a << b
        LocalDateTime y = b << a
        assert x == y
    }

    void testLocalDate_OffsetTime_leftShift() {
        def a = LocalDate.now()
        def b = OffsetTime.now()

        OffsetDateTime x = a << b
        OffsetDateTime y = b << a
        assert x == y
    }

    void testLocalDateTime_ZoneOffset_leftShift() {
        def a = LocalDateTime.now()
        def b = ZoneOffset.ofHours(5)

        OffsetDateTime x = a << b
        OffsetDateTime y = b << a
        assert x == y
    }

    void testLocalDateTime_ZoneId_leftShift() {
        def a = LocalDateTime.now()
        def b = ZoneId.systemDefault()

        ZonedDateTime x = a << b
        ZonedDateTime y = b << a
        assert x == y
    }

    void testLocalTime_ZoneOffset_leftShift() {
        def a = LocalTime.now()
        def b = ZoneOffset.ofHours(5)

        OffsetTime x = a << b
        OffsetTime y = b << a
        assert x == y
    }

    void testLocalDateTimeClearTime() {
        def d = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 9, 10, 100_032))
        d = d.clearTime()

        assert d.hour == 0
        assert d.minute == 0
        assert d.second == 0
        assert d.nano == 0
    }

    void testOffsetDateTimeClearTime() {
        def offset = ZoneOffset.ofHours(-1)
        def d = OffsetDateTime.of(LocalDate.now(), LocalTime.of(8, 9, 10, 100_032), offset)
        d = d.clearTime()

        assert d.hour == 0
        assert d.minute == 0
        assert d.second == 0
        assert d.nano == 0
        assert d.offset == offset : 'cleartTime() should not change offset'
    }

    void testZonedDateTimeClearTime() {
        def zone =  ZoneId.of('America/New_York')
        def d = ZonedDateTime.of(LocalDate.now(), LocalTime.of(8, 9, 10, 100_032), zone)
        d = d.clearTime()

        assert d.hour == 0
        assert d.minute == 0
        assert d.second == 0
        assert d.nano == 0
        assert d.zone == zone : 'cleartTime() should not change zone'
    }

    void testFormatByPattern() {
        def zone =  ZoneId.of('America/New_York')
        def offset = ZoneOffset.ofHours(2)

        LocalDate ld = LocalDate.of(2018, Month.FEBRUARY, 13)
        LocalTime lt = LocalTime.of(3,4,5,6_000_000)
        LocalDateTime ldt = LocalDateTime.of(ld, lt)
        OffsetTime ot = OffsetTime.of(lt, offset)
        OffsetDateTime odt = OffsetDateTime.of(ldt, offset)
        ZonedDateTime zdt = ZonedDateTime.of(ldt, zone)

        assert ld.format('yyyy-MM-dd') == '2018-02-13'
        assert lt.format('HH:mm:ss.SSS') == '03:04:05.006'
        assert ldt.format('yyyy-MM-dd HH:mm:ss.SSS') == '2018-02-13 03:04:05.006'
        assert ot.format('HH:mm:ss.SSS Z') == '03:04:05.006 +0200'
        assert odt.format('yyyy-MM-dd HH:mm:ss.SSS Z') == '2018-02-13 03:04:05.006 +0200'
        assert zdt.format('yyyy-MM-dd HH:mm:ss.SSS VV') == '2018-02-13 03:04:05.006 America/New_York'
    }

    void testLocalDateParse() {
        LocalDate ld = LocalDate.parse('2018-02-15', 'yyyy-MM-dd')
        assert [ld.year, ld.month, ld.dayOfMonth] == [2018, Month.FEBRUARY, 15]
    }

    void testLocalDateTimeParse() {
        LocalDateTime ldt = LocalDateTime.parse('2018-02-15 21:43:03.002', 'yyyy-MM-dd HH:mm:ss.SSS')
        assert [ldt.year, ldt.month, ldt.dayOfMonth] == [2018, Month.FEBRUARY, 15]
        assert [ldt.hour, ldt.minute, ldt.second] == [21, 43, 03]
        assert ldt.nano == 2 * 1e6
    }

    void testLocalTimeParse() {
        LocalTime lt = LocalTime.parse('21:43:03.002', 'HH:mm:ss.SSS')
        assert [lt.hour, lt.minute, lt.second] == [21, 43, 03]
        assert lt.nano == 2 * 1e6
    }

    void testOffsetDateTimeParse() {
        OffsetDateTime odt = OffsetDateTime.parse('2018-02-15 21:43:03.002 -00', 'yyyy-MM-dd HH:mm:ss.SSS X')
        assert [odt.year, odt.month, odt.dayOfMonth] == [2018, Month.FEBRUARY, 15]
        assert [odt.hour, odt.minute, odt.second] == [21, 43, 03]
        assert odt.nano == 2 * 1e6
        assert odt.offset.totalSeconds == 0
    }

    void testOffsetTimeParse() {
        OffsetTime ot = OffsetTime.parse('21:43:03.002 -00', 'HH:mm:ss.SSS X')
        assert [ot.hour, ot.minute, ot.second] == [21, 43, 03]
        assert ot.nano == 2 * 1e6
        assert ot.offset.totalSeconds == 0
    }

    void testZonedDateTimeParse() {
        ZonedDateTime zdt = ZonedDateTime.parse('2018-02-15 21:43:03.002 UTC', 'yyyy-MM-dd HH:mm:ss.SSS z')
        assert [zdt.year, zdt.month, zdt.dayOfMonth] == [2018, Month.FEBRUARY, 15]
        assert [zdt.hour, zdt.minute, zdt.second] == [21, 43, 03]
        assert zdt.nano == 2 * 1e6
    }
}
