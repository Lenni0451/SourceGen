package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.collections.Lists;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class IterateHytaleServerVersions extends IterateVersionsStep<String> {

    private final VersionStepProvider stepProvider;

    public IterateHytaleServerVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        super(repoDir, branch);
        this.stepProvider = stepProvider;
    }

    @Override
    protected String getName() {
        return "Hytale Server";
    }

    @Override
    protected Collection<String> loadVersions() throws IOException {
        //Maybe at some point there will be more than just the latest version
        return Lists.arrayList(Commands.HytaleDownloader.getLatestVersion());
    }

    @Override
    protected void processVersions(Collection<String> versions) throws Exception {
        super.removeBuiltVersions(versions);
    }

    @Override
    protected String getVersionId(String version) {
        return version;
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, String version) throws Exception {
        this.stepProvider.provideSteps(steps, version);
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName) throws Exception;
    }

}
