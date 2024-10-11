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
            //If the repo dir doesn't exist, clone the repo
            git.clone(this.url);
            if (!git.checkout(this.branch)) {
                //If the branch doesn't exist, create it
                git.checkoutOrphan(this.branch);
                git.rmAll();
            }
        } else {
            //The repo dir exists, fetch all changes and checkout the branch
            git.fetchAll();
            if (git.checkout(this.branch)) {
                //Reset all changes in the branch
                git.resetHardHead(this.branch);
            } else {
                //If the branch doesn't exist, create it
                git.checkoutOrphan(this.branch);
                git.rmAll();
            }
        }
    }

}
