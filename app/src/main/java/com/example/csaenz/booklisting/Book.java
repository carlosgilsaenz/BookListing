package com.example.csaenz.booklisting;

/**
 * Created by csaenz on 4/3/2017.
 */

public class Book {

    private String mTitle;
    private String mAuthor;

    Book(String title, String author){
        mTitle = title;
        mAuthor =  author;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }
}
