package com.aueb;

import java.util.ArrayList;

class Board
{
	public static final int W = 1;
    public static final int B = -1;
    public static final int EMPTY = 0;

    private int[][] gameBoard;

    private int lastPlayer;

    private Move lastMove;
	
	private int dimension = 8;
	
	public Board() {
        this.lastMove = new Move();
        this.lastPlayer = W;
        this.gameBoard = new int[8][8];
        for(int i = 0; i < this.gameBoard.length; i++)
        {
            for(int j = 0; j < this.gameBoard.length; j++)
            {
                this.gameBoard[i][j] = EMPTY;
            }
        }
        this.gameBoard[3][3]= 1;
        this.gameBoard[4][4]= 1;
        this.gameBoard[3][4]= -1;
        this.gameBoard[4][3]= -1;
    }
	
	// copy constructor
    public Board(Board board) {

        this.lastMove = board.lastMove;
        this.lastPlayer = board.lastPlayer;
        this.gameBoard = new int[8][8];
        for(int i = 0; i < this.gameBoard.length; i++)
        {
            for(int j = 0; j < this.gameBoard.length; j++)
            {
                this.gameBoard[i][j] = board.gameBoard[i][j];
            }
        }
    }

    //Make a move; it places a piece in the board
    void makeMove(int row, int col, int letter)
    {
        this.gameBoard[row][col] = letter;
        this.lastMove = new Move(row, col);
        this.lastPlayer = letter;
    }
	
	public void print() {
        System.out.println("*********");
        for(int row=0; row<8; row++)
        {
            System.out.print("* ");
            for(int col=0; col<8; col++)
            {
                switch (this.gameBoard[row][col]) {
                    case -1 -> System.out.print("B ");
                    case 1 -> System.out.print("W ");
                    case EMPTY -> System.out.print("- ");
                    default -> {
                    }
                }
            }
            System.out.println("*");
        }
        System.out.println("*********");
    }

	
	ArrayList<Board> getChildren(int letter) {
        ArrayList<Board> children = new ArrayList<>();
        ArrayList<Move> moves= getavailableMoves(letter);
        for(Move mv:moves){
            Board child = new Board(this);
            int row = mv.getRow();
            int col = mv.getCol(); 
            child.makeMove(row, col, letter);
            Move m=new Move(row,col,letter);
            
            child.switcher(m); //ta gyrizoume gia na vgainoun swsta
            children.add(child);
        }
        
        return children;
    }

	public int evaluate () {
        int scoreB = 0;
        int scoreW = 0;
        int sumB=0;
        int sumW=0;
        int centerW=0;
        int centerB=0;
        int cornerW=0;
        int cornerB=0;

        //checking rows
        for(int row = 0; row < dimension; row++){
            for (int col = 0; col < dimension; col++){
                if (this.gameBoard[row][col]==W){
                    sumW++; //number of W discs on board
                }
                else if(this.gameBoard[row][col]==B){
                    sumB++;//number of B discs on board                    
                }
            }
        }
        //center nodes 2x2
            if (gameBoard[3][3]==W) {
                    centerW++;
                }
            if(gameBoard[3][4]==W){
                centerW++;
            }
            if(gameBoard[4][3]==W){
                centerW++;
            }
            if(gameBoard[4][4]==W){
                centerW++;
            }
            
        centerB=4-centerW;
        //Stratigiki katlambanw to kentro tou tablo
        scoreW=scoreW-(centerW*10);
        scoreB=scoreB+(centerB*10);
        centerW=0;
        centerB=0;    
        //center nodes 4x4 
        for (int i=2;i<=5;i++){
            for(int j=2;j<=5;j++){
                if (gameBoard[i][j]==W){
                    centerW++;
                }else if (gameBoard[i][j]==B){
                    centerB++;
                }
            }

        }
        scoreW=scoreW-(centerW*10);
        scoreB=scoreB+(centerB*10);     
      

        //corner nodes
        if (gameBoard[0][0]==W){
            cornerW++;
        }else if(gameBoard[0][0]==B){
            cornerB++;
        }

        if (gameBoard[0][7]==W){
            cornerW++;
        }else if(gameBoard[0][7]==B){
            cornerB++;
        }

        if (gameBoard[7][0]==W){
            cornerW++;
        }else if(gameBoard[7][0]==B){
            cornerB++;
        }

        if (gameBoard[7][7]==W){
            cornerW++;
        }else if(gameBoard[7][7]==B){
            cornerB++;
        }

        scoreB=scoreB+(cornerB*1000);
        scoreW=scoreW-(cornerW*1000);

        //stable disks
        int stableDiscs_W = getStableDiscs(W).size();
        int stableDiscs_B = getStableDiscs(B).size();
        scoreW-=100*stableDiscs_W; 
        scoreB+= 100*stableDiscs_B;
        
        int totalDiscCnt = sumB+sumW;
        // perissotera poulia analoga tin fasi paixnidiou
        if(totalDiscCnt<58){ //bad for start and mid game
            if(sumW>sumB){
                scoreW+=20;
            }
            else{
                scoreB-=20;
            }
            
        }else { //good for end game 
            if(sumW>sumB){
                scoreW-=50;
            }
            else{
                scoreB+=50;
            }
        }
       
        //frontier strategy 
        if (getFrontierSquares(W).size()<getFrontierSquares(B).size()){
            scoreW -= 75; //if W has less frontier discs it is in a better condition
        }else if (getFrontierSquares(B).size()<getFrontierSquares(W).size()){
            scoreB += 75; //if B has less frontier discs it is in a better condition
        }    

        //mobility 
        int movesW= getavailableMoves(W).size();
        int movesB= getavailableMoves(B).size();

        int diafora=movesW-movesB;

        if(diafora>0){
            scoreW-=100;
        }else if(diafora<0){
            scoreB+=100;
        }

        //Stratigiki wedges (sfina) gia ta edges
        ArrayList<ArrayList<Integer>>wedges=getUpWedges();
        for (int j = 0; j < wedges.get(0).size(); j++) { //whites
            if(wedges.get(0).get(j)!=0 && wedges.get(0).get(j)%2==0){//zygo plithos kenwn gia ta aspra
                scoreW-=60;
            }else if(wedges.get(0).get(j)!=0 && wedges.get(0).get(j)%2!=0){
                scoreW+=60;
            }
        } 
        for (int j = 0; j < wedges.get(1).size(); j++) { //blacks
            if(wedges.get(1).get(j)!=0 && wedges.get(1).get(j)%2==0){//zygo plithos kenwn gia ta mayra(eynoikh katastash)
                scoreB+=60;
            }else if(wedges.get(1).get(j)!=0 && wedges.get(1).get(j)%2!=0){
                scoreB-=60;
            }
        } 

        ArrayList<ArrayList<Integer>>Lwedges=getLeftWedges();
        for (int j = 0; j < Lwedges.get(0).size(); j++) { //whites
            if(Lwedges.get(0).get(j)!=0 && Lwedges.get(0).get(j)%2==0){//zygo plithos kenwn gia ta aspra
                scoreW-=60;
            }else if(Lwedges.get(0).get(j)!=0 && Lwedges.get(0).get(j)%2!=0){
                scoreW+=60;
            }
        } 
        for (int j = 0; j < Lwedges.get(1).size(); j++) { //blacks
            if(Lwedges.get(1).get(j)!=0 && Lwedges.get(1).get(j)%2==0){//zygo plithos kenwn gia ta mayra(eynoikh katastash)
                scoreB+=60;
            }else if(Lwedges.get(1).get(j)!=0 && Lwedges.get(1).get(j)%2!=0){
                scoreB-=60;
            }
        } 

        ArrayList<ArrayList<Integer>>Rwedges=getRightWedges();
        for (int j = 0; j < Rwedges.get(0).size(); j++) { //whites
            if(Rwedges.get(0).get(j)!=0 && Rwedges.get(0).get(j)%2==0){//zygo plithos kenwn gia ta aspra
                scoreW-=60;
            }else if(Rwedges.get(0).get(j)!=0 && Rwedges.get(0).get(j)%2!=0){
                scoreW+=60;
            }
        } 
        for (int j = 0; j < Rwedges.get(1).size(); j++) { //blacks
            if(Rwedges.get(1).get(j)!=0 && Rwedges.get(1).get(j)%2==0){//zygo plithos kenwn gia ta mayra(eynoikh katastash)
                scoreB+=60;
            }else if(Rwedges.get(1).get(j)!=0 && Rwedges.get(1).get(j)%2!=0){
                scoreB-=60;
            }
        } 

        ArrayList<ArrayList<Integer>>dwedges=getDownWedges();
        for (int j = 0; j < dwedges.get(0).size(); j++) { //whites
            if(dwedges.get(0).get(j)!=0 && dwedges.get(0).get(j)%2==0){//zygo plithos kenwn gia ta aspra
                scoreW-=60;
            }else if(dwedges.get(0).get(j)!=0 && dwedges.get(0).get(j)%2!=0){
                scoreW+=60;
            }
        } 
        for (int j = 0; j < dwedges.get(1).size(); j++) { //blacks
            if(dwedges.get(1).get(j)!=0 && dwedges.get(1).get(j)%2==0){//zygo plithos kenwn gia ta mayra(eynoikh katastash)
                scoreB+=60;
            }else if(dwedges.get(1).get(j)!=0 && dwedges.get(1).get(j)%2!=0){
                scoreB-=60;
            }
        } 
        

        //Stratigiki parity counts on mid_game 
        if (totalDiscCnt>=20 ){
            int emptyaround=0;
            int row=this.lastMove.getRow();
            int col=this.lastMove.getCol(); 
            if(row>=1 && col>=1){ //panw kai aristera
                    if(gameBoard[row-1][col-1]==EMPTY){
                        emptyaround++;
                    }
                }
                if(row>=1 ){ //panw
                    if(gameBoard[row-1][col]==EMPTY){
                        emptyaround++;
                    }
                }
                if(row>=1 && col<7){ //panw kai deksia
                    if(gameBoard[row-1][col+1]==EMPTY){
                        emptyaround++;
                    }
                }
                if( col<7){ //deksia
                    if(gameBoard[row][col+1]==EMPTY){
                        emptyaround++;
                    }
                }
                if(col>=1){ //aristera
                    if(gameBoard[row][col-1]==EMPTY){
                        emptyaround++;
                    }
                }
                if(row<7 && col>=1){ //katw aristera
                    if(gameBoard[row+1][col-1]==EMPTY){
                        emptyaround++;
                    }
                }
                if(row<7){ //katw
                    if(gameBoard[row+1][col]==EMPTY){
                        emptyaround++;
                    }
                }
                if(row<7 && col<7){ //katw kai deksia 
                    if(gameBoard[row+1][col+1]==EMPTY){
                        emptyaround++;
                    }
                }

                if (emptyaround%2==0){//good move
                    if (this.lastMove.getValue()==B){
                        scoreB+=50;
                        scoreW+=50;
                    }else if (this.lastMove.getValue()== W){
                        scoreB-=50;
                        scoreW-=50;
                    }
                }else{//bad move
                    if (this.lastMove.getValue()==B){
                        scoreB-=50;
                        scoreW-=50;

                    }else if (this.lastMove.getValue()==W){
                        scoreB+=50;
                        scoreW+=50;
        
                    }
                }
        }     
        
        //stratigiki positioning(min paizei se xsquare otan i gwnia tou einai keni)
        if(gameBoard[0][0]==EMPTY){
            if(gameBoard[1][1]==B){
                scoreB-=25;
            }else if(gameBoard[1][1]==W){
                scoreW+=25;
            }
        }
        if(gameBoard[0][7]==EMPTY){
            if(gameBoard[1][6]==B){
                scoreB-=25;
            }else if(gameBoard[1][6]==W){
                scoreW+=25;
            }
        }
        if(gameBoard[7][0]==EMPTY){
            if(gameBoard[6][1]==B){
                scoreB-=25;
            }else if(gameBoard[6][1]==W){
                scoreW+=25;
            }
        } 
        if(gameBoard[7][7]==EMPTY){
            if(gameBoard[6][6]==B){
                scoreB-=25;
            }else if(gameBoard[6][6]==W){
                scoreW+=25;
            }
        }     
 
        //unbalanced edges pieces
        ArrayList<Integer> balanced= unbalancedEdges();
        int aspros=balanced.get(0);
        int mavros=balanced.get(1);
        scoreW+=aspros;
        scoreB+=mavros;
        
    

        return scoreB - scoreW;
    }
	
    private ArrayList<Integer> unbalancedEdges(){
        ArrayList<Integer> balanced =  new ArrayList<Integer>();
        int scoreb=0;
        int scorew=0;
        boolean balancedupedge=false;
        boolean foundW=false;
        boolean foundB=false;
        int plithosw=0;
        int plithosb=0;
        //first row(up)
        for (int col = 0; col <dimension; col++){
            //check same colour
            if(gameBoard[0][col]==B){
                foundB=true;
                plithosb++;
            }
            if(gameBoard[0][col]==W){
                foundW=true;
                plithosw++;
            }
        }
        
        if( (foundB==true && foundW!=true)){
            if(plithosb>1){
                int col1=0;
                int aristerakena=0;
                while(col1<dimension && gameBoard[0][col1]==EMPTY){
                    aristerakena++;
                    col1++;
                }
                col1=7;
                int deksiakena=0;
                while(col1>-1 && gameBoard[0][col1]==EMPTY){
                    deksiakena++;
                    col1--;
                }
                if(deksiakena==aristerakena){
                    balancedupedge=true;
                }
                if(balancedupedge){
                    scoreb+=10;
                }else{
                    scoreb-=10;
                }
            }
           
            
        }else if (foundB!=true && foundW==true){
           if(plithosw>1){
            int col1=0;
            int aristerakena=0;
            while(col1<dimension && gameBoard[0][col1]==EMPTY){
                aristerakena++;
                col1++;
            }
            col1=7;
            int deksiakena=0;
            while(col1>-1 && gameBoard[0][col1]==EMPTY){
                deksiakena++;
                col1--;
            }
            if(deksiakena==aristerakena){
                balancedupedge=true;
            }
            if(balancedupedge){
                scorew-=10;
            }else{
                scorew+=10;
            }
           }
            

        }
        
        //first column(left)
        boolean balancedleftedge=false;
        foundW=false;
        foundB=false;
        int col2=0;
        plithosw=0;
        plithosb=0;
         
        for (int col = 0; col <dimension; col++){
            //check same colour
            if(gameBoard[col][0]==B){
                foundB=true;
                plithosb++;
            }
            if(gameBoard[col][0]==W){
                foundW=true;
                plithosw++;
            }
        }
        if( (foundB==true && foundW!=true)){
            if(plithosb>1){
                int col1=0;
                int panwkena=0;
                while(col1<dimension && gameBoard[col1][0]==EMPTY){
                    panwkena++;
                    col1++;
                }
                col1=7;
                int katwkena=0;
                while(col1>-1 && gameBoard[col1][0]==EMPTY){
                    katwkena++;
                    col1--;
                }
                if(panwkena==katwkena){
                    balancedleftedge=true;
                }
            if(balancedleftedge){
                scoreb+=10;
            }else{
                scoreb-=10;
            }
            }
            
            
        }
        else if (foundB!=true && foundW==true){
            if(plithosw>1){
                int col1=0;
                int panwkena=0;
                while(col1<dimension && gameBoard[col1][0]==EMPTY){
                    panwkena++;
                    col1++;
                }
                col1=7;
                int katwkena=0;
                while(col1>-1 && gameBoard[col1][0]==EMPTY){
                    katwkena++;
                    col1--;
                }
                if(panwkena==katwkena){
                    balancedleftedge=true;
                }
                if(balancedleftedge){
                    scorew-=10;
                }else{
                    scorew+=10;
                }
            }
           
        }
        
       
        //last row (down)
        boolean balanceddownedge=false;
        foundW=false;
        foundB=false;
        plithosw=0;
        plithosb=0;
        
         
        for (int col = 0; col <dimension; col++){
            //check same colour
            if(gameBoard[7][col]==B){
                foundB=true;
                plithosb++;
            }
            if(gameBoard[7][col]==W){
                foundW=true;
                plithosw++;
            }
        }
        if( (foundB==true && foundW!=true)){
            if(plithosb>1){
                int col1=0;
                int aristerakena=0;
                while(col1<dimension && gameBoard[7][col1]==EMPTY){
                    aristerakena++;
                    col1++;
                }
                col1=7;
                int deksiakena=0;
                while(col1>-1 && gameBoard[7][col1]==EMPTY){
                    deksiakena++;
                    col1--;
                }
                if(deksiakena==aristerakena){
                    balanceddownedge=true;
                }
            if(balanceddownedge){
                scoreb+=10;
            }else{
                scoreb-=10;
            }
            }
            
        }
        else if (foundB!=true && foundW==true){
            if(plithosw>1){
                int col1=0;
                int aristerakena=0;
                while(col1<dimension && gameBoard[7][col1]==EMPTY){
                    aristerakena++;
                    col1++;
                }
                col1=7;
                int deksiakena=0;
                while(col1>-1 && gameBoard[7][col1]==EMPTY){
                    deksiakena++;
                    col1--;
                }
                if(deksiakena==aristerakena){
                    balanceddownedge=true;
                }
            if(balanceddownedge){
                scorew-=10;
            }else{
                scorew+=10;
            }
            }
            
        }
        
        //right column 
        boolean balancedrightedge=false;
        foundW=false;
        foundB=false;
        
        plithosw=0;
        plithosb=0;
         
        for (int col = 0; col <dimension; col++){
            //check same colour
            if(gameBoard[col][7]==B){
                foundB=true;
                plithosb++;
            }
            if(gameBoard[col][7]==W){
                foundW=true;
                plithosw++;
            }
        }
        if( (foundB==true && foundW!=true)){
            if(plithosb>1){
                int col1=0;
                int panwkena=0;
                while(col1<dimension && gameBoard[col1][7]==EMPTY){
                    panwkena++;
                    col1++;
                }
                col1=7;
                int katwkena=0;
                while(col1>-1 && gameBoard[col1][7]==EMPTY){
                    katwkena++;
                    col1--;
                }
                if(panwkena==katwkena){
                    balancedrightedge=true;
                }
            if(balancedrightedge){
                scoreb+=10;
            }else{
                scoreb-=10;
            }
            }
            
            
        }
        else if (foundB!=true && foundW==true){
            if(plithosw>1){
                int col1=0;
                int panwkena=0;
                while(col1<dimension && gameBoard[col1][7]==EMPTY){
                    panwkena++;
                    col1++;
                }
                col1=7;
                int katwkena=0;
                while(col1>-1 && gameBoard[col1][7]==EMPTY){
                    katwkena++;
                    col1--;
                }
                if(panwkena==katwkena){
                    balancedrightedge=true;
                }
            if(balancedrightedge){
                scorew-=10;
            }else{
                scorew+=10;
            }
            }
            
            
        }
        balanced.add(scorew);
        balanced.add(scoreb);
        return balanced;
    }

	public boolean isTerminal() {


        //check if it has skipped up to 2 times
        int skipPlayer = Main.skipP;
        int skipComputer = Main.skipC;
        
        if (skipPlayer >= 1 && skipComputer >= 1){
            return true;
        }

        // check if all the pieces are one color
        int Bpieces = 0;
        int Wpieces = 0;
        boolean notsamecolor_Alldiscs = true;
        for(int row = 0; row < this.gameBoard.length; row++)
        {
            for(int col = 0; col < this.gameBoard.length; col++)
            {
                if(this.gameBoard[row][col] != EMPTY){
                    if (this.gameBoard[row][col] == W){
                        Wpieces ++;
                    }else{
                        Bpieces++;
                    }
                }
            }
        } 
        if (Wpieces == 0 || Bpieces ==0){
            notsamecolor_Alldiscs = false;            
        }

        // check for empty
        boolean filled =true;
        for(int row = 0; row < this.gameBoard.length; row++)
        {
            for(int col = 0; col < this.gameBoard.length; col++)
            {
                if(this.gameBoard[row][col] == EMPTY) {
                    filled=false;
                    return false;
                }
            }
        }  
        if (filled || notsamecolor_Alldiscs){
            
            return true;
        }        
        
        return true;
    }
	
	public Move getLastMove()
    {
        return this.lastMove;
    }

    public int getLastPlayer()
    {
        return this.lastPlayer;
    }

    public int[][] getGameBoard()
    {
        return this.gameBoard;
    }
	
	void setGameBoard(int[][] gameBoard)
    {
        for(int i = 0; i < this.dimension; i++)
        {
            for(int j = 0; j < this.dimension; j++)
            {
                this.gameBoard[i][j] = gameBoard[i][j];
            }
        }
    }

    void setLastMove(Move lastMove)
    {
        this.lastMove.setRow(lastMove.getRow());
        this.lastMove.setCol(lastMove.getCol());
        this.lastMove.setValue(lastMove.getValue());
    }

    void setLastPlayer(int lastPlayer)
    {
        this.lastPlayer = lastPlayer;
    }


    int switcher(Move move){

        boolean isChecked;
        int i,j;
        int row = move.getRow();

        int col = move.getCol();       

        //1.up        
        isChecked = false;
        if (row>0){
            i = row-1;
            while (i >= 0 ){
                if(gameBoard[i][col]==0){//an yparxei endiamesa keno den xreiazetai na gyristei kati
                    break;
                }
                if(gameBoard[i][col] == move.getValue()){//elegxoume an yparxei pioni idiou xrwmatos sthn stili 
                    isChecked = true;
                    break;
                }           
                i--;
            }
            if (isChecked){
                for (int k = i+1; k<move.getRow(); k++){
                    if(gameBoard[k][col] == move.getValue()*(-1)){//elegxoume an yparxei pioni allou xrwmatos anamesa
                        gameBoard[k][col] = move.getValue();
                    }
                    
                }
                setGameBoard(gameBoard);
                           
            }
        }

        //2.down        
        isChecked = false;
        if (row<7){
            i = row+1;
            while (i  <= 7 ){
                if(gameBoard[i][col]==0){
                    break;
                }
                if(gameBoard[i][col] == move.getValue()){
                    isChecked = true;
                    break;
                }           
                i++;
            }
            if (isChecked){
                for (int k = move.getRow()+1; k<i; k++){
                    if(gameBoard[k][col] == move.getValue()*(-1)){
                        gameBoard[k][col] = move.getValue();
                    }
                    
                }
                setGameBoard(gameBoard);
                
            }
        }

        //3.left        
        isChecked = false;
        if (col>0){
            j = col-1;
            while (j >=0 ){
                if(gameBoard[row][j]==0){
                    break;
                }
                if(gameBoard[row][j] == move.getValue()){
                    isChecked = true;
                    break;
                }           
                j--;
            }
            
            if (isChecked){
                for (int k = j+1; k<move.getCol(); k++){
                    if(gameBoard[row][k] == move.getValue()*(-1)){
                        gameBoard[row][k] = move.getValue();
                    }
                    
                }
                setGameBoard(gameBoard);
                
            }
        }

        //4.right        
        isChecked = false;
        if (col<7){
            j = col+1;
            while (j <= 7 ){
                if(gameBoard[row][j]==0){
                    break;
                }
                if(gameBoard[row][j] == move.getValue()){
                    isChecked = true;
                    break;
                }           
                j++;
            }
            if (isChecked){
                for (int k = move.getCol()+1; k<j; k++){
                    if(gameBoard[row][k] == move.getValue()*(-1)){
                        gameBoard[row][k] = move.getValue();
                    }
                }
                setGameBoard(gameBoard);
                
            }
        }

        //5.right up      
        isChecked = false;
        if (row>0 && col<7){
            i = row-1;
            j = col +1;
            
            while (i>=0 && j<=7){
                if(gameBoard[i][j]==0){
                    break;
                }
                if(gameBoard[i][j] == move.getValue()){
                    isChecked = true;
                    break;
                } 
                i--;          
                j++;
                 
            }
            if (isChecked){
                int k=i+1;
                int l=j-1;
                
                while ( k<move.getRow() && l>move.getCol() ){
                    if(gameBoard[k][l] == move.getValue()*(-1)){
                        gameBoard[k][l] = move.getValue();
                    } 
                    k++;          
                    l--;
                    
                }    
                setGameBoard(gameBoard);
                         
            }
        }
        
        //6.right down    
        isChecked = false;
        if (row<7 && col<7){
            i = move.getRow()+1;
            j = move.getCol()+1 ;
            
            while ( i<=7 && j<=7){
                if(gameBoard[i][j]==0){
                    break;
                }
                if(gameBoard[i][j] == move.getValue()){
                    isChecked = true;
                    break;
                } 
                i++;          
                j++;
               
            }
            if (isChecked){
                int k=i-1;
                int l=j-1;
                
                while (  k>move.getRow() && l>move.getCol()){
                    if(gameBoard[k][l] == move.getValue()*(-1)){
                        gameBoard[k][l] = move.getValue();
                    } 
                    k--;          
                    l--;
                    
                }        
                setGameBoard(gameBoard);
                
            } 
        }

       //7.left up    
        isChecked = false;
        
        if (row>0 && col>0){
            i = move.getRow()-1;
            j = move.getCol() -1;
            
            while ( i>=0 && j>=0){
                if(gameBoard[i][j]==0){
                    break;
                }
                if(gameBoard[i][j] == move.getValue()){
                    isChecked = true;
                    break;
                } 
                i--;          
                j--;
                
            }
           
            if (isChecked){
                int k= i+1;
                int l=j+1;
               
                while (  k<move.getRow() && l<move.getCol()){
                    if(gameBoard[k][l] == move.getValue()*(-1)){
                        gameBoard[k][l] = move.getValue();
                    } 
                    k++;          
                    l++;
                    
                }   
                setGameBoard(gameBoard);
                        
            } 
        } 

        //8.left down    
        isChecked = false;
        if (row<7 && col>0){
            i = move.getRow()+1;
            j = move.getCol() -1;
           
            while (i<=7 && j>=0){
                if(gameBoard[i][j]==0){
                    break;
                }
                if(gameBoard[i][j] == move.getValue()){
                    isChecked = true;
                    break;
                } 
                i++;          
                j--;
                
            }
            if (isChecked){
                i = move.getRow()+1;
                j = move.getCol() -1;
                
                while (i<=7 && j>=0){
                    if(gameBoard[i][j] == move.getValue()*(-1)){
                        gameBoard[i][j] = move.getValue();
                    } 
                    i++;          
                    j--;
                    
                }   
                setGameBoard(gameBoard);
                       
            }
        } 
        return 0; 

    }
   
    ArrayList<Move>  getStableDiscs(int playerLetter){
        ArrayList<Move> stablediscs = new ArrayList<>();
        int sd=0;
        // if it has the left top corner
        if (gameBoard[0][0] == playerLetter){  
            sd++;
            stablediscs.add(new Move(0,0));
            //checking stable disks on same row
            for (int col=1; col<8; col++ ){
                if (gameBoard[0][col] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(0,col));
                }else{
                    break;
                }
            }
            //checking stable disks on same column
            for (int row=1; row<8; row++ ){
                if (gameBoard[row][0] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(row,0));
                }else{
                    break;
                }
            }
            if (sd>0){
                for (int row=1; row<7; row++){
                    for (int col=1; col<7; col++){
                        //if diagonal is stable it should be white
                        if (gameBoard[row][col]==playerLetter){
                            //secondly it should have a same color disk up and left
                            if (gameBoard[row-1][col]==playerLetter && gameBoard[row][col-1]==playerLetter){
                                //AND neither up and diagonal OR down and diagonal
                                if (gameBoard[row-1][col+1] == playerLetter || gameBoard[row+1][col-1] == playerLetter){
                                    sd ++;
                                    stablediscs.add(new Move(row,col));
                                }else{
                                    break;
                                }
                            }else{
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                    //If any ONE of the above is not valid the disk is NOT stable
                }
            }
        }
        // if it has the right up corner
        if (gameBoard[0][7] == playerLetter){
            sd++;
            stablediscs.add(new Move(0,7));
            //checking stable disks on same row
            for (int col=6; col>=0; col--){  
                if (gameBoard[0][col] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(0,col));
                }else{
                    break;
                }
            }
            
            //checking stable disks on same column
            for (int row=1; row<8; row++ ){
                if (gameBoard[row][7] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(row,7));
                }else{
                    break;
                }
            }
            if (sd>0){
                for (int row=1; row<7; row++){
                    for (int col=6; col>0; col--){
                        //if diagonal is stable it should be white
                        if (gameBoard[row][col]==playerLetter){
                            //secondly it should have a same color disk up and left
                            if (gameBoard[row-1][col]==playerLetter && gameBoard[row][col+1]==playerLetter){
                                //AND neither up and diagonal OR down and diagonal
                                if (gameBoard[row-1][col-1] == playerLetter || gameBoard[row+1][col+1] == playerLetter){
                                    sd ++;
                                    stablediscs.add(new Move(row,col));
                                }else{
                                    break;
                                }
                            }else{
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                    //If any ONE of the above is not valid the disk is NOT stable
                }
            }
        }
        // if it has the left bottom corner
        if (gameBoard[7][0] == playerLetter){   
            sd++;
            stablediscs.add(new Move(7,0));
            //checking stable disks on same row
            for (int col=1; col<8; col++){
                if (gameBoard[7][col] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(7,col));
                }else{
                    break;
                }
            }
            //checking stable disks on same column
            for (int row=6; row>=0; row--){
                if (gameBoard[row][0] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(row,0));
                }else{
                    break;
                }
            }
            if (sd>0){
                for (int row=6; row>0; row--){
                    for (int col=1; col<7; col++){
                        //if diagonal is stable it should be white
                        if (gameBoard[row][col]==playerLetter){
                            //secondly it should have a same color disk up and left
                            if (gameBoard[row+1][col]==playerLetter && gameBoard[row][col-1]==playerLetter){
                                //AND neither up and diagonal OR down and diagonal
                                if (gameBoard[row-1][col-1] == playerLetter || gameBoard[row+1][col+1] == playerLetter){
                                    sd ++;
                                    stablediscs.add(new Move(row,col));
                                }else{
                                    break;
                                }
                            }else{
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                    //If any ONE of the above is not valid the disk is NOT stable
                }
            }
        }
        // if it has the right bottom corner
        if (gameBoard[7][7] == playerLetter){   
            sd++;
            stablediscs.add(new Move(7,7));
            //checking stable disks on same row
            for (int col=6; col>=0; col--){
                if (gameBoard[7][col] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(7,col));
                }else{
                    break;
                }
            }
            //checking stable disks on same column
            for (int row=6; row>=0; row--){
                if (gameBoard[row][7] == playerLetter){
                    sd ++;
                    stablediscs.add(new Move(row,7));
                }else{
                    break;
                }
            }
            if (sd>0){
                for (int row=6; row>0; row--){
                    for (int col=6; col>0; col--){
                        //if diagonal is stable it should be white
                        if (gameBoard[row][col]==playerLetter){
                            //secondly it should have a same color disk up and left
                            if (gameBoard[row+1][col]==playerLetter && gameBoard[row][col+1]==playerLetter){
                                //AND neither up and diagonal OR down and diagonal
                                if (gameBoard[row-1][col+1] == playerLetter || gameBoard[row+1][col-1] == playerLetter){
                                    sd ++;
                                    stablediscs.add(new Move(row,col));
                                }else{
                                    break;
                                }
                            }else{
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                    //If any ONE of the above is not valid the disk is NOT stable
                }
            }
        }


        ArrayList<Move> stableD = new ArrayList<Move>();
        ArrayList<Move> copiedStableDisks = stablediscs;
        //remove duplicates
        for (int i=0; i<stablediscs.size(); i++){            
            Move checkingEl = stablediscs.get(i);
            boolean duplicated = false;
            for (int j=0; j<stablediscs.size(); j++){
                if (i != j){  
                
                    if ((checkingEl.getRow()==stablediscs.get(j).getRow())&& (checkingEl.getCol()==stablediscs.get(j).getCol())){
                        duplicated=true;
                        break;
                    }
                }

            }
            if (duplicated){
                boolean not_inserted = true;
                for (Move d: stableD){
                    // checking if its imported
                    if ((checkingEl.getRow()==d.getRow())&& (checkingEl.getCol()==d.getCol())){
                        not_inserted =false;
                    }
                }
                if (not_inserted){
                    stableD.add(checkingEl);
                }
            }else{
                stableD.add(checkingEl);
            }
        }
        
        return stableD;
    }

    public ArrayList<Move> getavailableMoves(int playerLetter){
        ArrayList<Move> rc = new ArrayList<Move>();
        ArrayList<Move> frontiersOpponent = getFrontierSquares((-1)*playerLetter);
        for (Move frontOpMove: frontiersOpponent){
            //check 8 directions
            //up
            if (frontOpMove.getRow()>0 && frontOpMove.getRow()<7){
                boolean OurDiscExists = false;
                for (int row=frontOpMove.getRow()-1; row>=0; row--){
                    if (gameBoard[row][frontOpMove.getCol()] == playerLetter){
                        OurDiscExists = true;
                        break;              
                    }
                    else if (gameBoard[row][frontOpMove.getCol()] == EMPTY){
                        break;
                    }
                }
                if (OurDiscExists){
                    boolean avail = false;
                    int row=frontOpMove.getRow()+1;
                    while (row<8){
                        if (gameBoard[row][frontOpMove.getCol()] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[row][frontOpMove.getCol()] == playerLetter){
                            break;
                        }
                        row++;
                    }
                    if (avail){
                       // System.out.println("YOU HAVE MOVE row: " + row + " column: "+frontOpMove.getCol()+" WE FOUND OPPONENT DISK ON UP ");

                        rc.add(new Move(row,frontOpMove.getCol()));
                    }
                }
            }
            //down
            if (frontOpMove.getRow()<7){
                boolean OurDiscExists = false;
                for (int row=frontOpMove.getRow()+1; row<=7; row++){
                    if (gameBoard[row][frontOpMove.getCol()] == playerLetter){
                        OurDiscExists = true;
                        break;                 
                    }
                    else if (gameBoard[row][frontOpMove.getCol()] == EMPTY){
                        break;
                    }
                }
                if (OurDiscExists){
                    boolean avail = false;
                    int row=frontOpMove.getRow()-1;
                    while (row>=0){
                        if (gameBoard[row][frontOpMove.getCol()] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[row][frontOpMove.getCol()] == playerLetter){
                            break;
                        }
                        row--;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + row + " column: "+frontOpMove.getCol()+" WE FOUND OPPONENT DISK ON DOWN ");

                        
                        rc.add(new Move(row,frontOpMove.getCol()));
                    }
                }
            }
            //right
            if (frontOpMove.getCol()<7){
                boolean OurDiscExists = false;
                for (int col=frontOpMove.getCol()+1; col<=7; col++){
                    if (gameBoard[frontOpMove.getRow()][col] == playerLetter){
                        OurDiscExists = true;
                        break;                    
                    }
                    else if (gameBoard[frontOpMove.getRow()][col] == EMPTY){
                        break;
                    }
                }
                if (OurDiscExists){
                    boolean avail = false;
                    int col=frontOpMove.getCol()-1;
                    while (col>=0){
                        if (gameBoard[frontOpMove.getRow()][col] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[frontOpMove.getRow()][col] == playerLetter){
                            break;
                        }
                        col--;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + frontOpMove.getRow() + " column: "+col+" WE FOUND OPPONENT DISK ON RIGHT ");
                        rc.add(new Move(frontOpMove.getRow(),col));
                    }
                }
            }
            // left
            if (frontOpMove.getCol()>0 ){
                boolean OurDiscExists = false;
                for (int col=frontOpMove.getCol()-1; col>=0; col--){
                    if (gameBoard[frontOpMove.getRow()][col] == playerLetter){
                        OurDiscExists = true;
                        break;                     
                    }
                    else if (gameBoard[frontOpMove.getRow()][col] == EMPTY){
                        break;
                    }
                    
                }
                if (OurDiscExists){
                    boolean avail = false;
                    int col=frontOpMove.getCol()+1;
                    while (col<8){
                        if (gameBoard[frontOpMove.getRow()][col] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[frontOpMove.getRow()][col] == playerLetter){
                            break;
                        }
                        
                        col++;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + frontOpMove.getRow() + " column: "+col+" WE FOUND OPPONENT DISK ON LEFT");
                        rc.add(new Move(frontOpMove.getRow(),col));
                    }
                }
            }
            //left-up
            if ((frontOpMove.getRow()>0 && frontOpMove.getRow()<7)&&(frontOpMove.getCol()>0 && frontOpMove.getCol()<7)){
                boolean OurDiscExists = false;
                boolean cantplay = false;
                int col=frontOpMove.getCol()-1;
                int row=frontOpMove.getRow()-1;
                while (row>=0 && col>=0){
                    
                    if (gameBoard[row][col] == playerLetter){
                        OurDiscExists = true;
                        break;                     

                    }
                    else if (gameBoard[row][col] == EMPTY){
                        cantplay = true;
                        break;
                    }                  
                    
                    col--;                       
                      
                    row--;               
                }
                if (OurDiscExists && !cantplay){
                    boolean avail = false;
                    row=row+1;
                    col=col+1;
                    while (row<8 && col<8){
                       
                        if (gameBoard[row][col] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[row][col] == playerLetter){
                            avail = false;
                            break;
                        }    
                        
                        col++;
                        row++;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + row + " column: "+col+" WE FOUND OPPONENT DISK ON LEFT UP");
                        rc.add(new Move(row,col));
                    }
                }
            }    
            //right-up
            if ((frontOpMove.getRow()>0 && frontOpMove.getRow()<7)&&(frontOpMove.getCol()>0 && frontOpMove.getCol()<7)){
                boolean OurDiscExists = false;
                boolean cantplay = false;
                int col=frontOpMove.getCol()+1;
                int row=frontOpMove.getRow()-1;
                while ( row>=0 && col<8){
                    
                    if (gameBoard[row][col] == playerLetter){
                        OurDiscExists = true;
                        break;                     

                    }
                    else if (gameBoard[row][col] == EMPTY){
                        cantplay = true;
                        break;
                    }                    
                    col++;    
                    
                    row--;               
                }
                //checking if we have an empty space on a left bottom
                if (OurDiscExists && !cantplay){
                    boolean avail = false;
                    row=row+1;
                    col=col-1;
                    while (row<8 && col>=0){
                        
                        if (gameBoard[row][col] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[row][col] == playerLetter){
                            break;
                        }                                           
                        
                        col--;
                        row++;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + row + " column: "+col+" WE FOUND OPPONENT DISK ON RIGHT UP");
                        rc.add(new Move(row,col));
                    }
                }
            }       
            //left-down
            if ((frontOpMove.getRow()>0 && frontOpMove.getRow()<7)&&(frontOpMove.getCol()>0 && frontOpMove.getCol()<7)){
                boolean OurDiscExists = false;
                boolean cantplay = false;
                int col=frontOpMove.getCol()-1;
                int row=frontOpMove.getRow()+1;
                while ( row<8 && col>=0){
                    
                    if (gameBoard[row][col] == playerLetter){
                        OurDiscExists = true;
                        break;                     

                    }else if (gameBoard[row][col] == EMPTY){
                        cantplay = true;
                        break;
                    }
                    col--;
                    row++;
                }
                //checking if we have an empty space on a right top
                if (OurDiscExists && !cantplay){
                    boolean avail = false;
                    row=row-1;
                    col=col+1;
                    while (row>=0 && col<8){
                        
                        if (gameBoard[row][col] == 0){
                            avail = true;
                            break;
                        }
                        if (gameBoard[row][col] == playerLetter){
                            break;
                        }    
                    
                        col++;
                        row--;
                    }
                    if (avail){
                       // System.out.println("YOU HAVE MOVE row: " + row + " column: "+col+" WE FOUND OPPONENT DISK ON LEFT DOWN");
                        rc.add(new Move(row,col));
                    }
                }
            }  
            //right down
            if ((frontOpMove.getRow()>0 && frontOpMove.getRow()<7)&&(frontOpMove.getCol()>0 && frontOpMove.getCol()<7)){
                boolean OurDiscExists = false;
                boolean cantplay = false;
                int col=frontOpMove.getCol()+1;
                int row=frontOpMove.getRow()+1;
                
                while  (row<8 && col<8){
                    if (gameBoard[row][col] == playerLetter){
                        OurDiscExists = true;
                        break;                     

                    }
                    else if (gameBoard[row][col] == EMPTY){
                        cantplay = true;
                        break;
                    }                
                        
                    col++;
                    row++;
                }
                //checking if we have an empty space on a left top
                if (OurDiscExists && !cantplay){
                    boolean avail = false;
                    row=row-1;
                    col=col-1;
                    while (row>=0 && col>=0){                        
                        if (gameBoard[row][col] == 0){
                            avail = true;
                            break;
                        }                    
                        if (gameBoard[row][col] == playerLetter){
                            avail = false;
                            break;
                        }                          
                        col--;
                        row--;
                    }
                    if (avail){
                        //System.out.println("YOU HAVE MOVE row: " + row + " column: "+col+" WE FOUND OPPONENT DISK ON RIGHT DOWN");
                        rc.add(new Move(row,col));
                    }
                }
            }      
     
        }


        ArrayList<Move> availableMoves = new ArrayList<Move>();
        //remove duplicates
        for (int i=0; i<rc.size(); i++){            
            Move checkingEl = rc.get(i);
            boolean duplicated = false;
            for (int j=0; j<rc.size(); j++){
                if (i != j){                 
                    if ((checkingEl.getRow()==rc.get(j).getRow())&& (checkingEl.getCol()==rc.get(j).getCol())){                        
                        duplicated=true;
                        break;
                    }
                }

            }
            if (duplicated){
                boolean not_inserted = true;
                for (Move d: availableMoves){
                    // checking if its imported
                    if ((checkingEl.getRow()==d.getRow())&& (checkingEl.getCol()==d.getCol())){
                        not_inserted =false;
                    }
                }
                if (not_inserted){
                    availableMoves.add(checkingEl);
                }
            }else{
                availableMoves.add(checkingEl);
            }
        }
        return availableMoves;
    }


    ArrayList<Move> getFrontierSquares(int playerletter){

        ArrayList<Move> frontiers = new ArrayList<>();
        ArrayList<Move> possiblefrontiers = new ArrayList<>();
        boolean is_frontier = false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                is_frontier = false;
                if(gameBoard[i][j] == playerletter){
    
                    //check 8 directions
                    //up
                    if(i>0 && gameBoard[i-1][j]==0) is_frontier =true;
                    //down
                    if(i<7 && gameBoard[i+1][j]==0) is_frontier =true;
                    //right
                    if(j<7 && gameBoard[i][j+1]==0) is_frontier =true;
                    //left
                    if(j>0 && gameBoard[i][j-1]==0) is_frontier =true;
                    //up-left
                    if(i>0 && j>0 && gameBoard[i-1][j-1]==0) is_frontier =true;
                    //up-right
                    if(i>0 && j<7 && gameBoard[i-1][j+1]==0) is_frontier =true;
                    //down-left
                    if(i<7 && j>0 && gameBoard[i+1][j-1]==0) is_frontier =true;
                    //down-right
                    if(i<7 && j<7 && gameBoard[i+1][j+1]==0) is_frontier =true;                    
                }
                if (is_frontier){
                    possiblefrontiers.add(new Move(i,j));
                }
            }
        }
        //remove duplicates                                      
        for (int t=0; t<possiblefrontiers.size(); t++){            
            Move checkingEl = possiblefrontiers.get(t);
            boolean duplicated = false;
            for (int m=0; m<possiblefrontiers.size(); m++){
                if (t != m){               
                    if ((checkingEl.getRow()==possiblefrontiers.get(m).getRow()) && (checkingEl.getCol()==possiblefrontiers.get(m).getCol())){
                        duplicated=true;

                        break;
                    }
                }
            }
            if (duplicated){
                boolean not_inserted = true;
                for (Move d: frontiers){
                    // checking if its imported
                    if ((checkingEl.getRow()==d.getRow()) && (checkingEl.getCol()==d.getCol())){
                        not_inserted =false;

                    }
                }
                if (not_inserted){
                    frontiers.add(checkingEl);
                }
            }else{
                frontiers.add(checkingEl);
            }
        }
        //remove from frontiers discs which are stable
        ArrayList<Move> stablediscsPL = getStableDiscs(playerletter);
        for (Move fd: frontiers){
            if (stablediscsPL.contains(fd)){

                frontiers.remove(fd);
            }
            
        }    
        return frontiers;
    }


    private ArrayList<ArrayList<Integer>> getUpWedges(){
        boolean ypoloipa_adeia=false;
        int empsqB=0;
        int empsqW=0;
        int stili=0;
        int stop_stili=0;
        ArrayList<Integer> whkeno=new ArrayList<Integer>(10);
        ArrayList<Integer> blkeno=new ArrayList<Integer>(10);
        ArrayList<ArrayList<Integer>>wedges=new ArrayList<ArrayList<Integer>>(2);

        for(int j=0;j<dimension;j++){ //panw edge 
            if(gameBoard[0][j]==B){ //gia black
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[0][k]==EMPTY){
                        empsqB++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension){//simainei oti meta to black pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqB=0;
                    break;
                }
                if(gameBoard[0][stop_stili]==B && empsqB>0 && stop_stili!=0){
                    blkeno.add(empsqB);
                    empsqB=0;
                }else if(gameBoard[0][stop_stili]==W && empsqB>0 && stop_stili!=0){//an meta apo empty vrw aspro
                    empsqB=0;
                }
            }

            if(gameBoard[0][j]==W){ //gia white
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[0][k]==EMPTY){
                        empsqW++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension ){//simainei oti meta to aspro pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqW=0;
                    break;
                }
                if(gameBoard[0][stop_stili]==W && empsqW>0 && stop_stili!=0){
                    whkeno.add(empsqW);
                    empsqW=0;
                }else if(gameBoard[0][stop_stili]==B && empsqW>0 && stop_stili!=0){//an meta apo empty vrw mavro
                    empsqW=0;
                }
            }
        }
        wedges.add(whkeno);
        wedges.add(blkeno);
        return wedges;

    }

    private ArrayList<ArrayList<Integer>> getLeftWedges(){

        boolean ypoloipa_adeia=false;
        int empsqB=0;
        int empsqW=0;
        int stili=0;
        int stop_stili=0;
        ArrayList<Integer> whkeno=new ArrayList<Integer>(10);
        ArrayList<Integer> blkeno=new ArrayList<Integer>(10);
        ArrayList<ArrayList<Integer>>wedges=new ArrayList<ArrayList<Integer>>(2);

        for(int j=0;j<dimension;j++){  
            if(gameBoard[j][0]==B){ //gia black
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[k][0]==EMPTY){
                        empsqB++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension){//simainei oti meta to black pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqB=0;
                    break;
                }
                if(gameBoard[stop_stili][0]==B && empsqB>0 && stop_stili!=0){
                    blkeno.add(empsqB);
                    empsqB=0;
                }else if(gameBoard[stop_stili][0]==W && empsqB>0 && stop_stili!=0){//an meta apo empty vrw aspro
                    empsqB=0;
                }
            }

            if(gameBoard[j][0]==W){ //gia white
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[k][0]==EMPTY){
                        empsqW++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension ){//simainei oti meta to aspro pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqW=0;
                    break;
                }
                if(gameBoard[stop_stili][0]==W && empsqW>0 && stop_stili!=0){
                    whkeno.add(empsqW);
                    empsqW=0;
                }else if(gameBoard[stop_stili][0]==B && empsqW>0 && stop_stili!=0){//an meta apo empty vrw mavro
                    empsqW=0;
                }
            }
        }
        wedges.add(whkeno);
        wedges.add(blkeno);
        return wedges;

    }
	
    private ArrayList<ArrayList<Integer>> getRightWedges(){
        
        boolean ypoloipa_adeia=false;
        int empsqB=0;
        int empsqW=0;
        int stili=0;
        int stop_stili=0;
        ArrayList<Integer> whkeno=new ArrayList<Integer>(10);
        ArrayList<Integer> blkeno=new ArrayList<Integer>(10);
        ArrayList<ArrayList<Integer>>wedges=new ArrayList<ArrayList<Integer>>(2);

        for(int j=0;j<dimension;j++){  
            if(gameBoard[j][7]==B){ //gia black
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[k][7]==EMPTY){
                        empsqB++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension){//simainei oti meta to black pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqB=0;
                    break;
                }
                if(gameBoard[stop_stili][7]==B && empsqB>0 && stop_stili!=0){
                    blkeno.add(empsqB);
                    empsqB=0;
                }else if(gameBoard[stop_stili][7]==W && empsqB>0 && stop_stili!=0){//an meta apo empty vrw aspro
                    empsqB=0;
                }
            }

            if(gameBoard[j][7]==W){ //gia white
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[k][7]==EMPTY){
                        empsqW++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension ){//simainei oti meta to aspro pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqW=0;
                    break;
                }
                if(gameBoard[stop_stili][7]==W && empsqW>0 && stop_stili!=0){
                    whkeno.add(empsqW);
                    empsqW=0;
                }else if(gameBoard[stop_stili][7]==B && empsqW>0 && stop_stili!=0){//an meta apo empty vrw mavro
                    empsqW=0;
                }
            }
        }
        wedges.add(whkeno);
        wedges.add(blkeno);
        return wedges;

    }

    private ArrayList<ArrayList<Integer>> getDownWedges(){
        
        boolean ypoloipa_adeia=false;
        int empsqB=0;
        int empsqW=0;
        int stili=0;
        int stop_stili=0;
        ArrayList<Integer> whkeno=new ArrayList<Integer>(10);
        ArrayList<Integer> blkeno=new ArrayList<Integer>(10);
        ArrayList<ArrayList<Integer>>wedges=new ArrayList<ArrayList<Integer>>(2);

        for(int j=0;j<dimension;j++){  
            if(gameBoard[7][j]==B){ //gia black
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[7][k]==EMPTY){
                        empsqB++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension){//simainei oti meta to black pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqB=0;
                    break;
                }
                if(gameBoard[7][stop_stili]==B && empsqB>0 && stop_stili!=0){
                    blkeno.add(empsqB);
                    empsqB=0;
                }else if(gameBoard[7][stop_stili]==W && empsqB>0 && stop_stili!=0){//an meta apo empty vrw aspro
                    empsqB=0;
                }
            }

            if(gameBoard[7][j]==W){ //gia white
                stili=j+1;
                int k=stili;
                for(k=stili;k<dimension;k++){
                    if(gameBoard[7][k]==EMPTY){
                        empsqW++;
                    }else{
                        stop_stili=k;
                        break;
                    }
                }
                if(k==dimension ){//simainei oti meta to aspro pou vrikame ola ta ypoloipa einai adeia
                    ypoloipa_adeia=true;
                    empsqW=0;
                    break;
                }
                if(gameBoard[7][stop_stili]==W && empsqW>0 && stop_stili!=0){
                    whkeno.add(empsqW);
                    empsqW=0;
                }else if(gameBoard[7][stop_stili]==B && empsqW>0 && stop_stili!=0){//an meta apo empty vrw mavro
                    empsqW=0;
                }
            }
        }
        wedges.add(whkeno);
        wedges.add(blkeno);
        return wedges;

    }

}