public class Piece {
    boolean isQueen;
    Position position;
    boolean isWhite;

    public boolean isQueen() {
        return isQueen;
    }

    public void setQueen(boolean queen) {
        isQueen = queen;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }
}
