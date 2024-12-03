package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.decompile.ParchmentMetadataStep;
import net.lenni0451.sourcegen.steps.decompile.RemapStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.target.LoadParchmentVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.utils.remapping.ProguardRemapper;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MinecraftParchmentMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftParchmentMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_parchment_mappings");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings");
    private final File metadataJar = new File(Main.WORK_DIR, "mappings.jar");
    private final File metadataFile = new File(Main.WORK_DIR, "metadata.json");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftParchmentMappingsTarget() {
        super("Minecraft (Mojang + Parchment Mappings)");
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftParchmentMappings.gitRepo, Config.MinecraftParchmentMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftParchmentMappings.authorName, Config.MinecraftParchmentMappings.authorEmail));
        steps.add(new LoadParchmentVersions((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(
                    this.repoDir,
                    Config.MinecraftParchmentMappings.branch,
                    new IterateMinecraftVersions.VersionRange("1.16.5", null),
                    ver -> versionToUrl.apply(ver) == null,
                    false,
                    (versionSteps, versionName, releaseTime, manifest) -> {
                        JSONObject downloads = manifest.getJSONObject("downloads");
                        String clientUrl = downloads.getJSONObject("client").getString("url");
                        String mappingsUrl = downloads.getJSONObject("client_mappings").getString("url");
                        String metadataUrl = versionToUrl.apply(versionName);
                        Map<String, byte[]> jarEntries = new HashMap<>();
                        List<String[]> comments = new ArrayList<>();

                        versionSteps.add(new CleanRepoStep(this.repoDir));
                        versionSteps.add(new DownloadStep(mappingsUrl, this.mappingsFile));
                        versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                        versionSteps.add(new DownloadStep(metadataUrl, this.metadataJar));
                        versionSteps.add(new UnzipSingleFileStep(this.metadataJar, "parchment.json", this.metadataFile));
                        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                        versionSteps.add(new RemapStep(new ProguardRemapper(jarEntries, this.mappingsFile)));
                        versionSteps.add(new FixLocalVariablesStep(jarEntries));
                        versionSteps.add(new ParchmentMetadataStep(jarEntries, this.metadataFile, comments));
                        versionSteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                        versionSteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                        versionSteps.add(new ParchmentMetadataStep(this.repoDir, comments));
                        versionSteps.add(new RemoveResourcesStep(this.repoDir, new File(this.repoDir, "version.json")));
                        versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                        versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                        versionSteps.add(new CleanupStep(this.mappingsFile, this.clientJar, this.metadataJar, this.metadataFile, this.remappedJar));
                    }
            ));
        }));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftParchmentMappings.branch));
    }

    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftParchmentMappings.branch);
    }

}
