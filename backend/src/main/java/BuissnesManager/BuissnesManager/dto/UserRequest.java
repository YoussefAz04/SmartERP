package BuissnesManager.BuissnesManager.dto;

public class UserRequest {
    private String username;
    private String email;
    private String phone;
    private String imageUrl;




    public UserRequest(String username, String email, String password,String phone,String imageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone ;
        this.imageUrl = imageUrl ;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String password;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
