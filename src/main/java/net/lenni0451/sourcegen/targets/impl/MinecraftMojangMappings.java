package net.lenni0451.sourcegen.targets.impl;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.impl.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.impl.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.impl.decompile.ReconstructStep;
import net.lenni0451.sourcegen.steps.impl.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.impl.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.impl.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.impl.io.CleanRepoStep;
import net.lenni0451.sourcegen.steps.impl.io.CleanupStep;
import net.lenni0451.sourcegen.steps.impl.io.DownloadStep;
import net.lenni0451.sourcegen.steps.impl.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MinecraftMojangMappings implements GeneratorTarget {

    private static final String REPO_URL = "https://github.com/Lenni0451/MinecraftSources";
    private static final String REPO_BRANCH = "mojang";
    private static final File REPO_DIR = new File("minecraft");
    private static final File MAPPINGS_FILE = new File("mappings");
    private static final File CLIENT_JAR = new File("client.jar");
    private static final File REMAPPED_JAR = new File("remapped.jar");
    private static final File FIXED_LOCALS_JAR = new File("fixed_locals.jar");
    private static final File[] UNWANTED_SOURCE_FILES = {new File(REPO_DIR, "assets"), new File(REPO_DIR, "data"), new File(REPO_DIR, "META-INF"), new File(REPO_DIR, "pack.png"), new File(REPO_DIR, "pack.mcmeta")};

    @Override
    public String getName() {
        return "Minecraft (Mojang Mappings)";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(REPO_DIR, REPO_URL, REPO_BRANCH));
        steps.add(new IterateMinecraftVersions(REPO_DIR, REPO_BRANCH, (versionSteps, versionName, releaseTime, manifest) -> {
            JSONObject downloads = manifest.getJSONObject("downloads");
            String clientUrl = downloads.getJSONObject("client").getString("url");
            String mappingsUrl = downloads.getJSONObject("client_mappings").getString("url");

            versionSteps.add(new CleanRepoStep(REPO_DIR));
            versionSteps.add(new DownloadStep(mappingsUrl, MAPPINGS_FILE));
            versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
            versionSteps.add(new ReconstructStep(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR));
            versionSteps.add(new FixLocalVariablesStep(REMAPPED_JAR, FIXED_LOCALS_JAR));
            versionSteps.add(new DecompileStandaloneStep(FIXED_LOCALS_JAR, REPO_DIR));
            versionSteps.add(new CleanupStep(UNWANTED_SOURCE_FILES));
            versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
            versionSteps.add(new CleanupStep(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR, FIXED_LOCALS_JAR, new File("logs")));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
