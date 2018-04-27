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
import java.util.regex.Pattern;

public class Decode extends AbstractManyChildFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "decode";

    private Generator gen;
    private boolean simple;

    public Decode(final String name) {
        super(name, 4, Integer.MAX_VALUE);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        if (params.length % 2 == 1) {
            throw new ParseException("Expected to get an even number of arguments of '" + name + "' function", 0);
        }

        // See if this is a static computation.
        simple = true;
        for (Param param : params) {
            if (!(param instanceof Var)) {
                simple = false;
                break;
            }
        }

        if (simple) {
            // Static computation.
            final String value = params[0].toString();

            String newValue = params[params.length - 1].toString();
            for (int i = 1; i < params.length - 1; i += 2) {
                final String regex = params[i].toString();
                if (regex.length() == 0) {
                    throw new ParseException("An empty regex has been defined for argument of '" + name + "' function", 0);
                }

                final Pattern pattern = PatternCache.get(regex);
                if (pattern.matcher(value).matches()) {
                    newValue = params[i + 1].toString();
                    break;
                }
            }

            gen = new StaticValueFunction(VarString.create(newValue)).createGenerator();

        } else {
            for (int i = 1; i < params.length - 1; i += 2) {
                if (params[i] instanceof Var) {
                    // Test regex is valid.
                    final String regex = params[i].toString();
                    if (regex.length() == 0) {
                        throw new ParseException("An empty regex has been defined for argument of '" + name + "' function", 0);
                    }
                    PatternCache.get(regex);
                }
            }
        }
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }
        return super.createGenerator();
    }

    @Override
    protected Generator createGenerator(final Generator[] childGenerators) {
        return new Gen(childGenerators);
    }

    @Override
    public boolean hasAggregate() {
        if (simple) {
            return false;
        }
        return super.hasAggregate();
    }

    private static class Gen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        public Gen(final Generator[] childGenerators) {
            super(childGenerators);
        }

        @Override
        public void set(final Var[] values) {
            for (final Generator generator : childGenerators) {
                generator.set(values);
            }
        }

        @Override
        public Var eval() {
            final Var val = childGenerators[0].eval();
            if (!val.hasValue()) {
                return val;
            }

            try {
                final String value = val.toString();
                String newValue = childGenerators[childGenerators.length - 1].eval().toString();
                for (int i = 1; i < childGenerators.length - 1; i += 2) {
                    final String regex = childGenerators[i].eval().toString();
                    if (regex == null || regex.length() == 0) {
                        throw new ParseException("Empty regex", 0);
                    }

                    final Pattern pattern = PatternCache.get(regex);
                    if (pattern.matcher(value).matches()) {
                        newValue = childGenerators[i + 1].eval().toString();
                        break;
                    }
                }

                if (newValue == null) {
                    return VarNull.INSTANCE;
                }

                return VarString.create(newValue);

            } catch (final ParseException | RuntimeException e) {
                return VarErr.create(e.getMessage());
            }
        }
    }
}
