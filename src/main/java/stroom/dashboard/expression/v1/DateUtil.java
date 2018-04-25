/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.dashboard.expression.v1;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

final class DateUtil {
    private static final int DATE_LENGTH = "2000-01-01T00:00:00.000Z".length();

    static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private DateUtil() {
        // Private constructor.
    }


    /**
     * Parse a 'normal' type date.
     *
     * @param date string date
     * @return date as milliseconds since epoch
     * @throws IllegalArgumentException if date does not parse
     */
    static long parseNormalDateTimeString(final String date) {
        if (date == null || date.length() != DATE_LENGTH) {
            throw new IllegalArgumentException("Unable to parse date: \"" + date + '"');
        }

        final ZonedDateTime dateTime = parseInternal(date, DEFAULT_FORMATTER, ZoneOffset.UTC);
        if (dateTime == null) {
            throw new IllegalArgumentException("Unable to parse date: \"" + date + '"');
        }

        return dateTime.toInstant().toEpochMilli();
    }

    static ZoneId getTimeZone(final String timeZone) throws ParseException {
        ZoneId dateTimeZone;

        if (timeZone != null) {
            try {
                dateTimeZone = ZoneId.of(timeZone);
            } catch (final DateTimeException | IllegalArgumentException e) {
                throw new ParseException("Time Zone '" + timeZone + "' is not recognised", 0);
            }
        } else {
            dateTimeZone = ZoneOffset.UTC;
        }

        return dateTimeZone;
    }

    static long parse(final String value, final DateTimeFormatter formatter, final ZoneId zoneId) {
        final ZonedDateTime dateTime = parseInternal(value, formatter, zoneId);
        if (dateTime == null) {
            throw new IllegalArgumentException("Unable to parse date: \"" + value + '"');
        }

        return dateTime.toInstant().toEpochMilli();
    }

    static String format(final Long value, final DateTimeFormatter formatter, final ZoneId zoneId) {
        return formatter.format(Instant.ofEpochMilli(value).atZone(zoneId));
    }

    private static ZonedDateTime parseInternal(final String value, final DateTimeFormatter formatter, final ZoneId zoneId) {
        final TemporalAccessor temporalAccessor = formatter.parseBest(value, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor).withZoneSameInstant(zoneId);
        }
        if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).atZone(zoneId);
        }
        return ((LocalDate) temporalAccessor).atStartOfDay(zoneId);
    }
}