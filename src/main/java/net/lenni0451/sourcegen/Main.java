package net.lenni0451.sourcegen;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.Requirements;
import net.lenni0451.sourcegen.targets.minecraft.*;
import net.lenni0451.sourcegen.targets.other.CosmicReachTarget;
import net.lenni0451.sourcegen.targets.other.HytaleServerTarget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {

    public static final File WORK_DIR = new File("work");
    public static final File DEFAULTS_DIR = new File("defaults");
    private static final List<GeneratorTarget> TARGETS = new ArrayList<>();

    public static void main(String[] args) throws Throwable {
        loadConfig();
        loadGenerators();
        if (args.length != 1) {
            log.info("Please specify a target to generate!");
            printTargets();
            return;
        }

        int targetIndex;
        try {
            targetIndex = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            log.error("Invalid target index: {}", args[0]);
            printTargets();
            return;
        }
        if (targetIndex < 1 || targetIndex > TARGETS.size() || TARGETS.get(targetIndex - 1) == null) {
            log.error("Invalid target index: {}", targetIndex);
            printTargets();
            return;
        }

        FileUtils.recursiveDelete(WORK_DIR); //Remove old leftovers
        WORK_DIR.mkdirs();
        DEFAULTS_DIR.mkdirs();
        GeneratorTarget target = TARGETS.get(targetIndex - 1);
        for (Requirements requirement : target.getRequirements()) {
            if (!requirement.isPresent()) {
                log.info(requirement.getMessage());
                return;
            }
        }
        log.info("Generating target: {}", target.getName());
        target.execute();
        log.info("Done!");
    }

    /**
     * Load all available generators.<br>
     * When adding a new generator, add it to the <b>end</b> of this list to keep the target indices stable.<br>
     * If a generator is removed, replace it with null to keep the target indices stable.
     */
    private static void loadGenerators() {
        TARGETS.add(new MinecraftMojangMappingsTarget());
        TARGETS.add(new CosmicReachTarget());
        TARGETS.add(new MinecraftRetroMCPMappingsTarget());
        TARGETS.add(new MinecraftFeatherMappingsTarget());
        TARGETS.add(new MinecraftYarnMappingsTarget());
        TARGETS.add(new MinecraftParchmentMappingsTarget());
        TARGETS.add(new MinecraftAssetsTarget());
        TARGETS.add(null); //previously: Minecraft Bedrock Edition assets (removed due to UWP discontinuation)
        TARGETS.add(new MinecraftMCPMappingsTarget());
        TARGETS.add(new HytaleServerTarget());
        TARGETS.add(new MinecraftNostalgiaMappingsTarget());
    }

    private static void printTargets() {
        log.info("Available targets:");
        for (int i = 0; i < TARGETS.size(); i++) {
            GeneratorTarget target = TARGETS.get(i);
            if (target == null) continue;
            log.info("{}. {}", i + 1, target.getName());
        }
    }

    private static void loadConfig() throws IOException {
        ConfigLoader<Config> configLoader = new ConfigLoader<>(Config.class);
        configLoader.getConfigOptions()
                .setResetInvalidOptions(true)
                .setRewriteConfig(true);
        configLoader.loadStatic(ConfigProvider.file(new File("config.yml")));
    }

}
