package com.smartsplit.app.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.smartsplit.app.data.dao.ExpenseDao;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.dao.GroupDao;
import com.smartsplit.app.data.dao.MemberDao;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.Member;

/**
 * Central Room database for SmartSplit.
 */
@Database(
    entities = {Group.class, Member.class, Expense.class, ExpenseSplit.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            final long now = System.currentTimeMillis();

            database.execSQL("ALTER TABLE groups ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE groups ADD COLUMN client_uuid TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE groups ADD COLUMN remote_id TEXT");
            database.execSQL("ALTER TABLE groups ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'");
            database.execSQL("UPDATE groups SET updated_at = CASE WHEN updated_at = 0 THEN created_at ELSE updated_at END");
            database.execSQL("UPDATE groups SET client_uuid = CASE WHEN client_uuid = '' THEN 'group-' || id ELSE client_uuid END");
            database.execSQL("UPDATE groups SET sync_state = CASE WHEN sync_state IS NULL OR sync_state = '' THEN 'PENDING' ELSE sync_state END");

            database.execSQL("ALTER TABLE members ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE members ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE members ADD COLUMN client_uuid TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE members ADD COLUMN remote_id TEXT");
            database.execSQL("ALTER TABLE members ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'");
            database.execSQL("UPDATE members SET created_at = CASE WHEN created_at = 0 THEN " + now + " ELSE created_at END");
            database.execSQL("UPDATE members SET updated_at = CASE WHEN updated_at = 0 THEN created_at ELSE updated_at END");
            database.execSQL("UPDATE members SET client_uuid = CASE WHEN client_uuid = '' THEN 'member-' || id ELSE client_uuid END");
            database.execSQL("UPDATE members SET sync_state = CASE WHEN sync_state IS NULL OR sync_state = '' THEN 'PENDING' ELSE sync_state END");

            database.execSQL("ALTER TABLE expenses ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE expenses ADD COLUMN client_uuid TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE expenses ADD COLUMN remote_id TEXT");
            database.execSQL("ALTER TABLE expenses ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'");
            database.execSQL("UPDATE expenses SET updated_at = CASE WHEN updated_at = 0 THEN created_at ELSE updated_at END");
            database.execSQL("UPDATE expenses SET client_uuid = CASE WHEN client_uuid = '' THEN 'expense-' || id ELSE client_uuid END");
            database.execSQL("UPDATE expenses SET sync_state = CASE WHEN sync_state IS NULL OR sync_state = '' THEN 'PENDING' ELSE sync_state END");

            database.execSQL("ALTER TABLE expense_splits ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE expense_splits ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE expense_splits ADD COLUMN client_uuid TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE expense_splits ADD COLUMN remote_id TEXT");
            database.execSQL("ALTER TABLE expense_splits ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'");
            database.execSQL("UPDATE expense_splits SET created_at = CASE WHEN created_at = 0 THEN " + now + " ELSE created_at END");
            database.execSQL("UPDATE expense_splits SET updated_at = CASE WHEN updated_at = 0 THEN created_at ELSE updated_at END");
            database.execSQL("UPDATE expense_splits SET client_uuid = CASE WHEN client_uuid = '' THEN 'split-' || id ELSE client_uuid END");
            database.execSQL("UPDATE expense_splits SET sync_state = CASE WHEN sync_state IS NULL OR sync_state = '' THEN 'PENDING' ELSE sync_state END");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public abstract GroupDao groupDao();
    public abstract MemberDao memberDao();
    public abstract ExpenseDao expenseDao();
    public abstract ExpenseSplitDao expenseSplitDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "smartsplit_db"
                    ).addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return INSTANCE;
    }
}
