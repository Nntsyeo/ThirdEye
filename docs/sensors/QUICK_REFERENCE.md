# RayNeo XR2 Height Measurement - Quick Reference

## TL;DR - Get Started in 5 Minutes

### 1. Check if Sensor is Available
```java
SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
    // Good to go!
}
```

### 2. Register Listener
```java
Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
```

### 3. Read Pressure & Calculate Height
```java
@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
        float pressure = event.values[0];  // in hPa
        float altitude = SensorManager.getAltitude(
            SensorManager.PRESSURE_STANDARD_ATMOSPHERE, 
            pressure
        );
    }
}
```

### 4. ALWAYS Unregister (Important!)
```java
@Override
protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);  // Critical for battery life
}
```

---

## Common Use Cases

### Measure Height Change (Relative)

**Best for**: Floor detection, stair climbing, relative altitude

```java
// Step 1: Calibrate at starting point
float referenceAltitude = SensorManager.getAltitude(1013.25f, currentPressure);

// Step 2: Measure current altitude
float currentAltitude = SensorManager.getAltitude(1013.25f, newPressure);

// Step 3: Calculate height change
float heightChange = currentAltitude - referenceAltitude;
```

**Accuracy**: ±1-3 meters

---

### Measure Absolute Altitude (GPS Calibrated)

**Best for**: Outdoor navigation, accurate altitude measurement

```java
// Step 1: Get GPS altitude when you have good fix
float gpsAltitude = location.getAltitude();

// Step 2: Calculate reference pressure
float referencePressure = currentPressure / 
    Math.pow(1.0 - (gpsAltitude / 44330.0), 5.255);

// Step 3: Use reference pressure for accurate altitude
float altitude = SensorManager.getAltitude(referencePressure, newPressure);
```

**Accuracy**: ±3-8 meters (after calibration)

---

## Key Facts

| Property | Value |
|----------|-------|
| **Sensor Type** | `Sensor.TYPE_PRESSURE` |
| **Units** | hPa (hectopascals) or mbar |
| **Permissions** | None required |
| **Update Rate** | 5-50 Hz (use SENSOR_DELAY_NORMAL) |
| **Accuracy** | ±1-3m relative, ±3-8m absolute (calibrated) |
| **Power Usage** | Low (~0.5-1 mA typical) |

---

## Important Rules

1. **ALWAYS unregister in onPause()** - Critical for battery life
2. **Use SENSOR_DELAY_NORMAL** - Faster rates waste battery
3. **Calibrate for absolute altitude** - Otherwise use relative measurements
4. **Recalibrate every 30 minutes** - Weather changes affect readings
5. **Test on real device** - Emulator won't work properly

---

## Pressure-Height Conversion

| Pressure Change | Height Change |
|-----------------|---------------|
| -1 hPa | +8.4 m |
| -2 hPa | +16.9 m |
| -5 hPa | +42.2 m |
| -10 hPa | +84.4 m |
| -12 hPa | +101.2 m |

**Formula**: 1 hPa ≈ 8.43 meters

---

## Common Problems & Solutions

### Problem: onSensorChanged() never called
**Solution**: Check you registered in `onResume()` and app is in foreground

### Problem: Altitude drifting over time
**Solution**: Recalibrate periodically (every 30 minutes)

### Problem: Noisy readings
**Solution**: Apply simple moving average filter

### Problem: High battery drain
**Solution**: Make sure you unregister listener in `onPause()`

---

## Minimal Working Example

```java
public class SimpleHeightActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private float referenceAltitude = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, 
                SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressure = event.values[0];
        float altitude = SensorManager.getAltitude(1013.25f, pressure);
        float height = altitude - referenceAltitude;
        
        Log.d("Height", "Current height: " + height + " meters");
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Optional: handle accuracy changes
    }
    
    public void calibrate() {
        // Call this when user is at reference point
        float pressure = getCurrentPressure();
        referenceAltitude = SensorManager.getAltitude(1013.25f, pressure);
    }
}
```

---

## What Sensors NOT to Use

| Sensor | Why Not? |
|--------|----------|
| **Accelerometer** | Double integration causes massive errors |
| **Gyroscope** | Measures rotation, not position |
| **Magnetometer** | Only provides direction, not altitude |
| **GPS** | Poor vertical accuracy (±10-20m) |

**Only use**: Barometer for height, GPS only for calibration

---

## Performance Tips

1. **Use SENSOR_DELAY_NORMAL** (5 Hz) - sufficient for height
2. **Apply filtering** - reduce noise with moving average
3. **Unregister when not needed** - save battery
4. **Avoid complex calculations** in `onSensorChanged()`
5. **Cache values** - don't recalculate unnecessarily

---

## Testing Checklist

- [ ] Test on actual RayNeo XR2 device
- [ ] Verify sensor is available
- [ ] Test relative height between floors
- [ ] Test calibration methods
- [ ] Verify listener unregisters in onPause
- [ ] Check battery usage
- [ ] Test with filtering enabled
- [ ] Log sensor info for debugging

---

## Next Steps

For complete implementation details, code examples, and advanced topics, see:
- **Full Guide**: `/docs/sensors/HEIGHT_MEASUREMENT_GUIDE.md`
- **Android Sensors**: https://developer.android.com/guide/topics/sensors/sensors_overview
- **RayNeo Docs**: https://open.rayneo.com/

---

**Quick Reference Version**: 1.0  
**Platform**: RayNeo XR2 / Android 12+
