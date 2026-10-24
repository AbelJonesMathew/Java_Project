package com.campusvoice.model;

public class Department {
    private int id;
    private String deptCode;
    private String deptName;
    private String headOfDeptName;

    public Department() {}

    public Department(int id, String deptCode, String deptName, String headOfDeptName) {
        this.id = id;
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.headOfDeptName = headOfDeptName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getHeadOfDeptName() { return headOfDeptName; }
    public void setHeadOfDeptName(String headOfDeptName) { this.headOfDeptName = headOfDeptName; }

    @Override
    public String toString() {
        return deptName;
    }
}
