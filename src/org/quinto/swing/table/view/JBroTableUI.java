package org.quinto.swing.table.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelSpan;

public class JBroTableUI extends BasicTableUI {
  private final Map< String, Map< String, ModelSpan > > spans = new HashMap< String, Map< String, ModelSpan > >();
  private boolean spanCoveredCells[][] = new boolean[ 0 ][ 0 ];
  private final JTableHeader innerHeader = new JTableHeader();
  private boolean noDefaults;
  
  @Override
  public void paint( Graphics g, JComponent c ) {
    Rectangle clip = g.getClipBounds();
    Rectangle bounds = table.getBounds();
    bounds.x = bounds.y = 0;
    int rowCount = table.getRowCount();
    int columnCount = table.getColumnCount();
    if ( rowCount <= 0 || columnCount <= 0 || !bounds.intersects( clip ) ) {
      paintDropLines( g );
      return;
    }
    Point upperLeft = clip.getLocation();
    Point lowerRight = new Point( clip.x + clip.width - 1, clip.y + clip.height - 1 );
    int rMin = table.rowAtPoint( upperLeft );
    int rMax = table.rowAtPoint( lowerRight );
    if ( rMin == -1 )
      rMin = 0;
    if ( rMax == -1 || rMax >= rowCount )
      rMax = rowCount - 1;
    int cMin = table.columnAtPoint( upperLeft );
    int cMax = table.columnAtPoint( lowerRight );
    if ( cMin == -1 )
      cMin = 0;
    if ( cMax == -1 || cMax >= columnCount )
      cMax = columnCount - 1;
    if ( spanCoveredCells.length <= rMax || spanCoveredCells.length > 0 && spanCoveredCells[ 0 ].length <= cMax )
      spanCoveredCells = new boolean[ rMax + 1 ][ cMax + 1 ];
    else
      for ( int i = rMin; i <= rMax; i++ )
        Arrays.fill( spanCoveredCells[ i ], cMin, cMax + 1, false );
    paintGrid( g, rMin, rMax, cMin, cMax );
    paintCells( g, rMin, rMax, cMin, cMax );
    paintDropLines( g );
  }

  private void paintDropLines( Graphics g ) {
    JTable.DropLocation loc = table.getDropLocation();
    if ( loc == null )
      return;
    Color color = UIManager.getColor( "Table.dropLineColor" );
    Color shortColor = UIManager.getColor( "Table.dropLineShortColor" );
    if ( color == null && shortColor == null )
      return;
    Rectangle rect;
    rect = getHDropLineRect( loc );
    if ( rect != null ) {
      int x = rect.x;
      int w = rect.width;
      if ( color != null ) {
        extendRect( rect, true );
        g.setColor( color );
        g.fillRect( rect.x, rect.y, rect.width, rect.height );
      }
      if ( !loc.isInsertColumn() && shortColor != null ) {
        g.setColor( shortColor );
        g.fillRect( x, rect.y, w, rect.height );
      }
    }
    rect = getVDropLineRect( loc );
    if ( rect != null ) {
      int y = rect.y;
      int h = rect.height;
      if ( color != null ) {
        extendRect( rect, false );
        g.setColor( color );
        g.fillRect( rect.x, rect.y, rect.width, rect.height );
      }
      if ( !loc.isInsertRow() && shortColor != null ) {
        g.setColor( shortColor );
        g.fillRect( rect.x, y, rect.width, h );
      }
    }
  }

  private Rectangle getHDropLineRect( JTable.DropLocation loc ) {
    if ( !loc.isInsertRow() )
      return null;
    int row = loc.getRow();
    int col = loc.getColumn();
    if ( col >= table.getColumnCount() )
      col--;
    Rectangle rect = table.getCellRect( row, col, true );
    if ( row >= table.getRowCount() ) {
      row--;
      Rectangle prevRect = table.getCellRect( row, col, true );
      rect.y = prevRect.y + prevRect.height;
    }
    if ( rect.y == 0 )
      rect.y = -1;
    else
      rect.y -= 2;
    rect.height = 3;
    return rect;
  }

  private Rectangle getVDropLineRect( JTable.DropLocation loc ) {
    if ( !loc.isInsertColumn() )
      return null;
    int col = loc.getColumn();
    Rectangle rect = table.getCellRect( loc.getRow(), col, true );
    if ( col >= table.getColumnCount() ) {
      col--;
      rect = table.getCellRect( loc.getRow(), col, true );
      rect.x = rect.x + rect.width;
    }
    if ( rect.x == 0 )
      rect.x = -1;
    else
      rect.x -= 2;
    rect.width = 3;
    return rect;
  }

  private Rectangle extendRect( Rectangle rect, boolean horizontal ) {
    if ( rect == null )
      return rect;
    if ( horizontal ) {
      rect.x = 0;
      rect.width = table.getWidth();
    } else {
      rect.y = 0;
      if ( table.getRowCount() != 0 ) {
        Rectangle lastRect = table.getCellRect( table.getRowCount() - 1, 0, true );
        rect.height = lastRect.y + lastRect.height;
      } else
        rect.height = table.getHeight();
    }
    return rect;
  }

  private void paintGrid( Graphics g, int rMin, int rMax, int cMin, int cMax ) {
    g.setColor( table.getGridColor() );
    Rectangle minCell = table.getCellRect( rMin, cMin, true );
    Rectangle maxCell = table.getCellRect( rMax, cMax, true );
    Rectangle damagedArea = minCell.union( maxCell );
    if ( table.getShowHorizontalLines() ) {
      int tableWidth = damagedArea.x + damagedArea.width;
      int y = damagedArea.y;
      for ( int row = rMin; row <= rMax; row++ ) {
        y += table.getRowHeight( row );
        g.drawLine( damagedArea.x, y - 1, tableWidth - 1, y - 1 );
      }
    }
    if ( table.getShowVerticalLines() ) {
      TableColumnModel cm = table.getColumnModel();
      int tableHeight = damagedArea.y + damagedArea.height;
      int x;
      if ( table.getComponentOrientation().isLeftToRight() ) {
        x = damagedArea.x;
        for ( int column = cMin; column <= cMax; column++ ) {
          int w = cm.getColumn( column ).getWidth();
          x += w;
          g.drawLine( x - 1, 0, x - 1, tableHeight - 1 );
        }
      } else {
        x = damagedArea.x;
        for ( int column = cMax; column >= cMin; column-- ) {
          int w = cm.getColumn( column ).getWidth();
          x += w;
          g.drawLine( x - 1, 0, x - 1, tableHeight - 1 );
        }
      }
    }
  }

  private void paintCells( Graphics g, int rMin, int rMax, int cMin, int cMax ) {
    JTableHeader header = table.getTableHeader();
    TableColumn draggedColumn = header == null ? null : header.getDraggedColumn();
    TableColumnModel cm = table.getColumnModel();
    int columnMargin = cm.getColumnMargin();
    Rectangle cellRect;
    Rectangle spannedCellRect = new Rectangle();
    TableColumn aColumn;
    int columnWidth;
    int rowCount = table.getRowCount();
    int columnCount = table.getColumnCount();
    ModelData data = ( ( JBroTable )table ).getData();
    Rectangle focusedRegion = new Rectangle( -1, -1, -1, -1 );
    if ( table.isFocusOwner() ) {
      focusedRegion = getSpanCoordinates( table.getColumnModel().getSelectionModel().getLeadSelectionIndex(), table.getSelectionModel().getLeadSelectionIndex() );
      focusedRegion.width += focusedRegion.x;
      focusedRegion.height += focusedRegion.y;
    }
    for ( int row = rMin; row <= rMax; row++ ) {
      int modelRow = table.convertRowIndexToModel( row );
      cellRect = table.getCellRect( row, cMin, false );
      for ( int column = cMin; column <= cMax; column++ ) {
        aColumn = cm.getColumn( column );
        columnWidth = aColumn.getWidth();
        cellRect.width = columnWidth - columnMargin;
        if ( aColumn != draggedColumn ) {
          if ( table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column )
            paintEditingCell( g, cellRect );
          else if ( !spanCoveredCells[ row ][ column ] ) {
            String columnName = ( String )aColumn.getIdentifier();
            Map< String, ModelSpan > columnSpans = spans.get( columnName );
            Object value = null;
            boolean valueInitialized = false;
            boolean drawAsHeader = false;
            spannedCellRect.setBounds( cellRect );
            if ( columnSpans != null ) {
              for ( Map.Entry< String, ModelSpan > spanWithId : columnSpans.entrySet() ) {
                String idColumn = spanWithId.getKey();
                int idIdx = data.getIndexOfModelField( idColumn );
                if ( idIdx < 0 )
                  continue;
                Object id = data.getValue( modelRow, idIdx );
                if ( id == null )
                  continue;
                ModelSpan span = spanWithId.getValue();
                Set< String > columns = span.getColumns();
                int fromCol = column - 1;
                for ( ; fromCol >= 0; fromCol-- ) {
                  TableColumn spanCoveredColumn = cm.getColumn( fromCol );
                  if ( !columns.contains( ( String )spanCoveredColumn.getIdentifier() ) )
                    break;
                  int width = spanCoveredColumn.getWidth();
                  spannedCellRect.width += width;
                  spannedCellRect.x -= width;
                }
                int toCol = column + 1;
                for ( ; toCol < columnCount; toCol++ ) {
                  TableColumn spanCoveredColumn = cm.getColumn( toCol );
                  if ( !columns.contains( ( String )spanCoveredColumn.getIdentifier() ) )
                    break;
                  spannedCellRect.width += spanCoveredColumn.getWidth();
                }
                int fromRow = row - 1;
                for ( ; fromRow >= 0; fromRow-- ) {
                  if ( !id.equals( data.getValue( table.convertRowIndexToModel( fromRow ), idIdx ) ) )
                    break;
                  int height = table.getRowHeight( fromRow );
                  spannedCellRect.height += height;
                  spannedCellRect.y -= height;
                }
                int toRow = row + 1;
                for ( ; toRow < rowCount; toRow++ ) {
                  if ( !id.equals( data.getValue( table.convertRowIndexToModel( toRow ), idIdx ) ) )
                    break;
                  spannedCellRect.height += table.getRowHeight( toRow );
                }
                for ( int i = fromRow + 1; i < spanCoveredCells.length; i++ ) {
                  if ( i >= toRow )
                    break;
                  for ( int j = fromCol + 1; j < spanCoveredCells[ i ].length; j++ ) {
                    if ( j >= toCol )
                      break;
                    spanCoveredCells[ i ][ j ] = true;
                  }
                }
                valueInitialized = true;
                value = data.getValue( modelRow, span.getValueColumn() );
                drawAsHeader = span.isDrawAsHeader();
                break;
              }
            }
            if ( !valueInitialized )
              value = table.getValueAt( row, column );
            if ( drawAsHeader ) {
              spannedCellRect.height += table.getRowMargin();
              spannedCellRect.width += columnMargin;
            }
            paintCell( g, spannedCellRect, row, column, value, drawAsHeader, focusedRegion.x <= column && column < focusedRegion.width && focusedRegion.y <= row && row < focusedRegion.height );
          }
        }
        cellRect.x += columnWidth;
      }
    }
    if ( draggedColumn != null )
      paintDraggedArea( g, rMin, rMax, draggedColumn, header.getDraggedDistance() );
    rendererPane.removeAll();
  }

  private int viewIndexForColumn( TableColumn aColumn ) {
    TableColumnModel cm = table.getColumnModel();
    for ( int column = 0; column < cm.getColumnCount(); column++ ) {
      if ( cm.getColumn( column ) == aColumn ) {
        return column;
      }
    }
    return -1;
  }

  private void paintDraggedArea( Graphics g, int rMin, int rMax, TableColumn draggedColumn, int distance ) {
    int draggedColumnIndex = viewIndexForColumn( draggedColumn );
    Rectangle minCell = table.getCellRect( rMin, draggedColumnIndex, true );
    Rectangle maxCell = table.getCellRect( rMax, draggedColumnIndex, true );
    Rectangle vacatedColumnRect = minCell.union( maxCell );
    g.setColor( table.getParent().getBackground() );
    g.fillRect( vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width - ( vacatedColumnRect.width > 0 && table.getShowVerticalLines() ? 1 : 0 ), vacatedColumnRect.height );
    vacatedColumnRect.x += distance;
    g.setColor( table.getBackground() );
    g.fillRect( vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width, vacatedColumnRect.height );
    if ( table.getShowVerticalLines() ) {
      g.setColor( table.getGridColor() );
      int x1 = vacatedColumnRect.x;
      int y1 = vacatedColumnRect.y;
      int x2 = x1 + vacatedColumnRect.width - 1;
      int y2 = y1 + vacatedColumnRect.height - 1;
      g.drawLine( x1 - 1, y1, x1 - 1, y2 );
      g.drawLine( x2, y1, x2, y2 );
    }
    for ( int row = rMin; row <= rMax; row++ ) {
      Rectangle r = table.getCellRect( row, draggedColumnIndex, false );
      r.x += distance;
      paintDraggedCell( g, r, row, draggedColumnIndex );
      if ( table.getShowHorizontalLines() ) {
        g.setColor( table.getGridColor() );
        Rectangle rcr = table.getCellRect( row, draggedColumnIndex, true );
        rcr.x += distance;
        int x1 = rcr.x;
        int y1 = rcr.y;
        int x2 = x1 + rcr.width - 1;
        int y2 = y1 + rcr.height - 1;
        g.drawLine( x1, y2, x2, y2 );
      }
    }
  }

  private void paintDraggedCell( Graphics g, Rectangle cellRect, int row, int column ) {
    TableCellRenderer renderer = table.getCellRenderer( row, column );
    Component component = table.prepareRenderer( renderer, row, column );
    rendererPane.paintComponent( g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
  }

  private void paintEditingCell( Graphics g, Rectangle cellRect ) {
    Component component = table.getEditorComponent();
    component.setBounds( cellRect );
    component.validate();
  }

  private void paintCell( Graphics g, Rectangle cellRect, int row, int column, Object value, boolean drawAsHeader, boolean hasFocus ) {
    TableCellRenderer renderer = null;
    JTableHeader header = null;
    if ( drawAsHeader ) {
      header = innerHeader;
      if ( header != null ) {
        if ( header instanceof JBroTableHeader ) {
          JBroTableHeader gth = ( JBroTableHeader )header;
          if ( gth.getLevelsQuantity() > 0 )
            renderer = gth.getDefaultRenderer( 0 );
        }
        if ( renderer == null )
          renderer = header.getDefaultRenderer();
      }
    }
    if ( renderer == null ) {
      renderer = table.getCellRenderer( row, column );
      header = null;
    }
    boolean isSelected = table.isCellSelected( row, column );
    if ( header != null ) {
      boolean parentUIdeterminesRolloverColumnItself = JBroTableHeaderUI.hasParentUI( renderer );
      row = -2;
      if ( parentUIdeterminesRolloverColumnItself && isSelected ) {
        column = -1;
        isSelected = false;
      } else
        column = -2;
      if ( !hasFocus )
        hasFocus = isSelected;
    }
    Component component = renderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
    g.setColor( header != null ? header.getBackground() : isSelected ? table.getSelectionBackground() : table.getBackground() );
    g.fillRect( cellRect.x, cellRect.y, cellRect.width, cellRect.height );
    if ( header != null )
      header.add( rendererPane );
    else
      table.add( rendererPane );
    rendererPane.paintComponent( g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
  }
  
  public JBroTableUI withSpan( ModelSpan span ) {
    if ( span == null || span.getIdColumn() == null || span.getColumns().isEmpty() )
      throw new IllegalArgumentException( "Span formed not properly: " + span );
    for ( String column : span.getColumns() ) {
      Map< String, ModelSpan > columnSpans = spans.get( column );
      if ( columnSpans == null ) {
        columnSpans = new LinkedHashMap< String, ModelSpan >();
        spans.put( column, columnSpans );
      }
      ModelSpan prev = columnSpans.put( span.getIdColumn(), span );
      if ( prev != null )
        throw new IllegalArgumentException( "Span column intersection: column " + column + " is used in both spans " + prev + " and " + span );
    }
    return this;
  }

  @Override
  protected void installDefaults() {
    if ( !noDefaults )
      super.installDefaults();
    table.setShowGrid( true );
    table.setIntercellSpacing( new Dimension( 1, 1 ) );
  }

  public boolean isNoDefaults() {
    return noDefaults;
  }

  public void setNoDefaults(boolean noDefaults) {
    this.noDefaults = noDefaults;
  }
  
  /**
   * Find a span at position ( col, row ). Return its starting point and size in cells.
   * The value returned is <b>not</b> the span bounds in pixels.
   * @param col any column of span
   * @param row any row of span
   * @return a rectangle( min column of this span, min row of this span, columns quantity of this span, rows quantity of this span ),
   * or rectangle( col, row, 1, 1 ) if there's no span at ( col, row )
   */
  public Rectangle getSpanCoordinates( int col, int row ) {
    Rectangle ret = new Rectangle( col, row, 1, 1 );
    if ( col < 0 || row < 0 )
      return ret;
    TableColumnModel cm = table.getColumnModel();
    if ( cm == null )
      return ret;
    int columnCount = cm.getColumnCount();
    if ( col >= columnCount )
      return ret;
    TableColumn aColumn = cm.getColumn( col );
    if ( aColumn == null )
      return ret;
    String columnName = ( String )aColumn.getIdentifier();
    Map< String, ModelSpan > columnSpans = spans.get( columnName );
    if ( columnSpans == null )
      return ret;
    int rowCount = table.getRowCount();
    if ( row >= rowCount )
      return ret;
    int modelRow = table.convertRowIndexToModel( row );
    if ( modelRow < 0 )
      return ret;
    ModelData data = ( ( JBroTable )table ).getData();
    if ( data == null || modelRow >= data.getRowsCount() )
      return ret;
    for ( Map.Entry< String, ModelSpan > spanWithId : columnSpans.entrySet() ) {
      int idColumnIdx = data.getIndexOfModelField( spanWithId.getKey() );
      if ( idColumnIdx < 0 )
        continue;
      Object id = data.getValue( modelRow, idColumnIdx );
      if ( id == null )
        continue;
      int minRow = row;
      for ( int i = row - 1; i >= 0; i-- ) {
        if ( id.equals( data.getValue( table.convertRowIndexToModel( i ), idColumnIdx ) ) )
          minRow = i;
        else
          break;
      }
      int maxRow = row;
      for ( int i = row + 1; i < rowCount; i++ ) {
        if ( id.equals( data.getValue( table.convertRowIndexToModel( i ), idColumnIdx ) ) )
          maxRow = i;
        else
          break;
      }
      ModelSpan span = spanWithId.getValue();
      Set< String > columns = span.getColumns();
      int minCol = col;
      for ( int i = col - 1; i >= 0; i-- ) {
        TableColumn spanCoveredColumn = cm.getColumn( i );
        if ( columns.contains( ( String )spanCoveredColumn.getIdentifier() ) )
          minCol = i;
        else
          break;
      }
      int maxCol = col;
      for ( int i = col + 1; i < columnCount; i++ ) {
        TableColumn spanCoveredColumn = cm.getColumn( i );
        if ( columns.contains( ( String )spanCoveredColumn.getIdentifier() ) )
          maxCol = i;
        else
          break;
      }
      ret.setBounds( minCol, minRow, maxCol - minCol + 1, maxRow - minRow + 1 );
      break;
    }
    return ret;
  }
}