package org.quinto.swing.table.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.plaf.TableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.Utils;

public class JBroTable extends JTable {
  private static final Logger LOGGER = Logger.getLogger( JBroTable.class );
  private static final Pattern BR_PATTERN = Pattern.compile( "<br>|<br/>", Pattern.CASE_INSENSITIVE );
  private static final Pattern TAG_PATTERN = Pattern.compile( "<[^<>]++>" );
  private Integer headerHeight;
  private HeaderHeightWatcher headerHeightWatcher;

  public JBroTable() {
    this( null );
  }
  
  public JBroTable( ModelData data ) {
    super( new JBroTableModel( data ) );
    setShowGrid( true );
    setIntercellSpacing( new Dimension( 1, 1 ) );
    super.setUI( new JBroTableUI() );
    checkFieldWidths();
    refresh();
  }

  @Override
  public void setUI( TableUI ui ) {
    if ( ui instanceof JBroTableUI )
      super.setUI( ui );
    else {
      JBroTableUI oldUI = getUI();
      if ( oldUI != null ) {
        super.setUI( ui );
        super.setUI( oldUI );
      }
    }
  }

  @Override
  public JBroTableUI getUI() {
    return ( JBroTableUI )super.getUI();
  }

  /**
   * Set the whole header height.
   * @param headerHeight the whole header height, {@code null} means to let Swing determine it
   */
  public void setHeaderHeight( Integer headerHeight ) {
    if ( headerHeight == null ) {
      if ( headerHeightWatcher != null ) {
        getColumnModel().removeColumnModelListener( headerHeightWatcher );
      }
    } else if ( this.headerHeight == null ) {
      getColumnModel().addColumnModelListener( headerHeightWatcher = new HeaderHeightWatcher() );
    }
    this.headerHeight = headerHeight;
    updateHeaderSize();
  }

  public int getHeaderHeight() {
    return headerHeight == null ? 0 : headerHeight;
  }

  private void refresh() {
    revalidate();
    repaint( getVisibleRect() );
  }

  @Override
  public JBroTableModel getModel() {
    return ( JBroTableModel )super.getModel();
  }

  public ModelData getData() {
    return getModel().getData();
  }

  public void updateHeaderSize() {
    if ( headerHeight == null || headerHeight < 0 ) {
      return;
    }
    JTableHeader header = getTableHeader();
    int h = headerHeight;
    TableColumnModel cmodel = getColumnModel();
    Font font = header.getFont();
    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics( font );
    for ( int c = 0; c < cmodel.getColumnCount(); c++ ) {
      TableColumn column = cmodel.getColumn( c );
      String text = String.valueOf( column.getHeaderValue() );
      if ( text != null && text.length() >= 6 && text.charAt( 0 ) == '<' && text.substring( 0, 6 ).equalsIgnoreCase( "<html>" ) ) {
        text = BR_PATTERN.matcher( text ).replaceAll( "\n" );
        text = TAG_PATTERN.matcher( text ).replaceAll( "" );
      }
      text = Utils.getTextWithWordWraps( text, fm, column.getWidth() - 8 );
      int newLines = 0;
      if ( text != null ) {
        char chars[] = text.toCharArray();
        for ( char ch : chars )
          if ( ch == '\n' )
            newLines++;
      }
      int height = ( newLines + 1 ) * fm.getHeight() + 6;
      if ( height > h )
        h = height;
    }
    TableHeaderUI ui = header.getUI();
    if ( ui instanceof JBroTableHeaderUI ) {
      JBroTableHeaderUI gui = ( JBroTableHeaderUI )ui;
      Dimension d = gui.getPreferredSize( header );
      if ( h > d.height ) {
        int levels = gui.getHeader().getLevelsQuantity();
        int lastRowHeight = gui.getRowHeight( levels - 1 );
        gui.setRowHeight( levels - 1, lastRowHeight + h - d.height );
      }
    }
    header.setPreferredSize( new Dimension( cmodel.getTotalColumnWidth(), h ) );
  }
  
  private void checkFieldWidths() {
    ModelData data = getData();
    if ( data == null ) {
      return;
    }
    ModelField fields[] = data.getFields();
    if ( fields == null ) {
      return;
    }
    boolean changed = false;
    for ( int i = columnModel.getColumnCount() - 1; i >= 0; i-- ) {
      TableColumn column = columnModel.getColumn( i );
      int modelIndex = column.getModelIndex();
      ModelField field = fields[ modelIndex ];
      if ( field.isVisible() ) {
        String headerValue = field.getCaption();
        if ( !Utils.equals( headerValue, column.getHeaderValue() ) ) {
          column.setHeaderValue( headerValue );
          changed = true;
        }
        Integer defaultWidth = field.getDefaultWidth();
        if ( defaultWidth != null && defaultWidth >= 0 ) {
          defaultWidth = Math.min( Math.max( defaultWidth, column.getMinWidth() ), column.getMaxWidth() );
          if ( defaultWidth != column.getPreferredWidth() ) {
            column.setPreferredWidth( defaultWidth );
            changed = true;
          }
        }
      } else {
        changed = true;
        removeColumn( column );
      }
    }
    if ( changed ) {
      getTableHeader().repaint();
    }
  }
  
  /**
   * Set new data.
   * @param data new array of data
   */
  public void setData( ModelData data ) {
    getModel().setData( data );
    checkFieldWidths();
    refresh();
  }

  /**
   * Set new order of columns (including visibility). Does not affect table model.
   * @param newFields a list of new fields identifiers
   */
  public void reorderFields( String newFields[] ) {
    ModelData data = getData();
    ModelField modelFields[] = data.getFields();
    // Column indexes in model
    LinkedHashMap< String, Integer > modelPositions = ModelField.getIndexes( modelFields );
    // Current order of visible columns
    LinkedHashSet< Integer > iold = new LinkedHashSet< Integer >( modelFields.length );
    for ( int i = 0; i < columnModel.getColumnCount(); i++ ) {
      TableColumn column = columnModel.getColumn( i );
      iold.add( column.getModelIndex() );
    }
    // New order of visible columns
    LinkedHashSet< Integer > inew = new LinkedHashSet< Integer >( modelFields.length );
    for ( int i = 0; i < newFields.length; i++ ) {
      if ( newFields[ i ] == null ) {
        continue;
      }
      Integer pos = modelPositions.get( newFields[ i ] );
      // Fields list doesn't correspond to current data array
      if ( pos == null ) {
        LOGGER.warn( "reorderFields called on obsolete data model. Call setData first." );
        return;
      }
      inew.add( pos );
    }
    ArrayList< ModelField > manageable = null;
    // Delete absent columns
    for ( Iterator< Integer > it = iold.iterator(); it.hasNext(); ) {
      Integer pos = it.next();
      if ( !inew.contains( pos ) ) {
        ModelField field = modelFields[ pos ];
        if ( field.isManageable() ) {
          it.remove();
          field.setVisible( false );
          removeColumn( columnModel.getColumn( convertColumnIndexToView( pos ) ) );
        } else {
          field.setManageable( true );
          if ( manageable == null )
            manageable = new ArrayList< ModelField >();
          manageable.add( field );
          // Exceptional event: unmanageable column was hidden. It should become visible again.
          // This situation is really rare (API doesn't allow this to happen), so performance is not an issue.
          // Traversing the whole list to find initial column position.
          ArrayList< Integer > swap = new ArrayList< Integer >( inew.size() + 1 );
          swap.addAll( inew );
          for ( int i = 0; i < swap.size(); i++ ) {
            Integer p = swap.get( i );
            if ( p.compareTo( pos ) > 0 ) {
              swap.add( i, pos );
              break;
            }
          }
          if ( swap.size() <= inew.size() )
            swap.add( pos );
          inew.clear();
          inew.addAll( swap );
        }
      }
    }
    // Add new columns
    for ( Iterator< Integer > it = inew.iterator(); it.hasNext(); ) {
      Integer pos = it.next();
      if ( !iold.contains( pos ) ) {
        ModelField field = modelFields[ pos ];
        if ( field.isManageable() ) {
          iold.add( pos );
          field.setVisible( true );
          int coords[] = data.getIndexOfModelFieldGroup( field.getIdentifier() );
          addColumn( new JBroTableColumn( coords[ 0 ], coords[ 1 ], pos, field.getRowspan() ) );
        } else
          it.remove();
      }
    }
    int newVisibleIndexes[] = new int[ inew.size() ];
    int n = 0;
    for ( Integer pos : inew ) {
      newVisibleIndexes[ n++ ] = convertColumnIndexToView( pos );
    }
    // Permutations
    for ( int i = 0; i < newVisibleIndexes.length; i++ ) {
      int pos = newVisibleIndexes[ i ];
      while ( pos != i ) {
        int posPos = newVisibleIndexes[ pos ];
        swapColumns( posPos, pos );
        newVisibleIndexes[ pos ] = pos;
        newVisibleIndexes[ i ] = posPos;
        pos = posPos;
      }
    }
    if ( manageable != null )
      for ( ModelField field : manageable )
        field.setManageable( false );
    checkFieldWidths();
  }
  
  /**
   * Swap two columns positions (does not affect model).
   * @param first swapping column
   * @param second swapping column
   */
  public void swapColumns( int first, int second ) {
    if ( first > second ) {
      int t = first;
      first = second;
      second = t;
    } else if ( first == second ) {
      return;
    }
    moveColumn( first, second );
    moveColumn( second - 1, first );
  }

  /**
   * A list of visible columns in the view order separated by ";". One extra ";" is added at the end.
   * @return a list of visible columns
   */
  public String getFieldsOrder() {
    ModelData data = getData();
    if ( data == null ) {
      return "";
    }
    ModelField fields[] = data.getFields();
    if ( fields == null ) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for ( int i = 0; i < columnModel.getColumnCount(); i++ ) {
      result.append( fields[ columnModel.getColumn( i ).getModelIndex() ].getIdentifier() ).append( ';' );
    }
    return result.toString();
  }
  
  /**
   * Set a list of columns.
   * @param fieldsOrder a list of visible columns in the view order separated by ";", must end with ";"
   */
  public void setFieldsOrder( String fieldsOrder ) {
    if ( fieldsOrder == null ) {
      fieldsOrder = "";
    }
    reorderFields( fieldsOrder.split( ";" ) );
  }

  /**
   * A list of pairs ( Identifier, column width in pixels ).
   * <p>A separator inside a pair is ":".<br/>
   * Pairs separator is ";".<br/>
   * No extra ";" at the end.<br/>
   * Columns have the view order. Only visible columns are printed.<br/>
   * The model order fields can be obtained using method {@link #getColumnsWidths()}.</p>
   * @return a list of fields and their widths
   */
  public String getFieldsOrderAndWidths() {
    ModelData data = getData();
    if ( data == null ) {
      return "";
    }
    ModelField fields[] = data.getFields();
    if ( fields == null ) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for ( int i = 0; i < columnModel.getColumnCount(); i++ ) {
      TableColumn column = columnModel.getColumn( i );
      if ( result.length() != 0 ) {
        result.append( ';' );
      }
      result.append( fields[ column.getModelIndex() ].getIdentifier() )
            .append( ':' )
            .append( column.getPreferredWidth() );
    }
    return result.toString();
  }

  /**
   * Set a list of fields and the width of each of them.
   * @param colWidths a list of pairs ( Identifier, column width ) in format of method {@link #getFieldsOrderAndWidths()}
   */
  public void setFieldsOrderAndWidths( String colWidths ) {
    reorderFields( setColumnsWidths( colWidths, true ) );
  }

  /**
   * A list of pairs ( Identifier, field width in pixels ).
   * <p>The output format is described at method {@link #getFieldsOrderAndWidths()}.<br/>
   * Fields are printed in the model order. Only visible fields are included.<br/>
   * The view order list can be obtained by method {@link #getFieldsOrderAndWidths()}.</p>
   * @return a list of fields and their widths
   */
  public String getColumnsWidths() {
    StringBuilder result = new StringBuilder();
    TableColumnModel colModel = getColumnModel();
    ModelField[] fields = getData().getFields();
    for ( int a = 0; a < fields.length; a++ ) {
      int idx = convertColumnIndexToView( a );
      if ( idx >= 0 ) {
        if ( result.length() != 0 ) {
          result.append( ';' );
        }
        result.append( fields[ a ].getIdentifier() )
              .append( ':' )
              .append( colModel.getColumn( idx ).getPreferredWidth() );
      }
    }
    return result.toString();
  }

  /**
   * Set widths of columns. The order and visibility of columns wouldn't be touched.
   * @param colWidths a list of pairs ( Identifier, column width ) in format of method {@link #getFieldsOrderAndWidths()}
   */
  public void setColumnsWidths( String colWidths ) {
    setColumnsWidths( colWidths, false );
  }

  private String[] setColumnsWidths( String colWidths, boolean addIfAbsent ) {
    if ( colWidths == null || colWidths.isEmpty() ) {
      return new String[ 0 ];
    }
    String[] widths = colWidths.split( ";" );
    if ( widths == null || widths.length == 0 ) {
      return new String[ 0 ];
    }
    Pattern pattern = Pattern.compile( "(\\S+):(\\d+)" );
    ModelField[] fields = getData().getFields();
    LinkedHashMap< String, Integer > fieldIndexes = ModelField.getIndexes( fields );
    ArrayList< String > ret = new ArrayList< String >();
    for ( String width : widths ) {
      Matcher matcher = pattern.matcher( width );
      if ( matcher.matches() ) {
        String colName = matcher.group( 1 );
        int colWidth = Integer.parseInt( matcher.group( 2 ) );
        Integer origIdx = fieldIndexes.get( colName );
        if ( origIdx == null ) {
          continue;
        }
        ret.add( colName );
        int fIdx = convertColumnIndexToView( origIdx );
        if ( fIdx < 0 ) {
          if ( addIfAbsent ) {
            addColumn( new TableColumn( origIdx ) );
            ModelField field = fields[ origIdx ];
            if ( field.isManageable() )
              field.setVisible( true );
            fIdx = convertColumnIndexToView( origIdx );
          } else
            continue;
        }
        TableColumn column = columnModel.getColumn( fIdx );
        column.setPreferredWidth( colWidth );
      }
    }
    return ret.toArray( new String[ ret.size() ] );
  }
  
  /**
   * Get index of column in table header by field identifier.
   * @param identifier field identifier.
   * @return index of column in the view order
   */
  public int convertColumnIndexToView( String identifier ) {
    return convertColumnIndexToView( getData().getIndexOfModelField( identifier ) );
  }

  @Override
  public void columnMoved( TableColumnModelEvent e ) {
  }

  /**
   * Value in a cell located at the given row and a column identified by name.
   * @param row row number
   * @param identifier column identifier
   * @return cell value
   */
  public Object getValueAt( int row, String identifier ) {
    return getData().getValue( row, identifier );
  }

  @Override
  protected JBroTableColumnModel createDefaultColumnModel() {
    return new JBroTableColumnModel( this );
  }

  @Override
  public void createDefaultColumnsFromModel() {
    TableColumnModel m = getColumnModel();
    if ( !( m instanceof JBroTableColumnModel ) ) {
      super.createDefaultColumnsFromModel();
      return;
    }
    JBroTableColumnModel gcm = ( JBroTableColumnModel )m;
    gcm.clear();
    ModelData data = getData();
    if ( data == null )
      return;
    Iterable< IModelFieldGroup > groups = data.getAllFieldGroupsFromTop( true );
    for ( IModelFieldGroup fieldGroup : groups ) {
      int groupCoords[] = data.getIndexOfModelFieldGroup( fieldGroup.getIdentifier() );
      int groupLevel = groupCoords[ 1 ];
      int groupX = groupCoords[ 0 ];
      gcm.addColumn( fieldGroup, groupX, groupLevel );
    }
  }

  @Override
  protected JTableHeader createDefaultTableHeader() {
    TableColumnModel m = getColumnModel();
    if ( !( m instanceof JBroTableColumnModel ) )
      return super.createDefaultTableHeader();
    JBroTableColumnModel gcm = ( JBroTableColumnModel )m;
    return new JBroTableHeader( gcm );
  }

  @Override
  public void columnSelectionChanged( ListSelectionEvent e ) {
    if ( getRowSelectionAllowed() ) {
      int leadRow = selectionModel.getLeadSelectionIndex();
      if ( leadRow >= 0 && leadRow < getRowCount() ) {
        Rectangle first = getUI().getSpanCoordinates( e.getFirstIndex(), leadRow );
        Rectangle last = getUI().getSpanCoordinates( e.getLastIndex(), leadRow );
        first = first.x < 0 || first.y < 0 ? last : last.x < 0 || last.y < 0 ? first : first.union( last );
        if ( first.x >= 0 && first.width > 1 ) {
          e = new ListSelectionEvent( e.getSource(), first.x, first.x + first.width - 1, e.getValueIsAdjusting() );
        }
      }
    }
    super.columnSelectionChanged( e );
  }
  
  private class HeaderHeightWatcher implements TableColumnModelListener {
    @Override
    public void columnAdded( TableColumnModelEvent evt ) {
      updateHeaderSize();
    }

    @Override
    public void columnRemoved( TableColumnModelEvent evt ) {
      updateHeaderSize();
    }

    @Override
    public void columnMoved( TableColumnModelEvent evt ) {
      updateHeaderSize();
    }

    @Override
    public void columnMarginChanged( ChangeEvent evt ) {
      updateHeaderSize();
    }

    @Override
    public void columnSelectionChanged( ListSelectionEvent evt ) {
    }
  }
}