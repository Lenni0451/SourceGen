package net.lenni0451.sourcegen.targets;

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
import net.lenni0451.sourcegen.utils.remapping.ProguardRemapper;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MinecraftParchmentMappingsTarget implements GeneratorTarget {

    private static final File REPO_DIR = new File(Config.MinecraftParchmentMappings.repoName);
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "minecraft_parchment_mappings");
    private static final File MAPPINGS_FILE = new File(Main.WORK_DIR, "mappings");
    private static final File METADATA_JAR = new File(Main.WORK_DIR, "mappings.jar");
    private static final File METADATA_FILE = new File(Main.WORK_DIR, "metadata.json");
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final File REMAPPED_JAR = new File(Main.WORK_DIR, "remapped.jar");

    @Override
    public String getName() {
        return "Minecraft (Mojang + Parchment Mappings)";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(REPO_DIR, Config.MinecraftParchmentMappings.gitRepo, Config.MinecraftParchmentMappings.branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, Config.MinecraftParchmentMappings.authorName, Config.MinecraftParchmentMappings.authorEmail));
        steps.add(new LoadParchmentVersions((subSteps, versionToUrl) -> {
            subSteps.add(new IterateMinecraftVersions(REPO_DIR, Config.MinecraftParchmentMappings.branch, new IterateMinecraftVersions.VersionRange("1.16.5", null), ver -> versionToUrl.apply(ver) == null, false, (versionSteps, versionName, releaseTime, manifest) -> {
                JSONObject downloads = manifest.getJSONObject("downloads");
                String clientUrl = downloads.getJSONObject("client").getString("url");
                String mappingsUrl = downloads.getJSONObject("client_mappings").getString("url");
                String metadataUrl = versionToUrl.apply(versionName);
                Map<String, byte[]> jarEntries = new HashMap<>();
                List<String[]> comments = new ArrayList<>();

                versionSteps.add(new CleanRepoStep(REPO_DIR));
                versionSteps.add(new DownloadStep(mappingsUrl, MAPPINGS_FILE));
                versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
                versionSteps.add(new DownloadStep(metadataUrl, METADATA_JAR));
                versionSteps.add(new UnzipSingleFileStep(METADATA_JAR, "parchment.json", METADATA_FILE));
                versionSteps.add(new ReadJarEntriesStep(CLIENT_JAR, jarEntries));
                versionSteps.add(new RemapStep(new ProguardRemapper(jarEntries, MAPPINGS_FILE)));
                versionSteps.add(new FixLocalVariablesStep(jarEntries));
                versionSteps.add(new ParchmentMetadataStep(jarEntries, METADATA_FILE, comments));
                versionSteps.add(new WriteJarEntriesStep(jarEntries, REMAPPED_JAR));
                versionSteps.add(new DecompileStandaloneStep(REMAPPED_JAR, REPO_DIR));
                versionSteps.add(new ParchmentMetadataStep(REPO_DIR, comments));
                versionSteps.add(new RemoveResourcesStep(REPO_DIR, new File(REPO_DIR, "version.json")));
                versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
                versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                versionSteps.add(new CleanupStep(MAPPINGS_FILE, CLIENT_JAR, METADATA_JAR, METADATA_FILE, REMAPPED_JAR));
            }));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
