package pw.mpb.dzielnica.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Type {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("typeName")
    @Expose
    private String typeName;
    @SerializedName("category")
    @Expose
    private Category category;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getCategoryName() {
        return category.toString();
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {

        return this.typeName + " [" + this.getCategoryName() + "]";

    }


    public Category getCategory() {
        return category;
    }
}