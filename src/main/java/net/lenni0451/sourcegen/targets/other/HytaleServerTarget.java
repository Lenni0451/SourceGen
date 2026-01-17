package net.lenni0451.sourcegen.targets.other;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.decompile.DecompileWithLibStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateHytaleServerVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class HytaleServerTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.HytaleServer.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "hytale_server");
    private final File serverArchive = new File(Main.WORK_DIR, "server_archive.zip");
    private final File rawJar = new File(Main.WORK_DIR, "raw.jar");
    private final File noGameJar = new File(Main.WORK_DIR, "no_game.jar");
    private final File onlyGameJar = new File(Main.WORK_DIR, "only_game.jar");

    public HytaleServerTarget() {
        super("Hytale Server");
    }

    @Override
    protected void addSteps(final List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.HytaleServer.gitRepo, Config.HytaleServer.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.HytaleServer.authorName, Config.HytaleServer.authorEmail));
        steps.add(new IterateHytaleServerVersions(this.repoDir, Config.HytaleServer.branch, (versionSteps, versionName) -> {
            versionSteps.add(new CleanRepoStep(this.repoDir));
            versionSteps.add(new DownloadLatestHytaleServerStep(this.serverArchive));
            versionSteps.add(new UnzipSingleFileStep(this.serverArchive, "Server/HytaleServer.jar", this.rawJar));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.noGameJar, entry -> keep(entry)));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.onlyGameJar, entry -> !keep(entry)));
            versionSteps.add(new DecompileWithLibStep(this.onlyGameJar, this.noGameJar, this.repoDir));
            versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
            versionSteps.add(new CommitChangesStep(this.repoDir, versionName, Calendar.getInstance().getTime()));
            versionSteps.add(new CleanupStep(this.serverArchive, this.rawJar, this.noGameJar, this.onlyGameJar));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.HytaleServer.branch));
    }

    private static boolean keep(final String entry) {
        if (entry.startsWith("com/hypixel")) return true;
        if (entry.startsWith("/com/hypixel")) return true;
        if (entry.startsWith("migration")) return true;
        if (entry.startsWith("/migration")) return true;
        if (entry.equals("manifests.json")) return true;
        return false;
    }

}
