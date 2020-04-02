package com.amandamcnair.testingassign2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

public class LogItemsRecyclerView extends AppCompatActivity {

    private  KetoTracker kt = new KetoTracker();
    private double dailyNetCarbs = 0.0;
    ArrayList<Food> foods = new ArrayList<Food>();
    //ArrayList<Food> foods;
    FoodAdapter foodAdapter;
    private RecyclerView recyclerView;
    boolean itemAddedBool = false;
    private static int ID;
    public static int getIDFromClass() {
        return ID;
    }
    public class DNC {  double dnc = 0.0 ; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);

        /*
        if (savedInstanceState == null) {
            itemAddedBool = false;
        } else {
            itemAddedBool = (boolean) savedInstanceState.getSerializable("itemAddedBool");
            Log.i("SavedInstanceStateNonNull: ","itemaddedBool = " + itemAddedBool);

            //an item was added, so call toast and set it back to false.
            itemAddedToast();
            itemAddedBool = false;
        }
        */

        Intent in= getIntent();
        Bundle b = in.getExtras();

        if(b != null) {
            itemAddedBool =(boolean) b.get("itemAddedBool");
            Log.i("SavedInstanceStateNonNull: ","itemAddedBool = " + itemAddedBool);

            //if an item was added, call toast and set it back to false.
            if (itemAddedBool) itemAddedToast();
            itemAddedBool = false;
        } else { itemAddedBool = false;}


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


        findViewById(R.id.clearLogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LogItemsRecyclerView.this);
                builder.setMessage("Are you sure you want to clear the log?");

                builder.setTitle("Food Info").setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                            File save = new File(getFilesDir(), "logSave.txt");
                            save.delete();

                            kt.clearData();
                            foods.clear();
                            foodAdapter.notifyDataSetChanged();

                            dailyNetCarbs = 0.0;
                            updateNetCarbsTextView();

                            logClearedToast();
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

    private class FoodDownload extends AsyncTask<Void, Void, DNC> {
        @Override
        protected DNC doInBackground(Void... voids) {
            DNC carbCount = new DNC();
            restoreKetoTrackerFromFile(kt);

            Log.i("kt.getNumOFoods(): ", "" + kt.getNumOfFoods());
            for (int i = 0; i < kt.getNumOfFoods(); i++) {
                //put keto tracker info into foods
                foods.add(kt.getFoodAt(i));
                Log.i("foods.at(i): ", "" + foods.get(i).getDescription());
                carbCount.dnc += (foods.get(i).getCarbs() - foods.get(i).getFiber());
            }
            return carbCount;
        }

        @Override
        protected void onPostExecute(DNC carbCount) {
            foodAdapter.notifyDataSetChanged();
            Log.i("carbCount: ","" + carbCount.dnc);
            dailyNetCarbs = carbCount.dnc;
            Log.i("dnc: ","" + dailyNetCarbs);

            updateNetCarbsTextView();
            dataDownload = null;
        }
    }

    public void updateNetCarbsTextView() {
        TextView tv = findViewById(R.id.dailyNetCarbsTextView);
        Log.i("dailynetcarbs: ",""+ dailyNetCarbs);

        //round textview valueto 2 decimal places
        tv.setText(String.format("Net Carbs: %.2f", dailyNetCarbs));

        if (dailyNetCarbs >= 50) {
            Log.i("carb quota hit: ","" + dailyNetCarbs);
            carbQuotaHitAlertDialog();
        }
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
            String brandNameString = "";
            if (foods.get(position).getDataType().equals("Branded"))
            { brandNameString = "by " + foods.get(position).getBrandOwner(); }

            AlertDialog.Builder builder = new AlertDialog.Builder(LogItemsRecyclerView.this);
            builder.setMessage(Html.fromHtml("<html>" +  brandNameString +
                    "<p><b>Description: </b> " + foods.get(position).getDescription() + "</p>" +
                    "<p><b>Carbs: </b> " + foods.get(position).getCarbs() + "</p>" +
                    "<p><b>Fiber: </b> " + foods.get(position).getFiber() + "</p>" +
                    "<html>"
            ));

            final int index = position;
            ID = foods.get(position).getId();

            builder.setTitle("Modify or Remove").setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int choice) {
                    //have a toast message
                    itemRemovedToast(index);

                    //get removedfoodnetcarbs before the item is deleted
                    double removedFoodNetCarbs = foods.get(index).getCarbs() - foods.get(index).getFiber();

                    //remove from list
                    foods.remove(index);
                    foodAdapter.notifyItemRemoved(index);

                    //remove the item from kt and save file
                    kt.removeFoodAtIndex(index);
                    saveKetoTrackerToFile(kt);

                    //subtract the net carbs of the food removed from dailyNetCarbs
                    dailyNetCarbs -= removedFoodNetCarbs;
                    if (kt.getNumOfFoods() == 0) dailyNetCarbs = 0.0;

                    updateNetCarbsTextView();
                }
            });

            builder.setTitle("Food Details").setNegativeButton("Go back", null);
            builder.create().show();
        }

    }

    public void carbQuotaHitAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Carb Limit Met");
        builder.setMessage("   You have hit your carb quota \n   of 50g for today.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { } });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void itemAddedToast() {
        Toast.makeText(LogItemsRecyclerView.this,
                "Item added successfully.", Toast.LENGTH_LONG).show();
    }

    public void itemRemovedToast(int position) {
        String itemRemoved = foods.get(position).getDescription();
        Toast.makeText(LogItemsRecyclerView.this,
                itemRemoved+ " removed successfully.", Toast.LENGTH_LONG).show();
    }

    public void logClearedToast() {
        Toast.makeText(LogItemsRecyclerView.this,
                "Log cleared successfully.", Toast.LENGTH_LONG).show();
    }

    public void logEmptyToast() {
        Toast.makeText(LogItemsRecyclerView.this,
                "Log already empty.", Toast.LENGTH_LONG).show();
    }

    public void saveKetoTrackerToFile(KetoTracker ketoTracker) {
        File save = new File(getFilesDir(), "logSave.txt");
        save.delete();

        try {
            FileOutputStream FOS = openFileOutput("logSave.txt", Context.MODE_PRIVATE);
            OutputStreamWriter OSW = new OutputStreamWriter(FOS);
            BufferedWriter BW = new BufferedWriter(OSW);
            PrintWriter PW = new PrintWriter(BW);

            Log.i("ketoTracker.getNumOfFoods(): ","" + ketoTracker.getNumOfFoods());
            for (int i = 0; i < ketoTracker.getNumOfFoods(); i++) {
                Log.i("Food #" + i + ": ","saving food " + ketoTracker.getFoodNameAt(i));
                //1) int id
                PW.println(ketoTracker.getFoodIdAt(i));
                Log.i("ketoTracker.getFoodIdAt(" + "i):",""+ ketoTracker.getFoodIdAt(i));

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

                //4 lines for each food
                int foodId;
                while (scanner.hasNextInt()) {
                    //1) id
                    foodId = Integer.parseInt(scanner.next());

                    //2) carbs
                    double c = Double.parseDouble(scanner.next());

                    //4) fiber
                    double f = Double.parseDouble(scanner.next());

                    nameAndDataType obj = getNameAndDataTypeUsingId(foodId);
                    String fn = obj.name;

                    Food food = new Food(foodId, fn, c, f);
                    food.setDataType(obj.dataType);
                    food.setBrandOwner(obj.brandOwner);

                    ketoTracker.addFood(food);
                }

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

    //helper class for getting a name and data type using id
    public class nameAndDataType {
        String name = "";
        String dataType = "";
        String brandOwner = "";
    }

    public nameAndDataType getNameAndDataTypeUsingId(int id) throws IOException, JSONException {
        nameAndDataType nadt = new nameAndDataType();

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
        nadt.name = reader.getString("description");
        nadt.dataType = reader.getString("dataType");

        if (nadt.dataType.equals("Branded")) {
            nadt.brandOwner = reader.getString("brandOwner");
        }   //otherwise brandOwner remains ""

        return nadt;
    }

}
