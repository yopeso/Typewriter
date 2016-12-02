package com.yopeso.typewrite.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.yopeso.typewrite.R;
import com.yopeso.typewriter.TypewriterRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TypewriterRefreshLayout typewriterRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        typewriterRefreshLayout = (TypewriterRefreshLayout) findViewById(R.id.typewriter_layout);
    }
}
