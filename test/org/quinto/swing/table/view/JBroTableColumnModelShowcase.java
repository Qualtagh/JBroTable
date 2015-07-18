package org.quinto.swing.table.view;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;
import org.quinto.swing.table.model.Utils;

public class JBroTableColumnModelShowcase {
  private static final Logger LOGGER = Logger.getLogger( JBroTableColumnModelShowcase.class );
  private static int laf = 0;
  private static JTable tab;
  private static JBroTable table;
  private static JFrame frame;
  private static MetalTheme highContrastTheme;

  public static void main( String args[] ) {
    Utils.initSimpleConsoleLogger();
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
          if ( laf == 0 )
            UIManager.setLookAndFeel( NimbusLookAndFeel.class.getName() );
          else if ( laf == 1 )
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
          SwingUtilities.updateComponentTreeUI( frame );
          frame.pack();
        }
      }
    };
    lafChanger.actionPerformed( null );
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
    ModelFieldGroup groupС = new ModelFieldGroup( "GС", "GС" );
    groupС.withChild( fields[ 4 ] );
    groupС.withChild( groupB );
    ModelData data = new ModelData( fields );
    System.out.println( data.getHeaderString() );
    ModelRow rows[] = new ModelRow[ rowCnt ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, String.valueOf( ( char )( 'A' + j ) ) + i );
    }
    data.setRows( rows );
    table = new JBroTable( data );
   // table.setAutoCreateRowSorter( true );
    System.out.println( table.getColumnModel() );
    for ( TableColumn tc : Collections.list( table.getColumnModel().getColumns() ) )
      System.out.println( tc.getHeaderValue() );
    frame = new JFrame( "Testing" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( new JScrollPane( table ) );
    JButton button = new JButton( "click" );
    button.addActionListener( lafChanger );
    frame.add( button );
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