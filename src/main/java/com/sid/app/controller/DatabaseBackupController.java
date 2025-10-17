package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.exception.SchemaNotFoundException;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.DatabaseBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class DatabaseBackupController {

    private final DatabaseBackupService backupService;

    @Autowired
    public DatabaseBackupController(DatabaseBackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping(EndpointConstants.DB_BACKUP_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<Map<String, String>>> createBackup(
            @RequestParam(name = "type", defaultValue = "sql") String type,
            @RequestParam(name = "db", required = false) String databaseName,
            @RequestParam(name = "schema", required = false) String schemaName) {

        log.info("Received backup request - Type: {}, DB: {}, Schema: {}", type, databaseName, schemaName);

        try {
            String backupPath = backupService.createBackup(type, databaseName, schemaName);

            log.info("Backup created successfully at: {}", backupPath);
            return ResponseEntity.ok(
                    ResponseDTO.<Map<String, String>>builder()
                            .status("SUCCESS")
                            .message("Backup completed successfully")
                            .data(Map.of("backupPath", backupPath))
                            .build()
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid backup type", e);
            return ResponseEntity.badRequest().body(
                    ResponseDTO.<Map<String, String>>builder()
                            .status("ERROR")
                            .message("Invalid backup type. Use 'sql' or 'dump'")
                            .build()
            );
        } catch (SchemaNotFoundException e) {
            log.error("Schema validation failed", e);
            return ResponseEntity.badRequest().body(
                    ResponseDTO.<Map<String, String>>builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Backup failed", e);
            return ResponseEntity.internalServerError().body(
                    ResponseDTO.<Map<String, String>>builder()
                            .status("ERROR")
                            .message("Backup failed: " + e.getMessage())
                            .build()
            );
        }
    }
}
