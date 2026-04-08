package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a spending group (e.g., "Goa Trip", "Flatmates").
 * Core entity in the Smart Split data model.
 */
@Entity(tableName = "groups")
public class Group {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    /** Emoji or letter used as the group's avatar icon */
    @ColumnInfo(name = "icon")
    public String icon;

    /** Unix timestamp (ms) when the group was created */
    @ColumnInfo(name = "created_at")
    public long createdAt;

    /** Firebase user ID of the group creator — used for ownership checks */
    @ColumnInfo(name = "created_by_uid")
    public String createdByUid;

    public Group(String name, String icon, long createdAt, String createdByUid) {
        this.name = name;
        this.icon = icon;
        this.createdAt = createdAt;
        this.createdByUid = createdByUid;
    }
}
