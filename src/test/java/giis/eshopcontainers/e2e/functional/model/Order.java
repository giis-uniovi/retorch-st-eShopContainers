package giis.eshopcontainers.e2e.functional.model;

import java.util.List;

public class Order {

    private String orderNumber;
    private String date;
    private String status;
    private double total;
    private String description;
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private String cardNumber;
    private String cardHolderName;
    private boolean isDraft;
    private String cardExpiration;
    private String cardExpirationShort;
    private String cardSecurityNumber;
    private int cardTypeId;
    private String buyer;
    private List<OrderItem> orderItems;

    public String getOrderNumber() {
        return orderNumber;
    }
    public String getDate() {
        return date;
    }
    public String getStatus() {
        return status;
    }
    public double getTotal() {
        return total;
    }
    public String getDescription() {
        return description;
    }
    public String getCity() {
        return city;
    }
    public String getStreet() {
        return street;
    }
    public String getState() {
        return state;
    }
    public String getCountry() {
        return country;
    }
    public String getZipCode() {
        return zipCode;
    }
    public String getCardNumber() {
        return cardNumber;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public boolean isDraft() {
        return isDraft;
    }
    public String getCardExpiration() {
        return cardExpiration;
    }
    public String getCardExpirationShort() {
        return cardExpirationShort;
    }
    public String getCardSecurityNumber() {
        return cardSecurityNumber;
    }
    public int getCardTypeId() {
        return cardTypeId;
    }
    public String getBuyer() {
        return buyer;
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTotal(double total) {
        this.total = total;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    public void setDraft(boolean draft) {
        isDraft = draft;
    }
    public void setCardExpiration(String cardExpiration) {
        this.cardExpiration = cardExpiration;
    }
    public void setCardExpirationShort(String cardExpirationShort) {
        this.cardExpirationShort = cardExpirationShort;
    }
    public void setCardSecurityNumber(String cardSecurityNumber) {
        this.cardSecurityNumber = cardSecurityNumber;
    }
    public void setCardTypeId(int cardTypeId) {
        this.cardTypeId = cardTypeId;
    }
    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
}