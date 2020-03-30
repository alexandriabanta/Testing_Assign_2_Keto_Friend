package com.amandamcnair.testingassign2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static String foodSearch;

    public static String getFoodSeachFromClass() {
        return foodSearch;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        findViewById(R.id.recyclerView_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText editText = (EditText) findViewById(R.id.food_search);
                String foodSearchText = editText.getText().toString();

                //Log.i("STATUS:", "" + (foodSearchText.matches("")));
                Log.i("Food Search:", "" + foodSearchText);

                if(foodSearchText.matches("")) {
                    Toast.makeText(getApplicationContext(), "Must enter a food to search!", Toast.LENGTH_SHORT).show();
                } else {
                    foodSearch = foodSearchText;
                    useRecyclerView();
                }
            }
        });

        findViewById(R.id.descriptions_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDescriptions();
            }
        });

    }

    private void getDescriptions() {
        Intent intent = new Intent(getApplicationContext(), Descriptions.class);
        startActivity(intent);
    }

    private void useRecyclerView() {
        Intent intent = new Intent(getApplicationContext(), FoodItemsRecyclerView.class);
        startActivity(intent);
    }

}
