package net.lenni0451.sourcegen;

import net.lenni0451.optconfig.annotations.Description;
import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.Section;

import java.util.List;

@OptConfig
public class Config {

    @Section(name = "External")
    public static class External {
        @Option("GitPath")
        @Description("The path to the git executable")
        public static String gitPath = "git";

        @Option("JavaPath")
        @Description("The path to the java executable")
        public static String javaPath = "java";

        @Option("VineflowerJar")
        @Description("The path to the vineflower jar file")
        public static String vineflowerJar = "vineflower.jar";

        @Option("VineflowerRam")
        @Description("The amount of ram that should be allocated to vineflower")
        public static String vineflowerRam = (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "M";

        @Option("VineflowerArgs")
        @Description("The arguments that should be passed to vineflower")
        public static String[] vineflowerArgs = {"-dgs=1", "-asc=1", "-ump=0", "-rsy=1", "-aoa=1"};
    }

    @Section(name = "Exclusions", description = {
            "The versions on the exclusion list will not be decompiled",
            "This is recommended for april fools version because they contain many temporary changes which makes diffing the actual source code harder"
    })
    public static class Exclusions {
        @Option("CosmicReach")
        public static List<String> cosmicReach = List.of("0.1.17b red", "0.1.17b blue");

        @Option("Minecraft")
        @Description("This list is for Mojang mappings, Feather mappings, Yarn mappings and Parchment mappings")
        public static List<String> minecraft = List.of("22w13oneblockatatime", "3D Shareware v1.34", "15w14a", "1.RV-Pre1", "20w14infinite", "23w13a_or_b", "24w14potato");

        @Option("RetroMCP")
        public static List<String> retroMCP = List.of("rd-20090515", "b1.4_01", "b1.5_01");

        @Option("RetroMCPFork")
        public static List<String> retroMCPFork = List.of("4k-2", "4k-021742", "minicraft-ld22", "c0.30_01c", "b1.2_02-20110517");

        @Option("Bedrock")
        public static List<String> bedrock = List.of("0.13.0.0", "0.13.1.0", "0.13.2.0");
    }

    @Section(name = "MappingsSources")
    public static class OnlineResources {
        @Option("CosmicReachArchive")
        @Description("The URL to the cosmic reach archive json file")
        public static String cosmicReachArchive = "https://raw.githubusercontent.com/CRModders/CosmicArchive/refs/heads/main/versions_v2.json";

        @Option("MinecraftVersionManifest")
        @Description("The URL to the minecraft version manifest")
        public static String minecraftVersionManifest = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

        @Option("RetroMCPVersions")
        @Description("The URL to the retro mcp versions json file")
        public static String retroMCPVersions = "https://mcphackers.org/versionsV2/versions.json";

        @Option("RetroMCPForkVersions")
        @Description("The URL to the retro mcp fork versions json file")
        public static String retroMCPForkVersions = "https://raw.githubusercontent.com/Blizzardfur-Maxxx/MCPHackers.github.io/main/versions/versions.json";

        @Option("RetroMCPForkData")
        @Description("The URL to the retro mcp fork data files")
        public static String retroMCPForkData = "https://raw.githubusercontent.com/Blizzardfur-Maxxx/MCPHackers.github.io/main/versions/";

        @Option("FeatherMappings")
        @Description("The URL to the maven repository where the feather mappings are stored")
        public static String featherMappings = "https://maven.ornithemc.net/releases/net/ornithemc/feather/";

        @Option("YarnMappings")
        @Description("The URL to the maven repository where the yarn mappings are stored")
        public static String yarnMappings = "https://maven.fabricmc.net/net/fabricmc/yarn/";

        @Option("ParchmentMappings")
        @Description("The URL to the maven repository where the parchment mappings are stored")
        public static String parchmentMappings = "https://ldtteam.jfrog.io/artifactory/parchmentmc-public/org/parchmentmc/data/";

        @Option("ParchmentMetadata")
        @Description("The URL to the parchment metadata json file")
        public static String parchmentMetadata = "https://ldtteam.jfrog.io/ui/api/v1/ui/v2/nativeBrowser/parchmentmc-public/org/parchmentmc/data/?recordNum=0";

        @Option("BedrockVersions")
        @Description("The URL to the bedrock versions json file")
        public static String bedrockVersions = "https://raw.githubusercontent.com/ddf8196/mc-w10-versiondb-auto-update/refs/heads/master/versions.json.min";

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

        public static String getParchmentMappings(String suffix) {
            String url = parchmentMappings;
            if (!url.endsWith("/")) url += "/";
            while (suffix.startsWith("/")) suffix = suffix.substring(1);
            return url + suffix;
        }
    }

    @Section(name = "CosmicReach")
    public static class CosmicReach {
        @Option("ClientGitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String clientGitRepo = "https://github.com/ExampleDude/CosmicReachSources";

        @Option("ServerGitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String serverGitRepo = "https://github.com/ExampleDude/CosmicReachSources";

        @Option("ClientRepoName")
        @Description("The name of the local repository directory")
        public static String clientRepoName = "cosmicreach";

        @Option("ServerRepoName")
        @Description("The name of the local repository directory")
        public static String serverRepoName = "cosmicreach";

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

    @Section(name = "MinecraftParchmentMappings")
    public static class MinecraftParchmentMappings {
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
        public static String branch = "parchment";
    }

    @Section(name = "MinecraftAssets")
    public static class MinecraftAssets {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftAssets";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft_assets";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "main";
    }

    @Section(name = "MinecraftBedrockAssets")
    public static class MinecraftBedrockAssets {
        @Option("GitRepo")
        @Description("The URL to the git repo where the decompiled files should be stored")
        public static String gitRepo = "https://github.com/ExampleDude/MinecraftSources";

        @Option("RepoName")
        @Description("The name of the local repository directory")
        public static String repoName = "minecraft_bedrock_assets";

        @Option("AuthorName")
        @Description("The name of the author that should be used for the git commits")
        public static String authorName = "mojang";

        @Option("AuthorEmail")
        @Description("The email of the author that should be used for the git commits")
        public static String authorEmail = "noreply@mojang.com";

        @Option("Branch")
        @Description("The branch where the files should be stored")
        public static String branch = "main";
    }

}
