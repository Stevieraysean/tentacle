/**
 * Tentacle Grid Object.
 * 
 * @author (Sean Pierce)
 * @version (b1.0)
 */
import com.cycling74.max.*;

public abstract class gridObject
{
	int x;              //
	int y;              //  xy location of object on monome grid

	int channel;        //  channel. 
	
	int value;          //  midi note/cc value
	int current;        //  current note/cc value
	int min;			//  minimum value for current
	int max;			//  maximum value for current
	
	float maxF;         //  maximum float value
	float minF;			//  minimum float value
	float currentF;		//  current float value
	float thresh;		//  threshhold for led on

	int page;			//  obj page [DELETE - can work w/o this easily]
	int type;           //  integer shortcut button/fader
	int option1;        //  for objects with multiple modes. momentary / toggle etc..
	int option2;        //
	int ledHigh;		//  led value when high
	int ledLow;			//	led value when low
	
	public String mask; // parameter,cc,note,page

	int width;			// width for multi-cell objects
	int height;			// height for multi-cell objects
	
	int speed;			// normal slider speed (SINGLE press) 
	int fineSpeed;      // fine slider speed (DOUBLE press)
	
	int dest;		//  internal / external
	
	boolean saved = false;

	public gridObject(){
	} 

	public String press(int[] list){
		return null;
	}

	public String led(){                                        // returns ledstate
		return null;
	}
	public String led(int x, int y){                                        // returns ledstate
		return null;
	}

	public int getLedLevel(int x, int y){
		return 0;
	}

	public String getParams(){                                 //for editing the object in gui
		return null;
	}

	public String getOption1(){                                 //for editing the object in gui
		return null;
	}

	public String getOption2(){
		// return string formated for loading straight into max/msp umenu object.
		return null;
	}

	public String getDests(){
		return null;
	}

	public void setDest(int i){
	}

	public void setParam(int param, Atom i){
		if(param==0)
			this.channel = i.getInt();
		if(param==1)
			this.value = i.getInt();
		if(param==2){
			this.max = i.getInt();
			this.current = this.min;
		}
		if(param==3){
			this.min = i.getInt();
			this.current = this.min;
		}
		if(param==4){
			this.maxF = i.getFloat();
			this.currentF = this.minF;
		}
		if(param==5){
			this.minF = i.getFloat();
			this.currentF = this.minF;
			}
		if(param==6)
			this.speed = i.getInt();
		if(param==7)
			this.fineSpeed = i.getInt();
		if(param==8)
			this.ledHigh = i.getInt();
		if(param==9)
			this.ledLow = i.getInt(); 
		if(param==10){
			setMask(i.getInt());
			this.option1 = i.getInt();
		}
		if(param==11)
			this.option2 = i.getInt();
		if(param==12)
			this.dest = i.getInt();
		
		thresh = (((height * width - 1) * (maxF - minF))/ ((height * width) - 1 + minF) );
	}

	public void setOption(int value){ 
	}

	public void setMask(String msk){
		this.mask = msk; 
	}
	public void setMask(int msk){
		if(msk==0){
			this.mask = "parameter"; 
		}
		if(msk==1){
			this.mask = "cc";
			this.current = 0;
		}
		if(msk==2)
			this.mask = "note"; 
		if(msk==3)
			this.mask = "page"; 
	}
	public String getMask(){
		return mask;
	}

	public boolean saved(){
		return saved;
	} 
	public void saved(boolean set){
		saved = set;
	}
	
	public String save(){
		return null;
	}

}
