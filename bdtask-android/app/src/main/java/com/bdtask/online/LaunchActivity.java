package com.bdtask.online;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LaunchActivity — handles the bdtask://launch deep link
 *
 * Expected URI format:
 *   bdtask://launch?profile=PROFILE_ID&url=LANDING_URL&proxy=PROXY_STRING&ua=USER_AGENT
 *
 * This activity:
 *  1. Parses the deep link parameters
 *  2. Checks if GoLogin Orbita is installed
 *  3. Constructs the Orbita launch intent with proxy + profile
 *  4. Starts Orbita, then finishes itself
 *
 * Orbita deep link format (from GoLogin docs):
 *   orbita://open?profileId=ID&proxy=TYPE://USER:PASS@HOST:PORT
 */
public class LaunchActivity extends AppCompatActivity {

    // GoLogin / Orbita package names (try in order)
    private static final String[] ORBITA_PACKAGES = {
        "com.gologin.orbita",
        "com.gologin.android",
        "com.android.chrome"   // fallback — open landing page in Chrome
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Intent incoming = getIntent();
        if (incoming == null || incoming.getData() == null) {
            showError("Invalid launch link.");
            return;
        }

        Uri data = incoming.getData();
        handleDeepLink(data);
    }

    private void handleDeepLink(Uri uri) {
        TextView statusText = findViewById(R.id.statusText);

        // Parse parameters
        String profileId = uri.getQueryParameter("profile");
        String landingUrl = uri.getQueryParameter("url");
        String proxyString = uri.getQueryParameter("proxy");
        String userAgent  = uri.getQueryParameter("ua");

        if (profileId == null || profileId.isEmpty()) {
            showError("Missing profile ID in launch link.");
            return;
        }

        statusText.setText("🔗 Profile: " + profileId.substring(0, Math.min(16, profileId.length())) + "…");

        // Short delay for UX
        new Handler(Looper.getMainLooper()).postDelayed(() ->
            launchOrbita(profileId, landingUrl, proxyString, userAgent),
        800);
    }

    private void launchOrbita(String profileId, String landingUrl, String proxyString, String userAgent) {
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText("🚀 Launching Orbita…");

        // Build GoLogin Orbita URI
        // Format: orbita://open?profileId=PROFILE_ID
        // GoLogin Orbita reads the profile (which already has proxy+fingerprint locked in)
        Uri.Builder orbitaUri = new Uri.Builder()
            .scheme("orbita")
            .authority("open")
            .appendQueryParameter("profileId", profileId);

        if (proxyString != null && !proxyString.isEmpty()) {
            orbitaUri.appendQueryParameter("proxy", proxyString);
        }
        if (landingUrl != null && !landingUrl.isEmpty()) {
            orbitaUri.appendQueryParameter("startUrl", landingUrl);
        }

        Uri finalUri = orbitaUri.build();

        // Try each Orbita package
        for (String pkg : ORBITA_PACKAGES) {
            if (pkg.equals("com.android.chrome")) continue; // handle fallback separately
            if (isPackageInstalled(pkg)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, finalUri);
                    intent.setPackage(pkg);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    statusText.setText("✅ Launched in " + pkg);
                    // Finish after short delay
                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
                    return;
                } catch (Exception e) {
                    // Try next package
                }
            }
        }

        // Try generic orbita:// intent (device might handle it)
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, finalUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
            return;
        } catch (Exception ignored) {}

        // Last resort: open landing URL in browser + show install prompt
        showOrbitaNotInstalled(landingUrl);
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showOrbitaNotInstalled(String landingUrl) {
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText("⚠️ GoLogin Orbita not installed");

        Toast.makeText(this,
            "GoLogin Orbita is not installed. Please install it from gologin.com",
            Toast.LENGTH_LONG).show();

        // Open Play Store or GoLogin website
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Uri storeUri = Uri.parse("https://gologin.com/download");
                Intent intent = new Intent(Intent.ACTION_VIEW, storeUri);
                startActivity(intent);
            } catch (Exception ignored) {}
            finish();
        }, 2000);
    }

    private void showError(String msg) {
        TextView statusText = findViewById(R.id.statusText);
        if (statusText != null) statusText.setText("❌ " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3000);
    }
}
