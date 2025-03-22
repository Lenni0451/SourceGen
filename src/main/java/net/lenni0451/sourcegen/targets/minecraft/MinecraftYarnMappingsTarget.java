package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.decompile.RemapStep;
import net.lenni0451.sourcegen.steps.decompile.TinyV2MetadataStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.target.LoadYarnMappings;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.utils.remapping.TinyV1Remapper;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MinecraftYarnMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftYarnMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_yarn_mappings");
    private final File mappingsJar = new File(Main.WORK_DIR, "mappings.jar");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings.tiny");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftYarnMappingsTarget() {
        super("Minecraft (Yarn Mappings)");
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftYarnMappings.gitRepo, Config.MinecraftYarnMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftYarnMappings.authorName, Config.MinecraftYarnMappings.authorEmail));
        steps.add(new LoadYarnMappings((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(
                    this.repoDir,
                    Config.MinecraftYarnMappings.branch,
                    new IterateMinecraftVersions.VersionRange(null, null),
                    version -> versionToUrl.apply(version.getString("id")) == null,
                    true,
                    (versionSteps, versionName, releaseTime, manifest) -> {
                        JSONObject downloads = manifest.getJSONObject("downloads");
                        String clientUrl = downloads.getJSONObject("client").getString("url");
                        Map<String, byte[]> jarEntries = new HashMap<>();
                        List<String[]> comments = new ArrayList<>();

                        versionSteps.add(new CleanRepoStep(this.repoDir));
                        versionSteps.add(new DownloadAlternativesStep(versionToUrl.apply(versionName), this.mappingsJar));
                        versionSteps.add(new UnzipSingleFileStep(this.mappingsJar, "mappings/mappings.tiny", this.mappingsFile));
                        versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                        versionSteps.add(new DetectTinyVersionStep(this.mappingsFile, (version, tinySteps) -> {
                            switch (version) {
                                case V1 -> tinySteps.add(new RemapStep(new TinyV1Remapper(jarEntries, this.mappingsFile)));
                                case V2 -> tinySteps.add(new RemapStep(new TinyV2Remapper(jarEntries, this.mappingsFile)));
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                            tinySteps.add(new FixLocalVariablesStep(jarEntries));
                            switch (version) {
                                case V1 -> {
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                                }
                                case V2 -> {
                                    tinySteps.add(new TinyV2MetadataStep(jarEntries, this.mappingsFile, comments));
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                                    tinySteps.add(new TinyV2MetadataStep(this.repoDir, comments));
                                }
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                        }));
                        versionSteps.add(new RemoveResourcesStep(this.repoDir));
                        versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                        versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                        versionSteps.add(new CleanupStep(this.mappingsJar, this.mappingsFile, this.clientJar, this.remappedJar));
                    }
            ));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftYarnMappings.branch));
    }

    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftYarnMappings.branch);
    }

}
