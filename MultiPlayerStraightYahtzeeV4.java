/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;
import java.util.Arrays;

public class MultiPlayerStraightYahtzeeV4 extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
	while(nPlayers>MAX_PLAYERS || nPlayers<0) {
			nPlayers=dialog.readInt("That's the wrong number. Enter number of players");
		}
	
	playerNames = new String[nPlayers];
	for (int i = 1; i <= nPlayers; i++) {
		String n = dialog.readLine("Enter name for player " + i);
		while(n.length()>11 ||n.length()==0) {
			n = dialog.readLine("Please enter a name between 1 and 11 characters for player " + i);
		}
		playerNames[i-1]=n;
	}
	display = new YahtzeeDisplay(getGCanvas(), playerNames);
	int[][]scores= new int[nPlayers+1][N_CATEGORIES];
	playGame(scores);
}

private void playGame(int[][] scores) {
	int sentinel=-1;
	for(int player=1; player<=nPlayers;player++) {
		for (int i=0; i<N_CATEGORIES;i++) {
			scores[player][i]=sentinel;
		}
	}
	for(int i=0;i<N_SCORING_CATEGORIES; i++) {
		for(int player=1; player<=nPlayers;player++){
			String name=playerNames[player-1];
			display.printMessage(name+", your turn to ROLL DICE.");
			int[] roll=rollDice(name);
			int point=0;
			display.printMessage("Pick a category, "+name);
			int category = display.waitForPlayerToSelectCategory();
			while(scores[player][category]!=sentinel) {
				display.printMessage("You've used that category, "+name+". Choose another.");
				category = display.waitForPlayerToSelectCategory();
			}
			if(salthouseCheckCategory(roll, category)==true) {
				switch(category) {
				case ONES: point=sumOfInstances(ONES, roll);
				break;
				case TWOS: point=sumOfInstances(TWOS, roll);
				break;
				case THREES: point=sumOfInstances(THREES, roll);
				break;
				case FOURS: point=sumOfInstances(FOURS, roll);
				break;
				case FIVES: point=sumOfInstances(FIVES, roll);
				break;
				case SIXES: point=sumOfInstances(SIXES, roll);
				break;
				case THREE_OF_A_KIND: point=addUpRolls(roll);
				break;
				case FOUR_OF_A_KIND: point=addUpRolls(roll);
				break;
				case FULL_HOUSE: point=25;
				break;
				case SMALL_STRAIGHT: point=30;
				break;
				case LARGE_STRAIGHT: point=40;
				break;
				case YAHTZEE: point=50;
				break;
				default: point=0;
				}
				if(category==CHANCE) {
					point=addUpRolls(roll);
					}
				}
		updateAllScores(scores, category, player, point);
		}
	}
	//sum up scores from upper categories
	int[] finalScores = new int[nPlayers+1];
	for(int player=1; player<=nPlayers; player++) {
		sumUpRange(scores, UPPER_SCORE, player, ONES, UPPER_SCORE);
		int sumOfUpperScores=scores[player][UPPER_SCORE];
		int valueOfUpperBonus=0;
		if(sumOfUpperScores>63){
			valueOfUpperBonus=35;
		}
		updateAllScores(scores,UPPER_BONUS, player,valueOfUpperBonus);
		sumUpRange(scores, LOWER_SCORE, player, THREE_OF_A_KIND, LOWER_SCORE);
		int finalTally=sumOfUpperScores+valueOfUpperBonus+scores[player][LOWER_SCORE];
		finalScores[player]=finalTally;
		display.updateScorecard(TOTAL, player,finalTally);
	}
	int highScore = getHighest(finalScores);
	if(nPlayers==1|| isTie(finalScores)) {
	display.printMessage("Nice Game. The highest Score was "+highScore);
	}else {
		String winner=getWinner(playerNames,finalScores, highScore);
		display.printMessage(winner+" is the winner, with a score of "+highScore);
	}
	
}
private String getWinner(String[]names,int[]scores, int highScore) {
	int i = getIndex(scores, highScore);
	return names[i-1];
}
private int getIndex(int[] array, int value) {
	int i=0;
	int result = 0;
    while(i<array.length) { 
         if(array[i] == value) {
             result=i;}
         i++;
         }
    return result;
}
private int getHighest(int[] array) {
	int highest = array[0];
	for (int i = 0; i < array.length; i++) {
		if (array[i] > highest) {
	    		highest = array[i];
			}
	    }
	return highest;
	}
private boolean isTie(int[]array) {
	boolean p = false;
	int highest = -2;
	for (int i = 0; i < array.length; i++) {
		if(array[i]==highest) {
			p=true;
		}
		if (array[i] > highest) {
	    		highest = array[i];
			}
	    }
	return p;
}
private boolean salthouseCheckCategory(int[] roll, int category) {
	switch(category) {
	case ONES: return true;
	case TWOS: return true;
	case THREES: return true;
	case FOURS: return true;
	case FIVES: return true;
	case SIXES: return true;
	case THREE_OF_A_KIND: return isXOfAKind(roll, THREES);
	case FOUR_OF_A_KIND: return isXOfAKind(roll, FOURS);
	case FULL_HOUSE: return isFullHouse(roll);
	case SMALL_STRAIGHT: return isSmallStraight(roll);
	case LARGE_STRAIGHT: return isConsecutive(roll);
	case YAHTZEE: return isXOfAKind(roll, N_DICE);
	case CHANCE: return true;
	default: return false;
	}
	
}

private boolean isSmallStraight(int[] roll) {
	if(isConsecutive(roll)==true) {
		return true;
	}
	if (isXOfAKind(roll,3)==true) {
		return false;
	}
	if (isXOfAKind(roll,4)==true) {
		return false;
	}
	if (isXOfAKind(roll,5)==true) {
		return false;
	}
	//declare array upperFour, that takes pos 1-4 from roll
	int[] upperFour = new int[4];
	int[] lowerFour = new int[4];
	System.arraycopy(roll, 1, upperFour, 0, 4);
	System.arraycopy(roll, 0, lowerFour, 0, 4);
	//declare array lower Four that takes pos 0-3 from roll
	if( isConsecutive(upperFour)==true || isConsecutive(lowerFour)==true ) {
		return true;
	}else {
		return false;
	}
}

private boolean isConsecutive(int[]roll) {
	boolean p = true;
	Arrays.sort(roll);
	for (int i=1; i<roll.length; i++) {
		if (roll[i]-roll[i-1]!=1) {
			return false;
		}
	}
	return p;
}

private boolean isFullHouse(int[] roll) {
	boolean p=false;
	if(isXOfAKind(roll, TWOS) && isXOfAKind(roll,THREES)) {
		p=true;
	}
	if(isXOfAKind(roll,N_DICE)) {
		p=false;
	}
	return p;
}
private boolean isXOfAKind(int[]roll, int count) {
	boolean p = false;
	int j=0;
	for (int i=1; i<=6;i++) {
		if(sumOfInstances(i,roll)>=i*count) {
			j++;
		}
	}
	if(j>0) {
		p = true;
		display.printMessage("That's three of a kind!");
	}
	return p;
}
private int sumOfInstances(int category, int[] roll) {
	int total=0;
	for(int i=0; i<N_DICE; i++) {
		if(roll[i]==category){
			total+=category;
		}
	}
	return total;
}

private void sumUpRange(int[][] scores,int category, int player, int startRange, int StopRange) {
	int[] range = new int[StopRange-startRange];
	for(int i = startRange; i<StopRange; i++) {
		range[i-startRange]=scores[player][i];
	}
	int sum=addUpRolls(range);
	updateAllScores(scores, category,player, sum);
}

private void updateAllScores(int[][] scores, int category, int player, int score) {
	scores[player][category]=score;
	display.updateScorecard(category, player, score);
}

private int addUpRolls(int[] roll) {
	int sum=0;
	for(int i=0;i<roll.length;i++) {
		sum=sum+roll[i];
	}
	return sum;
}

private int rollCall() {
	int  num = rgen.nextInt(6) + 1;
	return num;
}

private int[] rollDice(String name) {
	int[] dice = new int[N_DICE];
	display.waitForPlayerToClickRoll(1);
	for (int i=0; i<N_DICE;i++) {
		dice[i] = rollCall();
	}
	display.displayDice(dice);
	for(int i=0;i<2;i++) {
		String roll="rolls";
		if(2-i==1) {
			roll="roll";
		}
		display.printMessage(2-i+" more " +roll+". Pick which dice to roll again, "+name+".");
		display.waitForPlayerToSelectDice();
		for (int j=0; j<N_DICE;j++) {
			if(display.isDieSelected(j)==true) {	
				dice[j] = rollCall();
			}
		}
		display.displayDice(dice);
	}
	return dice;
}
	
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

}
