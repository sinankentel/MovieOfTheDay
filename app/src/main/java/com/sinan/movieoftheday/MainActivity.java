package com.sinan.movieoftheday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //api_key: dfbc389973f4a0bf88d31f55d423b08a
    int randomNumber;
    TextView movieTitleTextView;
    Random rand = new Random();
    String randomTitle  = "";
    String randomMovieYear = "";
    Bitmap randomMovieImage = null;
    String randomMovieImageURL = "";
    String randomStatus;
    Button movieDetailButton;


    public void testButton (View view){

        int testRandom = rand.nextInt(5);
        Log.i("test_numarası", String.valueOf(testRandom));


    }

    public void generateNewMovie(){

        int minNumber = 10;
        int maxNumber = 11;

        randomNumber = rand.nextInt(maxNumber-minNumber + 1) + minNumber;

        try {
            DownloadTask task = new DownloadTask();
            task.execute("https://api.themoviedb.org/3/movie/" + Integer.toString(randomNumber) + "?api_key=dfbc389973f4a0bf88d31f55d423b08a").get();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public void chooseMovie (View view){

        generateNewMovie();

    }

    public void movieDetail (View view){

        Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);

        intent.putExtra("randomMovieYear", randomMovieYear);

        ImageDownloader imageTask = new ImageDownloader();

        try {

            randomMovieImage = imageTask.execute("https://image.tmdb.org/t/p/w500" + randomMovieImageURL).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        randomMovieImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        intent.putExtra("randomMovieImage", byteArray);

        startActivity(intent);


    }

    public class ImageDownloader extends  AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            URL url = null;
            try {

                url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


    public class DownloadTask extends AsyncTask <String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url = null;
            HttpURLConnection connection = null;

            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();

                Log.i("Info", String.valueOf(responseCode));

                if(responseCode == HttpURLConnection.HTTP_OK) {

                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream);

                    int data = reader.read();

                    while (data != -1) {

                        char current = (char) data;
                        result += current;
                        data = reader.read();
                    }

                    return result;

                } else{

                    Log.i("Error", "responseCode 200 değil: " + String.valueOf(responseCode));
                    //generateNewMovie();

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result2) {
            super.onPostExecute(result2);

            try {

                JSONObject jsonObject = new JSONObject(result2);

                randomTitle = jsonObject.getString("original_title");
                randomMovieYear = jsonObject.getString("release_date");
                randomMovieImageURL = jsonObject.getString("backdrop_path");
                randomStatus = jsonObject.getString("status");

                movieTitleTextView.setText(randomTitle);
                movieDetailButton.setVisibility(View.VISIBLE);


                if (randomStatus.equals("Released")) {

                    Log.i("Info", "Status Released, hata yok.");

                }else if (randomStatus.equals("34")){

                    Log.i("Info", "Bu random number'da film yok");
                    generateNewMovie();

                    movieTitleTextView.setText("Try Again...");

                }else{

                    Log.i("Info", "unexpected...");

                }


            } catch (JSONException e) {

                e.printStackTrace();

            }
            catch (Exception e) {

                e.printStackTrace();

            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieTitleTextView = (TextView) findViewById(R.id.movieTitleTextView);
        movieDetailButton = (Button) findViewById(R.id.movieDetailButton);


    }
}
