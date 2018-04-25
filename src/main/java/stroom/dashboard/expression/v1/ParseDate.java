/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.dashboard.expression.v1;

import java.io.Serializable;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class ParseDate extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    private static final String GMT_BST_GUESS = "GMT/BST";
    private static final ZoneId EUROPE_LONDON_TIME_ZONE = ZoneId.of("Europe/London");

    public static final String NAME = "parseDate";

    private String pattern = DEFAULT_PATTERN;
    private String timeZone = null;

    private Generator gen;
    private Function function = null;

    public ParseDate(final String name) {
        super(name, 1, 3);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        if (params.length == 1) {
            final Param param = params[0];
            if (param instanceof Function) {
                function = (Function) param;
                if (function.hasAggregate()) {
                    throw new ParseException("Non aggregate function expected as first argument of '" + name + "' function", 0);
                }

            } else if (param instanceof VarString) {
                gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();

                final String string = param.toString();
                try {
                    final long millis = DateUtil.parseNormalDateTimeString(string);
                    gen = new StaticValueFunction(new VarLong(millis)).createGenerator();
                } catch (final RuntimeException e) {
                    // Ignore.
                }
            } else {
                final Long millis = ((Var) param).toLong();
                if (millis != null) {
                    gen = new StaticValueFunction(new VarLong(millis)).createGenerator();
                } else {
                    gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();
                }
            }
        } else {
            if (params.length >= 2) {
                pattern = parsePosParam(params[1], "second");
            }
            if (params.length >= 3) {
                timeZone = parsePosParam(params[2], "third");
            }

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            final ZoneId zoneId = ParseDate.getTimeZone(timeZone);

            final Param param = params[0];
            if (param instanceof Function) {
                function = (Function) param;
                if (function.hasAggregate()) {
                    throw new ParseException("Non aggregate function expected as first argument of '" + name + "' function", 0);
                }

            } else if (param instanceof VarString) {
                gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();

                final String string = param.toString();
                try {
                    final long millis = ParseDate.parse(string, formatter, zoneId);
                    gen = new StaticValueFunction(new VarLong(millis)).createGenerator();
                } catch (final RuntimeException e) {
                    // Ignore.
                }
            } else {
                throw new ParseException("Expected string or function as first parameter to '" + name + "'", 0);
            }
        }
    }

    private String parsePosParam(final Param param, final String paramPos) throws ParseException {
        if (!(param instanceof VarString)) {
            throw new ParseException("String expected as " + paramPos + " argument of '" + name + "' function", 0);
        }
        return param.toString();
    }

    private static ZoneId getTimeZone(final String timeZone) throws ParseException {
        ZoneId dateTimeZone;

        if (timeZone != null) {
            try {
                if (GMT_BST_GUESS.equals(timeZone)) {
                    dateTimeZone = EUROPE_LONDON_TIME_ZONE;
                } else {
                    dateTimeZone = ZoneId.of(timeZone);
                }
            } catch (final DateTimeException | IllegalArgumentException e) {
                throw new ParseException("Time Zone '" + timeZone + "' is not recognised", 0);
            }
        } else {
            dateTimeZone = ZoneOffset.UTC;
        }

        return dateTimeZone;
    }

    private static long parse(final String value, final DateTimeFormatter formatter, final ZoneId zoneId) {
        final TemporalAccessor temporalAccessor = formatter.parseBest(value, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        if (temporalAccessor instanceof ZonedDateTime) {
            return (((ZonedDateTime) temporalAccessor).withZoneSameInstant(zoneId)).toInstant().toEpochMilli();
        }
        if (temporalAccessor instanceof LocalDateTime) {
            return (((LocalDateTime) temporalAccessor).atZone(zoneId)).toInstant().toEpochMilli();
        }
        return (((LocalDate) temporalAccessor).atStartOfDay(zoneId)).toInstant().toEpochMilli();
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }

        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator, pattern, timeZone);
    }

    @Override
    public boolean hasAggregate() {
        return false;
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private final String pattern;
        private final String timeZone;

        private transient DateTimeFormatter formatter;
        private transient ZoneId zoneId;

        public Gen(final Generator childGenerator, final String pattern, final String timeZone) {
            super(childGenerator);
            this.pattern = pattern;
            this.timeZone = timeZone;
        }

        @Override
        public void set(final Var[] values) {
            childGenerator.set(values);
        }

        @Override
        public Var eval() {
            final String value = childGenerator.eval().toString();
            if (value != null) {
                try {
                    final long millis = ParseDate.parse(value, getFormatter(), getZoneId());
                    return new VarLong(millis);
                } catch (final ParseException | RuntimeException e) {
                    return new VarErr(e.getMessage());
                }
            }

            return VarNull.INSTANCE;
        }

        private DateTimeFormatter getFormatter() {
            if (formatter == null) {
                if (pattern == null || DEFAULT_PATTERN.equals(pattern)) {
                    formatter = DateUtil.NORMAL_STROOM_TIME_FORMATTER;
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
                    zoneId = ParseDate.getTimeZone(timeZone);
                }
            }
            return zoneId;
        }
    }
}
