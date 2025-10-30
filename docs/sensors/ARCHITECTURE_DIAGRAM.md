# RayNeo XR2 Height Measurement Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      RayNeo XR2 Device                          │
│                   (Android 12 / API 31)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Qualcomm Snapdragon XR2                       │
│                     Hardware Platform                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌──────────┐ │
│  │ Barometer  │  │Accelero-   │  │ Gyroscope  │  │ Compass  │ │
│  │ (Pressure) │  │  meter     │  │            │  │   (Mag)  │ │
│  └────────────┘  └────────────┘  └────────────┘  └──────────┘ │
│       ✓                ✓               ✓              ✓        │
│   [HEIGHT]         [MOTION]        [ROTATION]    [DIRECTION]   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               Android Sensor Framework (API)                     │
│                    android.hardware.Sensor                       │
├─────────────────────────────────────────────────────────────────┤
│  TYPE_PRESSURE  │ TYPE_ACCELEROMETER │ TYPE_GYROSCOPE │ TYPE_  │
│                 │                    │                │MAGNETIC │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Your Application                             │
│              HeightMeasurementActivity.java                      │
└─────────────────────────────────────────────────────────────────┘
```

## Height Measurement Data Flow

```
┌──────────────────────┐
│  Barometer Sensor    │
│  Physical Hardware   │
└──────────┬───────────┘
           │ Raw pressure (analog)
           ▼
┌──────────────────────┐
│  Sensor Driver       │
│  (Kernel Level)      │
└──────────┬───────────┘
           │ Digitized pressure (hPa)
           ▼
┌──────────────────────┐
│  Android HAL         │
│  (Hardware Abstract) │
└──────────┬───────────┘
           │ Sensor events
           ▼
┌──────────────────────┐
│  SensorManager       │
│  (Framework)         │
└──────────┬───────────┘
           │ SensorEvent objects
           ▼
┌──────────────────────┐
│  SensorEventListener │
│  onSensorChanged()   │
└──────────┬───────────┘
           │ event.values[0] = pressure
           ▼
┌──────────────────────┐
│  Data Filtering      │
│  (Moving Average)    │
└──────────┬───────────┘
           │ Filtered pressure
           ▼
┌──────────────────────┐
│  Altitude Calculation│
│  SensorManager.      │
│  getAltitude()       │
└──────────┬───────────┘
           │ Altitude (meters)
           ▼
┌──────────────────────┐
│  Height Calculation  │
│  altitude - reference│
└──────────┬───────────┘
           │ Relative height
           ▼
┌──────────────────────┐
│  Application Logic   │
│  (UI, AR, etc.)      │
└──────────────────────┘
```

## Sensor Selection Decision Tree

```
                    Need Height Measurement?
                              │
                              ▼
                        ┌─────────┐
                        │  YES    │
                        └────┬────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
    ┌─────────────────┐          ┌─────────────────┐
    │ Relative Height │          │ Absolute        │
    │ (from starting  │          │ Altitude        │
    │  point)         │          │ (sea level)     │
    └────────┬────────┘          └────────┬────────┘
             │                            │
             │                            │
    ┌────────▼────────┐          ┌────────▼────────┐
    │ Use Barometer   │          │ Use Barometer   │
    │ + Calibration   │          │ + GPS or Weather│
    │ at start point  │          │ API Calibration │
    └─────────────────┘          └─────────────────┘
             │                            │
             ▼                            ▼
    ┌─────────────────┐          ┌─────────────────┐
    │ Accuracy:       │          │ Accuracy:       │
    │ ±1-3 meters     │          │ ±3-8 meters     │
    └─────────────────┘          └─────────────────┘


    ❌ DON'T USE THESE FOR HEIGHT:

    ┌─────────────────┐          ┌─────────────────┐
    │ Accelerometer   │          │ Gyroscope       │
    │ ❌ Integration  │          │ ❌ No position  │
    │    errors       │          │    data         │
    └─────────────────┘          └─────────────────┘

    ┌─────────────────┐          ┌─────────────────┐
    │ GPS Altitude    │          │ Magnetometer    │
    │ ❌ Poor vertical│          │ ❌ Only         │
    │   accuracy      │          │   direction     │
    └─────────────────┘          └─────────────────┘
```

## Application Architecture

```
┌───────────────────────────────────────────────────────────────┐
│                    HeightMeasurementActivity                   │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Sensor Management Layer                            │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │  • initializeSensor()                               │    │
│  │  • registerSensorListener()                         │    │
│  │  • unregisterSensorListener()                       │    │
│  │  • onSensorChanged()                                │    │
│  │  • onAccuracyChanged()                              │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────┐    │
│  │  Data Processing Layer                              │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │  • filterPressure() - Noise reduction               │    │
│  │  • calculateAltitude() - Pressure to altitude       │    │
│  │  • calculateHeight() - Relative height              │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────┐    │
│  │  Calibration Layer                                  │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │  • calibrateReference() - Set reference point       │    │
│  │  • calibrateWithGPS() - GPS-based calibration       │    │
│  │  • resetCalibration() - Clear calibration           │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────┐    │
│  │  Application Logic Layer                            │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │  • onHeightChanged() - Callback for app logic       │    │
│  │  • updateUI() - Display measurements                │    │
│  │  • Floor detection / Stair counting / AR content    │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

## Calibration Strategies

### Strategy 1: Relative Height (Simplest)

```
User at Ground Floor
        │
        ▼
┌──────────────────┐
│ Read Pressure    │ P1 = 1013.25 hPa
│ Calculate Alt    │ Alt1 = 0 m
│ Store Reference  │ RefAlt = 0 m
└────────┬─────────┘
         │
         │ User moves to 3rd floor
         ▼
┌──────────────────┐
│ Read Pressure    │ P2 = 1009.50 hPa
│ Calculate Alt    │ Alt2 = 31.5 m
│ Height = Alt2-   │ Height = 31.5 m
│         RefAlt   │
└──────────────────┘

Accuracy: ±1-3 meters
Use Case: Indoor navigation, floor detection
```

### Strategy 2: GPS Calibration (Most Accurate)

```
Device with GPS Fix
        │
        ▼
┌──────────────────┐
│ GPS Altitude     │ GPSAlt = 150.0 m
│ Current Pressure │ P = 990.5 hPa
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Calculate Sea    │ P0 = P / (1 - GPSAlt/44330)^5.255
│ Level Pressure   │ P0 = 1013.8 hPa
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Use P0 for       │ Alt = getAltitude(P0, Pcurrent)
│ Future Readings  │
└──────────────────┘

Accuracy: ±3-8 meters
Use Case: Outdoor activities, absolute altitude
Recalibrate: Every 30 minutes
```

### Strategy 3: Weather API (Network-Based)

```
Device Location (Lat, Lon)
        │
        ▼
┌──────────────────┐
│ Query Weather    │ API: openweathermap.org
│ API              │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Get Sea Level    │ P0 = 1015.2 hPa
│ Pressure         │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Use P0 for       │ Alt = getAltitude(P0, Pcurrent)
│ Calculations     │
└──────────────────┘

Accuracy: ±5-10 meters
Use Case: When GPS unavailable
Requires: Internet connection
```

## Data Filtering Pipeline

```
Raw Sensor Data (Noisy)
        │
        ▼
┌──────────────────┐
│ Samples:         │
│ 1013.2 hPa       │
│ 1013.5 hPa       │
│ 1012.9 hPa       │
│ 1013.3 hPa       │
│ 1013.1 hPa       │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Moving Average   │
│ Filter (n=5)     │
│                  │
│ Avg = (1013.2 +  │
│        1013.5 +  │
│        1012.9 +  │
│        1013.3 +  │
│        1013.1)/5 │
│     = 1013.2 hPa │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Smoothed Data    │
│ 1013.2 hPa       │
│ (±0.2 hPa noise) │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Altitude Calc    │
│ Alt = 0.0 m      │
│ (±1.7 m error)   │
└──────────────────┘

Improvement: ~60-80% noise reduction
```

## Error Sources and Mitigation

```
┌─────────────────────────────────────────────────────────┐
│                   Error Sources                          │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Weather Changes                                         │
│  ±20-40 hPa → ±170-340m error                           │
│      ↓                                                   │
│  Mitigation: Periodic recalibration (30 min)            │
│                                                          │
│  Temperature Changes                                     │
│  ±5°C → ±1-2 hPa → ±8-17m error                         │
│      ↓                                                   │
│  Mitigation: Built-in temp compensation (automatic)     │
│                                                          │
│  Sensor Noise                                            │
│  ±0.1-0.3 hPa → ±1-3m error                             │
│      ↓                                                   │
│  Mitigation: Moving average filter                      │
│                                                          │
│  HVAC / Wind                                             │
│  ±0.5-2 hPa → ±4-17m error (temporary)                  │
│      ↓                                                   │
│  Mitigation: Time-based filtering, avoid vents          │
│                                                          │
│  Altitude Range                                          │
│  Error increases at >3000m elevation                     │
│      ↓                                                   │
│  Mitigation: Use within 0-2000m range                   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## Performance Characteristics

```
┌────────────────────────────────────────────────────────┐
│              Sensor Sampling Rates                      │
├────────────────────────────────────────────────────────┤
│                                                         │
│  SENSOR_DELAY_NORMAL    →    5 Hz   [RECOMMENDED]     │
│  ├─ Battery: Low (0.5-1 mA)                           │
│  ├─ Accuracy: Excellent for height                    │
│  └─ Use: General height measurement                   │
│                                                         │
│  SENSOR_DELAY_UI        →   16 Hz                      │
│  ├─ Battery: Medium (1-2 mA)                          │
│  ├─ Accuracy: Good (slightly noisier)                 │
│  └─ Use: Real-time UI animations                      │
│                                                         │
│  SENSOR_DELAY_GAME      →   50 Hz                      │
│  ├─ Battery: High (2-4 mA)                            │
│  ├─ Accuracy: Fair (noisier, requires filtering)      │
│  └─ Use: Gaming, rapid height changes                 │
│                                                         │
│  SENSOR_DELAY_FASTEST   →  100+ Hz                     │
│  ├─ Battery: Very High (4-8 mA)                       │
│  ├─ Accuracy: Poor (very noisy)                       │
│  └─ Use: NOT recommended for height                   │
│                                                         │
└────────────────────────────────────────────────────────┘
```

## Typical Use Case: Floor Detection

```
Building with 4 floors (3.5m per floor)

Ground Floor
  │  P = 1013.25 hPa
  │  Alt = 0 m
  │  Floor = 0
  │
  ▼ User takes elevator up
  │
1st Floor
  │  P = 1012.83 hPa
  │  Alt = 3.5 m
  │  Floor = 1  ✓ Detected
  │
  ▼ User continues up
  │
2nd Floor
  │  P = 1012.41 hPa
  │  Alt = 7.0 m
  │  Floor = 2  ✓ Detected
  │
  ▼ User continues up
  │
3rd Floor
  │  P = 1011.99 hPa
  │  Alt = 10.5 m
  │  Floor = 3  ✓ Detected

Detection Algorithm:
floor = round(altitude / 3.5)

Accuracy: 95%+ for floor detection
```

## State Machine: Sensor Lifecycle

```
┌─────────────┐
│   CREATED   │
│  (onCreate) │
└──────┬──────┘
       │ initializeSensor()
       ▼
┌─────────────┐
│ INITIALIZED │
│ (sensor OK) │
└──────┬──────┘
       │ onResume()
       │ registerListener()
       ▼
┌─────────────┐         onSensorChanged()
│   ACTIVE    │◄────────────────────────┐
│ (receiving  │                         │
│   events)   │─────────────────────────┘
└──────┬──────┘         (continuous)
       │ onPause()
       │ unregisterListener()
       ▼
┌─────────────┐
│   PAUSED    │
│  (inactive) │
└──────┬──────┘
       │ onResume()
       │ registerListener()
       ▼
┌─────────────┐
│   ACTIVE    │
│   (again)   │
└──────┬──────┘
       │ onDestroy()
       ▼
┌─────────────┐
│  DESTROYED  │
│ (cleanup)   │
└─────────────┘
```

---

**Architecture Version**: 1.0  
**Last Updated**: 2025-10-30  
**Platform**: RayNeo XR2 / Android 12
