package com.smartsplit.app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.smartsplit.app.data.dao.ExpenseDao;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.dao.GroupDao;
import com.smartsplit.app.data.dao.MemberDao;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.Member;

/**
 * The central Room database for SmartSplit.
 * Uses the Singleton pattern to prevent multiple instances from being opened simultaneously.
 */
@Database(
    entities = {Group.class, Member.class, Expense.class, ExpenseSplit.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

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
                    )
                    .fallbackToDestructiveMigration()  // VERIFY: Replace with proper MigrationSpec before production release
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
