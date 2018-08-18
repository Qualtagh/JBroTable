package io.github.qualtagh.swing.table.view;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.ModelSpan;
import io.github.qualtagh.swing.table.model.Utils;
import io.github.qualtagh.swing.table.view.JBroPredefinedRowSorter.SortKey;

public class TutorialAdvancedSpanCells {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelField( "DEPARTMENT", "Department" ),
      new ModelField( "TOTAL_ID", "Total identifier for merging cells" )
        .withVisible( false ),
      new ModelField( "USER_ID", "User identifier" )
        .withVisible( false ),
      new ModelField( "FAMILY_ID", "Family identifier" )
        .withVisible( false ),
      new ModelFieldGroup( "NAME", "Person name" )
        .withChild( new ModelField( "FIRST_NAME", "First name" ) )
        .withChild( new ModelField( "LAST_NAME", "Last name" ) ),
      new ModelField( "PHONE", "Phone number" )
    };
    
    final ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    ModelRow rows[] = new ModelRow[ 13 ];
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
    
    data.setValue( 6, "FIRST_NAME", "Vanessa" );
    data.setValue( 6, "LAST_NAME", "McQueen" );
    data.setValue( 7, "FIRST_NAME", "Albert" );
    data.setValue( 7, "LAST_NAME", "Newmann" );
    data.setValue( 8, "FIRST_NAME", "John" );
    data.setValue( 8, "LAST_NAME", "Goode" );
    data.setValue( 9, "FIRST_NAME", "William" );
    data.setValue( 9, "LAST_NAME", "Key" );
    data.setValue( 10, "FIRST_NAME", "Robert" );
    data.setValue( 10, "LAST_NAME", "Peterson" );
    
    data.setValue( 5, "USER_ID", 0 );
    data.setValue( 5, "FIRST_NAME", "Total" );
    data.setValue( 5, "PHONE", 5 );
    
    data.setValue( 11, "USER_ID", 1 );
    data.setValue( 11, "FIRST_NAME", "Total" );
    data.setValue( 11, "PHONE", 5 );
    
    data.setValue( 12, "TOTAL_ID", 0 );
    data.setValue( 12, "DEPARTMENT", "Total" );
    data.setValue( 12, "PHONE", 10 );
    
    for ( int i = 0; i < rows.length; i++ )
      data.setValue( i, "FAMILY_ID", i );
    data.setValue( 1, "FAMILY_ID", 0 );
    
    for ( int i = 0; i < rows.length - 1; i++ )
      data.setValue( i, "DEPARTMENT", i < rows.length / 2 ? "Development" : "Testing" );
    
    int p = 0;
    for ( int i = 0; i < rows.length - 1; i++ )
      if ( i % 6 != 5 )
        data.setValue( i, "PHONE", "3456" + p++ );
    
    final JBroTable table = new JBroTable( data );
    table.setRowSorter( new JBroPredefinedRowSorter( table )
      .withPreColumnsByName( new SortKey( "TOTAL_ID", SortOrder.ASCENDING ),
                             new SortKey( "DEPARTMENT", SortOrder.ASCENDING ),
                             new SortKey( "USER_ID", SortOrder.ASCENDING ) ) );
    
    table.setUI( new JBroTableUI()
      .withSpan( new ModelSpan( "FAMILY_ID", "LAST_NAME" ).withColumns( "LAST_NAME" ) )
      .withSpan( new ModelSpan( "USER_ID", "FIRST_NAME" ).withColumns( "FIRST_NAME", "LAST_NAME" ) )
      .withSpan( new ModelSpan( "TOTAL_ID", "DEPARTMENT" ).withColumns( "DEPARTMENT", "FIRST_NAME", "LAST_NAME" ) )
      .withSpan( new ModelSpan( "DEPARTMENT", "DEPARTMENT" ).withColumns( "DEPARTMENT" ).withDrawAsHeader( true ) ) );
    
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}