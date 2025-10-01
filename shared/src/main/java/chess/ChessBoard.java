package chess;

//This is to import the PieceType enum
import java.util.Arrays;
import java.util.Objects;
import static chess.ChessPiece.PieceType.*;
//import static chess.ChessGame.TeamColor.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;
    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    //copy constructor
    public ChessBoard(ChessBoard other){
        this.board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }
    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //create an empty board
        board = new ChessPiece[8][8];
        ChessPiece.PieceType [] pieces = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
        int row;
        int pawnRow;
        //color loop
        for (ChessGame.TeamColor color : ChessGame.TeamColor.values()) {
            int col = 0;
            //White
            if (ChessGame.TeamColor.WHITE == color){
                row = 1;
                pawnRow = 2;
            }
            //Black
            else {
                row = 8;
                pawnRow = 7;
            }
            //piece loop
            for (ChessPiece.PieceType piece: pieces){
                col++;
                addPiece(new ChessPosition(row, col), new ChessPiece(color, piece));
                addPiece(new ChessPosition(pawnRow, col), new ChessPiece(color, PAWN));
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
