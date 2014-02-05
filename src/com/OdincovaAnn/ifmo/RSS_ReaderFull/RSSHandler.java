package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class RSSHandler extends DefaultHandler {

    ArrayList<String> summaries;
    ArrayList<String> titles;
    String buffer = "";
    final String FEED = "feed";
    boolean item;
    boolean title;
    boolean description;
    CONST myConst;
    String allDescription;

    RSSHandler(ArrayList<String> summaries, ArrayList<String> titles) {
        super();
        this.titles = titles;
        this.summaries = summaries;
        myConst = new CONST(false);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs) throws SAXException {
        buffer = "";
        if (localName.equals(FEED)) {
            myConst = new CONST(true);
        }

        if (localName.equals(myConst.ITEM)) {
            item = true;
        }

        if (localName.equals(myConst.TITLE)) {
            title = true;
        }

        if (localName.equals(myConst.DESCRIPTION) || localName.equals(myConst.CONTENT)) {
            description = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals(myConst.ITEM)) {
            item = false;
            summaries.add(allDescription.trim().replaceAll("\n", "<br>"));
            allDescription = "";
        }

        if (localName.equals(myConst.TITLE)) {
            title = false;
            if (item == true)
                titles.add(buffer);
        }

        if (localName.equals(myConst.DESCRIPTION)) {
            description = false;
            if (item) {
                allDescription += buffer;
            }
        }

        if (localName.equals(myConst.CONTENT)) {
            description = false;
            if (item)
                allDescription += buffer;
        }
        buffer = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (item == true) {
            buffer += new String(ch, start, length);
        }
    }
}
