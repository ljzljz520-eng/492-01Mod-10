package com.scaffolding.utils;

import com.scaffolding.entity.User;

public class UserContext {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    public static void setUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getUser() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        User user = CURRENT_USER.get();
        return user != null ? user.getId() : null;
    }

    public static String getUserRole() {
        User user = CURRENT_USER.get();
        return user != null ? user.getUserRole() : null;
    }

    public static Long getEnterpriseId() {
        User user = CURRENT_USER.get();
        return user != null ? user.getEnterpriseId() : null;
    }

    public static Long getLaborCompanyId() {
        User user = CURRENT_USER.get();
        return user != null ? user.getLaborCompanyId() : null;
    }

    public static String getUserName() {
        User user = CURRENT_USER.get();
        if (user == null) return null;
        return user.getNickname() != null ? user.getNickname() : user.getUsername();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
