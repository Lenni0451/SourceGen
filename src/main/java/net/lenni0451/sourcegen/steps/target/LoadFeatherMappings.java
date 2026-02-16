package net.lenni0451.sourcegen.steps.target;

import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LoadFeatherMappings extends LoadContextStep<Function<String, String>> {

    private static final Pattern BUILD_PATTERN = Pattern.compile("(.*)\\+build\\.(\\d+)$");
    private static final String PRE_RELEASE_PATTERN = "(\\S+) Pre-Release (\\d+)";

    private final VersionStepProvider stepProvider;

    public LoadFeatherMappings(final VersionStepProvider stepProvider) {
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        log.info("Loading Feather mappings...");
    }

    @Override
    protected Function<String, String> loadContext() throws Exception {
        List<String> rawVersions = NetUtils.getMavenVersions(Config.OnlineResources.getFeatherMappings("maven-metadata.xml"));
        Map<String, MavenVersion> parsedVersions = this.parseVersions(rawVersions);

        return version -> {
            MavenVersion ver = parsedVersions.get(version);
            if (ver == null) {
                Matcher matcher = Pattern.compile(PRE_RELEASE_PATTERN).matcher(version);
                if (!matcher.find()) return null;
                ver = parsedVersions.get(matcher.group(1));
            }
            if (ver == null) return null;
            return ver.url();
        };
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, Function<String, String> context) throws Exception {
        this.stepProvider.provideSteps(steps, context);
    }

    private Map<String, MavenVersion> parseVersions(final List<String> versions) {
        Map<String, MavenVersion> parsedVersions = new HashMap<>();
        for (String version : versions) {
            Matcher matcher = BUILD_PATTERN.matcher(version);
            if (!matcher.find()) throw new IllegalStateException("Invalid version: " + version);

            String minecraftVersion = matcher.group(1);
            int build = Integer.parseInt(matcher.group(2));
            if (!parsedVersions.containsKey(minecraftVersion) || parsedVersions.get(minecraftVersion).build < build) {
                String encodedVersion = UrlEscapers.urlFragmentEscaper().escape(version);
                String url = Config.OnlineResources.getFeatherMappings(encodedVersion + "/feather-" + encodedVersion + "-mergedv2.jar");
                parsedVersions.put(minecraftVersion, new MavenVersion(build, url));
            }
        }
        return parsedVersions;
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> subSteps, final Function<String, String> versionToUrl);
    }

    private record MavenVersion(int build, String url) {
    }

}
