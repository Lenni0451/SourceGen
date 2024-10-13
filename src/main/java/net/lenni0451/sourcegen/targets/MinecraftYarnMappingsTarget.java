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
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.steps.target.IterateMinecraftVersions;
import net.lenni0451.sourcegen.steps.target.LoadYarnMappings;
import net.lenni0451.sourcegen.utils.remapping.TinyV1Remapper;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MinecraftYarnMappingsTarget implements GeneratorTarget {

    private static final File REPO_DIR = new File("minecraft");
    private static final File DEFAULTS_DIR = new File(Main.DEFAULTS_DIR, "minecraft_yarn_mappings");
    private static final File MAPPINGS_JAR = new File(Main.WORK_DIR, "mappings.jar");
    private static final File MAPPINGS_FILE = new File(Main.WORK_DIR, "mappings.tiny");
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final File REMAPPED_JAR = new File(Main.WORK_DIR, "remapped.jar");
    private static final File FIXED_LOCALS_JAR = new File(Main.WORK_DIR, "fixed_locals.jar");

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

                versionSteps.add(new CleanRepoStep(REPO_DIR));
                versionSteps.add(new DownloadStep(versionToUrl.apply(versionName), MAPPINGS_JAR));
                versionSteps.add(new UnzipSingleFileStep(MAPPINGS_JAR, "mappings/mappings.tiny", MAPPINGS_FILE));
                versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
                versionSteps.add(new RemapStep(new TinyV1Remapper(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR)));
                versionSteps.add(new FixLocalVariablesStep(REMAPPED_JAR, FIXED_LOCALS_JAR));
                versionSteps.add(new DecompileStandaloneStep(FIXED_LOCALS_JAR, REPO_DIR));
                versionSteps.add(new RemoveResourcesStep(REPO_DIR));
                versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
                versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                versionSteps.add(new CleanupStep(MAPPINGS_JAR, MAPPINGS_FILE, CLIENT_JAR, REMAPPED_JAR, FIXED_LOCALS_JAR));
            }));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
