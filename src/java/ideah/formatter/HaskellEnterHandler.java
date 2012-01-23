package ideah.formatter;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class HaskellEnterHandler implements EnterHandlerDelegate {

    public Result preprocessEnter(PsiFile file,
                                  Editor editor,
                                  Ref<Integer> caretOffset,
                                  Ref<Integer> caretAdvance,
                                  DataContext dataContext,
                                  EditorActionHandler originalHandler) {
        return Result.Default; // todo
    }

    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        return Result.Default; // todo
    }
}
