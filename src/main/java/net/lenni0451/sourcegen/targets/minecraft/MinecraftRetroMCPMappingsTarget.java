package net.lenni0451.sourcegen.targets.minecraft;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.decompile.FillExceptionsStep;
import net.lenni0451.sourcegen.steps.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.decompile.RemapStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateRetroMCPVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftRetroMCPMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftRetroMCPMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "retromcp");
    private final File resourcesFile = new File(Main.WORK_DIR, "resources.zip");
    private final File resourcesDir = new File(Main.WORK_DIR, "resources");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftRetroMCPMappingsTarget() {
        super("Minecraft (RetroMCP Mappings)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftRetroMCPMappings.gitRepo, Config.MinecraftRetroMCPMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftRetroMCPMappings.authorName, Config.MinecraftRetroMCPMappings.authorEmail));
        steps.add(new IterateRetroMCPVersions(
                this.repoDir,
                Config.MinecraftRetroMCPMappings.branch,
                (versionSteps, versionData) -> {
                    versionSteps.add(new CleanRepoStep(this.repoDir));
                    versionSteps.add(new DownloadStep(versionData.clientUrl().get(), this.clientJar));
                    if (versionData.resourcesUrl() != null) {
                        Map<String, byte[]> jarEntries = new HashMap<>();

                        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
                        versionSteps.add(new DownloadStep(versionData.resourcesUrl(), this.resourcesFile));
                        versionSteps.add(new UnzipStep(this.resourcesFile, this.resourcesDir));
                        versionSteps.add(new RemapStep(new TinyV2Remapper(jarEntries, new File(this.resourcesDir, versionData.mappingsName()))));
                        versionSteps.add(new FillExceptionsStep(jarEntries, new File(this.resourcesDir, versionData.exceptionsName())));
                        versionSteps.add(new FixLocalVariablesStep(jarEntries));
                        versionSteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
                        versionSteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
                    } else {
                        versionSteps.add(new DecompileStandaloneStep(this.clientJar, this.repoDir));
                    }
                    versionSteps.add(new RemoveResourcesStep(this.repoDir));
                    versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
                    versionSteps.add(new CommitChangesStep(this.repoDir, versionData.name(), new Date(versionData.time().toInstant().toEpochMilli())));
                    versionSteps.add(new CleanupStep(this.resourcesFile, this.resourcesDir, this.clientJar, this.remappedJar));
                }
        ));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftRetroMCPMappings.branch));
    }

    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftRetroMCPMappings.branch);
    }

}
