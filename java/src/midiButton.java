/**
 * Tentacle Midi Button Object.
 * 
 * @author (Sean Pierce) 
 * @version (b1.0)
 */
public class midiButton extends gridObject
{	
	
    public midiButton(String mask, int pg, int xIn, int yIn, int wdt, int hgt, int chan, int val, int mx, int mn, double mxF, double mnF, int spd, int fspd,int lH, int lL, int opt1, int opt2, int d)
    {
        setMask(mask);
        
        this.page = pg;
        this.x = xIn;
        this.y = yIn;
        this.width = wdt;
        this.height = hgt;
        
        this.channel = chan;
        this.value = val;
        this.max = mx;
        this.min = mn;
        this.maxF = 0.5f;
        this.minF = 0.5f;
        this.current = 0;
        this.ledHigh = lH;
        this.ledLow = lL;
        this.option2 = opt2;
        this.option1 = 2;
        setDest(d);
        
        this.type = 0;
    }

    public String save(){       // return descriptive string;
        String desc ="Button "+mask+" "+page+" "+x+" "+y+" "+width+" "+height+" "+channel+" "+value+" "+max+" "+min+" "+maxF+" "+minF+" 0 0 "+ledHigh+" "+ledLow+" "+option1+" "+option2+" "+dest;
        saved = true;
        return desc;
    }

    public String getParams(){
        String params = channel+" "+value+" "+max+" "+min+" "+maxF+" "+minF+" "+" 0 0 "+ledHigh+" "+ledLow+" "+mask+" "+option2+" "+dest;
        return params;
    }

    public String getOption1(){
        String options1 = "append parameter, append cc, append note, append page ,set "+option1;
        return options1;
    }

    public String getOption2(){
        String options2 = "append momentary, append toggle, set "+option2;
        return options2;
    }

    public String getDests(){
        String dests = "append internal, append external, set "+dest;
        return dests;
    }

    public String press(int[] list){
        if(option1==0){ 		// parameter
        	
        }
        else if(option1==3){	// page button
        	if(list[2]==1)
        		return "page "+value;
        	else
        		return "release";
        }
        else {					// note or cc (midi formatted, sortof)
    	if(option2!=1){         //momentary
            if (list[2]==1){
                this.current = max;
            }
            else
                this.current = min;
            String midi = channel+" "+mask+" "+value+" "+current;;  
            return midi;
        }
        if(option2==1){         //toggle
            if(list[2]==1){
                if(this.current==max){
                    this.current = min;
                }
                else{
                    this.current = max;
                }
                String midi = channel+" "+mask+" "+value+" "+current;;  
                return midi;
            }
            else{
            	return "release";
            }
        }
        }
        return null;
    }

    public String led(){
    	/*if(this.option2 == 2){				// inverted.
    		if(this.current > min)
    			return x+" "+y+" "+ledLow;

    		else if(this.current == min)
    			return x+" "+y+" "+ledHigh;     
    	}  

    	if(this.option2 == 3)				// always off
    		return x+" "+y+" "+ledLow;

    	if(this.option2 == 4)				// always on
    		return x+" "+y+" "+ledHigh;   */

    	if(this.current > min)				// standard
    		return +x+" "+y+" "+ledHigh;

    	return x+" "+y+" "+ledLow;   
    }
    
    

    public int getLedLevel(int xIn, int yIn){
    	if(current == min)
    		return ledLow;
    	return ledHigh;
    }

}
