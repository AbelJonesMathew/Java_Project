package com.campusvoice.security;

import com.campusvoice.model.Role;
import com.campusvoice.model.User;

public class AccessControlManager {

    public static boolean isStudent(User user) {
        return user != null && user.getRoleId() == Role.ROLE_STUDENT;
    }

    public static boolean isHOD(User user) {
        return user != null && user.getRoleId() == Role.ROLE_HOD;
    }

    public static boolean isPrincipal(User user) {
        return user != null && user.getRoleId() == Role.ROLE_PRINCIPAL;
    }

    public static boolean isManagement(User user) {
        return user != null && user.getRoleId() == Role.ROLE_MANAGEMENT;
    }

    public static boolean canManageUsers(User user) {
        return isManagement(user);
    }

    public static boolean canAssignComplaint(User user) {
        return isHOD(user) || isPrincipal(user) || isManagement(user);
    }

    public static boolean canEscalate(User user) {
        return isHOD(user) || isPrincipal(user);
    }
}
