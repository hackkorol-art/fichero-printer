## Bericht zur Prüfung der EUDSGVO-Konformität  

**Anwendung:** „fichero-printer“ (Web-GUI und Python-CLI für Fichero D11s Etikettendrucker)  
**Version / Stand des Quellcodes:** Stand der Analyse: 10.03.2026  
**Prüfgegenstand:** Untersuchung des öffentlich vorliegenden Quellcodes auf datenschutzrelevante Funktionen, Risiken und Vereinbarkeit mit den Anforderungen der EUDSGVO/DSGVO, insbesondere im Kontext des Einsatzes durch deutsche öffentliche Stellen.  

---

## 1 Zweck und Geltungsbereich der Prüfung

Ziel dieses Berichts ist es, die Software „fichero-printer“ im Hinblick auf ihre **datenschutzrechtliche Konformität nach EUDSGVO/DSGVO** zu bewerten. Der Schwerpunkt liegt auf:

- Identifikation von **Verarbeitungstätigkeiten personenbezogener Daten** innerhalb der Anwendung,  
- Bewertung der **technischen und organisatorischen Ausgestaltung** im Lichte der DSGVO-Grundsätze (Art. 5 DSGVO),  
- Identifikation etwaiger **Datentransfers an Dritte oder in Drittländer**,  
- Aufzeigen von **Risiken und Handlungsempfehlungen** für einen datenschutzkonformen Einsatz, insbesondere durch deutsche Behörden.

Der Bericht richtet sich primär an Verantwortliche (Art. 4 Nr. 7 DSGVO), die den Einsatz dieser Software planen, sowie an Datenschutzbeauftragte, IT-Sicherheit und Fachbereiche.

---

## 2 Untersuchungsgegenstand und Systembeschreibung

Die geprüfte Lösung umfasst:

- **Python-CLI / Bibliothek**  
  - Paket `fichero-printer` gemäß `pyproject.toml`.  
  - Module unter `fichero/` (u.a. `printer.py`, `cli.py`, `imaging.py`).  
  - Zweck: Direkte Ansteuerung eines Fichero D11s (AiYin D11s) Etikettendruckers per **Bluetooth Low Energy (BLE)** oder **Classic Bluetooth (RFCOMM)**.

- **Web-GUI**  
  - Svelte/Vite-basierte Webanwendung (`web/`), inklusive Komponenten, Utils und Lokalisierungen unter `web/src`.  
  - Bereitstellung einer **Client-seitigen Label-Design-Oberfläche**, die vollständig im Browser ausgeführt wird (keine eigene Server-Backendschicht).

- **CI/Deployment-Konfiguration**  
  - GitHub Actions Workflow `deploy.yml` zur Bereitstellung statischer Assets auf GitHub Pages.

Es wurden nur die im Repository vorhandenen Dateien und Konfigurationen geprüft. Eine Untersuchung externer Infrastrukturen, Laufzeitumgebungen oder Build-Artefakte ist nicht Gegenstand dieses Berichts.

---

## 3 Methodik der Konformitätsprüfung

Die Prüfung erfolgte als **statische Quellcodeanalyse** ohne Ausführung der Software in einer Laufzeitumgebung. Die Schritte umfassten:

- Durchsicht der Projektstruktur (Python- und Web-Teil).  
- Analyse der zentralen Module für Kommunikation und Datenverarbeitung, insbesondere:
  - `fichero/printer.py` (Bluetooth-Kommunikation, Statusabfragen, Geräteinformationen),  
  - `fichero/cli.py` (CLI-Kommandologie, Verarbeitung von Text- und Bilddaten),  
  - Web-Komponenten und Utilities unter `web/src/`, z.B.:
    - `web/src/utils/file_utils.ts`,  
    - `web/src/utils/persistence.ts`,  
    - `web/src/utils/i18n.ts`,  
    - Komponenten unter `web/src/components/…`, insb. `ZplImportButton.svelte`.  
- Volltext-Suchen nach typischen datenschutzrelevanten Mustern:
  - Tracking-/Analytics-Dienste (z.B. Google Analytics, Sentry, Mixpanel, Segment),  
  - externe HTTP-/WebSocket-Aufrufe,  
  - persistente Identifier, invasive Logging-Mechanismen,  
  - Nutzung von `localStorage`/`sessionStorage` und Netzwerkbibliotheken in Python.

Die Bewertung der DSGVO-Konformität erfolgte anhand der relevanten Normen (insb. Art. 4, 5, 6, 12–22, 24, 25, 32, 35, 44 ff. DSGVO) unter Berücksichtigung eines typischen Einsatzszenarios in deutschen Behörden.

---

## 4 Beschreibung der Datenverarbeitung

### 4.1 Arten von Daten und typische Nutzungsszenarien

Die Anwendung dient zur Erstellung und zum Druck von Etiketten. Der konkrete Etiketteninhalt wird vollständig vom jeweiligen Nutzer (z.B. Behördenmitarbeiter) bestimmt. Übliche Inhalte können sein:

- Namen, Rollen, Raumnummern, organisatorische Einheiten,  
- Aktenzeichen, Vorgangsnummern, interne Kennungen,  
- Standort- oder Lagerkennzeichnungen,  
- ggf. sonstige Freitexte, die personenbezogene Daten enthalten können.

Aus technischer Sicht verarbeitet die Software:

- Text- und Bilddaten, die der Nutzer als Etiketteninhalt eingibt oder hochlädt,  
- gerätebezogene Daten des Druckers (z.B. Modell, Seriennummer, Firmware-Informationen),  
- lokale Konfigurations- und Nutzungsdaten (z.B. letzte Label-Einstellungen, Vorlagen, Schriftarten) im Browser-Storage.

### 4.2 Python-CLI / Bibliothek

Die Python-Module (`fichero/printer.py`, `fichero/cli.py`) zeigen folgendes Verhalten:

- Aufbau von Verbindungen zu Bluetooth-Geräten via:
  - BLE (über die Bibliothek `bleak`, Nutzung von `BleakClient` und `BleakScanner`),  
  - Classic Bluetooth (RFCOMM) über das Standardmodul `socket`.
- Auslesen und Setzen von Druckerstatus, Konfiguration (Dichte, Papiertyp, Shutdown-Zeit) und Geräteinformationen (Modell, Seriennummer, Firmware).
- Rendering von Etikettentext und -bildern (mittels `Pillow`) in ein 1-Bit-Rasterbild und Übertragung dieses Rasters an den Drucker.

Wesentliche Feststellung:  
**Es finden sich im Python-Code keine HTTP-/HTTPS-Aufrufe oder sonstigen Netzwerkzugriffe auf externe Server.**  
Die Kommunikation beschränkt sich ausschließlich auf:

- die Bluetooth-Verbindung zwischen Rechner und Drucker,  
- das lokale Dateisystem und die CLI-Ein-/Ausgabe.

### 4.3 Web-GUI

Die Webanwendung (Svelte) arbeitet vollständig **Client-seitig** im Browser. Wichtige Beobachtungen:

- **Persistenz im Browser:**
  - Modul `web/src/utils/persistence.ts` implementiert eine Klasse `LocalStoragePersistence` sowie eine Hilfsfunktion `writablePersisted`.  
  - Persistiert werden u.a.:  
    - letzte Label-Einstellungen (Layout, Vorschauparameter),  
    - gespeicherte Etikettenvorlagen (`saved_label_*`),  
    - Label-Presets, Automationseinstellungen, Standardvorlage,  
    - zuletzt genutzter Verbindungstyp (`connection_type`),  
    - Sprachpräferenz des Nutzers (`locale`),  
    - Zwischenspeicher für Fontdaten.  
  - Die Speicherung erfolgt explizit im **`localStorage` des Browsers**; es wird keine automatische Übertragung dieser Daten an Server implementiert.

- **Externe HTTP-Aufrufe:**
  - Eine gezielte Suche zeigt **nur einen relevanten Netzwerkaufruf** in `ZplImportButton.svelte`:  
    - `fetch("https://api.labelary.com/v1/printers/.../labels/...")` mit HTTP-Methode `POST`.  
    - Übertragen wird der vom Nutzer ausgewählte **ZPL-Text** (Inhalt der Etikettenbeschreibung) zur Konvertierung in ein PNG-Bild.  
    - Dieser Aufruf erfolgt ausschließlich, wenn der Nutzer aktiv die Funktion „ZPL-Datei importieren“ nutzt.
  - Daneben existieren lediglich statische Links (`<a href="https://github.com/...">`, `https://fonts.google.com`), die keinen automatischen Datentransfer durch die Anwendung selbst implementieren.

- **Kein Tracking / Analytics:**
  - Es sind **keine** Einbindungen von Analytics-/Trackingdiensten (Google Analytics, Sentry, Matomo, Facebook Pixel, etc.) vorhanden.  
  - Es werden keine Session-IDs, benutzerbezogenen Profile oder Telemetriedaten an Dritte gesendet.

---

## 5 Rollen, Verantwortlichkeiten und Rechtsgrundlagen

### 5.1 Verantwortlicher und Auftragsverarbeiter

- Verantwortlicher nach Art. 4 Nr. 7 DSGVO ist die jeweilige **Organisation/Behörde**, die die Software einsetzt und über Zwecke und Mittel der Verarbeitung entscheidet.  
- Die Entwickler veröffentlichen den Quellcode als Open-Source-Projekt; sie betreiben keine zentrale Plattform und verarbeiten keine Daten im Auftrag der Behörde. Ein klassisches **Auftragsverarbeitungsverhältnis** liegt daher in der Regel nicht vor.
- Bei Nutzung der Labelary-API ist der Labelary-Betreiber faktisch **Empfänger** von Daten (siehe Abschnitt 7) und nach eigenständiger Prüfung rechtlich einzuordnen (Dritter, ggf. Auftragsverarbeiter oder sonstiger Verantwortlicher).

### 5.2 Mögliche Rechtsgrundlagen (Art. 6 DSGVO)

Die Rechtsgrundlage hängt vom konkreten Einsatzzweck ab. Typische Konstellation bei deutschen Behörden:

- **Art. 6 Abs. 1 lit. e DSGVO i.V.m. § 3 BDSG**  
  – Verarbeitung zur Wahrnehmung einer Aufgabe im öffentlichen Interesse oder in Ausübung öffentlicher Gewalt (z.B. Kennzeichnung von Akten, Räumen, Geräten, interner Organisation).

Die konkrete Rechtsgrundlage ist **behördenseitig** zu dokumentieren (Verzeichnis von Verarbeitungstätigkeiten).

---

## 6 Bewertung entlang zentraler DSGVO-Anforderungen

### 6.1 Grundsätze der Verarbeitung (Art. 5 DSGVO)

- **Zweckbindung:**  
  - Die Software ist technisch auf den Zweck „Etikettendesign und -druck“ ausgerichtet.  
  - Es gibt keine Funktionen zur weitergehenden Profilbildung oder Nutzung der Etikettendaten für andere Zwecke.

- **Datenminimierung:**  
  - Welche personenbezogenen Daten verarbeitet werden, bestimmt vollständig die nutzende Stelle (Inhalt der Etiketten).  
  - Die Anwendung speichert nur solche Daten, die für die Nutzung (Layouts, Vorlagen, letzte Einstellungen) erforderlich sind.

- **Richtigkeit:**  
  - Verantwortlich für die Inhalte bleibt die nutzende Organisation; die Software bietet keine automatischen Datenanreicherungen oder -manipulationen.

- **Speicherbegrenzung:**  
  - Browserseitige Speicherung in `localStorage` erfolgt ohne eingebaute automatische Löschfristen.  
  - Die Einhaltung von Aufbewahrungsfristen und Löschkonzepten muss organisatorisch sichergestellt werden (z.B. Richtlinien für das Speichern nur technischer/unsensibler Vorlagen, periodisches Löschen).

- **Integrität und Vertraulichkeit:**  
  - Es werden keine Daten unverschlüsselt über das Internet an den Entwickler gesendet.  
  - Die Bluetooth-Verbindung ist grundsätzlich durch die Fähigkeiten des Protokolls und des Endgeräts geschützt; zusätzliche organisatorische Maßnahmen (physische Sicherheit, Zugriffsrechte auf Endgeräte) sind erforderlich.

### 6.2 Betroffenenrechte (Art. 12–22 DSGVO)

- Die Software selbst implementiert **keine Nutzeraccounts** und keine Server, auf denen personenbezogene Daten langfristig gespeichert würden.  
- Rechte wie Auskunft, Berichtigung, Löschung, Einschränkung oder Widerspruch sind daher **gegenüber der nutzenden Organisation** geltend zu machen.  
- Die technische Umsetzung dieser Rechte (z.B. Löschen von gespeicherten Etiketten im Browser, Nicht-Speichern sensibler Inhalte in Vorlagen) kann durch die Anwendung unterstützt werden, ist jedoch organisatorisch zu regeln.

### 6.3 Datenschutz durch Technikgestaltung und Voreinstellungen (Art. 25 DSGVO)

Positiv ist zu bewerten:

- Die Architektur ist grundsätzlich **datensparsam**, da kein zentrales Backend benötigt wird und Daten im Regelfall lokal verbleiben.  
- Es gibt **keine** aktivierte Telemetrie oder trackingbezogene Voreinstellungen.

Zu beachten:

- Labelary-Nutzung ist standardmäßig möglich; bei Einsatz in besonders sensiblen Umgebungen sollte diese Funktionalität technisch/organisatorisch eingeschränkt oder deaktiviert werden, sofern ein konformer Drittlandtransfer nicht sichergestellt werden kann.

### 6.4 Sicherheit der Verarbeitung (Art. 32 DSGVO)

Auf Quellcode-Ebene erkennbare Eigenschaften:

- Keine externen Netzwerkverbindungen im Python-Teil; Begrenzung auf lokale Bluetooth-Kommunikation.  
- Web-Teil: Isolierte Nutzung von Browser-APIs; Persistenz in `localStorage` mit Validierung der gespeicherten Strukturen (u.a. Zod-Schemas).  
- Kein eigenständiges Berechtigungs- oder Rollenmodell; Zugriffsschutz muss im Rahmen der behördlichen IT umgesetzt werden (Betriebssystem-, Netzwerk- und Arbeitsplatzsicherheit).

Es sind jedoch **behördliche TOMs** notwendig, u.a.:

- Zugriffsschutz auf Arbeitsplatzrechner, auf denen die Anwendung ausgeführt wird.  
- Sichere Pairing- und Nutzungskonzepte für Bluetooth-Drucker (Vermeidung unbefugter Nutzung).  
- Regelmäßige Aktualisierung der zugrundeliegenden Softwarekomponenten (Browser, Python-Runtime, Bibliotheken).

---

## 7 Drittlandübermittlungen und externe Dienste (Art. 44 ff. DSGVO)

Die einzige im Quellcode identifizierte externe Datenübermittlung findet im Rahmen der **Labelary-Integration** statt:

- In der Komponente `ZplImportButton.svelte` wird bei Nutzung der Importfunktion ein HTTP-`POST` Request an  
  `https://api.labelary.com/v1/printers/{...}/labels/{...}`  
  gesendet.  
- Der Request enthält den **ZPL-Inhalt** der Etikette (Text-Layout-Beschreibung), wodurch personenbezogene Daten betroffen sein können, wenn diese im ZPL kodiert sind.

Risiken und offene Punkte:

- Der Betreiber von `api.labelary.com` ist nach öffentlich verfügbaren Informationen typischerweise in einem **Drittland (regelmäßig USA)** anzusiedeln.  
- Es besteht die Möglichkeit, dass die übertragenen Inhalte **protokolliert oder weiterverarbeitet** werden.  
- Ohne geeignete Garantien (z.B. Standardvertragsklauseln, zusätzliche technische Schutzmaßnahmen) kann die Konformität eines solchen Drittlandtransfers zweifelhaft sein, insbesondere für öffentliche Stellen.

Bewertung:

- Wird die Labelary-Funktion mit personenbezogenen Etiketteninhalten genutzt, liegt eine **Übermittlung personenbezogener Daten in ein Drittland** vor, die nach Art. 44 ff. DSGVO zu bewerten und abzusichern ist.  
- Für besonders schutzbedürftige Daten (z.B. Gesundheitsdaten, Daten von Schutzbedürftigen, interne Sicherheitskennzeichnungen) ist der Einsatz externer Cloud-Dienste ohne nachgewiesene Schutzmaßnahmen kritisch.

Empfehlung:

- Für einen **strikt datenschutzkonformen Einsatz** in deutschen Behörden sollte:
  - die Labelary-Integration **entweder nicht genutzt** werden, oder  
  - der Einsatz auf **nicht-personenbezogene oder ausreichend pseudonymisierte Daten** beschränkt werden, oder  
  - alternativ eine **On-Premise-/lokale Konvertierungslösung** ohne Drittlandtransfer implementiert werden.  
- Vor einer produktiven Nutzung mit personenbezogenen Daten ist eine **rechtliche Prüfung der Drittlandübermittlung** (inkl. Transfer Impact Assessment) erforderlich.

---

## 8 Datenschutz-Folgenabschätzung (Art. 35 DSGVO)

Ob eine Datenschutz-Folgenabschätzung (DSFA) erforderlich ist, hängt vom konkreten Einsatzszenario ab:

- **Reiner interner Etikettendruck** mit begrenztem Personenkreis und ohne zentrale Speicherung großer Datenmengen wird typischerweise **nicht automatisch** eine hohe Risikolage im Sinne des Art. 35 DSGVO auslösen.  
- Werden jedoch Etiketten in großem Umfang, mit sensiblen Daten oder zur systematischen Kennzeichnung von Personen verarbeitet, oder erfolgt eine **umfangreiche Drittlandübermittlung** via Labelary, kann eine DSFA angezeigt sein.

Die Entscheidung über die DSFA-Pflicht obliegt der jeweiligen Organisation.

---

## 9 Gesamtergebnis und Empfehlungen

### 9.1 Zusammenfassende Bewertung

Auf Basis der statischen Quellcodeanalyse ergibt sich folgendes Bild:

- **Keine Hinweise auf versteckte Trojaner, Backdoors oder Tracking-Komponenten.**  
- Die Verarbeitung ist **technisch auf Etikettendesign und -druck beschränkt**; es gibt keine Funktionalität zur zentralen Sammlung oder Auswertung von Nutzerdaten.  
- Die Python-CLI kommuniziert ausschließlich mit dem Drucker und greift nicht auf externe Server zu.  
- Die Web-GUI speichert Nutzungsdaten **lokal im Browser** (localStorage) und überträgt sie nicht automatisch an Drittserver.  
- Einziger relevanter externer Datenfluss: **Labelary-API**, die bei Verwendung personenbezogene Inhalte in ein Drittland übermittelt.

Unter der Voraussetzung, dass:

- die Software **ohne Labelary** oder nur mit **nicht-personenbezogenen / pseudonymisierten Inhalten** genutzt wird,  
- angemessene **TOMs** für Endgeräte und Bluetooth-Verbindungen implementiert sind,  
- die Nutzung im **Verzeichnis der Verarbeitungstätigkeiten** dokumentiert und auf einer geeigneten Rechtsgrundlage basiert,

erscheint ein **datenschutzkonformer Einsatz nach EUDSGVO/DSGVO grundsätzlich möglich**.

### 9.2 Konkrete Empfehlungen für einen datenschutzkonformen Einsatz

- **Labelary-Nutzung steuern:**
  - Bei Verarbeitung personenbezogener Etiketteninhalte sollte die Labelary-Funktion entweder deaktiviert oder nur nach vorheriger datenschutzrechtlicher Bewertung genutzt werden.  
  - Alternativ sollte eine lokale Konvertierungslösung implementiert werden.

- **Organisatorische Vorgaben zur Speicherung im Browser:**
  - Festlegen, ob und in welchem Umfang personenbezogene Inhalte in Vorlagen gespeichert werden dürfen.  
  - Regelungen zum **Löschen** gespeicherter Vorlagen in regelmäßigen Abständen bzw. nach Wegfall des Zwecks.

- **Integration in behördliche Datenschutzdokumentation:**
  - Aufnahme der Software in das behördliche **Verzeichnis von Verarbeitungstätigkeiten** mit Beschreibung der Zwecke, Kategorien von Daten und Betroffenen, Rechtsgrundlage, Speicherfristen und TOMs.  
  - Anpassung der **Datenschutzhinweise** (sofern Etikettendruck ein relevanter Verarbeitungsschritt ist).

- **Technische Mindeststandards:**
  - Betrieb nur auf **verwalteten, gehärteten Endgeräten**.  
  - Sicheres Management von Bluetooth-Druckern (z.B. Pairing nur mit autorisierten Geräten, physischer Schutz).  
  - Regelmäßige Updates von Browser, Betriebssystem, Python-Runtime und Bibliotheken.

---

## 10 Hinweis zum Charakter dieses Berichts

Dieser Bericht basiert ausschließlich auf der **statischen Analyse des bereitgestellten Quellcodes** und ersetzt keine individuelle **rechtliche Beratung**.  
Die endgültige Bewertung der EUDSGVO-/DSGVO-Konformität ist abhängig von:

- dem konkreten Einsatzkontext,  
- den organisatorischen Maßnahmen und Richtlinien der nutzenden Stelle,  
- der Auswahl und Konfiguration externer Dienste (insb. Labelary),  
- den tatsächlichen Inhalten der verarbeiteten Etiketten.

Verantwortliche Stellen sollten diesen Bericht als **technisch-organisatorische Grundlage** nutzen und gemeinsam mit Datenschutzbeauftragten und Rechtsabteilung eine abschließende Bewertung vornehmen.

