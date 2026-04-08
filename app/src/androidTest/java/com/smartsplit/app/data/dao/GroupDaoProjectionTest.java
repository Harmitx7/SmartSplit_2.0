package com.smartsplit.app.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.util.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class GroupDaoProjectionTest {

    private AppDatabase db;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
            .allowMainThreadQueries()
            .build();
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void getGroupSummaries_returnsMemberCountAndSpend() throws InterruptedException, TimeoutException {
        Group recentGroup = new Group("Trip", "T", 2000L, "u1");
        Group olderGroup = new Group("Home", "H", 1000L, "u1");
        long recentGroupId = db.groupDao().insertGroup(recentGroup);
        long olderGroupId = db.groupDao().insertGroup(olderGroup);

        long recentMemberA = db.memberDao().insertMember(new Member(recentGroupId, "Alice", null));
        long recentMemberB = db.memberDao().insertMember(new Member(recentGroupId, "Bob", null));
        long olderMemberA = db.memberDao().insertMember(new Member(olderGroupId, "Nina", null));

        db.expenseDao().insertExpense(new Expense(
            recentGroupId, "Dinner", 15000L, recentMemberA, 2100L, "EQUAL"
        ));
        db.expenseDao().insertExpense(new Expense(
            recentGroupId, "Taxi", 3500L, recentMemberB, 2200L, "EQUAL"
        ));
        db.expenseDao().insertExpense(new Expense(
            olderGroupId, "Rent", 50000L, olderMemberA, 1100L, "EQUAL"
        ));

        List<GroupSummary> summaries = LiveDataTestUtil.getOrAwaitValue(db.groupDao().getGroupSummaries());
        assertNotNull(summaries);
        assertEquals(2, summaries.size());

        // Query orders by created_at DESC, so recent group should be first.
        assertEquals(recentGroupId, summaries.get(0).groupId);

        Map<Long, GroupSummary> byId = new HashMap<>();
        for (GroupSummary summary : summaries) {
            byId.put(summary.groupId, summary);
        }

        assertEquals(2L, byId.get(recentGroupId).memberCount);
        assertEquals(18500L, byId.get(recentGroupId).totalSpendPaise);
        assertEquals(1L, byId.get(olderGroupId).memberCount);
        assertEquals(50000L, byId.get(olderGroupId).totalSpendPaise);
    }
}
