package com.smartsplit.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.GroupSummary;

import java.util.List;

/**
 * Data Access Object for Group operations.
 * All LiveData queries are automatically observed and updated by Room.
 */
@Dao
public interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroup(Group group);

    @Update
    void updateGroup(Group group);

    @Delete
    void deleteGroup(Group group);

    @Query("SELECT * FROM groups ORDER BY created_at DESC")
    LiveData<List<Group>> getAllGroups();

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    LiveData<Group> getGroupById(long groupId);

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    Group getGroupByIdSync(long groupId);

    @Query("SELECT " +
           "g.id AS groupId, " +
           "g.name AS name, " +
           "g.icon AS icon, " +
           "(SELECT COUNT(*) FROM members m WHERE m.group_id = g.id) AS memberCount, " +
           "COALESCE((SELECT SUM(e.amount_paise) FROM expenses e WHERE e.group_id = g.id), 0) AS totalSpendPaise " +
           "FROM groups g " +
           "ORDER BY g.created_at DESC")
    LiveData<List<GroupSummary>> getGroupSummaries();
}
