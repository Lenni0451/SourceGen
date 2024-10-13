package net.lenni0451.sourcegen.steps.impl.target;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.NetUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadFeatherMappings implements GeneratorStep {

    private static final String META_URL = "https://maven.lenni0451.net/cache/net/ornithemc/feather/maven-metadata.xml";
    private static final Pattern BUILD_PATTERN = Pattern.compile("(.*)\\+build\\.(\\d+)$");
    private static final String PRE_RELEASE_PATTERN = "([^\\s]+) Pre-Release (\\d+)";

    private final VersionStepProvider stepProvider;

    public LoadFeatherMappings(final VersionStepProvider stepProvider) {
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Loading Feather mappings...");
    }

    @Override
    public void run() throws Exception {
        List<String> rawVersions = this.getVersions();
        Map<String, MavenVersion> parsedVersions = this.parseVersions(rawVersions);

        List<GeneratorStep> steps = new ArrayList<>();
        this.stepProvider.provideSteps(steps, version -> {
            MavenVersion ver = parsedVersions.get(version);
            if (ver == null) {
                Matcher matcher = Pattern.compile(PRE_RELEASE_PATTERN).matcher(version);
                if (!matcher.find()) return null;
                ver = parsedVersions.get(matcher.group(1));
            }
            if (ver == null) return null;
            return ver.url();
        });
        StepExecutor executor = new StepExecutor(steps);
        executor.run();
    }

    private List<String> getVersions() throws Exception {
        String xml = new String(NetUtils.get(META_URL), StandardCharsets.UTF_8);
        List<String> versions = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList versionNodes = document.getElementsByTagName("version");
        for (int i = 0; i < versionNodes.getLength(); i++) {
            versions.add(versionNodes.item(i).getTextContent());
        }
        return versions;
    }

    private Map<String, MavenVersion> parseVersions(final List<String> versions) {
        Map<String, MavenVersion> parsedVersions = new HashMap<>();
        for (String version : versions) {
            Matcher matcher = BUILD_PATTERN.matcher(version);
            if (!matcher.find()) throw new IllegalStateException("Invalid version: " + version);

            String minecraftVersion = matcher.group(1);
            int build = Integer.parseInt(matcher.group(2));
            if (!parsedVersions.containsKey(minecraftVersion) || parsedVersions.get(minecraftVersion).build < build) {
                String encodedVersion = URLEncoder.encode(version, StandardCharsets.UTF_8);
                String url = "https://maven.lenni0451.net/cache/net/ornithemc/feather/" + encodedVersion + "/feather-" + encodedVersion + "-mergedv2.jar";
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
