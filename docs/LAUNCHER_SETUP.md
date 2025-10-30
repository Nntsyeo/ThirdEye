# ThirdEye Launcher Setup Guide

## Fix Applied: Intent Filter Verification Service Crash

The HOME intent filter has been removed from `AndroidManifest.xml` to prevent the Android 12 AOSP bug that causes "Intent Filter Verification Service keeps stopping" crashes on RayNeo XR2.

**Root Cause:** WorkManager 10KB data serialization limit in `com.android.statementservice`
**AOSP Fix:** https://android.googlesource.com/platform/frameworks/base/+/4978d1ad
**Status:** Not yet incorporated in RayNeo XR2 firmware

---

## Installation Instructions

### Step 1: Install the App

```bash
# Install the APK on your RayNeo XR2 glasses
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app will now appear in the launcher/app drawer without causing system crashes.

---

## Option A: Use as Regular App (RECOMMENDED)

**No additional steps needed!** Your app will:
- ✅ Appear in Mercury Launcher app drawer
- ✅ Launch normally without crashes
- ✅ Integrate with RayNeo XR2 Mercury SDK features
- ✅ Work with all XR-specific functionality

This is the **recommended approach** because:
- Mercury Launcher is optimized for AR/XR navigation
- Maintains system stability
- Provides best user experience on XR2

---

## Option B: Set as Default Launcher (PROGRAMMATIC)

If you still need this app to act as the home screen launcher, you can set it programmatically via ADB **after** installation:

### Prerequisites
- App must be installed first (see Step 1 above)
- ADB connection to RayNeo XR2 glasses
- USB debugging enabled on glasses

### Commands

```bash
# 1. Install the app (if not already done)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Set as default home activity (launcher)
adb shell cmd package set-home-activity com.ffalcon.thirdeye.demo/.DemoHomeActivity

# 3. Verify the setting
adb shell cmd package get-home-activity

# 4. Test by pressing the home button
adb shell input keyevent KEYCODE_HOME
```

**Expected output from Step 3:**
```
Home activity: ComponentInfo{com.ffalcon.thirdeye.demo/com.ffalcon.thirdeye.demo.DemoHomeActivity}
```

### To Revert to Default Launcher

If you want to restore the default Mercury Launcher:

```bash
# Reset to default launcher
adb shell cmd package set-home-activity com.ffalconxr.mercury.launcher/.MainActivity

# Or clear home preference (will prompt user to choose)
adb shell pm clear-home-preference
```

---

## Option C: Re-enable HOME Intent Filter (NOT RECOMMENDED)

⚠️ **WARNING:** This will bring back the crashes until RayNeo releases a firmware fix.

If you need to re-enable the HOME intent filter for testing or distribution:

### Step 1: Edit AndroidManifest.xml

Uncomment lines 42-48 in `/app/src/main/AndroidManifest.xml`:

```xml
<!-- Uncomment this block: -->
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

### Step 2: Disable Verification Service (Required)

```bash
# Disable the crash-prone service
adb shell pm disable-user --user 0 com.android.statementservice

# Reboot the device
adb reboot

# Install your app with HOME filter
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

⚠️ **Consequences:**
- Disables intent filter verification system-wide
- Security implications (app link verification won't work)
- Affects all apps, not just yours
- May re-enable after firmware updates

---

## Troubleshooting

### Crash Still Occurring?

If you still see "Intent Filter Verification Service keeps stopping":

```bash
# Clear the service cache
adb shell pm clear com.android.statementservice

# Force stop the service
adb shell am force-stop com.android.statementservice

# Clear logcat and monitor
adb logcat -c
adb logcat -s AndroidRuntime:E StatementService:E WorkManager:E
```

### App Not Appearing as Launcher Option?

Make sure your app:
1. Is installed successfully
2. Has HOME intent filter in manifest (if using Option C)
3. Is not disabled or hidden

```bash
# Check if app is installed
adb shell pm list packages | grep thirdeye

# Check HOME-capable activities
adb shell pm list packages -e | grep launcher
```

### ADB Connection Issues?

```bash
# Check connected devices
adb devices

# Reconnect if needed
adb disconnect
adb connect <DEVICE_IP>

# Or via USB
adb kill-server
adb start-server
adb devices
```

---

## Current Configuration

**Package Name:** `com.ffalcon.thirdeye.demo`
**Main Activity:** `com.ffalcon.thirdeye.demo.DemoHomeActivity`
**Mercury SDK:** Enabled (`com.rayneo.mercury.app = true`)
**Intent Filters:**
- ✅ MAIN + LAUNCHER (app drawer visibility)
- ❌ HOME + DEFAULT (removed to prevent crashes)

---

## Next Steps

### For Production Release:

1. **Monitor RayNeo Firmware Updates:**
   - Check: https://www.rayneo.com/pages/hardware-update
   - Subscribe to developer announcements
   - Test new firmware with HOME intent filter

2. **Contact RayNeo Support:**
   - Report this AOSP bug
   - Request firmware roadmap
   - Ask for AOSP commit 4978d1ad integration timeline

3. **Consider Alternative UX:**
   - Instead of replacing launcher, integrate with Mercury Launcher
   - Provide quick launch shortcuts
   - Use Mercury SDK's launcher integration APIs (if available)

4. **Testing Checklist:**
   - ✅ App launches without crashes
   - ✅ Mercury SDK features work
   - ✅ XR display and input functions properly
   - ✅ No system service crashes in logcat
   - ✅ User can navigate back to Mercury Launcher

---

## Additional Resources

**Project Documentation:**
- Launcher Configuration: `/docs/LAUNCHER_CONFIGURATION.md`
- Main README: `/README.md`

**RayNeo Resources:**
- Hardware Updates: https://www.rayneo.com/pages/hardware-update
- Support FAQ: https://www.rayneo.com/pages/support-faq

**Android Documentation:**
- Intent Filters: https://developer.android.com/guide/components/intents-filters
- Android 12 Changes: https://developer.android.com/about/versions/12/behavior-changes-12
- Launcher Apps: https://developer.android.com/guide/topics/ui/launchers

**Bug References:**
- AOSP Fix Commit: https://android.googlesource.com/platform/frameworks/base/+/4978d1ad
- LineageOS Issue: https://gitlab.com/LineageOS/issues/android/-/issues/4865

---

## Support

If you encounter issues:

1. Check logcat for errors: `adb logcat -s AndroidRuntime:E`
2. Verify RayNeo XR2 firmware version: `adb shell getprop ro.build.display.id`
3. Review this guide's troubleshooting section
4. Contact RayNeo Developer Support with:
   - Device model: ARGT28 (RayNeo XR2)
   - Firmware version
   - Logcat output
   - Steps to reproduce

---

**Last Updated:** 2025-10-30
**Fix Version:** AndroidManifest.xml - HOME intent filter removed
**Build:** app-debug.apk
