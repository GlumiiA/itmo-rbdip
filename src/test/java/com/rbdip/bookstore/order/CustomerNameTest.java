package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerNameTest {

    @Test
    void splitsFirstAndLastName() {
        CustomerName name = CustomerName.parse("Ivan Petrov");

        assertThat(name.firstName()).isEqualTo("Ivan");
        assertThat(name.lastName()).isEqualTo("Petrov");
    }

    @Test
    void keepsEverythingAfterFirstSpaceInLastName() {
        CustomerName name = CustomerName.parse("Maria Anna Sidorova");

        assertThat(name.firstName()).isEqualTo("Maria");
        assertThat(name.lastName()).isEqualTo("Anna Sidorova");
    }

    @Test
    void singleWordHasEmptyLastName() {
        CustomerName name = CustomerName.parse("Madonna");

        assertThat(name.firstName()).isEqualTo("Madonna");
        assertThat(name.lastName()).isEmpty();
    }

    @Test
    void ignoresSurroundingWhitespace() {
        CustomerName name = CustomerName.parse("  Anna   Sidorova  ");

        assertThat(name.firstName()).isEqualTo("Anna");
        assertThat(name.lastName()).isEqualTo("Sidorova");
    }

    @Test
    void fullNameJoinsParts() {
        assertThat(new CustomerName("Ivan", "Petrov").fullName()).isEqualTo("Ivan Petrov");
        assertThat(new CustomerName("Madonna", "").fullName()).isEqualTo("Madonna");
    }

    @Test
    void customerExposesFullName() {
        Customer customer = new Customer(CustomerName.parse("Ivan Petrov"), "Moscow", null);

        assertThat(customer.getFirstName()).isEqualTo("Ivan");
        assertThat(customer.getLastName()).isEqualTo("Petrov");
        assertThat(customer.getFullName()).isEqualTo("Ivan Petrov");
    }
}
