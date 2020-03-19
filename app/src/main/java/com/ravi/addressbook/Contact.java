package com.ravi.addressbook;

import java.util.Comparator;

public class Contact {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;

    public Contact(int id, String firstName, String lastName, String email, String contactNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


    public static final Comparator<Contact> BY_FIRSTNAME_ASCENDING = new Comparator<Contact>() {
        @Override
        public int compare(Contact o1, Contact o2) {
            //ascending
            return o1.getFirstName().compareTo(o2.getFirstName());
        }
    };


    public static final Comparator<Contact> BY_FIRSTNAME_DESCENDING = new Comparator<Contact>() {
        @Override
        public int compare(Contact o1, Contact o2) {
            //ascending
            return o2.getFirstName().compareTo(o1.getFirstName());
        }
    };

}
