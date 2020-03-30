package com.amandamcnair.testingassign2;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

public class Descriptions extends AppCompatActivity {


    //private RecyclerView recyclerView;
    //private RecyclerView.Adapter adapter;

    private List<Food> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.descriptions);

        /*recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();

        for(int i = 0; i <= 10; i++)
        {
            Food foodItem = new Food(1, "Chicken", "Food", 13);

        foodList.add(foodItem);
        }


        adapter = new MyAdapter(foodList, this);
        recyclerView.setAdapter(adapter);

         */


        findViewById(R.id.descriptions_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDownload();
            }
        });
    }

    private StackExchangeDownload dataDownload;

    private void doDownload() {
        if (dataDownload == null) {
            dataDownload = new StackExchangeDownload();
            dataDownload.execute();
        }
    }

    //private static final String SE_URL = "https://api.stackexchange.com/2.1/questions?order=desc&sort=creation&site=stackoverflow&tagged=android";

    private class StackExchangeDownload extends AsyncTask<Void, Void, ResultData> {
        @Override
        protected ResultData doInBackground(Void... voids) {
            ResultData resultData = new ResultData();

            Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/search").buildUpon();
            builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
            //builder.appendQueryParameter("generalSearchInput", "pepperoni pizza");

            try {
                URL url = new URL(builder.toString());
                Log.i("RESULT", url.toString());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                StringBuilder jsonData = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonData.append(line);
                }

                StringBuilder titleBuilder = new StringBuilder();
                // changed HashSet to TreeSet so that the results are sorted.
                TreeSet<String> taglist = new TreeSet<>();

                /*JSONObject reader = new JSONObject(jsonData.toString());
                JSONArray items = reader.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);

                    String title = item.getString("title");
                    titleBuilder.append(title);
                    titleBuilder.append("\n----------\n");

                    JSONArray tags = item.getJSONArray("tags");
                    for (int x = 0; x < tags.length(); x++) {
                        String tag = tags.getString(x);
                        taglist.add(tag);
                    }
                }

                 */

                JSONObject reader = new JSONObject(jsonData.toString());
                JSONArray foods = reader.getJSONArray("foods");
                for(int i = 0; i < foods.length(); i++)
                {
                    JSONObject food = foods.getJSONObject(i);
                    //JSONArray description = food.getJSONArray("description");
                    //String descript = description.getString(i);
                    String descript = food.getString("description");
                    taglist.add(descript);
                }

                StringBuilder tagBuilder = new StringBuilder();
                for (String tag : taglist) {
                    tagBuilder.append(tag);
                    tagBuilder.append("\n");
                }

                resultData.titleStr = titleBuilder.toString();
                resultData.tagStr = tagBuilder.toString();

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultData;
        }

        @Override
        protected void onPostExecute(ResultData resultData) {
            /*TextView tv = findViewById(R.id.textView);
            tv.setText(resultData.titleStr);*/

            TextView tv = findViewById(R.id.tag_textView);
            tv.setText(resultData.tagStr);

            dataDownload = null;
        }
    }

    private class ResultData {
        String titleStr = "";
        String tagStr = "";
    }
}
