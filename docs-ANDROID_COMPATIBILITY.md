# Android compatibility matrix

| Capability | API / mechanism | User consent | Current status |
|---|---|---|---|
| Usage Access | `UsageStatsManager` | Android Settings | Implemented with permission check |
| Notifications | `POST_NOTIFICATIONS` | Runtime permission on supported versions | Manifest declared; notification channels pending |
| Website filtering | `VpnService` | `VpnService.prepare()` | Consent flow and service skeleton implemented |
| App restriction | Optional `AccessibilityService` | Accessibility Settings | Planned native integration |
| Battery | `BatteryManager` | No runtime permission | UI state reserved; device adapter pending |
| Storage | `StatFs` | No runtime permission for app storage | UI state reserved; adapter pending |
| Network | `ConnectivityManager` | No runtime permission for basic state | UI state reserved; adapter pending |
| Background reminders | WorkManager / notifications | Notification permission | Planned |

Minimum SDK is 26. Target and compile SDK are 35. Native behavior must be tested on Android 8 through current stable versions, including manufacturer-specific background restrictions.
