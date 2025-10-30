package net.ddsmedia.tusa.tusamovil.Utils;

import android.graphics.drawable.Drawable;

public class GridViewItem
{
    String title;
    Drawable image;

    public Integer getMnu_option() {
        return mnu_option;
    }

    public void setMnu_option(Integer mnu_option) {
        this.mnu_option = mnu_option;
    }

    Integer mnu_option;

    // Empty Constructor
    public GridViewItem()
    {

    }

    // Constructor
    public GridViewItem(String title, Drawable image, Integer mnu_option)
    {
        super();
        this.title = title;
        this.image = image;
        this.mnu_option = mnu_option;
    }

    // Getter and Setter Method
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Drawable getImage()
    {
        return image;
    }

    public void setImage(Drawable image)
    {
        this.image = image;
    }


}