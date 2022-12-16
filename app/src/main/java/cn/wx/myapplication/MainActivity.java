package cn.wx.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.config.CMD;
import com.rfid.config.ERROR;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;
import com.util.StringTool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import cn.wx.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.security.InvalidParameterException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "COONECTRS232";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public static ModuleConnector connector = new ReaderConnector();
    private int baud = 115200;
    private Context mcontext;
    private RFIDReaderHelper mReaderHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    OtgUtils.set53CGPIOEnabled(true);
                    SystemClock.sleep(150);
                    if (connector.connectCom("/dev/ttyHSL0", baud, mcontext)) { //sq53c:/dev/ttyHSL0  //sq53:/dev/ttyMSM1  //sq52:dev/ttyUSB0
                        Log.e(TAG, "connectCom: success");
                        Toast.makeText(
                                getApplicationContext(),
                                "connect ok",
                                Toast.LENGTH_SHORT).show();

                        mReaderHelper = RFIDReaderHelper.getDefaultHelper();
                        mReaderHelper.registerObserver(mObserver);
                    }
                } catch (SecurityException e) {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.error_security),
                            Toast.LENGTH_SHORT).show();
                } catch (InvalidParameterException e) {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.error_configuration),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            try {

                String[] strings = StringTool.stringToStringArray("00000000", 2);
                byte[] bytes = StringTool.stringArrayToByteArray(strings, 4);
                int i = mReaderHelper.readTag((byte) -1, (byte) 0x01, (byte) 0, (byte) 1, bytes);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private Handler mLoopHandler = new Handler();

    private RXObserver mObserver = new RXObserver() {
        @Override
        protected void onOperationTag(final RXOperationTag tag) {
            mLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            getApplicationContext(),
                            tag.strEPC,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Runnable mLoopRunnable = new Runnable() {
        @Override
        public void run() {
            mLoopHandler.removeCallbacks(this);
            mReaderHelper.realTimeInventory((byte)-1,(byte)0);
            mLoopHandler.postDelayed(this, 2000);
        }
    };




    @Override
    public void onDestroy() {
        super.onDestroy();
        mReaderHelper.unRegisterObserver(mObserver);
        mLoopHandler.removeCallbacks(mLoopRunnable);
    }

}