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

import java.io.Serializable;
import java.text.ParseException;

public class Decode extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "decode";

    private Generator gen;
    private Function function;

    public Decode(final String name) {
        super(name, 4, Integer.MAX_VALUE);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        if (params.length % 2 == 1) {
            throw new ParseException("Expected to get an even number of arguments of '" + name + "' function", 0);
        }

        for (int i = 0; i < params.length - 1; i++) {
            final Param param = params[i];

            if (param instanceof Function) {
                if (((Function) param).hasAggregate()) {
                    throw new ParseException("Parameter cannot be an aggregating function in '" + name + "' function", 0);
                }
            }

            if (i % 2 == 1) {
                if (param instanceof VarString) {
                    final String regex = param.toString();
                    if (regex.length() == 0) {
                        throw new ParseException("An empty regex has been defined in '" + name + "' function", 0);
                    }
                }
            }
        }

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
        } else {
            gen = new StaticValueFunction((Var) param).createGenerator();
        }
    }

    @Override
    public Generator createGenerator() {
        final Test[] test = new Test[params.length / 2];
        final Generator[] result = new Generator[params.length / 2];
        Generator otherwise;

        int j = 0;
        for (int i = 1; i < params.length - 1; i++) {
            final Param param = params[i];

            if (i % 2 == 1) {
                if (param instanceof Function) {
                    final Generator generator = ((Function) param).createGenerator();
                    test[j] = new GeneratorTest(generator);

                } else if (param instanceof VarString) {
                    final String regex = param.toString();
                    test[j] = new PatternTest(new SerializablePattern(regex));
                } else {
                    final Generator generator = new StaticValueFunction((Var) param).createGenerator();
                    test[j] = new GeneratorTest(generator);
                }
            } else {
                if (param instanceof Function) {
                    final Generator generator = ((Function) param).createGenerator();
                    result[j] = generator;
                } else {
                    final Generator generator = new StaticValueFunction((Var) param).createGenerator();
                    result[j] = generator;
                }
                j++;
            }
        }

        final Param lastParam = params[params.length - 1];
        if (lastParam instanceof Function) {
            otherwise = ((Function) lastParam).createGenerator();
        } else {
            otherwise = new StaticValueFunction((Var) lastParam).createGenerator();
        }

        Generator childGenerator = gen;
        if (childGenerator == null) {
            childGenerator = function.createGenerator();
        }
        return new Gen(childGenerator, test, result, otherwise);
    }

    @Override
    public boolean hasAggregate() {
        return false;
    }

    private interface Test extends Serializable {
        boolean match(String value);
    }

    private static class PatternTest implements Test {
        private final SerializablePattern pattern;

        PatternTest(final SerializablePattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean match(final String value) {
            return pattern.matcher(value).matches();
        }
    }

    private static class GeneratorTest implements Test {
        private final Generator generator;

        GeneratorTest(final Generator generator) {
            this.generator = generator;
        }

        @Override
        public boolean match(final String value) {
            final String match = generator.eval().toString();
            if (match == null) {
                return false;
            }

            return match.equals(value);
        }
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private final Test[] test;
        private final Generator[] result;
        private final Generator otherwise;

        Gen(final Generator childGenerator,
            final Test[] test,
            final Generator[] result,
            final Generator otherwise) {
            super(childGenerator);
            this.test = test;
            this.result = result;
            this.otherwise = otherwise;
        }

        @Override
        public void set(final Var[] values) {
            childGenerator.set(values);
        }

        @Override
        public Var eval() {
            final Var val = childGenerator.eval();
            final String value = val.toString();
            if (value == null) {
                return VarNull.INSTANCE;
            }

            Var newValue = otherwise.eval();
            for (int i = 0; i < test.length; i++) {
                if (test[i].match(value)) {
                    newValue = result[i].eval();
                    break;
                }
            }

            return newValue;
        }
    }
}
