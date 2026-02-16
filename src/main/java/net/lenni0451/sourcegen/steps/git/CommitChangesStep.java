package net.lenni0451.sourcegen.steps.git;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.util.Date;

@Slf4j
public class CommitChangesStep implements GeneratorStep {

    private final File repoDir;
    private final String message;
    private final Date commitDate;

    public CommitChangesStep(final File repoDir, final String message, final Date commitDate) {
        this.repoDir = repoDir;
        this.message = message;
        this.commitDate = commitDate;
    }

    @Override
    public void printStep() {
        log.info("Committing changes to git repository...");
    }

    @Override
    public void run() throws Exception {
        Commands.Git git = Commands.git(this.repoDir);
        git.addAll();
        git.commit(this.message, this.commitDate);
    }

}
