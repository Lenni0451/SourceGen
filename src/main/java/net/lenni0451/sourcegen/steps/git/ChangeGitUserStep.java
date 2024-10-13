package net.lenni0451.sourcegen.steps.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

public class ChangeGitUserStep implements GeneratorStep {

    private final File repoDir;
    private final String name;
    private final String email;

    public ChangeGitUserStep(final File repoDir, final String name, final String email) {
        this.repoDir = repoDir;
        this.name = name;
        this.email = email;
    }

    @Override
    public void printStep() {
        System.out.println("Changing git user to: " + this.name + " <" + this.email + ">");
    }

    @Override
    public void run() throws Exception {
        Commands.git(this.repoDir).setConfig(this.name, this.email);
    }

}
