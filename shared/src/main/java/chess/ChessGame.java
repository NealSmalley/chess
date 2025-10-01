package chess;

import java.util.Collection;
import java.util.HashSet;
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
        //get potential moves
        Collection<ChessMove> moves = startPiece.pieceMoves(board, startPosition);
        //piece color
        ChessGame.TeamColor colorPiece = startPiece.getTeamColor();
        //doesn't endanger the king
        if (!isInCheck(colorPiece)) {
            return moves;
        }
        //endangers the king

        return new HashSet<>();
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
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
        for (int row = 1; row < 8; row++){
            for (int col = 1; col < 8; col++){
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
        //loop
        for (int row = 1; row < 8; row++) {
            for (int col = 1; col < 8; col++) {
                if (isEnemy(board, row, col, myColor)) {
                    ChessPosition potentialPosition = new ChessPosition(row, col);
                    ChessPiece potentialPiece = board.getPiece(potentialPosition);
                    Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
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

    public interface customFinding{
        ChessPosition finding(int rowKing, int colKing, int row, int col);
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
