package com.norton.backend.services.superadmin;

import com.norton.backend.dto.responses.superadmin.SystemHealthResponse;
import java.util.Map;

public interface SuperAdminSystemService {

  SystemHealthResponse getSystemHealth();

  Map<String, Object> clearCache();
}
