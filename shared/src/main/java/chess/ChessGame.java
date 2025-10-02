package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }


    /**
     * @return Which team's turn it is
     */
    //getter
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    //setter
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece startPiece = board.getPiece(startPosition);
        //no piece at position
        if (startPiece == null){
            Collection<ChessMove> emptyMoves = new HashSet<>();
            return emptyMoves;
        }
        Collection<ChessMove> moves = startPiece.pieceMoves(board, startPosition);
        ChessGame.TeamColor colorPiece = startPiece.getTeamColor();
        ChessPosition kingPosition = findKing(colorPiece);
        alternateBoards(moves, startPosition, startPiece, colorPiece);

        return moves;
    }
    public void alternateBoards(Collection<ChessMove> moves, ChessPosition startPosition, ChessPiece startPiece, TeamColor colorPiece){
        ChessBoard realBoard = this.board;
        ChessBoard copyBoard = new ChessBoard(realBoard);
        //alternate boards
        Iterator<ChessMove> iterate = moves.iterator();
        while (iterate.hasNext()) {
            ChessMove move = iterate.next();
            ChessBoard hypeBoard = new ChessBoard(copyBoard);
            this.board = hypeBoard;
            potentialPieceMov(this.board, move, startPosition, startPiece);
            //if isInCheck remove move
            if (isInCheck(colorPiece)) {
                iterate.remove();
            }
            this.board = realBoard;
            //board = originalBoard;
        }
    }



    public void potentialPieceMov(ChessBoard board, ChessMove move, ChessPosition startPosition, ChessPiece startPiece){
        //remove piece
        board.addPiece(startPosition, null);
        //piece different location
        ChessPosition endPosition = move.getEndPosition();
        board.addPiece(endPosition, startPiece);
    }

    ChessPosition positionCheckEnemy(ChessPosition positionKing, TeamColor myColor){
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (isEnemy(board, row, col, myColor)) {
                    ChessPosition potentialPosition = new ChessPosition(row, col);
                    ChessPiece potentialPiece = board.getPiece(potentialPosition);
                    Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
                    for (ChessMove move : moves) {
                        ChessPosition potentialEndPosition = move.getEndPosition();
                        if (potentialEndPosition.equals(positionKing)) {
                            return potentialPosition;
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("Couldn't find position of enemy that put king in check ");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        int row = startPosition.getRow();
        int col = startPosition.getColumn();
        if (isEmpty(board, row, col)) {
            throw new InvalidMoveException("Illegal empty piece");
        }
        ChessPiece startPiece = board.getPiece(startPosition);
        TeamColor startColor = startPiece.getTeamColor();
        //correct turn?
        if (startColor.equals(getTeamTurn())){
            //legal move?

            ChessPosition potentialPosition = new ChessPosition(row, col);
            ChessPiece potentialPiece = board.getPiece(potentialPosition);
            Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
            boolean inMoves = false;
            for (ChessMove eachMove : moves){
                if (eachMove.equals(move)){
                    inMoves = true;
                }
            }
            if (!inMoves){
                throw new InvalidMoveException("Illegal move");
            }
        }
        throw new InvalidMoveException("Illegal turn");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition positionKing = findKing(teamColor);
        return doesEnemyCheck(positionKing, teamColor);

    }
    public ChessPosition findKing(TeamColor teamColor){
        //loop through board
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition potentialPosition = new ChessPosition(row, col);
                if (isKing(potentialPosition, teamColor)){
                    return potentialPosition;
                }
            }
        }
        throw new IllegalArgumentException("Couldn't find the king piece");
    }
    public boolean isKing(ChessPosition potentialPosition, TeamColor myColor){
        int potentialRow = potentialPosition.getRow();
        int potentialCol = potentialPosition.getColumn();
        //is friend
        if (!isEmpty(board, potentialRow, potentialCol) && !isEnemy(board, potentialRow, potentialCol, myColor)){
            //is King
            ChessPiece potentialPiece = board.getPiece(new ChessPosition(potentialRow, potentialCol));
            ChessPiece.PieceType potentialType = potentialPiece.getPieceType();
            if (potentialType == ChessPiece.PieceType.KING){
                return true;
            }
        }
        return false;
    }

    public boolean doesEnemyCheck(ChessPosition positionKing, TeamColor myColor){
        //loop board
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                //isEnemy
                if (isEnemy(board, row, col, myColor)) {
                    ChessPosition potentialPosition = new ChessPosition(row, col);
                    ChessPiece potentialPiece = board.getPiece(potentialPosition);
                    Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
                    //Check enemy moves
                    for (ChessMove move : moves) {
                        ChessPosition potentialEndPosition = move.getEndPosition();
                        if (potentialEndPosition.equals(positionKing)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isBound(int row, int col){
        return (row <= 8 && row >=1 && col <=8 && col >= 1);
    }
    public boolean isEmpty(ChessBoard board, int row, int col){
        if (isBound(row, col)) {
            ChessPiece potentialPiece = board.getPiece(new ChessPosition(row, col));
            return (potentialPiece == null);
        }
        return false;
    }
    public boolean isEnemy(ChessBoard board, int row, int col, ChessGame.TeamColor myColor){
        if (isBound(row,col)) {
            ChessPiece potentialPiece = board.getPiece(new ChessPosition(row, col));
            return (!isEmpty(board,row,col) && potentialPiece.getTeamColor() != myColor);
        }
        return false;
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition positionKing = findKing(teamColor);
        ChessPiece pieceKing = board.getPiece(positionKing);
        Collection<ChessMove> moves = pieceKing.pieceMoves(board, positionKing);
        if (surroundingCheck(moves, positionKing, pieceKing, teamColor)){
            if (isInCheck(teamColor)){
                //saved by other pieces?
                //loop board
                for (int row = 1; row <= 8; row++) {
                    for (int col = 1; col <= 8; col++) {
                        //isFriend
                        if (!isEmpty(board, row, col) && !isEnemy(board, row, col, teamColor)) {
                            moves = validMoves(new ChessPosition(row, col));
                            if (!moves.isEmpty()){
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition positionKing = findKing(teamColor);
        ChessPiece pieceKing = board.getPiece(positionKing);
        Collection<ChessMove> moves = pieceKing.pieceMoves(board, positionKing);
        if (!isInCheck(teamColor)) {
            return surroundingCheck(moves, positionKing, pieceKing, teamColor);
        }
        return false;
    }
    public boolean surroundingCheck(Collection<ChessMove> moves, ChessPosition positionKing, ChessPiece pieceKing, TeamColor teamColor){
        alternateBoards(moves, positionKing, pieceKing, teamColor);
        if (moves.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }
}
