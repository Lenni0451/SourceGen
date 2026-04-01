import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.targets.minecraft.MinecraftMojangMappingsTarget;

import java.util.List;

public class ManualMinecraftMojangMappings {

    public static void main(String[] args) throws Throwable {
        FileUtils.recursiveDelete(Main.WORK_DIR);
        Main.WORK_DIR.mkdirs();
        Main.DEFAULTS_DIR.mkdirs();

        Config.Exclusions.minecraft = List.of();
        Config.MinecraftMojangMappings.branch = "26w14a";
        Config.MinecraftMojangMappings.gitRepo = "https://github.com/ExampleDude/AprilFoolsSources.git";
        Config.MinecraftMojangMappings.repoName = "manual_minecraft_mojang_mappings";
        MinecraftMojangMappingsTarget target = new MinecraftMojangMappingsTarget("26w14a");
        target.execute();
    }

}
