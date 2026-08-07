package gui.app.controllers;

import javafx.scene.control.TableColumn;

import java.util.Comparator;

/**
 * Sorting helpers for table columns.
 * <p>
 * The tables hold formatted strings so that prices keep two decimal places,
 * which means TableView sorts them alphabetically by default: 10 lands before
 * 2, and 4.99 after 34.99. These columns need to be told to compare the number
 * inside the text instead.
 */
final class TableColumns {

    private TableColumns() {
    }

    /** Sorts a formatted-string column by the number it contains. */
    static <S> void sortNumerically(TableColumn<S, String> column) {
        column.setComparator(Comparator.comparingDouble(TableColumns::valueOf));
    }

    /** Convenience for applying the numeric comparator to several columns. */
    @SafeVarargs
    static <S> void sortNumerically(TableColumn<S, String>... columns) {
        for (TableColumn<S, String> column : columns) {
            sortNumerically(column);
        }
    }

    /**
     * Reads the number out of a display string, tolerating a currency symbol
     * or spaces around it. Anything unparseable sorts to the bottom rather
     * than throwing, so a malformed row can never break the table.
     */
    private static double valueOf(String display) {
        if (display == null) {
            return Double.NEGATIVE_INFINITY;
        }
        String digits = display.replaceAll("[^0-9.-]", "");
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }
}
