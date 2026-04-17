# LexiMatch

LexiMatch is a playable Wordle-style game with:

- A Java console version using MVC architecture
- A browser version with a full web UI
- Four difficulty modes
- A 12,000-word guess dictionary and curated answer bank
- JUnit tests for the Java game logic

## Play In The Browser

Run the local server:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\serve-web.ps1
```

Then open:

```text
http://localhost:8080/web/
```

## Run The Java Console Version

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

## Run Tests

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
```
