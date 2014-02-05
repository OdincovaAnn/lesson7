package com.OdincovaAnn.ifmo.RSS_ReaderFull;

public class CONST {

    final String ITEM;
    final String TITLE;
    final String DESCRIPTION;
    final String CONTENT;

    CONST(boolean atom) {
        if (atom) {
            ITEM = "entry";
            TITLE = "title";
            DESCRIPTION = "summary";
            CONTENT = "content";
        } else {
            ITEM = "item";
            TITLE = "title";
            DESCRIPTION = "description";
            CONTENT = "";
        }
    }
}

