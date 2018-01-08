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
    private User author;
    private String storeId;
    private String name;
    private String cuantum;
    private Date promoBeginDate;
    private Date promoEndDate;
    private int category;
    private boolean isExpanded;

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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
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

    public Date getPromoBeginDate() {
        return promoBeginDate;
    }

    public void setPromoBeginDate(Date promoBeginDate) {
        this.promoBeginDate = promoBeginDate;
    }

    public Date getPromoEndDate() {
        return promoEndDate;
    }

    public void setPromoEndDate(Date promoEndDate) {
        this.promoEndDate = promoEndDate;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getCuantum() {
        return cuantum;
    }

    public void setCuantum(String cuantum) {
        this.cuantum = cuantum;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
