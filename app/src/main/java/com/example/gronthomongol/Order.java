package com.example.gronthomongol;

public class Order {
    private String ID;
    private String Recipient_Name;
    private String Book_Name;
    private String Writer_Name;
    private String Contact_No;
    private String Comment;
    private int Quantity;
    private String Address;
    private String bKashRef;

    public Order(String ID, String recipient_Name, String book_Name, String writer_Name, String contact_No, String comment, int quantity, String address, String bKashRef) {
        this.ID = ID;
        Recipient_Name = recipient_Name;
        Book_Name = book_Name;
        Writer_Name = writer_Name;
        Contact_No = contact_No;
        Comment = comment;
        Quantity = quantity;
        Address = address;
        this.bKashRef = bKashRef;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRecipient_Name() {
        return Recipient_Name;
    }

    public void setRecipient_Name(String recipient_Name) {
        Recipient_Name = recipient_Name;
    }

    public String getBook_Name() {
        return Book_Name;
    }

    public void setBook_Name(String book_Name) {
        Book_Name = book_Name;
    }

    public String getWriter_Name() {
        return Writer_Name;
    }

    public void setWriter_Name(String writer_Name) {
        Writer_Name = writer_Name;
    }

    public String getContact_No() {
        return Contact_No;
    }

    public void setContact_No(String contact_No) {
        Contact_No = contact_No;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getbKashRef() {
        return bKashRef;
    }

    public void setbKashRef(String bKashRef) {
        this.bKashRef = bKashRef;
    }
}
