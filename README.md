# LifeGuard Android — Kotlin native project

هذا مشروع Android أصلي مستقل يحافظ على تصميم LifeGuard، ومبني على Kotlin وJetpack Compose. المشروع منفصل عن نسخة Expo الموجودة في `/home/ubuntu/lifeguard` حتى لا يتم فقدان نسخة المعاينة.

## ما تم إنشاؤه

- Jetpack Compose مع Material 3.
- دعم العربية RTL والإنجليزية LTR من خلال `LocalLayoutDirection`.
- لوحة Dashboard، جلسات Focus، ملفات Guard، مركز Device، والإعدادات.
- ViewModel مع StateFlow.
- Room لجلسات التركيز وقواعد الحماية.
- DataStore للإعدادات واللغة والمظهر.
- مؤقت تركيز يعمل داخل ViewModel ويحفظ الجلسات المكتملة.
- Usage Access حقيقي عبر `UsageStatsManager` مع فحص الإذن قبل عرض البيانات.
- تدفق موافقة VpnService رسمي وقابل للإيقاف.
- روابط إلى إعدادات Usage Access والنظام.
- حذف محلي بعد تأكيد.

## البناء على جهاز Android Studio

تحتاج إلى Android Studio حديث، Android SDK 35، JDK 17، وGradle/Android Gradle Plugin. افتح مجلد المشروع في Android Studio ثم نفّذ Gradle Sync. بعد توفر Gradle Wrapper أو Gradle محلي، شغّل:

```bash
gradle assembleDebug
gradle test
```

ثم ثبّت الناتج من:

```text
app/build/outputs/apk/debug/app-debug.apk
```

هذه البيئة الحالية لا تحتوي على Android SDK أو Gradle، لذلك تم إنشاء المصدر وملفات البناء لكن لم يتم الادعاء بنجاح APK داخل Sandbox.

## الحدود الحالية

خدمة VPN الحالية تؤسس واجهة VpnService وتطلب موافقة النظام، لكنها ليست بعد محرك DNS كامل الإنتاج. يلزم إضافة محلل DNS محلي، قائمة نطاقات موثوقة، تحقق من التحديثات، ومعالجة تعارض VPN قبل النشر. كما يلزم إضافة AccessibilityService اختياري لتقييد التطبيقات، مع الالتزام بسياسات Google Play وعدم قراءة نص الشاشة أو البيانات الخاصة.

يجب اختبار الإشعارات، عمل الخلفية، إعادة التشغيل، تغيير المنطقة الزمنية، رفض الأذونات، انقطاع الشبكة، وأحجام الشاشات على أجهزة Android فعلية قبل إصدار APK أو AAB.
