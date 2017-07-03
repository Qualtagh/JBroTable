package org.quinto.swing.table.model;

import java.util.Arrays;

/**
 * Data row.
 */
public class ModelRow implements java.io.Serializable {

  private static final long serialVersionUID = 4L;

  private Object values[];

  public ModelRow() {
    this( 0 );
  }

  public ModelRow( int length ) {
    values = null;
    setLength( length );
  }
  
  public ModelRow( Object... values ) {
    this.values = values;
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode( values );
  }

  @Override
  public boolean equals( Object obj ) {
    return obj instanceof ModelRow && Arrays.deepEquals( values, ( ( ModelRow )obj ).values );
  }

  @Override
  public ModelRow clone() {
    return values == null ? new ModelRow() : new ModelRow( values.clone() );
  }

  public Object getValue( int index ) {
    if ( values == null || index < 0 || index >= values.length ) {
      return null;
    }
    return values[ index ];
  }

  public boolean setValue( int index, Object value ) {
    if ( values == null || index < 0 || index >= values.length ) {
      return false;
    }
    values[ index ] = value;
    return true;
  }

  public void setLength( int length ) {
    if ( values != null && values.length == length ) {
      return;
    }
    Object newValues[] = new Object[ length ];
    if ( values != null && values.length != 0 ) {
      System.arraycopy( values, 0, newValues, 0, Math.min( values.length, length ) );
    }
    values = newValues;
  }

  public int getLength() {
    return values == null ? 0 : values.length;
  }

  public Object[] getValues() {
    return values;
  }
  
  public void setValues( Object[] values ) {
    this.values = values;
  }

}