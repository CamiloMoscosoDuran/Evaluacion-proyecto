package vallegrande.edu.pe.agroTecno.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import io.swagger.v3.oas.annotations.media.Schema;

@Table(value = "[user]", schema = "seguridad")
@Schema(description = "Usuario del sistema")
public class User {

    @Id
    @Column("user_id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer userId;

    @Column("username")
    @Schema(example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Column("password_hash")
    @Schema(description = "Hash SHA2_512 generado por base de datos", example = "abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    private byte[] passwordHash;

    @Column("role")
    @Schema(example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Column("status")
    @Schema(description = "1 Activo, 0 Inactivo", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean status;

    @Column("created_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column("created_by")
    @Schema(example = "system", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdBy;

    @Column("updated_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Column("restored_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    public User() {}

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public byte[] getPasswordHash() { return passwordHash; }
    public void setPasswordHash(byte[] passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
