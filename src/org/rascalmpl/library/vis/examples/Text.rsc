module vis::examples::Text

import vis::Figure;
import vis::Render;
import Number;

import List;
import Set;
import IO;

// Text
public void txt1(){
	render(text("Een label"));
}

// Blue text of size 20
public void txt2(){
	render(text("A giant label", fontSize(20), fontColor("blue")));
}

// Unsized box with inner text
public void txt3(){
	render(box(text("A giant label", fontSize(20), fontColor("black")), gap(1)));
}

// Horizontal bottom-aligned composition of text of different size

public void txt4(){
	render(box(hcat([ text("Giant xyz 1", fontSize(20), fontColor("black")),
	 				  text("Giant xyz 2", fontSize(40), fontColor("blue")),
	 				  text("Giant xyz 3", fontSize(60), fontColor("red"))
	 			    ],
	 			   bottom()
	 			   ),
	 			gap(10)));
}

// Text rotated -90 degrees (counter clockwise)
public void txt5(){
	render(text("Een label", fontSize(20), textAngle(-90)));
}

// Vertical; composition of rotated text and a box

public void txt6(){
	render(vcat( [  box(width(100), height(200), fillColor("red")),
	                text("Een label", fontSize(20), textAngle(-90))
	             ],
	             center(), gap(10)));
}

// Overlay of box and text
//Note: the result is not pretty since the baseline of the text and the bottom of the box are aligned.
public void txt7(){
	render(overlay(
	              [box(width(150), height(200), fillColor("red")),
			       text( "Een label", fontSize(20))
			      ],
			      bottom(), right()
		));
}

// Vertical stack of text of various font sizes

public void txt8(){
   render(box(vcat([ text("A", fontSize(20), fontColor("black")),
	 				 text("BB", fontSize(40), fontColor("blue")),
	 				 text("CC", fontSize(60), fontColor("red"))
	 			   ],
	 			   bottom()),
	 		  gap(1)));
}

public void txt9(){
   words = [ text("aappp"), text("noot"), text("mies"), text("wim"), text("zus") ];
   
   render(grid(words, width(100), fillColor("black"), gap(40), bottom(), left()));
 }
 //TODO
 public void txt10(){
   words = [ text("aappp"), text("noot"), text("mies"), text("wim"), text("zus") ];
   
   render(grid(words, width(100), fillColor("black"), gap(40), bottom(), left(), textAngle(-90)));
 }

private map[str, int] leesplank = 
 ("aap" : 10, "noot" :5, "mies" : 7,
        "wim" : 5, "zus": 10, "jet": 40, 
        "teun" : 10, "vuur" : 20, "gijs" : 5,
        "lam" : 50, "kees" : 30, "bok" : 20,
        "weide" : 20,  "does" : 25, "hok" : 15,
        "duif" : 30, "schapen" : 35
         );
         
// Word cloud using hvcat
public void txt11(){
     words = [text("<name>", fontSize(2*leesplank[name])) | name <- leesplank];
     render(hvcat(words, width(400), fillColor("black"), gap(10), bottom(), left()));
}

// Word cloud using pack
public void txt12(){
     words = [text( "<name>", fontSize(2*leesplank[name])) | name <- leesplank];
     render(pack(words, width(400), fillColor("black"), gap(10), bottom(), left()));
}

// Word cloud using pack with rotated words
public void txt13(){
     words = [text("<name>", fontSize(2*leesplank[name]), (arbInt(3) == 2) ? textAngle(-90) : textAngle(0)) | name <- leesplank];
     render(pack(words, width(400), fillColor("black"), gap(10), bottom(), left()));
}
