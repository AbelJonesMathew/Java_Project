package com.campusvoice.model;

public class Category {
    private int id;
    private String categoryName;
    private String description;
    private String iconName;

    public Category() {}

    public Category(int id, String categoryName, String description, String iconName) {
        this.id = id;
        this.categoryName = categoryName;
        this.description = description;
        this.iconName = iconName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    @Override
    public String toString() {
        return categoryName;
    }
}
