package blog.aida.promotixproject.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aida on 06-Jan-18.
 */

public class User {

    private String id;
    private String databaseReferenceId;
    private String firstName;
    private String lastName;
    private String email;
    private List<Promotion> promotions;
    private boolean isBlocked;
    private URL photo;
    private Map<String,String> likedPromotions = new HashMap<>();
    private Map<String,String> dislikedPromotions = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Map<String,String> getLikedPromotions() {
        return likedPromotions;
    }

    public void setLikedPromotions(Map<String,String> likedPromotions) {
        this.likedPromotions = likedPromotions;
    }

    public Map<String,String> getDislikedPromotions() {
        return dislikedPromotions;
    }

    public void setDislikedPromotions(Map<String,String> dislikedPromotions) {
        this.dislikedPromotions = dislikedPromotions;
    }

    public String getDatabaseReferenceId() {
        return databaseReferenceId;
    }

    public void setDatabaseReferenceId(String databaseReferenceId) {
        this.databaseReferenceId = databaseReferenceId;
    }
}
