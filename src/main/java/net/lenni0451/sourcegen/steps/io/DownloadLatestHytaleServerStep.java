package net.lenni0451.sourcegen.steps.io;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class DownloadLatestHytaleServerStep implements GeneratorStep {

    private final File output;

    @Override
    public void printStep() {
        log.info("Downloading latest Hytale Server...");
    }

    @Override
    public void run() throws Exception {
        Commands.HytaleDownloader.downloadLatestVersion(this.output.getAbsolutePath());
    }


}
