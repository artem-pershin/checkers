import java.io.*;
import java.util.*;

public class CheckersLogic {
    private final int boardSize;
    private CELL_STATE[][] gameboard;
    private boolean isWhiteTurn;
    private final boolean isRobotWhite;
    private int[] delta = { +1, -1 };
    private int[] deltaEat = { +2, -2 };
    private boolean selectedNeedEating = false;

    private enum CELL_STATE {
        WHITE_PIECE, WHITE_QUEEN_PIECE, BLACK_PIECE, BLACK_QUEEN_PIECE, UNOCCUPIED;

        public CELL_STATE advanceToQueen() {
            return this == WHITE_PIECE ? WHITE_QUEEN_PIECE : (this == BLACK_PIECE ? BLACK_QUEEN_PIECE : this);
        }

        public boolean isQueen() {
            return this == WHITE_QUEEN_PIECE || this == BLACK_QUEEN_PIECE;
        }

        public boolean isWhite() {
            return this == WHITE_PIECE || this == WHITE_QUEEN_PIECE;
        }

        public boolean isBlack() {
            return this == BLACK_PIECE || this == BLACK_QUEEN_PIECE;
        }
    }

    private boolean checkValidPos(int x, int y) {
        return y >= 0 && y < boardSize && x >= 0 && x < boardSize;
    }

    boolean canEat(int eatXPos,int eatYPos, int newXPos, int newYPos, boolean isEaterWhite) {
        return  checkValidPos(eatXPos, eatYPos) &&
                gameboard[eatYPos][eatXPos] != CELL_STATE.UNOCCUPIED &&
                ((isEaterWhite && gameboard[eatYPos][eatXPos].isBlack()) || (!isEaterWhite && gameboard[eatYPos][eatXPos].isWhite())) &&
                checkValidPos(newXPos, newYPos) && gameboard[newYPos][newXPos] == CELL_STATE.UNOCCUPIED;
    }

    private boolean getEatPath(Position from, Position to, boolean isEaterWhite, List<Position> path, List<Position> eaten) {
        if (from.equals(to)) {
            // get it
            return true;
        }

        int posx = from.getxPos();
        int posy = from.getyPos();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int eatXPos = posx + delta[i];
                int eatYPos = posy + delta[j];

                int newXPos = posx + deltaEat[i];
                int newYPos = posy + deltaEat[j];

                if (canEat(eatXPos, eatYPos, newXPos, newYPos, isEaterWhite)) {
                    Position eatPos = new Position(newXPos, newYPos);
                    if (!path.contains(eatPos)) {
                        path.add(eatPos);
                        eaten.add(new Position(eatXPos, eatYPos));
                        if (!getEatPath(eatPos, to, isEaterWhite, path, eaten)) {
                            path.remove(eaten.size() - 1);
                            eaten.remove(eaten.size() - 1);
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void getEatPositions(Position position, boolean isEaterWhite, List<Position> result, List<Position> eated) {
        int posx = position.getxPos();
        int posy = position.getyPos();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int eatXPos = posx + delta[i];
                int eatYPos = posy + delta[j];

                int newXPos = posx + deltaEat[i];
                int newYPos = posy + deltaEat[j];

                if (canEat(eatXPos, eatYPos, newXPos, newYPos, isEaterWhite)) {
                        Position eatPos = new Position(newXPos, newYPos);
                        if (!result.contains(eatPos)) {
                            result.add(eatPos);
                            eated.add(new Position(eatXPos, eatYPos));
                            getEatPositions(eatPos, isEaterWhite, result, eated);
                        }
                }
            }
        }
    }

    public List<Position> getMovePositions(Position position, boolean justInfoQuery) {
        int x = position.getxPos();
        int y = position.getyPos();

        if (gameboard[y][x] == CELL_STATE.UNOCCUPIED ||
           (gameboard[y][x].isWhite() && !isWhiteTurn) ||
           (gameboard[y][x].isBlack() && isWhiteTurn)) {
            return null;
        }

        if (gameboard[y][x].isQueen()) {
            return getQueenMovePositions(position);
        }

        List<Position> result = new LinkedList<>();

        List<Position> eatPathPositions = new LinkedList<>();
        List<Position> eatenPieces = new LinkedList<>();

        getEatPositions(position, gameboard[y][x].isWhite(), eatPathPositions, eatenPieces);
        result.addAll(eatPathPositions);

        if (!eatPathPositions.isEmpty()) {
            if (!justInfoQuery) {
                selectedNeedEating = true;
            }
            return result;
        }

        int deltaY = gameboard[y][x].isWhite() ? -1 : +1;

        for (int deltaX: delta) {
            int possibleXPos = x + deltaX;
            int possibleYPos = y + deltaY;
            if (checkValidPos(possibleXPos, possibleYPos) && gameboard[possibleYPos][possibleXPos] == CELL_STATE.UNOCCUPIED) {
                result.add(new Position(possibleXPos, possibleYPos));
            }
        }

        if (!justInfoQuery) {
            selectedNeedEating = false;
        }

        return result;
    }

    public boolean isYouWin() {
        return !isWhiteTurn;
    }

    private List<Position> getQueenMovePositions(Position position) {
        Set<Position> visitedCells = new HashSet<>();
        Stack<Position> cellsToVisit = new Stack<>();
        // starting with one cell
        cellsToVisit.push(position);

        while (!cellsToVisit.isEmpty()) {
            Position curPos = cellsToVisit.pop();

            int posx = curPos.getxPos();
            int posy = curPos.getyPos();
            // first trying to do simple moves
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    if (i != j && !curPos.isCanMoveAnyWay()) continue;

                    int newXPos = posx + delta[i];
                    int newYPos = posy + delta[j];
                    Position newPos = new Position(newXPos, newYPos, false);

                    if (!visitedCells.contains(newPos) &&
                       (checkValidPos(newXPos, newYPos) && gameboard[newYPos][newXPos] == CELL_STATE.UNOCCUPIED)) {
                       cellsToVisit.push(newPos);
                    }
                }
            }

            //then trying to eat some pieces
            boolean isEaterWhite = gameboard[position.getyPos()][position.getxPos()].isWhite();

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    if (i != j && !curPos.isCanMoveAnyWay()) continue;
                    int eatXPos = posx + delta[i];
                    int eatYPos = posy + delta[j];

                    int newXPos = posx + deltaEat[i];
                    int newYPos = posy + deltaEat[j];

                    Position newPos = new Position(newXPos, newYPos, false);

                    if (!visitedCells.contains(newPos) && canEat(eatXPos, eatYPos, newXPos, newYPos, isEaterWhite)) {
                        cellsToVisit.push(newPos);
                    }
                }
            }

            visitedCells.add(curPos);
        }

        visitedCells.remove(position);
        return new ArrayList<>(visitedCells);
    }

    public boolean isRobotMove() {
        return (isWhiteTurn && isRobotWhite) || (!isWhiteTurn && !isRobotWhite);
    }

    private Position getRandomPos(List<Position> p) {
        return p.get(new Random().nextInt(p.size()));
    }

    public void doRobotMove() {
        List<Position> piecesToMove = new ArrayList<>();
        HashMap<Position, List<Position>> movesByPosition = new HashMap<>();
        boolean isAnyEatMove = false;

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameboard[i][j] == CELL_STATE.UNOCCUPIED) continue;

                if (gameboard[i][j].isBlack()) {
                    List<Position> possibleMoves = getMovePositions(new Position(j, i), true);

                    if (!possibleMoves.isEmpty()) {
                        Position currentPos = new Position(j, i);
                        piecesToMove.add(currentPos);
                        movesByPosition.put(currentPos, possibleMoves);
                        isAnyEatMove |= containsEatMove(currentPos, possibleMoves);
                    }
                }
            }
        }

        while (true) {
            Position startPos = getRandomPos(piecesToMove);
            List<Position> possibleMoves = getMovePositions(startPos, false);
            Position finishPos = getRandomPos(possibleMoves);

            if (isAnyEatMove && !containsEatMove(startPos, possibleMoves)) {
                continue;
            } else {
                move(startPos, finishPos);
                break;
            }
        }
    }

    private boolean containsEatMove(Position startPos, List<Position> possibleMoves) {
        for (Position p: possibleMoves) {
            if (startPos.diff(p) >= 2) {
                return true;
            }
        }

        return false;
    }

    public boolean isGameEnded() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameboard[i][j] == CELL_STATE.UNOCCUPIED) continue;

                List<Position> possibleMoves = getMovePositions(new Position(j, i), true);
                if ((isWhiteTurn && gameboard[i][j].isWhite() ||
                    !isWhiteTurn && gameboard[i][j].isBlack()) && !possibleMoves.isEmpty())
                    return false;
            }
        }

        return true;
    }

    public boolean needDrawPossibleMoves(Position position) {
        return  (isWhiteTurn && gameboard[position.getyPos()][position.getxPos()].isWhite()) ||
                (!isWhiteTurn && gameboard[position.getyPos()][position.getxPos()].isBlack());
    }

    public boolean canMoveToPos(Position selectedPos, Position clickedPos) {
        // nothing selected, nothing to move
        if (selectedPos == null) return false;

        List<Position> validPos = getMovePositions(selectedPos, false);

        return validPos.contains(clickedPos);
    }

    public void eat(Position selectedPos, Position finalPos) {
        if (!selectedNeedEating) return;

        List<Position> eatPositions = new LinkedList<>();
        List<Position> piecesToEat = new LinkedList<>();

        getEatPath(selectedPos, finalPos, gameboard[selectedPos.getyPos()][selectedPos.getxPos()].isWhite(), eatPositions, piecesToEat);

        for (Position posToEat: piecesToEat) {
            gameboard[posToEat.getyPos()][posToEat.getxPos()] = CELL_STATE.UNOCCUPIED;
        }
    }

    public void move(Position selectedPos, Position clickedPos) {
        if (selectedNeedEating) {
            eat(selectedPos, clickedPos);
        }

        if ((clickedPos.getyPos() == (boardSize - 1) && !isWhiteTurn) ||
            (clickedPos.getyPos() == 0 && isWhiteTurn)) {
            //becomes a queen
            gameboard[clickedPos.getyPos()][clickedPos.getxPos()] = gameboard[selectedPos.getyPos()][selectedPos.getxPos()].advanceToQueen();
        } else {
            gameboard[clickedPos.getyPos()][clickedPos.getxPos()] = gameboard[selectedPos.getyPos()][selectedPos.getxPos()];
        }

        gameboard[selectedPos.getyPos()][selectedPos.getxPos()] = CELL_STATE.UNOCCUPIED;
        isWhiteTurn = !isWhiteTurn;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public CheckersLogic(int boardSize, boolean isRobotWhite) {
        this.boardSize = boardSize;
        gameboard = new CELL_STATE[boardSize][boardSize];
        isWhiteTurn = true;
        this.isRobotWhite = isRobotWhite;
    }

    private void putPiecesOnBoard(String line, boolean isWhite) {
        String[] allPiecesCoords = line.split(" ");

        for (String pieceCoord: allPiecesCoords) {
            int y = Integer.parseInt(pieceCoord.split(",")[0]);
            int x = Integer.parseInt(pieceCoord.split(",")[1]);

            gameboard[y][x] = isWhite ? CELL_STATE.WHITE_PIECE : CELL_STATE.BLACK_PIECE;
        }
    }

    private void loadFromFile(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        try {
            String whites = br.readLine();
            String blacks = br.readLine();

            putPiecesOnBoard(whites, true);
            putPiecesOnBoard(blacks, false);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                gameboard[i][j] = CELL_STATE.UNOCCUPIED;
            }
        }

        loadFromFile(new File("initial.txt"));
    }

    public List<Piece> getAllPieces() {
        List<Piece> result = new LinkedList<>();

        for (int i = 0; i < boardSize ; i++) {
            for (int j = 0; j < boardSize ; j++) {
                if (gameboard[i][j] != CELL_STATE.UNOCCUPIED) {
                    Piece piece = new Piece();
                    piece.setPosition(new Position(j, i));
                    piece.setWhite(gameboard[i][j].isWhite());
                    piece.setQueen(gameboard[i][j].isQueen());
                    result.add(piece);
                }
            }
        }

        return result;
    }
}
