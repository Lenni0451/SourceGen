import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.steps.decompile.DecompileStandaloneStep;
import net.lenni0451.sourcegen.steps.decompile.FixLocalVariablesStep;
import net.lenni0451.sourcegen.steps.decompile.RemapStep;
import net.lenni0451.sourcegen.steps.decompile.TinyV2MetadataStep;
import net.lenni0451.sourcegen.steps.git.ChangeGitUserStep;
import net.lenni0451.sourcegen.steps.git.CommitChangesStep;
import net.lenni0451.sourcegen.steps.git.PrepareRepoStep;
import net.lenni0451.sourcegen.steps.git.PushRepoStep;
import net.lenni0451.sourcegen.steps.io.*;
import net.lenni0451.sourcegen.utils.remapping.TinyV2Remapper;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;

public class MinecraftNostalgiaMappings {

    private static final File REPO_DIR = new File("nostalgia-mappings");
    private static final String REPO_URL = "https://github.com/ExampleDude/nostalgia-mappings.git";
    private static final String BRANCH = "main";
    private static final String VERSION_NAME = "b1.7.3";
    private static final String RELEASE_TIME_STR = "2011-07-07T22:00:00+00:00";
    private static final String CLIENT_URL = "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar";
    private static final File CLIENT_JAR = new File(Main.WORK_DIR, "client.jar");
    private static final String MAPPINGS_URL = "https://mvn.devos.one/releases/me/alphamode/nostalgia/b1.7.3+build.49/nostalgia-b1.7.3+build.49-mergedv2.jar";
    private static final File MAPPINGS_JAR = new File(Main.WORK_DIR, "mappings.jar");
    private static final File MAPPINGS_FILE = new File(Main.WORK_DIR, "mappings.tiny");
    private static final File REMAPPED_CLIENT = new File(Main.WORK_DIR, "remapped_client.jar");

    public static void main(String[] args) throws Throwable {
        Main.WORK_DIR.mkdirs();
        Map<String, byte[]> jarEntries = new HashMap<>();
        List<String[]> comments = new ArrayList<>();
        new StepExecutor(
                new PrepareRepoStep(REPO_DIR, REPO_URL, BRANCH),
                new ChangeGitUserStep(REPO_DIR, Config.MinecraftMojangMappings.authorName, Config.MinecraftMojangMappings.authorEmail),
                new CleanRepoStep(REPO_DIR),
                new DownloadStep(CLIENT_URL, CLIENT_JAR),
                new DownloadStep(MAPPINGS_URL, MAPPINGS_JAR),
                new UnzipSingleFileStep(MAPPINGS_JAR, "mappings/mappings.tiny", MAPPINGS_FILE),
                new ReadJarEntriesStep(CLIENT_JAR, jarEntries),
                new FixLocalVariablesStep(jarEntries),
                new RemapStep(new TinyV2Remapper(jarEntries, MAPPINGS_FILE)),
                new TinyV2MetadataStep(jarEntries, MAPPINGS_FILE, comments),
                new WriteJarEntriesStep(jarEntries, REMAPPED_CLIENT),
                new DecompileStandaloneStep(REMAPPED_CLIENT, REPO_DIR),
                new TinyV2MetadataStep(REPO_DIR, comments),
                new RemoveResourcesStep(REPO_DIR, new File(REPO_DIR, "version.json")),
                new CommitChangesStep(REPO_DIR, VERSION_NAME, new Date(OffsetDateTime.parse(RELEASE_TIME_STR).toInstant().toEpochMilli())),
                new CleanupStep(CLIENT_JAR, MAPPINGS_JAR, MAPPINGS_FILE, REMAPPED_CLIENT),
                new PushRepoStep(REPO_DIR, BRANCH)
        ).run();
    }

}
