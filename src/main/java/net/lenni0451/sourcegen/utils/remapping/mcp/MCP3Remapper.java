package net.lenni0451.sourcegen.utils.remapping.mcp;

import net.lenni0451.commons.asm.mappings.Mappings;
import net.lenni0451.commons.asm.mappings.loader.MappingsProvider;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.utils.CsvReader;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

import java.io.File;
import java.util.Map;

public class MCP3Remapper extends BaseRemapper {

    public MCP3Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsProvider initProvider(File mappings) throws Exception {
        File classesCsv = FileUtils.create(mappings, "conf", "classes.csv");
        File methodsCsv = FileUtils.create(mappings, "conf", "methods.csv");
        File fieldsCsv = FileUtils.create(mappings, "conf", "fields.csv");

        Mappings out = new Mappings();
        for (Map<String, String> line : CsvReader.readQuotedCsv(classesCsv, 1, "to", "from", "unused", "package", "side")) {
            if (!"0".equals(line.get("side"))) continue;
            String pkg = line.get("package");
            if (!pkg.isEmpty() && !pkg.endsWith("/")) pkg += "/";
            out.addClassMapping(line.get("from"), pkg + line.get("to"));
        }
        for (Map<String, String> line : CsvReader.readQuotedCsv(methodsCsv, 1, "unused", "to", "from", "unused", "signature", "unused", "owner", "unused", "side")) {
            if (!"0".equals(line.get("side"))) continue;
            out.addMethodMapping(line.get("owner"), line.get("from"), line.get("signature"), line.get("to"));
        }
        for (Map<String, String> line : CsvReader.readQuotedCsv(fieldsCsv, 1, "unused", "to", "from", "unused", "signature", "unused", "owner", "unused", "side")) {
            if (!"0".equals(line.get("side"))) continue;
            out.addFieldMapping(line.get("owner"), line.get("from"), line.get("signature"), line.get("to"));
        }
        return MappingsProvider.direct(out);
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
