# RayNeo XR2 Launcher Configuration Guide

This guide provides steps to change the default launcher on the RayNeo XR2 device between Mercury Launcher and AOSP Launcher3 using ADB.

## Prerequisites

- ADB installed and configured on your development machine
- RayNeo XR2 device connected via ADB
- USB debugging enabled on the device

## Installed Launchers

The RayNeo XR2 device comes with the following launchers:

1. **Mercury Launcher** (default): `com.ffalconxr.mercury.launcher`
   - Custom XR-optimized launcher with AR/XR features
   - Location: `/system_ext/priv-app/OverseaLauncher/OverseaLauncher.apk`

2. **AOSP Launcher3**: `com.android.launcher3`
   - Standard Android launcher
   - Lighter weight, better battery life
   - No XR-specific features

## Change Launcher from Mercury to AOSP Launcher3

### Step 1: Clear Mercury Launcher Preferences
```bash
adb shell pm clear com.ffalconxr.mercury.launcher
```

### Step 2: Set AOSP Launcher3 as Default
```bash
adb shell cmd package set-home-activity com.android.launcher3/.uioverrides.QuickstepLauncher
```

### Step 3: Grant Necessary Permissions
```bash
adb shell pm grant com.android.launcher3 android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant com.android.launcher3 android.permission.WRITE_EXTERNAL_STORAGE
```

### Step 4: Force Stop Mercury Launcher
```bash
adb shell am force-stop com.ffalconxr.mercury.launcher
```

### Step 5: Activate AOSP Launcher3
```bash
adb shell input keyevent KEYCODE_HOME
```

### Step 6 (Optional): Disable Mercury Launcher Auto-Start
```bash
adb shell pm disable-user com.ffalconxr.mercury.launcher/.home.MainActivity
```

## Revert to Mercury Launcher

### Step 1: Enable Mercury Launcher (if disabled)
```bash
adb shell pm enable com.ffalconxr.mercury.launcher/.home.MainActivity
```

### Step 2: Clear AOSP Launcher3 Preferences
```bash
adb shell pm clear com.android.launcher3
```

### Step 3: Set Mercury Launcher as Default
```bash
adb shell cmd package set-home-activity com.ffalconxr.mercury.launcher/.home.MainActivity
```

### Step 4: Grant Necessary Permissions (if needed)
```bash
adb shell pm grant com.ffalconxr.mercury.launcher android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant com.ffalconxr.mercury.launcher android.permission.WRITE_EXTERNAL_STORAGE
```

### Step 5: Force Stop AOSP Launcher3
```bash
adb shell am force-stop com.android.launcher3
```

### Step 6: Activate Mercury Launcher
```bash
adb shell input keyevent KEYCODE_HOME
```

## Verification Commands

### Check Current Default Launcher
```bash
adb shell cmd package get-home-activity
```

### Check HOME Role Assignment
```bash
adb shell cmd role get-role-holders android.app.role.HOME
```

### View Active Activity Stack
```bash
adb shell dumpsys activity activities | grep -i "launcher"
```

### Check Running Launcher Process
```bash
adb shell dumpsys activity activities | grep -A 5 "mCurrentFocus"
```

## Troubleshooting

### Launcher Not Starting
If the launcher doesn't start after changing:
1. Reboot the device: `adb reboot`
2. After reboot, press HOME: `adb shell input keyevent KEYCODE_HOME`

### Multiple Launcher Prompts
If you see a launcher selection dialog:
1. Select your preferred launcher
2. Tap "Always" to set it as default

### Permissions Issues
If apps don't show up in the launcher:
```bash
adb shell pm grant <launcher-package> android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant <launcher-package> android.permission.WRITE_EXTERNAL_STORAGE
```

## Notes

- Launcher changes persist across device reboots
- AOSP Launcher3 may not support XR-specific features that Mercury Launcher provides
- Mercury Launcher is optimized for the RayNeo XR2's AR/XR capabilities
- AOSP Launcher3 typically uses less battery and memory

## Quick Reference

### Switch to AOSP Launcher3 (One-liner)
```bash
adb shell pm clear com.ffalconxr.mercury.launcher && \
adb shell cmd package set-home-activity com.android.launcher3/.uioverrides.QuickstepLauncher && \
adb shell input keyevent KEYCODE_HOME
```

### Switch to Mercury Launcher (One-liner)
```bash
adb shell pm clear com.android.launcher3 && \
adb shell cmd package set-home-activity com.ffalconxr.mercury.launcher/.home.MainActivity && \
adb shell input keyevent KEYCODE_HOME
```
