package com.aueb;

import java.util.ArrayList;
public class Main {
    public static int skipP = 0;
    public static int skipC = 0;
    public static void main(String[] args) {
        int playerLetter = 0;
        int computerLetter = 0;

        // Default values if no args provided
        String playerColor = args.length > 0 ? args[0] : "B";
        int level = args.length > 1 ? Integer.parseInt(args[1]) : 1;

        if (playerColor.equals("B")) {
            playerLetter = Board.B;
            computerLetter = Board.W;
        } else if (playerColor.equals("W")) {
            playerLetter = Board.W;
            computerLetter = Board.B;
        }

        // Validate level
        if (level < 1 || level > 3) {
            level = 1;
        }

        Player player = new Player(level+1,playerLetter);
        Player computer = new Player(level+1,computerLetter);

        Board board = new Board();
        board.print();

        while(!board.isTerminal())
        {
            System.out.println();
            switch(board.getLastPlayer())
            {
                //If B played last, then W  plays now
                case Board.B:
                     if (!playerColor.equals("B")){
                        System.out.println("Player make your move");
                        System.out.println("You can choose between this moves: ");
                        Board tempB=new Board(board);
                        ArrayList<Move> rcList = tempB.getavailableMoves(playerLetter);
                        if (rcList.size()>0){
                            skipP = 0;
                            for  (int i=0; i<rcList.size(); i++){
                                System.out.println(i+1 +" :  row:" +rcList.get(i).getRow()+" column: "+rcList.get(i).getCol());
                            }
                            System.out.println("What move will you make (place number)");
                            int mchoice = new java.util.Scanner(System.in).nextInt();
                            int row = rcList.get(mchoice-1).getRow();
                            int col = rcList.get(mchoice-1).getCol();                          
                                       
                            board.makeMove(row,col, Board.W);
                            board.switcher(new Move(row,col, Board.W));
                        }else{
                            System.out.println("No available moves :( ");
                            board.setLastPlayer(board.W);
                            skipP++;
                            break;
                        }
                    }else{//if W is the computer
                        System.out.println("Computer makes move");
                        Move moveW = computer.min(new Board(board),0);
                       
                        
                        if (moveW.getValue() == Integer.MAX_VALUE && (moveW.getRow()<0 && (moveW.getCol()<0))){
                            System.out.println("Computer skipped.");
                            board.setLastPlayer(board.W);
                            skipC++;
                            break;
                        }else{
                            skipC = 0;
                            board.makeMove(moveW.getRow(), moveW.getCol(), Board.W);
                            board.switcher(new Move(moveW.getRow(), moveW.getCol(), Board.W));
                        }
                    }
                    break;
                //If W played last, then B plays now
                case Board.W:
                    if (!playerColor.equals("W")){
                        System.out.println("Player make your move");
                        System.out.println("You can choose between this moves: ");
                        Board tempB=new Board(board);
                        ArrayList<Move> rcList = tempB.getavailableMoves(playerLetter);
                        if (rcList.size()>0){
                            skipP = 0;
                            for  (int i=0; i<rcList.size(); i++){
                                System.out.println(i+1 +" :  row:" +rcList.get(i).getRow()+" column: "+rcList.get(i).getCol());
                            }
                            System.out.println("What move will you make (place number)");
                            int mchoice = new java.util.Scanner(System.in).nextInt();
                            int row = rcList.get(mchoice-1).getRow();
                            int col = rcList.get(mchoice-1).getCol();                                          

                            board.makeMove(row,col, Board.B);
                            board.switcher(new Move(row,col, Board.B));
                        }else{
                            System.out.println("No available moves :( ");
                            board.setLastPlayer(board.B);
                            skipP++;
                            break;
                        }
                        
                    }else{//if computer is B
                        System.out.println("Computer makes move");
                        Move moveB = computer.max(new Board(board),0);
                        System.out.println("Computer chose row"+moveB.getRow()+" column "+moveB.getCol());
                        if (moveB.getValue() == Integer.MIN_VALUE && (moveB.getRow()<0 && (moveB.getCol()<0))){
                            System.out.println("Computer skipped.");
                            board.setLastPlayer(board.B);
                            skipC++;
                            break;
                        }else{
                            skipC = 0;
                            board.makeMove(moveB.getRow(), moveB.getCol(), Board.B);
                            board.switcher(new Move(moveB.getRow(), moveB.getCol(), Board.B));
                        }
                        
                    }
                    break;
            
                default:
                    break;
            }
            if (board.isTerminal()){
                System.out.println("GAME OVER");
                break;
            }
            if (skipC == 0 || skipP == 0){
                System.out.println("SkippedComputer :"+skipC);
                System.out.println("SkippedPlayer :"+skipP);
                board.print();
            }
        }
        board.print();
        int[][] final_board = board.getGameBoard();
        int sumB = 0;
        for(int row = 0; row < final_board.length; row++)
        {
            for(int col = 0; col < final_board.length; col++)
            {
                if(final_board[row][col] == board.B) {
                    sumB ++;                    
                }
            }
        }
        int sumW = 64-sumB;
        int winner = board.B;            
        if (sumB<sumW){
            winner = board.W;
        }else if (sumB==sumW){
            winner=2;
        }
        if (computerLetter == winner){
            System.out.println("COMPUTER WON");
        }else if(computerLetter != winner){
            System.out.println("YOU WON");
        }else{
            System.out.println("YOU BOTH WON");
        }
    }
}
