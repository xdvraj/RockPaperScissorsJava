const playerMoveEl = document.getElementById("playerMove");
const computerMoveEl = document.getElementById("computerMove");
const resultTextEl = document.getElementById("resultText");
const buttons = document.querySelectorAll("button[data-move]");

const resultLabel = {
    win: "You win!",
    lose: "You lose!",
    draw: "Draw!"
};

function setResultClass(result) {
    resultTextEl.classList.remove("win", "lose", "draw");
    if (result) {
        resultTextEl.classList.add(result);
    }
}

async function play(move) {
    try {
        const response = await fetch("/api/play", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ move })
        });

        if (!response.ok) {
            throw new Error("Could not play this round.");
        }

        const data = await response.json();
        playerMoveEl.textContent = data.playerMove;
        computerMoveEl.textContent = data.computerMove;
        resultTextEl.textContent = resultLabel[data.result] || "Unknown result";
        setResultClass(data.result);
    } catch (error) {
        setResultClass("");
        resultTextEl.textContent = error.message;
    }
}

buttons.forEach((button) => {
    button.addEventListener("click", () => {
        play(button.dataset.move);
    });
});
