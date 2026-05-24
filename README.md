# QRShield

A security-focused QR code scanner for Android that analyzes scanned payloads for threats before you act on them.

Built with Jetpack Compose, CameraX, ML Kit, and VirusTotal API v3.

> **Authors:** Sîrbu Vlad-Ștefan & Juja Vlad — SMD Assignment 3

---

## Features

- **Live QR / Gallery scanning** via CameraX + ML Kit Barcode
- **Multi-path threat analysis:**
  - **URLs** — typosquatting detection (Levenshtein distance vs. known brands), homoglyph substitution, misleading subdomains, suspicious TLDs, IP-based URLs
  - **WiFi** — flags open networks and WEP encryption
  - **Any payload** — XSS patterns, base64-encoded content, open redirect parameters
- **VirusTotal API v3** — cloud reputation check with offline fallback
- **Risk scoring** with three verdict levels: `SAFE`, `SUSPICIOUS`, `DANGEROUS`
- **Scan history** persisted locally with Room
- Material 3 UI, dark/light theme

## Screens

| Scanner | Gallery | Result | History | Settings |
|---|---|---|---|---|
| Live camera viewfinder with animated overlay | Galery Scanner | Verdict badge, score bar, threat details | Full scan log with swipe-to-delete | Change collor theme |
