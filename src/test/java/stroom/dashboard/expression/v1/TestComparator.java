package stroom.dashboard.expression.v1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestComparator {
    @Test
    public void test() {
        for (int j = 0; j < 100000; j++) {
            final List<Val> list = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                final int selector = (int) (Math.random() * 7);
                Val val = null;

                switch (selector) {
                    case 0:
                        val = ValNull.INSTANCE;
                        break;
                    case 1:
                        val = ValErr.create("Error");
                        break;
                    case 2:
                        val = ValInteger.create(((int) (Math.random() * Integer.MAX_VALUE)) - (Integer.MAX_VALUE / 2));
                        break;
                    case 3:
                        val = ValDouble.create((Math.random() * Double.MAX_VALUE) - (Double.MAX_VALUE / 2));
                        break;
                    case 4:
                        val = ValLong.create(((long) (Math.random() * Long.MAX_VALUE)) - (Long.MAX_VALUE / 2));
                        break;
                    case 5:
                        val = ValBoolean.create(Math.random() > 0.5);
                        break;
                    case 6:
                        val = ValString.create(String.valueOf((Math.random() * Double.MAX_VALUE) - (Double.MAX_VALUE / 2)));
                        break;
                }

                list.add(val);
            }

            list.sort(new ValComparator());

        }


    }
}
