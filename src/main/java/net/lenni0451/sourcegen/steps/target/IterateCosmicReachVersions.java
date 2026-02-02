package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterateCosmicReachVersions extends IterateVersionsStep<IterateCosmicReachVersions.CosmicReachVersion> {

    private final VersionType type;
    private final VersionStepProvider stepProvider;

    public IterateCosmicReachVersions(final VersionType type, final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        super(repoDir, branch);
        this.type = type;
        this.stepProvider = stepProvider;
    }

    @Override
    protected String getName() {
        return "CosmicReach (" + this.type.name().toLowerCase() + ")";
    }

    @Override
    protected Collection<CosmicReachVersion> loadVersions() throws IOException {
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

    @Override
    protected void processVersions(Collection<CosmicReachVersion> versions) throws Exception {
        super.removeBuiltVersions(versions);
    }

    @Override
    protected String getVersionId(CosmicReachVersion version) {
        return version.id();
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, CosmicReachVersion version) throws Exception {
        this.stepProvider.provideSteps(steps, version.id, version.releaseTime, this.type.url(version));
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

    public record CosmicReachVersion(String id, Date releaseTime, @Nullable String clientUrl, @Nullable String serverUrl) {
        public boolean hasClient() {
            return this.clientUrl != null;
        }

        public boolean hasServer() {
            return this.serverUrl != null;
        }
    }

}
