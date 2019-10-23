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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

abstract class AbstractManyChildGenerator extends AbstractGenerator {
    private static final long serialVersionUID = 513621715143449935L;

    final Generator[] childGenerators;

    AbstractManyChildGenerator(final Generator[] childGenerators) {
        this.childGenerators = childGenerators;
    }

    @Override
    public void addChildKey(final Key key) {
        if (childGenerators != null) {
            for (final Generator gen : childGenerators) {
                gen.addChildKey(key);
            }
        }
    }

    @Override
    public abstract void set(Val[] values);

    @Override
    public abstract Val eval();

    @Override
    public void merge(final Generator generator) {
        addChildren((AbstractManyChildGenerator) generator);
    }

    private void addChildren(final AbstractManyChildGenerator generator) {
        for (int i = 0; i < childGenerators.length; i++) {
            childGenerators[i].merge(generator.childGenerators[i]);
        }
    }

    @Override
    public void read(final Kryo kryo, final Input input) {
        if (childGenerators != null) {
            for (final Generator gen : childGenerators) {
                gen.read(kryo, input);
            }
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output) {
        if (childGenerators != null) {
            for (final Generator gen : childGenerators) {
                gen.write(kryo, output);
            }
        }
    }
}
