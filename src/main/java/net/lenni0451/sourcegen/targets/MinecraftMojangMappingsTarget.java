package net.lenni0451.sourcegen.targets;

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
import net.lenni0451.sourcegen.steps.io.CleanRepoStep;
import net.lenni0451.sourcegen.steps.io.CleanupStep;
import net.lenni0451.sourcegen.steps.io.DownloadStep;
import net.lenni0451.sourcegen.steps.io.RemoveResourcesStep;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions.VersionRange;
import net.lenni0451.sourcegen.utils.remapping.ProguardRemapper;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MinecraftMojangMappingsTarget implements GeneratorTarget {

    private static final File REPO_DIR = new File("minecraft");
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "minecraft_mojang_mappings");
    private static final File MAPPINGS_FILE = new File(Main.WORK_DIR, "mappings");
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final File REMAPPED_JAR = new File(Main.WORK_DIR, "remapped.jar");
    private static final File FIXED_LOCALS_JAR = new File(Main.WORK_DIR, "fixed_locals.jar");

    @Override
    public String getName() {
        return "Minecraft (Mojang Mappings)";
    }

    @Override
    public void addSteps(List<GeneratorStep> steps) {
        steps.add(new PrepareRepoStep(REPO_DIR, Config.MinecraftMojangMappings.gitRepo, Config.MinecraftMojangMappings.branch));
        steps.add(new ChangeGitUserStep(REPO_DIR, Config.MinecraftMojangMappings.authorName, Config.MinecraftMojangMappings.authorEmail));
        steps.add(new IterateMinecraftVersions(REPO_DIR, Config.MinecraftMojangMappings.branch, new VersionRange("1.14", null), (versionSteps, versionName, releaseTime, manifest) -> {
            JSONObject downloads = manifest.getJSONObject("downloads");
            String clientUrl = downloads.getJSONObject("client").getString("url");
            String mappingsUrl = downloads.getJSONObject("client_mappings").getString("url");

            versionSteps.add(new CleanRepoStep(REPO_DIR, DEFAULTS_DIR));
            versionSteps.add(new DownloadStep(mappingsUrl, MAPPINGS_FILE));
            versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
            versionSteps.add(new RemapStep(new ProguardRemapper(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR)));
            versionSteps.add(new FixLocalVariablesStep(REMAPPED_JAR, FIXED_LOCALS_JAR));
            versionSteps.add(new DecompileStandaloneStep(FIXED_LOCALS_JAR, REPO_DIR));
            versionSteps.add(new RemoveResourcesStep(REPO_DIR, new File(REPO_DIR, "version.json")));
            versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
            versionSteps.add(new CleanupStep(MAPPINGS_FILE, CLIENT_JAR, REMAPPED_JAR, FIXED_LOCALS_JAR));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
