package com.smartsplit.app.data.sync;

import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.Member;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps local entities into sync-ready records.
 */
public final class SyncEntityMapper {

    public static final String ENTITY_GROUP = "group";
    public static final String ENTITY_MEMBER = "member";
    public static final String ENTITY_EXPENSE = "expense";
    public static final String ENTITY_EXPENSE_SPLIT = "expense_split";

    private SyncEntityMapper() {
        // Utility class.
    }

    public static SyncRecord toRecord(Group group) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", group.name);
        payload.put("icon", group.icon);
        payload.put("createdAt", group.createdAt);
        payload.put("createdByUid", group.createdByUid);
        payload.put("syncState", group.syncState);
        return new SyncRecord(ENTITY_GROUP, group.clientUuid, group.remoteId, group.updatedAt, payload);
    }

    public static SyncRecord toRecord(Member member) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("groupId", member.groupId);
        payload.put("name", member.name);
        payload.put("firebaseUid", member.firebaseUid);
        payload.put("createdAt", member.createdAt);
        payload.put("syncState", member.syncState);
        return new SyncRecord(ENTITY_MEMBER, member.clientUuid, member.remoteId, member.updatedAt, payload);
    }

    public static SyncRecord toRecord(Expense expense) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("groupId", expense.groupId);
        payload.put("title", expense.title);
        payload.put("amountPaise", expense.amountPaise);
        payload.put("paidByMemberId", expense.paidByMemberId);
        payload.put("createdAt", expense.createdAt);
        payload.put("splitType", expense.splitType);
        payload.put("syncState", expense.syncState);
        return new SyncRecord(ENTITY_EXPENSE, expense.clientUuid, expense.remoteId, expense.updatedAt, payload);
    }

    public static SyncRecord toRecord(ExpenseSplit split) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("expenseId", split.expenseId);
        payload.put("memberId", split.memberId);
        payload.put("sharePaise", split.sharePaise);
        payload.put("createdAt", split.createdAt);
        payload.put("syncState", split.syncState);
        return new SyncRecord(ENTITY_EXPENSE_SPLIT, split.clientUuid, split.remoteId, split.updatedAt, payload);
    }
}
