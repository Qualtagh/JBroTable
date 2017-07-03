package org.quinto.swing.table.view;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;

public class JBroPredefinedRowSorter< This extends JBroPredefinedRowSorter< This > > extends PredefinedRowSorter< This > {
  private static final SortKey EMPTY_ARRAY[] = new SortKey[ 0 ];
  
  public JBroPredefinedRowSorter( JBroTable table ) {
    super( table );
  }
  
  private RowSorter.SortKey[] toNumericKeys( SortKey... modelColumns ) {
    if ( modelColumns != null && modelColumns.length != 0 ) {
      JBroTable table = getTable();
      ModelData data = table.getData();
      RowSorter.SortKey keys[] = new RowSorter.SortKey[ modelColumns.length ];
      for ( int i = 0; i < modelColumns.length; i++ ) {
        SortKey modelColumn = modelColumns[ i ];
        int col = data.getIndexOfModelField( modelColumn.getColumn() );
        if ( col < 0 )
          throw new IllegalArgumentException( "Field \"" + modelColumn.getColumn() + "\" not found" );
        RowSorter.SortKey key = new RowSorter.SortKey( col, modelColumn.getSortOrder() );
        keys[ i ] = key;
      }
      return keys;
    }
    return null;
  }
  
  private SortKey[] toStringKeys( RowSorter.SortKey... modelColumns ) {
    if ( modelColumns != null && modelColumns.length != 0 ) {
      JBroTable table = getTable();
      ModelData data = table.getData();
      ModelField fields[] = data.getFields();
      SortKey keys[] = new SortKey[ modelColumns.length ];
      for ( int i = 0; i < modelColumns.length; i++ ) {
        RowSorter.SortKey modelColumn = modelColumns[ i ];
        ModelField col = fields[ modelColumn.getColumn() ];
        SortKey key = new SortKey( col.getIdentifier(), modelColumn.getSortOrder() );
        keys[ i ] = key;
      }
      return keys;
    }
    return EMPTY_ARRAY;
  }

  public This withPostColumnsByName( SortKey... modelColumns ) {
    return super.withPostColumns( toNumericKeys( modelColumns ) );
  }

  public This withPreColumnsByName( SortKey... modelColumns ) {
    return super.withPreColumns( toNumericKeys( modelColumns ) );
  }

  @Override
  public JBroTable getTable() {
    return ( JBroTable )super.getTable();
  }

  public SortKey[] getPostColumnsWithNames() {
    return toStringKeys( super.getPostColumns() );
  }

  public SortKey[] getPreColumnsWithNames() {
    return toStringKeys( super.getPreColumns() );
  }
  
  public static class SortKey {
    private final String column;
    private final SortOrder sortOrder;

    public SortKey( String column, SortOrder sortOrder ) {
      if ( column == null )
        throw new IllegalArgumentException( "column must be non-null" );
      if ( sortOrder == null )
        throw new IllegalArgumentException( "sort order must be non-null" );
      this.column = column;
      this.sortOrder = sortOrder;
    }

    public String getColumn() {
      return column;
    }

    public SortOrder getSortOrder() {
      return sortOrder;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 53 * hash + column.hashCode();
      hash = 53 * hash + sortOrder.hashCode();
      return hash;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( obj == this )
        return true;
      if ( !( obj instanceof SortKey ) )
        return false;
      SortKey other = ( SortKey )obj;
      return sortOrder == other.sortOrder && column.equals( other.column );
    }
  }
}