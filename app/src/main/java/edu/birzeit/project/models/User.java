package edu.birzeit.project.models;

import java.util.Locale;

public class User {
    private final String email;
    private String firstName;
    private String lastName;

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getInitials() {
        String first = firstName.isEmpty() ? "" : firstName.substring(0, 1);
        String last = lastName.isEmpty() ? "" : lastName.substring(0, 1);
        return (first + last).toUpperCase(Locale.getDefault());
    }
}
