package com.pd.modules.generator.service;

import com.pd.modules.generator.api.dto.CreateTableRequest;
import com.pd.modules.generator.api.dto.CreateTableRequest.ColumnDefinition;
import com.pd.modules.system.datasource.domain.SysDatasource;
import com.pd.modules.system.datasource.infrastructure.repository.SysDatasourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenService {

    private static final Logger log = LoggerFactory.getLogger(GenService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysDatasourceRepository datasourceRepository;

    private JdbcTemplate resolveJdbcTemplate(String datasourceKey) {
        if (datasourceKey == null || datasourceKey.isEmpty() || "master".equals(datasourceKey)) {
            return jdbcTemplate;
        }
        Optional<SysDatasource> dsOpt = datasourceRepository.findByDatasourceKey(datasourceKey);
        if (dsOpt.isEmpty()) {
            log.warn("Datasource {} not found, falling back to default", datasourceKey);
            return jdbcTemplate;
        }
        SysDatasource ds = dsOpt.get();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(ds.getDriverClass());
        dataSource.setUrl(ds.getUrl());
        dataSource.setUsername(ds.getUsername());
        dataSource.setPassword(ds.getPassword());
        return new JdbcTemplate(dataSource);
    }

    private static final Set<String> H2_INTERNAL_SCHEMAS = new HashSet<>(Arrays.asList(
        "INFORMATION_SCHEMA", "SYSTEM_SCHEMA", "PG_CATALOG"
    ));

    private static final Set<String> H2_INTERNAL_TABLE_NAMES = new HashSet<>(Arrays.asList(
        "_ROWS"
    ));

    private boolean isH2InternalTable(String schema, String tableName) {
        if (tableName == null) return true;
        if (schema != null && H2_INTERNAL_SCHEMAS.contains(schema.toUpperCase())) return true;
        String upper = tableName.toUpperCase();
        if (H2_INTERNAL_TABLE_NAMES.contains(upper)) return true;
        if (upper.startsWith("SYSTEM_")) return true;
        if (upper.startsWith("_")) return true;
        if (upper.contains("_INDEX_")) return true;
        return false;
    }

    public List<Map<String, Object>> getDatabaseTables(String datasourceKey) {
        JdbcTemplate tmpl = resolveJdbcTemplate(datasourceKey);
        return tmpl.execute((Connection conn) -> {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            List<Map<String, Object>> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName == null) continue;
                if (isH2InternalTable(rs.getString("TABLE_SCHEM"), tableName)) continue;
                String remarks = rs.getString("REMARKS");
                Map<String, Object> map = new HashMap<>();
                map.put("tableName", tableName);
                map.put("tableComment", remarks != null && !remarks.isEmpty() ? remarks : tableName);
                tables.add(map);
            }
            tables.sort(Comparator.comparing(m -> (String) m.get("tableName")));
            return tables;
        });
    }

    public void cloneTable(String sourceTableName, String newTableName, String tableComment, String datasourceKey) {
        List<Map<String, Object>> columns = getTableColumns(sourceTableName, datasourceKey);
        if (columns.isEmpty()) {
            throw new RuntimeException("Source table '" + sourceTableName + "' has no columns or does not exist");
        }
        JdbcTemplate tmpl = resolveJdbcTemplate(datasourceKey);
        String tblName = newTableName.toUpperCase();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tblName).append(" (");
        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            String dataType = (String) col.get("DATA_TYPE");
            String nullable = (String) col.get("isNullable");
            sql.append(colName).append(" ").append(dataType);
            if ("0".equals(nullable)) {
                sql.append(" NOT NULL");
            }
            sql.append(", ");
        }
        String ddl = sql.substring(0, sql.length() - 2) + ")";
        tmpl.execute(ddl);
        if (tableComment != null && !tableComment.isEmpty()) {
            try {
                tmpl.execute("COMMENT ON TABLE " + tblName + " IS '" + tableComment + "'");
            } catch (Exception ignored) {}
        }
        // Copy column comments
        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            String comment = (String) col.get("columnComment");
            if (comment != null && !comment.isEmpty()) {
                try {
                    tmpl.execute("COMMENT ON COLUMN " + tblName + "." + colName + " IS '" + comment + "'");
                } catch (Exception ignored) {}
            }
        }
    }

    public void createTable(CreateTableRequest request) {
        JdbcTemplate tmpl = resolveJdbcTemplate(request.getDatasourceKey());
        String tableName = request.getTableName().toUpperCase();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (");
        boolean hasId = request.getColumns() != null &&
            request.getColumns().stream().anyMatch(c -> "id".equalsIgnoreCase(c.getColumnName()));
        if (!hasId) {
            sql.append("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
        }
        if (request.getColumns() != null) {
            for (ColumnDefinition col : request.getColumns()) {
                String colName = col.getColumnName().toUpperCase();
                String colType = mapColumnType(col);
                sql.append(colName).append(" ").append(colType);
                if (!col.isNullable()) {
                    sql.append(" NOT NULL");
                }
                if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
                    sql.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
                }
                sql.append(", ");
            }
        }
        String ddl = sql.substring(0, sql.length() - 2) + ")";
        tmpl.execute(ddl);
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            try {
                tmpl.execute("COMMENT ON TABLE " + tableName + " IS '" + request.getTableComment() + "'");
            } catch (Exception ignored) {}
        }
    }

    private String mapColumnType(ColumnDefinition col) {
        String type = col.getColumnType().toUpperCase();
        int length = col.getColumnLength() != null && col.getColumnLength() > 0 ? col.getColumnLength() : 255;
        switch (type) {
            case "VARCHAR": return "VARCHAR(" + length + ")";
            case "INTEGER": case "INT": return "INTEGER";
            case "BIGINT": return "BIGINT";
            case "BOOLEAN": case "BIT": return "BOOLEAN";
            case "DECIMAL": return "DECIMAL(18,2)";
            case "DOUBLE": case "FLOAT": return "DOUBLE";
            case "DATE": return "DATE";
            case "TIMESTAMP": case "DATETIME": return "TIMESTAMP";
            case "TEXT": return "TEXT";
            case "CLOB": return "CLOB";
            case "BLOB": return "BLOB";
            default: return "VARCHAR(" + length + ")";
        }
    }

    public List<Map<String, Object>> getTableColumns(String tableName, String datasourceKey) {
        JdbcTemplate tmpl = resolveJdbcTemplate(datasourceKey);
        return tmpl.execute((Connection conn) -> {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), "%");
            List<Map<String, Object>> columns = new ArrayList<>();
            while (rs.next()) {
                addColMeta(rs, columns);
            }
            if (columns.isEmpty()) {
                rs = metaData.getColumns(null, null, tableName, "%");
                while (rs.next()) {
                    addColMeta(rs, columns);
                }
            }
            return columns;
        });
    }

    private void addColMeta(ResultSet rs, List<Map<String, Object>> columns) throws java.sql.SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
        map.put("DATA_TYPE", rs.getString("TYPE_NAME"));
        map.put("columnComment", rs.getString("REMARKS"));
        map.put("isNullable", "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")) ? "1" : "0");
        columns.add(map);
    }

    public Map<String, String> generateModuleFiles(String tableName, String tableComment, String datasourceKey,
                                                     String moduleName, String packageName, String author) {
        List<Map<String, Object>> columns = getTableColumns(tableName, datasourceKey);
        String className = tableNameToClassName(tableName);
        String modPath = moduleName;
        String basePkg = packageName + "." + modPath;
        String comment = (tableComment != null && !tableComment.isEmpty()) ? tableComment : className;
        String apiPkg = basePkg + ".api";
        String dtoPkg = basePkg + ".api.dto";
        String entityPkg = basePkg + ".domain";
        String repoPkg = basePkg + ".infrastructure.repository";
        String serviceImplPkg = basePkg + ".service.impl";

        Map<String, String> files = new LinkedHashMap<>();
        files.put("package-info.java",
            "@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)\n" +
            "package " + basePkg + ";\n");
        files.put("api/dto/" + className + "Dto.java", generateDto(className, dtoPkg, columns));
        files.put("api/I" + className + "Service.java", generateApiInterface(className, apiPkg, dtoPkg));
        files.put("domain/" + className + ".java", generateEntity(className, entityPkg, tableName, columns));
        files.put("infrastructure/repository/" + className + "Repository.java", generateRepository(className, repoPkg, entityPkg));
        files.put("service/impl/" + className + "ServiceImpl.java", generateServiceImpl(className, serviceImplPkg, apiPkg, dtoPkg, entityPkg, repoPkg));
        files.put("ui/" + className + "List.jsx", generateUiPage(className, modPath, columns, comment));
        files.put("menu.sql", generateMenuSql(className, comment, modPath));
        return files;
    }

    public Map<String, String> previewModule(String tableName, String tableComment, String datasourceKey,
                                              String moduleName, String packageName, String author) {
        return generateModuleFiles(tableName, tableComment, datasourceKey, moduleName, packageName, author);
    }

    public void downloadModule(String tableName, String tableComment, String datasourceKey,
                                String moduleName, String packageName, String author,
                                HttpServletResponse response) throws IOException {
        Map<String, String> files = generateModuleFiles(tableName, tableComment, datasourceKey, moduleName, packageName, author);
        String modPath = moduleName;

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + modPath + "-module.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String key = entry.getKey();
                String path = key.startsWith("ui/") || "menu.sql".equals(key) ? key : modPath + "/" + key;
                zos.putNextEntry(new ZipEntry(path));
                zos.write(entry.getValue().getBytes());
                zos.closeEntry();
            }
        }
    }

    private String generateEntity(String className, String pkg, String tableName, List<Map<String, Object>> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.time.LocalDateTime;\n");
        sb.append("import java.time.LocalDate;\n\n");
        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(tableName).append("\")\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" {\n\n");

        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    @Column(name = \"id\")\n");
        sb.append("    private Long id;\n\n");

        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equalsIgnoreCase(colName)) continue;
            String fieldName = columnToField(colName);
            String javaType = sqlTypeToJavaType((String) col.get("DATA_TYPE"));
            sb.append("    @Column(name = \"").append(colName).append("\")\n");
            sb.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateRepository(String className, String pkg, String entityPkg) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import ").append(entityPkg).append(".").append(className).append(";\n");
        sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        sb.append("import org.springframework.stereotype.Repository;\n\n");
        sb.append("@Repository\n");
        sb.append("public interface ").append(className).append("Repository extends JpaRepository<").append(className).append(", Long> {\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateDto(String className, String pkg, List<Map<String, Object>> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.time.LocalDateTime;\n");
        sb.append("import java.time.LocalDate;\n\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append("Dto implements Serializable {\n\n");
        sb.append("    private Long id;\n\n");
        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equalsIgnoreCase(colName)) continue;
            String fieldName = columnToField(colName);
            String javaType = sqlTypeToJavaType((String) col.get("DATA_TYPE"));
            sb.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateApiInterface(String className, String pkg, String dtoPkg) {
        String dtoClass = className + "Dto";
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import ").append(dtoPkg).append(".").append(dtoClass).append(";\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Optional;\n\n");
        sb.append("public interface I").append(className).append("Service {\n\n");
        sb.append("    List<").append(dtoClass).append("> list(").append(dtoClass).append(" dto);\n\n");
        sb.append("    Optional<").append(dtoClass).append("> getById(Long id);\n\n");
        sb.append("    ").append(dtoClass).append(" create(").append(dtoClass).append(" dto);\n\n");
        sb.append("    ").append(dtoClass).append(" update(").append(dtoClass).append(" dto);\n\n");
        sb.append("    void deleteById(Long id);\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateServiceImpl(String className, String pkg, String apiPkg, String dtoPkg, String entityPkg, String repoPkg) {
        String dtoClass = className + "Dto";
        String fieldName = className.substring(0, 1).toLowerCase() + className.substring(1);
        String simpleField = columnToField(className);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import ").append(apiPkg).append(".I").append(className).append("Service;\n");
        sb.append("import ").append(dtoPkg).append(".").append(dtoClass).append(";\n");
        sb.append("import ").append(entityPkg).append(".").append(className).append(";\n");
        sb.append("import ").append(repoPkg).append(".").append(className).append("Repository;\n");
        sb.append("import org.springframework.beans.BeanUtils;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.stereotype.Service;\n");
        sb.append("import org.springframework.transaction.annotation.Transactional;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Optional;\n");
        sb.append("import java.util.stream.Collectors;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(className).append("ServiceImpl implements I").append(className).append("Service {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private ").append(className).append("Repository repository;\n\n");

        // list
        sb.append("    @Override\n");
        sb.append("    public List<").append(dtoClass).append("> list(").append(dtoClass).append(" dto) {\n");
        sb.append("        return repository.findAll().stream()\n");
        sb.append("                .map(this::toDto)\n");
        sb.append("                .collect(Collectors.toList());\n");
        sb.append("    }\n\n");

        // getById
        sb.append("    @Override\n");
        sb.append("    public Optional<").append(dtoClass).append("> getById(Long id) {\n");
        sb.append("        return repository.findById(id).map(this::toDto);\n");
        sb.append("    }\n\n");

        // create
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public ").append(dtoClass).append(" create(").append(dtoClass).append(" dto) {\n");
        sb.append("        ").append(className).append(" entity = new ").append(className).append("();\n");
        sb.append("        BeanUtils.copyProperties(dto, entity);\n");
        sb.append("        entity = repository.save(entity);\n");
        sb.append("        return toDto(entity);\n");
        sb.append("    }\n\n");

        // update
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public ").append(dtoClass).append(" update(").append(dtoClass).append(" dto) {\n");
        sb.append("        ").append(className).append(" entity = repository.findById(dto.getId())\n");
        sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(className).append(" not found with id \" + dto.getId()));\n");
        sb.append("        BeanUtils.copyProperties(dto, entity);\n");
        sb.append("        entity = repository.save(entity);\n");
        sb.append("        return toDto(entity);\n");
        sb.append("    }\n\n");

        // deleteById
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public void deleteById(Long id) {\n");
        sb.append("        repository.deleteById(id);\n");
        sb.append("    }\n\n");

        // toDto
        sb.append("    private ").append(dtoClass).append(" toDto(").append(className).append(" entity) {\n");
        sb.append("        if (entity == null) return null;\n");
        sb.append("        ").append(dtoClass).append(" dto = new ").append(dtoClass).append("();\n");
        sb.append("        BeanUtils.copyProperties(entity, dto);\n");
        sb.append("        return dto;\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateController(String className, String pkg, String servicePkg, String entityPkg, String moduleName, String author) {
        String mapping = "/api/" + moduleName + "/" + className.toLowerCase();
        String permPrefix = moduleName + ":" + className.substring(0, 1).toLowerCase() + className.substring(1);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import ").append(entityPkg).append(".").append(className).append(";\n");
        sb.append("import ").append(servicePkg).append(".I").append(className).append("Service;\n");
        sb.append("import com.pd.common.core.controller.BaseController;\n");
        sb.append("import com.pd.common.core.domain.AjaxResult;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"").append(mapping).append("\")\n");
        sb.append("public class ").append(className).append("Controller extends BaseController {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private I").append(className).append("Service ").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service;\n\n");

        sb.append("    @GetMapping(\"/list\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('").append(permPrefix).append(":list')\")\n");
        sb.append("    public AjaxResult list(").append(className).append(" entity) {\n");
        sb.append("        return success(").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service.list(entity));\n");
        sb.append("    }\n\n");

        sb.append("    @GetMapping(\"/{id}\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('").append(permPrefix).append(":query')\")\n");
        sb.append("    public AjaxResult get(@PathVariable Long id) {\n");
        sb.append("        return success(").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service.getById(id));\n");
        sb.append("    }\n\n");

        sb.append("    @PostMapping\n");
        sb.append("    @PreAuthorize(\"hasAuthority('").append(permPrefix).append(":add')\")\n");
        sb.append("    public AjaxResult create(@RequestBody ").append(className).append(" entity) {\n");
        sb.append("        return success(").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service.create(entity));\n");
        sb.append("    }\n\n");

        sb.append("    @PutMapping\n");
        sb.append("    @PreAuthorize(\"hasAuthority('").append(permPrefix).append(":edit')\")\n");
        sb.append("    public AjaxResult update(@RequestBody ").append(className).append(" entity) {\n");
        sb.append("        return success(").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service.update(entity));\n");
        sb.append("    }\n\n");

        sb.append("    @DeleteMapping(\"/{id}\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('").append(permPrefix).append(":remove')\")\n");
        sb.append("    public AjaxResult delete(@PathVariable Long id) {\n");
        sb.append("        ").append(className.substring(0, 1).toLowerCase()).append(className.substring(1)).append("Service.deleteById(id);\n");
        sb.append("        return success();\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateUiPage(String className, String moduleName, List<Map<String, Object>> columns, String comment) {
        String entityName = className.substring(0, 1).toLowerCase() + className.substring(1);
        String apiPath = "/api/" + moduleName + "/" + entityName;
        String route = "/" + moduleName + "/" + entityName.toLowerCase();
        String entityTitle = comment;
        String camel = className.substring(0, 1).toLowerCase() + className.substring(1);

        List<String> colEntries = new ArrayList<>();
        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equalsIgnoreCase(colName)) continue;
            if (colEntries.size() >= 6) break;
            String fieldName = columnToField(colName);
            String header = colName.substring(0, 1) + colName.substring(1).toLowerCase().replace("_", " ");
            colEntries.add("        { key: '" + fieldName + "', header: '" + header + "' }");
        }
        String cols = String.join(",\n", colEntries);

        StringBuilder formFields = new StringBuilder();
        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equalsIgnoreCase(colName)) continue;
            String fieldName = columnToField(colName);
            String dataType = (String) col.get("DATA_TYPE");
            String label = colName.substring(0, 1) + colName.substring(1).toLowerCase().replace("_", " ");
            boolean isTextArea = "TEXT".equalsIgnoreCase(dataType) || "CLOB".equalsIgnoreCase(dataType);
            boolean isDate = "DATE".equalsIgnoreCase(dataType) || "TIMESTAMP".equalsIgnoreCase(dataType);
            String inputType = isTextArea ? "textarea" : isDate ? "text" : "text";
            formFields.append("                        <FormInput\n");
            formFields.append("                            label=\"").append(label).append("\"\n");
            formFields.append("                            name=\"").append(fieldName).append("\"\n");
            formFields.append("                            value={formData.").append(fieldName).append(" || ''}\n");
            formFields.append("                            onChange={handleInputChange}\n");
            formFields.append("                            type=\"").append(inputType).append("\"\n");
            formFields.append("                        />\n");
        }

        return "import { useState, useEffect } from 'react';\n" +
               "import { Plus, Edit, Trash2, RefreshCw } from 'lucide-react';\n" +
               "import DataGrid from '../components/DataGrid';\n" +
               "import Modal from '../components/Modal';\n" +
               "import FormInput from '../components/FormInput';\n" +
               "import { useToast } from '../components/Toast';\n\n" +
               "const " + className + "List = () => {\n" +
               "    const { addToast } = useToast();\n" +
               "    const [items, setItems] = useState([]);\n" +
               "    const [loading, setLoading] = useState(true);\n" +
               "    const [isModalOpen, setIsModalOpen] = useState(false);\n" +
               "    const [modalMode, setModalMode] = useState('add');\n" +
               "    const [formData, setFormData] = useState({});\n" +
               "    const [submitting, setSubmitting] = useState(false);\n\n" +
               "    useEffect(() => { fetchItems(); }, []);\n\n" +
               "    const fetchItems = () => {\n" +
               "        setLoading(true);\n" +
               "        fetch('" + apiPath + "/list')\n" +
               "            .then(res => res.json())\n" +
               "            .then(data => {\n" +
               "                setLoading(false);\n" +
               "                if (data.code === 200) {\n" +
               "                    setItems(data.data || []);\n" +
               "                } else {\n" +
               "                    addToast('error', data.msg || 'Failed to load', 4000);\n" +
               "                }\n" +
               "            })\n" +
               "            .catch(() => { setLoading(false); });\n" +
               "    };\n\n" +
               "    const handleAddClick = () => {\n" +
               "        setModalMode('add');\n" +
               "        setFormData({});\n" +
               "        setIsModalOpen(true);\n" +
               "    };\n\n" +
               "    const handleEditClick = (row) => {\n" +
               "        setModalMode('edit');\n" +
               "        setFormData({ ...row });\n" +
               "        setIsModalOpen(true);\n" +
               "    };\n\n" +
               "    const handleInputChange = (e) => {\n" +
               "        setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));\n" +
               "    };\n\n" +
               "    const handleDeleteClick = (row) => {\n" +
               "        if (!window.confirm('Are you sure you want to delete this " + entityTitle + "?')) return;\n" +
               "        fetch('" + apiPath + "/' + row.id, { method: 'DELETE' })\n" +
               "            .then(res => res.json())\n" +
               "            .then(data => {\n" +
               "                if (data.code === 200) {\n" +
               "                    addToast('success', 'Deleted successfully', 3000);\n" +
               "                    fetchItems();\n" +
               "                } else {\n" +
               "                    addToast('error', data.msg || 'Delete failed', 5000);\n" +
               "                }\n" +
               "            });\n" +
               "    };\n\n" +
               "    const handleSubmit = () => {\n" +
               "        setSubmitting(true);\n" +
               "        const method = modalMode === 'add' ? 'POST' : 'PUT';\n" +
               "        fetch('" + apiPath + "', {\n" +
               "            method,\n" +
               "            headers: { 'Content-Type': 'application/json' },\n" +
               "            body: JSON.stringify(formData)\n" +
               "        })\n" +
               "        .then(res => res.json())\n" +
               "        .then(data => {\n" +
               "            setSubmitting(false);\n" +
               "            if (data.code === 200) {\n" +
               "                addToast('success', modalMode === 'add' ? 'Created' : 'Updated', 3000);\n" +
               "                setIsModalOpen(false);\n" +
               "                fetchItems();\n" +
               "            } else {\n" +
               "                addToast('error', data.msg || 'Failed to save', 5000);\n" +
               "            }\n" +
               "        })\n" +
               "        .catch(() => { setSubmitting(false); });\n" +
               "    };\n\n" +
               "    const columns = [\n" +
               cols + "\n" +
               "    ];\n\n" +
               "    const actions = [\n" +
               "        { icon: Edit, label: 'Edit', onClick: handleEditClick },\n" +
               "        { icon: Trash2, label: 'Delete', onClick: handleDeleteClick, danger: true }\n" +
               "    ];\n\n" +
               "    const toolbarActions = [\n" +
               "        { icon: Plus, label: 'Add " + entityTitle + "', onClick: handleAddClick, primary: true }\n" +
               "    ];\n\n" +
               "    return (\n" +
               "        <div>\n" +
               "            <div className=\"page-header\">\n" +
               "                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>\n" +
               "                    <h2>" + entityTitle + " Management</h2>\n" +
               "                </div>\n" +
               "                <button className=\"btn btn-secondary\" onClick={fetchItems} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>\n" +
               "                    <RefreshCw size={16} /> Refresh\n" +
               "                </button>\n" +
               "            </div>\n\n" +
               "            <DataGrid\n" +
               "                data={items}\n" +
               "                columns={columns}\n" +
               "                actions={actions}\n" +
               "                toolbarActions={toolbarActions}\n" +
               "                loading={loading}\n" +
               "                searchable={true}\n" +
               "                emptyMessage=\"No " + entityTitle.toLowerCase() + " found.\"\n" +
               "            />\n\n" +
               "            <Modal\n" +
               "                isOpen={isModalOpen}\n" +
               "                onClose={() => setIsModalOpen(false)}\n" +
               "                title={modalMode === 'add' ? 'Add " + entityTitle + "' : 'Edit " + entityTitle + "'}\n" +
               "                size=\"medium\"\n" +
               "                footer={\n" +
               "                    <>\n" +
               "                        <button className=\"btn btn-secondary\" onClick={() => setIsModalOpen(false)}>Cancel</button>\n" +
               "                        <button className=\"btn btn-primary\" onClick={handleSubmit} disabled={submitting}>\n" +
               "                            {submitting ? 'Saving...' : 'Save'}\n" +
               "                        </button>\n" +
               "                    </>\n" +
               "                }\n" +
               "            >\n" +
               "                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>\n" +
               "                    <div className=\"form-row\">\n" +
               formFields.toString() +
               "                    </div>\n" +
               "                </div>\n" +
               "            </Modal>\n" +
               "        </div>\n" +
               "    );\n" +
               "};\n\n" +
               "export default " + className + "List;\n";
    }

    private String generateMenuSql(String className, String comment, String moduleName) {
        String parentMenu = comment + " Management";
        String childMenu = comment + " List";
        String permPrefix = moduleName + ":" + className.substring(0, 1).toLowerCase() + className.substring(1);
        String route = "/" + moduleName + "/" + className.toLowerCase();

        return "-- Menu SQL for " + className + " module\n" +
               "-- Run these INSERTs after deploying the module\n" +
               "-- NOTE: Adjust order_num to avoid conflicts with existing menus\n\n" +
               "-- Parent menu\n" +
               "INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)\n" +
               "VALUES ('" + parentMenu + "', 0, 99, '#', '', 'M', '0', '1', '', 'fa fa-cube', '0', 'admin', current_timestamp, '');\n\n" +
               "-- Child: list page\n" +
               "INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)\n" +
               "SELECT '" + childMenu + "', m.menu_id, 1, '" + route + "', '', 'C', '0', '1', '" + permPrefix + ":list," + permPrefix + ":query," + permPrefix + ":add," + permPrefix + ":edit," + permPrefix + ":remove', 'fa fa-list', '0', 'admin', current_timestamp, ''\n" +
               "FROM sys_menu m WHERE m.menu_name = '" + parentMenu + "';\n";
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
            case "BIGINT": case "INTEGER": case "INT": return "Long";
            case "BOOLEAN": case "BIT": return "Boolean";
            case "DATE": return "LocalDate";
            case "TIMESTAMP": case "DATETIME": return "LocalDateTime";
            case "DECIMAL": case "NUMERIC": case "DOUBLE": case "FLOAT": return "Double";
            default: return "String";
        }
    }
}
