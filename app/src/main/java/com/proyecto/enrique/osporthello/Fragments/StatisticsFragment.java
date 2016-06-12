package com.proyecto.enrique.osporthello.Fragments;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Models.SportPercentage;
import com.proyecto.enrique.osporthello.Models.Statistic;
import com.proyecto.enrique.osporthello.Models.StatisticsTotal;
import com.proyecto.enrique.osporthello.R;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cz.msebera.android.httpclient.Header;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Fragment que muestra los datos de las estadísticas acumulativas del usuario, con
 * información numérica y mediante dos gráficos.
 */

public class StatisticsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private LinearLayout layoutContainer;
    private LinearLayout layoutInfo;
    private RelativeLayout layoutBarChart;
    private BarChart barChart;
    private PieChart pieChart;
    private Spinner spinnerYear;
    private Spinner spinnerMonth;
    private Spinner spinnerDistanceUnits;
    private ProgressBar progressBar;
    private TextView txtDuration;
    private TextView txtDistance;
    private TextView txtCalories;
    private TextView txtDistanceTitle;

    private Context context;
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedUnits = 0;

    private ArrayList<SportPercentage> sportPercentages;
    private ArrayList<Statistic> statisticsList;
    private StatisticsTotal statisticsTotal;

    private final int barColor = 0xFF1FF4AC;
    private final String emptyColor = "#939393";
    private final String cyclingColor = "#FE6DA8";
    private final String runningColor = "#CDA67F";
    private final String joggingColor = "#56B7F1";
    private final String walkingColor = "#FED70E";

    public StatisticsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        layoutContainer = (LinearLayout)view.findViewById(R.id.layoutContainer);
        layoutInfo = (LinearLayout)view.findViewById(R.id.layoutInfo);
        layoutBarChart = (RelativeLayout)view.findViewById(R.id.layoutBarChart);
        barChart = (BarChart) view.findViewById(R.id.barchart);
        pieChart = (PieChart) view.findViewById(R.id.piechart);
        spinnerYear = (Spinner)view.findViewById(R.id.spinnerYear);
        spinnerMonth = (Spinner)view.findViewById(R.id.spinnerMonth);
        spinnerDistanceUnits = (Spinner)view.findViewById(R.id.spinnerDistance);
        progressBar = (ProgressBar)view.findViewById(R.id.progressChart);
        txtDuration = (TextView)view.findViewById(R.id.txtDuration);
        txtDistance = (TextView)view.findViewById(R.id.txtDistance);
        txtDistanceTitle = (TextView)view.findViewById(R.id.txtDistanceTitle);
        txtCalories = (TextView)view.findViewById(R.id.txtCalories);
        context = getContext();

        // Change user interface if landscape orientation
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
            layoutInfo.requestLayout();
            int width = displayMetrics.widthPixels/2;
            layoutInfo.getLayoutParams().width = width;
            layoutInfo.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutBarChart.getLayoutParams().width = width;
            layoutBarChart.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        initializeSpinners();
        if(savedInstanceState == null){
            downloadStatistics();
        }
        else {
            progressBar.setVisibility(View.GONE);
            sportPercentages = (ArrayList<SportPercentage>)savedInstanceState.getSerializable("sportPercentages");
            statisticsList = (ArrayList<Statistic>)savedInstanceState.getSerializable("statisticsList");
            statisticsTotal = (StatisticsTotal)savedInstanceState.getSerializable("statisticsTotal");
            spinnerDistanceUnits.setSelection(savedInstanceState.getInt("selectedUnits"));
            selectedYear = savedInstanceState.getInt("selectedYear");
            selectedMonth = savedInstanceState.getInt("selectedMonth");
            spinnerDistanceUnits.setSelection(selectedUnits);
            spinnerYear.setSelection(selectedYear);
            spinnerMonth.setSelection(selectedMonth);

            if(statisticsTotal != null) {
                if (spinnerDistanceUnits.getSelectedItemPosition() == 0) {
                    txtDistance.setText(String.format("%.2f", statisticsTotal.getKms_total()));
                    txtDistanceTitle.setText(getResources().getString(R.string.distance_km_oneline));
                } else {
                    txtDistance.setText(String.format("%.2f", statisticsTotal.getMiles_total()));
                    txtDistanceTitle.setText(getResources().getString(R.string.distance_mi_oneline));
                }
                int duration = statisticsTotal.getDuration_total();
                int hours = (int)duration / 3600000;
                int minutes = (int) (duration % 3600000) / 60000;
                int seconds = (int) ((duration % 3600000) % 60000) / 1000 ;
                txtDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                txtCalories.setText(""+statisticsTotal.getCalories_total());
            }
            if(statisticsList != null && sportPercentages != null) {
                initializeBarChart(statisticsList);
                if(sportPercentages.isEmpty()) {
                    pieChart.setVisibility(View.VISIBLE);
                    pieChart.addPieSlice(new PieModel(0, Color.parseColor(emptyColor)));
                    pieChart.startAnimation();
                }
                else
                    initializePieChart(sportPercentages);
            }
            else
                downloadStatistics();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("sportPercentages", sportPercentages);
        outState.putSerializable("statisticsList", statisticsList);
        outState.putSerializable("statisticsTotal", statisticsTotal);
        outState.putInt("selectedUnits", selectedUnits);
        outState.putInt("selectedYear", selectedYear);
        outState.putInt("selectedMonth", selectedMonth);
    }

    private void downloadStatistics() {
        barChart.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        String myEmail = MainActivity.USER_ME.getEmail();

        String urlStatistics = "";
        if(spinnerMonth.getSelectedItemPosition() == 0)
            urlStatistics = "api/statistics/"+myEmail+"/"+spinnerYear.getSelectedItem().toString();
        else
            urlStatistics = "api/statistics/"+myEmail+"/"+spinnerYear.getSelectedItem().toString() +"/"+spinnerMonth.getSelectedItemPosition();

        ApiClient.getMyStatistics(urlStatistics, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressBar.setVisibility(View.GONE);
                barChart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressBar.setVisibility(View.GONE);
                barChart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressBar.setVisibility(View.GONE);
                barChart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        statisticsList = AnalyzeJSON.analyzeStatistics(response);
                        double kms = response.getJSONObject("data_aux").getDouble("KmsTotal");
                        double miles = response.getJSONObject("data_aux").getDouble("MilesTotal");
                        int duration = response.getJSONObject("data_aux").getInt("DurationTotal");
                        int calories = response.getJSONObject("data_aux").getInt("CaloriesTotal");
                        statisticsTotal = new StatisticsTotal(kms, miles, duration, calories);

                        if (spinnerDistanceUnits.getSelectedItemPosition() == 0) {
                            animateDistance(txtDistance, (int)kms, kms);
                            txtDistanceTitle.setText(getResources().getString(R.string.distance_km_oneline));
                        }
                        else {
                            animateDistance(txtDistance, (int)miles, kms);
                            txtDistanceTitle.setText(getResources().getString(R.string.distance_mi_oneline));
                        }
                        animateDuration(txtDuration, duration);
                        animateCalories(txtCalories, calories);

                        barChart.setVisibility(View.VISIBLE);
                        initializeBarChart(statisticsList);
                    }
                    else{
                        statisticsList = new ArrayList<Statistic>();
                        statisticsTotal = null;
                        if (spinnerDistanceUnits.getSelectedItemPosition() == 0) {
                            txtDistance.setText("0.00");
                            txtDistanceTitle.setText(getResources().getString(R.string.distance_km_oneline));
                        } else {
                            txtDistance.setText("0.00");
                            txtDistanceTitle.setText(getResources().getString(R.string.distance_mi_oneline));
                        }
                        txtDuration.setText("00:00:00");
                        txtCalories.setText("0");
                        initializeBarChart(statisticsList);
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    barChart.setVisibility(View.VISIBLE);
                }
            }
        });

        String urlPercentage = "";
        if(spinnerMonth.getSelectedItemPosition() == 0)
            urlPercentage = "api/statistics/sports-percentage/"+myEmail+"/"+spinnerYear.getSelectedItem().toString();
        else
            urlPercentage = "api/statistics/sports-percentage/"+myEmail+"/"+spinnerYear.getSelectedItem().toString()+"/"+spinnerMonth.getSelectedItemPosition();

        ApiClient.getSportsPercentage(urlPercentage, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                //
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        sportPercentages = AnalyzeJSON.analyzeSportsPercentage(response);
                        initializePieChart(sportPercentages);
                    }
                    else{
                        sportPercentages = new ArrayList<SportPercentage>();
                        pieChart.setVisibility(View.VISIBLE);
                        pieChart.getData().clear();
                        pieChart.addPieSlice(new PieModel(0,Color.parseColor(emptyColor)));
                        pieChart.startAnimation();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeSpinners() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<Integer> listYear = new ArrayList<>();
        for (int i = 2016; i <= currentYear; i++) {
            listYear.add(i);
        }
        ArrayAdapter<Integer> adapterY = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item,listYear);
        spinnerYear.setAdapter(adapterY);

        Resources resources = getResources();
        ArrayList<String> listMonth = new ArrayList<String>();
        listMonth.add(resources.getStringArray(R.array.array_month)[0]);
        listMonth.add(resources.getStringArray(R.array.array_month)[1]);
        listMonth.add(resources.getStringArray(R.array.array_month)[2]);
        listMonth.add(resources.getStringArray(R.array.array_month)[3]);
        listMonth.add(resources.getStringArray(R.array.array_month)[4]);
        listMonth.add(resources.getStringArray(R.array.array_month)[5]);
        listMonth.add(resources.getStringArray(R.array.array_month)[6]);
        listMonth.add(resources.getStringArray(R.array.array_month)[7]);
        listMonth.add(resources.getStringArray(R.array.array_month)[8]);
        listMonth.add(resources.getStringArray(R.array.array_month)[9]);
        listMonth.add(resources.getStringArray(R.array.array_month)[10]);
        listMonth.add(resources.getStringArray(R.array.array_month)[11]);
        listMonth.add(resources.getStringArray(R.array.array_month)[12]);

        ArrayAdapter<String> adapterM = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item,listMonth);
        spinnerMonth.setAdapter(adapterM);

        ArrayList<String> listUnits = new ArrayList<String>();
        listUnits.add(getString(R.string.km_units));
        listUnits.add(getString(R.string.miles_units));

        ArrayAdapter<String> adapterU = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item,listUnits);
        spinnerDistanceUnits.setAdapter(adapterU);

        spinnerYear.setOnItemSelectedListener(this);
        spinnerMonth.setOnItemSelectedListener(this);
        spinnerDistanceUnits.setOnItemSelectedListener(this);
    }

    private void initializePieChart(ArrayList<SportPercentage> list) {
        pieChart.setVisibility(View.VISIBLE);
        float walkingPercentage = 0;
        pieChart.clearChart();

        Resources resources = getResources();
        for (SportPercentage sport : list) {
            switch (sport.getSportType()){
                case 0:
                    pieChart.addPieSlice(new PieModel(resources.getStringArray(R.array.array_activities)[0], (float) sport.getSportPercentage(), Color.parseColor(cyclingColor)));
                    break;
                case 1:
                    pieChart.addPieSlice(new PieModel(resources.getStringArray(R.array.array_activities)[1], (float)sport.getSportPercentage(), Color.parseColor(runningColor)));
                    break;
                case 2:
                    pieChart.addPieSlice(new PieModel(resources.getStringArray(R.array.array_activities)[2], (float)sport.getSportPercentage(), Color.parseColor(joggingColor)));
                    break;
                case 3:
                    walkingPercentage += (float) sport.getSportPercentage();
                    break;
                case 4:
                    walkingPercentage += (float) sport.getSportPercentage();
                    break;
            }
        }

        if(walkingPercentage > 0)
            pieChart.addPieSlice(new PieModel(resources.getStringArray(R.array.array_activities)[5], walkingPercentage, Color.parseColor(walkingColor)));

        pieChart.startAnimation();
    }

    private void initializeBarChart(ArrayList<Statistic> list){
        ArrayList<BarModel> barModelList = new ArrayList<>();

        if(spinnerMonth.getSelectedItemPosition() == 0) {
            Resources resources = getResources();
            for (int i = 0; i < 12; i++) {
                barModelList.add(new BarModel(resources.getStringArray(R.array.array_month_short)[i], 0, barColor));
            }
            for (Statistic statistic : list) {
                if(spinnerDistanceUnits.getSelectedItemPosition() == 0) {
                    barModelList.get(statistic.getDate() - 1).setValue((float) statistic.getDistance_kms());
                }
                else
                    barModelList.get(statistic.getDate()-1).setValue((float)statistic.getDistance_miles());
            }
        }
        else{
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, (int)spinnerYear.getSelectedItem());
            calendar.set(Calendar.MONTH, spinnerMonth.getSelectedItemPosition()-1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 1; i <= daysInMonth; i++) {
                barModelList.add(new BarModel(String.valueOf(i), 0, barColor));
            }
            for (Statistic statistic : list) {
                if(spinnerDistanceUnits.getSelectedItemPosition() == 0)
                    barModelList.get(statistic.getDate()-1).setValue((float)statistic.getDistance_kms());
                else
                    barModelList.get(statistic.getDate()-1).setValue((float)statistic.getDistance_miles());
            }
        }

        barChart.clearChart();
        barChart.addBarList(barModelList);
        barChart.startAnimation();
    }

    private void animateCalories(final TextView txt, int count){
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(0, count);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                txt.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.setDuration(1500);
        animator.start();
    }

    private void animateDistance(final TextView txt, int count, final double finalValue){
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(0, count);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                txt.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                txt.setText(String.format("%.2f", finalValue));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(1500);
        animator.start();
    }

    private void animateDuration(final TextView txt, int count){
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(0, count);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int duration = (int)animation.getAnimatedValue();
                int hours = (int)duration / 3600000;
                int minutes = (int) (duration % 3600000) / 60000;
                int seconds = (int) ((duration % 3600000) % 60000) / 1000 ;
                txt.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });

        animator.setDuration(1500);
        animator.start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spinnerYear:
                if(position != selectedYear){
                    selectedYear = position;
                    downloadStatistics();
                }
                break;
            case R.id.spinnerMonth:
                if(selectedMonth != position){
                    selectedMonth = position;
                    downloadStatistics();
                }
                break;
            case R.id.spinnerDistance:
                if(position != selectedUnits) {
                    selectedUnits = position;
                    if (position == 0) {
                        if (statisticsTotal != null)
                            txtDistance.setText(String.format("%.2f", statisticsTotal.getKms_total()));
                        txtDistanceTitle.setText(getResources().getString(R.string.distance_km_oneline));
                        if (statisticsList != null)
                            initializeBarChart(statisticsList);
                    } else {
                        if (statisticsTotal != null)
                            txtDistance.setText(String.format("%.2f", statisticsTotal.getMiles_total()));
                        txtDistanceTitle.setText(getResources().getString(R.string.distance_mi_oneline));
                        if (statisticsList != null)
                            initializeBarChart(statisticsList);
                    }
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //
    }
}
