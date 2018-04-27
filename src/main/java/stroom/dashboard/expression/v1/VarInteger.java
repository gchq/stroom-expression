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

import java.util.Objects;

public class VarInteger implements VarNumber {
    private final int value;

    private VarInteger(final int value) {
        this.value = value;
    }

    public static VarInteger create(final int value) {
        final int offset = 128;
        if (value >= -128 && value <= 127) { // will cache
            return VarIntegerCache.cache[value + offset];
        }
        return new VarInteger(value);
    }

    @Override
    public Integer toInteger() {
        return value;
    }

    @Override
    public Long toLong() {
        return (long) value;
    }

    @Override
    public Double toDouble() {
        return (double) value;
    }

    @Override
    public Boolean toBoolean() {
        return value != 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public void appendString(final StringBuilder sb) {
        sb.append(toString());
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VarInteger that = (VarInteger) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private static class VarIntegerCache {
        private VarIntegerCache() {
        }

        static final VarInteger cache[] = new VarInteger[-(-128) + 127 + 1];

        static {
            for (int i = 0; i < cache.length; i++)
                cache[i] = new VarInteger(i - 128);
        }
    }
}
