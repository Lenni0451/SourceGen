package net.lenni0451.sourcegen.steps.target;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.collections.Lists;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class IterateHytaleServerVersions implements GeneratorStep {

    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepProvider;

    @Override
    public void printStep() {
        System.out.println("Searching for Hytale Server versions...");
    }

    @Override
    public void run() throws Exception {
        List<String> versions = this.loadVersions();
        this.removeBuiltVersions(versions);

        int i = 0;
        ETA eta = new ETA();
        for (String version : versions) {
            List<GeneratorStep> steps = new ArrayList<>();
            this.stepProvider.provideSteps(steps, version);
            System.out.println("Running steps for version " + version + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + version + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private List<String> loadVersions() throws IOException {
        //Maybe at some point there will be more than just the latest version
        return Lists.arrayList(Commands.HytaleDownloader.getLatestVersion());
    }

    private void removeBuiltVersions(final List<String> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<String> it = versions.iterator();
        while (it.hasNext()) {
            String version = it.next();
            it.remove();
            if (version.equals(lastBuiltVersion)) break;
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName) throws Exception;
    }

}
