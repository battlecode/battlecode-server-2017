package battlecode.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MapEditor extends JFrame {
	boolean[] keysHeld = new boolean[256];
	int[][] map;
	int xmax, ymax;
	int tilePixels = 20;
	int mode = 2;
	boolean huge = false;
	MapEditor() {
		xmax= Integer.parseInt(JOptionPane.showInputDialog("width?", "50"));
		ymax= Integer.parseInt(JOptionPane.showInputDialog("height?", "50"));
		map = new int[xmax][ymax];
		
		for(int x=0; x<xmax; x++) for(int y=0; y<ymax; y++) map[x][y] = 1;
		
		JComponent component = new JComponent() {
			private static final long serialVersionUID = 41243L;
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(xmax*tilePixels, ymax*tilePixels);
			}
			@Override
			public void paintComponent(Graphics g) {
				Color c[] = new Color[] {Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED};
				for(int x=0; x<xmax; x++) for(int y=0; y<ymax; y++) {
					g.setColor(c[map[x][y]]);
					g.fillRect(x*tilePixels, y*tilePixels, tilePixels, tilePixels);
				}
			}
		};
		this.add(component, BorderLayout.CENTER);
		this.add(new JLabel("1=ground, 2=mine, 3=encampment, 4=spawn"),
				BorderLayout.PAGE_START);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		component.setFocusable(true);
		component.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if(key>=KeyEvent.VK_1 && key<=KeyEvent.VK_9) {
					mode = key-KeyEvent.VK_1+1;
				}
				if(key==KeyEvent.VK_P) {
					print();
				}
				keysHeld[key] = true;
			}
			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				keysHeld[key] = false;
			}
			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		
		class MapMouseListener implements MouseListener, MouseMotionListener {
			
			/** -1 is not dragging, 0 is adding, 1 is erasing **/
			int dragging = -1;
			
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				int button = e.getButton();
				int x = (e.getX())/tilePixels;
				int y = (e.getY())/tilePixels;
				System.out.println(x+" "+y);
				if(x<0 || y<0 || x>=xmax || y>=ymax) return;
				
				if(button == MouseEvent.BUTTON1) {
					mouseDragged(e);
				}
				if(button == MouseEvent.BUTTON3) {
					huge = !huge;
				}
				repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			public void mouseMoved(MouseEvent arg0) {}
			public void mouseDragged(MouseEvent e) {
				
				int x = (e.getX())/tilePixels;
				int y = (e.getY())/tilePixels;
				System.out.println(x+" "+y);
				if(x<0 || y<0 || x>=xmax || y>=ymax) return;
				if(!huge)
					setMap(x, y, mode);
				else {
					for(int a=x-1; a<=x+1; a++) for(int b=y-1; b<=y+1; b++)
						if(a>=0&&b>=0&&a<xmax&&b<ymax)
							setMap(a,b,mode);
				}
				repaint();
			}
		}
		
		MouseListener mouse_handler = new MapMouseListener();
		component.addMouseListener(mouse_handler);
		component.addMouseMotionListener((MouseMotionListener)mouse_handler);
	}
	void setMap(int x, int y, int v) {
		map[x][y] = v;
		map[xmax-1-x][ymax-1-y] = v;
	}
	void print() {
		char[] c = new char[] {'.', '.', 'o', '#', 'a'};
		for(int x=0; x<xmax; x++) {
			for(int y=0; y<ymax; y++) {
				System.out.print((map[x][y]==4&&x<xmax/2)?'A':c[map[x][y]]);
			}
			System.out.println();
		}
	}
	public static void main(String args[]) {
		MapEditor frame = new MapEditor();
		frame.setLocation(100, 30);
		frame.setVisible(true);
	}
}
