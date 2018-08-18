package io.github.qualtagh.swing.table.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Attributes that identify a way how table cells should me merged.
 */
public class ModelSpan {
  private final Set< String > columns = new HashSet< String >();
  private final String valueColumn;
  private final String idColumn;
  private boolean drawAsHeader;

  /**
   * Rows with the same ID would be merged vertically (ID is a value in a cell of idColumn).
   * A caption of a merged cell would be equal to the caption found in valueColumn's cell.
   * @param idColumn identifier of ID field
   * @param valueColumn identifier of value field
   */
  public ModelSpan( String idColumn, String valueColumn ) {
    this.idColumn = idColumn;
    this.valueColumn = valueColumn;
  }

  public boolean isDrawAsHeader() {
    return drawAsHeader;
  }

  public void setDrawAsHeader( boolean drawAsHeader ) {
    this.drawAsHeader = drawAsHeader;
  }

  /**
   * The way how a merged cell should be rendered.
   * @param drawAsHeader true - a merged cell would be rendered as a header cell, false - as a regular table cell
   * @return this
   */
  public ModelSpan withDrawAsHeader( boolean drawAsHeader ) {
    setDrawAsHeader( drawAsHeader );
    return this;
  }

  public String getValueColumn() {
    return valueColumn;
  }

  public String getIdColumn() {
    return idColumn;
  }

  public Set< String > getColumns() {
    return columns;
  }

  public ModelSpan withColumn( String column ) {
    columns.add( column );
    return this;
  }

  /**
   * A list of columns that should be merged.
   * @param columns a list of merging columns
   * @return this
   */
  public ModelSpan withColumns( String... columns ) {
    this.columns.addAll( Arrays.asList( columns ) );
    return this;
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder( "Span of columns [" );
    boolean added = false;
    for ( String column : columns ) {
      if ( added )
        ret.append( ',' );
      ret.append( ' ' ).append( column );
      added = true;
    }
    if ( added )
      ret.append( ' ' );
    ret.append( "] identified by " ).append( idColumn ).append( " showing value of " ).append( valueColumn );
    if ( drawAsHeader )
      ret.append( " drawn as header" );
    return ret.toString();
  }
}