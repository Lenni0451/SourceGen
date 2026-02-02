package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;

public class IterateMinecraftVersions extends IterateVersionsStep<GsonObject> {

    private final VersionRange versionRange;
    private final Predicate<GsonObject> removeVersionIf;
    private final boolean ignoreExclusions;
    private final VersionStepProvider stepProvider;

    public IterateMinecraftVersions(final File repoDir, final String branch, final VersionRange versionRange, final Predicate<GsonObject> removeVersionIf, final boolean ignoreExclusions, final VersionStepProvider stepProvider) {
        super(repoDir, branch);
        this.versionRange = versionRange;
        this.removeVersionIf = removeVersionIf;
        this.ignoreExclusions = ignoreExclusions;
        this.stepProvider = stepProvider;
    }

    @Override
    protected String getName() {
        return "Minecraft";
    }

    @Override
    protected Collection<GsonObject> loadVersions() throws Exception {
        GsonObject meta = NetUtils.getJsonObject(Config.OnlineResources.minecraftVersionManifest);
        GsonArray versions = meta.getArray("versions");
        Map<OffsetDateTime, GsonObject> sortedVersions = new TreeMap<>();
        for (int i = 0; i < versions.size(); i++) {
            GsonObject version = versions.getObject(i);
            if (!this.ignoreExclusions && Config.Exclusions.minecraft.contains(version.getString("id"))) continue;

            String time = version.getString("releaseTime");
            sortedVersions.put(OffsetDateTime.parse(time), version);
        }
        return sortedVersions.values();
    }

    @Override
    protected void processVersions(Collection<GsonObject> versions) throws Exception {
        this.filterVersionRange(versions);
        super.removeBuiltVersions(versions);
        versions.removeIf(this.removeVersionIf);
        this.resolveVersionManifest(versions);
    }

    @Override
    protected String getVersionId(GsonObject version) {
        return version.getString("id");
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, GsonObject version) throws Exception {
        String versionName = version.getString("id");
        GsonObject versionManifest = version.getObject("manifest");
        GsonObject downloads = versionManifest.getObject("downloads");
        this.stepProvider.provideSteps(steps, versionName, OffsetDateTime.parse(version.getString("releaseTime")), downloads.getObject("client").getString("url"), downloads.optObject("client_mappings").map(o -> o.getString("url")).orElse(null));
    }

    private void filterVersionRange(final Collection<GsonObject> versions) {
        if (this.versionRange.minVersion != null) {
            Iterator<GsonObject> it = versions.iterator();
            while (it.hasNext()) {
                GsonObject version = it.next();
                String versionName = version.getString("id");
                if (versionName.equals(this.versionRange.minVersion)) break;
                it.remove();
            }
        }
        if (this.versionRange.maxVersion != null) {
            Iterator<GsonObject> it = versions.iterator();
            boolean remove = false;
            while (it.hasNext()) {
                GsonObject version = it.next();
                String versionName = version.getString("id");
                if (remove) it.remove();
                else if (versionName.equals(this.versionRange.maxVersion)) remove = true;
            }
        }
    }

    private void resolveVersionManifest(final Collection<GsonObject> versions) throws IOException {
        for (GsonObject version : versions) {
            String url = version.getString("url");
            GsonObject versionManifest = NetUtils.getJsonObject(url);
            version.add("manifest", versionManifest);
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final String clientUrl, @Nullable final String clientMappingsUrl) throws Exception;
    }

    public record VersionRange(@Nullable String minVersion, @Nullable String maxVersion) {
    }

}
