package ideah.tree;

import ideah.util.LineColRange;

import java.util.Collections;

public final class Import extends Located {

    // todo

    public Import(LineColRange location) {
        super(location);
    }

    protected Iterable<? extends Located> getChildren() {
        return Collections.emptyList(); // todo
    }
}
