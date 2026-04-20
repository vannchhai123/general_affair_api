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
}
