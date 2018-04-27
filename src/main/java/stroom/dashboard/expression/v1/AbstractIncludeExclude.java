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

public abstract class AbstractIncludeExclude extends AbstractManyChildFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    private Generator gen;
    private boolean simple;

    public AbstractIncludeExclude(final String name) {
        super(name, 2, Integer.MAX_VALUE);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

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

            boolean found = false;
            for (int i = 1; i < params.length && !found; i++) {
                final String regex = params[i].toString();
                if (regex.length() == 0) {
                    throw new ParseException("An empty regex has been defined for argument of '" + name + "' function", 0);
                }

                final Pattern pattern = PatternCache.get(regex);
                if (pattern.matcher(value).matches()) {
                    found = true;
                }
            }

            if (inverse()) {
                found = !found;
            }

            if (found) {
                gen = new StaticValueFunction(VarString.create(value)).createGenerator();
            } else {
                gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();
            }

        } else {
            for (int i = 1; i < params.length; i++) {
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
    public boolean hasAggregate() {
        if (simple) {
            return false;
        }
        return super.hasAggregate();
    }

    abstract boolean inverse();

    abstract static class AbstractGen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        public AbstractGen(final Generator[] childGenerators) {
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

                boolean found = false;
                for (int i = 1; i < childGenerators.length && !found; i++) {
                    final String regex = childGenerators[i].eval().toString();
                    if (regex != null && regex.length() > 0) {
                        final Pattern pattern = PatternCache.get(regex);
                        if (pattern.matcher(value).matches()) {
                            found = true;
                        }
                    }
                }

                if (inverse()) {
                    found = !found;
                }

                if (found) {
                    return VarString.create(value);
                } else {
                    return VarNull.INSTANCE;
                }

            } catch (final RuntimeException e) {
                return VarErr.create(e.getMessage());
            }
        }

        abstract boolean inverse();
    }
}
