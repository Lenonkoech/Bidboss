package com.example.bidboss2;

public  class Users {
    public String name;
    public String email;
    public String profilePhotoURL;
    public String country;
    public String postalAddress;
    public String phone;
    public String passport;
    public double balance;

    public Users() {
    }

    public Users(String name, String email, String profilePhotoURL, String country, String postalAddress, String phone, String passport, double balance) {
        this.name = name;
        this.email = email;
        this.passport = passport;
        this.profilePhotoURL = profilePhotoURL;
        this.country = country;
        this.postalAddress = postalAddress;
        this.phone = phone;
        this.balance = balance;
    }
}
