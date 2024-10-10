package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

public class ChangeGitUserStep implements GeneratorStep {

    private final File gitDir;
    private final String name;
    private final String email;

    public ChangeGitUserStep(final File gitDir, final String name, final String email) {
        this.gitDir = gitDir;
        this.name = name;
        this.email = email;
    }

    @Override
    public void printStep() {
        System.out.println("Changing git user to: " + this.name + " <" + this.email + ">");
    }

    @Override
    public void run() throws Exception {
        Commands.git(this.gitDir).setConfig(this.name, this.email);
    }

}
