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

import java.math.BigDecimal;
import java.util.Objects;

public class VarString implements Var {
    public static final VarString EMPTY = new VarString("");

    private final String value;

    private VarString(final String value) {
        this.value = value;
    }

    public static VarString create(final String value) {
        if ("".equals(value)) {
            return EMPTY;
        }

        return new VarString(value);
    }

    @Override
    public Integer toInteger() {
        try {
            return Integer.valueOf(value);
        } catch (final RuntimeException e) {
            // Ignore.
        }
        return null;
    }

    @Override
    public Long toLong() {
        try {
            return Long.valueOf(value);
        } catch (final RuntimeException e) {
            // Ignore.
        }
        try {
            return DateUtil.parseNormalDateTimeString(value);
        } catch (final RuntimeException e) {
            // Ignore.
        }
        return null;
    }

    @Override
    public Double toDouble() {
        try {
            return new BigDecimal(value).doubleValue();
        } catch (final RuntimeException e) {
            // Ignore.
        }
        try {
            return (double) DateUtil.parseNormalDateTimeString(value);
        } catch (final RuntimeException e) {
            // Ignore.
        }
        return null;
    }

    @Override
    public Boolean toBoolean() {
        try {
            return Boolean.valueOf(value);
        } catch (final RuntimeException e) {
            // Ignore.
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public void appendString(final StringBuilder sb) {
        sb.append(StringUtil.escape(value));
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VarString varString = (VarString) o;
        return Objects.equals(value, varString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
