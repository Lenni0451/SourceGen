package net.lenni0451.sourcegen;

import net.lenni0451.sourcegen.targets.GeneratorTarget;
import net.lenni0451.sourcegen.targets.impl.CosmicReachTarget;
import net.lenni0451.sourcegen.targets.impl.MinecraftFeatherMappingsTarget;
import net.lenni0451.sourcegen.targets.impl.MinecraftMojangMappingsTarget;
import net.lenni0451.sourcegen.targets.impl.RetroMCPTarget;

import java.util.List;

public class Main {

    private static final List<GeneratorTarget> TARGETS = List.of(
            new MinecraftMojangMappingsTarget(),
            new CosmicReachTarget(),
            new RetroMCPTarget(),
            new MinecraftFeatherMappingsTarget()
    );

    public static void main(String[] args) throws Throwable {
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

        GeneratorTarget target = TARGETS.get(targetIndex - 1);
        System.out.println("Generating target: " + target.getName());
        target.execute();
        System.out.println("Done!");
    }

    private static void printTargets() {
        System.out.println("Available targets:");
        for (int i = 0; i < TARGETS.size(); i++) {
            GeneratorTarget target = TARGETS.get(i);
            System.out.println((i + 1) + ". " + target.getName());
        }
    }

}
