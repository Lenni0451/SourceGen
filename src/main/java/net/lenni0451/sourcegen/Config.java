package net.lenni0451.sourcegen;

import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.annotations.Description;
import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.Section;
import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

@OptConfig
public class Config {

    public static void load() throws IOException {
        ConfigLoader<Config> configLoader = new ConfigLoader<>(Config.class);
        configLoader.getConfigOptions()
                .setResetInvalidOptions(true)
                .setRewriteConfig(true)
                .setClassAccessFactory(clazz -> new ReflectionClassAccess(clazz, true));
        configLoader.loadStatic(ConfigProvider.file(new File("config.yml")));
    }

    @Section(name = "Exclusions", description = {
            "The versions on the exclusion list will not be decompiled",
            "This is recommended for april fools version because they contain many temporary changes which makes diffing the actual source code harder"
    })
    public static class Exclusions {
        @Option("CosmicReach")
        public static List<String> cosmicReach = List.of("0.1.17b red", "0.1.17b blue");

        @Option("Minecraft")
        @Description("This list is for Mojang mappings, Feather mappings and Yarn mappings")
        public static List<String> minecraft = List.of("22w13oneblockatatime", "20w14infinite", "3D Shareware v1.34", "15w14a", "1.RV-Pre1", "20w14infinite", "23w13a_or_b", "24w14potato");

        @Option("RetroMCP")
        public static List<String> retroMCP = List.of("rd-20090515", "b1.4_01", "b1.5_01");
    }

    @Section(name = "MappingsSources")
    public static class OnlineResources {
        @Option("CosmicReachArchive")
        @Description("The URL to the cosmic reach archive json file")
        public static String cosmicReachArchive = "https://raw.githubusercontent.com/CRModders/CosmicArchive/refs/heads/main/versions.json";

        @Option("MinecraftVersionManifest")
        @Description("The URL to the minecraft version manifest")
        public static String minecraftVersionManifest = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

        @Option("RetroMCPVersions")
        @Description("The URL to the retro mcp versions json file")
        public static String retroMCPVersions = "https://mcphackers.org/versionsV2/versions.json";

        @Option("FeatherMappings")
        @Description("The URL to the maven repository where the feather mappings are stored")
        public static String featherMappings = "https://maven.ornithemc.net/releases/net/ornithemc/feather/";

        @Option("YarnMappings")
        @Description("The URL to the maven repository where the yarn mappings are stored")
        public static String yarnMappings = "https://maven.fabricmc.net/net/fabricmc/yarn/";

        public static String getFeatherMappings(String suffix) {
            String url = featherMappings;
            if (!url.endsWith("/")) url += "/";
            while (suffix.startsWith("/")) suffix = suffix.substring(1);
            return url + suffix;
        }

        public static String getYarnMappings(String suffix) {
            String url = yarnMappings;
            if (!url.endsWith("/")) url += "/";
            while (suffix.startsWith("/")) suffix = suffix.substring(1);
            return url + suffix;
        }
    }

    @Section(name = "CosmicReach")
    public static class CosmicReach {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/CosmicReachSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "cosmicreach";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "finalforeach";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "finalforeach@github.io";

        @Option("ClientBranch")
        @Description("The branch where the client files should be stored")
        public static String clientBranch = "client";

        @Option("ServerBranch")
        @Description("The branch where the server files should be stored")
        public static String serverBranch = "server";
    }

    @Section(name = "MinecraftFeatherMappings")
    public static class MinecraftFeatherMappings {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "feather";
    }

    @Section(name = "MinecraftMojangMappings")
    public static class MinecraftMojangMappings {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "mojang";
    }

    @Section(name = "MinecraftYarnMappings")
    public static class MinecraftYarnMappings {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "yarn";
    }

    @Section(name = "MinecraftRetroMCPMappings")
    public static class MinecraftRetroMCPMappings {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "retromcp";
    }

}
