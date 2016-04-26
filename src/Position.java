public class Position {
    private int xPos;
    private int yPos;
    private boolean canMoveAnyWay = true;

    public Position(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public Position(int xPos, int yPos, boolean canMoveAnyWay) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.canMoveAnyWay = canMoveAnyWay;
    }

    public boolean isCanMoveAnyWay() {
        return canMoveAnyWay;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int diff(Position p) {
        return Math.abs(this.xPos - p.getxPos());
    }

    public boolean equals(Object p) {
        return (p instanceof Position && ((Position) p).getxPos() == xPos && ((Position) p).getyPos() == yPos);
    }

    public int hashCode() {
        return xPos * 31 + yPos;
    }
}
