package com.norton.backend.security;

public final class Permissions {

  private Permissions() {}

  public static final String PERMISSION_CREATE = "PERMISSION_CREATE";
  public static final String PERMISSION_UPDATE = "PERMISSION_UPDATE";
  public static final String PERMISSION_DELETE = "PERMISSION_DELETE";
  public static final String PERMISSION_VIEW = "PERMISSION_VIEW";

  public static final String ROLE_ASSIGN_PERMISSION = "ROLE_ASSIGN_PERMISSION";
  public static final String ROLE_REMOVE_PERMISSION = "ROLE_REMOVE_PERMISSION";
  public static final String ROLE_VIEW = "ROLE_VIEW";

  public static final String OFFICER_ASSIGN_PERMISSION = "OFFICER_ASSIGN_PERMISSION";
  public static final String OFFICER_REMOVE_PERMISSION = "OFFICER_REMOVE_PERMISSION";
  public static final String OFFICER_VIEW_PERMISSION = "OFFICER_VIEW_PERMISSION";

  public static final String OFFICER_VIEW = "OFFICER_VIEW";
  public static final String OFFICER_CREATE = "OFFICER_CREATE";
  public static final String OFFICER_UPDATE = "OFFICER_UPDATE";
  public static final String OFFICER_DELETE = "OFFICER_DELETE";

  public static final String ATTENDANCE_VIEW = "ATTENDANCE_VIEW";
  public static final String ATTENDANCE_CREATE = "ATTENDANCE_CREATE";
  public static final String ATTENDANCE_UPDATE = "ATTENDANCE_UPDATE";
  public static final String ATTENDANCE_EXPORT = "ATTENDANCE_EXPORT";
  public static final String ATTENDANCE_IMPORT = "ATTENDANCE_IMPORT";
  public static final String ATTENDANCE_SCAN = "ATTENDANCE_SCAN";

  public static final String SHIFT_VIEW = "SHIFT_VIEW";
  public static final String SHIFT_CREATE = "SHIFT_CREATE";
  public static final String SHIFT_UPDATE = "SHIFT_UPDATE";
  public static final String SHIFT_DELETE = "SHIFT_DELETE";
  public static final String SHIFT_ASSIGN = "SHIFT_ASSIGN";

  public static final String ORGANIZATION_VIEW = "ORGANIZATION_VIEW";
  public static final String ORGANIZATION_CREATE = "ORGANIZATION_CREATE";
  public static final String ORGANIZATION_UPDATE = "ORGANIZATION_UPDATE";
  public static final String ORGANIZATION_DELETE = "ORGANIZATION_DELETE";

  public static final String QR_SESSION_VIEW = "QR_SESSION_VIEW";
  public static final String QR_SESSION_CREATE = "QR_SESSION_CREATE";
  public static final String QR_SESSION_UPDATE = "QR_SESSION_UPDATE";
  public static final String QR_SESSION_END = "QR_SESSION_END";
  public static final String QR_SESSION_CHECKIN = "QR_SESSION_CHECKIN";

  public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
}
