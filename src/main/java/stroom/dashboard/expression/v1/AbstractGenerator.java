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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

abstract class AbstractGenerator implements Generator, Serializable, Comparable<Generator> {
    private static final long serialVersionUID = 513621715143449935L;

    private static final ValComparator COMPARATOR = new ValComparator();

    @Override
    public final int compareTo(final Generator gen) {
        final Val o1 = eval();
        final Val o2 = gen.eval();
        return COMPARATOR.compare(o1, o2);
    }

    @Override
    public void read(final Kryo kryo, final Input input) {
    }

    @Override
    public void write(final Kryo kryo, final Output output) {
    }
}
