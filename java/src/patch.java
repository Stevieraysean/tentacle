
public class patch {
	private String type;
	private String from;
	private String to;

	private int fromNumber;
	private int toNumber;

	private int toX;
	private int toY;
	private int fromX;
	private int fromY;

	public patch(String type, String from, int fromNumber, int fromX, int fromY, String to, int toNumber, int toX, int toY){
		this.type = type;
		this.from = from;
		this.fromNumber = fromNumber;
		this.fromX = fromX;
		this.fromY = fromY;
		this.to = to;
		this.toNumber = toNumber;
		this.toX = toX;
		this.toY = toY;
	}

	public String getFromName(){
		return from;
	}

	public int getFromNumber(){
		return fromNumber;
	}

	public String getToName(){
		return to;
	}

	public int getToNumber(){
		return toNumber;
	}

	public String getType(){
		return type;
	}

	public int getFromX(){
		return fromX;
	}
	public int getFromY(){
		return fromY;
	}
	public int getToX(){
		return toX;
	}
	public int getToY(){
		return toY;
	}
	public void updateToPosition(int[] coords){
		toX += coords[0];
		toY += coords[1];
	}
	public void updateFromPosition(int[] coords){
		fromX += coords[0];
		fromY += coords[1];
	}
	
	public String toString(){
		return type+" "+from+" "+fromNumber+" "+fromX+" "+fromY+" "+to+" "+toNumber+" "+toX+" "+toY;
	}


}
