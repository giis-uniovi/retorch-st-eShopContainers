package giis.eshopcontainers.e2e.functional.model;



public class EShopUser {
    private String email;
    private String password;
    /**
     * Represents an EShop user entity used to store user credentials.*
     * @param email The user's email address.
     * @param password The user's password.
     */
    public EShopUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
