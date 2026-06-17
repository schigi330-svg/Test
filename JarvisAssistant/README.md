# Jarvis Assistant (Android)

Ein Android-Projekt (Kotlin + Jetpack Compose), das das "Jarvis inside Obsidian"-Konzept
nachbaut: ein verbundener Obsidian-Vault-Ordner, ein CLAUDE.md mit Anweisungen, und ein
täglicher Lauf, der Notizen liest, die Claude-API aufruft und Insights/Reports zurückschreibt.

## Was wirklich funktioniert (und was nicht von selbst)

- **Vault-Anbindung**: Du wählst beim ersten Start einen echten Ordner auf deinem Gerät
  (am besten direkt deinen bestehenden Obsidian-Vault-Ordner). Die App legt dort
  `Daily/`, `Projects/`, `Knowledge/`, `Memory/`, `Outputs/` und `CLAUDE.md` an, falls sie
  fehlen. Obsidian sieht dieselben Dateien, weil es derselbe Ordner ist.
- **Claude-Anbindung**: Echte Anthropic API, kein MCP. MCP ist für Desktop/Claude Code
  gebaut, nicht für Android-Apps – die App ruft daher direkt die Messages-API auf.
- **Zeitplan**: Ein `AlarmManager`-Alarm (exakt, weckt das Gerät auf) löst einmal täglich
  einen `WorkManager`-Job aus. Ab Android 12 muss die Berechtigung "Alarme & Erinnerungen"
  einmalig erteilt werden (Button dafür ist in den Einstellungen der App).
- **Du brauchst einen eigenen Anthropic API-Key** (console.anthropic.com, kostenpflichtig
  nach Tokenverbrauch). Ohne Key macht die App nichts.

## Bekannte Lücken / nächste Schritte

- Der API-Key liegt aktuell in normalen SharedPreferences, nicht verschlüsselt. Für mehr
  als den Eigengebrauch: auf `androidx.security:security-crypto` umstellen.
- Kein Akku-Optimierungs-Ausnahme-Dialog – auf manchen Herstellern (Xiaomi, Huawei, …)
  kann das OS den Alarm/Worker trotzdem verzögern. Ggf. App manuell von Akku-Optimierung
  ausnehmen lassen.
- "Wird jede Woche schlauer" ist hier als einfache Gedächtnis-Akkumulation umgesetzt
  (`Memory/Learnings.md` wächst und wird bei jedem Lauf mitgelesen) – kein echtes Training.

## Build mit Claude Code

Android-Tooling hat sich 2026 verändert (AGP 9 brachte eingebaute Kotlin-Unterstützung mit
neuer DSL). Dieses Projekt ist mit der klassischen AGP 8.5 / Kotlin-Plugin-Syntax geschrieben,
die noch unterstützt wird. Falls dein lokales Setup AGP 9+ erzwingt, betrifft die Migration
nur die Plugin-Deklarationen in `build.gradle.kts` / `app/build.gradle.kts` – der gesamte
Kotlin-Code bleibt unverändert.

Prompt für Claude Code (im Projektordner ausführen):

```
Öffne dieses Android-Projekt. Prüfe die installierten Android-SDK/Gradle/AGP-Versionen,
aktualisiere build.gradle.kts, app/build.gradle.kts und ggf. die Gradle-Wrapper-Version auf
aktuelle stabile Versionen, falls die vorhandenen Versionen nicht zur lokalen Toolchain passen.
Baue dann mit ./gradlew assembleDebug, behebe alle Fehler, und installiere die Debug-APK auf
einem verbundenen Gerät/Emulator mit ./gradlew installDebug.
```

## Erste Schritte nach dem Build

1. App öffnen → Obsidian-Vault-Ordner auswählen.
2. Einstellungen → Anthropic API-Key eintragen, Uhrzeit setzen, ggf. "Berechtigung erteilen"
   für exakte Alarme tippen.
3. "Jetzt ausführen" tippen, um den ersten Lauf sofort zu testen, statt auf die Uhrzeit zu warten.
