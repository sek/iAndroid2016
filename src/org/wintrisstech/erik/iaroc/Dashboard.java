package org.wintrisstech.erik.iaroc;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.irobot.ioio.SimpleIRobotCreate;

/**
 * This is the main activity of the iRobot2012 application.
 *
 * <p>This class assumes that there are 3 ultrasonic sensors attached to the
 * iRobot. An instance of the Dashboard class will display the readings of these
 * three sensors.
 *
 * <p> There should be no need to modify this class. Modify Trabant instead.
 *
 */
public class Dashboard extends Activity {

    /**
     * Tag used for debugging.
     */
    private static final String TAG = "Dashboard";
    /**
     * Text view that contains all logged messages
     */
    private LogTextView mText;
    /**
     * The thread that interacts with the IOIO.
     */
    private IOIOThread ioio_thread_;
    /**
     * A Trabant instance
     */
    private Trabant trabi601;

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Since the android device is carried by the iRobot Create, we want to
         * prevent a change of orientation, which would cause the activity to
         * pause.
         */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        mText = (LogTextView) findViewById(R.id.text);
    }

    /**
     * Called when the application is resumed (also when first started). Here is
     * where we'll create our IOIO thread.
     */
    @Override
    protected void onResume() {
        super.onResume();
        ioio_thread_ = new IOIOThread();
        ioio_thread_.start();
    }

    /**
     * Called when the application is paused. We want to disconnect with the
     * IOIO at this point, as the user is no longer interacting with our
     * application. We also disconnect from the Trabant.
     */
    @Override
    protected void onPause() {
        super.onPause();
        ioio_thread_.abort();
        try {
            ioio_thread_.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * This is the thread that does the IOIO interaction.
     *
     * It first creates a IOIO instance and wait for a connection to be
     * established.
     *
     * Whenever a connection drops, it tries to reconnect, unless this is a
     * result of abort().
     */
    class IOIOThread extends Thread {

        private IOIO ioio_;
        private boolean abort_ = false;
        private boolean connected;

        /**
         * Thread body.
         */
        @Override
        public void run() {
            boolean done = false;
            while (!done) {
                if (isAborted()) {
                    done = true;
                    continue;
                }
                ioio_ = IOIOFactory.create();
                try {
                    /*
                     * Establish connection between the android and the IOIO
                     * board Note: ioio_ is a protected field in the super class
                     */
                    log(getString(R.string.wait_ioio));
                    ioio_.waitForConnect();
                    connected = true;
                    log(getString(R.string.ioio_connected));

                    /*
                     * Establish communication between the android and the
                     * iRobot Create through the IOIO board.
                     */
                    log(getString(R.string.wait_create));
                    IRobotCreateInterface iRobotCreate = new SimpleIRobotCreate(ioio_);
                    log(getString(R.string.create_connected));

                    /*
                     * Get a Trabant (built on the iRobot Create) and let it
                     * go... The ioio_ instance is passed to the constructor in
                     * case it is needed to establish connections to other
                     * peripherals, such as sensors that are not part of the
                     * iRobot Create.
                     */
                    trabi601 = new Trabant(ioio_, iRobotCreate, Dashboard.this);
                    trabi601.go();
                    //trabi601 is done
                    log("Shutting down ...");
                    trabi601.shutDown();
                    done = true;
                } catch (ConnectionLostException ex) {
                    log("Connection Lost: " + ex.getMessage());
                } catch (Exception ex) {
                    Log.e(TAG, "Unexpected exception caught", ex);
                    log("Unexpected exception caught: " + ex.getMessage());
                    abort();
                    done = true;
                } finally {
                    try {
                        ioio_.disconnect();
                        ioio_.waitForDisconnect();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        /**
         * Abort the connection.
         *
         * This is a little tricky synchronization-wise: we need to be handle
         * the case of abortion happening before the IOIO instance is created or
         * during its creation.
         */
        synchronized public void abort() {
            abort_ = true;
            if (ioio_ != null && connected) {
                ioio_.disconnect();
            }
        }

        synchronized private boolean isAborted() {
            return abort_;
        }
    }

    /**
     * Writes a message to the Dashboard instance.
     *
     * @param msg the message to write
     */
    public void log(final String msg) {
        runOnUiThread(new Runnable() {

            public void run() {
                mText.append(msg);
                mText.append("\n");
            }
        });
    }
}