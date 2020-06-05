package com.example.gronthomongol;

import java.io.Serializable;

public class Book implements Serializable {
    private String objectId;
    private String name;
    private String writer;
    private int price;
    private int quantity;
    private String language;

//    public Book(String language) {
//        this.language = language;
//    }


    @Override
    public boolean equals(Object book) {
        if (!(book instanceof Book)) {
            return false;
        }

        Book thatBook = (Book) book;

        // Custom equality check here.
        return this.objectId.equals(thatBook.objectId);
    }

    public String getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
