/*
 * Copyright 2018 Crown Copyright
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

public class IndexOf extends AbstractFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "indexOf";

    private Function stringFunction;

    private Generator gen;
    private Function function;
    private boolean hasAggregate;

    public IndexOf(final String name) {
        super(name, 2, 2);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        stringFunction = parseParam(params[1], "second");

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
            hasAggregate = function.hasAggregate();

        } else {
            function = new StaticValueFunction((Var) param);
            hasAggregate = false;

            // Optimise replacement of static input in case user does something stupid.
            if (stringFunction instanceof StaticValueFunction) {
                final String string = stringFunction.createGenerator().eval().toString();
                if (string != null) {
                    final String value = param.toString();
                    final int index = value.indexOf(string);
                    if (index < 0) {
                        gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();
                    } else {
                        gen = new StaticValueFunction(VarInteger.create(index)).createGenerator();
                    }
                } else {
                    gen = new StaticValueFunction(VarNull.INSTANCE).createGenerator();
                }
            }
        }
    }

    private Function parseParam(final Param param, final String paramPos) throws ParseException {
        Function function;
        if (param instanceof Function) {
            function = (Function) param;
            if (function.hasAggregate()) {
                throw new ParseException("Non aggregate function expected as " + paramPos + " argument of '" + name + "' function", 0);
            }
        } else if (!(param instanceof VarString)) {
            throw new ParseException("String or function expected as " + paramPos + " argument of '" + name + "' function", 0);
        } else {
            function = new StaticValueFunction((Var) param);
        }
        return function;
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }

        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator, stringFunction.createGenerator());
    }

    @Override
    public boolean hasAggregate() {
        return hasAggregate;
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private Generator stringGenerator;

        public Gen(final Generator childGenerator, final Generator stringGenerator) {
            super(childGenerator);
            this.stringGenerator = stringGenerator;
        }

        @Override
        public void set(final Var[] values) {
            childGenerator.set(values);
            stringGenerator.set(values);
        }

        @Override
        public Var eval() {
            final Var val = childGenerator.eval();
            final String value = val.toString();

            if (value != null) {
                final String string = stringGenerator.eval().toString();
                if (string != null) {
                    final int index = value.indexOf(string);
                    if (index >= 0) {
                        return VarInteger.create(index);
                    }
                }
            }

            return VarNull.INSTANCE;
        }
    }
}
