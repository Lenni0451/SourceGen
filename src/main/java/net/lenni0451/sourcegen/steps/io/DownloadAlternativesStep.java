package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DownloadAlternativesStep implements GeneratorStep {

    private final String[] urls;
    private final File output;

    public DownloadAlternativesStep(final String[] urls, final File output) {
        this.urls = urls;
        this.output = output;
    }

    @Override
    public void printStep() {
        log.info("Downloading file from alternatives...");
    }

    @Override
    public void run() throws Exception {
        List<Throwable> errors = new ArrayList<>();
        for (String url : this.urls) {
            try {
                NetUtils.download(url, this.output);
                log.info("Downloaded file from {}", url);
                return;
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        IOException exception = new IOException("Failed to download file from alternatives");
        errors.forEach(exception::addSuppressed);
        throw exception;
    }

}
