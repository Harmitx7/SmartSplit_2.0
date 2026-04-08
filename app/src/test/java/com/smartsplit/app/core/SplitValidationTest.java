package com.smartsplit.app.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SplitValidationTest {

    @Test
    public void validateEqualSplit_whenPayerNotParticipant_addsZeroShareForPayer() {
        SplitValidation.ValidationResult result = SplitValidation.validateEqualSplit(
            300L,
            9L,
            Arrays.asList(1L, 2L, 3L)
        );

        assertTrue(result.valid);
        assertEquals(Long.valueOf(0L), result.normalizedShares.get(9L));
        assertEquals(4, result.normalizedShares.size());
    }

    @Test
    public void validateCustomSplit_whenMapEmpty_returnsInvalid() {
        SplitValidation.ValidationResult result = SplitValidation.validateCustomSplit(
            500L,
            1L,
            new LinkedHashMap<>()
        );

        assertFalse(result.valid);
    }

    @Test
    public void validateCustomSplit_whenSharesDoNotMatchTotal_returnsInvalid() {
        Map<Long, Long> shares = new LinkedHashMap<>();
        shares.put(1L, 200L);
        shares.put(2L, 100L);

        SplitValidation.ValidationResult result = SplitValidation.validateCustomSplit(
            500L,
            1L,
            shares
        );

        assertFalse(result.valid);
    }

    @Test
    public void validateCustomSplit_whenValid_keepsTotalsAndAddsPayerIfMissing() {
        Map<Long, Long> shares = new LinkedHashMap<>();
        shares.put(2L, 300L);
        shares.put(3L, 200L);

        SplitValidation.ValidationResult result = SplitValidation.validateCustomSplit(
            500L,
            1L,
            shares
        );

        assertTrue(result.valid);
        assertEquals(Long.valueOf(0L), result.normalizedShares.get(1L));

        long total = 0L;
        for (Long value : result.normalizedShares.values()) {
            total += value;
        }
        assertEquals(500L, total);
    }
}
