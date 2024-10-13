package net.lenni0451.sourcegen.steps.target;

import com.google.common.net.UrlEscapers;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.NetUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadYarnMappings implements GeneratorStep {

    private static final Pattern BUILD_PATTERN = Pattern.compile("(.*)\\+build\\.(\\d+)$");
    private static final Pattern SMALL_BUILD_PATTERN = Pattern.compile("(.*)\\.(\\d+)$");

    private final VersionStepProvider stepProvider;

    public LoadYarnMappings(final VersionStepProvider stepProvider) {
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Loading Yarn mappings...");
    }

    @Override
    public void run() throws Exception {
        List<String> rawVersions = this.getVersions();
        Map<String, MavenVersion> parsedVersions = this.parseVersions(rawVersions);

        List<GeneratorStep> steps = new ArrayList<>();
        this.stepProvider.provideSteps(steps, version -> Optional.ofNullable(parsedVersions.get(version)).map(MavenVersion::url).orElse(null));
        StepExecutor executor = new StepExecutor(steps);
        executor.run();
    }

    private List<String> getVersions() throws Exception {
        String xml = new String(NetUtils.get(Config.OnlineResources.getYarnMappings("maven-metadata.xml")), StandardCharsets.UTF_8);
        List<String> versions = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
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
            if (!matcher.find()) {
                matcher = SMALL_BUILD_PATTERN.matcher(version);
                if (!matcher.find()) throw new IllegalStateException("Invalid version: " + version);
            }

            String minecraftVersion = matcher.group(1);
            int build = Integer.parseInt(matcher.group(2));
            if (!parsedVersions.containsKey(minecraftVersion) || parsedVersions.get(minecraftVersion).build < build) {
                String encodedVersion = UrlEscapers.urlFragmentEscaper().escape(version);
                String url = Config.OnlineResources.getYarnMappings(encodedVersion + "/yarn-" + encodedVersion + ".jar");
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
