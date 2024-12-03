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

    private static final File REPO_DIR = new File(Config.CosmicReach.repoName);
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "cosmicreach");
    private static final File RAW_JAR = new File(Main.WORK_DIR, "raw.jar");
    private static final File NO_GAME_JAR = new File(Main.WORK_DIR, "no_game.jar");
    private static final File ONLY_GAME_JAR = new File(Main.WORK_DIR, "only_game.jar");

    public CosmicReachTarget() {
        super("CosmicReach (Client & Server)");
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.CLIENT, Config.CosmicReach.clientBranch);
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.SERVER, Config.CosmicReach.serverBranch);
    }

    private void addSteps(final List<GeneratorStep> steps, final IterateCosmicReachVersions.VersionType type, final String branch) {
        steps.add(new PrepareRepoStep(REPO_DIR, Config.CosmicReach.gitRepo, branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, Config.CosmicReach.authorName, Config.CosmicReach.authorEmail));
        steps.add(new IterateCosmicReachVersions(type, REPO_DIR, branch, (versionSteps, versionName, releaseTime, url) -> {
            versionSteps.add(new CleanRepoStep(REPO_DIR));
            versionSteps.add(new DownloadStep(url, RAW_JAR));
            versionSteps.add(new ModifyJarStep(RAW_JAR, NO_GAME_JAR, entry -> entry.startsWith("finalforeach") || entry.startsWith("/finalforeach")));
            versionSteps.add(new ModifyJarStep(RAW_JAR, ONLY_GAME_JAR, entry -> !entry.startsWith("finalforeach") && !entry.startsWith("/finalforeach")));
            versionSteps.add(new DecompileWithLibStep(ONLY_GAME_JAR, NO_GAME_JAR, REPO_DIR));
            versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
            versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, releaseTime));
            versionSteps.add(new CleanupStep(RAW_JAR, NO_GAME_JAR, ONLY_GAME_JAR));
        }));
        steps.add(new PushRepoStep(REPO_DIR, branch));
    }

}
