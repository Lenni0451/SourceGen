package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;

public class PrepareRepoStep implements GeneratorStep {

    private final String url;
    private final File repoDir;
    private final String branch;

    public PrepareRepoStep(final File repoDir, final String url, final String branch) {
        this.url = url;
        this.repoDir = repoDir;
        this.branch = branch;
    }

    @Override
    public void printStep() {
        System.out.println("Preparing repository...");
    }

    @Override
    public void run() throws IOException {
        Commands.Git git = Commands.git(this.repoDir);
        if (!this.repoDir.exists()) {
            //If the repository does not exist yet, clone it
            git.clone(this.url);
        } else {
            //Fetch all changes
            git.fetchAll();
        }
        git.checkout(this.branch); //Checkout the specified branch
    }

}
