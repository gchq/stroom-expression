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

public class Replace extends AbstractFunction implements Serializable {
    public static final String NAME = "replace";
    private static final long serialVersionUID = -305845496003936297L;
    private String replacement;
    private SerializablePattern pattern;
    private Generator gen;
    private Function function = null;
    private boolean hasAggregate;

    public Replace(final String name) {
        super(name, 3, 3);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        if (!(params[1] instanceof VarString)) {
            throw new ParseException("String expected as second argument of '" + name + "' function", 0);
        }
        final String regex = params[1].toString();
        if (regex.length() == 0) {
            throw new ParseException("An empty regex has been defined for second argument of '" + name + "' function", 0);
        }
        if (!(params[2] instanceof VarString)) {
            throw new ParseException("String expected as third argument of '" + name + "' function", 0);
        }
        replacement = params[2].toString();

        // Try and create pattern with the supplied regex.
        pattern = new SerializablePattern(regex);
        pattern.getOrCreatePattern();

        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
            hasAggregate = function.hasAggregate();
        } else {
            // Optimise replacement of static input in case user does something
            // stupid.
            final String newValue = pattern.matcher(param.toString()).replaceAll(replacement);
            gen = new StaticValueFunction(new VarString(newValue)).createGenerator();
            hasAggregate = false;
        }
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }

        final Generator childGenerator = function.createGenerator();
        return new Gen(childGenerator, pattern, replacement);
    }

    @Override
    public boolean hasAggregate() {
        return hasAggregate;
    }

    private static class Gen extends AbstractSingleChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        private final SerializablePattern pattern;
        private final String replacement;

        public Gen(final Generator childGenerator, final SerializablePattern pattern, final String replacement) {
            super(childGenerator);
            this.pattern = pattern;
            this.replacement = replacement;
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

            return new VarString(pattern.matcher(val.toString()).replaceAll(replacement));
        }
    }
}
