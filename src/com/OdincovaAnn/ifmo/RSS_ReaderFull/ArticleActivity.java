package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

public class ArticleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        String title = getIntent().getExtras().getString("title");
        String description = getIntent().getExtras().getString("description");
        TextView textView = (TextView) findViewById(R.id.Title);
        textView.setText(title);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadData(description, "text/html; charset=utf-8", null);
    }

}
