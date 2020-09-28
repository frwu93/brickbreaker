package breakout;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;


public class Game {
    private final GameLauncher gameLauncher;
    private final Ball ball;
    private final Paddle paddle;
    private final ScoreDisplay scoreDisplay;
    private final LivesDisplay livesDisplay;
    private GamePiece[][] gridOfGamePieces;
    private final LevelDisplay levelDisplay;
    private final HighScoreDisplay highScoreDisplay;
    private boolean pause = false;
    private int level;

    public Game(GameLauncher gameLauncher, LivesDisplay livesDisplay, ScoreDisplay scoreDisplay,
                LevelDisplay levelDisplay, Ball ball, Paddle paddle, GamePiece[][] gridOfGamePieces,
                HighScoreDisplay highScoreDisplay) {
        this.gameLauncher = gameLauncher;
        this.livesDisplay = livesDisplay;
        this.scoreDisplay = scoreDisplay;
        this.levelDisplay = levelDisplay;
        this.ball = ball;
        this.paddle = paddle;
        this.gridOfGamePieces = gridOfGamePieces;
        this.level = GameStatus.FIRST_LEVEL;
        this.highScoreDisplay = highScoreDisplay;
    }

    public void handleMouseInput(double x) {
        if (ball.getXVelocity() == 0 && ball.getYVelocity() == 0) {
            ball.setXVelocity(x - GameStatus.WINDOWWIDTH/2.0);
            ball.setYVelocity(-150);
        }
    }

    public void handleKeyInput(KeyCode code) {
        cheatKeys(code);
        switch (code) {
            case LEFT -> handleLeftPress();
            case RIGHT -> handleRightPress();
        }
    }

    public void step(double elapsedTime) {
        if (!pause) {
            updateShapes(elapsedTime);
        }
        checkGameStatus();
    }

    public void resetLevel()  {
        resetBallPaddle();
        clearLevel();
        gridOfGamePieces = gameLauncher.setUpLevel(level);
        scoreDisplay.resetDisplayValue();
    }

    public void resetBallPaddle(){
        ball.reset();
        paddle.reset();
    }

    public void clearLevel() {
        for (GamePiece[] rowOfGamePieces : gridOfGamePieces) {
            for (GamePiece gamePiece : rowOfGamePieces) {
                gamePiece.setLives(0);
                if (gamePiece instanceof Node) {
                    gameLauncher.removeFromRoot((Node) gamePiece);
                }
            }
        }
    }

    public void setPause() {
        pause = !pause;
    }

    public void scalePaddleSize(double factor) {
        paddle.setWidth(paddle.getWidth() * factor);
    }

    public void addLife() {
        livesDisplay.addLife();
    }

    public void scaleBallVelocity(double factor) {
        ball.setXVelocity(ball.getXVelocity() * factor);
        ball.setYVelocity(ball.getYVelocity() * factor);
    }

    private void updateShapes(double elapsedTime) {
        checkPaddleCollision();
        checkBorderCollision();
        checkGamePieceCollision();
        ball.updatePosition(elapsedTime);
    }

    private void checkPaddleCollision() {
        if (isIntersectingWithBall(paddle)) {
            ball.updateVelocityUponCollision(paddle);
        }
    }

    private void checkGamePieceCollision() {
        for (int i = 0; i < gridOfGamePieces.length; i++) {
            for (int j = 0; j < gridOfGamePieces[i].length; j++) {
                GamePiece gamePiece = gridOfGamePieces[i][j];
                if (gamePiece.getLives() > 0 && gamePiece instanceof Rectangle &&
                        isIntersectingWithBall((Rectangle) gamePiece)) {
                    ball.updateVelocityUponCollision((Rectangle) gamePiece);
                    updateGamePieceStatus(gamePiece, i, j);
                    scoreDisplay.increaseScore();
                    highScoreDisplay.updateHighScore(scoreDisplay.getScore());
                }
            }
        }
    }

    private void updateGamePieceStatus(GamePiece gamePiece, int i, int j) {
        gamePiece.updateStatus();
        if (gamePiece instanceof PowerUp) {
            ((PowerUp)gamePiece).updateGameStatus(this);
        }
//        ExpandPaddlePowerUp jfkd = new ExpandPaddlePowerUp(1, 2);
//        System.out.println(jfkd.getClass() == GameStatus.GAME_PIECES[0]);
        if (gamePiece.getLives() == 0 && gamePiece instanceof Node) {
            gameLauncher.removeFromRoot((Node) gamePiece);
            if (gamePiece instanceof Block) {
                generatePowerUp((Block)gamePiece, i, j);
            }
        }
    }

    private void generatePowerUp(Block deletedBlock, int i, int j) {
        if (Math.random() <= GameStatus.POWER_UP_PROBABILITY) {
            Random random = new Random();
            try {
                Class<? extends PowerUp> powerUpClass = GameStatus.POWERUPS.get(random.nextInt(GameStatus.POWERUPS.size()));
                Constructor<? extends PowerUp> powerUpConstructor = powerUpClass.getConstructor(double.class, double.class, double.class, double.class);
                PowerUp powerUp = powerUpConstructor.newInstance(deletedBlock.getX(), deletedBlock.getY(), deletedBlock.getWidth(), deletedBlock.getHeight());
                gameLauncher.addToRoot(powerUp);
                gridOfGamePieces[i][j] = powerUp;
            } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    private void checkBorderCollision() {
        if (ball.getLeft() <= 0 || ball.getRight() >= GameStatus.WINDOWWIDTH) {
            ball.updateXVelocityUponBorderCollision();
        } else if (ball.getTop() <= GameStatus.DISPLAYHEIGHT) {
            ball.updateYVelocityUponBorderCollision();
        } else if (ball.getTop() > GameStatus.WINDOWHEIGHT) { // goes below the screen
            resetBallPaddle();
            livesDisplay.subtractLife();
            scoreDisplay.resetBonus();
        }
    }

    private boolean isIntersectingWithBall(Rectangle gamePiece) {
        return gamePiece.getBoundsInParent().intersects(ball.getBoundsInParent());
    }

    private void checkGameStatus() {
        Text gameMessage = new Text(200, 300, "");
        if (hasWon()) {
            gameMessage.setText("You Passed This Level!");
            gameMessage.setId("winMessage");
            loadNextLevel();
        } else if (hasLost()) {
            gameMessage.setText("You Ran Out Of Lives! You lost!");
            gameMessage.setId("lossMessage");
            pause = true;
            gameLauncher.addToRoot(gameMessage);
        }
    }

    private boolean hasWon(){
        for (GamePiece[] rowOfGamePieces : gridOfGamePieces){
            for (GamePiece gamePiece : rowOfGamePieces){
                if (gamePiece.getLives() != 0){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasLost(){
        return livesDisplay.getLives() == 0;
    }

    private void handleLeftPress() {
        if (!pause && paddle.getX() >= GameStatus.PADDLEDELTA) {
            paddle.setX(paddle.getX() - GameStatus.PADDLEDELTA);
            if (ball.notInMotion()) {
                ball.setCenterX(ball.getCenterX() - GameStatus.PADDLEDELTA);
            }
        }
    }

    private void handleRightPress() {
        if (!pause && paddle.getX() + paddle.getWidth() <= GameStatus.WINDOWWIDTH - GameStatus.PADDLEDELTA) {
            paddle.setX(paddle.getX() + GameStatus.PADDLEDELTA);
            if (ball.notInMotion()) {
                ball.setCenterX(ball.getCenterX() + GameStatus.PADDLEDELTA);
            }
        }
    }

    private void cheatKeys(KeyCode code) {
        switch (code) {
            case R -> resetLevel();
            case SPACE -> setPause();
            case L -> addLife();
//            case P -> ;
            case C -> clearLevel();
            case D -> clearFirstBlock();
            case S -> highScoreDisplay.clearHighScore();
            case F -> allBlocksLoseLife();
            case DIGIT1 -> jumpToLevel(1);
            case DIGIT2 -> jumpToLevel(2);
            case DIGIT3 -> jumpToLevel(3);
            case DIGIT4 -> jumpToLevel(4);
            case DIGIT5 -> jumpToLevel(5);

        }
    }

    private void clearFirstBlock(){
        GamePiece blockToRemove = getFirstBlock();
        blockToRemove.setLives(0);
        if (blockToRemove instanceof Node) {
            gameLauncher.removeFromRoot((Node)blockToRemove);
        }
    }

    private void allBlocksLoseLife(){
        for (int i = 0; i < gridOfGamePieces.length; i++){
            for (int j = 0; j < gridOfGamePieces[0].length; j++){
                if (gridOfGamePieces[i][j].getLives() > 0){
                    updateGamePieceStatus(gridOfGamePieces[i][j], i, j);
                }
            }
        }
    }

    private void jumpToLevel(int level) {
        clearLevel();
        gridOfGamePieces = gameLauncher.setUpLevel(level);
        levelDisplay.setLevel(level);
    }

    private GamePiece getFirstBlock(){
        for (int i = gridOfGamePieces.length - 1; i >= 0; i--){
            for (int j = 0; j <gridOfGamePieces[0].length; j++){
                if (gridOfGamePieces[i][j].getLives() > 0){
                    return gridOfGamePieces[i][j];
                }
            }
        }
        return null;
    }

    private void loadNextLevel(){
        BlockConfigurationReader blockReader = new BlockConfigurationReader();
        int maxLevel = blockReader.getFileCount();
        if (level >= maxLevel){
            Text gameMessage = new Text(150, 300, "WOWOWOW!!! YOU BEAT THE WHOLE GAME");
            gameLauncher.addToRoot(gameMessage);
            resetBallPaddle();
            return;
        }
        clearLevel();
        level += 1;
        gridOfGamePieces = gameLauncher.setUpLevel(level);
        scoreDisplay.setCheckPointScore();
        levelDisplay.incrementLevel();
        resetBallPaddle();
    }
}
