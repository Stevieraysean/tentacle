/**
 * Tentacle Slider Object.
 * 
 * @author (Sean Pierce)
 * @version (b1.0)
 */
public class slider extends gridObject {
	int pressCount = 0;
	int currentSpeed = 200;

	int target;

	float targetF;
	float incF = 0;

	int tickCount = 0;
	int inc;
	String output;

	public slider(String mask, int pg, int xIn, int yIn, int wdt, int hgt, int chan, int val,
			int mx, int mn, float mxF, float mnF, int spd,
			int finespeed, int lH, int lL, int opt1, int opt2, int d) {
		setMask(mask);
		this.page = pg;

		this.x = xIn;
		this.y = yIn;
		this.width = wdt;
		this.height = hgt;

		this.channel = chan;
		this.value = val;
		this.max = mx;				// MIDI
		this.min = mn;
		this.maxF = mxF;			// PARAM
		this.minF = mnF;
		this.current = 0;
		this.ledHigh = lH;
		this.ledLow = lL;
		this.option1 = opt1;		// parameter / cc
		this.option2 = opt2;			// bar / line
		setDest(d);
		this.type = 1;
		this.speed = spd;
		this.fineSpeed = finespeed;
		saved = false;

	}

	public String save() { // return descriptive string;
		String desc = "Slider " + mask + " " + page + " " + x + " " + y
				+ " "+ width + " " + height + " " + channel + " " + value + " " + max + " " + min + " "
				+ maxF + " " + minF + " " + speed + " " + fineSpeed + " "
				+ ledHigh + " " + ledLow + " " + option1 + " " +option2 + " " + dest;
		return desc;
	}

	public String getParams() {
		String params = channel + " " + value + " " + max + " " + min + " "
				+ maxF + " " + minF + " " + speed + " " + fineSpeed + " "
				+ ledHigh + " " + ledLow + " " + mask + " " + option2 + " "
				+ dest;
		return params;
	}

	public String getOption1() {
		String options1 = "append parameter, append cc, set " + option1;
		return options1;
	}

	public String getOption2() {
		String options2 = "append bar, append dot, set " + option2;
		return options2;
	}

	public String getDests() {
		String dests = "append internal, append external, set " + dest;
		return dests;
	}


	public String press(int[] list) {
		if (option1 == 1) {  													// midi / integers 
			if (list[2] == 1) {
				pressCount++;
				if (pressCount == 1) {
					currentSpeed = speed;
					target = (int)((((height-1-list[1]+y)*width)+(width-list[0]-1+x))*((float)(max-min)/((width*height)-1))+min);
					//target = (((height * width - 1) - (width * (list[1] - y)) - (list[0] - x)) * (max - min)) / ((height * width) - 1 + min);
				}
				if (pressCount == 2) {
					currentSpeed = fineSpeed;
					if (target > (int)((((height-1-list[1]+y)*width)+(width-list[0]-1+x))*((float)(max-min)/((width*height)-1))+min)) { 
						target = min;
					} else
						target = max;
					if(grid.slides.contains(this))
						grid.slides.remove(this);
					grid.slides.add(this);
				}
			}
			if (list[2] == 0) {
				if (pressCount == 1) {
					grid.slides.add(this);
					pressCount = 0;
				}
				if (pressCount == 2) {
					target = current;
					pressCount = 0;
				}
				if (pressCount == 3) {
					pressCount = 0;
				}
			}
			return "null";

		} else if (option1 == 0) {												// parameters / floats
			if (list[2] == 1) {
				pressCount++;
				if (pressCount == 1) {
					currentSpeed = speed;
					targetF = ((((height-1-list[1]+y)*width)+(width-list[0]-1+x))*((maxF-minF)/((width*height)-1))+minF);
					//targetF = (((height * width - 1) - (width * (list[1] - y)) - (list[0] - x)) * (maxF - minF))/ ((height * width) - 1 + minF);
				}
				if (pressCount == 2) {
					currentSpeed = fineSpeed;
					if (targetF > ((((height-1-list[1]+y)*width)+(width-list[0]-1+x))*((maxF-minF)/((width*height)-1))+minF)) {
						targetF = minF;
					} else
						targetF = maxF;
					if(grid.slides.contains(this))
						grid.slides.remove(this);
					grid.slides.add(this);
				}
			}
			if (list[2] == 0) {
				if (pressCount == 1) {
					grid.slides.add(this);
					pressCount = 0;
				}
				if (pressCount == 2) {
					targetF = currentF;
					target = current;
					pressCount = 0;
					grid.slides.remove(this);

				}
				if (pressCount == 3) {
					pressCount = 0;
				}
			}
			current = 0;
			target = currentSpeed;
			incF = (targetF-currentF)/currentSpeed;
			return "null";

		}

		return null; 
	}

	public String bang() {
		tickCount++;
		output = null;
		if (option1 == 1) {										// midi / integers
			if (tickCount >= currentSpeed) {
				if (current < target) {
					inc = 1;
				} else if (current > target) {
					inc = -1;
				} else
					inc = 0;
				current = current + inc;
				output = channel + " cc " + value + " " + current;
				tickCount = 0;
			}
		}
		else if (option1 == 0) {										// parameters / floats
			if(current != target)	{
				current++;
				currentF = currentF+incF;
				if(currentF >= maxF){
					currentF = maxF;
					current = target;
				}
				if(currentF<= minF){
					currentF = minF;
					current = target;
				}
			}
			else{
				incF = 0.f;
				current = target;
			}
			tickCount = 0;
			output = channel+" "+mask+" "+value + " " + String.format("%.6f", currentF);
		}
		return output;
	}

	public String led() {					// unused, need coordinates to calculate 
		return null;
	}

	public String led(int xIn, int yIn) {
		return xIn+" "+yIn+" "+getLedLevel(xIn,yIn);     	
	}

	public int getLedLevel(int xIn, int yIn) {

		int currentLed = 0;
		int inputLed = ((height-(yIn-y)-1)*width)+(width-(xIn-x)-1);

		if(this.option1==0){  						// parameters
			
			currentLed = (int)(((currentF-minF)*width*height)/(maxF-minF+0.01));
			
			if(this.option2==0){  						// bar
				if(inputLed<=currentLed){
					return ledHigh;     
				}
				else{
					return ledLow;     
				}
			}
			if(this.option2==1){  						// dot
				if(inputLed==currentLed){
					return ledHigh;     
				}
				else{
					return ledLow;     
				}
			}
		}
		if(this.option1==1){  						// midi cc
			
			currentLed = (int)((current*width*height)/(max-min+1));

			if(this.option2==0){  						// bar
				if(inputLed<=currentLed){
					return ledHigh;     
				}
				else{
					return ledLow;     
				}
			}
			if(this.option2==1){  						// dot
				if(inputLed==currentLed){
					return ledHigh;     
				}
				else{
					return ledLow;     
				}
			}
		}

		return 0;
	}

}
