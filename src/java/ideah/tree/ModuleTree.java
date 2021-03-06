package ideah.tree;

import com.google.common.collect.Iterables;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import ideah.tree.decl.Declaration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ModuleTree extends Located {

    public final Ident name;
    public final List<Export> exports;
    public final List<Import> imports;
    public final List<Declaration> declarations;

    public ModuleTree(IRange location, Ident name, List<Export> exports, List<Import> imports, List<Declaration> declarations) {
        super(location);
        this.name = name;
        this.exports = exports;
        this.imports = imports;
        this.declarations = declarations;
    }

    protected Iterable<Located> getChildren() {
        return Iterables.concat(
            name == null ? Collections.<Ident>emptyList() : Arrays.asList(name),
            exports, imports, declarations
        );
    }

    @Override
    public Spacing getSpacing(Block child1, Block child2) {
        // todo: handle comments/imports/declarations/exports/name
        return Spacing.createSpacing(0, 0, 1, false, 2); // todo: ???
    }
}
