package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        System.out.println("Unzipping " + this.folder + " from " + this.zipFile.getName() + " to " + this.targetFolder.getName() + "...");
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
