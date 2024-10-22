package net.lenni0451.sourcegen.targets;

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
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetroMCPTarget implements GeneratorTarget {

    private static final File REPO_DIR = new File(Config.MinecraftRetroMCPMappings.repoName);
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "retromcp");
    private static final File RESOURCES_FILE = new File(Main.WORK_DIR, "resources.zip");
    private static final File RESOURCES_DIR = new File(Main.WORK_DIR, "resources");
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final File REMAPPED_JARr = new File(Main.WORK_DIR, "remapped.jar");

    @Override
    public String getName() {
        return "RetroMCP";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(REPO_DIR, Config.MinecraftRetroMCPMappings.gitRepo, Config.MinecraftRetroMCPMappings.branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, Config.MinecraftRetroMCPMappings.authorName, Config.MinecraftRetroMCPMappings.authorEmail));
        steps.add(new IterateRetroMCPVersions(REPO_DIR, Config.MinecraftRetroMCPMappings.branch, (versionSteps, versionName, releaseTime, resourcesUrl, manifest) -> {
            JSONObject downloads = manifest.getJSONObject("downloads");
            String clientUrl = downloads.getJSONObject("client").getString("url");

            versionSteps.add(new CleanRepoStep(REPO_DIR));
            versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
            if (resourcesUrl != null) {
                Map<String, byte[]> jarEntries = new HashMap<>();

                versionSteps.add(new ReadJarEntriesStep(CLIENT_JAR, jarEntries));
                versionSteps.add(new DownloadStep(resourcesUrl, RESOURCES_FILE));
                versionSteps.add(new UnzipStep(RESOURCES_FILE, RESOURCES_DIR));
                versionSteps.add(new RemapStep(new TinyV2Remapper(jarEntries, new File(RESOURCES_DIR, "mappings.tiny"))));
                versionSteps.add(new FillExceptionsStep(jarEntries, new File(RESOURCES_DIR, "exceptions.exc")));
                versionSteps.add(new FixLocalVariablesStep(jarEntries));
                versionSteps.add(new WriteJarEntriesStep(jarEntries, REMAPPED_JARr));
                versionSteps.add(new DecompileStandaloneStep(REMAPPED_JARr, REPO_DIR));
            } else {
                versionSteps.add(new DecompileStandaloneStep(CLIENT_JAR, REPO_DIR));
            }
            versionSteps.add(new RemoveResourcesStep(REPO_DIR));
            versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
            versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
            versionSteps.add(new CleanupStep(RESOURCES_FILE, RESOURCES_DIR, CLIENT_JAR, REMAPPED_JARr));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
