package org.quinto.swing.table.view;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
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

  public static void main( String args[] ) {
    Utils.initSimpleConsoleLogger();
    try {
      UIManager.setLookAndFeel( WindowsLookAndFeel.class.getName() );
    } catch ( InstantiationException e ) {
      LOGGER.error( null, e );
    } catch ( ClassNotFoundException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( UnsupportedLookAndFeelException e ) {
      LOGGER.error( null, e );
    }

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
    JBroTable table = new JBroTable( data );
   // table.setAutoCreateRowSorter( true );
    System.out.println( table.getColumnModel() );
    for ( TableColumn tc : Collections.list( table.getColumnModel().getColumns() ) )
      System.out.println( tc.getHeaderValue() );
    final JFrame frame = new JFrame( "Testing" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( new JScrollPane( table ) );
    JButton button = new JButton( "click" );
    button.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent event ) {
        try {
          if ( laf == 0 )
            UIManager.setLookAndFeel( NimbusLookAndFeel.class.getName());
          else if ( laf == 1 )
            UIManager.setLookAndFeel( WindowsClassicLookAndFeel.class.getName());
          else if ( laf == 2 )
            UIManager.setLookAndFeel( WindowsLookAndFeel.class.getName());
          else if ( laf == 3 )
            UIManager.setLookAndFeel( SynthLookAndFeel.class.getName());
          else if ( laf == 4 )
            UIManager.setLookAndFeel( MotifLookAndFeel.class.getName());
          else if ( laf == 5 ) {
            UIManager.setLookAndFeel( MetalLookAndFeel.class.getName());
            laf = -1;
          }
          laf++;
        } catch ( ClassNotFoundException e ) {
          LOGGER.error( null, e );
        } catch ( InstantiationException e ) {
          LOGGER.error( null, e );
        } catch ( IllegalAccessException e ) {
          LOGGER.error( null, e );
        } catch ( UnsupportedLookAndFeelException e ) {
          LOGGER.error( null, e );
        }
        SwingUtilities.updateComponentTreeUI( frame );
        frame.pack();
      }
    });
    frame.add( button );
    JTable tab = new JTable( new Integer[][]{ { 1, 2, 3 },
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