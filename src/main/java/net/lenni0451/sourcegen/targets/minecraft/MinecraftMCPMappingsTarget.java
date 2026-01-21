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
import net.lenni0451.sourcegen.steps.target.IterateMCPVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;
import net.lenni0451.sourcegen.utils.remapping.mcp.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftMCPMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftMCPMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_mcp_mappings");
    private final File mappingsTmpFile = new File(Main.WORK_DIR, "mappings_tmp");
    private final File mappingsDirectory = new File(Main.WORK_DIR, "mappings");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftMCPMappingsTarget() {
        super("Minecraft (MCP Mappings)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftMCPMappings.gitRepo, Config.MinecraftMCPMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftMCPMappings.authorName, Config.MinecraftMCPMappings.authorEmail));
        steps.add(new IterateMCPVersions(
                this.repoDir,
                Config.MinecraftMCPMappings.branch,
                (versionSteps, versionName, releaseTime, clientUrl, mappingsURLs, mappingsVersion) -> {
                    Map<String, byte[]> jarEntries = new HashMap<>();

                    versionSteps.add(new CleanRepoStep(this.repoDir));
                    for (String url : mappingsURLs) {
                        versionSteps.add(new DownloadStep(url, this.mappingsTmpFile));
                        versionSteps.add(new UnzipStep(this.mappingsTmpFile, this.mappingsDirectory));
                        versionSteps.add(new CleanupStep(this.mappingsTmpFile));
                    }
                    versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
                    versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                    switch (mappingsVersion) {
                        case "MCP1" -> versionSteps.add(new RemapStep(new MCP1Remapper(jarEntries, this.mappingsDirectory)));
                        case "MCP2" -> versionSteps.add(new RemapStep(new MCP2Remapper(jarEntries, this.mappingsDirectory)));
                        case "MCP3" -> versionSteps.add(new RemapStep(new MCP3Remapper(jarEntries, this.mappingsDirectory)));
                        case "MCP4" -> versionSteps.add(new RemapStep(new MCP4Remapper(jarEntries, this.mappingsDirectory)));
                        case "MCP5" -> versionSteps.add(new RemapStep(new MCP5Remapper(jarEntries, this.mappingsDirectory)));
                        case "FML1" -> versionSteps.add(new RemapStep(new FML1Remapper(jarEntries, this.mappingsDirectory)));
                        case "FML2" -> versionSteps.add(new RemapStep(new FML2Remapper(jarEntries, this.mappingsDirectory)));
                        case "FML3" -> versionSteps.add(new RemapStep(new FML3Remapper(jarEntries, this.mappingsDirectory)));
                        default -> throw new IllegalArgumentException("Unknown mappings version: " + mappingsVersion);
                    }
                    versionSteps.add(new FixLocalVariablesStep(jarEntries));
                    versionSteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                    versionSteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                    versionSteps.add(new RemoveResourcesStep(this.repoDir, new File(this.repoDir, "version.json")));
                    versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                    versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                    versionSteps.add(new CleanupStep(this.mappingsTmpFile, this.mappingsDirectory, this.clientJar, this.remappedJar));
                }
        ));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftMCPMappings.branch));
    }

    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftMCPMappings.branch);
    }

}
