package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IterateBedrockVersions implements GeneratorStep {

    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepProvider;

    public IterateBedrockVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        this.repoDir = repoDir;
        this.branch = branch;
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for Bedrock versions...");
    }

    @Override
    public void run() throws Exception {
        List<BedrockVersion> versions = this.loadVersions();
        this.removeBuiltVersions(versions);

        int i = 0;
        ETA eta = new ETA();
        for (BedrockVersion version : versions) {
            List<GeneratorStep> steps = new ArrayList<>();
            this.stepProvider.provideSteps(steps, version.id, version.name);
            System.out.println("Running steps for version " + version.name + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + version.name + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private List<BedrockVersion> loadVersions() throws IOException {
        final JSONArray versions = NetUtils.getJsonArray(Config.OnlineResources.bedrockVersions);
        final List<BedrockVersion> bedrockVersions = new ArrayList<>();
        for (int i = 0; i < versions.length(); i++) {
            final JSONArray version = versions.getJSONArray(i);
            if (Config.Exclusions.bedrock.contains(version.getString(0))) continue;
            bedrockVersions.add(new BedrockVersion(
                    version.getString(0),
                    version.getString(1),
                    version.getInt(2)
            ));
        }
        bedrockVersions.removeIf(version -> version.type != 0);
        return bedrockVersions;
    }

    private void removeBuiltVersions(final List<BedrockVersion> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.stream().map(BedrockVersion::name).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<BedrockVersion> it = versions.iterator();
        while (it.hasNext()) {
            BedrockVersion version = it.next();
            it.remove();
            if (version.id().equals(lastBuiltVersion)) break;
        }
    }

    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionId, final String versionName) throws Exception;
    }

    private record BedrockVersion(String name, String id, int type) {
    }

}
