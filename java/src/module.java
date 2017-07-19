import java.util.HashSet;

/**
 * Abstract class audioObject - Object for handling the creation, moving, patching, 
 * loading and saving of audio audio objects within the tentacle environment
 * 
 * @author (Sean Pierce)
 * @version (b1.0)
 */
public class module{
                        // location and dimensions in the plane
     int x;
     int y;
     int width;
     int height;

     String file;
     String id;         // for receiving signals
     
     HashSet<patch> patches;
     String struct;     // contructor for thispatcher
     
     int value;
   
    public module(String type, String name, int w, int h, int xPos, int yPos, HashSet<patch> patches)
    {
        this.file = type;
    	this.id = name;
    	
        this.width = w;
        this.height = h;
        
        this.x = xPos;
        this.y = yPos;
        
        this.patches = new HashSet<patch>();
        this.patches = patches;
        
        this.struct = "thispatcher script newobject bpatcher "+file+".maxpat size "+width+" "+height+". @varname "+id+" @presentation 1 @border 1 @offset 0 0 @presentation_position "+x+" "+y+" args "+id;
    }

    public boolean on(int[] list){
        //if(list[0] > x && list[0] < x+40 && list[1] > y && list[1] < y+15)
        //    return true;
        return false;
    }

    public String move(int[] list){
        x = x+list[0];
        y = y+list[1];
        
        for(patch p: patches){
        	p.updateFromPosition(list);
        }
        
        String move = "thispatcher script sendbox "+id+" presentation_position "+x+" "+y;
        return move;
    }
    
    /*public String setSend(int send, String to){
    	if (send==1){
    		send1 = to;
        	return id+"::sends::send1 "+to;
    	}
    	else if (send==2){
    		send2 = to;
        	return id+"::sends::send2 "+to;

    	}
    	else if (send==3){
    		send3 = to;
        	return id+"::sends::send3 "+to;
    	}
    	return null;
    }*/

    public String save(){       // return descriptive string;
        String desc = file+" "+id+" "+width+" "+height+" "+x+" "+y+" "+patches;
        return desc;
    }
}

