package io.github.qualtagh.swing.table.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.LRUCache;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelSpan;
import io.github.qualtagh.swing.table.model.Utils;

public class JBroTableUI extends BasicTableUI {
  private final Map< String, Map< String, ModelSpan > > spans = new HashMap< String, Map< String, ModelSpan > >();
  private boolean spanCoveredCells[][] = new boolean[ 0 ][ 0 ];
  private final JTableHeader innerHeader = new JTableHeader();
  private boolean noDefaults;
  private volatile boolean rowsScrolling;
  private final LRUCache< List< Object >, Image > cellImagesCache = new LRUCache< List< Object >, Image >( 1000 );
  private Image draggedAreaCache;
  private boolean cacheUsed = true;
  
  public void clearCellImagesCache() {
    if ( !cellImagesCache.isEmpty() )
      cellImagesCache.clear();
  }
  
  public void clearDraggedAreaCache() {
    draggedAreaCache = null;
  }

  public boolean isCacheUsed() {
    return cacheUsed;
  }

  public void setCacheUsed( boolean cacheUsed ) {
    this.cacheUsed = cacheUsed;
    if ( !cacheUsed ) {
      rowsScrolling = true;
      clearCellImagesCache();
      clearDraggedAreaCache();
    }
  }

  void setRowsScrolling( boolean rowsScrolling ) {
    if ( !isCacheUsed() )
      rowsScrolling = true;
    if ( !this.rowsScrolling && rowsScrolling )
      clearCellImagesCache();
    this.rowsScrolling = rowsScrolling;
  }
  
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
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    boolean hasAnySpans = false;
    for ( int column = cMin; column <= cMax; column++ ) {
      JBroTableColumn aColumn = cm.getColumn( column );
      String columnName = ( String )aColumn.getIdentifier();
      if ( spans.containsKey( columnName ) ) {
        hasAnySpans = true;
        break;
      }
    }
    if ( hasAnySpans ) {
      if ( spanCoveredCells.length <= rMax - rMin || spanCoveredCells.length > 0 && spanCoveredCells[ 0 ].length <= cMax - cMin )
        spanCoveredCells = new boolean[ rMax - rMin + 1 ][ cMax - cMin + 1 ];
      else
        for ( int i = rMax - rMin; i >= 0; i-- )
          Arrays.fill( spanCoveredCells[ i ], 0, cMax - cMin + 1, false );
    }
    paintGrid( g, rMin, rMax, cMin, cMax );
    paintCells( g, rMin, rMax, cMin, cMax, hasAnySpans );
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

  private void paintCells( Graphics g, int rMin, int rMax, int cMin, int cMax, boolean hasAnySpans ) {
    JBroTableHeader header = ( JBroTableHeader )table.getTableHeader();
    JBroTableColumn draggedColumn = header == null ? null : header.getDraggedGroup();
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    IModelFieldGroup draggedField = cm.getModelField( draggedColumn );
    int columnMargin = cm.getColumnMargin();
    Rectangle spannedCellRect = new Rectangle();
    int rowCount = table.getRowCount();
    int columnCount = table.getColumnCount();
    ModelData data = ( ( JBroTable )table ).getData();
    Rectangle focusedRegion;
    if ( table.isFocusOwner() ) {
      if ( hasAnySpans )
        focusedRegion = getSpanCoordinates( table.getColumnModel().getSelectionModel().getLeadSelectionIndex(), table.getSelectionModel().getLeadSelectionIndex() );
      else
        focusedRegion = new Rectangle( table.getColumnModel().getSelectionModel().getLeadSelectionIndex(), table.getSelectionModel().getLeadSelectionIndex(), 1, 1 );
      focusedRegion.width += focusedRegion.x;
      focusedRegion.height += focusedRegion.y;
    } else
      focusedRegion = new Rectangle( -1, -1, -1, -1 );
    for ( int row = rMin; row <= rMax; row++ ) {
      int modelRow = table.convertRowIndexToModel( row );
      Rectangle cellRect = table.getCellRect( row, cMin, false );
      for ( int column = cMin; column <= cMax; column++ ) {
        JBroTableColumn aColumn = cm.getColumn( column );
        ModelField field = ( ModelField )cm.getModelField( aColumn );
        int columnWidth = aColumn.getWidth();
        cellRect.width = columnWidth - columnMargin;
        if ( !field.isDescendantOf( draggedField, true ) ) {
          if ( table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column )
            paintEditingCell( g, cellRect );
          else if ( !hasAnySpans || !spanCoveredCells[ row - rMin ][ column - cMin ] ) {
            Object value = null;
            boolean valueInitialized = false;
            boolean drawAsHeader = false;
            spannedCellRect.setBounds( cellRect );
            if ( hasAnySpans ) {
              String columnName = ( String )aColumn.getIdentifier();
              Map< String, ModelSpan > columnSpans = spans.get( columnName );
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
                  fromRow -= rMin;
                  if ( fromRow < -1 )
                    fromRow = -1;
                  toRow -= rMin;
                  fromCol -= cMin;
                  if ( fromCol < -1 )
                    fromCol = -1;
                  toCol -= cMin;
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
              if ( drawAsHeader ) {
                spannedCellRect.height += table.getRowMargin();
                spannedCellRect.width += columnMargin;
              }
            }
            if ( !valueInitialized )
              value = table.getValueAt( row, column );
            paintCell( g, spannedCellRect, row, column, value, drawAsHeader, focusedRegion.x <= column && column < focusedRegion.width && focusedRegion.y <= row && row < focusedRegion.height, modelRow, field.getIdentifier() );
          }
        }
        cellRect.x += columnWidth;
      }
    }
    if ( draggedColumn != null )
      paintDraggedArea( g, rMin, rMax, draggedColumn, header.getDraggedDistance() );
    rendererPane.removeAll();
  }

  private void paintDraggedArea( Graphics g, int rMin, int rMax, JBroTableColumn draggedColumn, int distance ) {
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    int startIndex = cm.getColumnAbsoluteIndex( draggedColumn );
    int endIndex = startIndex + draggedColumn.getColspan() - 1;
    Rectangle minCell = table.getCellRect( rMin, startIndex, true );
    Rectangle maxCell = table.getCellRect( rMax, endIndex, true );
    Rectangle vacatedColumnRect = minCell.union( maxCell );
    g.setColor( table.getParent().getBackground() );
    g.fillRect( vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width - ( vacatedColumnRect.width > 0 && table.getShowVerticalLines() && endIndex < cm.getColumnCount() - 1 ? 1 : 0 ), vacatedColumnRect.height );
    vacatedColumnRect.x += distance;
    if ( draggedAreaCache != null ) {
      g.drawImage( draggedAreaCache, vacatedColumnRect.x - 1, vacatedColumnRect.y, null );
      return;
    }
    Graphics gg;
    if ( isCacheUsed() ) {
      draggedAreaCache = new BufferedImage( vacatedColumnRect.width + 1, vacatedColumnRect.height, BufferedImage.TYPE_INT_RGB );
      gg = draggedAreaCache.getGraphics();
      gg.setColor( table.getParent().getBackground() );
      gg.fillRect( 0, 0, vacatedColumnRect.width + 1, vacatedColumnRect.height );
      gg.translate( 1 - vacatedColumnRect.x, -vacatedColumnRect.y );
    } else
      gg = g;
    gg.setColor( table.getBackground() );
    gg.fillRect( vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width, vacatedColumnRect.height );
    if ( table.getShowVerticalLines() ) {
      gg.setColor( table.getGridColor() );
      int x1 = vacatedColumnRect.x - 1;
      int y1 = vacatedColumnRect.y;
      int y2 = y1 + vacatedColumnRect.height - 1;
      gg.drawLine( x1, y1, x1, y2 );
      for ( int draggedColumnIndex = startIndex; draggedColumnIndex <= endIndex; draggedColumnIndex++ ) {
        x1 += cm.getColumn( draggedColumnIndex ).getWidth();
        gg.drawLine( x1, y1, x1, y2 );
      }
    }
    for ( int row = rMin; row <= rMax; row++ ) {
      for ( int draggedColumnIndex = startIndex; draggedColumnIndex <= endIndex; draggedColumnIndex++ ) {
        Rectangle r = table.getCellRect( row, draggedColumnIndex, false );
        r.x += distance;
        paintDraggedCell( gg, r, row, draggedColumnIndex );
        if ( table.getShowHorizontalLines() ) {
          gg.setColor( table.getGridColor() );
          Rectangle rcr = table.getCellRect( row, draggedColumnIndex, true );
          rcr.x += distance;
          int x1 = rcr.x;
          int y1 = rcr.y;
          int x2 = x1 + rcr.width - 1;
          int y2 = y1 + rcr.height - 1;
          gg.drawLine( x1, y2, x2, y2 );
        }
      }
    }
    if ( isCacheUsed() )
      g.drawImage( draggedAreaCache, vacatedColumnRect.x - 1, vacatedColumnRect.y, null );
  }

  private void paintDraggedCell( Graphics g, Rectangle cellRect, int row, int column ) {
    TableCellRenderer renderer = table.getCellRenderer( row, column );
    Component comp = table.prepareRenderer( renderer, row, column );
    JBroTableHeaderUI.htmlHack( g, comp, cellRect );
    rendererPane.paintComponent( g, comp, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
  }

  private void paintEditingCell( Graphics g, Rectangle cellRect ) {
    Component component = table.getEditorComponent();
    component.setBounds( cellRect );
    component.validate();
  }

  private void paintCell( Graphics g, Rectangle cellRect, int row, int column, Object value, boolean drawAsHeader, boolean hasFocus, int modelRow, String fieldId ) {
    boolean isSelected = table.isCellSelected( row, column );
    List< Object > key;
    if ( rowsScrolling )
      key = null;
    else {
      key = Arrays.< Object >asList( String.valueOf( value ), modelRow, fieldId, drawAsHeader, hasFocus, isSelected, cellRect.width, cellRect.height );
      Image image = cellImagesCache.get( key );
      if ( image != null ) {
        g.drawImage( image, cellRect.x, cellRect.y, null );
        return;
      }
    }
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
    Component comp = renderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
    Graphics gg;
    Image image;
    if ( rowsScrolling ) {
      gg = g;
      image = null;
    } else {
      image = new BufferedImage( cellRect.width, cellRect.height, BufferedImage.TYPE_INT_RGB );
      gg = image.getGraphics();
      gg.setColor( table.getParent().getBackground() );
      gg.fillRect( 0, 0, cellRect.width, cellRect.height );
      gg.translate( -cellRect.x, -cellRect.y );
    }
    gg.setColor( header != null ? header.getBackground() : isSelected ? table.getSelectionBackground() : table.getBackground() );
    gg.fillRect( cellRect.x, cellRect.y, cellRect.width, cellRect.height );
    if ( header != null ) {
      if ( rendererPane.getParent() != header )
        header.add( rendererPane );
    } else if ( rendererPane.getParent() != table )
      table.add( rendererPane );
    JBroTableHeaderUI.htmlHack( gg, comp, cellRect );
    rendererPane.paintComponent( gg, comp, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
    if ( !rowsScrolling ) {
      g.drawImage( image, cellRect.x, cellRect.y, null );
      cellImagesCache.put( key, image );
    }
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
    clearCellImagesCache();
    innerHeader.updateUI();
    if ( !noDefaults )
      super.installDefaults();
    table.setShowGrid( true );
    table.setIntercellSpacing( new Dimension( 1, 1 ) );
    if ( Utils.equals( table.getGridColor(), table.getBackground() ) )
      table.setGridColor( Color.GRAY.equals( table.getBackground() ) ? Color.LIGHT_GRAY : Color.GRAY );
    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics( table.getFont() );
    LookAndFeel.installProperty( table, "rowHeight", fm.getHeight() + fm.getMaxDescent() );
  }

  void setNoDefaults( boolean noDefaults ) {
    this.noDefaults = noDefaults;
  }
  
  /**
   * Get span bounds in pixels.
   * @param col any column of span
   * @param row any row of span
   * @param includeSpacing if false, return the true cell bounds
   * computed by subtracting the intercell spacing from the height
   * and widths of the column and row models
   * @return span bounds in pixels
   * @see javax.swing.JTable#getCellRect
   */
  public Rectangle getSpanCoordinatesInPixels( int col, int row, boolean includeSpacing ) {
    Rectangle rect = getSpanCoordinates( col, row );
    return table.getCellRect( rect.y, rect.x, includeSpacing ).union( table.getCellRect( rect.y + rect.height - 1, rect.x + rect.width - 1, includeSpacing ) );
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
    SpanWithId spanWithId = getSpanWithId( col, row );
    if ( spanWithId == null )
      return ret;
    TableColumnModel cm = table.getColumnModel();
    int columnCount = cm.getColumnCount();
    int rowCount = table.getRowCount();
    ModelData data = ( ( JBroTable )table ).getData();
    int minRow = row;
    for ( int i = row - 1; i >= 0; i-- ) {
      if ( spanWithId.id.equals( data.getValue( table.convertRowIndexToModel( i ), spanWithId.idColumnIdx ) ) )
        minRow = i;
      else
        break;
    }
    int maxRow = row;
    for ( int i = row + 1; i < rowCount; i++ ) {
      if ( spanWithId.id.equals( data.getValue( table.convertRowIndexToModel( i ), spanWithId.idColumnIdx ) ) )
        maxRow = i;
      else
        break;
    }
    Set< String > columns = spanWithId.span.getColumns();
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
    return ret;
  }
  
  public ModelSpan getSpan( int col, int row ) {
    SpanWithId spanWithId = getSpanWithId( col, row );
    return spanWithId == null ? null : spanWithId.span;
  }
  
  public Object getSpanValue( int col, int row ) {
    ModelSpan span = getSpan( col, row );
    if ( span == null )
      return table.getValueAt( col, row );
    int modelRow = table.convertRowIndexToModel( row );
    ModelData data = ( ( JBroTable )table ).getData();
    return data.getValue( modelRow, span.getValueColumn() );
  }
  
  private SpanWithId getSpanWithId( int col, int row ) {
    if ( col < 0 || row < 0 )
      return null;
    TableColumnModel cm = table.getColumnModel();
    if ( cm == null )
      return null;
    int columnCount = cm.getColumnCount();
    if ( col >= columnCount )
      return null;
    TableColumn aColumn = cm.getColumn( col );
    if ( aColumn == null )
      return null;
    String columnName = ( String )aColumn.getIdentifier();
    Map< String, ModelSpan > columnSpans = spans.get( columnName );
    if ( columnSpans == null )
      return null;
    int rowCount = table.getRowCount();
    if ( row >= rowCount )
      return null;
    int modelRow = table.convertRowIndexToModel( row );
    if ( modelRow < 0 )
      return null;
    ModelData data = ( ( JBroTable )table ).getData();
    if ( data == null || modelRow >= data.getRowsCount() )
      return null;
    for ( Map.Entry< String, ModelSpan > spanWithId : columnSpans.entrySet() ) {
      int idColumnIdx = data.getIndexOfModelField( spanWithId.getKey() );
      if ( idColumnIdx < 0 )
        continue;
      Object id = data.getValue( modelRow, idColumnIdx );
      if ( id == null )
        continue;
      return new SpanWithId( spanWithId.getValue(), id, idColumnIdx );
    }
    return null;
  }
  
  public Iterable< ModelSpan > getSpans() {
    if ( spans.isEmpty() )
      return Collections.EMPTY_SET;
    IdentityHashMap< ModelSpan, String > ret = new IdentityHashMap< ModelSpan, String >();
    for ( Map< String, ModelSpan > value : spans.values() )
      for ( ModelSpan span : value.values() )
        ret.put( span, null );
    return ret.keySet();
  }
  
  public Set< String > getSpannedColumns() {
    if ( spans.isEmpty() )
      return Collections.EMPTY_SET;
    Set< String > ret = new HashSet< String >();
    for ( Map< String, ModelSpan > value : spans.values() )
      for ( ModelSpan span : value.values() )
        ret.addAll( span.getColumns() );
    return ret;
  }

  void onRowsSelected( int firstIndex, int lastIndex ) {
    if ( spans.isEmpty() )
      return;
    int rc = table.getRowCount();
    int cc = table.getColumnCount();
    firstIndex = Math.min( Math.max( firstIndex, 0 ), rc - 1 );
    lastIndex = Math.min( Math.max( lastIndex, 0 ), rc - 1 );
    Rectangle firstRowRect = table.getCellRect( firstIndex, 0, false );
    Rectangle lastRowRect = table.getCellRect( lastIndex, cc - 1, false );
    Rectangle dirtyRegion = firstRowRect.union( lastRowRect );
    for ( int s = 0; s < 2; s++ ) {
      for ( int i = cc - 1; i >= 0; i-- ) {
        Rectangle rect = getSpanCoordinates( i, s == 0 ? firstIndex : lastIndex );
        if ( rect.width > 1 || rect.height > 1 ) {
          dirtyRegion = dirtyRegion.union( table.getCellRect( rect.y, rect.x, false ) );
          dirtyRegion = dirtyRegion.union( table.getCellRect( rect.y + rect.height - 1, rect.x + rect.width - 1, false ) );
        }
      }
    }
    table.repaint( dirtyRegion );
  }
  
  private static class SpanWithId {
    private final ModelSpan span;
    private final Object id;
    private final int idColumnIdx;

    public SpanWithId( ModelSpan span, Object id, int idColumnIdx ) {
      this.span = span;
      this.id = id;
      this.idColumnIdx = idColumnIdx;
    }
  }
}