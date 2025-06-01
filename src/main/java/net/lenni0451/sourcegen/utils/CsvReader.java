package net.lenni0451.sourcegen.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReader {

    public static List<Map<String, String>> readCsv(final File csvFile, final int headerSize, final String... columns) throws IOException {
        return read(line -> line.split(",", -1), csvFile, headerSize, columns);
    }

    public static List<Map<String, String>> readQuotedCsv(final File csvFile, final int headerSize, final String... columns) throws IOException {
        return read(line -> {
            List<String> parts = new ArrayList<>();
            char[] chars = line.toCharArray();
            StringBuilder currentPart = new StringBuilder();
            boolean inQuotes = false;
            for (char c : chars) {
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    parts.add(currentPart.toString().trim());
                    currentPart.setLength(0);
                } else {
                    currentPart.append(c);
                }
            }
            if (!currentPart.isEmpty()) parts.add(currentPart.toString().trim());
            return parts.toArray(new String[0]);
        }, csvFile, headerSize, columns);
    }

    private static List<Map<String, String>> read(final LineSplitter splitter, final File csvFile, final int headerSize, final String... columns) throws IOException {
        List<String> lines = Files.readAllLines(csvFile.toPath());
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = headerSize; i < lines.size(); i++) {
            String[] values = splitter.split(lines.get(i));
            Map<String, String> entry = new HashMap<>();
            for (int c = 0; c < Math.min(values.length, columns.length); c++) {
                entry.put(columns[c], values[c].trim());
            }
            result.add(entry);
        }
        return result;
    }


    private interface LineSplitter {
        String[] split(String line);
    }

}
