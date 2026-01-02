package dev.main;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInput implements MouseListener, MouseMotionListener {
    
    private int mouseX;
    private int mouseY;
    private boolean mousePressed;
    
    private boolean leftClick;
    private boolean rightClick;
    
    public MouseInput() {
        this.mousePressed = false;
        this.leftClick = false;
        this.rightClick = false;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mousePressed = true;
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClick = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightClick = true;
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // Keep pressed true until Engine reads it
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    public int getX() { return mouseX; }
    public int getY() { return mouseY; }
    public boolean isPressed() { return mousePressed; }
    public boolean isLeftClick() { return leftClick; }
    public boolean isRightClick() { return rightClick; }
    
    public void resetPressed() {
        mousePressed = false;
        leftClick = false;
        rightClick = false;
    }
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}