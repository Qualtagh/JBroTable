package org.quinto.swing.table.view;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;
import org.quinto.swing.table.model.Utils;

public class JBroTableColumnModelTest {
  private static final Logger LOGGER = Logger.getLogger( JBroTableColumnModelTest.class );
  private ModelData data;
  private JBroTable table;
  
  public JBroTableColumnModelTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
    Utils.initSimpleConsoleLogger();
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
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
    data = new ModelData( groups );
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, i == j ? "sort me" : fields[ j ].getCaption() + i );
    }
    data.setRows( rows );
    table = new JBroTable( data );
    table.setAutoCreateRowSorter( true );
  }
  
  @After
  public void tearDown() {
  }
  
  @Test( timeout = 1000L )
  public void captionsOrder() {
    List< String > modelCaptions = new ArrayList< String >();
    for ( ModelField field : data.getFields() )
      modelCaptions.add( field.getCaption() );
    List< String > viewCaptions = new ArrayList< String >();
    for ( TableColumn tc : Collections.list( table.getColumnModel().getColumns() ) )
      viewCaptions.add( ( String )tc.getHeaderValue() );
    assertEquals( modelCaptions, viewCaptions );
  }
  
  @Test( timeout = 1000L )
  public void idsMatch() {
    for ( TableColumn tc : Collections.list( table.getColumnModel().getColumns() ) )
      assertEquals( ( String )tc.getIdentifier(), ( String )tc.getHeaderValue() );
  }
  
  @Test( timeout = 1000L )
  public void getColumnIndex() {
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    assertEquals( "A", 0, cm.getColumnIndex( "A" ) );
    assertEquals( "B", 0, cm.getColumnIndex( "B" ) );
    assertEquals( "C", 1, cm.getColumnIndex( "C" ) );
    assertEquals( "D", 2, cm.getColumnIndex( "D" ) );
    assertEquals( "E", 2, cm.getColumnIndex( "E" ) );
    assertEquals( "F", 3, cm.getColumnIndex( "F" ) );
    assertEquals( "G", 4, cm.getColumnIndex( "G" ) );
    assertEquals( "H", 5, cm.getColumnIndex( "H" ) );
    assertEquals( "I", 5, cm.getColumnIndex( "I" ) );
    assertEquals( "J", 5, cm.getColumnIndex( "J" ) );
    assertEquals( "K", 6, cm.getColumnIndex( "K" ) );
    assertEquals( "L", 7, cm.getColumnIndex( "L" ) );
    assertEquals( "M", 7, cm.getColumnIndex( "M" ) );
    assertEquals( "N", 8, cm.getColumnIndex( "N" ) );
  }
  
  @Test( timeout = 1000L )
  public void getModelIndexOfModelField() {
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    for ( int i = 0; i < cm.getColumnCount(); i++ ) {
      JBroTableColumn col = cm.getColumn( i );
      int modelIndex = data.getIndexOfModelField( col.getIdentifier() );
      if ( modelIndex >= 0 )
        assertEquals( data.getFields()[ modelIndex ].getIdentifier(), modelIndex, col.getModelIndex() );
    }
  }
  
  @Test( timeout = 1000L )
  public void getModelIndexAllDistinct() {
    JBroTableColumnModel cm = ( JBroTableColumnModel )table.getColumnModel();
    Set< Integer > distinctIndexes = new HashSet< Integer >( cm.getColumnCount() );
    for ( int i = 0; i < cm.getColumnCount(); i++ ) {
      JBroTableColumn col = cm.getColumn( i );
      assertTrue( col.getIdentifier(), distinctIndexes.add( col.getModelIndex() ) );
    }
  }
  
  @Test( timeout = 1000L )
  public void getRowHeight() {
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 0, 50 );
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 1, 50 );
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 2, 50 );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 0 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 1 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 2 ) );
    
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 2, null );
    table.getTableHeader().setRowHeight( 2, 100 );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 0 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 1 ) );
    assertEquals( 100, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 2 ) );
    
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 2, 50 );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 0 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 1 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 2 ) );
  }
  
  @Test( timeout = 1000L )
  public void getRowHeightLastNotSet() {
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 0, 50 );
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 1, 50 );
    table.getTableHeader().setRowHeight( 2, 100 );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 0 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 1 ) );
    assertEquals( 100, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 2 ) );
    
    ( ( JBroTableHeader )table.getTableHeader() ).setRowHeight( 2, 50 );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 0 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 1 ) );
    assertEquals( 50, ( ( JBroTableHeader )table.getTableHeader() ).getRowHeight( 2 ) );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnJustOneInsideGroup() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( 0, 1 );
    assertEquals( "C", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 1 ).getIdentifier() );
    gcm.moveColumn( gcm.getColumn( "B" ), 0 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnJustOneInsideGroupBackwards() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( 1, 0 );
    assertEquals( "C", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 1 ).getIdentifier() );
    gcm.moveColumn( gcm.getColumn( "C" ), 1 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnJustOneAgainstGroups() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "G" ), 0 );
    assertEquals( "G", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "E", gcm.getColumn( 3 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 4 ).getIdentifier() );
    assertEquals( "J", gcm.getColumn( 5 ).getIdentifier() );
    
    assertEquals( "G", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "A", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "G", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "G", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "E", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnGroupAgainstAnotherGroup() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 2 );
    assertEquals( "E", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 3 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "A", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "E", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnGroupAgainstAnotherGroupABitFarther() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 3 );
    assertEquals( "E", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 3 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "A", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "E", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnNoMove() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 0 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "E", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 3 ).getIdentifier() );
    
    assertEquals( "A", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "B", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "B", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "E", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnNoMoveABitFarther() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 1 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "E", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 3 ).getIdentifier() );
    
    assertEquals( "A", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "B", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "D", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "B", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "E", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnABitFarther() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 4 );
    assertEquals( "E", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 3 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 4 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "A", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "E", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnToTheEnd() {
    for ( int pos = 5; pos < 9; pos++ ) {
      JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
      gcm.moveColumn( gcm.getColumn( "A" ), pos );
      assertEquals( "E", gcm.getColumn( 0 ).getIdentifier() );
      assertEquals( "F", gcm.getColumn( 1 ).getIdentifier() );
      assertEquals( "G", gcm.getColumn( 2 ).getIdentifier() );
      assertEquals( "J", gcm.getColumn( 3 ).getIdentifier() );
      assertEquals( "K", gcm.getColumn( 4 ).getIdentifier() );
      assertEquals( "M", gcm.getColumn( 5 ).getIdentifier() );
      assertEquals( "N", gcm.getColumn( 6 ).getIdentifier() );
      assertEquals( "B", gcm.getColumn( 7 ).getIdentifier() );
      assertEquals( "C", gcm.getColumn( 8 ).getIdentifier() );

      assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
      assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
      assertEquals( "H", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
      assertEquals( "A", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );

      assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
      assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
      assertEquals( "I", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
      assertEquals( "K", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
      assertEquals( "L", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
      assertEquals( "B", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
      assertEquals( "C", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );

      assertEquals( "E", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
      assertEquals( "F", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
      assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
      assertEquals( "J", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
      assertEquals( "K", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
      assertEquals( "M", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
      assertEquals( "N", gcm.getColumnAtRelativePosition( 6, 2 ).getIdentifier() );
      assertEquals( "B", gcm.getColumnAtRelativePosition( 7, 2 ).getIdentifier() );
      assertEquals( "C", gcm.getColumnAtRelativePosition( 8, 2 ).getIdentifier() );
      
      setUp();
    }
  }
  
  @Test( timeout = 1000L )
  public void moveColumnOutOfBoundsSmall() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), -1 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnOutOfBoundsLarge() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 9 );
    assertEquals( "B", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 1 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void moveColumnToTheEndAndThenAnotherToTheEnd() {
    JBroTableColumnModel gcm = ( JBroTableColumnModel )table.getColumnModel();
    gcm.moveColumn( gcm.getColumn( "A" ), 5 );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    gcm.moveColumn( gcm.getColumn( "H" ), 7 );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 0 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 0 ).getIdentifier() );
    assertEquals( "A", gcm.getColumnAtRelativePosition( 2, 0 ).getIdentifier() );
    assertEquals( "H", gcm.getColumnAtRelativePosition( 3, 0 ).getIdentifier() );
    
    assertEquals( "D", gcm.getColumnAtRelativePosition( 0, 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 1, 1 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 2, 1 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 3, 1 ).getIdentifier() );
    assertEquals( "I", gcm.getColumnAtRelativePosition( 4, 1 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 5, 1 ).getIdentifier() );
    assertEquals( "L", gcm.getColumnAtRelativePosition( 6, 1 ).getIdentifier() );
    
    assertEquals( "E", gcm.getColumnAtRelativePosition( 0, 2 ).getIdentifier() );
    assertEquals( "F", gcm.getColumnAtRelativePosition( 1, 2 ).getIdentifier() );
    assertEquals( "G", gcm.getColumnAtRelativePosition( 2, 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumnAtRelativePosition( 3, 2 ).getIdentifier() );
    assertEquals( "C", gcm.getColumnAtRelativePosition( 4, 2 ).getIdentifier() );
    assertEquals( "J", gcm.getColumnAtRelativePosition( 5, 2 ).getIdentifier() );
    assertEquals( "K", gcm.getColumnAtRelativePosition( 6, 2 ).getIdentifier() );
    assertEquals( "M", gcm.getColumnAtRelativePosition( 7, 2 ).getIdentifier() );
    assertEquals( "N", gcm.getColumnAtRelativePosition( 8, 2 ).getIdentifier() );
    
    assertEquals( "E", gcm.getColumn( 0 ).getIdentifier() );
    assertEquals( "F", gcm.getColumn( 1 ).getIdentifier() );
    assertEquals( "G", gcm.getColumn( 2 ).getIdentifier() );
    assertEquals( "B", gcm.getColumn( 3 ).getIdentifier() );
    assertEquals( "C", gcm.getColumn( 4 ).getIdentifier() );
    assertEquals( "J", gcm.getColumn( 5 ).getIdentifier() );
    assertEquals( "K", gcm.getColumn( 6 ).getIdentifier() );
    assertEquals( "M", gcm.getColumn( 7 ).getIdentifier() );
    assertEquals( "N", gcm.getColumn( 8 ).getIdentifier() );
  }
  
  @Test( timeout = 1000L )
  public void addRow() {
    JBroTableModel model = table.getModel();
    int rc = model.getRowCount();
    model.addRow( new Object[ 0 ] );
    model.addRow( new Object[ 100 ] );
    assertEquals( rc + 2, model.getRowCount() );
    assertEquals( data.getRows()[ rc ], data.getRows()[ rc + 1 ] );
  }
  
  @Test( timeout = 1000L )
  public void addColumn() {
    JBroTableModel model = table.getModel();
    int fc = data.getFieldsCount();
    model.addColumn( "H", new ModelFieldGroup( "NEW", "New" ).withChild( new ModelField( "CHILD", "Child" ) ) );
    assertTrue( model.getData() != data );
    assertEquals( fc + 1, model.getData().getFieldsCount() );
  }
  
  public static void main( String args[] ) {
    setUpClass();
    try {
     // UIManager.setLookAndFeel( WindowsClassicLookAndFeel.class.getName() );
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
    final JBroTableColumnModelTest test = new JBroTableColumnModelTest();
    test.setUp();
    JFrame frame = new JFrame( "Testing" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( test.table.getScrollPane() );
    JTable tab = new JTable( new Object[][]{ { 1, 2, 3 },
                                             { 4, 5, 6 },
                                             { 7, 8, 9 } }, new String[]{ "a", "b", "c" } );
    tab.getTableHeader().setReorderingAllowed( true );
    frame.add( new JScrollPane( tab ) );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}