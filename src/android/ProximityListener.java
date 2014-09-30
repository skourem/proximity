package gr.wizzup.proximity;

import java.util.List;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

import android.os.Handler;
import android.os.Looper;

/**
 * This class listens to the proximity sensor and stores the latest proximityFlag value.
 */
public class ProximityListener extends CordovaPlugin implements SensorEventListener {

    public static int STOPPED = 0;
    public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;

    public long TIMEOUT = 30000;        // Timeout in msec to shut off listener

    int status;                         // status of listener
    int proximityFlag;                      // most recent proximityFlag value
    //long timeStamp;                     // time of most recent value
    //long lastAccessTime;                // time the value was last retrieved
    //int accuracy;                       // accuracy of the sensor

    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;                     // proximity sensor returned by sensor manager

    private CallbackContext callbackContext;

    /**
     * Constructor.
     */
    public ProximityListener() {
        this.proximityFlag = 0;
        //this.timeStamp = 0;
        this.setStatus(ProximityListener.STOPPED);
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.sensorManager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action                The action to execute.
     * @param args                  JSONArry of arguments for the plugin.
     * @param callbackS=Context     The callback id used when calling back into JavaScript.
     * @return                      True if the action was valid.
     * @throws JSONException 
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            this.start();
        }
        else if (action.equals("stop")) {
            this.stop();
        }
        else if (action.equals("getStatus")) {
            int i = this.getStatus();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, i));
        }
        else if (action.equals("getProximityFlag")) {
            // If not running, then this is an async call, so don't worry about waiting
            if (this.status != ProximityListener.RUNNING) {
                int r = this.start();
                if (r == ProximityListener.ERROR_FAILED_TO_START) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, ProximityListener.ERROR_FAILED_TO_START));
                    return true;
                }
                // Set a timeout callback on the main thread.
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        ProximityListener.this.timeout();
                    }
                }, 2000);
            }
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, getProximityFlag()));
        }
        else if (action.equals("setTimeout")) {
            this.setTimeout(args.getLong(0));
        }
        else if (action.equals("getTimeout")) {
            long l = this.getTimeout();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, l));
        } else {
            // Unsupported action
            return false;
        }
        return true;
    }

    /**
     * Called when listener is to be shut down and object is being destroyed.
     */
    public void onDestroy() {
        this.stop();
    }

    /**
     * Called when app has navigated and JS listeners have been destroyed.
     */
    public void onReset() {
        this.stop();
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Start listening for proximity sensor.
     *
     * @return          status of listener
     */
    public int start() {

        // If already starting or running, then just return
        if ((this.status == ProximityListener.RUNNING) || (this.status == ProximityListener.STARTING)) {
            return this.status;
        }

        // Get proximity sensor from sensor manager
        @SuppressWarnings("deprecation")
        List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);

        // If found, then register as listener
        if (list != null && list.size() > 0) {
            this.mSensor = list.get(0);
            this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            //this.lastAccessTime = System.currentTimeMillis();
            this.setStatus(ProximityListener.STARTING);
        }

        // If error, then set status to error
        else {
            this.setStatus(ProximityListener.ERROR_FAILED_TO_START);
        }

        return this.status;
    }

    /**
     * Stop listening to proximity sensor.
     */
    public void stop() {
        if (this.status != ProximityListener.STOPPED) {
            this.sensorManager.unregisterListener(this);
        }
        this.setStatus(ProximityListener.STOPPED);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    /**
     * Called after a delay to time out if the listener has not attached fast enough.
     */
    private void timeout() {
        if (this.status == ProximityListener.STARTING) {
            this.setStatus(ProximityListener.ERROR_FAILED_TO_START);
            if (this.callbackContext != null) {
                this.callbackContext.error("proximity listener failed to start.");
            }
        }
    }

    /**
     * Sensor listener event.
     *
     * @param SensorEvent event
     */
    public void onSensorChanged(SensorEvent event) {

        // We only care about the orientation as far as it refers to Magnetic North
        int proximityFlag = (int) event.values[0];

        // Save proximityFlag
        //this.timeStamp = System.currentTimeMillis();
        this.proximityFlag = proximityFlag;
        this.setStatus(ProximityListener.RUNNING);

        // If proximityFlag hasn't been read for TIMEOUT time, then turn off proximity sensor to save power
        //if ((this.timeStamp - this.lastAccessTime) > this.TIMEOUT) {
          //  this.stop();
        //}
    }

    /**
     * Get status of proximity sensor.
     *
     * @return          status
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Get the most recent proximity proximityFlag.
     *
     * @return          proximityFlag
     */
    public float getProximityFlag() {
        //this.lastAccessTime = System.currentTimeMillis();
        return this.proximityFlag;
    }

    /**
     * Set the timeout to turn off proximity sensor if getproximityFlag() hasn't been called.
     *
     * @param timeout       Timeout in msec.
     */
    public void setTimeout(long timeout) {
        this.TIMEOUT = timeout;
    }

    /**
     * Get the timeout to turn off proximity sensor if getproximityFlag() hasn't been called.
     *
     * @return timeout in msec
     */
    public long getTimeout() {
        return this.TIMEOUT;
    }

    /**
     * Set the status and send it to JavaScript.
     * @param status
     */
    private void setStatus(int status) {
        this.status = status;
    }

    /**
     * Create the proximityproximityFlag JSON object to be returned to JavaScript
     *
     * @return a proximity proximityFlag
     */
//    private JSONObject getproximityFlag() throws JSONException {
//        JSONObject obj = new JSONObject();
//
//        obj.put("magneticproximityFlag", this.getproximityFlag());
//        //obj.put("trueproximityFlag", this.getproximityFlag());
//        // Since the magnetic and true proximityFlag are always the same our and accuracy
//        // is defined as the difference between true and magnetic always return zero
//        //obj.put("proximityFlagAccuracy", 0);
//        //obj.put("timestamp", this.timeStamp);
//
//        return obj;
//    }

}
