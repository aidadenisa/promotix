package blog.aida.promotixproject.model;

import java.net.URL;
import java.util.List;

/**
 * Created by aida on 06-Jan-18.
 */

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private List<Promotion> promotions;
    private boolean isBlocked;
    private URL photo;

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

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public URL getPhoto() {
        return photo;
    }

    public void setPhoto(URL photo) {
        this.photo = photo;
    }

}
