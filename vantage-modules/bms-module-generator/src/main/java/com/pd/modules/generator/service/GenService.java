package com.pd.modules.generator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Get all tables from database
     */
    public List<Map<String, Object>> getDatabaseTables() {
        String sql = "SELECT table_name AS tableName, table_comment AS tableComment, create_time AS createTime " +
                     "FROM information_schema.tables " +
                     "WHERE table_schema = 'PUBLIC' AND table_type = 'BASE TABLE' " +
                     "ORDER BY table_name";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Get table columns
     */
    public List<Map<String, Object>> getTableColumns(String tableName) {
        String sql = "SELECT column_name AS columnName, data_type AS dataType, " +
                     "column_comment AS columnComment, is_nullable AS isNullable " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = ? AND table_schema = 'PUBLIC' " +
                     "ORDER BY ordinal_position";
        return jdbcTemplate.queryForList(sql, tableName.toUpperCase());
    }

    /**
     * Generate code for a single table
     */
    public String generateCode(String tableName) {
        List<Map<String, Object>> columns = getTableColumns(tableName);
        StringBuilder code = new StringBuilder();
        
        // Generate Entity class
        code.append("// Entity: ").append(tableName).append("\n\n");
        code.append("@Data\n");
        code.append("@Entity\n");
        code.append("@Table(name = \"").append(tableName).append("\")\n");
        code.append("public class ").append(tableNameToClassName(tableName)).append(" {\n\n");
        
        for (Map<String, Object> col : columns) {
            code.append("    @Column(name = \"").append(col.get("COLUMN_NAME")).append("\")\n");
            code.append("    private ").append(sqlTypeToJavaType((String) col.get("DATA_TYPE"))).append(" ")
                .append(columnToField((String) col.get("COLUMN_NAME"))).append(";\n\n");
        }
        
        code.append("}\n");
        
        return code.toString();
    }

    /**
     * Batch generate code for multiple tables
     */
    public void batchGenerate(List<String> tables, Map<String, Object> config) {
        for (String table : tables) {
            generateCode(table);
            // Code is generated and saved to project structure
            // In production, this would write files to disk
        }
    }

    /**
     * Download generated code as ZIP
     */
    public void downloadCode(String[] tables, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=generated-code.zip");
        
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (String table : tables) {
                List<Map<String, Object>> columns = getTableColumns(table);
                String className = tableNameToClassName(table);
                
                // Add Entity file
                ZipEntry entityEntry = new ZipEntry("entity/" + className + ".java");
                zos.putNextEntry(entityEntry);
                zos.write(generateEntityCode(table, columns).getBytes());
                zos.closeEntry();
                
                // Add Repository file
                ZipEntry repoEntry = new ZipEntry("repository/" + className + "Repository.java");
                zos.putNextEntry(repoEntry);
                zos.write(generateRepositoryCode(className).getBytes());
                zos.closeEntry();
                
                // Add Service file
                ZipEntry serviceEntry = new ZipEntry("service/" + className + "Service.java");
                zos.putNextEntry(serviceEntry);
                zos.write(generateServiceCode(className).getBytes());
                zos.closeEntry();
                
                // Add Controller file
                ZipEntry controllerEntry = new ZipEntry("controller/" + className + "Controller.java");
                zos.putNextEntry(controllerEntry);
                zos.write(generateControllerCode(className, table).getBytes());
                zos.closeEntry();
            }
        }
    }

    // Helper methods for code generation
    private String generateEntityCode(String tableName, List<Map<String, Object>> columns) {
        String className = tableNameToClassName(tableName);
        StringBuilder sb = new StringBuilder();
        
        sb.append("package com.pd.modules.").append(tableNameToModuleName(tableName)).append(".domain;\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import lombok.Data;\n\n");
        sb.append("@Data\n");
        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(tableName).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");
        
        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    private Long id;\n\n");
        
        for (Map<String, Object> col : columns) {
            String columnName = (String) col.get("COLUMN_NAME");
            if ("id".equalsIgnoreCase(columnName) || "create_time".equalsIgnoreCase(columnName)) continue;
            
            String fieldName = columnToField(columnName);
            String javaType = sqlTypeToJavaType((String) col.get("DATA_TYPE"));
            
            sb.append("    @Column(name = \"").append(columnName).append("\")\n");
            sb.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }
        
        sb.append("}\n");
        return sb.toString();
    }

    private String generateRepositoryCode(String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.pd.modules.").append(className.toLowerCase()).append(".infrastructure.repository;\n\n");
        sb.append("import com.pd.modules.").append(className.toLowerCase()).append(".domain.").append(className).append(";\n");
        sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        sb.append("import org.springframework.stereotype.Repository;\n\n");
        sb.append("@Repository\n");
        sb.append("public interface ").append(className).append("Repository extends JpaRepository<").append(className).append(", Long> {\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateServiceCode(String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.pd.modules.").append(className.toLowerCase()).append(".service;\n\n");
        sb.append("import com.pd.modules.").append(className.toLowerCase()).append(".domain.").append(className).append(";\n");
        sb.append("import com.pd.modules.").append(className.toLowerCase()).append(".infrastructure.repository.").append(className).append("Repository;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.stereotype.Service;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(className).append("Service {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private ").append(className).append("Repository repository;\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateControllerCode(String className, String tableName) {
        StringBuilder sb = new StringBuilder();
        String moduleName = tableNameToModuleName(tableName);
        String basePath = "/api/" + moduleName + "/" + className.toLowerCase();
        
        sb.append("package com.pd.modules.").append(className.toLowerCase()).append(".web;\n\n");
        sb.append("import com.pd.common.core.controller.BaseController;\n");
        sb.append("import com.pd.common.core.domain.AjaxResult;\n");
        sb.append("import com.pd.modules.").append(className.toLowerCase()).append(".domain.").append(className).append(";\n");
        sb.append("import com.pd.modules.").append(className.toLowerCase()).append(".service.").append(className).append("Service;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"").append(basePath).append("\")\n");
        sb.append("public class ").append(className).append("Controller extends BaseController {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private ").append(className).append("Service service;\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String tableNameToClassName(String tableName) {
        String[] parts = tableName.replaceFirst("^(sys_|qrtz_|gen_|ai_)", "").split("_");
        StringBuilder className = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                className.append(Character.toUpperCase(part.charAt(0)))
                         .append(part.substring(1).toLowerCase());
            }
        }
        return className.toString();
    }

    private String tableNameToModuleName(String tableName) {
        if (tableName.startsWith("sys_")) return "system";
        if (tableName.startsWith("qrtz_")) return "quartz";
        if (tableName.startsWith("gen_")) return "generator";
        return "system";
    }

    private String columnToField(String columnName) {
        String[] parts = columnName.split("_");
        StringBuilder fieldName = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            fieldName.append(Character.toUpperCase(parts[i].charAt(0)))
                     .append(parts[i].substring(1).toLowerCase());
        }
        return fieldName.toString();
    }

    private String sqlTypeToJavaType(String sqlType) {
        if (sqlType == null) return "String";
        switch (sqlType.toUpperCase()) {
            case "BIGINT":
            case "INTEGER":
            case "INT":
                return "Long";
            case "BOOLEAN":
            case "BIT":
                return "Boolean";
            case "DATE":
                return "LocalDate";
            case "TIMESTAMP":
            case "DATETIME":
                return "LocalDateTime";
            case "DECIMAL":
            case "NUMERIC":
            case "DOUBLE":
            case "FLOAT":
                return "Double";
            default:
                return "String";
        }
    }
}
