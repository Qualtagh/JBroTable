package io.github.qualtagh.swing.table.tutorial;

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

public class QuickStart {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelField( "USER_ID", "User identifier" ),
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
    
    JBroTable table = new JBroTable( data );
    
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}