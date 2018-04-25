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

import java.io.Serializable;

public abstract class Calculator implements Serializable {
    private static final long serialVersionUID = 7429374303179048909L;

    public Var calc(final Var current, final Var value) {
        final Double cur = current.toDouble();
        final Double val = value.toDouble();
        if (val == null) {
            return current;
        }
        if (cur == null) {
            return value;
        }
        return new VarDouble(op(cur, val));
    }

    protected abstract double op(final double cur, final double val);
}
