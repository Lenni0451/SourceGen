package net.lenni0451.sourcegen.steps.target;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
public abstract class IterateVersionsStep<V> implements GeneratorStep {

    private final File repoDir;
    private final String branch;

    public IterateVersionsStep(final File repoDir, final String branch) {
        this.repoDir = repoDir;
        this.branch = branch;
    }

    protected abstract String getName();

    protected abstract Collection<V> loadVersions() throws Exception;

    protected abstract void processVersions(final Collection<V> versions) throws Exception;

    protected abstract String getVersionId(final V version);

    protected abstract void provideSteps(final List<GeneratorStep> steps, final V version) throws Exception;

    @Override
    public final void printStep() {
        log.info("Searching for {} versions...", this.getName());
    }

    @Override
    public final void run() throws Exception {
        Collection<V> versions = this.loadVersions();
        this.processVersions(versions);

        int i = 0;
        ETA eta = new ETA();
        for (V version : versions) {
            String versionName = this.getVersionId(version);
            List<GeneratorStep> steps = new ArrayList<>();
            this.provideSteps(steps, version);

            log.info("Running steps for version {} ({}/{}{})...", versionName, ++i, versions.size(), eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            log.info("Finished steps for version {} in {}", versionName, ETA.format(eta.getLastDuration()));
        }
    }

    protected final void removeBuiltVersions(final Collection<V> versions) throws Exception {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = false;
        for (V version : versions) {
            if (this.getVersionId(version).equals(lastBuiltVersion)) {
                hasVersion = true;
                break;
            }
        }
        if (!hasVersion) return;

        Iterator<V> it = versions.iterator();
        while (it.hasNext()) {
            V version = it.next();
            String versionName = this.getVersionId(version);
            it.remove();
            if (versionName.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }

}
