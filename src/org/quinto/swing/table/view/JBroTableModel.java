package org.quinto.swing.table.view;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;

/**
 * Table model based on {@link ModelData}.
 */
public class JBroTableModel extends AbstractTableModel {
  protected ModelData data;

  public JBroTableModel( ModelData data ) {
    this.data = data;
  }

  public ModelData getData() {
    return data;
  }

  public void setData( ModelData data ) {
    boolean structureChanged;
    if ( data == null ) {
      if ( this.data == null ) {
        return;
      }
      structureChanged = true;
    } else if ( this.data == null ) {
      structureChanged = true;
    } else {
      ModelField df1[] = data.getFields();
      ModelField df2[] = this.data.getFields();
      if ( df1 == null ) {
        structureChanged = df2 != null;
      } else if ( df2 == null || df1.length != df2.length ) {
        structureChanged = true;
      } else {
        structureChanged = false;
        for ( int i = 0; i < df1.length; i++ ) {
          ModelField f1 = df1[ i ];
          ModelField f2 = df2[ i ];
          if ( f1 == null ) {
            if ( f2 != null ) {
              structureChanged = true;
              break;
            }
          } else if ( f2 == null || !f1.equals( f2 ) || f1.isVisible() != f2.isVisible() ) {
            structureChanged = true;
            break;
          }
        }
      }
    }
    this.data = data;
    if ( structureChanged ) {
      fireTableStructureChanged();
    } else if ( data != null ) {
      int lastRow = structureChanged ? 0 : Math.max( data.getRowsCount(), this.data.getRowsCount() );
      fireTableChanged( new TableModelEvent( this, 0, lastRow ) );
    }
  }
  
  public void removeRow( int row ) throws IndexOutOfBoundsException {
    ModelRow newRows[] = new ModelRow[ data.getRowsCount() - 1 ];
    if ( row < 0 )
      System.arraycopy( data.getRows(), 0, newRows, 0, row );
    if ( row < newRows.length )
      System.arraycopy( data.getRows(), row + 1, newRows, row, newRows.length - row );
    data.setRows( newRows );
    fireTableRowsDeleted( row, row );
  }

  public void addRow( Object rowData[] ) {
    ModelRow newRows[] = new ModelRow[ data.getRowsCount() + 1 ];
    int row = newRows.length - 1;
    if ( row > 0 )
      System.arraycopy( data.getRows(), 0, newRows, 0, row );
    newRows[ row ] = new ModelRow( data.getFieldsCount() );
    newRows[ row ].setValues( rowData );
    newRows[ row ].setLength( data.getFieldsCount() );
    data.setRows( newRows );
    fireTableRowsInserted( row, row );
  }

  /**
   * Add a new column (or group)
   * @param addTo id of parent group (null for root) to which the new column should be inserted
   * @param column the new column (or group) to be inserted
   * @throws IllegalArgumentException if addTo points to a non-existing group
   * @throws ClassCastException if addTo points to a regular column (not a group)
   */
  public void addColumn( String addTo, IModelFieldGroup column ) throws IllegalArgumentException, ClassCastException {
    if ( column == null )
      return;
    int idx[] = addTo == null ? null : data.getIndexOfModelFieldGroup( addTo );
    if ( idx != null && idx[ 0 ] == -1 )
      throw new IllegalArgumentException( "Parent group \"" + addTo + "\" not found" );
    IModelFieldGroup newGroups[];
    if ( idx == null )
    {
      IModelFieldGroup groups[] = ModelFieldGroup.getUpperFieldGroups( data.getFields() );
      newGroups = new IModelFieldGroup[ groups.length + 1 ];
      System.arraycopy( groups, 0, newGroups, 0, groups.length );
      newGroups[ groups.length ] = column;
    }
    else
    {
      ModelField fields[] = ModelField.copyOfModelFields( data.getFields() );
      ModelData idxData = new ModelData( fields );
      ModelFieldGroup group = ( ModelFieldGroup )idxData.getFieldGroups().get( idx[ 1 ] )[ idx[ 0 ] ];
      group.withChild( column );
      newGroups = ModelFieldGroup.getUpperFieldGroups( fields );
    }
    ModelField fields[] = ModelFieldGroup.getBottomFields( newGroups );
    ModelData newData = new ModelData( fields );
    ModelRow rows[] = data.getRows();
    if ( rows != null )
    {
      fields = ModelFieldGroup.getBottomFields( new IModelFieldGroup[]{ column } );
      if ( fields.length == 0 )
        throw new IllegalArgumentException( "No columns found in column group \"" + column.getIdentifier() + '"' );
      int colFromIncl = newData.getIndexOfModelField( fields[ 0 ].getIdentifier() );
      int colToExcl = colFromIncl + column.getColspan();
      int newLength = newData.getFieldsCount();
      ModelRow newRows[] = new ModelRow[ rows.length ];
      for ( int i = 0; i < rows.length; i++ )
      {
        ModelRow newRow = new ModelRow( newLength );
        newRows[ i ] = newRow;
        Object values[] = rows[ i ].getValues();
        if ( values == null || values.length == 0 )
          continue;
        Object newValues[] = newRow.getValues();
        if ( colFromIncl > 0 )
          System.arraycopy( values, 0, newValues, 0, colFromIncl );
        if ( colFromIncl < values.length )
          System.arraycopy( values, colFromIncl, newValues, colToExcl, values.length - colFromIncl );
      }
      newData.setRows( newRows );
    }
    setData( newData );
  }

  @Override
  public int getColumnCount() {
    return data == null ? 0 : data.getFieldsCount();
  }

  @Override
  public int getRowCount() {
    return data == null ? 0 : data.getRowsCount();
  }

  @Override
  public void setValueAt( Object value, int row, int column ) {
    if ( data == null ) {
      return;
    }
    data.setValue( row, column, value );
    fireTableCellUpdated( row, column );
  }

  @Override
  public Object getValueAt( int row, int column ) {
    return data == null ? null : data.getValue( row, column );
  }

  @Override
  public String getColumnName( int column ) {
    if ( data == null ) {
      return null;
    }
    ModelField fields[] = data.getFields();
    if ( fields == null || column >= fields.length ) {
      return null;
    }
    ModelField field = fields[ column ];
    return field == null ? null : field.getCaption();
  }
}