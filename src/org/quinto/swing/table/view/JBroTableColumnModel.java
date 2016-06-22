package org.quinto.swing.table.view;

import java.beans.PropertyChangeEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;

public class JBroTableColumnModel extends DefaultTableColumnModel {
  private final List< List< JBroTableColumn > > columns = new ArrayList< List< JBroTableColumn > >();
  private final Map< String, JBroTableColumn > columnsIndex = new HashMap< String, JBroTableColumn >();
  private final JBroTable table;
  private final List< TableColumnModel > delegates = new ArrayList< TableColumnModel >();

  public JBroTableColumnModel( JBroTable table ) {
    this.table = table;
  }

  public JBroTable getTable() {
    return table;
  }
  
  public void clear() {
    columns.clear();
    columnsIndex.clear();
    delegates.clear();
    while ( getColumnCount() > 0 )
      super.removeColumn( getColumn( 0 ) );
  }
  
  @Override
  public void propertyChange( PropertyChangeEvent e ) {
    String name = e.getPropertyName();
    if ( "width".equals( name ) && table.getAutoResizeMode() != JTable.AUTO_RESIZE_ALL_COLUMNS && e.getSource() instanceof JBroTableColumn && e.getNewValue() instanceof Integer && e.getOldValue() instanceof Integer ) {
      totalColumnWidth = -1;
      JBroTableColumn column = ( JBroTableColumn )e.getSource();
      Enumeration< TableColumn > cols = getColumns();
      int x = 0;
      while ( cols.hasMoreElements() ) {
        TableColumn col = cols.nextElement();
        if ( col == column )
          break;
        x += col.getWidth();
      }
      table.revalidate();
      JBroTableHeader header = table.getTableHeader();
      JBroTableHeaderUI ui = header.getUI();
      header.repaintHeaderAndTable( x, 0, header.getWidth() - x );
      while ( ( column = getColumnParent( column ) ) != null )
        header.repaint( ui.getGroupHeaderBoundsFor( column ) );
    } else
      super.propertyChange( e );
  }

  public JBroTableColumn addColumn( IModelFieldGroup group, int x, int y ) {
    ModelData data = table.getData();
    JBroTableColumn ret = new JBroTableColumn( x, y, group instanceof ModelField ? data.getIndexOfModelField( group.getIdentifier() ) : data.getFieldsCount() + x + ( y << 16 ), group.getRowspan() );
    String id = group.getIdentifier();
    ret.setIdentifier( id );
    ret.setHeaderValue( group.getCaption() );
    int colspan = group instanceof ModelField ? 1 : 0;
    ret.setColspan( colspan );
    int rowspan = group.getRowspan() - 1;
    while ( columns.size() <= y + rowspan )
      columns.add( new ArrayList< JBroTableColumn >() );
    for ( int level = 0; level <= rowspan; level++ ) {
      List< JBroTableColumn > levelColumns = columns.get( y + level );
      levelColumns.add( ret );
    }
    if ( group instanceof ModelField )
      super.addColumn( ret );
    columnsIndex.put( id, ret );
    JBroTableColumn parent = getColumnParent( ret );
    while ( parent != null ) {
      parent.setColspan( parent.getColspan() + colspan );
      parent = getColumnParent( parent );
    }
    if ( table.getTableHeader() instanceof JBroTableHeader ) {
      JBroTableHeaderUI ui = ( ( JBroTableHeader )table.getTableHeader() ).getUI();
      if ( ui != null )
        ui.updateModel();
    }
    return ret;
  }

  @Override
  public void addColumn( TableColumn column ) {
    if ( column instanceof JBroTableColumn ) {
      JBroTableColumn dtc = ( JBroTableColumn )column;
      addColumn( getModelField( dtc ), dtc.getX(), dtc.getY() );
    } else {
      String id;
      // Identifier may be replaced by headerValue in getter, so we need to find out if it's really present.
      Object identifier = column.getIdentifier();
      if ( identifier instanceof String ) {
        if ( identifier.equals( column.getHeaderValue() ) ) {
          column.setHeaderValue( 0 );
          // It was replaced by headerValue, so the real identifier is null.
          if ( column.getIdentifier() instanceof Number ) {
            column.setHeaderValue( identifier );
            identifier = null;
          // It was a real identifier.
          } else
            column.setHeaderValue( identifier );
        }
      } else
        identifier = null;
      if ( identifier instanceof String )
        id = ( String )column.getIdentifier();
      else {
        ModelField field = table.getData().getFields()[ column.getModelIndex() ];
        id = field.getIdentifier();
        if ( columnsIndex.containsKey( id ) )
          throw new IllegalArgumentException( "An attempt to add a second column with the ID already used: " + column + " (" + column.getHeaderValue() + "), ID = " + field.getIdentifier() + ", model index = " + column.getModelIndex() );
      }
      int index[] = table.getData().getIndexOfModelFieldGroup( id );
      int x = index[ 0 ];
      if ( x >= 0 ) {
        int y = index[ 1 ];
        addColumn( table.getData().getFieldGroups().get( y )[ x ], x, y );
      }
    }
  }
  
  public IModelFieldGroup getModelField( JBroTableColumn column ) {
    return column == null ? null : table.getData().getFieldGroups().get( column.getY() )[ column.getX() ];
  }
  
  public JBroTableColumn getTableColumn( IModelFieldGroup modelField ) {
    return modelField == null ? null : columnsIndex.get( modelField.getIdentifier() );
  }

  public JBroTableColumn getColumnParent( JBroTableColumn column ) {
    if ( column == null )
      return null;
    IModelFieldGroup modelField = getModelField( column );
    ModelFieldGroup parent = modelField.getParent();
    return getTableColumn( parent );
  }

  public Collection< JBroTableColumn > getColumnParents( JBroTableColumn column, boolean includeThis ) {
    Deque< JBroTableColumn > ret = new ArrayDeque< JBroTableColumn >();
    JBroTableColumn col = includeThis ? column : column == null ? null : getColumnParent( column );
    while ( col != null ) {
      ret.addFirst( col );
      col = getColumnParent( col );
    }
    return ret;
  }
  
  public List< JBroTableColumn > getColumnChildren( JBroTableColumn column ) {
    IModelFieldGroup modelField = getModelField( column );
    if ( modelField instanceof ModelFieldGroup ) {
      int kidsLevel = column.getY() + column.getRowspan();
      if ( kidsLevel >= columns.size() )
        return Collections.EMPTY_LIST;
      ModelFieldGroup group = ( ModelFieldGroup )modelField;
      List< JBroTableColumn > ret = new ArrayList< JBroTableColumn >( group.getColspan() );
      Set< String > ids = new HashSet< String >( group.getColspan() );
      for ( IModelFieldGroup child : group.getChildren() )
        ids.add( child.getIdentifier() );
      List< JBroTableColumn > childrenCandidates = columns.get( kidsLevel );
      for ( JBroTableColumn childColumn : childrenCandidates ) {
        if ( ids.contains( childColumn.getIdentifier() ) )
          ret.add( childColumn );
      }
      return ret;
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public void removeColumn( TableColumn column ) {
    if ( column instanceof JBroTableColumn ) {
      JBroTableColumn dtc = ( JBroTableColumn )column;
      int colspan = dtc.getColspan();
      for ( JBroTableColumn child : getColumnChildren( dtc ) )
        removeColumn( child );
      JBroTableColumn parent = getColumnParent( dtc );
      for ( int level = 0; level < dtc.getRowspan(); level++ )
        columns.get( dtc.getY() + level ).remove( dtc );
      columnsIndex.remove( dtc.getIdentifier() );
      if ( getColumnChildren( parent ).isEmpty() )
        removeColumn( parent );
      else {
        while ( parent != null ) {
          parent.setColspan( parent.getColspan() - colspan );
          parent = getColumnParent( parent );
        }
      }
    }
    super.removeColumn( column );
  }

  @Override
  public int getColumnIndex( Object identifier ) {
    if ( identifier instanceof String )
      return getColumnIndex( ( String )identifier );
    return super.getColumnIndex( identifier );
  }

  public int getColumnIndex( String identifier ) {
    JBroTableColumn column = columnsIndex.get( identifier );
    return getColumnAbsoluteIndex( column );
  }

  public int getColumnAbsoluteIndex( JBroTableColumn column ) {
    if ( column == null )
      return -1;
    int ret = 0;
    for ( JBroTableColumn col : columns.get( column.getY() ) ) {
      if ( column.equals( col ) )
        return ret;
      ret += col.getColspan();
    }
    return -1;
  }

  public int getColumnRelativeIndex( JBroTableColumn column ) {
    if ( column == null )
      return -1;
    return columns.get( column.getY() ).indexOf( column );
  }
  
  public JBroTableColumn getColumnAtAbsolutePosition( int xWithColspans, int level ) {
    if ( xWithColspans < 0 || level < 0 )
      return null;
    int ret = 0;
    for ( JBroTableColumn column : columns.get( level ) ) {
      ret += column.getColspan();
      if ( ret > xWithColspans )
        return column;
    }
    return null;
  }
  
  public int getRelativePosition( int xWithColspans, int level ) {
    if ( xWithColspans < 0 || level < 0 )
      return -1;
    int ret = 0;
    List< JBroTableColumn > levelColumns = columns.get( level );
    for ( int x = 0; x < levelColumns.size(); x++ ) {
      JBroTableColumn column = levelColumns.get( x );
      ret += column.getColspan();
      if ( ret > xWithColspans )
        return x;
    }
    return levelColumns.size();
  }
  
  public int getAbsolutePosition( int x, int level ) {
    if ( x < 0 || level < 0 )
      return -1;
    int ret = 0;
    for ( JBroTableColumn column : columns.get( level ) ) {
      if ( x == 0 )
        break;
      x--;
      ret += column.getColspan();
    }
    return ret;
  }
  
  public JBroTableColumn getColumnAtRelativePosition( int x, int level ) {
    return x < 0 || level < 0 ? null : columns.get( level ).get( x );
  }

  public void moveColumn( JBroTableColumn column, int newIndex ) {
    moveColumn( column, newIndex, true );
  }

  private void moveColumn( JBroTableColumn column, int newIndex, boolean checkBounds ) {
    int startIndex = getColumnIndex( column.getIdentifier() );
    if ( checkBounds && ( startIndex < 0 || newIndex < 0 ) || startIndex == newIndex )
      return;
    int colspan = column.getColspan() - 1;
    int endIndex = startIndex + colspan;
    if ( checkBounds && ( endIndex >= getColumnCount() || newIndex >= getColumnCount() ) )
      return;
    IModelFieldGroup modelField = getModelField( column );
    if ( modelField == null || !modelField.isManageable() )
      return;
    JBroTableColumn peerAtNewIndex = getColumnAtAbsolutePosition( newIndex, column.getY() );
    if ( peerAtNewIndex == column )
      return;
    modelField = getModelField( peerAtNewIndex );
    if ( modelField == null || !modelField.isManageable() )
      return;
    JBroTableColumn parent = getColumnParent( column );
    if ( parent != null ) {
      int parentStartIndex = getColumnIndex( parent.getIdentifier() );
      if ( checkBounds && newIndex < parentStartIndex )
        return;
      int parentEndIndex = parentStartIndex + parent.getColspan() - 1;
      if ( checkBounds && newIndex > parentEndIndex )
        return;
    }
    int direction = newIndex < startIndex ? 1 : -1;
    if ( checkBounds ) {
      newIndex = getColumnAbsoluteIndex( peerAtNewIndex );
      if ( direction == -1 )
        newIndex = newIndex + peerAtNewIndex.getColspan() - 1;
    }
    List< JBroTableColumn > columnChildren = getColumnChildren( column );
    if ( columnChildren.isEmpty() )
      super.moveColumn( startIndex, newIndex );
    else {
      if ( direction == -1 )
        Collections.reverse( columnChildren );
      int childNewIndex = newIndex;
      for ( JBroTableColumn child : columnChildren ) {
        moveColumn( child, childNewIndex, false );
        childNewIndex += direction * child.getColspan();
      }
    }
    for ( int level = column.getY(); level < column.getY() + column.getRowspan(); level++ ) {
      List< JBroTableColumn > levelColumns = columns.get( level );
      int newRelativeIndex = getRelativePosition( newIndex, level );
      levelColumns.remove( getRelativePosition( startIndex, level ) );
      levelColumns.add( newRelativeIndex, column );
    }
  }

  @Override
  public void moveColumn( int columnIndex, int newIndex ) {
    TableColumn column = getColumn( columnIndex );
    if ( column instanceof JBroTableColumn )
      moveColumn( ( JBroTableColumn )column, newIndex );
    else
      super.moveColumn( columnIndex, newIndex );
  }
  
  public int getWidth( JBroTableColumn column ) {
    int ret = 0;
    for ( JBroTableColumn child : getColumnChildren( column ) )
      ret += getWidth( child );
    if ( ret == 0 )
      ret = column.getWidth();
    return ret;
  }
  
  public int getLevelsQuantity() {
    return columns.size();
  }

  public List< List< JBroTableColumn > > getColumnGroups() {
    return columns;
  }

  @Override
  public JBroTableColumn getColumn( int columnIndex ) {
    return ( JBroTableColumn )super.getColumn( columnIndex );
  }
  
  public JBroTableColumn getColumn( String identifier ) {
    return columnsIndex.get( identifier );
  }
  
  public TableColumnModel getDelegate( int level ) {
    if ( level < 0 || level >= columns.size() )
      return null;
    while ( delegates.size() <= level )
      delegates.add( new DelegateColumnModel( delegates.size() ) );
    return delegates.get( level );
  }
  
  private class DelegateColumnModel implements TableColumnModel {
    private final int level;
    
    public DelegateColumnModel( int level ) {
      this.level = level;
    }

    @Override
    public void addColumn( TableColumn aColumn ) {
      JBroTableColumnModel.this.addColumn( aColumn );
    }

    @Override
    public void removeColumn( TableColumn column ) {
      JBroTableColumnModel.this.removeColumn( column );
    }

    @Override
    public void moveColumn( int columnIndex, int newIndex ) {
      JBroTableColumnModel.this.moveColumn( getAbsolutePosition( columnIndex, level ), getAbsolutePosition( newIndex, level ) );
    }

    @Override
    public void setColumnMargin( int newMargin ) {
      JBroTableColumnModel.this.setColumnMargin( newMargin );
    }

    @Override
    public int getColumnCount() {
      return columns.get( level ).size();
    }

    @Override
    public Enumeration< TableColumn > getColumns() {
      return Collections.enumeration( ( List )columns.get( level ) );
    }

    @Override
    public int getColumnIndex( Object columnIdentifier ) {
      if ( !( columnIdentifier instanceof String ) )
        return -1;
      JBroTableColumn column = columnsIndex.get( ( String )columnIdentifier );
      if ( column == null || column.getY() > level || column.getY() + column.getRowspan() <= level )
        return -1;
      return getColumnRelativeIndex( column );
    }

    @Override
    public TableColumn getColumn( int columnIndex ) {
      return JBroTableColumnModel.this.getColumnAtRelativePosition( columnIndex, level );
    }

    @Override
    public int getColumnMargin() {
      return JBroTableColumnModel.this.getColumnMargin();
    }

    @Override
    public int getColumnIndexAtX( int x ) {
      if ( x < 0 )
        return -1;
      int i = 0;
      for ( JBroTableColumn column : columns.get( level ) ) {
        int width = getWidth( column );
        x -= width;
        if ( x < 0 )
          return i;
        i++;
      }
      return -1;
    }

    @Override
    public int getTotalColumnWidth() {
      return JBroTableColumnModel.this.getTotalColumnWidth();
    }

    @Override
    public void setColumnSelectionAllowed( boolean flag ) {
      JBroTableColumnModel.this.setColumnSelectionAllowed( flag );
    }

    @Override
    public boolean getColumnSelectionAllowed() {
      return JBroTableColumnModel.this.getColumnSelectionAllowed();
    }

    @Override
    public int[] getSelectedColumns() {
      int ret[] = JBroTableColumnModel.this.getSelectedColumns();
      for ( int i = 0; i < ret.length; i++ )
        ret[ i ] = getAbsolutePosition( i, level );
      return ret;
    }

    @Override
    public int getSelectedColumnCount() {
      return JBroTableColumnModel.this.getSelectedColumnCount();
    }

    @Override
    public void setSelectionModel( ListSelectionModel newModel ) {
      JBroTableColumnModel.this.setSelectionModel( newModel );
    }

    @Override
    public ListSelectionModel getSelectionModel() {
      return JBroTableColumnModel.this.getSelectionModel();
    }

    @Override
    public void addColumnModelListener( TableColumnModelListener x ) {
      JBroTableColumnModel.this.addColumnModelListener( x );
    }

    @Override
    public void removeColumnModelListener( TableColumnModelListener x ) {
      JBroTableColumnModel.this.removeColumnModelListener( x );
    }
  }
}