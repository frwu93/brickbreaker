package breakout;

import javafx.scene.Group;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import static breakout.Game.*;

public class BlockConfigurationReader {

    String directory = "blockfiles/";


    public void readInBlocks(int level, Block[][] gridOfBlocks) throws FileNotFoundException {
        String filePath = directory + "level" + level + ".txt";
        Scanner scanner = new Scanner(new File(filePath));
        int row = 0;
        while (scanner.hasNextLine()) {
            String[] blockLives = scanner.nextLine().split(" ");
            for(int i = 0; i < gridOfBlocks[row].length; i++){
                double xPos = i * (BLOCKWIDTH + GAP) + GAP;
                double yPos = row * (BLOCKHEIGHT + GAP) + GAP;
                gridOfBlocks[row][i] = new SingleHitBlock(xPos, yPos, BLOCKWIDTH, BLOCKHEIGHT, Integer.parseInt(blockLives[i]));
            }
            row++;
        }
    }

    public Block[][] loadLevel(Group root, int level) throws FileNotFoundException {
        Block[][] gridOfBlocks = new Block[getRowNum(level)][getColNum(level)];
        readInBlocks(level, gridOfBlocks);
        for (Block[] row : gridOfBlocks){
            for (Block block : row){
                root.getChildren().add(block);
            }
        }
        return gridOfBlocks;
    }

    public int getRowNum(int level) throws FileNotFoundException {
        String filePath = directory + "level" + level + ".txt";
        Scanner scanner = new Scanner(new File(filePath));
        int numOfRows = 0;
        while (scanner.hasNextLine()) {
            numOfRows++;
            scanner.nextLine();
        }
        return numOfRows;
    }

    public int getColNum(int level) throws FileNotFoundException {
        String filePath = directory + "level" + level + ".txt";
        Scanner scanner = new Scanner(new File(filePath));
        while (scanner.hasNextLine()) {
            String[] columns = scanner.nextLine().split(" ");
            return columns.length;
        }
        return -1;
    }
}

