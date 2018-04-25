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

public class Not extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "not";

    private Generator gen;
    private Function function;
    private boolean hasAggregate;

    public Not(final String name) {
        super(name, 1, 1);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
            hasAggregate = function.hasAggregate();
        } else {
            final Boolean condition = ((Var) params[0]).asBoolean();
            if (condition == null) {
                throw new ParseException("Expecting a condition for first argument of '" + name + "' function", 0);
            }
            // Static computation.
            gen = new StaticValueFunction(VarBoolean.create(!condition)).createGenerator();
        }
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }

        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator);
    }

    @Override
    public boolean hasAggregate() {
        return hasAggregate;
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;


        public Gen(final Generator childGenerator) {
            super(childGenerator);
        }

        @Override
        public void set(final Var[] values) {
            childGenerator.set(values);
        }

        @Override
        public Var eval() {
            final Var val = childGenerator.eval();
            if (!val.hasValue()) {
                return val;
            }

            try {
                final Boolean condition = val.asBoolean();
                if (condition == null) {
                    return new VarErr("Expecting a condition");
                }
                return VarBoolean.create(!condition);
            } catch (final RuntimeException e) {
                return new VarErr(e.getMessage());
            }
        }
    }
}
