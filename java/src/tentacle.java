/*
 * Tentacle Environment Control Interface.
 * 
 * @author (Sean Pierce)
 * @version (b1.1)
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;


public class tentacle extends MaxObject
{
	public HashMap<String, module> environment;     // map of module.id's to module environment objects
	public HashSet<module> currentModule;                    // currently selected module object

	int mouseState;            					 	// 0 not / 1 clicked
	
	int fromX;
	int fromY;
	int toX;
	int toY;
	
	int envXSize = 880;         					// environment dimensions
	int envYSize = 700;
	int send = 1;
	boolean make;
	int alpha = 255;
			
	String audioColour;
	String midiColour;
	
	// PATCHING VARIABLES
	module from;
	module to;
	int out;
	int in;
	String type1;
	String type2;
	Atom function1;
	Atom function2;

	int index;

	public tentacle()
	{
		declareIO(11,1);
		initialiseEnv();
	}

	public void inlet(int i){
		int inlet = getInlet();
		if(inlet == 1){
			send = i;
		}
	}
	public void inlet(float d){
		int inlet = getInlet();
		if(inlet == 8){
			alpha = (int) (d*255);
		}
	}
   
	public void list(int[] list){   
		int inlet = getInlet();
		if(inlet == 1){ 
			if(mouseState ==1 && !currentModule.isEmpty()){
				for(module m: currentModule){
					
					outlet(0, m.move(list));
					
					for(module md: environment.values()){
						for(patch p :md.patches){
							if (p.getToName().equals(m.id)){
								p.updateToPosition(list);
							}
						}
					}
				}
				redraw();
			}
		} 
		if(inlet == 9){
			audioColour = list[0]+" "+list[1]+" "+list[2];
			print(audioColour);
		}
		if(inlet == 10){
			midiColour = list[0]+" "+list[1]+" "+list[2];
			print(midiColour);

		}
	}

	public void anything(String symbol, Atom[] args) {
		int inlet = getInlet();
		if (inlet == 0){
			if(symbol.equals("delete")){
				if(!currentModule.isEmpty())
					deleteModule(currentModule);
				else
					unpatch();
				return;
			}

			//if(symbol.equals("unpatch")){

			//}
		}

		if (inlet == 1){
			if(symbol.equals("click")){
				mouseState = 1;
			}
			if(symbol.equals("unclick")){
				mouseState = 0;
				currentModule = new HashSet<module>();
				redraw();
			}
			if(currentModule!=null){
				for(module m: currentModule){
					int[] moveList = {0,0};
					if(symbol.equals("left")){
						moveList[0] = -1;		
					}
					if(symbol.equals("right")){
						moveList[0] = +1;		
					}
					if(symbol.equals("up")){
						moveList[1] = -1;
					}
					if(symbol.equals("down")){
						moveList[1] = 1;		
					}
					outlet(0, m.move(moveList));
					
					for(module md: environment.values()){
						for(patch p :md.patches){
							if (p.getToName().equals(m.id)){
								p.updateToPosition(moveList);
							}
						}
					}
				}
				redraw();
			}
		}

		if(inlet == 2){
			make(symbol);
		}

		if(inlet == 3){
			currentModule.add(environment.get(symbol));
			redraw();
		}

		if(inlet == 4){
			saveEnv(symbol);
		}

		if(inlet == 5){
			if(symbol.equals("new"))
				initialiseEnv();
			else
				loadEnv(symbol);
		}
		// Patch modules together
		if(inlet == 6){
			setPatch1(symbol, args);
		}
		if(inlet == 7){
			setPatch2(symbol, args);
		}
	}

	// MODULE OBJECT METHODS //

	public void initialiseEnv(){
		outlet(0, "print InitialiseEnv");
		if(environment!=null){
			for(String obj: environment.keySet()){
				outlet(0, "thispatcher script delete "+obj);
			}
			environment.clear();
		}
		environment = new HashMap<String, module>();
		currentModule = new HashSet<module>();
		outlet(0, "preset clearall");
		outlet(0, "pattrstorage clear");

		redraw();
	} 

	public void setPatch1(String symbol ,Atom[] args){
		type1 = symbol;
		function1 = args[0];
		from = environment.get(args[1].toString());
		out = args[2].toInt();
		fromX = args[3].getInt();
		fromY = args[4].getInt();
		if(function2 != null)
			patch();
	}

	public void setPatch2(String symbol ,Atom[] args){
		type2 = symbol;
		function2 = args[0];
		to = environment.get(args[1].toString());
		in = args[2].toInt();
		toX = args[3].getInt();
		toY = args[4].getInt();
		if(function1 != null)
			patch();
	}

	public void patch(){
		if(type1.equals(type2) && !from.equals(to) && !function1.toString().equals(function2.toString())){
			HashSet<patch> outs = new HashSet<patch>();
			if(from.patches != null){
				outs = from.patches;
			}
			boolean patch = true;
			
			for(patch p: outs){
				//print("patch check to  : "+p.getFromName()+" "+p.getFromNumber()+" "+from.id+" "+out);  debugging..
				//print("patch check from: "+p.getToName()+" "+p.getToNumber()+" "+to.id+" "+in);
				if (p.getToName().equals(to.id) && p.getFromName().equals(from.id)){
					if(p.getToNumber() == in && p.getFromNumber() == out){
					patch = false;
					//print("match");
					}
				}
			}
			if(patch){
			print("patching"+from.id+" to "+to);
			outs.add(new patch(type1,from.id,out,fromX,fromY,to.id,in,toX,toY));
			outlet(0, "thispatcher script connect "+from.id+" "+out+" "+to.id+" "+in);
			}
		}
		clearPatch();
	}

	public void unpatch(){
		HashSet<patch> removes = new HashSet<patch>();
		for(patch  p : from.patches){
			if(p.getFromName().equals(from.id) && p.getFromNumber()==out){
				outlet(0, "thispatcher script disconnect "+from.id+" "+out+" "+p.getToName()+" "+p.getToNumber());
				removes.add(p);
			}
		}
		for(patch p: removes){
			from.patches.remove(p);
		}
		redraw();
		clearPatch();
	}

	public void clearPatch(){
		from = null;
		to = null;
		out = -1;
		in = -1;
		type1 = null;
		type2 = null;
		function1 = null;
		function2 = null;
		outlet(0, "setpatch 0");
	}

	public void redraw(){           // draw patch cords
		outlet(0, "lcd clear");
		for(module obj :environment.values()){

			if(!obj.patches.isEmpty()){
				for(patch p: obj.patches){
					if(p.getType().equals("audio")){
						outlet(0, "lcd frgb "+audioColour+" "+alpha);  // yellow
					}else if(p.getType().equals("midi")){
						outlet(0, "lcd frgb "+midiColour+ " "+alpha); 	// grey
						//outlet(0, "lcd frgb 80 80 80 "+alpha); 	// grey
					}

					try{
						outlet(0, "lcd linesegment "+p.getFromX()+" "
								+p.getFromY()+" "
								+p.getToX()+" "
								+p.getToY());
					}
					catch(Exception E){
						outlet(0, "print "+obj.id+" "+E.getMessage());
					}
				}
			}
		}
		/*
		if(currentModule!=null){
			outlet(0, "lcd frgb 255 255 100");
			for(module m: currentModule)
				outlet(0, "lcd framerect "+(m.x-2)+" "+(m.y-2)+" "+((m.x+m.width)+2)+" "+(m.y+m.height+2));
		}*/
	}

	public void make(String desc){     // make new module object
		Scanner scan = new Scanner(desc);

		String type = scan.next();
		String name = scan.next();

		int w = scan.nextInt();
		int h = scan.nextInt();
		int x = scan.nextInt();
		int y = scan.nextInt();

		HashSet<patch> outs = new HashSet<patch>();

		String line = scan.nextLine();

		index = 1;
		boolean loadPatches = false;
		if(line.length() > 5)
			loadPatches = true;

		while(index < line.length()-1 && loadPatches){

			String patchType = getNextWord(line);
			if(index == line.length()) break;

			String module1 = getNextWord(line);
			if(index == line.length()) break;

			int outlet = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;

			int fromX = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;

			int fromY = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;

			String module2 = getNextWord(line);
			if(index == line.length()) break;

			int inlet = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;

			int toX = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;

			int toY = Integer.parseInt(getNextWord(line)) ;
			if(index == line.length()) break;


			patch np = new patch(patchType, module1, outlet,fromX,fromY, module2, inlet,toX,toY);

			outs.add(np);
		}

		make = true;
		if(environment.containsKey(name)){
			outlet(0, "make::dialog bang");
			make = false;
		}
		if(make == true){
			module newObj = new module(type, name, w, h, x, y, outs);
			environment.put(name, newObj);
			outlet(0, newObj.struct);
			outlet(0,"thispatcher script sendbox "+newObj.id+" size "+newObj.width+" "+newObj.height);
			outlet(0, "thispatcher script sendbox "+newObj.id+" args "+newObj.id);
		}
	}

	public String getNextWord(String line){
		String word = "";
		while( line.charAt(index)!=']' && index < line.length()){
			if(line.charAt(index) == ','){
				index++;
			}

			if(line.charAt(index) == ' ' && word.length()==0){
				index++;
			}

			if(line.charAt(index) == '[' ){
				index++;
			}

			if (line.charAt(index) == ' ' && word.length()>0){
				break;
			}
			word = word+String.valueOf(line.charAt(index++));
		}
		line = line.substring(index);
		return word;
	}

	public void deleteModule(HashSet<module> modules){
		for(module obj : environment.values()){
			if(!modules.contains(obj)){
				HashSet<patch> removes = new HashSet<patch>();
				for(patch p: obj.patches){
					for(module m: modules){
						if(p.getToName().equals(m.id)){
							removes.add(p);
						}
					}
				}
				obj.patches.removeAll(removes);
			}
		}
		for(module m: modules){
			environment.remove(m.id);
			outlet(0, "thispatcher script delete "+m.id);
		}
		currentModule = new HashSet<module>();
		redraw();
	}

	public void saveEnv(String filename){
		String fname = filename;
		try {
			PrintStream ps = new PrintStream(new File(fname));
			for(module obj: environment.values())
				ps.println(obj.save());
			ps.close();
		}
		catch(IOException ex) {
			int info_idx = getInfoIdx();
			outlet(info_idx, "save failed");
		}
		outlet(0,"pattrstorage write \""+filename+"\"");
	}

	public void loadEnv(String filename){
		initialiseEnv();
		print("print loadEnv");

		//int info_idx = getInfoIdx();
		System.getProperty("user.dir");
		// outlet(info_idx,"success-initialised");
		try {
			Scanner scan = new Scanner(new File(filename));
			// outlet(info_idx, "success-scanner-made");
			while(scan.hasNextLine()){
				make(scan.nextLine());

			}
			scan.close();
		}
		catch(IOException ex) {
			print(ex.getMessage());
		} 
		// iterate over modules and make midi connections
		for(module module: environment.values()){
			for(patch p: module.patches){
				outlet(0, "thispatcher script connect "+p.getFromName()+" "+p.getFromNumber()+" "+p.getToName()+" "+p.getToNumber());
			}
		}
		
		outlet(0,"pattrstorage read \""+filename+".json\"");
		//outlet(0,"preset 1");
		redraw();

	}
	public void print(String s){
		outlet(0, "print "+s);
	}
}
