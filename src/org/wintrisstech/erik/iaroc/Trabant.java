package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;

/**
 * A Trabant is an implementation of the IRobotCreateInterface.
 *
 * @author Erik
 */
public class Trabant extends IRobotCreateAdapter
{
    private static final String TAG = "Ferrari";
    private final Dashboard dashboard;
    /*
     * State variables:
     */
    private int speed = 100; // The normal speed of the Trabant when going straight
    private boolean running = true;

    /**
     * Constructs a Trabant, an amazing machine!
     *
     * @param ioio the IOIO instance that the Trabant can use to communicate
     * with other peripherals such as sensors
     * @param create an implementation of an iRobot
     * @param dashboard the Dashboard instance that is connected to the Trabant
     * @throws ConnectionLostException
     */
    public Trabant(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException
    {
        super(create);
        this.dashboard = dashboard;
        song(0, new int[]
                {
                    58, 10
                });
    }

    /**
     * Main method that gets the Trabant going.
     *
     */
    public void go() throws InterruptedException
    {
        dashboard.log("Running ...");
        while (isRunning())
        {
            try
            {
                readSensors(SENSORS_GROUP_ID6);
                SystemClock.sleep(10000);
                playSong(0);
            } catch (ConnectionLostException ex)
            {
                dashboard.log("Connection Lost Exception in go()");
            }
        }
        dashboard.log("Run completed.");
    }

    /**
     * Closes down all the connections of the Trabant, including the connection
     * to the iRobot Create and the connections to all the sensors.
     */
    public void shutDown()
    {
        closeConnection(); // close the connection to the Create
    }

    /**
     * Checks if the Trabant is running
     *
     * @return true if the Trabant is running
     */
    public synchronized boolean isRunning()
    {
        return running;
    }

    private synchronized void setRunning(boolean b)
    {
        running = false;
    }
}
