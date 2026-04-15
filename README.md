# Java Chess Game

A playable console chess game built in Java with:

- Full move validation
- Check, checkmate, and stalemate detection
- Castling, en passant, and promotion support
- A minimax AI with alpha-beta pruning

## Run

```powershell
javac -d out src\chess\*.java
java -cp out chess.ChessConsoleApp
```

## Controls

- Enter moves like `e2e4`
- Use promotion suffixes like `e7e8q`
- Type `quit` to end the session
