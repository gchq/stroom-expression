package stroom.dashboard.expression.v1;

import java.io.Serializable;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SerializableDateFormatter implements Serializable {
    private static final long serialVersionUID = 3482210112462557773L;

    private final String pattern;
    private final String timeZone;

    private transient DateTimeFormatter formatter;
    private transient ZoneId zoneId;

    public SerializableDateFormatter(final String pattern, final String timeZone) {
        this.pattern = pattern;
        this.timeZone = timeZone;
    }

    public long parse(final String value) throws ParseException {
        return DateUtil.parse(value, getFormatter(), getZoneId());
    }

    public String format(final Long value) throws ParseException {
        return DateUtil.format(value, getFormatter(), getZoneId());
    }

    private DateTimeFormatter getFormatter() {
        if (formatter == null) {
            if (pattern == null || DateUtil.DEFAULT_PATTERN.equals(pattern)) {
                formatter = DateUtil.DEFAULT_FORMATTER;
            } else {
                formatter = DateTimeFormatter.ofPattern(pattern);
            }
        }
        return formatter;
    }

    private ZoneId getZoneId() throws ParseException {
        if (zoneId == null) {
            if (timeZone == null) {
                zoneId = ZoneOffset.UTC;
            } else {
                zoneId = DateUtil.getTimeZone(timeZone);
            }
        }
        return zoneId;
    }
}
