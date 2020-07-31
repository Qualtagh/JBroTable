package io.github.qualtagh.swing.table.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.LRUCache;
import io.github.qualtagh.swing.table.model.ModelData;

public class JBroTableHeaderUI extends BasicTableHeaderUI {
  private static final Logger LOGGER = Logger.getLogger( JBroTableHeaderUI.class );
  private static final Map< String, Boolean > EXISTING_PARENT_UIS = new HashMap< String, Boolean >();
  static final Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR );
  
  private JBroTableColumn selectedColumn;
  private final JBroTable table;
  private List< ComponentUI > delegates;
  private List< CellRendererPane > rendererPanes;
  private List< JTableHeader > headers;
  private List< Integer > rowHeights;
  private boolean updating;
  private ComponentUI headerDelegate;
  private ReverseBorder lastBorder;
  private CustomTableHeaderRenderer customRenderer;
  private int heightsCache[];
  private boolean cacheUsed = true;
  private final LRUCache< List< Object >, Image > cellImagesCache = new LRUCache< List< Object >, Image >( 1000 );
  
  public JBroTableHeaderUI( JBroTable table ) {
    this.table = table;
    updateLookAndFeel();
  }
  
  void updateLookAndFeel() {
    if ( updating )
      return;
    updating = true;
    if ( header != null ) {
      uninstallUI( header );
      installUI( header );
    }
    updating = false;
  }
  
  public void clearCellImagesCache() {
    if ( !cellImagesCache.isEmpty() )
      cellImagesCache.clear();
  }

  public boolean isCacheUsed() {
    return cacheUsed;
  }

  public void setCacheUsed( boolean cacheUsed ) {
    this.cacheUsed = cacheUsed;
    if ( !cacheUsed )
      clearCellImagesCache();
  }

  public CustomTableHeaderRenderer getCustomRenderer() {
    return customRenderer;
  }

  public void setCustomRenderer( CustomTableHeaderRenderer customRenderer ) {
    this.customRenderer = customRenderer;
  }
  
  public JBroTableHeader getHeader() {
    return ( JBroTableHeader )header;
  }
  
  public JTableHeader getHeader( int level ) {
    return headers.get( level );
  }
  
  private CellRendererPane getRendererPane( ComponentUI ui ) {
    Object o = getField( "rendererPane", ui );
    if ( o instanceof CellRendererPane )
      return ( CellRendererPane )o;
    return null;
  }
  
  private Object getField( String fieldName, ComponentUI ui ) {
    if ( ui == null )
      return null;
    try {
      Field field = BasicTableHeaderUI.class.getDeclaredField( fieldName );
      boolean accessible = field.isAccessible();
      if ( !accessible )
        field.setAccessible( true );
      Object ret = field.get( ui );
      if ( !accessible )
        field.setAccessible( false );
      return ret;
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchFieldException e ) {
      LOGGER.error( null, e );
    } catch ( SecurityException e ) {
      LOGGER.error( null, e );
    }
    return null;
  }
  
  private void setField( String fieldName, Object value, ComponentUI ui ) {
    if ( ui == null )
      return;
    try {
      Field field = BasicTableHeaderUI.class.getDeclaredField( fieldName );
      boolean accessible = field.isAccessible();
      if ( !accessible )
        field.setAccessible( true );
      field.set( ui, value );
      if ( !accessible )
        field.setAccessible( false );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchFieldException e ) {
      LOGGER.error( null, e );
    } catch ( SecurityException e ) {
      LOGGER.error( null, e );
    }
  }
  
  private Object call( String methodName, ComponentUI ui ) {
    return call( methodName, null, ui, null );
  }
  
  private Object call( String methodName, Class parameters[], ComponentUI ui, Object args[] ) {
    return call( BasicTableHeaderUI.class, methodName, parameters, ui, args );
  }
  
  private Object call( Class clazz, String methodName, Class parameters[], ComponentUI ui, Object args[] ) {
    if ( ui == null )
      return null;
    try {
      Method method = parameters == null ? clazz.getDeclaredMethod( methodName ) : clazz.getDeclaredMethod( methodName, parameters );
      boolean accessible = method.isAccessible();
      if ( !accessible )
        method.setAccessible( true );
      return args == null ? method.invoke( ui ) : method.invoke( ui, args );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchMethodException e ) {
      LOGGER.error( null, e );
    } catch ( InvocationTargetException e ) {
      LOGGER.error( null, e );
      LOGGER.error( null, ( ( InvocationTargetException )e ).getCause() );
    }
    return null;
  }

  @Override
  protected void rolloverColumnUpdated( int oldColumn, int newColumn ) {
    for ( ComponentUI delegate : delegates )
      call( "rolloverColumnUpdated", new Class[]{ int.class, int.class }, delegate, new Object[]{ oldColumn, newColumn } );
    super.rolloverColumnUpdated( oldColumn, newColumn );
  }
  
  private void rolloverColumnUpdated( int oldColumn, int newColumn, int level ) {
    call( "rolloverColumnUpdated", new Class[]{ int.class, int.class }, delegates.get( level ), new Object[]{ oldColumn, newColumn } );
  }

  @Override
  protected void uninstallKeyboardActions() {
    super.uninstallKeyboardActions();
    for ( ComponentUI delegate : delegates )
      call( "uninstallKeyboardActions", delegate );
  }

  @Override
  protected void uninstallListeners() {
    super.uninstallListeners();
    for ( ComponentUI delegate : delegates )
      call( "uninstallListeners", delegate );
  }

  @Override
  protected void uninstallDefaults() {
    super.uninstallDefaults();
    for ( ComponentUI delegate : delegates )
      call( "uninstallDefaults", delegate );
  }

  @Override
  public void uninstallUI( JComponent c ) {
    for ( int i = 0; i < delegates.size(); i++ )
      call( "uninstallUI", new Class[]{ JComponent.class }, delegates.get( i ), new Object[]{ headers.get( i ) } );
    call( "uninstallUI", new Class[]{ JComponent.class }, headerDelegate, new Object[]{ header } );
    super.uninstallDefaults();
    super.uninstallListeners();
    super.uninstallKeyboardActions();
    header.removeAll();
    rendererPane = null;
    delegates.clear();
    headers.clear();
    rendererPanes.clear();
    clearCellImagesCache();
  }

  @Override
  protected void installKeyboardActions() {
    for ( ComponentUI delegate : delegates )
      call( "installKeyboardActions", delegate );
    super.installKeyboardActions();
  }

  @Override
  protected void installListeners() {
    for ( ComponentUI delegate : delegates )
      call( "installListeners", delegate );
    super.installListeners();
    for ( MouseListener ml : header.getMouseListeners() )
      if ( ml instanceof BasicTableHeaderUI.MouseInputHandler )
        header.removeMouseListener( ml );
    for ( MouseMotionListener ml : header.getMouseMotionListeners() )
      if ( ml instanceof BasicTableHeaderUI.MouseInputHandler )
        header.removeMouseMotionListener( ml );
  }

  @Override
  protected void installDefaults() {
    for ( ComponentUI delegate : delegates )
      call( "installDefaults", delegate );
    super.installDefaults();
  }
  
  public void updateModel() {
    int levelsCnt = table.getData() == null ? 0 : table.getData().getFieldGroups().size();
    if ( levelsCnt <= delegates.size() )
      return;
    JBroTableHeader header = getHeader();
    try {
      Class uiClass = UIManager.getLookAndFeelDefaults().getUIClass( "TableHeaderUI" );
      Method createUImethod = uiClass.getMethod( "createUI", JComponent.class );
      headerDelegate = ( ComponentUI )createUImethod.invoke( null, header );
      call( "installUI", new Class[]{ JComponent.class }, headerDelegate, new Object[]{ header } );
      Arrays.fill( heightsCache, -1 );
      for ( int level = delegates.size(); level < levelsCnt; level++ ) {
        ComponentUI delegate = ( ComponentUI )createUImethod.invoke( null, header );
        delegates.add( delegate );
        JTableHeader levelHeader = header.createDelegateForLevel( level );
        call( "installUI", new Class[]{ JComponent.class }, delegate, new Object[]{ levelHeader } );
        headers.add( levelHeader );
        CellRendererPane delegateRendererPane = getRendererPane( delegate );
        rendererPanes.add( delegateRendererPane );
      }
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchMethodException e ) {
      LOGGER.error( null, e );
    } catch ( InvocationTargetException e ) {
      LOGGER.error( null, e );
      LOGGER.error( null, ( ( InvocationTargetException )e ).getCause() );
    }
  }

  @Override
  public void installUI( JComponent c ) {
    if ( !( c instanceof JBroTableHeader ) )
      throw new IllegalArgumentException( "Not a groupable header: " + c );
    JBroTableHeader header = ( JBroTableHeader )c;
    this.header = header;
    int levelsCnt = table.getData() == null ? 0 : table.getData().getFieldGroups().size();
    delegates = new ArrayList< ComponentUI >( levelsCnt );
    headers = new ArrayList< JTableHeader >( levelsCnt );
    rendererPanes = new ArrayList< CellRendererPane >( levelsCnt );
    if ( heightsCache == null || heightsCache.length < levelsCnt )
      heightsCache = new int[ levelsCnt ];
    updateModel();
    super.installUI( c );
    SwingUtilities.updateComponentTreeUI( header );
    for ( JTableHeader delegateHeader : headers )
      SwingUtilities.updateComponentTreeUI( delegateHeader );
  }
  
  @Override
  public void update( Graphics g, JComponent c ) {
    for ( ComponentUI delegate : delegates )
      call( ComponentUI.class, "update", new Class[]{ Graphics.class, JComponent.class }, delegate, new Object[]{ g, c } );
    super.update( g, c );
  }

  private TableCellRenderer getRenderer( TableColumn column ) {
    TableCellRenderer renderer = column.getHeaderRenderer();
    if ( renderer == null ) {
      if ( column instanceof JBroTableColumn ) {
        JBroTableColumn dtc = ( JBroTableColumn )column;
        renderer = headers.get( dtc.getY() ).getDefaultRenderer();
      } else
        renderer = header.getDefaultRenderer();
    }
    return renderer;
  }

  @Override
  public void paint( Graphics g, JComponent c ) {
    JBroTableColumnModel groupModel = getTableColumnModel();
    if ( groupModel == null )
      return;
    Rectangle clip = g.getClipBounds();
    Point left = clip.getLocation();
    Point right = new Point( clip.x + clip.width - 1, clip.y );
    int rMin = getRowAtPoint( left );
    int cMin = header.columnAtPoint( left );
    int cMax = header.columnAtPoint( right );
    int columnCount = groupModel.getColumnCount();
    if ( cMax < 0 || cMax >= columnCount ) {
      cMax = columnCount - 1;
      if ( cMin > cMax )
        cMin = cMax;
    }
    if ( cMin < 0 )
      cMin = 0;
    JBroTableHeader header = getHeader();
    int headerHeight = calculateHeaderHeight();
    Rectangle cellRect = new Rectangle();
    JBroTableColumn draggedColumn = header.getDraggedGroup();
    List< JBroTableColumn > currentColumns = new ArrayList< JBroTableColumn >();
    List< Integer > coords = new ArrayList< Integer >();
    currentColumns.add( null );
    boolean draggedColumnMet = false;
    int calcStartXFrom = 0;
    for ( int cIdx = cMin; cIdx <= cMax; cIdx++ ) {
      JBroTableColumn column = groupModel.getColumn( cIdx );
      Collection< JBroTableColumn > columnParents = groupModel.getColumnParents( column, true );
      int level = 0;
      boolean addAbsent = false;
      boolean doNotPaintCells = draggedColumnMet && columnParents.contains( draggedColumn );
      boolean firstColumnParentsNeedToBeRepainted = cIdx == cMin && cMin > 0;
      for ( JBroTableColumn columnParent : columnParents ) {
        if ( firstColumnParentsNeedToBeRepainted ) {
          int calcStartXTo = groupModel.getColumnAbsoluteIndex( columnParent );
          for ( int i = calcStartXFrom; i < calcStartXTo; i++ ) {
            JBroTableColumn col = groupModel.getColumn( i );
            cellRect.x += col.getWidth();
          }
          calcStartXFrom = calcStartXTo;
        }
        if ( !addAbsent ) {
          JBroTableColumn currentColumn = currentColumns.get( level );
          if ( columnParent != currentColumn ) {
            while ( currentColumns.size() > level )
              currentColumns.remove( level );
            while ( coords.size() > level )
              coords.remove( level );
            addAbsent = true;
          }
          level++;
        }
        if ( addAbsent ) {
          currentColumns.add( columnParent );
          cellRect.y = coords.isEmpty() ? 0 : coords.get( coords.size() - 1 );
          cellRect.width = getGroupWidth( columnParent );
          if ( columnParent == column )
            cellRect.height = headerHeight - cellRect.y;
          else
            cellRect.height = getGroupHeight( columnParent );
          if ( draggedColumn == columnParent ) {
            g.setColor( header.getParent().getBackground() );
            g.fillRect( cellRect.x, cellRect.y, cellRect.width, headerHeight - cellRect.y );
            draggedColumnMet = true;
            doNotPaintCells = true;
          } else if ( !doNotPaintCells )
            paintCell( g, cellRect, columnParent );
          if ( columnParent != column ) {
            cellRect.y += cellRect.height;
            coords.add( cellRect.y );
          }
        }
      }
      cellRect.x += cellRect.width;
    }
    if ( draggedColumn != null ) {
      Rectangle bounds = getGroupHeaderBoundsFor( draggedColumn );
      bounds.x += header.getDraggedDistance();
      paintWithChildren( g, groupModel, draggedColumn, bounds );
    }
    rendererPane.removeAll();
  }
  
  private void paintWithChildren( Graphics g, JBroTableColumnModel groupModel, JBroTableColumn parent, Rectangle initial ) {
    Rectangle bounds = new Rectangle( initial );
    g.setColor( header.getParent().getBackground() );
    g.fillRect( bounds.x, bounds.y, bounds.width, bounds.height );
    paintCell( g, bounds, parent );
    bounds.y += bounds.height;
    for ( JBroTableColumn child : groupModel.getColumnChildren( parent ) ) {
      bounds.setSize( getGroupSize( child ) );
      paintWithChildren( g, groupModel, child, bounds );
      bounds.x += bounds.width;
    }
  }
  
  /**
   * A hack to prepare HTML-based Swing components: size initialization is called inside paint method.
   * @param g a non-null Graphics object
   * @param component an HTML-based component that needs to be initialized
   * @param cellRect a space where the component should be painted
   */
  public static void htmlHack( Graphics g, Component component, Rectangle cellRect ) {
    String text;
    if ( component instanceof JLabel )
      text = ( ( JLabel )component ).getText();
    else if ( component instanceof AbstractButton )
      text = ( ( AbstractButton )component ).getText();
    else if ( component instanceof JToolTip )
      text = ( ( JToolTip )component ).getTipText();
    else
      text = null;
    if ( !BasicHTML.isHTMLString( text ) )
      return;
    component.setBounds( cellRect );
    Graphics gg = g.create( -cellRect.width, -cellRect.height, cellRect.width, cellRect.height );
    try {
      component.paint( gg );
    } catch ( NullPointerException e ) {
      // Thrown on applet reinitialization.
    } finally {
      gg.dispose();
    }
  }

  private void paintCell( Graphics g, Component component, Rectangle cellRect ) {
    htmlHack( g, component, cellRect );
    rendererPane.add( component );
    rendererPane.paintComponent( g, component, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
  }
  
  public boolean isLeaf( JBroTableColumn column ) {
    return column.getY() + column.getRowspan() == headers.size();
  }

  private void paintCell( Graphics g, Rectangle cellRect, JBroTableColumn group ) {
    Object value = group.getHeaderValue();
    int row = group.getY();
    List< Object > key;
    if ( isCacheUsed() ) {
      key = Arrays.< Object >asList( String.valueOf( value ), row, group.getIdentifier(), group == getHeader().getDraggedGroup(), group == selectedColumn, cellRect.width, cellRect.height );
      Image image = cellImagesCache.get( key );
      if ( image != null ) {
        g.drawImage( image, cellRect.x, cellRect.y, null );
        return;
      }
    } else
      key = null;
    TableCellRenderer renderer = getRenderer( group );
    boolean parentUIdeterminesRolloverColumnItself = hasParentUI( renderer );
    boolean rollover = parentUIdeterminesRolloverColumnItself ? group == getHeader().getDraggedGroup() : group == selectedColumn;
    table.setCurrentLevel( group.getY() );
    JBroTableColumnModel tcm = getTableColumnModel();
    int viewColumn = tcm.getColumnRelativeIndex( group );
    Component comp = renderer.getTableCellRendererComponent( table, value, rollover, rollover, row, viewColumn );
    table.setCurrentLevel( null );
    if ( !parentUIdeterminesRolloverColumnItself && comp instanceof JComponent && group == getHeader().getDraggedGroup() ) {
      Border border = ( ( JComponent )comp ).getBorder();
      if ( border != null ) {
        if ( lastBorder == null || lastBorder.getDelegate() != border )
          lastBorder = new ReverseBorder( border );
        ( ( JComponent )comp ).setBorder( lastBorder );
      }
    }
    if ( customRenderer != null ) {
      IModelFieldGroup dataField = null;
      int modelColumn = group.getModelIndex();
      ModelData data = table.getData();
      if ( data != null ) {
        if ( modelColumn >= 0 && data.getFields() != null && modelColumn < data.getFields().length )
          dataField = data.getFields()[ modelColumn ];
        else {
          int coords[] = data.getIndexOfModelFieldGroup( String.valueOf( group.getIdentifier() ) );
          int level = coords[ 1 ];
          int col = coords[ 0 ];
          if ( level >= 0 && col >= 0 ) {
            List< IModelFieldGroup[] > fields = data.getFieldGroups();
            if ( fields.size() > level ) {
              IModelFieldGroup levelFields[] = fields.get( level );
              if ( levelFields.length > col )
                dataField = levelFields[ col ];
            }
          }
        }
      }
      comp = customRenderer.getTableCellRendererComponent( comp, table, value, rollover, rollover, group == getHeader().getDraggedGroup(), row, viewColumn, modelColumn, dataField );
    }
    if ( isCacheUsed() ) {
      Image image = new BufferedImage( cellRect.width, cellRect.height, BufferedImage.TYPE_INT_RGB );
      Graphics gg = image.getGraphics();
      gg.setColor( header.getParent().getBackground() );
      gg.fillRect( 0, 0, cellRect.width, cellRect.height );
      gg.translate( -cellRect.x, -cellRect.y );
      paintCell( gg, comp, cellRect );
      g.drawImage( image, cellRect.x, cellRect.y, null );
      cellImagesCache.put( key, image );
    } else
      paintCell( g, comp, cellRect );
  }

  public int getGroupHeight( JBroTableColumn group ) {
    if ( group == null )
      return 0;
    int height = 0;
    int from = group.getY();
    int to = from + group.getRowspan();
    for ( int level = from; level < to; level++ )
      height += getRowHeight( level );
    return height;
  }

  public int getGroupWidth( JBroTableColumn group ) {
    int width = 0;
    JBroTableColumnModel groupModel = getTableColumnModel();
    List< JBroTableColumn > children = groupModel.getColumnChildren( group );
    if ( children.isEmpty() )
      width = group == null ? 0 : group.getWidth();
    else
      for ( JBroTableColumn column : children )
        width += groupModel.getWidth( column );
    return width;
  }

  public Dimension getGroupSize( JBroTableColumn group ) {
    return new Dimension( getGroupWidth( group ), getGroupHeight( group ) );
  }

  private int calculateHeaderHeight() {
    int height = 0;
    int levelsCnt = table.getData() == null ? 0 : table.getData().getFieldGroups().size();
    for ( int level = 0; level < levelsCnt; level++ )
      height += getRowHeight( level );
    return height;
  }

  public int getRowHeight( int level ) {
    int ret = heightsCache[ level ];
    if ( ret >= 0 )
      return ret;
    if ( rowHeights != null && rowHeights.size() > level ) {
      Integer height = rowHeights.get( level );
      if ( height != null )
        return heightsCache[ level ] = height;
    }
    int mHeight = 0;
    JBroTableColumnModel groupModel = getTableColumnModel();
    for ( int column = 0; column < groupModel.getColumnCount(); column++ ) {
      JBroTableColumn parent = groupModel.getColumn( column );
      while ( parent != null ) {
        if ( parent.getY() <= level && level < parent.getY() + parent.getRowspan() ) {
          TableCellRenderer renderer = headers.get( level ).getDefaultRenderer();
          Component comp = renderer.getTableCellRendererComponent( header.getTable(), parent.getHeaderValue(), false, false, -1, column );
          mHeight = Math.max( mHeight, comp.getPreferredSize().height );
        }
        parent = groupModel.getColumnParent( parent );
      }
    }
    return heightsCache[ level ] = mHeight;
  }

  @Override
  public Dimension getPreferredSize( JComponent c ) {
    int width = 0;
    Enumeration enumeration = header.getColumnModel().getColumns();
    while ( enumeration.hasMoreElements() ) {
      JBroTableColumn aColumn = ( JBroTableColumn )enumeration.nextElement();
      width += aColumn.getPreferredWidth();
    }
    return new Dimension( width, calculateHeaderHeight() );
  }

  private void selectColumn( JBroTableColumn newSelectedColumn ) {
    if ( selectedColumn != newSelectedColumn ) {
      JBroTableColumn oldSelectedColumn = selectedColumn;
      selectedColumn = newSelectedColumn;
      Rectangle repaintRect = null;
      if ( oldSelectedColumn != null )
        repaintRect = getGroupHeaderBoundsFor( oldSelectedColumn );
      if ( newSelectedColumn != null ) {
        Rectangle rect = getGroupHeaderBoundsFor( newSelectedColumn );
        if ( repaintRect == null )
          repaintRect = rect;
        else if ( rect != null && repaintRect.intersects( rect ) )
          repaintRect = repaintRect.union( rect );
        else
          header.repaint( rect );
      }
      if ( repaintRect != null )
        header.repaint( repaintRect );
      JBroTableColumnModel columnModel = getTableColumnModel();
      int oldAbs = columnModel.getColumnAbsoluteIndex( oldSelectedColumn );
      int newAbs = columnModel.getColumnAbsoluteIndex( newSelectedColumn );
      int level;
      if ( oldSelectedColumn == null ) {
        if ( newSelectedColumn == null )
          level = -1;
        else
          level = newSelectedColumn.getY();
      } else if ( newSelectedColumn == null )
        level = oldSelectedColumn.getY();
      else {
        int oldY = oldSelectedColumn.getY();
        int oldYR = oldY + oldSelectedColumn.getRowspan() - 1;
        int newY = newSelectedColumn.getY();
        int newYR = newY + newSelectedColumn.getRowspan() - 1;
        if ( oldY <= newY && newY <= oldYR )
          level = newY;
        else if ( newY <= oldY && oldY <= newYR )
          level = oldY;
        else {
          level = newY;
          rolloverColumnUpdated( oldAbs, newAbs, oldY );
        }
      }
      if ( level >= 0 )
        rolloverColumnUpdated( oldAbs, newAbs, level );
      super.rolloverColumnUpdated( oldAbs, newAbs );
      if ( oldSelectedColumn != null && newSelectedColumn != null && oldSelectedColumn.getY() != newSelectedColumn.getY() ) {
        BasicTableHeaderUI parentUI = getParentUI( getRenderer( oldSelectedColumn ) );
        setField( "rolloverColumn", -1, parentUI );
      }
      if ( oldSelectedColumn != null || newSelectedColumn != null ) {
        BasicTableHeaderUI parentUI = getParentUI( getRenderer( newSelectedColumn == null ? oldSelectedColumn : newSelectedColumn ) );
        setField( "rolloverColumn", columnModel.getColumnRelativeIndex( newSelectedColumn ), parentUI );
      }
    }
  }
  
  protected static boolean hasParentUI( TableCellRenderer renderer ) {
    if ( renderer == null )
      return false;
    Class clazz = renderer.getClass();
    String className = clazz.getName();
    Boolean parentUIexists = EXISTING_PARENT_UIS.get( className );
    if ( parentUIexists != null )
      return parentUIexists;
    getParentUI( renderer );
    parentUIexists = EXISTING_PARENT_UIS.get( className );
    if ( parentUIexists != null )
      return parentUIexists;
    return false;
  }
  
  private static BasicTableHeaderUI getParentUI( TableCellRenderer renderer ) {
    if ( renderer == null )
      return null;
    Class clazz = renderer.getClass();
    String className = clazz.getName();
    try {
      Boolean parentUIexists = EXISTING_PARENT_UIS.get( className );
      if ( parentUIexists != null && !parentUIexists )
        return null;
      Field field = clazz.getDeclaredField( "this$0" );
      boolean accessible = field.isAccessible();
      if ( !accessible )
        field.setAccessible( true );
      Object ret = field.get( renderer );
      if ( !accessible )
        field.setAccessible( false );
      if ( ret instanceof BasicTableHeaderUI ) {
        EXISTING_PARENT_UIS.put( className, Boolean.TRUE );
        return ( BasicTableHeaderUI )ret;
      }
    } catch ( NoSuchFieldException e ) {
      // do nothing
    } catch ( SecurityException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    }
    EXISTING_PARENT_UIS.put( className, Boolean.FALSE );
    return null;
  }

  private JBroTableColumnModel getTableColumnModel() {
    return ( JBroTableColumnModel )header.getColumnModel();
  }

  private boolean canResize( Point p, JBroTableColumn column, JTableHeader header ) {
    Rectangle bounds = getGroupHeaderBoundsFor( column );
    bounds.grow( -3, 0 );
    return column != null && !bounds.contains( p ) && header.getResizingAllowed() && column.getResizable();
  }

  private void updateRolloverColumn( MouseEvent e ) {
    Point point = e.getPoint();
    if ( getHeader().getDraggedGroup() == null && header.contains( point ) ) {
      JBroTableColumn column = getColumnAtPoint( point );
      selectColumn( column );
    }
  }
  
  public JBroTableColumn getColumnAtPoint( Point point ) {
    int col = header.columnAtPoint( point );
    int level = getRowAtPoint( point );
    return getTableColumnModel().getColumnAtAbsolutePosition( col, level );
  }

  private int changeColumnWidth( JBroTableColumn resizingColumn, JTableHeader th, int oldWidth, int newWidth ) {
    resizingColumn.setWidth( newWidth );
    Container container;
    JTable table;
    if ( th.getParent() == null
         || !( ( container = th.getParent().getParent() ) instanceof JScrollPane )
         || ( table = th.getTable() ) == null )
      return 0;
    if ( !container.getComponentOrientation().isLeftToRight() && !th.getComponentOrientation().isLeftToRight() ) {
      JViewport viewport = ( ( JScrollPane )container ).getViewport();
      int viewportWidth = viewport.getWidth();
      int diff = newWidth - oldWidth;
      int newHeaderWidth = table.getWidth() + diff;
      Dimension tableSize = table.getSize();
      tableSize.width += diff;
      table.setSize( tableSize );
      if ( newHeaderWidth >= viewportWidth && table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF ) {
        Point p = viewport.getViewPosition();
        p.x = Math.max( 0, Math.min( newHeaderWidth - viewportWidth, p.x + diff ) );
        viewport.setViewPosition( p );
        return diff;
      }
    }
    return 0;
  }

  private int viewIndexForColumn( JBroTableColumn aColumn ) {
    JBroTableColumnModel cm = getHeader().getColumnModel();
    for ( int column = 0; column < cm.getColumnCount(); column++ )
      if ( cm.getColumn( column ) == aColumn )
        return column;
    return -1;
  }
  
  public void setRowHeight( int level, Integer height ) {
    if ( rowHeights == null )
      rowHeights = new ArrayList< Integer >( level + 1 );
    while ( rowHeights.size() <= level )
      rowHeights.add( null );
    rowHeights.set( level, height );
    heightsCache[ level ] = -1;
  }

  public Rectangle getGroupHeaderBoundsFor( JBroTableColumn group ) {
    return new Rectangle( getGroupX( group ), getGroupY( group ), getGroupWidth( group ), getGroupHeight( group ) );
  }
  
  public Point getGroupLocation( JBroTableColumn group ) {
    return new Point( getGroupX( group ), getGroupY( group ) );
  }
  
  public int getGroupY( JBroTableColumn group ) {
    int y = 0;
    for ( int level = group == null ? -1 : group.getY() - 1; level >= 0; level-- )
      y += getRowHeight( level );
    return y;
  }
  
  public int getGroupX( JBroTableColumn group ) {
    JBroTableColumnModel columnModel = getTableColumnModel();
    int x = 0;
    int lastColumnIndex = group == null ? -1 : columnModel.getColumnIndex( group.getIdentifier() );
    for ( int index = 0; index < lastColumnIndex; index++ ) {
      JBroTableColumn tc = columnModel.getColumn( index );
      x += tc.getWidth();
    }
    return x;
  }
  
  public int getRowAtPoint( Point point ) {
    int y = 0;
    for ( int level = 0; level < headers.size(); level++ ) {
      y += getRowHeight( level );
      if ( y > point.y )
        return level;
    }
    return -1;
  }

  @Override
	protected MouseInputListener createMouseInputListener() {
		return new MouseInputHandler();
	}

  public class MouseInputHandler implements MouseInputListener {
    private int mouseXOffset;
    private int prevMouseX;
    private Cursor otherCursor = RESIZE_CURSOR;

    @Override
    public void mouseClicked( MouseEvent e ) {
      JBroTableHeader header = getHeader();
      if ( !header.isEnabled() )
        return;
      Point point = e.getPoint();
      JBroTableColumn column = getColumnAtPoint( point );
      if ( column == null )
        return;
      if ( isLeaf( column ) && e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton( e ) ) {
        JTable table = header.getTable();
        RowSorter sorter;
        if ( table != null && ( sorter = table.getRowSorter() ) != null ) {
          int columnIndex = column.getModelIndex();
          if ( columnIndex != -1 ) {
            sorter.toggleSortOrder( columnIndex );
            header.repaint();
          }
        }
      }
    }

    private JBroTableColumn getResizingColumn( Point p ) {
      int row = getRowAtPoint( p );
      if ( row == -1 )
        return null;
      int column = getResizingColumnIndex( p );
      if ( column == -1 )
        return null;
      JBroTableColumnModel columnModel = getTableColumnModel();
      return columnModel.getColumnAtAbsolutePosition( column, row );
    }

    private int getResizingColumnIndex( Point p ) {
      int row = getRowAtPoint( p );
      if ( row == -1 )
        return -1;
      int column = header.columnAtPoint( p );
      if ( column == -1 )
        return -1;
      JBroTableHeader header = getHeader();
      JBroTableColumnModel columnModel = getTableColumnModel();
      JBroTableColumn dtc = columnModel.getColumnAtAbsolutePosition( column, row );
      Rectangle r = getGroupHeaderBoundsFor( dtc );
      r.grow( -3, 0 );
      if ( r.contains( p ) )
        return -1;
      int midPoint = r.x + r.width / 2;
      int columnIndex;
      if ( header.getComponentOrientation().isLeftToRight() )
        columnIndex = p.x < midPoint ? column - 1 : column;
      else
        columnIndex = p.x < midPoint ? column : column - 1;
      return columnIndex;
    }

    @Override
    public void mousePressed( MouseEvent e ) {
      if ( !header.isEnabled() )
        return;
      header.setDraggedColumn( null );
      header.setResizingColumn( null );
      header.setDraggedDistance( 0 );
      Point point = e.getPoint();
      int idx = getResizingColumnIndex( point );
      JBroTableColumn resizingColumn = idx < 0 ? null : getTableColumnModel().getColumn( idx );
      if ( canResize( point, resizingColumn, header ) ) {
        header.setResizingColumn( resizingColumn );
        if ( header.getComponentOrientation().isLeftToRight() )
          mouseXOffset = point.x - getGroupWidth( resizingColumn );
        else
          mouseXOffset = point.x + getGroupWidth( resizingColumn );
      } else if ( header.getReorderingAllowed() ) {
        JBroTableColumn column = getColumnAtPoint( point );
        if ( column != null ) {
          header.setDraggedColumn( column );
          mouseXOffset = point.x;
          prevMouseX = mouseXOffset;
          selectColumn( null );
        }
      }
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
      if ( !header.isEnabled() )
        return;
      Point point = e.getPoint();
      JBroTableColumn selectedColumn = getColumnAtPoint( point );
      selectColumn( selectedColumn );
      JBroTableColumn resizingColumn = getResizingColumn( point );
      Cursor cursor = header.getCursor();
      if ( canResize( point, resizingColumn, header ) ) {
        if ( cursor != RESIZE_CURSOR ) {
          header.setCursor( RESIZE_CURSOR );
          otherCursor = cursor;
        }
      } else if ( cursor == RESIZE_CURSOR )
        header.setCursor( otherCursor == RESIZE_CURSOR ? null : otherCursor );
      updateRolloverColumn( e );
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
      JBroTableHeader header = getHeader();
      if ( !header.isEnabled() )
        return;
      int mouseX = e.getX();
      JBroTableColumn resizingColumn = header.getResizingColumn();
      JBroTableColumn draggedColumn = header.getDraggedGroup();
      boolean headerLeftToRight = header.getComponentOrientation().isLeftToRight();
      JBroTableColumnModel groupModel = getTableColumnModel();
      if ( draggedColumn != null ) {
        boolean calcNewPosition = true;
        int draggedDistance = mouseX - mouseXOffset;
        boolean moved = false;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        JBroTableColumn parent = null;
        while ( calcNewPosition ) {
          int startIndex = groupModel.getColumnAbsoluteIndex( draggedColumn );
          int endIndex = startIndex + draggedColumn.getColspan() - 1;
          int direction = draggedDistance < 0 ? -1 : 1;
          int newColumnIndex = direction < 0 ? startIndex - 1 : endIndex + 1;
          boolean shouldMove = true;
          if ( newColumnIndex < 0 ) {
            newColumnIndex = 0;
            shouldMove = false;
          } else if ( newColumnIndex >= groupModel.getColumnCount() ) {
            newColumnIndex = groupModel.getColumnCount() - 1;
            shouldMove = false;
          }
          if ( shouldMove ) {
            IModelFieldGroup modelField = groupModel.getModelField( draggedColumn );
            if ( modelField != null && !modelField.isManageable() ) {
              newColumnIndex = direction < 0 ? startIndex : endIndex;
              shouldMove = false;
            }
          }
          if ( shouldMove ) {
            parent = groupModel.getColumnParent( draggedColumn );
            if ( parent != null ) {
              int parentStartIndex = groupModel.getColumnAbsoluteIndex( parent );
              int parentEndIndex = parentStartIndex + parent.getColspan() - 1;
              if ( newColumnIndex < parentStartIndex ) {
                newColumnIndex = parentStartIndex;
                shouldMove = false;
              } else if ( newColumnIndex > parentEndIndex ) {
                newColumnIndex = parentEndIndex;
                shouldMove = false;
              }
            }
          }
          JBroTableColumn newGroup = null;
          if ( shouldMove ) {
            newGroup = groupModel.getColumnAtAbsolutePosition( newColumnIndex, draggedColumn.getY() );
            IModelFieldGroup modelField = groupModel.getModelField( newGroup );
            if ( modelField != null && !modelField.isManageable() )
              shouldMove = false;
          }
          if ( shouldMove ) {
            int width = getGroupWidth( newGroup );
            int groupStartIndex = groupModel.getColumnAbsoluteIndex( newGroup );
            int groupEndIndex = newGroup.getColspan() + groupStartIndex - 1;
            if ( direction < 0 )
              newColumnIndex = groupStartIndex;
            else
              newColumnIndex = groupEndIndex;
            if ( Math.abs( draggedDistance ) > width / 2 ) {
              if ( newColumnIndex >= 0 && newColumnIndex < groupModel.getColumnCount() ) {
                mouseXOffset += direction * width;
                draggedDistance = mouseX - mouseXOffset;
                groupModel.moveColumn( draggedColumn, newColumnIndex );
                moved = true;
              } else
                calcNewPosition = false;
            } else
              calcNewPosition = false;
          } else {
            draggedDistance = 0;
            calcNewPosition = false;
          }
          if ( minCol > startIndex )
            minCol = startIndex;
          if ( minCol > newColumnIndex )
            minCol = newColumnIndex;
          if ( maxCol < startIndex )
            maxCol = startIndex;
          if ( maxCol < newColumnIndex )
            maxCol = newColumnIndex;
        }
        header.setDraggedDistance( draggedDistance );
        int y = getGroupY( draggedColumn );
        int diff = prevMouseX - mouseX;
        int addition = table.getShowVerticalLines() ? 1 : 0;
        int draggedWidth = getGroupWidth( draggedColumn ) + Math.abs( diff ) + addition;
        int draggedX = getGroupX( draggedColumn ) + draggedDistance + Math.min( 0, diff ) - addition;
        // A hack to avoid column blinking on dragging.
        // It shrinks repaint area.
        if ( isCacheUsed() ) {
          diff *= 2;
          if ( diff > 0 ) {
            if ( diff > 30 )
              diff = 30;
            draggedX -= diff;
            draggedWidth += diff;
          } else {
            if ( diff < -30 )
              diff = -30;
            draggedWidth -= diff;
          }
        }
        // End of hack.
        int x;
        int width;
        if ( moved ) {
          if ( maxCol >= 0 ) {
            maxCol += draggedColumn.getColspan() - 1;
            if ( maxCol >= groupModel.getColumnCount() ) {
              maxCol = groupModel.getColumnCount() - 1;
              if ( minCol > maxCol )
                minCol = maxCol;
            }
          } else {
            minCol = 0;
            maxCol = groupModel.getColumnCount() - 1;
          }
          x = 0;
          for ( int i = 0; i < minCol; i++ )
            x += groupModel.getColumn( i ).getWidth();
          width = 0;
          for ( int i = minCol; i <= maxCol; i++ )
            width += groupModel.getColumn( i ).getWidth();
          int end = x + width;
          if ( x > draggedX )
            x = draggedX;
          int draggedEnd = draggedX + draggedWidth;
          if ( end < draggedEnd )
            end = draggedEnd;
          width = end - x;
        } else {
          x = draggedX;
          width = draggedWidth;
        }
        if ( parent != null ) {
          int px = getGroupX( parent );
          int pw = getGroupWidth( parent );
          int pend = px + pw;
          if ( x < px ) {
            width -= px - x;
            x = px;
          }
          int end = x + width;
          if ( end > pend )
            width -= end - pend;
        }
        Set< String > spannedColumns = table.getUI().getSpannedColumns();
        if ( !spannedColumns.isEmpty() ) {
          int tableX = x;
          int tableWidth = width;
          int cMin = groupModel.getColumnIndexAtX( x );
          int cMax = groupModel.getColumnIndexAtX( x + width - 1 );
          int cCnt = groupModel.getColumnCount();
          boolean containsSpans = false;
          for ( int i = cMin < 0 ? cCnt : cMin; i <= cMax; i++ ) {
            if ( i >= cCnt )
              break;
            JBroTableColumn col = groupModel.getColumn( i );
            if ( spannedColumns.contains( col.getIdentifier() ) ) {
              containsSpans = true;
              break;
            }
          }
          if ( containsSpans ) {
            for ( int i = cMin - 1; i >= 0; i-- ) {
              JBroTableColumn col = groupModel.getColumn( i );
              if ( !spannedColumns.contains( col.getIdentifier() ) )
                break;
              if ( i == cMin - 1 ) {
                int gx = getGroupX( col );
                tableWidth += tableX - gx;
                tableX = gx;
              } else {
                tableX -= col.getWidth();
                tableWidth += col.getWidth();
              }
            }
            for ( int i = cMax < 0 ? cCnt : cMax + 1; i < cCnt; i++ ) {
              JBroTableColumn col = groupModel.getColumn( i );
              if ( !spannedColumns.contains( col.getIdentifier() ) )
                break;
              if ( i == cMax + 1 )
                tableWidth = getGroupX( col ) + col.getWidth() - tableX;
              else
                tableWidth += col.getWidth();
            }
            if ( tableWidth > width ) {
              x = tableX;
              width = tableWidth;
              y = 0;
            }
          }
        }
        if ( width > 0 )
          header.repaintHeaderAndTable( x, y, width );
        prevMouseX = mouseX;
      } else if ( resizingColumn != null ) {
        // TODO: child column resizing should affect only columns inside a parent group.
        // TODO: parent column resizing should proportionally affect all child columns.
        int oldWidth = getGroupWidth( resizingColumn );
        int newWidth;
        if ( headerLeftToRight )
          newWidth = mouseX - mouseXOffset;
        else
          newWidth = mouseXOffset - mouseX;
        mouseXOffset += changeColumnWidth( resizingColumn, header, oldWidth, newWidth );
      }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
      JBroTableHeader header = getHeader();
      if ( !header.isEnabled() )
        return;
      JBroTableColumn draggedColumn = header.getDraggedGroup();
      int y = getGroupY( draggedColumn );
      int addition = table.getShowVerticalLines() ? 1 : 0;
      int draggedDistance = header.getDraggedDistance();
      int draggedWidth = getGroupWidth( draggedColumn ) + addition;
      int draggedX = getGroupX( draggedColumn ) - addition;
      header.setDraggedDistance( 0 );
      header.setResizingColumn( null );
      header.setDraggedColumn( null );
      table.getUI().clearDraggedAreaCache();
      updateRolloverColumn( e );
      if ( draggedDistance > draggedWidth || draggedDistance < -draggedWidth ) {
        header.repaintHeaderAndTable( draggedX + draggedDistance, y, draggedWidth );
        header.repaintHeaderAndTable( draggedX, y, draggedWidth );
      } else if ( draggedDistance < 0 )
        header.repaintHeaderAndTable( draggedX + draggedDistance, y, draggedWidth - draggedDistance );
      else
        header.repaintHeaderAndTable( draggedX, y, draggedWidth + draggedDistance );
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
      if ( !header.isEnabled() )
        return;
      updateRolloverColumn( e );
    }

    @Override
    public void mouseExited( MouseEvent e ) {
      if ( !header.isEnabled() )
        return;
      selectColumn( null );
    }
  }
}