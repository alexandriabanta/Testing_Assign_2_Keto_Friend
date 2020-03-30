package com.amandamcnair.testingassign2;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

public class FoodItemsRecyclerView extends AppCompatActivity {

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
        setContentView(R.layout.fooditemsrecyclerview);



        recyclerView = findViewById(R.id.logRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        doDownload();
        foodAdapter = new FoodAdapter();
        recyclerView.setAdapter(foodAdapter);

        /*findViewById(R.id.getFood_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDownload();
            }
        });
         */
    }


    /*private ArrayList<Food> foods() {
        ArrayList<Food> list = new ArrayList<>();

        list.add(new Food(1, "Chicken", "Food", 200));
        list.add(new Food(2, "Steak", "Food", 300));
        list.add(new Food(3, "Shrimp", "Food", 400));
        list.add(new Food(4, "Calamari", "Food", 500));

        return list;
    }

     */

    private FoodDownload dataDownload;

    private void doDownload() {
        if (dataDownload == null) {
            dataDownload = new FoodDownload();
            dataDownload.execute();
        }
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        foodAdapter = new FoodAdapter();
        recyclerView.setAdapter(foodAdapter);
        foods.add(new Food());
        //foods.add(new Food(1, "Chicken", "Food", 200));
        //foods.add(new Food(1, "Turkey", "Food", 200));
    }*/

    private class FoodDownload extends AsyncTask<Void, Void, ResultData> {
        @Override
        protected ResultData doInBackground(Void... voids) {
            ResultData resultData = new ResultData();

            Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/search").buildUpon();
            builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
            //builder.appendQueryParameter("generalSearchInput", "pepperoni pizza");
            builder.appendQueryParameter("generalSearchInput", foodSearchName);
            Log.i("FOOD SEARCH", "" + foodSearchName);

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

                JSONObject reader = new JSONObject(jsonData.toString());
                JSONArray foodsArray = reader.getJSONArray("foods");

                for(int i = 0; i < foodsArray.length(); i++)
                {
                    JSONObject food = foodsArray.getJSONObject(i);
                    //JSONArray description = food.getJSONArray("description");
                    //String descript = description.getString(i);
                    int id = food.getInt("fdcId");
                    String descript = food.getString("description");
                    String dataType = food.getString("dataType");
                    //String foodCode = food.getString("foodCode");

                    /*
                    String brandowner = "";
                    if (dataType.equals("Branded")) {
                        brandowner = reader.getString("brandOwner");
                    }

                     */

                    foods.add(new Food(id, descript, dataType, ""));
                    //Log.i("Food Object", "" + foods.add(new Food(id, descript, dataType)));
                    Log.i("Food Object", "" + foods.get(i).getId());
                    taglist.add(descript);
                    //taglist.add(Integer.toString(id));
                }

                StringBuilder tagBuilder = new StringBuilder();

                int count = 0;
                for (String tag : taglist) {
                    tagBuilder.append(count++ + ". " + tag);
                    Log.i("MESSAGE", count++ + ". " + tag);
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

            //TextView tv = findViewById(R.id.tag_textView);
            //TextView textView = findViewById(R.id.textView);
            //textView.setText(resultData.tagStr);
            foodAdapter.notifyDataSetChanged();

            dataDownload = null;
        }
    }

    private class ResultData {
        String titleStr = "";
        String tagStr = "";
    }

    interface RecyclerViewClickListener
    {
        public void onClick(View view, int position);
    }

    /*class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView view;
        private RecyclerViewClickListener listener;

        public FoodViewHolder(TextView view) { // not itemView
            super(view);
            this.view = view;
            this.view.setOnClickListener(this);
        }

        public TextView getView()
        {
            return view;
        }


        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }


    }*/

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
            holder.view.setText(foods.get(position).getDescription() + "");
        }

        @Override
        public int getItemCount() {
            if(foods != null) {
                return foods.size();
            }
            else {
                return 0;
            }
            //return 5;
        }

        @Override
        public void onClick(View view, int position) {
            //City city = cities.get(getAdapterPosition()); nmj

            String brandNameString = "";

            //if (foods.get(position).getDataType().equals("Branded")) { brandNameString = "by " + foods.get(position).getBrandOwner(); }

            AlertDialog.Builder builder = new AlertDialog.Builder(FoodItemsRecyclerView.this);
            builder.setMessage(Html.fromHtml("<html>" +
                            brandNameString +
                            "<p><b>Data Type: </b> " + foods.get(position).getDataType() + "</p>" +
                            "<p><b>ID: </b> " + foods.get(position).getId() + "</p>" +
                            "<p><b>Description: </b> " + foods.get(position).getDescription() + "</p>" +
                            "<html>"
                    /*"<p><b>ID: </b> " + food.getId() + "</p>" +
                    "<p><b>Description: </b> " + food.getDescription() + "</p>" +
                    "<p><b>Data Type: </b> " + food.getDataType() + "</p>" +
                    "<p><b>Food Code: </b> " + food.getFoodCode() + "</p>" +
                    "<html>"

                     */


            ));


            ID = foods.get(position).getId();

            Log.i("Position", "" + position);
            Log.i("ID", "" + foods.get(position).getId());

            builder.setTitle("Food Info").setPositiveButton("Get Nutrition Info", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int choice) {
                    Intent i = new Intent(getApplicationContext(), GetNutritionInfo.class);
                    getApplicationContext().startActivity(i);
                }
            });

            builder.setTitle("Food Info").setNegativeButton("Go Back", null);
            builder.create().show();
        }


    }
}
