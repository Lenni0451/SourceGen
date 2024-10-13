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
        NetUtils.download(this.url, this.output);
    }

}
