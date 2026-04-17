const DIFFICULTIES = {
    BEGINNER: { label: "Beginner", maxAttempts: 8, hardMode: false, hintAfter: 3 },
    STANDARD: { label: "Standard", maxAttempts: 6, hardMode: false, hintAfter: Infinity },
    ADVANCED: { label: "Advanced", maxAttempts: 6, hardMode: true, hintAfter: Infinity },
    EXPERT: { label: "Expert", maxAttempts: 5, hardMode: true, hintAfter: Infinity }
};

const KEYBOARD_ROWS = [
    ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
    ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
    ["ENTER", "Z", "X", "C", "V", "B", "N", "M", "BACK"]
];

const STATUS_PRIORITY = { empty: 0, absent: 1, present: 2, correct: 3 };

const state = {
    difficultyKey: "STANDARD",
    allowedWords: new Set(),
    answerWords: [],
    targetWord: "",
    guesses: [],
    currentGuess: "",
    keyboardState: {},
    isOver: false,
    stats: loadStats()
};

const elements = {
    board: document.getElementById("board"),
    keyboard: document.getElementById("keyboard"),
    messageBanner: document.getElementById("messageBanner"),
    hintBanner: document.getElementById("hintBanner"),
    attemptsText: document.getElementById("attemptsText"),
    dictionaryText: document.getElementById("dictionaryText"),
    stateText: document.getElementById("stateText"),
    difficultySelect: document.getElementById("difficultySelect"),
    newGameButton: document.getElementById("newGameButton"),
    winsCount: document.getElementById("winsCount"),
    playedCount: document.getElementById("playedCount"),
    winRate: document.getElementById("winRate")
};

async function bootstrap() {
    renderKeyboard();
    bindEvents();

    try {
        const [allowedWords, answerWords] = await Promise.all([
            loadWordList("../src/main/resources/allowed-guesses.txt"),
            loadWordList("../src/main/resources/answer-bank.txt")
        ]);

        state.allowedWords = new Set(allowedWords);
        state.answerWords = answerWords;
        elements.dictionaryText.textContent = `${allowedWords.length.toLocaleString()} words`;
        startNewGame();
    } catch (error) {
        setMessage("Could not load dictionary files. Start the app with the local server script in scripts/.");
        elements.stateText.textContent = "Load failed";
        console.error(error);
    }
}

async function loadWordList(path) {
    const response = await fetch(path);
    if (!response.ok) {
        throw new Error(`Failed to load ${path}: ${response.status}`);
    }

    const content = await response.text();
    return content
        .split(/\r?\n/)
        .map((word) => word.trim().toLowerCase())
        .filter((word) => /^[a-z]{5}$/.test(word));
}

function bindEvents() {
    elements.newGameButton.addEventListener("click", startNewGame);
    elements.difficultySelect.addEventListener("change", () => {
        state.difficultyKey = elements.difficultySelect.value;
        startNewGame();
    });

    window.addEventListener("keydown", (event) => {
        if (event.ctrlKey || event.metaKey || event.altKey) {
            return;
        }

        const key = event.key.toUpperCase();
        if (/^[A-Z]$/.test(key)) {
            addLetter(key);
        } else if (key === "BACKSPACE") {
            removeLetter();
        } else if (key === "ENTER") {
            submitGuess();
        }
    });
}

function startNewGame() {
    if (!state.answerWords.length) {
        return;
    }

    state.targetWord = state.answerWords[Math.floor(Math.random() * state.answerWords.length)];
    state.guesses = [];
    state.currentGuess = "";
    state.keyboardState = {};
    state.isOver = false;

    const difficulty = currentDifficulty();
    elements.attemptsText.textContent = `0 / ${difficulty.maxAttempts}`;
    elements.stateText.textContent = `${difficulty.label} mode`;
    hideHint();
    setMessage(difficulty.hardMode
        ? "Hard mode is active. Confirmed clues must be reused."
        : "Enter any five-letter word to start.");
    renderBoard();
    renderKeyboard();
    renderStats();
}

function currentDifficulty() {
    return DIFFICULTIES[state.difficultyKey];
}

function addLetter(letter) {
    if (state.isOver || state.currentGuess.length >= 5) {
        return;
    }

    state.currentGuess += letter.toLowerCase();
    renderBoard();
}

function removeLetter() {
    if (state.isOver || !state.currentGuess.length) {
        return;
    }

    state.currentGuess = state.currentGuess.slice(0, -1);
    renderBoard();
}

function submitGuess() {
    if (state.isOver) {
        return;
    }

    const guess = state.currentGuess.toLowerCase();
    const difficulty = currentDifficulty();

    if (guess.length !== 5) {
        setMessage("Enter a full five-letter word.");
        return;
    }

    if (!state.allowedWords.has(guess)) {
        setMessage("That word is not in the LexiMatch dictionary.");
        return;
    }

    const hardModeError = validateHardMode(guess);
    if (hardModeError) {
        setMessage(hardModeError);
        return;
    }

    const evaluation = evaluateGuess(guess, state.targetWord);
    state.guesses.push(evaluation);
    state.currentGuess = "";
    updateKeyboard(evaluation);

    elements.attemptsText.textContent = `${state.guesses.length} / ${difficulty.maxAttempts}`;

    if (guess === state.targetWord) {
        finishGame(true, `Solved in ${state.guesses.length} guess${state.guesses.length === 1 ? "" : "es"}.`);
    } else if (state.guesses.length >= difficulty.maxAttempts) {
        finishGame(false, `Out of attempts. The word was ${state.targetWord.toUpperCase()}.`);
    } else {
        setMessage("Guess recorded.");
        maybeShowHint();
    }

    renderBoard(true);
    renderKeyboard();
}

function validateHardMode(guess) {
    const difficulty = currentDifficulty();
    if (!difficulty.hardMode) {
        return "";
    }

    for (const evaluation of state.guesses) {
        for (const result of evaluation.results) {
            if (result.status === "correct" && guess[result.index] !== result.letter) {
                return "Hard mode: keep confirmed letters fixed in place.";
            }
        }

        for (const result of evaluation.results) {
            if (result.status === "present" && !guess.includes(result.letter)) {
                return "Hard mode: include revealed letters in future guesses.";
            }
        }
    }

    return "";
}

function evaluateGuess(guess, target) {
    const remaining = new Map();
    const statuses = new Array(5).fill("absent");

    for (let index = 0; index < 5; index += 1) {
        if (guess[index] !== target[index]) {
            remaining.set(target[index], (remaining.get(target[index]) || 0) + 1);
        }
    }

    for (let index = 0; index < 5; index += 1) {
        if (guess[index] === target[index]) {
            statuses[index] = "correct";
        } else if ((remaining.get(guess[index]) || 0) > 0) {
            statuses[index] = "present";
            remaining.set(guess[index], remaining.get(guess[index]) - 1);
        }
    }

    return {
        guess,
        results: statuses.map((status, index) => ({
            index,
            status,
            letter: guess[index]
        }))
    };
}

function updateKeyboard(evaluation) {
    for (const result of evaluation.results) {
        const current = state.keyboardState[result.letter] || "empty";
        if (STATUS_PRIORITY[result.status] > STATUS_PRIORITY[current]) {
            state.keyboardState[result.letter] = result.status;
        }
    }
}

function finishGame(won, message) {
    state.isOver = true;
    setMessage(message);
    elements.stateText.textContent = won ? "Win" : "Loss";
    recordStats(won);
}

function maybeShowHint() {
    const difficulty = currentDifficulty();
    if (state.guesses.length >= difficulty.hintAfter && difficulty.hintAfter !== Infinity) {
        elements.hintBanner.hidden = false;
        elements.hintBanner.textContent = `Hint: the word starts with ${state.targetWord[0].toUpperCase()}.`;
    }
}

function hideHint() {
    elements.hintBanner.hidden = true;
    elements.hintBanner.textContent = "";
}

function setMessage(message) {
    elements.messageBanner.textContent = message;
}

function renderBoard(animate = false) {
    const difficulty = currentDifficulty();
    elements.board.innerHTML = "";

    for (let rowIndex = 0; rowIndex < difficulty.maxAttempts; rowIndex += 1) {
        const row = document.createElement("div");
        row.className = "board-row";

        const completedGuess = state.guesses[rowIndex];
        const activeGuess = rowIndex === state.guesses.length ? state.currentGuess : "";

        for (let colIndex = 0; colIndex < 5; colIndex += 1) {
            const tile = document.createElement("div");
            tile.className = "tile";

            if (completedGuess) {
                const result = completedGuess.results[colIndex];
                tile.textContent = result.letter;
                tile.classList.add(result.status);
                if (animate) {
                    tile.classList.add("reveal");
                }
            } else if (activeGuess[colIndex]) {
                tile.textContent = activeGuess[colIndex];
                tile.classList.add("filled");
            }

            row.appendChild(tile);
        }

        elements.board.appendChild(row);
    }
}

function renderKeyboard() {
    elements.keyboard.innerHTML = "";

    for (const rowKeys of KEYBOARD_ROWS) {
        const row = document.createElement("div");
        row.className = "keyboard-row";

        for (const keyLabel of rowKeys) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "key";
            button.textContent = keyLabel === "BACK" ? "Back" : keyLabel;

            if (keyLabel === "ENTER" || keyLabel === "BACK") {
                button.classList.add("wide");
            }

            if (keyLabel.length === 1) {
                const status = state.keyboardState[keyLabel.toLowerCase()];
                if (status) {
                    button.classList.add(status);
                }
            }

            button.addEventListener("click", () => {
                if (keyLabel === "ENTER") {
                    submitGuess();
                } else if (keyLabel === "BACK") {
                    removeLetter();
                } else {
                    addLetter(keyLabel);
                }
            });

            row.appendChild(button);
        }

        elements.keyboard.appendChild(row);
    }
}

function loadStats() {
    try {
        const parsed = JSON.parse(localStorage.getItem("leximatch-stats") || "{}");
        return {
            played: Number.isFinite(parsed.played) ? parsed.played : 0,
            wins: Number.isFinite(parsed.wins) ? parsed.wins : 0
        };
    } catch {
        return { played: 0, wins: 0 };
    }
}

function recordStats(won) {
    state.stats.played += 1;
    if (won) {
        state.stats.wins += 1;
    }

    localStorage.setItem("leximatch-stats", JSON.stringify(state.stats));
    renderStats();
}

function renderStats() {
    elements.playedCount.textContent = state.stats.played.toString();
    elements.winsCount.textContent = state.stats.wins.toString();

    const rate = state.stats.played === 0
        ? 0
        : Math.round((state.stats.wins / state.stats.played) * 100);

    elements.winRate.textContent = `${rate}%`;
}

bootstrap();
