package hgo.btprint4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class DemoListActivity extends Activity {
    // Debugging
    private static final String TAG = "DemoListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_FILE_NAME = "selected_file";

    // Member fields
    private AssetFiles mAssetFiles;
    ArrayAdapter<String> mFilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_list);
        mAssetFiles=new AssetFiles(this);
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        //bind TextView to string
        mFilesAdapter=new ArrayAdapter<String>(this, R.layout.demo_filename);

        // Find and set up the ListView for paired devices
        ListView filesListView = (ListView) findViewById(R.id.ListViewFileName);

        // we register for the contextmneu
        registerForContextMenu(filesListView);

        filesListView.setAdapter(mFilesAdapter);
        filesListView.setOnItemClickListener(mFileClickListener);
        getFiles();
        addLog("+++OnCreate+++ DONE");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // We want to create a context Menu when the user long click on an item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // We know that each row in the adapter is a Map
        //HashMap map =  (HashMap) simpleAdpt.getItem(aInfo.position);

        //menu.setHeaderTitle("Options for " + map.get("planet"));
        menu.add(1, 1, 1, "Details");
        menu.add(1, 2, 2, "Delete");

    }
    //- See more at: http://www.survivingwithandroid.com/2012/09/listviewpart-1.html#sthash.PpsRem7j.dpuf

    @Override
    protected void onDestroy(){
        super.onDestroy();
        addLog("+++OnDestroy+++");
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

    void getFiles(){
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("scanning...");
        for(String s : mAssetFiles._files){
            mFilesAdapter.add(s);
        }
        setProgressBarIndeterminateVisibility(false);
        setTitle(R.string.select_file);
    }

    public void addLog(String s) {
        Log.d(TAG, s);
    }

}
