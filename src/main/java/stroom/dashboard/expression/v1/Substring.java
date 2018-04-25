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

public class Substring extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "substring";

    private Function startFunction;
    private Function endFunction;

    private Generator gen;
    private Function function;
    private boolean hasAggregate;

    public Substring(final String name) {
        super(name, 3, 3);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        startFunction = parsePosParam(params[1], "second");
        endFunction = parsePosParam(params[2], "third");

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
            hasAggregate = function.hasAggregate();

        } else {
            function = new StaticValueFunction((Var) param);
            hasAggregate = false;

            // Optimise replacement of static input in case user does something stupid.
            if (startFunction instanceof StaticValueFunction && endFunction instanceof StaticValueFunction) {
                final String value = param.toString();
                final Double startPos = startFunction.createGenerator().eval().toDouble();
                final Double endPos = endFunction.createGenerator().eval().toDouble();

                if (value == null || startPos == null || endPos == null) {
                    gen = new StaticValueFunction(VarString.EMPTY).createGenerator();
                } else {
                    int start = startPos.intValue();
                    int end = endPos.intValue();

                    if (start < 0) {
                        start = 0;
                    }

                    if (end < 0 || end < start || start >= value.length()) {
                        gen = new StaticValueFunction(VarString.EMPTY).createGenerator();
                    } else if (end >= value.length()) {
                        gen = new StaticValueFunction(new VarString(value.substring(start))).createGenerator();
                    } else {
                        gen = new StaticValueFunction(new VarString(value.substring(start, end))).createGenerator();
                    }
                }
            }
        }
    }

    private Function parsePosParam(final Param param, final String paramPos) throws ParseException {
        Function function;
        if (param instanceof Function) {
            function = (Function) param;
            if (function.hasAggregate()) {
                throw new ParseException("Non aggregate function expected as " + paramPos + " argument of '" + name + "' function", 0);
            }
        } else {
            Integer pos = ((Var) param).toInteger();
            if (pos == null) {
                throw new ParseException("Number expected as " + paramPos + " argument of '" + name + "' function", 0);
            }
            function = new StaticValueFunction(new VarInteger(pos));
        }
        return function;
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }

        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator, startFunction.createGenerator(), endFunction.createGenerator());
    }

    @Override
    public boolean hasAggregate() {
        return hasAggregate;
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private Generator startPosGenerator;
        private Generator endPosGenerator;

        public Gen(final Generator childGenerator, final Generator startPosGenerator, final Generator endPosGenerator) {
            super(childGenerator);
            this.startPosGenerator = startPosGenerator;
            this.endPosGenerator = endPosGenerator;
        }

        @Override
        public void set(final Var[] values) {
            childGenerator.set(values);
            startPosGenerator.set(values);
            endPosGenerator.set(values);
        }

        @Override
        public Var eval() {
            final String value = childGenerator.eval().toString();
            if (value != null) {
                Integer startPos = startPosGenerator.eval().toInteger();
                Integer endPos = endPosGenerator.eval().toInteger();
                if (startPos == null || endPos == null) {
                    return VarString.EMPTY;
                }

                int start = startPos;
                int end = endPos;

                if (start < 0) {
                    start = 0;
                }

                if (end < 0 || end < start || start >= value.length()) {
                    return VarString.EMPTY;
                }

                if (end >= value.length()) {
                    return new VarString(value.substring(start));
                }
                return new VarString(value.substring(start, end));
            }

            return VarNull.INSTANCE;
        }
    }
}
