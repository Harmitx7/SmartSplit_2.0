package com.smartsplit.app.core;

import com.smartsplit.app.data.model.ExpenseSplit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates and normalizes split shares before persisting expenses.
 */
public final class SplitValidation {

    public static final class ValidationResult {
        public final boolean valid;
        public final String errorMessage;
        public final Map<Long, Long> normalizedShares;

        private ValidationResult(boolean valid, String errorMessage, Map<Long, Long> normalizedShares) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.normalizedShares = normalizedShares;
        }

        public static ValidationResult ok(Map<Long, Long> normalizedShares) {
            return new ValidationResult(true, null, normalizedShares);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, new LinkedHashMap<>());
        }
    }

    private SplitValidation() {
        // Utility class.
    }

    public static ValidationResult validateEqualSplit(long amountPaise, long payerMemberId, List<Long> participantIds) {
        if (amountPaise <= 0L) {
            return ValidationResult.error("Amount must be greater than zero.");
        }
        if (payerMemberId <= 0L) {
            return ValidationResult.error("Payer must be selected.");
        }
        if (participantIds == null || participantIds.isEmpty()) {
            return ValidationResult.error("At least one participant is required.");
        }

        LinkedHashMap<Long, Long> uniqueParticipants = new LinkedHashMap<>();
        for (Long participantId : participantIds) {
            if (participantId == null || participantId <= 0L) {
                return ValidationResult.error("Participant IDs must be valid.");
            }
            uniqueParticipants.putIfAbsent(participantId, 0L);
        }

        if (uniqueParticipants.isEmpty()) {
            return ValidationResult.error("At least one participant is required.");
        }

        long share = amountPaise / uniqueParticipants.size();
        long remainder = amountPaise % uniqueParticipants.size();

        boolean assignedRemainder = false;
        for (Map.Entry<Long, Long> entry : uniqueParticipants.entrySet()) {
            long value = share;
            if (!assignedRemainder) {
                value += remainder;
                assignedRemainder = true;
            }
            entry.setValue(value);
        }

        // Keep payer represented even when they are not part of the split shares.
        uniqueParticipants.putIfAbsent(payerMemberId, 0L);
        return ValidationResult.ok(uniqueParticipants);
    }

    public static ValidationResult validateCustomSplit(long amountPaise, long payerMemberId, Map<Long, Long> memberSharesMap) {
        if (amountPaise <= 0L) {
            return ValidationResult.error("Amount must be greater than zero.");
        }
        if (payerMemberId <= 0L) {
            return ValidationResult.error("Payer must be selected.");
        }
        if (memberSharesMap == null || memberSharesMap.isEmpty()) {
            return ValidationResult.error("Custom split requires at least one participant.");
        }

        long total = 0L;
        LinkedHashMap<Long, Long> normalized = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : memberSharesMap.entrySet()) {
            Long memberId = entry.getKey();
            Long share = entry.getValue();

            if (memberId == null || memberId <= 0L) {
                return ValidationResult.error("Participant IDs must be valid.");
            }
            if (share == null || share < 0L) {
                return ValidationResult.error("Custom split shares cannot be negative.");
            }

            normalized.put(memberId, share);
            total += share;
        }

        if (total != amountPaise) {
            return ValidationResult.error("Custom split shares must add up to the total amount.");
        }

        // Keep payer represented so balance computation always includes the paying member.
        normalized.putIfAbsent(payerMemberId, 0L);
        return ValidationResult.ok(normalized);
    }

    public static List<ExpenseSplit> toExpenseSplits(Map<Long, Long> normalizedShares) {
        List<ExpenseSplit> splits = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : normalizedShares.entrySet()) {
            splits.add(new ExpenseSplit(0L, entry.getKey(), entry.getValue()));
        }
        return splits;
    }
}
