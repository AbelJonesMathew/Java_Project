package com.campusvoice.model;

public class Role {
    public static final int ROLE_STUDENT = 1;
    public static final int ROLE_HOD = 2;
    public static final int ROLE_PRINCIPAL = 3;
    public static final int ROLE_MANAGEMENT = 4;

    private int id;
    private String roleName;
    private String description;

    public Role() {}

    public Role(int id, String roleName, String description) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
