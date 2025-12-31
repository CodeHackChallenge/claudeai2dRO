package dev.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInput implements MouseListener, MouseMotionListener {
    
    private int mouseX;
    private int mouseY;
    private boolean mousePressed;
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mousePressed = true;
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
    
    // Getters
    public int getX() { return mouseX; }
    public int getY() { return mouseY; }
    public boolean isPressed() { return mousePressed; }
    
    // Reset after Engine handles the click
    public void resetPressed() {
        mousePressed = false;
    }
    
    // Unused MouseListener methods
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}