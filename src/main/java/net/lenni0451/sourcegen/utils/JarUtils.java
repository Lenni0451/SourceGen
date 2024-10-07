package net.lenni0451.sourcegen.utils;

import net.lenni0451.commons.Sneaky;
import net.lenni0451.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipFile;

public class JarUtils {

    public static Map<String, byte[]> read(final File file) throws IOException {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.stream().forEach(entry -> {
                try {
                    entries.put(entry.getName(), IOUtils.readAll(zipFile.getInputStream(entry)));
                } catch (Throwable t) {
                    Sneaky.sneak(t);
                }
            });
        }
        return entries;
    }

    public static void write(final File file, final Map<String, byte[]> entries) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(entry.getKey()));
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }
    }

}
