package org.quinto.swing.table.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.nimbus.NimbusStyle;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.quinto.swing.table.model.IModelFieldGroup;

public class JBroTableHeaderUI extends BasicTableHeaderUI {
  private static final Logger LOGGER = Logger.getLogger( JBroTableHeaderUI.class );
  private static final Cursor resizeCursor = Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR );
  private static final Map< String, Boolean > EXISTING_PARENT_UIS = new HashMap< String, Boolean >();
  
  private JBroTableColumn selectedColumn;
  private LookAndFeel lookAndFeel;
  private MetalTheme metalTheme;
  private final PropertyChangeListener listener;
  private final JBroTable table;
  private List< ComponentUI > delegates;
  private List< CellRendererPane > rendererPanes;
  private List< JTableHeader > headers;
  private List< Integer > rowHeights;
  
  public JBroTableHeaderUI( JBroTable table ) {
    this.table = table;
    updateLookAndFeel();
    UIManager.addPropertyChangeListener( listener = new PropertyChangeListener() {
      @Override
      public void propertyChange( PropertyChangeEvent evt ) {
        updateLookAndFeel();
      }
    } );
  }

  @Override
  protected void finalize() throws Throwable {
    UIManager.removePropertyChangeListener( listener );
    super.finalize();
  }
  
  private void updateLookAndFeel() {
    LookAndFeel newLookAndFeel = UIManager.getLookAndFeel();
    if ( lookAndFeel == null || !newLookAndFeel.getName().equals( lookAndFeel.getName() ) || newLookAndFeel instanceof MetalLookAndFeel &&
       ( metalTheme == null || !MetalLookAndFeel.getCurrentTheme().getName().equals( metalTheme.getName() ) ) ) {
      if ( header != null )
        uninstallUI( header );
      lookAndFeel = newLookAndFeel;
      if ( lookAndFeel instanceof MetalLookAndFeel )
        metalTheme = MetalLookAndFeel.getCurrentTheme();
      if ( header != null )
        installUI( header );
    }
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
      Object ret = ( Object )field.get( ui );
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
        method.setAccessible( true );NimbusStyle f;
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
    for ( ComponentUI delegate : delegates )
      call( "uninstallUI", new Class[]{ JComponent.class }, delegate, new Object[]{ c } );
    super.uninstallDefaults();
    super.uninstallListeners();
    super.uninstallKeyboardActions();
    header.removeAll();
    rendererPane = null;
    delegates.clear();
    headers.clear();
    rendererPanes.clear();
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
      Class uiClass = lookAndFeel.getDefaults().getUIClass( "TableHeaderUI" );
      Method createUImethod = uiClass.getMethod( "createUI", JComponent.class );
      ComponentUI delegate = ( ComponentUI )createUImethod.invoke( null, header );
      call( "installUI", new Class[]{ JComponent.class }, delegate, new Object[]{ header } );
      for ( int level = delegates.size(); level < levelsCnt; level++ ) {
        delegate = ( ComponentUI )createUImethod.invoke( null, header );
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
    JBroTableHeader header = getHeader();
    int headerHeight = header.getSize().height;
    Rectangle cellRect = new Rectangle();
    JBroTableColumn draggedColumn = header.getDraggedGroup();
    Enumeration< TableColumn > enumeration = groupModel.getColumns();
    List< JBroTableColumn > currentColumns = new ArrayList< JBroTableColumn >();
    List< Integer > coords = new ArrayList< Integer >();
    currentColumns.add( null );
    boolean draggedColumnMet = false;
    while ( enumeration.hasMoreElements() ) {
      JBroTableColumn column = ( JBroTableColumn )enumeration.nextElement();
      List< JBroTableColumn > columnParents = groupModel.getColumnParents( column, true );
      int level = 0;
      boolean addAbsent = false;
      boolean doNotPaintCells = draggedColumnMet && columnParents.contains( draggedColumn );
      for ( JBroTableColumn columnParent : columnParents ) {
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
          Dimension cellSize = getGroupSize( columnParent );
          if ( columnParent == column )
            cellSize.height = headerHeight - cellRect.y;
          cellRect.setSize( cellSize );
          if ( draggedColumn == columnParent ) {
            g.setColor( header.getParent().getBackground() );
            g.fillRect( cellRect.x, cellRect.y, cellRect.width, headerHeight - cellRect.y );
            draggedColumnMet = true;
            doNotPaintCells = true;
          } else if ( !doNotPaintCells )
            paintCell( g, cellRect, columnParent );
          if ( columnParent != column ) {
            cellRect.y += cellSize.height;
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

  private void paintCell( Graphics g, Component component, Rectangle cellRect ) {
    rendererPane.add( component );
    rendererPane.paintComponent( g, component, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
  }
  
  public boolean isLeaf( JBroTableColumn column ) {
    return column.getY() + column.getRowspan() == headers.size();
  }

  private void paintCell( Graphics g, Rectangle cellRect, JBroTableColumn group ) {
    TableCellRenderer renderer = getRenderer( group );
    boolean parentUIdeterminesRolloverColumnItself = hasParentUI( renderer );
    boolean rollover = parentUIdeterminesRolloverColumnItself ? group == getHeader().getDraggedGroup() : group == selectedColumn;
    Component component = renderer.getTableCellRendererComponent( header.getTable(), group.getHeaderValue(), rollover, rollover, group.getY(), getTableColumnModel().getColumnRelativeIndex( group ) );
    paintCell( g, component, cellRect );
  }

  public Dimension getGroupSize( JBroTableColumn group ) {
    Dimension size = new Dimension();
    JBroTableColumnModel groupModel = getTableColumnModel();
    for ( int level = group.getY(); level < group.getY() + group.getRowspan(); level++ ) {
      if ( rowHeights != null && rowHeights.size() > level ) {
        Integer height = rowHeights.get( level );
        if ( height != null ) {
          size.height += height;
          continue;
        }
      }
      TableCellRenderer renderer = headers.get( level ).getDefaultRenderer();
      Component comp = renderer.getTableCellRendererComponent( header.getTable(), group.getHeaderValue(), false, false, -1, -1 );
      size.height += comp.getPreferredSize().height;
    }
    List< JBroTableColumn > children = groupModel.getColumnChildren( group );
    if ( children.isEmpty() )
      size.width = group.getWidth();
    else
      for ( JBroTableColumn column : children )
        size.width += groupModel.getWidth( column );
    return size;
  }

  private int calculateHeaderHeight() {
    int mHeight = 0;
    JBroTableColumnModel groupModel = getTableColumnModel();
    for ( int column = 0; column < groupModel.getColumnCount(); column++ ) {
      int cHeight = 0;
      JBroTableColumn parent = groupModel.getColumn( column );
      while ( parent != null ) {
        for ( int level = parent.getY(); level < parent.getY() + parent.getRowspan(); level++ ) {
          if ( rowHeights != null && rowHeights.size() > level ) {
            Integer height = rowHeights.get( level );
            if ( height != null ) {
              cHeight += height;
              continue;
            }
          }
          TableCellRenderer renderer = headers.get( level ).getDefaultRenderer();
          Component comp = renderer.getTableCellRendererComponent( header.getTable(), parent.getHeaderValue(), false, false, -1, column );
          cHeight += comp.getPreferredSize().height;
        }
        parent = groupModel.getColumnParent( parent );
      }
      mHeight = Math.max( mHeight, cHeight );
    }
    return mHeight;
  }

  public int getRowHeight( int level ) {
    int extra = level == headers.size() - 1 ? getHeader().getPreferredSize().height - calculateHeaderHeight() : 0;
    if ( rowHeights != null && rowHeights.size() > level ) {
      Integer height = rowHeights.get( level );
      if ( height != null )
        return extra + height;
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
    return extra + mHeight;
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
      if ( oldSelectedColumn != null ) {
        Rectangle repaintRect = getGroupHeaderBoundsFor( oldSelectedColumn );
        header.repaint( repaintRect );
      }
      if ( newSelectedColumn != null ) {
        Rectangle repaintRect = getGroupHeaderBoundsFor( newSelectedColumn );
        header.repaint( repaintRect );
      }
      JBroTableColumnModel columnModel = getTableColumnModel();
      rolloverColumnUpdated( columnModel.getColumnAbsoluteIndex( oldSelectedColumn ), columnModel.getColumnAbsoluteIndex( newSelectedColumn ) );
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
    return bounds != null && column != null && !bounds.contains( p ) && header.getResizingAllowed() && column.getResizable();
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
         || ( container = th.getParent().getParent() ) == null
         || !( container instanceof JScrollPane )
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
  }

  public Rectangle getGroupHeaderBoundsFor( JBroTableColumn group ) {
    if ( group == null )
      return new Rectangle();
    JBroTableColumnModel columnModel = getTableColumnModel();
    Dimension size = getGroupSize( group );
    Rectangle bounds = new Rectangle( size );
    bounds.y = 0;
    for ( JBroTableColumn parent : columnModel.getColumnParents( group, false ) )
      bounds.y += getGroupSize( parent ).height;
    int lastColumnIndex = columnModel.getColumnIndex( group.getIdentifier() );
    for ( int index = 0; index < lastColumnIndex; index++ ) {
      JBroTableColumn tc = columnModel.getColumn( index );
      bounds.x += tc.getWidth();
    }
    return bounds;
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
    private Cursor otherCursor = resizeCursor;

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
          mouseXOffset = point.x - getGroupSize( resizingColumn ).width;
        else
          mouseXOffset = point.x + getGroupSize( resizingColumn ).width;
      } else if ( header.getReorderingAllowed() ) {
        JBroTableColumn column = getColumnAtPoint( point );
        if ( column != null ) {
          header.setDraggedColumn( column );
          mouseXOffset = point.x;
          selectColumn( null );
        }
      }
    }

    private void swapCursor() {
      Cursor tmp = header.getCursor();
      header.setCursor( otherCursor );
      otherCursor = tmp;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
      if ( !header.isEnabled() )
        return;
      Point point = e.getPoint();
      JBroTableColumn selectedColumn = getColumnAtPoint( point );
      selectColumn( selectedColumn );
      JBroTableColumn resizingColumn = getResizingColumn( point );
      if ( canResize( point, resizingColumn, header ) != ( header.getCursor() == resizeCursor ) )
        swapCursor();
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
        int startIndex = groupModel.getColumnAbsoluteIndex( draggedColumn );
        int endIndex = startIndex + draggedColumn.getColspan() - 1;
        int draggedDistance = mouseX - mouseXOffset;
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
          JBroTableColumn parent = groupModel.getColumnParent( draggedColumn );
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
          if ( modelField != null && !modelField.isManageable() ) {
            newColumnIndex = direction < 0 ? startIndex : endIndex;
            shouldMove = false;
          }
        }
        if ( shouldMove ) {
          int width = getGroupSize( newGroup ).width;
          int groupStartIndex = groupModel.getColumnAbsoluteIndex( newGroup );
          int groupEndIndex = newGroup.getColspan() + groupStartIndex - 1;
          if ( direction < 0 )
            newColumnIndex = groupStartIndex;
          else
            newColumnIndex = groupEndIndex;
          if ( Math.abs( draggedDistance ) > width / 2 ) {
            if ( newColumnIndex >= 0 && newColumnIndex < groupModel.getColumnCount() ) {
              mouseXOffset = mouseXOffset + direction * width;
              draggedDistance = mouseX - mouseXOffset;
              header.setDraggedDistance( draggedDistance - direction * width );
              groupModel.moveColumn( draggedColumn, newColumnIndex );
            }
          }
        } else
          draggedDistance = 0;
        header.repaint();
        table.repaint();
        setDraggedDistance( draggedDistance, newColumnIndex );
      } else if ( resizingColumn != null ) {
        // TODO: child column resizing should affect only columns inside a parent group.
        // TODO: parent column resizing should proportionally affect all child columns.
        int oldWidth = getGroupSize( resizingColumn ).width;
        int newWidth;
        if ( headerLeftToRight )
          newWidth = mouseX - mouseXOffset;
        else
          newWidth = mouseXOffset - mouseX;
        mouseXOffset += changeColumnWidth( resizingColumn, header, oldWidth, newWidth );
      }
      updateRolloverColumn( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
      JBroTableHeader header = getHeader();
      if ( !header.isEnabled() )
        return;
      setDraggedDistance( 0, viewIndexForColumn( header.getDraggedGroup() ) );
      header.setResizingColumn( null );
      header.setDraggedColumn( null );
      updateRolloverColumn( e );
      header.repaint();
      table.repaint();
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
    
    private void setDraggedDistance( int draggedDistance, int column ) {
      header.setDraggedDistance( draggedDistance );
      if ( column != -1 )
        header.getColumnModel().moveColumn( column, column );
    }
  }
}