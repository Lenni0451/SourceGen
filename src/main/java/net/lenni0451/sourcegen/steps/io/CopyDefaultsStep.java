package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CopyDefaultsStep implements GeneratorStep {

    private final File repoDir;
    private final File defaultsDir;

    public CopyDefaultsStep(final File repoDir, final File defaultsDir) {
        this.repoDir = repoDir;
        this.defaultsDir = defaultsDir;
    }

    @Override
    public void printStep() {
        System.out.println("Copying defaults from " + this.defaultsDir.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        if (this.defaultsDir.exists()) {
            for (File file : this.defaultsDir.listFiles()) {
                Files.copy(file.toPath(), new File(this.repoDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            //Give the user a hint
            this.defaultsDir.mkdirs();
        }
    }

}
