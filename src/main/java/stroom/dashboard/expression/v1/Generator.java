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

import java.io.Serializable;

public interface Generator extends Serializable, Comparable<Generator> {
    /**
     * For countGroups() we need to know what child keys are used.
     *
     * @param key The key of a child group.
     */
    void addChildKey(Key key);

    /**
     * Set values that can be used to source whatever data is required by value
     * references using the Ref - ${} construct.
     *
     * @param values The current data values to pick data from.
     */
    void set(Val[] values);

    /**
     * Evaluate this generator by applying the function that this generator
     * performs to the values supplied by set().
     *
     * @return The result of applying this function to the supplied values.
     */
    Val eval();

    /**
     * Merge the values from another generator into this generator, e.g. for a
     * min generator take the min value from the supplied min generator and
     * compute a new min for this generator.
     *
     * @param generator The generator to merge with this one.
     */
    void merge(Generator generator);

    void read(Kryo kryo, Input input);

    void write(Kryo kryo, Output output);
}
