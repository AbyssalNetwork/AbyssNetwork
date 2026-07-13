package org.vardinsdev.abyssnetwork.staff;

public enum StaffRank {
    MOD(2),
    HEAD_MOD(2),
    ADMIN(4),
    HEAD_ADMIN(4),
    MANAGEMENT(4),
    HEAD_MANAGEMENT(4),
    DEVELOPMENT(4),
    LEAD_DEVELOPER(4),
    BOARD_OF_DIRECTORS(4),
    OWNER(4);

    private final int permissionLevel;

    StaffRank(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }
}