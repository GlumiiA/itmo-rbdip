package com.rbdip.bookstore.order;

public record CustomerName(String firstName, String lastName) {

    private static final char SEPARATOR = ' ';

    public static CustomerName parse(String fullName) {
        String trimmed = fullName.trim();
        int separator = trimmed.indexOf(SEPARATOR);
        if (separator < 0) {
            return new CustomerName(trimmed, "");
        }
        return new CustomerName(trimmed.substring(0, separator), trimmed.substring(separator + 1).trim());
    }

    public String fullName() {
        return lastName.isEmpty() ? firstName : firstName + SEPARATOR + lastName;
    }
}
