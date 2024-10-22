package net.lenni0451.sourcegen.targets;

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
import net.lenni0451.sourcegen.utils.remapping.TinyV1Remapper;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MinecraftYarnMappingsTarget implements GeneratorTarget {

    private static final File REPO_DIR = new File(Config.MinecraftYarnMappings.repoName);
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "minecraft_yarn_mappings");
    private static final File MAPPINGS_JAR = new File(Main.WORK_DIR, "mappings.jar");
    private static final File MAPPINGS_FILE = new File(Main.WORK_DIR, "mappings.tiny");
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final File REMAPPED_JAR = new File(Main.WORK_DIR, "remapped.jar");

    @Override
    public String getName() {
        return "Minecraft (Yarn Mappings)";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(REPO_DIR, Config.MinecraftYarnMappings.gitRepo, Config.MinecraftYarnMappings.branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, Config.MinecraftYarnMappings.authorName, Config.MinecraftYarnMappings.authorEmail));
        steps.add(new LoadYarnMappings((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(REPO_DIR, Config.MinecraftYarnMappings.branch, new IterateMinecraftVersions.VersionRange(null, null), version -> versionToUrl.apply(version) == null, true, (versionSteps, versionName, releaseTime, manifest) -> {
                JSONObject downloads = manifest.getJSONObject("downloads");
                String clientUrl = downloads.getJSONObject("client").getString("url");
                Map<String, byte[]> jarEntries = new HashMap<>();
                List<String> comments = new ArrayList<>();

                versionSteps.add(new CleanRepoStep(REPO_DIR));
                versionSteps.add(new DownloadAlternativesStep(versionToUrl.apply(versionName), MAPPINGS_JAR));
                versionSteps.add(new UnzipSingleFileStep(MAPPINGS_JAR, "mappings/mappings.tiny", MAPPINGS_FILE));
                versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
                versionSteps.add(new ReadJarEntriesStep(CLIENT_JAR, jarEntries));
                versionSteps.add(new DetectTinyVersionStep(MAPPINGS_FILE, (version, tinySteps) -> {
                    switch (version) {
                        case V1 -> tinySteps.add(new RemapStep(new TinyV1Remapper(jarEntries, MAPPINGS_FILE)));
                        case V2 -> tinySteps.add(new RemapStep(new TinyV2Remapper(jarEntries, MAPPINGS_FILE)));
                        default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                    }
                    tinySteps.add(new FixLocalVariablesStep(jarEntries));
                    switch (version) {
                        case V1 -> {
                            tinySteps.add(new WriteJarEntriesStep(jarEntries, REMAPPED_JAR));
                            tinySteps.add(new DecompileStandaloneStep(REMAPPED_JAR, REPO_DIR));
                        }
                        case V2 -> {
                            tinySteps.add(new TinyV2MetadataStep(jarEntries, MAPPINGS_FILE, comments));
                            tinySteps.add(new WriteJarEntriesStep(jarEntries, REMAPPED_JAR));
                            tinySteps.add(new DecompileStandaloneStep(REMAPPED_JAR, REPO_DIR));
                            tinySteps.add(new TinyV2MetadataStep(REPO_DIR, comments));
                        }
                        default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                    }
                }));
                versionSteps.add(new RemoveResourcesStep(REPO_DIR));
                versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
                versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                versionSteps.add(new CleanupStep(MAPPINGS_JAR, MAPPINGS_FILE, CLIENT_JAR, REMAPPED_JAR));
            }));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
