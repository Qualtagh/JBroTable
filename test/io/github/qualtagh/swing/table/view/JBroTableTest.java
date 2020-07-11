package io.github.qualtagh.swing.table.view;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import io.github.qualtagh.swing.table.model.IModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelData;
import io.github.qualtagh.swing.table.model.ModelField;
import io.github.qualtagh.swing.table.model.ModelFieldGroup;
import io.github.qualtagh.swing.table.model.ModelRow;
import io.github.qualtagh.swing.table.model.ModelSpan;
import io.github.qualtagh.swing.table.util.MouseRobot;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.UIManager;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

public class JBroTableTest {
  private JBroTable table;
  
  @Before
  public void setUp() throws Exception {
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
    table = new JBroTable( data );
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
  
  @Test( timeout = 10000L )
  public void disappearingCell() throws Exception {
    Point start = table.getLocationOnScreen();
    MouseRobot robot = new MouseRobot();
    robot.mouseMove( start.x, start.y );
    robot.mouseMoveRelative( 120, -10 );
    robot.mousePress( InputEvent.BUTTON1_DOWN_MASK );
    robot.mouseMoveRelative( 150, 0 );
    robot.mouseRelease( InputEvent.BUTTON1_DOWN_MASK );
    robot.mouseMoveRelative( 10, 0 );
    BufferedImage bi = robot.createScreenCapture( new Rectangle( start.x, start.y - 75, 400, 75 ) );
    int actual = bi.getRGB( 220, 60 );
    int wrong = table.getTableHeader().getBackground().getRGB();
    assertFalse( wrong == actual);
  }
}