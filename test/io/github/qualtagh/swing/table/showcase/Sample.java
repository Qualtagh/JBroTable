package io.github.qualtagh.swing.table.showcase;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.UIManager;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.Utils;
import io.github.qualtagh.swing.table.view.JBroTable;

public class Sample {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    // Hierarchically create columns and column groups.
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelFieldGroup( "A", "A" )
        .withChild( new ModelField( "B", "B" ) )
        .withChild( new ModelField( "C", "C" ).withRowspan( 2 ) ), // Custom rowspan set.
      new ModelFieldGroup( "D", "D" )
        .withChild( new ModelField( "E", "E" ) )
        .withChild( new ModelField( "F", "F" ) ),
      new ModelField( "G", "G" ),
      new ModelFieldGroup( "H", "H" )
        .withChild( new ModelFieldGroup( "I", "I" )
                      .withChild( new ModelField( "J", "J" ) ) )
        .withChild( new ModelField( "K", "K" ) )
        .withChild( new ModelFieldGroup( "L", "L" )
                      .withChild( new ModelField( "M", "M" ) )
                      .withChild( new ModelField( "N", "N" ) ) )
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
    table.setAutoCreateRowSorter( true );
    
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