package giis.eshopcontainers.e2e.functional.model;

public class OrderItem {

    private int productId;
    private String productName;
    private double unitPrice;
    private int discount;
    private int units;
    private String pictureUrl;

    public int getProductId() {
        return productId;
    }
    public String getProductName() {
        return productName;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public int getDiscount() {
        return discount;
    }
    public int getUnits() {
        return units;
    }
    public String getPictureUrl() {return pictureUrl;}

    public void setProductId(int productId) {
        this.productId = productId;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public void setDiscount(int discount) {
        this.discount = discount;
    }
    public void setUnits(int units) {
        this.units = units;
    }
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}