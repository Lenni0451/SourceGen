package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

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
    public void run() throws Exception {
        Commands.Git git = Commands.git(this.repoDir);
        if (!this.repoDir.exists()) {
            git.clone(this.url); //If the repository does not exist yet, clone it
            git.checkout(this.branch); //Checkout the specified branch
        } else {
            git.fetchAll(); //Fetch all changes
            git.checkout(this.branch); //Checkout the specified branch
            git.resetHardHead(this.branch); //Reset the repository to the latest commit
        }
    }

}
