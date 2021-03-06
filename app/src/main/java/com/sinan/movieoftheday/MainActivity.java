package com.sinan.movieoftheday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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
    String randomMovieCountry;


    public void logger() {

        Log.i("Info", "logger: ");

    }

    public void testButton (View view){

        int testRandom = rand.nextInt(5);
        Log.i("test_numarası", String.valueOf(testRandom));


    }


    public void notificationCall(String movieTitle, String movieYear, String movieCountry){

        String formattedMovieYear = movieYear.substring(0,4);

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),1,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher_round,"Go",pendingIntent).build();

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(formattedMovieYear + " yılından bir film...")
                .setContentText(movieCountry + " yapımı " + movieTitle)
                .setContentIntent(pendingIntent)
                .addAction(action)
                .setSmallIcon(android.R.drawable.presence_away)
                .build();


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    public void generateNewMovie(){

        int minNumber = 10;
        int maxNumber = 10000;

        randomNumber = rand.nextInt(maxNumber-minNumber + 1) + minNumber;

        try {
            DownloadTask task = new DownloadTask();
            Log.i("task statüsü", task.getStatus().toString());

            task.execute("https://api.themoviedb.org/3/movie/" + Integer.toString(randomNumber) + "?api_key=dfbc389973f4a0bf88d31f55d423b08a");

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
        } catch (Exception e){
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
            }catch (Exception e){
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
            InputStream inputStream;
            InputStreamReader reader;

            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();

                Log.i("Info", String.valueOf(responseCode));

                if(responseCode == HttpURLConnection.HTTP_OK) {


                    inputStream = connection.getInputStream();
                    reader = new InputStreamReader(inputStream);

                    int data = reader.read();

                    while (data != -1) {

                        char current = (char) data;
                        result += current;
                        data = reader.read();
                    }

                    return result;

                } else{

                    inputStream = connection.getErrorStream();
                    reader = new InputStreamReader(inputStream);

                    int data = reader.read();

                    while (data != -1) {

                        char current = (char) data;
                        result += current;
                        data = reader.read();
                    }

                    Log.i("Error", "responseCode 200 değil: " + String.valueOf(responseCode));
                    generateNewMovie();

                    return null;

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                JSONObject jsonObject = new JSONObject(result);

                randomTitle = jsonObject.getString("original_title");
                randomMovieYear = jsonObject.getString("release_date");
                randomMovieImageURL = jsonObject.getString("backdrop_path");
                randomMovieCountry = jsonObject.getString("production_countries");


                movieTitleTextView.setText(randomTitle);
                movieDetailButton.setVisibility(View.VISIBLE);

                notificationCall(randomTitle, randomMovieYear, null);


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
