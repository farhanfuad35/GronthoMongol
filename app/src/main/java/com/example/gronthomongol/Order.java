package com.example.gronthomongol;

import com.backendless.BackendlessUser;

public class Order {
    private String objectId;
    private String Recipient_Name;
    private String Recipient_Email;
    private String Book_Name;
    private String Writer_Name;
    private String Contact_No;
    private String Book_Object_ID;
    private String Comment;
    private int Quantity;
    private String Address;
    private String bKashTxnId;
    private Book book;
    private boolean paid;
    private BackendlessUser user;

    public Order(String recipient_Name, String book_Name, String writer_Name, String contact_No, String comment, int quantity, String address, String bKashTxnId) {
        Recipient_Name = recipient_Name;
        Book_Name = book_Name;
        Writer_Name = writer_Name;
        Contact_No = contact_No;
        Comment = comment;
        Quantity = quantity;
        Address = address;
        this.bKashTxnId = bKashTxnId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getBook_Object_ID() {
        return Book_Object_ID;
    }

    public void setBook_Object_ID(String book_Object_ID) {
        Book_Object_ID = book_Object_ID;
    }

    public String getRecipient_Email() {
        return Recipient_Email;
    }

    public void setRecipient_Email(String recipient_Email) {
        Recipient_Email = recipient_Email;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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

    public String getbKashTxnId() {
        return bKashTxnId;
    }

    public void setbKashTxnId(String bKashTxnId) {
        this.bKashTxnId = bKashTxnId;
    }
}
