package com.smartsplit.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smartsplit.app.data.model.Member;

import java.util.List;

/**
 * Data Access Object for Member operations.
 */
@Dao
public interface MemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMember(Member member);

    @Delete
    void deleteMember(Member member);

    @Query("SELECT * FROM members WHERE group_id = :groupId ORDER BY name ASC")
    LiveData<List<Member>> getMembersForGroup(long groupId);

    @Query("SELECT * FROM members WHERE group_id = :groupId ORDER BY name ASC")
    List<Member> getMembersForGroupSync(long groupId);

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    Member getMemberByIdSync(long memberId);
}
