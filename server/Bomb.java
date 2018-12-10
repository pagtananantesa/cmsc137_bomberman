import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;


public class Bomb extends JLabel{
	private int DIMENSION = 50;
	private int x, y;
	private ImageIcon img;

	public Bomb(int x, int y){
		img = new ImageIcon("img/heart.gif");
		this.setIcon(img);
		this.setBounds(50+DIMENSION*y, 100+DIMENSION*x, DIMENSION, DIMENSION);
	}

	
}