package net.lenni0451.sourcegen.utils.remapping.mcp;

import net.lenni0451.commons.asm.info.MemberDeclaration;
import net.lenni0451.commons.asm.mappings.Mappings;
import net.lenni0451.commons.asm.mappings.loader.DirectMappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.RetroguardMappingsLoader;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.utils.CsvReader;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

import java.io.File;
import java.util.Map;

public class MCP2Remapper extends BaseRemapper {

    public MCP2Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) throws Exception {
        File retroguardConfig = FileUtils.create(mappings, "conf", "minecraft.rgs");
        File methodsCsv = FileUtils.create(mappings, "conf", "methods.csv");
        File fieldsCsv = FileUtils.create(mappings, "conf", "fields.csv");

        Mappings retroguardMappings = new RetroguardMappingsLoader(retroguardConfig).getMappings();
        Mappings out = new Mappings();
        for (Map.Entry<String, String> entry : retroguardMappings.getClassMappings().entrySet()) {
            out.addClassMapping(entry.getKey(), entry.getValue());
        }
        Map<String, MemberDeclaration> reverseMethodMappings = this.toReverseMappings(retroguardMappings.getMethodMappings(), MemberDeclaration::fromMethodMapping);
        for (Map<String, String> line : CsvReader.readCsv(methodsCsv, 4, "unused", "from", "unused", "unused", "to")) {
            String from = line.get("from");
            String to = line.get("to");
            if (from == null || to == null) continue;

            MemberDeclaration declaration = reverseMethodMappings.get(from);
            if (declaration != null) {
                out.addMethodMapping(declaration.getOwner(), declaration.getName(), declaration.getDescriptor(), to);
            }
        }
        Map<String, MemberDeclaration> reverseFieldMappings = this.toReverseMappings(retroguardMappings.getFieldMappings(), MemberDeclaration::fromFieldMapping);
        for (Map<String, String> line : CsvReader.readCsv(fieldsCsv, 3, "unused", "unused", "from", "unused", "unused", "unused", "to")) {
            String from = line.get("from");
            String to = line.get("to");
            if (from == null || to == null) continue;

            MemberDeclaration declaration = reverseFieldMappings.get(from);
            if (declaration != null) {
                out.addFieldMapping(declaration.getOwner(), declaration.getName(), declaration.getDescriptor(), to);
            }
        }
        return this.load(new DirectMappingsLoader(out));
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
