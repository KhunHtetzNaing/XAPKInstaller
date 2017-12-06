package com.htetznaing.xapkinstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<String> fileSize = new ArrayList<>();

    private File root;
    String apk;
    private ArrayList<File> fileList = new ArrayList<File>();
    ProgressDialog progressDialog;

    AdView banner;
    AdRequest adRequest;
    InterstitialAd interstitialAd;
    myAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermissions()==true){
            startWork();
        }else{
            Toast.makeText(this, "You need to Allow WRITE STORAGE Permission!", Toast.LENGTH_SHORT).show();
        }
    }

    public void startWork(){
        try {
            Intent receivedIntent = getIntent();
            String receivedAction = receivedIntent.getAction();
            if (receivedAction.equals(Intent.ACTION_SEND)) {
                Uri gUri = (Uri)getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (gUri !=null){
                    final String ss = new PathUtils().getPath(this,gUri);
                    if (ss.endsWith(".xapk") || ss.endsWith(".XAPK")){
                        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                        b.setTitle("Attention!");
                        b.setMessage("Do you want to Install ?");
                        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new work(new File(ss)).execute();
                            }
                        });
                        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showAD();
                            }
                        });
                        AlertDialog d = b.create();
                        d.show();
                    }
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "something was wrong!", Toast.LENGTH_SHORT).show();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait!");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Working...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        adRequest = new AdRequest.Builder().build();
        banner = (AdView) findViewById(R.id.adView);
        banner.loadAd(adRequest);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-2502552457553139/1611097866");
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadAD();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                loadAD();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                loadAD();
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listOn();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Attention!");
                b.setMessage("Do you want to Install ?");
                final int o = i;
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new work(fileList.get(o)).execute();
                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                    }
                });
                AlertDialog d = b.create();
                d.show();
            }
        });
    }

    private boolean checkPermissions() {
        int REQUEST_ID_MULTIPLE_PERMISSIONS = 5217;
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            Log.d("TAG","Permission"+"\n"+String.valueOf(false));
            return false;
        }
        Log.d("Permission","Permission"+"\n"+String.valueOf(true));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5217: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startWork();
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow WRITE STORAGE Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public String getSize(File file){
        float sizeInBytes = file.length();
        float sizeInMb = sizeInBytes / (1024 * 1024);

        String size = String.valueOf(sizeInMb);
        size = size.substring(0,3)+" MB";
        Log.d("FileSize",size);
        return size;
    }

    public void loadAD(){
        if (!interstitialAd.isLoaded()){
            interstitialAd.loadAd(adRequest);
        }
    }

    public void showAD(){
        if (interstitialAd.isLoaded()){
            interstitialAd.show();
        }else{
            interstitialAd.loadAd(adRequest);
        }
    }

    public void listOn(){
        ArrayList<String> path = new ArrayList<>();

        try {
            if (getExternalStorageDirectories().length>0){
            String lol = getExternalStorageDirectories()[0];
            if (lol.isEmpty()&&!lol.equals(null)){
            }else{
                path.add(lol);
            }}
        }catch (Exception e){

        }

        path.add(Environment.getExternalStorageDirectory().getAbsolutePath());

        //getting SDcard root path
        for (int i =0;i<path.size();i++){
            root = new File(path.get(i));
            fileList = getfile(root);
        }

        for (int i = 0; i < fileList.size(); i++) {
            fileSize.add(getSize(fileList.get(i)));
            arrayList.add(fileList.get(i).getName());
            adapter = new myAdapter(this, arrayList,fileSize);
            listView.setAdapter(adapter);
        }
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    getfile(listFile[i]);

                } else {
                    if (listFile[i].getName().endsWith(".xapk")
                            || listFile[i].getName().endsWith(".XAPK"))
                    {
                        fileList.add(listFile[i]);
                    }
                }

            }
        }
        return fileList;
    }

    /* returns external storage paths (directory of external memory card) as array of Strings */
    public String[] getExternalStorageDirectories() {
        String LOG_TAG = "SDCARD";

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d(LOG_TAG, results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    public class work extends AsyncTask<String,String,String>{
        File lol;
        public work(File lol){
            this.lol=lol;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            String btn = null;
            if (s.equals("true")){
                String name = readTextFile("/sdcard/XAPK_Installer/manifest.json");
                name = name.substring(name.indexOf("\"name\":"),name.lastIndexOf("\"locales_name\":"));
                name = name.replace("\"name\":\"","");
                name = name.replace("\",","");
                s = "Now, you need to Install"+"\n"+name+" APK";
                btn = "Install";

                File n = new File("/sdcard/XAPK_Installer/");
                File [] n1 = n.listFiles();
                for (int ii=0;ii<n1.length;ii++){
                    if (n1[ii].toString().endsWith(".apk")){
                        apk = n1[ii].toString();
                    }
                }
            }else{
                btn = "OK";
                s = "Failed!";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Attention!");
            builder.setMessage(s);
            final String finalBtn = btn;
            builder.setPositiveButton(btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAD();
                    if (finalBtn.equals("Install")){
                        File toInstall = new File(apk);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            MainActivity.this.startActivity(intent);
                        } else {
                            Uri apkUri = Uri.fromFile(toInstall);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainActivity.this.startActivity(intent);
                        }

                        Fucker my = new Fucker();
                        my.deleteDirectory("/sdcard/XAPK_Installer/Android");
                        my.deleteFile("/sdcard/XAPK_Installer/manifest.json");
                        my.deleteFile("/sdcard/XAPK_Installer/icon.png");
                    }
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            Fucker my = new Fucker();
            my.deleteDirectory("/sdcard/XAPK_Installer");

            File file = new File("/sdcard/XAPK_Installer/");
            if (!file.exists()){
                file.mkdir();
            }
            boolean b = my.unZip(lol.toString(),file.toString());
            try {
                copyDirectory(new File("/sdcard/XAPK_Installer/Android"),new File("/sdcard/Android/"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return String.valueOf(b);
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


    public String readTextFile(String path) {
        File file = new File(path);
//Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.browse) {
            Intent i = new Intent(this, FilePicker.class);
            startActivityForResult(i, 5217);
        }

        if (id==R.id.refresh){
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 5217 && resultCode == Activity.RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            File file = null;
            for (Uri uri: files) {
                file = Utils.getFileForUri(uri);
            }

            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setTitle("Attention!");
            b.setMessage("Do you want to Install ?");
            final File finalFile = file;
            b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new work(finalFile).execute();
                }
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAD();
                }
            });
            AlertDialog d = b.create();
            d.show();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attention!");
        builder.setMessage("Do you want to Exit ?");
        builder.setIcon(R.drawable.icon);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showAD();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showAD();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}