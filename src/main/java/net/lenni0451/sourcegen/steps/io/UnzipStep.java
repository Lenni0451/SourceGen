package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class UnzipStep implements GeneratorStep {

    private final File zipFile;
    private final File targetDir;

    public UnzipStep(final File zipFile, final File targetDir) {
        this.zipFile = zipFile;
        this.targetDir = targetDir;
    }

    @Override
    public void printStep() {
        log.info("Unzipping {} to {}...", this.zipFile.getName(), this.targetDir.getName());
    }

    @Override
    public void run() throws Exception {
        try (ZipFile zf = new ZipFile(this.zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                File entryFile = new File(this.targetDir, entry.getName().replace('\\', '/'));
                entryFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    zf.getInputStream(entry).transferTo(fos);
                }
            }
        }
    }

}
