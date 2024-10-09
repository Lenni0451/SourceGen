package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.util.Date;

public class CommitChangesStep implements GeneratorStep {

    private final File gitDir;
    private final String message;
    private final Date commitDate;

    public CommitChangesStep(final File gitDir, final String message, final Date commitDate) {
        this.gitDir = gitDir;
        this.message = message;
        this.commitDate = commitDate;
    }

    @Override
    public void printStep() {
        System.out.println("Committing changes to git repository...");
    }

    @Override
    public void run() throws Exception {
        Commands.Git git = Commands.git(this.gitDir);
        git.addAll();
        git.commit(this.message, this.commitDate);
    }

}
