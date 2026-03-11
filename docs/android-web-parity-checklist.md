# Android vs Web Parity Checklist

This checklist is the source of truth for functional parity between:
- Web: `web/src/components/*`
- Android: `android-app/app/src/main/java/com/example/ficheroandroid/*`

Status legend:
- `pass`: implemented and validated in Android
- `gap`: not implemented yet
- `limitation`: constrained by platform/library differences

## 1) Connection and Diagnostics

- [pass] Connect/disconnect printer
- [pass] Select paired printer before connect
- [pass] Diagnostic panel with info metadata
- [pass] Heartbeat visibility and fails counter
- [pass] Service actions parity (refresh info, reconnect)
- [limitation] Sound and low-level firmware controls are not exposed by current Android transport layer

## 2) Label Designer Core

- [pass] Add objects: text, qrcode, barcode, rectangle, line, circle, image
- [pass] Select object and edit position/size/font
- [pass] Clone/delete selected object
- [pass] Undo/redo history
- [pass] Center object horizontally/vertically
- [pass] Arrange selected object to front/back
- [gap] Keyboard shortcuts parity (delete, arrows, ctrl/cmd combos)

## 3) Print Preview and Print Pipeline

- [pass] Preview modal with print controls
- [pass] Copies and density controls
- [pass] Postprocess: threshold/dither/bayer
- [pass] Invert control
- [pass] Label type control
- [pass] Offset controls (inner/outer, x/y)
- [pass] CSV variable replacement
- [pass] CSV `$times` row expansion
- [pass] Print progress visibility
- [pass] Print cancel action
- [gap] System printer fallback parity

## 4) Templates and Data Portability

- [pass] Save/load templates locally
- [pass] Browse/select saved template index
- [pass] Backward-compatible template parsing for older records
- [pass] Explicit JSON export template action
- [pass] Explicit JSON import template action

## 5) UI and Behavior Alignment

- [pass] Web-inspired main layout (header/connection/editor/footer)
- [pass] Two-toolbar structure
- [pass] Dark theme tokens aligned with web palette
- [limitation] Exact 1:1 canvas behavior with Fabric.js is not fully possible in Compose without migrating to equivalent scene graph

## Validation Checklist (must be green)

- [x] `gradlew.bat compileDebugKotlin`
- [x] Lints clean for edited files
- [ ] Smoke test: connect -> design -> preview -> print
- [ ] Smoke test: save template -> reload -> print
