# Anansi: Tic Tac Toe Implementation

## Description

This repository contains an implementation of Tic Tac Toe using Anansi, a graph-based grid data structure. The project showcases how to leverage graph algorithms to manage and solve grid-like games.

Anansi (pronounced /əˈnɑːnsi/ ə-NAHN-see) is inspired by the Akan folktale character known for his web-like connections, reflecting the grid's resemblance to a spider's web.

## Installation

### Prerequisites

- Node.js (v14 or higher recommended)
- npm (Node Package Manager)

### Steps

1. Clone the repository:
    ```bash
    git clone https://github.com/gomosdg/anansi.git
    ```

2. Navigate to the project directory:
    ```bash
    cd anansi
    ```

3. Install dependencies:
    ```bash
    npm install
    ```

## Running Tic Tac Toe

To start the Tic Tac Toe game, use the following command:
```bash
npm run tictactoe

Open your web browser and visit [http://localhost:8021](http://localhost:8021) to play the game.

## Configuration

The game uses the following default configuration:
- **Grid Height**: 3
- **Grid Width**: 3
- **Token Win Length**: 3

These parameters define the size of the grid and the number of consecutive tokens required to win. They can be adjusted directly in the code if needed. The configuration is specified within the `tictactoe.views` namespace and can be modified as follows:

- **`game-grid-height`**: Defines the number of rows in the grid.
- **`game-grid-width`**: Defines the number of columns in the grid.
- **`token-win-length`**: Defines the number of consecutive tokens required to win the game.

The relevant values are set in the `tictactoe.views` namespace:

```clojure
(def game-grid-height 3)
(def game-grid-width 3)
(def token-win-length 3)


You can adjust these parameters in the configuration file or directly in the code.

### Usage

Once the server is running, you can interact with the Tic Tac Toe game through your web browser. Follow these steps to play the game:

1. **Open Your Web Browser**: Navigate to [http://localhost:8021](http://localhost:8021).
2. **Play the Game**: The game will load with a 3x3 grid. Players will take turns placing their tokens (`X` or `O`) on the grid.
3. **Win or Draw**: The game checks for a win or draw after each move. A player wins if they align 3 of their tokens horizontally, vertically, or diagonally. If all cells are filled without a win, the game results in a draw.

### Code Overview

#### Anansi Grid

The core data structure used to model the grid is the Anansi grid. It provides the foundation for handling the game's logic and state. The grid is initialized as a 3x3 grid with each cell connected to its neighboring cells, creating a web-like structure that supports the game's mechanics.

#### Game Loop

The game loop is responsible for:
- **Updating the Game State**: It processes user interactions (i.e., placing tokens) and updates the game state accordingly.
- **Drawing the Graphics**: It uses PixiJS to render the grid and tokens, including updating the display to reflect moves and game outcomes.
- **Handling Human-Computer Interactions**: It listens for user clicks to place tokens and manages game progression.

#### Win Checking Algorithm

The win-checking algorithm utilizes breadth-first traversal to determine if a player has won. This approach effectively handles the cyclical nature of the grid and checks for the required token length (3 in this case). The algorithm works as follows:
1. **Initialization**: Starts with the cell where the last token was placed.
2. **Traversal**: Finds and enqueues neighboring cells that match the current token and have not been visited.
3. **Validation**: Checks if the number of consecutive tokens matches the required win length (3). If so, the player wins; otherwise, the game continues until a win or draw condition is met.


## Screenshots

...

## Contributing

Contributions are welcome! To contribute to the project:
1. Open an issue or submit a pull request on GitHub.
2. Follow the guidelines in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for more details.

## Contact

For questions or support, please contact [gomotso@mererotech.com](mailto:gomotso@mererotech.com).

