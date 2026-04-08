package com.smartsplit.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smartsplit.app.data.model.Expense;

import java.util.List;

/**
 * Data Access Object for Expense operations.
 */
@Dao
public interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExpense(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    void deleteExpenseById(long expenseId);

    @Query("SELECT * FROM expenses WHERE group_id = :groupId ORDER BY created_at DESC")
    LiveData<List<Expense>> getExpensesForGroup(long groupId);

    @Query("SELECT * FROM expenses WHERE group_id = :groupId ORDER BY created_at DESC")
    List<Expense> getExpensesForGroupSync(long groupId);

    @Query("SELECT SUM(amount_paise) FROM expenses WHERE group_id = :groupId")
    LiveData<Long> getTotalSpendForGroup(long groupId);
}
