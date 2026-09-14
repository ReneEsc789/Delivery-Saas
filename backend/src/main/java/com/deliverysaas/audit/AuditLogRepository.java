package com.deliverysaas.audit;
import java.util.UUID;import org.springframework.data.jpa.repository.*;import com.deliverysaas.audit.domain.AuditLog;
public interface AuditLogRepository extends JpaRepository<AuditLog,UUID>,JpaSpecificationExecutor<AuditLog>{}
