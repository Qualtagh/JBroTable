package org.quinto.swing.table.view;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;

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
          } else if ( f2 == null || !f1.equals( f2 ) || f1.isVisible() != f2.isVisible() || f1.isFixed() != f2.isFixed() ) {
            structureChanged = true;
            break;
          }
        }
      }
    }
    this.data = data;
    if ( structureChanged )
      fireTableStructureChanged();
    else if ( data != null )
      fireTableChanged( new TableModelEvent( this, 0, Integer.MAX_VALUE ) );
  }
  
  public void removeRow( int row ) throws IndexOutOfBoundsException {
    data.removeRow( row );
    fireTableRowsDeleted( row, row );
  }

  public void addRow( Object rowData[] ) {
    int row = data.getRowsCount();
    data.addRow( rowData );
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
    setData( data.withField( addTo, column ) );
  }

  /**
   * Remove a given column (or group)
   * @param id identifier of a column to be removed
   * @throws IllegalArgumentException when no column found with a given id
   */
  public void removeColumn( String id ) throws IllegalArgumentException {
    setData( data.withoutField( id ) );
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