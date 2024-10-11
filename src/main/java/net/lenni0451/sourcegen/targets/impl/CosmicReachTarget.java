package net.lenni0451.sourcegen.targets.impl;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.impl.decompile.DecompileWithLibStep;
import net.lenni0451.sourcegen.steps.impl.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.impl.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.impl.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.impl.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.impl.io.CleanRepoStep;
import net.lenni0451.sourcegen.steps.impl.io.CleanupStep;
import net.lenni0451.sourcegen.steps.impl.io.DownloadStep;
import net.lenni0451.sourcegen.steps.impl.io.ModifyJarStep;
import net.lenni0451.sourcegen.steps.impl.target.IterateCosmicReachVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;

import java.io.File;
import java.util.List;

public class CosmicReachTarget implements GeneratorTarget {

    private static final String REPO_URL = "https://github.com/Lenni0451/CosmicReachSources";
    private static final File REPO_DIR = new File("cosmicreach");
    private static final File DEFAULTS_DIR = new File("defaults", "cosmicreach");
    private static final File RAW_JAR = new File("raw.jar");
    private static final File NO_GAME_JAR = new File("no_game.jar");
    private static final File ONLY_GAME_JAR = new File("only_game.jar");

    @Override
    public String getName() {
        return "CosmicReach (Client & Server)";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.CLIENT, "client");
        this.addSteps(steps, IterateCosmicReachVersions.VersionType.SERVER, "server");
    }

    private void addSteps(final List<GeneratorStep> steps, final IterateCosmicReachVersions.VersionType type, final String branch) {
        steps.add(new PrepareRepoStep(REPO_DIR, REPO_URL, branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, "finalforeach", "finalforeach@github.io"));
        steps.add(new IterateCosmicReachVersions(type, REPO_DIR, branch, (versionSteps, versionName, releaseTime, url) -> {
            versionSteps.add(new CleanRepoStep(REPO_DIR, DEFAULTS_DIR));
            versionSteps.add(new DownloadStep(url, RAW_JAR));
            versionSteps.add(new ModifyJarStep(RAW_JAR, NO_GAME_JAR, entry -> entry.startsWith("finalforeach") || entry.startsWith("/finalforeach")));
            versionSteps.add(new ModifyJarStep(RAW_JAR, ONLY_GAME_JAR, entry -> !entry.startsWith("finalforeach") && !entry.startsWith("/finalforeach")));
            versionSteps.add(new DecompileWithLibStep(ONLY_GAME_JAR, NO_GAME_JAR, REPO_DIR));
            versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, releaseTime));
            versionSteps.add(new CleanupStep(RAW_JAR, NO_GAME_JAR, ONLY_GAME_JAR));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
