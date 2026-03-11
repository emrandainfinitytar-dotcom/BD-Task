# BD Task — Android App
## Package: com.bdtask.online

---

## What This App Does

1. **WebView wrapper** — Opens `https://bdtask.online` as a full-screen web app
2. **Deep link handler** — Intercepts `bdtask://launch?...` links from the website
3. **Orbita launcher** — Passes the GoLogin profile ID + proxy to Orbita/GoLogin and launches it

---

## Deep Link Flow

```
User clicks "Create Profile & Launch" on website
    ↓
Website creates GoLogin profile via API (proxy + fingerprint locked in)
    ↓
Website shows popup modal → "Open in BD Task App"
    ↓
User taps → triggers: bdtask://launch?profile=ID&url=URL&proxy=TYPE://user:pass@host:port
    ↓
Android intercepts the URI scheme
    ↓
LaunchActivity.java parses parameters
    ↓
Builds: orbita://open?profileId=ID&proxy=...&startUrl=...
    ↓
Orbita opens with locked proxy + fingerprint, loads task landing page
```

---

## Build Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Gradle 8.1+
- minSdk: 24 (Android 7.0)

---

## How to Build

### 1. Open in Android Studio
```
File → Open → select bdtask-android/ folder
```

### 2. Sync Gradle
```
Click "Sync Now" when prompted
```

### 3. Build APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK
```
Build → Generate Signed Bundle / APK
→ APK
→ Create/select keystore
→ Build
```
Release APK: `app/build/outputs/apk/release/app-release.apk`

---

## Serving the APK from your Website

Place the built APK at:
```
/var/www/html/bdtask.online/app/bdtask.apk
```

The website's download modal links to: `https://bdtask.online/app/bdtask.apk`

Add to `.htaccess` for proper MIME type:
```apache
AddType application/vnd.android.package-archive .apk
```

---

## Deep Link URI Format

```
bdtask://launch
  ?profile=GOLOGIN_PROFILE_ID
  &url=ENCODED_LANDING_URL
  &proxy=ENCODED_PROXY_STRING     (e.g. https://user:pass@host:10000)
  &ua=ENCODED_USER_AGENT
```

All parameters are URL-encoded.

---

## GoLogin Orbita Integration

The app launches Orbita using:
```
orbita://open?profileId=PROFILE_ID&proxy=PROXY_STRING&startUrl=LANDING_URL
```

**GoLogin Orbita** must be installed separately:
- Download from: https://gologin.com/download
- Or Google Play: search "GoLogin"

Orbita uses the profile created by the website (which already has the proxy and 
full fingerprint locked in: WebGL, Canvas, Audio, Screen, Timezone, Language).

---

## Customization

- Change `HOME_URL` in `MainActivity.java` if your domain changes
- Add more Orbita package names to `ORBITA_PACKAGES[]` in `LaunchActivity.java`
- Modify `network_security_config.xml` to allow additional domains

---

## Security Notes

- cleartext HTTP is disabled (`usesCleartextTraffic="false"`)
- Only bdtask.online and api.gologin.com domains are explicitly allowed
- WebView disables `allowFileAccess` and `allowContentAccess`
- External links open in system browser, not in WebView
