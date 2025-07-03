package com.aegis.portal.model;

public class PortalUser {

    private String usrID;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String created;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    // Getters and setters
    public String getUsrID() {
		return usrID;
	}
    public void setUsrID(String usrID) {
		this.usrID = usrID;
	}

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
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

    // toString method for logging/debugging
    @Override
    public String toString() {
        return "PortalUser [id=" + usrID + ", login=" + login + ", firstName=" + firstName +
               ", lastName=" + lastName + ", email=" + email + "]";
    }
}
