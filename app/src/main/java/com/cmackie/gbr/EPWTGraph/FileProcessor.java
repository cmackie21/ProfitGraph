package com.cmackie.gbr.EPWTGraph;

import android.content.Context;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileProcessor extends Thread {
    /*This class will process an EPWT CSV file requested by the user and save it to the Android internal storage.
    Code from lines 13-66 has been interpreted from code given by Dr Paul Cockshott and used with his permission.
      */
    Context app_context;
    URL file_url;
    String file_name;

    public FileProcessor(URL file, Context activity_context, String guess_file) {
        //Constructor

        app_context = activity_context;
        file_url = file;
        file_name = guess_file;
    }

    public void run() {
        processFile();
    }

    public void processFile() {
        //This method will open the user given URL, set up the CSV Reader and Writer objects and then process the data before writing to the file in internal storage
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file_url.openStream()));
            CSVReader reader = new CSVReader(in, ';');
            FileOutputStream file_out = app_context.openFileOutput(file_name, Context.MODE_PRIVATE);
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(file_out), ';');
            String[] columns = {"Country", "ID", "qual", "year", "pop", "fert", "birth", "mort", "N=workforce", "dN", "K", "c", "v", "s", "gdp", "delta", "gamma", "alpha", "r", "requil", "s/v", "K/v"};
            java.util.List<String[]> file = reader.readAll();
            reader.close();
            ArrayList<String[]> epwt = new ArrayList<>();
            epwt.add(columns);
            DecimalFormat df = new DecimalFormat("0.00");
            for (int j = 0; j < file.size(); j++) {
                String[] line = file.get(j);
                if (line[3].equals("A") || line[3].equals("B") || line[0].equals("China")) {
                    double dn = 0;
                    int temp_pop = Integer.parseInt(line[4]);
                    temp_pop = temp_pop * 1000;
                    int temp_worker = Integer.parseInt(line[5]);
                    if (temp_worker > temp_pop) {
                        //Swap pop and worker figures as some are in wrong place
                        String temp = line[4];
                        line[4] = line[5];
                        line[5] = temp;
                    }
                    //Population figures
                    int pop = Integer.parseInt(line[4]);
                    pop = pop * 1000;
                    //Number of workers in employment
                    int n = Integer.parseInt(line[5]);
                    //Fertility - child per woman
                    double fertility = 0;
                    if (line[6].equals("")) {
                        //Do nothing
                    } else {
                        fertility = Double.parseDouble(line[6]);
                    }
                    //Birth rate per person per year
                    double birth = 0;
                    if (line[7].equals("")) {
                        //Do nothing
                    } else {
                        birth = (Double.parseDouble(line[7]) / 1000);
                    }
                    //Mortality rate per person per year
                    double mortality = 0;
                    if (line[8].equals("")) {
                        //Do nothing
                    } else {
                        mortality = (Double.parseDouble(line[8]) / 1000);
                    }
                    //GDP
                    long gdp = Long.parseLong(line[9]);
                    //Capital stock 2005 US$
                    long k = Long.parseLong(line[10]);
                    //Capital used up
                    long c;
                    if (line[11].equals("")) {
                        continue;
                    } else {
                        c = Long.parseLong(line[11]);
                    }
                    double ws;
                    if (line[19].equals("")) {
                        continue;
                    } else {
                        ws = Double.parseDouble(line[19]);
                    }
                    double ipw = Double.parseDouble(line[22]);
                    //V = variable capital
                    double v = ws * gdp;
                    //S = surplus value
                    double s = gdp - v - c;
                    //Delta - depreciation rate of capital stock
                    double delta;
                    if (line[12].equals("")) {
                        continue;
                    } else {
                        delta = (Double.parseDouble(line[12]) / 100);
                    }
                    double gamma;
                    //Rate of growth of labour productivity
                    if (line[26].equals("")) {
                        continue;
                    } else {
                        gamma = (Double.parseDouble(line[26]) / 100);
                    }
                    //Accumulation
                    double acc = n * ipw;
                    //Rate of profit
                    double r = s / k;
                    double s_v = 0;
                    double k_v = 0;
                    if (v > 0) {
                        //Rate of surplus value
                        s_v = s / v;
                        //Organic compositoon of capital
                        k_v = k / v;
                    }
                    if (j != (file.size() - 1)) {
                        if (line[0].equals(file.get(j + 1)[0])) {
                            //We are not in the last year of a country so process
                            //DN - growth of workforce
                            dn = Double.parseDouble(file.get(j + 1)[5]);
                            dn = (dn - n) / n;
                            double new_acc = (Double.parseDouble(file.get(j + 1)[10]));
                            acc = new_acc - k + c;
                        }
                    }
                    //Alpha - accumulation of capital stock
                    double alpha = acc / s;
                    //Equilibrium Rate of Profit  - Growth of workforce + Depreciation of capital stock + Growth of Labour Productivity/Accumulation of capital stock
                    double requil = (dn + delta + gamma) / alpha;
                    //Prepare a single line of the processed file and add line to arraylist
                    String[] nextline = new String[]{line[0], line[1], line[2], line[3], String.valueOf(pop),
                            String.valueOf(fertility), String.valueOf(birth), String.valueOf(mortality), String.valueOf(n), String.valueOf(dn), String.valueOf(k),
                            String.valueOf(c), String.valueOf(v), String.valueOf(s), String.valueOf(gdp), String.valueOf(df.format(delta)), String.valueOf(df.format(gamma)), String.valueOf(df.format(alpha)), String.valueOf(df.format(r)),
                            String.valueOf(df.format(requil)), String.valueOf(df.format(s_v)), String.valueOf(df.format(k_v))};
                    epwt.add(nextline);
                } else {
                    //We are not interested in processing this line - the quality is not good enough
                    continue;
                }
            }
            //Write to the processed file (false so no quote characters are written) and close the writer
            writer.writeAll(epwt, false);
            writer.close();
        } catch (IOException e) {
            Toast.makeText(app_context, "Error with file.", Toast.LENGTH_SHORT).show();
        }
    }
}


