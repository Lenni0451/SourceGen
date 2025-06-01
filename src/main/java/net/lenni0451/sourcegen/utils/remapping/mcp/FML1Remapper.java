package net.lenni0451.sourcegen.utils.remapping.mcp;

import net.lenni0451.commons.asm.info.MemberDeclaration;
import net.lenni0451.commons.asm.mappings.Mappings;
import net.lenni0451.commons.asm.mappings.loader.DirectMappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.SrgMappingsLoader;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.utils.CsvReader;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

import java.io.File;
import java.util.Map;

public class FML1Remapper extends BaseRemapper {

    public FML1Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) throws Exception {
        File srgConfig = FileUtils.create(mappings, "forge", "fml", "conf", "joined.srg");
        File methodsCsv = FileUtils.create(mappings, "forge", "fml", "conf", "methods.csv");
        File fieldsCsv = FileUtils.create(mappings, "forge", "fml", "conf", "fields.csv");

        Mappings srgMappings = new SrgMappingsLoader(srgConfig).getMappings();
        Mappings out = new Mappings();
        for (Map.Entry<String, String> entry : srgMappings.getClassMappings().entrySet()) {
            out.addClassMapping(entry.getKey(), entry.getValue());
        }
        Map<String, MemberDeclaration> reverseMethodMappings = this.toReverseMappings(srgMappings.getMethodMappings(), MemberDeclaration::fromMethodMapping);
        for (Map<String, String> line : CsvReader.readCsv(methodsCsv, 1, "srg", "to", "side")) {
            if (!"0".equals(line.get("side")) && !"2".equals(line.get("side"))) continue;
            String srg = line.get("srg");
            String to = line.get("to");

            MemberDeclaration declaration = reverseMethodMappings.get(srg);
            if (declaration != null) {
                out.addMethodMapping(declaration.getOwner(), declaration.getName(), declaration.getDescriptor(), to);
            }
        }
        Map<String, MemberDeclaration> reverseFieldMappings = this.toReverseMappings(srgMappings.getFieldMappings(), MemberDeclaration::fromFieldMapping);
        for (Map<String, String> line : CsvReader.readCsv(fieldsCsv, 1, "srg", "to", "side")) {
            if (!"0".equals(line.get("side")) && !"2".equals(line.get("side"))) continue;
            String srg = line.get("srg");
            String to = line.get("to");

            MemberDeclaration declaration = reverseFieldMappings.get(srg);
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
