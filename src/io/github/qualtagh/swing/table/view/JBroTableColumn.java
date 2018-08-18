package io.github.qualtagh.swing.table.view;

import javax.swing.table.TableColumn;
import io.github.qualtagh.swing.table.model.Utils;

public class JBroTableColumn extends TableColumn {
  private final int x;
  private final int y;
  private int colspan;
  private final int rowspan;
  
  public JBroTableColumn( int x, int y, int modelIndex, int rowspan ) {
    super( modelIndex );
    this.x = x;
    this.y = y;
    this.rowspan = rowspan;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  @Override
  public String getIdentifier() {
    return ( String )super.getIdentifier();
  }

  public int getColspan() {
    return colspan;
  }

  public void setColspan( int colspan ) {
    this.colspan = colspan;
  }

  public int getRowspan() {
    return rowspan;
  }

  @Override
  public String toString() {
    return "Column: " + getIdentifier() + " (" + getHeaderValue() + ") ( " + x + ", " + y + " )";
  }

  @Override
  public int hashCode() {
    return identifier == null ? 0 : identifier.hashCode();
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof JBroTableColumn ) )
      return false;
    if ( obj == this )
      return true;
    return Utils.equals( identifier, ( ( JBroTableColumn )obj ).identifier );
  }
}