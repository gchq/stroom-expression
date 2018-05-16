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

import java.util.Optional;

class Equals extends AbstractManyChildFunction {
    static final String NAME = "=";
    static final String ALIAS = "equals";
    private final boolean usingOperator;

    public Equals(final String name) {
        super(name, 2, 2);
        usingOperator = name.length() == 1;

    }

    @Override
    protected Generator createGenerator(final Generator[] childGenerators) {
        return new Gen(childGenerators);
    }

    @Override
    public void appendString(final StringBuilder sb) {
        if (usingOperator) {
            appendParams(sb);
        } else {
            super.appendString(sb);
        }
    }

    @Override
    protected void appendParams(final StringBuilder sb) {
        if (usingOperator) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    final Param param = params[i];
                    appendParam(sb, param);
                    if (i < params.length - 1) {
                        sb.append(name);
                    }
                }
            }
        } else {
            super.appendParams(sb);
        }
    }

    private static class Gen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 217968020285584214L;

        private static final Evaluator EVALUATOR = Evaluator.builder(NAME)
                .addReturnErrorOnFirstErrorValue()
                .addEvaluationFunction(Gen::doEval)
                .build();

        Gen(final Generator[] childGenerators) {
            super(childGenerators);
        }

        @Override
        public void set(final Val[] values) {
            for (final Generator generator : childGenerators) {
                generator.set(values);
            }
        }

        @Override
        public Val eval() {
            return EVALUATOR.evaluate(childGenerators);
        }

        private static Optional<Val> doEval(Val... values) {
            Val a = values[0];
            Val b = values[1];
            if (a instanceof ValNull && b instanceof ValNull) {
                // both null so equality is true
                return Optional.of(ValBoolean.TRUE);
            } else if (!a.hasValue() || !b.hasValue()) {
                return Optional.of(ValErr.create(String.format("Both values must have a value, %s %s", a, b)));
            } else if (a.getClass().equals(b.getClass())) {
                if (a instanceof ValInteger) {
                    return Optional.of(ValBoolean.create(a.toInteger().equals(b.toInteger())));
                }
                if (a instanceof ValLong) {
                    return Optional.of(ValBoolean.create(a.toLong().equals(b.toLong())));
                }
                if (a instanceof ValBoolean) {
                    return Optional.of(ValBoolean.create(a.toBoolean().equals(b.toBoolean())));
                }
            }

            return Optional.of(ValBoolean.create(a.toString().equals(b.toString())));
        }
    }
}