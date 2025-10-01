package chess;

public interface MovementRule {
    java.util.Collection<ChessMove> getMove(ChessBoard board, ChessPosition position);
}
