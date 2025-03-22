package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateBedrockVersions;
import net.lenni0451.sourcegen.steps.transform.JsonBeautifyStep;
import net.lenni0451.sourcegen.targets.GeneratorTarget;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MinecraftBedrockAssetsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftBedrockAssets.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_bedrock_assets");
    private final File bedrockAppx = new File(Main.WORK_DIR, "bedrock.appx");

    public MinecraftBedrockAssetsTarget() {
        super("Minecraft Bedrock Assets");
    }

    @Override
    protected void addSteps(final List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftBedrockAssets.gitRepo, Config.MinecraftBedrockAssets.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftBedrockAssets.authorName, Config.MinecraftBedrockAssets.authorEmail));
        steps.add(new IterateBedrockVersions(this.repoDir, Config.MinecraftBedrockAssets.branch, (versionSteps, versionId, versionName) -> {
            versionSteps.add(new CleanRepoStep(this.repoDir));
            versionSteps.add(new DownloadMicrosoftStep(versionId, this.bedrockAppx));
            versionSteps.add(new UnzipSingleFolderStep(this.bedrockAppx, "data", this.repoDir));
            versionSteps.add(new JsonBeautifyStep(this.repoDir));
            versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
            versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date()));
            versionSteps.add(new CleanupStep(this.bedrockAppx));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftBedrockAssets.branch));
    }

}
