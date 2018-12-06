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

class Link extends AbstractManyChildFunction {
    static final String NAME = "link";

    private static final Val DEFAULT_TARGET = ValString.create("BROWSER_TAB");

    public Link(final String name) {
        super(name, 1, 4);
    }

    @Override
    protected Generator createGenerator(final Generator[] childGenerators) {
        return new Gen(childGenerators);
    }

    private static class Gen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 217968020285584214L;

        Gen(final Generator[] childGenerators) {
            super(childGenerators);
        }

        @Override
        public void set(final Val[] values) {
            for (final Generator generator : childGenerators) {
                generator.set(values);
            }
        }

        @Override
        public Val eval() {
            Val link = ValNull.INSTANCE;

            if (childGenerators.length == 1) {
                final Val url = childGenerators[0].eval();
                link = makeLink(url, url, ValString.EMPTY, DEFAULT_TARGET);
            } else if (childGenerators.length == 2) {
                final Val title = childGenerators[0].eval();
                final Val url = childGenerators[1].eval();
                link = makeLink(title, url, ValString.EMPTY, DEFAULT_TARGET);
            } else if (childGenerators.length == 3) {
                final Val title = childGenerators[0].eval();
                final Val host = childGenerators[1].eval();
                final Val path = childGenerators[2].eval();
                link = makeLink(title, host, path, DEFAULT_TARGET);
            } else if (childGenerators.length == 4) {
                final Val title = childGenerators[0].eval();
                final Val host = childGenerators[1].eval();
                final Val path = childGenerators[2].eval();
                final Val target = childGenerators[3].eval();
                link = makeLink(title, host, path, target);
            }

            return link;
        }

        private Val makeLink(final Val title, final Val host, final Val path, final Val target) {
            if (title.type().isError()) {
                return title;
            }
            if (host.type().isError()) {
                return host;
            }
            if (path.type().isError()) {
                return path;
            }
            if (target.type().isError()) {
                return target;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("[");
            append(sb, title);
            sb.append("](");
            append(sb, host);
            append(sb, path);
            sb.append("){");
            append(sb, target);
            sb.append("}");

            return ValString.create(sb.toString());
        }

        private void append(final StringBuilder sb, final Val val) {
            if (val.type().isValue()) {
                sb.append(val.toString());
            }
        }
    }
}
