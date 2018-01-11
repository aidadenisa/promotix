package blog.aida.promotixproject.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by aida on 06-Jan-18.
 */

@IgnoreExtraProperties
public class Promotion {

    private int badVotes;
    private int goodVotes;
    private String author;
    private String storeId;
    private String name;
    private String cuantum;
    private long promoEndDate;
    private String category;
    private String uniqueId;

    public int getBadVotes() {
        return badVotes;
    }

    public void setBadVotes(int badVotes) {
        this.badVotes = badVotes;
    }

    public int getGoodVotes() {
        return goodVotes;
    }

    public void setGoodVotes(int goodVotes) {
        this.goodVotes = goodVotes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPromoEndDate() {
        return promoEndDate;
    }

    public void setPromoEndDate(long promoEndDate) {
        this.promoEndDate = promoEndDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCuantum() {
        return cuantum;
    }

    public void setCuantum(String cuantum) {
        this.cuantum = cuantum;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
