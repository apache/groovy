package groovy.time;

import java.util.Calendar;
import java.util.Date;

/**
 * Ago Date Result
 *
 * @author Aliaksei Bialiauski (abialiauski@solvd.com)
 */
public class TimeAgoFromData extends Date implements AgoDate {

    private final Calendar calendar;
    private final Date date;

    public TimeAgoFromData(final int days, final int hours,
                           final int minutes, final int sec, final int millis) {
        this.calendar = Calendar.getInstance();
        this.date = this.unbox(days, hours, minutes, sec, millis);
    }

    @Override
    public Date toDateFormat() {
        return this.date;
    }

    private Date unbox(final int days, final int hours, final int minutes,
                       final int sec, final int millis) {
        this.calendar.add(Calendar.DAY_OF_YEAR, -days);
        this.calendar.add(Calendar.HOUR_OF_DAY, -hours);
        this.calendar.add(Calendar.MINUTE, -minutes);
        this.calendar.add(Calendar.SECOND, -sec);
        this.calendar.add(Calendar.MILLISECOND, -millis);
        return this.calendar.getTime();
    }
}
