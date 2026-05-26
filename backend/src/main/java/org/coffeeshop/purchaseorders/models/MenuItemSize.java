package org.coffeeshop.purchaseorders.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/** Enum defining the available size options for menu item types. */
public enum MenuItemSize {
    REGULAR,
    LARGE;

    /** Converts a string value to a MenuItemSize, handling null and blank inputs. */
    @JsonCreator
    public static MenuItemSize fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return MenuItemSize.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    /** Returns the name of this enum constant as a JSON string value. */
    @JsonValue
    public String toValue() {
        return name();
    }
}
