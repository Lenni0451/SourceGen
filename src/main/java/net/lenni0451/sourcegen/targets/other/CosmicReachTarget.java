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
import net.lenni0451.sourcegen.targets.Requirements;

import java.io.File;
import java.util.List;

public class CosmicReachTarget extends GeneratorTarget {

    private final File rawJar = new File(Main.WORK_DIR, "raw.jar");
    private final File noGameJar = new File(Main.WORK_DIR, "no_game.jar");
    private final File onlyGameJar = new File(Main.WORK_DIR, "only_game.jar");

    public CosmicReachTarget() {
        super("CosmicReach (Client & Server)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        this.addSteps(
                new File(Config.CosmicReach.clientRepoName),
                new File(Main.DEFAULTS_DIR, "cosmicreach_client"),
                Config.CosmicReach.clientGitRepo,
                steps,
                IterateCosmicReachVersions.VersionType.CLIENT,
                Config.CosmicReach.clientBranch
        );
        this.addSteps(
                new File(Config.CosmicReach.serverRepoName),
                new File(Main.DEFAULTS_DIR, "cosmicreach_server"),
                Config.CosmicReach.serverGitRepo,
                steps,
                IterateCosmicReachVersions.VersionType.SERVER,
                Config.CosmicReach.serverBranch
        );
    }

    private void addSteps(final File repoDir, final File defaultsDir, final String gitRepo, final List<GeneratorStep> steps, final IterateCosmicReachVersions.VersionType type, final String branch) {
        steps.add(new PrepareRepoStep(repoDir, gitRepo, branch));
        steps.add(new ChangeGitUserStep(repoDir, Config.CosmicReach.authorName, Config.CosmicReach.authorEmail));
        steps.add(new IterateCosmicReachVersions(type, repoDir, branch, (versionSteps, versionName, releaseTime, url) -> {
            versionSteps.add(new CleanRepoStep(repoDir));
            versionSteps.add(new DownloadStep(url, this.rawJar));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.noGameJar, entry -> entry.startsWith("finalforeach") || entry.startsWith("/finalforeach")));
            versionSteps.add(new ModifyJarStep(this.rawJar, this.onlyGameJar, entry -> !entry.startsWith("finalforeach") && !entry.startsWith("/finalforeach")));
            versionSteps.add(new DecompileWithLibStep(this.onlyGameJar, this.noGameJar, repoDir));
            versionSteps.add(new CopyDefaultsStep(repoDir, defaultsDir));
            versionSteps.add(new CommitChangesStep(repoDir, versionName, releaseTime));
            versionSteps.add(new CleanupStep(this.rawJar, this.noGameJar, this.onlyGameJar));
        }));
        steps.add(new PushRepoStep(repoDir, branch));
    }

}
