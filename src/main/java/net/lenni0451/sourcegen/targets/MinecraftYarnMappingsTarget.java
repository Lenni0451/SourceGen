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
import java.util.ArrayList;
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
    private static final File APPLIED_METADATA_JAR = new File(Main.WORK_DIR, "applied_metadata.jar");

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
                List<String> comments = new ArrayList<>();

                versionSteps.add(new CleanRepoStep(REPO_DIR));
                versionSteps.add(new DownloadAlternativesStep(versionToUrl.apply(versionName), MAPPINGS_JAR));
                versionSteps.add(new UnzipSingleFileStep(MAPPINGS_JAR, "mappings/mappings.tiny", MAPPINGS_FILE));
                versionSteps.add(new DownloadStep(clientUrl, CLIENT_JAR));
                versionSteps.add(new DetectTinyVersionStep(MAPPINGS_FILE, (version, tinySteps) -> {
                    switch (version) {
                        case V1 -> tinySteps.add(new RemapStep(new TinyV1Remapper(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR)));
                        case V2 -> tinySteps.add(new RemapStep(new TinyV2Remapper(CLIENT_JAR, MAPPINGS_FILE, REMAPPED_JAR)));
                        default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                    }
                    tinySteps.add(new FixLocalVariablesStep(REMAPPED_JAR, FIXED_LOCALS_JAR));
                    switch (version) {
                        case V1 -> tinySteps.add(new DecompileStandaloneStep(FIXED_LOCALS_JAR, REPO_DIR));
                        case V2 -> {
                            tinySteps.add(new TinyV2MetadataStep(FIXED_LOCALS_JAR, MAPPINGS_FILE, APPLIED_METADATA_JAR, comments));
                            tinySteps.add(new DecompileStandaloneStep(APPLIED_METADATA_JAR, REPO_DIR));
                            tinySteps.add(new TinyV2MetadataStep(REPO_DIR, comments));
                        }
                        default -> throw new IllegalStateException("Unknown tiny mappings version: " + version);
                    }
                }));
                versionSteps.add(new RemoveResourcesStep(REPO_DIR));
                versionSteps.add(new CopyDefaultsStep(REPO_DIR, DEFAULTS_DIR));
                versionSteps.add(new CommitChangesStep(REPO_DIR, versionName, new Date(releaseTime.toInstant().toEpochMilli())));
                versionSteps.add(new CleanupStep(MAPPINGS_JAR, MAPPINGS_FILE, CLIENT_JAR, REMAPPED_JAR, FIXED_LOCALS_JAR, APPLIED_METADATA_JAR));
            }));
        }));
        steps.add(new PushRepoStep(REPO_DIR));
    }

}
