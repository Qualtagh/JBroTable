package io.github.qualtagh.swing.table.util;

import java.awt.AWTException;
import java.awt.Robot;

public class MouseRobot extends Robot {
  private int lastX;
  private int lastY;
  
  public MouseRobot() throws AWTException {
    super();
    initDebugMode();
  }
  
  private void initDebugMode() {
    setAutoDelay( 1000 );
    setAutoWaitForIdle( true );
  }
  
  public void mouseMoveRelative( int x, int y ) {
    mouseMove( lastX + x, lastY + y );
  }

  @Override
  public void mouseMove( int x, int y ) {
    super.mouseMove( x, y );
    lastX = x;
    lastY = y;
  }
}