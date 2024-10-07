package net.lenni0451.sourcegen;

import net.lenni0451.sourcegen.targets.impl.MinecraftMojangMappings;

public class Main {

    public static void main(String[] args) throws Throwable {
        new MinecraftMojangMappings().execute();
        System.out.println("Execution finished!");
    }

}
