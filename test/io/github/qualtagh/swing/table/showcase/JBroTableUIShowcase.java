package io.github.qualtagh.swing.table.showcase;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.UIManager;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.ModelSpan;
import io.github.qualtagh.swing.table.view.JBroTable;
import io.github.qualtagh.swing.table.view.JBroTableUI;

public class JBroTableUIShowcase {
  public static void main( String args[] ) throws Exception {
    UIManager.setLookAndFeel( WindowsLookAndFeel.class.getName() );
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelFieldGroup( "A", "A" )
        .withChild( new ModelField( "B", "B" ) )
        .withChild( new ModelField( "C", "C" ).withRowspan( 2 ) ),
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
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, j == 1 || j == 2 ? rows[ i ].getValue( 0 ) : i == j ? "sort me" : fields[ j ].getCaption() + i );
    }
    data.setRows( rows );
    JBroTable table = new JBroTable( data );
    table.setAutoCreateRowSorter( true );
    table.setUI( new JBroTableUI().withSpan( new ModelSpan( "B", "B" ).withColumns( "B", "C", "E" ).withDrawAsHeader( true ) )
                                  .withSpan( new ModelSpan( "G", "G" ).withColumns( "G", "J" ) ) );
    JFrame frame = new JFrame( "Testing" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}