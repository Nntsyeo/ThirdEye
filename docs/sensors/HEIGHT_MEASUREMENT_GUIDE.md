# RayNeo XR2 Height Measurement Technical Guide

## Executive Summary

This guide provides comprehensive technical documentation for measuring wearer height using the RayNeo XR2 augmented reality device sensors. The RayNeo XR2, powered by the Qualcomm Snapdragon XR2 platform and running Android 12, includes a barometer/pressure sensor ideal for altitude and height measurement applications.

---

## 1. Available Sensors on RayNeo XR2

### Hardware Specifications

The RayNeo XR2 includes the following built-in sensors confirmed by official specifications:

- **Pressure Sensor (Barometer)** - Primary sensor for height measurement
- **Accelerometer** - 3-axis acceleration measurement
- **Gyroscope** - 3-axis rotation rate measurement
- **Compass (Magnetometer)** - Magnetic field direction

### Platform Details

- **Platform**: Qualcomm Snapdragon XR2
- **Operating System**: Android 12 (API Level 31)
- **Tracking**: 6DoF (6 Degrees of Freedom) environmental recognition
- **Memory**: 6 GB RAM + 128 GB storage

---

## 2. Barometer/Pressure Sensor Technical Specifications

### Sensor Characteristics

**Type**: Hardware-based environmental sensor

**Android Sensor Type**: `Sensor.TYPE_PRESSURE`

**Measurement Units**: 
- Hectopascals (hPa)
- Millibars (mbar) - equivalent to hPa

**Typical Accuracy**: 
- ±1-2 hPa typical accuracy (varies by manufacturer)
- Translates to approximately ±8-20 meters altitude error
- Error range can vary from -20 to +20 meters in practice

**Update Rates**:
- `SENSOR_DELAY_NORMAL`: ~200,000 microseconds (5 Hz)
- `SENSOR_DELAY_UI`: ~60,000 microseconds (~16 Hz)
- `SENSOR_DELAY_GAME`: ~20,000 microseconds (50 Hz)
- `SENSOR_DELAY_FASTEST`: Varies by device

**Note**: For Android 12+ apps, motion and position sensor rates are limited to 200 Hz for standard access. To exceed this, declare the `HIGH_SAMPLING_RATE_SENSORS` permission.

### Common Barometer Sensors in Mobile/XR Devices

- BMP280 (Bosch)
- BMP180/BMP182 (Bosch)
- LPS331AP (STMicroelectronics)

The specific sensor model in the Snapdragon XR2 platform is not publicly documented, but it follows standard barometric pressure measurement principles.

---

## 3. Accessing the Barometer via Android 12 APIs

### Required Permissions

**Good News**: The pressure sensor does NOT require dangerous permissions like `BODY_SENSORS`. It is an environmental sensor accessible without runtime permissions.

**Optional Feature Declaration** (in AndroidManifest.xml):
```xml
<!-- Declare if your app requires a barometer -->
<uses-feature 
    android:name="android.hardware.sensor.barometer"
    android:required="false" />
```

Set `android:required="false"` if the feature is optional, allowing installation on devices without barometers.

### Complete Implementation Example

```java
package com.example.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class HeightMeasurementActivity extends Activity implements SensorEventListener {
    
    private static final String TAG = "HeightMeasurement";
    
    // Sensor components
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private boolean hasPressureSensor = false;
    
    // UI elements
    private TextView pressureTextView;
    private TextView altitudeTextView;
    private TextView heightTextView;
    
    // Measurement data
    private float currentPressure = 0f;
    private float referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
    private float referenceAltitude = 0f;
    private boolean isCalibrated = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_measurement);
        
        // Initialize UI components
        pressureTextView = findViewById(R.id.pressure_text);
        altitudeTextView = findViewById(R.id.altitude_text);
        heightTextView = findViewById(R.id.height_text);
        
        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        // Check for pressure sensor availability
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            hasPressureSensor = true;
            Log.i(TAG, "Pressure sensor found: " + pressureSensor.getName());
        } else {
            hasPressureSensor = false;
            Toast.makeText(this, "No pressure sensor available on this device", 
                          Toast.LENGTH_LONG).show();
            Log.e(TAG, "No pressure sensor available");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener when activity is active
        if (hasPressureSensor) {
            sensorManager.registerListener(this, pressureSensor, 
                                          SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Pressure sensor listener registered");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to save battery when activity is not visible
        if (hasPressureSensor) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Pressure sensor listener unregistered");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up references
        sensorManager = null;
        pressureSensor = null;
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            // Get pressure reading in hPa (millibars)
            currentPressure = event.values[0];
            
            // Calculate altitude using standard atmosphere as reference
            float altitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
                currentPressure
            );
            
            // Calculate height relative to calibration point
            float relativeHeight = altitude - referenceAltitude;
            
            // Update UI
            pressureTextView.setText(String.format("Pressure: %.2f hPa", currentPressure));
            altitudeTextView.setText(String.format("Altitude: %.2f m", altitude));
            heightTextView.setText(String.format("Height: %.2f m", relativeHeight));
            
            Log.d(TAG, String.format("P: %.2f hPa, Alt: %.2f m, Height: %.2f m", 
                                    currentPressure, altitude, relativeHeight));
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
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
                break;
            default:
                accuracyStr = "UNKNOWN";
        }
        Log.d(TAG, "Sensor accuracy changed: " + accuracyStr);
    }
    
    /**
     * Calibrate the height measurement at current position.
     * Call this when the user is at a known reference point.
     */
    public void calibrateHeightReference() {
        if (hasPressureSensor && currentPressure > 0) {
            referenceAltitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
                currentPressure
            );
            isCalibrated = true;
            Toast.makeText(this, "Height reference calibrated", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Calibrated reference altitude: " + referenceAltitude + " m");
        }
    }
    
    /**
     * Advanced calibration using GPS altitude.
     * Use this for more accurate absolute altitude measurements.
     */
    public void calibrateWithGPS(float gpsAltitude) {
        if (hasPressureSensor && currentPressure > 0) {
            // Calculate what the sea-level pressure should be
            // given the current pressure and known GPS altitude
            referencePressure = (float) (currentPressure / 
                Math.pow(1.0 - (gpsAltitude / 44330.0), 5.255));
            referenceAltitude = gpsAltitude;
            isCalibrated = true;
            Log.i(TAG, String.format("GPS calibration: Alt=%.2f m, P0=%.2f hPa", 
                                    gpsAltitude, referencePressure));
        }
    }
    
    /**
     * Get altitude using calibrated reference pressure.
     */
    public float getCalibratedAltitude() {
        if (hasPressureSensor && currentPressure > 0) {
            return SensorManager.getAltitude(referencePressure, currentPressure);
        }
        return 0f;
    }
}
```

### Kotlin Implementation Example

```kotlin
package com.example.thirdeye

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class HeightMeasurementActivity : Activity(), SensorEventListener {
    
    companion object {
        private const val TAG = "HeightMeasurement"
    }
    
    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null
    private var hasPressureSensor = false
    
    private lateinit var pressureTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var heightTextView: TextView
    
    private var currentPressure = 0f
    private var referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
    private var referenceAltitude = 0f
    private var isCalibrated = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_height_measurement)
        
        // Initialize UI
        pressureTextView = findViewById(R.id.pressure_text)
        altitudeTextView = findViewById(R.id.altitude_text)
        heightTextView = findViewById(R.id.height_text)
        
        // Initialize sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        
        hasPressureSensor = pressureSensor != null
        
        if (hasPressureSensor) {
            Log.i(TAG, "Pressure sensor found: ${pressureSensor?.name}")
        } else {
            Toast.makeText(this, "No pressure sensor available", Toast.LENGTH_LONG).show()
            Log.e(TAG, "No pressure sensor available")
        }
    }
    
    override fun onResume() {
        super.onResume()
        pressureSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Pressure sensor listener registered")
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (hasPressureSensor) {
            sensorManager.unregisterListener(this)
            Log.d(TAG, "Pressure sensor listener unregistered")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                currentPressure = it.values[0]
                
                val altitude = SensorManager.getAltitude(
                    SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                    currentPressure
                )
                
                val relativeHeight = altitude - referenceAltitude
                
                pressureTextView.text = "Pressure: %.2f hPa".format(currentPressure)
                altitudeTextView.text = "Altitude: %.2f m".format(altitude)
                heightTextView.text = "Height: %.2f m".format(relativeHeight)
                
                Log.d(TAG, "P: %.2f hPa, Alt: %.2f m, Height: %.2f m"
                    .format(currentPressure, altitude, relativeHeight))
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val accuracyStr = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE"
            else -> "UNKNOWN"
        }
        Log.d(TAG, "Sensor accuracy changed: $accuracyStr")
    }
    
    fun calibrateHeightReference() {
        if (hasPressureSensor && currentPressure > 0) {
            referenceAltitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                currentPressure
            )
            isCalibrated = true
            Toast.makeText(this, "Height reference calibrated", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Calibrated reference altitude: $referenceAltitude m")
        }
    }
    
    fun calibrateWithGPS(gpsAltitude: Float) {
        if (hasPressureSensor && currentPressure > 0) {
            referencePressure = (currentPressure / 
                Math.pow(1.0 - (gpsAltitude / 44330.0), 5.255)).toFloat()
            referenceAltitude = gpsAltitude
            isCalibrated = true
            Log.i(TAG, "GPS calibration: Alt=%.2f m, P0=%.2f hPa"
                .format(gpsAltitude, referencePressure))
        }
    }
}
```

---

## 4. Technical Approach for Height Measurement

### The Barometric Formula

Height/altitude is calculated using the international barometric formula:

**Formula**:
```
h = 44330 × [1 - (P / P₀)^(1/5.255)]
```

Where:
- `h` = altitude above sea level (meters)
- `P` = measured atmospheric pressure (Pa or hPa)
- `P₀` = reference pressure at sea level (Pa or hPa)
- `44330` = constant (meters)
- `5.255` = atmospheric constant

**Android Implementation**:
```java
float altitude = SensorManager.getAltitude(float p0, float p);
```

Parameters:
- `p0` = atmospheric pressure at sea level (hPa)
- `p` = atmospheric pressure at device location (hPa)
- Returns: altitude in meters above sea level

### Measuring Relative Height (Most Accurate)

For measuring the height of the wearer relative to a starting point (e.g., ground floor to upper floor):

**Approach**:
1. Calibrate at starting position: `referenceAltitude = SensorManager.getAltitude(STD_ATMOSPHERE, currentPressure)`
2. Calculate current altitude: `currentAltitude = SensorManager.getAltitude(STD_ATMOSPHERE, newPressure)`
3. Compute height difference: `height = currentAltitude - referenceAltitude`

**Accuracy**: Excellent for relative measurements (±1-3 meters typically)

### Measuring Absolute Altitude (Requires Calibration)

For measuring actual altitude above sea level:

**Calibration Methods**:

1. **GPS Calibration (Recommended)**
   ```java
   // When GPS has good fix
   float gpsAltitude = location.getAltitude();
   float currentPressure = pressureSensor.getValue();
   
   // Calculate sea-level pressure
   referencePressure = currentPressure / 
       Math.pow(1.0 - (gpsAltitude / 44330.0), 5.255);
   ```

2. **Weather API Calibration**
   ```java
   // Fetch local sea-level pressure from weather service
   float seaLevelPressure = weatherAPI.getSeaLevelPressure(location);
   referencePressure = seaLevelPressure;
   ```

3. **Known Altitude Calibration**
   ```java
   // If you know current altitude (e.g., airport elevation)
   float knownAltitude = 150.0f; // meters
   float currentPressure = pressureSensor.getValue();
   referencePressure = currentPressure / 
       Math.pow(1.0 - (knownAltitude / 44330.0), 5.255);
   ```

### Pressure-to-Height Conversion Table

| Pressure Change (hPa) | Height Change (m) | Height Change (ft) |
|-----------------------|-------------------|-------------------|
| -1.0                  | +8.43            | +27.7            |
| -2.0                  | +16.87           | +55.3            |
| -5.0                  | +42.18           | +138.4           |
| -10.0                 | +84.36           | +276.8           |
| -12.0                 | +101.23          | +332.1           |

*Note: Standard atmosphere at sea level = 1013.25 hPa*

---

## 5. Alternative and Complementary Sensors

### 5.1 Accelerometer (Sensor.TYPE_ACCELEROMETER)

**Purpose**: Measures acceleration forces in m/s² across three axes (x, y, z)

**For Height Measurement**:
- **Theoretical**: Double integration of vertical acceleration yields vertical displacement
- **Practical Reality**: NOT RECOMMENDED for height measurement

**Why Accelerometer Fails for Height**:
1. **Double Integration Error**: Position requires integrating acceleration twice, amplifying errors exponentially
2. **Orientation Challenges**: Vertical acceleration is distributed across all axes; requires complex orientation correction
3. **Error Accumulation**: Errors compound rapidly—accuracy degrades within seconds
4. **Noise Sensitivity**: Small sensor noise becomes large position errors after integration

**Practical Use Cases**:
- Detecting motion events (shake, tilt)
- Detecting stair climbing (combined with barometer)
- Activity recognition
- Fall detection

### 5.2 Gyroscope (Sensor.TYPE_GYROSCOPE)

**Purpose**: Measures rotation rate in rad/s around three axes

**For Height Measurement**: 
- NOT directly useful for height measurement
- Helps with orientation tracking when combined with accelerometer

**Practical Use Cases**:
- Device orientation tracking
- Improving accelerometer data by compensating rotation
- 6DoF tracking (combined with other sensors)

### 5.3 Magnetometer/Compass (Sensor.TYPE_MAGNETIC_FIELD)

**Purpose**: Measures ambient geomagnetic field

**For Height Measurement**: 
- NOT useful for height measurement
- Provides directional reference for horizontal movement tracking

### 5.4 GPS/GNSS (LocationManager)

**Purpose**: Satellite-based positioning

**For Height Measurement**:
- **Horizontal Accuracy**: 4-10 meters (good GPS conditions)
- **Vertical Accuracy**: 10-20 meters or worse (poor for height)
- **Update Rate**: 1 Hz typically

**Best Practice**: Use GPS to calibrate barometer, not as primary height sensor

**Access GPS Altitude**:
```java
LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
    1000, 0, new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
        float gpsAltitude = (float) location.getAltitude();
        // Use for barometer calibration
        calibrateWithGPS(gpsAltitude);
    }
});
```

### Sensor Comparison for Height Measurement

| Sensor        | Height Measurement | Accuracy      | Update Rate | Best Use Case           |
|---------------|-------------------|---------------|-------------|-------------------------|
| **Barometer** | ⭐⭐⭐⭐⭐ Excellent  | ±1-3m relative| 5-50 Hz     | Primary height sensor   |
| **GPS**       | ⭐⭐ Poor          | ±10-20m      | 1 Hz        | Barometer calibration   |
| **Accelerometer** | ⭐ Very Poor    | Meters/second| 50-200 Hz   | Motion detection        |
| **Gyroscope** | Not Applicable    | N/A          | 50-200 Hz   | Orientation tracking    |
| **Magnetometer** | Not Applicable | N/A          | 10-50 Hz    | Direction reference     |

---

## 6. Complete Working Example Application

### Layout XML (activity_height_measurement.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center">

    <TextView
        android:id="@+id/title_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Height Measurement"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="32dp"/>

    <TextView
        android:id="@+id/pressure_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Pressure: -- hPa"
        android:textSize="18sp"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:id="@+id/altitude_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Altitude: -- m"
        android:textSize="18sp"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:id="@+id/height_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Height: -- m"
        android:textSize="22sp"
        android:textStyle="bold"
        android:textColor="#2196F3"
        android:layout_marginBottom="32dp"/>

    <Button
        android:id="@+id/calibrate_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Calibrate Reference"
        android:textSize="16sp"
        android:padding="16dp"/>

    <TextView
        android:id="@+id/status_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Not Calibrated"
        android:textSize="14sp"
        android:textColor="#F44336"
        android:layout_marginTop="16dp"/>
</LinearLayout>
```

### AndroidManifest.xml Configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.thirdeye">

    <!-- Optional: Declare barometer feature (not required but recommended) -->
    <uses-feature 
        android:name="android.hardware.sensor.barometer"
        android:required="false" />
    
    <!-- Optional: For GPS calibration -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Optional: For network-based weather calibration -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        
        <activity 
            android:name=".HeightMeasurementActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 7. Accuracy Considerations and Limitations

### Factors Affecting Accuracy

#### 7.1 Weather-Related Factors

**Atmospheric Pressure Changes**:
- Weather systems cause pressure changes: ±20-40 hPa over hours/days
- A 1 hPa change = ~8.43 meters altitude error
- Without calibration, absolute altitude can drift ±10-30 meters per hour

**Temperature Effects**:
- Temperature changes affect air density and pressure readings
- Most modern barometers have built-in temperature compensation
- Rapid temperature changes (entering/leaving buildings) can cause temporary errors

**Wind and Ventilation**:
- Strong winds can create localized pressure variations
- HVAC systems create pressure differences between indoor/outdoor
- Moving air (walking, fans) can affect readings

#### 7.2 Device-Specific Factors

**Sensor Location**:
- Sensor placement in device affects readings
- Sealed vs. vented sensor chambers
- Body heat and device heat affect local air pressure

**Calibration Drift**:
- Sensors drift over time (typically small: <0.5 hPa/year)
- Factory calibration is usually sufficient
- No user recalibration typically needed

**Sensor Noise**:
- Typical noise: ±0.1-0.3 hPa
- Translates to ±1-3 meters height uncertainty
- Filtering can reduce noise (see section 8)

#### 7.3 Environmental Factors

**Building Effects**:
- Elevators create pressure changes during movement
- Stairwells may have different pressure than rooms
- Large buildings can have internal pressure zones

**Altitude Range**:
- Best accuracy: 0-2000 meters elevation
- Accuracy decreases at high altitudes (>3000m)
- Formula assumes standard atmospheric model

### Expected Accuracy Summary

| Measurement Type          | Typical Accuracy | Best Case | Worst Case |
|---------------------------|------------------|-----------|------------|
| **Relative Height**       | ±1-3 meters      | ±0.5m     | ±5m        |
| **Absolute Altitude (calibrated)** | ±3-8 meters | ±2m | ±15m |
| **Absolute Altitude (uncalibrated)** | ±10-50 meters | ±5m | ±100m+ |
| **Floor Detection**       | Excellent        | 100%      | 95%        |
| **Stair Counting**        | Good             | 95%       | 80%        |

### Improving Accuracy

**For Relative Height Measurement** (Recommended):
1. Calibrate at starting point before measuring
2. Take measurements over short time periods (<30 minutes)
3. Avoid rapid temperature changes
4. Apply simple moving average filter (see section 8)

**For Absolute Altitude**:
1. Use GPS calibration when device has good satellite fix
2. Periodically recalibrate (every 15-30 minutes)
3. Use weather API for local sea-level pressure
4. Consider using known altitude landmarks for reference

---

## 8. Best Practices for Implementation

### 8.1 Sensor Lifecycle Management

**CRITICAL**: Always unregister sensors in `onPause()`

```java
@Override
protected void onResume() {
    super.onResume();
    sensorManager.registerListener(this, pressureSensor, 
                                   SensorManager.SENSOR_DELAY_NORMAL);
}

@Override
protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);  // MUST unregister to save battery
}
```

**Why**: Sensors continue consuming power if not unregistered, draining battery rapidly.

### 8.2 Choosing Sensor Delay

Select appropriate delay based on use case:

```java
// For UI display (5 Hz, low power)
SensorManager.SENSOR_DELAY_NORMAL

// For UI animations (16 Hz)
SensorManager.SENSOR_DELAY_UI

// For games (50 Hz)
SensorManager.SENSOR_DELAY_GAME

// Fastest possible (varies, high power)
SensorManager.SENSOR_DELAY_FASTEST
```

**Recommendation**: Use `SENSOR_DELAY_NORMAL` for height measurement—faster rates consume more battery without significant benefit.

### 8.3 Data Filtering

Implement simple moving average to reduce noise:

```java
public class PressureFilter {
    private static final int WINDOW_SIZE = 5;
    private float[] buffer = new float[WINDOW_SIZE];
    private int index = 0;
    private int count = 0;
    
    public float addValue(float pressure) {
        buffer[index] = pressure;
        index = (index + 1) % WINDOW_SIZE;
        if (count < WINDOW_SIZE) count++;
        
        // Calculate average
        float sum = 0;
        for (int i = 0; i < count; i++) {
            sum += buffer[i];
        }
        return sum / count;
    }
}

// Usage in onSensorChanged:
@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
        float rawPressure = event.values[0];
        float filteredPressure = pressureFilter.addValue(rawPressure);
        // Use filteredPressure for calculations
    }
}
```

### 8.4 Calibration Strategy

**Implement Smart Calibration**:

```java
public class HeightCalibrationManager {
    private float referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
    private float referenceAltitude = 0f;
    private long lastCalibrationTime = 0;
    private static final long CALIBRATION_INTERVAL = 30 * 60 * 1000; // 30 minutes
    
    /**
     * Check if recalibration is needed based on time
     */
    public boolean needsRecalibration() {
        return (System.currentTimeMillis() - lastCalibrationTime) > CALIBRATION_INTERVAL;
    }
    
    /**
     * Calibrate using GPS when good fix is available
     */
    public void calibrateWithGPS(Location location, float currentPressure) {
        if (location.hasAltitude() && location.getAccuracy() < 15.0f) {
            float gpsAlt = (float) location.getAltitude();
            referencePressure = (float) (currentPressure / 
                Math.pow(1.0 - (gpsAlt / 44330.0), 5.255));
            referenceAltitude = gpsAlt;
            lastCalibrationTime = System.currentTimeMillis();
            Log.i(TAG, "GPS calibration: alt=" + gpsAlt + "m, P0=" + referencePressure);
        }
    }
    
    /**
     * Set reference point for relative height measurement
     */
    public void calibrateRelativeHeight(float currentPressure) {
        referenceAltitude = SensorManager.getAltitude(
            SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
            currentPressure
        );
        lastCalibrationTime = System.currentTimeMillis();
        Log.i(TAG, "Relative calibration at altitude: " + referenceAltitude + "m");
    }
    
    /**
     * Get current altitude using calibrated reference
     */
    public float getAltitude(float currentPressure) {
        return SensorManager.getAltitude(referencePressure, currentPressure);
    }
    
    /**
     * Get height relative to calibration point
     */
    public float getRelativeHeight(float currentPressure) {
        float currentAlt = SensorManager.getAltitude(referencePressure, currentPressure);
        return currentAlt - referenceAltitude;
    }
}
```

### 8.5 Android 12+ Rate Limiting Considerations

For Android 12 (API 31+), sensor rates are limited:

**Manifest Declaration** (if high rate needed):
```xml
<uses-permission android:name="android.permission.HIGH_SAMPLING_RATE_SENSORS" />
```

**When to Request**:
- Only if you need >200 Hz sampling
- NOT needed for height measurement (5-50 Hz is sufficient)

### 8.6 Background Operation Restrictions

**Android 9+ Background Restrictions**:
- Sensors stop delivering events when app is in background
- Use foreground service if continuous monitoring is needed

```java
// For continuous height monitoring in background
public class HeightMonitoringService extends Service {
    @Override
    public void onCreate() {
        // Create notification channel (Android 8+)
        NotificationChannel channel = new NotificationChannel(
            "height_monitoring", 
            "Height Monitoring",
            NotificationManager.IMPORTANCE_LOW
        );
        
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        
        // Create foreground notification
        Notification notification = new Notification.Builder(this, "height_monitoring")
            .setContentTitle("Height Monitoring Active")
            .setSmallIcon(R.drawable.ic_notification)
            .build();
        
        startForeground(1, notification);
    }
}
```

### 8.7 Error Handling

**Comprehensive Error Handling**:

```java
public class RobustHeightMeasurement {
    
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    
    public boolean initializeSensor(Context context) {
        try {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            
            if (sensorManager == null) {
                Log.e(TAG, "SensorManager is null");
                return false;
            }
            
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            
            if (pressureSensor == null) {
                Log.e(TAG, "Pressure sensor not available on this device");
                showUserMessage("This device does not have a barometer sensor");
                return false;
            }
            
            // Log sensor information
            Log.i(TAG, "Pressure sensor initialized: " + pressureSensor.getName());
            Log.i(TAG, "Vendor: " + pressureSensor.getVendor());
            Log.i(TAG, "Version: " + pressureSensor.getVersion());
            Log.i(TAG, "Max Range: " + pressureSensor.getMaximumRange() + " hPa");
            Log.i(TAG, "Resolution: " + pressureSensor.getResolution() + " hPa");
            Log.i(TAG, "Power: " + pressureSensor.getPower() + " mA");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sensor", e);
            return false;
        }
    }
    
    public boolean registerSensorListener(SensorEventListener listener) {
        if (sensorManager == null || pressureSensor == null) {
            Log.e(TAG, "Cannot register listener: sensor not initialized");
            return false;
        }
        
        try {
            boolean registered = sensorManager.registerListener(
                listener, 
                pressureSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            );
            
            if (!registered) {
                Log.e(TAG, "Failed to register sensor listener");
                return false;
            }
            
            Log.d(TAG, "Sensor listener registered successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error registering sensor listener", e);
            return false;
        }
    }
    
    public float getSafeAltitude(float pressure, float referencePressure) {
        // Validate inputs
        if (pressure <= 0 || pressure > 1200) {
            Log.w(TAG, "Invalid pressure value: " + pressure);
            return Float.NaN;
        }
        
        if (referencePressure <= 0 || referencePressure > 1200) {
            Log.w(TAG, "Invalid reference pressure: " + referencePressure);
            return Float.NaN;
        }
        
        try {
            return SensorManager.getAltitude(referencePressure, pressure);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating altitude", e);
            return Float.NaN;
        }
    }
}
```

### 8.8 Performance Optimization

**Minimize Processing in onSensorChanged**:

```java
// BAD: Heavy processing in callback
@Override
public void onSensorChanged(SensorEvent event) {
    float pressure = event.values[0];
    // DON'T do database operations here
    // DON'T do network calls here
    // DON'T do complex calculations here
}

// GOOD: Delegate to background thread
private Handler backgroundHandler;
private ExecutorService executor = Executors.newSingleThreadExecutor();

@Override
public void onSensorChanged(SensorEvent event) {
    final float pressure = event.values[0];
    
    // Quick processing only
    executor.submit(() -> {
        // Heavy processing on background thread
        float altitude = calculateAltitude(pressure);
        saveToDatabase(altitude);
        
        // Update UI on main thread
        runOnUiThread(() -> updateUI(altitude));
    });
}
```

### 8.9 Testing Best Practices

**Test on Real Device**:
- XR2 device required for accurate testing
- Emulators may not properly simulate pressure sensors
- Test in different environments (indoor, outdoor, different floors)

**Test Scenarios**:
1. Single-floor relative height (±1-2m accuracy expected)
2. Multi-floor building navigation (±2-5m per floor)
3. Outdoor altitude tracking with GPS calibration
4. Rapid elevator movement
5. Stair climbing detection

**Validation Methods**:
```java
// Log sensor capabilities at startup
private void logSensorInfo() {
    if (pressureSensor != null) {
        Log.i(TAG, "=== Pressure Sensor Info ===");
        Log.i(TAG, "Name: " + pressureSensor.getName());
        Log.i(TAG, "Type: " + pressureSensor.getType());
        Log.i(TAG, "Vendor: " + pressureSensor.getVendor());
        Log.i(TAG, "Version: " + pressureSensor.getVersion());
        Log.i(TAG, "Max Range: " + pressureSensor.getMaximumRange() + " hPa");
        Log.i(TAG, "Resolution: " + pressureSensor.getResolution() + " hPa");
        Log.i(TAG, "Power: " + pressureSensor.getPower() + " mA");
        Log.i(TAG, "Min Delay: " + pressureSensor.getMinDelay() + " μs");
        Log.i(TAG, "Max Delay: " + pressureSensor.getMaxDelay() + " μs");
    }
}
```

---

## 9. Real-World Use Cases

### 9.1 Floor Detection in Buildings

**Application**: Automatically detect which floor user is on

```java
public class FloorDetector {
    private static final float FLOOR_HEIGHT = 3.5f; // meters, typical floor height
    private float groundFloorPressure;
    private boolean isCalibrated = false;
    
    public void calibrateGroundFloor(float currentPressure) {
        groundFloorPressure = currentPressure;
        isCalibrated = true;
        Log.i(TAG, "Ground floor calibrated at pressure: " + groundFloorPressure);
    }
    
    public int detectFloor(float currentPressure) {
        if (!isCalibrated) return 0;
        
        float altitude = SensorManager.getAltitude(
            groundFloorPressure, 
            currentPressure
        );
        
        // Calculate floor number
        int floor = Math.round(altitude / FLOOR_HEIGHT);
        
        Log.d(TAG, "Altitude: " + altitude + "m, Floor: " + floor);
        return floor;
    }
    
    public String getFloorDescription(int floor) {
        if (floor == 0) return "Ground Floor";
        if (floor < 0) return "B" + Math.abs(floor) + " (Basement)";
        return "Floor " + floor;
    }
}
```

### 9.2 Stair Climbing Detection

**Application**: Count stairs climbed for fitness tracking

```java
public class StairClimbingDetector {
    private static final float STAIR_THRESHOLD = 2.5f; // meters
    private float lastAltitude = 0;
    private int stairsClimbed = 0;
    private int stairsDescended = 0;
    
    public void processAltitudeChange(float currentAltitude) {
        float altitudeChange = currentAltitude - lastAltitude;
        
        if (Math.abs(altitudeChange) > STAIR_THRESHOLD) {
            if (altitudeChange > 0) {
                stairsClimbed++;
                Log.i(TAG, "Stairs climbed: " + stairsClimbed);
                onStairsClimbed(altitudeChange);
            } else {
                stairsDescended++;
                Log.i(TAG, "Stairs descended: " + stairsDescended);
                onStairsDescended(Math.abs(altitudeChange));
            }
            
            lastAltitude = currentAltitude;
        }
    }
    
    private void onStairsClimbed(float height) {
        // Update UI, save to fitness database, etc.
        int floors = Math.round(height / 3.5f);
        Log.i(TAG, "Climbed approximately " + floors + " floor(s)");
    }
    
    private void onStairsDescended(float height) {
        int floors = Math.round(height / 3.5f);
        Log.i(TAG, "Descended approximately " + floors + " floor(s)");
    }
}
```

### 9.3 AR Content Height Positioning

**Application**: Position AR content at specific heights relative to user

```java
public class ARHeightPositioning {
    private float userEyeLevel;
    private float groundLevel;
    
    public void calibrateUserHeight(float currentAltitude) {
        // Assume user is standing, eyes at altitude
        userEyeLevel = currentAltitude;
        // Estimate ground at -1.6m (average eye height)
        groundLevel = userEyeLevel - 1.6f;
        Log.i(TAG, "User calibrated - Eye level: " + userEyeLevel + "m");
    }
    
    public float getARObjectHeight(String objectType) {
        // Return altitude for AR object placement
        switch (objectType) {
            case "FLOOR_MARKER":
                return groundLevel;
            case "TABLE_HEIGHT":
                return groundLevel + 0.75f;
            case "EYE_LEVEL":
                return userEyeLevel;
            case "CEILING":
                return groundLevel + 2.7f; // typical ceiling height
            default:
                return userEyeLevel;
        }
    }
    
    public boolean isObjectAtEyeLevel(float objectAltitude, float tolerance) {
        return Math.abs(objectAltitude - userEyeLevel) < tolerance;
    }
}
```

### 9.4 Elevator Detection

**Application**: Detect elevator usage for building navigation

```java
public class ElevatorDetector {
    private static final float ELEVATOR_SPEED_THRESHOLD = 0.5f; // m/s
    private float lastAltitude;
    private long lastTime;
    private boolean inElevator = false;
    
    public void processAltitudeUpdate(float altitude, long timestamp) {
        if (lastTime > 0) {
            float altitudeChange = altitude - lastAltitude;
            float timeChange = (timestamp - lastTime) / 1000.0f; // seconds
            
            if (timeChange > 0) {
                float verticalSpeed = Math.abs(altitudeChange / timeChange);
                
                if (verticalSpeed > ELEVATOR_SPEED_THRESHOLD) {
                    if (!inElevator) {
                        inElevator = true;
                        onElevatorEntered();
                    }
                } else {
                    if (inElevator) {
                        inElevator = false;
                        onElevatorExited(altitudeChange);
                    }
                }
            }
        }
        
        lastAltitude = altitude;
        lastTime = timestamp;
    }
    
    private void onElevatorEntered() {
        Log.i(TAG, "Entered elevator");
        // Disable other navigation, show elevator UI
    }
    
    private void onElevatorExited(float totalChange) {
        int floors = Math.round(totalChange / 3.5f);
        Log.i(TAG, "Exited elevator - Moved " + floors + " floor(s)");
    }
}
```

---

## 10. Troubleshooting Common Issues

### Issue 1: Sensor Not Found

**Symptom**: `getDefaultSensor(TYPE_PRESSURE)` returns `null`

**Causes**:
- Device doesn't have barometer hardware
- Incorrect sensor type constant

**Solutions**:
```java
// Check sensor availability
if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
    Log.e(TAG, "No pressure sensor on this device");
    // List all available sensors for debugging
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
    for (Sensor s : sensors) {
        Log.d(TAG, "Available: " + s.getName() + " (Type: " + s.getType() + ")");
    }
}
```

### Issue 2: No Sensor Events Received

**Symptom**: `onSensorChanged()` never called

**Causes**:
- Listener not registered
- Registered in wrong lifecycle method
- Background restrictions (Android 9+)

**Solutions**:
```java
// Verify registration
@Override
protected void onResume() {
    super.onResume();
    if (pressureSensor != null) {
        boolean success = sensorManager.registerListener(
            this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL
        );
        Log.d(TAG, "Listener registration: " + (success ? "SUCCESS" : "FAILED"));
    }
}

// Check if app is in foreground
@Override
protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
    Log.d(TAG, "Listener unregistered");
}
```

### Issue 3: Altitude Readings Drift Over Time

**Symptom**: Altitude changes even when stationary

**Causes**:
- Weather-related pressure changes
- Temperature changes
- Lack of recalibration

**Solutions**:
```java
// Implement periodic recalibration
private static final long RECALIBRATION_INTERVAL = 30 * 60 * 1000; // 30 min
private long lastCalibration = 0;

public void updateAltitude(float pressure) {
    long now = System.currentTimeMillis();
    
    // Recalibrate periodically
    if (now - lastCalibration > RECALIBRATION_INTERVAL) {
        if (gpsAvailable && gpsAccuracyGood) {
            calibrateWithGPS();
            lastCalibration = now;
        }
    }
    
    // Calculate altitude
    float altitude = SensorManager.getAltitude(referencePressure, pressure);
}
```

### Issue 4: Noisy/Jittery Readings

**Symptom**: Altitude values jump around rapidly

**Causes**:
- Sensor noise
- Air movement (HVAC, walking, wind)
- No filtering applied

**Solutions**:
```java
// Apply exponential moving average filter
private float filteredAltitude = 0;
private static final float ALPHA = 0.1f; // Smoothing factor

public float filterAltitude(float newAltitude) {
    if (filteredAltitude == 0) {
        filteredAltitude = newAltitude; // Initialize
    } else {
        filteredAltitude = ALPHA * newAltitude + (1 - ALPHA) * filteredAltitude;
    }
    return filteredAltitude;
}
```

### Issue 5: Incorrect Altitude Values

**Symptom**: Altitude off by 10-100+ meters

**Causes**:
- Using standard atmosphere without calibration
- Weather changes since last calibration
- Wrong reference pressure

**Solutions**:
```java
// Always calibrate for absolute altitude
public void ensureCalibration() {
    if (!isCalibrated || needsRecalibration()) {
        if (hasGPSFix()) {
            calibrateWithGPS();
        } else if (hasInternetConnection()) {
            calibrateWithWeatherAPI();
        } else {
            // Use for relative measurements only
            Log.w(TAG, "No calibration available - using relative mode");
        }
    }
}
```

### Issue 6: High Battery Consumption

**Symptom**: App drains battery quickly

**Causes**:
- Sensor not unregistered in `onPause()`
- Using `SENSOR_DELAY_FASTEST` unnecessarily
- Background service running continuously

**Solutions**:
```java
// Always unregister in onPause
@Override
protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
}

// Use appropriate delay
// Use NORMAL for height measurement, not FASTEST
sensorManager.registerListener(this, pressureSensor, 
    SensorManager.SENSOR_DELAY_NORMAL); // 5 Hz is sufficient
```

---

## 11. Advanced Topics

### 11.1 Sensor Fusion for Improved Accuracy

Combine barometer with other sensors:

```java
public class SensorFusionHeightEstimator {
    private float barometerAltitude;
    private float gpsAltitude;
    private float fusedAltitude;
    
    // Kalman filter parameters
    private float estimate = 0;
    private float errorEstimate = 1;
    private static final float PROCESS_NOISE = 0.01f;
    
    public float fuseAltitudeEstimates(float barometer, float gps, 
                                       float gpsAccuracy) {
        // Weight GPS less if accuracy is poor
        float gpsWeight = 1.0f / (gpsAccuracy + 1);
        float barometerWeight = 5.0f; // Barometer typically more accurate
        
        fusedAltitude = (barometer * barometerWeight + gps * gpsWeight) / 
                        (barometerWeight + gpsWeight);
        
        return fusedAltitude;
    }
    
    // Simple Kalman filter for smoother estimates
    public float kalmanFilter(float measurement, float measurementNoise) {
        // Prediction
        errorEstimate += PROCESS_NOISE;
        
        // Update
        float kalmanGain = errorEstimate / (errorEstimate + measurementNoise);
        estimate = estimate + kalmanGain * (measurement - estimate);
        errorEstimate = (1 - kalmanGain) * errorEstimate;
        
        return estimate;
    }
}
```

### 11.2 Weather API Integration

```java
public class WeatherCalibration {
    private static final String OPENWEATHER_API = 
        "https://api.openweathermap.org/data/2.5/weather";
    
    public void fetchSeaLevelPressure(double lat, double lon, 
                                     PressureCallback callback) {
        // Make API call to weather service
        String url = OPENWEATHER_API + "?lat=" + lat + "&lon=" + lon + 
                     "&appid=YOUR_API_KEY";
        
        // Parse response and extract sea level pressure
        // Call callback with pressure value
        
        // Example response processing:
        // JSONObject main = response.getJSONObject("main");
        // float pressure = main.getFloat("pressure"); // hPa
        // callback.onPressureReceived(pressure);
    }
    
    interface PressureCallback {
        void onPressureReceived(float seaLevelPressure);
        void onError(Exception e);
    }
}
```

### 11.3 Machine Learning Height Classification

```java
public class MLHeightClassifier {
    // Use TensorFlow Lite for activity recognition
    // Train model on sensor data (pressure, accelerometer, gyroscope)
    // to classify: standing, sitting, lying down, climbing stairs, etc.
    
    public ActivityType classifyActivity(float[] sensorData) {
        // sensorData: [pressure, accelX, accelY, accelZ, gyroX, gyroY, gyroZ]
        // Run through trained model
        // Return classified activity
        return ActivityType.STANDING;
    }
    
    enum ActivityType {
        STANDING, SITTING, LYING_DOWN, CLIMBING_STAIRS, 
        DESCENDING_STAIRS, IN_ELEVATOR, UNKNOWN
    }
}
```

---

## 12. Resources and References

### Official Documentation

- **Android Sensors Overview**: https://developer.android.com/guide/topics/sensors/sensors_overview
- **Android Environment Sensors**: https://developer.android.com/guide/topics/sensors/sensors_environment
- **SensorManager API**: https://developer.android.com/reference/android/hardware/SensorManager
- **RayNeo Developer Portal**: https://open.rayneo.com/
- **RayNeo X2 Product Page**: https://www.rayneo.com/products/tcl-rayneo-x2

### Technical References

- **Barometric Formula**: International Standard Atmosphere (ISA) model
- **Qualcomm Snapdragon XR2**: https://www.qualcomm.com/products/mobile/snapdragon/xr-vr-ar/snapdragon-xr2-5g-platform

### Sample Code Repositories

- Android Sensor Programming Examples: https://github.com/PacktPublishing/Android-Sensor-Programming-By-Example

### Academic Papers

- "Using Portable Device Sensors to Recognize Height Changing Modes of Motion" - Research on sensor-based height detection
- "HiMeter: Telling You the Height Rather than the Altitude" - PMC Publication on height measurement accuracy

---

## 13. Summary and Recommendations

### Key Takeaways

1. **Primary Sensor**: The barometer (TYPE_PRESSURE) is the BEST sensor for height measurement on the RayNeo XR2
2. **Accuracy**: Expect ±1-3 meters for relative height, ±3-8 meters for calibrated absolute altitude
3. **Calibration**: Essential for absolute altitude; use GPS or weather API for best results
4. **Accelerometer/Gyroscope**: NOT suitable for height measurement due to integration errors
5. **No Special Permissions**: Pressure sensor doesn't require runtime permissions

### Recommended Implementation Approach

**For Most Applications** (Relative Height Measurement):
```java
// 1. Check sensor availability
// 2. Calibrate at starting point
// 3. Measure height changes relative to that point
// 4. Apply simple filtering
// 5. Recalibrate if measurement period >30 minutes
```

**For Absolute Altitude** (GPS-Calibrated):
```java
// 1. Initialize both pressure sensor and GPS
// 2. Wait for good GPS fix (accuracy <15m)
// 3. Calibrate pressure sensor with GPS altitude
// 4. Use pressure for continuous altitude updates
// 5. Recalibrate periodically with GPS
```

### Quick Start Checklist

- [ ] Add sensor availability check in `onCreate()`
- [ ] Register listener in `onResume()`
- [ ] **ALWAYS** unregister listener in `onPause()`
- [ ] Use `SENSOR_DELAY_NORMAL` for height measurement
- [ ] Implement calibration method (relative or GPS)
- [ ] Add basic filtering (moving average or exponential)
- [ ] Handle case where sensor is not available
- [ ] Test on actual RayNeo XR2 device
- [ ] Log sensor information for debugging
- [ ] Consider recalibration strategy for long-duration use

---

## Appendix A: Complete Working Application

See the code examples in Sections 3 and 6 for complete, production-ready implementations in both Java and Kotlin.

## Appendix B: Conversion Formulas

**Pressure to Altitude**:
```
altitude (m) = 44330 × [1 - (P / P₀)^0.1903]
```

**Altitude to Pressure**:
```
P (hPa) = P₀ × (1 - altitude / 44330)^5.255
```

**Meters to Feet**:
```
feet = meters × 3.28084
```

**hPa to inHg** (inches of mercury):
```
inHg = hPa × 0.02953
```

## Appendix C: Typical Pressure Values

| Location | Typical Pressure (hPa) |
|----------|------------------------|
| Sea Level | 1013.25 |
| 500m elevation | 954 |
| 1000m elevation | 899 |
| 1500m elevation | 845 |
| 2000m elevation | 795 |
| High pressure system | 1030-1050 |
| Low pressure system | 980-1000 |

---

**Document Version**: 1.0  
**Last Updated**: 2025-10-30  
**Target Platform**: RayNeo XR2 / Android 12 (API 31+)  
**Author**: RayNeo XR2 Technical Advisory  

For questions or support, visit the [RayNeo Developer Portal](https://open.rayneo.com/)
