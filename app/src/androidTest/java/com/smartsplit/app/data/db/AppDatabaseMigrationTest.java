package com.smartsplit.app.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseMigrationTest {

    private static final String TEST_DB = "smartsplit-migration-test.db";
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(TEST_DB);
    }

    @After
    public void tearDown() {
        context.deleteDatabase(TEST_DB);
    }

    @Test
    public void migration1To2_preservesRowsAndBackfillsSyncMetadata() {
        createVersion1Database();

        AppDatabase migrated = Room.databaseBuilder(context, AppDatabase.class, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build();

        SupportSQLiteDatabase db = migrated.getOpenHelper().getWritableDatabase();
        assertEquals(2, db.getVersion());

        Cursor group = db.query("SELECT name, created_at, updated_at, client_uuid, sync_state FROM groups WHERE id = 1");
        assertTrue(group.moveToFirst());
        assertEquals("Trip", group.getString(0));
        long createdAt = group.getLong(1);
        long updatedAt = group.getLong(2);
        String clientUuid = group.getString(3);
        String syncState = group.getString(4);
        group.close();

        assertEquals(createdAt, updatedAt);
        assertNotNull(clientUuid);
        assertFalse(clientUuid.isEmpty());
        assertEquals("PENDING", syncState);

        Cursor member = db.query("SELECT created_at, updated_at, client_uuid, sync_state FROM members WHERE id = 1");
        assertTrue(member.moveToFirst());
        assertTrue(member.getLong(0) > 0L);
        assertTrue(member.getLong(1) > 0L);
        assertFalse(member.getString(2).isEmpty());
        assertEquals("PENDING", member.getString(3));
        member.close();

        Cursor expense = db.query("SELECT amount_paise, updated_at, client_uuid, sync_state FROM expenses WHERE id = 1");
        assertTrue(expense.moveToFirst());
        assertEquals(12345L, expense.getLong(0));
        assertTrue(expense.getLong(1) > 0L);
        assertFalse(expense.getString(2).isEmpty());
        assertEquals("PENDING", expense.getString(3));
        expense.close();

        migrated.close();
    }

    private void createVersion1Database() {
        SQLiteDatabase db = context.openOrCreateDatabase(TEST_DB, Context.MODE_PRIVATE, null);
        db.execSQL("PRAGMA user_version = 1");

        db.execSQL("CREATE TABLE IF NOT EXISTS groups (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name TEXT, " +
            "icon TEXT, " +
            "created_at INTEGER NOT NULL, " +
            "created_by_uid TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS members (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "group_id INTEGER NOT NULL, " +
            "name TEXT, " +
            "firebase_uid TEXT, " +
            "FOREIGN KEY(group_id) REFERENCES groups(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_members_group_id ON members(group_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS expenses (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "group_id INTEGER NOT NULL, " +
            "title TEXT, " +
            "amount_paise INTEGER NOT NULL, " +
            "paid_by_member_id INTEGER NOT NULL, " +
            "created_at INTEGER NOT NULL, " +
            "split_type TEXT, " +
            "FOREIGN KEY(group_id) REFERENCES groups(id) ON DELETE CASCADE, " +
            "FOREIGN KEY(paid_by_member_id) REFERENCES members(id) ON DELETE RESTRICT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_group_id ON expenses(group_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_paid_by_member_id ON expenses(paid_by_member_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS expense_splits (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "expense_id INTEGER NOT NULL, " +
            "member_id INTEGER NOT NULL, " +
            "share_paise INTEGER NOT NULL, " +
            "FOREIGN KEY(expense_id) REFERENCES expenses(id) ON DELETE CASCADE, " +
            "FOREIGN KEY(member_id) REFERENCES members(id) ON DELETE RESTRICT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_splits_expense_id ON expense_splits(expense_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_splits_member_id ON expense_splits(member_id)");

        db.execSQL("INSERT INTO groups(id, name, icon, created_at, created_by_uid) " +
            "VALUES(1, 'Trip', 'T', 1000, 'uid-1')");
        db.execSQL("INSERT INTO members(id, group_id, name, firebase_uid) " +
            "VALUES(1, 1, 'Alice', NULL)");
        db.execSQL("INSERT INTO expenses(id, group_id, title, amount_paise, paid_by_member_id, created_at, split_type) " +
            "VALUES(1, 1, 'Dinner', 12345, 1, 1500, 'EQUAL')");
        db.execSQL("INSERT INTO expense_splits(id, expense_id, member_id, share_paise) " +
            "VALUES(1, 1, 1, 12345)");

        db.close();
    }
}
