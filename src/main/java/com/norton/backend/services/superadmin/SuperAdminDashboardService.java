package com.norton.backend.services.superadmin;

import com.norton.backend.dto.responses.superadmin.SuperAdminStatsResponse;

public interface SuperAdminDashboardService {

  SuperAdminStatsResponse getSystemStats();
}
