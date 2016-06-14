package org.quinto.swing.table.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;

public class TutorialFixedColumns {
  public static void main( String... args ) throws Exception {
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    // Sample columns hierarchy.
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelFieldGroup( "A", "<html>A" )
        .withChild( new ModelField( "B", "<html>B" ).withFixed( true ) ) // The whole group "A" would become fixed.
        .withChild( new ModelField( "C", "<html>C" ) ),
      new ModelFieldGroup( "D", "<html>D" )
        .withChild( new ModelField( "E", "<html>E" ) )
        .withChild( new ModelField( "F", "<html>F" ) ),
      new ModelField( "G", "<html>G" ),
      new ModelFieldGroup( "H", "<html>H" )
        .withChild( new ModelFieldGroup( "I", "<html>I" )
                      .withChild( new ModelField( "J", "<html>J" ) ) )
        .withChild( new ModelField( "K", "<html>K" ) )
        .withChild( new ModelFieldGroup( "L", "<html>L" )
                      .withChild( new ModelField( "M", "<html>M" ) )
                      .withChild( new ModelField( "N", "<html>N" ) ) )
    };
    ModelData data = new ModelData( groups );
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    // Sample data.
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, i == j ? "sort me" : fields[ j ].getCaption() + i );
    }
    data.setRows( rows );
    JBroTable table = new JBroTable( data );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
    table.setAutoCreateRowSorter( true );
    
    // getScrollPane returns scroll pane with fixed columns.
    JScrollPane scrollPane = table.getScrollPane();
    scrollPane.setPreferredSize( new Dimension( 400, 300 ) );
    
    // Left fixed table.
    JViewport viewport = scrollPane.getRowHeader();
    if ( viewport != null ) {
      JBroTable fixed = ( JBroTable )viewport.getView();
      fixed.setAutoResizeMode( JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS );
    }
    
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout( FlowLayout.LEFT ) );
    frame.add( scrollPane );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}