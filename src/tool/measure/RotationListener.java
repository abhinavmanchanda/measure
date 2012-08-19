package tool.measure;

import java.text.DecimalFormat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.widget.TextView;

public class RotationListener implements SensorEventListener {


    private double xRotations = 0, totalXAngle;
    private double yRotations = 0, totalYAngle;
    private double zRotations = 0, totalZAngle;

    private long lastUpdate = -1;
    private long lastUIUpdate = -1;
    private TextView rotationTextView;
    private TextView numberOfRotations;

    private float[] gravity, geomagnetic;
    private float[] R = null, I = new float[16];

    private RotationAxis rotationAxis = RotationAxis.NONE;
    private RotationsUIDelegate uiDelegate;


    public RotationListener(TextView rotationTextView, TextView numberOfRotations, RotationsUIDelegate uiDelegate) {
        super();
        this.rotationTextView = rotationTextView;
        this.uiDelegate = uiDelegate;
        if (rotationTextView != null) {
            this.rotationTextView.setText("NONE");
        }
        this.numberOfRotations = numberOfRotations;
        if (numberOfRotations != null) {
            this.numberOfRotations.setText("0.0");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean isGravityFine(float[] gravityValues) {
        double value = Math.sqrt(gravityValues[0] * gravityValues[0] + gravityValues[1] * gravityValues[1] + gravityValues[2] * gravityValues[2]);
        return value < 12.0 && value > 8.0;
    }

    private boolean isMagneticFieldFine(float[] fieldValues) {
        double value = Math.sqrt(fieldValues[0] * fieldValues[0] + fieldValues[1] * fieldValues[1] + fieldValues[2] * fieldValues[2]);
        return value < 65.0 && value > 25.0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] gravityValues = event.values.clone();
            if (isGravityFine(gravityValues))
                gravity = gravityValues;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] magneticFieldValues = event.values.clone();
            if (isMagneticFieldFine(magneticFieldValues))
                geomagnetic = magneticFieldValues;
        }

        if (gravity == null || geomagnetic == null)
            return;


        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms, otherwise updates
            // come way too fast and the phone gets bogged down
            // with garbage collection

            if ((curTime - lastUpdate) > 20) {


                updateAnglesUsingRotationMatrix();

                updateRotations();
                if (rotationAxis == RotationAxis.NONE) {
                    if (Math.abs((Math.round(xRotations))) >= 1) {
                        rotationAxis = RotationAxis.X_AXIS;
                        rotationTextView.setText(rotationAxis.toString());
                    } else if (Math.abs((Math.round(yRotations))) >= 1) {
                        rotationAxis = RotationAxis.Y_AXIS;
                        rotationTextView.setText(rotationAxis.toString());
                    } else if (Math.abs((Math.round(zRotations))) >= 1)
                        rotationAxis = RotationAxis.Z_AXIS;
                    rotationTextView.setText(rotationAxis.toString());
                }

                if (curTime - lastUIUpdate > 100) {
                    DecimalFormat oneDForm = new DecimalFormat("#.#");
                    uiDelegate.invoke(totalXAngle, totalYAngle, totalZAngle);

                    if (rotationAxis == RotationAxis.X_AXIS) {
                        //numberOfRotations.setText(String.valueOf(xRotations/2));
                        numberOfRotations.setText(Double.valueOf(oneDForm.format(xRotations)).toString());
                    } else if (rotationAxis == RotationAxis.Y_AXIS) {
                        //numberOfRotations.setText(String.valueOf(yRotations/2));
                        numberOfRotations.setText(Double.valueOf(oneDForm.format(yRotations)).toString());
                    } else if (rotationAxis == RotationAxis.Z_AXIS) {
                        //numberOfRotations.setText(String.valueOf(zRotations/2));
                        numberOfRotations.setText(Double.valueOf(oneDForm.format(zRotations)).toString());
                    }
                    lastUIUpdate = curTime;
                }

                lastUpdate = curTime;
            }
        }

    }


    private void updateRotations() {
        xRotations = totalXAngle / 180.0;
        yRotations = totalYAngle / 180.0;
        zRotations = totalZAngle / 180.0;

    }


    private void updateAnglesUsingRotationMatrix() {

        try {
            float[] previousR = null;
            if (R != null)
                previousR = R.clone();

            R = new float[16];

            SensorManager.getRotationMatrix(R, I, gravity.clone(), geomagnetic.clone());
            if (previousR != null) {
                float[] previousInverse = new float[R.length];
                Matrix.invertM(previousInverse, 0, previousR, 0);

                float[] delta = new float[previousInverse.length];
                Matrix.multiplyMM(delta, 0, R, 0, previousInverse, 0);
                float[] values = new float[3];
                SensorManager.getOrientation(delta, values);
                values[0] *= 180.0 / Math.PI;
                values[1] *= 180.0 / Math.PI;
                values[2] *= 180.0 / Math.PI;

                totalXAngle += values[1];
                totalYAngle += values[2];
                totalZAngle += values[0];


                //updateAnglesUsingSensorEvent(values);
            }
        } catch (Exception exc) {
            System.out.println(exc);
            throw new RuntimeException(exc);
        }
    }


    private enum RotationAxis {
        NONE, X_AXIS, Y_AXIS, Z_AXIS
    }


}

