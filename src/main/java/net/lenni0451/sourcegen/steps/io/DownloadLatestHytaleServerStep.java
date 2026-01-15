package net.lenni0451.sourcegen.steps.io;

import lombok.RequiredArgsConstructor;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

@RequiredArgsConstructor
public class DownloadLatestHytaleServerStep implements GeneratorStep {

    private final File output;

    @Override
    public void printStep() {
        System.out.println("Downloading latest Hytale Server...");
    }

    @Override
    public void run() throws Exception {
        Commands.HytaleDownloader.downloadLatestVersion(this.output.getAbsolutePath());
    }


}
