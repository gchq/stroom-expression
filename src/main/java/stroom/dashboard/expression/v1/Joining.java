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

import java.text.ParseException;

class Joining extends AbstractFunction {
    static final String NAME = "joining";

    private String delimiter = "";

    private Function function;

    public Joining(final String name) {
        super(name, 1, 2);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        if (params.length >= 2) {
            delimiter = parseStringParam(params[1], "second");
        }

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
        } else {
            function = new StaticValueFunction((Val) param);
        }
    }

    private String parseStringParam(final Param param, final String paramPos) throws ParseException {
        if (!(param instanceof ValString)) {
            throw new ParseException("String expected as " + paramPos + " argument of '" + name + "' function", 0);
        }
        return param.toString();
    }

    @Override
    public Generator createGenerator() {
        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator, delimiter);
    }

    @Override
    public boolean isAggregate() {
        return true;
    }

    @Override
    public boolean hasAggregate() {
        return isAggregate();
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private final String delimiter;
        private final StringBuilder sb = new StringBuilder();

        Gen(final Generator childGenerator, final String delimiter) {
            super(childGenerator);
            this.delimiter = delimiter;
        }

        @Override
        public void set(final Val[] values) {
            childGenerator.set(values);
            final Val val = childGenerator.eval();
            final String value = val.toString();
            if (value != null) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(value);
            }
        }

        @Override
        public Val eval() {
            return ValString.create(sb.toString());
        }
    }
}
