package stroom.dashboard.expression.v1;

import java.util.Comparator;

class ValComparator implements Comparator<Val> {
    @Override
    public int compare(final Val o1, final Val o2) {
        if (o1 != null && o2 != null) {
            if (o1.getClass() == o2.getClass()) {
                return o1.compareTo(o2);
            }

            final Double d1 = o1.toDouble();
            if (d1 != null) {
                final Double d2 = o2.toDouble();
                if (d2 != null) {
                    return Double.compare(d1, d2);
                }
            }

            final String str1 = o1.toString();
            final String str2 = o2.toString();
            if (str1 != null && str2 != null) {
                return str1.compareToIgnoreCase(str2);
            }
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return 0;
    }
}
