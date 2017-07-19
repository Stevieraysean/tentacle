inlets = 10;
outlets = 9; 	 // groove~ / record~ / LEDS

var pressCount = 0;  // multipress counter 
var pressOne = 0;	// press tracking
var pressTwo = 0;
var pressThree = 0;

var offset = 0;
var slices = 8; 		// Number of monome buttons in the loop
var length = 1;			// ms length of recording
var sig = 1.;			// speed
var pitch = 1.;			// speed
var channel = 0;

var playing = 0;
var recording = 0;
var stopped = 1;
// channel, type, value, current
// post("received list " + a + "\n"); // TO MAX WINDOW

function loadbang(){
outlet(1, "loop 1");
outlet(1, "stop"); 	// initialization commands for groove~
outlet(2, sig);
}
	
var d = new Date();

function msg_int(i)
{
	if (inlet == 1)
		channel = i;	
	if (inlet == 2)
		length = i;	
	if (inlet == 3)
		offset = i;	
	if (inlet == 4)
		slices = i;			
}

function msg_float(f)
{
	if ( inlet == 8 ){
		sig = f;
		outlet(2, sig);
	}
}

function list()
{
	var inp = arrayfromargs(arguments);
	input = inp;
	channel = input[0];
	outlet(5, channel);
		if(input[1] == "note"){
			if (input[3] > 0 && inlet == 0){
				pressCount++;
				}
			else if (input[3] == 0 && inlet == 0){
				if(input[2]-offset == pressOne || input[2]-offset == pressTwo){
				pressCount = 0;
				}
				}
			if (pressCount == 1){
				jump();
				}
			if (pressCount == 2){
				innerLoop();
				}	
			if (pressCount > 2){
				grainLoop();
				}
			
			if(recording)
				startloop();
			else if(stopped){
				play();
			}	
		}
		if(input[1] == "cc"){
			if(input[2]-offset == 0){
				if(input[3]>0){
					if(playing){
						stop();
					}
					else if(recording){
						startloop();
					}
					else if(stopped){
						record();
					}	
				}
			}
		}
}

// RECORD BUTTON FUNCTIONS //
function record(){
	d = new Date();
	outlet(0, 1)
	recording = 1;
	stopped = 0;
	buttonLED(127);
	outlet(6,channel+" note "+offset+" "+127);
}
function startloop(){
	outlet(0, 0);
	length = new Date().getTime()-d.getTime();
	loop(0, length);
	loopLED(offset,slices+offset);
	//outlet(1, "startloop");
	outlet(7, length);
	recording = 0;
	playing = 1;
	buttonLED(127);
}
function play(){
	playing = 1;
	recording = 0;
	stopped = 0;
	buttonLED(127);
}
function stop(){
	outlet(1, "stop");
	playing = 0;
	stopped = 1;
	buttonLED(0);
}

// PLAYBACK FUNCTIONS //
function jump()
{
	pressOne = input[2]-offset;
	outlet(7, bang);
	position(pressOne);
	loop(0, length);
	loopLED(offset,slices+offset);
}

function innerLoop()
{
	pressTwo = input[2]-offset;
	loop((length/slices)*pressOne, (length/slices)*pressTwo);
	loopLED(pressOne+offset,pressTwo+offset);
}

function grainLoop()
{
	pressThree = input[2]-offset;
	loop( (length/slices)*pressOne, ((((length/slices)*pressTwo) - ((length/slices)*pressOne))/(pressThree+2))+(length/slices)*pressOne );
}

function position(p1){
	outlet(1, ( length / slices ) * p1);
}

function loop(p1, p2){
	if(p2== 0){
		outlet(1, "setloop "+p1+" "+length);
	}
	else{
		outlet(1, "setloop "+p1+" "+p2);
	}
}
function loopLED(p1, p2){
	outlet(3,p1);
	if(p2==0)
		outlet(4,slices);
	else
		outlet(4,p2);
	
}
function buttonLED(i){
	outlet(6,channel+" cc "+offset+" "+i);
}
