package com.amandamcnair.testingassign2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;

public class GetNutritionInfo extends AppCompatActivity {

    //private RecyclerView recyclerView;
    //private RecyclerView.Adapter adapter;

    private  KetoTracker ketoTracker = new KetoTracker();

    //ArrayList<Nutrition> nutritionAR = new ArrayList<Nutrition>();
    int id = FoodItemsRecyclerView.getIDFromClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getnutritioninfo);
        //log food button listener created in onPostExecute

        /*
        findViewById(R.id.changeLogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogItemsRecyclerView.class);
                startActivity(intent);
            }
        });

         */

        doDownload();
        //writeLog();
    }

    @Override
    protected void onResume() {
        doDownload();
        super.onResume();
    }


    @Override
    protected void onStart() {
        doDownload();
        super.onStart();
    }



    private StackExchangeDownload dataDownload;

    private void doDownload() {
        if (dataDownload == null) {
            dataDownload = new StackExchangeDownload();
            dataDownload.execute();
        }

        //writeLog();
    }


    private class StackExchangeDownload extends AsyncTask<Void, Void, ResultData> {
        @Override
        protected ResultData doInBackground(Void... voids) {
            //restore immediately
            //restoreKetoTrackerFromFile();

            String foodName = "";
            String tagString = "";
            double carbsPerServing = 0.0, fiberPerServing = 0.0;
            //ResultData resultData = new ResultData();

            Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/" + id).buildUpon();
            builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
            builder.appendQueryParameter("generalSearchInput", String.valueOf(id));

            // /data/user/0/com.amandamcnair.testingassign2/files/logSave.txt
            File save = new File(getFilesDir(), "logSave.txt");
            restoreKetoTrackerFromFile();

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

                StringBuilder textBuilder = new StringBuilder();
                JSONObject reader = new JSONObject(jsonData.toString());
                String foodClass = reader.getString("foodClass");
                double netCarbs = 0.0;

                boolean foundCarbs = false;

                //if the type is branded,
                if (foodClass.equals("Branded")) {
                    Log.i("food class", "" + foodClass);
                    JSONObject labelNutrients = reader.getJSONObject("labelNutrients");
                    JSONObject carbohydrates = labelNutrients.getJSONObject("carbohydrates");
                    JSONObject fiber = labelNutrients.getJSONObject("fiber");

                    foodName = reader.getString("description");
                    carbsPerServing = carbohydrates.getDouble("value");
                    fiberPerServing = fiber.getDouble("value");
                    netCarbs = carbsPerServing - fiberPerServing;
                    String brandOwner = reader.getString("brandOwner");

                    if (netCarbs >= 0) {
                        textBuilder.append("by " + brandOwner + "\n");
                        textBuilder.append("Nutrition\n\n");

                        textBuilder.append("This food has ");
                        textBuilder.append(carbsPerServing);
                        textBuilder.append("g of carbs and ");
                        textBuilder.append(fiberPerServing);
                        textBuilder.append("g of fiber. That comes to ");
                        if (netCarbs < 0) netCarbs = 0;
                        textBuilder.append(netCarbs);
                        textBuilder.append("g of net carbs per serving.\n\n");

                        if (netCarbs == 0) {
                            textBuilder.append("Congrats! This food is net carb free.");
                        } else if (netCarbs > 50) {
                            textBuilder.append("Uh oh! This food isn't keto-friendly.");
                        } else {
                            textBuilder.append("Yay! This food is keto-friendly as part of a balanced diet.");
                        }
                    } else {
                        Log.i("carbError; ","value netCarbs should be positive");
                    }

                } else { //otherwise,
                    Log.i("food class", "" + foodClass);

                    foodName = reader.getString("description");
                    //look through nutrients list to find info
                    JSONArray foodNutrients = reader.getJSONArray("foodNutrients");


                    for (int i = 0; i < 10; i++) {
                        JSONObject obj = foodNutrients.getJSONObject(i);
                        JSONObject nutrient = obj.getJSONObject("nutrient");
                        Log.i("nutrient.getString('name')", "" + nutrient.getString("name"));
                        if (nutrient.getString("name").equals("Carbohydrate, by difference")) {
                            foundCarbs = true;
                            carbsPerServing = obj.getDouble("amount");
                        }
                    }

                    for (int i = 0; i < 10; i++) {
                        JSONObject obj = foodNutrients.getJSONObject(i);
                        JSONObject nutrient = obj.getJSONObject("nutrient");
                        Log.i("nutrient.getString('name')", "" + nutrient.getString("name"));
                        if (nutrient.getString("name").equals("Fiber, total dietary")) {
                            fiberPerServing = obj.getDouble("amount");
                        }
                    }

                    if (!foundCarbs) {
                        //then carbs are not a significant nutrient in this item.
                        textBuilder.append("This listing does not have significant carbohydrate" +
                                " content. Please go back and choose a similar listing. ");
                    } else {

                        netCarbs = carbsPerServing - fiberPerServing;

                        if (netCarbs >= 0) {
                            textBuilder.append("Nutrition\n\n");
                            textBuilder.append("This food has ");
                            textBuilder.append(carbsPerServing);
                            textBuilder.append("g of carbs and ");
                            textBuilder.append(fiberPerServing);
                            textBuilder.append("g of fiber. That comes to ");
                            if (netCarbs < 0) netCarbs = 0;
                            textBuilder.append(netCarbs);
                            textBuilder.append("g of net carbs per serving.\n\n");

                            if (netCarbs == 0) {
                                textBuilder.append("Congrats! This food is net carb free.");
                            } else if (netCarbs > 50) {
                                textBuilder.append("Uh oh! This food isn't keto-friendly.");
                            } else {
                                textBuilder.append("Yay! This food is keto-friendly as part of a balanced diet.");
                            }
                        } else {
                            Log.i("carbError; ","value netCarbs should be positive");
                        }
                    }
                }


                tagString = textBuilder.toString();

                Log.i("url: ","");
                Log.i("textBuilder: ",textBuilder.toString());

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();

            }

            final ResultData resultData = new ResultData(foodName, tagString, carbsPerServing,fiberPerServing);
            return resultData;
        }

        @Override
        protected void onPostExecute(ResultData resultData) {

            final double carbAmount = resultData.carbs;
            final double fiberAmount = resultData.fiber;
            final String titleString = resultData.titleStr;

            TextView tv = findViewById(R.id.tag_textView);
            tv.setText(resultData.tagStr);

            TextView title = findViewById(R.id.textView4);
            title.setText(titleString);


            final Food foodItem = new Food(id, titleString, carbAmount, fiberAmount);


            //"log this food" button
            /*
            findViewById(R.id.descriptions_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // add this item to tracker

                    ketoTracker.addFood(foodItem);

                    File save = new File(getFilesDir(), "logSave.txt");
                    save.delete();

                    saveKetoTrackerToFile();

                    //writeLog();
                    Intent intent = new Intent(getApplicationContext(), LogItemsRecyclerView.class);
                    startActivity(intent);
                }
            });

             */

            dataDownload = null;
        }
    }

    private class ResultData {
        public String titleStr = "";
        public String tagStr = "";
        //public Food wholeFood = new Food();
        double carbs, fiber;

        ResultData(String titleStr, String tagStr, double carbs, double fiber) {
            this.titleStr = titleStr;
            this.tagStr = tagStr;
            this.carbs = carbs;
            this.fiber = fiber;
        }
    }

    public void writeLog() {
        //TextView logtv = findViewById(R.id.logTextView);
        //TextView netcarbstv = findViewById(R.id.netCarbTextView);

        StringBuilder logTextBuilder = new StringBuilder();
        StringBuilder carbsTextBuilder = new StringBuilder();
        logTextBuilder.append("Logged food:     \n");
        carbsTextBuilder.append("N.C.:\n");

        for (int i = 0; i < ketoTracker.getNumOfFoods(); i++) {
            //go through the ketotracker array and display the food's:
            //  1) name in logtv
            //  2) netCarbs in netcarbstv

            logTextBuilder.append(ketoTracker.getFoodNameAt(i));
            logTextBuilder.append("\n\n");

            carbsTextBuilder.append(ketoTracker.getFoodNetCarbsAt(i));
            carbsTextBuilder.append("\n\n");
        }

        String logText = logTextBuilder.toString();
        String carbsText = carbsTextBuilder.toString();

    }

    @Override
    protected void onStop() {
        //write all food data to file
        //saveKetoTrackerToFile();
        //file.delete();
        super.onStop();
    }

    public void saveKetoTrackerToFile() {
        try {
            FileOutputStream FOS = openFileOutput("logSave.txt", Context.MODE_PRIVATE);
            OutputStreamWriter OSW = new OutputStreamWriter(FOS);
            BufferedWriter BW = new BufferedWriter(OSW);
            PrintWriter PW = new PrintWriter(BW);

            //PW.println((int)ketoTracker.getNumOfFoods());
            Log.i("Save: ","contents of file;");
            for (int i = 0; i < ketoTracker.getNumOfFoods(); i++) {
                //1) int id
                PW.println(ketoTracker.getFoodIdAt(i));
                Log.i("ketoTracker.getFoodIdAt(" + "i):",""+ ketoTracker.getFoodIdAt(i));

                //PW.println(ketoTracker.getFoodNameAt(i));
                //Log.i("ketoTracker.getFoodNameAt(" + "i):",""+ ketoTracker.getFoodNameAt(i));

                //2) carbs
                PW.println(ketoTracker.getFoodCarbsAt(i));
                Log.i("ketoTracker.getFoodCarbsAt(" + "i):",""+ ketoTracker.getFoodCarbsAt(i));

                //3) fiber
                PW.println(ketoTracker.getFoodFiberAt(i));
                Log.i("ketoTracker.getFoodFiberAt(" + "i):",""+ ketoTracker.getFoodFiberAt(i));
            }

            PW.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void restoreKetoTrackerFromFile() {
        // /data/user/0/com.amandamcnair.testingassign2/files/logSave.txt
        File save = new File(getFilesDir(), "logSave.txt");
        Log.i("Save: ","file grabbed");
        try {
            FileInputStream fis = openFileInput("logSave.txt");
            Scanner scanner = new Scanner(fis);

            //see if there has been any log data saved
            if (save.exists() && (save.length() != 0)) {
                Log.i("Save: ","file exists and has nonzero length");
                Log.i("save.length: ",""+save.length());

                //int foodCount = Integer.parseInt(scanner.next());
                //Log.i("foodCount : ",""+foodCount);
                //get the saved data and write log

                //4 lines for each food
                int foodId;
                while (scanner.hasNextInt()) {
                    //1) id
                    foodId = Integer.parseInt(scanner.next());
                    //Log.i("file : ",""+scanner.next());
                    //Log.i("file : ",""+scanner.next())

                    //2) carbs
                    //Log.i("file : ",""+scanner.next());
                    double c = Double.parseDouble(scanner.next());

                    //4) fiber
                    //Log.i("file : ",""+scanner.next());
                    double f = Double.parseDouble(scanner.next());

                    String fn = getNameUsingId(foodId);

                    Food food = new Food(foodId, fn, c, f);
                    ketoTracker.addFood(food);
                    //foodCount++;
                }
                //ketoTracker.restore(foodCount);
                Log.i("Save: ","log written");
                //writeLog();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exitTracker() {
        ketoTracker.clearData();
        File save = new File(getFilesDir(), "logSave.txt");
        save.delete();
    }

    public String getNameUsingId(int id) throws IOException, JSONException {
        Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/" + id).buildUpon();
        builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
        builder.appendQueryParameter("generalSearchInput", String.valueOf(id));

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

        JSONObject reader = new JSONObject(jsonData.toString());
        String foodName = reader.getString("description");

        return foodName;
    }
}
