## L2Loot — Windows Installation Guide

### Quick install (MSI)
1. Open the project's Releases page and download the latest `.msi` installer.

   ![Placeholder — Releases page showing latest MSI](./images/install-01-releases.png)

2. Run the downloaded `.msi`.

   ![Placeholder — Downloaded MSI in File Explorer](./images/install-02-run-msi.png)

3. Bypass Windows SmartScreen (unsigned app): when Windows shows "Windows protected your PC",
   click "More info" → "Run anyway".

   ![Placeholder — SmartScreen warning](./images/install-03-smartscreen-warning.png)
   ![Placeholder — Click More info → Run anyway](./images/install-04-smartscreen-run-anyway.png)

4. Follow the installer wizard:
   - Choose your install location.
   - Click Install and allow any permission prompts.
   - Click Finish.

5. Launch L2Loot:
   - Use the desktop shortcut

### Notes about Windows warning
- The installer is not code-signed. Windows may display a warning. If you trust this app, use "More info" → "Run anyway" to proceed.

### Update to a newer version
1. Close L2Loot if it is running.
2. Download the latest `.msi` from the Releases page.
3. Run it just like a fresh install and agree to replace/update the existing installation.
4. Your stored prices data will be kept. The app's database is stored in a separate location and is not removed by updating.

### Uninstall (optional)
- Use "Add or remove programs" in Windows, search for `L2Loot`, and click Uninstall.


