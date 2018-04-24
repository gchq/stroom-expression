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

public abstract class AbstractNoChildGenerator extends AbstractGenerator {
    private static final long serialVersionUID = 513621715143449935L;

    public AbstractNoChildGenerator() {
    }

    @Override
    public void addChildKey(final Var key) {
    }

    @Override
    public void set(final Var[] values) {
    }

    @Override
    public abstract Var eval();

    @Override
    public void merge(final Generator generator) {
    }
}
