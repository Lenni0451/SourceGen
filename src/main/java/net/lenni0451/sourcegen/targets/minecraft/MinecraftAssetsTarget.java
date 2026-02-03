package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.transform.JsonBeautifyStep;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MinecraftAssetsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftAssets.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_assets");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");

    public MinecraftAssetsTarget() {
        super("Minecraft Assets", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftAssets.gitRepo, Config.MinecraftAssets.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftAssets.authorName, Config.MinecraftAssets.authorEmail));
        steps.add(new IterateMinecraftVersions(
                this.repoDir,
                Config.MinecraftAssets.branch,
                new IterateMinecraftVersions.VersionRange(null, null),
                v -> v.getString("type").equalsIgnoreCase("snapshot"),
                true,
                (versionSteps, versionName, releaseTime, clientUrl, serverUrl, clientMappingsUrl, serverMappingsUrl) -> {
                    versionSteps.add(new CleanRepoStep(this.repoDir));
                    versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                    versionSteps.add(new UnzipStep(this.clientJar, this.repoDir));
                    versionSteps.add(new RemoveResourcesStep(this.repoDir, f -> f.getName().toLowerCase(Locale.ROOT).endsWith(".class")));
                    versionSteps.add(new JsonBeautifyStep(this.repoDir));
                    versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                    versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                    versionSteps.add(new CleanupStep(this.clientJar));
                }
        ));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftAssets.branch));
    }

    @Nullable
    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftAssets.branch);
    }

}
