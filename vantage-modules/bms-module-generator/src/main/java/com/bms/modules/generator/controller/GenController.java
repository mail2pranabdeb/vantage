package com.pd.modules.generator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;
import com.pd.modules.generator.service.IGenTableService;
import com.pd.modules.generator.service.IGenTableColumnService;

/**
 * Code generation controller
 */
@RestController
@RequestMapping("/tool/gen")
public class GenController {

    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private IGenTableColumnService genTableColumnService;

    /**
     * Get list of generator tables
     */
    @GetMapping("/list")
    public List<GenTable> list(GenTable genTable) {
        return genTableService.selectGenTableList(genTable);
    }

    /**
     * Get list of database tables
     */
    @GetMapping("/db/list")
    public List<GenTable> dataList(GenTable genTable) {
        return genTableService.selectDbTableList(genTable);
    }

    /**
     * Get table columns by ID
     */
    @GetMapping("/{tableId}")
    public Map<String, Object> getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> result = new HashMap<>();
        result.put("table", table);
        result.put("rows", list);
        return result;
    }

    /**
     * Preview generated code
     */
    @GetMapping("/preview/{tableId}")
    public Map<String, String> preview(@PathVariable Long tableId) {
        return genTableService.getTemplatePath(String.valueOf(tableId));
    }

    /**
     * Download generated code
     */
    @GetMapping("/download/{tableName}")
    public ResponseEntity<byte[]> download(@PathVariable String tableName) throws IOException {
        byte[] data = genTableService.downloadZipData(tableName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + tableName + ".zip\"")
                .body(data);
    }

    /**
     * Batch generate code
     */
    @GetMapping("/batchGen")
    public void batchGen(@RequestParam String tables) {
        // Implementation for batch generation
    }

    /**
     * Update generator table
     */
    @PutMapping
    public int editSave(@RequestBody GenTable genTable) {
        return genTableService.updateGenTable(genTable);
    }

    /**
     * Delete generator tables
     */
    @DeleteMapping("/{tableIds}")
    public int remove(@PathVariable Long[] tableIds) {
        return genTableService.deleteGenTableByIds(tableIds);
    }

    /**
     * Import tables
     */
    @PostMapping("/importTable")
    public int importTableSave(@RequestParam String tables) {
        genTableService.importGenTable(tables);
        return 1;
    }

    /**
     * Sync database table changes
     */
    @PutMapping("/synchDb/{tableName}")
    public int synchDb(@PathVariable String tableName) {
        // Implementation for syncing database
        return 1;
    }
}
