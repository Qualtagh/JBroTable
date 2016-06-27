package org.quinto.swing.table.view;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.EventListener;
import java.util.Locale;
import java.util.Set;
import javax.accessibility.AccessibleContext;
import javax.swing.InputVerifier;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class JBroTableHeader extends JTableHeader {
  public JBroTableHeader( JBroTableColumnModel model ) {
    super( model );
    super.setUI( new JBroTableHeaderUI( model.getTable() ) );
  }
  
  public int getLevelsQuantity() {
    return getColumnModel().getLevelsQuantity();
  }

  public void repaintHeaderAndTable( int x, int y, int width ) {
    Container c = table;
    while ( c != null ) {
      if ( SwingUtilities.isDescendingFrom( this, c ) )
        break;
      c = c.getParent();
    }
    Rectangle headerRect = new Rectangle( x, y, width, getHeight() - y );
    Rectangle tableRect = new Rectangle( x, 0, width, table.getHeight() );
    if ( c == null ) {
      repaint( headerRect );
      table.repaint( tableRect );
    } else {
      Rectangle r = SwingUtilities.convertRectangle( this, headerRect, c );
      r = r.union( SwingUtilities.convertRectangle( table, tableRect, c ) );
      c.repaint( r.x, r.y, r.width, r.height );
    }
  }

  public TableCellRenderer getDefaultRenderer( int level ) {
    return getUI().getHeader( level ).getDefaultRenderer();
  }

  public void setDefaultRenderer( int level, TableCellRenderer renderer ) {
    getUI().getHeader( level ).setDefaultRenderer( renderer );
  }

  @Override
  public void setUI( TableHeaderUI ui ) {
  }

  @Override
  public JBroTableHeaderUI getUI() {
    return ( JBroTableHeaderUI )super.getUI();
  }

  @Override
  public void updateUI() {
    JBroTableHeaderUI ui = getUI();
    if ( ui != null )
      ui.updateLookAndFeel();
    super.updateUI();
  }

  public void setRowHeight( int level, Integer height ) {
    getUI().setRowHeight( level, height );
  }

  public int getRowHeight( int level ) {
    return getUI().getRowHeight( level );
  }
  
  public int getRowAtPoint( Point p ) {
    return getUI().getRowAtPoint( p );
  }
  
  public JTableHeader createDelegateForLevel( int level ) {
    return new DelegateHeader( level );
  }

  @Override
  public JBroTableColumn getDraggedColumn() {
    JBroTableColumn dtc = ( JBroTableColumn )super.getDraggedColumn();
    if ( dtc != null && !getUI().isLeaf( dtc ) )
      dtc = null;
    return dtc;
  }

  public JBroTableColumn getDraggedGroup() {
    return ( JBroTableColumn )super.getDraggedColumn();
  }

  @Override
  public JBroTableColumn getResizingColumn() {
    return ( JBroTableColumn )super.getResizingColumn();
  }

  @Override
  public JBroTableColumnModel getColumnModel() {
    return ( JBroTableColumnModel )super.getColumnModel();
  }
  
  public TableColumnModel getColumnModel( int level ) {
    return ( ( JBroTableColumnModel )super.getColumnModel() ).getDelegate( level );
  }

  @Override
  public JBroTable getTable() {
    return ( JBroTable )super.getTable();
  }

  @Override
  public void columnMoved( TableColumnModelEvent e ) {
  }

  @Override
  public void columnMarginChanged( ChangeEvent e ) {
    if ( !( e instanceof JBroTableColumnModel.WidthChangeEvent ) )
      super.columnMarginChanged( e );
  }
  
  private class DelegateHeader extends JTableHeader {
    private final int level;
    
    public DelegateHeader( int level ) {
      this.level = level;
      setDefaultRenderer( createDefaultRenderer() );
    }

    @Override
    public AccessibleContext getAccessibleContext() {
      return JBroTableHeader.this.getAccessibleContext();
    }

    @Override
    protected String paramString() {
      return JBroTableHeader.this.paramString();
    }

    @Override
    public void setResizingColumn( TableColumn aColumn ) {
      JBroTableHeader.this.setResizingColumn( aColumn );
    }

    @Override
    public void setDraggedDistance( int distance ) {
      JBroTableHeader.this.setDraggedDistance( distance );
    }

    @Override
    public void setDraggedColumn( TableColumn aColumn ) {
      JBroTableHeader.this.setDraggedColumn( aColumn );
    }

    @Override
    public void resizeAndRepaint() {
      JBroTableHeader.this.resizeAndRepaint();
    }

    @Override
    protected void initializeLocalVars() {
    }

    @Override
    protected TableCellRenderer createDefaultRenderer() {
      return JBroTableHeader.this.createDefaultRenderer();
    }

    @Override
    protected TableColumnModel createDefaultColumnModel() {
      return JBroTableHeader.this.createDefaultColumnModel();
    }

    @Override
    public void columnSelectionChanged( ListSelectionEvent e ) {
      JBroTableHeader.this.columnSelectionChanged( e );
    }

    @Override
    public void columnMarginChanged( ChangeEvent e ) {
      JBroTableHeader.this.columnMarginChanged( e );
    }

    @Override
    public void columnMoved( TableColumnModelEvent e ) {
      JBroTableHeader.this.columnMoved( e );
    }

    @Override
    public void columnRemoved( TableColumnModelEvent e ) {
      JBroTableHeader.this.columnRemoved( e );
    }

    @Override
    public void columnAdded( TableColumnModelEvent e ) {
      JBroTableHeader.this.columnAdded( e );
    }

    @Override
    public TableColumnModel getColumnModel() {
      return JBroTableHeader.this.getColumnModel( level );
    }

    @Override
    public void setColumnModel( TableColumnModel columnModel ) {
    }

    @Override
    public String getUIClassID() {
      return JBroTableHeader.this.getUIClassID();
    }

    @Override
    public void updateUI() {
      super.updateUI();
    }

    @Override
    public void setUI( TableHeaderUI ui ) {
    }

    @Override
    public TableHeaderUI getUI() {
      return super.getUI();
    }

    @Override
    public String getToolTipText( MouseEvent event ) {
      return JBroTableHeader.this.getToolTipText( event );
    }

    @Override
    public Rectangle getHeaderRect( int column ) {
      JBroTableColumn dtc = JBroTableHeader.this.getColumnModel().getColumnAtAbsolutePosition( column, level );
      return JBroTableHeader.this.getUI().getGroupHeaderBoundsFor( dtc );
    }

    @Override
    public int columnAtPoint( Point point ) {
      int row = JBroTableHeader.this.getUI().getRowAtPoint( point );
      if ( row != level )
        return -1;
      return super.columnAtPoint( point );
    }

    @Override
    public TableCellRenderer getDefaultRenderer() {
      return super.getDefaultRenderer();
    }

    @Override
    public void setDefaultRenderer( TableCellRenderer defaultRenderer ) {
      super.setDefaultRenderer( defaultRenderer );
    }

    @Override
    public boolean getUpdateTableInRealTime() {
      return JBroTableHeader.this.getUpdateTableInRealTime();
    }

    @Override
    public void setUpdateTableInRealTime( boolean flag ) {
      JBroTableHeader.this.setUpdateTableInRealTime( flag );
    }

    @Override
    public TableColumn getResizingColumn() {
      return JBroTableHeader.this.getResizingColumn();
    }

    @Override
    public int getDraggedDistance() {
      return JBroTableHeader.this.getDraggedDistance();
    }

    @Override
    public TableColumn getDraggedColumn() {
      TableColumn ret = JBroTableHeader.this.getDraggedGroup();
      if ( ret instanceof JBroTableColumn ) {
        JBroTableColumn dtc = ( JBroTableColumn )ret;
        if ( dtc.getY() != level )
          return null;
      }
      return ret;
    }

    @Override
    public boolean getResizingAllowed() {
      return JBroTableHeader.this.getResizingAllowed();
    }

    @Override
    public void setResizingAllowed( boolean resizingAllowed ) {
      JBroTableHeader.this.setResizingAllowed( resizingAllowed );
    }

    @Override
    public boolean getReorderingAllowed() {
      return JBroTableHeader.this.getReorderingAllowed();
    }

    @Override
    public void setReorderingAllowed( boolean reorderingAllowed ) {
      JBroTableHeader.this.setReorderingAllowed( reorderingAllowed );
    }

    @Override
    public JTable getTable() {
      return JBroTableHeader.this.getTable();
    }

    @Override
    public void setTable( JTable table ) {
      JBroTableHeader.this.setTable( table );
    }

    @Override
    public void hide() {
      JBroTableHeader.this.hide();
    }

    @Override
    public JRootPane getRootPane() {
      return JBroTableHeader.this.getRootPane();
    }

    @Override
    public boolean isDoubleBuffered() {
      return JBroTableHeader.this.isDoubleBuffered();
    }

    @Override
    public void setDoubleBuffered( boolean aFlag ) {
      JBroTableHeader.this.setDoubleBuffered( aFlag );
    }

    @Override
    public void paintImmediately( Rectangle r ) {
      JBroTableHeader.this.paintImmediately( r );
    }

    @Override
    public void paintImmediately( int x, int y, int w, int h ) {
      JBroTableHeader.this.paintImmediately( x, y, w, h );
    }

    @Override
    protected boolean isPaintingOrigin() {
      return JBroTableHeader.this.isPaintingOrigin();
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
      return JBroTableHeader.this.isOptimizedDrawingEnabled();
    }

    @Override
    public boolean isValidateRoot() {
      return JBroTableHeader.this.isValidateRoot();
    }

    @Override
    public void revalidate() {
      JBroTableHeader.this.revalidate();
    }

    @Override
    public void repaint( Rectangle r ) {
      JBroTableHeader.this.repaint( r );
    }

    @Override
    public void repaint( long tm, int x, int y, int width, int height ) {
      JBroTableHeader.this.repaint( tm, x, y, width, height );
    }

    @Override
    public void removeNotify() {
      JBroTableHeader.this.removeNotify();
    }

    @Override
    public void addNotify() {
      JBroTableHeader.this.addNotify();
    }

    @Override
    public <T extends EventListener> T[] getListeners( Class<T> listenerType ) {
      return JBroTableHeader.this.getListeners( listenerType );
    }

    @Override
    public AncestorListener[] getAncestorListeners() {
      return JBroTableHeader.this.getAncestorListeners();
    }

    @Override
    public void removeAncestorListener( AncestorListener listener ) {
      JBroTableHeader.this.removeAncestorListener( listener );
    }

    @Override
    public void addAncestorListener( AncestorListener listener ) {
      JBroTableHeader.this.addAncestorListener( listener );
    }

    @Override
    public Container getTopLevelAncestor() {
      return JBroTableHeader.this.getTopLevelAncestor();
    }

    @Override
    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
      return JBroTableHeader.this.getVetoableChangeListeners();
    }

    @Override
    public synchronized void removeVetoableChangeListener( VetoableChangeListener listener ) {
      JBroTableHeader.this.removeVetoableChangeListener( listener );
    }

    @Override
    public synchronized void addVetoableChangeListener( VetoableChangeListener listener ) {
      JBroTableHeader.this.addVetoableChangeListener( listener );
    }

    @Override
    protected void fireVetoableChange( String propertyName, Object oldValue, Object newValue ) throws PropertyVetoException {
      JBroTableHeader.this.fireVetoableChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, char oldValue, char newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, int oldValue, int newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public Rectangle getVisibleRect() {
      return JBroTableHeader.this.getVisibleRect();
    }

    @Override
    public void computeVisibleRect( Rectangle visibleRect ) {
      JBroTableHeader.this.computeVisibleRect( visibleRect );
    }

    @Override
    public void setOpaque( boolean isOpaque ) {
      JBroTableHeader.this.setOpaque( isOpaque );
    }

    @Override
    public boolean isOpaque() {
      return JBroTableHeader.this.isOpaque();
    }

    @Override
    public int getHeight() {
      return JBroTableHeader.this.getHeight();
    }

    @Override
    public int getWidth() {
      return JBroTableHeader.this.getWidth();
    }

    @Override
    public int getY() {
      return JBroTableHeader.this.getY();
    }

    @Override
    public int getX() {
      return JBroTableHeader.this.getX();
    }

    @Override
    public Point getLocation( Point rv ) {
      return JBroTableHeader.this.getLocation( rv );
    }

    @Override
    public Dimension getSize( Dimension rv ) {
      return JBroTableHeader.this.getSize( rv );
    }

    @Override
    public Rectangle getBounds( Rectangle rv ) {
      return JBroTableHeader.this.getBounds( rv );
    }

    @Override
    public void reshape( int x, int y, int w, int h ) {
      JBroTableHeader.this.reshape( x, y, w, h );
    }

    @Override
    public void setFocusTraversalKeys( int id, Set<? extends AWTKeyStroke> keystrokes ) {
      JBroTableHeader.this.setFocusTraversalKeys( id, keystrokes );
    }

    @Override
    public void disable() {
      JBroTableHeader.this.disable();
    }

    @Override
    public void enable() {
      JBroTableHeader.this.enable();
    }

    @Override
    protected void processMouseMotionEvent( MouseEvent e ) {
      JBroTableHeader.this.processMouseMotionEvent( e );
    }

    @Override
    protected void processMouseEvent( MouseEvent e ) {
      JBroTableHeader.this.processMouseEvent( e );
    }

    @Override
    public TransferHandler getTransferHandler() {
      return JBroTableHeader.this.getTransferHandler();
    }

    @Override
    public void setTransferHandler( TransferHandler newHandler ) {
      JBroTableHeader.this.setTransferHandler( newHandler );
    }

    @Override
    public boolean getAutoscrolls() {
      return JBroTableHeader.this.getAutoscrolls();
    }

    @Override
    public void setAutoscrolls( boolean autoscrolls ) {
      JBroTableHeader.this.setAutoscrolls( autoscrolls );
    }

    @Override
    public void scrollRectToVisible( Rectangle aRect ) {
      JBroTableHeader.this.scrollRectToVisible( aRect );
    }

    @Override
    public JToolTip createToolTip() {
      return JBroTableHeader.this.createToolTip();
    }

    @Override
    public Point getPopupLocation( MouseEvent event ) {
      return JBroTableHeader.this.getPopupLocation( event );
    }

    @Override
    public Point getToolTipLocation( MouseEvent event ) {
      return JBroTableHeader.this.getToolTipLocation( event );
    }

    @Override
    public String getToolTipText() {
      return JBroTableHeader.this.getToolTipText();
    }

    @Override
    public void setToolTipText( String text ) {
      JBroTableHeader.this.setToolTipText( text );
    }

    @Override
    protected boolean processKeyBinding( KeyStroke ks, KeyEvent e, int condition, boolean pressed ) {
      return JBroTableHeader.this.processKeyBinding( ks, e, condition, pressed );
    }

    @Override
    protected void processKeyEvent( KeyEvent e ) {
      JBroTableHeader.this.processKeyEvent( e );
    }

    @Override
    protected void processComponentKeyEvent( KeyEvent e ) {
      JBroTableHeader.this.processComponentKeyEvent( e );
    }

    @Override
    public void setFont( Font font ) {
      JBroTableHeader.this.setFont( font );
    }

    @Override
    public void setBackground( Color bg ) {
      JBroTableHeader.this.setBackground( bg );
    }

    @Override
    public void setForeground( Color fg ) {
      JBroTableHeader.this.setForeground( fg );
    }

    @Override
    public void setEnabled( boolean enabled ) {
      JBroTableHeader.this.setEnabled( enabled );
    }

    @Override
    public void setVisible( boolean aFlag ) {
      JBroTableHeader.this.setVisible( aFlag );
    }

    @Override
    public boolean requestDefaultFocus() {
      return JBroTableHeader.this.requestDefaultFocus();
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
      return JBroTableHeader.this.getBaselineResizeBehavior();
    }

    @Override
    public int getBaseline( int width, int height ) {
      return JBroTableHeader.this.getBaseline( width, height );
    }

    @Override
    public void resetKeyboardActions() {
      JBroTableHeader.this.resetKeyboardActions();
    }

    @Override
    public ActionListener getActionForKeyStroke( KeyStroke aKeyStroke ) {
      return JBroTableHeader.this.getActionForKeyStroke( aKeyStroke );
    }

    @Override
    public int getConditionForKeyStroke( KeyStroke aKeyStroke ) {
      return JBroTableHeader.this.getConditionForKeyStroke( aKeyStroke );
    }

    @Override
    public KeyStroke[] getRegisteredKeyStrokes() {
      return JBroTableHeader.this.getRegisteredKeyStrokes();
    }

    @Override
    public void unregisterKeyboardAction( KeyStroke aKeyStroke ) {
      JBroTableHeader.this.unregisterKeyboardAction( aKeyStroke );
    }

    @Override
    public void registerKeyboardAction( ActionListener anAction, KeyStroke aKeyStroke, int aCondition ) {
      JBroTableHeader.this.registerKeyboardAction( anAction, aKeyStroke, aCondition );
    }

    @Override
    public void registerKeyboardAction( ActionListener anAction, String aCommand, KeyStroke aKeyStroke, int aCondition ) {
      JBroTableHeader.this.registerKeyboardAction( anAction, aCommand, aKeyStroke, aCondition );
    }

    @Override
    public int getDebugGraphicsOptions() {
      return JBroTableHeader.this.getDebugGraphicsOptions();
    }

    @Override
    public void setDebugGraphicsOptions( int debugOptions ) {
      JBroTableHeader.this.setDebugGraphicsOptions( debugOptions );
    }

    @Override
    public Graphics getGraphics() {
      return JBroTableHeader.this.getGraphics();
    }

    @Override
    public InputVerifier getInputVerifier() {
      return JBroTableHeader.this.getInputVerifier();
    }

    @Override
    public void setInputVerifier( InputVerifier inputVerifier ) {
      JBroTableHeader.this.setInputVerifier( inputVerifier );
    }

    @Override
    public void setAlignmentX( float alignmentX ) {
      JBroTableHeader.this.setAlignmentX( alignmentX );
    }

    @Override
    public float getAlignmentX() {
      return JBroTableHeader.this.getAlignmentX();
    }

    @Override
    public void setAlignmentY( float alignmentY ) {
      JBroTableHeader.this.setAlignmentY( alignmentY );
    }

    @Override
    public float getAlignmentY() {
      return JBroTableHeader.this.getAlignmentY();
    }

    @Override
    public Insets getInsets( Insets insets ) {
      return JBroTableHeader.this.getInsets( insets );
    }

    @Override
    public Insets getInsets() {
      return JBroTableHeader.this.getInsets();
    }

    @Override
    public Border getBorder() {
      return JBroTableHeader.this.getBorder();
    }

    @Override
    public void setBorder( Border border ) {
      JBroTableHeader.this.setBorder( border );
    }

    @Override
    public boolean contains( int x, int y ) {
      return JBroTableHeader.this.contains( x, y );
    }

    @Override
    public Dimension getMinimumSize() {
      return JBroTableHeader.this.getMinimumSize();
    }

    @Override
    public void setMinimumSize( Dimension minimumSize ) {
      JBroTableHeader.this.setMinimumSize( minimumSize );
    }

    @Override
    public Dimension getMaximumSize() {
      return JBroTableHeader.this.getMaximumSize();
    }

    @Override
    public void setMaximumSize( Dimension maximumSize ) {
      JBroTableHeader.this.setMaximumSize( maximumSize );
    }

    @Override
    public Dimension getPreferredSize() {
      return JBroTableHeader.this.getPreferredSize();
    }

    @Override
    public void setPreferredSize( Dimension preferredSize ) {
      JBroTableHeader.this.setPreferredSize( preferredSize );
    }

    @Override
    public FontMetrics getFontMetrics( Font font ) {
      return JBroTableHeader.this.getFontMetrics( font );
    }

    @Override
    public boolean getVerifyInputWhenFocusTarget() {
      return JBroTableHeader.this.getVerifyInputWhenFocusTarget();
    }

    @Override
    public void setVerifyInputWhenFocusTarget( boolean verifyInputWhenFocusTarget ) {
      JBroTableHeader.this.setVerifyInputWhenFocusTarget( verifyInputWhenFocusTarget );
    }

    @Override
    public void grabFocus() {
      JBroTableHeader.this.grabFocus();
    }

    @Override
    protected boolean requestFocusInWindow( boolean temporary ) {
      return JBroTableHeader.this.requestFocusInWindow( temporary );
    }

    @Override
    public boolean requestFocusInWindow() {
      return JBroTableHeader.this.requestFocusInWindow();
    }

    @Override
    public boolean requestFocus( boolean temporary ) {
      return JBroTableHeader.this.requestFocus( temporary );
    }

    @Override
    public void requestFocus() {
      JBroTableHeader.this.requestFocus();
    }

    @Override
    public boolean isRequestFocusEnabled() {
      return JBroTableHeader.this.isRequestFocusEnabled();
    }

    @Override
    public void setRequestFocusEnabled( boolean requestFocusEnabled ) {
      JBroTableHeader.this.setRequestFocusEnabled( requestFocusEnabled );
    }

    @Override
    public Component getNextFocusableComponent() {
      return JBroTableHeader.this.getNextFocusableComponent();
    }

    @Override
    public void setNextFocusableComponent( Component aComponent ) {
      JBroTableHeader.this.setNextFocusableComponent( aComponent );
    }

    @Override
    public boolean isManagingFocus() {
      return JBroTableHeader.this.isManagingFocus();
    }

    @Override
    public boolean isPaintingTile() {
      return JBroTableHeader.this.isPaintingTile();
    }

    @Override
    protected void printBorder( Graphics g ) {
      JBroTableHeader.this.printBorder( g );
    }

    @Override
    protected void printChildren( Graphics g ) {
      JBroTableHeader.this.printChildren( g );
    }

    @Override
    protected void printComponent( Graphics g ) {
      JBroTableHeader.this.printComponent( g );
    }

    @Override
    public void print( Graphics g ) {
      JBroTableHeader.this.print( g );
    }

    @Override
    public void printAll( Graphics g ) {
      JBroTableHeader.this.printAll( g );
    }

    @Override
    public void paint( Graphics g ) {
      JBroTableHeader.this.paint( g );
    }

    @Override
    public void update( Graphics g ) {
      JBroTableHeader.this.update( g );
    }

    @Override
    protected void paintBorder( Graphics g ) {
      JBroTableHeader.this.paintBorder( g );
    }

    @Override
    protected void paintChildren( Graphics g ) {
      JBroTableHeader.this.paintChildren( g );
    }

    @Override
    protected void paintComponent( Graphics g ) {
      JBroTableHeader.this.paintComponent( g );
    }

    @Override
    protected Graphics getComponentGraphics( Graphics g ) {
      return JBroTableHeader.this.getComponentGraphics( g );
    }

    @Override
    protected void setUI( ComponentUI newUI ) {
    }

    @Override
    public JPopupMenu getComponentPopupMenu() {
      return JBroTableHeader.this.getComponentPopupMenu();
    }

    @Override
    public void setComponentPopupMenu( JPopupMenu popup ) {
      JBroTableHeader.this.setComponentPopupMenu( popup );
    }

    @Override
    public boolean getInheritsPopupMenu() {
      return JBroTableHeader.this.getInheritsPopupMenu();
    }

    @Override
    public void setInheritsPopupMenu( boolean value ) {
      JBroTableHeader.this.setInheritsPopupMenu( value );
    }

    @Override
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
      JBroTableHeader.this.addPropertyChangeListener( propertyName, listener );
    }

    @Override
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
      JBroTableHeader.this.addPropertyChangeListener( listener );
    }

    @Override
    public void applyComponentOrientation( ComponentOrientation o ) {
      JBroTableHeader.this.applyComponentOrientation( o );
    }

    @Override
    public void transferFocusDownCycle() {
      JBroTableHeader.this.transferFocusDownCycle();
    }

    @Override
    public boolean isFocusCycleRoot() {
      return JBroTableHeader.this.isFocusCycleRoot();
    }

    @Override
    public void setFocusCycleRoot( boolean focusCycleRoot ) {
      JBroTableHeader.this.setFocusCycleRoot( focusCycleRoot );
    }

    @Override
    public boolean isFocusTraversalPolicySet() {
      return JBroTableHeader.this.isFocusTraversalPolicySet();
    }

    @Override
    public FocusTraversalPolicy getFocusTraversalPolicy() {
      return JBroTableHeader.this.getFocusTraversalPolicy();
    }

    @Override
    public void setFocusTraversalPolicy( FocusTraversalPolicy policy ) {
      JBroTableHeader.this.setFocusTraversalPolicy( policy );
    }

    @Override
    public boolean isFocusCycleRoot( Container container ) {
      return JBroTableHeader.this.isFocusCycleRoot( container );
    }

    @Override
    public boolean areFocusTraversalKeysSet( int id ) {
      return JBroTableHeader.this.areFocusTraversalKeysSet( id );
    }

    @Override
    public Set<AWTKeyStroke> getFocusTraversalKeys( int id ) {
      return JBroTableHeader.this.getFocusTraversalKeys( id );
    }

    @Override
    public void list( PrintWriter out, int indent ) {
      JBroTableHeader.this.list( out, indent );
    }

    @Override
    public void list( PrintStream out, int indent ) {
      JBroTableHeader.this.list( out, indent );
    }

    @Override
    public boolean isAncestorOf( Component c ) {
      return JBroTableHeader.this.isAncestorOf( c );
    }

    @Override
    public Component findComponentAt( Point p ) {
      return JBroTableHeader.this.findComponentAt( p );
    }

    @Override
    public Component findComponentAt( int x, int y ) {
      return JBroTableHeader.this.findComponentAt( x, y );
    }

    @Override
    public Point getMousePosition( boolean allowChildren ) throws HeadlessException {
      return JBroTableHeader.this.getMousePosition( allowChildren );
    }

    @Override
    public Component getComponentAt( Point p ) {
      return JBroTableHeader.this.getComponentAt( p );
    }

    @Override
    public Component locate( int x, int y ) {
      return JBroTableHeader.this.locate( x, y );
    }

    @Override
    public Component getComponentAt( int x, int y ) {
      return JBroTableHeader.this.getComponentAt( x, y );
    }

    @Override
    public void deliverEvent( Event e ) {
      JBroTableHeader.this.deliverEvent( e );
    }

    @Override
    protected void processContainerEvent( ContainerEvent e ) {
      JBroTableHeader.this.processContainerEvent( e );
    }

    @Override
    protected void processEvent( AWTEvent e ) {
      JBroTableHeader.this.processEvent( e );
    }

    @Override
    public synchronized ContainerListener[] getContainerListeners() {
      return JBroTableHeader.this.getContainerListeners();
    }

    @Override
    public synchronized void removeContainerListener( ContainerListener l ) {
      JBroTableHeader.this.removeContainerListener( l );
    }

    @Override
    public synchronized void addContainerListener( ContainerListener l ) {
      JBroTableHeader.this.addContainerListener( l );
    }

    @Override
    public void printComponents( Graphics g ) {
      JBroTableHeader.this.printComponents( g );
    }

    @Override
    public void paintComponents( Graphics g ) {
      JBroTableHeader.this.paintComponents( g );
    }

    @Override
    public Dimension minimumSize() {
      return JBroTableHeader.this.minimumSize();
    }

    @Override
    public Dimension preferredSize() {
      return JBroTableHeader.this.preferredSize();
    }

    @Override
    protected void validateTree() {
      JBroTableHeader.this.validateTree();
    }

    @Override
    public void validate() {
      JBroTableHeader.this.validate();
    }

    @Override
    public void invalidate() {
      JBroTableHeader.this.invalidate();
    }

    @Override
    public void layout() {
      JBroTableHeader.this.layout();
    }

    @Override
    public void doLayout() {
      JBroTableHeader.this.doLayout();
    }

    @Override
    public void setLayout( LayoutManager mgr ) {
      JBroTableHeader.this.setLayout( mgr );
    }

    @Override
    public LayoutManager getLayout() {
      return JBroTableHeader.this.getLayout();
    }

    @Override
    public void removeAll() {
      JBroTableHeader.this.removeAll();
    }

    @Override
    public void remove( Component comp ) {
      JBroTableHeader.this.remove( comp );
    }

    @Override
    public void remove( int index ) {
      JBroTableHeader.this.remove( index );
    }

    @Override
    protected void addImpl( Component comp, Object constraints, int index ) {
      JBroTableHeader.this.addImpl( comp, constraints, index );
    }

    @Override
    public void add( Component comp, Object constraints, int index ) {
      JBroTableHeader.this.add( comp, constraints, index );
    }

    @Override
    public void add( Component comp, Object constraints ) {
      JBroTableHeader.this.add( comp, constraints );
    }

    @Override
    public int getComponentZOrder( Component comp ) {
      return JBroTableHeader.this.getComponentZOrder( comp );
    }

    @Override
    public void setComponentZOrder( Component comp, int index ) {
      JBroTableHeader.this.setComponentZOrder( comp, index );
    }

    @Override
    public Component add( Component comp, int index ) {
      return JBroTableHeader.this.add( comp, index );
    }

    @Override
    public Component add( String name, Component comp ) {
      return JBroTableHeader.this.add( name, comp );
    }

    @Override
    public Component add( Component comp ) {
      return JBroTableHeader.this.add( comp );
    }

    @Override
    public Insets insets() {
      return JBroTableHeader.this.insets();
    }

    @Override
    public Component[] getComponents() {
      return JBroTableHeader.this.getComponents();
    }

    @Override
    public Component getComponent( int n ) {
      return JBroTableHeader.this.getComponent( n );
    }

    @Override
    public int countComponents() {
      return JBroTableHeader.this.countComponents();
    }

    @Override
    public int getComponentCount() {
      return JBroTableHeader.this.getComponentCount();
    }

    @Override
    public ComponentOrientation getComponentOrientation() {
      return JBroTableHeader.this.getComponentOrientation();
    }

    @Override
    public void setComponentOrientation( ComponentOrientation o ) {
      JBroTableHeader.this.setComponentOrientation( o );
    }

    @Override
    public void firePropertyChange( String propertyName, double oldValue, double newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, float oldValue, float newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, long oldValue, long newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, short oldValue, short newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public void firePropertyChange( String propertyName, byte oldValue, byte newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
      JBroTableHeader.this.firePropertyChange( propertyName, oldValue, newValue );
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners( String propertyName ) {
      return JBroTableHeader.this.getPropertyChangeListeners( propertyName );
    }

    @Override
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
      JBroTableHeader.this.removePropertyChangeListener( propertyName, listener );
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
      return JBroTableHeader.this.getPropertyChangeListeners();
    }

    @Override
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
      JBroTableHeader.this.removePropertyChangeListener( listener );
    }

    @Override
    public void list( PrintWriter out ) {
      JBroTableHeader.this.list( out );
    }

    @Override
    public void list( PrintStream out ) {
      JBroTableHeader.this.list( out );
    }

    @Override
    public void list() {
      JBroTableHeader.this.list();
    }

    @Override
    public String toString() {
      return super.toString();
    }

    @Override
    public void remove( MenuComponent popup ) {
      JBroTableHeader.this.remove( popup );
    }

    @Override
    public void add( PopupMenu popup ) {
      JBroTableHeader.this.add( popup );
    }

    @Override
    public boolean isFocusOwner() {
      return JBroTableHeader.this.isFocusOwner();
    }

    @Override
    public boolean hasFocus() {
      return JBroTableHeader.this.hasFocus();
    }

    @Override
    public void transferFocusUpCycle() {
      JBroTableHeader.this.transferFocusUpCycle();
    }

    @Override
    public void transferFocusBackward() {
      JBroTableHeader.this.transferFocusBackward();
    }

    @Override
    public void nextFocus() {
      JBroTableHeader.this.nextFocus();
    }

    @Override
    public void transferFocus() {
      JBroTableHeader.this.transferFocus();
    }

    @Override
    public Container getFocusCycleRootAncestor() {
      return JBroTableHeader.this.getFocusCycleRootAncestor();
    }

    @Override
    public boolean getFocusTraversalKeysEnabled() {
      return JBroTableHeader.this.getFocusTraversalKeysEnabled();
    }

    @Override
    public void setFocusTraversalKeysEnabled( boolean focusTraversalKeysEnabled ) {
      JBroTableHeader.this.setFocusTraversalKeysEnabled( focusTraversalKeysEnabled );
    }

    @Override
    public void setFocusable( boolean focusable ) {
      JBroTableHeader.this.setFocusable( focusable );
    }

    @Override
    public boolean isFocusable() {
      return JBroTableHeader.this.isFocusable();
    }

    @Override
    public boolean isFocusTraversable() {
      return JBroTableHeader.this.isFocusTraversable();
    }

    @Override
    public boolean lostFocus( Event evt, Object what ) {
      return JBroTableHeader.this.lostFocus( evt, what );
    }

    @Override
    public boolean gotFocus( Event evt, Object what ) {
      return JBroTableHeader.this.gotFocus( evt, what );
    }

    @Override
    public boolean action( Event evt, Object what ) {
      return JBroTableHeader.this.action( evt, what );
    }

    @Override
    public boolean keyUp( Event evt, int key ) {
      return JBroTableHeader.this.keyUp( evt, key );
    }

    @Override
    public boolean keyDown( Event evt, int key ) {
      return JBroTableHeader.this.keyDown( evt, key );
    }

    @Override
    public boolean mouseExit( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseExit( evt, x, y );
    }

    @Override
    public boolean mouseEnter( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseEnter( evt, x, y );
    }

    @Override
    public boolean mouseMove( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseMove( evt, x, y );
    }

    @Override
    public boolean mouseUp( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseUp( evt, x, y );
    }

    @Override
    public boolean mouseDrag( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseDrag( evt, x, y );
    }

    @Override
    public boolean mouseDown( Event evt, int x, int y ) {
      return JBroTableHeader.this.mouseDown( evt, x, y );
    }

    @Override
    public boolean handleEvent( Event evt ) {
      return JBroTableHeader.this.handleEvent( evt );
    }

    @Override
    protected void processHierarchyBoundsEvent( HierarchyEvent e ) {
      JBroTableHeader.this.processHierarchyBoundsEvent( e );
    }

    @Override
    protected void processHierarchyEvent( HierarchyEvent e ) {
      JBroTableHeader.this.processHierarchyEvent( e );
    }

    @Override
    protected void processInputMethodEvent( InputMethodEvent e ) {
      JBroTableHeader.this.processInputMethodEvent( e );
    }

    @Override
    protected void processMouseWheelEvent( MouseWheelEvent e ) {
      JBroTableHeader.this.processMouseWheelEvent( e );
    }

    @Override
    protected void processFocusEvent( FocusEvent e ) {
      JBroTableHeader.this.processFocusEvent( e );
    }

    @Override
    protected void processComponentEvent( ComponentEvent e ) {
      JBroTableHeader.this.processComponentEvent( e );
    }

    @Override
    protected AWTEvent coalesceEvents( AWTEvent existingEvent, AWTEvent newEvent ) {
      return JBroTableHeader.this.coalesceEvents( existingEvent, newEvent );
    }

    @Override
    public InputContext getInputContext() {
      return JBroTableHeader.this.getInputContext();
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
      return JBroTableHeader.this.getInputMethodRequests();
    }

    @Override
    public synchronized InputMethodListener[] getInputMethodListeners() {
      return JBroTableHeader.this.getInputMethodListeners();
    }

    @Override
    public synchronized void removeInputMethodListener( InputMethodListener l ) {
      JBroTableHeader.this.removeInputMethodListener( l );
    }

    @Override
    public synchronized void addInputMethodListener( InputMethodListener l ) {
      JBroTableHeader.this.addInputMethodListener( l );
    }

    @Override
    public synchronized MouseWheelListener[] getMouseWheelListeners() {
      return JBroTableHeader.this.getMouseWheelListeners();
    }

    @Override
    public synchronized void removeMouseWheelListener( MouseWheelListener l ) {
      JBroTableHeader.this.removeMouseWheelListener( l );
    }

    @Override
    public synchronized void addMouseWheelListener( MouseWheelListener l ) {
      JBroTableHeader.this.addMouseWheelListener( l );
    }

    @Override
    public synchronized MouseMotionListener[] getMouseMotionListeners() {
      return JBroTableHeader.this.getMouseMotionListeners();
    }

    @Override
    public synchronized void removeMouseMotionListener( MouseMotionListener l ) {
      JBroTableHeader.this.removeMouseMotionListener( l );
    }

    @Override
    public synchronized void addMouseMotionListener( MouseMotionListener l ) {
      JBroTableHeader.this.addMouseMotionListener( l );
    }

    @Override
    public synchronized MouseListener[] getMouseListeners() {
      return JBroTableHeader.this.getMouseListeners();
    }

    @Override
    public synchronized void removeMouseListener( MouseListener l ) {
      JBroTableHeader.this.removeMouseListener( l );
    }

    @Override
    public synchronized void addMouseListener( MouseListener l ) {
      JBroTableHeader.this.addMouseListener( l );
    }

    @Override
    public synchronized KeyListener[] getKeyListeners() {
      return JBroTableHeader.this.getKeyListeners();
    }

    @Override
    public synchronized void removeKeyListener( KeyListener l ) {
      JBroTableHeader.this.removeKeyListener( l );
    }

    @Override
    public synchronized void addKeyListener( KeyListener l ) {
      JBroTableHeader.this.addKeyListener( l );
    }

    @Override
    public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners() {
      return JBroTableHeader.this.getHierarchyBoundsListeners();
    }

    @Override
    public void removeHierarchyBoundsListener( HierarchyBoundsListener l ) {
      JBroTableHeader.this.removeHierarchyBoundsListener( l );
    }

    @Override
    public void addHierarchyBoundsListener( HierarchyBoundsListener l ) {
      JBroTableHeader.this.addHierarchyBoundsListener( l );
    }

    @Override
    public synchronized HierarchyListener[] getHierarchyListeners() {
      return JBroTableHeader.this.getHierarchyListeners();
    }

    @Override
    public void removeHierarchyListener( HierarchyListener l ) {
      JBroTableHeader.this.removeHierarchyListener( l );
    }

    @Override
    public void addHierarchyListener( HierarchyListener l ) {
      JBroTableHeader.this.addHierarchyListener( l );
    }

    @Override
    public synchronized FocusListener[] getFocusListeners() {
      return JBroTableHeader.this.getFocusListeners();
    }

    @Override
    public synchronized void removeFocusListener( FocusListener l ) {
      JBroTableHeader.this.removeFocusListener( l );
    }

    @Override
    public synchronized void addFocusListener( FocusListener l ) {
      JBroTableHeader.this.addFocusListener( l );
    }

    @Override
    public synchronized ComponentListener[] getComponentListeners() {
      return JBroTableHeader.this.getComponentListeners();
    }

    @Override
    public synchronized void removeComponentListener( ComponentListener l ) {
      JBroTableHeader.this.removeComponentListener( l );
    }

    @Override
    public synchronized void addComponentListener( ComponentListener l ) {
      JBroTableHeader.this.addComponentListener( l );
    }

    @Override
    public boolean postEvent( Event e ) {
      return JBroTableHeader.this.postEvent( e );
    }

    @Override
    public boolean contains( Point p ) {
      return JBroTableHeader.this.contains( p );
    }

    @Override
    public boolean inside( int x, int y ) {
      return JBroTableHeader.this.inside( x, y );
    }

    @Override
    public boolean getIgnoreRepaint() {
      return JBroTableHeader.this.getIgnoreRepaint();
    }

    @Override
    public void setIgnoreRepaint( boolean ignoreRepaint ) {
      JBroTableHeader.this.setIgnoreRepaint( ignoreRepaint );
    }

    @Override
    public int checkImage( Image image, int width, int height, ImageObserver observer ) {
      return JBroTableHeader.this.checkImage( image, width, height, observer );
    }

    @Override
    public int checkImage( Image image, ImageObserver observer ) {
      return JBroTableHeader.this.checkImage( image, observer );
    }

    @Override
    public boolean prepareImage( Image image, int width, int height, ImageObserver observer ) {
      return JBroTableHeader.this.prepareImage( image, width, height, observer );
    }

    @Override
    public boolean prepareImage( Image image, ImageObserver observer ) {
      return JBroTableHeader.this.prepareImage( image, observer );
    }

    @Override
    public VolatileImage createVolatileImage( int width, int height, ImageCapabilities caps ) throws AWTException {
      return JBroTableHeader.this.createVolatileImage( width, height, caps );
    }

    @Override
    public VolatileImage createVolatileImage( int width, int height ) {
      return JBroTableHeader.this.createVolatileImage( width, height );
    }

    @Override
    public Image createImage( int width, int height ) {
      return JBroTableHeader.this.createImage( width, height );
    }

    @Override
    public Image createImage( ImageProducer producer ) {
      return JBroTableHeader.this.createImage( producer );
    }

    @Override
    public boolean imageUpdate( Image img, int infoflags, int x, int y, int w, int h ) {
      return JBroTableHeader.this.imageUpdate( img, infoflags, x, y, w, h );
    }

    @Override
    public void repaint( int x, int y, int width, int height ) {
      JBroTableHeader.this.repaint( x, y, width, height );
    }

    @Override
    public void repaint( long tm ) {
      JBroTableHeader.this.repaint( tm );
    }

    @Override
    public void repaint() {
      JBroTableHeader.this.repaint();
    }

    @Override
    public void paintAll( Graphics g ) {
      JBroTableHeader.this.paintAll( g );
    }

    @Override
    public boolean isCursorSet() {
      return JBroTableHeader.this.isCursorSet();
    }

    @Override
    public Cursor getCursor() {
      return JBroTableHeader.this.getCursor();
    }

    @Override
    public void setCursor( Cursor cursor ) {
      JBroTableHeader.this.setCursor( cursor );
    }

    @Override
    public boolean isMaximumSizeSet() {
      return JBroTableHeader.this.isMaximumSizeSet();
    }

    @Override
    public boolean isMinimumSizeSet() {
      return JBroTableHeader.this.isMinimumSizeSet();
    }

    @Override
    public boolean isPreferredSizeSet() {
      return JBroTableHeader.this.isPreferredSizeSet();
    }

    @Override
    public boolean isLightweight() {
      return JBroTableHeader.this.isLightweight();
    }

    @Override
    public void setBounds( Rectangle r ) {
      JBroTableHeader.this.setBounds( r );
    }

    @Override
    public void setBounds( int x, int y, int width, int height ) {
      JBroTableHeader.this.setBounds( x, y, width, height );
    }

    @Override
    public Rectangle bounds() {
      return JBroTableHeader.this.bounds();
    }

    @Override
    public Rectangle getBounds() {
      return JBroTableHeader.this.getBounds();
    }

    @Override
    public void resize( Dimension d ) {
      JBroTableHeader.this.resize( d );
    }

    @Override
    public void setSize( Dimension d ) {
      JBroTableHeader.this.setSize( d );
    }

    @Override
    public void resize( int width, int height ) {
      JBroTableHeader.this.resize( width, height );
    }

    @Override
    public void setSize( int width, int height ) {
      JBroTableHeader.this.setSize( width, height );
    }

    @Override
    public Dimension size() {
      return JBroTableHeader.this.size();
    }

    @Override
    public Dimension getSize() {
      return JBroTableHeader.this.getSize();
    }

    @Override
    public void setLocation( Point p ) {
      JBroTableHeader.this.setLocation( p );
    }

    @Override
    public void move( int x, int y ) {
      JBroTableHeader.this.move( x, y );
    }

    @Override
    public void setLocation( int x, int y ) {
      JBroTableHeader.this.setLocation( x, y );
    }

    @Override
    public Point location() {
      return JBroTableHeader.this.location();
    }

    @Override
    public Point getLocationOnScreen() {
      return JBroTableHeader.this.getLocationOnScreen();
    }

    @Override
    public Point getLocation() {
      return JBroTableHeader.this.getLocation();
    }

    @Override
    public ColorModel getColorModel() {
      return JBroTableHeader.this.getColorModel();
    }

    @Override
    public void setLocale( Locale l ) {
      JBroTableHeader.this.setLocale( l );
    }

    @Override
    public Locale getLocale() {
      return JBroTableHeader.this.getLocale();
    }

    @Override
    public boolean isFontSet() {
      return JBroTableHeader.this.isFontSet();
    }

    @Override
    public Font getFont() {
      return JBroTableHeader.this.getFont();
    }

    @Override
    public boolean isBackgroundSet() {
      return JBroTableHeader.this.isBackgroundSet();
    }

    @Override
    public Color getBackground() {
      return JBroTableHeader.this.getBackground();
    }

    @Override
    public boolean isForegroundSet() {
      return JBroTableHeader.this.isForegroundSet();
    }

    @Override
    public Color getForeground() {
      return JBroTableHeader.this.getForeground();
    }

    @Override
    public void show( boolean b ) {
      JBroTableHeader.this.show( b );
    }

    @Override
    public void show() {
      JBroTableHeader.this.show();
    }

    @Override
    public void enableInputMethods( boolean enable ) {
      JBroTableHeader.this.enableInputMethods( enable );
    }

    @Override
    public void enable( boolean b ) {
      JBroTableHeader.this.enable( b );
    }

    @Override
    public boolean isEnabled() {
      return JBroTableHeader.this.isEnabled();
    }

    @Override
    public boolean isShowing() {
      return JBroTableHeader.this.isShowing();
    }

    @Override
    public Point getMousePosition() throws HeadlessException {
      return JBroTableHeader.this.getMousePosition();
    }

    @Override
    public boolean isVisible() {
      return JBroTableHeader.this.isVisible();
    }

    @Override
    public boolean isDisplayable() {
      return JBroTableHeader.this.isDisplayable();
    }

    @Override
    public boolean isValid() {
      return JBroTableHeader.this.isValid();
    }

    @Override
    public Toolkit getToolkit() {
      return JBroTableHeader.this.getToolkit();
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
      return JBroTableHeader.this.getGraphicsConfiguration();
    }

    @Override
    public synchronized DropTarget getDropTarget() {
      return JBroTableHeader.this.getDropTarget();
    }

    @Override
    public synchronized void setDropTarget( DropTarget dt ) {
      JBroTableHeader.this.setDropTarget( dt );
    }

    @Override
    public ComponentPeer getPeer() {
      return JBroTableHeader.this.getPeer();
    }

    @Override
    public Container getParent() {
      return JBroTableHeader.this.getParent();
    }

    @Override
    public void setName( String name ) {
      JBroTableHeader.this.setName( name );
    }

    @Override
    public String getName() {
      return JBroTableHeader.this.getName();
    }
    
    
  }
}