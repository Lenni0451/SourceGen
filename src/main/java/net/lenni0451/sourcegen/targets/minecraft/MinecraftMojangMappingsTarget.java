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
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions.VersionRange;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;
import net.lenni0451.sourcegen.utils.remapping.ProguardRemapper;

import javax.annotation.Nullable;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftMojangMappingsTarget extends GeneratorTarget {

    private final File repoDir = new File(Config.MinecraftMojangMappings.repoName);
    private final File defaultsDir = new File(Main.DEFAULTS_DIR, "minecraft_mojang_mappings");
    private final File mappingsFile = new File(Main.WORK_DIR, "mappings");
    private final File clientJar = new File(Main.WORK_DIR, "client.jar");
    private final File remappedJar = new File(Main.WORK_DIR, "remapped.jar");

    public MinecraftMojangMappingsTarget() {
        super("Minecraft (Mojang Mappings)", Requirements.VINEFLOWER);
    }

    @Override
    protected void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(this.repoDir, Config.MinecraftMojangMappings.gitRepo, Config.MinecraftMojangMappings.branch));
        steps.add(new ChangeGitUserStep(this.repoDir, Config.MinecraftMojangMappings.authorName, Config.MinecraftMojangMappings.authorEmail));
        steps.add(new IterateMinecraftVersions(
                this.repoDir,
                Config.MinecraftMojangMappings.branch,
                new VersionRange("1.14.4", null), //First version with official mappings
                version -> Config.Exclusions.minecraftOfficial.contains(version.getString("id")),
                false,
                (versionSteps, versionName, releaseTime, clientUrl, clientMappingsUrl) -> {
                    this.addVersionSteps(versionSteps, versionName, releaseTime, clientUrl, clientMappingsUrl);
                    if (versionName.equals("1.21.11")) {
                        //Transition from obfuscated to deobfuscated jars
                        //Generate the sources for 1.21.11 twice to make diffing easier
                        //Maybe this can be put into the config at some point to make the process more flexible
                        this.addVersionSteps(versionSteps, versionName, releaseTime, "https://piston-data.mojang.com/v1/objects/4509ee9b65f226be61142d37bf05f8d28b03417b/client.jar", null);
                    }
                }
        ));
        steps.add(new PushRepoStep(this.repoDir, Config.MinecraftMojangMappings.branch));
    }

    private void addVersionSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final String clientUrl, final String clientMappingsUrl) {
        Map<String, byte[]> jarEntries = new HashMap<>();

        versionSteps.add(new CleanRepoStep(this.repoDir));
        versionSteps.add(new DownloadStep(clientUrl, this.clientJar));
        versionSteps.add(new ReadJarEntriesStep(this.clientJar, jarEntries));
        if (clientMappingsUrl != null) {
            versionSteps.add(new DownloadStep(clientMappingsUrl, this.mappingsFile));
            versionSteps.add(new RemapStep(new ProguardRemapper(jarEntries, this.mappingsFile)));
            versionSteps.add(new FixLocalVariablesStep(jarEntries));
        }
        versionSteps.add(new WriteJarEntriesStep(jarEntries, this.remappedJar));
        versionSteps.add(new DecompileStandaloneStep(this.remappedJar, this.repoDir));
        versionSteps.add(new RemoveResourcesStep(this.repoDir, new File(this.repoDir, "version.json")));
        versionSteps.add(new CopyDefaultsStep(this.repoDir, this.defaultsDir));
        versionSteps.add(new CommitChangesStep(this.repoDir, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
        versionSteps.add(new CleanupStep(this.mappingsFile, this.clientJar, this.remappedJar));
    }

    @Nullable
    @Override
    protected GeneratorStep getErrorStep() {
        return new PushRepoStep(this.repoDir, Config.MinecraftMojangMappings.branch);
    }

}
