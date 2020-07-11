package io.github.qualtagh.swing.table.showcase;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.Utils;
import io.github.qualtagh.swing.table.view.CustomTableHeaderRenderer;
import io.github.qualtagh.swing.table.view.JBroTable;
import io.github.qualtagh.swing.table.view.JBroTableModel;

public class JBroTableColumnModelShowcase {
  private static final Logger LOGGER = Logger.getLogger( JBroTableColumnModelShowcase.class );
  private static int laf = 0;
  private static JTable tab;
  private static JBroTable table;
  private static JFrame frame;
  private static MetalTheme highContrastTheme;

  public static void main( String args[] ) {
    Utils.initSimpleConsoleLogger();
    
    // Look & Feel switcher.
    Method m;
    try {
      m = Toolkit.class.getDeclaredMethod( "setDesktopProperty", String.class, Object.class );
      m.setAccessible( true );
    } catch ( NoSuchMethodException e ) {
      LOGGER.error( null, e );
      m = null;
    }
    final Method setDesktopProperty = m;
    ActionListener lafChanger = new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent event ) {
        try {
          if ( laf == 0 ) {
            for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
              if ( "Nimbus".equals( info.getName() ) ) {
                UIManager.setLookAndFeel( info.getClassName() );
                break;
              }
            }
          } else if ( laf == 1 )
            UIManager.setLookAndFeel( WindowsClassicLookAndFeel.class.getName() );
          else if ( laf == 2 )
            UIManager.setLookAndFeel( WindowsLookAndFeel.class.getName() );
          else if ( laf == 3 )
            UIManager.setLookAndFeel( MotifLookAndFeel.class.getName() );
          else if ( laf == 4 ) {
            if ( setDesktopProperty != null )
              setDesktopProperty.invoke( Toolkit.getDefaultToolkit(), "win.highContrast.on", true );
            if ( highContrastTheme == null ) {
              System.setProperty( "swing.useSystemFontSettings", "true" );
              UIManager.put( "Application.useSystemFontSettings", true );
            } else
              MetalLookAndFeel.setCurrentTheme( highContrastTheme );
            UIManager.setLookAndFeel( MetalLookAndFeel.class.getName() );
            highContrastTheme = MetalLookAndFeel.getCurrentTheme();
          } else if ( laf == 5 ) {
            if ( setDesktopProperty != null )
              setDesktopProperty.invoke( Toolkit.getDefaultToolkit(), "win.highContrast.on", false );
            MetalLookAndFeel.setCurrentTheme( new DefaultMetalTheme() );
            UIManager.setLookAndFeel( MetalLookAndFeel.class.getName() );
          } else if ( laf == 6 ) {
            laf = -1;
            MetalLookAndFeel.setCurrentTheme( new OceanTheme() );
            UIManager.setLookAndFeel( MetalLookAndFeel.class.getName() );
          }
        } catch ( ClassNotFoundException e ) {
          LOGGER.error( null, e );
        } catch ( InstantiationException e ) {
          LOGGER.error( null, e );
        } catch ( IllegalAccessException e ) {
          LOGGER.error( null, e );
        } catch ( UnsupportedLookAndFeelException e ) {
          LOGGER.error( null, e );
        } catch ( IllegalArgumentException e ) {
          LOGGER.error( null, e );
        } catch ( InvocationTargetException e ) {
          LOGGER.error( null, e );
          LOGGER.error( null, e.getCause() );
        } catch ( SecurityException e ) {
          LOGGER.error( null, e );
        }
        laf++;
        if ( frame != null ) {
          Utils.updateComponentTreeUI();
          frame.pack();
        }
      }
    };
    lafChanger.actionPerformed( null );
    
    // Columns adder.
    ActionListener columnAdder = new ActionListener() {
      private final Random random = new Random( System.nanoTime() );
      private final int nextId[] = new int[]{ 0 };
      
      private IModelFieldGroup genColumn( int level ) {
        int id = nextId[ 0 ]++;
        String sid = "N" + id;
        boolean b = true;
        for ( int i = 0; i <= level; i++ )
          b = b && random.nextBoolean();
        return b ? new ModelFieldGroup( sid, sid ) : new ModelField( sid, sid );
      }
      
      private IModelFieldGroup genGroup( int level ) {
        IModelFieldGroup col = genColumn( level );
        if ( col instanceof ModelFieldGroup )
        {
          int len = random.nextInt( 2 ) + 2;
          for ( int i = 0; i < len; i++ )
            ( ( ModelFieldGroup )col ).withChild( genGroup( level + 1 ) );
        }
        return col;
      }
      
      @Override
      public void actionPerformed( ActionEvent e ) {
        JBroTableModel model = table.getModel();
        List< ModelFieldGroup > groups = new ArrayList< ModelFieldGroup >();
        for ( IModelFieldGroup group : model.getData().getAllFieldGroupsFromTop( true ) )
          if ( group instanceof ModelFieldGroup )
            groups.add( ( ModelFieldGroup )group );
        String groupId = random.nextInt( 3 ) == 0 || groups.isEmpty() ? null : groups.get( Math.abs( random.nextInt() % groups.size() ) ).getIdentifier();
        IModelFieldGroup column = genGroup( 0 );
        model.addColumn( groupId, column );
      }
    };
    
    // Table model.
    int colCnt = 10;
    int rowCnt = 10;
    ModelField fields[] = new ModelField[ colCnt ];
    for ( int i = 0; i < fields.length; i++ )
      fields[ i ] = new ModelField( String.valueOf( ( char )( 'A' + i ) ), String.valueOf( ( char )( 'A' + i ) ) );
    ModelFieldGroup groupA = new ModelFieldGroup( "GA", "GA" );
    for ( int i = 0; i < 4; i++ )
      groupA.withChild( fields[ i ] );
    ModelFieldGroup groupB = new ModelFieldGroup( "GB", "GB" );
    for ( int i = 5; i < 9; i++ )
      groupB.withChild( fields[ i ] );
    ModelFieldGroup groupC = new ModelFieldGroup( "GC", "GC" );
    groupC.withChild( fields[ 4 ] );
    groupC.withChild( groupB );
    ModelData data = new ModelData( fields );
    System.out.println( data.getHeaderString() );
    ModelRow rows[] = new ModelRow[ rowCnt ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, i == j ? "sort me" : String.valueOf( ( char )( 'A' + j ) ) + i );
    }
    data.setRows( rows );
    
    // Table view.
    table = new JBroTable( data );
    table.setAutoCreateRowSorter( true );
    table.getTableHeader().getUI().setCustomRenderer( new CustomTableHeaderRenderer() {
      @Override
      public Component getTableCellRendererComponent( final Component originalComponent, JBroTable table, Object value, boolean isSelected, boolean hasFocus, boolean isDragged, int row, int viewColumn, int modelColumn, IModelFieldGroup dataField ) {
        if ( dataField == null || !( originalComponent instanceof JLabel ) )
          return originalComponent;
        JLabel ret = ( JLabel )originalComponent;
        String fieldName = dataField.getIdentifier();
        // "GC" cell is right aligned. Other cells are centered.
        ret.setHorizontalAlignment( "GC".equals( fieldName ) ? SwingConstants.RIGHT : SwingConstants.CENTER );
        // "D" cell caption is written in bold. Other captions have plain font.
        ret.setFont( ret.getFont().deriveFont( "D".equals( fieldName ) ? Font.BOLD : Font.PLAIN ) );
        // "B" cell would be half-transparent red with Windows L&F (not classic).
        // Note that background is reset on each call by underlying renderer so there's no need to reset background for other cells.
        // Properties like alignment and font should be reset on each call if they were changed before.
        if ( "B".equals( fieldName ) && !originalComponent.getClass().getName().contains( "DefaultTableCellHeaderRenderer" ) )
          ret.setBackground( new Color( 220, 50, 50, 50 ) );
        // "C" cell would be half-transparent red.
        if ( "C".equals( fieldName ) ) {
          JPanel p = new JPanel( new GridLayout( 1, 1 ) ) {
            @Override
            public void paint( Graphics g ) {
              super.paint( g );
              g.setColor( new Color( 220, 50, 50, 50 ) );
              g.fillRect( 0, 0, getWidth(), getHeight() );
            }
          };
          p.add( ret );
          return p;
        }
        return ret;
      }
    } );
    System.out.println( table.getColumnModel() );
    for ( TableColumn tc : Collections.list( table.getColumnModel().getColumns() ) )
      System.out.println( tc.getHeaderValue() );
    
    // Frame.
    frame = new JFrame( "Testing" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    JButton switchLookButton = new JButton( "Switch L&F" );
    switchLookButton.addActionListener( lafChanger );
    JPanel pane = new JPanel();
    pane.setLayout( new BoxLayout( pane, BoxLayout.Y_AXIS ) );
    pane.add( switchLookButton );
    JButton addColumnButton = new JButton( "Add column" );
    addColumnButton.addActionListener( columnAdder );
    pane.add( addColumnButton );
    final JTextField columnToRemoveInput = new JTextField();
    columnToRemoveInput.setMaximumSize( new Dimension( 100, 50 ) );
    columnToRemoveInput.setAlignmentX( 0.0F );
    pane.add( columnToRemoveInput );
    JButton removeColumnButton = new JButton( "Remove column" );
    removeColumnButton.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent e ) {
        table.getModel().removeColumn( columnToRemoveInput.getText().trim().toUpperCase() );
      }
    } );
    pane.add( removeColumnButton );
    frame.add( pane );
    tab = new JTable( new Integer[][]{ { 1, 2, 3 },
                                       { 4, 5, 6 },
                                       { 7, 8, 9 } },
                      new String[]{ "a", "b", "c" } );
    tab.setAutoCreateRowSorter( true );
    frame.add( new JScrollPane( tab ) );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}