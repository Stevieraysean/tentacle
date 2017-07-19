/**
 * Tentacle Grid interface.
 * 
 * @author (Sean Pierce)
 * @version (b1.0)
 */
import com.cycling74.max.*;
import java.util.*;
import java.io.*;
//import com.cycling74.max.Atom;

public class grid extends MaxObject
{
	public gridObject[][][] grid;                       // [page][x][y]
	public gridObject[][] currentGridObject;            // currently selected grid object
	public gridObject[][] copiedPage;         
	public String[][] leds;         

	public HashMap<String, HashSet<gridObject>> notes; 
	public HashMap<String, HashSet<gridObject>> ccs;  
	public HashMap<String, HashSet<gridObject>> parameters;   

	public static HashSet<slider> slides;

	int monome; // 0-3 = 64, 128h, 128v, 256
	int numPages;
	int currentPage;
	int xSize;
	int ySize;
	int variBright;   //   0 or 1.
	
	public String ledPre;
	public String mapPre;
	
	String offset;
	int row;

	String pageLeds;

	int selectCount = 0;

	slider remove;
	String slideOut;

	MaxClock c;

	public grid()
	{
		xSize = 8;
		ySize = 8;
		numPages = 16;
		declareIO(13,4);
		notes = new HashMap<String, HashSet<gridObject>>();
		ccs = new HashMap<String, HashSet<gridObject>>();
		parameters = new HashMap<String, HashSet<gridObject>>();
		slides = new HashSet<slider>();
		currentPage = 0;

		c = new MaxClock(new Callback(this, "bang") );
		c.delay(100.0);
	}

	public void inlet(int i){
		int inlet = getInlet();

		if(inlet == 3){
			setVariBright(i);
		}

		if(inlet==4){
			initialiseGrid(i);
		}
		if (inlet==5){
			changeType(i);
		}
		if(inlet==1){
			changePage(i);
		}
		if(inlet==9){
			if (i == 1){
				selectAll();
			}
			if (i == 2){
				copyPage();
			}
			if (i == 3){
				pastePage();
			}
		}

	}

	public void inlet(float f){		
		int inlet = getInlet();
		Atom[] list = {Atom.newAtom(inlet-3), Atom.newAtom(f)};
		if(inlet==7)
			setParams(list);
		if(inlet==8)
			setParams(list);
	}

	public void list(int[] list){   
		int inlet = getInlet();
		if(inlet==0){
			gridObject obj = grid[currentPage][list[0]][list[1]];
			outlet(1, obj.press(list));
			if(obj.dest == 0){
				Atom[] arr = {Atom.newAtom(obj.channel), Atom.newAtom(obj.mask), Atom.newAtom(obj.value),Atom.newAtom(obj.current) };
				midiFeedback(arr);
			}
		}

		if(inlet==2){					
			// GUI button SELECTOR *needs own method* //
			select(list);
		}
		if (inlet==6){
			setParams(Atom.newAtom(list));
			ledRefresh(0);
			ledDisplay(1);
		}

	}

	public void anything(String symbol,Atom[] list) {
		int inlet = getInlet();

		if(inlet == 10){
			saveGrid(symbol);
		}

		if(inlet == 11){
			if(symbol.equals("new"))
				initialiseGrid(monome);
			else
				loadGrid(symbol);
		}

	}

	public void bang(){
		if(!slides.isEmpty()){
			for(slider obj : slides){
				if(obj!=null){
					slideOut = obj.bang();
					if(slideOut!=null)
						outlet(1, slideOut);
					if(obj.dest==0){
						Atom[] arr = {Atom.newAtom(obj.channel), Atom.newAtom(obj.mask), Atom.newAtom(obj.value),Atom.newAtom(obj.current) };
						midiFeedback(arr);	
					}
				}
			}
			remove = null;
			for(slider obj : slides){
				if(obj.current==obj.target)
					remove = obj; 
			}
			if(remove!=null)
				slides.remove(remove);
		}
		c.delay(1.0);   // 10 ms polling..

	}
	public void notifyDeleted()
	{
		c.release();
	}

	public void select(int[] list){
		if(list[2]==1){ 

			gridObject selected = grid[currentPage][list[0]][list[1]];
			if(selected.type==1){
				for(int y = selected.y; y<=(selected.y+(selected.height-1)); y++){		///////
					for(int x = selected.x; x<=(selected.x+(selected.width-1)); x++){	///////
						currentGridObject[x][y] = grid[currentPage][x][y];
						selectCount++;
					}
				}
			}else{
				currentGridObject[list[0]][list[1]] = grid[currentPage][list[0]][list[1]];
				selectCount++;
			}



			//outlet (2, "print select count: "+selectCount);

			ledRefresh(1);
			ledDisplay(0);
			outlet (2, "object set "+grid[currentPage][list[0]][list[1]].type);
			outlet (2, "option1 clear, "+grid[currentPage][list[0]][list[1]].getOption1());
			outlet (2, "option2 clear, "+grid[currentPage][list[0]][list[1]].getOption2());
			outlet (2, "dest clear,"+grid[currentPage][list[0]][list[1]].getDests());
			outlet (2, "params "+grid[currentPage][list[0]][list[1]].getParams());

		}  
		if(list[2]==0){
			selectCount = 0;
			currentGridObject = new gridObject[xSize][ySize];
			outlet(2, "ledDisplay press clear");
			ledRefresh(0);
			ledDisplay(1);
		}
	}

	public void selectAll(){
		currentGridObject = grid[currentPage];
		ledRefresh(1);
		ledDisplay(0);
	}

	public void copyPage(){
		selectAll();
		copiedPage = currentGridObject;
		currentGridObject = new gridObject[xSize][ySize];
	}
	
	public void pastePage(){
		selectAll();
		grid[currentPage] = copiedPage;
	}
	
	public void changePage(int i){
		currentPage = i; 
		ledRefresh(0);
		ledDisplay(1);
	}

	public void midiFeedback(Atom[] list){
		int chan = list[0].getInt();
		String mask = list[1].getString();
		int value = list[2].getInt();

		String key = chan+" "+mask+" "+value; 

		if(mask.equals("note")){
			if(notes.containsKey(key)){
				for(gridObject obj : notes.get(key)){
					obj.current = list[3].getInt();
					//outlet(3, "print note feedback :"+list[3].getInt());
					if(obj.page==currentPage)
						led(obj);
				}
			}
		}
		else if(mask.equals("parameter")){
			if(parameters.containsKey(key)){
				for(gridObject obj : parameters.get(key)){
					if(!slides.contains(obj))
						if(list[3].isFloat())
							obj.currentF = list[3].getFloat();
					//outlet(3, "print parameter feedback :"+list[3].getFloat());
					if(obj.page==currentPage)
						led(obj);
				}
			}
		}

		else if(mask.equals("cc")){
			if(ccs.containsKey(key)){
				for(gridObject obj : ccs.get(key)){
					if(!slides.contains(obj))
						obj.current = list[3].getInt();
					//if(obj.page==currentPage)
					led(obj);
				}
			} 
		}
	}

	public void initialiseGrid(int i)
	{
		monome = i;
		switch(monome){
		case 0: xSize = 8; ySize = 8;
		break;
		case 1: xSize = 16; ySize = 8;
		break;
		case 2: xSize = 8; ySize = 16;
		break;
		case 3: xSize = 16; ySize = 16;
		break;
		}

		grid = new gridObject[numPages][xSize][ySize];         
		currentGridObject = new gridObject[xSize][ySize];
		copiedPage = new gridObject[xSize][ySize];
		leds = new String[xSize][ySize];
		
		notes = new HashMap<String, HashSet<gridObject>>();
		ccs = new HashMap<String, HashSet<gridObject>>();
		parameters = new HashMap<String, HashSet<gridObject>>();

		for(int p = 0; p<numPages; p++){
			for(int x = 0; x<xSize; x++){
				for(int y = 0; y<ySize; y++){       // Default grid notes

					gridObject newMidi = new midiButton("note",p,x,y,1,1,1,((x*5)-y+55),100,0,1,1,0,0,15,0,0,0,0);
					grid[p][x][y] = newMidi;

					String midiString = newMidi.channel+" "+newMidi.mask+" "+newMidi.value;
					HashSet<gridObject> set;
					set = new HashSet<gridObject>();
					if(notes.containsKey(midiString))
						set = notes.get(midiString);
					set.add(newMidi);
					notes.put(midiString, set);

				}
			} 
		}
		ledDisplay(1);
		setVariBright(0);
		ledRefresh(0);
	}

	public void changeType(int i){
		if(i==0){
			for(int y=0; y<ySize; y++){
				for(int x=0; x<xSize; x++){
					gridObject obj = currentGridObject[x][y];
					if(obj!=null){
						String midiString = obj.channel+" "+obj.mask+" "+obj.value;         // string

						if((obj.mask).equals("note")){
							notes.get(midiString).remove(obj);
						}
						if((obj.mask).equals("cc")){
							ccs.get(midiString).remove(obj); 
						} 
						if((obj.mask).equals("parameter")){
							parameters.get(midiString).remove(obj);
						}
						midiButton newButton = new midiButton("note",currentPage,x,y,1,1,1,((x*4)+y+50),100,0,1,1,0,0,15,0,0,0,0);
						grid[currentPage][x][y] = newButton;

						midiString = newButton.channel+" "+newButton.mask+" "+newButton.value;
						HashSet<gridObject> set;
						set = new HashSet<gridObject>();
						if(notes.containsKey(midiString))
							set = notes.get(midiString);
						set.add(newButton);
						notes.put(midiString, set);
					}
				}
			}
		}
		if(i==1){ 						// if changing to slider
			//outlet(2, "print Changing cells to slider");	

			int left = xSize;
			int right = 0;
			int top = ySize;
			int bottom = 0;
			for(int y=0; y<ySize; y++){
				for(int x=0; x<xSize; x++){
					gridObject obj = currentGridObject[x][y];
					if(obj!=null){
						if(obj.x>right)
							right = obj.x;
						if(obj.x<left)
							left = obj.x;
						if(obj.y>bottom)
							bottom = obj.y;
						if(obj.y<top)
							top = obj.y; 
					}
				}
			}
			//outlet(2, "print left "+left);	
			//outlet(2, "print right "+right);	
			//outlet(2, "print top"+top);	
			//outlet(2, "print bottom"+bottom);	

			int value = currentGridObject[left][top].value;
			int channel = currentGridObject[left][top].channel;
			slider newSlider = new slider("parameter", currentPage, left, top, right-left+1, bottom-top+1, channel, value, 127, 0, 1.f, 0.f, 100, 1000, 15, 0, 0, 0, 0 );
			//outlet(2, " created: "+newSlider.save());

			for(int y=top; y<=bottom; y++){
				for(int x=left; x<=right; x++){					
					gridObject obj = grid[currentPage][x][y];

					if(obj==null){
						//outlet(2, "print obj: obj is null");	
					}
     
					HashSet<gridObject> set = new HashSet<gridObject>();

					String midiString = obj.channel+" "+obj.mask+" "+obj.value;         // string

					if((obj.mask).equals("note")){
						set = notes.get(midiString);   
						//outlet(2, " obj: found in notes");	
					}
					if((obj.mask).equals("cc")){
						set = ccs.get(midiString); 
						//outlet(2, "print obj: found in CC's");	
					} 
					if((obj.mask).equals("parameter")){
						set = parameters.get(midiString);  
						//outlet(2, "print obj: found in parameters's");	
					}
					if(set.contains(obj)){
						set.remove(obj);   // remove from set  
						//outlet(2, "print obj: removed from set");	
					}
					grid[currentPage][x][y] = newSlider;

					midiString = newSlider.channel+" "+newSlider.mask+" "+newSlider.value;
					set = new HashSet<gridObject>();
					if(parameters.containsKey(midiString)){
						set = parameters.get(midiString);
						//outlet(2,"print added to parameters hashset");
					}
					set.add(newSlider);

					parameters.put(midiString, set);
					//outlet(2, "print changed to slider");	
				}
			}
			int[] reselect = {left, top, 0};
			select(reselect);
			reselect[2] = 1;
			select(reselect);
		}
		ledDisplay(0);
	}

	public void setParams(Atom[] list){      // get parameters for current grid button
		int param = list[0].getInt();
		int listLen = list.length;
		int listNum = 1;
		for(int y=0; y<ySize; y++){
			for(int x=0; x<xSize; x++){
				gridObject obj = currentGridObject[x][y];
				if(obj!=null){
					Atom val = list[listNum];	
					if(param==0 || param==1 || param==10){
						// if midi note or channel changes, object needs to be shifted in midi collection//			
						String midiString = obj.channel+" "+obj.mask+" "+obj.value;         // string
						HashSet<gridObject> set;
						set = new HashSet<gridObject>();

						int setType = 0;

						// FIND AND REMOVE FROM OLD SET //
						if((obj.mask).equals("note")){
							set = notes.get(midiString);
							//outlet(2, "print mask is note");	
						}
						if((obj.mask).equals("cc")){
							set = ccs.get(midiString); 
							//outlet(2, "print mask is cc");	
						} 
						if((obj.mask).equals("parameter")){
							set = parameters.get(midiString);  
							//outlet(2, "print mask is parameter");
						}
						if(set.contains(obj)){
							set.remove(obj);   // remove from set    
							//outlet(2, "print successfully removed from set");
						}
						// SET PARAMETER CHANGE //
						obj.setParam(param, val);                                             // change parameters
						midiString = obj.channel+" "+obj.mask+" "+obj.value;
						set = new HashSet<gridObject>();

						if((obj.mask).equals("parameter")){
							if(parameters.containsKey(midiString))
								set = parameters.get(midiString);                                       // get set of midi value
							setType = 0;
						}
						if((obj.mask).equals("cc")){
							if(ccs.containsKey(midiString))
								set = ccs.get(midiString);  
							setType = 1;
						}   
						if((obj.mask).equals("note")){
							if(notes.containsKey(midiString))
								set = notes.get(midiString);    
							setType = 2;
						} 
						set.add(obj);
						if(setType==0){
							parameters.put(midiString, set); 
							//outlet(2, "print added to parameters");

						}
						if(setType==1){
							ccs.put(midiString, set);    
							//outlet(2, "print added to ccs");
						}       
						if(setType==2){
							notes.put(midiString, set);       
							//outlet(2, "print added to notes");
						}   
					}
					else{
						obj.setParam(param, val);
					}
					if(listNum<listLen-1)
						listNum++;
					else if(listNum==listLen-1){
						listNum = 1;
					}
				}
			}
		}


	}  

	public void setVariBright(int i){
		variBright = i;
		if(i==0){
			ledPre = "/center/grid/led/set ";
			mapPre = "/center/grid/led/map ";
		}

		if(i==1){
			ledPre = "/center/grid/led/level/set ";
			mapPre = "/center/grid/led/level/map ";
		}    
	}

	public void led(gridObject obj){                                            
		String getObj = obj.channel+" "+obj.mask+" "+obj.value;
		if(obj.type==1){
			for(int y = obj.y; y<=(obj.y+(obj.height-1)); y++){		
				for(int x = obj.x; x<=(obj.x+(obj.width-1)); x++){
					if (leds[x][y]==null){
						outletHigh(0, ledPre+obj.led(x, y));
						leds[x][y] = obj.led(x,y);
						}
					else{
						if(obj.led(x,y).equals(leds[x][y])){}
						else{
							outletHigh(0, ledPre+obj.led(x, y));
							leds[x][y] = obj.led(x,y);
						}
					}
				}
			}
		}
		if(obj.type==0){
			if((obj.mask).equals("note")){
				if(notes.containsKey(getObj)){
					for(gridObject ob: notes.get(getObj)){
						if(ob.page==currentPage)
							if (leds[ob.x][ob.y]==null){
								if(obj.led().equals(leds[ob.x][ob.y])){
								}	
								else{
									outletHigh(0, ledPre+obj.led());
									leds[ob.x][ob.y] = obj.led();
								}
							}
							else{
								outletHigh(0, ledPre+obj.led());
								leds[ob.x][ob.y] = obj.led();
							}
					}
				}
			}
			else if((obj.mask).equals("cc")){
				if(ccs.containsKey(getObj)){
					for(gridObject ob: ccs.get(getObj)){
						if(ob.page==currentPage)
							if (leds[obj.x][obj.y]==null){
								if(obj.led().equals(leds[obj.x][obj.y])){
								}	
								else{
									outletHigh(0, ledPre+ob.led());
									leds[obj.x][obj.y] = obj.led();
								}
							}
							else{
								outletHigh(0, ledPre+obj.led());
								leds[ob.x][ob.y] = obj.led();
							}
					}
				}
			}
			else if((obj.mask).equals("parameter")){
				if(parameters.containsKey(getObj)){
					for(gridObject ob: parameters.get(getObj)){
						if(ob.page==currentPage)
							if (leds[obj.x][obj.y]==null){
								if(obj.led().equals(leds[obj.x][obj.y])){
								}	
								else{
									outletHigh(0, ledPre+ob.led());
									leds[obj.x][obj.y] = obj.led();
								}
							}
							else{
								outletHigh(0, ledPre+obj.led());
								leds[ob.x][ob.y] = obj.led();
							}
					}
				}
			}
		}
	}

	public void ledRefresh(int i){
		if(i==0){
			if(variBright==1){								// variable brightness
				for(int gridY = 0; gridY<ySize;){
					for(int gridX = 0; gridX<xSize;){
						pageLeds = "";
						for(int y = 0; y<8; y++){
							for(int x = 0; x<8; x++){
								pageLeds = pageLeds+" "+grid[currentPage][x+gridX][y+gridY].getLedLevel(x+gridX,y+gridY);
							}
						}
						outlet(0, mapPre+gridX+" "+gridY+" "+pageLeds);
						pageLeds="";
						gridX=gridX+8;
					}
					gridY=gridY+8;
				}
			}

			if(variBright==0){								// monochrome
				for(int gridY = 0; gridY<ySize;){
					for(int gridX = 0; gridX<xSize;){
						pageLeds = "";
						for(int y = 0; y<8; y++){
							row = 0;
							for(int x = 0; x<8; x++){
								if((grid[currentPage][x+gridX][y+gridY].getLedLevel(x+gridX,y+gridY)>0))
									row = row+((int)Math.pow(2, x));
							}
							pageLeds = pageLeds+" "+row;
						}
						outlet(0, mapPre+gridX+" "+gridY+" "+pageLeds);
						gridX=gridX+8;
					}
					gridY=gridY+8;
				}
			}  
		}

		if(i==1){
			outlet(0, "/center/grid/led/all 0");
			for(int y=0; y<ySize; y++){
				for(int x=0; x<xSize; x++){
					gridObject obj = currentGridObject[x][y];                
					if(obj!=null)
						outlet(0, "/center/grid/led/set "+obj.x+" "+obj.y+" 1");
				}
			}
		}
	}

	public void ledDisplay(int i){  // GUI LED Display
		if(i==0){											// selected objects
			outlet(2, "ledDisplay clear");
			for(int y=0; y<ySize; y++){
				for(int x=0; x<xSize; x++){
					gridObject obj = currentGridObject[x][y];
					if(obj!=null)
						outlet(2, "ledDisplay "+x+" "+y+" 16");
				}
			}
		}

		if(i==1){											// default editing view
			outlet(2, "ledDisplay clear");
			for(int y = 0; y<ySize; y++){
				for(int x = 0; x<xSize; x++){
					String out = ""; 
					if(grid[currentPage][x][y].type==0)
						out = grid[currentPage][x][y].led();
					if(grid[currentPage][x][y].type==1)
						out = grid[currentPage][x][y].led(x,y);
					if(!out.equals("null"))
						outlet(2, "ledDisplay "+out);
				}

			}
		}
	}    

	public void saveGrid(String filename){
		String fname = filename;
		try {
			PrintStream ps = new PrintStream(new File(fname));
			ps.println(monome);
			ps.println(variBright);
			for(int page = 0; page<numPages; page++){
				for(int y = 0; y < ySize; y++)
					for(int x = 0; x < xSize; x++)
						if(!grid[page][x][y].saved()){
							ps.println(grid[page][x][y].save());
							grid[page][x][y].saved(true);
						}
			}
			ps.close();
			for(int page = 0; page<numPages; page++){
				for(int y = 0; y < ySize; y++)
					for(int x = 0; x < xSize; x++)
						grid[page][x][y].saved(false);
			}
		}
		catch(IOException ex) {
			int info_idx = getInfoIdx();
			outlet(info_idx, "save failed");
		}
	}

	public void loadGrid(String filename){
		try {
			Scanner scan = new Scanner(new File(filename));
			initialiseGrid(scan.nextInt());
			outlet(2, "gridType "+monome);

			scan.nextLine();

			setVariBright(scan.nextInt());
			outlet (2, "varibright "+variBright);

			scan.nextLine();

			notes = new HashMap<String, HashSet<gridObject>>();
			ccs = new HashMap<String, HashSet<gridObject>>();
			parameters = new HashMap<String, HashSet<gridObject>>();

		    Thread t = new Thread();{
		    	while(scan.hasNextLine()){
					makeGridObject(scan.nextLine());
				}
				scan.close();
		    }
			
			t.start();
			}

			catch(IOException ex) {
				outlet(2,"print "+ex.getMessage());
			} 
			ledDisplay(1);
			ledRefresh(0);
	
	}
	public void makeGridObject(String desc){
		gridObject newObj = null;
		Scanner scan = new Scanner(desc);
		String type = scan.next();
		String mask = scan.next();
		int pg = scan.nextInt();
		int x = scan.nextInt();
		int y = scan.nextInt();
		int wdt = scan.nextInt();
		int hgt = scan.nextInt();
		int ch = scan.nextInt();
		int val = scan.nextInt();
		int mx = scan.nextInt();
		int mn = scan.nextInt();
		float mxF = scan.nextFloat();
		float mnF = scan.nextFloat();
		int spd = scan.nextInt();
		int fspd = scan.nextInt();
		int lH = scan.nextInt();
		int lL = scan.nextInt();
		int opt1 = scan.nextInt();
		int opt2 = scan.nextInt();  
		int destInt = scan.nextInt();

		if(type.equals("Button")){
			newObj = new midiButton(mask,pg,x,y,wdt,hgt,ch,val,mx,mn,mxF,mnF,spd,fspd,lH,lL,opt1,opt2,destInt);
			grid[newObj.page][newObj.x][newObj.y] = newObj;
		}
		if(type.equals("Slider")){
			newObj = new slider(mask,pg,x,y,wdt,hgt,ch,val,mx,mn,mxF,mnF,spd,fspd,lH,lL,opt1,opt2,destInt);
			for(int xS = newObj.x; xS<= newObj.x+newObj.width-1; xS++){
				for(int yS = newObj.y; yS <= newObj.y+newObj.height-1; yS++){
					grid[newObj.page][xS][yS] = newObj;
					//outlet(2, "print slider cell loaded "+xS+" "+yS);
				}  
			}
		}          

		String midiString = newObj.channel+" "+newObj.getMask()+" "+newObj.value;    
		HashSet<gridObject> set;
		set = new HashSet<gridObject>();
		if(newObj.mask.equals("note")){
			if(notes.containsKey(midiString))
				set = notes.get(midiString);
			set.add(newObj);
			notes.put(midiString, set);
		}
		else if(newObj.mask.equals("cc")){
			if(ccs.containsKey(midiString))   
				set = ccs.get(midiString);
			set.add(newObj);
			ccs.put(midiString, set);
		}
		else if(newObj.mask.equals("parameter")){
			if(parameters.containsKey(midiString))
				set = parameters.get(midiString);
			set.add(newObj);
			parameters.put(midiString, set);
		}

	}  

}  

  