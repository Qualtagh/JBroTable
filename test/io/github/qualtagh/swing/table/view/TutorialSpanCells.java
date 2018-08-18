package io.github.qualtagh.swing.table.view;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.UIManager;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.ModelSpan;
import io.github.qualtagh.swing.table.model.Utils;

public class TutorialSpanCells {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelField( "USER_ID", "User identifier" ),
      new ModelField( "FAMILY_ID", "Family identifier" )
        .withVisible( false ),
      new ModelFieldGroup( "NAME", "Person name" )
        .withChild( new ModelField( "FIRST_NAME", "First name" ) )
        .withChild( new ModelField( "LAST_NAME", "Last name" ) ),
      new ModelField( "PHONE", "Phone number" )
    };
    
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ )
      rows[ i ] = new ModelRow( fields.length );
    
    ModelData data = new ModelData( groups );
    data.setRows( rows );
    
    data.setValue( 0, "FIRST_NAME", "John" );
    data.setValue( 0, "LAST_NAME", "Doe" );
    data.setValue( 1, "FIRST_NAME", "Jane" );
    data.setValue( 1, "LAST_NAME", "Doe" );
    data.setValue( 2, "FIRST_NAME", "Anony" );
    data.setValue( 2, "LAST_NAME", "Mouse" );
    data.setValue( 3, "FIRST_NAME", "William" );
    data.setValue( 3, "LAST_NAME", "Perry" );
    data.setValue( 4, "FIRST_NAME", "Morgan" );
    data.setValue( 4, "LAST_NAME", "McQueen" );
    data.setValue( 5, "FIRST_NAME", "Vanessa" );
    data.setValue( 5, "LAST_NAME", "McQueen" );
    data.setValue( 6, "FIRST_NAME", "Albert" );
    data.setValue( 6, "LAST_NAME", "Newmann" );
    data.setValue( 7, "FIRST_NAME", "John" );
    data.setValue( 7, "LAST_NAME", "Goode" );
    data.setValue( 8, "FIRST_NAME", "William" );
    data.setValue( 8, "LAST_NAME", "Key" );
    data.setValue( 9, "FIRST_NAME", "Robert" );
    data.setValue( 9, "LAST_NAME", "Peterson" );
    
    for ( int i = 0; i < rows.length; i++ )
      data.setValue( i, "FAMILY_ID", i );
    data.setValue( 1, "FAMILY_ID", 0 );
    
    JBroTable table = new JBroTable( data );
    table.setAutoCreateRowSorter( true );
    
    table.setUI( new JBroTableUI()
      .withSpan( new ModelSpan( "FAMILY_ID", "LAST_NAME" ).withColumns( "LAST_NAME", "PHONE" ) ) );
    
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}