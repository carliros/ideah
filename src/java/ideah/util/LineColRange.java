package ideah.util;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

public final class LineColRange {

    public final LineCol start;
    public final LineCol end;

    private LineColRange(LineCol start, LineCol end) {
        this.start = start;
        this.end = end;
    }

    private LineColRange(int startLine, int endLine, int startColumn, int endColumn) {
        this.start = new LineCol(startLine, startColumn);
        this.end = new LineCol(endLine, endColumn);
    }

    public static LineColRange getFake() {
        return new LineColRange(1, 1, 1, 1);
    }

    public LineColRange(String str) {
        int p = str.indexOf('-');
        String start = str.substring(0, p);
        String end = str.substring(p + 1);
        int[] starts = parsePair(start);
        int[] ends = parsePair(end);
        this.start = new LineCol(starts[0], starts[1]);
        this.end = new LineCol(ends[0], ends[1]);
    }

    private static int[] parsePair(String str) {
        str = str.trim();
        if ("?".equals(str))
            return new int[] {0, 0};
        int p = str.indexOf(':');
        String from = str.substring(0, p);
        String to = str.substring(p + 1);
        return new int[] {parseInt(from), parseInt(to)};
    }

    private static int parseInt(String str) {
        str = str.trim();
        if ("?".equals(str))
            return 0;
        return Integer.parseInt(str);
    }

    public TextRange getRange(PsiFile file) {
        return new TextRange(start.getOffset(file), end.getOffset(file));
    }

    public static LineColRange fromTextRange(PsiFile file, TextRange range) {
        LineCol start = LineCol.fromOffset(file, range.getStartOffset());
        LineCol end = LineCol.fromOffset(file, range.getEndOffset());
        return new LineColRange(start, end);
    }

    public String toString() {
        return start + " - " + end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineColRange) {
            LineColRange that = (LineColRange) obj;
            return this.start.equals(that.start) && this.end.equals(that.end);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * start.hashCode() + end.hashCode();
    }
}
