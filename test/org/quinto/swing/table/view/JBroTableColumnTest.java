package org.quinto.swing.table.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelRow;
import org.quinto.swing.table.model.Utils;

public class JBroTableColumnTest {
  private static final ModelField FIELDS[] = new ModelField[] {
    new ModelField().withIdentifier( "INSERT_DATE" ).withCaption( "Date" ),
    new ModelField().withIdentifier( "ID" ).withCaption( "Identifier" ),
    new ModelField().withIdentifier( "NAME" ).withCaption( "Person name" ),
    new ModelField().withIdentifier( "JOB" ).withCaption( "Job" ),
    new ModelField().withIdentifier( "ADDRESS" ).withCaption( "Address" ),
    new ModelField().withIdentifier( "AGE" ).withCaption( "Age" )
  };
  private static final Random RANDOM = new Random( System.nanoTime() );
  
  private ModelField fields[];
  private ModelData data;
  private ModelRow rows[];
  private JBroTable table;
  private JScrollPane pane;
  private JFrame frame;
  
  @BeforeClass
  public static void setClassUp() {
    Utils.initSimpleConsoleLogger();
  }
  
  public static void main( String args[] ) {
    setClassUp();
    JBroTableColumnTest test = new JBroTableColumnTest();
    test.setUp();
    test.fields[ 3 ].setManageable( false );
    test.frame.setVisible( true );
  }
  
  @Before
  public void setUp() {
    fields = new ModelField[ FIELDS.length ];
    for ( int i = 0; i < FIELDS.length; i++ )
      fields[ i ] = FIELDS[ i ].clone();
    data = new ModelData( fields );
    rows = new ModelRow[] {
      new ModelRow( "2014-12-01", 123L, "Michael", "Junior developer", "Wall st.", 23 ),
      new ModelRow( "2014-12-01", 234L, "Bill", "Lead developer", "Broadway", 40 ),
      new ModelRow( "2014-12-02", 123L, "Michael", "Middle developer", "Wall st.", 23 )
    };
    data.setRows( rows );
    table = new JBroTable( data );
   // table.setAutoCreateRowSorter( true );
    pane = new JScrollPane( table );
    frame = new JFrame();
    frame.add( pane );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( null );
    frame.setBounds( 100, 100, 500, 600 );
    pane.setBounds( 0, 0, 500, 600 );
  }
  
  @After
  public void tearDown() {
    frame.dispose();
  }

  @Test( timeout = 1000L )
  public void correctSetup() {
    assertEquals( 123L, rows[ 0 ].getValue( 1 ) );
  }

  @Test( timeout = 1000L )
  public void moveColumn() {
    table.moveColumn( 5, 4 );
    assertEquals( "Age", table.getColumnName( 4 ) );
    assertEquals( "Address", table.getColumnName( 5 ) );
    assertEquals( "Age", table.getColumnModel().getColumn( 4 ).getHeaderValue() );
    assertEquals( "Address", table.getColumnModel().getColumn( 5 ).getHeaderValue() );
    table.moveColumn( 0, 1 );
    assertEquals( "Identifier", table.getColumnName( 0 ) );
    assertEquals( "Date", table.getColumnName( 1 ) );
    table.moveColumn( 2, 3 );
    assertEquals( "Job", table.getColumnName( 2 ) );
    assertEquals( "Person name", table.getColumnName( 3 ) );
  }

  @Test( timeout = 1000L )
  public void moveColumnFar() {
    table.moveColumn( 0, 2 );
    assertEquals( "Identifier", table.getColumnName( 0 ) );
    assertEquals( "Person name", table.getColumnName( 1 ) );
    assertEquals( "Date", table.getColumnName( 2 ) );
  }

  @Test( timeout = 1000L )
  public void moveColumnRandom() {
    for ( int attempt = 0; attempt < 100; attempt++ ) {
      int from = RANDOM.nextInt( FIELDS.length );
      int to = RANDOM.nextInt( FIELDS.length );
      String message = "From " + from + " to " + to;
      table.moveColumn( from, to );
      assertEquals( message, FIELDS[ from > to ? from - 1 : from < to ? from + 1 : from ].getCaption(), table.getColumnName( from ) );
      assertEquals( message, FIELDS[ from ].getCaption(), table.getColumnName( to ) );
      for ( int i = 0; i < table.getColumnCount(); i++ )
        assertEquals( message, table.getColumnName( i ), table.getColumnModel().getColumn( i ).getHeaderValue() );
      table.moveColumn( to, from );
      for ( int i = 0; i < FIELDS.length; i++ ) {
        assertEquals( message, FIELDS[ i ].getCaption(), table.getColumnName( i ) );
        assertEquals( message, FIELDS[ i ].getCaption(), table.getColumnModel().getColumn( i ).getHeaderValue() );
      }
    }
  }
  
  @Test( timeout = 1000L )
  public void setFieldsOrder() {
    table.setFieldsOrder( "NAME;ID;" );
    TableColumnModel columnModel = table.getColumnModel();
    assertEquals( 2, columnModel.getColumnCount() );
    assertEquals( "Person name", columnModel.getColumn( 0 ).getHeaderValue() );
    assertEquals( "Identifier", columnModel.getColumn( 1 ).getHeaderValue() );
    assertEquals( "Person name", table.getColumnName( 0 ) );
    assertEquals( "Identifier", table.getColumnName( 1 ) );
    reset();
    table.setFieldsOrder( "NAME;ID;INSERT_DATE;ADDRESS;JOB;AGE;" );
    String expected[] = new String[]{ "Person name", "Identifier", "Date", "Address", "Job", "Age" };
    columnModel = table.getColumnModel();
    assertEquals( expected.length, columnModel.getColumnCount() );
    for ( int i = 0; i < expected.length; i++ ) {
      assertEquals( expected[ i ], columnModel.getColumn( i ).getHeaderValue() );
      assertEquals( expected[ i ], table.getColumnName( i ) );
    }
  }
  
  @Test( timeout = 1000L )
  public void reorderFields() {
    table.reorderFields( new String[]{ "INSERT_DATE", "ID", "ADDRESS", "JOB", "AGE" } );
    assertEquals( "INSERT_DATE;ID;ADDRESS;JOB;AGE;", table.getFieldsOrder() );
    reset();
    table.getData().getFields()[ 4 ].setManageable( false );
    table.reorderFields( new String[]{ "INSERT_DATE", "ID", "ADDRESS", "JOB", "AGE" } );
    assertEquals( "INSERT_DATE;ID;JOB;ADDRESS;AGE;", table.getFieldsOrder() );
    reset();
    table.getData().getFields()[ 4 ].setManageable( false );
    table.reorderFields( new String[]{ "INSERT_DATE", "ID", "JOB", "AGE" } );
    assertEquals( "INSERT_DATE;ID;JOB;ADDRESS;AGE;", table.getFieldsOrder() );
    reset();
    table.getData().getFields()[ 4 ].setManageable( false );
    table.reorderFields( new String[]{ "INSERT_DATE", "JOB", "AGE", "ID" } );
    assertEquals( "INSERT_DATE;JOB;ADDRESS;AGE;ID;", table.getFieldsOrder() );
  }
  
  @Test( timeout = 1000L )
  public void swapColumns() {
    ArrayList< ModelField > hidden = new ArrayList< ModelField >( Arrays.asList( fields ) );
    hidden.remove( 2 );
    ModelField fields[] = hidden.toArray( new ModelField[ hidden.size() ] );
    for ( int i = 0; i < fields.length; i++ ) {
      for ( int j = 0; j < fields.length; j++ ) {
        table.removeColumn( table.getColumnModel().getColumn( 2 ) );
        table.swapColumns( i, j );
        assertEquals( toStringSwapped( fields, i, j ), table.getFieldsOrder() );
        reset();
      }
    }
  }
  
  private String toStringSwapped( ModelField fields[], int first, int second ) {
    String names[] = new String[ fields.length ];
    for ( int i = 0; i < fields.length; i++ )
      names[ i ] = fields[ i ].getIdentifier();
    String t = names[ first ];
    names[ first ] = names[ second ];
    names[ second ] = t;
    StringBuilder sb = new StringBuilder();
    for ( String name : names )
      sb.append( name ).append( ';' );
    return sb.toString();
  }
  
  private void reset() {
    tearDown();
    setUp();
  }
}