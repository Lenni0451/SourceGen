package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipSingleFileStep implements GeneratorStep {

    private final File zipFile;
    private final String file;
    private final File targetFile;

    public UnzipSingleFileStep(final File zipFile, final String file, final File targetFile) {
        this.zipFile = zipFile;
        this.file = file;
        this.targetFile = targetFile;
    }

    @Override
    public void printStep() {
        System.out.println("Unzipping " + this.file + " from " + this.zipFile.getName() + " to " + this.targetFile.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        try (ZipFile zf = new ZipFile(this.zipFile)) {
            ZipEntry entry = zf.getEntry(this.file);
            if (entry == null) throw new FileNotFoundException("File " + this.file + " not found in " + this.zipFile.getName());
            try (FileOutputStream fos = new FileOutputStream(this.targetFile)) {
                zf.getInputStream(entry).transferTo(fos);
            }
        }
    }

}
