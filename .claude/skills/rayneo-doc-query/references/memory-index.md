# RayNeo XR Docs Memory Index

## Directory Snapshot
- `Device Introduction.pdf`: hardware overview for RayNeo X-series glasses with spec tables.
- `Developer manual.pdf`: onboarding guide covering setup, SDK choices, publishing workflow.
- `Design specifications for AR glasses.pdf`: interaction, layout, visual guidelines for OST displays.
- `Capabilities & API.pdf`: ARDK SDK reference for binocular UI, input, sensors, and peripherals.
- `Development Issue_SDK.pdf`: FAQ-style troubleshooting for SDK usage and device quirks.
- `USB debugging & Screen mirroring.pdf`: step-by-step for enabling ADB, Unity deployment, scrcpy mirroring.
- `ADB.pdf`: quick command reference plus ADB toggle instructions for X3 Pro and X2.

## Quick Reference Table
| File | Pages | Primary Focus | Notable Sections | Suggested Keywords |
| --- | --- | --- | --- | --- |
| Device Introduction.pdf | 3 | Hardware lineup (X3 Pro vs X2) | Core specs, sensors, connectivity | `MicroLED`, `specification`, `FOV`, `battery` |
| Developer manual.pdf | 6 | Dev workflow overview | Pre-development, SDK options, publish flow | `RayNeo ARDK`, `Unity SDK 1.1.2`, `Open Platform` |
| Design specifications for AR glasses.pdf | 21 | UX/UI standards | Interaction patterns, display zones, typography | `temple touch`, `FoV layout`, `color usage`, `font size` |
| Capabilities & API.pdf | 24 | SDK deep dive | Binocular display, focus mgmt, touch dispatcher, 3D, sensors | `BindingPair`, `FocusHolder`, `TouchDispatcher`, `IMU`, `GPS streaming` |
| Development Issue_SDK.pdf | 10 | Troubleshooting | ADB perms, camera FOV, overheating, backgrounding | `mercury_install_allowed`, `ShareCamera`, `power 500mA` |
| USB debugging & Screen mirroring.pdf | 6 | Device setup | Unity deployment, scrcpy usage, ADB enable | `scrcpy`, `Build And Run`, `ADB switch` |
| ADB.pdf | 2 | Command cheatsheet | ADB toggle, core commands | `wall collision`, `pm grant`, `top`, `dumpsys` |

## Document Details & Retrieval Hooks
### `Device Introduction.pdf`
- Audience: quick orientation for hardware capabilities and component differences.
- Highlights: Snapdragon AR1 vs XR2 platforms, display brightness, interaction methods, sensors, battery, OS versions.
- Search cues: combine `Device Introduction` with target spec (e.g., `Device Introduction battery`, `Device Introduction connectivity`) to jump to comparison tables.

### `Developer manual.pdf`
- Audience: developers starting with RayNeo X2/X3 Pro.
- Highlights: change log (Unity SDK versioning, ADB switch), preparation checklist (hardware, manual, ADB, mirroring), SDK breakdown (Unity OpenXR ARDK, Android ARDK + IPCSDK), consumer vs enterprise scenario ideas, publishing via Open Platform.
- Search cues: `Developer manual` + `pre-development`, `SDK Introduction`, `publish`, `scenario` to locate relevant guidance.
- Cross-links: references `Design specifications`, `ADB`, `USB debugging`, `Capabilities & API`, and external Discord/Open Platform resources.

### `Design specifications for AR glasses.pdf`
- Audience: UX/UI designers and developers aligning to RayNeo OST design language.
- Highlights: interaction patterns for temple touch and ring, FoV zoning (reserved boundary, prompt/main/navigation/media areas), depth handling for 0/3/6 DOF, icon and color usage, typography sizing, component guidelines (buttons, lists, dialogs, switches, progress indicators).
- Search cues: `Design specifications` + `display area`, `color`, `font`, `ring interaction`, `0DOF`.
- Note: many visuals; text search benefits from combining with component names (e.g., `Design specifications button`).

### `Capabilities & API.pdf`
- Audience: engineers integrating with RayNeo ARDK (Android focus).
- Highlights:
  - Binocular display foundation (`BindingPair`, `BaseMirrorActivity`, `FToast`, `FDialog`).
  - Focus management constructs (`FocusHolder`, `FocusInfo`, RecyclerView trackers).
  - Touch and gesture handling via `TouchDispatcher`, `CommonTouchCallback`, `TempleAction`.
  - List scrolling strategies (fixed vs moving focus), pseudo-3D effects (`make3DEffect`).
  - Device interfaces: audio recording modes (per device), camera APIs including `ShareCamera`, IMU sensors, IPC SDK for mobile link & GPS streaming.
- Search cues: pair `Capabilities & API` with class or subsystem names (`RecyclerViewFocusTracker`, `audio_source`, `IMU Data`, `IPC SDK`).
- Tip: for Android manifest or config snippets, look for `Configuration Settings` references in this doc and the developer manual.

### `Development Issue_SDK.pdf`
- Audience: troubleshooting support for developers mid-integration.
- Highlights: direct download path for SDK, enabling install permissions, granting runtime permissions via `adb shell pm grant`, UI background transparency tips, aligning camera FoV, supported 3D formats, SDK limitations (no ARKit/ARCore, no UE5), ring/temple customization boundaries, overheating mitigation, app lifecycle constraints (no backgrounding), album sync directory requirements, GPS accuracy guidance.
- Search cues: `Development Issue` + `camera`, `overheating`, `permission`, `ShareCamera`, `background`.
- Task map: pairs well with `ADB.pdf` for command syntax and `Capabilities & API` for API follow-up.

### `USB debugging & Screen mirroring.pdf`
- Audience: setup engineers needing device connectivity and visualization.
- Highlights: enabling install permissions, Unity build pipeline checklist, scrcpy install/run steps (Windows focus, Mac references), handling Single Pass vs Multi-pass projection.
- Search cues: `USB debugging` + `scrcpy`, `Unity`, `display 0`, `ADB switch`.
- Supports quick copy/paste of commands; integrate with `ADB.pdf` for enabling toggles.

### `ADB.pdf`
- Audience: anyone enabling USB debugging or running device commands.
- Highlights: X3 Pro hidden gesture to toggle ADB (`wall collision`), X2 install permission command, table of frequently used ADB commands (install/uninstall, start activity, query CPU/memory, reboot).
- Search cues: `ADB` + `mercury_install_allowed`, `wall collision`, `pm list package`.
- Use alongside `USB debugging` doc for end-to-end connection workflows.

## Cross-Doc Task Map
- Setup device for development: start with `USB debugging & Screen mirroring` (connection, scrcpy) and `ADB.pdf` (toggle commands), then follow `Developer manual` pre-development checklist.
- Designing UI/UX: consult `Design specifications` for layout rules, then check `Capabilities & API` for implementing binocular UI and focus handling.
- SDK integration: begin with `Developer manual` SDK overview, dive into `Capabilities & API` for APIs, rely on `Development Issue_SDK` for troubleshooting quirks.
- Hardware/product positioning: use `Device Introduction` for spec comparisons when deciding feature support or communicating requirements.

## Retrieval Tips
- Use `pdftotext <file> -` piped to `rg` for fast searching inside PDFs (e.g., `pdftotext rayneo-xr-docs/Capabilities\ \&\ API.pdf - | rg 'FocusHolder'`).
- Search strategy: prefix queries with the PDF filename (e.g., `rg 'ShareCamera' rayneo-xr-docs`) to immediately see which document holds the answer.
- Maintain awareness of PDF page counts (above) to estimate reading effort before diving in.
- When investigating SDK updates or breaking changes, scan `Developer manual.pdf` first for dated notices (e.g., Unity SDK 1.1.2 requirement, ADB switch release).

