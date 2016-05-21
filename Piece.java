// Mark Johnson
// AP Computer Science final project
// 2016

import java.awt.*;
import java.util.ArrayList;

public class Piece extends Rectangle
{
	private final Font pieceFont=new Font("Monospaced",Font.BOLD,75);
	public Color pieceColor,borderColor; // Public so other pieces can check before capture
	public int x,y; // Public for checking attacking
	public int xPos,yPos; // Public for drawing legal move circles
	private String pieceChar;
	public ArrayList<int[]> legalMoveCache=new ArrayList<int[]>();
	public boolean enPassant=false,canPromote=false; // Pawn promotion
	public boolean canCastle=false,hasMoved=false; // Castling
	
	public Piece(int xIn,int yIn,Color c,String s)
	{
		// Create rectangle object for click detection
		super((xIn*75),(yIn*75),75,75);
		setLocation(xIn,yIn);
		// Set piece information
		pieceChar=s;
		pieceColor=c;
		// Set border color
		setBorderColor();
		// Print debug message
		//System.out.println("New "+pieceName()+" at "+x+", "+y); // Usually disabled because of log spam
	}

	public Piece(int xIn,int yIn) // Short way to instantiate nonexistant pieces
	{
		this(xIn,yIn,Color.GREEN,""); // Green to avoid NullPointerException and for other reasons
	}
	
	public void setLocation(int xIn,int yIn)
	{
		// Set coordinates and real character position
		x=xIn;
		y=yIn;
		super.x=xIn*75;
		super.y=yIn*75;
		// Center pieces in tiles (currently optimized for Windows font)
		xPos=x*75-2;
		yPos=(y*75)+64;
	}

	public String pieceName() // Only for printing debug messages
	{
		// Instantiate string for concatenation
		String result="";
		// Check colors for binarism, and state if so
		if (pieceColor.equals(Color.BLACK))
			result += "black ";
		else if (pieceColor.equals(Color.WHITE))
			result += "white ";
		// Check piece by character
		if (pieceChar.equals(Chess.PAWN))
			result += "pawn";
		else if (pieceChar.equals(Chess.KING))
			result += "king";
		else if (pieceChar.equals(Chess.QUEEN))
			result += "queen";
		else if (pieceChar.equals(Chess.ROOK))
			result += "rook";
		else if (pieceChar.equals(Chess.KNIGHT))
			result += "knight";
		else if (pieceChar.equals(Chess.BISHOP))
			result += "bishop";
		else result="unknown "+result+"piece";
		// If color is non-binary, state so.
		if (!pieceColor.equals(Color.WHITE)&&!pieceColor.equals(Color.BLACK)) // "If neither white nor black"
			result += " of non-binary color";
		// If tile is empty, replace safe answer ("unknown piece of non-binary color") with accurate answer
		if (pieceChar.equals(""))
			result="empty tile";
		// Return concatenated answer
		return result;

	}

	public void drawPiece(Graphics g)
	{
		// Draw outline of piece for visibility (with 1px shift in 8 directions, maybe only 4 is necessary?)
		// Change all 1's to other numbers for border thickness
		g.setColor(borderColor);
		g.setFont(pieceFont);
			// Up, down, left, & right
		g.drawString(pieceChar,xPos+1,yPos);
		g.drawString(pieceChar,xPos,yPos+1);
		g.drawString(pieceChar,xPos-1,yPos);
		g.drawString(pieceChar,xPos,yPos-1);
			// Diagonals
		g.drawString(pieceChar,xPos+1,yPos+1);
		g.drawString(pieceChar,xPos-1,yPos-1);
		g.drawString(pieceChar,xPos+1,yPos-1);
		g.drawString(pieceChar,xPos-1,yPos+1);
		// Switch to piece color and draw actual piece
		g.setColor(pieceColor);
		g.setFont(pieceFont);
		g.drawString(pieceChar,xPos,yPos);
	}
	
	public boolean moveIsLegal(int x,int y)
	{
		for (int[] a : legalMoveCache) // For all legal moves,
			if (a[0]==x&&a[1]==y) // if x, y is one of them,
				return true; // return true.
		return false; // Otherwise, return false.
	}

	protected void buildLegalMoveCache()
	{
		// If the piece is generic or nonexistant, it should not have any moves.
	}

	protected boolean addToLegalMoveCache(int x,int y) // Returns false if there is a piece on x, y
	{
		if (Board.pieces[x][y].pieceColor.equals(pieceColor)) // If the piece on the spot is the same color
			return false; // tell it to stop immediately.
		int[] tmp={x,y};
		legalMoveCache.add(tmp); // If not, add the move to the list of legal moves
		if (Board.pieces[x][y].pieceColor.equals(Piece.oppositeColor(pieceColor))) // If the piece is opposite color
			return false; // tell it to stop
		return true; // Otherwise, keep going.
	}

	public void drawLegalMoves(Graphics g)
	{
		// Set the color to a neutral one visible on all tiles
		g.setColor(Color.GRAY);
		// For every legal move
		for (int[] a : legalMoveCache)
			// Draw an oval on that tile
			g.drawOval(Board.pieces[a[0]][a[1]].xPos+2,Board.pieces[a[0]][a[1]].yPos-64,73,73);
	}

	public boolean isAttacked(Color o)
	{
		// Standard 2-dimensional matrix nested for-loop
		for (int r=0;r<8;r++)
			for (int c=0;c<8;c++)
			{
				// If there is a piece and it is the opponent's,
				if (Board.pieces[r][c].pieceColor.equals(o) && !Board.pieces[r][c].pieceName().equals("empty tile"))
				{
					if (Board.pieces[r][c].pieceName().indexOf("pawn")==-1) // for all non-pawns,
						for (int[] a : Board.pieces[r][c].legalMoveCache) // for every legal move,
							if (a[0]==x&&a[1]==y) // if x, y is that tile,
								return true; // return true.
					// An extra section is required because pawns can move forward, but not capture forward, so we can not assume that all legal moves can capture.
					if (Board.pieces[r][c].pieceName().indexOf("pawn")!=-1) // For all pawns,
						for (int[] b : Board.pieces[r][c].legalMoveCache) // for every legal move,
							if (b[0]==x&&b[1]==y&&r!=x) // if x, y is that tile and the pawn is not on the same column
								return true; // return true.
				}
			}
		return false; // If no capturing moves land on that square, return false.
	}

	public void setBorderColor()
	{
		// Border color is set to opposite right now, but this is subject to change
		borderColor=oppositeColor(pieceColor);
	}

	public static Color oppositeColor(Color c)
	{
		// Return a color with red, green, and blue values opposite those of c
		return new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue());
	}
}

class Pawn extends Piece
{
	public Pawn(int x,int y,Color c)
	{
		// Create a piece object with the pawn character
		super(x,y,c,Chess.PAWN);
	}

	public boolean addToLegalMoveCacheIfClear(int x,int y) // For forward movement needs
	{
		if (!Board.pieces[x][y].pieceName().equals("empty tile"))
			return false;
		int[] tmp={x,y};
		legalMoveCache.add(tmp);
		return true;
	}
	
	public void buildLegalMoveCache()
	{
		// Clear current legal move cache
		legalMoveCache = new ArrayList<int[]>();
		// Act based on pawn color
		if (pieceColor.equals(Color.WHITE))
		{
			if (y==0)
			{
				// Do nothing.
			}
			else
			{
				if (enPassant&&Chess.whiteTurn)
					enPassant=false;
				// Add the space in front to the legal move cache
				if (addToLegalMoveCacheIfClear(x,y-1)&&y==6) // Add both if both are clear
					// That takes advantage of the fact that the first statement is always evaluated.
					addToLegalMoveCacheIfClear(x,y-2);
				// Check diagonals for capturing
				if (x<7&&Board.pieces[x+1][y-1].pieceColor.equals(Color.BLACK))
					addToLegalMoveCache(x+1,y-1);
				if (x>0&&Board.pieces[x-1][y-1].pieceColor.equals(Color.BLACK))
					addToLegalMoveCache(x-1,y-1);
				if (x>0&&Board.pieces[x-1][y].pieceColor.equals(Color.BLACK)&&Board.pieces[x-1][y].enPassant)
					addToLegalMoveCache(x-1,y-1);
				if (x<7&&Board.pieces[x+1][y].pieceColor.equals(Color.BLACK)&&Board.pieces[x+1][y].enPassant)
					addToLegalMoveCache(x+1,y-1);
			}
		}
		else if (pieceColor.equals(Color.BLACK)) // Really, though, just `else` should work here.
		{
			if (y==7)
			{
				// Do nothing.
			}
			else
			{
				if (enPassant&&!Chess.whiteTurn)
					enPassant=false;
				// Add the space in front to the legal move cache
				if (addToLegalMoveCacheIfClear(x,y+1)&&y==1) // Add both if both are clear
					// That takes advantage of the fact that the first statement is always evaluated.
					addToLegalMoveCacheIfClear(x,y+2);
				// Check diagonals for capturing
				if (x<7&&Board.pieces[x+1][y+1].pieceColor.equals(Color.WHITE))
					addToLegalMoveCache(x+1,y+1);
				if (x>0&&Board.pieces[x-1][y+1].pieceColor.equals(Color.WHITE))
					addToLegalMoveCache(x-1,y+1);
				if (x>0&&Board.pieces[x-1][y].pieceColor.equals(Color.WHITE)&&Board.pieces[x-1][y].enPassant)
					addToLegalMoveCache(x-1,y+1);
				if (x<7&&Board.pieces[x+1][y].pieceColor.equals(Color.WHITE)&&Board.pieces[x+1][y].enPassant)
					addToLegalMoveCache(x+1,y+1);
			}
		}
	}
}

class Rook extends Piece
{
	public Rook (int x,int y,Color c)
	{
		// Create a piece object with the rook character
		super(x,y,c,Chess.ROOK);
	}

	public void buildLegalMoveCache()
	{
		// Clear current legal move cache
		legalMoveCache = new ArrayList<int[]>();
		/* For all four directions:
		 Add the next horizontal or vertical space until the edge of the board is reached
		 or there is a piece in the way. */
		for (int i=x+1;i<8;i++)
			if (!addToLegalMoveCache(i,y))
				break;
		for (int i=x-1;i>-1;i--)
			if (!addToLegalMoveCache(i,y))
				break;
		for (int i=y+1;i<8;i++)
			if (!addToLegalMoveCache(x,i))
				break;
		for (int i=y-1;i>-1;i--)
			if (!addToLegalMoveCache(x,i))
				break;
	}
}

class Bishop extends Piece
{
	public Bishop(int x,int y,Color c)
	{
		// Create a piece object with the bishop character
		super(x,y,c,Chess.BISHOP);
	}
	
	public void buildLegalMoveCache()
	{
		// Clear current legal move cache
		legalMoveCache=new ArrayList<int[]>();
		/* For all four diagonal directions:
		 From one piece in that direction, try to add every tile
		 unless there is a piece there (see addToLegalMoveCache())
		 If not, go to next direction. */
		int c=x+1,r=y+1;
		while (c<8&&r<8&&addToLegalMoveCache(c,r))
			{c++; r++;}
		c=x+1; r=y-1;
		while (c<8&&r>-1&&addToLegalMoveCache(c,r))
			{c++; r--;}
		c=x-1; r=y+1;
		while (c>-1&&r<8&&addToLegalMoveCache(c,r))
			{c--; r++;}
		c=x-1; r=y-1;
		while (c>-1&&r>-1&&addToLegalMoveCache(c,r))
			{c--; r--;}
}
}

class Knight extends Piece
{
	public Knight (int x,int y,Color c)
	{
		// Create a piece object with the knight character
		super(x,y,c,Chess.KNIGHT);
	}

	public void buildLegalMoveCache()
	{
		// Clear current legal move cache
		legalMoveCache=new ArrayList<int[]>();
		// I must manually add these one by one
		//	Right
		if (x<6)
		{
			if (y>0)
				addToLegalMoveCache(x+2,y-1); // right 2, up 1
			if (y<7)
				addToLegalMoveCache(x+2,y+1); // right 2, down 1
		}
		//	Down
		if (y<6)
		{
			if (x<7)
				addToLegalMoveCache(x+1,y+2); // right 1, down 2
			if (x>0)
				addToLegalMoveCache(x-1,y+2); // left 1, down 2
		}
		//	Left
		if (x>1)
		{
			if (y<7)
				addToLegalMoveCache(x-2,y+1); // left 2, down 1
			if (y>0)
				addToLegalMoveCache(x-2,y-1); // left 2, up 1
		}
		//	Up
		if (y>1)
		{
			if (x>0)
				addToLegalMoveCache(x-1,y-2); // left 1, up 2
			if (x<7)
				addToLegalMoveCache(x+1,y-2); // right 1, up 2
		}
	}
}

class Queen extends Piece
{
	public Queen (int x,int y,Color c)
	{
		// Create a piece object with the queen character
		super(x,y,c,Chess.QUEEN);
	}

	public void buildLegalMoveCache()
	{
		// Clear current legal move cache
		legalMoveCache = new ArrayList<int[]>();
		/* Rook:
		 	For all four directions:
		 	Add the next horizontal or vertical space until the edge of the board is reached
		 	or there is a piece in the way. */
		for (int i=x+1;i<8;i++)
			if (!addToLegalMoveCache(i,y))
				break;
		for (int i=x-1;i>-1;i--)
			if (!addToLegalMoveCache(i,y))
				break;
		for (int i=y+1;i<8;i++)
			if (!addToLegalMoveCache(x,i))
				break;
		for (int i=y-1;i>-1;i--)
			if (!addToLegalMoveCache(x,i))
				break;
		/* Bishop:
			For all four diagonal directions:
			From one piece in that direction, try to add every tile
			unless there is a piece there (see addToLegalMoveCache())
			If not, go to next direction. */
		int c=x+1,r=y+1;
		while (c<8&&r<8&&addToLegalMoveCache(c,r))
			{c++; r++;}
		c=x+1; r=y-1;
		while (c<8&&r>-1&&addToLegalMoveCache(c,r))
			{c++; r--;}
		c=x-1; r=y+1;
		while (c>-1&&r<8&&addToLegalMoveCache(c,r))
			{c--; r++;}
		c=x-1; r=y-1;
		while (c>-1&&r>-1&&addToLegalMoveCache(c,r))
			{c--; r--;}
	}
}

class King extends Piece
{
	public King(int x,int y,Color c)
	{
		// Create a piece object with the king character
		super(x,y,c,Chess.KING);
	}

	public void buildLegalMoveCache()
	{
		// Clear current list of legal moves
		legalMoveCache = new ArrayList<int[]>();
		// I must manually add these one by one
		if (y>0)
		{
			addToLegalMoveCache(x,y-1);	// up center
			if (x<7)
				addToLegalMoveCache(x+1,y-1);	// up right
			if (x>0)
				addToLegalMoveCache(x-1,y-1);	// up left
		}
		if (x<7)
			addToLegalMoveCache(x+1,y);	// middle right
		if (x>0)
			addToLegalMoveCache(x-1,y);	// middle left
		if (y<7)
		{
			addToLegalMoveCache(x,y+1);	// down center
			if (x<7)
				addToLegalMoveCache(x+1,y+1);	// down right
			if (x>0)
				addToLegalMoveCache(x-1,y+1);	// down left
		}
		int castle=Board.canCastle(pieceColor);
		switch (castle)
		{
			case 0: break;
			case 1: addToLegalMoveCache(x+2,y); break;
			case 2: addToLegalMoveCache(x-2,y); break;
			case 3: addToLegalMoveCache(x+2,y);
					addToLegalMoveCache(x-2,y);
		}
	}
}