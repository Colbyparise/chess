package chess;

import java.util.Objects;

    public class ChessMove {

        private final ChessPosition origin;
        private final ChessPosition destination;
        private final ChessPiece.PieceType promotedPiece;

        public ChessMove(ChessPosition from, ChessPosition to, ChessPiece.PieceType promoteTo) {
            this.origin = from;
            this.destination = to;
            this.promotedPiece = promoteTo;
        }

        public ChessPosition getStartPosition() {
            return origin;
        }

        public ChessPosition getEndPosition() {
            return destination;
        }

        public ChessPiece.PieceType getPromotionPiece() {
            return promotedPiece;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ChessMove move)) return false;
            return Objects.equals(origin, move.origin) &&
                    Objects.equals(destination, move.destination) &&
                    promotedPiece == move.promotedPiece;
        }

        @Override
        public int hashCode() {
            return Objects.hash(origin, destination, promotedPiece);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(origin).append(" -> ").append(destination);
            if (promotedPiece != null) {
                sb.append(", promotes to ").append(promotedPiece);
            }
            return sb.toString();
        }
    }