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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftNostalgiaMappingsTarget extends GeneratorTarget {

    private final File mappingsJar = new File(Main.WORK_DIR, "mappings.jar");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings.tiny");
    private final File targetJar = new File(Main.WORK_DIR, "target.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftNostalgiaMappingsTarget() {
        super("Minecraft (Nostalgia Mappings)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        if (Config.MinecraftNostalgiaMappings.Client.enabled) {
            this.addSteps(
                    new File(Config.MinecraftNostalgiaMappings.Client.repoName),
                    new File(Main.DEFAULTS_DIR, "minecraft_client_nostalgia_mappings"),
                    Config.MinecraftNostalgiaMappings.Client.gitRepo,
                    Target.CLIENT,
                    new TinyNamespace("clientOfficial", "named"),
                    steps,
                    Config.MinecraftNostalgiaMappings.Client.branch
            );
        }
        if (Config.MinecraftNostalgiaMappings.Server.enabled) {
            this.addSteps(
                    new File(Config.MinecraftNostalgiaMappings.Server.repoName),
                    new File(Main.DEFAULTS_DIR, "minecraft_server_nostalgia_mappings"),
                    Config.MinecraftNostalgiaMappings.Server.gitRepo,
                    Target.SERVER,
                    new TinyNamespace("serverOfficial", "named"),
                    steps,
                    Config.MinecraftNostalgiaMappings.Server.branch
            );
        }
    }

    private void addSteps(final File repoDir, final File defaultsDir, final String gitRepo, final Target target, final TinyNamespace namespace, final List<GeneratorStep> steps, final String branch) {
        steps.add(new PrepareRepoStep(repoDir, gitRepo, branch));
        steps.add(new ChangeGitUserStep(repoDir, Config.MinecraftNostalgiaMappings.authorName, Config.MinecraftNostalgiaMappings.authorEmail));
        steps.add(new LoadNostalgiaMappings((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(
                    repoDir,
                    branch,
                    new IterateMinecraftVersions.VersionRange(null, null),
                    version -> versionToUrl.apply(version.getString("id")) == null,
                    false,
                    (versionSteps, versionName, releaseTime, clientUrl, serverUrl, clientMappingsUrl, serverMappingsUrl) -> {
                        Map<String, byte[]> jarEntries = new HashMap<>();

                        versionSteps.add(new CleanRepoStep(repoDir));
                        versionSteps.add(new DownloadAlternativesStep(versionToUrl.apply(versionName), this.mappingsJar));
                        versionSteps.add(new UnzipSingleFileStep(this.mappingsJar, "mappings/mappings.tiny", this.mappingsFile));
                        versionSteps.add(new DownloadStep(target.getDownloadUrl(clientUrl, serverUrl), this.targetJar));
                        versionSteps.add(new ReadJarEntriesStep(this.targetJar, jarEntries));
                        versionSteps.add(new DetectTinyVersionStep(this.mappingsFile, (version, tinySteps) -> {
                            switch (version) {
                                case V1 -> tinySteps.add(new RemapStep(new TinyV1Remapper(jarEntries, this.mappingsFile, namespace)));
                                case V2 -> tinySteps.add(new RemapStep(new TinyV2Remapper(jarEntries, this.mappingsFile, namespace)));
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                            tinySteps.add(new FixLocalVariablesStep(jarEntries));
                            switch (version) {
                                case V1 -> {
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, repoDir));
                                }
                                case V2 -> {
                                    tinySteps.add(TinyV2MetadataStep.generate(jarEntries, this.mappingsFile, namespace));
                                    tinySteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                                    tinySteps.add(new DecompileStandaloneStep(this.remappedJar, repoDir));
                                    tinySteps.add(TinyV2MetadataStep.apply(repoDir));
                                }
                                default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                            }
                        }));
                        versionSteps.add(new RemoveResourcesStep(repoDir, new File(repoDir, "version.json")));
                        versionSteps.add(new CopyDefaultsStep(repoDir, defaultsDir));
                        versionSteps.add(new CommitChangesStep(repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                        versionSteps.add(new CleanupStep(this.mappingsJar, this.mappingsFile, this.targetJar, this.remappedJar));
                    }
            ));
        }));
        steps.add(new PushRepoStep(repoDir, branch));
    }


    private enum Target {
        CLIENT {
            @Override
            public String getDownloadUrl(final String clientUrl, final String serverUrl) {
                return clientUrl;
            }
        },
        SERVER {
            @Override
            public String getDownloadUrl(final String clientUrl, final String serverUrl) {
                return serverUrl;
            }
        };

        public abstract String getDownloadUrl(final String clientUrl, final String serverUrl);
    }

}
