package net.swallowsnest.booklisting;

/**
 * Created by marshas on 10/24/16.
 */

public class Book {
    private String mTitle;
    private String mAuthor;

    public Book(String title, String author){
        mTitle = title;
        mAuthor = author;
    }

    public String getTitle(){ return mTitle;}

    public String getAuthor() {return mAuthor;}

}

