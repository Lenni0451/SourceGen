package net.lenni0451.sourcegen.targets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lenni0451.sourcegen.Config;

import java.io.File;

@RequiredArgsConstructor
public enum Requirements {

    VINEFLOWER(Config.External.vineflowerJar, """
            VineFlower is not present in the working directory.
            Please download VineFlower and put '%s' into the working directory.""".formatted(Config.External.vineflowerJar)),
    HYTALE_DOWNLOADER(Config.External.hytaleDownloaderExecutable, """
            The Hytale Downloader is not present in the working directory.
            Please download the Hytale Downloader and put '%s' into the working directory.
            Make sure you have authenticated it properly by running it once.""".formatted(Config.External.hytaleDownloaderExecutable)),
    ;

    private final String command;
    @Getter
    private final String message;

    public boolean isPresent() {
        return new File(this.command).isFile();
    }

}
