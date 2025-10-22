package chess;

import java.util.HashSet;


public class Rule {
    private final int repeats;
    private final int [][] coordinates;

    public Rule(int repeats, int [][] coordinates) {
        this.repeats = repeats;
        this.coordinates = coordinates;
    }
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition){
        //moves set
        HashSet<ChessMove> moves = new HashSet<>();
        //row and column
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        //piece and color
        ChessPiece myPiece = board.getPiece(new ChessPosition(myRow, myCol));
        ChessGame.TeamColor myColor = myPiece.getTeamColor();

        //is Pawn
        if (repeats == 2){
            int verticalMove = (myColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
            int startRow = (myColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
            int promoRow = (myColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

           // single forward
            int nextFRow = myRow + verticalMove;
            // double forward
            int doubleFRow = nextFRow + verticalMove;
           //isBound and isEmpty
           if (isBound(nextFRow, myCol) && isEmpty(board,nextFRow,myCol)){
               //promo
               if(nextFRow == promoRow){
                   promotions(moves, board, myPosition, new ChessPosition(nextFRow, myCol), true);
               }
               //double forward or single forward
               else{
                   moves.add(new ChessMove(myPosition, new ChessPosition(nextFRow, myCol), null));
                   //double forward
                   if (isEmpty(board,doubleFRow, myCol) && (myRow == startRow)){
                       moves.add(new ChessMove(myPosition, new ChessPosition(doubleFRow, myCol), null));
                   }
               }
            }
           //attack
           int attackRow = myRow + verticalMove;
           int attackColR = myCol + 1;
           int attackColL = myCol - 1;

           attack(board, moves, attackRow, attackColR, promoRow, myColor,myPosition);
           attack(board, moves, attackRow, attackColL, promoRow, myColor,myPosition);
        }
        //everything except pawn
        for (int [] coordinate: coordinates) {
            int rowMod = coordinate[0];
            int colMod = coordinate[1];
            int nextRow = myRow + rowMod;
            int nextCol = myCol + colMod;

            if (repeats == 0 || repeats == 1) {
                //king, knight
                if (repeats == 0) {
                    //isBound and isAvailable
                    if (isBound(nextRow, nextCol) && isAvailable(board, nextRow, nextCol, myColor)) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(nextRow, nextCol), null));
                    }
                    continue;
                }

                //Queen, Rook, Bishop
                while (isBound(nextRow, nextCol)) {
                    ChessPosition nextPosition = new ChessPosition(nextRow, nextCol);
                    //Empty
                    if (isEmpty(board, nextRow, nextCol)) {
                        moves.add(new ChessMove(myPosition, nextPosition, null));
                        nextRow = nextRow + rowMod;
                        nextCol = nextCol + colMod;
                        continue;
                    }
                    //Blocked
                    if (isEnemyRule(board, nextRow, nextCol, myColor)) {
                        moves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    //Friend
                    break;


                }
            }
        }
        return moves;
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
    public void promotions(HashSet<ChessMove> moves, ChessBoard board, ChessPosition myPosition, ChessPosition nextPosition, boolean promotion){
        if (promotion){
            moves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
        }
        else{
            moves.add(new ChessMove(myPosition, nextPosition,null));
        }
    }
    public void attack(ChessBoard board, HashSet<ChessMove> moves, int row, int col, int promoRow, ChessGame.TeamColor myColor, ChessPosition myPos){
        if (isBound(row,col)){
            if (isEnemyRule(board, row, col, myColor)) {
                promotions(moves, board, myPos, new ChessPosition(row,col), (row == promoRow));
            }
        }
    }
    public boolean isEnemyRule(ChessBoard board, int row, int col, ChessGame.TeamColor myColor){
        if (isBound(row,col)) {
            ChessPiece potentialPiece = board.getPiece(new ChessPosition(row, col));
            var cond1 = !isEmpty(board,row,col);
            var cond2 = potentialPiece.getTeamColor()!= myColor;
            return (cond1 && cond2);
        }
        return false;
    }
    public boolean isAvailable(ChessBoard board, int row, int col, ChessGame.TeamColor myColor){
        return (isEnemyRule(board, row, col, myColor)||isEmpty(board, row, col));
    }
}

