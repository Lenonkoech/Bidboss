package com.example.bidboss2;

public class Auction {
    String auctionId;
    String description;
    String price;
    String imageUrl;
    String auctionEndDate;
//default constructor
    public Auction() {
        super();
    }
//parameterized constructor
    public Auction(String auctionId, String description, String price, String imageUrl, String auctionEndDate) {
        this.auctionId = auctionId;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.auctionEndDate = auctionEndDate;
    }
}