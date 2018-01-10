package blog.aida.promotixproject.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by aida on 06-Jan-18.
 */
@IgnoreExtraProperties
public class Store {

    private String name;
    private String address;
    private String categories;
    //private String latlng;
    private double lat;
    private double lng;
    private String id;


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

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    /*public String getLatLng(){ return latlng;}

    public void setLat(String latlng) { this.latlng = latlng;}
    */
    public double getLat(){ return lat;}

    public void setLat(double lat) { this.lat = lat;}

    public double getLng(){ return lng;}

    public void setLng(double lng) { this.lng = lng;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
