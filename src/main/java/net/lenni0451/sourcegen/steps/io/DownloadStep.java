package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.io.File;

public class DownloadStep implements GeneratorStep {

    private final String url;
    private final File output;

    public DownloadStep(final String url, final File output) {
        this.url = url;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Downloading file from " + this.url + "...");
    }

    @Override
    public void run() throws Exception {
        for (int i = 0; i < 5; i++) {
            try {
                NetUtils.download(this.url, this.output);
                break;
            } catch (Throwable t) {
                System.out.println("Failed to download file from " + this.url + " (Attempt " + (i + 1) + "/5): " + t.getMessage());
                if (i == 4) throw t;
                Thread.sleep(1000);
            }
        }
    }

}
