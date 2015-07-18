package org.quinto.swing.table.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelDataTest {
  private ModelData data;
  
  public ModelDataTest() {
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
  }
  
  @After
  public void tearDown() {
  }

  @Test( timeout = 1000L )
  public void getAllFieldGroupsFromBottomBreadthFirst() {
    List< Triple< String, Integer, Integer > > spans = new ArrayList< Triple< String, Integer, Integer > >();
    Iterable< IModelFieldGroup > groups = data.getAllFieldGroupsFromBottom( false );
    for ( IModelFieldGroup group : groups )
      spans.add( new Triple< String, Integer, Integer >( group.getIdentifier(), group.getColspan(), group.getRowspan() ) );
    List< Triple< String, Integer, Integer > > expected = new ArrayList< Triple< String, Integer, Integer > >();
    expected.add( new Triple< String, Integer, Integer >( "B", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "C", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "E", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "F", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "G", 1, 3 ) );
    expected.add( new Triple< String, Integer, Integer >( "J", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "K", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "M", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "N", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "A", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "D", 2, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "I", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "H", 4, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "L", 2, 1 ) );
    assertEquals( expected, spans );
  }

  @Test( timeout = 1000L )
  public void getAllFieldGroupsFromBottomDepthFirst() {
    List< Triple< String, Integer, Integer > > spans = new ArrayList< Triple< String, Integer, Integer > >();
    Iterable< IModelFieldGroup > groups = data.getAllFieldGroupsFromBottom( true );
    for ( IModelFieldGroup group : groups )
      spans.add( new Triple< String, Integer, Integer >( group.getIdentifier(), group.getColspan(), group.getRowspan() ) );
    List< Triple< String, Integer, Integer > > expected = new ArrayList< Triple< String, Integer, Integer > >();
    expected.add( new Triple< String, Integer, Integer >( "B", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "A", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "C", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "E", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "D", 2, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "F", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "G", 1, 3 ) );
    expected.add( new Triple< String, Integer, Integer >( "J", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "I", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "H", 4, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "K", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "M", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "L", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "N", 1, 1 ) );
    assertEquals( expected, spans );
  }

  @Test( timeout = 1000L )
  public void getAllFieldGroupsFromTopBreadthFirst() {
    List< Triple< String, Integer, Integer > > spans = new ArrayList< Triple< String, Integer, Integer > >();
    Iterable< IModelFieldGroup > groups = data.getAllFieldGroupsFromTop( false );
    for ( IModelFieldGroup group : groups )
      spans.add( new Triple< String, Integer, Integer >( group.getIdentifier(), group.getColspan(), group.getRowspan() ) );
    List< Triple< String, Integer, Integer > > expected = new ArrayList< Triple< String, Integer, Integer > >();
    expected.add( new Triple< String, Integer, Integer >( "A", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "D", 2, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "G", 1, 3 ) );
    expected.add( new Triple< String, Integer, Integer >( "H", 4, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "B", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "C", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "E", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "F", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "I", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "K", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "L", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "J", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "M", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "N", 1, 1 ) );
    assertEquals( expected, spans );
  }

  @Test( timeout = 1000L )
  public void getAllFieldGroupsFromTopDepthFirst() {
    List< Triple< String, Integer, Integer > > spans = new ArrayList< Triple< String, Integer, Integer > >();
    Iterable< IModelFieldGroup > groups = data.getAllFieldGroupsFromTop( true );
    for ( IModelFieldGroup group : groups )
      spans.add( new Triple< String, Integer, Integer >( group.getIdentifier(), group.getColspan(), group.getRowspan() ) );
    List< Triple< String, Integer, Integer > > expected = new ArrayList< Triple< String, Integer, Integer > >();
    expected.add( new Triple< String, Integer, Integer >( "A", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "B", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "C", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "D", 2, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "E", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "F", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "G", 1, 3 ) );
    expected.add( new Triple< String, Integer, Integer >( "H", 4, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "I", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "J", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "K", 1, 2 ) );
    expected.add( new Triple< String, Integer, Integer >( "L", 2, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "M", 1, 1 ) );
    expected.add( new Triple< String, Integer, Integer >( "N", 1, 1 ) );
    assertEquals( expected, spans );
  }

  @Test( timeout = 1000L )
  public void getHeaderString() {
    StringBuilder expected = new StringBuilder();
    expected.append( "+++++++++++++++++++++++++++++++++++++++++++++\n" );
    expected.append( "+ A      ++ D      ++ G ++ H                +\n" );
    expected.append( "+++++++++++++++++++++++++++++++++++++++++++++\n" );
    expected.append( "+ B ++ C ++ D      ++ G ++ I ++ K ++ L      +\n" );
    expected.append( "+++++++++++++++++++++++++++++++++++++++++++++\n" );
    expected.append( "+ B ++ C ++ E ++ F ++ G ++ J ++ K ++ M ++ N +\n" );
    expected.append( "+++++++++++++++++++++++++++++++++++++++++++++" );
    String actual = data.getHeaderString();
    assertEquals( actual, expected.toString(), actual );
  }
  
  @Test( timeout = 1000L )
  public void copyOfModelFields() {
    assertArrayEquals( data.getFields(), ModelField.copyOfModelFields( data.getFields() ) );
  }
  
  @Test( timeout = 1000L )
  public void readObject() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream( baos );
      try {
        oos.writeObject( data );
        oos.flush();
        byte byteData[] = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream( byteData );
        try {
          ObjectInputStream ois = new ObjectInputStream( bais );
          try {
            ModelData readData = ( ModelData )ois.readObject();
            assertArrayEquals( data.getFields(), readData.getFields() );
            assertTrue( ModelField.equals( data.getFields(), readData.getFields() ) );
            assertArrayEquals( data.getRows(), readData.getRows() );
            if ( data.getRows() != null ) {
              for ( int i = 0; i < data.getRows().length; i++ ) {
                ModelRow r1 = data.getRows()[ i ];
                ModelRow r2 = readData.getRows()[ i ];
                assertEquals( r1.getLength(), r2.getLength() );
                assertArrayEquals( r1.getValues(), r2.getValues() );
              }
            }
            assertEquals( data.getHeaderString(), readData.getHeaderString() );
            assertEquals( data.getFieldGroups().size(), readData.getFieldGroups().size() );
            if ( data.getFieldGroups() != null )
              for ( int i = 0; i < data.getFieldGroups().size(); i++ )
                assertArrayEquals( ( IModelFieldGroup[] )data.getFieldGroups().get( i ), ( IModelFieldGroup[] )readData.getFieldGroups().get( i ) );
          } finally {
            close( ois );
          }
        } finally {
          close( bais );
        }
      } finally {
        close( oos );
      }
    } finally {
      close( baos );
    }
  }
  
  private void close( Closeable c ) {
    if ( c != null ) {
      try {
        c.close();
      } catch ( IOException e ) {
      }
    }
  }
}