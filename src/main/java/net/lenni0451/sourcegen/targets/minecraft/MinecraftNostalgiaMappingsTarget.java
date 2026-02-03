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
import net.lenni0451.sourcegen.steps.target.LoadNostalgiaMappings;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;
import net.lenni0451.sourcegen.utils.remapping.TinyNamespace;
import net.lenni0451.sourcegen.utils.remapping.TinyV1Remapper;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftNostalgiaMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftNostalgiaMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_nostalgia_mappings");
    private final File mappingsJar = new File(Main.WORK_DIR, "mappings.jar");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings.tiny");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftNostalgiaMappingsTarget() {
        super("Minecraft (Nostalgia Mappings)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftNostalgiaMappings.gitRepo, Config.MinecraftNostalgiaMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftNostalgiaMappings.authorName, Config.MinecraftNostalgiaMappings.authorEmail));
        steps.add(new LoadNostalgiaMappings((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(
                    this.repoDir,
                    Config.MinecraftNostalgiaMappings.branch,
                    new IterateMinecraftVersions.VersionRange(null, null),
                    version -> versionToUrl.apply(version.getString("id")) == null,
                    false,
                    (versionSteps, versionName, releaseTime, clientUrl, serverUrl, clientMappingsUrl, serverMappingsUrl) -> {
                        Map<String, byte[]> jarEntries = new HashMap<>();

                        versionSteps.add(new CleanRepoStep(this.repoDir));
                        versionSteps.add(new DownloadAlternativesStep(versionToUrl.apply(versionName), this.mappingsJar));
                        versionSteps.add(new UnzipSingleFileStep(this.mappingsJar, "mappings/mappings.tiny", this.mappingsFile));
                        versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                        versionSteps.add(new DetectTinyVersionStep(this.mappingsFile, (version, tinySteps) -> {
                            switch (version) {
                                case V1 -> tinySteps.add(new RemapStep(new TinyV1Remapper(jarEntries, this.mappingsFile, new TinyNamespace("clientOfficial", "named"))));
                                case V2 -> tinySteps.add(new RemapStep(new TinyV2Remapper(jarEntries, this.mappingsFile, new TinyNamespace("clientOfficial", "named"))));
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                            tinySteps.add(new FixLocalVariablesStep(jarEntries));
                            switch (version) {
                                case V1 -> {
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                                }
                                case V2 -> {
                                    tinySteps.add(TinyV2MetadataStep.generate(jarEntries, this.mappingsFile));
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                                    tinySteps.add(TinyV2MetadataStep.apply(this.repoDir));
                                }
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                        }));
                        versionSteps.add(new RemoveResourcesStep(this.repoDir, new File(this.repoDir, "version.json")));
                        versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                        versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                        versionSteps.add(new CleanupStep(this.mappingsJar, this.mappingsFile, this.clientJar, this.remappedJar));
                    }
            ));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftNostalgiaMappings.branch));
    }

    @Nullable
    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftNostalgiaMappings.branch);
    }

}
