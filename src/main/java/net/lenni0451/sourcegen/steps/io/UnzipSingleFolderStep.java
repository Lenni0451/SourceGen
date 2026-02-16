package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class UnzipSingleFolderStep implements GeneratorStep {

    private final File zipFile;
    private final String folder;
    private final File targetFolder;

    public UnzipSingleFolderStep(final File zipFile, final String folder, final File targetFolder) {
        this.zipFile = zipFile;
        this.folder = folder;
        this.targetFolder = targetFolder;
    }

    @Override
    public void printStep() {
        log.info("Unzipping {} from {} to {}...", this.folder, this.zipFile.getName(), this.targetFolder.getName());
    }

    @Override
    public void run() throws Exception {
        try (ZipFile zf = new ZipFile(this.zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                if (entry.getName().startsWith(this.folder + "/")) {
                    final String newName = entry.getName().substring(this.folder.length() + 1);
                    File entryFile = new File(this.targetFolder, newName);
                    entryFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        zf.getInputStream(entry).transferTo(fos);
                    }
                }
            }
        }
    }

}
