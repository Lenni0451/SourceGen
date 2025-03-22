package net.lenni0451.sourcegen;

import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.minecraft.*;
import net.lenni0451.sourcegen.targets.other.CosmicReachTarget;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final File WORK_DIR = new File("work");
    public static final File DEFAULTS_DIR = new File("defaults");
    private static final List<GeneratorTarget> TARGETS = new ArrayList<>();

    public static void main(String[] args) throws Throwable {
        loadConfig();
        loadGenerators();
        checkRequirements();
        if (args.length != 1) {
            System.out.println("Please specify a target to generate!");
            printTargets();
            return;
        }

        int targetIndex;
        try {
            targetIndex = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid target index: " + args[0]);
            printTargets();
            return;
        }
        if (targetIndex < 1 || targetIndex > TARGETS.size()) {
            System.out.println("Invalid target index: " + targetIndex);
            printTargets();
            return;
        }

        FileUtils.recursiveDelete(WORK_DIR); //Remove old leftovers
        WORK_DIR.mkdirs();
        DEFAULTS_DIR.mkdirs();
        GeneratorTarget target = TARGETS.get(targetIndex - 1);
        System.out.println("Generating target: " + target.getName());
        target.execute();
        System.out.println("Done!");
    }

    private static void loadGenerators() {
        TARGETS.add(new MinecraftMojangMappingsTarget());
        TARGETS.add(new CosmicReachTarget());
        TARGETS.add(new MinecraftRetroMCPMappingsTarget());
        TARGETS.add(new MinecraftFeatherMappingsTarget());
        TARGETS.add(new MinecraftYarnMappingsTarget());
        TARGETS.add(new MinecraftParchmentMappingsTarget());
    }

    private static void printTargets() {
        System.out.println("Available targets:");
        for (int i = 0; i < TARGETS.size(); i++) {
            GeneratorTarget target = TARGETS.get(i);
            System.out.println((i + 1) + ". " + target.getName());
        }
    }

    private static void checkRequirements() {
        if (!Commands.Vineflower.exists()) {
            System.out.println("VineFlower is not present in the working directory.");
            System.out.println("Please download VineFlower and put 'vineflower.jar' into the working directory.");
            System.exit(-1);
        }
    }

    private static void loadConfig() throws IOException {
        ConfigLoader<Config> configLoader = new ConfigLoader<>(Config.class);
        configLoader.getConfigOptions()
                .setResetInvalidOptions(true)
                .setRewriteConfig(true)
                .setClassAccessFactory(clazz -> new ReflectionClassAccess(clazz, true));
        configLoader.loadStatic(ConfigProvider.file(new File("config.yml")));
    }

}
