package org.vardinsdev.abyssnetwork.staff;

import net.kyori.adventure.text.format.NamedTextColor;

public enum StaffRank {
    MOD(2, "MOD", NamedTextColor.GREEN),
    HEAD_MOD(2, "HEAD MOD", NamedTextColor.DARK_GREEN),
    ADMIN(4, "ADMIN", NamedTextColor.BLUE),
    HEAD_ADMIN(4, "HEAD ADMIN", NamedTextColor.DARK_BLUE),
    MANAGEMENT(4, "MANAGEMENT", NamedTextColor.AQUA),
    HEAD_MANAGEMENT(4, "HEAD MANAGEMENT", NamedTextColor.DARK_AQUA),
    DEVELOPMENT(4, "DEVELOPER", NamedTextColor.LIGHT_PURPLE),
    LEAD_DEVELOPER(4, "LEAD DEVELOPER", NamedTextColor.DARK_PURPLE),
    BOARD_OF_DIRECTORS(4, "BOARD OF DIRECTORS", NamedTextColor.GOLD),
    OWNER(4, "OWNER", NamedTextColor.RED);

    private final int permissionLevel;
    private final String displayRank;
    private final NamedTextColor color;

    StaffRank(int permissionLevel, String displayRank, NamedTextColor color) {
        this.permissionLevel = permissionLevel;
        this.displayRank = displayRank;
        this.color = color;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }
    public String getDisplayRank() {return this.displayRank; }
    public NamedTextColor getColor() {return this.color;}
}