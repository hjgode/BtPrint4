package hgo.btprint4;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by E841719 on 18.12.2014.
 */
public class FileBrowserActivity extends ListActivity {

    private static final String TAG = "FileBrowserActivity";

    private String path;
    // Return Intent extra
    public static String EXTRA_FILE_NAME = "selected_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_demofiles);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Use the current directory as title
        path = "/";
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }

        fillList(path);
    }

    void fillList(String sPath){
        // Read all files sorted into the values-array
        List values = new ArrayList();
        File dir = new File(sPath);
        setTitle(sPath);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);

    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
            fillList(filename);
//            Intent intent = new Intent(this, FileBrowserActivity.class);
//            intent.putExtra("path", filename);
//            startActivity(intent);
        } else {
            //Toast.makeText(this, filename + " is not a directory", Toast.LENGTH_LONG).show();
            addLog("file '"+filename+"' selected");
            Intent intent = new Intent();
            intent.putExtra(EXTRA_FILE_NAME, filename);

            addLog("setResult=OK");
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
    // The on-click listener for all items in the ListViews
    private AdapterView.OnItemClickListener mFileClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            addLog("OnItemClickListener()");

            String info = ((TextView) v).getText().toString();

            // Create the result Intent and include the file name
            Intent intent = new Intent();
            intent.putExtra(EXTRA_FILE_NAME, info);

            addLog("setResult=OK");
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    public void addLog(String s) {
        Log.d(TAG, s);
    }

}