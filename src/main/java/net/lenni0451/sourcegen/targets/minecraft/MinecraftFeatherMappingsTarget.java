package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.decompile.RemapStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.target.LoadFeatherMappings;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftFeatherMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftFeatherMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_feather_mappings");
    private final File mappingsJar = new File(Main.WORK_DIR, "mappings.jar");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings.tiny");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftFeatherMappingsTarget() {
        super("Minecraft (Feather Mappings)");
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftFeatherMappings.gitRepo, Config.MinecraftFeatherMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftFeatherMappings.authorName, Config.MinecraftFeatherMappings.authorEmail));
        steps.add(new LoadFeatherMappings((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(
                    this.repoDir,
                    Config.MinecraftFeatherMappings.branch,
                    new IterateMinecraftVersions.VersionRange(null, null),
                    version -> versionToUrl.apply(version) == null,
                    true,
                    (versionSteps, versionName, releaseTime, manifest) -> {
                        JSONObject downloads = manifest.getJSONObject("downloads");
                        String clientUrl = downloads.getJSONObject("client").getString("url");
                        Map<String, byte[]> jarEntries = new HashMap<>();

                        versionSteps.add(new CleanRepoStep(this.repoDir));
                        versionSteps.add(new DownloadStep(versionToUrl.apply(versionName), this.mappingsJar));
                        versionSteps.add(new UnzipSingleFileStep(this.mappingsJar, "mappings/mappings.tiny", this.mappingsFile));
                        versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                        versionSteps.add(new RemapStep(new TinyV2Remapper(jarEntries, this.mappingsFile)));
                        versionSteps.add(new FixLocalVariablesStep(jarEntries));
                        versionSteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                        versionSteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                        versionSteps.add(new RemoveResourcesStep(this.repoDir));
                        versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                        versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                        versionSteps.add(new CleanupStep(this.mappingsJar, this.mappingsFile, this.clientJar, this.remappedJar));
                    }
            ));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftFeatherMappings.branch));
    }

    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftFeatherMappings.branch);
    }

}
