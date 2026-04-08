package com.smartsplit.app.core;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Central formatter for currency values stored in paise.
 */
public final class MoneyFormatter {

    private static final Locale DISPLAY_LOCALE = new Locale("en", "IN");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(DISPLAY_LOCALE);

    static {
        CURRENCY_FORMAT.setCurrency(Currency.getInstance("INR"));
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
    }

    private MoneyFormatter() {
        // Utility class.
    }

    public static String formatPaise(long amountPaise) {
        synchronized (CURRENCY_FORMAT) {
            return CURRENCY_FORMAT.format(amountPaise / 100.0);
        }
    }
}
