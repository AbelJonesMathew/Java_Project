package com.campusvoice.dao;

import com.campusvoice.model.AuditLog;
import java.util.List;

public interface AuditLogDAO {
    boolean logAction(AuditLog auditLog);
    List<AuditLog> findAll(int limit);
}
