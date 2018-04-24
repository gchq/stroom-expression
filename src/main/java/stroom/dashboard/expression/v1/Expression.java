/*
 * Copyright 2016 Crown Copyright
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

public class Expression implements Function {
    private Function function = null;
    private boolean hasAggregate;

    @Override
    public void setParams(final Param[] params) {
        final Param param = params[0];
        if (param instanceof Function) {
            function = (Function) param;
        } else {
            function = new StaticValueFunction((Var) param);
        }

        this.hasAggregate = function.hasAggregate();
    }

    @Override
    public Generator createGenerator() {
        return function.createGenerator();
    }

    @Override
    public String toString() {
        if (function == null) {
            return "";
        }

        return function.toString();
    }

    @Override
    public boolean isAggregate() {
        return false;
    }

    @Override
    public boolean hasAggregate() {
        return hasAggregate;
    }
}
