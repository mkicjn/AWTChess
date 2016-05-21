// Mark Johnson
// AP Computer Science final project
// 2016

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

public class Chess extends Applet
{
	private boolean initialized=false; // To prevent multiple initializations as a result of repaint()
	private final Rectangle boardSpace=new Rectangle(0,0,600,600); // Final because board size should (obviously) never change
	private final Rectangle resetButton=new Rectangle(600,525,200,75); // Final because reset button should never change
	private final Rectangle promotionUI=new Rectangle(600,225,150,150); // Final because the UI should never change
	public static boolean whiteTurn=true; // True because white always goes first, static just in case it needs to be checked elsewhere
	public boolean pawnCanPromote=false;
	public static int[] lastPiece={-1,-1}; // Maybe this could be private instead?
	// This piece is cached for undoing illegal moves
	public static Piece lastCapturedPiece;
	// Coordinates for check detection
	public static int[] whiteKingCoords = new int[2];
	public static int[] blackKingCoords = new int[2];
	public static int[] testWhiteKingCoords = new int[2];
	public static int[] testBlackKingCoords = new int[2];
	// These string "characters" are static to be referenced elsewhere as Chess.KING, et cetera, and should not be changed
	public static final String KING="\u2654",QUEEN="\u2655",ROOK="\u2656",BISHOP="\u2657",KNIGHT="\u2658",PAWN="\u2659";
	// These pieces are for click detection in pawn promotion UI
	private Piece uiQueen,uiKnight,uiRook,uiBishop;
	// These variables are obviously for handling the end of the game
	public static String win="Stalemate"; // (default case)
	
	public void paint(Graphics g)
	{
		drawSquares(g);
		if (!initialized)
		{
			System.out.println("\n2016 AP Computer Science 1 final project\nby Mark Johnson, 6th period\n"); // Print my name, et cetera
			whiteTurn=true;
			pawnCanPromote=false;
			win="Stalemate";
			Board.initPieces(g);
			initialized=true;
		}
		drawTurnDisplay(g);
		drawResetButton(g);
		Board.drawPieces(g);
		Board.calculateLegalMoves();
		if (pawnCanPromote&&whiteTurn)
			drawPromotionList(g,Color.WHITE);
		if (pawnCanPromote&&!whiteTurn)
			drawPromotionList(g,Color.BLACK);
		if ((whiteTurn&&checkmate(Color.WHITE)>0)||(!whiteTurn&&checkmate(Color.BLACK)>0))
		{
			System.out.println(win); // Debug message
			// Set font and color
			g.setColor(Color.BLUE);
			g.setFont(new Font("Monospaced",Font.PLAIN,30));
			// Declare the match winner, if any
			g.drawString(win,600,110);
		}
	}
	
	public void drawSquares(Graphics g)
	{	// For every piece on the board
		for (int r=0; r<8; r++)
			for (int c=0; c<8; c++)
			{
				if (r%2 != c%2) //u Checkerboard pattern
				{
					g.setColor(Color.BLACK);
					g.fillRect(r*75,c*75,75,75);
				}
				else
				{
					g.setColor(Color.WHITE);
					g.fillRect(r*75,c*75,75,75);
				}
			}
	}
	
	public boolean mouseDown(Event e, int x, int y)
	{ // Add instructions here to check whose turn it is, and act based on that.
		// Print debug message
		//System.out.println("Clicked at "+x+", "+y); // Usually disabled because of log spam
		if (resetButton.inside(x,y))
		{
			// Print debug message
			System.out.println("Clicked on reset button");
			initialized=false;
			repaint();
			return true;
		}
		if (pawnCanPromote&&promotionUI.inside(x,y))
		{
			String promotedPiece="";
			if (uiQueen.inside(x,y))
				promotedPiece="queen";
			else if (uiKnight.inside(x,y))
				promotedPiece="knight";
			else if (uiRook.inside(x,y))
				promotedPiece="rook";
			else if (uiBishop.inside(x,y))
				promotedPiece="bishop";
			// Print message for debug
			System.out.println("Promoting pawn to "+promotedPiece);
			for (int rt=0;rt<8;rt++)
				for (int ct=0;ct<8;ct++)
				{
					if (Board.pieces[rt][ct].canPromote)
					{
						if (promotedPiece.equals("queen"))
							Board.pieces[rt][ct]=new Queen(rt,ct,Board.pieces[rt][ct].pieceColor);
						else if (promotedPiece.equals("knight"))
							Board.pieces[rt][ct]=new Knight(rt,ct,Board.pieces[rt][ct].pieceColor);
						else if (promotedPiece.equals("rook"))
							Board.pieces[rt][ct]=new Rook(rt,ct,Board.pieces[rt][ct].pieceColor);
						else if (promotedPiece.equals("bishop"))
							Board.pieces[rt][ct]=new Bishop(rt,ct,Board.pieces[rt][ct].pieceColor);
					}
				}
			pawnCanPromote=false;
			repaint();
			whiteTurn=!whiteTurn;
			// Add check for checkmate here just in case pawn promotion causes checkmate
			return true;
		}
		else if (pawnCanPromote)
		{
			System.out.println("You must select a piece to promote to.");
			return true;
		}
		if (!boardSpace.inside(x,y)) // If the click occurred outside of the board
		{
			// Print debug message
			System.out.println("Clicked outside of board space");
			// Reset last clicked piece's border color (if possible) and reset last coordinates
			if (lastPiece[0]>-1&&lastPiece[1]>-1)
				Board.pieces[lastPiece[0]][lastPiece[1]].setBorderColor();
			lastPiece[0]=lastPiece[1]=-1;
			return true;
		}
		for (int r=0; r<8; r++)// For every piece on the board
			for (int c=0; c<8; c++)
			{
				if (Board.pieces[r][c].inside(x,y)) // If the square is clicked on
				{
					// Print debug message
					// System.out.println("Clicked on "+Board.pieces[r][c].pieceName()+" at "+r+", "+c);
					if (lastPiece[0]<0 || lastPiece[1]<0) // If there was not a last piece
					{
						if (whiteTurn&&Board.pieces[r][c].pieceColor.equals(Color.BLACK)||
							!whiteTurn&&Board.pieces[r][c].pieceColor.equals(Color.WHITE))	// If it is not their turn
						{
							// Print debug message
							System.out.println("Can not move; wrong turn");
							return true;
						}
						if (!Board.pieces[r][c].pieceName().equals("empty tile")) // If this piece is real
						{
							// Replace border color with green.
							Board.pieces[r][c].borderColor=Color.GREEN;
							// Set the last piece's coordinates to this one's
							lastPiece[0]=r;
							lastPiece[1]=c;
						}
					}
					else if (lastPiece[0]==r&&lastPiece[1]==c) // If the square clicked on is the last square
					{
						// Reset border color and last piece coordinates
						Board.pieces[r][c].setBorderColor();
						lastPiece[0]=lastPiece[1]=-1;
					}
					else // If there is a last square and the piece clicked is not the last square
					{
						// Swap pieces:
							// Reset border color on last clicked piece
						Board.pieces[lastPiece[0]][lastPiece[1]].setBorderColor();
						// Stop if the move is illegal
						if (!Board.pieces[lastPiece[0]][lastPiece[1]].moveIsLegal(r,c))
						{
							// Print debug message
							System.out.println("Move not allowed!");
							repaint();
							// Reset last clicked piece coordinates to -1, -1
							lastPiece[0]=lastPiece[1]=-1;
							// Stop
							return true;
						}
						// Now we know the move is legal; we can move the piece:
							// Print a debug message about capturing
						if (!Board.pieces[r][c].pieceName().equals("empty tile"))
							System.out.println("A "+Board.pieces[lastPiece[0]][lastPiece[1]].pieceName()+
							" from "+(char)(97+lastPiece[0])+(8-lastPiece[1])+" captures the "+Board.pieces[r][c].pieceName()+" on "+(char)(97+r)+(8-c));
						else // or moving
							System.out.println("A "+Board.pieces[lastPiece[0]][lastPiece[1]].pieceName()+" moved from "+
									(char)(97+lastPiece[0])+(8-lastPiece[1])+" to "+(char)(97+r)+(8-c));
						// Back up the captured tile
						lastCapturedPiece=(Piece)Board.pieces[r][c].clone();
						// Physically relocate the piece
						movePiece(lastPiece[0],lastPiece[1],r,c);
						// If the king of this turn's color is now in check, revert the move.
						if (whiteTurn&&kingInCheck(Color.WHITE)||!whiteTurn&&kingInCheck(Color.BLACK))
						{
							// Print debug message
							System.out.println("Can not move, king would be in check!");
							// Undo the move
							movePiece(r,c,lastPiece[0],lastPiece[1]); // lastPiece[] and r/c are swapped
								// Set the tile moved to back to before it was captured
							Board.pieces[r][c]=(Piece)lastCapturedPiece.clone();
								// Recalculate legal moves again
							Board.calculateLegalMoves();
						}
						else
						{
							// Record that the piece has moved
							Board.pieces[r][c].hasMoved=true;
							// Toggle turn boolean to allow other player's move
							whiteTurn=!whiteTurn;
						}
						// Detect pawn promotion
						if (Board.pieces[r][c].pieceName().indexOf("pawn")!=-1) // Pawn-specific functions
						{
							// En passant: If the pawn performed an en-passant, remove the now-captured piece
							if (lastPiece[0]-r==1&&Board.pieces[lastPiece[0]-1][lastPiece[1]].enPassant)
								Board.pieces[lastPiece[0]-1][lastPiece[1]]=new Piece(lastPiece[0]-1,lastPiece[1]);
							else if (r-lastPiece[0]==1&&Board.pieces[lastPiece[0]-1][lastPiece[1]].enPassant)
								Board.pieces[lastPiece[0]+1][lastPiece[1]]=new Piece(lastPiece[0]+1,lastPiece[1]);
							if (c>6||c<1) // Promotion: If the piece lies on the final ranks
							{
								// Set proper booleans
								Board.pieces[r][c].canPromote=true;
								pawnCanPromote=true;
								// Set turn back to wait for promotion choice
								whiteTurn=!whiteTurn;
							}
							// Else because it should never be able to do more than one, but either order should work here
							else if (Math.abs(lastPiece[1]-c)==2) // If the piece just moved 2 spaces
							{
								// Allow it to be en-passant'ed
								Board.pieces[r][c].enPassant=true;
							}
						}
						if (Board.pieces[r][c].pieceName().indexOf("king")!=-1) // King-specific functions (castling)
						{
							if (r-lastPiece[0]==2) // If the king castled to the right
							{
								Board.pieces[5][c]=(Piece)Board.pieces[7][c].clone();
								Board.pieces[5][c].setLocation(5,c);
								Board.pieces[7][c]=new Piece(7,c);
							}
							if (lastPiece[0]-r==2) // If the king castled to the left
							{
								Board.pieces[3][c]=(Piece)Board.pieces[0][c].clone();
								Board.pieces[3][c].setLocation(3,c);
								Board.pieces[0][c]=new Piece(0,c);
							}

						}
						// Reset last clicked piece coordinates
						lastPiece[0]=lastPiece[1]=-1;
						// Set hasMoved to true (castling legality)
						Board.pieces[r][c].hasMoved=true;
						repaint();
					}
				}
				else // If the square is not clicked on
				{
					// Make sure the border color is correct.
					Board.pieces[r][c].setBorderColor();				
				}
			}
		repaint(); // Redraw the board to make changes appear.
		return true;
	}

	// movePiece and kingInCheck were made separate to allow for checkmate detection
	public static void movePiece(int x1,int y1,int x2,int y2)
	{
		// Move the piece (obviously)
		Board.pieces[x2][y2]=(Piece)Board.pieces[x1][y1].clone();
			// Move old piece "physically" for new click detection parameters
		Board.pieces[x2][y2].setLocation(x2,y2);
			// Turn the piece's old tile into an empty tile
		Board.pieces[x1][y1]=new Piece(x1,y1);
			// Recalculate the legal moves for check detection
		Board.calculateLegalMoves();
	}

	public static boolean kingInCheck(Color c)
	{
		// Returns whether or not the king of a chosen color (c) is attacked by the opponent
		if (c.equals(Color.WHITE))
			return Board.pieces[whiteKingCoords[0]][whiteKingCoords[1]].isAttacked(Color.BLACK);
		if (c.equals(Color.BLACK))
			return Board.pieces[blackKingCoords[0]][blackKingCoords[1]].isAttacked(Color.WHITE);
		else return false;
	}
	
	public static int checkmate(Color p) // Returns 0 if no winner, 1 if checkmate, and 2 if stalemate
	{
		int mate=2;
		if (kingInCheck(p))
			mate=1;
		for (int r=0;r<8;r++) // For every piece on the board
			for (int c=0;c<8;c++)
				if (Board.pieces[r][c].pieceColor.equals(p)) // If it is on the same side as p,
					for (int[] m : Board.pieces[r][c].legalMoveCache) // for all of its legal moves,
					{
						// make the move,
						lastCapturedPiece=(Piece)Board.pieces[m[0]][m[1]].clone();
						movePiece(r,c,m[0],m[1]);
						Board.calculateLegalMoves();
						// check if the king is still in check,
						if (!kingInCheck(p))
							mate=0;
						// and move the piece back.
						movePiece(m[0],m[1],r,c);
						Board.pieces[m[0]][m[1]]=(Piece)lastCapturedPiece.clone();
						Board.calculateLegalMoves();
					}
		if (mate==1&&!whiteTurn)
			win="White wins!";
		else if (mate==1&&whiteTurn)
			win="Black wins!";
		return mate;
	}

	public void drawTurnDisplay(Graphics g)
	{
		g.setFont(new Font("Monospaced",Font.BOLD,15));
		g.setColor(Color.BLACK);
		g.drawString("Turn:",600,20);
		g.setFont(new Font("Monospaced",Font.BOLD,60));
		if (whiteTurn)
			g.drawString("White",600,70);
		else
			g.drawString("Black",600,70);
	}
	
	public void drawResetButton(Graphics g)
	{
		g.setFont(new Font("Monospaced",Font.BOLD,60));
		g.setColor(Color.RED);
		g.drawRect(601,525,198,74);
		g.drawString("Reset",610,580);
	}
	
	public void drawPromotionList(Graphics g,Color c)
	{
		g.setColor(Color.GRAY);
		g.drawRect(600,225,150,150);
		uiQueen = new Queen(8,3,c);
		uiQueen.drawPiece(g);
		uiKnight = new Knight(8,4,c);
		uiKnight.drawPiece(g);
		uiRook = new Rook(9,3,c);
		uiRook.drawPiece(g);
		uiBishop = new Bishop(9,4,c);
		uiBishop.drawPiece(g);
	}
}

class Board
{
	public static Piece[][] pieces=new Piece[8][8];

	public static void initPieces(Graphics g)
	{
		Color nextColor=Color.GREEN; // Precaution to avoid NullPointerException
		for (int r=0; r<8; r++)
			for (int c=0; c<8; c++)
			{
				pieces[r][c]=new Piece(r,c);
				// If the piece is on one of the four home rows, make it a pawn
				// This is so that the color of the piece is already set properly
				// and pawns do not need to be re-made
				if (c<2)
				{
					nextColor=Color.BLACK;
					pieces[r][c]=new Pawn(r,c,nextColor);
				}
				if (c>5)
				{
					nextColor=Color.WHITE;
					pieces[r][c]=new Pawn(r,c,nextColor);
				}
				// If the square does not start off as a pawn, set it based on its column
				if (c==0 || c==7)
					switch (r)
					{
						case 0: pieces[r][c]=new Rook(r,c,nextColor);
								break;
						case 1: pieces[r][c]=new Knight(r,c,nextColor);
								break;
						case 2: pieces[r][c]=new Bishop(r,c,nextColor);
								break;
						case 3: pieces[r][c]=new Queen(r,c,nextColor);
								break;
						case 4: pieces[r][c]=new King(r,c,nextColor);
								break;
						case 5: pieces[r][c]=new Bishop(r,c,nextColor);
								break;
						case 6: pieces[r][c]=new Knight(r,c,nextColor);
								break;
						case 7: pieces[r][c]=new Rook(r,c,nextColor);
								break;
					}
			}
		Chess.lastPiece[0]=Chess.lastPiece[1]=-1;
	}

	public static void drawPieces(Graphics g)
	{	// Standard 2-dimensional matrix nested for-loop
		for (int r=0; r<8; r++)
			for (int c=0; c<8; c++)
			{
				pieces[r][c].drawPiece(g);
				if (pieces[r][c].borderColor.equals(Color.GREEN))
					pieces[r][c].drawLegalMoves(g);
			}
	}

	public static void calculateLegalMoves()
	{ // Standard 2-dimensional matrix nested for-loop
		for (int r=0; r<8; r++)
			for (int c=0;c<8;c++)
			{
				if (pieces[r][c].pieceName().indexOf("king")!=-1) // Detect kings for coordinate storage
				{
					if (pieces[r][c].pieceColor.equals(Color.WHITE))
					{
						Chess.whiteKingCoords[0]=r;
						Chess.whiteKingCoords[1]=c;
					}
					if (pieces[r][c].pieceColor.equals(Color.BLACK))
					{
						Chess.blackKingCoords[0]=r;
						Chess.blackKingCoords[1]=c;
					}	
				}
				pieces[r][c].buildLegalMoveCache();
			}
	}
	
	public static int canCastle(Color c)
	{
		int result=0;
		if (c.equals(Color.WHITE))
		{
			if (!Board.pieces[Chess.whiteKingCoords[0]][Chess.whiteKingCoords[1]].hasMoved) // If the king has not moved
			{
				// Check to the right
				if (Board.pieces[5][7].pieceName().equals("empty tile")&&
					Board.pieces[6][7].pieceName().equals("empty tile")&&
					!Board.pieces[7][7].hasMoved)
						result+=1;
				// Check to the left
				if (Board.pieces[3][7].pieceName().equals("empty tile")&&
					Board.pieces[2][7].pieceName().equals("empty tile")&&
					Board.pieces[1][7].pieceName().equals("empty tile")&&
					!Board.pieces[0][7].hasMoved)
						result+=2;
			}
		}
		else if (c.equals(Color.BLACK))
		{
			if (!Board.pieces[Chess.blackKingCoords[0]][Chess.blackKingCoords[1]].hasMoved) // If the king has not moved
			{
				// Check to the right
				if (Board.pieces[5][0].pieceName().equals("empty tile")&&
					Board.pieces[6][0].pieceName().equals("empty tile")&&
					!Board.pieces[7][0].hasMoved)
						result+=1;
				// Check to the left
				if (Board.pieces[3][0].pieceName().equals("empty tile")&&
					Board.pieces[2][0].pieceName().equals("empty tile")&&
					Board.pieces[1][0].pieceName().equals("empty tile")&&
					!Board.pieces[0][0].hasMoved)
						result+=2;
			}
		}
		return result;
	}
}