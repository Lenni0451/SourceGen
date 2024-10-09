package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

public class ChangeGitUserStep implements GeneratorStep {

    private final String name;
    private final String email;

    public ChangeGitUserStep(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public void printStep() {
        System.out.println("Changing git user to: " + this.name + " <" + this.email + ">");
    }

    @Override
    public void run() throws Exception {
        Commands.Git.setConfig(this.name, this.email);
    }

}
