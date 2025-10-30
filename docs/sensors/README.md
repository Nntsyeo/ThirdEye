# RayNeo XR2 Height Measurement Documentation

This directory contains comprehensive technical documentation for implementing height measurement functionality using the RayNeo XR2 augmented reality device sensors.

## Documentation Files

### 1. HEIGHT_MEASUREMENT_GUIDE.md (52 KB)
**Complete Technical Guide**

The definitive, comprehensive technical reference covering:
- Complete sensor specifications and capabilities
- Detailed Android 12 API documentation
- Technical approach and algorithms
- Multiple code examples (Java & Kotlin)
- Accuracy considerations and limitations
- Best practices and optimization strategies
- Troubleshooting guide
- Advanced topics including sensor fusion
- Real-world use case implementations

**Who should read this**: Developers implementing height measurement from scratch, those needing deep technical understanding, or debugging complex issues.

### 2. QUICK_REFERENCE.md (6.3 KB)
**Quick Start Guide**

Fast-track reference for developers who need to get started quickly:
- TL;DR implementation in 5 minutes
- Common use case examples
- Key facts table
- Conversion formulas
- Problem-solution reference
- Minimal working code example

**Who should read this**: Developers who need a quick implementation, or as a refresher when you've already read the full guide.

### 3. HeightMeasurementTemplate.java (14 KB)
**Production-Ready Code Template**

Complete, copy-paste ready Java class implementing:
- Full sensor lifecycle management
- Height measurement with calibration
- Data filtering for noise reduction
- Comprehensive error handling
- Well-documented code structure
- Customizable callbacks

**Who should use this**: Developers who want to quickly integrate height measurement by copying and customizing this template.

---

## Quick Navigation

### I want to...

**Get started in 5 minutes**
→ Read: [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
→ Copy: [HeightMeasurementTemplate.java](HeightMeasurementTemplate.java)

**Understand how barometers work for height measurement**
→ Read: [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) - Section 4

**Implement floor detection**
→ Read: [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) - Section 9.1

**Improve accuracy of my measurements**
→ Read: [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) - Section 7 & 8

**Troubleshoot sensor issues**
→ Read: [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) - Section 10

**See complete working examples**
→ Read: [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) - Section 3 & 6
→ Copy: [HeightMeasurementTemplate.java](HeightMeasurementTemplate.java)

---

## Key Findings Summary

### Available Sensors on RayNeo XR2
- **Barometer (Pressure Sensor)** ✓ - Primary sensor for height measurement
- **Accelerometer** ✓ - NOT suitable for height (use for motion detection only)
- **Gyroscope** ✓ - NOT suitable for height (use for orientation only)
- **Magnetometer/Compass** ✓ - NOT suitable for height

### Technical Specifications
- **Sensor Type**: `Sensor.TYPE_PRESSURE` (Android)
- **Measurement Units**: hPa (hectopascals) / mbar
- **Accuracy**: ±1-3 meters (relative), ±3-8 meters (absolute, calibrated)
- **Update Rate**: 5-50 Hz (recommend SENSOR_DELAY_NORMAL = 5 Hz)
- **Permissions**: None required (environmental sensor)
- **Power Consumption**: Low (~0.5-1 mA)

### Implementation Approach
1. **Barometer is the ONLY viable sensor** for height measurement
2. **Relative height measurement** (change from starting point) is most accurate
3. **Calibration is essential** for absolute altitude measurement
4. **GPS or weather API** can be used for calibration
5. **Filtering recommended** to reduce noise and improve stability

### Key Limitations
- Weather changes affect absolute altitude (requires periodic recalibration)
- Typical accuracy: ±1-3 meters relative, ±3-8 meters absolute (calibrated)
- Accelerometer/gyroscope NOT suitable due to integration errors
- GPS vertical accuracy is poor (±10-20m) - use only for calibration

---

## Platform Information

- **Device**: RayNeo XR2 AR Glasses
- **Processor**: Qualcomm Snapdragon XR2
- **Operating System**: Android 12 (API Level 31)
- **Memory**: 6 GB RAM + 128 GB storage
- **Sensors**: Accelerometer, Gyroscope, Compass, Pressure Sensor
- **Tracking**: 6DoF environmental recognition

---

## Code Example Preview

Here's a minimal example to get started:

```java
// Initialize sensor
SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

// Register listener
sensorManager.registerListener(this, pressureSensor, 
    SensorManager.SENSOR_DELAY_NORMAL);

// Read pressure and calculate height
@Override
public void onSensorChanged(SensorEvent event) {
    float pressure = event.values[0];  // in hPa
    float altitude = SensorManager.getAltitude(1013.25f, pressure);
    float height = altitude - referenceAltitude;
}

// CRITICAL: Unregister to save battery
@Override
protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
}
```

---

## Additional Resources

### Official Documentation
- [Android Sensors Overview](https://developer.android.com/guide/topics/sensors/sensors_overview)
- [RayNeo Developer Portal](https://open.rayneo.com/)
- [RayNeo X2 Product Page](https://www.rayneo.com/products/tcl-rayneo-x2)

### Related Documentation in This Project
- Launcher configuration: `/docs/LAUNCHER_CONFIGURATION.md`
- Launcher setup: `/docs/LAUNCHER_SETUP.md`

---

## Document Information

- **Created**: 2025-10-30
- **Version**: 1.0
- **Target Platform**: RayNeo XR2 / Android 12 (API 31+)
- **Total Documentation**: ~2,300 lines
- **Language**: Java (code examples), English (documentation)

---

## Contributing

If you find issues, have improvements, or want to add examples:
1. Document your findings
2. Test on actual RayNeo XR2 hardware
3. Include code examples where applicable
4. Update the appropriate documentation file

---

## Questions?

For technical questions or support:
- Review the [HEIGHT_MEASUREMENT_GUIDE.md](HEIGHT_MEASUREMENT_GUIDE.md) troubleshooting section
- Check the [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for common issues
- Visit the [RayNeo Developer Portal](https://open.rayneo.com/)

---

**Happy Coding!** 🚀
