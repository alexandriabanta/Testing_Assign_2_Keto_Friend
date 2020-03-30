package com.amandamcnair.testingassign2;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

public class LogItemsRecyclerView extends AppCompatActivity {

    private  KetoTracker kt = new KetoTracker();
    ArrayList<Food> foods = new ArrayList<Food>();
    //ArrayList<Food> foods;
    FoodAdapter foodAdapter;
    private RecyclerView recyclerView;
    String foodSearchName = MainActivity.getFoodSeachFromClass();

    private static int ID;
    public static int getIDFromClass() {
        return ID;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);

        recyclerView = findViewById(R.id.logRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //foods = new ArrayList<>();
        foodAdapter = new FoodAdapter();
        recyclerView.setAdapter(foodAdapter);

        doDownload();

        StringBuilder tagBuilder = new StringBuilder();
        TreeSet<String> taglist = new TreeSet<>();

        int count = 0;
        for (String tag : taglist) {
            tagBuilder.append(count++ + ". " + tag);
            Log.i("MESSAGE", count++ + ". " + tag);
            tagBuilder.append("\n");
        }

        //foodAdapter(foods);
        foodAdapter = new LogItemsRecyclerView.FoodAdapter();
        recyclerView.setAdapter(foodAdapter);
        //resultData.tagStr = tagBuilder.toString();

        final TextView tv = findViewById(R.id.dailyNetCarbsTextView);

        double netCarbs = 0.0;
        for (int i = 0; i < foods.size(); i++) {
            netCarbs += foods.get(i).carbs;
            Log.i("netcarbs: ","" + netCarbs);
        }

        tv.setText("Net Carbs: " + netCarbs);

        findViewById(R.id.clearLogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LogItemsRecyclerView.this);
                builder.setMessage("Are you sure you want to clear the log?");

                builder.setTitle("Food Info").setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        //clear

                        File save = new File(getFilesDir(), "logSave.txt");
                        save.delete();

                        kt.clearData();
                        foods.clear();

                        saveKetoTrackerToFile(kt);

                        foodAdapter.notifyDataSetChanged();
                        tv.setText("Net Carbs: 0.0");
                    }
                });

                builder.setTitle("Clear log").setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });

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

    private FoodDownload dataDownload;

    private void doDownload() {
        if (dataDownload == null) {
            dataDownload = new FoodDownload();
            dataDownload.execute();

        }
    }


    private class FoodDownload extends AsyncTask<Void, Void, ResultData> {
        @Override
        protected ResultData doInBackground(Void... voids) {
            ResultData resultData = new ResultData();

            restoreKetoTrackerFromFile(kt);

            for (int i = 0; i < kt.getNumOfFoods(); i++) {
                //put keto tracker info into foods
                foods.add(kt.getFoodAt(i));
            }

            //saveKetoTrackerToFile(kt);

            return resultData;
        }

        @Override
        protected void onPostExecute(ResultData resultData) {
            foodAdapter.notifyDataSetChanged();
            dataDownload = null;
        }
    }

    private class ResultData {
        String titleStr = "";
        String tagStr = "";
    }

    interface RecyclerViewClickListener {
        public void onClick(View view, int position);
    }


    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        private TextView view;
        private RecyclerViewClickListener listener;

        public FoodViewHolder(final TextView view, final FoodAdapter listener) { // not itemView
            super(view);
            this.view = view;
            this.listener = listener;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view, getAdapterPosition());
                }
            });
        }
    }

    class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> implements RecyclerViewClickListener{

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //return null;
            TextView textView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_view, parent, false);

            FoodViewHolder viewHolder = new FoodViewHolder(textView, this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {

            //holder.view.setText(foods.get(position).getDescription());
            holder.view.setText(foods.get(position).getDescription());

        }

        @Override
        public int getItemCount() {
            if(foods != null) {
                return foods.size();
            } else {
                return 0;
            }
        }

        @Override
        public void onClick(View view, int position) {
            //City city = cities.get(getAdapterPosition()); nmj

            AlertDialog.Builder builder = new AlertDialog.Builder(LogItemsRecyclerView.this);
            builder.setMessage(Html.fromHtml("<html>" +
                    "<p><b>Description: </b> " + foods.get(position).getDescription() + "</p>" +
                    "<p><b>Carbs: </b> " + foods.get(position).getCarbs() + "</p>" +
                    "<p><b>Fiber: </b> " + foods.get(position).getFiber() + "</p>" +
                    "<html>"
            ));

            final int index = position;
            ID = foods.get(position).getId();

            //Log.i("Position", "" + position);
            //Log.i("ID", "" + foods.get(position).getId());


            builder.setTitle("Modify or Remove").setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int choice) {
                    //have a toast message
                    //remove from list
                    foods.remove(index);
                    foodAdapter.notifyItemRemoved(index);

                    kt.removeFoodAtIndex(index);

                    //update file
                    File save = new File(getFilesDir(), "logSave.txt");
                    save.delete();

                    saveKetoTrackerToFile(kt);
                }
            });

            builder.setTitle("Food Details").setNegativeButton("Go back", null);
            builder.create().show();
        }


    }

    public void saveKetoTrackerToFile(KetoTracker ketoTracker) {
        File save = new File(getFilesDir(), "logSave.txt");
        save.delete();

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

    public void restoreKetoTrackerFromFile(KetoTracker ketoTracker) {
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

    public void exitTracker(KetoTracker ketoTracker) {
        ketoTracker.clearData();
        File save = new File(getFilesDir(), "logSave.txt");
        save.delete();
    }

}
