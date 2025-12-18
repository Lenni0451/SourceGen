package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterateCosmicReachVersions implements GeneratorStep {

    private final VersionType type;
    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepProvider;

    public IterateCosmicReachVersions(final VersionType type, final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        this.type = type;
        this.repoDir = repoDir;
        this.branch = branch;
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for CosmicReach versions (" + this.type.name().toLowerCase() + ")...");
    }

    @Override
    public void run() throws Exception {
        List<CosmicReachVersion> versions = this.loadVersions();
        this.removeBuiltVersions(versions);

        int i = 0;
        ETA eta = new ETA();
        for (CosmicReachVersion version : versions) {
            List<GeneratorStep> steps = new ArrayList<>();
            this.stepProvider.provideSteps(steps, version.id, version.releaseTime, this.type.url(version));
            System.out.println("Running steps for version " + version.id + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + version.id + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private List<CosmicReachVersion> loadVersions() throws IOException {
        GsonObject meta = NetUtils.getJsonObject(Config.OnlineResources.cosmicReachArchive);
        GsonArray versions = meta.getArray("versions");
        List<CosmicReachVersion> cosmicReachVersions = new ArrayList<>();
        for (int i = 0; i < versions.size(); i++) {
            GsonObject version = versions.getObject(i);
            if (Config.Exclusions.cosmicReach.contains(version.getString("id"))) continue;
            GsonObject client = version.getObject("client");
            GsonObject server = version.getObject("server");
            if (client == null && server == null) continue;
            cosmicReachVersions.add(new CosmicReachVersion(
                    version.getString("id"),
                    new Date(version.getLong("releaseTime") * 1000L),
                    client != null ? client.getString("url") : null,
                    server != null ? server.getString("url") : null
            ));
        }
        Collections.reverse(cosmicReachVersions);
        cosmicReachVersions.removeIf(version -> !this.type.matches(version));
        return cosmicReachVersions;
    }

    private void removeBuiltVersions(final List<CosmicReachVersion> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.stream().map(CosmicReachVersion::id).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<CosmicReachVersion> it = versions.iterator();
        while (it.hasNext()) {
            CosmicReachVersion version = it.next();
            it.remove();
            if (version.id().equals(lastBuiltVersion)) break;
        }
    }


    public enum VersionType {
        CLIENT(CosmicReachVersion::hasClient, CosmicReachVersion::clientUrl),
        SERVER(CosmicReachVersion::hasServer, CosmicReachVersion::serverUrl);

        private final Predicate<CosmicReachVersion> predicate;
        private final Function<CosmicReachVersion, String> urlGetter;

        VersionType(final Predicate<CosmicReachVersion> predicate, final Function<CosmicReachVersion, String> urlGetter) {
            this.predicate = predicate;
            this.urlGetter = urlGetter;
        }

        private boolean matches(final CosmicReachVersion version) {
            return this.predicate.test(version);
        }

        private String url(final CosmicReachVersion version) {
            return this.urlGetter.apply(version).replace(" ", "%20");
        }
    }

    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final Date releaseTime, final String url) throws Exception;
    }

    private record CosmicReachVersion(String id, Date releaseTime, @Nullable String clientUrl, @Nullable String serverUrl) {
        public boolean hasClient() {
            return this.clientUrl != null;
        }

        public boolean hasServer() {
            return this.serverUrl != null;
        }
    }

}
