package io.github.qualtagh.swing.table.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.UIManager;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.Utils;

public class BigSample {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    // Hierarchically create columns and column groups.
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelFieldGroup( "A", "<html>A" )
        .withChild( new ModelField( "B", "<html>B" ) )
        .withChild( new ModelField( "C", "<html>C" ).withRowspan( 2 ) ), // Custom rowspan set.
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
                      .withChild( new ModelField( "N", "<html>N" ) ) ),
      new ModelField( "G0", "<html>G0" ),
      new ModelField( "G1", "<html>G1" ),
      new ModelField( "G2", "<html>G2" ),
      new ModelField( "G3", "<html>G3" ),
      new ModelField( "G4", "<html>G4" ),
      new ModelField( "G5", "<html>G5" ),
      new ModelField( "G6", "<html>G6" ),
      new ModelField( "G7", "<html>G7" ),
      new ModelField( "G8", "<html>G8" ),
      new ModelField( "G9", "<html>G9" ),
      new ModelFieldGroup( "H0", "<html>H0" )
        .withChild( new ModelFieldGroup( "I0", "<html>I0" )
                      .withChild( new ModelField( "J0", "<html>J0" ) ) )
        .withChild( new ModelField( "K0", "<html>K0" ) )
        .withChild( new ModelFieldGroup( "L0", "<html>L0" )
                      .withChild( new ModelField( "M0", "<html>M0" ) )
                      .withChild( new ModelField( "N0", "<html>N0" ) ) )
    };
    ModelData data = new ModelData( groups );
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    // Sample data.
    ModelRow rows[] = new ModelRow[ 10000 ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, i == j ? "sort me" : fields[ j ].getCaption() + i );
    }
    data.setRows( rows );
    JBroTable table = new JBroTable( data );
    table.setAutoCreateRowSorter( true );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
    table.getScrollPane().setPreferredSize( new Dimension( 1024, 768 ) );
    
    // Window.
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}