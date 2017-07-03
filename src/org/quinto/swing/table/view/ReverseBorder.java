package org.quinto.swing.table.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 * A border that rotates a delegate border by 180 degrees.
 * <p>It causes a raised bevel border to be lowered and vice versa.</p>
 */
public class ReverseBorder implements Border {
  private final Border delegate;

  public ReverseBorder( Border delegate ) {
    this.delegate = delegate;
  }

  @Override
  public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
    if ( g instanceof Graphics2D ) {
      Graphics2D gg = ( Graphics2D )g;
      gg.scale( -1.0, -1.0 );
      gg.translate( 1 - w, 1 - h );
    }
    delegate.paintBorder( c, g, x, y, w, h );
  }

  @Override
  public Insets getBorderInsets( Component c ) {
    return delegate.getBorderInsets( c );
  }

  @Override
  public boolean isBorderOpaque() {
    return delegate.isBorderOpaque();
  }

  public Border getDelegate() {
    return delegate;
  }
}