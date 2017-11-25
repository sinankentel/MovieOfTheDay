package com.sinan.movieoftheday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        TextView textView = (TextView) findViewById(R.id.textView);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);

        Intent intent = getIntent();

        textView.setText(intent.getStringExtra("randomMovieYear"));

        byte[] byteArray = getIntent().getByteArrayExtra("randomMovieImage");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageView2.setImageBitmap(bmp);


    }
}
