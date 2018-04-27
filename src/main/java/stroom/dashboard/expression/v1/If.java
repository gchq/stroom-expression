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

public class If extends AbstractManyChildFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "if";

    private Generator gen;
    private boolean simple;

    public If(final String name) {
        super(name, 3, 3);
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

        if (params[0] instanceof Var) {
            final Boolean condition = ((Var) params[0]).toBoolean();
            if (condition == null) {
                throw new ParseException("Expecting a condition for first argument of '" + name + "' function", 0);
            }
        }

        if (simple) {
            // Static computation.
            final Boolean condition = ((Var) params[0]).toBoolean();
            if (condition) {
                gen = new StaticValueFunction((Var) params[1]).createGenerator();
            } else {
                gen = new StaticValueFunction((Var) params[2]).createGenerator();
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
                final Boolean condition = val.toBoolean();
                if (condition == null) {
                    return VarErr.create("Expecting a condition");
                }
                if (condition) {
                    return childGenerators[1].eval();
                } else {
                    return childGenerators[2].eval();
                }
            } catch (final RuntimeException e) {
                return VarErr.create(e.getMessage());
            }
        }
    }
}
