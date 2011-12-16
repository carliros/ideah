package ideah.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;
import ideah.sdk.HaskellSdkAdditionalData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class to build ask_* executables
 */
final class AskUtil {

    private static final Logger LOG = Logger.getInstance("ideah.util.AskUtil");

    private static Long sourcesLastModified = null;

    @NotNull
    private final Module module;
    @NotNull
    private final Sdk sdk;
    @NotNull
    private final VirtualFile ghcHome;
    @NotNull
    private final File pluginPath;
    @NotNull
    private final File exe;
    @NotNull
    private final String mainFile;

    private AskUtil(@NotNull Module module, @NotNull Sdk sdk, @NotNull VirtualFile ghcHome, @NotNull File pluginPath, @NotNull File exe, @NotNull String mainFile) {
        this.module = module;
        this.sdk = sdk;
        this.ghcHome = ghcHome;
        this.pluginPath = pluginPath;
        this.exe = exe;
        this.mainFile = mainFile;
    }

    @Nullable
    static AskUtil get(@Nullable Module module, String mainFile) {
        if (module == null)
            return null;
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk == null)
            return null;
        VirtualFile ghcHome = sdk.getHomeDirectory();
        if (ghcHome == null)
            return null;
        File pluginPath = new File(new File(System.getProperty("user.home"), ".ideah"), sdk.getVersionString());
        pluginPath.mkdirs();
        File exe = new File(pluginPath, GHCUtil.getExeName(mainFile));
        return new AskUtil(module, sdk, ghcHome, pluginPath, exe, mainFile);
    }

    String getLibDir() {
        SdkAdditionalData sdkAdditionalData = sdk.getSdkAdditionalData();
        if (!(sdkAdditionalData instanceof HaskellSdkAdditionalData))
            return null;
        HaskellSdkAdditionalData data = (HaskellSdkAdditionalData) sdkAdditionalData;
        return data.getLibPath();
    }

    String getCabalPath() {
        SdkAdditionalData sdkAdditionalData = sdk.getSdkAdditionalData();
        if (!(sdkAdditionalData instanceof HaskellSdkAdditionalData))
            return null;
        HaskellSdkAdditionalData data = (HaskellSdkAdditionalData) sdkAdditionalData;
        return data.getCabalPath();
    }

    File getExe() {
        if (exe.exists()) {
            return exe;
        } else {
            return null;
        }
    }

    private interface HsCallback {

        void run(ZipInputStream zis, ZipEntry entry) throws IOException;
    }

    private static void listHaskellSources(HsCallback callback) throws IOException {
        InputStream is = CompilerLocation.class.getResourceAsStream("/ask_ghc.jar");
        ZipInputStream zis = new ZipInputStream(is);
        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null)
                break;
            callback.run(zis, entry);
            zis.closeEntry();
        }
        zis.close();
    }

    boolean needRecompile() throws IOException {
        if (exe.exists()) {
            if (sourcesLastModified == null) {
                final Long[] maxModified = new Long[1];
                listHaskellSources(new HsCallback() {
                    public void run(ZipInputStream zis, ZipEntry entry) {
                        long lastModified = entry.getTime();
                        if (maxModified[0] == null) {
                            maxModified[0] = lastModified;
                        } else {
                            maxModified[0] = Math.max(maxModified[0].longValue(), lastModified);
                        }
                    }
                });
                sourcesLastModified = maxModified[0];
            }
            return sourcesLastModified != null && sourcesLastModified.longValue() > exe.lastModified();
        } else {
            return true;
        }
    }

    boolean compileHs() throws IOException, InterruptedException {
        return compileHs(null, 0);
    }

    boolean compileHs(@Nullable ProgressIndicator indicator, double maxIndicatorFraction) throws IOException, InterruptedException {
        exe.delete();
        String ghcExe = GHCUtil.getGhcCommandPath(ghcHome);
        if (ghcExe == null)
            return false;
        double step = 0;
        if (indicator != null) {
            step = (maxIndicatorFraction - indicator.getFraction()) / 3;
        }
        GHCUtil.updateIndicator(indicator, step, "Collecting source files...");
        listHaskellSources(new AskUtil.HsCallback() {
            public void run(ZipInputStream zis, ZipEntry entry) throws IOException {
                File outFile = new File(pluginPath, entry.getName());
                OutputStream os = new FileOutputStream(outFile);
                try {
                    StreamUtil.copyStreamContent(zis, os);
                } finally {
                    os.close();
                }
            }
        });
        GHCUtil.updateIndicator(indicator, step, "Compiling " + mainFile + "...");
        String mainHs = mainFile + ".hs";
        ProcessLauncher launcher = new ProcessLauncher(true, null, ghcExe,
            "--make", "-cpp", "-O", "-package", "ghc",
            "-i" + pluginPath.getAbsolutePath(),
            new File(pluginPath, mainHs).getAbsolutePath()
        );
        GHCUtil.updateIndicator(indicator, step, "Finishing compilation...");
        for (int i = 0; i < 3; i++) {
            if (exe.exists())
                return true;
            Thread.sleep(100);
        }
        String stdErr = launcher.getStdErr();
        LOG.error("Compiling " + mainHs + ":\n" + stdErr);
        return false;
    }
}
