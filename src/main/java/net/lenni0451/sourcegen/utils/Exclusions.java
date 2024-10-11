package net.lenni0451.sourcegen.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Exclusions {

    private final List<String> exclusions = new ArrayList<>();

    public Exclusions(final File file) {
        try {
            this.exclusions.addAll(Files.readAllLines(file.toPath()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean isExcluded(final String name) {
        return this.exclusions.contains(name);
    }

}
