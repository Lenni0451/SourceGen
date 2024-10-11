package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipStep implements GeneratorStep {

    private final File zipFile;
    private final File targetDir;

    public UnzipStep(final File zipFile, final File targetDir) {
        this.zipFile = zipFile;
        this.targetDir = targetDir;
    }

    @Override
    public void printStep() {
        System.out.println("Unzipping " + this.zipFile.getName() + " to " + this.targetDir.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        try (ZipFile zf = new ZipFile(this.zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                File entryFile = new File(this.targetDir, entry.getName());
                entryFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    zf.getInputStream(entry).transferTo(fos);
                }
            }
        }
    }

}
