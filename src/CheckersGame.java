import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckersGame extends JPanel {
    private CheckersLogic gameModel = new CheckersLogic(8, false);
    private static final Color BLACK_CELL_COLOR = new Color(209, 139, 71);
    private static final Color WHITE_CELL_COLOR = new Color(255, 206,158);
    private static final int CELL_SIZE = 50;
    private static final int PIECE_SIZE = 46;
    private static final int PIECE_BORDER_SIZE = (CELL_SIZE - PIECE_SIZE) / 2;
    private boolean isDrawPossibleMoves = false;
    private Position selectedPos = null;

    private class BoardClickListener extends MouseAdapter {
        private final CheckersGame parent;

        private BoardClickListener(CheckersGame parent) {
            this.parent = parent;
        }

        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int xPiece = x / CELL_SIZE;
            int yPiece = y / CELL_SIZE;

            if (yPiece < 0 || yPiece >= gameModel.getBoardSize() || xPiece < 0 || xPiece >= gameModel.getBoardSize()) {
                return;
            }

            Position clickedPos = new Position(xPiece, yPiece);

            if (gameModel.canMoveToPos(selectedPos, clickedPos)) {
                gameModel.move(selectedPos, clickedPos);
                parent.isDrawPossibleMoves = false;
                selectedPos = null;
                parent.repaint();
            } else if (gameModel.needDrawPossibleMoves(clickedPos)) {
                parent.isDrawPossibleMoves = true;
                parent.selectedPos = clickedPos;
                parent.repaint();
            }
        }
    }

    public void initializeNewGame() {
        gameModel.initialize();
        repaint();
    }

    public CheckersGame() {
        gameModel.initialize();
        this.addMouseListener(new BoardClickListener(this));
    }

    private void drawBoard(Graphics g) {
        int boardSize = gameModel.getBoardSize();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if ((i + j) % 2 == 0) {
                    g.setColor(WHITE_CELL_COLOR);
                } else {
                    g.setColor(BLACK_CELL_COLOR);
                }
                g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawBoardCellByPos(Graphics g, int x, int y) {
        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private void drawPieces(Graphics g) {
        java.util.List<Piece> pieces = gameModel.getAllPieces();

        for (Piece piece: pieces) {
            if (piece.isQueen) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.BLACK);
            }
            int xPos = piece.getPosition().getxPos();
            int yPos = piece.getPosition().getyPos();

            g.fillOval(xPos * CELL_SIZE, yPos * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.setColor(piece.isWhite() ? Color.WHITE : Color.RED);
            g.fillOval(xPos * CELL_SIZE + PIECE_BORDER_SIZE, yPos * CELL_SIZE + PIECE_BORDER_SIZE, PIECE_SIZE, PIECE_SIZE);
        }
    }

    public void paint(Graphics g) {
        drawBoard(g);
        drawPieces(g);

        if (isDrawPossibleMoves) {
            drawPossibleMoves(g);
        }

        if (gameModel.isGameEnded()) {
            JOptionPane.showMessageDialog(null, "Game is Over. You" + (gameModel.isYouWin() ? "Win!" : "Lose("));
        }
        else if (gameModel.isRobotMove()) {
            gameModel.doRobotMove();
            repaint();
        }
    }

    private void drawPossibleMoves(Graphics g) {
        java.util.List<Position> possibleMoves = gameModel.getMovePositions(selectedPos, false);
        g.setColor(Color.GREEN);

        for (Position pos: possibleMoves) {
            drawBoardCellByPos(g, pos.getxPos(), pos.getyPos());
        }
    }

    private static class newGameActionListener implements ActionListener {
        private final CheckersGame game;

        public newGameActionListener(CheckersGame game) {
            this.game = game;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            game.initializeNewGame();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setLayout(null);
        frame.setTitle("Checkers");
        frame.setSize(600, 600);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBounds(450, 0, 100, 100);
        JButton newGameButton = new JButton("New Game");

        CheckersGame gamePanel = new CheckersGame();

        newGameButton.addActionListener(new newGameActionListener(gamePanel));

        gamePanel.setSize(400, 400);
        frame.getContentPane().add(gamePanel);
        buttonsPanel.add(newGameButton);

        frame.getContentPane().add(buttonsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
