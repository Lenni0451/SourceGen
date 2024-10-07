package net.lenni0451.sourcegen;

import net.lenni0451.sourcegen.targets.impl.MinecraftMojangMappings;

public class Main {

    public static void main(String[] args) throws Throwable {
//        Commands.Git git = Commands.git(new File(".", "CosmicReachSources"));
////        git.clone("https://github.com/Lenni0451/CosmicReachSources.git");
////        git.checkout("client");
//        Date yesterday = Calendar.getInstance().getTime();
//        yesterday.setDate(yesterday.getDate() - 1);
//
//        git.addAll();
//        git.commit("Test", yesterday);

        new MinecraftMojangMappings().execute();
        System.out.println("Execution finished!");
    }

}
