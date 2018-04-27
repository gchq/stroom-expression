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

public class ToInteger extends AbstractCast implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    private static final Cast CAST = new Cast();

    public static final String NAME = "toInteger";

    public ToInteger(final String name) {
        super(name);
    }

    @Override
    AbstractCaster getCaster() {
        return CAST;
    }

    private static class Cast extends AbstractCaster {
        @Override
        Var cast(final Var var) {
            if (!var.hasValue()) {
                return var;
            }

            final Integer value = var.toInteger();
            if (value != null) {
                return VarInteger.create(value);
            }
            return VarErr.INSTANCE;
        }
    }
}
