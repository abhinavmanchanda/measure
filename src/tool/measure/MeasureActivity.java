package tool.measure;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class MeasureActivity extends Activity {

    private SensorManager sensorMgr;
    private RotationListener rotationListener;


    private boolean lockKeys = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_new);

            final Button startButton = (Button) findViewById(R.id.Button01);
            final Button stopButton = (Button) findViewById(R.id.Button02);

            startButton.setEnabled(true);
            stopButton.setEnabled(false);

            startButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                        startButton.setEnabled(false);
                        stopButton.setEnabled(true);

                        lockKeys = true;


                        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
                        List<Sensor> sensors = sensorMgr.getSensorList(
                                Sensor.TYPE_ORIENTATION);

                        Sensor gravitySensor = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
                        Sensor magneticSensor = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);

                    rotationListener = new RotationListener((TextView) findViewById(R.id.TextView05), (TextView) findViewById(R.id.TextView07), rotationsUIDelegate());


                        boolean accelSupported = sensorMgr.registerListener(rotationListener,
                                sensors.get(0),
                                SensorManager.SENSOR_DELAY_UI);


                        boolean gravitySupported = sensorMgr.registerListener(rotationListener,
                                gravitySensor,
                                SensorManager.SENSOR_DELAY_UI);


                        boolean magneticFieldSupported = sensorMgr.registerListener(rotationListener,
                                magneticSensor,
                                SensorManager.SENSOR_DELAY_UI);

                        if (!accelSupported || !gravitySupported || !magneticFieldSupported) {
                            sensorMgr.unregisterListener(rotationListener);
                        }

                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    sensorMgr.unregisterListener(rotationListener);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    lockKeys = false;
                }
            });



    }

    private RotationsUIDelegate rotationsUIDelegate() {
        return new RotationsUIDelegate() {
            @Override
            public void invoke(double xAngle, double yAngle, double zAngle) {
                DecimalFormat oneDForm = new DecimalFormat("#.#");
                setTextToViewWithId(R.id.x_rotations, oneDForm.format(xAngle));
                setTextToViewWithId(R.id.y_rotations, oneDForm.format(yAngle));
                setTextToViewWithId(R.id.z_rotations, oneDForm.format(zAngle));
            }
        };
    }

    private void setTextToViewWithId(int textViewId, String text) {
        ((TextView)findViewById(textViewId)).setText(text);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (lockKeys && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    Button stopButton = (Button) findViewById(R.id.Button02);
                    stopButton.performClick();
                    return true;
                default:
                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

}