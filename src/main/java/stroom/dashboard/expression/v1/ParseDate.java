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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ParseDate extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "parseDate";

    private String pattern = DateUtil.DEFAULT_PATTERN;
    private String timeZone;

    private Generator gen;
    private Function function;

    public ParseDate(final String name) {
        super(name, 1, 3);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        DateTimeFormatter formatter = DateUtil.DEFAULT_FORMATTER;
        ZoneId zoneId = ZoneOffset.UTC;

        if (params.length >= 2) {
            pattern = parseStringParam(params[1], "second");
            formatter = FormatterCache.getFormatter(pattern);
        }
        if (params.length >= 3) {
            timeZone = parseStringParam(params[2], "third");
            zoneId = FormatterCache.getZoneId(timeZone);
        }

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
            if (function.hasAggregate()) {
                throw new ParseException("Non aggregate function expected as first argument of '" + name + "' function", 0);
            }

        } else if (param instanceof VarString) {
            final String string = param.toString();
            final long millis = DateUtil.parse(string, formatter, zoneId);
            gen = new StaticValueFunction(VarLong.create(millis)).createGenerator();

        } else {
            final Long millis = ((Var) param).toLong();
            if (millis == null) {
                throw new ParseException("Unable to convert first argument of '" + name + "' function to long", 0);
            }
            gen = new StaticValueFunction(VarLong.create(millis)).createGenerator();
        }
    }

    private String parseStringParam(final Param param, final String paramPos) throws ParseException {
        if (!(param instanceof VarString)) {
            throw new ParseException("String expected as " + paramPos + " argument of '" + name + "' function", 0);
        }
        return param.toString();
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
                    return VarLong.create(FormatterCache.parse(value, pattern, timeZone));
                } catch (final RuntimeException e) {
                    return VarErr.create(e.getMessage());
                }
            }

            return VarNull.INSTANCE;
        }
    }
}
