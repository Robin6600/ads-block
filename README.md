# Disturb Robin - Ad Blocker VPN

"No Ads. Ever. Not from Games, YouTube, Facebook, or Any App."

Disturb Robin is a production-ready, non-root Android application that blocks advertisements device-wide using a local VPN service.

## Core Features
*   **100% Non-Root**: Uses Android's `VpnService` to capture outbound traffic.
*   **System-Wide Blocking**: Blocks ads in games, browsing, and social media natively using DNS manipulation.
*   **Ad-Blocking DNS**: Deflects ad connections by forwarding port 53 UDP requests to AdGuard DNS.
*   **Privacy-First**: No data is sent to external servers for tracking or analytics.
*   **Material 3 UI**: Built with Jetpack Compose featuring a sleek pulse animation and stats dashboard.
*   **Bengali & English Support**: Internationalized strings.

## How It Works
The app creates a local VPN tunnel. It registers known ad-blocking DNS servers (such as AdGuard DNS) and intercepts internet traffic. Ad requests are dropped or null-routed without ever reaching external servers.

## Build and Run
1. Open in Android Studio or build via `.gradlew assembleDebug`.
2. Install the APK to your device.
3. Tap the central Shield icon to start the VPN block service.
4. When prompted, allow the VPN configuration.

## License
MIT License
