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
import net.lenni0451.sourcegen.steps.target.IterateCosmicReachVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;

import java.io.File;
import java.util.List;

public class CosmicReachTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.CosmicReach.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "cosmicreach");
    private final File rawJar = new File(Main.WORK_DIR, "raw.jar");
    private final File noGameJar = new File(Main.WORK_DIR, "no_game.jar");
    private final File onlyGameJar = new File(Main.WORK_DIR, "only_game.jar");

    public CosmicReachTarget() {
        super("CosmicReach (Client & Server)");
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.CLIENT, Config.CosmicReach.clientBranch);
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.SERVER, Config.CosmicReach.serverBranch);
    }

    private void addSteps(final List<GeneratorStep> steps, final IterateCosmicReachVersions.VersionType type, final String branch) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.CosmicReach.gitRepo, branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.CosmicReach.authorName, Config.CosmicReach.authorEmail));
        steps.add(new IterateCosmicReachVersions(type, this.repoDir, branch, (versionSteps, versionName, releaseTime, url) -> {
            versionSteps.add(new CleanRepoStep(this.repoDir));
            versionSteps.add(new DownloadStep(url, this.rawJar));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.noGameJar, entry -> entry.startsWith("finalforeach") || entry.startsWith("/finalforeach")));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.onlyGameJar, entry -> !entry.startsWith("finalforeach") && !entry.startsWith("/finalforeach")));
            versionSteps.add(new DecompileWithLibStep(this.onlyGameJar, this.noGameJar, this.repoDir));
            versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
            versionSteps.add(new CommitChangesStep(this.repoDir, versionName, releaseTime));
            versionSteps.add(new CleanupStep(this.rawJar, this.noGameJar, this.onlyGameJar));
        }));
        steps.add(new PushRepoStep(this.repoDir, branch));
    }

}
