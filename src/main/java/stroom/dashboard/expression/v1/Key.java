package stroom.dashboard.expression.v1;

import java.io.Serializable;
import java.util.List;

public interface Key extends Serializable {
    int getDepth();

    Key getParent();

    List<Val> getValues();
}
