package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a member who belongs to a Group.
 * A member is a participant in expense splits.
 * Members are local to the device (not synced).
 */
@Entity(
    tableName = "members",
    foreignKeys = @ForeignKey(
        entity = Group.class,
        parentColumns = "id",
        childColumns = "group_id",
        onDelete = ForeignKey.CASCADE  // Deleting a group removes its members
    ),
    indices = {@Index("group_id")}
)
public class Member {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "group_id")
    public long groupId;

    @ColumnInfo(name = "name")
    public String name;

    /**
     * Optional: Firebase UID if this member is also a registered user.
     * Null means the member was added manually by name only.
     */
    @ColumnInfo(name = "firebase_uid")
    public String firebaseUid;

    public Member(long groupId, String name, String firebaseUid) {
        this.groupId = groupId;
        this.name = name;
        this.firebaseUid = firebaseUid;
    }
}
