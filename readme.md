# Anansi
[image](https://github.com/user-attachments/assets/2be9d9b2-1983-4327-8332-6776f641bd26)

A graph based datastructure aims to model gridlike things. While exploring graph algorithms and datastructures, I realised that some well known games could be modelled using graph data structures. With Minesweeper and Tic Tac Toe in mind, I began a quest to figure out how well could these fit! In this repository you will find two things: the gridlike datastructure that I have now named Anansi (/əˈnɑːnsi/ ə-NAHN-see; literally translates to spider) after the Akan folktale character. This is because the grid resembles a spider's web. Secondly, you will find a number of games that I aim to model with Anansi.

# Getting Started
The following section provides information on how you can run the games that are built on top of anansi. First you will need to run a server. You can do this in the following ways:

Run `npm run server` which will start a shadow-cljs server. Connect you repl and run `(do (shadow/watch :<game-of-choice>) (shadow/repl :<game-of-choice>)` where `<game-of-choice>` is the build name according to the shadow-cljs.edn file.

## Tic Tac Toe
The build name for tic tac toe is `tictactoe`. One you have ran the watch code for this build you can visit `localhost:8021` to open the tic tac toe game.

## Minesweeper
The build name for minesweeper is `minesweeper`. One you have ran the watch code for this build you can visit `localhost:8020` to open the minesweeper game.
