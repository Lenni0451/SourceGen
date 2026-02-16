package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.io.File;

@Slf4j
public class DownloadStep implements GeneratorStep {

    private final String url;
    private final File output;

    public DownloadStep(final String url, final File output) {
        this.url = url;
        this.output = output;
    }

    @Override
    public void printStep() {
        log.info("Downloading file from " + this.url + "...");
    }

    @Override
    public void run() throws Exception {
        for (int i = 0; i < 5; i++) {
            try {
                NetUtils.download(this.url, this.output);
                break;
            } catch (Throwable t) {
                log.error("Failed to download file from {} (Attempt {}/5): {}", this.url, i + 1, t.getMessage());
                if (i == 4) throw t;
                Thread.sleep(1000);
            }
        }
    }

}
