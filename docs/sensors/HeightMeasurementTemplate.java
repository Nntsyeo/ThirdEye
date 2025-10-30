package com.example.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * RayNeo XR2 Height Measurement Template
 * 
 * This is a ready-to-use template for implementing height measurement
 * using the RayNeo XR2's barometer sensor.
 * 
 * Features:
 * - Barometer sensor initialization and management
 * - Height measurement relative to calibration point
 * - Automatic sensor lifecycle management
 * - Data filtering to reduce noise
 * - Proper error handling
 * 
 * Usage:
 * 1. Copy this file to your project
 * 2. Update package name and layout references
 * 3. Add UI elements to your layout
 * 4. Call calibrateReference() when user is at starting point
 * 5. Read height values from onHeightChanged() callback
 * 
 * @author RayNeo XR2 Technical Advisory
 * @version 1.0
 */
public class HeightMeasurementTemplate extends Activity implements SensorEventListener {
    
    private static final String TAG = "HeightMeasurement";
    
    // ========== Sensor Components ==========
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private boolean hasPressureSensor = false;
    
    // ========== UI Components ==========
    private TextView pressureTextView;
    private TextView altitudeTextView;
    private TextView heightTextView;
    private TextView statusTextView;
    private Button calibrateButton;
    
    // ========== Measurement State ==========
    private float currentPressure = 0f;
    private float referenceAltitude = 0f;
    private boolean isCalibrated = false;
    
    // ========== Filtering ==========
    private static final int FILTER_SIZE = 5;
    private float[] pressureBuffer = new float[FILTER_SIZE];
    private int bufferIndex = 0;
    private int bufferCount = 0;
    
    // ========================================================================
    // LIFECYCLE METHODS
    // ========================================================================
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // TODO: Update with your layout resource
        // setContentView(R.layout.activity_height_measurement);
        
        // Initialize UI components
        // TODO: Update with your view IDs
        // pressureTextView = findViewById(R.id.pressure_text);
        // altitudeTextView = findViewById(R.id.altitude_text);
        // heightTextView = findViewById(R.id.height_text);
        // statusTextView = findViewById(R.id.status_text);
        // calibrateButton = findViewById(R.id.calibrate_button);
        
        // Set up calibration button
        // calibrateButton.setOnClickListener(v -> calibrateReference());
        
        // Initialize sensor
        initializeSensor();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener when activity becomes visible
        registerSensorListener();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // CRITICAL: Unregister sensor to save battery
        unregisterSensorListener();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up references
        sensorManager = null;
        pressureSensor = null;
    }
    
    // ========================================================================
    // SENSOR INITIALIZATION
    // ========================================================================
    
    /**
     * Initialize the pressure sensor.
     * Checks for sensor availability and logs sensor information.
     */
    private void initializeSensor() {
        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            
            if (sensorManager == null) {
                Log.e(TAG, "SensorManager is null");
                showError("Unable to access sensors");
                return;
            }
            
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            
            if (pressureSensor == null) {
                hasPressureSensor = false;
                Log.e(TAG, "Pressure sensor not available on this device");
                showError("This device does not have a barometer sensor");
                return;
            }
            
            hasPressureSensor = true;
            logSensorInfo();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sensor", e);
            showError("Sensor initialization failed");
        }
    }
    
    /**
     * Log detailed sensor information for debugging.
     */
    private void logSensorInfo() {
        if (pressureSensor != null) {
            Log.i(TAG, "=== Pressure Sensor Info ===");
            Log.i(TAG, "Name: " + pressureSensor.getName());
            Log.i(TAG, "Vendor: " + pressureSensor.getVendor());
            Log.i(TAG, "Version: " + pressureSensor.getVersion());
            Log.i(TAG, "Type: " + pressureSensor.getType());
            Log.i(TAG, "Max Range: " + pressureSensor.getMaximumRange() + " hPa");
            Log.i(TAG, "Resolution: " + pressureSensor.getResolution() + " hPa");
            Log.i(TAG, "Power: " + pressureSensor.getPower() + " mA");
            Log.i(TAG, "Min Delay: " + pressureSensor.getMinDelay() + " μs");
        }
    }
    
    /**
     * Register the sensor listener.
     * Should be called in onResume().
     */
    private void registerSensorListener() {
        if (hasPressureSensor && sensorManager != null && pressureSensor != null) {
            boolean success = sensorManager.registerListener(
                this, 
                pressureSensor,
                SensorManager.SENSOR_DELAY_NORMAL  // 5 Hz - sufficient for height
            );
            
            if (success) {
                Log.d(TAG, "Sensor listener registered successfully");
            } else {
                Log.e(TAG, "Failed to register sensor listener");
            }
        }
    }
    
    /**
     * Unregister the sensor listener.
     * MUST be called in onPause() to save battery.
     */
    private void unregisterSensorListener() {
        if (hasPressureSensor && sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listener unregistered");
        }
    }
    
    // ========================================================================
    // SENSOR EVENT HANDLING
    // ========================================================================
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            // Get raw pressure reading
            float rawPressure = event.values[0];  // in hPa (millibars)
            
            // Apply filtering to reduce noise
            float filteredPressure = filterPressure(rawPressure);
            currentPressure = filteredPressure;
            
            // Calculate altitude using standard atmosphere as reference
            float altitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
                filteredPressure
            );
            
            // Calculate height relative to calibration point
            float relativeHeight = altitude - referenceAltitude;
            
            // Update UI
            updateUI(filteredPressure, altitude, relativeHeight);
            
            // Callback for custom handling
            onHeightChanged(relativeHeight, altitude, filteredPressure);
            
            // Log for debugging
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, String.format(
                    "P: %.2f hPa, Alt: %.2f m, Height: %.2f m",
                    filteredPressure, altitude, relativeHeight
                ));
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String accuracyStr;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyStr = "HIGH";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyStr = "MEDIUM";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyStr = "LOW";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyStr = "UNRELIABLE";
                showWarning("Sensor accuracy is unreliable");
                break;
            default:
                accuracyStr = "UNKNOWN";
        }
        Log.d(TAG, "Sensor accuracy changed: " + accuracyStr);
    }
    
    // ========================================================================
    // CALIBRATION
    // ========================================================================
    
    /**
     * Calibrate the height reference at the current position.
     * Call this when the user is at a known reference point (e.g., ground floor).
     */
    public void calibrateReference() {
        if (!hasPressureSensor) {
            showError("No pressure sensor available");
            return;
        }
        
        if (currentPressure <= 0) {
            showWarning("Waiting for pressure reading...");
            return;
        }
        
        // Calculate reference altitude
        referenceAltitude = SensorManager.getAltitude(
            SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
            currentPressure
        );
        
        isCalibrated = true;
        
        Log.i(TAG, String.format(
            "Height reference calibrated at %.2f m (P: %.2f hPa)",
            referenceAltitude, currentPressure
        ));
        
        showSuccess("Height reference calibrated");
        updateCalibrationStatus();
    }
    
    /**
     * Reset calibration.
     */
    public void resetCalibration() {
        referenceAltitude = 0f;
        isCalibrated = false;
        Log.i(TAG, "Calibration reset");
        updateCalibrationStatus();
    }
    
    /**
     * Check if the sensor is calibrated.
     */
    public boolean isCalibrated() {
        return isCalibrated;
    }
    
    // ========================================================================
    // DATA FILTERING
    // ========================================================================
    
    /**
     * Apply simple moving average filter to reduce noise.
     * 
     * @param newPressure New pressure reading
     * @return Filtered pressure value
     */
    private float filterPressure(float newPressure) {
        // Add to circular buffer
        pressureBuffer[bufferIndex] = newPressure;
        bufferIndex = (bufferIndex + 1) % FILTER_SIZE;
        if (bufferCount < FILTER_SIZE) {
            bufferCount++;
        }
        
        // Calculate average
        float sum = 0;
        for (int i = 0; i < bufferCount; i++) {
            sum += pressureBuffer[i];
        }
        
        return sum / bufferCount;
    }
    
    // ========================================================================
    // UI UPDATES
    // ========================================================================
    
    /**
     * Update UI with current measurements.
     */
    private void updateUI(float pressure, float altitude, float height) {
        if (pressureTextView != null) {
            pressureTextView.setText(String.format("Pressure: %.2f hPa", pressure));
        }
        
        if (altitudeTextView != null) {
            altitudeTextView.setText(String.format("Altitude: %.2f m", altitude));
        }
        
        if (heightTextView != null) {
            heightTextView.setText(String.format("Height: %.2f m", height));
        }
    }
    
    /**
     * Update calibration status display.
     */
    private void updateCalibrationStatus() {
        if (statusTextView != null) {
            if (isCalibrated) {
                statusTextView.setText("Calibrated");
                // TODO: Update text color to green
                // statusTextView.setTextColor(Color.GREEN);
            } else {
                statusTextView.setText("Not Calibrated");
                // TODO: Update text color to red
                // statusTextView.setTextColor(Color.RED);
            }
        }
    }
    
    // ========================================================================
    // USER NOTIFICATIONS
    // ========================================================================
    
    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }
    
    private void showWarning(String message) {
        Toast.makeText(this, "Warning: " + message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, message);
    }
    
    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.i(TAG, message);
    }
    
    // ========================================================================
    // CUSTOM CALLBACKS (Override these in your implementation)
    // ========================================================================
    
    /**
     * Called whenever height measurement is updated.
     * Override this method to implement custom behavior.
     * 
     * @param relativeHeight Height relative to calibration point (meters)
     * @param absoluteAltitude Altitude above sea level (meters)
     * @param pressure Current atmospheric pressure (hPa)
     */
    protected void onHeightChanged(float relativeHeight, float absoluteAltitude, 
                                   float pressure) {
        // TODO: Implement custom height change handling
        // Example: Update AR content, detect floor changes, etc.
    }
    
    // ========================================================================
    // PUBLIC API
    // ========================================================================
    
    /**
     * Get current pressure reading.
     * 
     * @return Current pressure in hPa, or 0 if not available
     */
    public float getCurrentPressure() {
        return currentPressure;
    }
    
    /**
     * Get current altitude above sea level.
     * 
     * @return Altitude in meters
     */
    public float getCurrentAltitude() {
        if (currentPressure > 0) {
            return SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
                currentPressure
            );
        }
        return 0f;
    }
    
    /**
     * Get current height relative to calibration point.
     * 
     * @return Height in meters, or 0 if not calibrated
     */
    public float getCurrentHeight() {
        if (isCalibrated && currentPressure > 0) {
            float altitude = getCurrentAltitude();
            return altitude - referenceAltitude;
        }
        return 0f;
    }
    
    /**
     * Check if pressure sensor is available on this device.
     * 
     * @return true if sensor is available, false otherwise
     */
    public boolean hasPressureSensor() {
        return hasPressureSensor;
    }
}
