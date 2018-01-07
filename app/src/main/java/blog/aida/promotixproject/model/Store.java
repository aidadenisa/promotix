package blog.aida.promotixproject.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by aida on 06-Jan-18.
 */
@IgnoreExtraProperties
public class Store {

    private String name;
    private String address;
    private int categories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCategories() {
        return categories;
    }

    public void setCategories(int categories) {
        this.categories = categories;
    }

}
