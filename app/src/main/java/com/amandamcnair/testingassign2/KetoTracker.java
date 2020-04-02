package com.amandamcnair.testingassign2;

import android.util.Log;

import java.util.ArrayList;

public class KetoTracker {
    /*

    public class WeeklyTracker {
        DailyTracker weeklyTracker[] = new DailyTracker[7];
    }
     */
        private ArrayList<Food> dailyFoodList = new ArrayList<Food>();
        private int numOfFoods = 0;
        private double dailyCarbs = 0.0,
                       dailyFiber = 0.0,
                       dailyNetCarbs = 0.0;
        private String dataType = "";

        KetoTracker() { }

        public int getNumOfFoods() { return numOfFoods;}
        public double getDailyNetCarbs() { return dailyNetCarbs; }
        public double getDailyCarbs() { return dailyCarbs; }
        public double getDailyFiber() { return dailyFiber; }

        public void addFood(Food foodItem) {
            dailyFoodList.add(foodItem);
            numOfFoods++;

            dailyCarbs += foodItem.carbs;
            dailyFiber += foodItem.fiber;
            dailyNetCarbs = dailyCarbs - dailyFiber;
        }

        public void removeFoodAtIndex(int index) {
            if (index >= 0 && index < numOfFoods) {
                //int foodID = foodItem.id;
                //Log.i("foodID", "" + dailyFoodList);
                numOfFoods--;

                dailyFoodList.remove(index);

                double carbstosubtract = 0.0;
                double fibertosubtract = 0.0;

                dailyCarbs -= carbstosubtract;
                dailyFiber -= fibertosubtract;
                dailyNetCarbs = dailyCarbs - dailyFiber;
            } else {
                Log.i("error","index invalid");
            }
        }

        public void clearData() {
            dailyFoodList.clear();
            numOfFoods = 0;
            dailyCarbs = 0.0; dailyFiber = 0.0; dailyNetCarbs = 0.0;
        }

        public void setDataType(String dataType) { this.dataType = dataType;}

        public Food getFoodAt(int index) {
            return dailyFoodList.get(index);
        }

        public int getFoodIdAt(int index) {
            return dailyFoodList.get(index).id;
        }

        public String getFoodNameAt(int index) {
            return dailyFoodList.get(index).description;
        }

        public double getFoodCarbsAt(int index) {
            return dailyFoodList.get(index).carbs;
        }

        public double getFoodFiberAt(int index) {
            return dailyFoodList.get(index).fiber;
        }

        public double getFoodNetCarbsAt(int index) {
            double c = dailyFoodList.get(index).carbs;
            double f = dailyFoodList.get(index).fiber;
            return (c - f);
        }
}
