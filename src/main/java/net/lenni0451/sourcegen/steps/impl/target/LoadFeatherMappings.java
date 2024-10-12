package net.lenni0451.sourcegen.steps.impl.target;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.NetUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
        Map<String, Integer> splitVersions = this.splitVersions(rawVersions);

        List<GeneratorStep> steps = new ArrayList<>();
        this.stepProvider.provideSteps(steps, version -> this.versionToUrl(splitVersions, version));
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

    private Map<String, Integer> splitVersions(final List<String> versions) {
        Map<String, Integer> splitVersions = new HashMap<>();
        for (String version : versions) {
            Matcher matcher = BUILD_PATTERN.matcher(version);
            if (!matcher.find()) {
                throw new IllegalStateException("Invalid version: " + version);
            }
            Integer build = splitVersions.get(matcher.group(1));
            if (build == null || build < Integer.parseInt(matcher.group(2))) {
                splitVersions.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            }
        }
        return splitVersions;
    }

    @Nullable
    private String versionToUrl(final Map<String, Integer> splitVersions, String version) {
        if (version.matches(PRE_RELEASE_PATTERN)) version = version.replaceAll(PRE_RELEASE_PATTERN, "$1-pre$2");
        Integer build = splitVersions.get(version);
        if (build == null) {
            version = version + "-client";
            build = splitVersions.get(version);
        }
        if (build == null) return null;
        return this.toUrl(version, build);
    }

    private String toUrl(final String version, final int build) {
        String buildName = version + "+build." + build;
        return "https://maven.lenni0451.net/cache/net/ornithemc/feather/" + buildName + "/feather-" + buildName + "-mergedv2.jar";
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> subSteps, final Function<String, String> versionToUrl);
    }

}
